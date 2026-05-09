package com.solusi.erp.inventory.goodsreceipt.web.template.integration;

import com.solusi.erp.inventory.goodsreceipt.domain.model.GoodsReceiptReferenceType;
import com.solusi.erp.inventory.goodsreceipt.web.dto.GoodsReceiptSaveLineRequest;
import com.solusi.erp.inventory.goodsreceipt.web.dto.GoodsReceiptSaveRequest;
import com.solusi.erp.testutils.TemplateTestUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@DisplayName("Goods Receipt Form — Template Contract Test")
@Tag("integration-template")
class GoodsReceiptFormIntegrationTest {

    private static final String CREATE_TEMPLATE = "templates/inventory/goods-receipts/form.html";
    private static final String PO_LINE_SELECTOR_TEMPLATE = "templates/inventory/goods-receipts/fragments/po-line-selector-modal.html";
    private static final String FORM_SCRIPT = "static/js/inventory/goods-receipt/goods-receipt-form.js";

    @Test
    @DisplayName("create form template renders form with input fields")
    void createFormTemplate_renders() throws Exception {
        String template = readResource(CREATE_TEMPLATE);

        assertThat(template).contains("goods-receipt-form");
        assertThat(template).contains("form-control");
        assertThat(template).contains("type=\"submit\"");
    }

    @Test
    @DisplayName("edit form template renders prefilled form data")
    void editFormTemplate_renders() throws Exception {
        String template = readResource(CREATE_TEMPLATE);

        assertThat(template).contains("th:value");
        assertThat(template).contains("th:object");
    }

    @Test
    @DisplayName("form submit posts JSON request")
    void formSubmit_postsJSON() throws Exception {
        String template = readResource(CREATE_TEMPLATE);

        assertThat(template).contains("data-ajax-form");
        assertThat(template).contains("th:object=\"${grRequest}\"");
        assertThat(template).contains("type=\"submit\"");
    }

    @Test
    @DisplayName("create form renders read-only purchase-order snapshot header")
    void createForm_rendersReadonlyReferenceHeader() throws Exception {
        String template = readResource(CREATE_TEMPLATE);

        assertThat(template).contains("label.gr.referenceType");
        assertThat(template).contains("label.gr.referenceCode");
        assertThat(template).contains("label.gr.supplier");
        assertThat(template).contains("label.gr.facility");
        assertThat(template).contains("form-control-plaintext");
    }

    @Test
    @DisplayName("create form renders through thymeleaf without parsing errors")
    void createForm_rendersThroughThymeleafWithoutParsingErrors() {
        GoodsReceiptSaveRequest request = new GoodsReceiptSaveRequest();
        request.setReceiptDate(LocalDate.of(2026, 7, 1));
        request.setReferenceType(GoodsReceiptReferenceType.PURCHASE_ORDER);
        request.setReferenceCode("PO-001");
        request.setSupplierName("PT Supplier");
        request.setFacilityName("Main Warehouse");

        assertDoesNotThrow(() -> {
            String html = TemplateTestUtils.renderWithSecurity(
                "inventory/goods-receipts/form",
                Map.of("grRequest", request),
                auth("GOODS-RECEIPT_CREATE")
            );
            assertThat(html).isNotBlank();
        });
    }

    @Test
    @DisplayName("create form preserves generic reference fields for submit")
    void createForm_preservesGenericReferenceFieldsForSubmit() {
        GoodsReceiptSaveRequest request = new GoodsReceiptSaveRequest();
        request.setReceiptDate(LocalDate.of(2026, 7, 1));
        request.setReferenceType(GoodsReceiptReferenceType.PURCHASE_ORDER);
        request.setReferenceId(7L);
        request.setReferenceCode("PO-001");
        request.setSupplierName("PT Supplier");
        request.setFacilityName("Main Warehouse");

        assertDoesNotThrow(() -> {
            String html = TemplateTestUtils.renderWithSecurity(
                "inventory/goods-receipts/form",
                Map.of("grRequest", request),
                auth("GOODS-RECEIPT_CREATE")
            );
            assertThat(html).contains("type=\"hidden\" field=\"PURCHASE_ORDER\"");
            assertThat(html).contains("type=\"hidden\" field=\"7\"");
        });
    }

