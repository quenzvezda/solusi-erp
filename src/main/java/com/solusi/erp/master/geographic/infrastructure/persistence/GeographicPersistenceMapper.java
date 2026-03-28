package com.solusi.erp.master.geographic.infrastructure.persistence;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.master.geographic.domain.model.Geographic;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct Persistence Mapper for Geographic module.
 * Maps between JPA entity (com.solusi.erp.master.model.Geographic) and pure domain model.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface GeographicPersistenceMapper {

    @Mapping(target = "metadata", expression = "java(toAuditMetadata(entity))")
    @Mapping(target = "parentId",   expression = "java(entity.getParent() != null ? entity.getParent().getId()   : null)")
    @Mapping(target = "parentName", expression = "java(entity.getParent() != null ? entity.getParent().getName() : null)")
    Geographic toDomain(com.solusi.erp.master.model.Geographic entity);

    @Mapping(target = "id",          source = "metadata.id")
    @Mapping(target = "version",     source = "metadata.version")
    @Mapping(target = "createdDate", source = "metadata.createdDate")
    @Mapping(target = "createdBy",   source = "metadata.createdBy")
    @Mapping(target = "updatedDate", source = "metadata.updatedDate")
    @Mapping(target = "updatedBy",   source = "metadata.updatedBy")
    @Mapping(target = "parent",          ignore = true)
    @Mapping(target = "children",        ignore = true)
    @Mapping(target = "createdByUser",   ignore = true)
    @Mapping(target = "updatedByUser",   ignore = true)
    com.solusi.erp.master.model.Geographic toEntity(Geographic domain);

    default AuditMetadata toAuditMetadata(com.solusi.erp.master.model.Geographic entity) {
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
