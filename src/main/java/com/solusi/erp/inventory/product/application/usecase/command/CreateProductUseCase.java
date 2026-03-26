package com.solusi.erp.inventory.product.application.usecase.command;

import com.solusi.erp.inventory.product.domain.model.Product;
import java.math.BigDecimal;

/**
 * Command Use Case to create a new product.
 */
public interface CreateProductUseCase {
    Product execute(
        String name,
        String barcode,
        String note,
        Long categoryId,
        Long uomId,
        Long brandId,
        String hscode,
        boolean isActive,
        boolean isSerialized,
        BigDecimal minStock,
        BigDecimal maxStock,
        BigDecimal weightNet,
        BigDecimal weightGross,
        Long weightUomId,
        BigDecimal length,
        BigDecimal width,
        BigDecimal height,
        Long dimensionUomId
    );
}
