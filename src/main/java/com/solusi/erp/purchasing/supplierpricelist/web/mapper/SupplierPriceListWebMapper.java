package com.solusi.erp.purchasing.supplierpricelist.web.mapper;

import com.solusi.erp.core.dto.BaseAuditResponse;
import com.solusi.erp.core.mapper.AuditMapperHelper;
import com.solusi.erp.purchasing.supplierpricelist.domain.model.SupplierPriceList;
import com.solusi.erp.purchasing.supplierpricelist.web.dto.SupplierPriceListDetailResponse;
import com.solusi.erp.purchasing.supplierpricelist.web.dto.SupplierPriceListSaveRequest;
import com.solusi.erp.purchasing.supplierpricelist.web.dto.SupplierPriceListSummaryResponse;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class SupplierPriceListWebMapper {

    @Autowired
    protected AuditMapperHelper auditMapperHelper;

    public abstract SupplierPriceListSummaryResponse toSummaryResponse(SupplierPriceList domain);

    public abstract SupplierPriceListDetailResponse toDetailResponse(SupplierPriceList domain);

    public abstract SupplierPriceListSaveRequest toSaveRequest(SupplierPriceList domain);

    @AfterMapping
    protected void mapAuditFields(SupplierPriceList domain, @MappingTarget BaseAuditResponse target) {
        if (domain.getMetadata() != null) {
            target.setId(domain.getId());
            target.setVersion(domain.getMetadata().version() != null ? domain.getMetadata().version().intValue() : null);
            target.setCreatedDate(domain.getMetadata().createdDate());
            target.setUpdatedDate(domain.getMetadata().updatedDate());
            target.setCreatedByName(auditMapperHelper.resolveUserDisplayName(domain.getMetadata().createdBy()));
            target.setUpdatedByName(auditMapperHelper.resolveUserDisplayName(domain.getMetadata().updatedBy()));
        }
    }
}
