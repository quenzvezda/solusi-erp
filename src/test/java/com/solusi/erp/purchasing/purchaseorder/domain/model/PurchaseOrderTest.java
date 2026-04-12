package com.solusi.erp.purchasing.purchaseorder.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("PurchaseOrder Domain Model Tests")
class PurchaseOrderTest {

    private PurchaseOrderLine createLine(BigDecimal quantity, BigDecimal unitPrice, BigDecimal taxRate) {
        return new PurchaseOrderLine(
            AuditMetadata.empty(), null,
            1L, quantity, BigDecimal.ZERO, 1L,
            unitPrice, taxRate,
            null, null
        );
    }

    private PurchaseOrderLine createDefaultLine() {
        return createLine(new BigDecimal("10"), new BigDecimal("100"), new BigDecimal("0.11"));
    }

    private PurchaseOrder createDraftPO(List<PurchaseOrderLine> lines) {
        return PurchaseOrder.createNew(
            "PO-202607-00001",
            LocalDate.of(2026, 7, 14),
            LocalDate.of(2026, 8, 14),
            1L, 2L, 1L,
            BigDecimal.ONE,
            30, null, PurchaseOrderType.DIRECT, "Test PO",
            lines
        );
    }

    private PurchaseOrder createPOWithStatus(PurchaseOrderStatus status, List<PurchaseOrderLine> lines) {
        return new PurchaseOrder(
            new AuditMetadata(1L, 1L, null, null, null, null),
            "PO-202607-00001",
            LocalDate.of(2026, 7, 14),
            LocalDate.of(2026, 8, 14),
            1L, 2L, 1L,
            BigDecimal.ONE,
            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
            status, 30, null, PurchaseOrderType.DIRECT, "Test PO", true,
            lines
        );
    }

    @Nested
    @DisplayName("createNew factory method")
    class CreateNew {

        @Test
        @DisplayName("creates draft PO with valid data")
        void createNew_withValidParams_createsDraftPO() {
            List<PurchaseOrderLine> lines = new ArrayList<>(List.of(createDefaultLine()));
            PurchaseOrder po = PurchaseOrder.createNew(
                "PO-202607-00001",
                LocalDate.of(2026, 7, 14),
                LocalDate.of(2026, 8, 14),
                1L, 2L, 1L,
                new BigDecimal("1.0"),
                30, 5L, PurchaseOrderType.DIRECT, "Test PO",
                lines
            );

            assertThat(po.getId()).isNull();
            assertThat(po.getCode()).isEqualTo("PO-202607-00001");
            assertThat(po.getOrderDate()).isEqualTo(LocalDate.of(2026, 7, 14));
            assertThat(po.getExpectedDate()).isEqualTo(LocalDate.of(2026, 8, 14));
            assertThat(po.getSupplierId()).isEqualTo(1L);
            assertThat(po.getFacilityId()).isEqualTo(2L);
            assertThat(po.getCurrencyId()).isEqualTo(1L);
            assertThat(po.getExchangeRate()).isEqualByComparingTo("1.0");
            assertThat(po.getStatus()).isEqualTo(PurchaseOrderStatus.DRAFT);
            assertThat(po.getPaymentTermDays()).isEqualTo(30);
            assertThat(po.getPrId()).isEqualTo(5L);
            assertThat(po.getNote()).isEqualTo("Test PO");
            assertThat(po.isActive()).isTrue();
            assertThat(po.getLines()).hasSize(1);
        }

        @Test
        @DisplayName("recalculates totals from lines")
        void createNew_recalculatesTotals() {
            PurchaseOrderLine line = createDefaultLine(); // qty=10, price=100, tax=0.11
            PurchaseOrder po = createDraftPO(new ArrayList<>(List.of(line)));

            assertThat(po.getSubtotal()).isEqualByComparingTo("1000");
            assertThat(po.getTaxAmount()).isEqualByComparingTo("110");
            assertThat(po.getTotalAmount()).isEqualByComparingTo("1110");
        }

        @Test
        @DisplayName("creates PO with null optional fields")
        void createNew_withNullOptionals_succeeds() {
            PurchaseOrder po = PurchaseOrder.createNew(
                "PO-001",
                LocalDate.of(2026, 7, 14),
                null, // expectedDate nullable
                1L, null, 1L, // facilityId nullable
                BigDecimal.ONE,
                30, null, PurchaseOrderType.DIRECT, null, // prId, poType, note
                new ArrayList<>()
            );

            assertThat(po.getExpectedDate()).isNull();
            assertThat(po.getFacilityId()).isNull();
            assertThat(po.getPrId()).isNull();
            assertThat(po.getNote()).isNull();
        }

