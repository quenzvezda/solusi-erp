package com.solusi.erp.common.approval.signature.application.usecase;

import com.solusi.erp.common.approval.signature.domain.model.ApprovalSignature;
import com.solusi.erp.common.approval.signature.domain.repository.ApprovalSignatureRepository;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

/**
 * Implementation of GetApprovalSignatureUrlUseCase.
 */
@RequiredArgsConstructor
public class GetApprovalSignatureUrlUseCaseImpl implements GetApprovalSignatureUrlUseCase {

    private final ApprovalSignatureRepository signatureRepository;

    @Override
    public Optional<ApprovalSignature> findByRequestId(Long requestId) {
        return signatureRepository.findByRequestId(requestId);
    }
}
