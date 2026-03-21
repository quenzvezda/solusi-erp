package com.solusi.erp.security.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * DTO for creating or updating a User.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserRequest extends BaseAuditResponse {

    @NotBlank(message = "Username wajib diisi")
    @Size(min = 3, max = 50, message = "Username minimal 3 dan maksimal 50 karakter")
    private String username;

    @NotBlank(message = "Email wajib diisi")
    @Email(message = "Format email tidak valid")
    private String email;

    private String password; // Optional on update, validated manually

    @NotNull(message = "Role wajib dipilih")
    private Long roleId;

    @NotBlank(message = "Nama lengkap wajib diisi")
    @Size(max = 100, message = "Nama lengkap maksimal 100 karakter")
    private String fullName;

    @Size(max = 20, message = "Nomor telepon maksimal 20 karakter")
    private String phoneNumber;

    @Builder.Default
    private Boolean enabled = true;

    @Builder.Default
    private Boolean passwordChangeRequired = false;
}
