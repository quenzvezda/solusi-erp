package com.solusi.erp.inventory.uomconversion.infrastructure.persistence;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.inventory.model.ProductUomConversion;
import com.solusi.erp.inventory.uomconversion.domain.model.UomConversion;
import org.mapstruct.*;

/**
 * MapStruct Persistence Mapper for UomConversion module.
 * Maps between JPA entity (ProductUomConversion) and pure domain model.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UomConversionPersistenceMapper {

    @Mapping(target = "metadata", expression = "java(toAuditMetadata(entity))")
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productCode", source = "product.code")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "fromUomId", source = "fromUom.id")
    @Mapping(target = "fromUomName", source = "fromUom.name")
    @Mapping(target = "toUomId", source = "toUom.id")
    @Mapping(target = "toUomName", source = "toUom.name")
    UomConversion toDomain(ProductUomConversion entity);

    default AuditMetadata toAuditMetadata(ProductUomConversion entity) {
        return new AuditMetadata(
            entity.getId(),
            entity.getVersion() != null ? entity.getVersion().longValue() : null,
            entity.getCreatedDate(),
            entity.getCreatedBy(),
            entity.getUpdatedDate(),
            entity.getUpdatedBy()
        );
    }
}
