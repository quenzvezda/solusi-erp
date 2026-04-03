package com.solusi.erp.common.approval.infrastructure.persistence;

import com.solusi.erp.common.approval.domain.model.ApprovalRequest;
import com.solusi.erp.common.approval.domain.repository.ApprovalRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Implementation of ApprovalRequestRepository using JPA.
 */
@Repository
@RequiredArgsConstructor
public class ApprovalRequestRepositoryImpl implements ApprovalRequestRepository {

    private final ApprovalRequestJpaRepository jpaRepository;
    private final ApprovalPersistenceMapper mapper;

    @Override
    public ApprovalRequest save(ApprovalRequest request) {
        ApprovalRequestEntity entity = mapper.toEntity(request);
        ApprovalRequestEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<ApprovalRequest> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<ApprovalRequest> findByReference(String referenceType, Long referenceId) {
        return jpaRepository.findByReferenceTypeAndReferenceId(referenceType, referenceId)
            .map(mapper::toDomain);
    }
}
