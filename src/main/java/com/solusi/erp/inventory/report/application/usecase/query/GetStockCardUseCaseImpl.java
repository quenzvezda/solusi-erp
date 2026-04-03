package com.solusi.erp.inventory.report.application.usecase.query;

import com.solusi.erp.inventory.report.web.dto.InventoryMovementResponse;
import com.solusi.erp.inventory.report.web.dto.StockCardFilter;
import com.solusi.erp.inventory.report.web.mapper.InventoryMovementMapper;
import com.solusi.erp.inventory.stock.infrastructure.persistence.InventoryMovementEntity;
import com.solusi.erp.inventory.stock.infrastructure.persistence.InventoryMovementJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.time.LocalTime;

@RequiredArgsConstructor
public class GetStockCardUseCaseImpl implements GetStockCardUseCase {

    private final InventoryMovementJpaRepository inventoryMovementRepository;
    private final InventoryMovementMapper inventoryMovementMapper;

    @Override
    public Page<InventoryMovementResponse> execute(StockCardFilter filter, Pageable pageable) {
        LocalDateTime start = filter.getStartDate() != null ? filter.getStartDate().atStartOfDay() : null;
        LocalDateTime end = filter.getEndDate() != null ? filter.getEndDate().atTime(LocalTime.MAX) : null;
        Page<InventoryMovementEntity> page = inventoryMovementRepository.search(
                filter.getProductId(),
                filter.getContainerId(),
                start,
                end,
                pageable);
        return page.map(inventoryMovementMapper::toResponse);
    }
}
