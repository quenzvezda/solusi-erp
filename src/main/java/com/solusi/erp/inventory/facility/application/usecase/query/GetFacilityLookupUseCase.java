package com.solusi.erp.inventory.facility.application.usecase.query;

import com.solusi.erp.core.dto.LookupDto;
import java.util.List;

public interface GetFacilityLookupUseCase {
    LookupDto getById(Long id);
    List<LookupDto> search(String keyword, int limit);
}
