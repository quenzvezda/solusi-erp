package com.solusi.erp.master.partyroletype.web.template.integration;

import com.solusi.erp.master.partyroletype.web.dto.PartyRoleTypeSummaryResponse;
import com.solusi.erp.testutils.TemplateTestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PartyRoleType List — Template Integration Test (sec:authorize visibility)")
@Tag("integration-template")
class PartyRoleTypeListIntegrationTest {

    private static final String TEMPLATE = "master/party-role-types/list";

    private Authentication auth(String... authorities) {
        TestingAuthenticationToken token =
                new TestingAuthenticationToken("testuser", "n/a", authorities);
        token.setAuthenticated(true);
        return token;
    }

    private Map<String, Object> modelWithEmptyPage() {
        org.springframework.data.domain.Page<PartyRoleTypeSummaryResponse> emptyPage =
                new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        return Map.of("page", emptyPage, "keyword", "");
    }

    private Map<String, Object> modelWithOnePartyRoleType() {
        PartyRoleTypeSummaryResponse dto = new PartyRoleTypeSummaryResponse();
        dto.setId(1L);
        dto.setCode("PRT-001");
        dto.setName("Customer");

        org.springframework.data.domain.Page<PartyRoleTypeSummaryResponse> page =
                new PageImpl<>(List.of(dto), PageRequest.of(0, 20), 1);
        return Map.of("page", page, "keyword", "");
    }

    @Test
    @DisplayName("Template renders without error for user with PARTY-ROLE-TYPE_READ")
    void withPartyRoleTypeRead_templateRendersSuccessfully() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithEmptyPage(), auth("PARTY-ROLE-TYPE_READ"));

        assertThat(html).isNotBlank();
        assertThat(html).contains("party-role-type-table-container");
    }

    @Test
    @DisplayName("sec:authorize — PARTY-ROLE-TYPE_CREATE shows Add New button")
    void withPartyRoleTypeCreate_addButtonIsVisible() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithEmptyPage(), auth("PARTY-ROLE-TYPE_READ", "PARTY-ROLE-TYPE_CREATE"));

        assertThat(html).contains("/master/party-role-types/create");
    }

    @Test
    @DisplayName("sec:authorize — missing PARTY-ROLE-TYPE_CREATE hides Add New button")
    void withoutPartyRoleTypeCreate_addButtonIsHidden() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithEmptyPage(), auth("PARTY-ROLE-TYPE_READ"));

        assertThat(html).doesNotContain("/master/party-role-types/create");
    }

    @Test
    @DisplayName("sec:authorize — PARTY-ROLE-TYPE_UPDATE shows Edit button for each row")
    void withPartyRoleTypeUpdate_editButtonIsVisible() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithOnePartyRoleType(), auth("PARTY-ROLE-TYPE_READ", "PARTY-ROLE-TYPE_UPDATE"));

        assertThat(html).contains("Customer");
        assertThat(html).contains("/master/party-role-types/edit");
    }

    @Test
    @DisplayName("sec:authorize — PARTY-ROLE-TYPE_DELETE shows Delete button for each row")
    void withPartyRoleTypeDelete_deleteButtonIsVisible() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithOnePartyRoleType(), auth("PARTY-ROLE-TYPE_READ", "PARTY-ROLE-TYPE_DELETE"));

        assertThat(html).contains("Customer");
        assertThat(html).contains("modal-delete-1");
    }

    @Test
    @DisplayName("sec:authorize — missing PARTY-ROLE-TYPE_UPDATE hides Edit button")
    void withoutPartyRoleTypeUpdate_editButtonIsHidden() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithOnePartyRoleType(), auth("PARTY-ROLE-TYPE_READ"));

        assertThat(html).contains("Customer");
        assertThat(html).doesNotContain("/master/party-role-types/edit");
    }

    @Test
    @DisplayName("sec:authorize — missing PARTY-ROLE-TYPE_DELETE hides Delete button")
    void withoutPartyRoleTypeDelete_deleteButtonIsHidden() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithOnePartyRoleType(), auth("PARTY-ROLE-TYPE_READ"));

        assertThat(html).contains("Customer");
        assertThat(html).doesNotContain("modal-delete-1");
    }

}
