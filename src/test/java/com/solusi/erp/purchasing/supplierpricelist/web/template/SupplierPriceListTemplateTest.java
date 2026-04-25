package com.solusi.erp.purchasing.supplierpricelist.web.template;

import com.solusi.erp.purchasing.supplierpricelist.web.dto.SupplierPriceListSummaryResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Supplier Price List Template Tests")
class SupplierPriceListTemplateTest {

    private String loadTemplate(String path) throws Exception {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("templates/" + path + ".html")) {
            assertThat(is).as("Template not found: " + path).isNotNull();
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @Test
    @DisplayName("form script locks UoM input and validates effective date range")
    void formScript_locksUomAndValidatesDateRange() throws Exception {
        try (InputStream is = getClass().getClassLoader()
            .getResourceAsStream("static/js/purchasing/supplier-price-list-form.js")) {
            assertThat(is).as("supplier-price-list-form.js must exist").isNotNull();
            String script = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            assertThat(script).contains("uomTs.lock()");
            assertThat(script).contains("set('minDate'");
        }
    }

    @Test
    @DisplayName("form includes page-specific script for UoM/date behavior")
    void formTemplate_includesPageSpecificScript() throws Exception {
        String template = loadTemplate("purchasing/supplier-price-lists/form");
        assertThat(template).contains("@{/js/purchasing/supplier-price-list-form.js}");
    }

    @Test
    @DisplayName("list template renders currency symbol before unit price")
    void listTemplate_rendersCurrencySymbolBeforeUnitPrice() throws Exception {
        String template = loadTemplate("purchasing/supplier-price-lists/list");
        assertThat(template).contains("item.currencySymbol");
        assertThat(template).contains("#numbers.formatDecimal(item.unitPrice, 1, 'COMMA', 2, 'POINT')");
    }

    @Test
    @DisplayName("list template references valid summary response properties")
    void listTemplate_referencesValidProperties() throws Exception {
        String template = loadTemplate("purchasing/supplier-price-lists/list");
        Pattern p = Pattern.compile("\\$\\{item\\.([a-zA-Z0-9_]+)");
        Matcher m = p.matcher(template);
        while (m.find()) {
            String prop = m.group(1);
            String getter = "get" + prop.substring(0, 1).toUpperCase() + prop.substring(1);
            String booleanGetter = "is" + prop.substring(0, 1).toUpperCase() + prop.substring(1);
            boolean hasGetter = false;
            for (java.lang.reflect.Method method : SupplierPriceListSummaryResponse.class.getMethods()) {
                if ((method.getName().equals(getter) || method.getName().equals(booleanGetter))
                    && method.getParameterCount() == 0) {
                    hasGetter = true;
                    break;
                }
            }
            assertThat(hasGetter)
                .withFailMessage("Property '%s' referenced in list template but missing from %s",
                    prop, SupplierPriceListSummaryResponse.class.getName())
                .isTrue();
        }
    }
}
