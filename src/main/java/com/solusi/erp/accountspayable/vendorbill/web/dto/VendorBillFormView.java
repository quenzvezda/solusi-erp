package com.solusi.erp.accountspayable.vendorbill.web.dto;

import com.solusi.erp.accountspayable.vendorbill.domain.port.BillableGrView;

import java.util.List;

public record VendorBillFormView(
        VendorBillSaveRequest request,
        String vendorName,
        String currencyCode,
        boolean exchangeRateRequired,
        List<BillableGrView> billableGrs
) {
}
