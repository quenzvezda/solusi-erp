package com.solusi.erp.core.messaging.domain.repository;

import com.solusi.erp.core.messaging.domain.model.OutboxEvent;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OutboxEventRepository {
    OutboxEvent save(OutboxEvent event);

    List<OutboxEvent> findPublishableBatch(LocalDateTime now, int batchSize);

    Optional<OutboxEvent> findByEventId(String eventId);
}
