package com.solusi.erp.security.user.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ProfileSaveRequest extends BaseAuditResponse {
    @NotBlank(message = "Nama lengkap wajib diisi")
    @Size(max = 100, message = "Nama lengkap maksimal 100 karakter")
    private String fullName;

    @NotBlank(message = "Email wajib diisi")
    @Email(message = "Format email tidak valid")
    private String email;

    @Size(max = 20, message = "Nomor telepon maksimal 20 karakter")
    private String phoneNumber;

    @NotBlank(message = "Kode bahasa wajib diisi")
    @Size(min = 2, max = 5, message = "Kode bahasa tidak valid")
    private String languageCode;

    @NotNull(message = "Ukuran halaman default wajib diisi")
    private Integer defaultPageSize;

    @NotBlank(message = "Tema wajib dipilih")
    private String theme;

    private String currentPassword;
    private String newPassword;
    private String confirmPassword;

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getLanguageCode() { return languageCode; }
    public void setLanguageCode(String languageCode) { this.languageCode = languageCode; }
    public Integer getDefaultPageSize() { return defaultPageSize; }
    public void setDefaultPageSize(Integer defaultPageSize) { this.defaultPageSize = defaultPageSize; }
    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }
    public String getCurrentPassword() { return currentPassword; }
    public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
}
