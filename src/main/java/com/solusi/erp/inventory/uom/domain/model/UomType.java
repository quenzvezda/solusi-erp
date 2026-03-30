package com.solusi.erp.inventory.uom.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

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
