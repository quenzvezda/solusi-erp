package com.solusi.erp.master.model;

import com.solusi.erp.core.model.BaseModel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Address linked to a Party.
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AddressType type = AddressType.MAIN;

    @Column(name = "address_line1", columnDefinition = "TEXT", nullable = false)
    private String addressLine1;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String province;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(length = 100)
    private String country = "Indonesia";

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;
}
