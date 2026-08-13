package com.wsu.auth.service;

import com.wsu.auth.util.TempPasswordGenerator;
import com.wsu.core.Exception.ResourceNotFoundException;
import com.wsu.core.Exception.UserServiceException;
import com.wsu.core.model.dto.requests.UpdateUserRequest;
import com.wsu.core.model.dto.requests.UpdateUserStatusRequest;
import com.wsu.core.model.dto.responses.FetchUserResponse;
import com.wsu.core.model.dto.responses.UpdateUserStatusResponse;
import com.wsu.core.repository.*;
import com.wsu.core.model.dto.requests.BulkImportResult;
import com.wsu.core.model.dto.requests.CreateUserRequest;
import com.wsu.core.model.dto.responses.CreateUserResponse;
import com.wsu.core.model.entities.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class UserService {

    private static final Set<String> DEPARTMENT_SCOPED_ROLES = Set.of("HOD", "ADVISOR", "STUDENT");

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final AcademicBatchRepository academicBatchRepository;
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;

    public UserService(UserRepository userRepository,
                            RoleRepository roleRepository,
                            DepartmentRepository departmentRepository,
                            AcademicBatchRepository academicBatchRepository,
                            StudentRepository studentRepository,
                            PasswordEncoder passwordEncoder,
                           RefreshTokenRepository refreshTokenRepository
                       ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.departmentRepository = departmentRepository;
        this.academicBatchRepository = academicBatchRepository;
        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenRepository=refreshTokenRepository;
    }

    @Value("${bootstrap.secret}")
    private String secret;


    /**
     * Check if any user exists in database
     *
     * @return true if user exists else false
     */
    public boolean isUserExist(){
        return userRepository.findAll().isEmpty();
    }
    /**
     * Create an app user
     *
     * @param req request model
     * @return {@link CreateUserResponse}
     * @throws UserServiceException if any error occurs during user creation
     */
    @Transactional
    public CreateUserResponse createUser(CreateUserRequest req) {

        try {
            log.info("[createUser]: Creating user for: {} ", req);

            if (!isUserExist()){
                if (secret == null || secret.isBlank() || !req.getBootstrapSecret().equals(secret)){
                    throw new BadCredentialsException("Invalid token");
                }
            }

            validateRequest(req);

            Role role = roleRepository.findByName(req.getRoleName());

            if (role == null) {
                throw new IllegalArgumentException("Role not found: " + req.getRoleName());
            }

            Department department = null;
            if (DEPARTMENT_SCOPED_ROLES.contains(req.getRoleName())) {
                department = departmentRepository.findById(req.getDepartmentId())
                        .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + req.getDepartmentId()));
            }

            String tempPassword = req.getPassword() != null && !req.getPassword().trim().isEmpty() 
                    ? req.getPassword() 
                    : TempPasswordGenerator.generate(8);

            User user = User.builder()
                    .fullName(req.getFullName())
                    .userName(req.getUserName())
                    .email(req.getEmail())
                    .password(passwordEncoder.encode(tempPassword))
                    .role(role)
                    .department(department)
                    .mustChangePassword(true)
                    .isActive(true)
                    .build();

            User saved = userRepository.save(user);

            if ("STUDENT".equals(req.getRoleName())) {
                AcademicBatch batch = academicBatchRepository.findById(req.getBatchId())
                        .orElseThrow(() -> new IllegalArgumentException("Batch not found: " + req.getBatchId()));

                studentRepository.save(Student.builder()
                        .user(saved)
                        .batch(batch)
                        .advisor(null)   // assigned later by HoD
                        .build());
            }

            return CreateUserResponse.builder()
                    .userId(saved.getId())
                    .fullName(saved.getFullName())
                    .userName(saved.getUserName())
                    .roleName(role.getName())
                    .temporaryPassword(req.getPassword())
                    .build();
        } catch (Exception e) {
            throw new UserServiceException("Failed to create user: " + e.getMessage(), e);
        }
    }

    /**
     * Generate Temporary Password
     * @return String
     */
    public String generateTempPassword(){

        return TempPasswordGenerator.generate(8);
    }

    /**
     * Import CSV file which contains student data
     * @param csvFile csv file
     * @return {@link BulkImportResult}
     * @throws UserServiceException if any error occurs during bulk import
     */
    @Transactional
    public BulkImportResult bulkImportStudents(MultipartFile csvFile) {
        List<CreateUserResponse> created = new ArrayList<>();
        List<String> failed = new ArrayList<>();

        try (var reader = new BufferedReader(
                new InputStreamReader(csvFile.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            boolean firstLine = true;
            int rowNum = 1;

            while ((line = reader.readLine()) != null) {
                rowNum++;
                if (firstLine) { firstLine = false; continue; } // skip header
                if (line.isBlank()) continue;

                try {
                    String[] cols = line.split(",");
                    if (cols.length < 5) {
                        throw new IllegalArgumentException("Expected 5 columns, got " + cols.length);
                    }

                    CreateUserRequest req = CreateUserRequest.builder()
                            .fullName(cols[0].trim())
                            .userName(cols[1].trim())
                            .email(cols[2].trim().isEmpty() ? null : cols[2].trim())
                            .roleName("STUDENT")
                            .departmentId(Long.parseLong(cols[3].trim()))
                            .batchId(Long.parseLong(cols[4].trim()))
                            .build();

                    created.add(createUser(req));
                } catch (Exception e) {
                    failed.add("Row " + rowNum + ": " + line + " -> " + e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read CSV file", e);
        }

        return BulkImportResult.builder()
                .created(created)
                .failed(failed)
                .successCount(created.size())
                .failureCount(failed.size())
                .build();
    }

    /**
     * Change the temporary password of a user and force
     * re-login everywhere by revoking all existing refresh tokens.
     * @param userName unique username of the app user
     * @param newPassword new Password to be set for the user
     * @throws IllegalArgumentException if the user is not found
     */
    public void changePassword(String userName, String newPassword) {
        User user = userRepository.findByUserName(userName);
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + userName);
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setMustChangePassword(false);
        userRepository.save(user);

        // force re-login everywhere: kill all existing refresh tokens for this user
        refreshTokenRepository.findAll().stream()
                .filter(t -> t.getUserId().equals(user.getId()) && !t.isRevoked())
                .forEach(t -> { t.setRevoked(true); refreshTokenRepository.save(t); });
    }


    /**
     * Fetch List of users
     * @return List of {@link FetchUserResponse}
     * @throws UserServiceException if any error occurs during fetching users
     */
    public List<FetchUserResponse> getAllUsers() {
        try {
            return userRepository.findAll()
                    .stream()
                    .map(this::toResponse)
                    .toList();
        } catch (Exception e) {

            throw new UserServiceException("Failed to retrieve users: " + e.getMessage(), e);
        }

    }

    /**
     * Activate or deactivate user account status
     *
     * @param request {@link UpdateUserStatusRequest}
     * @throws ResourceNotFoundException if the user is not found
     * @throws UserServiceException if any error occurs during status change
     */
    public UpdateUserStatusResponse updateUserStatus(UpdateUserStatusRequest request, Long userId){
        try {

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

            user.setActive(request.isStatus());
            userRepository.save(user);

            return UpdateUserStatusResponse.builder()
                    .userId(user.getId())
                    .status(user.isActive())
                    .build();
        } catch (Exception e) {
            if (e instanceof ResourceNotFoundException) {
                throw e; // rethrow ResourceNotFoundException
            }
            throw new UserServiceException("Failed to change user status: " + e.getMessage(), e);
        }
    }

    /**
     * Update user information
     *
     * @param request {@link UpdateUserRequest} request model
     * @param id User Id
     * @return {@link FetchUserResponse} updated user information
     * @throws ResourceNotFoundException if the user is not found
     * @throws UserServiceException if any error occurs during user update
     */
    public FetchUserResponse updateUser(UpdateUserRequest request, Long id){
        try{
            log.info("[updateUser]: Updating user information for userId: {}", id);
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));

            user.setFullName(request.getFullName());
            user.setEmail(request.getEmail());

            User saved = userRepository.save(user);
            return toResponse(saved);
        }
        catch (Exception e) {
            if (e instanceof ResourceNotFoundException) {
                throw e; // rethrow ResourceNotFoundException
            }
            throw new UserServiceException("Failed to update user: " + e.getMessage(), e);
        }
    }

    /**
     * Fetch current logged-in user information
     * @param userId userId
     * @return {@link FetchUserResponse} current user information
     * @throws ResourceNotFoundException if the user is not found
     * @throws UserServiceException if any error occurs during fetching user information
     */
    public FetchUserResponse getCurrentUser(Long userId){
        try {
            log.info("[getCurrentUser]: Fetching profile for user :{}", userId);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
            return toResponse(user);
        } catch (Exception e) {
            if (e instanceof ResourceNotFoundException) {
                throw e; // rethrow ResourceNotFoundException
            }
            throw new UserServiceException("Failed to fetch current user: " + e.getMessage(), e);
        }
    }

    /**
     * Fetch users in a paginated manner
     *
     * @param page page number
     * @param size size
     * @return {@link Page<FetchUserResponse>} paginated user information
     * @throws UserServiceException if any error occurs during fetching users
     */
    public Page<FetchUserResponse> getUsersPaginated(int page, int size) {
        try {
            Pageable pageable= PageRequest.of(page, size, Sort.by("createdAt").descending());

            Page<User> userPage = userRepository.findAll(pageable);
            return userPage.map(this::toResponse);
        } catch (Exception e) {
            throw new UserServiceException("Failed to fetch users paginated: " + e.getMessage(), e);
        }
    }




    private void validateRequest(CreateUserRequest req) {
        if (userRepository.existsByUserName(req.getUserName())) {
            throw new IllegalArgumentException(
                    "Institutional ID already registered: " + req.getUserName());
        }

        boolean isStudent = "STUDENT".equals(req.getRoleName());
//        boolean validFormat = isStudent
//                ? UserNameValidator.isValidStudentId(req.getUserName())
//                : UserNameValidator.isValidStaffId(req.getUserName());
//
//        if (!validFormat) {
//            throw new UserServiceException(
//                    "Institutional ID format invalid for role " + req.getRoleName());
//        }

        if (DEPARTMENT_SCOPED_ROLES.contains(req.getRoleName()) && req.getDepartmentId() == null) {
            throw new UserServiceException("departmentId is required for role " + req.getRoleName());
        }

        if (isStudent && req.getBatchId() == null) {
            throw new UserServiceException("batchId is required for STUDENT role");
        }
    }



    public FetchUserResponse toResponse(User user) {
        return FetchUserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .userName(user.getUserName())
                .email(user.getEmail())
                .roleName(user.getRole().getName())
                .departmentId(user.getDepartment() != null ? user.getDepartment().getId() : null)
                .departmentName(user.getDepartment() != null ? user.getDepartment().getName() : null)
                .active(user.isActive())
                .mustChangePassword(user.isMustChangePassword())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
