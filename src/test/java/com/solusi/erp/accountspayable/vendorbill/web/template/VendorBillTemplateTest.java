package com.solusi.erp.accountspayable.vendorbill.web.template;

import com.solusi.erp.accountspayable.vendorbill.web.dto.VendorBillFormView;
import com.solusi.erp.accountspayable.vendorbill.web.dto.VendorBillSaveRequest;
import com.solusi.erp.testutils.TemplateTestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class VendorBillTemplateTest {

    @Test
    void list_template_should_expose_create_action() throws Exception {
        String template = readResource("templates/accountspayable/vendor-bills/list.html");

        assertThat(template).contains("hasAuthority('VENDOR-BILL_CREATE')");
        assertThat(template).contains("/accounts-payable/vendor-bills/select-references");
    }

    @Test
    void detail_template_should_expose_cancel_action() throws Exception {
        String template = readResource("templates/accountspayable/vendor-bills/detail.html");

        assertThat(template).contains("hasAuthority('VENDOR-BILL_CANCEL')");
        assertThat(template).contains("/cancel");
        assertThat(template).doesNotContain("hasAuthority('VENDOR-BILL_DELETE')");
    }

    @Test
    void list_template_should_expose_delete_action() throws Exception {
        String template = readResource("templates/accountspayable/vendor-bills/list.html");

        assertThat(template).contains("hasAuthority('VENDOR-BILL_DELETE')");
        assertThat(template).contains("fragments/modals :: delete-confirm");
        assertThat(template).contains("targetId='#row-' + ${item.id}");
    }

    @Test
    void list_template_should_show_unpaid_amount_column() throws Exception {
        String template = readResource("templates/accountspayable/vendor-bills/list.html");

        assertThat(template).contains("label.vb.unpaidAmount");
        assertThat(template).contains("item.outstandingAmount");
        assertThat(template).contains("colspan=\"8\"");
    }

    @Test
    void form_template_should_wire_gr_line_selector_and_line_body() throws Exception {
        String template = readResource("templates/accountspayable/vendor-bills/form.html");
        String modal = readResource("templates/accountspayable/vendor-bills/gr-line-selector-modal.html");

        assertThat(template).contains("vendor-bill-lines-body");
        assertThat(template).contains("selected-gr-ids");
        assertThat(template).contains("_csrf.parameterName");
        assertThat(template).doesNotContain("_csrf.paramName");
        assertThat(modal).contains("js-vb-gr-select");
        assertThat(modal).contains("data-lines-url");
    }

    @Test
    void create_form_template_should_render_with_security_context() {
        VendorBillSaveRequest request = new VendorBillSaveRequest();
        request.setVendorId(1L);
        request.setCurrencyId(1L);
        request.setVendorInvoiceNumber("INV-001");
        request.setBillDate(LocalDate.of(2026, 5, 11));
        request.setDueDate(LocalDate.of(2026, 5, 18));

        assertDoesNotThrow(() -> {
            String html = TemplateTestUtils.renderWithSecurity(
                    "accountspayable/vendor-bills/form",
                    Map.of("form", new VendorBillFormView(request, null, null, false, List.of())),
                    auth("VENDOR-BILL_CREATE"));

            assertThat(html).isNotBlank();
            assertThat(html).contains("vendor-bill-form");
        });
    }

    @Test
    void detail_template_should_show_payment_summary() throws Exception {
        String template = readResource("templates/accountspayable/vendor-bills/detail.html");

        assertThat(template).contains("label.vb.paymentStatus");
        assertThat(template).contains("label.vb.paidAmount");
        assertThat(template).contains("label.vb.unpaidAmount");
        assertThat(template).contains("bill.paidAmount");
        assertThat(template).contains("bill.outstandingAmount");
        assertThat(template).contains("PARTIAL_PAID");
    }

    @Test
    void shared_message_bundle_should_include_vendor_bill_filter_keys() {
        ResourceBundle english = ResourceBundle.getBundle("messages", Locale.ENGLISH);
        ResourceBundle indonesian = ResourceBundle.getBundle("messages", Locale.forLanguageTag("id"));

        assertThat(english.containsKey("label.vendor")).isTrue();
        assertThat(english.containsKey("label.filter")).isTrue();
        assertThat(english.containsKey("label.vb.unpaidAmount")).isTrue();
        assertThat(english.containsKey("label.vb.paymentStatus")).isTrue();
        assertThat(english.containsKey("label.vb.paidAmount")).isTrue();
        assertThat(indonesian.containsKey("label.vendor")).isTrue();
        assertThat(indonesian.containsKey("label.filter")).isTrue();
        assertThat(indonesian.containsKey("label.vb.unpaidAmount")).isTrue();
        assertThat(indonesian.containsKey("label.vb.paymentStatus")).isTrue();
        assertThat(indonesian.containsKey("label.vb.paidAmount")).isTrue();
    }

    @Test
    void vendor_bill_menu_migration_should_move_entry_under_finance_accounting() throws Exception {
        String migration = readResource("db/migration/V59__Move_Vendor_Bill_Menu_To_Accounting.sql");

        assertThat(migration).contains("Finance & Accounting > Account Payable > Vendor Bill");
        assertThat(migration).contains("Keuangan & Akuntansi > Hutang Usaha > Tagihan Vendor");
    }

    @Test
    void form_script_should_remove_line_rows() throws Exception {
        String script = readResource("static/js/accountspayable/vendor-bills/form.js");

        assertThat(script).contains("js-vb-line-remove");
        assertThat(script).contains("closest(\".vendor-bill-line-row\")");
        assertThat(script).doesNotContain("fetch(linesUrl");
    }

    private String readResource(String path) throws Exception {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(path)) {
            assertThat(inputStream).as("Resource not found: %s", path).isNotNull();
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private Authentication auth(String... authorities) {
        TestingAuthenticationToken token = new TestingAuthenticationToken("testuser", "n/a", authorities);
        token.setAuthenticated(true);
        return token;
    }
}
