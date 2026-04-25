package com.solusi.erp.purchasing.supplierpricelist.application.usecase.query;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.purchasing.supplierpricelist.domain.model.SupplierPriceList;
import com.solusi.erp.purchasing.supplierpricelist.domain.repository.SupplierPriceListRepository;

public class FindSupplierPriceListsUseCaseImpl implements FindSupplierPriceListsUseCase {

    private final SupplierPriceListRepository repository;

    public FindSupplierPriceListsUseCaseImpl(SupplierPriceListRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<SupplierPriceList> execute(String keyword, Pageable pageable) {
        return repository.findAll(keyword, pageable);
    }
}
