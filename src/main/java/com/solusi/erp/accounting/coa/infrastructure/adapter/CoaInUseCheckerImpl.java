package com.solusi.erp.accounting.coa.infrastructure.adapter;

import com.solusi.erp.accounting.coa.domain.port.CoaInUseChecker;

public class CoaInUseCheckerImpl implements CoaInUseChecker {
    @Override
    public boolean isInUse(Long coaId) {
        // Will wire to SchemaJpaRepository once Task 6 is complete
        return false;
    }
}
