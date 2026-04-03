package com.solusi.erp.security.user.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;

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

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getAvatarPath() { return avatarPath; }
    public void setAvatarPath(String avatarPath) { this.avatarPath = avatarPath; }
    public String getLanguageCode() { return languageCode; }
    public void setLanguageCode(String languageCode) { this.languageCode = languageCode; }
    public Integer getDefaultPageSize() { return defaultPageSize; }
    public void setDefaultPageSize(Integer defaultPageSize) { this.defaultPageSize = defaultPageSize; }
    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }
}
