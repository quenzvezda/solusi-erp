package com.solusi.erp.master.partyroletype.infrastructure.persistence;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.master.partyroletype.domain.model.PartyRoleType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct Persistence Mapper for PartyRoleType module.
 * Maps between JPA entity (com.solusi.erp.master.partyroletype.infrastructure.persistence.PartyRoleType) and pure domain model.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PartyRoleTypePersistenceMapper {

    @Mapping(target = "metadata", expression = "java(toAuditMetadata(entity))")
    PartyRoleType toDomain(com.solusi.erp.master.partyroletype.infrastructure.persistence.PartyRoleType entity);

    @Mapping(target = "id", source = "metadata.id")
    @Mapping(target = "version", expression = "java(domain.getMetadata().version() != null ? domain.getMetadata().version().intValue() : null)")
    @Mapping(target = "createdDate", source = "metadata.createdDate")
    @Mapping(target = "createdBy", source = "metadata.createdBy")
    @Mapping(target = "updatedDate", source = "metadata.updatedDate")
    @Mapping(target = "updatedBy", source = "metadata.updatedBy")
    com.solusi.erp.master.partyroletype.infrastructure.persistence.PartyRoleType toEntity(PartyRoleType domain);

    default AuditMetadata toAuditMetadata(com.solusi.erp.master.partyroletype.infrastructure.persistence.PartyRoleType entity) {
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
