package com.solusi.erp.common.approval.application.usecase.query;

import com.solusi.erp.common.approval.domain.model.ApprovalRequest;
import com.solusi.erp.common.approval.domain.repository.ApprovalRequestRepository;
import java.util.Objects;
import java.util.Optional;

/**
 * Implementation: delegates to repository.
 */
public class FindApprovalRequestByReferenceUseCaseImpl implements FindApprovalRequestByReferenceUseCase {

    private final ApprovalRequestRepository repository;

    public FindApprovalRequestByReferenceUseCaseImpl(ApprovalRequestRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
    }

    @Override
    public Optional<ApprovalRequest> execute(String referenceType, Long referenceId) {
        Objects.requireNonNull(referenceType, "referenceType must not be null");
        Objects.requireNonNull(referenceId, "referenceId must not be null");
        return repository.findByReference(referenceType, referenceId);
    }
}
