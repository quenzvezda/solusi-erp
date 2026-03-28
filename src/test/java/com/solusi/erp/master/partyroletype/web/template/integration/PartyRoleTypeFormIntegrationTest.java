package com.solusi.erp.master.partyroletype.web.template.integration;

import com.solusi.erp.master.partyroletype.web.dto.PartyRoleTypeSaveRequest;
import com.solusi.erp.testutils.TemplateTestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@DisplayName("PartyRoleType Form — Template Integration Test")
@Tag("integration-template")
class PartyRoleTypeFormIntegrationTest {

    private static final String TEMPLATE = "master/party-role-types/form";

    private Authentication auth(String... authorities) {
        TestingAuthenticationToken token = new TestingAuthenticationToken("testuser", "n/a", authorities);
        token.setAuthenticated(true);
        return token;
    }

    private PartyRoleTypeSaveRequest createRequest() {
        PartyRoleTypeSaveRequest req = new PartyRoleTypeSaveRequest();
        req.setName("Customer");
        req.setIsActive(true);
        return req;
    }

    private PartyRoleTypeSaveRequest editRequest() {
        PartyRoleTypeSaveRequest req = createRequest();
        req.setId(1L);
        req.setVersion(1);
        return req;
    }

    @Test
    @DisplayName("Create form renders without error")
    void createForm_rendersSuccessfully() {
        assertDoesNotThrow(() -> {
            String html = TemplateTestUtils.renderWithSecurity(
                    TEMPLATE, Map.of("partyRoleTypeRequest", createRequest()), auth("PARTY-ROLE-TYPE_CREATE"));
            assertThat(html).isNotBlank();
            assertThat(html).contains("/master/party-role-types/create");
        });
    }

    @Test
    @DisplayName("Edit form renders without error")
    void editForm_rendersSuccessfully() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, Map.of("partyRoleTypeRequest", editRequest()), auth("PARTY-ROLE-TYPE_UPDATE"));
        assertThat(html).isNotBlank();
        assertThat(html).contains("/master/party-role-types/edit/1");
    }
}
