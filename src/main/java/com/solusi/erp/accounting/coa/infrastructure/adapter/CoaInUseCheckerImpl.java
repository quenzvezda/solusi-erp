package com.solusi.erp.accounting.coa.infrastructure.adapter;

import com.solusi.erp.accounting.coa.domain.port.CoaInUseChecker;
import com.solusi.erp.accounting.schema.infrastructure.persistence.SchemaJpaRepository;

public class CoaInUseCheckerImpl implements CoaInUseChecker {

    private final SchemaJpaRepository schemaJpaRepository;

    public CoaInUseCheckerImpl(SchemaJpaRepository schemaJpaRepository) {
        this.schemaJpaRepository = schemaJpaRepository;
    }

    @Override
    public boolean isInUse(Long coaId) {
        return schemaJpaRepository.existsByDebitAccountIdOrCreditAccountId(coaId, coaId);
    }
}
