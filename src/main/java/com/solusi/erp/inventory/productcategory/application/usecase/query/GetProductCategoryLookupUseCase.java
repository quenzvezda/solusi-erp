package com.solusi.erp.inventory.productcategory.application.usecase.query;

import com.solusi.erp.core.dto.LookupDto;
import java.util.List;

public interface GetProductCategoryLookupUseCase {
    LookupDto getById(Long id);
    List<LookupDto> search(String keyword, int limit);
}
