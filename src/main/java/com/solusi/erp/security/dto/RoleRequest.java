package com.solusi.erp.security.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

/**
 * DTO for creating or updating a Role.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RoleRequest extends BaseAuditResponse {

    @NotBlank(message = "{validation.role.name.required}")
    @Size(max = 50, message = "{validation.role.name.max}")
    private String name;

    @Size(max = 255, message = "{validation.role.description.max}")
    private String description;

    @NotEmpty(message = "{validation.role.permissions.required}")
    @Builder.Default
    private Set<Long> permissionIds = new HashSet<>();
}
