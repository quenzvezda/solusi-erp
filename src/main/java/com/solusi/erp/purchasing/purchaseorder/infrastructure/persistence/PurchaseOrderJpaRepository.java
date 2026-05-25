package com.solusi.erp.purchasing.purchaseorder.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface PurchaseOrderJpaRepository extends JpaRepository<PurchaseOrderEntity, Long> {

    @Query("SELECT DISTINCT p FROM PurchaseOrderEntity p LEFT JOIN FETCH p.lines WHERE p.id = :id")
    Optional<PurchaseOrderEntity> findByIdWithLines(@Param("id") Long id);

    @Query(value = "SELECT DISTINCT p FROM PurchaseOrderEntity p LEFT JOIN FETCH p.lines WHERE " +
           "p.active = true AND (" +
           "LOWER(p.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.note) LIKE LOWER(CONCAT('%', :keyword, '%')))",
           countQuery = "SELECT COUNT(p) FROM PurchaseOrderEntity p WHERE " +
           "p.active = true AND (" +
           "LOWER(p.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.note) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<PurchaseOrderEntity> search(@Param("keyword") String keyword, Pageable pageable);

    @Query(value = "SELECT DISTINCT p FROM PurchaseOrderEntity p LEFT JOIN FETCH p.lines WHERE p.active = true",
           countQuery = "SELECT COUNT(p) FROM PurchaseOrderEntity p WHERE p.active = true")
    Page<PurchaseOrderEntity> findAllWithLines(Pageable pageable);

    @Query("""
        select new com.solusi.erp.purchasing.purchaseorder.infrastructure.persistence.PrLineConsumptionRow(
            line.prLineId,
            coalesce(sum(line.quantity), 0)
        )
        from PurchaseOrderLineEntity line
        join line.header header
        where line.prLineId in :prLineIds
          and header.status in :statuses
        group by line.prLineId
    """)
    List<PrLineConsumptionRow> sumConsumedByPrLineIds(@Param("prLineIds") Set<Long> prLineIds,
                                                      @Param("statuses") Set<com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrderStatus> statuses);
}
