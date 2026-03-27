package com.solusi.erp.inventory.grid.web.template.integration;

import com.solusi.erp.inventory.grid.web.dto.GridSummaryResponse;
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

@DisplayName("Grid List — Template Integration Test (sec:authorize visibility)")
@Tag("integration-template")
class GridListIntegrationTest {

    private static final String TEMPLATE = "inventory/grids/list";

    private Authentication auth(String... authorities) {
        TestingAuthenticationToken token = new TestingAuthenticationToken("testuser", "n/a", authorities);
        token.setAuthenticated(true);
        return token;
    }

    private Map<String, Object> modelWithEmptyPage() {
        org.springframework.data.domain.Page<GridSummaryResponse> emptyPage =
            new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        return Map.of("page", emptyPage, "keyword", "");
    }

    private Map<String, Object> modelWithOneGrid() {
        GridSummaryResponse dto = new GridSummaryResponse();
        dto.setId(1L);
        dto.setCode("GRD-001");
        dto.setName("Storage Area A");
        dto.setFacilityName("Main Warehouse");
        dto.setIsActive(true);

        org.springframework.data.domain.Page<GridSummaryResponse> page =
            new PageImpl<>(List.of(dto), PageRequest.of(0, 20), 1);
        return Map.of("page", page, "keyword", "");
    }

    @Test
    @DisplayName("Template renders without error for user with GRID_READ")
    void withGridRead_templateRendersSuccessfully() {
        String html = TemplateTestUtils.renderWithSecurity(TEMPLATE, modelWithEmptyPage(), auth("GRID_READ"));
        assertThat(html).isNotBlank();
        assertThat(html).contains("grid-table-container");
    }

    @Test
    @DisplayName("sec:authorize — GRID_CREATE shows Add New Grid button")
    void withGridCreate_addButtonIsVisible() {
        String html = TemplateTestUtils.renderWithSecurity(TEMPLATE, modelWithEmptyPage(), auth("GRID_READ", "GRID_CREATE"));
        assertThat(html).contains("/inventory/grids/create");
    }

    @Test
    @DisplayName("sec:authorize — missing GRID_CREATE hides Add New Grid button")
    void withoutGridCreate_addButtonIsHidden() {
        String html = TemplateTestUtils.renderWithSecurity(TEMPLATE, modelWithEmptyPage(), auth("GRID_READ"));
        assertThat(html).doesNotContain("/inventory/grids/create");
    }

    @Test
    @DisplayName("sec:authorize — GRID_UPDATE shows Edit button for each row")
    void withGridUpdate_editButtonIsVisible() {
        String html = TemplateTestUtils.renderWithSecurity(TEMPLATE, modelWithOneGrid(), auth("GRID_READ", "GRID_UPDATE"));
        assertThat(html).contains("Storage Area A");
        assertThat(html).contains("/inventory/grids/edit");
    }

    @Test
    @DisplayName("sec:authorize — missing GRID_UPDATE hides Edit button")
    void withoutGridUpdate_editButtonIsHidden() {
        String html = TemplateTestUtils.renderWithSecurity(TEMPLATE, modelWithOneGrid(), auth("GRID_READ"));
        assertThat(html).contains("Storage Area A");
        assertThat(html).doesNotContain("/inventory/grids/edit");
    }

    @Test
    @DisplayName("sec:authorize — GRID_DELETE shows Delete button for each row")
    void withGridDelete_deleteButtonIsVisible() {
        String html = TemplateTestUtils.renderWithSecurity(TEMPLATE, modelWithOneGrid(), auth("GRID_READ", "GRID_DELETE"));
        assertThat(html).contains("Storage Area A");
        assertThat(html).contains("modal-delete-1");
    }

    @Test
    @DisplayName("sec:authorize — missing GRID_DELETE hides Delete button")
    void withoutGridDelete_deleteButtonIsHidden() {
        String html = TemplateTestUtils.renderWithSecurity(TEMPLATE, modelWithOneGrid(), auth("GRID_READ"));
        assertThat(html).contains("Storage Area A");
        assertThat(html).doesNotContain("modal-delete-1");
    }
}
