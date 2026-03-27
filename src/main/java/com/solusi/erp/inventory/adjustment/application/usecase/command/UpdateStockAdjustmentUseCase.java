package com.solusi.erp.inventory.adjustment.application.usecase.command;

import com.solusi.erp.inventory.adjustment.domain.model.StockAdjustment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@FunctionalInterface
public interface UpdateStockAdjustmentUseCase {
    StockAdjustment execute(Long id, LocalDate transactionDate, String note, Long facilityId,
                            Long currencyId, BigDecimal exchangeRate, List<LineCommand> lines);
}
