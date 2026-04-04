package com.solusi.erp.common.approval.application.usecase;

import com.solusi.erp.common.approval.domain.model.ApprovalRequest;
import com.solusi.erp.common.approval.domain.repository.ApprovalRequestRepository;
import lombok.RequiredArgsConstructor;

/**
 * Implementation of CreateApprovalRequestUseCase.
 */
@RequiredArgsConstructor
public class CreateApprovalRequestUseCaseImpl implements CreateApprovalRequestUseCase {

    private final ApprovalRequestRepository repository;

    @Override
    public ApprovalRequest execute(String referenceType, Long referenceId, Long requesterId, Long approverId) {
        ApprovalRequest request = ApprovalRequest.createNew(referenceType, referenceId, requesterId, approverId);
        return repository.save(request);
    }
}
