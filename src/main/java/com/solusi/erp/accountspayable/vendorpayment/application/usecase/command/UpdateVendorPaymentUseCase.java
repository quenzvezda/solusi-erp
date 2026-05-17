package com.solusi.erp.accountspayable.vendorpayment.application.usecase.command;

import com.solusi.erp.accountspayable.vendorpayment.domain.model.VendorPayment;
import com.solusi.erp.accountspayable.vendorpayment.domain.model.VendorPaymentLine;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface UpdateVendorPaymentUseCase {
    VendorPayment execute(Long id, Long vendorId, Long currencyId, Long bankAccountId,
                          LocalDate paymentDate, BigDecimal exchangeRate, BigDecimal paymentAmount,
                          String reference, String notes, List<VendorPaymentLine> lines);
}
