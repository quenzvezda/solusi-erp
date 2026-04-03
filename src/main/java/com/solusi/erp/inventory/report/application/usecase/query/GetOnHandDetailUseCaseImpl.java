package com.solusi.erp.inventory.report.application.usecase.query;

import com.solusi.erp.inventory.report.web.dto.LocationStockDetailResponse;
import com.solusi.erp.inventory.stock.infrastructure.persistence.StockBalanceJpaRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class GetOnHandDetailUseCaseImpl implements GetOnHandDetailUseCase {

    private final StockBalanceJpaRepository stockBalanceRepository;

    @Override
    public List<LocationStockDetailResponse> execute(Long productId) {
        return stockBalanceRepository.getOnHandDetail(productId);
    }
}
