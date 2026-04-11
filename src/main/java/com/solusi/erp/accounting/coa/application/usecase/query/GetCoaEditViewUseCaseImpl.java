package com.solusi.erp.accounting.coa.application.usecase.query;

import com.solusi.erp.accounting.coa.domain.model.ChartOfAccount;
import com.solusi.erp.accounting.coa.domain.repository.CoaRepository;

import java.util.Optional;

public class GetCoaEditViewUseCaseImpl implements GetCoaEditViewUseCase {

    private final CoaRepository repository;

    public GetCoaEditViewUseCaseImpl(CoaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<ChartOfAccount> execute(Long id) {
        return repository.findById(id);
    }
}
