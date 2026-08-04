package com.wsu.auth.controller;

import com.wsu.auth.service.AuthService;
import com.wsu.core.model.dto.requests.ChangePasswordRequest;
import com.wsu.core.model.dto.requests.LoginRequest;
import com.wsu.core.model.dto.requests.RefreshRequest;
import com.wsu.core.model.dto.responses.LoginResponse;
import com.wsu.core.model.dto.responses.RefreshResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    public RefreshResponse refresh(@RequestBody RefreshRequest request) {
        return authService.refresh(request);
    }

    @PostMapping("/logout")
    public void logout(@RequestBody RefreshRequest request) {
        authService.logout(request.getRefreshToken());
    }

    @PostMapping("/change-password")
    public void changePassword(@RequestBody ChangePasswordRequest request, Authentication auth) {
        authService.changePassword(auth.getName(), request.getNewPassword());
    }
}