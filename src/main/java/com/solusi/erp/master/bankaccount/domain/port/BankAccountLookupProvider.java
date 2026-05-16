package com.solusi.erp.master.bankaccount.domain.port;

import com.solusi.erp.core.dto.LookupDto;

public interface BankAccountLookupProvider {
    LookupDto resolve(Long bankAccountId);
}
