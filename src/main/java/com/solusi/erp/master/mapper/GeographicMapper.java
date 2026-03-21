package com.solusi.erp.master.mapper;

import com.solusi.erp.core.mapper.AuditMapperHelper;
import com.solusi.erp.master.dto.GeographicRequest;
import com.solusi.erp.master.dto.GeographicResponse;
import com.solusi.erp.master.form.GeographicUIForm;
import com.solusi.erp.master.model.Geographic;
import org.mapstruct.*;

/**
 * Mapper for Geographic entity and DTO.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {AuditMapperHelper.class})
public interface GeographicMapper {

    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "parentName", source = "parent.name")
    GeographicResponse toResponse(Geographic geographic);

    @Mapping(target = "parentName", source = "parent.name")
    @Mapping(target = "parentCode", source = "parent.code")
    GeographicUIForm toUIForm(Geographic geographic);

    @Mapping(target = "parentId", source = "parent.id")
    GeographicRequest toRequest(Geographic geographic);

    @Mapping(target = "parent", ignore = true) // Handled in Service
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "createdByUser", ignore = true)
    @Mapping(target = "updatedByUser", ignore = true)
    Geographic toEntity(GeographicRequest request);

    @Mapping(target = "parent", ignore = true) // Handled in Service
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "createdByUser", ignore = true)
    @Mapping(target = "updatedByUser", ignore = true)
    void updateEntity(GeographicRequest request, @MappingTarget Geographic geographic);
}
