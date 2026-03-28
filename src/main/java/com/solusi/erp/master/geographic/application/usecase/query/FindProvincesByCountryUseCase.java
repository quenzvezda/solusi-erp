package com.solusi.erp.master.geographic.application.usecase.query;

import com.solusi.erp.master.geographic.domain.model.Geographic;

import java.util.List;

@FunctionalInterface
public interface FindProvincesByCountryUseCase {
    List<Geographic> execute(Long countryId, String keyword, int limit);
}
