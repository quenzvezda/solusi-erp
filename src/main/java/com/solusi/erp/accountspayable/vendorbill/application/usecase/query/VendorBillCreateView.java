package com.solusi.erp.accountspayable.vendorbill.application.usecase.query;

import com.solusi.erp.accountspayable.vendorbill.domain.port.BillableGrView;

import java.util.List;

public record VendorBillCreateView(
        Long vendorId,
        Long currencyId,
        List<BillableGrView> billableGrs
) {
}
