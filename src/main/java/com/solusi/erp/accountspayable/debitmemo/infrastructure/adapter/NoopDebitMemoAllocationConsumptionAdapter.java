package com.solusi.erp.accountspayable.debitmemo.infrastructure.adapter;

import com.solusi.erp.accountspayable.debitmemo.domain.port.DebitMemoAllocationConsumptionPort;

public class NoopDebitMemoAllocationConsumptionAdapter implements DebitMemoAllocationConsumptionPort {

    @Override
    public boolean hasConfirmedConsumption(Long debitMemoId) {
        return false;
    }
}

