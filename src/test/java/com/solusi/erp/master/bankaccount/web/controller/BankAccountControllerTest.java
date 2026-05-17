package com.solusi.erp.master.bankaccount.web.controller;

import com.solusi.erp.accounting.coa.application.usecase.query.CoaSelectorRow;
import com.solusi.erp.accounting.coa.application.usecase.query.FindCoaSelectorUseCase;
import com.solusi.erp.accounting.coa.domain.port.CoaLookupProvider;
import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.master.bankaccount.application.usecase.command.CreateBankAccountUseCase;
import com.solusi.erp.master.bankaccount.application.usecase.command.DeleteBankAccountUseCase;
import com.solusi.erp.master.bankaccount.application.usecase.command.UpdateBankAccountUseCase;
import com.solusi.erp.master.bankaccount.application.usecase.query.FindBankAccountsUseCase;
import com.solusi.erp.master.bankaccount.application.usecase.query.GetBankAccountEditViewUseCase;
import com.solusi.erp.master.bankaccount.domain.model.BankAccount;
import com.solusi.erp.master.bankaccount.web.dto.BankAccountSummaryResponse;
import com.solusi.erp.master.bankaccount.web.mapper.BankAccountWebMapper;
import com.solusi.erp.master.currency.domain.port.CurrencyLookupProvider;
import com.solusi.erp.master.geographic.domain.port.GeographicLookupProvider;
import com.solusi.erp.master.party.domain.port.PartyLookupProvider;
import com.solusi.erp.master.shared.model.PaymentType;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BankAccountControllerTest {

    @Test
    public void listShouldReturnListViewAndModel() {
        CreateBankAccountUseCase createUseCase = mock(CreateBankAccountUseCase.class);
        UpdateBankAccountUseCase updateUseCase = mock(UpdateBankAccountUseCase.class);
        DeleteBankAccountUseCase deleteUseCase = mock(DeleteBankAccountUseCase.class);
        FindBankAccountsUseCase findUseCase = mock(FindBankAccountsUseCase.class);
        FindCoaSelectorUseCase findCoaSelectorUseCase = mock(FindCoaSelectorUseCase.class);
        GetBankAccountEditViewUseCase getEditViewUseCase = mock(GetBankAccountEditViewUseCase.class);
        BankAccountWebMapper webMapper = mock(BankAccountWebMapper.class);
        MessageSource messageSource = mock(MessageSource.class);
        PartyLookupProvider partyLookupProvider = mock(PartyLookupProvider.class);
        GeographicLookupProvider geographicLookupProvider = mock(GeographicLookupProvider.class);
        CurrencyLookupProvider currencyLookupProvider = mock(CurrencyLookupProvider.class);
        CoaLookupProvider coaLookupProvider = mock(CoaLookupProvider.class);

        BankAccountController controller = new BankAccountController(
                createUseCase, updateUseCase, deleteUseCase,
                findUseCase, findCoaSelectorUseCase, getEditViewUseCase, webMapper, messageSource,
                partyLookupProvider, geographicLookupProvider,
                currencyLookupProvider, coaLookupProvider);

        BankAccount domainAccount = BankAccount.createNew("BA-001", "Bank BCA", "Sudirman",
                "John Doe", "1234567890", PaymentType.BANK_TRANSFER, "Note", 1L, 2L, true, 1L, 10L);
        com.solusi.erp.core.domain.model.Page<BankAccount> domainPage =
                new com.solusi.erp.core.domain.model.Page<>(List.of(domainAccount), 0, 20, 1L);
        when(findUseCase.execute(any(), any())).thenReturn(domainPage);

        BankAccountSummaryResponse summary = new BankAccountSummaryResponse();
        summary.setId(1L);
        summary.setCode("BA-001");
        summary.setBankName("Bank BCA");
        summary.setAccountName("John Doe");
        summary.setAccountNo("1234567890");
        summary.setAccountType("BANK");
        when(webMapper.toSummaryResponse(any(BankAccount.class))).thenReturn(summary);

        org.springframework.data.domain.Pageable springPageable = org.springframework.data.domain.PageRequest.of(0, 20);
        Model model = new ExtendedModelMap();

        String view = controller.list(null, springPageable, model);

        assertEquals("master/bank-accounts/list", view);
        Object pageObj = model.getAttribute("page");
        assertThat(pageObj).isInstanceOf(org.springframework.data.domain.Page.class);
        org.springframework.data.domain.Page<?> springPage = (org.springframework.data.domain.Page<?>) pageObj;
        assertEquals(1, springPage.getTotalElements());
        Object first = springPage.getContent().get(0);
        assertThat(first).isInstanceOf(BankAccountSummaryResponse.class);
        assertEquals("BA-001", ((BankAccountSummaryResponse) first).getCode());
    }

    @Test
    public void editShouldPopulateCurrencyAndCoaUi() {
        CreateBankAccountUseCase createUseCase = mock(CreateBankAccountUseCase.class);
        UpdateBankAccountUseCase updateUseCase = mock(UpdateBankAccountUseCase.class);
        DeleteBankAccountUseCase deleteUseCase = mock(DeleteBankAccountUseCase.class);
        FindBankAccountsUseCase findUseCase = mock(FindBankAccountsUseCase.class);
        FindCoaSelectorUseCase findCoaSelectorUseCase = mock(FindCoaSelectorUseCase.class);
        GetBankAccountEditViewUseCase getEditViewUseCase = mock(GetBankAccountEditViewUseCase.class);
        BankAccountWebMapper webMapper = mock(BankAccountWebMapper.class);
        MessageSource messageSource = mock(MessageSource.class);
        PartyLookupProvider partyLookupProvider = mock(PartyLookupProvider.class);
        GeographicLookupProvider geographicLookupProvider = mock(GeographicLookupProvider.class);
        CurrencyLookupProvider currencyLookupProvider = mock(CurrencyLookupProvider.class);
        CoaLookupProvider coaLookupProvider = mock(CoaLookupProvider.class);

        BankAccountController controller = new BankAccountController(
                createUseCase, updateUseCase, deleteUseCase,
                findUseCase, findCoaSelectorUseCase, getEditViewUseCase, webMapper, messageSource,
                partyLookupProvider, geographicLookupProvider,
                currencyLookupProvider, coaLookupProvider);

        BankAccount domainAccount = BankAccount.createNew("BA-001", "Bank BCA", "Sudirman",
                "John Doe", "1234567890", PaymentType.BANK_TRANSFER, "Note", 1L, 2L, true, 5L, 19L);
        when(getEditViewUseCase.execute(1L)).thenReturn(Optional.of(domainAccount));
        when(currencyLookupProvider.resolve(5L)).thenReturn(new LookupDto(5L, "IDR", "Indonesian Rupiah", null));
        when(coaLookupProvider.resolve(19L)).thenReturn(new LookupDto(19L, "Main Bank Account", "1120", null));

        Model model = new ExtendedModelMap();
        String view = controller.showEditForm(1L, model);

        assertEquals("master/bank-accounts/form", view);
        assertThat(model.getAttribute("bankAccountUI")).isInstanceOf(Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> ui = (Map<String, Object>) model.getAttribute("bankAccountUI");
        assertThat(ui).containsEntry("currencyText", "IDR");
        assertThat(ui).containsEntry("currencySubtext", "Indonesian Rupiah");
        assertThat(ui).containsEntry("coaText", "Main Bank Account");
        assertThat(ui).containsEntry("coaSubtext", "1120");
    }

    @Test
    public void coaSelectorShouldReturnFragmentAndModel() {
        CreateBankAccountUseCase createUseCase = mock(CreateBankAccountUseCase.class);
        UpdateBankAccountUseCase updateUseCase = mock(UpdateBankAccountUseCase.class);
        DeleteBankAccountUseCase deleteUseCase = mock(DeleteBankAccountUseCase.class);
        FindBankAccountsUseCase findUseCase = mock(FindBankAccountsUseCase.class);
        FindCoaSelectorUseCase findCoaSelectorUseCase = mock(FindCoaSelectorUseCase.class);
        GetBankAccountEditViewUseCase getEditViewUseCase = mock(GetBankAccountEditViewUseCase.class);
        BankAccountWebMapper webMapper = mock(BankAccountWebMapper.class);
        MessageSource messageSource = mock(MessageSource.class);
        PartyLookupProvider partyLookupProvider = mock(PartyLookupProvider.class);
        GeographicLookupProvider geographicLookupProvider = mock(GeographicLookupProvider.class);
        CurrencyLookupProvider currencyLookupProvider = mock(CurrencyLookupProvider.class);
        CoaLookupProvider coaLookupProvider = mock(CoaLookupProvider.class);

        BankAccountController controller = new BankAccountController(
                createUseCase, updateUseCase, deleteUseCase,
                findUseCase, findCoaSelectorUseCase, getEditViewUseCase, webMapper, messageSource,
                partyLookupProvider, geographicLookupProvider,
                currencyLookupProvider, coaLookupProvider);

        CoaSelectorRow row = new CoaSelectorRow(19L, "1120", "Main Bank Account", "ASSET", 2, false, null, null, null);
        when(findCoaSelectorUseCase.execute(any(), any(), any()))
                .thenReturn(new com.solusi.erp.core.domain.model.Page<>(List.of(row), 0, 20, 1L));

        Model model = new ExtendedModelMap();
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 20);
        String view = controller.showCoaSelector("bank", "ASSET", pageable, model);

        assertEquals("master/bank-accounts/fragments/coa-selector-modal", view);
        assertThat(model.getAttribute("page")).isInstanceOf(org.springframework.data.domain.Page.class);
        assertEquals("bank", model.getAttribute("keyword"));
        assertEquals("ASSET", model.getAttribute("accountType"));
        assertThat(model.getAttribute("accountTypes")).isNotNull();
    }
}
