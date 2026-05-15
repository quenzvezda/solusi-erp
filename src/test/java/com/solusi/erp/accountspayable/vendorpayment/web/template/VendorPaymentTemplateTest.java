package com.solusi.erp.accountspayable.vendorpayment.web.template;

import com.solusi.erp.accountspayable.vendorpayment.web.dto.VendorPaymentSummaryResponse;
import com.solusi.erp.testutils.TemplateTestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("VendorPayment List — Template Integration Test (sec:authorize visibility)")
@Tag("integration-template")
class VendorPaymentTemplateTest {

    private static final String TEMPLATE = "accountspayable/vendor-payments/list";

    private Authentication auth(String... authorities) {
        TestingAuthenticationToken token =
                new TestingAuthenticationToken("testuser", "n/a", authorities);
        token.setAuthenticated(true);
        return token;
    }

    private Map<String, Object> modelWithEmptyPage() {
        org.springframework.data.domain.Page<VendorPaymentSummaryResponse> emptyPage =
                new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        return Map.of("page", emptyPage, "keyword", "", "statuses",
                com.solusi.erp.accountspayable.vendorpayment.domain.model.VendorPaymentStatus.values());
    }

    private Map<String, Object> modelWithOnePayment() {
        VendorPaymentSummaryResponse dto = new VendorPaymentSummaryResponse();
        dto.setId(1L);
        dto.setCode("VP-001");
        dto.setPaymentDate(LocalDate.of(2026, 5, 15));
        dto.setPaymentAmount(new BigDecimal("500.00"));
        dto.setBankAccountName("Bank BCA");
        dto.setStatus("DRAFT");

        org.springframework.data.domain.Page<VendorPaymentSummaryResponse> page =
                new PageImpl<>(List.of(dto), PageRequest.of(0, 20), 1);
        return Map.of("page", page, "keyword", "", "statuses",
                com.solusi.erp.accountspayable.vendorpayment.domain.model.VendorPaymentStatus.values());
    }

    @Test
    @DisplayName("Template renders without error for user with VENDOR-PAYMENT_READ")
    void withRead_templateRendersSuccessfully() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithEmptyPage(), auth("VENDOR-PAYMENT_READ"));
        assertThat(html).isNotBlank();
        assertThat(html).contains("vendor-payment-list-content");
    }

    @Test
    @DisplayName("sec:authorize — VENDOR-PAYMENT_CREATE shows Create button")
    void withCreate_createButtonIsVisible() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithEmptyPage(), auth("VENDOR-PAYMENT_READ", "VENDOR-PAYMENT_CREATE"));
        assertThat(html).contains("/accounts-payable/vendor-payments/create");
    }

    @Test
    @DisplayName("sec:authorize — missing VENDOR-PAYMENT_CREATE hides Create button")
    void withoutCreate_createButtonIsHidden() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithEmptyPage(), auth("VENDOR-PAYMENT_READ"));
        assertThat(html).doesNotContain("/accounts-payable/vendor-payments/create");
    }

    @Test
    @DisplayName("sec:authorize — VENDOR-PAYMENT_UPDATE shows Edit button for DRAFT row")
    void withUpdate_editButtonIsVisible() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithOnePayment(), auth("VENDOR-PAYMENT_READ", "VENDOR-PAYMENT_UPDATE"));
        assertThat(html).contains("VP-001");
        assertThat(html).contains("/accounts-payable/vendor-payments/edit/");
    }

    @Test
    @DisplayName("sec:authorize — VENDOR-PAYMENT_DELETE shows Delete button for DRAFT row")
    void withDelete_deleteButtonIsVisible() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithOnePayment(), auth("VENDOR-PAYMENT_READ", "VENDOR-PAYMENT_DELETE"));
        assertThat(html).contains("VP-001");
        assertThat(html).contains("modal-delete-1");
    }

    @Test
    @DisplayName("sec:authorize — missing VENDOR-PAYMENT_DELETE hides Delete button")
    void withoutDelete_deleteButtonIsHidden() {
        String html = TemplateTestUtils.renderWithSecurity(
                TEMPLATE, modelWithOnePayment(), auth("VENDOR-PAYMENT_READ"));
        assertThat(html).contains("VP-001");
        assertThat(html).doesNotContain("modal-delete-1");
    }
}
