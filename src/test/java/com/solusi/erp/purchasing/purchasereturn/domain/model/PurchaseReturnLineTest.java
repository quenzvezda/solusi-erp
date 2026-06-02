package com.solusi.erp.purchasing.purchasereturn.domain.model;

import com.solusi.erp.core.exception.DomainException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PurchaseReturnLineTest {

    @Test
    void create_nonSerializedPositiveLine_succeeds() {
        PurchaseReturnLine line = line(false, BigDecimal.TEN, BigDecimal.TEN, null,
                PurchaseReturnReason.DAMAGED, null);

        assertThat(line.getContainerId()).isEqualTo(40L);
        assertThat(line.getTaxReversalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void create_zeroQuantity_rejects() {
        assertThatThrownBy(() -> line(false, BigDecimal.ZERO, BigDecimal.ZERO, null,
                PurchaseReturnReason.DAMAGED, null))
                .isInstanceOf(DomainException.class)
                .hasMessage("msg.error.purchase-return.line.quantity-positive");
    }

    @Test
    void create_otherWithoutNote_rejects() {
        assertThatThrownBy(() -> line(false, BigDecimal.ONE, BigDecimal.ONE, null,
                PurchaseReturnReason.OTHER, null))
                .isInstanceOf(DomainException.class)
                .hasMessage("msg.error.purchase-return.line.other-note-required");
    }

    @Test
    void create_withoutReason_rejects() {
        assertThatThrownBy(() -> line(false, BigDecimal.ONE, BigDecimal.ONE, null, null, null))
                .isInstanceOf(DomainException.class)
                .hasMessage("msg.error.purchase-return.line.reason-required");
    }

    @Test
    void create_withoutValuationReference_rejects() {
        assertThatThrownBy(() -> PurchaseReturnLine.create(
                1L, 10L, false, BigDecimal.ONE, 20L, BigDecimal.ONE, 30L, 35L, 40L,
                null, PurchaseReturnReason.DAMAGED, null, null, null, null,
                BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO))
                .isInstanceOf(DomainException.class)
                .hasMessage("msg.error.purchase-return.line.valuation-reference-required");
    }

    @Test
    void create_serializedFractionalBaseQuantity_rejects() {
        assertThatThrownBy(() -> line(true, new BigDecimal("1.5"), new BigDecimal("1.5"), "SER-001",
                PurchaseReturnReason.DAMAGED, null))
                .isInstanceOf(DomainException.class)
                .hasMessage("msg.error.purchase-return.line.serial-whole-quantity");
    }

    @Test
    void create_serializedCountMismatch_rejects() {
        assertThatThrownBy(() -> line(true, BigDecimal.valueOf(2), BigDecimal.valueOf(2), "SER-001",
                PurchaseReturnReason.DAMAGED, null))
                .isInstanceOf(DomainException.class)
                .hasMessage("msg.error.purchase-return.line.serial-count-mismatch");
    }

    @Test
    void create_serializedMatchingCsv_succeeds() {
        PurchaseReturnLine line = line(true, BigDecimal.valueOf(2), BigDecimal.valueOf(2), "SER-001, SER-002",
                PurchaseReturnReason.DAMAGED, null);

        assertThat(line.getSerialNumbers()).isEqualTo("SER-001, SER-002");
    }

    static PurchaseReturnLine line(boolean serialized,
                                   BigDecimal quantity,
                                   BigDecimal baseQuantity,
                                   String serialNumbers,
                                   PurchaseReturnReason reason,
                                   String note) {
        return PurchaseReturnLine.create(
                1L, 10L, serialized, quantity, 20L, baseQuantity, 30L, 35L, 40L,
                serialNumbers, reason, note, "GOODS_RECEIPT", 50L, 60L,
                new BigDecimal("100"), new BigDecimal("1000"), BigDecimal.ZERO, BigDecimal.ZERO
        );
    }
}
