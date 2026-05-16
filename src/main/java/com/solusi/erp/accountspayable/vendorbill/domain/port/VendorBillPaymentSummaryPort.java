package com.solusi.erp.accountspayable.vendorbill.domain.port;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface VendorBillPaymentSummaryPort {
    PaymentSummary getPaymentSummary(Long vendorBillId);

    Map<Long, PaymentSummary> getPaymentSummaries(List<Long> vendorBillIds);

    record PaymentSummary(
            Long vendorBillId,
            BigDecimal paidAmount,
            BigDecimal outstandingAmount
    ) {}
}
