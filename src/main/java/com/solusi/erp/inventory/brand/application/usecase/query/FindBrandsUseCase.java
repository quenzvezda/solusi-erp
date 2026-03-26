package com.solusi.erp.inventory.brand.application.usecase.query;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.inventory.brand.domain.model.Brand;

@FunctionalInterface
public interface FindBrandsUseCase {
    Page<Brand> execute(String keyword, Pageable pageable);
}
