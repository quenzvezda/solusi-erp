package com.solusi.erp.accountspayable.debitmemo.web.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class DebitMemoLineResponse {
    private Long id;
    private Long purchaseReturnLineId;
    private Long productId;
    private BigDecimal quantity;
    private Long uomId;
    private BigDecimal dppAmountOriginal;
    private BigDecimal taxAmountOriginal;
    private BigDecimal dppAmountBase;
    private BigDecimal taxAmountBase;
}

