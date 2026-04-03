package com.solusi.erp.inventory.stock.domain.service;

import com.solusi.erp.inventory.stock.domain.model.CostAmount;
import com.solusi.erp.inventory.stock.domain.model.ValuationLayer;
import com.solusi.erp.inventory.stock.domain.repository.ValuationLayerRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Domain Service: FIFO Valuation Engine.
 * Handles creation and consumption of valuation layers using First-In, First-Out method.
 * Pure Java — no Spring, no Lombok.
 */
public class FifoValuationService {

    private final ValuationLayerRepository layerRepository;

    public FifoValuationService(ValuationLayerRepository layerRepository) {
        this.layerRepository = layerRepository;
    }

    /**
     * Create a new valuation layer for inbound stock.
     */
    public void addLayer(Long productId, Long containerId, String serialNumber,
                         BigDecimal quantity, CostAmount unitCost) {
        ValuationLayer layer = ValuationLayer.createNew(productId, containerId, serialNumber, quantity, unitCost);
        layerRepository.save(layer);
    }

    /**
     * Consume existing layers using FIFO (oldest first).
     * Returns the weighted average unit cost in local currency for the consumed quantity.
     *
     * @throws IllegalStateException if insufficient stock in valuation layers
     */
    public CostAmount consumeLayers(Long productId, Long containerId,
                                    String serialNumber, BigDecimal quantityToConsume) {
        List<ValuationLayer> layers;
        if (serialNumber != null && !serialNumber.isEmpty()) {
            layers = layerRepository.findAvailableLayersBySerial(
                    productId, containerId, serialNumber, BigDecimal.ZERO);
        } else {
            layers = layerRepository.findAvailableLayers(
                    productId, containerId, BigDecimal.ZERO);
        }

        BigDecimal totalLocalCost = BigDecimal.ZERO;
        BigDecimal remainingToConsume = quantityToConsume;

        for (ValuationLayer layer : layers) {
            if (remainingToConsume.compareTo(BigDecimal.ZERO) <= 0) break;

            BigDecimal canConsume = layer.consumableQuantity(remainingToConsume);
            BigDecimal localCost = layer.consume(remainingToConsume);
            totalLocalCost = totalLocalCost.add(localCost);
            remainingToConsume = remainingToConsume.subtract(canConsume);

            layerRepository.save(layer);
        }

        if (remainingToConsume.compareTo(BigDecimal.ZERO) > 0) {
            throw new IllegalStateException("msg.error.valuation.insufficient_stock");
        }

        BigDecimal averageLocalUnitCost = totalLocalCost.divide(quantityToConsume, 4, RoundingMode.HALF_UP);
        return CostAmount.localOnly(averageLocalUnitCost);
    }
}
