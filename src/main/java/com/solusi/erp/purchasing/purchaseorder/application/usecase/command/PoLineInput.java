package com.solusi.erp.purchasing.purchaseorder.application.usecase.command;

import java.math.BigDecimal;

public record PoLineInput(Long productId, BigDecimal quantity, Long uomId,
                           BigDecimal unitPrice, BigDecimal taxRate,
                           Long prLineId, String note) {}
