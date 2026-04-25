package com.solusi.erp.purchasing.supplierpricelist.infrastructure.persistence;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.purchasing.supplierpricelist.domain.model.SupplierPriceList;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SupplierPriceListPersistenceMapper {

    @Mapping(target = "metadata", expression = "java(toAuditMetadata(entity))")
    SupplierPriceList toDomain(SupplierPriceListEntity entity);

    @Mapping(target = "id", source = "metadata.id")
    @Mapping(target = "version", expression = "java(domain.getMetadata().version() != null ? domain.getMetadata().version().intValue() : null)")
    @Mapping(target = "createdDate", source = "metadata.createdDate")
    @Mapping(target = "createdBy", source = "metadata.createdBy")
    @Mapping(target = "updatedDate", source = "metadata.updatedDate")
    @Mapping(target = "updatedBy", source = "metadata.updatedBy")
    SupplierPriceListEntity toEntity(SupplierPriceList domain);

    default AuditMetadata toAuditMetadata(SupplierPriceListEntity entity) {
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
