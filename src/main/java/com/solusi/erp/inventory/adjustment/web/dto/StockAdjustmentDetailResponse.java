package com.solusi.erp.inventory.adjustment.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import com.solusi.erp.inventory.adjustment.domain.model.AdjustmentStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Detail response DTO for StockAdjustment view page.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class StockAdjustmentDetailResponse extends BaseAuditResponse {
    private String code;
    private LocalDate transactionDate;
    private AdjustmentStatus status;
    private String note;
    private Long facilityId;
    private String facilityName;
    private Long currencyId;
    private String currencyAlias;
    private BigDecimal exchangeRate;
    private BigDecimal totalAmountOriginal;
    private BigDecimal totalAmountLocal;

    private List<StockAdjustmentLineDetailResponse> lines;
}
