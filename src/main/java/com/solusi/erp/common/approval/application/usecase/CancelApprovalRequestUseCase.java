package com.solusi.erp.common.approval.application.usecase;

import com.solusi.erp.common.approval.domain.model.ApprovalRequest;

public interface CancelApprovalRequestUseCase {

    ApprovalRequest execute(String referenceType, Long referenceId, Long actorId, String notes);
}
