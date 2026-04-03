package com.solusi.erp.master.geographic.application.usecase.query;

import com.solusi.erp.master.geographic.domain.model.Geographic;

import java.util.List;

@FunctionalInterface
public interface FindCitiesByProvinceUseCase {
    List<Geographic> execute(Long provinceId, String keyword, int limit);
}
