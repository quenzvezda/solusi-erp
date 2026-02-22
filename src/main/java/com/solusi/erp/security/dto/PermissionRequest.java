package com.solusi.erp.security.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionRequest {

    private Long id;

    @NotBlank(message = "Nama permission/modul wajib diisi")
    @Size(max = 50, message = "Nama maksimal 50 karakter")
    private String name;

    @Size(max = 255, message = "Deskripsi maksimal 255 karakter")
    private String description;

    // Untuk fitur Batch Generation
    private boolean isBatch = false;
    private List<String> batchActions; // Default: READ, CREATE, UPDATE, DELETE
}
