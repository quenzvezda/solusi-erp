package com.solusi.erp.purchasing.purchaseorder.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PurchaseOrderStatus Enum Tests")
class PurchaseOrderStatusTest {

    @Nested
    @DisplayName("canUpdate")
    class CanUpdate {

        @Test
        @DisplayName("DRAFT can update")
        void draft_canUpdate() {
            assertThat(PurchaseOrderStatus.DRAFT.canUpdate()).isTrue();
        }

        @Test
        @DisplayName("non-DRAFT statuses cannot update")
        void nonDraft_cannotUpdate() {
            assertThat(PurchaseOrderStatus.SUBMITTED.canUpdate()).isFalse();
            assertThat(PurchaseOrderStatus.APPROVED.canUpdate()).isFalse();
            assertThat(PurchaseOrderStatus.SENT.canUpdate()).isFalse();
            assertThat(PurchaseOrderStatus.PARTIALLY_RECEIVED.canUpdate()).isFalse();
            assertThat(PurchaseOrderStatus.FULLY_RECEIVED.canUpdate()).isFalse();
            assertThat(PurchaseOrderStatus.BILLED.canUpdate()).isFalse();
            assertThat(PurchaseOrderStatus.CLOSED.canUpdate()).isFalse();
            assertThat(PurchaseOrderStatus.CANCELLED.canUpdate()).isFalse();
            assertThat(PurchaseOrderStatus.REJECTED.canUpdate()).isFalse();
        }
    }

    @Nested
    @DisplayName("canSubmit")
    class CanSubmit {

        @Test
        @DisplayName("DRAFT can submit")
        void draft_canSubmit() {
            assertThat(PurchaseOrderStatus.DRAFT.canSubmit()).isTrue();
        }

        @Test
        @DisplayName("non-DRAFT statuses cannot submit")
        void nonDraft_cannotSubmit() {
            assertThat(PurchaseOrderStatus.SUBMITTED.canSubmit()).isFalse();
            assertThat(PurchaseOrderStatus.APPROVED.canSubmit()).isFalse();
            assertThat(PurchaseOrderStatus.SENT.canSubmit()).isFalse();
            assertThat(PurchaseOrderStatus.PARTIALLY_RECEIVED.canSubmit()).isFalse();
            assertThat(PurchaseOrderStatus.FULLY_RECEIVED.canSubmit()).isFalse();
            assertThat(PurchaseOrderStatus.BILLED.canSubmit()).isFalse();
            assertThat(PurchaseOrderStatus.CLOSED.canSubmit()).isFalse();
            assertThat(PurchaseOrderStatus.CANCELLED.canSubmit()).isFalse();
            assertThat(PurchaseOrderStatus.REJECTED.canSubmit()).isFalse();
        }
    }

    @Nested
    @DisplayName("canSend")
    class CanSend {

        @Test
        @DisplayName("APPROVED can send")
        void approved_canSend() {
            assertThat(PurchaseOrderStatus.APPROVED.canSend()).isTrue();
        }

        @Test
        @DisplayName("non-APPROVED statuses cannot send")
        void nonApproved_cannotSend() {
            assertThat(PurchaseOrderStatus.DRAFT.canSend()).isFalse();
            assertThat(PurchaseOrderStatus.SUBMITTED.canSend()).isFalse();
            assertThat(PurchaseOrderStatus.SENT.canSend()).isFalse();
            assertThat(PurchaseOrderStatus.PARTIALLY_RECEIVED.canSend()).isFalse();
            assertThat(PurchaseOrderStatus.FULLY_RECEIVED.canSend()).isFalse();
            assertThat(PurchaseOrderStatus.BILLED.canSend()).isFalse();
            assertThat(PurchaseOrderStatus.CLOSED.canSend()).isFalse();
            assertThat(PurchaseOrderStatus.CANCELLED.canSend()).isFalse();
            assertThat(PurchaseOrderStatus.REJECTED.canSend()).isFalse();
        }
    }

    @Nested
    @DisplayName("canCancel")
    class CanCancel {

        @Test
        @DisplayName("DRAFT can cancel")
        void draft_canCancel() {
            assertThat(PurchaseOrderStatus.DRAFT.canCancel()).isTrue();
        }

        @Test
        @DisplayName("SUBMITTED can cancel")
        void submitted_canCancel() {
            assertThat(PurchaseOrderStatus.SUBMITTED.canCancel()).isTrue();
        }

        @Test
        @DisplayName("APPROVED cannot cancel (too late)")
        void approved_cannotCancel() {
            assertThat(PurchaseOrderStatus.APPROVED.canCancel()).isFalse();
        }

