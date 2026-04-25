package com.solusi.erp.purchasing.supplierpricelist.application.usecase.query;

import com.solusi.erp.purchasing.supplierpricelist.domain.model.SupplierPriceList;
import com.solusi.erp.purchasing.supplierpricelist.domain.repository.SupplierPriceListRepository;

import java.util.Optional;

public class GetSupplierPriceListEditViewUseCaseImpl implements GetSupplierPriceListEditViewUseCase {

    private final SupplierPriceListRepository repository;

    public GetSupplierPriceListEditViewUseCaseImpl(SupplierPriceListRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<SupplierPriceList> execute(Long id) {
        return repository.findById(id);
    }
}
