package com.solusi.erp.inventory.report.application.usecase.query;

import com.solusi.erp.inventory.dto.ProductStockSummaryResponse;
import com.solusi.erp.inventory.repository.StockBalanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public class GetOnHandSummaryUseCaseImpl implements GetOnHandSummaryUseCase {

    private final StockBalanceRepository stockBalanceRepository;

    @Override
    public Page<ProductStockSummaryResponse> execute(String keyword, Pageable pageable) {
        return stockBalanceRepository.getOnHandSummary(keyword, pageable);
    }
}
