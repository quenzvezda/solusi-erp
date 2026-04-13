package com.solusi.erp.purchasing.purchaserequisition.application.usecase.command;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LineInput(Long productId, BigDecimal quantity, Long uomId,
                        LocalDate requiredDate, BigDecimal estimatedUnitPrice,
                        String note) {
}
