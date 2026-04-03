package com.solusi.erp.master.party.infrastructure.adapter;

import com.solusi.erp.master.party.domain.port.PartyInUseChecker;
import com.solusi.erp.master.bankaccount.infrastructure.persistence.BankAccountJpaRepository;

public class PartyInUseCheckerImpl implements PartyInUseChecker {

    private final BankAccountJpaRepository bankAccountJpaRepository;

    public PartyInUseCheckerImpl(BankAccountJpaRepository bankAccountJpaRepository) {
        this.bankAccountJpaRepository = bankAccountJpaRepository;
    }

    @Override
    public boolean isInUse(Long partyId) {
        return bankAccountJpaRepository.existsByPartyId(partyId);
    }
}
