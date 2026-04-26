package com.solusi.erp.purchasing.purchaseorder.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.master.tax.domain.model.TaxCalculationMode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("PurchaseOrderLine Domain Model Tests")
class PurchaseOrderLineTest {

    @Nested
    @DisplayName("valid construction")
    class ValidConstruction {

        @Test
        @DisplayName("creates line with proper calculation")
        void validLine_calculatesCorrectly() {
            PurchaseOrderLine line = new PurchaseOrderLine(
                AuditMetadata.empty(), null,
                1L, new BigDecimal("10"), BigDecimal.ZERO, 1L,
                new BigDecimal("100"), new BigDecimal("0.11"),
                null, "Test note"
            );

            assertThat(line.getProductId()).isEqualTo(1L);
            assertThat(line.getQuantity()).isEqualByComparingTo("10");
            assertThat(line.getReceivedQuantity()).isEqualByComparingTo("0");
            assertThat(line.getUomId()).isEqualTo(1L);
            assertThat(line.getUnitPrice()).isEqualByComparingTo("100");
            assertThat(line.getTaxRate()).isEqualByComparingTo("0.11");
            assertThat(line.getLineSubtotal()).isEqualByComparingTo("1000");
            assertThat(line.getLineTax()).isEqualByComparingTo("110");
            assertThat(line.getLineTotal()).isEqualByComparingTo("1110");
            assertThat(line.getPrLineId()).isNull();
            assertThat(line.getNote()).isEqualTo("Test note");
        }

        @Test
        @DisplayName("PPN calculation: qty=10, price=100, taxRate=0.11")
        void ppnCalculation_correctValues() {
            PurchaseOrderLine line = new PurchaseOrderLine(
                AuditMetadata.empty(), null,
                1L, new BigDecimal("10"), BigDecimal.ZERO, 1L,
                new BigDecimal("100"), new BigDecimal("0.11"),
                null, null
            );

            assertThat(line.getLineSubtotal()).isEqualByComparingTo("1000");
            assertThat(line.getLineTax()).isEqualByComparingTo("110");
            assertThat(line.getLineTotal()).isEqualByComparingTo("1110");
        }

        @Test
        @DisplayName("zero tax rate: lineSubtotal=1000, lineTax=0, lineTotal=1000")
        void zeroTaxRate_noTax() {
            PurchaseOrderLine line = new PurchaseOrderLine(
                AuditMetadata.empty(), null,
                1L, new BigDecimal("10"), BigDecimal.ZERO, 1L,
                new BigDecimal("100"), BigDecimal.ZERO,
                null, null
            );

            assertThat(line.getLineSubtotal()).isEqualByComparingTo("1000");
            assertThat(line.getLineTax()).isEqualByComparingTo("0");
            assertThat(line.getLineTotal()).isEqualByComparingTo("1000");
        }

        @Test
        @DisplayName("null taxRate defaults to zero")
        void nullTaxRate_defaultsToZero() {
            PurchaseOrderLine line = new PurchaseOrderLine(
                AuditMetadata.empty(), null,
                1L, new BigDecimal("5"), BigDecimal.ZERO, 1L,
                new BigDecimal("200"), null,
                null, null
            );

            assertThat(line.getTaxRate()).isEqualByComparingTo("0");
            assertThat(line.getLineSubtotal()).isEqualByComparingTo("1000");
            assertThat(line.getLineTax()).isEqualByComparingTo("0");
            assertThat(line.getLineTotal()).isEqualByComparingTo("1000");
        }

        @Test
        @DisplayName("null receivedQuantity defaults to zero")
        void nullReceivedQuantity_defaultsToZero() {
            PurchaseOrderLine line = new PurchaseOrderLine(
                AuditMetadata.empty(), null,
                1L, new BigDecimal("5"), null, 1L,
                new BigDecimal("200"), BigDecimal.ZERO,
                null, null
            );

            assertThat(line.getReceivedQuantity()).isEqualByComparingTo("0");
        }

        @Test
        @DisplayName("preserves headerId and prLineId")
        void preserves_headerIdAndPrLineId() {
            PurchaseOrderLine line = new PurchaseOrderLine(
                AuditMetadata.empty(), 10L,
                1L, new BigDecimal("1"), BigDecimal.ZERO, 1L,
                new BigDecimal("50"), BigDecimal.ZERO,
                99L, null
            );

            assertThat(line.getHeaderId()).isEqualTo(10L);
            assertThat(line.getPrLineId()).isEqualTo(99L);
        }

