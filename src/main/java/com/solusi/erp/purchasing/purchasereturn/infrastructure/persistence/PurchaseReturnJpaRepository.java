package com.solusi.erp.purchasing.purchasereturn.infrastructure.persistence;

import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturnStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PurchaseReturnJpaRepository extends JpaRepository<PurchaseReturnEntity, Long> {

    @Query("SELECT DISTINCT p FROM PurchaseReturnEntity p "
            + "LEFT JOIN FETCH p.lines "
            + "LEFT JOIN FETCH p.reversalLines "
            + "WHERE p.id = :id")
    Optional<PurchaseReturnEntity> findByIdWithLines(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT DISTINCT p FROM PurchaseReturnEntity p "
            + "LEFT JOIN FETCH p.lines "
            + "LEFT JOIN FETCH p.reversalLines "
            + "WHERE p.id = :id")
    Optional<PurchaseReturnEntity> findByIdWithLinesForUpdate(@Param("id") Long id);

    @Query(value = "SELECT DISTINCT p FROM PurchaseReturnEntity p LEFT JOIN FETCH p.lines LEFT JOIN FETCH p.reversalLines WHERE "
            + "p.status = :status AND (LOWER(p.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(p.referenceCode) LIKE LOWER(CONCAT('%', :keyword, '%')))",
            countQuery = "SELECT COUNT(p) FROM PurchaseReturnEntity p WHERE p.status = :status AND "
                    + "(LOWER(p.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
                    + "LOWER(p.referenceCode) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<PurchaseReturnEntity> searchByStatus(@Param("keyword") String keyword,
                                              @Param("status") PurchaseReturnStatus status,
                                              Pageable pageable);

    @Query(value = "SELECT DISTINCT p FROM PurchaseReturnEntity p LEFT JOIN FETCH p.lines LEFT JOIN FETCH p.reversalLines WHERE "
            + "LOWER(p.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(p.referenceCode) LIKE LOWER(CONCAT('%', :keyword, '%'))",
            countQuery = "SELECT COUNT(p) FROM PurchaseReturnEntity p WHERE "
                    + "LOWER(p.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
                    + "LOWER(p.referenceCode) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<PurchaseReturnEntity> search(@Param("keyword") String keyword, Pageable pageable);

    @Query(value = "SELECT DISTINCT p FROM PurchaseReturnEntity p LEFT JOIN FETCH p.lines LEFT JOIN FETCH p.reversalLines WHERE p.status = :status",
            countQuery = "SELECT COUNT(p) FROM PurchaseReturnEntity p WHERE p.status = :status")
    Page<PurchaseReturnEntity> findAllWithLinesByStatus(@Param("status") PurchaseReturnStatus status, Pageable pageable);

    @Query(value = "SELECT DISTINCT p FROM PurchaseReturnEntity p LEFT JOIN FETCH p.lines LEFT JOIN FETCH p.reversalLines",
            countQuery = "SELECT COUNT(p) FROM PurchaseReturnEntity p")
    Page<PurchaseReturnEntity> findAllWithLines(Pageable pageable);

    boolean existsByCode(String code);

    @Query("SELECT COUNT(p) > 0 FROM PurchaseReturnEntity p WHERE p.referenceType = :referenceType "
            + "AND p.referenceId = :referenceId AND p.status NOT IN "
            + "(com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturnStatus.REJECTED, "
            + "com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturnStatus.CANCELLED, "
            + "com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturnStatus.REVERSED)")
    boolean existsConfirmedOrOpenBySource(@Param("referenceType") String referenceType,
                                          @Param("referenceId") Long referenceId);

    @Query("SELECT DISTINCT p FROM PurchaseReturnEntity p "
            + "LEFT JOIN FETCH p.lines "
            + "LEFT JOIN FETCH p.reversalLines "
            + "WHERE p.generatedGoodsIssueId = :generatedGoodsIssueId")
    Optional<PurchaseReturnEntity> findByGeneratedGoodsIssueId(@Param("generatedGoodsIssueId") Long generatedGoodsIssueId);
}
