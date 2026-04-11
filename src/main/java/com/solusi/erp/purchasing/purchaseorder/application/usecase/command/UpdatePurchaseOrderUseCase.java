package com.solusi.erp.purchasing.purchaseorder.application.usecase.command;

import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@FunctionalInterface
public interface UpdatePurchaseOrderUseCase {
    PurchaseOrder execute(Long id, LocalDate orderDate, LocalDate expectedDate,
                          Long facilityId, Long currencyId, BigDecimal exchangeRate,
                          int paymentTermDays, String note, List<PoLineInput> lines);
}
