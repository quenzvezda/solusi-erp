package com.solusi.erp.master.party.web.template.integration;

import com.solusi.erp.master.shared.model.PartyType;
import com.solusi.erp.master.party.web.dto.PartySummaryResponse;
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

@DisplayName("Party List — Template Integration Test (sec:authorize visibility)")
@Tag("integration-template")
class PartyListIntegrationTest {

    private static final String TEMPLATE = "master/parties/list";

    private Authentication auth(String... authorities) {
        TestingAuthenticationToken token =
                new TestingAuthenticationToken("testuser", "n/a", authorities);
        token.setAuthenticated(true);
        return token;
    }

    private Map<String, Object> modelWithEmptyPage() {
        org.springframework.data.domain.Page<PartySummaryResponse> emptyPage =
                new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        return Map.of("page", emptyPage, "keyword", "");
    }

    private Map<String, Object> modelWithOneParty() {
        PartySummaryResponse dto = new PartySummaryResponse();
        dto.setId(1L);
        dto.setCode("PTY-001");
        dto.setName("Acme Corp");
        dto.setType(PartyType.ORGANIZATION);
        dto.setRoleNames(List.of("Customer"));
        dto.setIsActive(Boolean.TRUE);

        org.springframework.data.domain.Page<PartySummaryResponse> page =
                new PageImpl<>(List.of(dto), PageRequest.of(0, 20), 1);
        return Map.of("page", page, "keyword", "");
    }

    @Test
    @DisplayName("Template renders without error for user with PARTY_READ")
    void withPartyRead_templateRendersSuccessfully() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithEmptyPage(), auth("PARTY_READ"));

        assertThat(html).isNotBlank();
        assertThat(html).contains("party-table-container");
    }

    @Test
    @DisplayName("sec:authorize — PARTY_CREATE shows Add New Party button")
    void withPartyCreate_addButtonIsVisible() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithEmptyPage(), auth("PARTY_READ", "PARTY_CREATE"));

        assertThat(html).contains("/master/parties/create");
    }

    @Test
    @DisplayName("sec:authorize — missing PARTY_CREATE hides Add New Party button")
    void withoutPartyCreate_addButtonIsHidden() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithEmptyPage(), auth("PARTY_READ"));

        assertThat(html).doesNotContain("/master/parties/create");
    }

    @Test
    @DisplayName("sec:authorize — PARTY_UPDATE shows Edit button")
    void withPartyUpdate_editButtonIsVisible() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithOneParty(), auth("PARTY_READ", "PARTY_UPDATE"));

        assertThat(html).contains("Acme Corp");
        assertThat(html).contains("/master/parties/edit");
    }

    @Test
    @DisplayName("sec:authorize — missing PARTY_UPDATE hides Edit button")
    void withoutPartyUpdate_editButtonIsHidden() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithOneParty(), auth("PARTY_READ"));

        assertThat(html).contains("Acme Corp");
        assertThat(html).doesNotContain("/master/parties/edit");
    }

    @Test
    @DisplayName("sec:authorize — PARTY_DELETE shows Delete button")
    void withPartyDelete_deleteButtonIsVisible() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithOneParty(), auth("PARTY_READ", "PARTY_DELETE"));

        assertThat(html).contains("Acme Corp");
        assertThat(html).contains("modal-delete-1");
    }

    @Test
    @DisplayName("sec:authorize — missing PARTY_DELETE hides Delete button")
    void withoutPartyDelete_deleteButtonIsHidden() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithOneParty(), auth("PARTY_READ"));

        assertThat(html).contains("Acme Corp");
        assertThat(html).doesNotContain("modal-delete-1");
    }

}

