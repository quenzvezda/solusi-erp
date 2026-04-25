package com.solusi.erp.master.tax.domain.port;

import com.solusi.erp.core.dto.LookupDto;

public interface TaxLookupProvider {
    LookupDto resolve(Long taxId);
}
