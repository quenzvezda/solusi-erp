package com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.query;

import com.solusi.erp.accountspayable.debitmemoallocation.domain.repository.DebitMemoAllocationRepository;
import com.solusi.erp.core.exception.DomainException;

public class GetDebitMemoAllocationDetailUseCaseImpl implements GetDebitMemoAllocationDetailUseCase {

    private final DebitMemoAllocationRepository repository;

    public GetDebitMemoAllocationDetailUseCaseImpl(DebitMemoAllocationRepository repository) {
        this.repository = repository;
    }

    @Override
    public DebitMemoAllocationDetailView execute(Long id) {
        return repository.findById(id)
                .map(DebitMemoAllocationViewMapper::toDetail)
                .orElseThrow(() -> new DomainException("msg.error.debit-memo-allocation.not-found"));
    }
}
