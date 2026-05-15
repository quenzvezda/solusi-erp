package com.solusi.erp.accountspayable.vendorpayment.application.usecase.command;

import com.solusi.erp.accountspayable.vendorpayment.domain.model.VendorPayment;
import com.solusi.erp.accountspayable.vendorpayment.domain.model.VendorPaymentStatus;
import com.solusi.erp.accountspayable.vendorpayment.domain.repository.VendorPaymentRepository;
import com.solusi.erp.core.exception.DomainException;

public class DeleteVendorPaymentUseCaseImpl implements DeleteVendorPaymentUseCase {

    private final VendorPaymentRepository repository;

    public DeleteVendorPaymentUseCaseImpl(VendorPaymentRepository repository) {
        this.repository = repository;
    }

    @Override
    public void execute(Long id) {
        VendorPayment payment = repository.findById(id)
                .orElseThrow(() -> new DomainException("msg.err.vp.not.found"));
        if (payment.getStatus() != VendorPaymentStatus.DRAFT) {
            throw new DomainException("msg.err.vp.delete.only.draft");
        }
        repository.deleteById(id);
    }
}
