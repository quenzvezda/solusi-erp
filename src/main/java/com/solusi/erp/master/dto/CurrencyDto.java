package com.solusi.erp.master.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CurrencyDto {

    private Long id;

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

    private Integer version;
}
