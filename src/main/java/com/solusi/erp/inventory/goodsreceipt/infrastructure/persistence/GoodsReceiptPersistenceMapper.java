package com.solusi.erp.inventory.goodsreceipt.infrastructure.persistence;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.inventory.goodsreceipt.domain.model.GoodsReceipt;
import com.solusi.erp.inventory.goodsreceipt.domain.model.GoodsReceiptLine;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface GoodsReceiptPersistenceMapper {

    default GoodsReceipt toDomain(GoodsReceiptEntity entity) {
        return new GoodsReceipt(
                toAuditMetadata(entity),
                entity.getCode(),
                entity.getReceiptDate(),
                entity.getReferenceType(),
                entity.getReferenceId(),
                entity.getSupplierId(),
                entity.getFacilityId(),
                entity.getCurrencyId(),
                entity.getExchangeRate(),
                entity.getStatus(),
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
    GoodsReceiptEntity toEntity(GoodsReceipt domain);

    @Mapping(target = "header", ignore = true)
    GoodsReceiptLineEntity toLineEntity(GoodsReceiptLine line);

    default AuditMetadata toAuditMetadata(GoodsReceiptEntity entity) {
        return new AuditMetadata(
            entity.getId(),
            entity.getVersion() != null ? entity.getVersion().longValue() : null,
            entity.getCreatedDate(),
            entity.getCreatedBy(),
            entity.getUpdatedDate(),
            entity.getUpdatedBy()
        );
    }

    default AuditMetadata toLineAuditMetadata(GoodsReceiptLineEntity entity) {
        return new AuditMetadata(
            entity.getId(),
            entity.getVersion() != null ? entity.getVersion().longValue() : null,
            entity.getCreatedDate(),
            entity.getCreatedBy(),
            entity.getUpdatedDate(),
            entity.getUpdatedBy()
        );
    }

    default GoodsReceiptLine toLineDomain(GoodsReceiptLineEntity entity) {
        return GoodsReceiptLine.prefill(
                entity.getReferenceLineId(),
                entity.getProductId(),
                null,
                null,
                entity.getQuantityReceived(),
                entity.getUomId(),
                entity.getContainerId(),
                null,
                entity.getBaseQuantity(),
                entity.getInventoryAmount(),
                entity.getTaxBaseAmount(),
                entity.getTaxAmount(),
                entity.getGrIrAmount(),
                entity.getSerialNumber()
        );
    }

    default List<GoodsReceiptLine> toLineDomainList(List<GoodsReceiptLineEntity> entities) {
        if (entities == null) return List.of();
        return entities.stream().map(this::toLineDomain).toList();
    }

    default List<GoodsReceiptLineEntity> toLineEntityList(List<GoodsReceiptLine> lines,
                                                           GoodsReceiptEntity header) {
        if (lines == null) return List.of();
        return lines.stream().map(line -> {
            GoodsReceiptLineEntity entity = toLineEntity(line);
            entity.setHeader(header);
            return entity;
        }).toList();
    }
}
