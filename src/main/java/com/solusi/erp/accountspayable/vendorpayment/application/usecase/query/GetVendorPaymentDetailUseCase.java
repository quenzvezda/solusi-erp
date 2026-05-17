package com.solusi.erp.accountspayable.vendorpayment.application.usecase.query;

import com.solusi.erp.accountspayable.vendorpayment.domain.model.VendorPayment;

import java.util.Optional;

public interface GetVendorPaymentDetailUseCase {
    Optional<VendorPayment> execute(Long id);
}
