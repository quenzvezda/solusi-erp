package com.solusi.erp.inventory.facility.web.mapper;

import com.solusi.erp.core.dto.BaseAuditResponse;
import com.solusi.erp.core.mapper.AuditMapperHelper;
import com.solusi.erp.inventory.facility.domain.model.Facility;
import com.solusi.erp.inventory.facility.web.dto.FacilityDetailResponse;
import com.solusi.erp.inventory.facility.web.dto.FacilitySaveRequest;
import com.solusi.erp.inventory.facility.web.dto.FacilitySummaryResponse;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {AuditMapperHelper.class})
public abstract class FacilityWebMapper {

    public abstract FacilitySummaryResponse toSummaryResponse(Facility domain);

    public abstract FacilityDetailResponse toDetailResponse(Facility domain);

    public abstract FacilitySaveRequest toSaveRequest(Facility domain);

    @AfterMapping
    protected void mapAuditFields(Facility domain, @MappingTarget BaseAuditResponse target) {
        if (domain.getMetadata() != null) {
            target.setId(domain.getId());
            target.setVersion(domain.getMetadata().version() != null ? domain.getMetadata().version().intValue() : null);
            target.setCreatedDate(domain.getMetadata().createdDate());
            target.setUpdatedDate(domain.getMetadata().updatedDate());
        }
    }
}
