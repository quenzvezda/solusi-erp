package com.solusi.erp.master.party.web.mapper;

import com.solusi.erp.core.dto.BaseAuditResponse;
import com.solusi.erp.core.mapper.AuditMapperHelper;
import com.solusi.erp.master.party.domain.model.Party;
import com.solusi.erp.master.party.domain.model.PartyAddressData;
import com.solusi.erp.master.party.domain.model.PartyContactData;
import com.solusi.erp.master.party.domain.model.PartyIdentificationData;
import com.solusi.erp.master.party.web.dto.PartyAddressRequest;
import com.solusi.erp.master.party.web.dto.PartyContactRequest;
import com.solusi.erp.master.party.web.dto.PartyDetailResponse;
import com.solusi.erp.master.party.web.dto.PartyIdentificationRequest;
import com.solusi.erp.master.party.web.dto.PartySaveRequest;
import com.solusi.erp.master.party.web.dto.PartySummaryResponse;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class PartyWebMapper {

    @Autowired
    protected AuditMapperHelper auditMapperHelper;

    public abstract PartySummaryResponse toSummaryResponse(Party domain);

    public abstract PartyDetailResponse toDetailResponse(Party domain);

    public abstract PartySaveRequest toSaveRequest(Party domain);

    protected abstract PartyContactRequest toContactRequest(PartyContactData data);

    protected abstract PartyAddressRequest toAddressRequest(PartyAddressData data);

    protected abstract PartyIdentificationRequest toIdentificationRequest(PartyIdentificationData data);

    @AfterMapping
    protected void mapAuditFields(Party domain, @MappingTarget BaseAuditResponse target) {
        if (domain.getMetadata() != null) {
            target.setId(domain.getId());
            target.setVersion(domain.getMetadata().version() != null ? domain.getMetadata().version().intValue() : null);
            target.setCreatedDate(domain.getMetadata().createdDate());
            target.setUpdatedDate(domain.getMetadata().updatedDate());
            target.setCreatedByName(auditMapperHelper.resolveUserDisplayName(domain.getMetadata().createdBy()));
            target.setUpdatedByName(auditMapperHelper.resolveUserDisplayName(domain.getMetadata().updatedBy()));
        }
    }
}
