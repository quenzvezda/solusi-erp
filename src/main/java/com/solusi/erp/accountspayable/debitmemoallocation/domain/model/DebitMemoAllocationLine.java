package com.solusi.erp.accountspayable.debitmemoallocation.domain.model;

import com.solusi.erp.core.exception.DomainException;

import java.math.BigDecimal;

public final class DebitMemoAllocationLine {

    private final Long id;
    private final Long vendorBillId;
    private final String vendorBillCode;
    private final BigDecimal debitMemoRemainingAtDraft;
    private final BigDecimal vendorBillOutstandingAtDraft;
    private final BigDecimal appliedGrossOriginal;
    private final BigDecimal appliedDppOriginal;
    private final BigDecimal appliedTaxOriginal;
    private final BigDecimal grirReversalBase;
    private final BigDecimal taxReversalBase;
    private final BigDecimal vendorBillExchangeRate;
    private final BigDecimal apReductionBase;
    private final BigDecimal fxLossBase;
    private final BigDecimal fxGainBase;

    public DebitMemoAllocationLine(Long id,
                                   Long vendorBillId,
                                   String vendorBillCode,
                                   BigDecimal debitMemoRemainingAtDraft,
                                   BigDecimal vendorBillOutstandingAtDraft,
                                   BigDecimal appliedGrossOriginal,
                                   BigDecimal appliedDppOriginal,
                                   BigDecimal appliedTaxOriginal,
                                   BigDecimal grirReversalBase,
                                   BigDecimal taxReversalBase,
                                   BigDecimal vendorBillExchangeRate,
                                   BigDecimal apReductionBase,
                                   BigDecimal fxLossBase,
                                   BigDecimal fxGainBase) {
        validate(vendorBillId, vendorBillCode, debitMemoRemainingAtDraft, vendorBillOutstandingAtDraft,
                appliedGrossOriginal, appliedDppOriginal, appliedTaxOriginal, grirReversalBase, taxReversalBase,
                vendorBillExchangeRate, apReductionBase, fxLossBase, fxGainBase);
        this.id = id;
        this.vendorBillId = vendorBillId;
        this.vendorBillCode = vendorBillCode;
        this.debitMemoRemainingAtDraft = debitMemoRemainingAtDraft;
        this.vendorBillOutstandingAtDraft = vendorBillOutstandingAtDraft;
        this.appliedGrossOriginal = appliedGrossOriginal;
        this.appliedDppOriginal = appliedDppOriginal;
        this.appliedTaxOriginal = appliedTaxOriginal;
        this.grirReversalBase = grirReversalBase;
        this.taxReversalBase = taxReversalBase;
        this.vendorBillExchangeRate = vendorBillExchangeRate;
        this.apReductionBase = apReductionBase;
        this.fxLossBase = fxLossBase;
        this.fxGainBase = fxGainBase;
    }

    private static void validate(Long vendorBillId,
                                 String vendorBillCode,
                                 BigDecimal debitMemoRemainingAtDraft,
                                 BigDecimal vendorBillOutstandingAtDraft,
                                 BigDecimal appliedGrossOriginal,
                                 BigDecimal appliedDppOriginal,
                                 BigDecimal appliedTaxOriginal,
                                 BigDecimal grirReversalBase,
                                 BigDecimal taxReversalBase,
                                 BigDecimal vendorBillExchangeRate,
                                 BigDecimal apReductionBase,
                                 BigDecimal fxLossBase,
                                 BigDecimal fxGainBase) {
        if (vendorBillId == null || isBlank(vendorBillCode)) {
            throw new DomainException("msg.error.debit-memo-allocation.line.vendor-bill-required");
        }
        if (appliedGrossOriginal == null || appliedGrossOriginal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("msg.error.debit-memo-allocation.line.amount-positive");
        }
        if (isNegative(debitMemoRemainingAtDraft) || isNegative(vendorBillOutstandingAtDraft)
                || isNegative(appliedDppOriginal) || isNegative(appliedTaxOriginal)
                || isNegative(grirReversalBase) || isNegative(taxReversalBase)
                || isNegative(apReductionBase) || isNegative(fxLossBase) || isNegative(fxGainBase)) {
            throw new DomainException("msg.error.debit-memo-allocation.line.amount-non-negative");
        }
        if (vendorBillExchangeRate == null || vendorBillExchangeRate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("msg.error.debit-memo-allocation.line.exchange-rate-positive");
        }
        if (appliedGrossOriginal.compareTo(vendorBillOutstandingAtDraft) > 0) {
            throw new DomainException("msg.error.debit-memo-allocation.over-vendor-bill-outstanding");
        }
    }

    private static boolean isNegative(BigDecimal value) {
        return value == null || value.compareTo(BigDecimal.ZERO) < 0;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public Long getId() { return id; }
    public Long getVendorBillId() { return vendorBillId; }
    public String getVendorBillCode() { return vendorBillCode; }
    public BigDecimal getDebitMemoRemainingAtDraft() { return debitMemoRemainingAtDraft; }
    public BigDecimal getVendorBillOutstandingAtDraft() { return vendorBillOutstandingAtDraft; }
    public BigDecimal getAppliedGrossOriginal() { return appliedGrossOriginal; }
    public BigDecimal getAppliedDppOriginal() { return appliedDppOriginal; }
    public BigDecimal getAppliedTaxOriginal() { return appliedTaxOriginal; }
    public BigDecimal getGrirReversalBase() { return grirReversalBase; }
    public BigDecimal getTaxReversalBase() { return taxReversalBase; }
    public BigDecimal getVendorBillExchangeRate() { return vendorBillExchangeRate; }
    public BigDecimal getApReductionBase() { return apReductionBase; }
    public BigDecimal getFxLossBase() { return fxLossBase; }
    public BigDecimal getFxGainBase() { return fxGainBase; }
}
