package com.solusi.erp.inventory.adjustment.web.template;

import com.solusi.erp.inventory.adjustment.web.dto.StockAdjustmentSummaryResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("StockAdjustment Template Tests")
public class StockAdjustmentTemplateTest {

    private String loadTemplate(String path) throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("templates/" + path + ".html");
        assertThat(is).as("Template not found: " + path).isNotNull();
        return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("list.html references valid properties on StockAdjustmentSummaryResponse (via item.*)")
    void listTemplate_referencesValidProperties() throws Exception {
        String template = loadTemplate("inventory/adjustments/list");

        Pattern p = Pattern.compile("\\$\\{item\\.([a-zA-Z0-9_]+)\\}");
        Matcher m = p.matcher(template);
        while (m.find()) {
            String prop = m.group(1);
            boolean hasField = false;
            for (Field f : StockAdjustmentSummaryResponse.class.getDeclaredFields()) {
                if (f.getName().equals(prop)) { hasField = true; break; }
            }
            // Also check parent (BaseAuditResponse)
            if (!hasField) {
                for (Field f : StockAdjustmentSummaryResponse.class.getSuperclass().getDeclaredFields()) {
                    if (f.getName().equals(prop)) { hasField = true; break; }
                }
            }
            String getter = "get" + prop.substring(0, 1).toUpperCase() + prop.substring(1);
            boolean hasGetter = false;
            for (Method method : StockAdjustmentSummaryResponse.class.getMethods()) {
                if (method.getName().equals(getter) && method.getParameterCount() == 0) {
                    hasGetter = true; break;
                }
            }
            assertThat(hasField || hasGetter)
                    .withFailMessage("Property '%s' in list.html but not found on StockAdjustmentSummaryResponse", prop)
                    .isTrue();
        }
    }

    @Test
    @DisplayName("list.html contains expected model placeholders")
    void listTemplate_containsExpectedPlaceholders() throws Exception {
        String template = loadTemplate("inventory/adjustments/list");

        assertThat(template).contains("${item.code}");
        assertThat(template).contains("${item.status");
        assertThat(template).contains("item.totalAmountLocal");
        assertThat(template).contains("item.note");
        assertThat(template).contains("${page");
        assertThat(template).contains("search");
    }

    @Test
    @DisplayName("list.html contains sec:authorize for STOCK-ADJUSTMENT_CREATE")
    void listTemplate_containsSecAuthorizeCreate() throws Exception {
        String template = loadTemplate("inventory/adjustments/list");
        assertThat(template).contains("STOCK-ADJUSTMENT_CREATE");
    }

    @Test
    @DisplayName("form.html uses stockAdjustment model attribute")
    void formTemplate_usesStockAdjustmentModel() throws Exception {
        String template = loadTemplate("inventory/adjustments/form");
        assertThat(template).contains("stockAdjustment");
        assertThat(template).contains("currencies");
        assertThat(template).contains("exchangeRate");
        assertThat(template).contains("facilityId");
    }

    @Test
    @DisplayName("form.html contains sec:authorize for STOCK-ADJUSTMENT_PROCESS")
    void formTemplate_containsProcessAuthority() throws Exception {
        String template = loadTemplate("inventory/adjustments/form");
        assertThat(template).contains("STOCK-ADJUSTMENT_PROCESS");
    }

    @Test
    @DisplayName("view.html uses stockAdjustment model with expected fields")
    void viewTemplate_usesStockAdjustmentModel() throws Exception {
        String template = loadTemplate("inventory/adjustments/view");
        assertThat(template).contains("${stockAdjustment.code}");
        assertThat(template).contains("stockAdjustment.status");
        assertThat(template).contains("${stockAdjustment.facilityName}");
        assertThat(template).contains("stockAdjustment.totalAmountLocal");
        assertThat(template).contains("${stockAdjustment.currencyAlias}");
        assertThat(template).contains("stockAdjustment.exchangeRate");
        assertThat(template).contains("stockAdjustment.note");
    }

    @Test
    @DisplayName("view.html contains line placeholders")
    void viewTemplate_containsLinePlaceholders() throws Exception {
        String template = loadTemplate("inventory/adjustments/view");
        assertThat(template).contains("line.productCode");
        assertThat(template).contains("line.productName");
        assertThat(template).contains("line.quantity");
        assertThat(template).contains("line.unitCost");
        assertThat(template).contains("line.totalAmount");
    }
}
