package com.solusi.erp.inventory.stock.infrastructure.service;

import com.solusi.erp.core.model.CurrencyAmount;
import com.solusi.erp.inventory.stock.application.dto.StockMovementPayload;
import com.solusi.erp.inventory.stock.domain.model.MovementType;
import com.solusi.erp.inventory.stock.domain.port.StockService;
import com.solusi.erp.inventory.stock.domain.port.ValuationService;
import com.solusi.erp.inventory.stock.infrastructure.persistence.InventoryMovementEntity;
import com.solusi.erp.inventory.stock.infrastructure.persistence.InventoryMovementJpaRepository;
import com.solusi.erp.inventory.stock.infrastructure.persistence.StockBalanceEntity;
import com.solusi.erp.inventory.stock.infrastructure.persistence.StockBalanceJpaRepository;
import com.solusi.erp.inventory.product.infrastructure.persistence.ProductEntity;
import com.solusi.erp.inventory.product.infrastructure.persistence.JpaProductRepository;
import com.solusi.erp.inventory.uomconversion.domain.port.UomConversionService;
import com.solusi.erp.inventory.shared.util.SerialNumberGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

/**
 * Implementation of StockService with Multi-Currency and UOM support.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {

    private final StockBalanceJpaRepository stockBalanceRepository;
    private final InventoryMovementJpaRepository inventoryMovementRepository;
    private final JpaProductRepository productRepository;
    private final UomConversionService uomConversionService;
    private final ValuationService valuationService;
    private final MessageSource messageSource;

    @Override
    public void adjust(StockMovementPayload payload) {
        // 1. Convert Quantity to Base UOM
        BigDecimal baseQuantity = payload.getQuantity();
        if (payload.getUomId() != null) {
            baseQuantity = uomConversionService.convertToBaseUom(
                    payload.getProductId(), payload.getUomId(), payload.getQuantity());
        }

        ProductEntity product = productRepository.getReferenceById(payload.getProductId());

        // 2. Resolve Serial Number
        final String serialNumber = resolveSerialNumber(product, payload);

        // 3. Update Balance
        StockBalanceEntity balance = stockBalanceRepository
                .findByProductIdAndContainerIdAndSerialNumber(payload.getProductId(), payload.getContainerId(), serialNumber)
                .orElseGet(() -> createNewBalance(payload, serialNumber));

        updateBalance(balance, payload, baseQuantity);
        validateBalance(balance);
        stockBalanceRepository.save(balance);

        // 4. Handle Valuation (FIFO)
        CurrencyAmount unitCost = handleValuation(payload, baseQuantity, serialNumber);

        // 5. Log Movement
        logMovement(payload, baseQuantity, serialNumber, unitCost);
    }

    private CurrencyAmount handleValuation(StockMovementPayload payload, BigDecimal baseQuantity, String serialNumber) {
        BigDecimal absQuantity = baseQuantity.abs();
        if (isPositiveAdjustment(payload)) {
            CurrencyAmount cost = resolveCurrencyAmount(payload);
            valuationService.addStock(payload.getProductId(), payload.getContainerId(), serialNumber, absQuantity, cost);
            return cost;
        } else if (isNegativeAdjustment(payload)) {
            return valuationService.consumeStock(payload.getProductId(), payload.getContainerId(), serialNumber, absQuantity);
        }
        return null;
    }

    private CurrencyAmount resolveCurrencyAmount(StockMovementPayload payload) {
        BigDecimal exchangeRate = payload.getExchangeRate() != null ? payload.getExchangeRate() : BigDecimal.ONE;
        BigDecimal originalAmount = payload.getNetPrice() != null ? payload.getNetPrice() : BigDecimal.ZERO;
        BigDecimal localAmount = originalAmount.multiply(exchangeRate);

        return CurrencyAmount.builder()
                .currencyId(payload.getCurrencyId())
                .exchangeRate(exchangeRate)
                .originalAmount(originalAmount)
                .localAmount(localAmount)
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

    private String resolveSerialNumber(ProductEntity product, StockMovementPayload payload) {
        String sn = payload.getSerialNumber();
        if (Boolean.TRUE.equals(product.getIsSerialized()) && !StringUtils.hasText(sn) && isPositiveAdjustment(payload)) {
            return SerialNumberGenerator.generate();
        }
        return sn;
    }

    private StockBalanceEntity createNewBalance(StockMovementPayload payload, String serialNumber) {
        StockBalanceEntity balance = new StockBalanceEntity();
        balance.setProductId(payload.getProductId());
        balance.setContainerId(payload.getContainerId());
        balance.setSerialNumber(serialNumber);
        balance.setQuantity(BigDecimal.ZERO);
        balance.setReservedQuantity(BigDecimal.ZERO);
        balance.setInTransitQuantity(BigDecimal.ZERO);
        return balance;
    }

    private void updateBalance(StockBalanceEntity balance, StockMovementPayload payload, BigDecimal baseQuantity) {
        switch (payload.getMovementType()) {
            case RECEIPT, TRANSFER_IN, ADJUSTMENT -> {
                balance.setQuantity(balance.getQuantity().add(baseQuantity));
            }
            case ISSUE, TRANSFER_OUT -> {
                balance.setQuantity(balance.getQuantity().subtract(baseQuantity));
            }
            case ISSUE_RESERVED -> {
                balance.setQuantity(balance.getQuantity().subtract(baseQuantity));
                balance.setReservedQuantity(balance.getReservedQuantity().subtract(baseQuantity));
            }
            case RESERVE -> {
                balance.setReservedQuantity(balance.getReservedQuantity().add(baseQuantity));
            }
            case RELEASE -> {
                balance.setReservedQuantity(balance.getReservedQuantity().subtract(baseQuantity));
            }
        }
    }

    private void validateBalance(StockBalanceEntity balance) {
        if (balance.getQuantity().compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException(getMessage("msg.error.inventory.insufficient_stock"));
        }
        if (balance.getReservedQuantity().compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException(getMessage("msg.error.inventory.insufficient_reserved"));
        }
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
        movement.setUnitCost(unitCost);
        inventoryMovementRepository.save(movement);
    }

    private String getMessage(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }
}
