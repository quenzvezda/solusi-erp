package com.solusi.erp.inventory.uomconversion.infrastructure.persistence;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.inventory.product.infrastructure.persistence.JpaProductRepository;
import com.solusi.erp.inventory.product.infrastructure.persistence.ProductEntity;
import com.solusi.erp.inventory.uom.infrastructure.persistence.UomEntity;
import com.solusi.erp.inventory.uom.infrastructure.persistence.UomJpaRepository;
import com.solusi.erp.inventory.uomconversion.domain.model.UomConversion;

/**
 * Manual persistence mapper for UomConversion module.
 * Maps between local JPA entity and pure domain model, resolving names via repos.
 */
public class UomConversionPersistenceMapper {

    private final JpaProductRepository productRepo;
    private final UomJpaRepository uomRepo;

    public UomConversionPersistenceMapper(JpaProductRepository productRepo, UomJpaRepository uomRepo) {
        this.productRepo = productRepo;
        this.uomRepo = uomRepo;
    }

    public UomConversion toDomain(UomConversionEntity entity) {
        if (entity == null) return null;

        AuditMetadata metadata = new AuditMetadata(
            entity.getId(),
            entity.getVersion() != null ? entity.getVersion().longValue() : null,
            entity.getCreatedDate(),
            entity.getCreatedBy(),
            entity.getUpdatedDate(),
            entity.getUpdatedBy()
        );

        String productCode = null, productName = null;
        ProductEntity product = productRepo.findById(entity.getProductId()).orElse(null);
        if (product != null) {
            productCode = product.getCode();
            productName = product.getName();
        }

        String fromUomName = uomRepo.findById(entity.getFromUomId())
            .map(UomEntity::getName).orElse(null);
        String toUomName = uomRepo.findById(entity.getToUomId())
            .map(UomEntity::getName).orElse(null);

        return new UomConversion(metadata,
            entity.getProductId(), productCode, productName,
            entity.getFromUomId(), fromUomName,
            entity.getToUomId(), toUomName,
            entity.getConversionFactor());
    }
}
