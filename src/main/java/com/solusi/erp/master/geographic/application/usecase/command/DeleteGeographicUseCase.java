package com.solusi.erp.master.geographic.application.usecase.command;

@FunctionalInterface
public interface DeleteGeographicUseCase {
    void execute(Long id);
}
