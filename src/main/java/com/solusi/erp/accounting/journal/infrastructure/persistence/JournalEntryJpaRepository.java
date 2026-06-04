package com.solusi.erp.accounting.journal.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.EntityGraph;
import java.util.Optional;

public interface JournalEntryJpaRepository extends JpaRepository<JournalEntryEntity, Long>, JpaSpecificationExecutor<JournalEntryEntity> {
    boolean existsBySourceTypeAndSourceId(String sourceType, Long sourceId);
    @EntityGraph(attributePaths = "lines")
    Optional<JournalEntryEntity> findBySourceTypeAndSourceId(String sourceType, Long sourceId);
    boolean existsByReversalOfId(Long reversalOfId);
    @EntityGraph(attributePaths = "lines")
    Optional<JournalEntryEntity> findByReversalOfId(Long reversalOfId);
}
