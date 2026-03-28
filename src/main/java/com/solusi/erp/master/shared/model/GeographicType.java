package com.solusi.erp.master.shared.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enum for Geographic Types.
 */
@Getter
@RequiredArgsConstructor
public enum GeographicType {
    COUNTRY("COUNTRY"),
    STATE_PROVINCE("STATE_PROVINCE"),
    CITY_MUNICIPALITY("CITY_MUNICIPALITY");

    private final String value;
}

