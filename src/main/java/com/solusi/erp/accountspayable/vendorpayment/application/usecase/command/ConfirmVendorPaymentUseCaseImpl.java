package com.solusi.erp.accountspayable.vendorpayment.application.usecase.command;

import com.solusi.erp.accounting.journal.application.usecase.command.JournalPostingCommand;
import com.solusi.erp.accounting.journal.application.usecase.command.PostJournalForEventUseCase;
import com.solusi.erp.accounting.journal.domain.model.JournalVariable;
import com.solusi.erp.accounting.schema.domain.model.SchemaEventType;
import com.solusi.erp.accountspayable.vendorpayment.domain.model.VendorPayment;
import com.solusi.erp.accountspayable.vendorpayment.domain.model.VendorPaymentLine;
import com.solusi.erp.accountspayable.vendorpayment.domain.port.VendorBillPaymentUpdatePort;
import com.solusi.erp.accountspayable.vendorpayment.domain.repository.VendorPaymentRepository;
import com.solusi.erp.core.exception.DomainException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfirmVendorPaymentUseCaseImpl implements ConfirmVendorPaymentUseCase {

    private final VendorPaymentRepository repository;
    private final PostJournalForEventUseCase postJournalForEventUseCase;
    private final VendorBillPaymentUpdatePort vendorBillPaymentUpdatePort;
    private final Long bankAccountCoaId;

    public ConfirmVendorPaymentUseCaseImpl(VendorPaymentRepository repository,
                                            PostJournalForEventUseCase postJournalForEventUseCase,
                                            VendorBillPaymentUpdatePort vendorBillPaymentUpdatePort) {
        this.repository = repository;
        this.postJournalForEventUseCase = postJournalForEventUseCase;
        this.vendorBillPaymentUpdatePort = vendorBillPaymentUpdatePort;
        this.bankAccountCoaId = null;
    }

    @Override
    public void execute(Long id) {
        execute(id, null);
    }

    public void execute(Long id, Long bankCoaId) {
        VendorPayment payment = repository.findById(id)
                .orElseThrow(() -> new DomainException("msg.err.vp.not.found"));

        payment.confirm();
        vendorBillPaymentUpdatePort.lockAndValidatePayment(payment);

        BigDecimal paymentAmountBase = payment.getPaymentAmount()
                .multiply(payment.getExchangeRate())
                .setScale(4, RoundingMode.HALF_UP);

        BigDecimal apAmountBase = payment.getLines().stream()
                .map(VendorPaymentLine::getPaidAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .multiply(payment.getExchangeRate())
                .setScale(4, RoundingMode.HALF_UP);

        BigDecimal fxDiff = apAmountBase.subtract(paymentAmountBase);
        BigDecimal fxLoss = fxDiff.signum() > 0 ? fxDiff : BigDecimal.ZERO;
        BigDecimal fxGain = fxDiff.signum() < 0 ? fxDiff.abs() : BigDecimal.ZERO;

        Map<JournalVariable, BigDecimal> values = new HashMap<>();
        values.put(JournalVariable.VP_AP_AMT, apAmountBase);
        values.put(JournalVariable.VP_BANK_OUT_AMT, paymentAmountBase);
        values.put(JournalVariable.VP_FX_LOSS_AMT, fxLoss);
        values.put(JournalVariable.VP_FX_GAIN_AMT, fxGain);

        Map<JournalVariable, Long> accountOverrides = null;
        if (bankCoaId != null) {
            accountOverrides = Map.of(JournalVariable.VP_BANK_OUT_AMT, bankCoaId);
        }

        postJournalForEventUseCase.execute(new JournalPostingCommand(
                SchemaEventType.VENDOR_PAYMENT,
                "VENDOR_PAYMENT",
                payment.getId(),
                payment.getCode(),
                payment.getPaymentDate(),
                "Auto journal for vendor payment " + payment.getCode(),
                values,
                null, null, null,
                accountOverrides
        ));

        repository.save(payment);

        List<Long> billIds = payment.getLines().stream()
                .map(VendorPaymentLine::getVendorBillId)
                .distinct()
                .toList();
        vendorBillPaymentUpdatePort.updateSettlementStatus(billIds);
    }
}
