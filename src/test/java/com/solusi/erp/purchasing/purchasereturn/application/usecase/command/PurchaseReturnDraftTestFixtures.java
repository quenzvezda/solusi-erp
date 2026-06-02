package com.solusi.erp.purchasing.purchasereturn.application.usecase.command;

import com.solusi.erp.purchasing.purchasereturn.application.usecase.query.EligibleGoodsReceiptRow;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.query.ReturnableGrLineSlice;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.query.ReturnableSerialRow;
import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturn;
import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturnLine;
import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturnReason;
import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturnStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

final class PurchaseReturnDraftTestFixtures {

    private PurchaseReturnDraftTestFixtures() {
    }

    static EligibleGoodsReceiptRow source() {
        return new EligibleGoodsReceiptRow(
                1L, "GR-001", 2L, "PO-001", 3L, "Supplier", LocalDate.of(2026, 6, 1),
                4L, "Main", 5L, "IDR", BigDecimal.ONE, 1, BigDecimal.TEN
        );
    }

    static ReturnableGrLineSlice slice() {
        return new ReturnableGrLineSlice(
                "11:40", 1L, 11L, 10L, "Product", "P-001", false, 20L, "Piece", "PCS",
                30L, "Main", 35L, "A", 40L, "BIN", BigDecimal.TEN,
                "GOODS_RECEIPT", 1L, 11L, new BigDecimal("100"), new BigDecimal("1000"),
                new BigDecimal("100"), new BigDecimal("1000")
        );
    }

    static ReturnableSerialRow serial() {
        return new ReturnableSerialRow(
                "12:41:SER-001", 1L, 12L, 10L, "Product", "P-001", 20L, "Piece", "PCS",
                30L, "Main", 35L, "A", 41L, "MOVED", "SER-001", "GOODS_RECEIPT", 1L, 12L,
                new BigDecimal("100"), new BigDecimal("100"), new BigDecimal("10"), new BigDecimal("100")
        );
    }

    static PurchaseReturnLineCommand command(BigDecimal quantity) {
        return new PurchaseReturnLineCommand(
                11L, false, quantity, quantity, 40L, null, PurchaseReturnReason.DAMAGED, null);
    }

    static PurchaseReturn draft() {
        return PurchaseReturn.createNew(
                "PRT-001", LocalDate.of(2026, 6, 1), "GOODS_RECEIPT", 1L, "GR-001",
                2L, "PO-001", 3L, 4L, 5L, BigDecimal.ONE, PurchaseReturnReason.DAMAGED,
                null, List.of(line())
        );
    }

    static PurchaseReturn withStatus(PurchaseReturnStatus status) {
        PurchaseReturn purchaseReturn = draft();
        if (status == PurchaseReturnStatus.SUBMITTED || status == PurchaseReturnStatus.APPROVED) {
            purchaseReturn.submit(99L);
        }
        if (status == PurchaseReturnStatus.APPROVED) {
            purchaseReturn.approve();
        }
        return purchaseReturn;
    }

    private static PurchaseReturnLine line() {
        return PurchaseReturnLine.create(
                11L, 10L, false, BigDecimal.ONE, 20L, BigDecimal.ONE, 30L, 35L, 40L,
                null, PurchaseReturnReason.DAMAGED, null, "GOODS_RECEIPT", 1L, 11L,
                new BigDecimal("100"), new BigDecimal("100"), BigDecimal.ZERO, BigDecimal.ZERO
        );
    }
}
