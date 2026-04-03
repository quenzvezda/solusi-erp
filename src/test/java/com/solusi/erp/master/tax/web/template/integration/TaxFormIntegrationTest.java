package com.solusi.erp.master.tax.web.template.integration;

import com.solusi.erp.master.tax.web.dto.TaxSaveRequest;
import com.solusi.erp.testutils.TemplateTestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@DisplayName("Tax Form — Template Integration Test")
@Tag("integration-template")
class TaxFormIntegrationTest {

    private static final String TEMPLATE = "master/tax/form";

    private Authentication auth(String... authorities) {
        TestingAuthenticationToken token = new TestingAuthenticationToken("testuser", "n/a", authorities);
        token.setAuthenticated(true);
        return token;
    }

    private TaxSaveRequest createRequest() {
        TaxSaveRequest req = new TaxSaveRequest();
        req.setCode("TX-01");
        req.setName("PPN");
        req.setRate(BigDecimal.valueOf(11));
        req.setIsActive(true);
        req.setIsSubtract(false);
        return req;
    }

    private TaxSaveRequest editRequest() {
        TaxSaveRequest req = createRequest();
        req.setId(1L);
        req.setVersion(1);
        return req;
    }

    @Test
    @DisplayName("Create form renders without error")
    void createForm_rendersSuccessfully() {
        assertDoesNotThrow(() -> {
            String html = TemplateTestUtils.renderWithSecurity(
                    TEMPLATE, Map.of("taxRequest", createRequest()), auth("TAX_CREATE"));
            assertThat(html).isNotBlank();
            assertThat(html).contains("/master/taxes/create");
        });
    }

    @Test
    @DisplayName("Edit form renders without error")
    void editForm_rendersSuccessfully() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, Map.of("taxRequest", editRequest()), auth("TAX_UPDATE"));
        assertThat(html).isNotBlank();
        assertThat(html).contains("/master/taxes/edit/1");
    }
}
