package com.solusi.erp.inventory.stock.infrastructure.service;

import com.solusi.erp.core.model.CurrencyAmount;
import com.solusi.erp.inventory.stock.application.dto.StockMovementPayload;
import com.solusi.erp.inventory.stock.domain.model.CostAmount;
import com.solusi.erp.inventory.stock.domain.model.MovementType;
import com.solusi.erp.inventory.stock.domain.model.StockBalance;
import com.solusi.erp.inventory.stock.domain.port.StockService;
import com.solusi.erp.inventory.stock.domain.repository.StockBalanceRepository;
import com.solusi.erp.inventory.stock.domain.service.FifoValuationService;
import com.solusi.erp.inventory.stock.infrastructure.persistence.InventoryMovementEntity;
import com.solusi.erp.inventory.stock.infrastructure.persistence.InventoryMovementJpaRepository;
import com.solusi.erp.inventory.uomconversion.domain.port.UomConversionService;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Implementation of StockService delegating business logic to domain models.
 */
public class StockServiceImpl implements StockService {

    private final StockBalanceRepository stockBalanceRepository;
    private final InventoryMovementJpaRepository inventoryMovementRepository;
    private final UomConversionService uomConversionService;
    private final FifoValuationService fifoValuationService;
    private final MessageSource messageSource;

    public StockServiceImpl(StockBalanceRepository stockBalanceRepository,
                            InventoryMovementJpaRepository inventoryMovementRepository,
                            UomConversionService uomConversionService,
                            FifoValuationService fifoValuationService,
                            MessageSource messageSource) {
        this.stockBalanceRepository = stockBalanceRepository;
        this.inventoryMovementRepository = inventoryMovementRepository;
        this.uomConversionService = uomConversionService;
        this.fifoValuationService = fifoValuationService;
        this.messageSource = messageSource;
    }

    @Override
    public void adjust(StockMovementPayload payload) {
        // 1. Convert Quantity to Base UOM
        BigDecimal baseQuantity = payload.getQuantity();
        if (payload.getUomId() != null) {
            baseQuantity = uomConversionService.convertToBaseUom(
                    payload.getProductId(), payload.getUomId(), payload.getQuantity());
        }

        // 2. Resolve Serial Number
        final String serialNumber = payload.getSerialNumber();

        // 3. Load or create domain StockBalance, apply movement, validate
        StockBalance balance = stockBalanceRepository
                .findByProductContainerSerial(payload.getProductId(), payload.getContainerId(), serialNumber)
                .orElseGet(() -> StockBalance.createNew(payload.getProductId(), payload.getContainerId(), serialNumber));

        balance.applyMovement(payload.getMovementType(), baseQuantity);

        try {
            balance.validate();
        } catch (IllegalStateException e) {
            throw new RuntimeException(getMessage(e.getMessage()));
        }

        stockBalanceRepository.save(balance);

        // 4. Handle Valuation (FIFO) via domain service
        CostAmount costAmount = handleValuation(payload, baseQuantity, serialNumber);

        // 5. Log Movement (infrastructure concern — writes directly to JPA entity)
        CurrencyAmount unitCost = costAmount != null ? toCurrencyAmount(costAmount) : null;
        logMovement(payload, baseQuantity, serialNumber, unitCost);
    }

    private CostAmount handleValuation(StockMovementPayload payload, BigDecimal baseQuantity, String serialNumber) {
        BigDecimal absQuantity = baseQuantity.abs();
        if (isPositiveAdjustment(payload)) {
            CostAmount cost = resolveCostAmount(payload, baseQuantity);
            fifoValuationService.addLayer(
                    payload.getProductId(),
                    payload.getContainerId(),
                    serialNumber,
                    absQuantity,
                    cost,
                    payload.getValuationReferenceType(),
                    payload.getValuationReferenceId(),
                    payload.getValuationReferenceLineId(),
                    payload.getReversalOfMovementId()
            );
            return cost;
        } else if (isNegativeAdjustment(payload)) {
            if (hasValuationReference(payload)) {
                return fifoValuationService.consumeSpecificLayers(
                        payload.getProductId(),
                        payload.getContainerId(),
                        serialNumber,
                        payload.getValuationReferenceType(),
                        payload.getValuationReferenceId(),
                        payload.getValuationReferenceLineId(),
                        absQuantity
                );
            }
            return fifoValuationService.consumeLayers(payload.getProductId(), payload.getContainerId(), serialNumber, absQuantity);
        }
        return null;
    }

