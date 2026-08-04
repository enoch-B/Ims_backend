package com.wsu.auth.service;

import com.wsu.auth.util.TempPasswordGenerator;
import com.wsu.auth.util.UserNameValidator;
import com.wsu.core.Exception.ResourceNotFoundException;
import com.wsu.core.Exception.UserServiceException;
import com.wsu.core.repository.*;
import com.wsu.core.model.dto.requests.BulkImportResult;
import com.wsu.core.model.dto.requests.CreateUserRequest;
import com.wsu.core.model.dto.responses.CreateUserResponse;
import com.wsu.core.model.entities.*;
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

    @Transactional
    public CreateUserResponse createUser(CreateUserRequest req) {
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

        String tempPassword = TempPasswordGenerator.generate(8);

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
                    .userId(saved.getId())
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
                .temporaryPassword(tempPassword)
                .build();
    }


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
}
