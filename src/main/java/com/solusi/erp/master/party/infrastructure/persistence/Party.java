package com.solusi.erp.master.party.infrastructure.persistence;

import com.solusi.erp.core.model.BaseModel;
import com.solusi.erp.master.partyroletype.infrastructure.persistence.PartyRoleType;
import com.solusi.erp.master.shared.model.PartyType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Party Entity (Business Partner).
 */
@Entity
@Table(name = "parties")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Party extends BaseModel {

    @Column(length = 50)
    private String salutation;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 255)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PartyType type = PartyType.PERSON;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(length = 100)
    private String email;

    @Column(length = 50)
    private String phone;

    @ManyToMany
    @JoinTable(name = "party_roles", joinColumns = @JoinColumn(name = "party_id"), inverseJoinColumns = @JoinColumn(name = "role_type_id"))
    private Set<PartyRoleType> roles = new HashSet<>();

    @OneToMany(mappedBy = "party", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<PartyIdentification> identifications = new ArrayList<>();

    @OneToMany(mappedBy = "party", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<PartyAddress> addresses = new ArrayList<>();

    @OneToMany(mappedBy = "party", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<PartyContact> contacts = new ArrayList<>();

    // Helper methods to maintain bidirectional relationships
    public void addIdentification(PartyIdentification identification) {
        identifications.add(identification);
        identification.setParty(this);
    }

    public void addAddress(PartyAddress address) {
        addresses.add(address);
        address.setParty(this);
    }

    public void addContact(PartyContact contact) {
        contacts.add(contact);
        contact.setParty(this);
    }
}
