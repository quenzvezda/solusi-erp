package com.solusi.erp.accountspayable.debitmemo.infrastructure.persistence;

import com.solusi.erp.accountspayable.debitmemo.domain.model.DebitMemoSettlementStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface DebitMemoJpaRepository extends JpaRepository<DebitMemoEntity, Long> {

    @Query("""
            select dm from DebitMemoEntity dm
            where (:keyword is null or :keyword = ''
               or lower(dm.code) like lower(concat('%', :keyword, '%'))
               or lower(dm.purchaseReturnCode) like lower(concat('%', :keyword, '%'))
               or lower(dm.supplierMemoNumber) like lower(concat('%', :keyword, '%')))
              and (:vendorId is null or dm.vendorId = :vendorId)
              and (:settlementStatus is null or dm.settlementStatus = :settlementStatus)
              and (:memoDateFrom is null or dm.memoDate >= :memoDateFrom)
              and (:memoDateTo is null or dm.memoDate <= :memoDateTo)
            """)
    Page<DebitMemoEntity> findAllFiltered(@Param("keyword") String keyword,
                                          @Param("vendorId") Long vendorId,
                                          @Param("settlementStatus") DebitMemoSettlementStatus settlementStatus,
                                          @Param("memoDateFrom") LocalDate memoDateFrom,
                                          @Param("memoDateTo") LocalDate memoDateTo,
                                          Pageable pageable);

    Optional<DebitMemoEntity> findByPurchaseReturnId(Long purchaseReturnId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select distinct dm
            from DebitMemoEntity dm
            left join fetch dm.lines
            where dm.purchaseReturnId = :purchaseReturnId
            """)
    Optional<DebitMemoEntity> findByPurchaseReturnIdForUpdate(@Param("purchaseReturnId") Long purchaseReturnId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update DebitMemoEntity dm
            set dm.settlementStatus = :settlementStatus
            where dm.id = :id
            """)
    void updateSettlementStatus(@Param("id") Long id,
                                @Param("settlementStatus") DebitMemoSettlementStatus settlementStatus);

    boolean existsByPurchaseReturnId(Long purchaseReturnId);

    @Query("""
            select count(dm) > 0
            from DebitMemoEntity dm
            where dm.vendorId = :vendorId
              and dm.supplierMemoNumber = :supplierMemoNumber
              and (:excludedId is null or dm.id <> :excludedId)
            """)
    boolean existsSupplierMemoNumber(@Param("vendorId") Long vendorId,
                                     @Param("supplierMemoNumber") String supplierMemoNumber,
                                     @Param("excludedId") Long excludedId);

    @Query("""
            select count(dm) > 0
            from DebitMemoEntity dm
            where dm.taxDocumentNumber = :taxDocumentNumber
              and (:excludedId is null or dm.id <> :excludedId)
            """)
    boolean existsTaxDocumentNumber(@Param("taxDocumentNumber") String taxDocumentNumber,
                                    @Param("excludedId") Long excludedId);
}
