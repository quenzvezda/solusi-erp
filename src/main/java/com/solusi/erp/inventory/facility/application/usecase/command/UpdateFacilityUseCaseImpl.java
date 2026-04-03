package com.solusi.erp.inventory.facility.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.facility.domain.model.Facility;
import com.solusi.erp.inventory.facility.domain.repository.FacilityRepository;

public class UpdateFacilityUseCaseImpl implements UpdateFacilityUseCase {

    private final FacilityRepository repository;

    public UpdateFacilityUseCaseImpl(FacilityRepository repository) {
        this.repository = repository;
    }

    @Override
    public Facility execute(Long id, String name, Long ownerId, String addressLine1, Long cityId, String postalCode, String note, Boolean isActive) {
        Facility facility = repository.findById(id)
            .orElseThrow(() -> new DomainException("msg.error.facility.notfound"));
        facility.update(name, ownerId, addressLine1, cityId, postalCode, note, isActive);
        return repository.save(facility);
    }
}
