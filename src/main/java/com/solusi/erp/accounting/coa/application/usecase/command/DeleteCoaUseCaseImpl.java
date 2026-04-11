package com.solusi.erp.accounting.coa.application.usecase.command;

import com.solusi.erp.core.domain.model.DeleteResult;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.accounting.coa.domain.model.ChartOfAccount;
import com.solusi.erp.accounting.coa.domain.port.CoaInUseChecker;
import com.solusi.erp.accounting.coa.domain.repository.CoaRepository;

public class DeleteCoaUseCaseImpl implements DeleteCoaUseCase {

    private final CoaRepository repository;
    private final CoaInUseChecker inUseChecker;

    public DeleteCoaUseCaseImpl(CoaRepository repository, CoaInUseChecker inUseChecker) {
        this.repository = repository;
        this.inUseChecker = inUseChecker;
    }

    @Override
    public DeleteResult execute(Long id) {
        ChartOfAccount coa = repository.findById(id)
                .orElseThrow(() -> new DomainException("msg.error.coa.notfound"));

        if (inUseChecker.isInUse(id)) {
            coa.softDelete();
            repository.save(coa);
            return DeleteResult.SOFT_DELETED;
        }

        repository.delete(id);
        return DeleteResult.HARD_DELETED;
    }
}
