package com.solusi.erp.accounting.coa.application.usecase.query;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;

public interface FindCoaSelectorUseCase {
    Page<CoaSelectorRow> execute(String keyword, String accountType, Pageable pageable);
}