    @Test
    @DisplayName("create form renders through thymeleaf with prefilled lines")
    void createForm_rendersThroughThymeleafWithPrefilledLines() {
        GoodsReceiptSaveLineRequest line = new GoodsReceiptSaveLineRequest();
        line.setReferenceLineId(99L);
        line.setProductId(10L);
        line.setUomId(20L);
        line.setContainerId(30L);

        GoodsReceiptSaveRequest request = new GoodsReceiptSaveRequest();
        request.setReceiptDate(LocalDate.of(2026, 7, 1));
        request.setReferenceType(GoodsReceiptReferenceType.PURCHASE_ORDER);
        request.setReferenceCode("PO-001");
        request.setSupplierName("PT Supplier");
        request.setFacilityName("Main Warehouse");
        request.setLines(List.of(line));

        assertDoesNotThrow(() -> {
            String html = TemplateTestUtils.renderWithSecurity(
                "inventory/goods-receipts/form",
                Map.of("grRequest", request),
                auth("GOODS-RECEIPT_CREATE")
            );
            assertThat(html).isNotBlank();
            assertThat(html).contains("name=\"lines[0].referenceLineId\"");
            assertThat(html).contains("value=\"99\"");
        });
    }

    @Test
    @DisplayName("form template keeps referenceLineId for existing and new line rows")
    void formTemplate_keepsReferenceLineIdForExistingAndNewLineRows() throws Exception {
        String template = readResource(CREATE_TEMPLATE);

        assertThat(template).contains("].referenceLineId'");
        assertThat(template).contains("name=\"lines[INDEX].referenceLineId\"");
        assertThat(template).contains("type=\"hidden\"");
    }

    @Test
    @DisplayName("form template declares hidden generic reference fields")
    void formTemplate_declaresHiddenGenericReferenceFields() throws Exception {
        String template = readResource(CREATE_TEMPLATE);

        assertThat(template).contains("th:field=\"*{referenceType}\"");
        assertThat(template).contains("th:field=\"*{referenceId}\"");
        assertThat(template).contains("type=\"hidden\"");
    }

    @Test
    @DisplayName("rendered serialized line uses hidden draft fields and drawer-only serial editing contract")
    void renderedSerializedLine_usesHiddenDraftFieldsAndDrawerOnlySerialEditingContract() {
        Document html = renderForm(serializedRequestLine());
        Element row = requireLineRow(html, 0);

        assertThat(findInput(row, "lines[0].serialNumber")).isNotNull();
        assertThat(findInput(row, "lines[0].serialNumber").attr("type")).isEqualTo("hidden");
        assertThat(findInput(row, "lines[0].serialNumber").val()).isEqualTo("SN-1,SN-2");
        assertThat(findInput(row, "lines[0].serialized")).isNotNull();
        assertThat(findInput(row, "lines[0].serialized").attr("type")).isEqualTo("hidden");
        assertThat(findInput(row, "lines[0].serialized").val()).isEqualTo("true");
        assertThat(row.select(".input-sn-single:not([type=hidden]), .chk-serialized, .input-serial-number")).isEmpty();
        assertThat(row.selectFirst(".btn-edit-detail")).isNotNull();
        assertThat(row.selectFirst(".detail-mode-label").text()).isEqualTo("Serialized");
        assertThat(row.selectFirst(".detail-summary").text()).isEqualTo("2 serial(s) drafted");

        assertThat(html.selectFirst("#drawer-serial .input-qty-target")).isNotNull();
        assertThat(html.selectFirst("#drawer-serial .select-uom-target")).isNotNull();
        assertThat(html.selectFirst("#drawer-serial .serial-input-container")).isNotNull();
        assertThat(html.selectFirst("#drawer-serial .txt-total-qty")).isNotNull();
        assertThat(html.selectFirst("#drawer-non-serial .input-qty-target")).isNotNull();
        assertThat(html.selectFirst("#drawer-non-serial .select-uom-target")).isNotNull();
        assertThat(html.selectFirst("#drawer-non-serial .input-qty-base-display")).isNotNull();
    }

