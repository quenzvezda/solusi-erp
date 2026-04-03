package com.solusi.erp.security.role.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RoleSaveRequest extends BaseAuditResponse {

    @NotBlank(message = "{validation.role.name.required}")
    @Size(max = 50, message = "{validation.role.name.max}")
    private String name;

    @Size(max = 255, message = "{validation.role.description.max}")
    private String description;

    @NotEmpty(message = "{validation.role.permissions.required}")
    private Set<Long> permissionIds = new HashSet<>();
}

