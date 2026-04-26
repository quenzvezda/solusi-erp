package com.solusi.erp.inventory.goodsreceipt.domain.model;

import com.solusi.erp.core.exception.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("GoodsReceipt Domain Model Tests")
class GoodsReceiptTest {

    @Test
    @DisplayName("complete requires at least one positive-quantity line")
    void complete_requiresPositiveLine() {
        GoodsReceipt receipt = GoodsReceipt.createNew(
                "GR-202604-00001",
                LocalDate.of(2026, 4, 26),
                7L, 11L, 3L, 1L, BigDecimal.ONE,
                List.of(GoodsReceiptLine.prefill(
                        101L, 201L, 301L, false,
                        BigDecimal.ZERO, 1L, null,
                        new BigDecimal("150.00"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                        null
                ))
        );

        assertThatThrownBy(receipt::complete)
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.gr.complete.no.lines");
    }

    @Test
    @DisplayName("completed receipt cannot be updated")
    void completedReceipt_cannotBeUpdated() {
        GoodsReceiptLine active = GoodsReceiptLine.prefill(
                101L, 201L, 301L, false,
                new BigDecimal("2.0000"), 1L, 99L,
                new BigDecimal("150.00"), new BigDecimal("2.0000"), new BigDecimal("300.0000"),
                BigDecimal.ZERO, new BigDecimal("300.0000"), null
        );
        GoodsReceipt receipt = GoodsReceipt.createNew(
                "GR-202604-00001",
                LocalDate.of(2026, 4, 26),
                7L, 11L, 3L, 1L, BigDecimal.ONE,
                List.of(active)
        );
        receipt.complete();

        assertThatThrownBy(() -> receipt.update(LocalDate.of(2026, 4, 27), "late edit", List.of(active)))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.gr.completed.immutable");
    }
}
