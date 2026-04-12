package com.solusi.erp.purchasing.purchaseorder.web.mapper;

import com.solusi.erp.core.dto.BaseAuditResponse;
import com.solusi.erp.core.mapper.AuditMapperHelper;
import com.solusi.erp.purchasing.purchaseorder.application.usecase.command.PoLineInput;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrder;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrderLine;
import com.solusi.erp.purchasing.purchaseorder.web.dto.*;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class PurchaseOrderWebMapper {

    @Autowired
    protected AuditMapperHelper auditMapperHelper;

    @Mapping(target = "lineCount", expression = "java(domain.getLines() != null ? domain.getLines().size() : 0)")
    public abstract PurchaseOrderSummaryResponse toSummaryResponse(PurchaseOrder domain);

    public abstract PurchaseOrderDetailResponse toDetailResponse(PurchaseOrder domain);

    public abstract PurchaseOrderSaveRequest toSaveRequest(PurchaseOrder domain);

    public abstract PurchaseOrderLineRequest toLineRequest(PurchaseOrderLine line);

    public abstract PurchaseOrderLineResponse toLineResponse(PurchaseOrderLine line);

    public abstract PoLineInput toLineInput(PurchaseOrderLineRequest request);

    public List<PoLineInput> toLineInputs(List<PurchaseOrderLineRequest> requests) {
        if (requests == null) return List.of();
        return requests.stream().map(this::toLineInput).toList();
    }

    @AfterMapping
    protected void mapAuditFields(PurchaseOrder domain, @MappingTarget BaseAuditResponse target) {
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
