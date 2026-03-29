package com.solusi.erp.security.permission.infrastructure.persistence;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.security.permission.domain.model.Permission;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PermissionPersistenceMapper {

    @Mapping(target = "metadata", expression = "java(toAuditMetadata(entity))")
    @Mapping(target = "permissionGroupId", expression = "java(entity.getPermissionGroup() != null ? entity.getPermissionGroup().getId() : null)")
    Permission toDomain(com.solusi.erp.security.permission.infrastructure.persistence.Permission entity);

    @Mapping(target = "id", source = "metadata.id")
    @Mapping(target = "version", expression = "java(domain.getMetadata().version() != null ? domain.getMetadata().version().intValue() : null)")
    @Mapping(target = "createdDate", source = "metadata.createdDate")
    @Mapping(target = "createdBy", source = "metadata.createdBy")
    @Mapping(target = "updatedDate", source = "metadata.updatedDate")
    @Mapping(target = "updatedBy", source = "metadata.updatedBy")
    @Mapping(target = "permissionGroup", ignore = true)
    @Mapping(target = "createdByUser", ignore = true)
    @Mapping(target = "updatedByUser", ignore = true)
    com.solusi.erp.security.permission.infrastructure.persistence.Permission toEntity(Permission domain);

    default AuditMetadata toAuditMetadata(com.solusi.erp.security.permission.infrastructure.persistence.Permission entity) {
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
