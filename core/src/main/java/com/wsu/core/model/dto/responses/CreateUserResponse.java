package com.wsu.core.model.dto.responses;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateUserResponse {
    private Long userId;
    private String fullName;
    private String userName;
    private String roleName;
    private String temporaryPassword;
}
