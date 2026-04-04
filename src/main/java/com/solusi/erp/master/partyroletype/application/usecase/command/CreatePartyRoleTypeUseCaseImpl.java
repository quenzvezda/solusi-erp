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
    public PartyRoleType execute(String code, String name, String note, Boolean isActive) {
        String finalCode = (code != null && !code.isBlank()) ? code : sequenceGeneratorService.generate("PARTY-ROLE-TYPE");
        PartyRoleType partyRoleType = PartyRoleType.createNew(finalCode, name, note, isActive);
        return repository.save(partyRoleType);
    }
}
