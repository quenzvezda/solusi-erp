package com.solusi.erp.accountspayable.debitmemoallocation.infrastructure.adapter;

import com.solusi.erp.accountspayable.debitmemo.domain.port.DebitMemoAllocationConsumptionPort;
import com.solusi.erp.accountspayable.debitmemoallocation.domain.repository.DebitMemoAllocationRepository;

public class DebitMemoAllocationConsumptionAdapter implements DebitMemoAllocationConsumptionPort {

    private final DebitMemoAllocationRepository repository;

    public DebitMemoAllocationConsumptionAdapter(DebitMemoAllocationRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean hasConfirmedConsumption(Long debitMemoId) {
        return repository.existsActiveConsumptionByDebitMemoId(debitMemoId);
    }
}
