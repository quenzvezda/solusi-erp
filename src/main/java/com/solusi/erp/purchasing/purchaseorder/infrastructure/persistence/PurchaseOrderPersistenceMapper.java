package com.solusi.erp.purchasing.purchaseorder.infrastructure.persistence;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrder;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrderLine;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PurchaseOrderPersistenceMapper {

    @Mapping(target = "metadata", expression = "java(toAuditMetadata(entity))")
    @Mapping(target = "lines", expression = "java(toLineDomainList(entity.getLines()))")
    PurchaseOrder toDomain(PurchaseOrderEntity entity);

    @Mapping(target = "id", source = "metadata.id")
    @Mapping(target = "version", expression = "java(domain.getMetadata().version() != null ? domain.getMetadata().version().intValue() : null)")
    @Mapping(target = "createdDate", source = "metadata.createdDate")
    @Mapping(target = "createdBy", source = "metadata.createdBy")
    @Mapping(target = "updatedDate", source = "metadata.updatedDate")
    @Mapping(target = "updatedBy", source = "metadata.updatedBy")
    @Mapping(target = "lines", ignore = true)
    PurchaseOrderEntity toEntity(PurchaseOrder domain);

    @Mapping(target = "id", source = "line.metadata.id")
    @Mapping(target = "version", expression = "java(line.getMetadata().version() != null ? line.getMetadata().version().intValue() : null)")
    @Mapping(target = "createdDate", source = "line.metadata.createdDate")
    @Mapping(target = "createdBy", source = "line.metadata.createdBy")
    @Mapping(target = "updatedDate", source = "line.metadata.updatedDate")
    @Mapping(target = "updatedBy", source = "line.metadata.updatedBy")
    @Mapping(target = "header", ignore = true)
    PurchaseOrderLineEntity toLineEntity(PurchaseOrderLine line);

    default AuditMetadata toAuditMetadata(PurchaseOrderEntity entity) {
        return new AuditMetadata(
            entity.getId(),
            entity.getVersion() != null ? entity.getVersion().longValue() : null,
            entity.getCreatedDate(),
            entity.getCreatedBy(),
            entity.getUpdatedDate(),
            entity.getUpdatedBy()
        );
    }

    default AuditMetadata toLineAuditMetadata(PurchaseOrderLineEntity entity) {
        return new AuditMetadata(
            entity.getId(),
            entity.getVersion() != null ? entity.getVersion().longValue() : null,
            entity.getCreatedDate(),
            entity.getCreatedBy(),
            entity.getUpdatedDate(),
            entity.getUpdatedBy()
        );
    }

    default PurchaseOrderLine toLineDomain(PurchaseOrderLineEntity entity) {
        return new PurchaseOrderLine(
            toLineAuditMetadata(entity),
            entity.getHeader() != null ? entity.getHeader().getId() : null,
            entity.getProductId(),
            entity.getQuantity(),
            entity.getReceivedQuantity(),
            entity.getUomId(),
            entity.getUnitPrice(),
            entity.getTaxRate(),
            entity.getPrLineId(),
            entity.getNote()
        );
    }

    default List<PurchaseOrderLine> toLineDomainList(List<PurchaseOrderLineEntity> entities) {
        if (entities == null) return List.of();
        return entities.stream().map(this::toLineDomain).toList();
    }

    default List<PurchaseOrderLineEntity> toLineEntityList(List<PurchaseOrderLine> lines,
                                                           PurchaseOrderEntity header) {
        if (lines == null) return List.of();
        return lines.stream().map(line -> {
            PurchaseOrderLineEntity entity = toLineEntity(line);
            entity.setHeader(header);
            return entity;
        }).toList();
    }
}
