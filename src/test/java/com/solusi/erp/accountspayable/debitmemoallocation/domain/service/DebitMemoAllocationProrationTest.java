package com.solusi.erp.accountspayable.debitmemoallocation.domain.service;

import com.solusi.erp.accountspayable.debitmemoallocation.domain.model.DebitMemoAllocationLine;
import com.solusi.erp.core.exception.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import com.solusi.erp.accountspayable.debitmemoallocation.domain.service.DebitMemoAllocationProrationService.AllocationInput;
import com.solusi.erp.accountspayable.debitmemoallocation.domain.service.DebitMemoAllocationProrationService.AppliedTotals;
import com.solusi.erp.accountspayable.debitmemoallocation.domain.service.DebitMemoAllocationProrationService.DebitMemoSnapshot;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("DebitMemoAllocation Proration Tests")
class DebitMemoAllocationProrationTest {

    private final DebitMemoAllocationProrationService service = new DebitMemoAllocationProrationService();

    @Test
    @DisplayName("prorate should split gross into DPP tax base and FX gain")
    void prorate_shouldSplitGrossIntoDppTaxBaseAndFxGain() {
        List<DebitMemoAllocationLine> lines = service.prorate(
                snapshot("111.0000", "100.0000", "11.0000", "1500000.0000", "165000.0000"),
                AppliedTotals.none(),
                List.of(input(1L, "111.0000", "111.0000", "55.5000", "16000.000000"))
        );

        DebitMemoAllocationLine line = lines.get(0);
        assertThat(line.getAppliedDppOriginal()).isEqualByComparingTo("50.0000");
        assertThat(line.getAppliedTaxOriginal()).isEqualByComparingTo("5.5000");
        assertThat(line.getGrirReversalBase()).isEqualByComparingTo("750000.0000");
        assertThat(line.getTaxReversalBase()).isEqualByComparingTo("82500.0000");
        assertThat(line.getApReductionBase()).isEqualByComparingTo("888000.0000");
        assertThat(line.getFxGainBase()).isEqualByComparingTo("55500.0000");
        assertThat(line.getFxLossBase()).isEqualByComparingTo("0.0000");
    }

    @Test
    @DisplayName("prorate should put rounding remainder on last line when debit memo is exhausted")
    void prorate_shouldPutRoundingRemainderOnLastLineWhenDebitMemoIsExhausted() {
        List<DebitMemoAllocationLine> lines = service.prorate(
                snapshot("100.0000", "10.0000", "90.0000", "1000.0000", "9000.0000"),
                AppliedTotals.none(),
                List.of(
                        input(1L, "100.0000", "100.0000", "33.3333", "100.000000"),
                        input(2L, "100.0000", "100.0000", "33.3333", "100.000000"),
                        input(3L, "100.0000", "100.0000", "33.3334", "100.000000")
                )
        );

        assertThat(sum(lines, DebitMemoAllocationLine::getAppliedDppOriginal)).isEqualByComparingTo("10.0000");
        assertThat(sum(lines, DebitMemoAllocationLine::getAppliedTaxOriginal)).isEqualByComparingTo("90.0000");
        assertThat(sum(lines, DebitMemoAllocationLine::getGrirReversalBase)).isEqualByComparingTo("1000.0000");
        assertThat(sum(lines, DebitMemoAllocationLine::getTaxReversalBase)).isEqualByComparingTo("9000.0000");
        assertThat(lines.get(2).getAppliedDppOriginal()).isEqualByComparingTo("3.3334");
    }

    @Test
    @DisplayName("prorate should use remaining debit memo snapshots after prior confirmed consumption")
    void prorate_shouldUseRemainingSnapshotsAfterPriorConfirmedConsumption() {
        List<DebitMemoAllocationLine> lines = service.prorate(
                snapshot("100.0000", "10.0000", "90.0000", "1000.0000", "9000.0000"),
                new AppliedTotals(bd("40.0000"), bd("4.0000"), bd("36.0000"), bd("400.0000"), bd("3600.0000")),
                List.of(
                        input(1L, "60.0000", "60.0000", "30.0000", "100.000000"),
                        input(2L, "60.0000", "60.0000", "30.0000", "100.000000")
                )
        );

        assertThat(sum(lines, DebitMemoAllocationLine::getAppliedDppOriginal)).isEqualByComparingTo("6.0000");
        assertThat(sum(lines, DebitMemoAllocationLine::getAppliedTaxOriginal)).isEqualByComparingTo("54.0000");
        assertThat(sum(lines, DebitMemoAllocationLine::getGrirReversalBase)).isEqualByComparingTo("600.0000");
        assertThat(sum(lines, DebitMemoAllocationLine::getTaxReversalBase)).isEqualByComparingTo("5400.0000");
    }

