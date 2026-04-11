package com.solusi.erp.purchasing.supplierpricelist.application.usecase.query;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.purchasing.supplierpricelist.domain.model.SupplierPriceList;

@FunctionalInterface
public interface FindSupplierPriceListsUseCase {
    Page<SupplierPriceList> execute(String keyword, Pageable pageable);
}