    @Test
    @DisplayName("rendered serialized line with blank serial csv shows pending summary")
    void renderedSerializedLine_withBlankSerialCsv_showsPendingSummary() {
        GoodsReceiptSaveLineRequest line = serializedRequestLine();
        line.setSerialNumber(" ,  ");

        Document html = renderForm(line);
        Element row = requireLineRow(html, 0);

        assertThat(row.selectFirst(".detail-summary").text()).isEqualTo("Serial numbers pending");
    }

    @Test
    @DisplayName("rendered non-serialized line keeps standard summary path and drawer pattern")
    void renderedNonSerializedLine_keepsStandardSummaryPathAndDrawerPattern() {
        Document html = renderForm(nonSerializedRequestLine());
        Element row = requireLineRow(html, 0);

        assertThat(findInput(row, "lines[0].serialized")).isNotNull();
        assertThat(findInput(row, "lines[0].serialized").val()).isEqualTo("false");
        assertThat(findInput(row, "lines[0].serialNumber")).isNotNull();
        assertThat(findInput(row, "lines[0].serialNumber").attr("type")).isEqualTo("hidden");
        assertThat(findInput(row, "lines[0].serialNumber").val()).isEmpty();
        assertThat(row.selectFirst(".detail-mode-label").text()).isEqualTo("Standard");
        assertThat(row.selectFirst(".detail-summary").text()).isEqualTo("Set quantity and UoM");
        assertThat(row.select(".input-sn-single:not([type=hidden]), .chk-serialized, .input-serial-number")).isEmpty();
        assertThat(row.selectFirst(".btn-edit-detail")).isNotNull();

        assertThat(html.selectFirst("#drawer-non-serial .input-qty-target")).isNotNull();
        assertThat(html.selectFirst("#drawer-non-serial .select-uom-target")).isNotNull();
        assertThat(html.selectFirst("#drawer-non-serial .btn-save-drawer")).isNotNull();
    }

    @Test
    @DisplayName("form script persists serial drawer rows back into the hidden csv field")
    void formScript_persistsSerialDrawerRowsBackIntoTheHiddenCsvField() throws Exception {
        String script = readResource(FORM_SCRIPT);
        String saveDrawerHandler = extractBlock(script, "saveButton.onclick = function ()");

        assertThat(saveDrawerHandler).containsPattern("(?s)if \\(isSerialized\\) \\{.*querySelectorAll\\('\\.input-sn-item'\\).*value\\.trim\\(\\).*serials\\.join\\(','\\).*\\} else \\{\\s*row\\.querySelector\\('\\.input-sn-single'\\)\\.value = '';\\s*\\}");
        assertThat(saveDrawerHandler).contains("row.querySelector('.input-sn-single').value = serials.join(',');");
    }

    @Test
    @DisplayName("form script derives serial drawer rows from computed base quantity")
    void formScript_derivesSerialDrawerRowsFromComputedBaseQuantity() throws Exception {
        String script = readResource(FORM_SCRIPT);
        String updateCalculations = extractBlock(script, "function updateCalculations()");

        assertThat(updateCalculations).contains("var baseQty = targetQty * factor;");
        assertThat(updateCalculations).contains("setNumericValue(qtyBase, baseQty);");
        assertThat(updateCalculations).contains("if (totalQty) totalQty.textContent = formatQuantity(baseQty);");
        assertThat(updateCalculations).containsPattern("if \\(isSerialized\\) syncSerialRows\\(drawer, Math\\.max\\(0, Math\\.floor\\(baseQty\\)\\), row\\);");
    }

