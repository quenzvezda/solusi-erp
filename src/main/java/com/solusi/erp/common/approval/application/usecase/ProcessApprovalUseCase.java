package com.solusi.erp.common.approval.application.usecase;

import com.solusi.erp.common.approval.domain.model.ApprovalRequest;

/**
 * Use Case to approve or reject a request.
 */
public interface ProcessApprovalUseCase {
    ApprovalRequest approve(Long requestId, Long actorId, String notes);
    ApprovalRequest reject(Long requestId, Long actorId, String notes);
    ApprovalRequest forward(Long requestId, Long actorId, Long targetApproverId, String notes);
    ApprovalRequest approveAndForward(Long requestId, Long actorId, Long targetApproverId, String notes);
}
