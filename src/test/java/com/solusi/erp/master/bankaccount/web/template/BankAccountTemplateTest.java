package com.solusi.erp.master.bankaccount.web.template;

import com.solusi.erp.master.bankaccount.web.dto.BankAccountSummaryResponse;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

public class BankAccountTemplateTest {

    @Test
    public void templateReferencesValidProperties() throws Exception {
        InputStream is = getClass().getClassLoader()
                .getResourceAsStream("templates/master/bank-accounts/list.html");
        String template = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        Pattern p = Pattern.compile("\\$\\{item\\.([a-zA-Z0-9_]+)\\}");
        Matcher m = p.matcher(template);
        while (m.find()) {
            String prop = m.group(1);
            boolean hasField = false;
            for (java.lang.reflect.Field f : BankAccountSummaryResponse.class.getDeclaredFields()) {
                if (f.getName().equals(prop)) { hasField = true; break; }
            }
            boolean hasGetter = false;
            String getter = "get" + prop.substring(0, 1).toUpperCase() + prop.substring(1);
            String isGetter = "is" + prop.substring(0, 1).toUpperCase() + prop.substring(1);
            for (java.lang.reflect.Method method : BankAccountSummaryResponse.class.getMethods()) {
                if ((method.getName().equals(getter) || method.getName().equals(isGetter))
                        && method.getParameterCount() == 0) {
                    hasGetter = true;
                    break;
                }
            }
            assertThat(hasField || hasGetter)
                    .withFailMessage("Property '%s' referenced in template but not present on %s",
                            prop, BankAccountSummaryResponse.class.getName())
                    .isTrue();
        }
    }

    @Test
    public void bankAccountFormContainsCurrencyAndCoaControls() throws Exception {
        InputStream is = getClass().getClassLoader()
                .getResourceAsStream("templates/master/bank-accounts/form.html");
        String template = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        assertThat(template).contains("field='currencyId'");
        assertThat(template).contains("path='master/currencies'");
        assertThat(template).contains("bankAccountUI['currencyText']");
        assertThat(template).contains("bankAccountUI['currencySubtext']");
        assertThat(template).contains("*{coaId}");
        assertThat(template).contains("bank-account-coa-id");
        assertThat(template).contains("bank-account-coa-display");
        assertThat(template).contains("btn-select-bank-account-coa");
        assertThat(template).contains("modal-bank-account-coa-selector");
        assertThat(template).contains("bank-account-coa-selector-modal-body");
    }

    @Test
    public void bankAccountCoaSelectorFragmentContainsExpectedContract() throws Exception {
        InputStream is = getClass().getClassLoader()
                .getResourceAsStream("templates/master/bank-accounts/fragments/coa-selector-modal.html");
        String template = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        assertThat(template).contains("id=\"bank-account-coa-selector-modal-body\"");
        assertThat(template).contains("th:fragment=\"bank-account-coa-selector-modal-body\"");
        assertThat(template).contains("hx-get=\"/master/bank-accounts/selectors/coa\"");
        assertThat(template).contains("hx-target=\"#bank-account-coa-selector-modal-body\"");
        assertThat(template).contains("name=\"keyword\"");
        assertThat(template).contains("name=\"accountType\"");
        assertThat(template).contains("js-bank-account-coa-pick");
        assertThat(template).contains("th:data-coa-id=\"${row.id}\"");
        assertThat(template).contains("th:data-coa-code=\"${row.code}\"");
        assertThat(template).contains("th:data-coa-name=\"${row.name}\"");
        assertThat(template).contains("th:data-coa-account-type=\"${row.accountType}\"");
        assertThat(template).contains("fragments/table :: pagination(${page})");
    }
}
