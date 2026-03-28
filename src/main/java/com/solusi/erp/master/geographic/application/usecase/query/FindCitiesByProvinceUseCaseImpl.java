package com.solusi.erp.master.geographic.application.usecase.query;

import com.solusi.erp.master.geographic.domain.model.Geographic;
import com.solusi.erp.master.geographic.domain.repository.GeographicRepository;

import java.util.List;

public class FindCitiesByProvinceUseCaseImpl implements FindCitiesByProvinceUseCase {

    private final GeographicRepository repository;

    public FindCitiesByProvinceUseCaseImpl(GeographicRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Geographic> execute(Long provinceId, String keyword, int limit) {
        return repository.findCitiesByProvince(provinceId, keyword, limit);
    }
}
