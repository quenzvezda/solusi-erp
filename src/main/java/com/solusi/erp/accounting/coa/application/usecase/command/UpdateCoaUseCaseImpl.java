package com.solusi.erp.accounting.coa.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.accounting.coa.domain.model.AccountType;
import com.solusi.erp.accounting.coa.domain.model.ChartOfAccount;
import com.solusi.erp.accounting.coa.domain.repository.CoaRepository;

public class UpdateCoaUseCaseImpl implements UpdateCoaUseCase {

    private final CoaRepository repository;

    public UpdateCoaUseCaseImpl(CoaRepository repository) {
        this.repository = repository;
    }

    @Override
    public ChartOfAccount execute(Long id, String name, AccountType accountType,
                                   Long parentId, Integer level, Boolean isHeader,
                                   String note, Boolean isActive) {
        ChartOfAccount coa = repository.findById(id)
                .orElseThrow(() -> new DomainException("msg.error.coa.notfound"));
        coa.update(name, accountType, parentId, level, isHeader, note, isActive);
        return repository.save(coa);
    }
}
