package com.solusi.erp.inventory.container.infrastructure.persistence;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.inventory.container.domain.model.Container;
import com.solusi.erp.inventory.model.Dimensions;
import com.solusi.erp.inventory.model.Grid;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ContainerPersistenceMapper {

    default Container toDomain(com.solusi.erp.inventory.model.Container entity) {
        if (entity == null) return null;
        AuditMetadata metadata = new AuditMetadata(
            entity.getId(),
            entity.getVersion() != null ? entity.getVersion().longValue() : null,
            entity.getCreatedDate(),
            entity.getCreatedBy(),
            entity.getUpdatedDate(),
            entity.getUpdatedBy()
        );
        Long gridId = entity.getGrid() != null ? entity.getGrid().getId() : null;
        String gridName = entity.getGrid() != null ? entity.getGrid().getName() : null;
        String facilityName = (entity.getGrid() != null && entity.getGrid().getFacility() != null)
            ? entity.getGrid().getFacility().getName() : null;
        java.math.BigDecimal length = entity.getDimensions() != null ? entity.getDimensions().getLength() : null;
        java.math.BigDecimal width = entity.getDimensions() != null ? entity.getDimensions().getWidth() : null;
        java.math.BigDecimal height = entity.getDimensions() != null ? entity.getDimensions().getHeight() : null;
        return new Container(metadata, gridId, gridName, facilityName, entity.getCode(), entity.getName(),
            entity.getBarcode(), length, width, height, entity.getMaxWeight(), entity.getNote(), entity.getIsActive());
    }

    default com.solusi.erp.inventory.model.Container toEntity(Container domain) {
        if (domain == null) return null;
        com.solusi.erp.inventory.model.Container entity = new com.solusi.erp.inventory.model.Container();
        if (domain.getMetadata().id() != null) entity.setId(domain.getMetadata().id());
        if (domain.getMetadata().version() != null) entity.setVersion(domain.getMetadata().version().intValue());
        entity.setCode(domain.getCode());
        entity.setName(domain.getName());
        entity.setBarcode(domain.getBarcode());
        entity.setMaxWeight(domain.getMaxWeight());
        entity.setNote(domain.getNote());
        entity.setIsActive(domain.getIsActive() != null ? domain.getIsActive() : Boolean.TRUE);
        if (domain.getGridId() != null) {
            Grid grid = new Grid();
            grid.setId(domain.getGridId());
            entity.setGrid(grid);
        }
        Dimensions dimensions = new Dimensions(domain.getLength(), domain.getWidth(), domain.getHeight());
        entity.setDimensions(dimensions);
        return entity;
    }
}
