package com.solusi.erp.purchasing.purchasereturn.infrastructure.adapter;

import com.solusi.erp.accountspayable.debitmemo.domain.model.DebitMemo;
import com.solusi.erp.accountspayable.debitmemo.domain.model.DebitMemoSettlementStatus;
import com.solusi.erp.accountspayable.debitmemo.domain.repository.DebitMemoRepository;
import com.solusi.erp.accountspayable.debitmemoallocation.domain.repository.DebitMemoAllocationRepository;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.purchasing.purchasereturn.domain.port.PurchaseReturnDebitMemoReversalPort;

import java.math.BigDecimal;

public class PurchaseReturnDebitMemoReversalAdapter implements PurchaseReturnDebitMemoReversalPort {

    private final DebitMemoRepository debitMemoRepository;
    private final DebitMemoAllocationRepository allocationRepository;

    public PurchaseReturnDebitMemoReversalAdapter(DebitMemoRepository debitMemoRepository,
                                                  DebitMemoAllocationRepository allocationRepository) {
        this.debitMemoRepository = debitMemoRepository;
        this.allocationRepository = allocationRepository;
    }

    @Override
    public Long validateReversibleAndLock(Long purchaseReturnId) {
        DebitMemo debitMemo = debitMemoRepository.findByPurchaseReturnIdForUpdate(purchaseReturnId)
                .orElseThrow(() -> new DomainException("msg.error.purchase-return.reverse.debit-memo-not-found"));
        if (allocationRepository.existsActiveConsumptionByDebitMemoId(debitMemo.getId())) {
            throw new DomainException("msg.error.purchase-return.reverse.debit-memo-has-consumption");
        }
        BigDecimal confirmedApplied = allocationRepository.sumConfirmedAppliedByDebitMemoId(debitMemo.getId());
        if (confirmedApplied != null && confirmedApplied.compareTo(BigDecimal.ZERO) != 0) {
            throw new DomainException("msg.error.purchase-return.reverse.debit-memo-not-fully-open");
        }
        if (debitMemo.getSettlementStatus() != DebitMemoSettlementStatus.OPEN) {
            throw new DomainException("msg.error.purchase-return.reverse.debit-memo-not-fully-open");
        }
        return debitMemo.getId();
    }

    @Override
    public void cancelDebitMemo(Long debitMemoId) {
        DebitMemo debitMemo = debitMemoRepository.findById(debitMemoId)
                .orElseThrow(() -> new DomainException("msg.error.purchase-return.reverse.debit-memo-not-found"));
        debitMemo.cancel();
        debitMemoRepository.save(debitMemo);
    }
}
