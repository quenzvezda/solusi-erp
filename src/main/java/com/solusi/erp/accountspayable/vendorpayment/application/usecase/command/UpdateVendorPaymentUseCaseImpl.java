package com.solusi.erp.accountspayable.vendorpayment.application.usecase.command;

import com.solusi.erp.accountspayable.vendorpayment.domain.model.VendorPayment;
import com.solusi.erp.accountspayable.vendorpayment.domain.model.VendorPaymentLine;
import com.solusi.erp.accountspayable.vendorpayment.domain.repository.VendorPaymentRepository;
import com.solusi.erp.core.exception.DomainException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class UpdateVendorPaymentUseCaseImpl implements UpdateVendorPaymentUseCase {

    private final VendorPaymentRepository repository;

    public UpdateVendorPaymentUseCaseImpl(VendorPaymentRepository repository) {
        this.repository = repository;
    }

    @Override
    public VendorPayment execute(Long id, Long vendorId, Long currencyId, Long bankAccountId,
                                  LocalDate paymentDate, BigDecimal exchangeRate, BigDecimal paymentAmount,
                                  String reference, String notes, List<VendorPaymentLine> lines) {
        VendorPayment payment = repository.findById(id)
                .orElseThrow(() -> new DomainException("msg.err.vp.not.found"));
        payment.update(vendorId, currencyId, bankAccountId, paymentDate, exchangeRate,
                paymentAmount, reference, notes, lines);
        return repository.save(payment);
    }
}
