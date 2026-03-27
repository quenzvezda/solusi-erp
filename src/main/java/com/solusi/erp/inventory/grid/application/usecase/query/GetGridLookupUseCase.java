package com.solusi.erp.inventory.grid.application.usecase.query;

import com.solusi.erp.core.dto.LookupDto;
import java.util.List;

public interface GetGridLookupUseCase {
    LookupDto getById(Long id);
    List<LookupDto> search(String keyword, int limit);
}
