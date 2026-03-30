package com.solusi.erp.inventory.stock.infrastructure.service;

import com.solusi.erp.core.model.CurrencyAmount;
import com.solusi.erp.inventory.stock.domain.port.ValuationService;
import com.solusi.erp.inventory.stock.infrastructure.persistence.ValuationLayerEntity;
import com.solusi.erp.inventory.stock.infrastructure.persistence.ValuationLayerJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ValuationServiceImpl implements ValuationService {

    private final ValuationLayerJpaRepository valuationLayerRepository;
    private final MessageSource messageSource;

    @Override
    public void addStock(Long productId, Long containerId, String serialNumber, BigDecimal quantity, CurrencyAmount unitCost) {
        ValuationLayerEntity layer = new ValuationLayerEntity();
        layer.setProductId(productId);
        layer.setContainerId(containerId);
        layer.setSerialNumber(serialNumber);
        layer.setInitialQuantity(quantity);
        layer.setRemainingQuantity(quantity);
        layer.setUnitCost(unitCost);
        valuationLayerRepository.save(layer);
    }

    @Override
    public CurrencyAmount consumeStock(Long productId, Long containerId, String serialNumber, BigDecimal quantityToConsume) {
        List<ValuationLayerEntity> layers;
        if (serialNumber != null && !serialNumber.isEmpty()) {
            layers = valuationLayerRepository.findByProductIdAndContainerIdAndSerialNumberAndRemainingQuantityGreaterThan(
                    productId, containerId, serialNumber, BigDecimal.ZERO);
        } else {
            layers = valuationLayerRepository.findByProductIdAndContainerIdAndRemainingQuantityGreaterThanOrderByCreatedDateAsc(
                    productId, containerId, BigDecimal.ZERO);
        }

        BigDecimal totalLocalAmount = BigDecimal.ZERO;
        BigDecimal remainingToConsume = quantityToConsume;

        for (ValuationLayerEntity layer : layers) {
            if (remainingToConsume.compareTo(BigDecimal.ZERO) <= 0) break;

            BigDecimal canConsume = layer.getRemainingQuantity().min(remainingToConsume);

            // Calculate portion of cost
            totalLocalAmount = totalLocalAmount.add(canConsume.multiply(layer.getUnitCost().getLocalAmount()));

            layer.setRemainingQuantity(layer.getRemainingQuantity().subtract(canConsume));
            remainingToConsume = remainingToConsume.subtract(canConsume);

            valuationLayerRepository.save(layer);
        }

        if (remainingToConsume.compareTo(BigDecimal.ZERO) > 0) {
            throw new RuntimeException(getMessage("msg.error.valuation.insufficient_stock"));
        }

        // Return the weighted average unit cost for this transaction
        BigDecimal averageLocalUnitCost = totalLocalAmount.divide(quantityToConsume, 4, RoundingMode.HALF_UP);

        return CurrencyAmount.builder()
                .localAmount(averageLocalUnitCost)
                .build();
    }

    private String getMessage(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }
}
