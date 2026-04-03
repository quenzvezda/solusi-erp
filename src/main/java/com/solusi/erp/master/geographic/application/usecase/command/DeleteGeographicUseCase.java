package com.solusi.erp.master.geographic.application.usecase.command;

import com.solusi.erp.core.domain.model.DeleteResult;

@FunctionalInterface
public interface DeleteGeographicUseCase {
    DeleteResult execute(Long id);
}
