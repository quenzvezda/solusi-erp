package com.solusi.erp.master.geographic.infrastructure.adapter;

import com.solusi.erp.master.geographic.domain.port.GeographicInUseChecker;
import com.solusi.erp.master.geographic.infrastructure.persistence.GeographicJpaRepository;
import com.solusi.erp.master.bankaccount.infrastructure.persistence.BankAccountJpaRepository;

public class GeographicInUseCheckerImpl implements GeographicInUseChecker {

    private final GeographicJpaRepository geographicJpaRepository;
    private final BankAccountJpaRepository bankAccountJpaRepository;

    public GeographicInUseCheckerImpl(GeographicJpaRepository geographicJpaRepository,
                                       BankAccountJpaRepository bankAccountJpaRepository) {
        this.geographicJpaRepository = geographicJpaRepository;
        this.bankAccountJpaRepository = bankAccountJpaRepository;
    }

    @Override
    public boolean isInUse(Long geographicId) {
        if (geographicJpaRepository.existsByParentId(geographicId)) {
            return true;
        }
        return bankAccountJpaRepository.existsByCityId(geographicId);
    }
}
