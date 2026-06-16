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
    public ApprovalRequest execute(String referenceType, Long referenceId, String referenceCode, String documentPath,
                                   Long requesterId, Long approverId) {
        ApprovalRequest request = ApprovalRequest.createNew(
                referenceType, referenceId, referenceCode, documentPath, requesterId, approverId);
        return repository.save(request);
    }
}
