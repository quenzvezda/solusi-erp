package com.solusi.erp.accountspayable.vendorpayment.application.usecase.query;

import com.solusi.erp.accountspayable.vendorpayment.domain.port.PayableVendorBillQueryPort;

import java.util.List;

public interface GetPayableVendorBillsUseCase {
    List<PayableVendorBillQueryPort.PayableVendorBillView> execute(Long vendorId, Long currencyId);
}
