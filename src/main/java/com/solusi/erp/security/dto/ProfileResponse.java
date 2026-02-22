package com.solusi.erp.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponse {
    private String username;
    private String email;
    private String roleName;
    private String fullName;
    private String phoneNumber;
    private String avatarPath;
    private String languageCode;
    private Integer defaultPageSize;
    private String theme;
}
