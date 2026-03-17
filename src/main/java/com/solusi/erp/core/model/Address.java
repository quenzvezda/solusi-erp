package com.solusi.erp.core.model;

import com.solusi.erp.master.model.Geographic;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id")
    private Geographic city;

    @Column(name = "postal_code", length = 20)
    private String postalCode;
}
