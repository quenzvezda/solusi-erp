package com.solusi.erp.master.tax.infrastructure.persistence;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.master.tax.domain.model.Tax;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct Persistence Mapper for Tax module.
 * Maps between JPA entity (com.solusi.erp.master.tax.infrastructure.persistence.Tax) and pure domain model.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TaxPersistenceMapper {

    @Mapping(target = "metadata", expression = "java(toAuditMetadata(entity))")
    Tax toDomain(com.solusi.erp.master.tax.infrastructure.persistence.Tax entity);

    @Mapping(target = "id", source = "metadata.id")
    @Mapping(target = "version", expression = "java(domain.getMetadata().version() != null ? domain.getMetadata().version().intValue() : null)")
    @Mapping(target = "createdDate", source = "metadata.createdDate")
    @Mapping(target = "createdBy", source = "metadata.createdBy")
    @Mapping(target = "updatedDate", source = "metadata.updatedDate")
    @Mapping(target = "updatedBy", source = "metadata.updatedBy")
    com.solusi.erp.master.tax.infrastructure.persistence.Tax toEntity(Tax domain);

    default AuditMetadata toAuditMetadata(com.solusi.erp.master.tax.infrastructure.persistence.Tax entity) {
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
