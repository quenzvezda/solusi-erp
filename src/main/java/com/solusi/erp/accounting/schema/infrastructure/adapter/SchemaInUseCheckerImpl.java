package com.solusi.erp.accounting.schema.infrastructure.adapter;

import com.solusi.erp.accounting.schema.domain.port.SchemaInUseChecker;

public class SchemaInUseCheckerImpl implements SchemaInUseChecker {
    @Override
    public boolean isInUse(Long schemaId) {
        // Will be wired to JournalEntry references when Journal module is built
        return false;
    }
}
