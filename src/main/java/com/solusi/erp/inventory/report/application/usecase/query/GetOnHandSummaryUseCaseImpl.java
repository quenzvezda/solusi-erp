package com.solusi.erp.inventory.report.application.usecase.query;

import com.solusi.erp.inventory.report.web.dto.ProductStockSummaryResponse;
import com.solusi.erp.inventory.stock.infrastructure.persistence.StockBalanceJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public class GetOnHandSummaryUseCaseImpl implements GetOnHandSummaryUseCase {

    private final StockBalanceJpaRepository stockBalanceRepository;

    @Override
    public Page<ProductStockSummaryResponse> execute(String keyword, Pageable pageable) {
        return stockBalanceRepository.getOnHandSummary(keyword, pageable);
    }
}
