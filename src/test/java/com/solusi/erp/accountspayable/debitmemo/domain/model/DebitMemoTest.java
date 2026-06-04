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
    }

    @Test
    @DisplayName("line should reject negative amounts")
    void line_shouldRejectNegativeAmounts() {
        assertThatThrownBy(() -> line(1L, "-1.0000", "0.0000", "0.0000", "0.0000"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.debit-memo.line.amount-non-negative");
    }

    @Test
    @DisplayName("line should reject tax without dpp")
    void line_shouldRejectTaxWithoutDpp() {
        assertThatThrownBy(() -> line(1L, "0.0000", "1.0000", "0.0000", "1.0000"))
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

