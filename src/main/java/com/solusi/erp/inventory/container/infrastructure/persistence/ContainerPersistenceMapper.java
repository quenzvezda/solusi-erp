package com.solusi.erp.inventory.container.infrastructure.persistence;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.inventory.container.domain.model.Container;
import com.solusi.erp.inventory.facility.infrastructure.persistence.FacilityEntity;
import com.solusi.erp.inventory.facility.infrastructure.persistence.FacilityJpaRepository;
import com.solusi.erp.inventory.grid.infrastructure.persistence.GridEntity;
import com.solusi.erp.inventory.grid.infrastructure.persistence.GridJpaRepository;
import com.solusi.erp.inventory.shared.embeddable.Dimensions;
import org.springframework.stereotype.Component;

@Component
public class ContainerPersistenceMapper {

    private final GridJpaRepository gridJpaRepository;
    private final FacilityJpaRepository facilityJpaRepository;

    public ContainerPersistenceMapper(GridJpaRepository gridJpaRepository, FacilityJpaRepository facilityJpaRepository) {
        this.gridJpaRepository = gridJpaRepository;
        this.facilityJpaRepository = facilityJpaRepository;
    }

    public Container toDomain(ContainerEntity entity) {
        if (entity == null) return null;
        AuditMetadata metadata = new AuditMetadata(
            entity.getId(),
            entity.getVersion() != null ? entity.getVersion().longValue() : null,
            entity.getCreatedDate(),
            entity.getCreatedBy(),
            entity.getUpdatedDate(),
            entity.getUpdatedBy()
        );
        String gridName = null;
        String facilityName = null;
        if (entity.getGridId() != null) {
            GridEntity grid = gridJpaRepository.findById(entity.getGridId()).orElse(null);
            if (grid != null) {
                gridName = grid.getName();
                if (grid.getFacilityId() != null) {
                    facilityName = facilityJpaRepository.findById(grid.getFacilityId())
                        .map(FacilityEntity::getName).orElse(null);
                }
            }
        }
        java.math.BigDecimal length = entity.getDimensions() != null ? entity.getDimensions().getLength() : null;
        java.math.BigDecimal width = entity.getDimensions() != null ? entity.getDimensions().getWidth() : null;
        java.math.BigDecimal height = entity.getDimensions() != null ? entity.getDimensions().getHeight() : null;
        return new Container(metadata, entity.getGridId(), gridName, facilityName, entity.getCode(), entity.getName(),
            entity.getBarcode(), length, width, height, entity.getMaxWeight(), entity.getNote(), entity.getIsActive());
    }

    public ContainerEntity toEntity(Container domain) {
        if (domain == null) return null;
        ContainerEntity entity = new ContainerEntity();
        if (domain.getMetadata().id() != null) entity.setId(domain.getMetadata().id());
        if (domain.getMetadata().version() != null) entity.setVersion(domain.getMetadata().version().intValue());
        entity.setGridId(domain.getGridId());
        entity.setCode(domain.getCode());
        entity.setName(domain.getName());
        entity.setBarcode(domain.getBarcode());
        entity.setMaxWeight(domain.getMaxWeight());
        entity.setNote(domain.getNote());
        entity.setIsActive(domain.getIsActive() != null ? domain.getIsActive() : Boolean.TRUE);
        Dimensions dimensions = new Dimensions(domain.getLength(), domain.getWidth(), domain.getHeight());
        entity.setDimensions(dimensions);
        return entity;
    }
}
