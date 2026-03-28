package com.solusi.erp.master.party.application.usecase.command;

import com.solusi.erp.master.model.PartyType;
import com.solusi.erp.master.party.domain.model.Party;
import com.solusi.erp.master.party.domain.model.PartyAddressData;
import com.solusi.erp.master.party.domain.model.PartyContactData;
import com.solusi.erp.master.party.domain.model.PartyIdentificationData;

import java.util.List;
import java.util.Set;

@FunctionalInterface
public interface UpdatePartyUseCase {
    Party execute(Long id, String name, String salutation, PartyType type, String notes, Boolean isActive,
                  String email, String phone,
                  List<PartyContactData> contacts, List<PartyAddressData> addresses,
                  List<PartyIdentificationData> identifications, Set<Long> roleIds);
}
