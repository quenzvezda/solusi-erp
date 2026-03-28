package com.solusi.erp.master.model;

import com.solusi.erp.core.model.BaseModel;
import com.solusi.erp.master.shared.model.AddressType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/**
 * Address linked to a Party.
 * Refactored to use Geographic entity and multi-purpose types.
 */
@Entity
@Table(name = "party_addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PartyAddress extends BaseModel {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "party_id", nullable = false)
    private Party party;

    @ElementCollection(targetClass = AddressType.class)
    @CollectionTable(name = "party_address_types", joinColumns = @JoinColumn(name = "party_address_id"))
    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private Set<AddressType> types = new HashSet<>();

    @Column(name = "address_line1", columnDefinition = "TEXT", nullable = false)
    private String addressLine1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id")
    private Geographic city;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;
}