    @Test
    @DisplayName("form script blocks serialized drawer save when computed base quantity is fractional")
    void formScript_blocksSerializedDrawerSaveWhenComputedBaseQuantityIsFractional() throws Exception {
        String script = readResource(FORM_SCRIPT);
        String saveDrawerHandler = extractBlock(script, "saveButton.onclick = function ()");

        assertThat(saveDrawerHandler).contains("var baseQty = getNumericValue(drawer.querySelector('.input-qty-base'));");
        assertThat(saveDrawerHandler).containsPattern("if \\(isSerialized && .*baseQty.*\\) \\{\\s*warn\\(");
        assertThat(saveDrawerHandler).containsPattern("if \\(isSerialized && .*baseQty.*\\) \\{[\\s\\S]*return;[\\s\\S]*\\}\\s*row\\.querySelector\\('\\.input-uom-id'\\)\\.value");
    }

    @Test
    @DisplayName("form script builds serialized drawer rows without raw innerHTML interpolation")
    void formScript_buildsSerializedDrawerRowsWithoutRawInnerHtmlInterpolation() throws Exception {
        String script = readResource(FORM_SCRIPT);
        String syncSerialRows = extractBlock(script, "function syncSerialRows(drawer, count, row)");

        assertThat(syncSerialRows).doesNotContain("tr.innerHTML =");
        assertThat(syncSerialRows).contains("var numberCell = document.createElement('td');");
        assertThat(syncSerialRows).contains("var serialInput = document.createElement('input');");
        assertThat(syncSerialRows).contains("serialInput.value = existingSerials[index] || '';");
        assertThat(syncSerialRows).contains("qtyCell.textContent = '1.00';");
    }

    @Test
    @DisplayName("form script routes serialized and standard lines to different drawers")
    void formScript_routesSerializedAndStandardLinesToDifferentDrawers() throws Exception {
        String script = readResource(FORM_SCRIPT);
        String editHandler = extractBlock(script, "editBtn.onclick = function ()");

        assertThat(editHandler).contains("var isSerialized = serializedInput.value === 'true';");
        assertThat(editHandler).contains("var drawerId = isSerialized ? 'drawer-serial' : 'drawer-non-serial';");
        assertThat(editHandler).containsPattern("(?s)document\\.getElementById\\(drawerId\\).*if \\(setupDrawer\\(drawer, row, isSerialized\\)\\) \\{\\s*ErpDrawer\\.open\\(drawerId\\);\\s*\\}");
    }

    @Test
    @DisplayName("save line request exposes autocomplete display fields for prefilled lines")
    void saveLineRequest_exposesAutocompleteDisplayFields() {
        assertHasGetter("getProductName");
        assertHasGetter("getProductCode");
        assertHasGetter("getUomName");
        assertHasGetter("getUomCode");
        assertHasGetter("getContainerName");
        assertHasGetter("getContainerCode");
    }

    @Test
    @DisplayName("form template includes purchase-order line selector modal shell")
    void formTemplate_includesPurchaseOrderLineSelectorModalShell() throws Exception {
        String template = readResource(CREATE_TEMPLATE);

        assertThat(template).contains("modal-gr-po-line-selector");
        assertThat(template).contains("gr-po-line-selector-results");
        assertThat(template).contains("btn-add-line");
    }

    @Test
    @DisplayName("form script builds selector URL with excludeReferenceLineIds to prevent duplicate lines")
    void formScript_buildsSelectorUrlWithExcludeReferenceLineIds() throws Exception {
        String script = readResource(FORM_SCRIPT);

        assertThat(script).contains("excludeReferenceLineIds");
        assertThat(script).contains("modal-gr-po-line-selector");
        assertThat(script).contains("window.ERP.ModalSelector.open");
    }

    @Test
    @DisplayName("selector modal template shows remaining quantity and PO unit price")
    void selectorModalTemplate_showsRemainingQuantityAndPoUnitPrice() throws Exception {
        String template = readResource(PO_LINE_SELECTOR_TEMPLATE);

        assertThat(template).contains("label.gr.selector.poLine.column.remainingQty");
        assertThat(template).contains("label.gr.selector.poLine.column.unitPrice");
        assertThat(template).contains("row.remainingQuantity");
        assertThat(template).contains("row.unitPrice");
    }

