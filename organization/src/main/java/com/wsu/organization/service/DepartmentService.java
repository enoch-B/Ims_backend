package com.wsu.organization.service;


import com.wsu.core.Exception.DepartmentServiceException;
import com.wsu.core.Exception.ResourceNotFoundException;
import com.wsu.core.model.dto.requests.DepartmentRequest;
import com.wsu.core.model.dto.responses.DepartmentResponse;
import com.wsu.core.model.entities.Department;
import com.wsu.core.repository.DepartmentRepository;
import com.wsu.core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class DepartmentService {
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    public boolean isDepartmentExists(String departmentCode) {
        return departmentRepository.existsByCode(departmentCode);
    }

    public DepartmentResponse addDepartment(DepartmentRequest request) {

        try {


            if (isDepartmentExists(request.getCode())) {
                throw new IllegalArgumentException("Department with code " + request.getCode() + " already exists.");
            }
            Department department = Department
                    .builder()
                    .name(request.getName())
                    .code(request.getCode())
                    .build();

           Department saved= departmentRepository.save(department);
            return DepartmentResponse.builder()
                    .id(saved.getId())
                    .name(saved.getName())
                    .code(saved.getCode())
                    .hod(saved.getHod()!= null ? saved.getHod().getId() : null)
                    .build();
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                throw e; // rethrow the exception to be handled by the controller
            }
            throw new DepartmentServiceException("Failed to add department: ");
        }
    }

    public List<DepartmentResponse> getAllDepartments() {

        try {
            List<Department> departments = departmentRepository.findAll();
            return departments.stream().map(department -> DepartmentResponse.builder()
                    .id(department.getId())
                    .name(department.getName())
                    .code(department.getCode())
                    .hod(department.getHod() != null ? department.getHod().getId() : null)
                    .build()).toList();
        } catch (Exception e) {
            throw new DepartmentServiceException("Failed to retrieve departments: " + e.getMessage(), e);
        }
    }

    /**
     * Update department information
     *
     * @param request {@link DepartmentRequest}
     * @param id department id
     * @return {@link DepartmentResponse}
     * @throws ResourceNotFoundException if department with given id is not found
     * @throws DepartmentServiceException if any other error occurs during the update process
     */
    public  DepartmentResponse updateDepartment( Long id, DepartmentRequest request){
        try{
            log.info("[updateDepartment] Updating department with id {}: {}", id, request);

            Department department=departmentRepository.findById(id)
                    .orElseThrow(()->new ResourceNotFoundException("Department with id "+id+" not found"));
            log.info("[updateDepartment] Found department: {}", department);

            department.setName(request.getName() != null ? request.getName() : department.getName());
            department.setCode(request.getCode() !=null ? request.getCode() : department.getCode());

            Department updated = departmentRepository.save(department);

            return DepartmentResponse.builder()
                    .id(updated.getId())
                    .name(updated.getName())
                    .code(updated.getCode())
                    .hod(updated.getId())
                    .build();

        }catch (ResourceNotFoundException e){
            throw e;
        }catch (Exception e){
            throw new DepartmentServiceException("Failed to update department: " + e.getMessage());
        }
    }


    /**
     * Delete department by id
     * @param id department id
     *
     */
    public void deleteDepartment(Long id){
        try{
            Department department=departmentRepository.findById(id)
                    .orElseThrow(()->new ResourceNotFoundException("Department with id "+id+" not found"));
            departmentRepository.delete(department);
        }catch (ResourceNotFoundException e){
            throw e;
        }catch (Exception e){
            throw new DepartmentServiceException("Failed to delete department: " + e.getMessage());
        }
    }

}
