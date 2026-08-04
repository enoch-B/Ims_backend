package com.wsu.auth.controller;


import com.wsu.auth.service.UserService;
import com.wsu.core.model.dto.requests.BulkImportResult;
import com.wsu.core.model.dto.requests.CreateUserRequest;
import com.wsu.core.model.dto.responses.CreateUserResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasAuthority('MANAGE_USERS')")
public class UserController {

    private final UserService userService;

    public UserController(UserService adminUserService) {
        this.userService = adminUserService;
    }

    @PostMapping(value = "/create", consumes = "application/json")
    public CreateUserResponse createUser(@RequestBody CreateUserRequest request) {
        return userService.createUser(request);
    }

    @PostMapping(value = "/bulk-import-students", consumes = "multipart/form-data")
    public BulkImportResult bulkImportStudents(@RequestParam("file") MultipartFile csvFile) {
        return userService.bulkImportStudents(csvFile);
    }
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("UserController is healthy");
    }
}