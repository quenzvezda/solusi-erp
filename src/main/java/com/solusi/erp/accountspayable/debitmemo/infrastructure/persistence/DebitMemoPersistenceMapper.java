package com.solusi.erp.accountspayable.debitmemo.infrastructure.persistence;

import com.solusi.erp.accountspayable.debitmemo.domain.model.DebitMemo;
import com.solusi.erp.accountspayable.debitmemo.domain.model.DebitMemoLine;
import com.solusi.erp.core.domain.model.AuditMetadata;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DebitMemoPersistenceMapper {

    default DebitMemo toDomain(DebitMemoEntity entity) {
        return DebitMemo.reconstitute(
                toAuditMetadata(entity),
                entity.getCode(),
                entity.getPurchaseReturnId(),
                entity.getPurchaseReturnCode(),
                entity.getVendorId(),
                entity.getCurrencyId(),
                entity.getMemoDate(),
                entity.getSettlementStatus(),
                entity.getSupplierMemoNumber(),
                entity.getSupplierMemoDate(),
                entity.getTaxDocumentNumber(),
                entity.getTaxDocumentDate(),
                entity.getNotes(),
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
    DebitMemoEntity toEntity(DebitMemo domain);

    @Mapping(target = "id", source = "line.id")
    @Mapping(target = "debitMemo", ignore = true)
    DebitMemoLineEntity toLineEntity(DebitMemoLine line);

    default AuditMetadata toAuditMetadata(DebitMemoEntity entity) {
        return new AuditMetadata(
                entity.getId(),
                entity.getVersion() != null ? entity.getVersion().longValue() : null,
                entity.getCreatedDate(),
                entity.getCreatedBy(),
                entity.getUpdatedDate(),
                entity.getUpdatedBy()
        );
    }

    default DebitMemoLine toLineDomain(DebitMemoLineEntity entity) {
        return new DebitMemoLine(
                entity.getId(),
                entity.getPurchaseReturnLineId(),
                entity.getProductId(),
                entity.getQuantity(),
                entity.getUomId(),
                entity.getDppAmountOriginal(),
                entity.getTaxAmountOriginal(),
                entity.getDppAmountBase(),
                entity.getTaxAmountBase()
        );
    }

    default List<DebitMemoLine> toLineDomainList(List<DebitMemoLineEntity> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream().map(this::toLineDomain).toList();
    }

    default List<DebitMemoLineEntity> toLineEntityList(List<DebitMemoLine> lines, DebitMemoEntity debitMemo) {
        if (lines == null) {
            return List.of();
        }
        return lines.stream().map(line -> {
            DebitMemoLineEntity entity = toLineEntity(line);
            entity.setDebitMemo(debitMemo);
            return entity;
        }).toList();
    }
}

