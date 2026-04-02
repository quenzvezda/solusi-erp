package com.solusi.erp.inventory.stock.domain;

import com.solusi.erp.inventory.stock.domain.model.MovementType;
import com.solusi.erp.inventory.stock.domain.model.StockBalance;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("StockBalance Domain Tests")
class StockBalanceDomainTest {

    @Test
    @DisplayName("createNew initializes all quantities to zero")
    void createNew_initializesZero() {
        StockBalance sb = StockBalance.createNew(1L, 10L, null);

        assertThat(sb.getProductId()).isEqualTo(1L);
        assertThat(sb.getContainerId()).isEqualTo(10L);
        assertThat(sb.getSerialNumber()).isNull();
        assertThat(sb.getQuantity()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(sb.getReservedQuantity()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(sb.getInTransitQuantity()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(sb.getId()).isNull();
    }

    @Test
    @DisplayName("getAvailableQuantity = quantity - reserved")
    void availableQuantity_isOnHandMinusReserved() {
        StockBalance sb = StockBalance.createNew(1L, 1L, null);
        sb.applyMovement(MovementType.RECEIPT, BigDecimal.TEN);
        sb.applyMovement(MovementType.RESERVE, BigDecimal.valueOf(3));

        assertThat(sb.getAvailableQuantity()).isEqualByComparingTo(BigDecimal.valueOf(7));
    }

    @Nested
    @DisplayName("applyMovement")
    class ApplyMovement {

        @Test
        @DisplayName("RECEIPT increases on-hand quantity")
        void receipt_increasesOnHand() {
            StockBalance sb = StockBalance.createNew(1L, 1L, null);
            sb.applyMovement(MovementType.RECEIPT, BigDecimal.TEN);
            assertThat(sb.getQuantity()).isEqualByComparingTo(BigDecimal.TEN);
        }

        @Test
        @DisplayName("TRANSFER_IN increases on-hand quantity")
        void transferIn_increasesOnHand() {
            StockBalance sb = StockBalance.createNew(1L, 1L, null);
            sb.applyMovement(MovementType.TRANSFER_IN, BigDecimal.valueOf(5));
            assertThat(sb.getQuantity()).isEqualByComparingTo(BigDecimal.valueOf(5));
        }

        @Test
        @DisplayName("ADJUSTMENT adds to on-hand (positive or negative)")
        void adjustment_addsAlgebraically() {
            StockBalance sb = StockBalance.createNew(1L, 1L, null);
            sb.applyMovement(MovementType.ADJUSTMENT, BigDecimal.TEN);
            assertThat(sb.getQuantity()).isEqualByComparingTo(BigDecimal.TEN);

            sb.applyMovement(MovementType.ADJUSTMENT, BigDecimal.valueOf(-3));
            assertThat(sb.getQuantity()).isEqualByComparingTo(BigDecimal.valueOf(7));
        }

        @Test
        @DisplayName("ISSUE decreases on-hand quantity")
        void issue_decreasesOnHand() {
            StockBalance sb = StockBalance.createNew(1L, 1L, null);
            sb.applyMovement(MovementType.RECEIPT, BigDecimal.TEN);
            sb.applyMovement(MovementType.ISSUE, BigDecimal.valueOf(3));
            assertThat(sb.getQuantity()).isEqualByComparingTo(BigDecimal.valueOf(7));
        }

        @Test
        @DisplayName("TRANSFER_OUT decreases on-hand quantity")
        void transferOut_decreasesOnHand() {
            StockBalance sb = StockBalance.createNew(1L, 1L, null);
            sb.applyMovement(MovementType.RECEIPT, BigDecimal.TEN);
            sb.applyMovement(MovementType.TRANSFER_OUT, BigDecimal.valueOf(3));
            assertThat(sb.getQuantity()).isEqualByComparingTo(BigDecimal.valueOf(7));
        }

        @Test
        @DisplayName("ISSUE_RESERVED decreases both on-hand and reserved")
        void issueReserved_decreasesBoth() {
            StockBalance sb = StockBalance.createNew(1L, 1L, null);
            sb.applyMovement(MovementType.RECEIPT, BigDecimal.TEN);
            sb.applyMovement(MovementType.RESERVE, BigDecimal.valueOf(5));
            sb.applyMovement(MovementType.ISSUE_RESERVED, BigDecimal.valueOf(5));

            assertThat(sb.getQuantity()).isEqualByComparingTo(BigDecimal.valueOf(5));
            assertThat(sb.getReservedQuantity()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("RESERVE increases reserved quantity")
        void reserve_increasesReserved() {
            StockBalance sb = StockBalance.createNew(1L, 1L, null);
            sb.applyMovement(MovementType.RECEIPT, BigDecimal.TEN);
            sb.applyMovement(MovementType.RESERVE, BigDecimal.valueOf(4));
            assertThat(sb.getReservedQuantity()).isEqualByComparingTo(BigDecimal.valueOf(4));
            assertThat(sb.getQuantity()).isEqualByComparingTo(BigDecimal.TEN);
        }

        @Test
        @DisplayName("RELEASE decreases reserved quantity")
        void release_decreasesReserved() {
            StockBalance sb = StockBalance.createNew(1L, 1L, null);
            sb.applyMovement(MovementType.RECEIPT, BigDecimal.TEN);
            sb.applyMovement(MovementType.RESERVE, BigDecimal.valueOf(5));
            sb.applyMovement(MovementType.RELEASE, BigDecimal.valueOf(2));

            assertThat(sb.getReservedQuantity()).isEqualByComparingTo(BigDecimal.valueOf(3));
        }
    }

    @Nested
    @DisplayName("validate")
    class Validate {

        @Test
        @DisplayName("passes when quantities are non-negative")
        void passes_whenNonNegative() {
            StockBalance sb = StockBalance.createNew(1L, 1L, null);
            sb.applyMovement(MovementType.RECEIPT, BigDecimal.TEN);
            sb.validate(); // should not throw
        }

        @Test
        @DisplayName("throws when on-hand becomes negative")
        void throws_whenOnHandNegative() {
            StockBalance sb = StockBalance.createNew(1L, 1L, null);
            sb.applyMovement(MovementType.ISSUE, BigDecimal.TEN);

            assertThatThrownBy(sb::validate)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("insufficient_stock");
        }

        @Test
        @DisplayName("throws when reserved becomes negative")
        void throws_whenReservedNegative() {
            StockBalance sb = StockBalance.createNew(1L, 1L, null);
            sb.applyMovement(MovementType.RECEIPT, BigDecimal.TEN);
            sb.applyMovement(MovementType.RELEASE, BigDecimal.valueOf(5));

            assertThatThrownBy(sb::validate)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("insufficient_reserved");
        }

        @Test
        @DisplayName("passes when quantities are exactly zero")
        void passes_whenExactlyZero() {
            StockBalance sb = StockBalance.createNew(1L, 1L, null);
            sb.validate(); // zero is valid
        }
    }
}
