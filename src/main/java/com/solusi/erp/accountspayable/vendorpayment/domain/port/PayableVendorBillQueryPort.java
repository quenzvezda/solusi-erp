package com.solusi.erp.accountspayable.vendorpayment.domain.port;

import java.math.BigDecimal;
import java.util.List;

public interface PayableVendorBillQueryPort {
    List<PayableVendorBillView> findPayableVendorBills(Long vendorId, Long currencyId);

    record PayableVendorBillView(
            Long vendorBillId,
            String billCode,
            BigDecimal totalAmount,
            BigDecimal paidAmount,
            BigDecimal outstandingAmount
    ) {}
}
