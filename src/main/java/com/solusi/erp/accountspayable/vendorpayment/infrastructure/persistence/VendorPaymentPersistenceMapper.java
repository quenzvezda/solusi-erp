package com.solusi.erp.accountspayable.vendorpayment.infrastructure.persistence;

import com.solusi.erp.accountspayable.vendorpayment.domain.model.VendorPayment;
import com.solusi.erp.accountspayable.vendorpayment.domain.model.VendorPaymentLine;
import com.solusi.erp.core.domain.model.AuditMetadata;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface VendorPaymentPersistenceMapper {

    default VendorPayment toDomain(VendorPaymentEntity entity) {
        return new VendorPayment(
                toAuditMetadata(entity),
                entity.getCode(),
                entity.getVendorId(),
                entity.getCurrencyId(),
                entity.getBankAccountId(),
                entity.getPaymentDate(),
                entity.getExchangeRate(),
                entity.getPaymentAmount(),
                entity.getStatus(),
                entity.getReference(),
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
    VendorPaymentEntity toEntity(VendorPayment domain);

    @Mapping(target = "id", source = "line.id")
    @Mapping(target = "vendorPayment", ignore = true)
    VendorPaymentLineEntity toLineEntity(VendorPaymentLine line);

    default AuditMetadata toAuditMetadata(VendorPaymentEntity entity) {
        return new AuditMetadata(
                entity.getId(),
                entity.getVersion() != null ? entity.getVersion().longValue() : null,
                entity.getCreatedDate(),
                entity.getCreatedBy(),
                entity.getUpdatedDate(),
                entity.getUpdatedBy()
        );
    }

    default VendorPaymentLine toLineDomain(VendorPaymentLineEntity entity) {
        return new VendorPaymentLine(
                entity.getId(),
                entity.getVendorBillId(),
                entity.getBillCode(),
                entity.getOutstandingAmount(),
                entity.getPaidAmount()
        );
    }

    default List<VendorPaymentLine> toLineDomainList(List<VendorPaymentLineEntity> entities) {
        if (entities == null) return List.of();
        return entities.stream().map(this::toLineDomain).toList();
    }

    default List<VendorPaymentLineEntity> toLineEntityList(List<VendorPaymentLine> lines, VendorPaymentEntity parent) {
        if (lines == null) return List.of();
        return lines.stream().map(line -> {
            VendorPaymentLineEntity entity = toLineEntity(line);
            entity.setVendorPayment(parent);
            return entity;
        }).toList();
    }
}
