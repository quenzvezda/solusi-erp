package com.solusi.erp.common.approval.signature.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Data JPA repository for ApprovalSignatureEntity.
 */
public interface ApprovalSignatureJpaRepository extends JpaRepository<ApprovalSignatureEntity, Long> {
    Optional<ApprovalSignatureEntity> findByRequestId(Long requestId);
}
