package com.solusi.erp.security.role.web.mapper;

import com.solusi.erp.core.dto.BaseAuditResponse;
import com.solusi.erp.core.mapper.AuditMapperHelper;
import com.solusi.erp.security.permission.domain.model.Permission;
import com.solusi.erp.security.permission.web.dto.PermissionSummaryResponse;
import com.solusi.erp.security.permission.web.mapper.PermissionWebMapper;
import com.solusi.erp.security.role.domain.model.Role;
import com.solusi.erp.security.role.web.dto.RoleDetailResponse;
import com.solusi.erp.security.role.web.dto.RoleSaveRequest;
import com.solusi.erp.security.role.web.dto.RoleSummaryResponse;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class RoleWebMapper {

    @Autowired
    protected AuditMapperHelper auditMapperHelper;

    @Mapping(target = "permissions", ignore = true)
    public abstract RoleSummaryResponse toSummaryResponse(Role domain);

    @Mapping(target = "permissions", ignore = true)
    public abstract RoleDetailResponse toDetailResponse(Role domain);

    public abstract RoleSaveRequest toSaveRequest(Role domain);

    @AfterMapping
    protected void mapAuditFields(Role domain, @MappingTarget BaseAuditResponse target) {
        if (domain.getMetadata() != null) {
            target.setId(domain.getId());
            target.setVersion(domain.getMetadata().version() != null ? domain.getMetadata().version().intValue() : null);
            target.setCreatedDate(domain.getMetadata().createdDate());
            target.setUpdatedDate(domain.getMetadata().updatedDate());
            target.setCreatedByName(auditMapperHelper.resolveUserDisplayName(domain.getMetadata().createdBy()));
            target.setUpdatedByName(auditMapperHelper.resolveUserDisplayName(domain.getMetadata().updatedBy()));
        }
    }

    public Set<PermissionSummaryResponse> toPermissionResponses(
            Set<Long> permissionIds,
            List<Permission> permissions,
            PermissionWebMapper permissionWebMapper) {
        if (permissionIds == null || permissionIds.isEmpty() || permissions == null || permissions.isEmpty()) {
            return Collections.emptySet();
        }
        Map<Long, Permission> permissionMap = permissions.stream()
                .filter(p -> p.getId() != null)
                .collect(Collectors.toMap(Permission::getId, p -> p, (a, b) -> a, LinkedHashMap::new));
        return permissionIds.stream()
                .map(permissionMap::get)
                .filter(Objects::nonNull)
                .map(permissionWebMapper::toSummaryResponse)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}

