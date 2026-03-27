package com.solusi.erp.inventory.uom.application.usecase.query;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.inventory.model.UomType;

import java.util.List;

public interface GetUomLookupUseCase {
    LookupDto getById(Long id);
    List<LookupDto> search(String keyword, int limit);
    List<LookupDto> findByType(UomType type);
}
