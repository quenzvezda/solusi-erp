package com.solusi.erp.purchasing.purchaseorder.application.usecase.command;

import com.solusi.erp.master.tax.domain.model.TaxCalculationMode;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrder;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrderType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@FunctionalInterface
public interface CreatePurchaseOrderUseCase {
    PurchaseOrder execute(LocalDate orderDate, LocalDate expectedDate,
                          Long supplierId, Long facilityId, Long currencyId,
                          BigDecimal exchangeRate, int paymentTermDays,
                          Long prId, PurchaseOrderType poType,
                          Long taxId, String taxCode, String taxName, BigDecimal taxRate,
                          TaxCalculationMode taxCalculationMode,
                          String note, List<PoLineInput> lines);

    default PurchaseOrder execute(LocalDate orderDate, LocalDate expectedDate,
                                  Long supplierId, Long facilityId, Long currencyId,
                                  BigDecimal exchangeRate, int paymentTermDays,
                                  Long prId, PurchaseOrderType poType, String note, List<PoLineInput> lines) {
        return execute(orderDate, expectedDate, supplierId, facilityId, currencyId,
                exchangeRate, paymentTermDays, prId, poType,
                null, null, null, BigDecimal.ZERO, TaxCalculationMode.EXCLUSIVE,
                note, lines);
    }
}
