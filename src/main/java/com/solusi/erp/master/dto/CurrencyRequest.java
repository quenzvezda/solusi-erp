package com.solusi.erp.master.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * DTO for creating or updating a Currency.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CurrencyRequest extends BaseAuditResponse {

    @NotBlank(message = "{currency.symbol.required}")
    @Size(max = 10, message = "{currency.symbol.max}")
    private String symbol;

    @NotBlank(message = "{currency.alias.required}")
    @Size(max = 10, message = "{currency.alias.max}")
    private String alias;

    @NotBlank(message = "{currency.name.required}")
    @Size(max = 150, message = "{currency.name.max}")
    private String name;

    private String note;

    private Boolean isActive;

    private Boolean isDefault;
}
