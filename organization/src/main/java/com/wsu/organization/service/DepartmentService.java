package com.wsu.organization.service;


import com.wsu.core.Exception.ResourceNotFoundException;
import com.wsu.core.model.dto.requests.DepartmentRequest;
import com.wsu.core.model.dto.responses.DepartmentResponse;
import com.wsu.core.model.entities.Department;
import com.wsu.core.model.entities.User;
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

    public DepartmentResponse addDepartment(DepartmentRequest request){

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
    }

    public List<DepartmentResponse> getAllDepartments() {
        List<Department> departments = departmentRepository.findAll();
        return departments.stream().map(department -> DepartmentResponse.builder()
                .id(department.getId())
                .name(department.getName())
                .code(department.getCode())
                .hod(department.getHod() != null ? department.getHod().getId() : null)
                .build()).toList();
    }
}
