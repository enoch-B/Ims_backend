package com.wsu.organization.service;


import com.wsu.core.Exception.DepartmentServiceException;
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

            departmentRepository.save(department);
            return DepartmentResponse.builder()
                    .id(department.getId())
                    .name(department.getName())
                    .code(department.getCode())
                    .hod(department.getHod().getId())
                    .build();
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                throw e; // rethrow the exception to be handled by the controller
            }
            throw new DepartmentServiceException("Failed to add department: " + e.getMessage(), e);
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
}
