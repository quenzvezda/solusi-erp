package com.solusi.erp.purchasing.purchaseorder.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PurchaseOrderJpaRepository extends JpaRepository<PurchaseOrderEntity, Long> {

    @Query("SELECT DISTINCT p FROM PurchaseOrderEntity p LEFT JOIN FETCH p.lines WHERE p.id = :id")
    Optional<PurchaseOrderEntity> findByIdWithLines(@Param("id") Long id);

    @Query(value = "SELECT DISTINCT p FROM PurchaseOrderEntity p LEFT JOIN FETCH p.lines WHERE " +
           "LOWER(p.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.note) LIKE LOWER(CONCAT('%', :keyword, '%'))",
           countQuery = "SELECT COUNT(p) FROM PurchaseOrderEntity p WHERE " +
           "LOWER(p.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.note) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<PurchaseOrderEntity> search(@Param("keyword") String keyword, Pageable pageable);

    @Query(value = "SELECT DISTINCT p FROM PurchaseOrderEntity p LEFT JOIN FETCH p.lines",
           countQuery = "SELECT COUNT(p) FROM PurchaseOrderEntity p")
    Page<PurchaseOrderEntity> findAllWithLines(Pageable pageable);
}
