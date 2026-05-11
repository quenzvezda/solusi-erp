package com.solusi.erp.accountspayable.vendorbill.domain.model;

import com.solusi.erp.core.exception.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("VendorBill Domain Model Tests")
class VendorBillTest {

    @Test
    @DisplayName("confirm should fail when status is not draft")
    void confirm_should_fail_when_status_not_draft() {
        VendorBill bill = draftBill(List.of(line()));
        bill.confirm(new BigDecimal("100.00"), new BigDecimal("11.00"), new BigDecimal("111.00"));

        assertThatThrownBy(() -> bill.confirm(new BigDecimal("100.00"), BigDecimal.ZERO, new BigDecimal("100.00")))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.vb.invalid.status");
    }

    @Test
    @DisplayName("confirm should fail when no lines")
    void confirm_should_fail_when_no_lines() {
        VendorBill bill = draftBill(List.of());

        assertThatThrownBy(() -> bill.confirm(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.vb.lines.required");
    }

    @Test
    @DisplayName("cancel should fail when confirmed")
    void cancel_should_fail_when_confirmed() {
        VendorBill bill = draftBill(List.of(line()));
        bill.confirm(new BigDecimal("100.00"), new BigDecimal("11.00"), new BigDecimal("111.00"));

        assertThatThrownBy(bill::cancel)
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.vb.invalid.status");
    }

    @Test
    @DisplayName("confirm sets monetary totals and status")
    void confirm_setsTotalsAndStatus() {
        VendorBill bill = draftBill(List.of(line()));

        bill.confirm(new BigDecimal("100.00"), new BigDecimal("11.00"), new BigDecimal("111.00"));

        assertThat(bill.getStatus()).isEqualTo(VendorBillStatus.CONFIRMED);
        assertThat(bill.getSubtotal()).isEqualByComparingTo("100.00");
        assertThat(bill.getTaxAmount()).isEqualByComparingTo("11.00");
        assertThat(bill.getTotalAmount()).isEqualByComparingTo("111.00");
    }

    @Test
    @DisplayName("create new defensively copies references and lines")
    void createNew_defensivelyCopiesCollections() {
        VendorBillGrRef grRef = new VendorBillGrRef(null, 15L);
        VendorBillLine line = line();

        VendorBill bill = VendorBill.createNew(
                "VB-202605-00001", 20L, "INV-001", LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 31), 1L, "notes", List.of(grRef), List.of(line)
        );

        assertThat(bill.getStatus()).isEqualTo(VendorBillStatus.DRAFT);
        assertThat(bill.getGrRefs()).containsExactly(grRef);
        assertThat(bill.getLines()).containsExactly(line);
        assertThatThrownBy(() -> bill.getLines().add(line))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> bill.getGrRefs().add(grRef))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("cancel marks draft bill as cancelled")
    void cancel_marksDraftBillAsCancelled() {
        VendorBill bill = draftBill(List.of(line()));

        bill.cancel();

        assertThat(bill.getStatus()).isEqualTo(VendorBillStatus.CANCELLED);
    }

    private VendorBill draftBill(List<VendorBillLine> lines) {
        return VendorBill.createNew(
                "VB-202605-00001", 20L, "INV-001", LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 31), 1L, "notes",
                List.of(new VendorBillGrRef(null, 15L)), lines
        );
    }

    private VendorBillLine line() {
        return new VendorBillLine(
                null, 101L, 201L, "Product A", "Line A", new BigDecimal("2.0000"),
                1L, "PCS", new BigDecimal("50.00"), new BigDecimal("100.00"),
                new BigDecimal("11.00"), new BigDecimal("111.00")
        );
    }
}
