package com.solusi.erp.inventory.report.application.usecase.query;

import com.solusi.erp.inventory.dto.LocationStockDetailResponse;

import java.util.List;

@FunctionalInterface
public interface GetOnHandDetailUseCase {
    List<LocationStockDetailResponse> execute(Long productId);
}
