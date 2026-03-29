package com.solusi.erp.security.permission.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class PermissionSaveRequest {
    private Long id;

    @NotBlank(message = "{validation.permission.name.required}")
    @Size(max = 50, message = "{validation.permission.name.max}")
    private String name;

    @Size(max = 255, message = "{validation.permission.description.max}")
    private String description;

    private Long permissionGroupId;

    private List<String> batchActions;
}