        @Test
        @DisplayName("setHeaderId updates headerId")
        void setHeaderId_updatesValue() {
            PurchaseOrderLine line = new PurchaseOrderLine(
                AuditMetadata.empty(), null,
                1L, new BigDecimal("1"), BigDecimal.ZERO, 1L,
                new BigDecimal("50"), BigDecimal.ZERO,
                null, null
            );

            line.setHeaderId(42L);
            assertThat(line.getHeaderId()).isEqualTo(42L);
        }

        @Test
        @DisplayName("recalculate with inclusive tax extracts base from gross")
        void recalculate_inclusive_extractsBaseFromGross() {
            PurchaseOrderLine line = new PurchaseOrderLine(
                    AuditMetadata.empty(), null,
                    1L, new BigDecimal("1"), BigDecimal.ZERO, 1L,
                    new BigDecimal("10000.00"), BigDecimal.ZERO,
                    null, null
            );

            PurchaseOrderLine recalculated = line.recalculate(new BigDecimal("0.11"), TaxCalculationMode.INCLUSIVE);

            assertThat(recalculated.getTaxRate()).isEqualByComparingTo("0.11");
            assertThat(recalculated.getLineSubtotal()).isEqualByComparingTo("9009.0090");
            assertThat(recalculated.getLineTax()).isEqualByComparingTo("990.9910");
            assertThat(recalculated.getLineTotal()).isEqualByComparingTo("10000.0000");
        }

        @Test
        @DisplayName("recalculate with exclusive tax adds tax on top of base")
        void recalculate_exclusive_addsTaxOnTopOfBase() {
            PurchaseOrderLine line = new PurchaseOrderLine(
                    AuditMetadata.empty(), null,
                    1L, new BigDecimal("2"), BigDecimal.ZERO, 1L,
                    new BigDecimal("100.00"), BigDecimal.ZERO,
                    null, null
            );

            PurchaseOrderLine recalculated = line.recalculate(new BigDecimal("0.11"), TaxCalculationMode.EXCLUSIVE);

            assertThat(recalculated.getTaxRate()).isEqualByComparingTo("0.11");
            assertThat(recalculated.getLineSubtotal()).isEqualByComparingTo("200.0000");
            assertThat(recalculated.getLineTax()).isEqualByComparingTo("22.0000");
            assertThat(recalculated.getLineTotal()).isEqualByComparingTo("222.0000");
        }
    }

    @Nested
    @DisplayName("quantity validation")
    class QuantityValidation {

        @Test
        @DisplayName("quantity <= 0 throws DomainException")
        void zeroQuantity_throwsDomainException() {
            assertThatThrownBy(() -> new PurchaseOrderLine(
                AuditMetadata.empty(), null,
                1L, BigDecimal.ZERO, BigDecimal.ZERO, 1L,
                new BigDecimal("100"), BigDecimal.ZERO,
                null, null
            ))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.po.line.quantity.positive");
        }

        @Test
        @DisplayName("negative quantity throws DomainException")
        void negativeQuantity_throwsDomainException() {
            assertThatThrownBy(() -> new PurchaseOrderLine(
                AuditMetadata.empty(), null,
                1L, new BigDecimal("-1"), BigDecimal.ZERO, 1L,
                new BigDecimal("100"), BigDecimal.ZERO,
                null, null
            ))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.po.line.quantity.positive");
        }

        @Test
        @DisplayName("null quantity throws DomainException")
        void nullQuantity_throwsDomainException() {
            assertThatThrownBy(() -> new PurchaseOrderLine(
                AuditMetadata.empty(), null,
                1L, null, BigDecimal.ZERO, 1L,
                new BigDecimal("100"), BigDecimal.ZERO,
                null, null
            ))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.po.line.quantity.positive");
        }
    }

    @Nested
    @DisplayName("unitPrice validation")
    class UnitPriceValidation {

        @Test
        @DisplayName("unitPrice <= 0 throws DomainException")
        void zeroUnitPrice_throwsDomainException() {
            assertThatThrownBy(() -> new PurchaseOrderLine(
                AuditMetadata.empty(), null,
                1L, new BigDecimal("10"), BigDecimal.ZERO, 1L,
                BigDecimal.ZERO, BigDecimal.ZERO,
                null, null
            ))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.po.line.unitprice.positive");
        }

        @Test
        @DisplayName("negative unitPrice throws DomainException")
        void negativeUnitPrice_throwsDomainException() {
            assertThatThrownBy(() -> new PurchaseOrderLine(
                AuditMetadata.empty(), null,
                1L, new BigDecimal("10"), BigDecimal.ZERO, 1L,
                new BigDecimal("-50"), BigDecimal.ZERO,
                null, null
            ))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.po.line.unitprice.positive");
        }

