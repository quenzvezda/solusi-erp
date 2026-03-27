package com.solusi.erp.inventory.report.application.usecase.query;

import com.solusi.erp.inventory.dto.LocationStockDetailResponse;
import com.solusi.erp.inventory.repository.StockBalanceRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class GetOnHandDetailUseCaseImpl implements GetOnHandDetailUseCase {

    private final StockBalanceRepository stockBalanceRepository;

    @Override
    public List<LocationStockDetailResponse> execute(Long productId) {
        return stockBalanceRepository.getOnHandDetail(productId);
    }
}
