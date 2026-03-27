package com.solusi.erp.inventory.grid.infrastructure.persistence;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.inventory.grid.domain.model.Grid;
import com.solusi.erp.inventory.model.Facility;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface GridPersistenceMapper {

    default Grid toDomain(com.solusi.erp.inventory.model.Grid entity) {
        if (entity == null) return null;
        AuditMetadata metadata = new AuditMetadata(
            entity.getId(),
            entity.getVersion() != null ? entity.getVersion().longValue() : null,
            entity.getCreatedDate(),
            entity.getCreatedBy(),
            entity.getUpdatedDate(),
            entity.getUpdatedBy()
        );
        Long facilityId = entity.getFacility() != null ? entity.getFacility().getId() : null;
        String facilityName = entity.getFacility() != null ? entity.getFacility().getName() : null;
        return new Grid(metadata, facilityId, facilityName, entity.getCode(),
            entity.getName(), entity.getNote(), entity.getIsActive());
    }

    default com.solusi.erp.inventory.model.Grid toEntity(Grid domain) {
        if (domain == null) return null;
        com.solusi.erp.inventory.model.Grid entity = new com.solusi.erp.inventory.model.Grid();
        if (domain.getMetadata().id() != null) entity.setId(domain.getMetadata().id());
        if (domain.getMetadata().version() != null) entity.setVersion(domain.getMetadata().version().intValue());
        entity.setCode(domain.getCode());
        entity.setName(domain.getName());
        entity.setNote(domain.getNote());
        entity.setIsActive(domain.getIsActive() != null ? domain.getIsActive() : Boolean.TRUE);
        if (domain.getFacilityId() != null) {
            Facility facility = new Facility();
            facility.setId(domain.getFacilityId());
            entity.setFacility(facility);
        }
        return entity;
    }
}
