package com.solusi.erp.inventory.container.application.usecase.command;

@FunctionalInterface
public interface DeleteContainerUseCase {
    void execute(Long id);
}
