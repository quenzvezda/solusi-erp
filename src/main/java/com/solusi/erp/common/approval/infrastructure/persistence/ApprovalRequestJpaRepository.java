package com.solusi.erp.common.approval.infrastructure.persistence;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Spring Data JPA Repository for ApprovalRequestEntity.
 */
public interface ApprovalRequestJpaRepository extends JpaRepository<ApprovalRequestEntity, Long> {
    
    @EntityGraph(attributePaths = "histories")
    Optional<ApprovalRequestEntity> findByReferenceTypeAndReferenceId(String referenceType, Long referenceId);

    @EntityGraph(attributePaths = "histories")
    @Override
    Optional<ApprovalRequestEntity> findById(Long id);
}
