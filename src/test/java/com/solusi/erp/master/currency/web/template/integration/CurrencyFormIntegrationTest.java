package com.solusi.erp.master.currency.web.template.integration;

import com.solusi.erp.master.currency.web.dto.CurrencySaveRequest;
import com.solusi.erp.testutils.TemplateTestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@DisplayName("Currency Form — Template Integration Test")
@Tag("integration-template")
class CurrencyFormIntegrationTest {

    private static final String TEMPLATE = "master/currency/form";

    private Authentication auth(String... authorities) {
        TestingAuthenticationToken token = new TestingAuthenticationToken("testuser", "n/a", authorities);
        token.setAuthenticated(true);
        return token;
    }

    private CurrencySaveRequest createRequest() {
        CurrencySaveRequest req = new CurrencySaveRequest();
        req.setSymbol("Rp");
        req.setAlias("IDR");
        req.setName("Rupiah");
        req.setIsActive(true);
        req.setIsDefault(true);
        return req;
    }

    private CurrencySaveRequest editRequest() {
        CurrencySaveRequest req = createRequest();
        req.setId(1L);
        req.setVersion(1);
        return req;
    }

    @Test
    @DisplayName("Create form renders without error")
    void createForm_rendersSuccessfully() {
        assertDoesNotThrow(() -> {
            String html = TemplateTestUtils.renderWithSecurity(
                    TEMPLATE, Map.of("currencyRequest", createRequest()), auth("CURRENCY_CREATE"));
            assertThat(html).isNotBlank();
            assertThat(html).contains("/master/currencies/create");
        });
    }

    @Test
    @DisplayName("Edit form renders without error")
    void editForm_rendersSuccessfully() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, Map.of("currencyRequest", editRequest()), auth("CURRENCY_UPDATE"));
        assertThat(html).isNotBlank();
        assertThat(html).contains("/master/currencies/edit/1");
    }
}
