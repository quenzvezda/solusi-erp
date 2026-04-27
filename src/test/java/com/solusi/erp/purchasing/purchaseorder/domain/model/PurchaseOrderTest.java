package com.solusi.erp.purchasing.purchaseorder.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.master.tax.domain.model.TaxCalculationMode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    private PurchaseOrderLine createReceiptLine(long id, String orderedQty, String receivedQty) {
        return PurchaseOrderLine.rehydrate(
                new AuditMetadata(id, 1L, null, null, null, null),
                1L, 10L, new BigDecimal(orderedQty), new BigDecimal(receivedQty), 1L,
                new BigDecimal("100.00"), BigDecimal.ZERO,
                new BigDecimal(orderedQty).multiply(new BigDecimal("100.00")),
                BigDecimal.ZERO,
                new BigDecimal(orderedQty).multiply(new BigDecimal("100.00")),
                null, null
        );
    }

    private PurchaseOrder createDraftPO(List<PurchaseOrderLine> lines) {
        return PurchaseOrder.createNew(
            "PO-202607-00001",
            LocalDate.of(2026, 7, 14),
            LocalDate.of(2026, 8, 14),
            1L, 2L, 1L,
            BigDecimal.ONE,
            30, null, PurchaseOrderType.DIRECT,
            10L, "PPN-EX", "PPN 11% Exclusive", new BigDecimal("11.00"),
            TaxCalculationMode.EXCLUSIVE, "Test PO",
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
                30, 5L, PurchaseOrderType.DIRECT,
                10L, "NON-TAX", "Non Tax", BigDecimal.ZERO,
                TaxCalculationMode.EXCLUSIVE, "Test PO",
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
        @DisplayName("inclusive tax keeps line total gross and extracts DPP")
        void inclusiveTax_keepsGrossAndExtractsBase() {
            PurchaseOrderLine line = new PurchaseOrderLine(
                    AuditMetadata.empty(), null,
                    1L, new BigDecimal("1"), BigDecimal.ZERO, 1L,
                    new BigDecimal("10000.00"), BigDecimal.ZERO,
                    null, null
            );

            PurchaseOrder po = PurchaseOrder.createNew(
                    "PO-001", LocalDate.of(2026, 7, 14), null,
                    1L, null, 1L, BigDecimal.ONE, 30, null, PurchaseOrderType.DIRECT,
                    10L, "PPN-IN", "PPN 11% Inclusive", new BigDecimal("11.00"),
                    TaxCalculationMode.INCLUSIVE, null, List.of(line)
            );

            assertThat(po.getSubtotal()).isEqualByComparingTo("9009.0090");
            assertThat(po.getTaxAmount()).isEqualByComparingTo("990.9910");
            assertThat(po.getTotalAmount()).isEqualByComparingTo("10000.0000");
            assertThat(po.getTaxId()).isEqualTo(10L);
            assertThat(po.getTaxCalculationMode()).isEqualTo(TaxCalculationMode.INCLUSIVE);
        }

        @Test
        @DisplayName("exclusive tax adds tax on top of net base")
        void exclusiveTax_addsTaxOnTopOfNetBase() {
            PurchaseOrderLine line = new PurchaseOrderLine(
                    AuditMetadata.empty(), null,
                    1L, new BigDecimal("2"), BigDecimal.ZERO, 1L,
                    new BigDecimal("100.00"), BigDecimal.ZERO,
                    null, null
            );

            PurchaseOrder po = PurchaseOrder.createNew(
                    "PO-002", LocalDate.of(2026, 7, 14), null,
                    1L, null, 1L, BigDecimal.ONE, 30, null, PurchaseOrderType.DIRECT,
                    11L, "PPN-EX", "PPN 11% Exclusive", new BigDecimal("11.00"),
                    TaxCalculationMode.EXCLUSIVE, null, List.of(line)
            );

            assertThat(po.getSubtotal()).isEqualByComparingTo("200.0000");
            assertThat(po.getTaxAmount()).isEqualByComparingTo("22.0000");
            assertThat(po.getTotalAmount()).isEqualByComparingTo("222.0000");
            assertThat(po.getTaxCode()).isEqualTo("PPN-EX");
        }

        @Test
        @DisplayName("requires explicit tax selection")
        void createNew_withoutTaxSelection_throwsDomainException() {
            PurchaseOrderLine line = new PurchaseOrderLine(
                    AuditMetadata.empty(), null,
                    1L, new BigDecimal("2"), BigDecimal.ZERO, 1L,
                    new BigDecimal("100.00"), BigDecimal.ZERO,
                    null, null
            );

            assertThatThrownBy(() -> PurchaseOrder.createNew(
                    "PO-003", LocalDate.of(2026, 7, 14), null,
                    1L, null, 1L, BigDecimal.ONE, 30, null, PurchaseOrderType.DIRECT,
                    null, null, null, BigDecimal.ZERO,
                    TaxCalculationMode.EXCLUSIVE, null, List.of(line)
            ))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("msg.error.po.tax.required");
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
                30, null, PurchaseOrderType.DIRECT,
                10L, "NON-TAX", "Non Tax", BigDecimal.ZERO,
                TaxCalculationMode.EXCLUSIVE, null,
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
                30, null, PurchaseOrderType.DIRECT,
                10L, "NON-TAX", "Non Tax", BigDecimal.ZERO,
                TaxCalculationMode.EXCLUSIVE, null,
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
                30, null, PurchaseOrderType.DIRECT,
                10L, "NON-TAX", "Non Tax", BigDecimal.ZERO,
                TaxCalculationMode.EXCLUSIVE, null,
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
                30, null, PurchaseOrderType.DIRECT,
                10L, "NON-TAX", "Non Tax", BigDecimal.ZERO,
                TaxCalculationMode.EXCLUSIVE, null,
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
                30, null, PurchaseOrderType.DIRECT,
                10L, "NON-TAX", "Non Tax", BigDecimal.ZERO,
                TaxCalculationMode.EXCLUSIVE, null,
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

        @Test
        @DisplayName("rejects clearing tax selection on update")
        void update_withoutTaxSelection_throwsDomainException() {
            PurchaseOrderLine line = new PurchaseOrderLine(
                    AuditMetadata.empty(), null,
                    1L, new BigDecimal("2"), BigDecimal.ZERO, 1L,
                    new BigDecimal("100.00"), BigDecimal.ZERO,
                    null, null
            );
            PurchaseOrder po = PurchaseOrder.createNew(
                    "PO-004", LocalDate.of(2026, 7, 14), null,
                    1L, null, 1L, BigDecimal.ONE, 30, null, PurchaseOrderType.DIRECT,
                    10L, "NON-TAX", "Non Tax", BigDecimal.ZERO,
                    TaxCalculationMode.EXCLUSIVE, null, List.of(line)
            );

            assertThatThrownBy(() -> po.update(
                    LocalDate.of(2026, 7, 15), null,
                    null, 1L, BigDecimal.ONE, 30,
                    null, null, null, BigDecimal.ZERO,
                    TaxCalculationMode.EXCLUSIVE,
                    null, List.of(line)
            ))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("msg.error.po.tax.required");
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
    @DisplayName("recordReceipt method")
    class RecordReceipt {

        @Test
        @DisplayName("recordReceipt partial updates received quantity and status")
        void recordReceipt_partial_updatesReceivedQuantityAndStatus() {
            PurchaseOrderLine line = createReceiptLine(11L, "10.0000", "0.0000");
            PurchaseOrder po = createPOWithStatus(PurchaseOrderStatus.SENT, new ArrayList<>(List.of(line)));

            po.recordReceipt(Map.of(11L, new BigDecimal("4.0000")));

            assertThat(po.getLines().get(0).getReceivedQuantity()).isEqualByComparingTo("4.0000");
            assertThat(po.getStatus()).isEqualTo(PurchaseOrderStatus.PARTIALLY_RECEIVED);
        }

        @Test
        @DisplayName("recordReceipt full closes PO into fully received")
        void recordReceipt_full_marksPoFullyReceived() {
            PurchaseOrderLine line = createReceiptLine(12L, "10.0000", "0.0000");
            PurchaseOrder po = createPOWithStatus(PurchaseOrderStatus.SENT, new ArrayList<>(List.of(line)));

            po.recordReceipt(Map.of(12L, new BigDecimal("10.0000")));

            assertThat(po.getStatus()).isEqualTo(PurchaseOrderStatus.FULLY_RECEIVED);
        }

        @Test
        @DisplayName("recordReceipt rejects invalid status before mutating state")
        void recordReceipt_invalidStatus_throwsDomainException() {
            PurchaseOrderLine line = createReceiptLine(13L, "10.0000", "0.0000");
            PurchaseOrder po = createPOWithStatus(PurchaseOrderStatus.APPROVED, new ArrayList<>(List.of(line)));

            assertThatThrownBy(() -> po.recordReceipt(Map.of(13L, new BigDecimal("1.0000"))))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("msg.error.gr.po.invalid.status");

            assertThat(po.getStatus()).isEqualTo(PurchaseOrderStatus.APPROVED);
            assertThat(po.getLines().get(0).getReceivedQuantity()).isEqualByComparingTo("0.0000");
        }

        @Test
        @DisplayName("recordReceipt rejects empty receipt map")
        void recordReceipt_emptyReceiptMap_throwsDomainException() {
            PurchaseOrderLine line = createReceiptLine(14L, "10.0000", "0.0000");
            PurchaseOrder po = createPOWithStatus(PurchaseOrderStatus.SENT, new ArrayList<>(List.of(line)));

            assertThatThrownBy(() -> po.recordReceipt(Map.of()))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("msg.error.gr.po.receipt.lines.required");

            assertThat(po.getStatus()).isEqualTo(PurchaseOrderStatus.SENT);
            assertThat(po.getLines().get(0).getReceivedQuantity()).isEqualByComparingTo("0.0000");
        }

        @Test
        @DisplayName("recordReceipt rejects receipt map without matching PO lines")
        void recordReceipt_nonMatchingReceiptMap_throwsDomainException() {
            PurchaseOrderLine line = createReceiptLine(15L, "10.0000", "0.0000");
            PurchaseOrder po = createPOWithStatus(PurchaseOrderStatus.SENT, new ArrayList<>(List.of(line)));

            assertThatThrownBy(() -> po.recordReceipt(Map.of(999L, new BigDecimal("1.0000"))))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("msg.error.gr.po.receipt.lines.required");

            assertThat(po.getStatus()).isEqualTo(PurchaseOrderStatus.SENT);
            assertThat(po.getLines().get(0).getReceivedQuantity()).isEqualByComparingTo("0.0000");
        }

        @Test
        @DisplayName("recordReceipt recomputes status from cumulative multi-line receipts")
        void recordReceipt_cumulativeMultiLine_recomputesStatusFromOutstandingQuantities() {
            PurchaseOrderLine firstLine = createReceiptLine(16L, "10.0000", "0.0000");
            PurchaseOrderLine secondLine = createReceiptLine(17L, "5.0000", "0.0000");
            PurchaseOrder po = createPOWithStatus(
                    PurchaseOrderStatus.SENT,
                    new ArrayList<>(List.of(firstLine, secondLine))
            );

            po.recordReceipt(Map.of(16L, new BigDecimal("4.0000")));
            assertThat(po.getStatus()).isEqualTo(PurchaseOrderStatus.PARTIALLY_RECEIVED);
            assertThat(po.getLines().get(0).getOutstandingQuantity()).isEqualByComparingTo("6.0000");
            assertThat(po.getLines().get(1).getOutstandingQuantity()).isEqualByComparingTo("5.0000");

            po.recordReceipt(Map.of(
                    16L, new BigDecimal("6.0000"),
                    17L, new BigDecimal("5.0000")
            ));

            assertThat(po.getStatus()).isEqualTo(PurchaseOrderStatus.FULLY_RECEIVED);
            assertThat(po.getLines().get(0).getReceivedQuantity()).isEqualByComparingTo("10.0000");
            assertThat(po.getLines().get(1).getReceivedQuantity()).isEqualByComparingTo("5.0000");
        }

        @Test
        @DisplayName("recordReceipt does not partially mutate lines when one receipt is invalid")
        void recordReceipt_invalidBatch_keepsAllLinesAndStatusUnchanged() {
            PurchaseOrderLine firstLine = createReceiptLine(18L, "10.0000", "4.0000");
            PurchaseOrderLine secondLine = createReceiptLine(19L, "5.0000", "0.0000");
            PurchaseOrder po = createPOWithStatus(
                    PurchaseOrderStatus.PARTIALLY_RECEIVED,
                    new ArrayList<>(List.of(firstLine, secondLine))
            );

            assertThatThrownBy(() -> po.recordReceipt(Map.of(
                    18L, new BigDecimal("6.0000"),
                    19L, new BigDecimal("5.1000")
            )))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("msg.error.gr.line.exceeds.outstanding");

            assertThat(po.getStatus()).isEqualTo(PurchaseOrderStatus.PARTIALLY_RECEIVED);
            assertThat(po.getLines().get(0).getReceivedQuantity()).isEqualByComparingTo("4.0000");
            assertThat(po.getLines().get(1).getReceivedQuantity()).isEqualByComparingTo("0.0000");
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
        @DisplayName("header tax snapshot applies consistently across multiple lines")
        void mixedTaxRates_calculatesCorrectly() {
            PurchaseOrderLine taxedLine = createLine(new BigDecimal("10"), new BigDecimal("100"), new BigDecimal("0.11"));
            PurchaseOrderLine untaxedLine = createLine(new BigDecimal("5"), new BigDecimal("200"), BigDecimal.ZERO);

            PurchaseOrder po = createDraftPO(new ArrayList<>(List.of(taxedLine, untaxedLine)));

            // Header tax snapshot is 11% exclusive, so both lines follow header tax.
            assertThat(po.getSubtotal()).isEqualByComparingTo("2000");
            assertThat(po.getTaxAmount()).isEqualByComparingTo("220");
            assertThat(po.getTotalAmount()).isEqualByComparingTo("2220");
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
                BigDecimal.ONE, 30, null, PurchaseOrderType.DIRECT,
                10L, "NON-TAX", "Non Tax", BigDecimal.ZERO,
                TaxCalculationMode.EXCLUSIVE, null,
                null
            );

            assertThat(po.getLines()).isEmpty();
            assertThat(po.getSubtotal()).isEqualByComparingTo("0");
        }
    }
}
