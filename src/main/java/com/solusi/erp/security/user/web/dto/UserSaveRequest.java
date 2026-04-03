package com.solusi.erp.security.user.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UserSaveRequest extends BaseAuditResponse {
    @NotBlank(message = "{label.users.username} {validation.notblank.suffix}")
    @Size(min = 3, max = 50, message = "{label.users.username} {validation.size.suffix}")
    private String username;

    @NotBlank(message = "{label.users.email} {validation.notblank.suffix}")
    @Email(message = "{label.users.email} {validation.email.suffix}")
    private String email;

    private String password;

    @NotNull(message = "{label.users.role} {validation.notnull.suffix}")
    private Long roleId;

    @NotBlank(message = "{label.users.fullname} {validation.notblank.suffix}")
    @Size(max = 100, message = "{label.users.fullname} {validation.size.suffix}")
    private String fullName;

    @Size(max = 20, message = "{label.users.phone} {validation.size.suffix}")
    private String phoneNumber;

    private Boolean enabled = true;
    private Boolean passwordChangeRequired = false;
    private Long partyId;
    private String partyCode;
    private String partyName;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Long getRoleId() { return roleId; }
    public void setRoleId(Long roleId) { this.roleId = roleId; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public Boolean getPasswordChangeRequired() { return passwordChangeRequired; }
    public void setPasswordChangeRequired(Boolean passwordChangeRequired) { this.passwordChangeRequired = passwordChangeRequired; }
    public Long getPartyId() { return partyId; }
    public void setPartyId(Long partyId) { this.partyId = partyId; }
    public String getPartyCode() { return partyCode; }
    public void setPartyCode(String partyCode) { this.partyCode = partyCode; }
    public String getPartyName() { return partyName; }
    public void setPartyName(String partyName) { this.partyName = partyName; }
}
