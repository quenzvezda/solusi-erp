package com.solusi.erp.accountspayable.debitmemoallocation.domain.model;

import com.solusi.erp.core.exception.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("DebitMemoAllocation Domain Model Tests")
class DebitMemoAllocationTest {

    @Test
    @DisplayName("createNew should calculate totals and set draft status")
    void createNew_shouldCalculateTotalsAndDraftStatus() {
        DebitMemoAllocation allocation = allocation(List.of(
                line(1L, "60.0000", "54.0000", "6.0000", "54.0000", "6.0000", "600.0000", "0.0000", "0.0000"),
                line(2L, "40.0000", "36.0000", "4.0000", "36.0000", "4.0000", "400.0000", "0.0000", "0.0000")
        ));

        assertThat(allocation.getStatus()).isEqualTo(DebitMemoAllocationStatus.DRAFT);
        assertThat(allocation.getTotalAppliedGrossOriginal()).isEqualByComparingTo("100.0000");
        assertThat(allocation.getTotalDppOriginal()).isEqualByComparingTo("90.0000");
        assertThat(allocation.getTotalTaxOriginal()).isEqualByComparingTo("10.0000");
        assertThat(allocation.getTotalGrirReversalBase()).isEqualByComparingTo("90.0000");
        assertThat(allocation.getTotalTaxReversalBase()).isEqualByComparingTo("10.0000");
        assertThat(allocation.getTotalApReductionBase()).isEqualByComparingTo("1000.0000");
    }

    @Test
    @DisplayName("createNew should reject missing header values and lines")
    void createNew_shouldRejectMissingHeaderValuesAndLines() {
        List<DebitMemoAllocationLine> lines = List.of(line(1L, "10.0000"));

        assertThatThrownBy(() -> DebitMemoAllocation.createNew(" ", 10L, "DM-001", LocalDate.now(), null, lines))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.debit-memo-allocation.code-required");
        assertThatThrownBy(() -> DebitMemoAllocation.createNew("DMA-001", null, "DM-001", LocalDate.now(), null, lines))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.debit-memo-allocation.debit-memo-required");
        assertThatThrownBy(() -> DebitMemoAllocation.createNew("DMA-001", 10L, " ", LocalDate.now(), null, lines))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.debit-memo-allocation.debit-memo-required");
        assertThatThrownBy(() -> DebitMemoAllocation.createNew("DMA-001", 10L, "DM-001", null, null, lines))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.debit-memo-allocation.allocation-date-required");
        assertThatThrownBy(() -> DebitMemoAllocation.createNew("DMA-001", 10L, "DM-001", LocalDate.now(), null, List.of()))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.debit-memo-allocation.lines-required");
    }

    @Test
    @DisplayName("update should only be allowed while draft")
    void update_shouldOnlyBeAllowedWhileDraft() {
        DebitMemoAllocation allocation = allocation(List.of(line(1L, "10.0000")));

        allocation.update(LocalDate.of(2026, 6, 6), "updated", List.of(line(2L, "20.0000")));

        assertThat(allocation.getAllocationDate()).isEqualTo(LocalDate.of(2026, 6, 6));
        assertThat(allocation.getNotes()).isEqualTo("updated");
        assertThat(allocation.getTotalAppliedGrossOriginal()).isEqualByComparingTo("20.0000");

        allocation.confirm(9001L);
        assertThatThrownBy(() -> allocation.update(LocalDate.now(), null, List.of(line(3L, "30.0000"))))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.debit-memo-allocation.edit.only-draft");
    }

    @Test
    @DisplayName("cancel should only be allowed while draft")
    void cancel_shouldOnlyBeAllowedWhileDraft() {
        DebitMemoAllocation allocation = allocation(List.of(line(1L, "10.0000")));

        allocation.cancel();

        assertThat(allocation.getStatus()).isEqualTo(DebitMemoAllocationStatus.CANCELLED);
        assertThatThrownBy(allocation::cancel)
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.debit-memo-allocation.cancel.only-draft");
    }

