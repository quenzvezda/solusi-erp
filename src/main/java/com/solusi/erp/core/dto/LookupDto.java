package com.solusi.erp.core.dto;

import java.util.Map;

/**
 * Standardized Lookup DTO for autocomplete and dropdowns.
 * Includes a generic payload for supplementary metadata.
 */
public record LookupDto(
        Long id,
        String name,
        String subText,
        Map<String, Object> payload) {

    /**
     * Backward compatibility constructor.
     */
    public LookupDto(Long id, String name, String subText) {
        this(id, name, subText, null);
    }
}
