package com.solusi.erp.master.tax.web.controller;

import com.solusi.erp.master.tax.application.usecase.command.CreateTaxUseCase;
import com.solusi.erp.master.tax.application.usecase.command.DeleteTaxUseCase;
import com.solusi.erp.master.tax.application.usecase.command.UpdateTaxUseCase;
import com.solusi.erp.master.tax.application.usecase.query.FindTaxesUseCase;
import com.solusi.erp.master.tax.application.usecase.query.GetTaxEditViewUseCase;
import com.solusi.erp.master.tax.domain.model.Tax;
import com.solusi.erp.master.tax.web.dto.TaxSummaryResponse;
import com.solusi.erp.master.tax.web.mapper.TaxWebMapper;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TaxControllerTest {

    @Test
    void listShouldReturnListViewAndModel() {
        CreateTaxUseCase createTaxUseCase = mock(CreateTaxUseCase.class);
        UpdateTaxUseCase updateTaxUseCase = mock(UpdateTaxUseCase.class);
        DeleteTaxUseCase deleteTaxUseCase = mock(DeleteTaxUseCase.class);
        FindTaxesUseCase findTaxesUseCase = mock(FindTaxesUseCase.class);
        GetTaxEditViewUseCase getTaxEditViewUseCase = mock(GetTaxEditViewUseCase.class);
        TaxWebMapper webMapper = mock(TaxWebMapper.class);
        MessageSource messageSource = mock(MessageSource.class);

        TaxController controller = new TaxController(
                createTaxUseCase, updateTaxUseCase, deleteTaxUseCase,
                findTaxesUseCase, getTaxEditViewUseCase, webMapper, messageSource);

        Tax domainTax = Tax.createNew("TX-01", "PPN", BigDecimal.valueOf(11), null, false, true);
        com.solusi.erp.core.domain.model.Page<Tax> domainPage =
                new com.solusi.erp.core.domain.model.Page<>(List.of(domainTax), 0, 20, 1L);
        when(findTaxesUseCase.execute(any(), any())).thenReturn(domainPage);

        TaxSummaryResponse summary = new TaxSummaryResponse();
        summary.setId(1L);
        summary.setCode("TX-01");
        summary.setName("PPN");
        when(webMapper.toSummaryResponse(any(Tax.class))).thenReturn(summary);

        org.springframework.data.domain.Pageable springPageable =
                org.springframework.data.domain.PageRequest.of(0, 20);
        Model model = new ExtendedModelMap();

        String view = controller.list(null, springPageable, model);

        assertEquals("master/tax/list", view);
        Object pageObj = model.getAttribute("page");
        assertThat(pageObj).isInstanceOf(org.springframework.data.domain.Page.class);
        org.springframework.data.domain.Page<?> springPage =
                (org.springframework.data.domain.Page<?>) pageObj;
        assertEquals(1, springPage.getTotalElements());
        assertEquals("TX-01", ((TaxSummaryResponse) springPage.getContent().get(0)).getCode());
    }
}