    private boolean hasValuationReference(StockMovementPayload payload) {
        return payload.getValuationReferenceType() != null
                && payload.getValuationReferenceId() != null
                && payload.getValuationReferenceLineId() != null;
    }

    private CostAmount resolveCostAmount(StockMovementPayload payload, BigDecimal baseQuantity) {
        BigDecimal exchangeRate = payload.getExchangeRate() != null ? payload.getExchangeRate() : BigDecimal.ONE;
        BigDecimal originalUnitPrice = payload.getNetPrice() != null ? payload.getNetPrice() : BigDecimal.ZERO;
        BigDecimal transactionQuantity = payload.getQuantity() != null ? payload.getQuantity() : BigDecimal.ONE;
        if (baseQuantity == null
                || baseQuantity.compareTo(BigDecimal.ZERO) == 0
                || transactionQuantity.compareTo(BigDecimal.ZERO) == 0) {
            return CostAmount.of(payload.getCurrencyId(), exchangeRate, originalUnitPrice);
        }

        BigDecimal conversionFactor = baseQuantity.divide(transactionQuantity, 6, RoundingMode.HALF_UP);
        if (conversionFactor.compareTo(BigDecimal.ZERO) == 0) {
            return CostAmount.of(payload.getCurrencyId(), exchangeRate, originalUnitPrice);
        }

        BigDecimal normalizedOriginal = originalUnitPrice.divide(conversionFactor, 6, RoundingMode.HALF_UP);
        return CostAmount.of(payload.getCurrencyId(), exchangeRate, normalizedOriginal);
    }

    private CurrencyAmount toCurrencyAmount(CostAmount cost) {
        return CurrencyAmount.builder()
                .currencyId(cost.currencyId())
                .exchangeRate(cost.exchangeRate())
                .originalAmount(cost.originalAmount())
                .localAmount(cost.localAmount())
                .build();
    }

    private boolean isPositiveAdjustment(StockMovementPayload payload) {
        if (payload.getMovementType() == MovementType.ADJUSTMENT) {
            return payload.getQuantity().compareTo(BigDecimal.ZERO) > 0;
        }
        return switch (payload.getMovementType()) {
            case RECEIPT, TRANSFER_IN -> true;
            default -> false;
        };
    }

    private boolean isNegativeAdjustment(StockMovementPayload payload) {
        if (payload.getMovementType() == MovementType.ADJUSTMENT) {
            return payload.getQuantity().compareTo(BigDecimal.ZERO) < 0;
        }
        return switch (payload.getMovementType()) {
            case ISSUE, ISSUE_RESERVED, TRANSFER_OUT -> true;
            default -> false;
        };
    }

    private void logMovement(StockMovementPayload payload, BigDecimal baseQuantity, String serialNumber, CurrencyAmount unitCost) {
        InventoryMovementEntity movement = new InventoryMovementEntity();
        movement.setTransactionDate(payload.getTransactionDate());
        movement.setProductId(payload.getProductId());
        movement.setContainerId(payload.getContainerId());
        movement.setSerialNumber(serialNumber);
        movement.setQuantity(baseQuantity);
        movement.setMovementType(payload.getMovementType());
        movement.setReferenceType(payload.getReferenceType());
        movement.setReferenceId(payload.getReferenceId());
        movement.setReferenceCode(payload.getReferenceCode());
        movement.setReversalOfMovementId(payload.getReversalOfMovementId());
        movement.setUnitCost(unitCost);
        inventoryMovementRepository.save(movement);
    }

    private String getMessage(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }
}