        @Test
        @DisplayName("other statuses cannot cancel")
        void otherStatuses_cannotCancel() {
            assertThat(PurchaseOrderStatus.SENT.canCancel()).isFalse();
            assertThat(PurchaseOrderStatus.PARTIALLY_RECEIVED.canCancel()).isFalse();
            assertThat(PurchaseOrderStatus.FULLY_RECEIVED.canCancel()).isFalse();
            assertThat(PurchaseOrderStatus.BILLED.canCancel()).isFalse();
            assertThat(PurchaseOrderStatus.CLOSED.canCancel()).isFalse();
            assertThat(PurchaseOrderStatus.CANCELLED.canCancel()).isFalse();
            assertThat(PurchaseOrderStatus.REJECTED.canCancel()).isFalse();
        }
    }

    @Nested
    @DisplayName("canDelete")
    class CanDelete {

        @Test
        @DisplayName("DRAFT can delete")
        void draft_canDelete() {
            assertThat(PurchaseOrderStatus.DRAFT.canDelete()).isTrue();
        }

        @Test
        @DisplayName("non-DRAFT statuses cannot delete")
        void nonDraft_cannotDelete() {
            assertThat(PurchaseOrderStatus.SUBMITTED.canDelete()).isFalse();
            assertThat(PurchaseOrderStatus.APPROVED.canDelete()).isFalse();
            assertThat(PurchaseOrderStatus.SENT.canDelete()).isFalse();
            assertThat(PurchaseOrderStatus.PARTIALLY_RECEIVED.canDelete()).isFalse();
            assertThat(PurchaseOrderStatus.FULLY_RECEIVED.canDelete()).isFalse();
            assertThat(PurchaseOrderStatus.BILLED.canDelete()).isFalse();
            assertThat(PurchaseOrderStatus.CLOSED.canDelete()).isFalse();
            assertThat(PurchaseOrderStatus.CANCELLED.canDelete()).isFalse();
            assertThat(PurchaseOrderStatus.REJECTED.canDelete()).isFalse();
        }
    }

    @Nested
    @DisplayName("canReceive")
    class CanReceive {

        @Test
        @DisplayName("SENT can receive")
        void sent_canReceive() {
            assertThat(PurchaseOrderStatus.SENT.canReceive()).isTrue();
        }

        @Test
        @DisplayName("PARTIALLY_RECEIVED can receive")
        void partiallyReceived_canReceive() {
            assertThat(PurchaseOrderStatus.PARTIALLY_RECEIVED.canReceive()).isTrue();
        }

        @Test
        @DisplayName("other statuses cannot receive")
        void otherStatuses_cannotReceive() {
            assertThat(PurchaseOrderStatus.DRAFT.canReceive()).isFalse();
            assertThat(PurchaseOrderStatus.SUBMITTED.canReceive()).isFalse();
            assertThat(PurchaseOrderStatus.APPROVED.canReceive()).isFalse();
            assertThat(PurchaseOrderStatus.FULLY_RECEIVED.canReceive()).isFalse();
            assertThat(PurchaseOrderStatus.BILLED.canReceive()).isFalse();
            assertThat(PurchaseOrderStatus.CLOSED.canReceive()).isFalse();
            assertThat(PurchaseOrderStatus.CANCELLED.canReceive()).isFalse();
            assertThat(PurchaseOrderStatus.REJECTED.canReceive()).isFalse();
        }
    }

    @Nested
    @DisplayName("isEditable")
    class IsEditable {

        @Test
        @DisplayName("DRAFT is editable")
        void draft_isEditable() {
            assertThat(PurchaseOrderStatus.DRAFT.isEditable()).isTrue();
        }

        @Test
        @DisplayName("non-DRAFT statuses are not editable")
        void nonDraft_isNotEditable() {
            assertThat(PurchaseOrderStatus.SUBMITTED.isEditable()).isFalse();
            assertThat(PurchaseOrderStatus.APPROVED.isEditable()).isFalse();
            assertThat(PurchaseOrderStatus.SENT.isEditable()).isFalse();
            assertThat(PurchaseOrderStatus.PARTIALLY_RECEIVED.isEditable()).isFalse();
            assertThat(PurchaseOrderStatus.FULLY_RECEIVED.isEditable()).isFalse();
            assertThat(PurchaseOrderStatus.BILLED.isEditable()).isFalse();
            assertThat(PurchaseOrderStatus.CLOSED.isEditable()).isFalse();
            assertThat(PurchaseOrderStatus.CANCELLED.isEditable()).isFalse();
            assertThat(PurchaseOrderStatus.REJECTED.isEditable()).isFalse();
        }
    }
}
