package com.wsu.auth.controller;


import com.wsu.auth.service.UserPrincipal;
import com.wsu.auth.service.UserService;
import com.wsu.core.model.dto.requests.BulkImportResult;
import com.wsu.core.model.dto.requests.CreateUserRequest;
import com.wsu.core.model.dto.requests.UpdateUserRequest;
import com.wsu.core.model.dto.requests.UpdateUserStatusRequest;
import com.wsu.core.model.dto.responses.CreateUserResponse;
import com.wsu.core.model.dto.responses.FetchUserResponse;
import com.wsu.core.model.dto.responses.UpdateUserStatusResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    @GetMapping("/list")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PostMapping("/status/{userId}")
    public ResponseEntity<?> updateUserStatus(@RequestBody UpdateUserStatusRequest request, @PathVariable Long userId) {
        UpdateUserStatusResponse response = userService.updateUserStatus(request, userId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/update/{userId}")
    public ResponseEntity<?> updateUser(@RequestBody UpdateUserRequest request, @PathVariable Long userId) {
        FetchUserResponse response = userService.updateUser(request, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public FetchUserResponse me(@AuthenticationPrincipal UserPrincipal principal) {
        return userService.getCurrentUser(principal.getUserId());
    }

    @GetMapping("/getAll")
    public ResponseEntity<?> getUserById(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20" ) int size) {
        Page<FetchUserResponse> response = userService.getUsersPaginated(page, size);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("UserController is healthy");
    }
}