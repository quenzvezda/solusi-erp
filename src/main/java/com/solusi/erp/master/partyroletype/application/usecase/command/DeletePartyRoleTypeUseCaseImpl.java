package com.solusi.erp.master.partyroletype.application.usecase.command;

import com.solusi.erp.core.domain.model.DeleteResult;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.master.partyroletype.domain.model.PartyRoleType;
import com.solusi.erp.master.partyroletype.domain.port.PartyRoleTypeInUseChecker;
import com.solusi.erp.master.partyroletype.domain.repository.PartyRoleTypeRepository;

public class DeletePartyRoleTypeUseCaseImpl implements DeletePartyRoleTypeUseCase {

    private final PartyRoleTypeRepository repository;
    private final PartyRoleTypeInUseChecker inUseChecker;

    public DeletePartyRoleTypeUseCaseImpl(PartyRoleTypeRepository repository,
                                           PartyRoleTypeInUseChecker inUseChecker) {
        this.repository = repository;
        this.inUseChecker = inUseChecker;
    }

    @Override
    public DeleteResult execute(Long id) {
        PartyRoleType partyRoleType = repository.findById(id)
                .orElseThrow(() -> new DomainException("msg.error.party-role-type.notfound"));

        if (inUseChecker.isInUse(id)) {
            partyRoleType.softDelete();
            repository.save(partyRoleType);
            return DeleteResult.SOFT_DELETED;
        }

        repository.delete(id);
        return DeleteResult.HARD_DELETED;
    }
}
