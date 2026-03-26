package com.solusi.erp.common.approval.domain.model;

import java.time.LocalDateTime;

/**
 * Value Object/Entity representing a step in the approval process.
 */
public record ApprovalHistory(
    Long id,
    ApprovalAction action,
    Long actorId,
    Long targetApproverId,
    String notes,
    LocalDateTime actionDate
) {}
