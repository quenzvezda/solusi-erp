package com.solusi.erp.inventory.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import com.solusi.erp.inventory.model.StockAdjustment.AdjustmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Response DTO for Stock Adjustment.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class StockAdjustmentResponse extends BaseAuditResponse {
    private String code;
    private LocalDate transactionDate;
    private AdjustmentStatus status;
    private String note;
    
    private Long currencyId;
    private String currencyAlias;
    private BigDecimal exchangeRate;
    private BigDecimal totalAmountOriginal;
    private BigDecimal totalAmountLocal;

    @Builder.Default
    private List<StockAdjustmentLineResponse> lines = new ArrayList<>();
}
