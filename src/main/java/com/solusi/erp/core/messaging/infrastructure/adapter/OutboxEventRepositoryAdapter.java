package com.solusi.erp.core.messaging.infrastructure.adapter;

import com.solusi.erp.core.messaging.domain.model.OutboxEvent;
import com.solusi.erp.core.messaging.domain.model.OutboxStatus;
import com.solusi.erp.core.messaging.domain.repository.OutboxEventRepository;
import com.solusi.erp.core.messaging.infrastructure.persistence.OutboxEventEntity;
import com.solusi.erp.core.messaging.infrastructure.persistence.OutboxEventJpaRepository;
import com.solusi.erp.core.messaging.infrastructure.persistence.OutboxEventPersistenceMapper;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class OutboxEventRepositoryAdapter implements OutboxEventRepository {

    private final OutboxEventJpaRepository jpaRepository;
    private final OutboxEventPersistenceMapper mapper;

    public OutboxEventRepositoryAdapter(
            OutboxEventJpaRepository jpaRepository,
            OutboxEventPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public OutboxEvent save(OutboxEvent event) {
        OutboxEventEntity entity = event.getId() == null
                ? mapper.toEntity(event)
                : jpaRepository.findById(event.getId())
                        .map(existing -> {
                            mapper.copyToEntity(event, existing);
                            return existing;
                        })
                        .orElseGet(() -> mapper.toEntity(event));
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public List<OutboxEvent> findPublishableBatch(LocalDateTime now, int batchSize) {
        return jpaRepository.findPublishable(
                        List.of(OutboxStatus.PENDING, OutboxStatus.FAILED),
                        now,
                        PageRequest.of(0, batchSize))
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<OutboxEvent> findByEventId(String eventId) {
        return jpaRepository.findByEventId(eventId).map(mapper::toDomain);
    }
}