        @Test
        @DisplayName("validates exchangeRate > 0")
        void createNew_zeroExchangeRate_throwsDomainException() {
            assertThatThrownBy(() -> PurchaseOrder.createNew(
                "PO-001",
                LocalDate.of(2026, 7, 14),
                null, 1L, null, 1L,
                BigDecimal.ZERO,
                30, null, PurchaseOrderType.DIRECT, null,
                new ArrayList<>()
            ))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.po.exchangerate.positive");
        }

        @Test
        @DisplayName("validates exchangeRate not negative")
        void createNew_negativeExchangeRate_throwsDomainException() {
            assertThatThrownBy(() -> PurchaseOrder.createNew(
                "PO-001",
                LocalDate.of(2026, 7, 14),
                null, 1L, null, 1L,
                new BigDecimal("-1"),
                30, null, PurchaseOrderType.DIRECT, null,
                new ArrayList<>()
            ))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.po.exchangerate.positive");
        }

        @Test
        @DisplayName("validates expectedDate >= orderDate")
        void createNew_expectedDateBeforeOrderDate_throwsDomainException() {
            assertThatThrownBy(() -> PurchaseOrder.createNew(
                "PO-001",
                LocalDate.of(2026, 7, 14),
                LocalDate.of(2026, 7, 13), // before orderDate
                1L, null, 1L,
                BigDecimal.ONE,
                30, null, PurchaseOrderType.DIRECT, null,
                new ArrayList<>()
            ))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.po.expecteddate.before.orderdate");
        }

        @Test
        @DisplayName("expectedDate equal to orderDate is valid")
        void createNew_expectedDateEqualsOrderDate_succeeds() {
            PurchaseOrder po = PurchaseOrder.createNew(
                "PO-001",
                LocalDate.of(2026, 7, 14),
                LocalDate.of(2026, 7, 14),
                1L, null, 1L,
                BigDecimal.ONE,
                30, null, PurchaseOrderType.DIRECT, null,
                new ArrayList<>()
            );

            assertThat(po.getExpectedDate()).isEqualTo(LocalDate.of(2026, 7, 14));
        }
    }

    @Nested
    @DisplayName("update method")
    class Update {

        @Test
        @DisplayName("updates mutable fields when status is DRAFT")
        void update_whenDraft_updatesMutableFields() {
            PurchaseOrder po = createDraftPO(new ArrayList<>(List.of(createDefaultLine())));

            PurchaseOrderLine newLine = createLine(new BigDecimal("5"), new BigDecimal("200"), BigDecimal.ZERO);
            po.update(
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 9, 1),
                3L, 2L,
                new BigDecimal("1.5"),
                60, "Updated note",
                List.of(newLine)
            );

            assertThat(po.getOrderDate()).isEqualTo(LocalDate.of(2026, 8, 1));
            assertThat(po.getExpectedDate()).isEqualTo(LocalDate.of(2026, 9, 1));
            assertThat(po.getFacilityId()).isEqualTo(3L);
            assertThat(po.getCurrencyId()).isEqualTo(2L);
            assertThat(po.getExchangeRate()).isEqualByComparingTo("1.5");
            assertThat(po.getPaymentTermDays()).isEqualTo(60);
            assertThat(po.getNote()).isEqualTo("Updated note");
            assertThat(po.getLines()).hasSize(1);
        }

        @Test
        @DisplayName("recalculates totals after update")
        void update_recalculatesTotals() {
            PurchaseOrder po = createDraftPO(new ArrayList<>(List.of(createDefaultLine())));

            PurchaseOrderLine newLine = createLine(new BigDecimal("5"), new BigDecimal("200"), new BigDecimal("0.11"));
            po.update(
                LocalDate.of(2026, 7, 14),
                LocalDate.of(2026, 8, 14),
                2L, 1L,
                BigDecimal.ONE,
                30, null,
                List.of(newLine)
            );

            // 5 * 200 = 1000, tax = 110, total = 1110
            assertThat(po.getSubtotal()).isEqualByComparingTo("1000");
            assertThat(po.getTaxAmount()).isEqualByComparingTo("110");
            assertThat(po.getTotalAmount()).isEqualByComparingTo("1110");
        }

        @Test
        @DisplayName("throws DomainException when status is not DRAFT")
        void update_whenNotDraft_throwsDomainException() {
            PurchaseOrder po = createPOWithStatus(PurchaseOrderStatus.SUBMITTED, new ArrayList<>(List.of(createDefaultLine())));

            assertThatThrownBy(() -> po.update(
                LocalDate.of(2026, 8, 1), null, null, 1L,
                BigDecimal.ONE, 30, null, List.of()
            ))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.po.update.not.draft");
        }

