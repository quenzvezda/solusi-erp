package com.solusi.erp.purchasing.purchasereturn.domain.model;

import com.solusi.erp.core.exception.DomainException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturnLineTest.line;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PurchaseReturnTest {

    @Test
    void createNew_validGoodsReceiptSource_createsDraft() {
        PurchaseReturn purchaseReturn = draft();

        assertThat(purchaseReturn.getStatus()).isEqualTo(PurchaseReturnStatus.DRAFT);
        assertThat(purchaseReturn.getReferenceType()).isEqualTo("GOODS_RECEIPT");
        assertThat(purchaseReturn.getLines()).hasSize(1);
    }

    @Test
    void createNew_nonGoodsReceiptSource_rejects() {
        assertThatThrownBy(() -> draft("PURCHASE_ORDER", PurchaseReturnReason.DAMAGED, null))
                .isInstanceOf(DomainException.class)
                .hasMessage("msg.error.purchase-return.source.goods-receipt-required");
    }

    @Test
    void createNew_headerOtherWithoutNote_rejects() {
        assertThatThrownBy(() -> draft("GOODS_RECEIPT", PurchaseReturnReason.OTHER, null))
                .isInstanceOf(DomainException.class)
                .hasMessage("msg.error.purchase-return.other-note-required");
    }

    @Test
    void createNew_withoutHeaderReason_rejects() {
        assertThatThrownBy(() -> draft("GOODS_RECEIPT", null, null))
                .isInstanceOf(DomainException.class)
                .hasMessage("msg.error.purchase-return.reason-required");
    }

    @Test
    void createNew_withoutLines_rejects() {
        assertThatThrownBy(() -> PurchaseReturn.createNew(
                "PRT-001", LocalDate.now(), "GOODS_RECEIPT", 1L, "GR-001", 2L, "PO-001",
                3L, 4L, 5L, BigDecimal.ONE, PurchaseReturnReason.DAMAGED, null, List.of()))
                .isInstanceOf(DomainException.class)
                .hasMessage("msg.error.purchase-return.lines-required");
    }

    @Test
    void updateDraft_afterSubmit_rejects() {
        PurchaseReturn purchaseReturn = draft();
        purchaseReturn.submit(99L);

        assertThatThrownBy(() -> purchaseReturn.updateDraft(
                LocalDate.now(), PurchaseReturnReason.DAMAGED, null, List.of(defaultLine())))
                .isInstanceOf(DomainException.class)
                .hasMessage("msg.error.purchase-return.update.not-draft");
    }

    @Test
    void submit_draft_recordsSubmitter() {
        PurchaseReturn purchaseReturn = draft();

        purchaseReturn.submit(99L);

        assertThat(purchaseReturn.getStatus()).isEqualTo(PurchaseReturnStatus.SUBMITTED);
        assertThat(purchaseReturn.getSubmittedByUserId()).isEqualTo(99L);
    }

    @Test
    void submit_withoutSubmitter_rejects() {
        assertThatThrownBy(() -> draft().submit(null))
                .isInstanceOf(DomainException.class)
                .hasMessage("msg.error.purchase-return.submit.user-required");
    }

    @Test
    void approve_submitted_transitionsApproved() {
        PurchaseReturn purchaseReturn = submitted();

        purchaseReturn.approve();

        assertThat(purchaseReturn.getStatus()).isEqualTo(PurchaseReturnStatus.APPROVED);
    }

    @Test
    void approve_draft_rejects() {
        assertThatThrownBy(() -> draft().approve())
                .isInstanceOf(DomainException.class)
                .hasMessage("msg.error.purchase-return.approve.invalid-status");
    }

    @Test
    void reject_submitted_transitionsRejected() {
        PurchaseReturn purchaseReturn = submitted();

        purchaseReturn.reject();

        assertThat(purchaseReturn.getStatus()).isEqualTo(PurchaseReturnStatus.REJECTED);
    }

    @Test
    void reject_draft_rejects() {
        assertThatThrownBy(() -> draft().reject())
                .isInstanceOf(DomainException.class)
                .hasMessage("msg.error.purchase-return.reject.invalid-status");
    }

    @Test
    void cancelDraft_draft_transitionsCancelled() {
        PurchaseReturn purchaseReturn = draft();

        purchaseReturn.cancelDraft();

        assertThat(purchaseReturn.getStatus()).isEqualTo(PurchaseReturnStatus.CANCELLED);
    }

    @Test
    void cancelDraft_submitted_rejects() {
        assertThatThrownBy(() -> submitted().cancelDraft())
                .isInstanceOf(DomainException.class)
                .hasMessage("msg.error.purchase-return.cancel-draft.invalid-status");
    }

    @Test
    void cancelSubmission_creator_transitionsCancelled() {
        PurchaseReturn purchaseReturn = submitted();

        purchaseReturn.cancelSubmission(99L);

        assertThat(purchaseReturn.getStatus()).isEqualTo(PurchaseReturnStatus.CANCELLED);
    }

    @Test
    void cancelSubmission_differentUser_rejects() {
        PurchaseReturn purchaseReturn = submitted();

        assertThatThrownBy(() -> purchaseReturn.cancelSubmission(100L))
                .isInstanceOf(DomainException.class)
                .hasMessage("msg.error.purchase-return.cancel-submission.creator-only");
    }

    @Test
    void cancelSubmission_draft_rejects() {
        assertThatThrownBy(() -> draft().cancelSubmission(99L))
                .isInstanceOf(DomainException.class)
                .hasMessage("msg.error.purchase-return.cancel-submission.invalid-status");
    }

    @Test
    void cancelApproved_approved_transitionsCancelled() {
        PurchaseReturn purchaseReturn = approved();

        purchaseReturn.cancelApproved();

        assertThat(purchaseReturn.getStatus()).isEqualTo(PurchaseReturnStatus.CANCELLED);
    }

    @Test
    void cancelApproved_submitted_rejects() {
        assertThatThrownBy(() -> submitted().cancelApproved())
                .isInstanceOf(DomainException.class)
                .hasMessage("msg.error.purchase-return.cancel-approved.invalid-status");
    }

    @Test
    void confirm_approved_recordsGeneratedGoodsIssue() {
        PurchaseReturn purchaseReturn = approved();

        purchaseReturn.confirm(123L);

        assertThat(purchaseReturn.getStatus()).isEqualTo(PurchaseReturnStatus.CONFIRMED);
        assertThat(purchaseReturn.getGeneratedGoodsIssueId()).isEqualTo(123L);
    }

    @Test
    void confirm_submitted_rejects() {
        assertThatThrownBy(() -> submitted().confirm(123L))
                .isInstanceOf(DomainException.class)
                .hasMessage("msg.error.purchase-return.confirm.invalid-status");
    }

    @Test
    void confirm_withoutGeneratedGoodsIssue_rejects() {
        assertThatThrownBy(() -> approved().confirm(null))
                .isInstanceOf(DomainException.class)
                .hasMessage("msg.error.purchase-return.confirm.gi-required");
    }

    @Test
    void cancelApproved_confirmed_rejects() {
        PurchaseReturn purchaseReturn = approved();
        purchaseReturn.confirm(123L);

        assertThatThrownBy(purchaseReturn::cancelApproved)
                .isInstanceOf(DomainException.class)
                .hasMessage("msg.error.purchase-return.cancel-approved.invalid-status");
    }

    private PurchaseReturn approved() {
        PurchaseReturn purchaseReturn = submitted();
        purchaseReturn.approve();
        return purchaseReturn;
    }

    private PurchaseReturn submitted() {
        PurchaseReturn purchaseReturn = draft();
        purchaseReturn.submit(99L);
        return purchaseReturn;
    }

    private PurchaseReturn draft() {
        return draft("GOODS_RECEIPT", PurchaseReturnReason.DAMAGED, null);
    }

    private PurchaseReturn draft(String referenceType, PurchaseReturnReason reason, String note) {
        return PurchaseReturn.createNew(
                "PRT-001", LocalDate.now(), referenceType, 1L, "GR-001", 2L, "PO-001",
                3L, 4L, 5L, BigDecimal.ONE, reason, note, List.of(defaultLine())
        );
    }

    private PurchaseReturnLine defaultLine() {
        return line(false, BigDecimal.ONE, BigDecimal.ONE, null, PurchaseReturnReason.DAMAGED, null);
    }
}
