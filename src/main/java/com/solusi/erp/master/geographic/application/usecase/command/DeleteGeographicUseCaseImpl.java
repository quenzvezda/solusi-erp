package com.solusi.erp.master.geographic.application.usecase.command;

import com.solusi.erp.core.domain.model.DeleteResult;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.master.geographic.domain.model.Geographic;
import com.solusi.erp.master.geographic.domain.port.GeographicInUseChecker;
import com.solusi.erp.master.geographic.domain.repository.GeographicRepository;

public class DeleteGeographicUseCaseImpl implements DeleteGeographicUseCase {

    private final GeographicRepository repository;
    private final GeographicInUseChecker inUseChecker;

    public DeleteGeographicUseCaseImpl(GeographicRepository repository, GeographicInUseChecker inUseChecker) {
        this.repository = repository;
        this.inUseChecker = inUseChecker;
    }

    @Override
    public DeleteResult execute(Long id) {
        Geographic geographic = repository.findById(id)
                .orElseThrow(() -> new DomainException("msg.error.geographic.notfound"));

        if (inUseChecker.isInUse(id)) {
            geographic.softDelete();
            repository.save(geographic);
            return DeleteResult.SOFT_DELETED;
        }

        repository.delete(id);
        return DeleteResult.HARD_DELETED;
    }
}
