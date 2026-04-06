package com.solusi.erp.common.approval.signature.domain.model;

import java.time.LocalDateTime;

/**
 * Domain model representing a captured digital signature for an approval action.
 */
public class ApprovalSignature {

    private Long id;
    private final Long requestId;
    private final String storageKey;
    private final String bucketName;
    private final LocalDateTime storedAt;
    private final Long signerUserId;

    public ApprovalSignature(Long id, Long requestId, String storageKey, String bucketName,
                             LocalDateTime storedAt, Long signerUserId) {
        this.id = id;
        this.requestId = requestId;
        this.storageKey = storageKey;
        this.bucketName = bucketName;
        this.storedAt = storedAt;
        this.signerUserId = signerUserId;
    }

    public static ApprovalSignature createNew(Long requestId, String storageKey, String bucketName, Long signerUserId) {
        return new ApprovalSignature(null, requestId, storageKey, bucketName, LocalDateTime.now(), signerUserId);
    }

    public Long getId() { return id; }
    public Long getRequestId() { return requestId; }
    public String getStorageKey() { return storageKey; }
    public String getBucketName() { return bucketName; }
    public LocalDateTime getStoredAt() { return storedAt; }
    public Long getSignerUserId() { return signerUserId; }
}
