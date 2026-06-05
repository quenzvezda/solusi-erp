package com.solusi.erp.accountspayable.debitmemo.application.usecase.query;

import com.solusi.erp.accountspayable.debitmemo.domain.model.DebitMemo;
import com.solusi.erp.accountspayable.debitmemo.domain.model.DebitMemoLine;
import com.solusi.erp.accountspayable.debitmemo.domain.port.DebitMemoSourceDocumentPort;
import com.solusi.erp.accountspayable.debitmemo.domain.repository.DebitMemoRepository;
import com.solusi.erp.accountspayable.debitmemoallocation.domain.repository.DebitMemoAllocationRepository;
import com.solusi.erp.core.exception.DomainException;

import java.math.BigDecimal;

public class GetDebitMemoDetailUseCaseImpl implements GetDebitMemoDetailUseCase {

    private final DebitMemoRepository debitMemoRepository;
    private final DebitMemoSourceDocumentPort sourceDocumentPort;
    private final DebitMemoAllocationRepository allocationRepository;

    public GetDebitMemoDetailUseCaseImpl(DebitMemoRepository debitMemoRepository,
                                         DebitMemoSourceDocumentPort sourceDocumentPort,
                                         DebitMemoAllocationRepository allocationRepository) {
        this.debitMemoRepository = debitMemoRepository;
        this.sourceDocumentPort = sourceDocumentPort;
        this.allocationRepository = allocationRepository;
    }

    @Override
    public DebitMemoDetailView execute(Long id) {
        return debitMemoRepository.findById(id)
                .map(this::toDetail)
                .orElseThrow(() -> new DomainException("msg.error.debit-memo.not-found"));
    }

    private DebitMemoDetailView toDetail(DebitMemo debitMemo) {
        BigDecimal settledAmount = allocationRepository.sumConfirmedAppliedByDebitMemoId(debitMemo.getId());
        BigDecimal refundedAmount = BigDecimal.ZERO;
        BigDecimal remainingAmount = debitMemo.getGrossAmountOriginal().subtract(settledAmount).max(BigDecimal.ZERO);
        return new DebitMemoDetailView(
                debitMemo.getId(),
                debitMemo.getCode(),
                debitMemo.getPurchaseReturnId(),
                debitMemo.getPurchaseReturnCode(),
                sourceDocumentPort.findGeneratedGoodsIssueId(debitMemo.getPurchaseReturnId()).orElse(null),
                debitMemo.getVendorId(),
                debitMemo.getCurrencyId(),
                debitMemo.getMemoDate(),
                debitMemo.getGrossAmountOriginal(),
                debitMemo.getDppAmountOriginal(),
                debitMemo.getTaxAmountOriginal(),
                debitMemo.getGrossAmountBase(),
                debitMemo.getDppAmountBase(),
                debitMemo.getTaxAmountBase(),
                settledAmount,
                refundedAmount,
                remainingAmount,
                debitMemo.getSettlementStatus(),
                debitMemo.getSupplierMemoNumber(),
                debitMemo.getSupplierMemoDate(),
                debitMemo.getTaxDocumentNumber(),
                debitMemo.getTaxDocumentDate(),
                debitMemo.getNotes(),
                debitMemo.getLines().stream().map(this::toLine).toList()
        );
    }

    private DebitMemoLineView toLine(DebitMemoLine line) {
        return new DebitMemoLineView(
                line.getId(),
                line.getPurchaseReturnLineId(),
                line.getProductId(),
                line.getQuantity(),
                line.getUomId(),
                line.getDppAmountOriginal(),
                line.getTaxAmountOriginal(),
                line.getDppAmountBase(),
                line.getTaxAmountBase()
        );
    }
}
