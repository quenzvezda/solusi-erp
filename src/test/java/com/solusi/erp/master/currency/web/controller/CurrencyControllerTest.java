package com.solusi.erp.master.currency.web.controller;

import com.solusi.erp.master.currency.application.usecase.command.CreateCurrencyUseCase;
import com.solusi.erp.master.currency.application.usecase.command.DeleteCurrencyUseCase;
import com.solusi.erp.master.currency.application.usecase.command.UpdateCurrencyUseCase;
import com.solusi.erp.master.currency.application.usecase.query.FindCurrenciesUseCase;
import com.solusi.erp.master.currency.application.usecase.query.GetCurrencyEditViewUseCase;
import com.solusi.erp.master.currency.domain.model.Currency;
import com.solusi.erp.master.currency.web.dto.CurrencySummaryResponse;
import com.solusi.erp.master.currency.web.mapper.CurrencyWebMapper;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CurrencyControllerTest {

    @Test
    void listShouldReturnListViewAndModel() {
        CreateCurrencyUseCase createCurrencyUseCase = mock(CreateCurrencyUseCase.class);
        UpdateCurrencyUseCase updateCurrencyUseCase = mock(UpdateCurrencyUseCase.class);
        DeleteCurrencyUseCase deleteCurrencyUseCase = mock(DeleteCurrencyUseCase.class);
        FindCurrenciesUseCase findCurrenciesUseCase = mock(FindCurrenciesUseCase.class);
        GetCurrencyEditViewUseCase getCurrencyEditViewUseCase = mock(GetCurrencyEditViewUseCase.class);
        CurrencyWebMapper webMapper = mock(CurrencyWebMapper.class);
        MessageSource messageSource = mock(MessageSource.class);

        CurrencyController controller = new CurrencyController(
                createCurrencyUseCase, updateCurrencyUseCase, deleteCurrencyUseCase,
                findCurrenciesUseCase, getCurrencyEditViewUseCase, webMapper, messageSource);

        Currency domainCurrency = Currency.createNew("$", "USD", "US Dollar", null, false, true);
        com.solusi.erp.core.domain.model.Page<Currency> domainPage =
                new com.solusi.erp.core.domain.model.Page<>(List.of(domainCurrency), 0, 20, 1L);
        when(findCurrenciesUseCase.execute(any(), any())).thenReturn(domainPage);

        CurrencySummaryResponse summary = new CurrencySummaryResponse();
        summary.setId(1L);
        summary.setSymbol("$");
        summary.setAlias("USD");
        summary.setName("US Dollar");
        when(webMapper.toSummaryResponse(any(Currency.class))).thenReturn(summary);

        org.springframework.data.domain.Pageable springPageable =
                org.springframework.data.domain.PageRequest.of(0, 20);
        Model model = new ExtendedModelMap();

        String view = controller.list(null, springPageable, model);

        assertEquals("master/currency/list", view);
        Object pageObj = model.getAttribute("page");
        assertThat(pageObj).isInstanceOf(org.springframework.data.domain.Page.class);
        org.springframework.data.domain.Page<?> springPage =
                (org.springframework.data.domain.Page<?>) pageObj;
        assertEquals(1, springPage.getTotalElements());
        assertEquals("USD", ((CurrencySummaryResponse) springPage.getContent().get(0)).getAlias());
    }
}
