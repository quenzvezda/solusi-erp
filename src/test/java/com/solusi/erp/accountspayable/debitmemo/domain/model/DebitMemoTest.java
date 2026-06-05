package com.solusi.erp.accountspayable.debitmemo.domain.model;

import com.solusi.erp.core.exception.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("DebitMemo Domain Model Tests")
class DebitMemoTest {

    @Test
    @DisplayName("createNew should calculate header amounts and set open status")
    void createNew_shouldCalculateHeaderAmountsAndOpenStatus() {
        DebitMemo debitMemo = debitMemo(List.of(
                line(1L, "100.0000", "11.0000", "100.0000", "11.0000"),
                line(2L, "50.0000", "0.0000", "50.0000", "0.0000")
        ));

        assertThat(debitMemo.getSettlementStatus()).isEqualTo(DebitMemoSettlementStatus.OPEN);
        assertThat(debitMemo.getDppAmountOriginal()).isEqualByComparingTo("150.0000");
        assertThat(debitMemo.getTaxAmountOriginal()).isEqualByComparingTo("11.0000");
        assertThat(debitMemo.getGrossAmountOriginal()).isEqualByComparingTo("161.0000");
        assertThat(debitMemo.getDppAmountBase()).isEqualByComparingTo("150.0000");
        assertThat(debitMemo.getTaxAmountBase()).isEqualByComparingTo("11.0000");
        assertThat(debitMemo.getGrossAmountBase()).isEqualByComparingTo("161.0000");
    }

    @Test
    @DisplayName("createNew should fail without lines")
    void createNew_shouldFailWithoutLines() {
        assertThatThrownBy(() -> debitMemo(List.of()))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.debit-memo.lines-required");
    }

    @Test
    @DisplayName("createNew should fail when required header references are missing")
    void createNew_shouldFailWhenRequiredHeaderReferencesAreMissing() {
        List<DebitMemoLine> lines = List.of(line(1L, "100.0000", "0.0000", "100.0000", "0.0000"));

        assertThatThrownBy(() -> DebitMemo.createNew(" ", 100L, "PRT-001", 200L, 300L, LocalDate.now(), lines))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.debit-memo.code-required");
        assertThatThrownBy(() -> DebitMemo.createNew("DM-001", null, "PRT-001", 200L, 300L, LocalDate.now(), lines))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.debit-memo.purchase-return-required");
        assertThatThrownBy(() -> DebitMemo.createNew("DM-001", 100L, " ", 200L, 300L, LocalDate.now(), lines))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.debit-memo.purchase-return-required");
        assertThatThrownBy(() -> DebitMemo.createNew("DM-001", 100L, "PRT-001", null, 300L, LocalDate.now(), lines))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.debit-memo.vendor-currency-required");
        assertThatThrownBy(() -> DebitMemo.createNew("DM-001", 100L, "PRT-001", 200L, null, LocalDate.now(), lines))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.debit-memo.vendor-currency-required");
        assertThatThrownBy(() -> DebitMemo.createNew("DM-001", 100L, "PRT-001", 200L, 300L, null, lines))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.debit-memo.memo-date-required");
        assertThatThrownBy(() -> DebitMemo.createNew("DM-001", 100L, "PRT-001", 200L, 300L, LocalDate.now(), null))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.debit-memo.lines-required");
    }

    @Test
    @DisplayName("createNew should fail when gross is zero")
    void createNew_shouldFailWhenGrossIsZero() {
        assertThatThrownBy(() -> debitMemo(List.of(line(1L, "0.0000", "0.0000", "0.0000", "0.0000"))))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.debit-memo.gross-positive");
    }

    @Test
    @DisplayName("line should require positive quantity")
    void line_shouldRequirePositiveQuantity() {
        assertThatThrownBy(() -> new DebitMemoLine(1L, 10L, 20L, BigDecimal.ZERO, 30L,
                bd("100.0000"), bd("0.0000"), bd("100.0000"), bd("0.0000")))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.debit-memo.line.quantity-positive");
        assertThatThrownBy(() -> new DebitMemoLine(1L, 10L, 20L, null, 30L,
                bd("100.0000"), bd("0.0000"), bd("100.0000"), bd("0.0000")))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.debit-memo.line.quantity-positive");
    }

    @Test
    @DisplayName("line should require source product and uom references")
    void line_shouldRequireReferences() {
        assertThatThrownBy(() -> new DebitMemoLine(1L, null, 20L, bd("1.0000"), 30L,
                bd("100.0000"), bd("0.0000"), bd("100.0000"), bd("0.0000")))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.debit-memo.line.reference-required");
        assertThatThrownBy(() -> new DebitMemoLine(1L, 10L, null, bd("1.0000"), 30L,
                bd("100.0000"), bd("0.0000"), bd("100.0000"), bd("0.0000")))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.debit-memo.line.reference-required");
        assertThatThrownBy(() -> new DebitMemoLine(1L, 10L, 20L, bd("1.0000"), null,
                bd("100.0000"), bd("0.0000"), bd("100.0000"), bd("0.0000")))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.debit-memo.line.reference-required");
    }

