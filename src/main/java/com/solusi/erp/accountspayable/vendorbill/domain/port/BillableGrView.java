package com.solusi.erp.accountspayable.vendorbill.domain.port;

public record BillableGrView(
        Long grId, String grCode, Long poId, String poCode, Long vendorId, Long currencyId) {}
