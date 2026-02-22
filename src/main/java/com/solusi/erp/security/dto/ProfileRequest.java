package com.solusi.erp.security.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileRequest {

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
    
    // Optional: New password if user wants to change it
    private String currentPassword;
    private String newPassword;
    private String confirmPassword;
}
