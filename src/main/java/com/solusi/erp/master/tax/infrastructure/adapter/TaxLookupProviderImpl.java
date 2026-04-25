package com.solusi.erp.master.tax.infrastructure.adapter;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.master.tax.domain.port.TaxLookupProvider;
import com.solusi.erp.master.tax.infrastructure.persistence.TaxJpaRepository;

import java.util.Map;

public class TaxLookupProviderImpl implements TaxLookupProvider {

    private final TaxJpaRepository taxJpaRepository;

    public TaxLookupProviderImpl(TaxJpaRepository taxJpaRepository) {
        this.taxJpaRepository = taxJpaRepository;
    }

    @Override
    public LookupDto resolve(Long taxId) {
        if (taxId == null) {
            return null;
        }
        return taxJpaRepository.findById(taxId)
                .filter(com.solusi.erp.master.tax.infrastructure.persistence.Tax::getIsActive)
                .map(tax -> new LookupDto(
                        tax.getId(),
                        tax.getName(),
                        tax.getCode() + " - " + tax.getCalculationMode().name(),
                        Map.of(
                                "code", tax.getCode(),
                                "rate", tax.getRate().toPlainString(),
                                "calculationMode", tax.getCalculationMode().name()
                        )
                ))
                .orElse(null);
    }
}
