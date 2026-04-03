package com.solusi.erp.master.partyroletype.application.usecase.command;

import com.solusi.erp.core.domain.model.DeleteResult;

@FunctionalInterface
public interface DeletePartyRoleTypeUseCase {
    DeleteResult execute(Long id);
}
