package com.solusi.erp.master.mapper;

import com.solusi.erp.core.mapper.AuditMapperHelper;
import com.solusi.erp.master.dto.PartyRoleTypeRequest;
import com.solusi.erp.master.dto.PartyRoleTypeResponse;
import com.solusi.erp.master.model.PartyRoleType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
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
}
