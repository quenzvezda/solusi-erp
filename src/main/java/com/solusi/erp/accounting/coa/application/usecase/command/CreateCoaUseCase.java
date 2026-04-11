package com.solusi.erp.accounting.coa.application.usecase.command;

import com.solusi.erp.accounting.coa.domain.model.AccountType;
import com.solusi.erp.accounting.coa.domain.model.ChartOfAccount;

@FunctionalInterface
public interface CreateCoaUseCase {
    ChartOfAccount execute(String code, String name, AccountType accountType,
                           Long parentId, Integer level, Boolean isHeader,
                           String note, Boolean isActive);
}