    @Test
    @DisplayName("selector modal formats numeric columns with two decimal places")
    void selectorModal_formatsNumericColumnsWithTwoDecimals() throws Exception {
        String template = readResource(PO_LINE_SELECTOR_TEMPLATE);

        assertThat(template).contains("formatDecimal(row.orderedQuantity, 1, 'COMMA', 2, 'POINT')");
        assertThat(template).contains("formatDecimal(row.receivedToDateQuantity, 1, 'COMMA', 2, 'POINT')");
        assertThat(template).contains("formatDecimal(row.remainingQuantity, 1, 'COMMA', 2, 'POINT')");
        assertThat(template).contains("formatDecimal(row.unitPrice, 1, 'COMMA', 2, 'POINT')");
    }

    @Test
    @DisplayName("form script cleans tomselect wrappers before creating dynamic line rows")
    void formScript_cleansTomSelectWrappersBeforeCreatingDynamicLineRows() throws Exception {
        String script = readResource(FORM_SCRIPT);

        assertThat(script).contains("querySelectorAll('.ts-wrapper').forEach");
        assertThat(script).contains("classList.remove('tomselect-initialized', 'tomselected', 'ts-hidden-accessible')");
    }

    @Test
    @DisplayName("form script uses tomselect option shape for product prefill from PO selector")
    void formScript_usesTomSelectOptionShapeForProductPrefill() throws Exception {
        String script = readResource(FORM_SCRIPT);

        assertThat(script).contains("id: key");
        assertThat(script).contains("name: text || key");
    }

    @Test
    @DisplayName("form script locks referenced product lookup so PO-derived product cannot be changed")
    void formScript_locksReferencedProductLookup() throws Exception {
        String script = readResource(FORM_SCRIPT);

        assertThat(script).contains("function lockLookup(selectEl)");
        assertThat(script).contains("input[name$=\".referenceLineId\"]");
        assertThat(script).contains("lockLookup(productSelect);");
    }

    @Test
    @DisplayName("form script validates container selection before submit")
    void formScript_validatesContainerSelectionBeforeSubmit() throws Exception {
        String script = readResource(FORM_SCRIPT);

        assertThat(script).contains("Please select a container in line");
        assertThat(script).contains(".select-container");
    }

    @Test
    @DisplayName("form script blocks ajax submit when client-side validation fails")
    void formScript_blocksAjaxSubmitWhenClientValidationFails() throws Exception {
        String script = readResource(FORM_SCRIPT);

        assertThat(script).contains("e.stopImmediatePropagation();");
        assertThat(script).contains("}, true);");
    }

    @Test
    @DisplayName("form script suppresses browser beforeunload warning during intentional navigation")
    void formScript_suppressesBeforeUnloadWarningDuringIntentionalNavigation() throws Exception {
        String script = readResource(FORM_SCRIPT);

        assertThat(script).contains("!window.__erpSuppressBeforeUnload");
    }

    @Test
    @DisplayName("form script keeps decimal inputs consistent with global numeric precision")
    void formScript_usesTwoDecimalPrecisionForDynamicNumericInputs() throws Exception {
        String script = readResource(FORM_SCRIPT);

        assertThat(script).contains("decimalPlaces: 2");
    }

    @Test
    @DisplayName("form template marks container column as required")
    void formTemplate_marksContainerColumnAsRequired() throws Exception {
        String template = readResource(CREATE_TEMPLATE);

        assertThat(template).contains("<th class=\"required\" th:text=\"#{label.container}\"");
    }

    @Test
    @DisplayName("form script provides fallback modal shell when selector modal is missing")
    void formScript_providesFallbackModalShellWhenSelectorModalIsMissing() throws Exception {
        String script = readResource(FORM_SCRIPT);

        assertThat(script).contains("function ensurePoLineSelectorModal()");
        assertThat(script).contains("modal-gr-po-line-selector");
        assertThat(script).contains("document.body.appendChild(modalEl);");
        assertThat(script).contains("window.bootstrap.Modal.getOrCreateInstance(modalEl).show();");
    }

