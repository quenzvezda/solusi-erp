package com.solusi.erp.common.approval.signature.application.usecase;

import com.solusi.erp.common.approval.domain.repository.ApprovalRequestRepository;
import com.solusi.erp.common.approval.signature.domain.model.ApprovalSignature;
import com.solusi.erp.common.approval.signature.domain.repository.ApprovalSignatureRepository;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.core.storage.domain.port.StorageProvider;
import lombok.RequiredArgsConstructor;

import java.util.Base64;
import java.util.UUID;

/**
 * Implementation of SaveApprovalSignatureUseCase.
 * Validates input, uploads the image to storage, then saves the metadata.
 */
@RequiredArgsConstructor
public class SaveApprovalSignatureUseCaseImpl implements SaveApprovalSignatureUseCase {

    private final ApprovalSignatureRepository signatureRepository;
    private final ApprovalRequestRepository approvalRequestRepository;
    private final StorageProvider storageProvider;
    private final String bucketName;

    @Override
    public ApprovalSignature execute(Long requestId, String signatureBase64, Long signerUserId) {
        if (signatureBase64 == null || signatureBase64.isBlank()) {
            throw new DomainException("msg.error.approval.signature.empty");
        }

        approvalRequestRepository.findById(requestId)
                .orElseThrow(() -> new DomainException("msg.error.approval.not-found"));

        byte[] imageBytes = decodeBase64(signatureBase64);

        String storageKey = "signatures/" + requestId + "/" + UUID.randomUUID() + ".png";
        storageProvider.store(bucketName, storageKey, imageBytes, "image/png");

        ApprovalSignature signature = ApprovalSignature.createNew(requestId, storageKey, bucketName, signerUserId);
        return signatureRepository.save(signature);
    }

    private byte[] decodeBase64(String base64) {
        String raw = base64;
        if (base64.contains(",")) {
            raw = base64.substring(base64.indexOf(',') + 1);
        }
        try {
            return Base64.getDecoder().decode(raw.trim());
        } catch (IllegalArgumentException e) {
            throw new DomainException("msg.error.approval.signature.invalid-format");
        }
    }
}
