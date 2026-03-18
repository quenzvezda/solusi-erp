package com.solusi.erp.inventory.service;

import com.solusi.erp.inventory.dto.LocationStockDetailResponse;
import com.solusi.erp.inventory.dto.ProductStockSummaryResponse;
import com.solusi.erp.inventory.dto.StockCardFilter;
import com.solusi.erp.inventory.model.InventoryMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface InventoryReportService {
    Page<ProductStockSummaryResponse> getOnHandSummary(String keyword, Pageable pageable);
    List<LocationStockDetailResponse> getOnHandDetail(Long productId);
    Page<InventoryMovement> getStockCard(StockCardFilter filter, Pageable pageable);
}
