package com.solusi.erp.common.approval.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO representing a single step in the approval history timeline.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalHistoryResponse {
    private Long id;
    private String action;
    private Long actorId;
    private String actorName;
    private String notes;
    private LocalDateTime actionDate;
    private String signatureKey;
}
