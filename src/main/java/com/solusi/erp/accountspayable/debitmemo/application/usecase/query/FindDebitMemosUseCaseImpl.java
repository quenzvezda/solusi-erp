package com.solusi.erp.accountspayable.debitmemo.application.usecase.query;

import com.solusi.erp.accountspayable.debitmemo.domain.model.DebitMemo;
import com.solusi.erp.accountspayable.debitmemo.domain.model.DebitMemoSettlementStatus;
import com.solusi.erp.accountspayable.debitmemo.domain.repository.DebitMemoRepository;
import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;

public class FindDebitMemosUseCaseImpl implements FindDebitMemosUseCase {

    private final DebitMemoRepository debitMemoRepository;

    public FindDebitMemosUseCaseImpl(DebitMemoRepository debitMemoRepository) {
        this.debitMemoRepository = debitMemoRepository;
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
        return new Page<>(
                page.content().stream().map(this::toSummary).toList(),
                page.page(),
                page.size(),
                page.totalElements()
        );
    }

    private DebitMemoSummaryView toSummary(DebitMemo debitMemo) {
        BigDecimal settledAmount = BigDecimal.ZERO;
        BigDecimal refundedAmount = BigDecimal.ZERO;
        BigDecimal remainingAmount = debitMemo.getGrossAmountOriginal();
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

