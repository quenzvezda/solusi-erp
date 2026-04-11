package com.solusi.erp.purchasing.purchaserequisition.web.mapper;

import com.solusi.erp.core.dto.BaseAuditResponse;
import com.solusi.erp.core.mapper.AuditMapperHelper;
import com.solusi.erp.purchasing.purchaserequisition.application.usecase.command.LineInput;
import com.solusi.erp.purchasing.purchaserequisition.domain.model.PurchaseRequisition;
import com.solusi.erp.purchasing.purchaserequisition.domain.model.PurchaseRequisitionLine;
import com.solusi.erp.purchasing.purchaserequisition.web.dto.*;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class PurchaseRequisitionWebMapper {

    @Autowired
    protected AuditMapperHelper auditMapperHelper;

    @Mapping(target = "lineCount", expression = "java(domain.getLines() != null ? domain.getLines().size() : 0)")
    public abstract PurchaseRequisitionSummaryResponse toSummaryResponse(PurchaseRequisition domain);

    public abstract PurchaseRequisitionDetailResponse toDetailResponse(PurchaseRequisition domain);

    public abstract PurchaseRequisitionSaveRequest toSaveRequest(PurchaseRequisition domain);

    public abstract PurchaseRequisitionLineRequest toLineRequest(PurchaseRequisitionLine line);

    public abstract PurchaseRequisitionLineResponse toLineResponse(PurchaseRequisitionLine line);

    public abstract LineInput toLineInput(PurchaseRequisitionLineRequest request);

    public List<LineInput> toLineInputs(List<PurchaseRequisitionLineRequest> requests) {
        if (requests == null) return List.of();
        return requests.stream().map(this::toLineInput).toList();
    }

    @AfterMapping
    protected void mapAuditFields(PurchaseRequisition domain, @MappingTarget BaseAuditResponse target) {
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
