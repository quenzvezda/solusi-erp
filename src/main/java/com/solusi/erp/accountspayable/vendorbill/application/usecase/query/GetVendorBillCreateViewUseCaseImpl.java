package com.solusi.erp.accountspayable.vendorbill.application.usecase.query;

import com.solusi.erp.accountspayable.vendorbill.domain.port.BillableGrQueryPort;

import java.util.List;

public class GetVendorBillCreateViewUseCaseImpl implements GetVendorBillCreateViewUseCase {

    private final BillableGrQueryPort billableGrQueryPort;

    public GetVendorBillCreateViewUseCaseImpl(BillableGrQueryPort billableGrQueryPort) {
        this.billableGrQueryPort = billableGrQueryPort;
    }

    @Override
    public VendorBillCreateView execute(Long vendorId, Long currencyId) {
        return new VendorBillCreateView(
                vendorId,
                currencyId,
                vendorId == null || currencyId == null
                        ? List.of()
                        : billableGrQueryPort.findBillableGrs(vendorId, currencyId)
        );
    }
}
