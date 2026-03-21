package com.solusi.erp.master.mapper;

import com.solusi.erp.core.mapper.AuditMapperHelper;
import com.solusi.erp.master.dto.PartyRoleTypeRequest;
import com.solusi.erp.master.dto.PartyRoleTypeResponse;
import com.solusi.erp.master.model.PartyRoleType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

/**
 * Mapper for PartyRoleType.
 */
@Mapper(componentModel = "spring", 
        unmappedTargetPolicy = ReportingPolicy.IGNORE, 
        uses = {AuditMapperHelper.class})
public interface PartyRoleTypeMapper {

    PartyRoleTypeResponse toResponse(PartyRoleType entity);

    PartyRoleTypeRequest toRequest(PartyRoleType entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdByUser", ignore = true)
    @Mapping(target = "updatedByUser", ignore = true)
    PartyRoleType toEntity(PartyRoleTypeRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true) // Code is generated on create, usually not changed on update
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "createdByUser", ignore = true)
    @Mapping(target = "updatedByUser", ignore = true)
    void updateEntity(PartyRoleTypeRequest request, @MappingTarget PartyRoleType entity);
}