    @Test
    @DisplayName("prorate should support zero tax debit memo")
    void prorate_shouldSupportZeroTaxDebitMemo() {
        List<DebitMemoAllocationLine> lines = service.prorate(
                snapshot("100.0000", "100.0000", "0.0000", "150.0000", "0.0000"),
                AppliedTotals.none(),
                List.of(input(1L, "100.0000", "100.0000", "40.0000", "2.000000"))
        );

        DebitMemoAllocationLine line = lines.get(0);
        assertThat(line.getAppliedTaxOriginal()).isEqualByComparingTo("0.0000");
        assertThat(line.getTaxReversalBase()).isEqualByComparingTo("0.0000");
        assertThat(line.getAppliedDppOriginal()).isEqualByComparingTo("40.0000");
        assertThat(line.getGrirReversalBase()).isEqualByComparingTo("60.0000");
    }

    @Test
    @DisplayName("prorate should map lower AP reduction to FX loss")
    void prorate_shouldMapLowerApReductionToFxLoss() {
        List<DebitMemoAllocationLine> lines = service.prorate(
                snapshot("100.0000", "100.0000", "0.0000", "1000.0000", "0.0000"),
                AppliedTotals.none(),
                List.of(input(1L, "100.0000", "100.0000", "100.0000", "9.000000"))
        );

        DebitMemoAllocationLine line = lines.get(0);
        assertThat(line.getApReductionBase()).isEqualByComparingTo("900.0000");
        assertThat(line.getFxLossBase()).isEqualByComparingTo("100.0000");
        assertThat(line.getFxGainBase()).isEqualByComparingTo("0.0000");
    }

    @Test
    @DisplayName("prorate should reject invalid debit memo and over remaining requests")
    void prorate_shouldRejectInvalidDebitMemoAndOverRemainingRequests() {
        assertThatThrownBy(() -> service.prorate(
                snapshot("0.0000", "0.0000", "0.0000", "0.0000", "0.0000"),
                AppliedTotals.none(),
                List.of(input(1L, "1.0000", "1.0000", "1.0000", "1.000000"))
        ))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.debit-memo-allocation.debit-memo-amount-invalid");

        assertThatThrownBy(() -> service.prorate(
                snapshot("100.0000", "90.0000", "10.0000", "90.0000", "10.0000"),
                AppliedTotals.none(),
                List.of(input(1L, "100.0000", "200.0000", "101.0000", "1.000000"))
        ))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.debit-memo-allocation.over-debit-memo-remaining");
    }

    private DebitMemoSnapshot snapshot(String grossOriginal,
                                       String dppOriginal,
                                       String taxOriginal,
                                       String dppBase,
                                       String taxBase) {
        return new DebitMemoSnapshot(bd(grossOriginal), bd(dppOriginal), bd(taxOriginal), bd(dppBase), bd(taxBase));
    }

    private AllocationInput input(Long vendorBillId,
                                  String debitMemoRemainingAtDraft,
                                  String vendorBillOutstandingAtDraft,
                                  String appliedGrossOriginal,
                                  String vendorBillExchangeRate) {
        return new AllocationInput(vendorBillId, "VB-202606-" + vendorBillId,
                bd(debitMemoRemainingAtDraft), bd(vendorBillOutstandingAtDraft),
                bd(appliedGrossOriginal), bd(vendorBillExchangeRate));
    }

    private BigDecimal sum(List<DebitMemoAllocationLine> lines,
                           java.util.function.Function<DebitMemoAllocationLine, BigDecimal> mapper) {
        return lines.stream().map(mapper).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal bd(String value) {
        return new BigDecimal(value);
    }
}
