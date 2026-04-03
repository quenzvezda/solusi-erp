package com.solusi.erp.inventory.brand.application.usecase.query;

import com.solusi.erp.inventory.brand.domain.model.Brand;
import java.util.Optional;

@FunctionalInterface
public interface GetBrandEditViewUseCase {
    Optional<Brand> execute(Long id);
}
