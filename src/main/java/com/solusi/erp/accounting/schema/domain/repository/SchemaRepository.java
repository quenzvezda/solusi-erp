package com.solusi.erp.accounting.schema.domain.repository;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.accounting.schema.domain.model.AccountingSchema;
import com.solusi.erp.accounting.schema.domain.model.SchemaEventType;

import java.util.Optional;

public interface SchemaRepository {
    AccountingSchema save(AccountingSchema schema);
    Optional<AccountingSchema> findById(Long id);
    Page<AccountingSchema> findAll(String keyword, Pageable pageable);
    void delete(Long id);
    boolean existsByEventTypeAndIsActiveTrue(SchemaEventType eventType);
    Optional<AccountingSchema> findByEventTypeAndIsActiveTrue(SchemaEventType eventType);
}
