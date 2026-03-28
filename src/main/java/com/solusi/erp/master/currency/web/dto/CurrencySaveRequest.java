package com.solusi.erp.master.currency.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CurrencySaveRequest extends BaseAuditResponse {

    @NotBlank(message = "{label.currency.symbol} {validation.notblank.suffix}")
    @Size(max = 10, message = "{label.currency.symbol} {validation.size.suffix}")
    private String symbol;

    @NotBlank(message = "{label.currency.alias} {validation.notblank.suffix}")
    @Size(max = 10, message = "{label.currency.alias} {validation.size.suffix}")
    private String alias;

    @NotBlank(message = "{label.currency.name} {validation.notblank.suffix}")
    @Size(max = 150, message = "{label.currency.name} {validation.size.suffix}")
    private String name;

    private String note;
    private Boolean isDefault;
    private Boolean isActive;
}
