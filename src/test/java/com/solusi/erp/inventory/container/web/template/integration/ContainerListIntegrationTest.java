package com.solusi.erp.inventory.container.web.template.integration;

import com.solusi.erp.inventory.container.web.dto.ContainerSummaryResponse;
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

@DisplayName("Container List — Template Integration Test (sec:authorize visibility)")
@Tag("integration-template")
class ContainerListIntegrationTest {

    private static final String TEMPLATE = "inventory/containers/list";

    private Authentication auth(String... authorities) {
        TestingAuthenticationToken token = new TestingAuthenticationToken("testuser", "n/a", authorities);
        token.setAuthenticated(true);
        return token;
    }

    private Map<String, Object> modelWithEmptyPage() {
        org.springframework.data.domain.Page<ContainerSummaryResponse> emptyPage =
            new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        return Map.of("page", emptyPage, "keyword", "");
    }

    private Map<String, Object> modelWithOneContainer() {
        ContainerSummaryResponse dto = new ContainerSummaryResponse();
        dto.setId(1L);
        dto.setCode("CNT-001");
        dto.setName("Bin A1");
        dto.setGridName("Storage Area A");
        dto.setFacilityName("Main Warehouse");
        dto.setBarcode("BARC-001");
        dto.setIsActive(true);

        org.springframework.data.domain.Page<ContainerSummaryResponse> page =
            new PageImpl<>(List.of(dto), PageRequest.of(0, 20), 1);
        return Map.of("page", page, "keyword", "");
    }

    @Test
    @DisplayName("Template renders without error for user with CONTAINER_READ")
    void withContainerRead_templateRendersSuccessfully() {
        String html = TemplateTestUtils.renderWithSecurity(TEMPLATE, modelWithEmptyPage(), auth("CONTAINER_READ"));
        assertThat(html).isNotBlank();
        assertThat(html).contains("container-table-container");
    }

    @Test
    @DisplayName("sec:authorize — CONTAINER_CREATE shows Add New Container button")
    void withContainerCreate_addButtonIsVisible() {
        String html = TemplateTestUtils.renderWithSecurity(TEMPLATE, modelWithEmptyPage(), auth("CONTAINER_READ", "CONTAINER_CREATE"));
        assertThat(html).contains("/inventory/containers/create");
    }

    @Test
    @DisplayName("sec:authorize — missing CONTAINER_CREATE hides Add New Container button")
    void withoutContainerCreate_addButtonIsHidden() {
        String html = TemplateTestUtils.renderWithSecurity(TEMPLATE, modelWithEmptyPage(), auth("CONTAINER_READ"));
        assertThat(html).doesNotContain("/inventory/containers/create");
    }

    @Test
    @DisplayName("sec:authorize — CONTAINER_UPDATE shows Edit button for each row")
    void withContainerUpdate_editButtonIsVisible() {
        String html = TemplateTestUtils.renderWithSecurity(TEMPLATE, modelWithOneContainer(), auth("CONTAINER_READ", "CONTAINER_UPDATE"));
        assertThat(html).contains("Bin A1");
        assertThat(html).contains("/inventory/containers/edit");
    }

    @Test
    @DisplayName("sec:authorize — missing CONTAINER_UPDATE hides Edit button")
    void withoutContainerUpdate_editButtonIsHidden() {
        String html = TemplateTestUtils.renderWithSecurity(TEMPLATE, modelWithOneContainer(), auth("CONTAINER_READ"));
        assertThat(html).contains("Bin A1");
        assertThat(html).doesNotContain("/inventory/containers/edit");
    }

    @Test
    @DisplayName("sec:authorize — CONTAINER_DELETE shows Delete button for each row")
    void withContainerDelete_deleteButtonIsVisible() {
        String html = TemplateTestUtils.renderWithSecurity(TEMPLATE, modelWithOneContainer(), auth("CONTAINER_READ", "CONTAINER_DELETE"));
        assertThat(html).contains("Bin A1");
        assertThat(html).contains("modal-delete-1");
    }

    @Test
    @DisplayName("sec:authorize — missing CONTAINER_DELETE hides Delete button")
    void withoutContainerDelete_deleteButtonIsHidden() {
        String html = TemplateTestUtils.renderWithSecurity(TEMPLATE, modelWithOneContainer(), auth("CONTAINER_READ"));
        assertThat(html).contains("Bin A1");
        assertThat(html).doesNotContain("modal-delete-1");
    }
}
