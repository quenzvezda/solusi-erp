package com.solusi.erp.master.tax.web.mapper;

import com.solusi.erp.core.dto.BaseAuditResponse;
import com.solusi.erp.core.mapper.AuditMapperHelper;
import com.solusi.erp.master.tax.domain.model.Tax;
import com.solusi.erp.master.tax.web.dto.TaxDetailResponse;
import com.solusi.erp.master.tax.web.dto.TaxSaveRequest;
import com.solusi.erp.master.tax.web.dto.TaxSummaryResponse;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Web Mapper for Tax module.
 * Maps between Domain Model and Web DTOs.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class TaxWebMapper {

    @Autowired
    protected AuditMapperHelper auditMapperHelper;

    public abstract TaxSummaryResponse toSummaryResponse(Tax domain);

    public abstract TaxDetailResponse toDetailResponse(Tax domain);

    public abstract TaxSaveRequest toSaveRequest(Tax domain);

    @AfterMapping
    protected void mapAuditFields(Tax domain, @MappingTarget BaseAuditResponse target) {
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
