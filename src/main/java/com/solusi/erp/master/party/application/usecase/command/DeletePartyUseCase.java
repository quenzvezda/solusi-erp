package com.solusi.erp.master.party.application.usecase.command;

@FunctionalInterface
public interface DeletePartyUseCase {
    void execute(Long id);
}
