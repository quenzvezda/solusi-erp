package com.solusi.erp.security.user.web.dto;

public class UserUiForm {
    private String roleName;
    private String roleDescription;

    public UserUiForm() {
    }

    public UserUiForm(String roleName, String roleDescription) {
        this.roleName = roleName;
        this.roleDescription = roleDescription;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleDescription() {
        return roleDescription;
    }

    public void setRoleDescription(String roleDescription) {
        this.roleDescription = roleDescription;
    }
}
