package com.solusi.erp.master.party.web.template;

import com.solusi.erp.master.party.web.dto.PartySummaryResponse;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

class PartyTemplateTest {

    @Test
    void listTemplateReferencesValidSummaryProperties() throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("templates/master/parties/list.html");
        String template = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        Pattern p = Pattern.compile("\\$\\{item\\.([a-zA-Z0-9_]+)\\}");
        Matcher m = p.matcher(template);
        while (m.find()) {
            String prop = m.group(1);
            boolean hasField = false;
            for (java.lang.reflect.Field f : PartySummaryResponse.class.getDeclaredFields()) {
                if (f.getName().equals(prop)) {
                    hasField = true;
                    break;
                }
            }
            boolean hasGetter = false;
            String getter = "get" + prop.substring(0, 1).toUpperCase() + prop.substring(1);
            String isGetter = "is" + prop.substring(0, 1).toUpperCase() + prop.substring(1);
            for (java.lang.reflect.Method method : PartySummaryResponse.class.getMethods()) {
                if ((method.getName().equals(getter) || method.getName().equals(isGetter))
                        && method.getParameterCount() == 0) {
                    hasGetter = true;
                    break;
                }
            }
            assertThat(hasField || hasGetter)
                    .withFailMessage("Property '%s' referenced in template but not present on %s",
                            prop, PartySummaryResponse.class.getName())
                    .isTrue();
        }
    }

    @Test
    void listTemplateContainsExpectedPlaceholders() throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("templates/master/parties/list.html");
        String template = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        assertThat(template).contains("party-table-container");
        assertThat(template).contains("${item.name}");
        assertThat(template).contains("${item.code}");
        assertThat(template).contains("${item.roleNames}");
    }
}
