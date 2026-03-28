package com.solusi.erp.master.partyroletype.application.usecase.query;

import com.solusi.erp.master.partyroletype.domain.model.PartyRoleType;
import com.solusi.erp.master.partyroletype.domain.repository.PartyRoleTypeRepository;

import java.util.Optional;

public class GetPartyRoleTypeEditViewUseCaseImpl implements GetPartyRoleTypeEditViewUseCase {

    private final PartyRoleTypeRepository repository;

    public GetPartyRoleTypeEditViewUseCaseImpl(PartyRoleTypeRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<PartyRoleType> execute(Long id) {
        return repository.findById(id);
    }
}
