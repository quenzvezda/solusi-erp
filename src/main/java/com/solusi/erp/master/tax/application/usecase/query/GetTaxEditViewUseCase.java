package com.solusi.erp.master.tax.application.usecase.query;

import com.solusi.erp.master.tax.domain.model.Tax;

import java.util.Optional;

@FunctionalInterface
public interface GetTaxEditViewUseCase {
    Optional<Tax> execute(Long id);
}
