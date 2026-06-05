package com.solusi.erp.accountspayable.debitmemoallocation.infrastructure.persistence;

import com.solusi.erp.accountspayable.debitmemoallocation.domain.model.DebitMemoAllocation;
import com.solusi.erp.accountspayable.debitmemoallocation.domain.model.DebitMemoAllocationLine;
import com.solusi.erp.core.domain.model.AuditMetadata;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DebitMemoAllocationPersistenceMapper {

    default DebitMemoAllocation toDomain(DebitMemoAllocationEntity entity) {
        return DebitMemoAllocation.reconstitute(
                toAuditMetadata(entity),
                entity.getCode(),
                entity.getDebitMemoId(),
                entity.getDebitMemoCode(),
                entity.getAllocationDate(),
                entity.getStatus(),
                entity.getApplyJournalEntryId(),
                entity.getReversalJournalEntryId(),
                entity.getReversalDate(),
                entity.getReversalReason(),
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
    DebitMemoAllocationEntity toEntity(DebitMemoAllocation domain);

    @Mapping(target = "id", source = "line.id")
    @Mapping(target = "allocation", ignore = true)
    DebitMemoAllocationLineEntity toLineEntity(DebitMemoAllocationLine line);

    default AuditMetadata toAuditMetadata(DebitMemoAllocationEntity entity) {
        return new AuditMetadata(
                entity.getId(),
                entity.getVersion() != null ? entity.getVersion().longValue() : null,
                entity.getCreatedDate(),
                entity.getCreatedBy(),
                entity.getUpdatedDate(),
                entity.getUpdatedBy()
        );
    }

    default DebitMemoAllocationLine toLineDomain(DebitMemoAllocationLineEntity entity) {
        return new DebitMemoAllocationLine(
                entity.getId(),
                entity.getVendorBillId(),
                entity.getVendorBillCode(),
                entity.getDebitMemoRemainingAtDraft(),
                entity.getVendorBillOutstandingAtDraft(),
                entity.getAppliedGrossOriginal(),
                entity.getAppliedDppOriginal(),
                entity.getAppliedTaxOriginal(),
                entity.getGrirReversalBase(),
                entity.getTaxReversalBase(),
                entity.getVendorBillExchangeRate(),
                entity.getApReductionBase(),
                entity.getFxLossBase(),
                entity.getFxGainBase()
        );
    }

    default List<DebitMemoAllocationLine> toLineDomainList(List<DebitMemoAllocationLineEntity> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream().map(this::toLineDomain).toList();
    }

    default List<DebitMemoAllocationLineEntity> toLineEntityList(List<DebitMemoAllocationLine> lines,
                                                                 DebitMemoAllocationEntity allocation) {
        if (lines == null) {
            return List.of();
        }
        return lines.stream().map(line -> {
            DebitMemoAllocationLineEntity entity = toLineEntity(line);
            entity.setAllocation(allocation);
            return entity;
        }).toList();
    }
}
