package com.solusi.erp.common.approval.application.service;

import com.solusi.erp.common.approval.domain.model.ApprovalAction;
import com.solusi.erp.common.approval.domain.model.ApprovalHistory;
import com.solusi.erp.common.approval.domain.model.ApprovalRequest;
import com.solusi.erp.common.approval.domain.model.ApprovalStatus;
import com.solusi.erp.core.messaging.domain.model.IntegrationEvent;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

public class ApprovalActionOccurredEventFactory {

    private static final String TOPIC = "erp.approval.events.v1";
    private static final String EVENT_TYPE = "ApprovalActionOccurred";
    private static final int EVENT_VERSION = 1;
    private static final String SOURCE = "erp-monolith";
    private static final String AGGREGATE_TYPE = "ApprovalRequest";

    private final ApprovalNotificationTargetResolver targetResolver;
    private final Clock clock;

    public ApprovalActionOccurredEventFactory(
            ApprovalNotificationTargetResolver targetResolver,
            Clock clock) {
        this.targetResolver = Objects.requireNonNull(targetResolver, "targetResolver must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    public IntegrationEvent create(ApprovalRequest request, ApprovalAction action) {
        Objects.requireNonNull(request, "request must not be null");
        Objects.requireNonNull(action, "action must not be null");

        ApprovalHistory requested = findFirstHistory(request.getHistories(), ApprovalAction.REQUESTED);
        ApprovalHistory latest = findLatestHistory(request.getHistories(), action);

        ApprovalNotificationTarget actor = targetResolver.resolve("ACTOR", latest.actorId());
        ApprovalNotificationTarget requester = targetResolver.resolve("REQUESTER", requested.actorId());
        ApprovalNotificationTarget targetApprover = resolveTargetApprover(latest);
        ApprovalNotificationTarget notificationTarget = resolveNotificationTarget(action, requester, targetApprover);

        ApprovalActionOccurredPayload payload = new ApprovalActionOccurredPayload(
                request.getId(),
                request.getReferenceType(),
                request.getReferenceId(),
                request.getReferenceCode(),
                toDocumentLabel(request.getReferenceType()),
                request.getDocumentPath(),
                action.name(),
                request.getStatus().name(),
                latest.actorId(),
                actor.name(),
                targetApprover != null ? targetApprover.partyId() : null,
                targetApprover != null ? targetApprover.name() : null,
                targetApprover != null ? targetApprover.email() : null,
                request.getStatus() == ApprovalStatus.PENDING ? request.getCurrentApproverId() : null,
                requester.partyId(),
                requester.name(),
                requester.email(),
                notificationTarget,
                latest.notes(),
                DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(OffsetDateTime.now(clock)));

        return new ApprovalActionOccurredIntegrationEvent(String.valueOf(request.getId()), payload);
    }

    private ApprovalNotificationTarget resolveTargetApprover(ApprovalHistory latest) {
        if (latest.targetApproverId() == null) {
            return null;
        }
        return targetResolver.resolve("TARGET_APPROVER", latest.targetApproverId());
    }

    private static ApprovalNotificationTarget resolveNotificationTarget(
            ApprovalAction action,
            ApprovalNotificationTarget requester,
            ApprovalNotificationTarget targetApprover) {
        return switch (action) {
            case REQUESTED, FORWARD, APPROVE_AND_FORWARD -> targetApprover;
            case APPROVE_AND_FINISH, REJECTED -> requester;
            case CANCELLED -> null;
        };
    }

    private static ApprovalHistory findFirstHistory(List<ApprovalHistory> histories, ApprovalAction action) {
        return histories.stream()
                .filter(history -> history.action() == action)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Approval history not found for action " + action));
    }

    private static ApprovalHistory findLatestHistory(List<ApprovalHistory> histories, ApprovalAction action) {
        for (int i = histories.size() - 1; i >= 0; i--) {
            ApprovalHistory history = histories.get(i);
            if (history.action() == action) {
                return history;
            }
        }
        throw new IllegalStateException("Approval history not found for action " + action);
    }

    private static String toDocumentLabel(String referenceType) {
        if (referenceType == null || referenceType.isBlank()) {
            return null;
        }
        String[] words = referenceType.toLowerCase().split("_+");
        StringBuilder label = new StringBuilder();
        for (String word : words) {
            if (word.isBlank()) {
                continue;
            }
            if (!label.isEmpty()) {
                label.append(' ');
            }
            label.append(Character.toUpperCase(word.charAt(0)));
            if (word.length() > 1) {
                label.append(word.substring(1));
            }
        }
        return label.toString();
    }

    private record ApprovalActionOccurredIntegrationEvent(
            String aggregateId,
            ApprovalActionOccurredPayload payload) implements IntegrationEvent {

        @Override
        public String topic() {
            return TOPIC;
        }

        @Override
        public String messageKey() {
            return aggregateId;
        }

        @Override
        public String eventType() {
            return EVENT_TYPE;
        }

        @Override
        public int eventVersion() {
            return EVENT_VERSION;
        }

        @Override
        public String source() {
            return SOURCE;
        }

        @Override
        public String correlationId() {
            return aggregateId;
        }

        @Override
        public String aggregateType() {
            return AGGREGATE_TYPE;
        }
    }
}
