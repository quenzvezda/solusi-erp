package com.solusi.erp.accounting.coa.infrastructure.adapter;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.accounting.coa.domain.port.CoaLookupProvider;
import com.solusi.erp.accounting.coa.infrastructure.persistence.CoaJpaRepository;

public class CoaLookupProviderImpl implements CoaLookupProvider {

    private final CoaJpaRepository coaJpaRepository;

    public CoaLookupProviderImpl(CoaJpaRepository coaJpaRepository) {
        this.coaJpaRepository = coaJpaRepository;
    }

    @Override
    public LookupDto resolve(Long coaId) {
        if (coaId == null) return null;
        return coaJpaRepository.findById(coaId)
                .map(e -> new LookupDto(e.getId(), e.getName(), e.getCode()))
                .orElse(null);
    }
}
