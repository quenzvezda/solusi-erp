package com.solusi.erp.security.mapper;

import com.solusi.erp.security.dto.PermissionResponse;
import com.solusi.erp.security.dto.PermissionRequest;
import com.solusi.erp.security.model.Permission;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    PermissionResponse toResponse(Permission entity);
    List<PermissionResponse> toResponseList(List<Permission> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "version", ignore = true)
    Permission toEntity(PermissionRequest request);
}
