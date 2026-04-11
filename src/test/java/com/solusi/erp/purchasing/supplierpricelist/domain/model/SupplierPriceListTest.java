package com.solusi.erp.purchasing.supplierpricelist.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("SupplierPriceList Domain Model Tests")
class SupplierPriceListTest {

    @Nested
    @DisplayName("createNew factory method")
    class CreateNew {

        @Test
        @DisplayName("creates new price list with valid parameters and empty metadata")
        void createNew_withValidParams_setsFieldsAndEmptyMetadata() {
            SupplierPriceList spl = SupplierPriceList.createNew(
                "SPL-2607-00001",
                1L, 2L, 3L, 4L,
                new BigDecimal("150.0000"),
                new BigDecimal("10.0000"),
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 12, 31),
                "Bulk discount",
                true
            );

            assertThat(spl.getId()).isNull();
            assertThat(spl.getCode()).isEqualTo("SPL-2607-00001");
            assertThat(spl.getSupplierId()).isEqualTo(1L);
            assertThat(spl.getProductId()).isEqualTo(2L);
            assertThat(spl.getUomId()).isEqualTo(3L);
            assertThat(spl.getCurrencyId()).isEqualTo(4L);
            assertThat(spl.getUnitPrice()).isEqualByComparingTo("150.0000");
            assertThat(spl.getMinQuantity()).isEqualByComparingTo("10.0000");
            assertThat(spl.getEffectiveFrom()).isEqualTo(LocalDate.of(2026, 7, 1));
            assertThat(spl.getEffectiveTo()).isEqualTo(LocalDate.of(2026, 12, 31));
            assertThat(spl.getNote()).isEqualTo("Bulk discount");
            assertThat(spl.isActive()).isTrue();
        }

        @Test
        @DisplayName("creates new price list with null effective_to (open-ended)")
        void createNew_withNullEffectiveTo_succeeds() {
            SupplierPriceList spl = SupplierPriceList.createNew(
                "SPL-2607-00002",
                1L, 2L, 3L, 4L,
                new BigDecimal("100.0000"),
                new BigDecimal("1.0000"),
                LocalDate.of(2026, 7, 1),
                null,
                null,
                true
            );

            assertThat(spl.getEffectiveTo()).isNull();
            assertThat(spl.getNote()).isNull();
        }

        @Test
        @DisplayName("throws DomainException when unit price is zero")
        void createNew_withZeroPrice_throwsDomainException() {
            assertThatThrownBy(() -> SupplierPriceList.createNew(
                "SPL-001", 1L, 2L, 3L, 4L,
                BigDecimal.ZERO,
                new BigDecimal("1.0000"),
                LocalDate.of(2026, 7, 1),
                null, null, true
            ))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.spl.price.positive");
        }

        @Test
        @DisplayName("throws DomainException when unit price is negative")
        void createNew_withNegativePrice_throwsDomainException() {
            assertThatThrownBy(() -> SupplierPriceList.createNew(
                "SPL-001", 1L, 2L, 3L, 4L,
                new BigDecimal("-5.0000"),
                new BigDecimal("1.0000"),
                LocalDate.of(2026, 7, 1),
                null, null, true
            ))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.spl.price.positive");
        }

        @Test
        @DisplayName("throws DomainException when effective_from is after effective_to")
        void createNew_withInvalidDateRange_throwsDomainException() {
            assertThatThrownBy(() -> SupplierPriceList.createNew(
                "SPL-001", 1L, 2L, 3L, 4L,
                new BigDecimal("100.0000"),
                new BigDecimal("1.0000"),
                LocalDate.of(2026, 12, 31),
                LocalDate.of(2026, 1, 1),
                null, true
            ))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.spl.date.range");
        }

