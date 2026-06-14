package com.solusi.erp.inventory.stock.infrastructure.service;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.core.model.CurrencyAmount;
import com.solusi.erp.inventory.container.infrastructure.persistence.ContainerEntity;
import com.solusi.erp.inventory.container.infrastructure.persistence.ContainerJpaRepository;
import com.solusi.erp.inventory.grid.infrastructure.persistence.GridEntity;
import com.solusi.erp.inventory.grid.infrastructure.persistence.GridJpaRepository;
import com.solusi.erp.inventory.stock.application.dto.StockMovementPayload;
import com.solusi.erp.inventory.stock.domain.model.MovementType;
import com.solusi.erp.inventory.stock.domain.model.StockBalance;
import com.solusi.erp.inventory.stock.domain.model.StockMovementReversalRequest;
import com.solusi.erp.inventory.stock.domain.port.StockMovementReversalService;
import com.solusi.erp.inventory.stock.domain.port.StockService;
import com.solusi.erp.inventory.stock.domain.repository.StockBalanceRepository;
import com.solusi.erp.inventory.stock.infrastructure.persistence.InventoryMovementEntity;
import com.solusi.erp.inventory.stock.infrastructure.persistence.InventoryMovementJpaRepository;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.util.List;

public class StockMovementReversalServiceImpl implements StockMovementReversalService {

    private final InventoryMovementJpaRepository movementRepository;
    private final ContainerJpaRepository containerRepository;
    private final GridJpaRepository gridRepository;
    private final StockBalanceRepository stockBalanceRepository;
    private final StockService stockService;

    public StockMovementReversalServiceImpl(InventoryMovementJpaRepository movementRepository,
                                            ContainerJpaRepository containerRepository,
                                            GridJpaRepository gridRepository,
                                            StockBalanceRepository stockBalanceRepository,
                                            StockService stockService) {
        this.movementRepository = movementRepository;
        this.containerRepository = containerRepository;
        this.gridRepository = gridRepository;
        this.stockBalanceRepository = stockBalanceRepository;
        this.stockService = stockService;
    }

