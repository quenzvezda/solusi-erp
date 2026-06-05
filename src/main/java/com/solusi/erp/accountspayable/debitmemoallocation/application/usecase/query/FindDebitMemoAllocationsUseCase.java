package com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.query;

import com.solusi.erp.accountspayable.debitmemoallocation.domain.model.DebitMemoAllocationStatus;
import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;

import java.time.LocalDate;

public interface FindDebitMemoAllocationsUseCase {
    Page<DebitMemoAllocationSummaryView> execute(String keyword,
                                                 Long debitMemoId,
                                                 DebitMemoAllocationStatus status,
                                                 LocalDate allocationDateFrom,
                                                 LocalDate allocationDateTo,
                                                 Pageable pageable);
}
