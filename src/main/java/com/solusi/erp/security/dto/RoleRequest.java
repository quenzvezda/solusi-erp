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

    @NotBlank(message = "{validation.role.name.required}")
    @Size(max = 50, message = "{validation.role.name.max}")
    private String name;

    @Size(max = 255, message = "{validation.role.description.max}")
    private String description;

    @NotEmpty(message = "{validation.role.permissions.required}")
    @Builder.Default
    private Set<Long> permissionIds = new HashSet<>();
}
