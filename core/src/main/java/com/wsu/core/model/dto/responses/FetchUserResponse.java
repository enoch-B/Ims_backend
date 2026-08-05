package com.wsu.core.model.dto.responses;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FetchUserResponse {

        private Long id;
        private String fullName;
        private String userName;
        private String email;
        private String roleName;
        private Long departmentId;
        private String departmentName;
        private boolean active;
        private boolean mustChangePassword;
        private LocalDateTime createdAt;
}
