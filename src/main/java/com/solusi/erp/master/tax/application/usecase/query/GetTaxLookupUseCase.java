package com.solusi.erp.master.tax.application.usecase.query;

import com.solusi.erp.core.dto.LookupDto;

import java.util.List;

public interface GetTaxLookupUseCase {
    List<LookupDto> search(String q, int limit);
    LookupDto getById(Long id);
}
