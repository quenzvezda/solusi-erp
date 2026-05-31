package com.solusi.erp.master.currency.infrastructure.adapter;

import com.solusi.erp.master.currency.domain.port.CurrencyPostingValidator;
import com.solusi.erp.master.currency.infrastructure.persistence.CurrencyJpaRepository;

public class CurrencyPostingValidatorImpl implements CurrencyPostingValidator {

    private final CurrencyJpaRepository currencyJpaRepository;

    public CurrencyPostingValidatorImpl(CurrencyJpaRepository currencyJpaRepository) {
        this.currencyJpaRepository = currencyJpaRepository;
    }

    @Override
    public CurrencyPostingInfo getPostingInfo(Long currencyId) {
        if (currencyId == null) {
            return null;
        }
        return currencyJpaRepository.findById(currencyId)
                .map(currency -> new CurrencyPostingInfo(
                        Boolean.TRUE.equals(currency.getIsActive()),
                        Boolean.TRUE.equals(currency.getIsDefault())
                ))
                .orElse(null);
    }
}
