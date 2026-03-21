package com.solusi.erp.inventory.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * Request DTO for Container.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ContainerRequest extends BaseAuditResponse {

    @NotNull(message = "{label.container.grid} {validation.notnull.suffix}")
    private Long gridId;

    @Size(max = 50, message = "{label.container.code} {validation.size.suffix}")
    private String code;

    @NotBlank(message = "{label.container.name} {validation.notblank.suffix}")
    @Size(max = 150, message = "{label.container.name} {validation.size.suffix}")
    private String name;

    @Size(max = 100, message = "{label.container.barcode} {validation.size.suffix}")
    private String barcode;

    private BigDecimal length;
    private BigDecimal width;
    private BigDecimal height;
    private BigDecimal maxWeight;

    private String note;

    @Builder.Default
    private Boolean isActive = true;
}
