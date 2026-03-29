package com.solusi.erp.security.mapper;

import com.solusi.erp.core.mapper.AuditMapperHelper;
import com.solusi.erp.security.dto.PermissionResponse;
import com.solusi.erp.security.dto.PermissionRequest;
import com.solusi.erp.security.permission.infrastructure.persistence.Permission;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {AuditMapperHelper.class})
public interface PermissionMapper {
    @Mapping(target = "permissionGroupId", source = "permissionGroup.id")
    @Mapping(target = "permissionGroupName", source = "permissionGroup.localizedName")
    PermissionResponse toResponse(Permission entity);

    List<PermissionResponse> toResponseList(List<Permission> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "permissionGroup", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "version", ignore = true)
    Permission toEntity(PermissionRequest request);
}
