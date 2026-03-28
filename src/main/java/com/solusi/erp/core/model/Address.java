package com.solusi.erp.core.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Reusable Address component.
 * Mandate: Consistent with PartyAddress pattern.
 */
@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    @Column(name = "address_line1", columnDefinition = "TEXT")
    private String addressLine1;

    @Column(name = "city_id")
    private Long cityId;

    @Column(name = "postal_code", length = 20)
    private String postalCode;
}
