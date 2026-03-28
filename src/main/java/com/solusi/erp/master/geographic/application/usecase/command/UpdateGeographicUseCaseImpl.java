package com.solusi.erp.master.geographic.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.master.geographic.domain.model.Geographic;
import com.solusi.erp.master.geographic.domain.repository.GeographicRepository;
import com.solusi.erp.master.shared.model.GeographicType;

public class UpdateGeographicUseCaseImpl implements UpdateGeographicUseCase {

    private final GeographicRepository repository;

    public UpdateGeographicUseCaseImpl(GeographicRepository repository) {
        this.repository = repository;
    }

    @Override
    public Geographic execute(Long id, String name, GeographicType type,
                              Long parentId, String parentName, Boolean isActive) {
        Geographic existing = repository.findById(id)
                .orElseThrow(() -> new DomainException("msg.error.geographic.notfound"));
        return repository.save(new Geographic(
                existing.getMetadata(), existing.getCode(),
                name, type, parentId, parentName, isActive));
    }
}

