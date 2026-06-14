package com.solusi.erp.accountspayable.debitmemoallocation.domain.repository;

import com.solusi.erp.accountspayable.debitmemoallocation.domain.model.DebitMemoAllocation;
import com.solusi.erp.accountspayable.debitmemoallocation.domain.model.DebitMemoAllocationStatus;
import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface DebitMemoAllocationRepository {

    DebitMemoAllocation save(DebitMemoAllocation allocation);

    Optional<DebitMemoAllocation> findById(Long id);

    Page<DebitMemoAllocation> findAll(String keyword,
                                      Long debitMemoId,
                                      Long vendorId,
                                      DebitMemoAllocationStatus status,
                                      LocalDate allocationDateFrom,
                                      LocalDate allocationDateTo,
                                      Pageable pageable);

    boolean existsActiveConsumptionByDebitMemoId(Long debitMemoId);

    BigDecimal sumConfirmedAppliedByDebitMemoId(Long debitMemoId);

    Map<Long, BigDecimal> sumConfirmedAppliedByDebitMemoIds(Collection<Long> debitMemoIds);

    List<DebitMemoAllocationHistory> findHistoryByDebitMemoId(Long debitMemoId);

    List<DebitMemoAllocationHistory> findHistoryByVendorBillId(Long vendorBillId);
}
