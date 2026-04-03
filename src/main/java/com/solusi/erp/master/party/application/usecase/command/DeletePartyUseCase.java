package com.solusi.erp.master.party.application.usecase.command;

import com.solusi.erp.core.domain.model.DeleteResult;

@FunctionalInterface
public interface DeletePartyUseCase {
    DeleteResult execute(Long id);
}
