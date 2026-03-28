package com.solusi.erp.master.partyroletype.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.master.partyroletype.domain.model.PartyRoleType;
import com.solusi.erp.master.partyroletype.domain.repository.PartyRoleTypeRepository;

public class UpdatePartyRoleTypeUseCaseImpl implements UpdatePartyRoleTypeUseCase {

    private final PartyRoleTypeRepository repository;

    public UpdatePartyRoleTypeUseCaseImpl(PartyRoleTypeRepository repository) {
        this.repository = repository;
    }

    @Override
    public PartyRoleType execute(Long id, String name, String note, Boolean isActive) {
        PartyRoleType partyRoleType = repository.findById(id)
                .orElseThrow(() -> new DomainException("msg.error.party-role-type.notfound"));
        partyRoleType.update(name, note, isActive);
        return repository.save(partyRoleType);
    }
}
