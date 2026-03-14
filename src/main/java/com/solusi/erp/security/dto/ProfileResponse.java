package com.solusi.erp.security.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ProfileResponse extends BaseAuditResponse {
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