        @Test
        @DisplayName("null unitPrice throws DomainException")
        void nullUnitPrice_throwsDomainException() {
            assertThatThrownBy(() -> new PurchaseOrderLine(
                AuditMetadata.empty(), null,
                1L, new BigDecimal("10"), BigDecimal.ZERO, 1L,
                null, BigDecimal.ZERO,
                null, null
            ))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.po.line.unitprice.positive");
        }
    }

    @Nested
    @DisplayName("edge cases")
    class EdgeCases {

        @Test
        @DisplayName("very small quantity (0.0001) still works")
        void verySmallQuantity_works() {
            PurchaseOrderLine line = new PurchaseOrderLine(
                AuditMetadata.empty(), null,
                1L, new BigDecimal("0.0001"), BigDecimal.ZERO, 1L,
                new BigDecimal("100"), BigDecimal.ZERO,
                null, null
            );

            assertThat(line.getQuantity()).isEqualByComparingTo("0.0001");
            assertThat(line.getLineSubtotal()).isEqualByComparingTo("0.01");
            assertThat(line.getLineTotal()).isEqualByComparingTo("0.01");
        }

        @Test
        @DisplayName("very large amounts don't overflow")
        void veryLargeAmounts_noOverflow() {
            PurchaseOrderLine line = new PurchaseOrderLine(
                AuditMetadata.empty(), null,
                1L, new BigDecimal("999999999"), BigDecimal.ZERO, 1L,
                new BigDecimal("999999999"), new BigDecimal("0.11"),
                null, null
            );

            assertThat(line.getLineSubtotal()).isEqualByComparingTo("999999998000000001");
            assertThat(line.getLineTax()).isEqualByComparingTo(
                new BigDecimal("999999998000000001").multiply(new BigDecimal("0.11"))
            );
            assertThat(line.getLineTotal()).isEqualByComparingTo(
                line.getLineSubtotal().add(line.getLineTax())
            );
        }
    }

    @Nested
    @DisplayName("receive method")
    class Receive {

        private PurchaseOrderLine createReceivableLine(String orderedQty, String receivedQty) {
            return PurchaseOrderLine.rehydrate(
                    new AuditMetadata(1L, 1L, null, null, null, null),
                    1L, 1L, new BigDecimal(orderedQty), new BigDecimal(receivedQty), 1L,
                    new BigDecimal("100.00"), BigDecimal.ZERO,
                    new BigDecimal(orderedQty).multiply(new BigDecimal("100.00")),
                    BigDecimal.ZERO,
                    new BigDecimal(orderedQty).multiply(new BigDecimal("100.00")),
                    null, null
            );
        }

        @Test
        @DisplayName("rejects zero receipt quantity")
        void receive_zeroQuantity_throwsDomainException() {
            PurchaseOrderLine line = createReceivableLine("10.0000", "0.0000");

            assertThatThrownBy(() -> line.receive(BigDecimal.ZERO))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("msg.error.gr.line.quantity.positive");

            assertThat(line.getReceivedQuantity()).isEqualByComparingTo("0.0000");
        }

        @Test
        @DisplayName("rejects negative receipt quantity")
        void receive_negativeQuantity_throwsDomainException() {
            PurchaseOrderLine line = createReceivableLine("10.0000", "0.0000");

            assertThatThrownBy(() -> line.receive(new BigDecimal("-1.0000")))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("msg.error.gr.line.quantity.positive");

            assertThat(line.getReceivedQuantity()).isEqualByComparingTo("0.0000");
        }

        @Test
        @DisplayName("rejects receipt quantity that exceeds outstanding amount")
        void receive_overReceipt_throwsDomainException() {
            PurchaseOrderLine line = createReceivableLine("10.0000", "8.0000");

            assertThatThrownBy(() -> line.receive(new BigDecimal("2.5000")))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("msg.error.gr.line.exceeds.outstanding");

            assertThat(line.getReceivedQuantity()).isEqualByComparingTo("8.0000");
            assertThat(line.getOutstandingQuantity()).isEqualByComparingTo("2.0000");
        }

        @Test
        @DisplayName("accumulates receipts without exceeding ordered quantity")
        void receive_cumulativeReceipts_updatesOutstandingQuantity() {
            PurchaseOrderLine line = createReceivableLine("10.0000", "2.0000");

            line.receive(new BigDecimal("3.0000"));
            line.receive(new BigDecimal("5.0000"));

            assertThat(line.getReceivedQuantity()).isEqualByComparingTo("10.0000");
            assertThat(line.getOutstandingQuantity()).isEqualByComparingTo("0.0000");
        }
    }
}
