package com.solusi.erp.accounting.journal.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JournalEntryJpaRepository extends JpaRepository<JournalEntryEntity, Long> {
    boolean existsBySourceTypeAndSourceId(String sourceType, Long sourceId);
    Optional<JournalEntryEntity> findBySourceTypeAndSourceId(String sourceType, Long sourceId);
}