    @Test
    @DisplayName("line should reject negative amounts")
    void line_shouldRejectNegativeAmounts() {
        assertThatThrownBy(() -> line(1L, "-1.0000", "0.0000", "0.0000", "0.0000"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.debit-memo.line.amount-non-negative");
        assertThatThrownBy(() -> new DebitMemoLine(1L, 10L, 20L, bd("1.0000"), 30L,
                null, bd("0.0000"), bd("100.0000"), bd("0.0000")))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.debit-memo.line.amount-non-negative");
        assertThatThrownBy(() -> line(1L, "1.0000", "-1.0000", "1.0000", "0.0000"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.debit-memo.line.amount-non-negative");
        assertThatThrownBy(() -> line(1L, "1.0000", "0.0000", "-1.0000", "0.0000"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.debit-memo.line.amount-non-negative");
        assertThatThrownBy(() -> line(1L, "1.0000", "0.0000", "1.0000", "-1.0000"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.debit-memo.line.amount-non-negative");
    }

    @Test
    @DisplayName("line should reject tax without dpp")
    void line_shouldRejectTaxWithoutDpp() {
        assertThatThrownBy(() -> line(1L, "0.0000", "1.0000", "0.0000", "1.0000"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.debit-memo.line.dpp-required-for-tax");
        assertThatThrownBy(() -> line(1L, "1.0000", "0.0000", "0.0000", "1.0000"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.debit-memo.line.dpp-required-for-tax");
    }

    @Test
    @DisplayName("metadata update should be allowed while open partially settled and settled")
    void updateMetadata_shouldBeAllowedWhileNotCancelled() {
        DebitMemo debitMemo = debitMemo(List.of(line(1L, "100.0000", "0.0000", "100.0000", "0.0000")));

        debitMemo.updateMetadata("  SUP-DM-001  ", LocalDate.of(2026, 6, 2),
                "TAX-001", LocalDate.of(2026, 6, 3), "notes");
        assertThat(debitMemo.getSupplierMemoNumber()).isEqualTo("SUP-DM-001");
        assertThat(debitMemo.getSupplierMemoDate()).isEqualTo(LocalDate.of(2026, 6, 2));
        assertThat(debitMemo.getTaxDocumentNumber()).isEqualTo("TAX-001");
        assertThat(debitMemo.getTaxDocumentDate()).isEqualTo(LocalDate.of(2026, 6, 3));
        assertThat(debitMemo.getNotes()).isEqualTo("notes");

        debitMemo.markPartiallySettled();
        debitMemo.updateMetadata("SUP-DM-002", null, null, null, "partial");
        assertThat(debitMemo.getSupplierMemoNumber()).isEqualTo("SUP-DM-002");
        assertThat(debitMemo.getTaxDocumentNumber()).isNull();

        debitMemo.markSettled();
        debitMemo.updateMetadata("SUP-DM-003", null, "TAX-003", null, "settled");
        assertThat(debitMemo.getSupplierMemoNumber()).isEqualTo("SUP-DM-003");
        assertThat(debitMemo.getTaxDocumentNumber()).isEqualTo("TAX-003");
    }

    @Test
    @DisplayName("metadata update should normalize blank external references")
    void updateMetadata_shouldNormalizeBlankExternalReferences() {
        DebitMemo debitMemo = debitMemo(List.of(line(1L, "100.0000", "0.0000", "100.0000", "0.0000")));

        debitMemo.updateMetadata(" ", null, " ", null, "notes");

        assertThat(debitMemo.getSupplierMemoNumber()).isNull();
        assertThat(debitMemo.getTaxDocumentNumber()).isNull();
        assertThat(debitMemo.getNotes()).isEqualTo("notes");
    }

    @Test
    @DisplayName("metadata update should fail when cancelled")
    void updateMetadata_shouldFailWhenCancelled() {
        DebitMemo debitMemo = debitMemo(List.of(line(1L, "100.0000", "0.0000", "100.0000", "0.0000")));
        debitMemo.cancel();

        assertThatThrownBy(() -> debitMemo.updateMetadata("SUP-DM-001", null, null, null, null))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.debit-memo.metadata.cancelled");
    }

    @Test
    @DisplayName("cancel should mark open memo as cancelled")
    void cancel_shouldMarkOpenMemoAsCancelled() {
        DebitMemo debitMemo = debitMemo(List.of(line(1L, "100.0000", "0.0000", "100.0000", "0.0000")));

        debitMemo.cancel();

        assertThat(debitMemo.getSettlementStatus()).isEqualTo(DebitMemoSettlementStatus.CANCELLED);
    }

    @Test
    @DisplayName("cancel should fail after partial settlement")
    void cancel_shouldFailAfterPartialSettlement() {
        DebitMemo debitMemo = debitMemo(List.of(line(1L, "100.0000", "0.0000", "100.0000", "0.0000")));
        debitMemo.markPartiallySettled();

        assertThatThrownBy(debitMemo::cancel)
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.debit-memo.cancel.only-open");
    }

    @Test
    @DisplayName("settlement transitions should reject invalid sources")
    void settlementTransitions_shouldRejectInvalidSources() {
        DebitMemo partiallySettled = debitMemo(List.of(line(1L, "100.0000", "0.0000", "100.0000", "0.0000")));
        partiallySettled.markPartiallySettled();

        assertThatThrownBy(partiallySettled::markPartiallySettled)
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.debit-memo.invalid-settlement-transition");

        DebitMemo cancelled = debitMemo(List.of(line(2L, "100.0000", "0.0000", "100.0000", "0.0000")));
        cancelled.cancel();
        assertThatThrownBy(cancelled::markSettled)
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.debit-memo.invalid-settlement-transition");
    }

    @Test
    @DisplayName("refresh settlement status should restore status from confirmed applied amount")
    void refreshSettlementStatus_shouldRestoreStatusFromConfirmedAppliedAmount() {
        DebitMemo debitMemo = debitMemo(List.of(line(1L, "100.0000", "0.0000", "100.0000", "0.0000")));

        debitMemo.refreshSettlementStatus(new BigDecimal("40.0000"));
        assertThat(debitMemo.getSettlementStatus()).isEqualTo(DebitMemoSettlementStatus.PARTIALLY_SETTLED);

        debitMemo.refreshSettlementStatus(new BigDecimal("100.0000"));
        assertThat(debitMemo.getSettlementStatus()).isEqualTo(DebitMemoSettlementStatus.SETTLED);

        debitMemo.refreshSettlementStatus(BigDecimal.ZERO);
        assertThat(debitMemo.getSettlementStatus()).isEqualTo(DebitMemoSettlementStatus.OPEN);
    }

    @Test
    @DisplayName("refresh settlement status should reject cancelled memo and negative applied amount")
    void refreshSettlementStatus_shouldRejectCancelledMemoAndNegativeAppliedAmount() {
        DebitMemo debitMemo = debitMemo(List.of(line(1L, "100.0000", "0.0000", "100.0000", "0.0000")));
        assertThatThrownBy(() -> debitMemo.refreshSettlementStatus(new BigDecimal("-0.0001")))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.debit-memo.invalid-settlement-transition");

        debitMemo.cancel();
        assertThatThrownBy(() -> debitMemo.refreshSettlementStatus(BigDecimal.ZERO))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.debit-memo.invalid-settlement-transition");
    }

    @Test
    @DisplayName("reconstitute should default null metadata and settlement status")
    void reconstitute_shouldDefaultNullMetadataAndSettlementStatus() {
        DebitMemo debitMemo = DebitMemo.reconstitute(
                null,
                "DM-202606-00001",
                100L,
                "PRT-202606-00001",
                200L,
                300L,
                LocalDate.of(2026, 6, 2),
                null,
                null,
                null,
                null,
                null,
                null,
                List.of(line(1L, "100.0000", "0.0000", "100.0000", "0.0000"))
        );

        assertThat(debitMemo.getId()).isNull();
        assertThat(debitMemo.getSettlementStatus()).isEqualTo(DebitMemoSettlementStatus.OPEN);
    }

    @Test
    @DisplayName("lines should be defensive copied")
    void lines_shouldBeDefensiveCopied() {
        DebitMemoLine line = line(1L, "100.0000", "0.0000", "100.0000", "0.0000");
        DebitMemo debitMemo = debitMemo(List.of(line));

        assertThat(debitMemo.getLines()).containsExactly(line);
        assertThatThrownBy(() -> debitMemo.getLines().add(line))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    private DebitMemo debitMemo(List<DebitMemoLine> lines) {
        return DebitMemo.createNew(
                "DM-202606-00001",
                100L,
                "PRT-202606-00001",
                200L,
                300L,
                LocalDate.of(2026, 6, 2),
                lines
        );
    }

    private DebitMemoLine line(Long id,
                               String dppOriginal,
                               String taxOriginal,
                               String dppBase,
                               String taxBase) {
        return new DebitMemoLine(id, 1000L + id, 2000L + id, bd("2.0000"), 3000L + id,
                bd(dppOriginal), bd(taxOriginal), bd(dppBase), bd(taxBase));
    }

    private BigDecimal bd(String value) {
        return new BigDecimal(value);
    }
}