    @Test
    @DisplayName("confirm and reverse should enforce lifecycle transitions")
    void confirmAndReverse_shouldEnforceLifecycleTransitions() {
        DebitMemoAllocation allocation = allocation(List.of(line(1L, "10.0000")));

        allocation.confirm(9001L);

        assertThat(allocation.getStatus()).isEqualTo(DebitMemoAllocationStatus.CONFIRMED);
        assertThat(allocation.getApplyJournalEntryId()).isEqualTo(9001L);
        assertThatThrownBy(() -> allocation.confirm(9002L))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.debit-memo-allocation.confirm.only-draft");
        assertThatThrownBy(allocation::cancel)
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.debit-memo-allocation.cancel.only-draft");

        allocation.reverse(9003L, LocalDate.of(2026, 6, 7), "wrong allocation");

        assertThat(allocation.getStatus()).isEqualTo(DebitMemoAllocationStatus.REVERSED);
        assertThat(allocation.getReversalJournalEntryId()).isEqualTo(9003L);
        assertThat(allocation.getReversalDate()).isEqualTo(LocalDate.of(2026, 6, 7));
        assertThat(allocation.getReversalReason()).isEqualTo("wrong allocation");
        assertThatThrownBy(() -> allocation.reverse(9004L, LocalDate.now(), "again"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.debit-memo-allocation.reverse.only-confirmed");
    }

    @Test
    @DisplayName("reverse should require confirmed status and reversal date")
    void reverse_shouldRequireConfirmedStatusAndReversalDate() {
        DebitMemoAllocation draft = allocation(List.of(line(1L, "10.0000")));
        assertThatThrownBy(() -> draft.reverse(9001L, LocalDate.now(), "draft"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.debit-memo-allocation.reverse.only-confirmed");

        DebitMemoAllocation confirmed = allocation(List.of(line(2L, "10.0000")));
        confirmed.confirm(9001L);
        assertThatThrownBy(() -> confirmed.reverse(9002L, null, "missing date"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.debit-memo-allocation.reversal-date-required");
    }

    @Test
    @DisplayName("createNew should reject duplicate vendor bills")
    void createNew_shouldRejectDuplicateVendorBills() {
        assertThatThrownBy(() -> allocation(List.of(line(1L, "10.0000"), line(1L, "20.0000"))))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.debit-memo-allocation.duplicate-vendor-bill");
    }

    @Test
    @DisplayName("line should reject non-positive applied amount")
    void line_shouldRejectNonPositiveAppliedAmount() {
        assertThatThrownBy(() -> line(1L, "0.0000"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.debit-memo-allocation.line.amount-positive");
        assertThatThrownBy(() -> line(1L, "-1.0000"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.debit-memo-allocation.line.amount-positive");
    }

    @Test
    @DisplayName("line should reject invalid references amounts and exchange rate")
    void line_shouldRejectInvalidReferencesAmountsAndExchangeRate() {
        assertThatThrownBy(() -> new DebitMemoAllocationLine(null, null, "VB-001", bd("100.0000"), bd("100.0000"),
                bd("10.0000"), bd("9.0000"), bd("1.0000"), bd("9.0000"), bd("1.0000"), bd("1.0000"),
                bd("10.0000"), bd("0.0000"), bd("0.0000")))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.debit-memo-allocation.line.vendor-bill-required");
        assertThatThrownBy(() -> new DebitMemoAllocationLine(null, 1L, "VB-001", bd("-1.0000"), bd("100.0000"),
                bd("10.0000"), bd("9.0000"), bd("1.0000"), bd("9.0000"), bd("1.0000"), bd("1.0000"),
                bd("10.0000"), bd("0.0000"), bd("0.0000")))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.debit-memo-allocation.line.amount-non-negative");
        assertThatThrownBy(() -> new DebitMemoAllocationLine(null, 1L, "VB-001", bd("100.0000"), bd("100.0000"),
                bd("10.0000"), bd("9.0000"), bd("1.0000"), bd("9.0000"), bd("1.0000"), bd("0.0000"),
                bd("10.0000"), bd("0.0000"), bd("0.0000")))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.debit-memo-allocation.line.exchange-rate-positive");
    }

    @Test
    @DisplayName("createNew should reject over debit memo remaining and over vendor bill outstanding")
    void createNew_shouldRejectOverRemainingAndOutstanding() {
        assertThatThrownBy(() -> allocation(List.of(
                line(1L, "60.0000"),
                line(2L, "50.0000")
        )))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.debit-memo-allocation.over-debit-memo-remaining");

        assertThatThrownBy(() -> new DebitMemoAllocationLine(null, 1L, "VB-001", bd("100.0000"), bd("9.0000"),
                bd("10.0000"), bd("9.0000"), bd("1.0000"), bd("9.0000"), bd("1.0000"), bd("1.0000"),
                bd("10.0000"), bd("0.0000"), bd("0.0000")))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.debit-memo-allocation.over-vendor-bill-outstanding");
    }

    @Test
    @DisplayName("lines should be defensive copied")
    void lines_shouldBeDefensiveCopied() {
        DebitMemoAllocationLine line = line(1L, "10.0000");
        DebitMemoAllocation allocation = allocation(List.of(line));

        assertThat(allocation.getLines()).containsExactly(line);
        assertThatThrownBy(() -> allocation.getLines().add(line))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    private DebitMemoAllocation allocation(List<DebitMemoAllocationLine> lines) {
        return DebitMemoAllocation.createNew(
                "DMA-202606-00001",
                100L,
                "DM-202606-00001",
                LocalDate.of(2026, 6, 5),
                "notes",
                lines
        );
    }

    private DebitMemoAllocationLine line(Long vendorBillId, String appliedGrossOriginal) {
        return line(vendorBillId, appliedGrossOriginal, appliedGrossOriginal, "0.0000",
                appliedGrossOriginal, "0.0000", appliedGrossOriginal, "0.0000", "0.0000");
    }

    private DebitMemoAllocationLine line(Long vendorBillId,
                                         String appliedGrossOriginal,
                                         String appliedDppOriginal,
                                         String appliedTaxOriginal,
                                         String grirReversalBase,
                                         String taxReversalBase,
                                         String apReductionBase,
                                         String fxLossBase,
                                         String fxGainBase) {
        return new DebitMemoAllocationLine(
                null,
                vendorBillId,
                "VB-202606-" + vendorBillId,
                bd("100.0000"),
                bd("100.0000"),
                bd(appliedGrossOriginal),
                bd(appliedDppOriginal),
                bd(appliedTaxOriginal),
                bd(grirReversalBase),
                bd(taxReversalBase),
                bd("1.000000"),
                bd(apReductionBase),
                bd(fxLossBase),
                bd(fxGainBase)
        );
    }

    private BigDecimal bd(String value) {
        return new BigDecimal(value);
    }
}