    @Override
    public void reverse(List<StockMovementReversalRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new DomainException("msg.error.stock.reversal.requests.required");
        }
        for (StockMovementReversalRequest request : requests) {
            reverseOne(request);
        }
    }

    private void reverseOne(StockMovementReversalRequest request) {
        if (request == null || request.originalMovementId() == null) {
            throw new DomainException("msg.error.stock.reversal.original.required");
        }
        if (request.targetContainerId() == null) {
            throw new DomainException("msg.error.stock.reversal.target.container.required");
        }
        if (request.reversalDate() == null) {
            throw new DomainException("msg.error.stock.reversal.date.required");
        }

        InventoryMovementEntity original = movementRepository.findById(request.originalMovementId())
                .orElseThrow(() -> new DomainException("msg.error.stock.reversal.original.not.found"));
        validateOriginal(original);
        if (movementRepository.existsByReversalOfMovementId(original.getId())) {
            throw new DomainException("msg.error.stock.reversal.already.reversed");
        }

        ContainerEntity targetContainer = containerRepository.findById(request.targetContainerId())
                .orElseThrow(() -> new DomainException("msg.error.stock.reversal.target.container.not.found"));
        if (!Boolean.TRUE.equals(targetContainer.getIsActive())) {
            throw new DomainException("msg.error.stock.reversal.target.container.inactive");
        }
        validateSameFacility(original, targetContainer);
        validateSerialNotOnHand(original, request.targetContainerId());

        try {
            stockService.adjust(toPayload(original, request));
        } catch (DataIntegrityViolationException e) {
            throw new DomainException("msg.error.stock.reversal.already.reversed");
        }
    }

    private void validateOriginal(InventoryMovementEntity original) {
        if (original.getReversalOfMovementId() != null) {
            throw new DomainException("msg.error.stock.reversal.chain.not.allowed");
        }
        if (!isOutbound(original)) {
            throw new DomainException("msg.error.stock.reversal.outbound.required");
        }
        if (original.getQuantity() == null || original.getQuantity().compareTo(BigDecimal.ZERO) == 0) {
            throw new DomainException("msg.error.stock.reversal.quantity.required");
        }
    }

    private boolean isOutbound(InventoryMovementEntity original) {
        if (original.getMovementType() == MovementType.ADJUSTMENT) {
            return original.getQuantity() != null && original.getQuantity().compareTo(BigDecimal.ZERO) < 0;
        }
        return original.getMovementType() == MovementType.ISSUE
                || original.getMovementType() == MovementType.ISSUE_RESERVED
                || original.getMovementType() == MovementType.TRANSFER_OUT;
    }

    private void validateSameFacility(InventoryMovementEntity original, ContainerEntity targetContainer) {
        ContainerEntity originalContainer = containerRepository.findById(original.getContainerId())
                .orElseThrow(() -> new DomainException("msg.error.stock.reversal.original.container.not.found"));
        GridEntity originalGrid = gridRepository.findById(originalContainer.getGridId())
                .orElseThrow(() -> new DomainException("msg.error.stock.reversal.original.grid.not.found"));
        GridEntity targetGrid = gridRepository.findById(targetContainer.getGridId())
                .orElseThrow(() -> new DomainException("msg.error.stock.reversal.target.grid.not.found"));
        if (!originalGrid.getFacilityId().equals(targetGrid.getFacilityId())) {
            throw new DomainException("msg.error.stock.reversal.facility.mismatch");
        }
    }

    private void validateSerialNotOnHand(InventoryMovementEntity original, Long targetContainerId) {
        if (original.getSerialNumber() == null || original.getSerialNumber().isBlank()) {
            return;
        }
        stockBalanceRepository
                .findByProductContainerSerial(original.getProductId(), targetContainerId, original.getSerialNumber())
                .map(StockBalance::getQuantity)
                .filter(quantity -> quantity.compareTo(BigDecimal.ZERO) > 0)
                .ifPresent(quantity -> {
                    throw new DomainException("msg.error.stock.reversal.serial.on.hand");
                });
    }

    private StockMovementPayload toPayload(InventoryMovementEntity original, StockMovementReversalRequest request) {
        CurrencyAmount unitCost = original.getUnitCost();
        return StockMovementPayload.builder()
                .productId(original.getProductId())
                .containerId(request.targetContainerId())
                .serialNumber(original.getSerialNumber())
                .quantity(original.getQuantity().abs())
                .movementType(MovementType.RECEIPT)
                .referenceType(original.getReferenceType())
                .referenceId(original.getReferenceId())
                .referenceCode(original.getReferenceCode())
                .currencyId(unitCost != null ? unitCost.getCurrencyId() : null)
                .exchangeRate(resolveExchangeRate(unitCost))
                .netPrice(resolveNetPrice(unitCost))
                .transactionDate(request.reversalDate().atStartOfDay())
                .reversalOfMovementId(original.getId())
                .build();
    }

    private BigDecimal resolveNetPrice(CurrencyAmount unitCost) {
        if (unitCost == null) {
            return BigDecimal.ZERO;
        }
        if (unitCost.getOriginalAmount() != null) {
            return unitCost.getOriginalAmount();
        }
        return unitCost.getLocalAmount() != null ? unitCost.getLocalAmount() : BigDecimal.ZERO;
    }

    private BigDecimal resolveExchangeRate(CurrencyAmount unitCost) {
        if (unitCost == null) {
            return BigDecimal.ONE;
        }
        if (unitCost.getOriginalAmount() == null && unitCost.getLocalAmount() != null) {
            return BigDecimal.ONE;
        }
        return unitCost.getExchangeRate() != null ? unitCost.getExchangeRate() : BigDecimal.ONE;
    }
}
