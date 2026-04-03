package com.solusi.erp.inventory.facility.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.facility.domain.repository.FacilityRepository;
import com.solusi.erp.inventory.facility.infrastructure.adapter.FacilityInUseCheckerComposite;

public class DeleteFacilityUseCaseImpl implements DeleteFacilityUseCase {

    private final FacilityRepository repository;
    private final FacilityInUseCheckerComposite inUseChecker;

    public DeleteFacilityUseCaseImpl(FacilityRepository repository,
                                     FacilityInUseCheckerComposite inUseChecker) {
        this.repository = repository;
        this.inUseChecker = inUseChecker;
    }

    @Override
    public void execute(Long id) {
        repository.findById(id).orElseThrow(() -> new DomainException("msg.error.facility.notfound"));
        if (inUseChecker.isInUse(id)) {
            throw new DomainException("msg.error.facility.in-use");
        }
        repository.delete(id);
    }
}
