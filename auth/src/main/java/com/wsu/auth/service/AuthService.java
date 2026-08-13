package com.wsu.auth.service;


import com.wsu.auth.util.JwtUtil;
import com.wsu.core.Exception.AuthServiceException;
import com.wsu.core.Exception.UnauthorizedException;
import com.wsu.core.model.dto.requests.LoginRequest;
import com.wsu.core.model.dto.requests.RefreshRequest;
import com.wsu.core.model.dto.responses.LoginResponse;
import com.wsu.core.model.dto.responses.RefreshResponse;
import com.wsu.core.model.entities.RefreshToken;
import com.wsu.core.model.entities.User;
import com.wsu.core.repository.RefreshTokenRepository;
import com.wsu.core.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
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

    /**
     * Login
     *
     * @param request {@link LoginRequest}
     * @return {@link LoginResponse}
     * @throws BadCredentialsException if the username or password is incorrect
     * @throws IllegalStateException   if the account is deactivated
     * @throws AuthServiceException    for any unexpected errors during login
     */
    public LoginResponse login(LoginRequest request) {

        try {

            log.info("[login]: Login attempt for user: {}", request.getUserName());
            User user = userRepository.findByUserName(request.getUserName());

            if (user == null) {
                throw new BadCredentialsException("Invalid ID or password");
            }


            if (!user.isActive()) {
                throw new IllegalStateException("Account is deactivated");
            }
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                throw new BadCredentialsException("Invalid ID or password");
            }

            if (user.isMustChangePassword()) {
                return LoginResponse.passwordChangeRequired(
                        jwtUtil.generatePasswordChangeToken(user.getUserName()));
            }

            return issueTokenPair(user);
        } catch (BadCredentialsException e) {
            log.warn("[login]: Bad credentials for user {}: {}", request.getUserName(), e.getMessage());
            throw e; // rethrow the exception after logging
        } catch (UnauthorizedException | IllegalStateException e) {
            log.warn("[login]: Unauthorized: {}: {}", request.getUserName(), e.getMessage());
            throw e; // rethrow the exception after logging
        } catch (Exception e) {
            log.error("[login]: Error during login for user {}: {}", request.getUserName(), e.getMessage());
            throw new AuthServiceException("An unexpected error occurred during login", e.getMessage()
            );
        }
    }

    /**
     * Get refresh token
     *
     * @param request {@link RefreshRequest}
     * @return {@link RefreshResponse}
     * @throws BadCredentialsException if the refresh token is invalid, expired, or revoked
     * @throws IllegalStateException   if the user account is deactivated
     * @throws AuthServiceException    for any unexpected errors during token refresh
     */
    public RefreshResponse refresh(RefreshRequest request) {
        try {
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
        } catch (BadCredentialsException e) {
            log.warn("[refresh]: Bad credentials: {}", e.getMessage());
            throw e; // rethrow the exception after logging
        } catch (IllegalStateException e) {
            log.warn("[refresh]: Unauthorized: {}", e.getMessage());
            throw e; // rethrow the exception after logging
        } catch (Exception e) {
            log.error("[refresh]: Error during token refresh: {}", e.getMessage());
            throw new AuthServiceException("An unexpected error occurred during token refresh", e.getMessage());
        }
    }


    /**
     * Logout by revoking the refresh token
     *
     * @param refreshToken the refresh token to be revoked
     */
    public void logout(String refreshToken) {
        log.info("[logout]: Logging out");
        String tokenHash = hashToken(refreshToken);
        RefreshToken token = refreshTokenRepository.findByTokenHash(tokenHash);
        if (token != null) {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        }

    }

    /**
     * Change password for a user and revoke all existing refresh tokens
     *
     * @param userName    the username of the user whose password is to be changed
     * @param newPassword the new password to be set
     * @throws BadCredentialsException if the user is not found
     */
    public void changePassword(String userName, String newPassword) {

        try {
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
                    .forEach(t -> {
                        t.setRevoked(true);
                        refreshTokenRepository.save(t);
                    });
        } catch (BadCredentialsException e) {
            log.warn("[changePassword]: Bad credentials for user {}: {}", userName, e.getMessage());
            throw e; // rethrow the exception after logging
        } catch (Exception e) {
            log.error("[changePassword]: Error during password change for user {}: {}", userName, e.getMessage());
            throw new AuthServiceException("An unexpected error occurred during password change", e.getMessage());
        }
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