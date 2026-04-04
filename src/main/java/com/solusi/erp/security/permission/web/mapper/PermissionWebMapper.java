package com.solusi.erp.security.permission.web.mapper;

import com.solusi.erp.core.dto.BaseAuditResponse;
import com.solusi.erp.core.mapper.AuditMapperHelper;
import com.solusi.erp.security.permission.domain.model.Permission;
import com.solusi.erp.security.permission.web.dto.PermissionDetailResponse;
import com.solusi.erp.security.permission.web.dto.PermissionSaveRequest;
import com.solusi.erp.security.permission.web.dto.PermissionSummaryResponse;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class PermissionWebMapper {

    @Autowired
    protected AuditMapperHelper auditMapperHelper;

    public abstract PermissionSummaryResponse toSummaryResponse(Permission domain);

    public abstract PermissionDetailResponse toDetailResponse(Permission domain);

    public abstract PermissionSaveRequest toSaveRequest(Permission domain);

    @AfterMapping
    protected void mapAuditFields(Permission domain, @MappingTarget BaseAuditResponse target) {
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
