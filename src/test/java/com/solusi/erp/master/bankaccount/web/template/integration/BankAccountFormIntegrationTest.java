package com.solusi.erp.master.bankaccount.web.template.integration;

import com.solusi.erp.master.bankaccount.web.dto.BankAccountSaveRequest;
import com.solusi.erp.master.shared.model.PaymentType;
import com.solusi.erp.testutils.TemplateTestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("BankAccount Form — Template Integration Test")
@Tag("integration-template")
class BankAccountFormIntegrationTest {

    private static final String TEMPLATE = "master/bank-accounts/form";

    private Authentication auth(String... authorities) {
        TestingAuthenticationToken token = new TestingAuthenticationToken("testuser", "n/a", authorities);
        token.setAuthenticated(true);
        return token;
    }

    private BankAccountSaveRequest createRequest() {
        BankAccountSaveRequest req = new BankAccountSaveRequest();
        req.setCode("BA-001");
        req.setBankName("BCA");
        req.setBranch("Jakarta");
        req.setAccountName("PT Acme");
        req.setAccountNo("123456789");
        req.setAccountType("CHECKING");
        req.setCityId(1L);
        req.setPartyId(1L);
        req.setIsActive(true);
        return req;
    }

    private BankAccountSaveRequest editRequest() {
        BankAccountSaveRequest req = createRequest();
        req.setId(1L);
        req.setVersion(1);
        return req;
    }

    @Test
    @DisplayName("Create form renders without error")
    void createForm_rendersSuccessfully() {
        String html = TemplateTestUtils.renderFragment(
                TEMPLATE, "bank-account-form-content",
                new org.thymeleaf.context.Context(java.util.Locale.getDefault(),
                        Map.of("bankAccountRequest", createRequest(), "accountTypes", PaymentType.values(), "selectedCity", "", "selectedParty", "")));
        assertThat(html).isNotBlank();
        assertThat(html).contains("/master/bank-accounts/create");
    }

    @Test
    @DisplayName("Edit form renders without error")
    void editForm_rendersSuccessfully() {
        String html = TemplateTestUtils.renderFragment(
                TEMPLATE, "bank-account-form-content",
                new org.thymeleaf.context.Context(java.util.Locale.getDefault(),
                        Map.of("bankAccountRequest", editRequest(), "accountTypes", PaymentType.values(), "selectedCity", "", "selectedParty", "")));
        assertThat(html).isNotBlank();
        assertThat(html).contains("/master/bank-accounts/edit/");
    }
}

