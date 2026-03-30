package com.solusi.erp.inventory.report.application.usecase.query;

import com.solusi.erp.inventory.report.web.dto.InventoryMovementResponse;
import com.solusi.erp.inventory.report.web.dto.StockCardFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@FunctionalInterface
public interface GetStockCardUseCase {
    Page<InventoryMovementResponse> execute(StockCardFilter filter, Pageable pageable);
}
