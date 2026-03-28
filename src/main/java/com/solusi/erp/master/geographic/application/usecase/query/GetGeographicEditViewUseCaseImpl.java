package com.solusi.erp.master.geographic.application.usecase.query;

import com.solusi.erp.master.geographic.domain.model.Geographic;
import com.solusi.erp.master.geographic.domain.repository.GeographicRepository;

import java.util.Optional;

public class GetGeographicEditViewUseCaseImpl implements GetGeographicEditViewUseCase {

    private final GeographicRepository repository;

    public GetGeographicEditViewUseCaseImpl(GeographicRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Geographic> execute(Long id) {
        return repository.findById(id);
    }
}
