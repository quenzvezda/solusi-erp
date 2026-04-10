package com.solusi.erp.accounting.coa.application.usecase.query;

import com.solusi.erp.core.dto.LookupDto;

import java.util.List;

public interface GetCoaLookupUseCase {
    LookupDto getById(Long id);
    List<LookupDto> search(String keyword, int limit);
}
