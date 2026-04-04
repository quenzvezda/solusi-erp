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
    private String action; // "APPROVE" or "REJECT"

    private String notes;

    /** Base64-encoded PNG of the signature canvas. Optional but recommended for APPROVE. */
    private String signatureBase64;
}
