package com.solusi.erp.accountspayable.vendorbill.application.usecase.query;

@FunctionalInterface
public interface GetVendorBillDetailUseCase {
    VendorBillDetailView execute(Long id);
}
