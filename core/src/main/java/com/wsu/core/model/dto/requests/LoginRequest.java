package com.wsu.core.model.dto.requests;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginRequest {

    private String userName;
    private String password;
}
