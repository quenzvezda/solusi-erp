package com.solusi.erp.accountspayable.debitmemo.application.usecase.command;

import com.solusi.erp.accountspayable.debitmemo.domain.model.DebitMemo;
import com.solusi.erp.accountspayable.debitmemo.domain.port.DebitMemoAllocationConsumptionPort;
import com.solusi.erp.accountspayable.debitmemo.domain.repository.DebitMemoRepository;
import com.solusi.erp.core.exception.DomainException;

public class CancelDebitMemoUseCaseImpl implements CancelDebitMemoUseCase {

    private final DebitMemoRepository debitMemoRepository;
    private final DebitMemoAllocationConsumptionPort allocationConsumptionPort;

    public CancelDebitMemoUseCaseImpl(DebitMemoRepository debitMemoRepository,
                                      DebitMemoAllocationConsumptionPort allocationConsumptionPort) {
        this.debitMemoRepository = debitMemoRepository;
        this.allocationConsumptionPort = allocationConsumptionPort;
    }

    @Override
    public DebitMemo execute(Long id) {
        DebitMemo debitMemo = debitMemoRepository.findById(id)
                .orElseThrow(() -> new DomainException("msg.error.debit-memo.not-found"));
        if (allocationConsumptionPort.hasConfirmedConsumption(id)) {
            throw new DomainException("msg.error.debit-memo.cancel.has-consumption");
        }
        debitMemo.cancel();
        return debitMemoRepository.save(debitMemo);
    }
}

