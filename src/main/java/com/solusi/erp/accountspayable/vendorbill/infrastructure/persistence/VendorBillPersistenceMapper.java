package com.solusi.erp.accountspayable.vendorbill.infrastructure.persistence;

import com.solusi.erp.accountspayable.vendorbill.domain.model.VendorBill;
import com.solusi.erp.accountspayable.vendorbill.domain.model.VendorBillGrRef;
import com.solusi.erp.accountspayable.vendorbill.domain.model.VendorBillLine;
import com.solusi.erp.core.domain.model.AuditMetadata;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface VendorBillPersistenceMapper {

    default VendorBill toDomain(VendorBillEntity entity) {
        return new VendorBill(
                toAuditMetadata(entity),
                entity.getCode(),
                entity.getVendorId(),
                entity.getVendorInvoiceNumber(),
                entity.getBillDate(),
                entity.getDueDate(),
                entity.getCurrencyId(),
                entity.getExchangeRate(),
                entity.getStatus(),
                entity.getSubtotal(),
                entity.getTaxAmount(),
                entity.getTotalAmount(),
                entity.getNotes(),
                toGrRefDomainList(entity.getGrRefs()),
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
    @Mapping(target = "grRefs", ignore = true)
    VendorBillEntity toEntity(VendorBill domain);

    @Mapping(target = "id", source = "line.id")
    @Mapping(target = "bill", ignore = true)
    VendorBillLineEntity toLineEntity(VendorBillLine line);

    @Mapping(target = "bill", ignore = true)
    VendorBillGrRefEntity toGrRefEntity(VendorBillGrRef grRef);

    default AuditMetadata toAuditMetadata(VendorBillEntity entity) {
        return new AuditMetadata(
                entity.getId(),
                entity.getVersion() != null ? entity.getVersion().longValue() : null,
                entity.getCreatedDate(),
                entity.getCreatedBy(),
                entity.getUpdatedDate(),
                entity.getUpdatedBy()
        );
    }

    default VendorBillLine toLineDomain(VendorBillLineEntity entity) {
        return new VendorBillLine(
                entity.getId(),
                entity.getGrLineId(),
                entity.getProductId(),
                entity.getProductName(),
                entity.getDescription(),
                entity.getQtyBilled(),
                entity.getUomId(),
                entity.getUomName(),
                entity.getUnitPrice(),
                entity.getInventoryAmount(),
                entity.getTaxAmount(),
                entity.getLineTotal()
        );
    }

    default List<VendorBillLine> toLineDomainList(List<VendorBillLineEntity> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream().map(this::toLineDomain).toList();
    }

    default List<VendorBillGrRef> toGrRefDomainList(List<VendorBillGrRefEntity> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream()
                .map(entity -> new VendorBillGrRef(entity.getBillId(), entity.getGrId()))
                .toList();
    }

    default List<VendorBillLineEntity> toLineEntityList(List<VendorBillLine> lines, VendorBillEntity bill) {
        if (lines == null) {
            return List.of();
        }
        return lines.stream().map(line -> {
            VendorBillLineEntity entity = toLineEntity(line);
            entity.setBill(bill);
            return entity;
        }).toList();
    }

    default List<VendorBillGrRefEntity> toGrRefEntityList(List<VendorBillGrRef> grRefs, VendorBillEntity bill) {
        if (grRefs == null) {
            return List.of();
        }
        return grRefs.stream().map(grRef -> {
            VendorBillGrRefEntity entity = toGrRefEntity(grRef);
            entity.setBillId(bill.getId());
            entity.setGrId(grRef.grId());
            entity.setBill(bill);
            return entity;
        }).toList();
    }
}
