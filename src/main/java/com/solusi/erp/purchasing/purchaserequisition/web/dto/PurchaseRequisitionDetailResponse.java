package com.solusi.erp.purchasing.purchaserequisition.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import com.solusi.erp.purchasing.purchaserequisition.domain.model.PurchaseRequisitionPriority;
import com.solusi.erp.purchasing.purchaserequisition.domain.model.PurchaseRequisitionStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PurchaseRequisitionDetailResponse extends BaseAuditResponse {

    private String code;
    private LocalDate requestDate;
    private Long requesterId;
    private String requesterName;
    private Long facilityId;
    private String facilityName;
    private String department;
    private PurchaseRequisitionPriority priority;
    private PurchaseRequisitionStatus status;
    private String note;
    private Long suggestedSupplierId;
    private String supplierName;
    private Long currencyId;
    private String currencyCode;
    private List<PurchaseRequisitionLineResponse> lines;
}
