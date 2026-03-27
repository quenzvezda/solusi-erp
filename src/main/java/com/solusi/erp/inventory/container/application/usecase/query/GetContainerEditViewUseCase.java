package com.solusi.erp.inventory.container.application.usecase.query;

import com.solusi.erp.inventory.container.domain.model.Container;
import java.util.Optional;

@FunctionalInterface
public interface GetContainerEditViewUseCase {
    Optional<Container> execute(Long id);
}
