package com.solusi.erp.common.approval.infrastructure.persistence;

import com.solusi.erp.common.approval.domain.model.ApprovalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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

    @EntityGraph(attributePaths = "histories")
    Page<ApprovalRequestEntity> findByStatus(ApprovalStatus status, Pageable pageable);

    long countByStatus(ApprovalStatus status);

    @EntityGraph(attributePaths = "histories")
    Page<ApprovalRequestEntity> findByStatusAndCurrentApproverId(ApprovalStatus status, Long currentApproverId, Pageable pageable);

    long countByStatusAndCurrentApproverId(ApprovalStatus status, Long currentApproverId);

    @EntityGraph(attributePaths = "histories")
    @Query("SELECT r FROM ApprovalRequestEntity r ORDER BY r.id DESC")
    Page<ApprovalRequestEntity> findAllWithHistories(Pageable pageable);
}
