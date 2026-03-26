package com.solusi.erp.inventory.service.impl;

import com.solusi.erp.core.model.CurrencyAmount;
import com.solusi.erp.inventory.model.ValuationLayer;
import com.solusi.erp.inventory.repository.ContainerRepository;
import com.solusi.erp.inventory.product.infrastructure.persistence.JpaProductRepository;
import com.solusi.erp.inventory.repository.ValuationLayerRepository;
import com.solusi.erp.inventory.service.ValuationService;
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

    private final ValuationLayerRepository valuationLayerRepository;
    private final JpaProductRepository productRepository;
    private final ContainerRepository containerRepository;
    private final MessageSource messageSource;

    @Override
    public void addStock(Long productId, Long containerId, String serialNumber, BigDecimal quantity, CurrencyAmount unitCost) {
        ValuationLayer layer = new ValuationLayer();
        layer.setProduct(productRepository.getReferenceById(productId));
        layer.setContainer(containerRepository.getReferenceById(containerId));
        layer.setSerialNumber(serialNumber);
        layer.setInitialQuantity(quantity);
        layer.setRemainingQuantity(quantity);
        layer.setUnitCost(unitCost);
        valuationLayerRepository.save(layer);
    }

    @Override
    public CurrencyAmount consumeStock(Long productId, Long containerId, String serialNumber, BigDecimal quantityToConsume) {
        List<ValuationLayer> layers;
        if (serialNumber != null && !serialNumber.isEmpty()) {
            layers = valuationLayerRepository.findByProductIdAndContainerIdAndSerialNumberAndRemainingQuantityGreaterThan(
                    productId, containerId, serialNumber, BigDecimal.ZERO);
        } else {
            layers = valuationLayerRepository.findByProductIdAndContainerIdAndRemainingQuantityGreaterThanOrderByCreatedDateAsc(
                    productId, containerId, BigDecimal.ZERO);
        }

        BigDecimal totalLocalAmount = BigDecimal.ZERO;
        BigDecimal remainingToConsume = quantityToConsume;

        for (ValuationLayer layer : layers) {
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
