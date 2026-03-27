package com.solusi.erp.inventory.adjustment.infrastructure.persistence;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.inventory.adjustment.domain.model.AdjustmentStatus;
import com.solusi.erp.inventory.adjustment.domain.model.StockAdjustment;
import com.solusi.erp.inventory.adjustment.domain.model.StockAdjustmentLineItem;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manual persistence mapper — bidirectional between JPA entity and domain model.
 * Uses plain Java navigation with null-safety for optional associations.
 */
public class StockAdjustmentPersistenceMapper {

    public StockAdjustment toDomain(com.solusi.erp.inventory.model.StockAdjustment e) {
        if (e == null) return null;

        Long versionLong = (e.getVersion() != null) ? e.getVersion().longValue() : null;
        AuditMetadata metadata = new AuditMetadata(
                e.getId(), versionLong, e.getCreatedDate(), e.getCreatedBy(),
                e.getUpdatedDate(), e.getUpdatedBy());

        Long facilityId = (e.getFacility() != null) ? e.getFacility().getId() : null;
        String facilityName = (e.getFacility() != null) ? e.getFacility().getName() : null;

        Long currencyId = null;
        String currencyAlias = null;
        BigDecimal exchangeRate = null;
        BigDecimal totalOriginal = null;
        BigDecimal totalLocal = null;
        if (e.getTotalCost() != null) {
            if (e.getTotalCost().getCurrency() != null) {
                currencyId = e.getTotalCost().getCurrency().getId();
                currencyAlias = e.getTotalCost().getCurrency().getAlias();
            }
            exchangeRate = e.getTotalCost().getExchangeRate();
            totalOriginal = e.getTotalCost().getOriginalAmount();
            totalLocal = e.getTotalCost().getLocalAmount();
        }

        AdjustmentStatus status = (e.getStatus() != null)
                ? AdjustmentStatus.valueOf(e.getStatus().name())
                : AdjustmentStatus.DRAFT;

        List<StockAdjustmentLineItem> lines = (e.getLines() != null)
                ? e.getLines().stream().map(this::toLineItemDomain).collect(Collectors.toList())
                : new ArrayList<>();

        return new StockAdjustment(metadata, e.getCode(), e.getTransactionDate(), status, e.getNote(),
                facilityId, facilityName, currencyId, currencyAlias, exchangeRate,
                totalOriginal, totalLocal, lines);
    }

    private StockAdjustmentLineItem toLineItemDomain(com.solusi.erp.inventory.model.StockAdjustmentLine l) {
        if (l == null) return null;

        Long productId = l.getProduct() != null ? l.getProduct().getId() : null;
        String productCode = l.getProduct() != null ? l.getProduct().getCode() : null;
        String productName = l.getProduct() != null ? l.getProduct().getName() : null;
        Boolean isSerialized = l.getProduct() != null ? l.getProduct().getIsSerialized() : null;

        Long gridId = l.getGrid() != null ? l.getGrid().getId() : null;
        String gridCode = l.getGrid() != null ? l.getGrid().getCode() : null;
        String gridName = l.getGrid() != null ? l.getGrid().getName() : null;

        Long containerId = l.getContainer() != null ? l.getContainer().getId() : null;
        String containerCode = l.getContainer() != null ? l.getContainer().getCode() : null;
        String containerName = l.getContainer() != null ? l.getContainer().getName() : null;

        String facilityName = null;
        if (l.getContainer() != null && l.getContainer().getGrid() != null
                && l.getContainer().getGrid().getFacility() != null) {
            facilityName = l.getContainer().getGrid().getFacility().getName();
        }

        Long uomId = l.getUom() != null ? l.getUom().getId() : null;
        String uomName = l.getUom() != null ? l.getUom().getName() : null;

        Long lineVersion = l.getVersion() != null ? l.getVersion().longValue() : null;

        return new StockAdjustmentLineItem(l.getId(), l.getVersion(), productId, productCode, productName,
                isSerialized, gridId, gridCode, gridName, containerId, containerCode, containerName,
                facilityName, uomId, uomName, l.getConversionFactor(), l.getQuantity(),
                l.getUnitCost(), l.getTotalAmount(), l.getSerialNumber());
    }
}
