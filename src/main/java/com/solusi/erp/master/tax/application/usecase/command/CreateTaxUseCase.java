package com.solusi.erp.master.tax.application.usecase.command;

import com.solusi.erp.master.tax.domain.model.Tax;

import java.math.BigDecimal;

@FunctionalInterface
public interface CreateTaxUseCase {
    Tax execute(String code, String name, BigDecimal rate, String note, Boolean isSubtract, Boolean isActive);
}
