package com.solusi.erp.accountspayable.debitmemo.application.usecase.query;

import com.solusi.erp.accountspayable.debitmemo.domain.model.DebitMemo;
import com.solusi.erp.accountspayable.debitmemo.domain.model.DebitMemoLine;
import com.solusi.erp.accountspayable.debitmemo.domain.repository.DebitMemoRepository;
import com.solusi.erp.core.exception.DomainException;

import java.math.BigDecimal;

public class GetDebitMemoDetailUseCaseImpl implements GetDebitMemoDetailUseCase {

    private final DebitMemoRepository debitMemoRepository;

    public GetDebitMemoDetailUseCaseImpl(DebitMemoRepository debitMemoRepository) {
        this.debitMemoRepository = debitMemoRepository;
    }

    @Override
    public DebitMemoDetailView execute(Long id) {
        return debitMemoRepository.findById(id)
                .map(this::toDetail)
                .orElseThrow(() -> new DomainException("msg.error.debit-memo.not-found"));
    }

    private DebitMemoDetailView toDetail(DebitMemo debitMemo) {
        BigDecimal settledAmount = BigDecimal.ZERO;
        BigDecimal refundedAmount = BigDecimal.ZERO;
        BigDecimal remainingAmount = debitMemo.getGrossAmountOriginal();
        return new DebitMemoDetailView(
                debitMemo.getId(),
                debitMemo.getCode(),
                debitMemo.getPurchaseReturnId(),
                debitMemo.getPurchaseReturnCode(),
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

