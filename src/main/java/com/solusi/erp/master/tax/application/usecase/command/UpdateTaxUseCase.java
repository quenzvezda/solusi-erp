package com.solusi.erp.master.tax.application.usecase.command;

import com.solusi.erp.master.tax.domain.model.Tax;

import java.math.BigDecimal;

@FunctionalInterface
public interface UpdateTaxUseCase {
    Tax execute(Long id, String name, BigDecimal rate, String note, Boolean isSubtract, Boolean isActive);
}
