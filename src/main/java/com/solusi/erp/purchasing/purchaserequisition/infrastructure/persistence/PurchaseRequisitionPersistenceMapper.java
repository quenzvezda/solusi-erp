package com.solusi.erp.purchasing.purchaserequisition.infrastructure.persistence;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.purchasing.purchaserequisition.domain.model.PurchaseRequisition;
import com.solusi.erp.purchasing.purchaserequisition.domain.model.PurchaseRequisitionLine;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PurchaseRequisitionPersistenceMapper {

    @Mapping(target = "metadata", expression = "java(toAuditMetadata(entity))")
    @Mapping(target = "lines", expression = "java(toLineDomainList(entity.getLines()))")
    PurchaseRequisition toDomain(PurchaseRequisitionEntity entity);

    @Mapping(target = "id", source = "metadata.id")
    @Mapping(target = "version", expression = "java(domain.getMetadata().version() != null ? domain.getMetadata().version().intValue() : null)")
    @Mapping(target = "createdDate", source = "metadata.createdDate")
    @Mapping(target = "createdBy", source = "metadata.createdBy")
    @Mapping(target = "updatedDate", source = "metadata.updatedDate")
    @Mapping(target = "updatedBy", source = "metadata.updatedBy")
    @Mapping(target = "lines", ignore = true)
    PurchaseRequisitionEntity toEntity(PurchaseRequisition domain);

    @Mapping(target = "metadata", expression = "java(toLineAuditMetadata(entity))")
    @Mapping(target = "headerId", expression = "java(entity.getHeader() != null ? entity.getHeader().getId() : null)")
    PurchaseRequisitionLine toLineDomain(PurchaseRequisitionLineEntity entity);

    @Mapping(target = "id", source = "line.metadata.id")
    @Mapping(target = "version", expression = "java(line.getMetadata().version() != null ? line.getMetadata().version().intValue() : null)")
    @Mapping(target = "createdDate", source = "line.metadata.createdDate")
    @Mapping(target = "createdBy", source = "line.metadata.createdBy")
    @Mapping(target = "updatedDate", source = "line.metadata.updatedDate")
    @Mapping(target = "updatedBy", source = "line.metadata.updatedBy")
    @Mapping(target = "header", ignore = true)
    PurchaseRequisitionLineEntity toLineEntity(PurchaseRequisitionLine line);

    default AuditMetadata toAuditMetadata(PurchaseRequisitionEntity entity) {
        return new AuditMetadata(
            entity.getId(),
            entity.getVersion() != null ? entity.getVersion().longValue() : null,
            entity.getCreatedDate(),
            entity.getCreatedBy(),
            entity.getUpdatedDate(),
            entity.getUpdatedBy()
        );
    }

    default AuditMetadata toLineAuditMetadata(PurchaseRequisitionLineEntity entity) {
        return new AuditMetadata(
            entity.getId(),
            entity.getVersion() != null ? entity.getVersion().longValue() : null,
            entity.getCreatedDate(),
            entity.getCreatedBy(),
            entity.getUpdatedDate(),
            entity.getUpdatedBy()
        );
    }

    default List<PurchaseRequisitionLine> toLineDomainList(List<PurchaseRequisitionLineEntity> entities) {
        if (entities == null) return List.of();
        return entities.stream().map(this::toLineDomain).toList();
    }

    default List<PurchaseRequisitionLineEntity> toLineEntityList(List<PurchaseRequisitionLine> lines,
                                                                  PurchaseRequisitionEntity header) {
        if (lines == null) return List.of();
        return lines.stream().map(line -> {
            PurchaseRequisitionLineEntity entity = toLineEntity(line);
            entity.setHeader(header);
            return entity;
        }).toList();
    }
}
