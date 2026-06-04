package com.solusi.erp.accountspayable.debitmemo.domain.port;

public interface DebitMemoAllocationConsumptionPort {

    boolean hasConfirmedConsumption(Long debitMemoId);
}

