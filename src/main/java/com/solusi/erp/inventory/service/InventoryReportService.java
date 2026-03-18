package com.solusi.erp.inventory.service;

import com.solusi.erp.inventory.dto.InventoryMovementResponse;
import com.solusi.erp.inventory.dto.LocationStockDetailResponse;
import com.solusi.erp.inventory.dto.ProductStockSummaryResponse;
import com.solusi.erp.inventory.dto.StockCardFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface InventoryReportService {
    Page<ProductStockSummaryResponse> getOnHandSummary(String keyword, Pageable pageable);
    List<LocationStockDetailResponse> getOnHandDetail(Long productId);
    Page<InventoryMovementResponse> getStockCard(StockCardFilter filter, Pageable pageable);
}
