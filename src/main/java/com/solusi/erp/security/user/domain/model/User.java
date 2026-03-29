package com.solusi.erp.security.user.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;

import java.time.LocalDateTime;

public class User {
    private final AuditMetadata metadata;
    private String username;
    private String password;
    private String email;
    private boolean enabled;
    private boolean passwordChangeRequired;
    private LocalDateTime lastPasswordChange;
    private Long roleId;
    private String roleName;
    private String roleDescription;
    private Long partyId;
    private String partyCode;
    private String partyName;
    private UserProfile profile;

    public User(
            AuditMetadata metadata,
            String username,
            String password,
            String email,
            boolean enabled,
            boolean passwordChangeRequired,
            LocalDateTime lastPasswordChange,
            Long roleId,
            String roleName,
            String roleDescription,
            Long partyId,
            String partyCode,
            String partyName,
            UserProfile profile) {
        this.metadata = metadata;
        this.username = username;
        this.password = password;
        this.email = email;
        this.enabled = enabled;
        this.passwordChangeRequired = passwordChangeRequired;
        this.lastPasswordChange = lastPasswordChange;
        this.roleId = roleId;
        this.roleName = roleName;
        this.roleDescription = roleDescription;
        this.partyId = partyId;
        this.partyCode = partyCode;
        this.partyName = partyName;
        this.profile = profile;
    }

    public static User createNew(
            String username,
            String password,
            String email,
            Long roleId,
            UserProfile profile,
            boolean enabled,
            Long partyId) {
        return new User(
                AuditMetadata.empty(),
                username,
                password,
                email,
                enabled,
                true,
                null,
                roleId,
                null,
                null,
                partyId,
                null,
                null,
                profile);
    }

    public void updateFromForm(
            String username,
            String email,
            Long roleId,
            String fullName,
            String phoneNumber,
            Boolean enabled,
            Boolean passwordChangeRequired) {
        this.username = username;
        this.email = email;
        this.roleId = roleId;
        this.enabled = enabled == null || enabled;
        this.passwordChangeRequired = passwordChangeRequired != null && passwordChangeRequired;
        if (profile == null) {
            profile = UserProfile.createDefault(fullName, phoneNumber);
        } else {
            profile.update(fullName, phoneNumber, profile.getLanguageCode(), profile.getDefaultPageSize(), profile.getTheme());
        }
    }

    public void updatePassword(String encodedPassword, boolean requireChangeAfterReset) {
        this.password = encodedPassword;
        this.passwordChangeRequired = requireChangeAfterReset;
        this.lastPasswordChange = LocalDateTime.now();
    }

    public void setPartyReference(Long partyId, String partyCode, String partyName) {
        this.partyId = partyId;
        this.partyCode = partyCode;
        this.partyName = partyName;
    }

    public boolean isAdminUsername() {
        return "admin".equals(username);
    }

    public void toggleEnabled() {
        this.enabled = !enabled;
    }

    public Long getId() {
        return metadata.id();
    }

    public AuditMetadata getMetadata() {
        return metadata;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isPasswordChangeRequired() {
        return passwordChangeRequired;
    }

    public LocalDateTime getLastPasswordChange() {
        return lastPasswordChange;
    }

    public Long getRoleId() {
        return roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public String getRoleDescription() {
        return roleDescription;
    }

    public Long getPartyId() {
        return partyId;
    }

    public String getPartyCode() {
        return partyCode;
    }

    public String getPartyName() {
        return partyName;
    }

    public UserProfile getProfile() {
        return profile;
    }
}

