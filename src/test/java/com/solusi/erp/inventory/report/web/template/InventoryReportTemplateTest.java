package com.solusi.erp.inventory.report.web.template;

import com.solusi.erp.inventory.report.web.dto.InventoryMovementResponse;
import com.solusi.erp.inventory.report.web.dto.LocationStockDetailResponse;
import com.solusi.erp.inventory.report.web.dto.ProductStockSummaryResponse;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

public class InventoryReportTemplateTest {

    private String loadTemplate(String path) throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream(path);
        assertThat(is).withFailMessage("Template not found: " + path).isNotNull();
        return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    }

    private void assertPropertiesExistOnClass(String template, String iterVar, Class<?> dtoClass) {
        Pattern p = Pattern.compile("\\$\\{" + iterVar + "\\.([a-zA-Z0-9_]+)\\}");
        Matcher m = p.matcher(template);
        while (m.find()) {
            String prop = m.group(1);
            boolean found = false;
            for (java.lang.reflect.Field f : dtoClass.getDeclaredFields()) {
                if (f.getName().equals(prop)) { found = true; break; }
            }
            if (!found) {
                String getter = "get" + prop.substring(0, 1).toUpperCase() + prop.substring(1);
                String isGetter = "is" + prop.substring(0, 1).toUpperCase() + prop.substring(1);
                for (java.lang.reflect.Method method : dtoClass.getMethods()) {
                    if ((method.getName().equals(getter) || method.getName().equals(isGetter))
                            && method.getParameterCount() == 0) {
                        found = true;
                        break;
                    }
                }
            }
            assertThat(found)
                .withFailMessage("Property '%s' referenced in template but not present on %s",
                    prop, dtoClass.getName())
                .isTrue();
        }
    }

    @Test
    public void onHandListTemplate_containsExpectedPlaceholders() throws Exception {
        String template = loadTemplate("templates/inventory/reports/on-hand/list.html");

        assertThat(template).contains("${page.content}");
        assertThat(template).contains("${item.productCode}");
        assertThat(template).contains("${item.productName}");
        assertThat(template).contains("item.uomCode");
        assertThat(template).contains("item.totalOnHand");
        assertThat(template).contains("item.totalReserved");
        assertThat(template).contains("item.totalAvailable");
        assertThat(template).contains("item.totalInTransit");
        assertThat(template).contains("item.productId");
        assertThat(template).contains("${search}");
        assertThat(template).contains("/inventory/reports/on-hand");
    }

    @Test
    public void onHandListTemplate_itemPropertiesExistOnDto() throws Exception {
        String template = loadTemplate("templates/inventory/reports/on-hand/list.html");
        assertPropertiesExistOnClass(template, "item", ProductStockSummaryResponse.class);
    }

    @Test
    public void onHandDetailTemplate_containsExpectedPlaceholders() throws Exception {
        String template = loadTemplate("templates/inventory/reports/on-hand/detail.html");

        assertThat(template).contains("${details}");
        assertThat(template).contains("${row.facilityName}");
        assertThat(template).contains("${row.gridCode}");
        assertThat(template).contains("${row.containerCode}");
        assertThat(template).contains("row.onHand");
        assertThat(template).contains("row.reserved");
        assertThat(template).contains("row.available");
        assertThat(template).contains("product.code");
        assertThat(template).contains("product.name");
        assertThat(template).contains("/inventory/reports/on-hand");
    }

    @Test
    public void onHandDetailTemplate_rowPropertiesExistOnDto() throws Exception {
        String template = loadTemplate("templates/inventory/reports/on-hand/detail.html");
        assertPropertiesExistOnClass(template, "row", LocationStockDetailResponse.class);
    }

    @Test
    public void stockCardListTemplate_containsExpectedPlaceholders() throws Exception {
        String template = loadTemplate("templates/inventory/reports/stock-card/list.html");

        assertThat(template).contains("${page.content}");
        assertThat(template).contains("item.transactionDate");
        assertThat(template).contains("${item.productCode}");
        assertThat(template).contains("${item.productName}");
        assertThat(template).contains("${item.containerCode}");
        assertThat(template).contains("${item.facilityName}");
        assertThat(template).contains("item.quantity");
        assertThat(template).contains("${item.movementType}");
        assertThat(template).contains("${item.referenceCode}");
        assertThat(template).contains("${item.referenceType}");
        assertThat(template).contains("item.unitCostLocal");
        assertThat(template).contains("${filter}");
        assertThat(template).contains("${products}");
        assertThat(template).contains("${containers}");
        assertThat(template).contains("/inventory/reports/stock-card");
    }

    @Test
    public void stockCardListTemplate_itemPropertiesExistOnDto() throws Exception {
        String template = loadTemplate("templates/inventory/reports/stock-card/list.html");
        assertPropertiesExistOnClass(template, "item", InventoryMovementResponse.class);
    }
}
