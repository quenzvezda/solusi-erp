package com.solusi.erp.common.approval.application.usecase;

import com.solusi.erp.common.approval.domain.model.ApprovalRequest;

/**
 * Use Case to create a new approval request.
 */
public interface CreateApprovalRequestUseCase {
    ApprovalRequest execute(String referenceType, Long referenceId, Long requesterId, Long approverId);
}
