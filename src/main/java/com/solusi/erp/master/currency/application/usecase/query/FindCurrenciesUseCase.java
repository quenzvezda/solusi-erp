package com.solusi.erp.master.currency.application.usecase.query;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.master.currency.domain.model.Currency;

@FunctionalInterface
public interface FindCurrenciesUseCase {
    Page<Currency> execute(String keyword, Pageable pageable);
}
