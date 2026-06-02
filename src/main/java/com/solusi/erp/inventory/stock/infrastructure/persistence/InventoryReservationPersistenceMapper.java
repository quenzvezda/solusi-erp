package com.solusi.erp.inventory.stock.infrastructure.persistence;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.inventory.stock.domain.model.InventoryReservation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface InventoryReservationPersistenceMapper {

    @Mapping(target = "metadata", expression = "java(toAuditMetadata(entity))")
    InventoryReservation toDomain(InventoryReservationEntity entity);

    @Mapping(target = "id", source = "metadata.id")
    @Mapping(target = "version", expression = "java(domain.getMetadata().version() != null ? domain.getMetadata().version().intValue() : null)")
    @Mapping(target = "createdDate", source = "metadata.createdDate")
    @Mapping(target = "createdBy", source = "metadata.createdBy")
    @Mapping(target = "updatedDate", source = "metadata.updatedDate")
    @Mapping(target = "updatedBy", source = "metadata.updatedBy")
    InventoryReservationEntity toEntity(InventoryReservation domain);

    default AuditMetadata toAuditMetadata(InventoryReservationEntity entity) {
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
