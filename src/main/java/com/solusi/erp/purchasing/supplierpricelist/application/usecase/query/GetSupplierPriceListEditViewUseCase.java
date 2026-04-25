package com.solusi.erp.purchasing.supplierpricelist.application.usecase.query;

import com.solusi.erp.purchasing.supplierpricelist.domain.model.SupplierPriceList;

import java.util.Optional;

@FunctionalInterface
public interface GetSupplierPriceListEditViewUseCase {
    Optional<SupplierPriceList> execute(Long id);
}