        @Test
        @DisplayName("clears old lines and recalculates")
        void update_clearsOldLinesAndRecalculates() {
            PurchaseOrderLine line1 = createLine(new BigDecimal("10"), new BigDecimal("100"), BigDecimal.ZERO);
            PurchaseOrderLine line2 = createLine(new BigDecimal("20"), new BigDecimal("50"), BigDecimal.ZERO);
            PurchaseOrder po = createDraftPO(new ArrayList<>(List.of(line1, line2)));

            // Initial: subtotal = 1000 + 1000 = 2000
            assertThat(po.getSubtotal()).isEqualByComparingTo("2000");

            PurchaseOrderLine newLine = createLine(new BigDecimal("3"), new BigDecimal("300"), BigDecimal.ZERO);
            po.update(
                LocalDate.of(2026, 7, 14), LocalDate.of(2026, 8, 14),
                2L, 1L, BigDecimal.ONE, 30, null,
                List.of(newLine)
            );

            // After: subtotal = 3 * 300 = 900
            assertThat(po.getSubtotal()).isEqualByComparingTo("900");
            assertThat(po.getLines()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("submit method")
    class Submit {

        @Test
        @DisplayName("DRAFT → SUBMITTED with lines (happy path)")
        void submit_fromDraft_changesStatusToSubmitted() {
            PurchaseOrder po = createDraftPO(new ArrayList<>(List.of(createDefaultLine())));

            po.submit();

            assertThat(po.getStatus()).isEqualTo(PurchaseOrderStatus.SUBMITTED);
        }

        @Test
        @DisplayName("throws when no lines")
        void submit_withNoLines_throwsDomainException() {
            PurchaseOrder po = createDraftPO(new ArrayList<>());

            assertThatThrownBy(() -> po.submit())
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.po.submit.no.lines");
        }

        @Test
        @DisplayName("throws from non-DRAFT status")
        void submit_fromNonDraft_throwsDomainException() {
            PurchaseOrder po = createPOWithStatus(PurchaseOrderStatus.APPROVED, new ArrayList<>(List.of(createDefaultLine())));

            assertThatThrownBy(() -> po.submit())
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.po.submit.invalid.status");
        }
    }

    @Nested
    @DisplayName("approve method")
    class Approve {

        @Test
        @DisplayName("sets status to APPROVED")
        void approve_setsStatusToApproved() {
            PurchaseOrder po = createPOWithStatus(PurchaseOrderStatus.SUBMITTED, new ArrayList<>(List.of(createDefaultLine())));

            po.approve();

            assertThat(po.getStatus()).isEqualTo(PurchaseOrderStatus.APPROVED);
        }
    }

    @Nested
    @DisplayName("reject method")
    class Reject {

        @Test
        @DisplayName("sets status to REJECTED")
        void reject_setsStatusToRejected() {
            PurchaseOrder po = createPOWithStatus(PurchaseOrderStatus.SUBMITTED, new ArrayList<>(List.of(createDefaultLine())));

            po.reject();

            assertThat(po.getStatus()).isEqualTo(PurchaseOrderStatus.REJECTED);
        }
    }

    @Nested
    @DisplayName("send method")
    class Send {

        @Test
        @DisplayName("APPROVED → SENT")
        void send_fromApproved_changesStatusToSent() {
            PurchaseOrder po = createPOWithStatus(PurchaseOrderStatus.APPROVED, new ArrayList<>(List.of(createDefaultLine())));

            po.send();

            assertThat(po.getStatus()).isEqualTo(PurchaseOrderStatus.SENT);
        }

        @Test
        @DisplayName("throws from non-APPROVED status")
        void send_fromNonApproved_throwsDomainException() {
            PurchaseOrder po = createPOWithStatus(PurchaseOrderStatus.DRAFT, new ArrayList<>(List.of(createDefaultLine())));

            assertThatThrownBy(() -> po.send())
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.po.send.invalid.status");
        }

        @Test
        @DisplayName("throws from SENT status")
        void send_fromSent_throwsDomainException() {
            PurchaseOrder po = createPOWithStatus(PurchaseOrderStatus.SENT, new ArrayList<>(List.of(createDefaultLine())));

            assertThatThrownBy(() -> po.send())
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.po.send.invalid.status");
        }
    }

    @Nested
    @DisplayName("cancel method")
    class Cancel {

        @Test
        @DisplayName("cancels from DRAFT")
        void cancel_fromDraft_changesStatusToCancelled() {
            PurchaseOrder po = createDraftPO(new ArrayList<>());

            po.cancel();

            assertThat(po.getStatus()).isEqualTo(PurchaseOrderStatus.CANCELLED);
        }

        @Test
        @DisplayName("cancels from SUBMITTED")
        void cancel_fromSubmitted_changesStatusToCancelled() {
            PurchaseOrder po = createPOWithStatus(PurchaseOrderStatus.SUBMITTED, new ArrayList<>());

            po.cancel();

            assertThat(po.getStatus()).isEqualTo(PurchaseOrderStatus.CANCELLED);
        }

        @Test
        @DisplayName("throws from APPROVED (too late)")
        void cancel_fromApproved_throwsDomainException() {
            PurchaseOrder po = createPOWithStatus(PurchaseOrderStatus.APPROVED, new ArrayList<>());

            assertThatThrownBy(() -> po.cancel())
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.po.cancel.invalid.status");
        }

        @Test
        @DisplayName("throws from SENT")
        void cancel_fromSent_throwsDomainException() {
            PurchaseOrder po = createPOWithStatus(PurchaseOrderStatus.SENT, new ArrayList<>());

            assertThatThrownBy(() -> po.cancel())
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.po.cancel.invalid.status");
        }

        @Test
        @DisplayName("throws when already CANCELLED")
        void cancel_fromCancelled_throwsDomainException() {
            PurchaseOrder po = createPOWithStatus(PurchaseOrderStatus.CANCELLED, new ArrayList<>());

            assertThatThrownBy(() -> po.cancel())
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.po.cancel.invalid.status");
        }
    }

    @Nested
    @DisplayName("deactivate method")
    class Deactivate {

        @Test
        @DisplayName("deactivates from DRAFT")
        void deactivate_fromDraft_setsActiveToFalse() {
            PurchaseOrder po = createDraftPO(new ArrayList<>());

            po.deactivate();

            assertThat(po.isActive()).isFalse();
        }

        @Test
        @DisplayName("throws from non-DRAFT status")
        void deactivate_whenNotDraft_throwsDomainException() {
            PurchaseOrder po = createPOWithStatus(PurchaseOrderStatus.SUBMITTED, new ArrayList<>());

            assertThatThrownBy(() -> po.deactivate())
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.po.delete.not.draft");
        }
    }

    @Nested
    @DisplayName("getLines returns unmodifiable list")
    class GetLines {

        @Test
        @DisplayName("getLines returns unmodifiable view")
        void getLines_returnsUnmodifiableList() {
            PurchaseOrder po = createDraftPO(new ArrayList<>(List.of(createDefaultLine())));

            assertThatThrownBy(() -> po.getLines().add(createDefaultLine()))
                .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("edge cases")
    class EdgeCases {

        @Test
        @DisplayName("PO with many lines recalculates correctly")
        void manyLines_recalculatesCorrectly() {
            List<PurchaseOrderLine> lines = new ArrayList<>();
            // 10 lines, each qty=5, price=100, tax=0.11
            for (int i = 0; i < 10; i++) {
                lines.add(createLine(new BigDecimal("5"), new BigDecimal("100"), new BigDecimal("0.11")));
            }
            PurchaseOrder po = createDraftPO(lines);

            // Each line: subtotal=500, tax=55, total=555
            // Total: subtotal=5000, tax=550, total=5550
            assertThat(po.getSubtotal()).isEqualByComparingTo("5000");
            assertThat(po.getTaxAmount()).isEqualByComparingTo("550");
            assertThat(po.getTotalAmount()).isEqualByComparingTo("5550");
        }

        @Test
        @DisplayName("PPN calculation across multiple lines with mixed tax rates")
        void mixedTaxRates_calculatesCorrectly() {
            PurchaseOrderLine taxedLine = createLine(new BigDecimal("10"), new BigDecimal("100"), new BigDecimal("0.11"));
            PurchaseOrderLine untaxedLine = createLine(new BigDecimal("5"), new BigDecimal("200"), BigDecimal.ZERO);

            PurchaseOrder po = createDraftPO(new ArrayList<>(List.of(taxedLine, untaxedLine)));

            // taxedLine: subtotal=1000, tax=110
            // untaxedLine: subtotal=1000, tax=0
            assertThat(po.getSubtotal()).isEqualByComparingTo("2000");
            assertThat(po.getTaxAmount()).isEqualByComparingTo("110");
            assertThat(po.getTotalAmount()).isEqualByComparingTo("2110");
        }

        @Test
        @DisplayName("PO with empty lines has zero totals")
        void emptyLines_zeroTotals() {
            PurchaseOrder po = createDraftPO(new ArrayList<>());

            assertThat(po.getSubtotal()).isEqualByComparingTo("0");
            assertThat(po.getTaxAmount()).isEqualByComparingTo("0");
            assertThat(po.getTotalAmount()).isEqualByComparingTo("0");
        }

        @Test
        @DisplayName("null lines treated as empty list")
        void nullLines_treatedAsEmptyList() {
            PurchaseOrder po = PurchaseOrder.createNew(
                "PO-001",
                LocalDate.of(2026, 7, 14),
                null, 1L, null, 1L,
                BigDecimal.ONE, 30, null, PurchaseOrderType.DIRECT, null,
                null
            );

            assertThat(po.getLines()).isEmpty();
            assertThat(po.getSubtotal()).isEqualByComparingTo("0");
        }
    }
}
