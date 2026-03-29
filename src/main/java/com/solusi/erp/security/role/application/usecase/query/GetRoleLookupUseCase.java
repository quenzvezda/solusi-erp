package com.solusi.erp.security.role.application.usecase.query;

import com.solusi.erp.core.dto.LookupDto;
import java.util.List;

public interface GetRoleLookupUseCase {
    LookupDto getById(Long id);
    List<LookupDto> search(String keyword, int limit);
}
