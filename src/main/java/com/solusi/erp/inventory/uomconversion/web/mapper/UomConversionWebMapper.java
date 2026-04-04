package com.solusi.erp.inventory.uomconversion.web.mapper;

import com.solusi.erp.core.dto.BaseAuditResponse;
import com.solusi.erp.core.mapper.AuditMapperHelper;
import com.solusi.erp.inventory.uomconversion.domain.model.UomConversion;
import com.solusi.erp.inventory.uomconversion.web.dto.UomConversionDetailResponse;
import com.solusi.erp.inventory.uomconversion.web.dto.UomConversionSaveRequest;
import com.solusi.erp.inventory.uomconversion.web.dto.UomConversionSummaryResponse;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Web Mapper for UomConversion module.
 * Maps between Domain Model and Web DTOs.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class UomConversionWebMapper {

    @Autowired
    protected AuditMapperHelper auditMapperHelper;

    public abstract UomConversionSummaryResponse toSummaryResponse(UomConversion domain);

    public abstract UomConversionDetailResponse toDetailResponse(UomConversion domain);

    public abstract UomConversionSaveRequest toSaveRequest(UomConversion domain);

    @AfterMapping
    protected void mapAuditFields(UomConversion domain, @MappingTarget BaseAuditResponse target) {
        if (domain.getMetadata() != null) {
            target.setId(domain.getId());
            target.setVersion(domain.getMetadata().version() != null
                ? domain.getMetadata().version().intValue() : null);
            target.setCreatedDate(domain.getMetadata().createdDate());
            target.setUpdatedDate(domain.getMetadata().updatedDate());
            target.setCreatedByName(auditMapperHelper.resolveUserDisplayName(domain.getMetadata().createdBy()));
            target.setUpdatedByName(auditMapperHelper.resolveUserDisplayName(domain.getMetadata().updatedBy()));
        }
    }
}
