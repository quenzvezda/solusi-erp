package com.solusi.erp.common.approval.application.usecase;

import com.solusi.erp.common.approval.domain.model.ApprovalRequest;
import com.solusi.erp.common.approval.domain.repository.ApprovalRequestRepository;
import com.solusi.erp.core.exception.DomainException;

public class CancelApprovalRequestUseCaseImpl implements CancelApprovalRequestUseCase {

    private final ApprovalRequestRepository repository;

    public CancelApprovalRequestUseCaseImpl(ApprovalRequestRepository repository) {
        this.repository = repository;
    }

    @Override
    public ApprovalRequest execute(String referenceType, Long referenceId, Long actorId, String notes) {
        ApprovalRequest request = repository.findByReference(referenceType, referenceId)
                .orElseThrow(() -> new DomainException("msg.error.approval.not-found"));
        request.cancel(actorId, notes);
        return repository.save(request);
    }
}
