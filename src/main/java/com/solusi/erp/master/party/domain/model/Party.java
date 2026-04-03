package com.solusi.erp.master.party.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.master.shared.model.PartyType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Party {

    private final AuditMetadata metadata;
    private final String code;
    private final String salutation;
    private final String name;
    private final PartyType type;
    private final String notes;
    private final Boolean isActive;
    private final String email;
    private final String phone;
    private final Set<Long> roleIds;
    private final List<String> roleNames;
    private final List<PartyContactData> contacts;
    private final List<PartyAddressData> addresses;
    private final List<PartyIdentificationData> identifications;

    public Party(AuditMetadata metadata, String code, String salutation, String name, PartyType type,
                 String notes, Boolean isActive, String email, String phone,
                 Set<Long> roleIds, List<String> roleNames,
                 List<PartyContactData> contacts, List<PartyAddressData> addresses,
                 List<PartyIdentificationData> identifications) {
        this.metadata = metadata;
        this.code = code;
        this.salutation = salutation;
        this.name = name;
        this.type = type;
        this.notes = notes;
        this.isActive = isActive;
        this.email = email;
        this.phone = phone;
        this.roleIds = roleIds;
        this.roleNames = roleNames;
        this.contacts = contacts;
        this.addresses = addresses;
        this.identifications = identifications;
    }

    public static Party createNew(String code, String name, String salutation, PartyType type,
                                  String notes, Boolean isActive, String email, String phone,
                                  List<PartyContactData> contacts, List<PartyAddressData> addresses,
                                  List<PartyIdentificationData> identifications, Set<Long> roleIds) {
        return new Party(
                AuditMetadata.empty(),
                code, salutation, name, type, notes, isActive, email, phone,
                roleIds != null ? roleIds : new HashSet<>(),
                new ArrayList<>(),
                contacts != null ? contacts : new ArrayList<>(),
                addresses != null ? addresses : new ArrayList<>(),
                identifications != null ? identifications : new ArrayList<>()
        );
    }

    public Long getId() { return metadata.id(); }
    public AuditMetadata getMetadata() { return metadata; }
    public String getCode() { return code; }
    public String getSalutation() { return salutation; }
    public String getName() { return name; }
    public PartyType getType() { return type; }
    public String getNotes() { return notes; }
    public Boolean getIsActive() { return isActive; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public Set<Long> getRoleIds() { return roleIds; }
    public List<String> getRoleNames() { return roleNames; }
    public List<PartyContactData> getContacts() { return contacts; }
    public List<PartyAddressData> getAddresses() { return addresses; }
    public List<PartyIdentificationData> getIdentifications() { return identifications; }
}

