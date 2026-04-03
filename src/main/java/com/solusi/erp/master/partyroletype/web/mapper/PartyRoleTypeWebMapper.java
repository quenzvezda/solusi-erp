package com.solusi.erp.master.partyroletype.web.mapper;

import com.solusi.erp.core.dto.BaseAuditResponse;
import com.solusi.erp.core.mapper.AuditMapperHelper;
import com.solusi.erp.master.partyroletype.domain.model.PartyRoleType;
import com.solusi.erp.master.partyroletype.web.dto.PartyRoleTypeDetailResponse;
import com.solusi.erp.master.partyroletype.web.dto.PartyRoleTypeSaveRequest;
import com.solusi.erp.master.partyroletype.web.dto.PartyRoleTypeSummaryResponse;
import org.mapstruct.*;

/**
 * Web Mapper for PartyRoleType module.
 * Maps between Domain Model and Web DTOs.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {AuditMapperHelper.class})
public abstract class PartyRoleTypeWebMapper {

    public abstract PartyRoleTypeSummaryResponse toSummaryResponse(PartyRoleType domain);

    public abstract PartyRoleTypeDetailResponse toDetailResponse(PartyRoleType domain);

    public abstract PartyRoleTypeSaveRequest toSaveRequest(PartyRoleType domain);

    @AfterMapping
    protected void mapAuditFields(PartyRoleType domain, @MappingTarget BaseAuditResponse target) {
        if (domain.getMetadata() != null) {
            target.setId(domain.getId());
            target.setVersion(domain.getMetadata().version() != null ? domain.getMetadata().version().intValue() : null);
            target.setCreatedDate(domain.getMetadata().createdDate());
            target.setUpdatedDate(domain.getMetadata().updatedDate());
        }
    }
}
