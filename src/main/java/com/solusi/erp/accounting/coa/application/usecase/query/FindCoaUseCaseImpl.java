package com.solusi.erp.accounting.coa.application.usecase.query;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.accounting.coa.domain.model.ChartOfAccount;
import com.solusi.erp.accounting.coa.domain.repository.CoaRepository;

public class FindCoaUseCaseImpl implements FindCoaUseCase {

    private final CoaRepository repository;

    public FindCoaUseCaseImpl(CoaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<ChartOfAccount> execute(String keyword, Pageable pageable) {
        return repository.findAll(keyword, pageable);
    }
}
