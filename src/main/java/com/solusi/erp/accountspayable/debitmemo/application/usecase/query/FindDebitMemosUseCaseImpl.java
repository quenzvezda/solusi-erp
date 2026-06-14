package com.solusi.erp.accountspayable.debitmemo.application.usecase.query;

import com.solusi.erp.accountspayable.debitmemo.domain.model.DebitMemo;
import com.solusi.erp.accountspayable.debitmemo.domain.model.DebitMemoSettlementStatus;
import com.solusi.erp.accountspayable.debitmemo.domain.repository.DebitMemoRepository;
import com.solusi.erp.accountspayable.debitmemoallocation.domain.repository.DebitMemoAllocationRepository;
import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public class FindDebitMemosUseCaseImpl implements FindDebitMemosUseCase {

    private final DebitMemoRepository debitMemoRepository;
    private final DebitMemoAllocationRepository allocationRepository;

    public FindDebitMemosUseCaseImpl(DebitMemoRepository debitMemoRepository,
                                     DebitMemoAllocationRepository allocationRepository) {
        this.debitMemoRepository = debitMemoRepository;
        this.allocationRepository = allocationRepository;
    }

    @Override
    public Page<DebitMemoSummaryView> execute(String keyword,
                                             Long vendorId,
                                             DebitMemoSettlementStatus settlementStatus,
                                             LocalDate memoDateFrom,
                                             LocalDate memoDateTo,
                                             Pageable pageable) {
        Page<DebitMemo> page = debitMemoRepository.findAll(
                keyword, vendorId, settlementStatus, memoDateFrom, memoDateTo, pageable);
        Map<Long, BigDecimal> settledAmounts = allocationRepository.sumConfirmedAppliedByDebitMemoIds(
                page.content().stream().map(DebitMemo::getId).toList());
        return new Page<>(
                page.content().stream().map(debitMemo -> toSummary(debitMemo, settledAmounts)).toList(),
                page.page(),
                page.size(),
                page.totalElements()
        );
    }

    private DebitMemoSummaryView toSummary(DebitMemo debitMemo, Map<Long, BigDecimal> settledAmounts) {
        BigDecimal settledAmount = settledAmounts.getOrDefault(debitMemo.getId(), BigDecimal.ZERO);
        BigDecimal refundedAmount = BigDecimal.ZERO;
        BigDecimal remainingAmount = debitMemo.getGrossAmountOriginal().subtract(settledAmount).max(BigDecimal.ZERO);
        return new DebitMemoSummaryView(
                debitMemo.getId(),
                debitMemo.getCode(),
                debitMemo.getMemoDate(),
                debitMemo.getVendorId(),
                debitMemo.getCurrencyId(),
                debitMemo.getPurchaseReturnId(),
                debitMemo.getPurchaseReturnCode(),
                debitMemo.getGrossAmountOriginal(),
                settledAmount,
                refundedAmount,
                remainingAmount,
                debitMemo.getSettlementStatus()
        );
    }
}
