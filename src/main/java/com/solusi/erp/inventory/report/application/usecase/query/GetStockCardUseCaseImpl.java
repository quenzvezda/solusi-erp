package com.solusi.erp.inventory.report.application.usecase.query;

import com.solusi.erp.inventory.dto.InventoryMovementResponse;
import com.solusi.erp.inventory.dto.StockCardFilter;
import com.solusi.erp.inventory.mapper.InventoryMovementMapper;
import com.solusi.erp.inventory.model.InventoryMovement;
import com.solusi.erp.inventory.repository.InventoryMovementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.time.LocalTime;

@RequiredArgsConstructor
public class GetStockCardUseCaseImpl implements GetStockCardUseCase {

    private final InventoryMovementRepository inventoryMovementRepository;
    private final InventoryMovementMapper inventoryMovementMapper;

    @Override
    public Page<InventoryMovementResponse> execute(StockCardFilter filter, Pageable pageable) {
        LocalDateTime start = filter.getStartDate() != null ? filter.getStartDate().atStartOfDay() : null;
        LocalDateTime end = filter.getEndDate() != null ? filter.getEndDate().atTime(LocalTime.MAX) : null;
        Page<InventoryMovement> page = inventoryMovementRepository.search(
                filter.getProductId(),
                filter.getContainerId(),
                start,
                end,
                pageable);
        return page.map(inventoryMovementMapper::toResponse);
    }
}
