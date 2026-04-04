package com.solusi.erp.common.approval.infrastructure.persistence;

import com.solusi.erp.common.approval.domain.model.ApprovalRequest;
import com.solusi.erp.common.approval.domain.model.ApprovalStatus;
import com.solusi.erp.common.approval.domain.repository.ApprovalRequestRepository;
import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.infrastructure.util.PageableMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    @Override
    public Page<ApprovalRequest> findPendingApprovals(Pageable pageable) {
        org.springframework.data.domain.Pageable springPageable = PageableMapper.toSpring(pageable);
        org.springframework.data.domain.Page<ApprovalRequestEntity> entityPage =
                jpaRepository.findByStatus(ApprovalStatus.PENDING, springPageable);
        List<ApprovalRequest> content = entityPage.getContent().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
        return new Page<>(content, entityPage.getNumber(), entityPage.getSize(), entityPage.getTotalElements());
    }

    @Override
    public long countPendingApprovals() {
        return jpaRepository.countByStatus(ApprovalStatus.PENDING);
    }
}
