package com.solusi.erp.inventory.container.application.usecase.query;

import com.solusi.erp.core.dto.LookupDto;
import java.util.List;

public interface GetContainerLookupUseCase {
    LookupDto getById(Long id);
    List<LookupDto> search(String keyword, int limit);
    List<LookupDto> findAll();
}
