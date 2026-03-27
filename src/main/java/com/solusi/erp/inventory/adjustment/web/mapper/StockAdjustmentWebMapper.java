package com.solusi.erp.inventory.adjustment.web.mapper;

import com.solusi.erp.core.dto.BaseAuditResponse;
import com.solusi.erp.inventory.adjustment.application.usecase.command.LineCommand;
import com.solusi.erp.inventory.adjustment.domain.model.StockAdjustment;
import com.solusi.erp.inventory.adjustment.domain.model.StockAdjustmentLineItem;
import com.solusi.erp.inventory.adjustment.web.dto.*;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct web mapper for StockAdjustment module.
 * Abstract class pattern (same as UomWebMapper) to support @AfterMapping.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class StockAdjustmentWebMapper {

    public abstract StockAdjustmentSummaryResponse toSummaryResponse(StockAdjustment domain);

    public abstract StockAdjustmentDetailResponse toDetailResponse(StockAdjustment domain);

    public abstract List<StockAdjustmentLineDetailResponse> toLineDetailResponseList(List<StockAdjustmentLineItem> lines);

    public abstract StockAdjustmentLineDetailResponse toLineDetailResponse(StockAdjustmentLineItem line);

    public abstract StockAdjustmentSaveRequest toSaveRequest(StockAdjustment domain);

    public abstract StockAdjustmentSaveLineRequest toSaveLineRequest(StockAdjustmentLineItem line);

    public abstract LineCommand toLineCommand(StockAdjustmentSaveLineRequest request);

    public abstract List<LineCommand> toLineCommands(List<StockAdjustmentSaveLineRequest> requests);

    @AfterMapping
    protected void mapAuditFields(StockAdjustment domain, @MappingTarget BaseAuditResponse target) {
        if (domain.getMetadata() != null) {
            target.setId(domain.getId());
            target.setVersion(domain.getMetadata().version() != null
                    ? domain.getMetadata().version().intValue() : null);
            target.setCreatedDate(domain.getMetadata().createdDate());
            target.setUpdatedDate(domain.getMetadata().updatedDate());
        }
    }
}
