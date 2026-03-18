package com.solusi.erp.inventory.service;

import com.solusi.erp.inventory.dto.StockAdjustmentRequest;
import com.solusi.erp.inventory.dto.StockAdjustmentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StockAdjustmentService {
    Page<StockAdjustmentResponse> findAll(String keyword, Pageable pageable);
    StockAdjustmentResponse findById(Long id);
    StockAdjustmentResponse create(StockAdjustmentRequest request);
    StockAdjustmentResponse update(Long id, StockAdjustmentRequest request);
    void delete(Long id);
    void process(Long id);
}
