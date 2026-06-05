package com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.query;

import com.solusi.erp.accountspayable.debitmemoallocation.domain.model.DebitMemoAllocation;
import com.solusi.erp.accountspayable.debitmemoallocation.domain.model.DebitMemoAllocationStatus;
import com.solusi.erp.accountspayable.debitmemoallocation.domain.repository.DebitMemoAllocationRepository;
import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;

import java.time.LocalDate;

public class FindDebitMemoAllocationsUseCaseImpl implements FindDebitMemoAllocationsUseCase {

    private final DebitMemoAllocationRepository repository;

    public FindDebitMemoAllocationsUseCaseImpl(DebitMemoAllocationRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<DebitMemoAllocationSummaryView> execute(String keyword,
                                                        Long debitMemoId,
                                                        DebitMemoAllocationStatus status,
                                                        LocalDate allocationDateFrom,
                                                        LocalDate allocationDateTo,
                                                        Pageable pageable) {
        Page<DebitMemoAllocation> page = repository.findAll(
                keyword, debitMemoId, status, allocationDateFrom, allocationDateTo, pageable);
        return new Page<>(
                page.content().stream().map(DebitMemoAllocationViewMapper::toSummary).toList(),
                page.page(),
                page.size(),
                page.totalElements()
        );
    }
}
