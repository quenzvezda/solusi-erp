package com.solusi.erp.accounting.schema.domain.port;

public interface SchemaInUseChecker {
    boolean isInUse(Long schemaId);
}
