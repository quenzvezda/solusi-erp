package com.solusi.erp.master.party.web.template.integration;

import com.solusi.erp.master.model.AddressType;
import com.solusi.erp.master.model.PartyType;
import com.solusi.erp.master.party.web.dto.PartySaveRequest;
import com.solusi.erp.testutils.TemplateTestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Map;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Party Form — Template Integration Test")
@Tag("integration-template")
class PartyFormIntegrationTest {

    private static final String TEMPLATE = "master/parties/form";

    private Authentication auth(String... authorities) {
        TestingAuthenticationToken token = new TestingAuthenticationToken("testuser", "n/a", authorities);
        token.setAuthenticated(true);
        return token;
    }

    private PartySaveRequest createRequest() {
        PartySaveRequest req = new PartySaveRequest();
        req.setName("Acme Corp");
        req.setType(PartyType.ORGANIZATION);
        req.setIsActive(true);
        return req;
    }

    private PartySaveRequest editRequest() {
        PartySaveRequest req = createRequest();
        req.setId(1L);
        req.setVersion(1);
        return req;
    }

    private Map<String, Object> formModel(PartySaveRequest request) {
        return Map.of(
                "partyRequest", request,
                "partyTypes", PartyType.values(),
                "addressTypes", AddressType.values(),
                "roleTypes", List.of(),
                "idTypes", List.of()
        );
    }

    @Test
    @DisplayName("Create form renders without error")
    void createForm_rendersSuccessfully() {
        String html = TemplateTestUtils.renderFragment(
                TEMPLATE, "party-form-content",
                new org.thymeleaf.context.Context(Locale.getDefault(), formModel(createRequest())));
        assertThat(html).isNotBlank();
        assertThat(html).contains("/master/parties/create");
    }

    @Test
    @DisplayName("Edit form renders without error")
    void editForm_rendersSuccessfully() {
        String html = TemplateTestUtils.renderFragment(
                TEMPLATE, "party-form-content",
                new org.thymeleaf.context.Context(Locale.getDefault(), formModel(editRequest())));
        assertThat(html).isNotBlank();
        assertThat(html).contains("/master/parties/edit/");
    }
}
