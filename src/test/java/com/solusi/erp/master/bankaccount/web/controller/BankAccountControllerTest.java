package com.solusi.erp.master.bankaccount.web.controller;

import com.solusi.erp.master.bankaccount.application.usecase.command.CreateBankAccountUseCase;
import com.solusi.erp.master.bankaccount.application.usecase.command.DeleteBankAccountUseCase;
import com.solusi.erp.master.bankaccount.application.usecase.command.UpdateBankAccountUseCase;
import com.solusi.erp.master.bankaccount.application.usecase.query.FindBankAccountsUseCase;
import com.solusi.erp.master.bankaccount.application.usecase.query.GetBankAccountEditViewUseCase;
import com.solusi.erp.master.bankaccount.domain.model.BankAccount;
import com.solusi.erp.master.bankaccount.web.dto.BankAccountSummaryResponse;
import com.solusi.erp.master.bankaccount.web.mapper.BankAccountWebMapper;
import com.solusi.erp.master.geographic.domain.port.GeographicLookupProvider;
import com.solusi.erp.master.party.domain.port.PartyLookupProvider;
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

public class BankAccountControllerTest {

    @Test
    public void listShouldReturnListViewAndModel() {
        CreateBankAccountUseCase createUseCase = mock(CreateBankAccountUseCase.class);
        UpdateBankAccountUseCase updateUseCase = mock(UpdateBankAccountUseCase.class);
        DeleteBankAccountUseCase deleteUseCase = mock(DeleteBankAccountUseCase.class);
        FindBankAccountsUseCase findUseCase = mock(FindBankAccountsUseCase.class);
        GetBankAccountEditViewUseCase getEditViewUseCase = mock(GetBankAccountEditViewUseCase.class);
        BankAccountWebMapper webMapper = mock(BankAccountWebMapper.class);
        MessageSource messageSource = mock(MessageSource.class);
        PartyLookupProvider partyLookupProvider = mock(PartyLookupProvider.class);
        GeographicLookupProvider geographicLookupProvider = mock(GeographicLookupProvider.class);

        BankAccountController controller = new BankAccountController(
                createUseCase, updateUseCase, deleteUseCase,
                findUseCase, getEditViewUseCase, webMapper, messageSource,
                partyLookupProvider, geographicLookupProvider);

        BankAccount domainAccount = BankAccount.createNew("BA-001", "Bank BCA", "Sudirman",
                "John Doe", "1234567890", "BANK", "Note", 1L, 2L, true);
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
}
