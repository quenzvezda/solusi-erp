package com.solusi.erp.accountspayable.debitmemoallocation.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EligibleDebitMemoSelectorRow {
    private Long id;
    private String code;
    private BigDecimal grossAmountOriginal;
    private BigDecimal remainingAmountOriginal;
}
