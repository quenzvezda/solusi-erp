package com.solusi.erp.accountspayable.vendorpayment.application.usecase.query;

import com.solusi.erp.accountspayable.vendorpayment.domain.model.VendorPayment;
import com.solusi.erp.accountspayable.vendorpayment.domain.repository.VendorPaymentRepository;

import java.util.Optional;

public class GetVendorPaymentDetailUseCaseImpl implements GetVendorPaymentDetailUseCase {

    private final VendorPaymentRepository repository;

    public GetVendorPaymentDetailUseCaseImpl(VendorPaymentRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<VendorPayment> execute(Long id) {
        return repository.findById(id);
    }
}
