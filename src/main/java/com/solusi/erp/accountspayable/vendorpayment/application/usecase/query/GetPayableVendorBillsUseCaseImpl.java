package com.solusi.erp.accountspayable.vendorpayment.application.usecase.query;

import com.solusi.erp.accountspayable.vendorpayment.domain.port.PayableVendorBillQueryPort;

import java.util.List;

public class GetPayableVendorBillsUseCaseImpl implements GetPayableVendorBillsUseCase {

    private final PayableVendorBillQueryPort payableVendorBillQueryPort;

    public GetPayableVendorBillsUseCaseImpl(PayableVendorBillQueryPort payableVendorBillQueryPort) {
        this.payableVendorBillQueryPort = payableVendorBillQueryPort;
    }

    @Override
    public List<PayableVendorBillQueryPort.PayableVendorBillView> execute(Long vendorId, Long currencyId) {
        return payableVendorBillQueryPort.findPayableVendorBills(vendorId, currencyId);
    }
}
