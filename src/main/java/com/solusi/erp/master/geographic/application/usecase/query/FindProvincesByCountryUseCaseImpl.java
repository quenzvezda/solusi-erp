package com.solusi.erp.master.geographic.application.usecase.query;

import com.solusi.erp.master.geographic.domain.model.Geographic;
import com.solusi.erp.master.geographic.domain.repository.GeographicRepository;

import java.util.List;

public class FindProvincesByCountryUseCaseImpl implements FindProvincesByCountryUseCase {

    private final GeographicRepository repository;

    public FindProvincesByCountryUseCaseImpl(GeographicRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Geographic> execute(Long countryId, String keyword, int limit) {
        return repository.findProvincesByCountry(countryId, keyword, limit);
    }
}
