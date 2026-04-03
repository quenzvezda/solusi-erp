package com.solusi.erp.master.geographic.application.usecase.query;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.master.geographic.domain.model.Geographic;
import com.solusi.erp.master.geographic.domain.repository.GeographicRepository;

public class FindGeographicsUseCaseImpl implements FindGeographicsUseCase {

    private final GeographicRepository repository;

    public FindGeographicsUseCaseImpl(GeographicRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<Geographic> execute(String keyword, Long parentId, Pageable pageable) {
        return repository.findAll(keyword, parentId, pageable);
    }
}
