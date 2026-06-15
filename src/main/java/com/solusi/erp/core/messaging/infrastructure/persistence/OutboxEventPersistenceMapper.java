package com.solusi.erp.core.messaging.infrastructure.persistence;

import com.solusi.erp.core.messaging.domain.model.OutboxEvent;

public class OutboxEventPersistenceMapper {

    public OutboxEvent toDomain(OutboxEventEntity entity) {
        return OutboxEvent.restore(
                entity.getId(),
                entity.getEventId(),
                entity.getEventType(),
                entity.getEventVersion(),
                entity.getAggregateType(),
                entity.getAggregateId(),
                entity.getTopic(),
                entity.getMessageKey(),
                entity.getPayloadJson(),
                entity.getStatus(),
                entity.getAttemptCount(),
                entity.getLastError(),
                entity.getNextAttemptAt(),
                entity.getPublishedAt());
    }

    public OutboxEventEntity toEntity(OutboxEvent domain) {
        OutboxEventEntity entity = new OutboxEventEntity();
        copyToEntity(domain, entity);
        return entity;
    }

    public void copyToEntity(OutboxEvent domain, OutboxEventEntity entity) {
        if (entity.getId() == null) {
            entity.setId(domain.getId());
        }
        entity.setEventId(domain.getEventId());
        entity.setEventType(domain.getEventType());
        entity.setEventVersion(domain.getEventVersion());
        entity.setAggregateType(domain.getAggregateType());
        entity.setAggregateId(domain.getAggregateId());
        entity.setTopic(domain.getTopic());
        entity.setMessageKey(domain.getMessageKey());
        entity.setPayloadJson(domain.getPayloadJson());
        entity.setStatus(domain.getStatus());
        entity.setAttemptCount(domain.getAttemptCount());
        entity.setLastError(domain.getLastError());
        entity.setNextAttemptAt(domain.getNextAttemptAt());
        entity.setPublishedAt(domain.getPublishedAt());
    }
}
