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

    @Query(value = "SELECT DISTINCT p FROM PurchaseRequisitionEntity p " +
           "LEFT JOIN FETCH p.lines " +
           "LEFT JOIN com.solusi.erp.master.party.infrastructure.persistence.Party req ON req.id = p.requesterId " +
           "LEFT JOIN com.solusi.erp.master.currency.infrastructure.persistence.Currency cur ON cur.id = p.currencyId " +
           "WHERE " +
           "LOWER(p.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(req.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.department) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(cast(p.priority as String)) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(cast(p.status as String)) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(cur.alias) LIKE LOWER(CONCAT('%', :keyword, '%'))",
           countQuery = "SELECT COUNT(DISTINCT p) FROM PurchaseRequisitionEntity p " +
           "LEFT JOIN com.solusi.erp.master.party.infrastructure.persistence.Party req ON req.id = p.requesterId " +
           "LEFT JOIN com.solusi.erp.master.currency.infrastructure.persistence.Currency cur ON cur.id = p.currencyId " +
           "WHERE " +
           "LOWER(p.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(req.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.department) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(cast(p.priority as String)) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(cast(p.status as String)) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(cur.alias) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<PurchaseRequisitionEntity> search(@Param("keyword") String keyword, Pageable pageable);

    @Query(value = "SELECT DISTINCT p FROM PurchaseRequisitionEntity p " +
           "LEFT JOIN FETCH p.lines " +
           "LEFT JOIN com.solusi.erp.master.party.infrastructure.persistence.Party req ON req.id = p.requesterId " +
           "LEFT JOIN com.solusi.erp.master.currency.infrastructure.persistence.Currency cur ON cur.id = p.currencyId",
           countQuery = "SELECT COUNT(DISTINCT p) FROM PurchaseRequisitionEntity p " +
           "LEFT JOIN com.solusi.erp.master.party.infrastructure.persistence.Party req ON req.id = p.requesterId " +
           "LEFT JOIN com.solusi.erp.master.currency.infrastructure.persistence.Currency cur ON cur.id = p.currencyId")
    Page<PurchaseRequisitionEntity> findAllWithJoin(Pageable pageable);

    @Query("SELECT DISTINCT p FROM PurchaseRequisitionEntity p " +
           "WHERE p.status = com.solusi.erp.purchasing.purchaserequisition.domain.model.PurchaseRequisitionStatus.APPROVED " +
           "AND p.suggestedSupplierId = :supplierId")
    List<PurchaseRequisitionEntity> findApprovedBySupplier(@Param("supplierId") Long supplierId);

    @Query("SELECT DISTINCT p FROM PurchaseRequisitionEntity p " +
           "WHERE p.status = com.solusi.erp.purchasing.purchaserequisition.domain.model.PurchaseRequisitionStatus.APPROVED")
    List<PurchaseRequisitionEntity> findAllApproved();

    @Query("""
        select distinct pr
        from PurchaseRequisitionEntity pr
        left join fetch pr.lines line
        where pr.status = com.solusi.erp.purchasing.purchaserequisition.domain.model.PurchaseRequisitionStatus.APPROVED
          and (:supplierId is null or pr.suggestedSupplierId = :supplierId)
          and (
                :keyword is null
             or lower(pr.code) like lower(concat('%', :keyword, '%'))
             or lower(coalesce(pr.department, '')) like lower(concat('%', :keyword, '%'))
          )
        order by pr.requestDate desc, pr.id desc
    """)
    List<PurchaseRequisitionEntity> findApprovedForPoSelector(@Param("supplierId") Long supplierId,
                                                              @Param("keyword") String keyword);

    @Query("""
        select line
        from PurchaseRequisitionLineEntity line
        join fetch line.header pr
        where line.header.id = :prId
          and line.header.status = com.solusi.erp.purchasing.purchaserequisition.domain.model.PurchaseRequisitionStatus.APPROVED
          and (
                :keyword is null
             or lower(pr.code) like lower(concat('%', :keyword, '%'))
             or lower(coalesce(line.note, '')) like lower(concat('%', :keyword, '%'))
          )
        order by line.id asc
    """)
    List<PurchaseRequisitionLineEntity> findApprovedLinesForPoSelector(@Param("prId") Long prId,
                                                                       @Param("keyword") String keyword);
}
