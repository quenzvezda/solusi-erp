package com.solusi.erp.inventory.adjustment.infrastructure.persistence;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.inventory.adjustment.domain.model.AdjustmentStatus;
import com.solusi.erp.inventory.adjustment.domain.model.StockAdjustment;
import com.solusi.erp.inventory.adjustment.domain.model.StockAdjustmentLineItem;
import com.solusi.erp.inventory.container.infrastructure.persistence.ContainerEntity;
import com.solusi.erp.inventory.container.infrastructure.persistence.ContainerJpaRepository;
import com.solusi.erp.inventory.facility.infrastructure.persistence.FacilityEntity;
import com.solusi.erp.inventory.facility.infrastructure.persistence.FacilityJpaRepository;
import com.solusi.erp.inventory.grid.infrastructure.persistence.GridEntity;
import com.solusi.erp.inventory.grid.infrastructure.persistence.GridJpaRepository;
import com.solusi.erp.inventory.product.infrastructure.persistence.JpaProductRepository;
import com.solusi.erp.inventory.product.infrastructure.persistence.ProductEntity;
import com.solusi.erp.inventory.uom.infrastructure.persistence.UomEntity;
import com.solusi.erp.inventory.uom.infrastructure.persistence.UomJpaRepository;
import com.solusi.erp.master.currency.domain.repository.CurrencyRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manual persistence mapper — bidirectional between JPA entity and domain model.
 * Uses Long IDs and resolves names via local slice JPA repositories.
 */
public class StockAdjustmentPersistenceMapper {

    private final CurrencyRepository currencyRepository;
    private final FacilityJpaRepository facilityJpaRepository;
    private final JpaProductRepository productJpaRepository;
    private final GridJpaRepository gridJpaRepository;
    private final ContainerJpaRepository containerJpaRepository;
    private final UomJpaRepository uomJpaRepository;

    public StockAdjustmentPersistenceMapper(CurrencyRepository currencyRepository,
                                            FacilityJpaRepository facilityJpaRepository,
                                            JpaProductRepository productJpaRepository,
                                            GridJpaRepository gridJpaRepository,
                                            ContainerJpaRepository containerJpaRepository,
                                            UomJpaRepository uomJpaRepository) {
        this.currencyRepository = currencyRepository;
        this.facilityJpaRepository = facilityJpaRepository;
        this.productJpaRepository = productJpaRepository;
        this.gridJpaRepository = gridJpaRepository;
        this.containerJpaRepository = containerJpaRepository;
        this.uomJpaRepository = uomJpaRepository;
    }

    public StockAdjustment toDomain(StockAdjustmentEntity e) {
        if (e == null) return null;

        Long versionLong = (e.getVersion() != null) ? e.getVersion().longValue() : null;
        AuditMetadata metadata = new AuditMetadata(
                e.getId(), versionLong, e.getCreatedDate(), e.getCreatedBy(),
                e.getUpdatedDate(), e.getUpdatedBy());

        String facilityName = resolveFacilityName(e.getFacilityId());

        Long currencyId = null;
        String currencyAlias = null;
        BigDecimal exchangeRate = null;
        BigDecimal totalOriginal = null;
        BigDecimal totalLocal = null;
        if (e.getTotalCost() != null) {
            currencyId = e.getTotalCost().getCurrencyId();
            currencyAlias = resolveCurrencyAlias(currencyId);
            exchangeRate = e.getTotalCost().getExchangeRate();
            totalOriginal = e.getTotalCost().getOriginalAmount();
            totalLocal = e.getTotalCost().getLocalAmount();
        }

        AdjustmentStatus status = (e.getStatus() != null)
                ? e.getStatus()
                : AdjustmentStatus.DRAFT;

        List<StockAdjustmentLineItem> lines = (e.getLines() != null)
                ? e.getLines().stream().map(this::toLineItemDomain).collect(Collectors.toList())
                : new ArrayList<>();

        return new StockAdjustment(metadata, e.getCode(), e.getTransactionDate(), status, e.getNote(),
                e.getFacilityId(), facilityName, currencyId, currencyAlias, exchangeRate,
                totalOriginal, totalLocal, lines);
    }

    private String resolveFacilityName(Long facilityId) {
        if (facilityId == null) return null;
        return facilityJpaRepository.findById(facilityId).map(FacilityEntity::getName).orElse(null);
    }

    private String resolveCurrencyAlias(Long currencyId) {
        if (currencyId == null) return null;
        return currencyRepository.findById(currencyId).map(c -> c.getAlias()).orElse(null);
    }

    private StockAdjustmentLineItem toLineItemDomain(StockAdjustmentLineEntity l) {
        if (l == null) return null;

        String productCode = null;
        String productName = null;
        Boolean isSerialized = null;
        if (l.getProductId() != null) {
            ProductEntity p = productJpaRepository.findById(l.getProductId()).orElse(null);
            if (p != null) {
                productCode = p.getCode();
                productName = p.getName();
                isSerialized = p.getIsSerialized();
            }
        }

        String gridCode = null;
        String gridName = null;
        if (l.getGridId() != null) {
            GridEntity g = gridJpaRepository.findById(l.getGridId()).orElse(null);
            if (g != null) {
                gridCode = g.getCode();
                gridName = g.getName();
            }
        }

        String containerCode = null;
        String containerName = null;
        String facilityName = null;
        if (l.getContainerId() != null) {
            ContainerEntity c = containerJpaRepository.findById(l.getContainerId()).orElse(null);
            if (c != null) {
                containerCode = c.getCode();
                containerName = c.getName();
                if (c.getGridId() != null) {
                    GridEntity cg = gridJpaRepository.findById(c.getGridId()).orElse(null);
                    if (cg != null && cg.getFacilityId() != null) {
                        facilityName = facilityJpaRepository.findById(cg.getFacilityId())
                                .map(FacilityEntity::getName).orElse(null);
                    }
                }
            }
        }

        String uomName = null;
        if (l.getUomId() != null) {
            uomName = uomJpaRepository.findById(l.getUomId()).map(UomEntity::getName).orElse(null);
        }

        return new StockAdjustmentLineItem(l.getId(), l.getVersion(), l.getProductId(), productCode,
                productName, isSerialized, l.getGridId(), gridCode, gridName, l.getContainerId(),
                containerCode, containerName, facilityName, l.getUomId(), uomName,
                l.getConversionFactor(), l.getQuantity(), l.getUnitCost(), l.getTotalAmount(),
                l.getSerialNumber());
    }
}
