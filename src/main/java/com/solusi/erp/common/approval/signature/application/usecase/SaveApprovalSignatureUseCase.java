package com.solusi.erp.common.approval.signature.application.usecase;

import com.solusi.erp.common.approval.signature.domain.model.ApprovalSignature;

/**
 * Use case for saving a digital signature captured during an approval action.
 */
public interface SaveApprovalSignatureUseCase {

    /**
     * Stores the signature image and persists the metadata.
     *
     * @param requestId      the ID of the ApprovalRequest being signed
     * @param signatureBase64 the PNG image encoded as base64 (data URI or raw base64)
     * @param signerUserId   the user ID of the person signing
     * @return the persisted ApprovalSignature domain object
     */
    ApprovalSignature execute(Long requestId, String signatureBase64, Long signerUserId);
}
