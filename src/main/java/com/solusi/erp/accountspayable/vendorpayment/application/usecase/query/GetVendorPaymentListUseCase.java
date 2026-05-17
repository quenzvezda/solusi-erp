package com.solusi.erp.accountspayable.vendorpayment.application.usecase.query;

import com.solusi.erp.accountspayable.vendorpayment.domain.model.VendorPayment;
import com.solusi.erp.accountspayable.vendorpayment.domain.model.VendorPaymentStatus;
import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;

public interface GetVendorPaymentListUseCase {
    Page<VendorPayment> execute(String keyword, Long vendorId, VendorPaymentStatus status, Pageable pageable);
}
