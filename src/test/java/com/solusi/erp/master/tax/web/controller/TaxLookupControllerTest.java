package com.solusi.erp.master.tax.web.controller;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.master.tax.application.usecase.query.GetTaxLookupUseCase;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TaxLookupControllerTest {

    @Test
    void search_returnsActiveTaxesAsLookupDtos() {
        GetTaxLookupUseCase useCase = mock(GetTaxLookupUseCase.class);
        TaxLookupController controller = new TaxLookupController(useCase);
        LookupDto dto = new LookupDto(
                10L,
                "PPN 11% Inclusive",
                "PPN-IN - INCLUSIVE",
                Map.of(
                        "code", "PPN-IN",
                        "rate", "11.00",
                        "calculationMode", "INCLUSIVE"
                )
        );

        when(useCase.search("", 10)).thenReturn(List.of(dto));

        List<LookupDto> result = controller.search("", 10);

        assertThat(result).singleElement().satisfies(item -> {
            assertThat(item.id()).isEqualTo(10L);
            assertThat(item.name()).isEqualTo("PPN 11% Inclusive");
            assertThat(item.subText()).contains("PPN-IN");
            assertThat(item.payload()).containsEntry("rate", "11.00");
            assertThat(item.payload()).containsEntry("calculationMode", "INCLUSIVE");
        });
    }
}
