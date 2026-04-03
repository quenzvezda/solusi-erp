package com.solusi.erp.master.party.infrastructure.persistence;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.master.shared.model.AddressType;
import com.solusi.erp.master.partyroletype.infrastructure.persistence.PartyRoleType;
import com.solusi.erp.master.party.domain.model.Party;
import com.solusi.erp.master.party.domain.model.PartyAddressData;
import com.solusi.erp.master.party.domain.model.PartyContactData;
import com.solusi.erp.master.party.domain.model.PartyIdentificationData;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class PartyPersistenceMapper {

    public Party toDomain(com.solusi.erp.master.party.infrastructure.persistence.Party e) {
        if (e == null) return null;

        Long versionLong = e.getVersion() != null ? e.getVersion().longValue() : null;
        AuditMetadata metadata = new AuditMetadata(e.getId(), versionLong, e.getCreatedDate(),
                e.getCreatedBy(), e.getUpdatedDate(), e.getUpdatedBy());

        Set<Long> roleIds = e.getRoles() != null
                ? e.getRoles().stream().map(PartyRoleType::getId).collect(Collectors.toSet())
                : new HashSet<>();
        List<String> roleNames = e.getRoles() != null
                ? e.getRoles().stream().map(PartyRoleType::getName).collect(Collectors.toList())
                : new ArrayList<>();

        List<PartyContactData> contacts = e.getContacts() != null
                ? e.getContacts().stream().map(this::toContactData).collect(Collectors.toList())
                : new ArrayList<>();
        List<PartyAddressData> addresses = e.getAddresses() != null
                ? e.getAddresses().stream().map(this::toAddressData).collect(Collectors.toList())
                : new ArrayList<>();
        List<PartyIdentificationData> identifications = e.getIdentifications() != null
                ? e.getIdentifications().stream().map(this::toIdentificationData).collect(Collectors.toList())
                : new ArrayList<>();

        return new Party(metadata, e.getCode(), e.getSalutation(), e.getName(), e.getType(),
                e.getNotes(), e.getIsActive(), e.getEmail(), e.getPhone(),
                roleIds, roleNames, contacts, addresses, identifications);
    }

    private PartyContactData toContactData(com.solusi.erp.master.party.infrastructure.persistence.PartyContact c) {
        if (c == null) return null;
        return new PartyContactData(c.getId(), c.getLabel(), c.getMobile(), c.getPhone(), c.getEmail(),
                c.getIsActive(), c.getIsDefault());
    }

    private PartyAddressData toAddressData(com.solusi.erp.master.party.infrastructure.persistence.PartyAddress a) {
        if (a == null) return null;
        Long cityId = a.getCity() != null ? a.getCity().getId() : null;
        Set<AddressType> types = a.getTypes() != null ? new HashSet<>(a.getTypes()) : new HashSet<>();
        return new PartyAddressData(a.getId(), types, a.getAddressLine1(), cityId, a.getPostalCode(),
                a.getIsActive(), a.getIsDefault());
    }

    private PartyIdentificationData toIdentificationData(com.solusi.erp.master.party.infrastructure.persistence.PartyIdentification i) {
        if (i == null) return null;
        Long typeId = i.getType() != null ? i.getType().getId() : null;
        return new PartyIdentificationData(i.getId(), typeId, i.getIdNumber(), i.getIssuedDate(),
                i.getExpiryDate(), i.getIsActive(), i.getIsDefault());
    }
}

