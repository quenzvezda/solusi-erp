package com.solusi.erp.accounting.coa.application.usecase.query;

import com.solusi.erp.accounting.coa.domain.model.ChartOfAccount;

import java.util.Optional;

@FunctionalInterface
public interface GetCoaEditViewUseCase {
    Optional<ChartOfAccount> execute(Long id);
}
