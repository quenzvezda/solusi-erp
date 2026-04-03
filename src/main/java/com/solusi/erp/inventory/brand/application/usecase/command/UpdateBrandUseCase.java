package com.solusi.erp.inventory.brand.application.usecase.command;

import com.solusi.erp.inventory.brand.domain.model.Brand;

@FunctionalInterface
public interface UpdateBrandUseCase {
    Brand execute(Long id, String name, String note);
}
