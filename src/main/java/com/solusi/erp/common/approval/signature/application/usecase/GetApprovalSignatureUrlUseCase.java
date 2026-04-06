package com.solusi.erp.common.approval.signature.application.usecase;

import com.solusi.erp.common.approval.signature.domain.model.ApprovalSignature;

import java.util.Optional;

/**
 * Use case for retrieving the URL of a stored approval signature.
 */
public interface GetApprovalSignatureUrlUseCase {

    /**
     * Finds the signature for a given approval request and returns its storage URL.
     *
     * @param requestId the ID of the ApprovalRequest
     * @return optional ApprovalSignature domain object (empty if not yet signed)
     */
    Optional<ApprovalSignature> findByRequestId(Long requestId);
}
