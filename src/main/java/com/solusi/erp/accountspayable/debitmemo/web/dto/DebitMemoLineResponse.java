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
    private String productName;
    private String productCode;
    private BigDecimal quantity;
    private Long uomId;
    private String uomName;
    private String uomCode;
    private BigDecimal dppAmountOriginal;
    private BigDecimal taxAmountOriginal;
    private BigDecimal dppAmountBase;
    private BigDecimal taxAmountBase;
}

