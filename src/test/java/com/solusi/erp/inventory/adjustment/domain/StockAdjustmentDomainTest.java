package com.solusi.erp.inventory.adjustment.domain;

import com.solusi.erp.inventory.adjustment.domain.model.AdjustmentStatus;
import com.solusi.erp.inventory.adjustment.domain.model.StockAdjustment;
import com.solusi.erp.inventory.adjustment.domain.model.StockAdjustmentLineItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("StockAdjustment Domain Tests")
class StockAdjustmentDomainTest {

    private StockAdjustmentLineItem lineItem(BigDecimal qty, BigDecimal cost) {
        BigDecimal total = qty.multiply(cost);
        return new StockAdjustmentLineItem(null, null, 1L, "P001", "Product 1", false,
                null, null, null, 1L, "BIN-01", "Bin 1", "Main WH",
                1L, "PCS", BigDecimal.ONE, qty, cost, total, null);
    }

    @Test
    @DisplayName("createNew sets DRAFT status and null code")
    void createNew_setsDraftAndNullCode() {
        StockAdjustment sa = StockAdjustment.createNew(
                LocalDate.now(), "Test note", 1L, "Main WH",
                1L, "USD", BigDecimal.ONE,
                List.of(lineItem(new BigDecimal("2"), new BigDecimal("100"))));

        assertThat(sa.getStatus()).isEqualTo(AdjustmentStatus.DRAFT);
        assertThat(sa.getCode()).isNull();
        assertThat(sa.getNote()).isEqualTo("Test note");
        assertThat(sa.getFacilityName()).isEqualTo("Main WH");
    }

    @Test
    @DisplayName("createNew calculates totals correctly")
    void createNew_calculatesTotals() {
        List<StockAdjustmentLineItem> lines = List.of(
                lineItem(new BigDecimal("2"), new BigDecimal("100")),
                lineItem(new BigDecimal("3"), new BigDecimal("50"))
        );

        StockAdjustment sa = StockAdjustment.createNew(
                LocalDate.now(), null, 1L, "WH", 1L, "USD",
                new BigDecimal("15000"), lines);

        // original = 2*100 + 3*50 = 200 + 150 = 350
        assertThat(sa.getTotalAmountOriginal()).isEqualByComparingTo(new BigDecimal("350"));
        // local = 350 * 15000 = 5250000
        assertThat(sa.getTotalAmountLocal()).isEqualByComparingTo(new BigDecimal("5250000"));
    }

    @Test
    @DisplayName("update recalculates totals and updates fields")
    void update_recalculatesTotals() {
        StockAdjustment sa = StockAdjustment.createNew(
                LocalDate.now(), "old note", 1L, "WH", 1L, "USD",
                BigDecimal.ONE, List.of(lineItem(BigDecimal.ONE, new BigDecimal("100"))));

        sa.update(LocalDate.now(), "new note", 2L, "WH2", 2L, "IDR",
                new BigDecimal("2"),
                List.of(lineItem(new BigDecimal("5"), new BigDecimal("10"))));

        assertThat(sa.getNote()).isEqualTo("new note");
        assertThat(sa.getFacilityId()).isEqualTo(2L);
        assertThat(sa.getTotalAmountOriginal()).isEqualByComparingTo(new BigDecimal("50"));
        assertThat(sa.getTotalAmountLocal()).isEqualByComparingTo(new BigDecimal("100"));
    }

    @Test
    @DisplayName("update on COMPLETED throws exception")
    void update_onCompleted_throwsException() {
        StockAdjustment sa = StockAdjustment.createNew(
                LocalDate.now(), null, 1L, "WH", 1L, "USD", BigDecimal.ONE,
                List.of(lineItem(BigDecimal.ONE, BigDecimal.ONE)));
        sa.process();

        assertThatThrownBy(() ->
                sa.update(LocalDate.now(), null, 1L, "WH", 1L, "USD", BigDecimal.ONE, List.of()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already-completed");
    }

    @Test
    @DisplayName("process sets COMPLETED and returns lines")
    void process_setsCompletedAndReturnsLines() {
        List<StockAdjustmentLineItem> lines = List.of(
                lineItem(new BigDecimal("2"), new BigDecimal("50")));
        StockAdjustment sa = StockAdjustment.createNew(
                LocalDate.now(), null, 1L, "WH", 1L, "USD", BigDecimal.ONE, lines);

        List<StockAdjustmentLineItem> returned = sa.process();

        assertThat(sa.getStatus()).isEqualTo(AdjustmentStatus.COMPLETED);
        assertThat(returned).hasSize(1);
    }

    @Test
    @DisplayName("process on COMPLETED throws exception")
    void process_onCompleted_throwsException() {
        StockAdjustment sa = StockAdjustment.createNew(
                LocalDate.now(), null, 1L, "WH", 1L, "USD", BigDecimal.ONE,
                List.of(lineItem(BigDecimal.ONE, BigDecimal.ONE)));
        sa.process();

        assertThatThrownBy(sa::process)
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already-completed");
    }

    @Test
    @DisplayName("setCode updates code field")
    void setCode_updatesCode() {
        StockAdjustment sa = StockAdjustment.createNew(
                LocalDate.now(), null, 1L, "WH", 1L, "USD", BigDecimal.ONE, List.of());
        sa.setCode("ADJ-2025-0001");
        assertThat(sa.getCode()).isEqualTo("ADJ-2025-0001");
    }
}
