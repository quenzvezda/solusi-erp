package com.solusi.erp.core.messaging.infrastructure.persistence;

import com.solusi.erp.core.messaging.domain.model.OutboxStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface OutboxEventJpaRepository extends JpaRepository<OutboxEventEntity, Long> {

    Optional<OutboxEventEntity> findByEventId(String eventId);

    @Query("""
            SELECT e FROM OutboxEventEntity e
            WHERE e.status IN :statuses
              AND (e.nextAttemptAt IS NULL OR e.nextAttemptAt <= :now)
            ORDER BY e.createdDate ASC, e.id ASC
            """)
    List<OutboxEventEntity> findPublishable(
            @Param("statuses") Collection<OutboxStatus> statuses,
            @Param("now") LocalDateTime now,
            Pageable pageable);
}
