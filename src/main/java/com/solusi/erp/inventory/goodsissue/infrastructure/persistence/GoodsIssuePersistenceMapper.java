package com.solusi.erp.inventory.goodsissue.infrastructure.persistence;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssue;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssueLine;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface GoodsIssuePersistenceMapper {

    default GoodsIssue toDomain(GoodsIssueEntity entity) {
        return new GoodsIssue(
                toAuditMetadata(entity),
                entity.getCode(),
                entity.getIssueDate(),
                entity.getReferenceType(),
                entity.getReferenceId(),
                entity.getReferenceCode(),
                entity.getPartyId(),
                entity.getPartyType(),
                entity.getFacilityId(),
                entity.getCurrencyId(),
                entity.getExchangeRate(),
                entity.getStatus(),
                entity.getCancelledDate(),
                entity.getCancelReason(),
                entity.getNote(),
                toLineDomainList(entity.getLines())
        );
    }

    @Mapping(target = "id", source = "metadata.id")
    @Mapping(target = "version", expression = "java(domain.getMetadata().version() != null ? domain.getMetadata().version().intValue() : null)")
    @Mapping(target = "createdDate", source = "metadata.createdDate")
    @Mapping(target = "createdBy", source = "metadata.createdBy")
    @Mapping(target = "updatedDate", source = "metadata.updatedDate")
    @Mapping(target = "updatedBy", source = "metadata.updatedBy")
    @Mapping(target = "lines", ignore = true)
    GoodsIssueEntity toEntity(GoodsIssue domain);

    @Mapping(target = "id", source = "metadata.id")
    @Mapping(target = "version", expression = "java(line.getMetadata().version() != null ? line.getMetadata().version().intValue() : null)")
    @Mapping(target = "createdDate", source = "metadata.createdDate")
    @Mapping(target = "createdBy", source = "metadata.createdBy")
    @Mapping(target = "updatedDate", source = "metadata.updatedDate")
    @Mapping(target = "updatedBy", source = "metadata.updatedBy")
    @Mapping(target = "header", ignore = true)
    GoodsIssueLineEntity toLineEntity(GoodsIssueLine line);

    default AuditMetadata toAuditMetadata(GoodsIssueEntity entity) {
        return new AuditMetadata(
                entity.getId(),
                entity.getVersion() != null ? entity.getVersion().longValue() : null,
                entity.getCreatedDate(),
                entity.getCreatedBy(),
                entity.getUpdatedDate(),
                entity.getUpdatedBy()
        );
    }

    default AuditMetadata toLineAuditMetadata(GoodsIssueLineEntity entity) {
        return new AuditMetadata(
                entity.getId(),
                entity.getVersion() != null ? entity.getVersion().longValue() : null,
                entity.getCreatedDate(),
                entity.getCreatedBy(),
                entity.getUpdatedDate(),
                entity.getUpdatedBy()
        );
    }

    default GoodsIssueLine toLineDomain(GoodsIssueLineEntity entity) {
        return GoodsIssueLine.reconstitute(
                toLineAuditMetadata(entity),
                entity.getReferenceLineId(),
                entity.getProductId(),
                entity.getSerialized(),
                entity.getQuantityIssued(),
                entity.getUomId(),
                entity.getBaseQuantity(),
                entity.getFacilityId(),
                entity.getGridId(),
                entity.getContainerId(),
                entity.getSerialNumber(),
                entity.getUnitCost(),
                entity.getInventoryAmount(),
                entity.getTaxBaseAmount(),
                entity.getTaxAmount(),
                entity.getClearingAmount(),
                entity.getValuationRefType(),
                entity.getValuationRefId(),
                entity.getValuationRefLineId()
        );
    }

    default List<GoodsIssueLine> toLineDomainList(List<GoodsIssueLineEntity> entities) {
        if (entities == null) return List.of();
        return entities.stream().map(this::toLineDomain).toList();
    }

    default List<GoodsIssueLineEntity> toLineEntityList(List<GoodsIssueLine> lines,
                                                        GoodsIssueEntity header) {
        if (lines == null) return List.of();
        return lines.stream().map(line -> {
            GoodsIssueLineEntity entity = toLineEntity(line);
            entity.setHeader(header);
            return entity;
        }).toList();
    }
}
