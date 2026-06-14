package com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.query;

import com.solusi.erp.accountspayable.debitmemoallocation.domain.repository.DebitMemoAllocationRepository;

import java.util.List;

public class FindDebitMemoAllocationHistoryUseCaseImpl implements FindDebitMemoAllocationHistoryUseCase {

    private final DebitMemoAllocationRepository repository;

    public FindDebitMemoAllocationHistoryUseCaseImpl(DebitMemoAllocationRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<DebitMemoAllocationHistoryView> byDebitMemoId(Long debitMemoId) {
        return repository.findHistoryByDebitMemoId(debitMemoId).stream()
                .map(DebitMemoAllocationViewMapper::toHistory)
                .toList();
    }

    @Override
    public List<DebitMemoAllocationHistoryView> byVendorBillId(Long vendorBillId) {
        return repository.findHistoryByVendorBillId(vendorBillId).stream()
                .map(DebitMemoAllocationViewMapper::toHistory)
                .toList();
    }
}
