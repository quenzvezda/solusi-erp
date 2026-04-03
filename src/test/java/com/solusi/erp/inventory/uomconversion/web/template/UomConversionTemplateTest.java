package com.solusi.erp.inventory.uomconversion.web.template;

import com.solusi.erp.inventory.uomconversion.web.dto.UomConversionSummaryResponse;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

public class UomConversionTemplateTest {

    @Test
    void listTemplateReferencesValidProperties() throws Exception {
        InputStream is = getClass().getClassLoader()
            .getResourceAsStream("templates/inventory/uom-conversions/list.html");
        assertThat(is).as("list.html template must exist").isNotNull();
        String template = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        Pattern p = Pattern.compile("\\$\\{item\\.([a-zA-Z0-9_]+)\\}");
        Matcher m = p.matcher(template);
        while (m.find()) {
            String prop = m.group(1);
            boolean hasGetter = false;
            String getter = "get" + prop.substring(0, 1).toUpperCase() + prop.substring(1);
            for (java.lang.reflect.Method method : UomConversionSummaryResponse.class.getMethods()) {
                if (method.getName().equals(getter) && method.getParameterCount() == 0) {
                    hasGetter = true;
                    break;
                }
            }
            assertThat(hasGetter)
                .withFailMessage("Property '%s' referenced in list template but missing from %s",
                    prop, UomConversionSummaryResponse.class.getName())
                .isTrue();
        }
    }

    @Test
    void listTemplateContainsExpectedPlaceholders() throws Exception {
        InputStream is = getClass().getClassLoader()
            .getResourceAsStream("templates/inventory/uom-conversions/list.html");
        String template = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        assertThat(template).contains("uom-conversion-table-container");
        assertThat(template).contains("${item.productName}");
        assertThat(template).contains("${item.productCode}");
        assertThat(template).contains("${item.fromUomName}");
        assertThat(template).contains("${item.toUomName}");
        assertThat(template).contains("${item.conversionFactor}");
    }

    @Test
    void listTemplateContainsHtmxAttributes() throws Exception {
        InputStream is = getClass().getClassLoader()
            .getResourceAsStream("templates/inventory/uom-conversions/list.html");
        String template = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        assertThat(template).contains("hx-get");
        assertThat(template).contains("hx-trigger");
        assertThat(template).contains("refresh-table");
    }

    @Test
    void formTemplateContainsExpectedAttributes() throws Exception {
        InputStream is = getClass().getClassLoader()
            .getResourceAsStream("templates/inventory/uom-conversions/form.html");
        assertThat(is).as("form.html template must exist").isNotNull();
        String template = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        assertThat(template).contains("uomConversionRequest");
        assertThat(template).contains("fromUomId");
        assertThat(template).contains("conversionFactor");
        assertThat(template).contains("uomUIForm");
    }

    @Test
    void formTemplateContainsSecAuthorizeFragments() throws Exception {
        InputStream is = getClass().getClassLoader()
            .getResourceAsStream("templates/inventory/uom-conversions/list.html");
        String template = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        assertThat(template).contains("UOM-CONVERSION_CREATE");
        assertThat(template).contains("UOM-CONVERSION_UPDATE");
        assertThat(template).contains("UOM-CONVERSION_DELETE");
    }
}
