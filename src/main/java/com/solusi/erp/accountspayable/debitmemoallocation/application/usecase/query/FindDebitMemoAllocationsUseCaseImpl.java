package com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.query;

import com.solusi.erp.accountspayable.debitmemoallocation.domain.model.DebitMemoAllocation;
import com.solusi.erp.accountspayable.debitmemoallocation.domain.model.DebitMemoAllocationStatus;
import com.solusi.erp.accountspayable.debitmemoallocation.domain.port.DebitMemoAllocationSourcePort;
import com.solusi.erp.accountspayable.debitmemoallocation.domain.repository.DebitMemoAllocationRepository;
import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;

import java.time.LocalDate;

public class FindDebitMemoAllocationsUseCaseImpl implements FindDebitMemoAllocationsUseCase {

    private final DebitMemoAllocationRepository repository;
    private final DebitMemoAllocationSourcePort sourcePort;

    public FindDebitMemoAllocationsUseCaseImpl(DebitMemoAllocationRepository repository,
                                               DebitMemoAllocationSourcePort sourcePort) {
        this.repository = repository;
        this.sourcePort = sourcePort;
    }

    @Override
    public Page<DebitMemoAllocationSummaryView> execute(String keyword,
                                                        Long debitMemoId,
                                                        Long vendorId,
                                                        DebitMemoAllocationStatus status,
                                                        LocalDate allocationDateFrom,
                                                        LocalDate allocationDateTo,
                                                        Pageable pageable) {
        Page<DebitMemoAllocation> page = repository.findAll(
                keyword, debitMemoId, vendorId, status, allocationDateFrom, allocationDateTo, pageable);
        return new Page<>(
                page.content().stream().map(this::toSummary).toList(),
                page.page(),
                page.size(),
                page.totalElements()
        );
    }

    private DebitMemoAllocationSummaryView toSummary(DebitMemoAllocation allocation) {
        DebitMemoAllocationSourcePort.DebitMemoSnapshot debitMemoSnapshot = sourcePort
                .findDebitMemoSnapshot(allocation.getDebitMemoId())
                .orElse(null);
        return DebitMemoAllocationViewMapper.toSummary(allocation, debitMemoSnapshot);
    }
}
