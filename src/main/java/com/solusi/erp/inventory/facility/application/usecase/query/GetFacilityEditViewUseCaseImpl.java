package com.solusi.erp.inventory.facility.application.usecase.query;

import com.solusi.erp.inventory.facility.domain.model.Facility;
import com.solusi.erp.inventory.facility.domain.repository.FacilityRepository;
import java.util.Optional;

public class GetFacilityEditViewUseCaseImpl implements GetFacilityEditViewUseCase {

    private final FacilityRepository repository;

    public GetFacilityEditViewUseCaseImpl(FacilityRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Facility> execute(Long id) {
        return repository.findById(id);
    }
}
