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

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {AuditMapperHelper.class})
public abstract class PermissionWebMapper {

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
        }
    }
}