        @Test
        @DisplayName("succeeds when effective_from equals effective_to")
        void createNew_withSameDates_succeeds() {
            LocalDate sameDate = LocalDate.of(2026, 7, 15);
            SupplierPriceList spl = SupplierPriceList.createNew(
                "SPL-001", 1L, 2L, 3L, 4L,
                new BigDecimal("100.0000"),
                new BigDecimal("1.0000"),
                sameDate, sameDate,
                null, true
            );

            assertThat(spl.getEffectiveFrom()).isEqualTo(sameDate);
            assertThat(spl.getEffectiveTo()).isEqualTo(sameDate);
        }
    }

    @Nested
    @DisplayName("full constructor")
    class FullConstructor {

        @Test
        @DisplayName("preserves all fields including metadata")
        void constructor_preservesAllFieldsIncludingMetadata() {
            AuditMetadata metadata = new AuditMetadata(99L, 2L, null, null, null, null);
            SupplierPriceList spl = new SupplierPriceList(
                metadata,
                "SPL-2607-00001",
                10L, 20L, 30L, 40L,
                new BigDecimal("200.5000"),
                new BigDecimal("5.0000"),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 6, 30),
                "Note",
                true
            );

            assertThat(spl.getId()).isEqualTo(99L);
            assertThat(spl.getMetadata()).isEqualTo(metadata);
            assertThat(spl.getCode()).isEqualTo("SPL-2607-00001");
            assertThat(spl.getSupplierId()).isEqualTo(10L);
            assertThat(spl.getProductId()).isEqualTo(20L);
            assertThat(spl.getUomId()).isEqualTo(30L);
            assertThat(spl.getCurrencyId()).isEqualTo(40L);
            assertThat(spl.getUnitPrice()).isEqualByComparingTo("200.5000");
            assertThat(spl.getMinQuantity()).isEqualByComparingTo("5.0000");
            assertThat(spl.getEffectiveFrom()).isEqualTo(LocalDate.of(2026, 1, 1));
            assertThat(spl.getEffectiveTo()).isEqualTo(LocalDate.of(2026, 6, 30));
            assertThat(spl.getNote()).isEqualTo("Note");
            assertThat(spl.isActive()).isTrue();
        }
    }

    @Nested
    @DisplayName("update method")
    class Update {

        @Test
        @DisplayName("updates mutable fields; code and supplierId remain unchanged")
        void update_changesMutableFields() {
            SupplierPriceList spl = SupplierPriceList.createNew(
                "SPL-001", 1L, 2L, 3L, 4L,
                new BigDecimal("100.0000"),
                new BigDecimal("1.0000"),
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 12, 31),
                "Old note", true
            );

            spl.update(
                5L, 6L, 7L,
                new BigDecimal("250.0000"),
                new BigDecimal("20.0000"),
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2027, 1, 31),
                "New note",
                false
            );

            assertThat(spl.getCode()).isEqualTo("SPL-001");
            assertThat(spl.getSupplierId()).isEqualTo(1L);
            assertThat(spl.getProductId()).isEqualTo(5L);
            assertThat(spl.getUomId()).isEqualTo(6L);
            assertThat(spl.getCurrencyId()).isEqualTo(7L);
            assertThat(spl.getUnitPrice()).isEqualByComparingTo("250.0000");
            assertThat(spl.getMinQuantity()).isEqualByComparingTo("20.0000");
            assertThat(spl.getEffectiveFrom()).isEqualTo(LocalDate.of(2026, 8, 1));
            assertThat(spl.getEffectiveTo()).isEqualTo(LocalDate.of(2027, 1, 31));
            assertThat(spl.getNote()).isEqualTo("New note");
            assertThat(spl.isActive()).isFalse();
        }

        @Test
        @DisplayName("throws DomainException when update price is zero")
        void update_withZeroPrice_throwsDomainException() {
            SupplierPriceList spl = SupplierPriceList.createNew(
                "SPL-001", 1L, 2L, 3L, 4L,
                new BigDecimal("100.0000"),
                new BigDecimal("1.0000"),
                LocalDate.of(2026, 7, 1),
                null, null, true
            );

            assertThatThrownBy(() -> spl.update(
                2L, 3L, 4L,
                BigDecimal.ZERO,
                new BigDecimal("1.0000"),
                LocalDate.of(2026, 7, 1),
                null, null, true
            ))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.spl.price.positive");
        }

        @Test
        @DisplayName("throws DomainException when update date range is invalid")
        void update_withInvalidDateRange_throwsDomainException() {
            SupplierPriceList spl = SupplierPriceList.createNew(
                "SPL-001", 1L, 2L, 3L, 4L,
                new BigDecimal("100.0000"),
                new BigDecimal("1.0000"),
                LocalDate.of(2026, 7, 1),
                null, null, true
            );

            assertThatThrownBy(() -> spl.update(
                2L, 3L, 4L,
                new BigDecimal("100.0000"),
                new BigDecimal("1.0000"),
                LocalDate.of(2027, 1, 1),
                LocalDate.of(2026, 6, 1),
                null, true
            ))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.spl.date.range");
        }
    }

    @Nested
    @DisplayName("deactivate method")
    class Deactivate {

        @Test
        @DisplayName("sets isActive to false")
        void deactivate_setsIsActiveToFalse() {
            SupplierPriceList spl = SupplierPriceList.createNew(
                "SPL-001", 1L, 2L, 3L, 4L,
                new BigDecimal("100.0000"),
                new BigDecimal("1.0000"),
                LocalDate.of(2026, 7, 1),
                null, null, true
            );

            spl.deactivate();

            assertThat(spl.isActive()).isFalse();
        }
    }
}
