package com.wsu.organization.controller;


import com.wsu.core.model.dto.requests.DepartmentRequest;
import com.wsu.core.model.dto.responses.DepartmentResponse;
import com.wsu.organization.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/department")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('MANAGE_DEPARTMENTS')")
public class DepartmentController {
    private final DepartmentService departmentService;


    @PostMapping("/add")
    public ResponseEntity<?> addDepartment(@RequestBody DepartmentRequest request){

        DepartmentResponse response = departmentService.addDepartment(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/getAll")
    public ResponseEntity<?> getAllDepartments(){
        return ResponseEntity.ok(departmentService.getAllDepartments());
    }
}
