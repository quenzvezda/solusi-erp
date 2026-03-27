package com.solusi.erp.inventory.uom.infrastructure.persistence;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.inventory.uom.domain.model.UnitOfMeasure;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct Persistence Mapper for UnitOfMeasure module.
 * Maps between JPA entity (com.solusi.erp.inventory.model.UnitOfMeasure) and pure domain model.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UomPersistenceMapper {

    @Mapping(target = "metadata", expression = "java(toAuditMetadata(entity))")
    UnitOfMeasure toDomain(com.solusi.erp.inventory.model.UnitOfMeasure entity);

    @Mapping(target = "id", source = "metadata.id")
    @Mapping(target = "version", source = "metadata.version")
    @Mapping(target = "createdDate", source = "metadata.createdDate")
    @Mapping(target = "createdBy", source = "metadata.createdBy")
    @Mapping(target = "updatedDate", source = "metadata.updatedDate")
    @Mapping(target = "updatedBy", source = "metadata.updatedBy")
    com.solusi.erp.inventory.model.UnitOfMeasure toEntity(UnitOfMeasure domain);

    default AuditMetadata toAuditMetadata(com.solusi.erp.inventory.model.UnitOfMeasure entity) {
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
