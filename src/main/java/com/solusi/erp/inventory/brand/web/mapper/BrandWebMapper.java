package com.solusi.erp.inventory.brand.web.mapper;

import com.solusi.erp.core.dto.BaseAuditResponse;
import com.solusi.erp.core.mapper.AuditMapperHelper;
import com.solusi.erp.inventory.brand.domain.model.Brand;
import com.solusi.erp.inventory.brand.web.dto.BrandDetailResponse;
import com.solusi.erp.inventory.brand.web.dto.BrandSaveRequest;
import com.solusi.erp.inventory.brand.web.dto.BrandSummaryResponse;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Web Mapper for Brand module.
 * Maps between Domain Model and Web DTOs.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class BrandWebMapper {

    @Autowired
    protected AuditMapperHelper auditMapperHelper;

    public abstract BrandSummaryResponse toSummaryResponse(Brand domain);

    public abstract BrandDetailResponse toDetailResponse(Brand domain);

    public abstract BrandSaveRequest toSaveRequest(Brand domain);

    @AfterMapping
    protected void mapAuditFields(Brand domain, @MappingTarget BaseAuditResponse target) {
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
