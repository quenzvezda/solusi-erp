package com.solusi.erp.core.messaging.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class OutboxEvent {

    private static final int MAX_ERROR_LENGTH = 1_000;

    private Long id;
    private final String eventId;
    private final String eventType;
    private final int eventVersion;
    private final String aggregateType;
    private final String aggregateId;
    private final String topic;
    private final String messageKey;
    private final String payloadJson;
    private OutboxStatus status;
    private int attemptCount;
    private String lastError;
    private LocalDateTime nextAttemptAt;
    private LocalDateTime publishedAt;

    private OutboxEvent(
            Long id,
            String eventId,
            String eventType,
            int eventVersion,
            String aggregateType,
            String aggregateId,
            String topic,
            String messageKey,
            String payloadJson,
            OutboxStatus status,
            int attemptCount,
            String lastError,
            LocalDateTime nextAttemptAt,
            LocalDateTime publishedAt) {
        this.id = id;
        this.eventId = requireText(eventId, "eventId");
        this.eventType = requireText(eventType, "eventType");
        this.eventVersion = eventVersion;
        this.aggregateType = requireText(aggregateType, "aggregateType");
        this.aggregateId = requireText(aggregateId, "aggregateId");
        this.topic = requireText(topic, "topic");
        this.messageKey = requireText(messageKey, "messageKey");
        this.payloadJson = requireText(payloadJson, "payloadJson");
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.attemptCount = attemptCount;
        this.lastError = lastError;
        this.nextAttemptAt = nextAttemptAt;
        this.publishedAt = publishedAt;
    }

    public static OutboxEvent pending(
            String eventId,
            String eventType,
            int eventVersion,
            String aggregateType,
            String aggregateId,
            String topic,
            String messageKey,
            String payloadJson) {
        return new OutboxEvent(
                null,
                eventId,
                eventType,
                eventVersion,
                aggregateType,
                aggregateId,
                topic,
                messageKey,
                payloadJson,
                OutboxStatus.PENDING,
                0,
                null,
                null,
                null);
    }

    public static OutboxEvent restore(
            Long id,
            String eventId,
            String eventType,
            int eventVersion,
            String aggregateType,
            String aggregateId,
            String topic,
            String messageKey,
            String payloadJson,
            OutboxStatus status,
            int attemptCount,
            String lastError,
            LocalDateTime nextAttemptAt,
            LocalDateTime publishedAt) {
        return new OutboxEvent(
                id,
                eventId,
                eventType,
                eventVersion,
                aggregateType,
                aggregateId,
                topic,
                messageKey,
                payloadJson,
                status,
                attemptCount,
                lastError,
                nextAttemptAt,
                publishedAt);
    }

    public void markPublished(LocalDateTime publishedAt) {
        this.status = OutboxStatus.PUBLISHED;
        this.publishedAt = Objects.requireNonNull(publishedAt, "publishedAt must not be null");
        this.lastError = null;
        this.nextAttemptAt = null;
    }

    public void markFailed(String errorMessage, LocalDateTime nextAttemptAt) {
        this.status = OutboxStatus.FAILED;
        this.attemptCount++;
        this.lastError = truncate(errorMessage);
        this.nextAttemptAt = Objects.requireNonNull(nextAttemptAt, "nextAttemptAt must not be null");
    }

    public Long getId() {
        return id;
    }

    public String getEventId() {
        return eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public int getEventVersion() {
        return eventVersion;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public String getTopic() {
        return topic;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public String getPayloadJson() {
        return payloadJson;
    }

    public OutboxStatus getStatus() {
        return status;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public String getLastError() {
        return lastError;
    }

    public LocalDateTime getNextAttemptAt() {
        return nextAttemptAt;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }

    private static String truncate(String value) {
        if (value == null) {
            return null;
        }
        return value.length() <= MAX_ERROR_LENGTH ? value : value.substring(0, MAX_ERROR_LENGTH);
    }
}
