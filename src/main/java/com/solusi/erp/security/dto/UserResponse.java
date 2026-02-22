package com.solusi.erp.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String roleName;
    private String fullName;
    private boolean enabled;
    private LocalDateTime createdDate;
    private boolean passwordChangeRequired;
}
