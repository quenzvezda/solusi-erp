package com.solusi.erp.master.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TaxDto {
    private Long id;

    @NotBlank(message = "{tax.code.required}")
    private String code;

    @NotBlank(message = "{tax.name.required}")
    private String name;

    @NotNull(message = "{tax.rate.required}")
    @DecimalMin(value = "0.0", message = "{tax.rate.min}")
    private BigDecimal rate;

    private String note;

    private Boolean isSubtract;

    private Boolean isActive;

    private Integer version;
}
