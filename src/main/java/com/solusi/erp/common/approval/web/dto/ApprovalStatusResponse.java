package com.solusi.erp.common.approval.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for the full approval request status (used in the timeline fragment).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalStatusResponse {
    private Long requestId;
    private String status;
    private String referenceType;
    private Long referenceId;
    private String referenceCode;
    private String currentApproverName;
    private List<ApprovalHistoryResponse> histories;
}
