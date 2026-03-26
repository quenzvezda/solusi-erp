package com.solusi.erp.inventory.brand.application.usecase.command;

import com.solusi.erp.inventory.brand.domain.model.Brand;

@FunctionalInterface
public interface CreateBrandUseCase {
    Brand execute(String name, String note);
}
