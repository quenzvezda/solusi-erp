package com.solusi.erp.master.geographic.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.master.geographic.domain.model.Geographic;
import com.solusi.erp.master.geographic.domain.repository.GeographicRepository;
import com.solusi.erp.master.model.GeographicType;

public class CreateGeographicUseCaseImpl implements CreateGeographicUseCase {

    private final GeographicRepository repository;

    public CreateGeographicUseCaseImpl(GeographicRepository repository) {
        this.repository = repository;
    }

    @Override
    public Geographic execute(String code, String name, GeographicType type,
                              Long parentId, String parentName, Boolean isActive) {
        if (repository.existsByCode(code)) {
            throw new DomainException("msg.error.common.duplicate");
        }
        Geographic geographic = Geographic.createNew(code, name, type, parentId, parentName, isActive);
        return repository.save(geographic);
    }
}
