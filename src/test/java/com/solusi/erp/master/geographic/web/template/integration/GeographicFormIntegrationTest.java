package com.solusi.erp.master.geographic.web.template.integration;

import com.solusi.erp.master.geographic.web.dto.GeographicSaveRequest;
import com.solusi.erp.master.shared.model.GeographicType;
import com.solusi.erp.testutils.TemplateTestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Map;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Geographic Form — Template Integration Test")
@Tag("integration-template")
class GeographicFormIntegrationTest {

    private static final String TEMPLATE = "master/geographic/form";

    private Authentication auth(String... authorities) {
        TestingAuthenticationToken token = new TestingAuthenticationToken("testuser", "n/a", authorities);
        token.setAuthenticated(true);
        return token;
    }

    private GeographicSaveRequest createRequest() {
        GeographicSaveRequest req = new GeographicSaveRequest();
        req.setCode("ID");
        req.setName("Indonesia");
        req.setType(GeographicType.COUNTRY);
        req.setIsActive(true);
        return req;
    }

    private GeographicSaveRequest editRequest() {
        GeographicSaveRequest req = createRequest();
        req.setId(1L);
        req.setVersion(1);
        return req;
    }

    private Map<String, Object> model(GeographicSaveRequest request) {
        return Map.of("geographicRequest", request, "types", GeographicType.values());
    }

    @Test
    @DisplayName("Create form renders without error")
    void createForm_rendersSuccessfully() {
        String html = TemplateTestUtils.renderFragment(
                TEMPLATE, "geographic-form-content",
                new org.thymeleaf.context.Context(Locale.getDefault(), model(createRequest())));
        assertThat(html).isNotBlank();
        assertThat(html).contains("/master/geographics/create");
    }

    @Test
    @DisplayName("Edit form renders without error")
    void editForm_rendersSuccessfully() {
        String html = TemplateTestUtils.renderFragment(
                TEMPLATE, "geographic-form-content",
                new org.thymeleaf.context.Context(Locale.getDefault(), model(editRequest())));
        assertThat(html).isNotBlank();
        assertThat(html).contains("/master/geographics/edit/");
    }
}

