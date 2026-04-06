package com.solusi.erp.common.approval.signature.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * JPA Entity for ApprovalSignature.
 */
@Entity
@Table(name = "appr_signatures")
@Getter
@Setter
public class ApprovalSignatureEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_id", nullable = false, unique = true)
    private Long requestId;

    @Column(name = "storage_key", nullable = false, length = 500)
    private String storageKey;

    @Column(name = "bucket_name", nullable = false, length = 100)
    private String bucketName;

    @Column(name = "stored_at", nullable = false)
    private LocalDateTime storedAt;

    @Column(name = "signer_user_id", nullable = false)
    private Long signerUserId;
}
