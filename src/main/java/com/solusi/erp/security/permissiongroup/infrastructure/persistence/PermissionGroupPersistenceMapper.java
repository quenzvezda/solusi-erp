package com.solusi.erp.security.permissiongroup.infrastructure.persistence;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.security.permissiongroup.domain.model.PermissionGroup;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct Persistence Mapper for PermissionGroup.
 * Maps between JPA entity (permissiongroup.infrastructure.persistence.PermissionGroup) and pure domain model.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PermissionGroupPersistenceMapper {

    @Mapping(target = "metadata", expression = "java(toAuditMetadata(entity))")
    PermissionGroup toDomain(com.solusi.erp.security.permissiongroup.infrastructure.persistence.PermissionGroup entity);

    @Mapping(target = "id", source = "metadata.id")
    @Mapping(target = "version", expression = "java(domain.getMetadata().version() != null ? domain.getMetadata().version().intValue() : null)")
    @Mapping(target = "createdDate", source = "metadata.createdDate")
    @Mapping(target = "createdBy", source = "metadata.createdBy")
    @Mapping(target = "updatedDate", source = "metadata.updatedDate")
    @Mapping(target = "updatedBy", source = "metadata.updatedBy")
    @Mapping(target = "permissions", ignore = true)
    @Mapping(target = "createdByUser", ignore = true)
    @Mapping(target = "updatedByUser", ignore = true)
    com.solusi.erp.security.permissiongroup.infrastructure.persistence.PermissionGroup toEntity(PermissionGroup domain);

    default AuditMetadata toAuditMetadata(com.solusi.erp.security.permissiongroup.infrastructure.persistence.PermissionGroup entity) {
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
