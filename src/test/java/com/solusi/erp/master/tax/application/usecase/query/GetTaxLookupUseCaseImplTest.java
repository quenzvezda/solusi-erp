package com.solusi.erp.master.tax.application.usecase.query;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.master.tax.domain.model.TaxCalculationMode;
import com.solusi.erp.master.tax.infrastructure.persistence.Tax;
import com.solusi.erp.master.tax.infrastructure.persistence.TaxJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetTaxLookupUseCaseImplTest {

    @Test
    void search_returnsOnlyActiveTaxesWithPayloadMetadata() {
        TaxJpaRepository taxJpaRepository = mock(TaxJpaRepository.class);
        GetTaxLookupUseCaseImpl useCase = new GetTaxLookupUseCaseImpl(taxJpaRepository);

        Tax activeTax = new Tax();
        activeTax.setId(10L);
        activeTax.setCode("PPN-IN");
        activeTax.setName("PPN 11% Inclusive");
        activeTax.setRate(new BigDecimal("11.00"));
        activeTax.setCalculationMode(TaxCalculationMode.INCLUSIVE);
        activeTax.setIsActive(true);

        Tax inactiveTax = new Tax();
        inactiveTax.setId(11L);
        inactiveTax.setCode("OLD");
        inactiveTax.setName("Old Tax");
        inactiveTax.setRate(new BigDecimal("10.00"));
        inactiveTax.setCalculationMode(TaxCalculationMode.EXCLUSIVE);
        inactiveTax.setIsActive(false);

        when(taxJpaRepository.search(eq("ppn"), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(activeTax, inactiveTax)));

        List<LookupDto> result = useCase.search("ppn", 10);

        assertThat(result).singleElement().satisfies(dto -> {
            assertThat(dto.id()).isEqualTo(10L);
            assertThat(dto.name()).isEqualTo("PPN 11% Inclusive");
            assertThat(dto.subText()).isEqualTo("PPN-IN - INCLUSIVE");
            assertThat(dto.payload()).containsEntry("code", "PPN-IN");
            assertThat(dto.payload()).containsEntry("rate", "11.00");
            assertThat(dto.payload()).containsEntry("calculationMode", "INCLUSIVE");
        });
    }

    @Test
    void getById_returnsNullForInactiveTax() {
        TaxJpaRepository taxJpaRepository = mock(TaxJpaRepository.class);
        GetTaxLookupUseCaseImpl useCase = new GetTaxLookupUseCaseImpl(taxJpaRepository);

        Tax inactiveTax = new Tax();
        inactiveTax.setId(11L);
        inactiveTax.setCode("OLD");
        inactiveTax.setName("Old Tax");
        inactiveTax.setRate(new BigDecimal("10.00"));
        inactiveTax.setCalculationMode(TaxCalculationMode.EXCLUSIVE);
        inactiveTax.setIsActive(false);

        when(taxJpaRepository.findById(11L)).thenReturn(Optional.of(inactiveTax));

        LookupDto result = useCase.getById(11L);

        assertThat(result).isNull();
    }
}
