package com.solusi.erp.master.partyroletype.application.usecase.query;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.master.partyroletype.domain.model.PartyRoleType;

@FunctionalInterface
public interface FindPartyRoleTypesUseCase {
    Page<PartyRoleType> execute(String keyword, Pageable pageable);
}
