package com.solusi.erp.accounting.coa.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.accounting.coa.domain.model.AccountType;
import com.solusi.erp.accounting.coa.domain.model.ChartOfAccount;
import com.solusi.erp.accounting.coa.domain.repository.CoaRepository;

public class CreateCoaUseCaseImpl implements CreateCoaUseCase {

    private final CoaRepository repository;

    public CreateCoaUseCaseImpl(CoaRepository repository) {
        this.repository = repository;
    }

    @Override
    public ChartOfAccount execute(String code, String name, AccountType accountType,
                                   Long parentId, Integer level, Boolean isHeader,
                                   String note, Boolean isActive) {
        if (repository.existsByCode(code)) {
            throw new DomainException("msg.error.common.duplicate");
        }
        ChartOfAccount coa = ChartOfAccount.createNew(code, name, accountType,
                parentId, level, isHeader, note, isActive);
        return repository.save(coa);
    }
}
