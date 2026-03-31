package com.solusi.erp.inventory.grid.infrastructure.persistence;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.inventory.facility.infrastructure.persistence.FacilityEntity;
import com.solusi.erp.inventory.facility.infrastructure.persistence.FacilityJpaRepository;
import com.solusi.erp.inventory.grid.domain.model.Grid;
import org.springframework.stereotype.Component;

@Component
public class GridPersistenceMapper {

    private final FacilityJpaRepository facilityJpaRepository;

    public GridPersistenceMapper(FacilityJpaRepository facilityJpaRepository) {
        this.facilityJpaRepository = facilityJpaRepository;
    }

    public Grid toDomain(GridEntity entity) {
        if (entity == null) return null;
        AuditMetadata metadata = new AuditMetadata(
            entity.getId(),
            entity.getVersion() != null ? entity.getVersion().longValue() : null,
            entity.getCreatedDate(),
            entity.getCreatedBy(),
            entity.getUpdatedDate(),
            entity.getUpdatedBy()
        );
        String facilityName = null;
        String facilityCode = null;
        if (entity.getFacilityId() != null) {
            var facilityOpt = facilityJpaRepository.findById(entity.getFacilityId());
            facilityName = facilityOpt.map(FacilityEntity::getName).orElse(null);
            facilityCode = facilityOpt.map(FacilityEntity::getCode).orElse(null);
        }
        return new Grid(metadata, entity.getFacilityId(), facilityName, facilityCode,
            entity.getCode(), entity.getName(), entity.getNote(), entity.getIsActive());
    }

    public GridEntity toEntity(Grid domain) {
        if (domain == null) return null;
        GridEntity entity = new GridEntity();
        if (domain.getMetadata().id() != null) entity.setId(domain.getMetadata().id());
        if (domain.getMetadata().version() != null) entity.setVersion(domain.getMetadata().version().intValue());
        entity.setFacilityId(domain.getFacilityId());
        entity.setCode(domain.getCode());
        entity.setName(domain.getName());
        entity.setNote(domain.getNote());
        entity.setIsActive(domain.getIsActive() != null ? domain.getIsActive() : Boolean.TRUE);
        return entity;
    }
}
