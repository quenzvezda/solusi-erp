package com.solusi.erp.inventory.facility.web.template.integration;

import com.solusi.erp.inventory.facility.web.dto.FacilitySummaryResponse;
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

@DisplayName("Facility List — Template Integration Test (sec:authorize visibility)")
@Tag("integration-template")
class FacilityListIntegrationTest {

    private static final String TEMPLATE = "inventory/facilities/list";

    private Authentication auth(String... authorities) {
        TestingAuthenticationToken token = new TestingAuthenticationToken("testuser", "n/a", authorities);
        token.setAuthenticated(true);
        return token;
    }

    private Map<String, Object> modelWithEmptyPage() {
        org.springframework.data.domain.Page<FacilitySummaryResponse> emptyPage =
            new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        return Map.of("page", emptyPage, "keyword", "");
    }

    private Map<String, Object> modelWithOneFacility() {
        FacilitySummaryResponse dto = new FacilitySummaryResponse();
        dto.setId(1L);
        dto.setCode("FAC-001");
        dto.setName("Warehouse A");
        dto.setOwnerName("Owner Corp");
        dto.setAddressLine1("Jl. Test 1");
        dto.setCityName("Jakarta");
        dto.setIsActive(true);

        org.springframework.data.domain.Page<FacilitySummaryResponse> page =
            new PageImpl<>(List.of(dto), PageRequest.of(0, 20), 1);
        return Map.of("page", page, "keyword", "");
    }

    @Test
    @DisplayName("Template renders without error for user with FACILITY_READ")
    void withFacilityRead_templateRendersSuccessfully() {
        String html = TemplateTestUtils.renderWithSecurity(TEMPLATE, modelWithEmptyPage(), auth("FACILITY_READ"));
        assertThat(html).isNotBlank();
        assertThat(html).contains("facility-table-container");
    }

    @Test
    @DisplayName("sec:authorize — FACILITY_CREATE shows Add New Facility button")
    void withFacilityCreate_addButtonIsVisible() {
        String html = TemplateTestUtils.renderWithSecurity(TEMPLATE, modelWithEmptyPage(), auth("FACILITY_READ", "FACILITY_CREATE"));
        assertThat(html).contains("/inventory/facilities/create");
    }

    @Test
    @DisplayName("sec:authorize — missing FACILITY_CREATE hides Add New Facility button")
    void withoutFacilityCreate_addButtonIsHidden() {
        String html = TemplateTestUtils.renderWithSecurity(TEMPLATE, modelWithEmptyPage(), auth("FACILITY_READ"));
        assertThat(html).doesNotContain("/inventory/facilities/create");
    }

    @Test
    @DisplayName("sec:authorize — FACILITY_UPDATE shows Edit button for each row")
    void withFacilityUpdate_editButtonIsVisible() {
        String html = TemplateTestUtils.renderWithSecurity(TEMPLATE, modelWithOneFacility(), auth("FACILITY_READ", "FACILITY_UPDATE"));
        assertThat(html).contains("Warehouse A");
        assertThat(html).contains("/inventory/facilities/edit");
    }

    @Test
    @DisplayName("sec:authorize — missing FACILITY_UPDATE hides Edit button")
    void withoutFacilityUpdate_editButtonIsHidden() {
        String html = TemplateTestUtils.renderWithSecurity(TEMPLATE, modelWithOneFacility(), auth("FACILITY_READ"));
        assertThat(html).contains("Warehouse A");
        assertThat(html).doesNotContain("/inventory/facilities/edit");
    }

    @Test
    @DisplayName("sec:authorize — FACILITY_DELETE shows Delete button for each row")
    void withFacilityDelete_deleteButtonIsVisible() {
        String html = TemplateTestUtils.renderWithSecurity(TEMPLATE, modelWithOneFacility(), auth("FACILITY_READ", "FACILITY_DELETE"));
        assertThat(html).contains("Warehouse A");
        assertThat(html).contains("modal-delete-1");
    }

    @Test
    @DisplayName("sec:authorize — missing FACILITY_DELETE hides Delete button")
    void withoutFacilityDelete_deleteButtonIsHidden() {
        String html = TemplateTestUtils.renderWithSecurity(TEMPLATE, modelWithOneFacility(), auth("FACILITY_READ"));
        assertThat(html).contains("Warehouse A");
        assertThat(html).doesNotContain("modal-delete-1");
    }
}
