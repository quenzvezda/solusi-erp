package com.solusi.erp.inventory.adjustment.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import com.solusi.erp.inventory.adjustment.domain.model.AdjustmentStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Summary response DTO for StockAdjustment list view.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class StockAdjustmentSummaryResponse extends BaseAuditResponse {
    private String code;
    private LocalDate transactionDate;
    private AdjustmentStatus status;
    private String facilityName;
    private BigDecimal totalAmountLocal;
    private String currencyAlias;
    private String note;
}
