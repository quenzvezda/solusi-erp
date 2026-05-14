package com.solusi.erp.accountspayable.vendorbill.application.usecase.query;

import com.solusi.erp.accountspayable.vendorbill.domain.port.BillableApReference;

import java.util.List;

@FunctionalInterface
public interface FindBillableReferencesUseCase {
    List<BillableApReference> execute(Long vendorId, Long currencyId);
}
