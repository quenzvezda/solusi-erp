package com.solusi.erp.master.geographic.web.mapper;

import com.solusi.erp.core.dto.BaseAuditResponse;
import com.solusi.erp.core.mapper.AuditMapperHelper;
import com.solusi.erp.master.geographic.domain.model.Geographic;
import com.solusi.erp.master.geographic.web.dto.GeographicDetailResponse;
import com.solusi.erp.master.geographic.web.dto.GeographicSaveRequest;
import com.solusi.erp.master.geographic.web.dto.GeographicSummaryResponse;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Web Mapper for Geographic module.
 * Maps between Domain Model and Web DTOs.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class GeographicWebMapper {

    @Autowired
    protected AuditMapperHelper auditMapperHelper;

    public abstract GeographicSummaryResponse toSummaryResponse(Geographic domain);

    public abstract GeographicDetailResponse toDetailResponse(Geographic domain);

    public abstract GeographicSaveRequest toSaveRequest(Geographic domain);

    @AfterMapping
    protected void mapAuditFields(Geographic domain, @MappingTarget BaseAuditResponse target) {
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
