package com.solusi.erp.purchasing.supplierpricelist.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.purchasing.supplierpricelist.domain.model.SupplierPriceList;
import com.solusi.erp.purchasing.supplierpricelist.domain.repository.SupplierPriceListRepository;

public class DeleteSupplierPriceListUseCaseImpl implements DeleteSupplierPriceListUseCase {

    private final SupplierPriceListRepository repository;

    public DeleteSupplierPriceListUseCaseImpl(SupplierPriceListRepository repository) {
        this.repository = repository;
    }

    @Override
    public void execute(Long id) {
        SupplierPriceList spl = repository.findById(id)
            .orElseThrow(() -> new DomainException("msg.error.spl.notfound"));
        spl.deactivate();
        repository.save(spl);
    }
}