    private Authentication auth(String... authorities) {
        TestingAuthenticationToken token =
            new TestingAuthenticationToken("testuser", "n/a", authorities);
        token.setAuthenticated(true);
        return token;
    }

    private String readResource(String path) throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream(path);
        assertThat(is).as("Resource not found: %s", path).isNotNull();

        try (InputStream in = is) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private void assertHasGetter(String getterName) {
        assertThat(findMethod(getterName))
            .as("GoodsReceiptSaveLineRequest should expose %s for Thymeleaf autocomplete prefill", getterName)
            .isNotNull();
    }

    private Method findMethod(String name) {
        for (Method method : GoodsReceiptSaveLineRequest.class.getMethods()) {
            if (method.getName().equals(name) && method.getParameterCount() == 0) {
                return method;
            }
        }
        return null;
    }

    private Document renderForm(GoodsReceiptSaveLineRequest line) {
        GoodsReceiptSaveRequest request = new GoodsReceiptSaveRequest();
        request.setReceiptDate(LocalDate.of(2026, 7, 1));
        request.setReferenceType(GoodsReceiptReferenceType.PURCHASE_ORDER);
        request.setReferenceCode("PO-001");
        request.setSupplierName("PT Supplier");
        request.setFacilityName("Main Warehouse");
        request.setLines(List.of(line));

        String html = TemplateTestUtils.renderWithSecurity(
            "inventory/goods-receipts/form",
            Map.of("grRequest", request),
            auth("GOODS-RECEIPT_CREATE")
        );
        assertThat(html).isNotBlank();
        return Jsoup.parse(html);
    }

    private GoodsReceiptSaveLineRequest serializedRequestLine() {
        GoodsReceiptSaveLineRequest line = baseRequestLine();
        line.setSerialized(Boolean.TRUE);
        line.setQuantityReceived(BigDecimal.valueOf(2));
        line.setSerialNumber("SN-1,SN-2");
        return line;
    }

    private GoodsReceiptSaveLineRequest nonSerializedRequestLine() {
        GoodsReceiptSaveLineRequest line = baseRequestLine();
        line.setSerialized(Boolean.FALSE);
        line.setQuantityReceived(BigDecimal.valueOf(5));
        line.setSerialNumber("");
        return line;
    }

    private GoodsReceiptSaveLineRequest baseRequestLine() {
        GoodsReceiptSaveLineRequest line = new GoodsReceiptSaveLineRequest();
        line.setReferenceLineId(99L);
        line.setProductId(10L);
        line.setProductName("Serialized Product");
        line.setProductCode("P-10");
        line.setUomId(20L);
        line.setUomName("Unit");
        line.setUomCode("PCS");
        line.setContainerId(30L);
        line.setContainerName("Main Bin");
        line.setContainerCode("BIN-01");
        return line;
    }

    private Element requireLineRow(Document html, int index) {
        Element row = html.selectFirst("tr.line-row[data-index=\"" + index + "\"]");
        assertThat(row).as("Line row %s should be rendered", index).isNotNull();
        return row;
    }

    private Element findInput(Element scope, String name) {
        return scope.selectFirst("input[name=\"" + name + "\"]");
    }

    private String extractBlock(String script, String marker) {
        int markerIndex = script.indexOf(marker);
        assertThat(markerIndex).as("Expected script marker %s", marker).isGreaterThanOrEqualTo(0);

        int braceStart = script.indexOf('{', markerIndex);
        assertThat(braceStart).as("Opening brace for %s", marker).isGreaterThanOrEqualTo(0);

        int depth = 0;
        for (int index = braceStart; index < script.length(); index++) {
            char current = script.charAt(index);
            if (current == '{') {
                depth++;
            } else if (current == '}') {
                depth--;
                if (depth == 0) {
                    return script.substring(braceStart, index + 1);
                }
            }
        }

        throw new IllegalArgumentException("Unbalanced braces for marker: " + marker);
    }
}
