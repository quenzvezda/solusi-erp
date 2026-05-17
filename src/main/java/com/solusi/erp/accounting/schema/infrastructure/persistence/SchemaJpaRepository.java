package com.solusi.erp.accounting.schema.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SchemaJpaRepository extends JpaRepository<AccountingSchema, Long> {

    @Query("SELECT s FROM AccountingSchema s WHERE " +
            "(LOWER(s.eventType) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(s.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<AccountingSchema> search(@Param("keyword") String keyword, Pageable pageable);

    boolean existsByEventTypeAndIsActiveTrue(String eventType);

    Optional<AccountingSchema> findByEventTypeAndIsActiveTrue(String eventType);

    boolean existsByLines_AccountId(Long accountId);
}
