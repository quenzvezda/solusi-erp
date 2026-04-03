package com.solusi.erp.inventory.grid.web.mapper;

import com.solusi.erp.core.dto.BaseAuditResponse;
import com.solusi.erp.core.mapper.AuditMapperHelper;
import com.solusi.erp.inventory.grid.domain.model.Grid;
import com.solusi.erp.inventory.grid.web.dto.GridDetailResponse;
import com.solusi.erp.inventory.grid.web.dto.GridSaveRequest;
import com.solusi.erp.inventory.grid.web.dto.GridSummaryResponse;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {AuditMapperHelper.class})
public abstract class GridWebMapper {

    public abstract GridSummaryResponse toSummaryResponse(Grid domain);

    public abstract GridDetailResponse toDetailResponse(Grid domain);

    public abstract GridSaveRequest toSaveRequest(Grid domain);

    @AfterMapping
    protected void mapAuditFields(Grid domain, @MappingTarget BaseAuditResponse target) {
        if (domain.getMetadata() != null) {
            target.setId(domain.getId());
            target.setVersion(domain.getMetadata().version() != null ? domain.getMetadata().version().intValue() : null);
            target.setCreatedDate(domain.getMetadata().createdDate());
            target.setUpdatedDate(domain.getMetadata().updatedDate());
        }
    }
}
