package com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.command;

import com.solusi.erp.accountspayable.debitmemoallocation.domain.model.DebitMemoAllocation;
import com.solusi.erp.accountspayable.debitmemoallocation.domain.repository.DebitMemoAllocationRepository;
import com.solusi.erp.core.exception.DomainException;

public class CancelDebitMemoAllocationUseCaseImpl implements CancelDebitMemoAllocationUseCase {

    private final DebitMemoAllocationRepository repository;

    public CancelDebitMemoAllocationUseCaseImpl(DebitMemoAllocationRepository repository) {
        this.repository = repository;
    }

    @Override
    public void execute(Long id) {
        DebitMemoAllocation allocation = repository.findById(id)
                .orElseThrow(() -> new DomainException("msg.error.debit-memo-allocation.not-found"));
        allocation.cancel();
        repository.save(allocation);
    }
}
