package com.solusi.erp.purchasing.supplierpricelist.application.usecase.command;

import com.solusi.erp.purchasing.supplierpricelist.domain.model.SupplierPriceList;

import java.math.BigDecimal;
import java.time.LocalDate;

@FunctionalInterface
public interface UpdateSupplierPriceListUseCase {
    SupplierPriceList execute(Long id, Long productId, Long uomId, Long currencyId,
                              BigDecimal unitPrice, BigDecimal minQuantity,
                              LocalDate effectiveFrom, LocalDate effectiveTo,
                              String note, boolean active);
}
