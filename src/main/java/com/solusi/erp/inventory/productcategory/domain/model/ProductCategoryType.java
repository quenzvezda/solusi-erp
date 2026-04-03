package com.solusi.erp.inventory.productcategory.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductCategoryType {
    STOCK("STOCK"),
    NON_STOCK("NON_STOCK"),
    SERVICE("SERVICE");

    private final String value;
}
