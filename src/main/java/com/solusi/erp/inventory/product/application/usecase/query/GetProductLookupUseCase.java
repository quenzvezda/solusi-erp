package com.solusi.erp.inventory.product.application.usecase.query;

import com.solusi.erp.core.dto.LookupDto;
import java.util.List;

/**
 * Use Case to get product lookup data.
 */
public interface GetProductLookupUseCase {
    LookupDto getById(Long id);
    List<LookupDto> search(String keyword, int limit);
}
