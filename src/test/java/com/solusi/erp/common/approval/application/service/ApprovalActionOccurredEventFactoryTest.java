package com.solusi.erp.common.approval.application.service;

import com.solusi.erp.common.approval.domain.model.ApprovalAction;
import com.solusi.erp.common.approval.domain.model.ApprovalHistory;
import com.solusi.erp.common.approval.domain.model.ApprovalRequest;
import com.solusi.erp.common.approval.domain.model.ApprovalStatus;
import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.messaging.domain.model.IntegrationEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApprovalActionOccurredEventFactoryTest {

    private ApprovalNotificationTargetResolver targetResolver;
    private ApprovalActionOccurredEventFactory factory;

    @BeforeEach
    void setUp() {
        targetResolver = mock(ApprovalNotificationTargetResolver.class);
        factory = new ApprovalActionOccurredEventFactory(
                targetResolver,
                Clock.fixed(Instant.parse("2026-06-16T01:30:00Z"), ZoneOffset.UTC));

        when(targetResolver.resolve("ACTOR", 1L))
                .thenReturn(new ApprovalNotificationTarget("ACTOR", 1L, "Requester", "requester@example.com"));
        when(targetResolver.resolve("ACTOR", 2L))
                .thenReturn(new ApprovalNotificationTarget("ACTOR", 2L, "Approver 1", "approver1@example.com"));
        when(targetResolver.resolve("ACTOR", 3L))
                .thenReturn(new ApprovalNotificationTarget("ACTOR", 3L, "Approver 2", "approver2@example.com"));
        when(targetResolver.resolve("REQUESTER", 1L))
                .thenReturn(new ApprovalNotificationTarget("REQUESTER", 1L, "Requester", "requester@example.com"));
        when(targetResolver.resolve("TARGET_APPROVER", 2L))
                .thenReturn(new ApprovalNotificationTarget("TARGET_APPROVER", 2L, "Approver 1", "approver1@example.com"));
        when(targetResolver.resolve("TARGET_APPROVER", 3L))
                .thenReturn(new ApprovalNotificationTarget("TARGET_APPROVER", 3L, "Approver 2", "approver2@example.com"));
        when(targetResolver.resolve("TARGET_APPROVER", 4L))
                .thenReturn(new ApprovalNotificationTarget("TARGET_APPROVER", 4L, "Approver 3", "approver3@example.com"));
    }

    @Test
    void requestedShouldTargetAssignedApprover() {
        ApprovalRequest request = request(
                ApprovalStatus.PENDING,
                2L,
                history(ApprovalAction.REQUESTED, 1L, 2L, "Initial Request"));

        IntegrationEvent event = factory.create(request, ApprovalAction.REQUESTED);

        assertEnvelope(event);
        ApprovalActionOccurredPayload payload = (ApprovalActionOccurredPayload) event.payload();
        assertThat(payload.action()).isEqualTo("REQUESTED");
        assertThat(payload.status()).isEqualTo("PENDING");
        assertThat(payload.actorPartyId()).isEqualTo(1L);
        assertThat(payload.actorName()).isEqualTo("Requester");
        assertThat(payload.targetApproverPartyId()).isEqualTo(2L);
        assertThat(payload.targetApproverName()).isEqualTo("Approver 1");
        assertThat(payload.targetApproverEmail()).isEqualTo("approver1@example.com");
        assertThat(payload.currentApproverPartyId()).isEqualTo(2L);
        assertThat(payload.requesterPartyId()).isEqualTo(1L);
        assertThat(payload.notificationTarget().role()).isEqualTo("TARGET_APPROVER");
        assertThat(payload.notificationTarget().partyId()).isEqualTo(2L);
        assertThat(payload.actedAt()).isEqualTo("2026-06-16T01:30:00Z");
    }

    @Test
    void forwardShouldTargetNewApprover() {
        ApprovalRequest request = request(
                ApprovalStatus.PENDING,
                3L,
                history(ApprovalAction.REQUESTED, 1L, 2L, "Initial Request"),
                history(ApprovalAction.FORWARD, 2L, 3L, "Please review"));

        IntegrationEvent event = factory.create(request, ApprovalAction.FORWARD);

        ApprovalActionOccurredPayload payload = (ApprovalActionOccurredPayload) event.payload();
        assertThat(payload.action()).isEqualTo("FORWARD");
        assertThat(payload.actorPartyId()).isEqualTo(2L);
        assertThat(payload.targetApproverPartyId()).isEqualTo(3L);
        assertThat(payload.notificationTarget().role()).isEqualTo("TARGET_APPROVER");
        assertThat(payload.notificationTarget().partyId()).isEqualTo(3L);
        assertThat(payload.notes()).isEqualTo("Please review");
    }

    @Test
    void approveAndForwardShouldTargetNewApprover() {
        ApprovalRequest request = request(
                ApprovalStatus.PENDING,
                4L,
                history(ApprovalAction.REQUESTED, 1L, 2L, "Initial Request"),
                history(ApprovalAction.APPROVE_AND_FORWARD, 3L, 4L, "Approved, next"));

        IntegrationEvent event = factory.create(request, ApprovalAction.APPROVE_AND_FORWARD);

        ApprovalActionOccurredPayload payload = (ApprovalActionOccurredPayload) event.payload();
        assertThat(payload.action()).isEqualTo("APPROVE_AND_FORWARD");
        assertThat(payload.actorName()).isEqualTo("Approver 2");
        assertThat(payload.notificationTarget().role()).isEqualTo("TARGET_APPROVER");
        assertThat(payload.notificationTarget().partyId()).isEqualTo(4L);
    }

    @Test
    void approveAndFinishShouldTargetRequester() {
        ApprovalRequest request = request(
                ApprovalStatus.COMPLETED,
                2L,
                history(ApprovalAction.REQUESTED, 1L, 2L, "Initial Request"),
                history(ApprovalAction.APPROVE_AND_FINISH, 2L, null, "Approved"));

        IntegrationEvent event = factory.create(request, ApprovalAction.APPROVE_AND_FINISH);

        ApprovalActionOccurredPayload payload = (ApprovalActionOccurredPayload) event.payload();
        assertThat(payload.action()).isEqualTo("APPROVE_AND_FINISH");
        assertThat(payload.status()).isEqualTo("COMPLETED");
        assertThat(payload.currentApproverPartyId()).isNull();
        assertThat(payload.notificationTarget().role()).isEqualTo("REQUESTER");
        assertThat(payload.notificationTarget().partyId()).isEqualTo(1L);
        assertThat(payload.requesterEmail()).isEqualTo("requester@example.com");
    }

    @Test
    void rejectedShouldTargetRequester() {
        ApprovalRequest request = request(
                ApprovalStatus.REJECTED,
                2L,
                history(ApprovalAction.REQUESTED, 1L, 2L, "Initial Request"),
                history(ApprovalAction.REJECTED, 2L, null, "Rejected"));

        IntegrationEvent event = factory.create(request, ApprovalAction.REJECTED);

        ApprovalActionOccurredPayload payload = (ApprovalActionOccurredPayload) event.payload();
        assertThat(payload.action()).isEqualTo("REJECTED");
        assertThat(payload.status()).isEqualTo("REJECTED");
        assertThat(payload.notificationTarget().role()).isEqualTo("REQUESTER");
        assertThat(payload.notificationTarget().partyId()).isEqualTo(1L);
    }

    @Test
    void missingTargetEmailShouldStillProduceEvent() {
        when(targetResolver.resolve("TARGET_APPROVER", 2L))
                .thenReturn(new ApprovalNotificationTarget("TARGET_APPROVER", 2L, "Approver 1", null));
        ApprovalRequest request = request(
                ApprovalStatus.PENDING,
                2L,
                history(ApprovalAction.REQUESTED, 1L, 2L, "Initial Request"));

        IntegrationEvent event = factory.create(request, ApprovalAction.REQUESTED);

        ApprovalActionOccurredPayload payload = (ApprovalActionOccurredPayload) event.payload();
        assertThat(payload.notificationTarget().email()).isNull();
        assertThat(payload.targetApproverEmail()).isNull();
    }

    @Test
    void missingRequestedHistoryShouldThrow() {
        ApprovalRequest request = request(
                ApprovalStatus.PENDING,
                2L,
                history(ApprovalAction.FORWARD, 2L, 3L, "Please review"));

        assertThatThrownBy(() -> factory.create(request, ApprovalAction.FORWARD))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("REQUESTED");
    }

    @Test
    void missingPublishedActionHistoryShouldThrow() {
        ApprovalRequest request = request(
                ApprovalStatus.PENDING,
                2L,
                history(ApprovalAction.REQUESTED, 1L, 2L, "Initial Request"));

        assertThatThrownBy(() -> factory.create(request, ApprovalAction.FORWARD))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("FORWARD");
    }

    @Test
    void cancelledActionShouldBeRejectedAsOutOfScopeForNotificationMvp() {
        ApprovalRequest request = request(
                ApprovalStatus.CANCELLED,
                2L,
                history(ApprovalAction.REQUESTED, 1L, 2L, "Initial Request"),
                history(ApprovalAction.CANCELLED, 2L, null, "Cancelled"));

        assertThatThrownBy(() -> factory.create(request, ApprovalAction.CANCELLED))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CANCELLED");
    }

    private static void assertEnvelope(IntegrationEvent event) {
        assertThat(event.topic()).isEqualTo("erp.approval.events.v1");
        assertThat(event.eventType()).isEqualTo("ApprovalActionOccurred");
        assertThat(event.eventVersion()).isEqualTo(1);
        assertThat(event.source()).isEqualTo("erp-monolith");
        assertThat(event.aggregateType()).isEqualTo("ApprovalRequest");
        assertThat(event.aggregateId()).isEqualTo("55");
        assertThat(event.messageKey()).isEqualTo("55");
        assertThat(event.correlationId()).isEqualTo("55");

        ApprovalActionOccurredPayload payload = (ApprovalActionOccurredPayload) event.payload();
        assertThat(payload.approvalRequestId()).isEqualTo(55L);
        assertThat(payload.referenceType()).isEqualTo("PURCHASE_ORDER");
        assertThat(payload.referenceId()).isEqualTo(42L);
        assertThat(payload.referenceCode()).isEqualTo("PO-202606-00001");
        assertThat(payload.documentLabel()).isEqualTo("Purchase Order");
        assertThat(payload.documentPath()).isEqualTo("/purchasing/purchase-orders/view/42");
    }

    private static ApprovalRequest request(
            ApprovalStatus status,
            Long currentApproverId,
            ApprovalHistory... histories) {
        ApprovalRequest request = new ApprovalRequest(
                new AuditMetadata(55L, 1L, null, null, null, null),
                "PURCHASE_ORDER",
                42L,
                "PO-202606-00001",
                "/purchasing/purchase-orders/view/42",
                status,
                currentApproverId);
        request.setHistories(List.of(histories));
        return request;
    }

    private static ApprovalHistory history(ApprovalAction action, Long actorId, Long targetApproverId, String notes) {
        return new ApprovalHistory(1L, action, actorId, targetApproverId, notes, LocalDateTime.of(2026, 6, 16, 8, 0));
    }
}
