package com.wsu.core.model.dto.requests;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateUserRequest {
    private String fullName;
    private String userName;
    private String email;
    private String roleName;
    private Long departmentId;
    private Long batchId;
    private String password;

}
