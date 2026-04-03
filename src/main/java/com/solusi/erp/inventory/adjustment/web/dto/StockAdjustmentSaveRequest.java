package com.solusi.erp.inventory.adjustment.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import com.solusi.erp.inventory.adjustment.domain.model.AdjustmentStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
 * Save/Edit request DTO for StockAdjustment.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class StockAdjustmentSaveRequest extends BaseAuditResponse {

    private String code;

    @NotNull(message = "{label.stock-adjustment.date} {validation.notnull.suffix}")
    @Builder.Default
    private LocalDate transactionDate = LocalDate.now();

    private String note;

    @NotNull(message = "{label.stock-adjustment.currency} {validation.notnull.suffix}")
    private Long currencyId;

    @NotNull(message = "{label.facility} {validation.notnull.suffix}")
    private Long facilityId;

    private String facilityName;
    private String facilityCode;

    @NotNull(message = "{label.stock-adjustment.rate} {validation.notnull.suffix}")
    private BigDecimal exchangeRate;

    private AdjustmentStatus status;

    @NotEmpty(message = "{label.stock-adjustment.lines} {validation.notempty.suffix}")
    @Valid
    @Builder.Default
    private List<StockAdjustmentSaveLineRequest> lines = new ArrayList<>();
}
