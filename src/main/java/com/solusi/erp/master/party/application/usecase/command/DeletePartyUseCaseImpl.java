package com.solusi.erp.master.party.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.master.party.domain.repository.PartyRepository;

public class DeletePartyUseCaseImpl implements DeletePartyUseCase {

    private final PartyRepository repository;

    public DeletePartyUseCaseImpl(PartyRepository repository) {
        this.repository = repository;
    }

    @Override
    public void execute(Long id) {
        repository.findById(id).orElseThrow(() -> new DomainException("msg.error.party.notfound"));
        repository.delete(id);
    }
}
