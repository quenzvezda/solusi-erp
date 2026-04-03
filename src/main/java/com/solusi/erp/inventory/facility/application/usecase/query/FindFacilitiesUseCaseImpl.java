package com.solusi.erp.inventory.facility.application.usecase.query;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.inventory.facility.domain.model.Facility;
import com.solusi.erp.inventory.facility.domain.repository.FacilityRepository;

public class FindFacilitiesUseCaseImpl implements FindFacilitiesUseCase {

    private final FacilityRepository repository;

    public FindFacilitiesUseCaseImpl(FacilityRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<Facility> execute(String keyword, Pageable pageable) {
        return repository.findAll(keyword, pageable);
    }
}
