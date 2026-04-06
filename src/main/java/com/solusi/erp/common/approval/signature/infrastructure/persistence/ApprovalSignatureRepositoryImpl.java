package com.solusi.erp.common.approval.signature.infrastructure.persistence;

import com.solusi.erp.common.approval.signature.domain.model.ApprovalSignature;
import com.solusi.erp.common.approval.signature.domain.repository.ApprovalSignatureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JPA-backed implementation of ApprovalSignatureRepository.
 */
@Repository
@RequiredArgsConstructor
public class ApprovalSignatureRepositoryImpl implements ApprovalSignatureRepository {

    private final ApprovalSignatureJpaRepository jpaRepository;

    @Override
    public ApprovalSignature save(ApprovalSignature signature) {
        ApprovalSignatureEntity entity = toEntity(signature);
        ApprovalSignatureEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<ApprovalSignature> findByRequestId(Long requestId) {
        return jpaRepository.findByRequestId(requestId).map(this::toDomain);
    }

    private ApprovalSignatureEntity toEntity(ApprovalSignature domain) {
        ApprovalSignatureEntity entity = new ApprovalSignatureEntity();
        entity.setId(domain.getId());
        entity.setRequestId(domain.getRequestId());
        entity.setStorageKey(domain.getStorageKey());
        entity.setBucketName(domain.getBucketName());
        entity.setStoredAt(domain.getStoredAt());
        entity.setSignerUserId(domain.getSignerUserId());
        return entity;
    }

    private ApprovalSignature toDomain(ApprovalSignatureEntity entity) {
        return new ApprovalSignature(
                entity.getId(),
                entity.getRequestId(),
                entity.getStorageKey(),
                entity.getBucketName(),
                entity.getStoredAt(),
                entity.getSignerUserId()
        );
    }
}
