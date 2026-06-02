package com.solusi.erp.purchasing.purchasereturn.infrastructure.persistence;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturn;
import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturnLine;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PurchaseReturnPersistenceMapper {

    default PurchaseReturn toDomain(PurchaseReturnEntity entity) {
        return PurchaseReturn.reconstitute(
                toAuditMetadata(entity),
                entity.getCode(),
                entity.getReturnDate(),
                entity.getReferenceType(),
                entity.getReferenceId(),
                entity.getReferenceCode(),
                entity.getPurchaseOrderId(),
                entity.getPurchaseOrderCode(),
                entity.getSupplierId(),
                entity.getFacilityId(),
                entity.getCurrencyId(),
                entity.getExchangeRate(),
                entity.getStatus(),
                entity.getReason(),
                entity.getNote(),
                entity.getSubmittedByUserId(),
                entity.getGeneratedGoodsIssueId(),
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
    PurchaseReturnEntity toEntity(PurchaseReturn domain);

    @Mapping(target = "id", source = "metadata.id")
    @Mapping(target = "version", expression = "java(line.getMetadata().version() != null ? line.getMetadata().version().intValue() : null)")
    @Mapping(target = "createdDate", source = "metadata.createdDate")
    @Mapping(target = "createdBy", source = "metadata.createdBy")
    @Mapping(target = "updatedDate", source = "metadata.updatedDate")
    @Mapping(target = "updatedBy", source = "metadata.updatedBy")
    @Mapping(target = "header", ignore = true)
    PurchaseReturnLineEntity toLineEntity(PurchaseReturnLine line);

    default PurchaseReturnLine toLineDomain(PurchaseReturnLineEntity entity) {
        return PurchaseReturnLine.reconstitute(
                toLineAuditMetadata(entity),
                entity.getGoodsReceiptLineId(),
                entity.getProductId(),
                entity.isSerialized(),
                entity.getQuantity(),
                entity.getUomId(),
                entity.getBaseQuantity(),
                entity.getFacilityId(),
                entity.getGridId(),
                entity.getContainerId(),
                entity.getSerialNumbers(),
                entity.getReason(),
                entity.getNote(),
                entity.getValuationReferenceType(),
                entity.getValuationReferenceId(),
                entity.getValuationReferenceLineId(),
                entity.getUnitCost(),
                entity.getInventoryAmount(),
                entity.getTaxReversalAmount(),
                entity.getClearingAmount()
        );
    }

    default List<PurchaseReturnLine> toLineDomainList(List<PurchaseReturnLineEntity> entities) {
        return entities == null ? List.of() : entities.stream().map(this::toLineDomain).toList();
    }

    default List<PurchaseReturnLineEntity> toLineEntityList(List<PurchaseReturnLine> lines,
                                                           PurchaseReturnEntity header) {
        return lines.stream().map(line -> {
            PurchaseReturnLineEntity entity = toLineEntity(line);
            entity.setHeader(header);
            return entity;
        }).toList();
    }

    default AuditMetadata toAuditMetadata(PurchaseReturnEntity entity) {
        return new AuditMetadata(
                entity.getId(),
                entity.getVersion() != null ? entity.getVersion().longValue() : null,
                entity.getCreatedDate(),
                entity.getCreatedBy(),
                entity.getUpdatedDate(),
                entity.getUpdatedBy()
        );
    }

    default AuditMetadata toLineAuditMetadata(PurchaseReturnLineEntity entity) {
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
