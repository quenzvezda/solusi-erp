package com.solusi.erp.inventory.report.application.usecase.query;

import com.solusi.erp.inventory.dto.ProductStockSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@FunctionalInterface
public interface GetOnHandSummaryUseCase {
    Page<ProductStockSummaryResponse> execute(String keyword, Pageable pageable);
}
