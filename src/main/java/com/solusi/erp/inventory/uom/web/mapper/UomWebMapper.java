package com.solusi.erp.inventory.uom.web.mapper;

import com.solusi.erp.core.dto.BaseAuditResponse;
import com.solusi.erp.core.mapper.AuditMapperHelper;
import com.solusi.erp.inventory.uom.domain.model.UnitOfMeasure;
import com.solusi.erp.inventory.uom.web.dto.UomDetailResponse;
import com.solusi.erp.inventory.uom.web.dto.UomSaveRequest;
import com.solusi.erp.inventory.uom.web.dto.UomSummaryResponse;
import org.mapstruct.*;

/**
 * Web Mapper for UnitOfMeasure module.
 * Maps between Domain Model and Web DTOs.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {AuditMapperHelper.class})
public abstract class UomWebMapper {

    public abstract UomSummaryResponse toSummaryResponse(UnitOfMeasure domain);

    public abstract UomDetailResponse toDetailResponse(UnitOfMeasure domain);

    public abstract UomSaveRequest toSaveRequest(UnitOfMeasure domain);

    @AfterMapping
    protected void mapAuditFields(UnitOfMeasure domain, @MappingTarget BaseAuditResponse target) {
        if (domain.getMetadata() != null) {
            target.setId(domain.getId());
            target.setVersion(domain.getMetadata().version() != null ? domain.getMetadata().version().intValue() : null);
            target.setCreatedDate(domain.getMetadata().createdDate());
            target.setUpdatedDate(domain.getMetadata().updatedDate());
        }
    }
}
