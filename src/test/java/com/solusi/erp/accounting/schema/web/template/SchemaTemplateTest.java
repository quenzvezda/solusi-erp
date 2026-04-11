package com.solusi.erp.accounting.schema.web.template;

import com.solusi.erp.accounting.schema.web.dto.SchemaSummaryResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Schema Templates — Static Template Check")
public class SchemaTemplateTest {

    private String readTemplate(String path) throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("templates/" + path);
        assertThat(is).as("Template not found: " + path).isNotNull();
        return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("list.html — contains expected fragment ID and item properties")
    void listTemplate_containsExpectedFragmentAndProperties() throws Exception {
        String template = readTemplate("accounting/schema/list.html");

        assertThat(template).contains("schema-table-container");
        assertThat(template).contains("item.eventType");
        assertThat(template).contains("${item.description}");
        assertThat(template).contains("${item.debitAccountName}");
        assertThat(template).contains("${item.creditAccountName}");
        assertThat(template).contains("${item.isActive}");
    }

    @Test
    @DisplayName("list.html — all ${item.xxx} properties have getters on SchemaSummaryResponse")
    void listTemplate_referencesValidDtoProperties() throws Exception {
        String template = readTemplate("accounting/schema/list.html");

        Pattern p = Pattern.compile("\\$\\{item\\.([a-zA-Z0-9_]+)\\}");
        Matcher m = p.matcher(template);
        while (m.find()) {
            String prop = m.group(1);
            boolean hasField = false;
            for (java.lang.reflect.Field f : SchemaSummaryResponse.class.getDeclaredFields()) {
                if (f.getName().equals(prop)) { hasField = true; break; }
            }
            boolean hasGetter = false;
            String getter = "get" + prop.substring(0, 1).toUpperCase() + prop.substring(1);
            String isGetter = "is" + prop.substring(0, 1).toUpperCase() + prop.substring(1);
            for (java.lang.reflect.Method method : SchemaSummaryResponse.class.getMethods()) {
                if ((method.getName().equals(getter) || method.getName().equals(isGetter))
                        && method.getParameterCount() == 0) {
                    hasGetter = true;
                    break;
                }
            }
            assertThat(hasField || hasGetter)
                    .withFailMessage("Property '%s' referenced in template but not present on %s",
                            prop, SchemaSummaryResponse.class.getName())
                    .isTrue();
        }
    }

    @Test
    @DisplayName("form.html — uses correct model attribute and form fields")
    void formTemplate_usesCorrectModelAttribute() throws Exception {
        String template = readTemplate("accounting/schema/form.html");

        assertThat(template).contains("schemaRequest");
        assertThat(template).contains("*{eventType}");
        assertThat(template).contains("*{description}");
        assertThat(template).contains("*{debitAccountId}");
        assertThat(template).contains("*{creditAccountId}");
        assertThat(template).contains("*{isActive}");
    }

    @Test
    @DisplayName("list.html — has HTMX search trigger attributes")
    void listTemplate_hasHtmxSearchTrigger() throws Exception {
        String template = readTemplate("accounting/schema/list.html");

        assertThat(template).contains("hx-get=\"/accounting/schemas\"");
        assertThat(template).contains("hx-target=\"#schema-table-container\"");
    }

    @Test
    @DisplayName("list.html — has sec:authorize expressions for CRUD authorities")
    void listTemplate_hasSecAuthorizeExpressions() throws Exception {
        String template = readTemplate("accounting/schema/list.html");

        assertThat(template).contains("hasAuthority('ACCOUNTING-SCHEMA_CREATE')");
        assertThat(template).contains("hasAuthority('ACCOUNTING-SCHEMA_UPDATE')");
        assertThat(template).contains("hasAuthority('ACCOUNTING-SCHEMA_DELETE')");
    }
}
