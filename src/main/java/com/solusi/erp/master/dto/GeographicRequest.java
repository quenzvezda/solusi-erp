package com.solusi.erp.master.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import com.solusi.erp.master.model.GeographicType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO for creating or updating a Geographic entity.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class GeographicRequest extends BaseAuditResponse implements Serializable {

    @NotBlank(message = "{label.geographic.code} {validation.notblank.suffix}")
    @Size(max = 50, message = "{label.geographic.code} {validation.size.suffix}")
    private String code;

    @NotBlank(message = "{label.geographic.name} {validation.notblank.suffix}")
    @Size(max = 150, message = "{label.geographic.name} {validation.size.suffix}")
    private String name;

    @NotNull(message = "{label.geographic.type} {validation.notnull.suffix}")
    private GeographicType type;

    private Long parentId;

    @Builder.Default
    private Boolean isActive = true;
}
