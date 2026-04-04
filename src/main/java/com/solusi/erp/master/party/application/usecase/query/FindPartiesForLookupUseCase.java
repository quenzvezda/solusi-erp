package com.solusi.erp.master.party.application.usecase.query;

import com.solusi.erp.master.party.domain.model.Party;

import java.util.List;

public interface FindPartiesForLookupUseCase {
    List<Party> execute(String keyword);
    List<Party> executeByRoleType(String keyword, String roleTypeCode, Long excludePartyId);
}
