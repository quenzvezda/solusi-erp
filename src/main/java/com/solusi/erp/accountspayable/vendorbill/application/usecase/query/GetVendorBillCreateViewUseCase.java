package com.solusi.erp.accountspayable.vendorbill.application.usecase.query;

@FunctionalInterface
public interface GetVendorBillCreateViewUseCase {
    VendorBillCreateView execute(Long vendorId, Long currencyId);
}
