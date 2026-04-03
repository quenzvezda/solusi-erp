package com.solusi.erp.master.party.application.usecase.command;

import com.solusi.erp.core.domain.model.DeleteResult;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.master.party.domain.port.PartyInUseChecker;
import com.solusi.erp.master.party.domain.repository.PartyRepository;

public class DeletePartyUseCaseImpl implements DeletePartyUseCase {

    private final PartyRepository repository;
    private final PartyInUseChecker inUseChecker;

    public DeletePartyUseCaseImpl(PartyRepository repository, PartyInUseChecker inUseChecker) {
        this.repository = repository;
        this.inUseChecker = inUseChecker;
    }

    @Override
    public DeleteResult execute(Long id) {
        repository.findById(id)
                .orElseThrow(() -> new DomainException("msg.error.party.notfound"));

        if (inUseChecker.isInUse(id)) {
            repository.softDelete(id);
            return DeleteResult.SOFT_DELETED;
        }

        repository.delete(id);
        return DeleteResult.HARD_DELETED;
    }
}
