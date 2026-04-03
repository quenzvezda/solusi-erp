package com.solusi.erp.master.party.application.usecase.query;

import com.solusi.erp.master.party.domain.model.Party;

import java.util.List;

@FunctionalInterface
public interface FindPartiesForLookupUseCase {
    List<Party> execute(String keyword);
}
