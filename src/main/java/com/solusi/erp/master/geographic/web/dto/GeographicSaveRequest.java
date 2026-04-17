package com.solusi.erp.master.geographic.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import com.solusi.erp.master.shared.model.GeographicType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Intent-Based DTO for Saving (Create/Update) Geographic.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class GeographicSaveRequest extends BaseAuditResponse {

    @NotBlank(message = "{label.geographic.code} {validation.notblank.suffix}")
    @Size(max = 50, message = "{label.geographic.code} {validation.size.suffix}")
    private String code;

    @NotBlank(message = "{label.geographic.name} {validation.notblank.suffix}")
    @Size(max = 150, message = "{label.geographic.name} {validation.size.suffix}")
    private String name;

    @NotNull(message = "{label.geographic.type} {validation.notblank.suffix}")
    private GeographicType type;

    private Long parentId;

    private String parentName;

    private String parentSubtext;

    private Boolean isActive = Boolean.TRUE;
}

