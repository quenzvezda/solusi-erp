package com.solusi.erp.accountspayable.debitmemoallocation.domain.service;

import com.solusi.erp.accountspayable.debitmemoallocation.domain.model.DebitMemoAllocationLine;
import com.solusi.erp.core.exception.DomainException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class DebitMemoAllocationProrationService {

    private static final int MONEY_SCALE = 4;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    public List<DebitMemoAllocationLine> prorate(DebitMemoSnapshot debitMemo,
                                                 AppliedTotals alreadyApplied,
                                                 List<AllocationInput> inputs) {
        validate(debitMemo, alreadyApplied, inputs);
        AppliedTotals normalizedApplied = alreadyApplied == null ? AppliedTotals.none() : alreadyApplied;

        BigDecimal requestedGross = inputs.stream()
                .map(AllocationInput::appliedGrossOriginal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal cumulativeGross = normalizedApplied.appliedGrossOriginal().add(requestedGross);
        if (cumulativeGross.compareTo(debitMemo.grossOriginal()) > 0) {
            throw new DomainException("msg.error.debit-memo-allocation.over-debit-memo-remaining");
        }

        boolean exhaustsDebitMemo = cumulativeGross.compareTo(debitMemo.grossOriginal()) == 0;
        List<DebitMemoAllocationLine> lines = new ArrayList<>();
        BigDecimal runningDppOriginal = BigDecimal.ZERO.setScale(MONEY_SCALE, ROUNDING);
        BigDecimal runningTaxOriginal = BigDecimal.ZERO.setScale(MONEY_SCALE, ROUNDING);
        BigDecimal runningGrirBase = BigDecimal.ZERO.setScale(MONEY_SCALE, ROUNDING);
        BigDecimal runningTaxBase = BigDecimal.ZERO.setScale(MONEY_SCALE, ROUNDING);

        for (int i = 0; i < inputs.size(); i++) {
            AllocationInput input = inputs.get(i);
            boolean lastLine = i == inputs.size() - 1;
            BigDecimal dppOriginal = prorated(input.appliedGrossOriginal(), debitMemo.dppOriginal(), debitMemo.grossOriginal());
            BigDecimal taxOriginal = prorated(input.appliedGrossOriginal(), debitMemo.taxOriginal(), debitMemo.grossOriginal());
            BigDecimal grirBase = prorated(input.appliedGrossOriginal(), debitMemo.dppBase(), debitMemo.grossOriginal());
            BigDecimal taxBase = prorated(input.appliedGrossOriginal(), debitMemo.taxBase(), debitMemo.grossOriginal());

            if (exhaustsDebitMemo && lastLine) {
                dppOriginal = debitMemo.dppOriginal().subtract(normalizedApplied.appliedDppOriginal()).subtract(runningDppOriginal)
                        .setScale(MONEY_SCALE, ROUNDING);
                taxOriginal = debitMemo.taxOriginal().subtract(normalizedApplied.appliedTaxOriginal()).subtract(runningTaxOriginal)
                        .setScale(MONEY_SCALE, ROUNDING);
                grirBase = debitMemo.dppBase().subtract(normalizedApplied.grirReversalBase()).subtract(runningGrirBase)
                        .setScale(MONEY_SCALE, ROUNDING);
                taxBase = debitMemo.taxBase().subtract(normalizedApplied.taxReversalBase()).subtract(runningTaxBase)
                        .setScale(MONEY_SCALE, ROUNDING);
            }

            BigDecimal apReductionBase = input.appliedGrossOriginal()
                    .multiply(input.vendorBillExchangeRate())
                    .setScale(MONEY_SCALE, ROUNDING);
            BigDecimal fxDifference = apReductionBase.subtract(grirBase).subtract(taxBase).setScale(MONEY_SCALE, ROUNDING);
            BigDecimal fxGain = fxDifference.signum() > 0 ? fxDifference : BigDecimal.ZERO.setScale(MONEY_SCALE, ROUNDING);
            BigDecimal fxLoss = fxDifference.signum() < 0 ? fxDifference.abs() : BigDecimal.ZERO.setScale(MONEY_SCALE, ROUNDING);

            lines.add(new DebitMemoAllocationLine(null, input.vendorBillId(), input.vendorBillCode(),
                    input.debitMemoRemainingAtDraft(), input.vendorBillOutstandingAtDraft(),
                    input.appliedGrossOriginal(), dppOriginal, taxOriginal, grirBase, taxBase,
                    input.vendorBillExchangeRate(), apReductionBase, fxLoss, fxGain));

            runningDppOriginal = runningDppOriginal.add(dppOriginal);
            runningTaxOriginal = runningTaxOriginal.add(taxOriginal);
            runningGrirBase = runningGrirBase.add(grirBase);
            runningTaxBase = runningTaxBase.add(taxBase);
        }

        return List.copyOf(lines);
    }

    private void validate(DebitMemoSnapshot debitMemo, AppliedTotals alreadyApplied, List<AllocationInput> inputs) {
        if (debitMemo == null || debitMemo.grossOriginal() == null || debitMemo.grossOriginal().compareTo(BigDecimal.ZERO) <= 0
                || isNegative(debitMemo.dppOriginal()) || isNegative(debitMemo.taxOriginal())
                || isNegative(debitMemo.dppBase()) || isNegative(debitMemo.taxBase())) {
            throw new DomainException("msg.error.debit-memo-allocation.debit-memo-amount-invalid");
        }
        if (alreadyApplied != null && (isNegative(alreadyApplied.appliedGrossOriginal())
                || isNegative(alreadyApplied.appliedDppOriginal()) || isNegative(alreadyApplied.appliedTaxOriginal())
                || isNegative(alreadyApplied.grirReversalBase()) || isNegative(alreadyApplied.taxReversalBase()))) {
            throw new DomainException("msg.error.debit-memo-allocation.line.amount-non-negative");
        }
        if (inputs == null || inputs.isEmpty()) {
            throw new DomainException("msg.error.debit-memo-allocation.lines-required");
        }
        for (AllocationInput input : inputs) {
            if (input == null || input.vendorBillId() == null || input.vendorBillCode() == null || input.vendorBillCode().isBlank()) {
                throw new DomainException("msg.error.debit-memo-allocation.line.vendor-bill-required");
            }
            if (isNegative(input.debitMemoRemainingAtDraft()) || isNegative(input.vendorBillOutstandingAtDraft())) {
                throw new DomainException("msg.error.debit-memo-allocation.line.amount-non-negative");
            }
            if (input.appliedGrossOriginal() == null || input.appliedGrossOriginal().compareTo(BigDecimal.ZERO) <= 0) {
                throw new DomainException("msg.error.debit-memo-allocation.line.amount-positive");
            }
            if (input.vendorBillExchangeRate() == null || input.vendorBillExchangeRate().compareTo(BigDecimal.ZERO) <= 0) {
                throw new DomainException("msg.error.debit-memo-allocation.line.exchange-rate-positive");
            }
        }
    }

    private BigDecimal prorated(BigDecimal appliedGrossOriginal, BigDecimal totalAmount, BigDecimal debitMemoGrossOriginal) {
        if (totalAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO.setScale(MONEY_SCALE, ROUNDING);
        }
        return appliedGrossOriginal.multiply(totalAmount)
                .divide(debitMemoGrossOriginal, MONEY_SCALE, ROUNDING);
    }

    private boolean isNegative(BigDecimal value) {
        return value == null || value.compareTo(BigDecimal.ZERO) < 0;
    }

    public record DebitMemoSnapshot(BigDecimal grossOriginal,
                                    BigDecimal dppOriginal,
                                    BigDecimal taxOriginal,
                                    BigDecimal dppBase,
                                    BigDecimal taxBase) {
    }

    public record AppliedTotals(BigDecimal appliedGrossOriginal,
                                BigDecimal appliedDppOriginal,
                                BigDecimal appliedTaxOriginal,
                                BigDecimal grirReversalBase,
                                BigDecimal taxReversalBase) {
        public static AppliedTotals none() {
            BigDecimal zero = BigDecimal.ZERO.setScale(MONEY_SCALE, ROUNDING);
            return new AppliedTotals(zero, zero, zero, zero, zero);
        }
    }

    public record AllocationInput(Long vendorBillId,
                                  String vendorBillCode,
                                  BigDecimal debitMemoRemainingAtDraft,
                                  BigDecimal vendorBillOutstandingAtDraft,
                                  BigDecimal appliedGrossOriginal,
                                  BigDecimal vendorBillExchangeRate) {
    }
}
