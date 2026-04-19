package com.solusi.erp.purchasing.purchaserequisition.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import com.solusi.erp.purchasing.purchaserequisition.domain.model.PurchaseRequisitionPriority;
import com.solusi.erp.purchasing.purchaserequisition.domain.model.PurchaseRequisitionStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PurchaseRequisitionSaveRequest extends BaseAuditResponse {

    private String code;

    @NotNull(message = "{label.pr.requestDate} {validation.notnull.suffix}")
    private LocalDate requestDate;

    @NotNull(message = "{label.pr.requester} {validation.notnull.suffix}")
    private Long requesterId;

    @NotNull(message = "{label.pr.facility} {validation.notnull.suffix}")
    private Long facilityId;

    private String department;

    @NotNull(message = "{label.pr.priority} {validation.notnull.suffix}")
    private PurchaseRequisitionPriority priority = PurchaseRequisitionPriority.NORMAL;

    private String note;

    private PurchaseRequisitionStatus status;

    private Long approverId;

    @NotNull(message = "{label.pr.suggestedSupplier} {validation.notnull.suffix}")
    private Long suggestedSupplierId;
    
    @NotNull(message = "{label.pr.currency} {validation.notnull.suffix}")
    private Long currencyId;
    
    private String currencyCode;

    @Valid
    private List<PurchaseRequisitionLineRequest> lines = new ArrayList<>();
}
