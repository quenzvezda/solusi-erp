package com.solusi.erp.purchasing.purchaserequisition.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseRequisitionJpaRepository extends JpaRepository<PurchaseRequisitionEntity, Long> {

    @Query("SELECT DISTINCT p FROM PurchaseRequisitionEntity p LEFT JOIN FETCH p.lines WHERE p.id = :id")
    Optional<PurchaseRequisitionEntity> findByIdWithLines(@Param("id") Long id);

    @Query(value = "SELECT DISTINCT p FROM PurchaseRequisitionEntity p LEFT JOIN FETCH p.lines WHERE " +
           "LOWER(p.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.department) LIKE LOWER(CONCAT('%', :keyword, '%'))",
           countQuery = "SELECT COUNT(p) FROM PurchaseRequisitionEntity p WHERE " +
           "LOWER(p.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.department) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<PurchaseRequisitionEntity> search(@Param("keyword") String keyword, Pageable pageable);

    @Query(value = "SELECT DISTINCT p FROM PurchaseRequisitionEntity p LEFT JOIN FETCH p.lines",
           countQuery = "SELECT COUNT(p) FROM PurchaseRequisitionEntity p")
    Page<PurchaseRequisitionEntity> findAllWithLines(Pageable pageable);

    @Query("SELECT DISTINCT p FROM PurchaseRequisitionEntity p JOIN p.lines l " +
           "WHERE p.status = com.solusi.erp.purchasing.purchaserequisition.domain.model.PurchaseRequisitionStatus.APPROVED " +
           "AND l.suggestedSupplierId = :supplierId")
    List<PurchaseRequisitionEntity> findApprovedBySupplier(@Param("supplierId") Long supplierId);

    @Query("SELECT DISTINCT p FROM PurchaseRequisitionEntity p " +
           "WHERE p.status = com.solusi.erp.purchasing.purchaserequisition.domain.model.PurchaseRequisitionStatus.APPROVED")
    List<PurchaseRequisitionEntity> findAllApproved();
}
