package com.solusi.erp.accountspayable.vendorbill.domain.port;

import java.math.BigDecimal;

public record BillableGrView(
        Long grId, String grCode, Long poId, String poCode, Long vendorId, Long currencyId, BigDecimal exchangeRate) {}
