package com.solusi.erp.accountspayable.debitmemoallocation.infrastructure.persistence;

import com.solusi.erp.accountspayable.debitmemoallocation.domain.model.DebitMemoAllocationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface DebitMemoAllocationJpaRepository extends JpaRepository<DebitMemoAllocationEntity, Long> {

    @Query("""
            select allocation
            from DebitMemoAllocationEntity allocation,
                 com.solusi.erp.accountspayable.debitmemo.infrastructure.persistence.DebitMemoEntity dm
            where dm.id = allocation.debitMemoId
              and (:vendorId is null or dm.vendorId = :vendorId)
              and (:keyword is null or :keyword = ''
               or lower(allocation.code) like lower(concat('%', :keyword, '%'))
               or lower(allocation.debitMemoCode) like lower(concat('%', :keyword, '%')))
              and (:debitMemoId is null or allocation.debitMemoId = :debitMemoId)
              and (:status is null or allocation.status = :status)
              and (:allocationDateFrom is null or allocation.allocationDate >= :allocationDateFrom)
              and (:allocationDateTo is null or allocation.allocationDate <= :allocationDateTo)
            """)
    Page<DebitMemoAllocationEntity> findAllFiltered(@Param("keyword") String keyword,
                                                    @Param("debitMemoId") Long debitMemoId,
                                                    @Param("vendorId") Long vendorId,
                                                    @Param("status") DebitMemoAllocationStatus status,
                                                    @Param("allocationDateFrom") LocalDate allocationDateFrom,
                                                    @Param("allocationDateTo") LocalDate allocationDateTo,
                                                    Pageable pageable);

    @Query("""
            select count(allocation) > 0
            from DebitMemoAllocationEntity allocation
            where allocation.debitMemoId = :debitMemoId
              and allocation.status = com.solusi.erp.accountspayable.debitmemoallocation.domain.model.DebitMemoAllocationStatus.CONFIRMED
            """)
    boolean existsActiveConsumptionByDebitMemoId(@Param("debitMemoId") Long debitMemoId);

    @Query("""
            select coalesce(sum(line.appliedGrossOriginal), 0)
            from DebitMemoAllocationEntity allocation
            join allocation.lines line
            where allocation.debitMemoId = :debitMemoId
              and allocation.status = com.solusi.erp.accountspayable.debitmemoallocation.domain.model.DebitMemoAllocationStatus.CONFIRMED
            """)
    BigDecimal sumConfirmedAppliedByDebitMemoId(@Param("debitMemoId") Long debitMemoId);

    @Query("""
            select allocation.debitMemoId as debitMemoId,
                   coalesce(sum(line.appliedGrossOriginal), 0) as appliedAmount
            from DebitMemoAllocationEntity allocation
            join allocation.lines line
            where allocation.debitMemoId in :debitMemoIds
              and allocation.status = com.solusi.erp.accountspayable.debitmemoallocation.domain.model.DebitMemoAllocationStatus.CONFIRMED
            group by allocation.debitMemoId
            """)
    List<DebitMemoAllocationSumProjection> sumConfirmedAppliedByDebitMemoIds(
            @Param("debitMemoIds") Collection<Long> debitMemoIds);

    @Query("""
            select distinct allocation
            from DebitMemoAllocationEntity allocation
            left join fetch allocation.lines
            where allocation.debitMemoId = :debitMemoId
            order by allocation.allocationDate desc, allocation.id desc
            """)
    List<DebitMemoAllocationEntity> findHistoryByDebitMemoId(@Param("debitMemoId") Long debitMemoId);

    @Query("""
            select distinct allocation
            from DebitMemoAllocationEntity allocation
            join fetch allocation.lines line
            where line.vendorBillId = :vendorBillId
            order by allocation.allocationDate desc, allocation.id desc
            """)
    List<DebitMemoAllocationEntity> findHistoryByVendorBillId(@Param("vendorBillId") Long vendorBillId);

    interface DebitMemoAllocationSumProjection {
        Long getDebitMemoId();

        BigDecimal getAppliedAmount();
    }
}
