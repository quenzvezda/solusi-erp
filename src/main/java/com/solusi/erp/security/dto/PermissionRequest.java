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

    @NotBlank(message = "{validation.permission.name.required}")
    @Size(max = 50, message = "{validation.permission.name.max}")
    private String name;

    @Size(max = 255, message = "{validation.permission.description.max}")
    private String description;

    // Untuk fitur Batch Generation
    @Builder.Default
    private boolean isBatch = false;
    private List<String> batchActions; // Default: READ, CREATE, UPDATE, DELETE
}
