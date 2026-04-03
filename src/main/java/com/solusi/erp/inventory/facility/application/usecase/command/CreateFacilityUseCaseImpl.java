package com.solusi.erp.inventory.facility.application.usecase.command;

import com.solusi.erp.core.infrastructure.sequence.SequenceGeneratorService;
import com.solusi.erp.inventory.facility.domain.model.Facility;
import com.solusi.erp.inventory.facility.domain.repository.FacilityRepository;

public class CreateFacilityUseCaseImpl implements CreateFacilityUseCase {

    private final FacilityRepository repository;
    private final SequenceGeneratorService sequenceGeneratorService;

    public CreateFacilityUseCaseImpl(FacilityRepository repository, SequenceGeneratorService sequenceGeneratorService) {
        this.repository = repository;
        this.sequenceGeneratorService = sequenceGeneratorService;
    }

    @Override
    public Facility execute(String name, Long ownerId, String addressLine1, Long cityId, String postalCode, String note, Boolean isActive) {
        String code = sequenceGeneratorService.generate("FACILITY");
        Facility facility = Facility.createNew(name, ownerId, addressLine1, cityId, postalCode, note, isActive);
        facility.assignCode(code);
        return repository.save(facility);
    }
}
