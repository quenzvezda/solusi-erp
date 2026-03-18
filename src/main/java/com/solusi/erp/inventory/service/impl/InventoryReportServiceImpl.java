package com.solusi.erp.inventory.service.impl;

import com.solusi.erp.inventory.dto.InventoryMovementResponse;
import com.solusi.erp.inventory.dto.LocationStockDetailResponse;
import com.solusi.erp.inventory.dto.ProductStockSummaryResponse;
import com.solusi.erp.inventory.dto.StockCardFilter;
import com.solusi.erp.inventory.mapper.InventoryMovementMapper;
import com.solusi.erp.inventory.model.InventoryMovement;
import com.solusi.erp.inventory.repository.InventoryMovementRepository;
import com.solusi.erp.inventory.repository.StockBalanceRepository;
import com.solusi.erp.inventory.service.InventoryReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryReportServiceImpl implements InventoryReportService {

    private final StockBalanceRepository stockBalanceRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final InventoryMovementMapper movementMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<ProductStockSummaryResponse> getOnHandSummary(String keyword, Pageable pageable) {
        return stockBalanceRepository.getOnHandSummary(keyword, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocationStockDetailResponse> getOnHandDetail(Long productId) {
        return stockBalanceRepository.getOnHandDetail(productId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InventoryMovementResponse> getStockCard(StockCardFilter filter, Pageable pageable) {
        LocalDateTime start = filter.getStartDate() != null ? filter.getStartDate().atStartOfDay() : null;
        LocalDateTime end = filter.getEndDate() != null ? filter.getEndDate().atTime(LocalTime.MAX) : null;
        
        Page<InventoryMovement> page = inventoryMovementRepository.search(
                filter.getProductId(), 
                filter.getContainerId(), 
                start, 
                end, 
                pageable);
        
        return page.map(movementMapper::toResponse);
    }
}
