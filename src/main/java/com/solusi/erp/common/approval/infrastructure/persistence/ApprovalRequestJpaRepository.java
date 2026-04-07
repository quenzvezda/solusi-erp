package com.solusi.erp.common.approval.infrastructure.persistence;

import com.solusi.erp.common.approval.domain.model.ApprovalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @EntityGraph(attributePaths = "histories")
    @Query("SELECT r FROM ApprovalRequestEntity r WHERE r.status = :status " +
           "AND (:keyword IS NULL OR LOWER(r.referenceCode) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(r.referenceType) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<ApprovalRequestEntity> findByStatusAndKeyword(
            @Param("status") ApprovalStatus status,
            @Param("keyword") String keyword,
            Pageable pageable);

    long countByStatus(ApprovalStatus status);

    @EntityGraph(attributePaths = "histories")
    Page<ApprovalRequestEntity> findByStatusAndCurrentApproverId(ApprovalStatus status, Long currentApproverId, Pageable pageable);

    @EntityGraph(attributePaths = "histories")
    @Query("SELECT r FROM ApprovalRequestEntity r WHERE r.status = :status AND r.currentApproverId = :approverId " +
           "AND (:keyword IS NULL OR LOWER(r.referenceCode) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(r.referenceType) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<ApprovalRequestEntity> findByStatusAndCurrentApproverIdAndKeyword(
            @Param("status") ApprovalStatus status,
            @Param("approverId") Long approverId,
            @Param("keyword") String keyword,
            Pageable pageable);

    long countByStatusAndCurrentApproverId(ApprovalStatus status, Long currentApproverId);

    @EntityGraph(attributePaths = "histories")
    @Query("SELECT r FROM ApprovalRequestEntity r ORDER BY r.id DESC")
    Page<ApprovalRequestEntity> findAllWithHistories(Pageable pageable);

    @EntityGraph(attributePaths = "histories")
    @Query("SELECT r FROM ApprovalRequestEntity r " +
           "WHERE (:keyword IS NULL OR LOWER(r.referenceCode) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "       OR LOWER(r.referenceType) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY r.id DESC")
    Page<ApprovalRequestEntity> findAllWithHistoriesAndKeyword(
            @Param("keyword") String keyword,
            Pageable pageable);
}
