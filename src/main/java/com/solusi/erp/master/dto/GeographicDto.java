package com.solusi.erp.master.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import com.solusi.erp.master.model.GeographicType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO for {@link com.solusi.erp.master.model.Geographic}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class GeographicDto extends BaseAuditResponse implements Serializable {

    @NotBlank(message = "{validation.geographic.code.notblank}")
    @Size(max = 50, message = "{validation.geographic.code.size}")
    private String code;

    @NotBlank(message = "{validation.geographic.name.notblank}")
    @Size(max = 150, message = "{validation.geographic.name.size}")
    private String name;

    @NotNull(message = "{validation.geographic.type.notnull}")
    private GeographicType type;

    private Long parentId;
    private String parentName;

    private Boolean isActive = true;
}

