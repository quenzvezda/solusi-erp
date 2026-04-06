package com.solusi.erp.common.approval.signature.domain.repository;

import com.solusi.erp.common.approval.signature.domain.model.ApprovalSignature;

import java.util.Optional;

/**
 * Domain repository port for ApprovalSignature.
 */
public interface ApprovalSignatureRepository {
    ApprovalSignature save(ApprovalSignature signature);
    Optional<ApprovalSignature> findByRequestId(Long requestId);
}
