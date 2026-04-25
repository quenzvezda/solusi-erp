package com.solusi.erp.common.approval.application.usecase.query;

import com.solusi.erp.common.approval.domain.model.ApprovalRequest;
import java.util.Optional;

/**
 * Query use case: find approval request by reference type and ID.
 * Prevents leaking repository abstraction across bounded contexts.
 */
public interface FindApprovalRequestByReferenceUseCase {
    Optional<ApprovalRequest> execute(String referenceType, Long referenceId);
}
