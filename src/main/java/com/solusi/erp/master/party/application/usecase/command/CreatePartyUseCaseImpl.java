package com.solusi.erp.master.party.application.usecase.command;

import com.solusi.erp.core.service.SequenceGeneratorService;
import com.solusi.erp.master.shared.model.PartyType;
import com.solusi.erp.master.party.domain.model.Party;
import com.solusi.erp.master.party.domain.model.PartyAddressData;
import com.solusi.erp.master.party.domain.model.PartyContactData;
import com.solusi.erp.master.party.domain.model.PartyIdentificationData;
import com.solusi.erp.master.party.domain.repository.PartyRepository;

import java.util.List;
import java.util.Set;

public class CreatePartyUseCaseImpl implements CreatePartyUseCase {

    private final PartyRepository repository;
    private final SequenceGeneratorService sequenceGeneratorService;

    public CreatePartyUseCaseImpl(PartyRepository repository, SequenceGeneratorService sequenceGeneratorService) {
        this.repository = repository;
        this.sequenceGeneratorService = sequenceGeneratorService;
    }

    @Override
    public Party execute(String name, String salutation, PartyType type, String notes, Boolean isActive,
                         String email, String phone,
                         List<PartyContactData> contacts, List<PartyAddressData> addresses,
                         List<PartyIdentificationData> identifications, Set<Long> roleIds) {
        String code = sequenceGeneratorService.generate("PARTY");
        Party party = Party.createNew(code, name, salutation, type, notes, isActive, email, phone,
                contacts, addresses, identifications, roleIds);
        return repository.save(party);
    }
}

