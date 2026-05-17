package com.solusi.erp.accounting.journal.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.Optional;

public interface JournalEntryJpaRepository extends JpaRepository<JournalEntryEntity, Long>, JpaSpecificationExecutor<JournalEntryEntity> {
    boolean existsBySourceTypeAndSourceId(String sourceType, Long sourceId);
    Optional<JournalEntryEntity> findBySourceTypeAndSourceId(String sourceType, Long sourceId);
}
