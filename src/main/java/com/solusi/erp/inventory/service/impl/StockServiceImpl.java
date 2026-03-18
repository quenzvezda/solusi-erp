package com.solusi.erp.inventory.service.impl;

import com.solusi.erp.inventory.dto.StockMovementPayload;
import com.solusi.erp.inventory.model.InventoryMovement;
import com.solusi.erp.inventory.model.Product;
import com.solusi.erp.inventory.model.StockBalance;
import com.solusi.erp.inventory.repository.InventoryMovementRepository;
import com.solusi.erp.inventory.repository.ProductRepository;
import com.solusi.erp.inventory.repository.StockBalanceRepository;
import com.solusi.erp.inventory.repository.ContainerRepository;
import com.solusi.erp.inventory.service.StockService;
import com.solusi.erp.inventory.util.SerialNumberGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

/**
 * Implementation of StockService.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {

    private final StockBalanceRepository stockBalanceRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final ProductRepository productRepository;
    private final ContainerRepository containerRepository;

    @Override
    public void adjust(StockMovementPayload payload) {
        Product product = productRepository.getReferenceById(payload.getProductId());
        
        // Handle Serial Number generation for new stock
        final String serialNumber = resolveSerialNumber(product, payload);

        StockBalance balance = stockBalanceRepository
                .findByProductIdAndContainerIdAndSerialNumber(payload.getProductId(), payload.getContainerId(), serialNumber)
                .orElseGet(() -> createNewBalance(payload, serialNumber));

        updateBalance(balance, payload);
        validateBalance(balance);
        
        stockBalanceRepository.save(balance);
        logMovement(payload, serialNumber);
    }

    private String resolveSerialNumber(Product product, StockMovementPayload payload) {
        String sn = payload.getSerialNumber();
        if (product.getIsSerialized() && !StringUtils.hasText(sn) && isPositiveAdjustment(payload)) {
            return SerialNumberGenerator.generate();
        }
        return sn;
    }

    private boolean isPositiveAdjustment(StockMovementPayload payload) {
        return switch (payload.getMovementType()) {
            case RECEIPT, ADJUSTMENT, TRANSFER_IN -> true;
            default -> false;
        };
    }

    private StockBalance createNewBalance(StockMovementPayload payload, String serialNumber) {
        StockBalance balance = new StockBalance();
        balance.setProduct(productRepository.getReferenceById(payload.getProductId()));
        balance.setContainer(containerRepository.getReferenceById(payload.getContainerId()));
        balance.setSerialNumber(serialNumber);
        balance.setQuantity(BigDecimal.ZERO);
        balance.setReservedQuantity(BigDecimal.ZERO);
        balance.setInTransitQuantity(BigDecimal.ZERO);
        return balance;
    }

    private void updateBalance(StockBalance balance, StockMovementPayload payload) {
        BigDecimal qty = payload.getQuantity();
        switch (payload.getMovementType()) {
            case RECEIPT -> {
                balance.setQuantity(balance.getQuantity().add(qty));
            }
            case ISSUE -> {
                balance.setQuantity(balance.getQuantity().subtract(qty));
            }
            case ISSUE_RESERVED -> {
                balance.setQuantity(balance.getQuantity().subtract(qty));
                balance.setReservedQuantity(balance.getReservedQuantity().subtract(qty));
            }
            case RESERVE -> {
                balance.setReservedQuantity(balance.getReservedQuantity().add(qty));
            }
            case RELEASE -> {
                balance.setReservedQuantity(balance.getReservedQuantity().subtract(qty));
            }
            case TRANSFER_OUT -> {
                balance.setQuantity(balance.getQuantity().subtract(qty));
            }
            case TRANSFER_IN -> {
                balance.setQuantity(balance.getQuantity().add(qty));
            }
            case ADJUSTMENT -> {
                balance.setQuantity(balance.getQuantity().add(qty));
            }
        }
    }

    private void validateBalance(StockBalance balance) {
        if (balance.getQuantity().compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("msg.error.inventory.insufficient_stock");
        }
        if (balance.getReservedQuantity().compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("msg.error.inventory.insufficient_reserved");
        }
    }

    private void logMovement(StockMovementPayload payload, String serialNumber) {
        InventoryMovement movement = new InventoryMovement();
        movement.setTransactionDate(payload.getTransactionDate());
        movement.setProduct(productRepository.getReferenceById(payload.getProductId()));
        movement.setContainer(containerRepository.getReferenceById(payload.getContainerId()));
        movement.setSerialNumber(serialNumber);
        movement.setQuantity(payload.getQuantity());
        movement.setMovementType(payload.getMovementType());
        movement.setReferenceType(payload.getReferenceType());
        movement.setReferenceId(payload.getReferenceId());
        movement.setReferenceCode(payload.getReferenceCode());
        inventoryMovementRepository.save(movement);
    }
}
