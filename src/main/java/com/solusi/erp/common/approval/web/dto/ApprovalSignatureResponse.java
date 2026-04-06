package com.solusi.erp.common.approval.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for the digital signature associated with an approval request.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalSignatureResponse {
    private Long requestId;
    private String signatureUrl;
    private Long signerUserId;
    private LocalDateTime signedAt;
}
