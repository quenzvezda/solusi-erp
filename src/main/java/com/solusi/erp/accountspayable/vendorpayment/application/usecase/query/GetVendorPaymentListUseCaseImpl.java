package com.solusi.erp.accountspayable.vendorpayment.application.usecase.query;

import com.solusi.erp.accountspayable.vendorpayment.domain.model.VendorPayment;
import com.solusi.erp.accountspayable.vendorpayment.domain.model.VendorPaymentStatus;
import com.solusi.erp.accountspayable.vendorpayment.domain.repository.VendorPaymentRepository;
import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;

public class GetVendorPaymentListUseCaseImpl implements GetVendorPaymentListUseCase {

    private final VendorPaymentRepository repository;

    public GetVendorPaymentListUseCaseImpl(VendorPaymentRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<VendorPayment> execute(String keyword, Long vendorId, VendorPaymentStatus status, Pageable pageable) {
        return repository.findAll(keyword, vendorId, status, pageable);
    }
}
