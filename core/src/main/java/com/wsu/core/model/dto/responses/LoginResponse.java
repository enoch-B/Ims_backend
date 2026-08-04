package com.wsu.core.model.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long expiresIn;
    private String role;
    private String fullName;
    private boolean requiresPasswordChange;

    public static LoginResponse passwordChangeRequired(String limitedToken) {
        return LoginResponse.builder()
                .accessToken(limitedToken)
                .tokenType("Bearer")
                .requiresPasswordChange(true)
                .build();
    }
}
