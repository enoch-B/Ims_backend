package com.wsu.auth.service;


import com.wsu.auth.util.JwtUtil;
import com.wsu.core.Repository.RefreshTokenRepository;
import com.wsu.core.Repository.UserRepository;
import com.wsu.core.model.dto.requests.LoginRequest;
import com.wsu.core.model.dto.requests.RefreshRequest;
import com.wsu.core.model.dto.responses.LoginResponse;
import com.wsu.core.model.dto.responses.RefreshResponse;
import com.wsu.core.model.entities.RefreshToken;
import com.wsu.core.model.entities.User;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUserName(request.getUserName());

        if (user == null) {
            throw new BadCredentialsException("Invalid ID or password");
        }


        if (!user.isActive()) {
            throw new IllegalStateException("Account is deactivated");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getUserName())) {
            throw new BadCredentialsException("Invalid ID or password");
        }

        if (user.isMustChangePassword()) {
            return LoginResponse.passwordChangeRequired(
                    jwtUtil.generatePasswordChangeToken(user.getUserName()));
        }

        return issueTokenPair(user);
    }

    public RefreshResponse refresh(RefreshRequest request) {
        String rawToken = request.getRefreshToken();

        if (!"refresh".equals(jwtUtil.extractType(rawToken))) {
            throw new BadCredentialsException("Invalid token type");
        }

        String tokenHash = hashToken(rawToken);
        RefreshToken stored = refreshTokenRepository.findByTokenHash(tokenHash);
        if (stored == null) {
            throw new BadCredentialsException("Refresh token not found");
        }

        if (stored.isRevoked() || stored.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadCredentialsException("Refresh token expired or revoked");
        }

        User user = userRepository.findById(stored.getUserId())
                .orElseThrow(() -> new BadCredentialsException("User no longer exists"));

        if (!user.isActive()) {
            throw new IllegalStateException("Account is deactivated");
        }

        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        LoginResponse pair = issueTokenPair(user);
        return RefreshResponse.builder()
                .accessToken(pair.getAccessToken())
                .refreshToken(pair.getRefreshToken())
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getAccessExpirationSeconds())
                .build();
    }


    public void logout(String refreshToken) {
        String tokenHash = hashToken(refreshToken);
      RefreshToken token=  refreshTokenRepository.findByTokenHash(tokenHash);
      if (token != null) {
          token.setRevoked(true);
          refreshTokenRepository.save(token);
      }

    }

    public void changePassword(String userName, String newPassword) {
        User user = userRepository.findByUserName(userName);

        if (user == null) {
            throw new BadCredentialsException("User not found");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setMustChangePassword(false);
        userRepository.save(user);

        // force re-login everywhere: kill all existing refresh tokens for this user
        refreshTokenRepository.findAll().stream()
                .filter(t -> t.getUserId().equals(user.getId()) && !t.isRevoked())
                .forEach(t -> { t.setRevoked(true); refreshTokenRepository.save(t); });
    }

    private LoginResponse issueTokenPair(User user) {
        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        refreshTokenRepository.save(RefreshToken.builder()
                .userId(user.getId())
                .tokenHash(hashToken(refreshToken))
                .expiresAt(LocalDateTime.now().plusDays(7))
                .revoked(false)
                .build());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getAccessExpirationSeconds())
                .role(user.getRole().getName())
                .fullName(user.getFullName())
                .requiresPasswordChange(false)
                .build();
    }

    private String hashToken(String token) {
        return DigestUtils.sha256Hex(token);
    }
}