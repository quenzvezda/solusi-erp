package com.solusi.erp.accounting.coa.domain.port;

public interface CoaPostingValidator {
    boolean isPostable(Long coaId);
}
