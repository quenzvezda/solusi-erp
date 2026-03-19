package com.solusi.erp.inventory.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import com.solusi.erp.inventory.model.StockAdjustment.AdjustmentStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Request DTO for creating/updating Stock Adjustment.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class StockAdjustmentRequest extends BaseAuditResponse {

    private String code; // Read-only in UI

    @NotNull(message = "{validation.notnull}")
    @Builder.Default
    private LocalDate transactionDate = LocalDate.now();

    private String note;

    @NotNull(message = "{validation.notnull}")
    private Long currencyId;

    @NotNull(message = "{validation.notnull}")
    private Long facilityId;
    
    private String facilityName; // For display in Edit mode

    @NotNull(message = "{validation.notnull}")
    private java.math.BigDecimal exchangeRate;

    private AdjustmentStatus status;

    @NotEmpty(message = "{validation.notempty}")
    @Valid
    @Builder.Default
    private List<StockAdjustmentLineRequest> lines = new ArrayList<>();
}
