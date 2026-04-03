package com.solusi.erp.master.party.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.master.shared.model.PartyType;
import com.solusi.erp.master.party.domain.model.Party;
import com.solusi.erp.master.party.domain.model.PartyAddressData;
import com.solusi.erp.master.party.domain.model.PartyContactData;
import com.solusi.erp.master.party.domain.model.PartyIdentificationData;
import com.solusi.erp.master.party.domain.repository.PartyRepository;

import java.util.List;
import java.util.Set;

public class UpdatePartyUseCaseImpl implements UpdatePartyUseCase {

    private final PartyRepository repository;

    public UpdatePartyUseCaseImpl(PartyRepository repository) {
        this.repository = repository;
    }

    @Override
    public Party execute(Long id, String name, String salutation, PartyType type, String notes,
                         Boolean isActive, String email, String phone,
                         List<PartyContactData> contacts, List<PartyAddressData> addresses,
                         List<PartyIdentificationData> identifications, Set<Long> roleIds) {
        Party existing = repository.findById(id)
                .orElseThrow(() -> new DomainException("msg.error.party.notfound"));
        Party updated = new Party(existing.getMetadata(), existing.getCode(), salutation, name, type,
                notes, isActive, email, phone, roleIds, existing.getRoleNames(), contacts, addresses, identifications);
        return repository.save(updated);
    }
}

