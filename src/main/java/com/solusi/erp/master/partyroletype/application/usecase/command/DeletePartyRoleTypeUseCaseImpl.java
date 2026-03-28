package com.solusi.erp.master.partyroletype.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.master.partyroletype.domain.model.PartyRoleType;
import com.solusi.erp.master.partyroletype.domain.repository.PartyRoleTypeRepository;

public class DeletePartyRoleTypeUseCaseImpl implements DeletePartyRoleTypeUseCase {

    private final PartyRoleTypeRepository repository;

    public DeletePartyRoleTypeUseCaseImpl(PartyRoleTypeRepository repository) {
        this.repository = repository;
    }

    @Override
    public void execute(Long id) {
        PartyRoleType partyRoleType = repository.findById(id)
                .orElseThrow(() -> new DomainException("msg.error.party-role-type.notfound"));
        partyRoleType.softDelete();
        repository.save(partyRoleType);
    }
}
