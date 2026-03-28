package com.solusi.erp.master.tax.application.usecase.query;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.master.tax.domain.model.Tax;

@FunctionalInterface
public interface FindTaxesUseCase {
    Page<Tax> execute(String keyword, Pageable pageable);
}
