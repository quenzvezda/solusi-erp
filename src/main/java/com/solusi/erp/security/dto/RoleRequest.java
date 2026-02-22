package com.solusi.erp.security.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleRequest {
    
    private Long id;

    @NotBlank(message = "Nama role wajib diisi")
    @Size(max = 50, message = "Nama role maksimal 50 karakter")
    private String name;

    @Size(max = 255, message = "Deskripsi maksimal 255 karakter")
    private String description;

    @NotEmpty(message = "Minimal pilih satu permission")
    private Set<Long> permissionIds = new HashSet<>();
}
