package com.solusi.erp.accountspayable.vendorpayment.domain.model;

import com.solusi.erp.core.exception.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("VendorPayment Domain Model Tests")
class VendorPaymentTest {

    @Test
    @DisplayName("createNew sets status to DRAFT and defensively copies lines")
    void createNew_setsStatusDraftAndCopiesLines() {
        VendorPaymentLine line = line("VB-001", "500.00", "500.00");
        VendorPayment payment = VendorPayment.createNew(
                "VP-202605-00001", 1L, 1L, 1L,
                LocalDate.of(2026, 5, 15), BigDecimal.ONE,
                new BigDecimal("500.00"), "REF-001", "notes", List.of(line));

        assertThat(payment.getStatus()).isEqualTo(VendorPaymentStatus.DRAFT);
        assertThat(payment.getLines()).hasSize(1);
        assertThatThrownBy(() -> payment.getLines().add(line))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("createNew fails when amount is zero or negative")
    void createNew_failsWhenAmountNotPositive() {
        assertThatThrownBy(() -> VendorPayment.createNew(
                "VP-001", 1L, 1L, 1L, LocalDate.now(), BigDecimal.ONE,
                BigDecimal.ZERO, null, null, List.of(line("VB-001", "100.00", "100.00"))))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.err.vp.amount.positive");

        assertThatThrownBy(() -> VendorPayment.createNew(
                "VP-001", 1L, 1L, 1L, LocalDate.now(), BigDecimal.ONE,
                new BigDecimal("-1"), null, null, List.of(line("VB-001", "100.00", "100.00"))))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.err.vp.amount.positive");
    }

    @Test
    @DisplayName("createNew fails when lines are empty")
    void createNew_failsWhenNoLines() {
        assertThatThrownBy(() -> VendorPayment.createNew(
                "VP-001", 1L, 1L, 1L, LocalDate.now(), BigDecimal.ONE,
                new BigDecimal("100.00"), null, null, List.of()))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.err.vp.lines.required");
    }

    @Test
    @DisplayName("confirm sets status to CONFIRMED when sum matches paymentAmount")
    void confirm_setsStatusConfirmed() {
        VendorPayment payment = draftPayment("500.00",
                List.of(line("VB-001", "300.00", "300.00"), line("VB-002", "200.00", "200.00")));

        payment.confirm();

        assertThat(payment.getStatus()).isEqualTo(VendorPaymentStatus.CONFIRMED);
    }

    @Test
    @DisplayName("confirm fails when sum of paidAmount does not match paymentAmount")
    void confirm_failsWhenAmountMismatch() {
        VendorPayment payment = draftPayment("500.00",
                List.of(line("VB-001", "300.00", "200.00")));

        assertThatThrownBy(payment::confirm)
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.err.vp.amount.mismatch");
    }

    @Test
    @DisplayName("confirm fails when status is not DRAFT")
    void confirm_failsWhenNotDraft() {
        VendorPayment payment = draftPayment("500.00",
                List.of(line("VB-001", "500.00", "500.00")));
        payment.confirm();

        assertThatThrownBy(payment::confirm)
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.err.vp.confirm.only.draft");
    }

    @Test
    @DisplayName("cancel sets status to CANCELLED from DRAFT")
    void cancel_setsStatusCancelled() {
        VendorPayment payment = draftPayment("500.00",
                List.of(line("VB-001", "500.00", "500.00")));

        payment.cancel();

        assertThat(payment.getStatus()).isEqualTo(VendorPaymentStatus.CANCELLED);
    }

    @Test
    @DisplayName("cancel fails when status is CONFIRMED")
    void cancel_failsWhenConfirmed() {
        VendorPayment payment = draftPayment("500.00",
                List.of(line("VB-001", "500.00", "500.00")));
        payment.confirm();

        assertThatThrownBy(payment::cancel)
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.err.vp.cancel.only.draft");
    }

    @Test
    @DisplayName("update fails when status is not DRAFT")
    void update_failsWhenNotDraft() {
        VendorPayment payment = draftPayment("500.00",
                List.of(line("VB-001", "500.00", "500.00")));
        payment.confirm();

        assertThatThrownBy(() -> payment.update(1L, 1L, 1L, LocalDate.now(),
                BigDecimal.ONE, new BigDecimal("500.00"), null, null,
                List.of(line("VB-001", "500.00", "500.00"))))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.err.vp.edit.only.draft");
    }

    @Test
    @DisplayName("update changes mutable fields when DRAFT")
    void update_changesMutableFields() {
        VendorPayment payment = draftPayment("500.00",
                List.of(line("VB-001", "500.00", "500.00")));

        payment.update(2L, 2L, 2L, LocalDate.of(2026, 6, 1),
                new BigDecimal("16000"), new BigDecimal("1000.00"),
                "NEW-REF", "new notes",
                List.of(line("VB-002", "1000.00", "1000.00")));

        assertThat(payment.getVendorId()).isEqualTo(2L);
        assertThat(payment.getCurrencyId()).isEqualTo(2L);
        assertThat(payment.getBankAccountId()).isEqualTo(2L);
        assertThat(payment.getPaymentAmount()).isEqualByComparingTo("1000.00");
        assertThat(payment.getReference()).isEqualTo("NEW-REF");
        assertThat(payment.getLines()).hasSize(1);
        assertThat(payment.getLines().get(0).getBillCode()).isEqualTo("VB-002");
    }

    private VendorPayment draftPayment(String amount, List<VendorPaymentLine> lines) {
        return VendorPayment.createNew(
                "VP-202605-00001", 1L, 1L, 1L,
                LocalDate.of(2026, 5, 15), BigDecimal.ONE,
                new BigDecimal(amount), "REF-001", "notes", lines);
    }

    private VendorPaymentLine line(String billCode, String outstanding, String paid) {
        return new VendorPaymentLine(null, 1L, billCode,
                new BigDecimal(outstanding), new BigDecimal(paid));
    }
}
