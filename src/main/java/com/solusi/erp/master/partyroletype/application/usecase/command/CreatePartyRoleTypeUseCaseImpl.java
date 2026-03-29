package com.solusi.erp.master.partyroletype.application.usecase.command;

import com.solusi.erp.core.infrastructure.sequence.SequenceGeneratorService;
import com.solusi.erp.master.partyroletype.domain.model.PartyRoleType;
import com.solusi.erp.master.partyroletype.domain.repository.PartyRoleTypeRepository;

public class CreatePartyRoleTypeUseCaseImpl implements CreatePartyRoleTypeUseCase {

    private final PartyRoleTypeRepository repository;
    private final SequenceGeneratorService sequenceGeneratorService;

    public CreatePartyRoleTypeUseCaseImpl(PartyRoleTypeRepository repository,
                                          SequenceGeneratorService sequenceGeneratorService) {
        this.repository = repository;
        this.sequenceGeneratorService = sequenceGeneratorService;
    }

    @Override
    public PartyRoleType execute(String name, String note, Boolean isActive) {
        String code = sequenceGeneratorService.generate("PARTY-ROLE-TYPE");
        PartyRoleType partyRoleType = PartyRoleType.createNew(code, name, note, isActive);
        return repository.save(partyRoleType);
    }
}
