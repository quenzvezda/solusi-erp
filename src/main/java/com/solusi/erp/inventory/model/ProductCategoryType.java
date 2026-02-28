package com.solusi.erp.inventory.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enum for Product Category Type.
 */
@Getter
@RequiredArgsConstructor
public enum ProductCategoryType {
    STOCK("STOCK"),
    NON_STOCK("NON_STOCK"),
    SERVICE("SERVICE");

    private final String value;
}
