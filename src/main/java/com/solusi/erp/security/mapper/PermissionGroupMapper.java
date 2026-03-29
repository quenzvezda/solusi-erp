package com.solusi.erp.security.mapper;

import com.solusi.erp.core.mapper.AuditMapperHelper;
import com.solusi.erp.security.dto.PermissionGroupRequest;
import com.solusi.erp.security.dto.PermissionGroupResponse;
import com.solusi.erp.security.permissiongroup.infrastructure.persistence.PermissionGroup;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {AuditMapperHelper.class})
public interface PermissionGroupMapper {
    
    @Mapping(target = "name", ignore = true)
    PermissionGroupResponse toResponse(PermissionGroup entity);
    
    PermissionGroupRequest toRequest(PermissionGroup entity);
    
    List<PermissionGroupResponse> toResponseList(List<PermissionGroup> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "permissions", ignore = true)
    PermissionGroup toEntity(PermissionGroupRequest request);

    @AfterMapping
    default void setLocalizedName(@MappingTarget PermissionGroupResponse response, PermissionGroup entity) {
        response.setName(entity.getLocalizedName());
    }
}
