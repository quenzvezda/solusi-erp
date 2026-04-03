package com.solusi.erp.inventory.container.application.usecase.query;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.inventory.container.domain.model.Container;

@FunctionalInterface
public interface FindContainersUseCase {
    Page<Container> execute(String keyword, Long gridId, Pageable pageable);
}
