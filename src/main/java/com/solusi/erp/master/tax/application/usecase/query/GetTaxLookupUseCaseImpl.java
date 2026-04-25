package com.solusi.erp.master.tax.application.usecase.query;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.master.tax.infrastructure.persistence.Tax;
import com.solusi.erp.master.tax.infrastructure.persistence.TaxJpaRepository;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Map;

public class GetTaxLookupUseCaseImpl implements GetTaxLookupUseCase {

    private final TaxJpaRepository taxJpaRepository;

    public GetTaxLookupUseCaseImpl(TaxJpaRepository taxJpaRepository) {
        this.taxJpaRepository = taxJpaRepository;
    }

    @Override
    public List<LookupDto> search(String q, int limit) {
        PageRequest pageable = PageRequest.of(0, limit);
        List<Tax> taxes = (q == null || q.isBlank()
                ? taxJpaRepository.findByIsActiveTrue(pageable).getContent()
                : taxJpaRepository.search(q, pageable).getContent()).stream()
                .filter(Tax::getIsActive)
                .toList();

        return taxes.stream()
                .map(this::toLookupDto)
                .toList();
    }

    @Override
    public LookupDto getById(Long id) {
        return taxJpaRepository.findById(id)
                .filter(Tax::getIsActive)
                .map(this::toLookupDto)
                .orElse(null);
    }

    private LookupDto toLookupDto(Tax tax) {
        return new LookupDto(
                tax.getId(),
                tax.getName(),
                tax.getCode() + " - " + tax.getCalculationMode().name(),
                Map.of(
                        "code", tax.getCode(),
                        "rate", tax.getRate().toPlainString(),
                        "calculationMode", tax.getCalculationMode().name()
                )
        );
    }
}
