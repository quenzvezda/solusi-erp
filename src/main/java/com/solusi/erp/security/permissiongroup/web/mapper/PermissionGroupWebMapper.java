package com.solusi.erp.security.permissiongroup.web.mapper;

import com.solusi.erp.core.dto.BaseAuditResponse;
import com.solusi.erp.core.mapper.AuditMapperHelper;
import com.solusi.erp.security.permissiongroup.domain.model.PermissionGroup;
import com.solusi.erp.security.permissiongroup.web.dto.PermissionGroupDetailResponse;
import com.solusi.erp.security.permissiongroup.web.dto.PermissionGroupSaveRequest;
import com.solusi.erp.security.permissiongroup.web.dto.PermissionGroupSummaryResponse;
import org.mapstruct.*;

/**
 * Web Mapper for PermissionGroup module.
 * Maps between Domain Model and Web DTOs.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {AuditMapperHelper.class})
public abstract class PermissionGroupWebMapper {

    public abstract PermissionGroupSummaryResponse toSummaryResponse(PermissionGroup domain);

    public abstract PermissionGroupDetailResponse toDetailResponse(PermissionGroup domain);

    public abstract PermissionGroupSaveRequest toSaveRequest(PermissionGroup domain);

    @AfterMapping
    protected void mapAuditFields(PermissionGroup domain, @MappingTarget BaseAuditResponse target) {
        if (domain.getMetadata() != null) {
            target.setId(domain.getId());
            target.setVersion(domain.getMetadata().version() != null ? domain.getMetadata().version().intValue() : null);
            target.setCreatedDate(domain.getMetadata().createdDate());
            target.setUpdatedDate(domain.getMetadata().updatedDate());
        }
    }

    @AfterMapping
    protected void mapLocalizedName(PermissionGroup domain, @MappingTarget PermissionGroupSummaryResponse target) {
        String lang = org.springframework.context.i18n.LocaleContextHolder.getLocale().getLanguage();
        target.setName("en".equals(lang) ? domain.getNameEn() : domain.getNameId());
    }
}
