package com.solusi.erp.inventory.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enum for Unit of Measure Type.
 */
@Getter
@RequiredArgsConstructor
public enum UomType {
    WEIGHT("WEIGHT"),
    LENGTH("LENGTH"),
    UNIT("UNIT"),
    VOLUME("VOLUME"),
    TIME("TIME"),
    AREA("AREA");

    private final String value;
}
