package com.solusi.erp.master.geographic.application.usecase.query;

import com.solusi.erp.master.geographic.domain.model.Geographic;

import java.util.Optional;

@FunctionalInterface
public interface GetGeographicEditViewUseCase {
    Optional<Geographic> execute(Long id);
}
