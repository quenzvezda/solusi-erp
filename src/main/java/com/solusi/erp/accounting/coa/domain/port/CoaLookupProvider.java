package com.solusi.erp.accounting.coa.domain.port;

import com.solusi.erp.core.dto.LookupDto;

public interface CoaLookupProvider {
    LookupDto resolve(Long coaId);
}
