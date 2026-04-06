package com.solusi.erp.common.approval.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Request DTO for processing an approval (approve or reject) with optional digital signature.
 */
@Data
public class ProcessApprovalRequest {

    @NotNull(message = "{label.approval.action} {validation.notnull.suffix}")
    private String action; // APPROVE_AND_FINISH, APPROVE_AND_FORWARD, FORWARD, REJECTED

    private String notes;

    /** Base64-encoded PNG of the signature canvas. Required for APPROVE_AND_FINISH, APPROVE_AND_FORWARD. */
    private String signatureBase64;

    /** Target approver party ID. Required for FORWARD, APPROVE_AND_FORWARD. */
    private Long targetApproverId;
}
