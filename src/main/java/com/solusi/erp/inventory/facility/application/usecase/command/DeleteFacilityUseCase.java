package com.solusi.erp.inventory.facility.application.usecase.command;

@FunctionalInterface
public interface DeleteFacilityUseCase {
    void execute(Long id);
}
