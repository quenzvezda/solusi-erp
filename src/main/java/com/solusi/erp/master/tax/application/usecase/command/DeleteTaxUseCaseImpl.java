package com.solusi.erp.master.tax.application.usecase.command;

import com.solusi.erp.core.domain.model.DeleteResult;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.master.tax.domain.model.Tax;
import com.solusi.erp.master.tax.domain.port.TaxInUseChecker;
import com.solusi.erp.master.tax.domain.repository.TaxRepository;

public class DeleteTaxUseCaseImpl implements DeleteTaxUseCase {

    private final TaxRepository repository;
    private final TaxInUseChecker inUseChecker;

    public DeleteTaxUseCaseImpl(TaxRepository repository, TaxInUseChecker inUseChecker) {
        this.repository = repository;
        this.inUseChecker = inUseChecker;
    }

    @Override
    public DeleteResult execute(Long id) {
        Tax tax = repository.findById(id)
                .orElseThrow(() -> new DomainException("msg.error.tax.notfound"));

        if (inUseChecker.isInUse(id)) {
            tax.softDelete();
            repository.save(tax);
            return DeleteResult.SOFT_DELETED;
        }

        repository.delete(id);
        return DeleteResult.HARD_DELETED;
    }
}
