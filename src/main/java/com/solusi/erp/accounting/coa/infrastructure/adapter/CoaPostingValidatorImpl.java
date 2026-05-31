package com.solusi.erp.accounting.coa.infrastructure.adapter;

import com.solusi.erp.accounting.coa.domain.port.CoaPostingValidator;
import com.solusi.erp.accounting.coa.infrastructure.persistence.CoaJpaRepository;

public class CoaPostingValidatorImpl implements CoaPostingValidator {

    private final CoaJpaRepository coaJpaRepository;

    public CoaPostingValidatorImpl(CoaJpaRepository coaJpaRepository) {
        this.coaJpaRepository = coaJpaRepository;
    }

    @Override
    public boolean isPostable(Long coaId) {
        if (coaId == null) {
            return false;
        }
        return coaJpaRepository.findById(coaId)
                .map(coa -> Boolean.TRUE.equals(coa.getIsActive()) && !Boolean.TRUE.equals(coa.getIsHeader()))
                .orElse(false);
    }
}
