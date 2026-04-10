package com.solusi.erp.accounting.coa.application.usecase.query;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.accounting.coa.domain.model.ChartOfAccount;

@FunctionalInterface
public interface FindCoaUseCase {
    Page<ChartOfAccount> execute(String keyword, Pageable pageable);
}
