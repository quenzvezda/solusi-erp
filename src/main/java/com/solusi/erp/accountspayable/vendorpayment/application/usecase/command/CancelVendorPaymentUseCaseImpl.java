package com.solusi.erp.accountspayable.vendorpayment.application.usecase.command;

import com.solusi.erp.accountspayable.vendorpayment.domain.model.VendorPayment;
import com.solusi.erp.accountspayable.vendorpayment.domain.model.VendorPaymentStatus;
import com.solusi.erp.accountspayable.vendorpayment.domain.repository.VendorPaymentRepository;
import com.solusi.erp.core.exception.DomainException;

public class CancelVendorPaymentUseCaseImpl implements CancelVendorPaymentUseCase {

    private final VendorPaymentRepository repository;

    public CancelVendorPaymentUseCaseImpl(VendorPaymentRepository repository) {
        this.repository = repository;
    }

    @Override
    public void execute(Long id) {
        VendorPayment payment = repository.findById(id)
                .orElseThrow(() -> new DomainException("msg.err.vp.not.found"));
        payment.cancel();
        repository.save(payment);
    }
}
