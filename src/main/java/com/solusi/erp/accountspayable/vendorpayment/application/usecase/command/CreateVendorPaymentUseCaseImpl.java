package com.solusi.erp.accountspayable.vendorpayment.application.usecase.command;

import com.solusi.erp.accountspayable.vendorpayment.domain.model.VendorPayment;
import com.solusi.erp.accountspayable.vendorpayment.domain.model.VendorPaymentLine;
import com.solusi.erp.core.infrastructure.sequence.SequenceGeneratorService;
import com.solusi.erp.accountspayable.vendorpayment.domain.repository.VendorPaymentRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class CreateVendorPaymentUseCaseImpl implements CreateVendorPaymentUseCase {

    private final VendorPaymentRepository repository;
    private final SequenceGeneratorService sequenceGeneratorService;

    public CreateVendorPaymentUseCaseImpl(VendorPaymentRepository repository,
                                           SequenceGeneratorService sequenceGeneratorService) {
        this.repository = repository;
        this.sequenceGeneratorService = sequenceGeneratorService;
    }

    @Override
    public VendorPayment execute(Long vendorId, Long currencyId, Long bankAccountId,
                                  LocalDate paymentDate, BigDecimal exchangeRate, BigDecimal paymentAmount,
                                  String reference, String notes, List<VendorPaymentLine> lines) {
        String code = sequenceGeneratorService.generate("VENDOR-PAYMENT");
        VendorPayment payment = VendorPayment.createNew(code, vendorId, currencyId, bankAccountId,
                paymentDate, exchangeRate, paymentAmount, reference, notes, lines);
        return repository.save(payment);
    }
}
