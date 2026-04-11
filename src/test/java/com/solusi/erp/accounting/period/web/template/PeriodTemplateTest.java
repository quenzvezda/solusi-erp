package com.solusi.erp.accounting.period.web.template;

import com.solusi.erp.accounting.period.web.dto.FiscalYearSummaryResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Period Templates — Static Template Check")
public class PeriodTemplateTest {

    private String readTemplate(String path) throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("templates/" + path);
        assertThat(is).as("Template not found: " + path).isNotNull();
        return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    }

    // ── list.html ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("list.html — contains expected fragment ID and item properties")
    void listTemplate_containsExpectedFragmentAndProperties() throws Exception {
        String template = readTemplate("accounting/period/list.html");

        assertThat(template).contains("period-table-container");
        assertThat(template).contains("${item.code}");
        assertThat(template).contains("${item.name}");
        assertThat(template).contains("item.startDate");
        assertThat(template).contains("item.endDate");
        assertThat(template).contains("item.periodCount");
        assertThat(template).contains("${item.isActive}");
        assertThat(template).contains("${item.id}");
    }

    @Test
    @DisplayName("list.html — all ${item.xxx} properties have getters on FiscalYearSummaryResponse")
    void listTemplate_referencesValidDtoProperties() throws Exception {
        String template = readTemplate("accounting/period/list.html");

        Pattern p = Pattern.compile("\\$\\{item\\.([a-zA-Z0-9_]+)\\}");
        Matcher m = p.matcher(template);
        while (m.find()) {
            String prop = m.group(1);
            boolean hasField = false;
            for (java.lang.reflect.Field f : FiscalYearSummaryResponse.class.getDeclaredFields()) {
                if (f.getName().equals(prop)) { hasField = true; break; }
            }
            boolean hasGetter = false;
            String getter = "get" + prop.substring(0, 1).toUpperCase() + prop.substring(1);
            String isGetter = "is" + prop.substring(0, 1).toUpperCase() + prop.substring(1);
            for (java.lang.reflect.Method method : FiscalYearSummaryResponse.class.getMethods()) {
                if ((method.getName().equals(getter) || method.getName().equals(isGetter))
                        && method.getParameterCount() == 0) {
                    hasGetter = true;
                    break;
                }
            }
            assertThat(hasField || hasGetter)
                    .withFailMessage("Property '%s' referenced in template but not present on %s",
                            prop, FiscalYearSummaryResponse.class.getName())
                    .isTrue();
        }
    }

    @Test
    @DisplayName("list.html — has HTMX search trigger attributes")
    void listTemplate_hasHtmxSearchTrigger() throws Exception {
        String template = readTemplate("accounting/period/list.html");

        assertThat(template).contains("hx-get=\"/accounting/periods\"");
        assertThat(template).contains("hx-target=\"#period-table-container\"");
    }

    @Test
    @DisplayName("list.html — has sec:authorize expressions for CRUD authorities")
    void listTemplate_hasSecAuthorizeExpressions() throws Exception {
        String template = readTemplate("accounting/period/list.html");

        assertThat(template).contains("hasAuthority('ACCOUNTING-PERIOD_CREATE')");
        assertThat(template).contains("hasAuthority('ACCOUNTING-PERIOD_UPDATE')");
        assertThat(template).contains("hasAuthority('ACCOUNTING-PERIOD_DELETE')");
    }

    // ── detail.html ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("detail.html — contains fiscal year properties")
    void detailTemplate_containsFyProperties() throws Exception {
        String template = readTemplate("accounting/period/detail.html");

        assertThat(template).contains("${fy.code}");
        assertThat(template).contains("fy.name");
        assertThat(template).contains("fy.startDate");
        assertThat(template).contains("fy.endDate");
        assertThat(template).contains("${fy.isActive}");
    }

    @Test
    @DisplayName("detail.html — contains period row properties")
    void detailTemplate_containsPeriodProperties() throws Exception {
        String template = readTemplate("accounting/period/detail.html");

        assertThat(template).contains("${period.code}");
        assertThat(template).contains("${period.name}");
        assertThat(template).contains("period.startDate");
        assertThat(template).contains("period.endDate");
        assertThat(template).contains("period.status");
    }

    @Test
    @DisplayName("detail.html — has status badges for NEVER_OPENED, OPEN, CLOSED")
    void detailTemplate_hasStatusBadges() throws Exception {
        String template = readTemplate("accounting/period/detail.html");

        assertThat(template).contains("NEVER_OPENED");
        assertThat(template).contains("OPEN");
        assertThat(template).contains("CLOSED");
    }

    @Test
    @DisplayName("detail.html — has sec:authorize for ACCOUNTING-PERIOD_UPDATE")
    void detailTemplate_hasSecAuthorizeForUpdate() throws Exception {
        String template = readTemplate("accounting/period/detail.html");

        assertThat(template).contains("hasAuthority('ACCOUNTING-PERIOD_UPDATE')");
    }

    // ── form.html ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("form.html — uses correct model attribute and form fields")
    void formTemplate_usesCorrectModelAttribute() throws Exception {
        String template = readTemplate("accounting/period/form.html");

        assertThat(template).contains("fyRequest");
        assertThat(template).contains("field='name'");
        assertThat(template).contains("*{startDate}");
        assertThat(template).contains("*{endDate}");
        assertThat(template).contains("*{isActive}");
        assertThat(template).contains("*{version}");
    }
}
