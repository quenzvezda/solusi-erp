package com.solusi.erp.inventory.container.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ContainerSaveRequest extends BaseAuditResponse {

    @NotNull(message = "{label.container.grid} {validation.notblank.suffix}")
    private Long gridId;

    // code is auto-generated, readonly in form
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
    private Boolean isActive = Boolean.TRUE;
}
