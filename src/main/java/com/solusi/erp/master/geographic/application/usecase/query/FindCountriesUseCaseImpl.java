package com.solusi.erp.master.geographic.application.usecase.query;

import com.solusi.erp.master.geographic.domain.model.Geographic;
import com.solusi.erp.master.geographic.domain.repository.GeographicRepository;

import java.util.List;

public class FindCountriesUseCaseImpl implements FindCountriesUseCase {

    private final GeographicRepository repository;

    public FindCountriesUseCaseImpl(GeographicRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Geographic> execute(String keyword, int limit) {
        return repository.findCountries(keyword, limit);
    }
}
