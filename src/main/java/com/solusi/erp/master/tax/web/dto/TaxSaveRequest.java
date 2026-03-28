package com.solusi.erp.master.tax.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import jakarta.validation.constraints.DecimalMin;
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
public class TaxSaveRequest extends BaseAuditResponse {

    @NotBlank(message = "{label.tax.code} {validation.notblank.suffix}")
    @Size(max = 50, message = "{label.tax.code} {validation.size.suffix}")
    private String code;

    @NotBlank(message = "{label.tax.name} {validation.notblank.suffix}")
    @Size(max = 150, message = "{label.tax.name} {validation.size.suffix}")
    private String name;

    @NotNull(message = "{label.tax.rate} {validation.notnull.suffix}")
    @DecimalMin(value = "0.0", message = "{label.tax.rate} {validation.min.suffix}")
    private BigDecimal rate;

    private String note;
    private Boolean isSubtract;
    private Boolean isActive;
}
