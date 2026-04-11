package com.solusi.erp.accounting.coa.domain.port;

public interface CoaInUseChecker {
    boolean isInUse(Long coaId);
}
