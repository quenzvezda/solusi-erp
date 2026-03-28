package com.solusi.erp.master.party.application.usecase.query;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.master.party.domain.model.Party;

@FunctionalInterface
public interface FindPartiesUseCase {
    Page<Party> execute(String keyword, Pageable pageable);
}
