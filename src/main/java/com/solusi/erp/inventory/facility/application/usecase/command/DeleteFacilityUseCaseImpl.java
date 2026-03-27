package com.solusi.erp.inventory.facility.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.facility.domain.repository.FacilityRepository;

public class DeleteFacilityUseCaseImpl implements DeleteFacilityUseCase {

    private final FacilityRepository repository;

    public DeleteFacilityUseCaseImpl(FacilityRepository repository) {
        this.repository = repository;
    }

    @Override
    public void execute(Long id) {
        repository.findById(id).orElseThrow(() -> new DomainException("msg.error.facility.notfound"));
        repository.delete(id);
    }
}
