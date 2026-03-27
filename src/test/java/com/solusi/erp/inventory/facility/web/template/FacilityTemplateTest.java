package com.solusi.erp.inventory.facility.web.template;

import com.solusi.erp.inventory.facility.web.dto.FacilitySummaryResponse;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

public class FacilityTemplateTest {

    @Test
    public void templateReferencesValidProperties() throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("templates/inventory/facilities/list.html");
        String template = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        Pattern p = Pattern.compile("\\$\\{item\\.([a-zA-Z0-9_]+)\\}");
        Matcher m = p.matcher(template);
        while (m.find()) {
            String prop = m.group(1);
            boolean hasField = false;
            for (java.lang.reflect.Field f : FacilitySummaryResponse.class.getDeclaredFields()) {
                if (f.getName().equals(prop)) { hasField = true; break; }
            }
            boolean hasGetter = false;
            String getter = "get" + prop.substring(0,1).toUpperCase() + prop.substring(1);
            String isGetter = "is" + prop.substring(0,1).toUpperCase() + prop.substring(1);
            for (java.lang.reflect.Method method : FacilitySummaryResponse.class.getMethods()) {
                if ((method.getName().equals(getter) || method.getName().equals(isGetter)) && method.getParameterCount()==0) { hasGetter = true; break; }
            }
            assertThat(hasField || hasGetter)
                .withFailMessage("Property '%s' referenced in template but not in %s", prop, FacilitySummaryResponse.class.getName())
                .isTrue();
        }
    }

    @Test
    public void templateContainsExpectedPlaceholders() throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("templates/inventory/facilities/list.html");
        String template = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        assertThat(template).contains("facility-table-container");
        assertThat(template).contains("${item.name}");
        assertThat(template).contains("${item.code}");
    }
}
