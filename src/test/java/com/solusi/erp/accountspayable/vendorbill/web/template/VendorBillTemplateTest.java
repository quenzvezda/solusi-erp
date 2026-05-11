package com.solusi.erp.accountspayable.vendorbill.web.template;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class VendorBillTemplateTest {

    @Test
    void list_template_should_expose_create_action() throws Exception {
        String template = readResource("templates/accountspayable/vendor-bills/list.html");

        assertThat(template).contains("hasAuthority('VENDOR-BILL_CREATE')");
        assertThat(template).contains("/accounts-payable/vendor-bills/create");
    }

    @Test
    void detail_template_should_expose_cancel_and_delete_actions() throws Exception {
        String template = readResource("templates/accountspayable/vendor-bills/detail.html");

        assertThat(template).contains("hasAuthority('VENDOR-BILL_CANCEL')");
        assertThat(template).contains("/cancel");
        assertThat(template).contains("hasAuthority('VENDOR-BILL_DELETE')");
    }

    @Test
    void form_template_should_wire_gr_line_selector_and_line_body() throws Exception {
        String template = readResource("templates/accountspayable/vendor-bills/form.html");
        String modal = readResource("templates/accountspayable/vendor-bills/gr-line-selector-modal.html");

        assertThat(template).contains("vendor-bill-lines-body");
        assertThat(template).contains("selected-gr-ids");
        assertThat(modal).contains("js-vb-gr-select");
        assertThat(modal).contains("data-lines-url");
    }

    @Test
    void form_script_should_fetch_gr_lines_and_append_rows() throws Exception {
        String script = readResource("static/js/accountspayable/vendor-bills/form.js");

        assertThat(script).contains("fetch(linesUrl");
        assertThat(script).contains("appendLine");
        assertThat(script).contains("lines[");
        assertThat(script).contains("grIds[");
    }

    private String readResource(String path) throws Exception {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(path)) {
            assertThat(inputStream).as("Resource not found: %s", path).isNotNull();
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
