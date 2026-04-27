# SOL-53 SOL-54 PO-GR Alignment Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Align Purchase Order and Goods Receipt with the agreed procurement flow by making PO tax mandatory, restoring the Send PO action, and generalizing Goods Receipt references from `poId` to `referenceId + referenceType` while keeping the UI entry point PO-only.

**Architecture:** Keep the existing vertical slices (`purchasing.purchaseorder` and `inventory.goodsreceipt`) and make surgical changes inside their current boundaries. PO remains the commercial source document, while GR becomes reference-aware in the domain and persistence layers but continues to expose only the Purchase Order create flow in this sprint. Use TDD per slice: domain rule first, then application/web wiring, then template and docs updates.

**Tech Stack:** Java 21, Spring Boot 4, Spring Data JPA, Flyway, Thymeleaf, HTMX, Bootstrap/Tabler, MapStruct, JUnit 5, Mockito

---

## File Structure

### Purchase Order

- Modify: `src/main/java/com/solusi/erp/purchasing/purchaseorder/domain/model/PurchaseOrder.java`
- Modify: `src/main/java/com/solusi/erp/purchasing/purchaseorder/web/dto/PurchaseOrderSaveRequest.java`
- Modify: `src/main/java/com/solusi/erp/purchasing/purchaseorder/web/controller/PurchaseOrderController.java`
- Modify: `src/main/resources/templates/purchasing/purchase-orders/form.html`
- Modify: `src/main/resources/templates/purchasing/purchase-orders/view.html`
- Modify: `src/main/resources/static/js/purchasing/purchase-order-form.js`
- Modify: `src/test/java/com/solusi/erp/purchasing/purchaseorder/domain/model/PurchaseOrderTest.java`
- Modify: `src/test/java/com/solusi/erp/purchasing/purchaseorder/web/controller/PurchaseOrderControllerTest.java`
- Modify: `src/test/java/com/solusi/erp/purchasing/purchaseorder/web/template/integration/PurchaseOrderViewIntegrationTest.java`

### Goods Receipt

- Create: `src/main/java/com/solusi/erp/inventory/goodsreceipt/domain/model/GoodsReceiptReferenceType.java`
- Modify: `src/main/java/com/solusi/erp/inventory/goodsreceipt/domain/model/GoodsReceipt.java`
- Modify: `src/main/java/com/solusi/erp/inventory/goodsreceipt/domain/repository/GoodsReceiptRepository.java`
- Modify: `src/main/java/com/solusi/erp/inventory/goodsreceipt/application/usecase/command/CreateGoodsReceiptUseCase.java`
- Modify: `src/main/java/com/solusi/erp/inventory/goodsreceipt/application/usecase/command/CreateGoodsReceiptUseCaseImpl.java`
- Modify: `src/main/java/com/solusi/erp/inventory/goodsreceipt/application/usecase/query/GetGoodsReceiptCreateViewUseCase.java`
- Modify: `src/main/java/com/solusi/erp/inventory/goodsreceipt/application/usecase/query/GetGoodsReceiptCreateViewUseCaseImpl.java`
- Modify: `src/main/java/com/solusi/erp/inventory/goodsreceipt/application/usecase/query/CountGoodsReceiptsByPoUseCase.java`
- Modify: `src/main/java/com/solusi/erp/inventory/goodsreceipt/application/usecase/query/CountGoodsReceiptsByPoUseCaseImpl.java`
- Modify: `src/main/java/com/solusi/erp/inventory/goodsreceipt/infrastructure/persistence/GoodsReceiptEntity.java`
- Modify: `src/main/java/com/solusi/erp/inventory/goodsreceipt/infrastructure/persistence/GoodsReceiptJpaRepository.java`
- Modify: `src/main/java/com/solusi/erp/inventory/goodsreceipt/infrastructure/persistence/GoodsReceiptPersistenceMapper.java`
- Modify: `src/main/java/com/solusi/erp/inventory/goodsreceipt/infrastructure/adapter/GoodsReceiptRepositoryImpl.java`
- Modify: `src/main/java/com/solusi/erp/inventory/goodsreceipt/web/dto/GoodsReceiptSaveRequest.java`
- Modify: `src/main/java/com/solusi/erp/inventory/goodsreceipt/web/dto/GoodsReceiptDetailResponse.java`
- Modify: `src/main/java/com/solusi/erp/inventory/goodsreceipt/web/dto/GoodsReceiptSummaryResponse.java`
- Modify: `src/main/java/com/solusi/erp/inventory/goodsreceipt/web/mapper/GoodsReceiptWebMapper.java`
- Modify: `src/main/java/com/solusi/erp/inventory/goodsreceipt/web/controller/GoodsReceiptController.java`
- Modify: `src/main/resources/templates/inventory/goods-receipts/form.html`
- Modify: `src/main/resources/templates/inventory/goods-receipts/list.html`
- Modify: `src/main/resources/templates/inventory/goods-receipts/view.html`
- Modify: `src/main/resources/db/migration/V50__Add_Goods_Receipt_Module.sql` (reference only; do not edit)
- Create: `src/main/resources/db/migration/V51__Generalize_Goods_Receipt_Reference.sql`
- Modify: `src/test/java/com/solusi/erp/inventory/goodsreceipt/domain/model/GoodsReceiptTest.java`
- Modify: `src/test/java/com/solusi/erp/inventory/goodsreceipt/infrastructure/persistence/GoodsReceiptPersistenceMapperTest.java`
- Modify: `src/test/java/com/solusi/erp/inventory/goodsreceipt/application/usecase/query/GetGoodsReceiptCreateViewUseCaseTest.java`
- Modify: `src/test/java/com/solusi/erp/inventory/goodsreceipt/web/controller/GoodsReceiptControllerTest.java`
- Modify: `src/test/java/com/solusi/erp/inventory/goodsreceipt/web/template/integration/GoodsReceiptFormIntegrationTest.java`
- Modify: `src/test/java/com/solusi/erp/inventory/goodsreceipt/web/template/integration/GoodsReceiptListIntegrationTest.java`
- Modify: `src/test/java/com/solusi/erp/inventory/goodsreceipt/web/template/integration/GoodsReceiptViewIntegrationTest.java`

### Cross-cutting docs and i18n

- Modify: `src/main/resources/messages.properties`
- Modify: `src/main/resources/messages_id.properties`
- Modify: `docs/modules/procurement/purchase-order.md`
- Create: `docs/modules/inventory/goods-receipt.md`

---

### Task 1: Enforce mandatory PO tax selection

**Files:**
- Modify: `src/test/java/com/solusi/erp/purchasing/purchaseorder/domain/model/PurchaseOrderTest.java`
- Modify: `src/main/java/com/solusi/erp/purchasing/purchaseorder/domain/model/PurchaseOrder.java`
- Modify: `src/main/java/com/solusi/erp/purchasing/purchaseorder/web/dto/PurchaseOrderSaveRequest.java`
- Modify: `src/main/resources/templates/purchasing/purchase-orders/form.html`

- [ ] **Step 1: Write the failing tax-required tests**

```java
@Test
@DisplayName("createNew requires tax selection instead of null tax snapshot")
void createNew_withoutTaxSelection_throwsDomainException() {
    assertThatThrownBy(() -> PurchaseOrder.createNew(
            "PO-001",
            LocalDate.of(2026, 7, 14),
            null,
            1L, 2L, 1L,
            BigDecimal.ONE,
            30, null, PurchaseOrderType.DIRECT,
            null, null, null, BigDecimal.ZERO,
            TaxCalculationMode.EXCLUSIVE,
            null,
            List.of(createDefaultLine())
    ))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining("msg.error.po.tax.required");
}

@Test
@DisplayName("update requires tax selection instead of clearing tax snapshot")
void update_withoutTaxSelection_throwsDomainException() {
    PurchaseOrder po = PurchaseOrder.createNew(
            "PO-002",
            LocalDate.of(2026, 7, 14),
            null,
            1L, 2L, 1L,
            BigDecimal.ONE,
            30, null, PurchaseOrderType.DIRECT,
            10L, "NON-TAX", "Non Tax", BigDecimal.ZERO,
            TaxCalculationMode.EXCLUSIVE,
            null,
            List.of(createDefaultLine())
    );

    assertThatThrownBy(() -> po.update(
            LocalDate.of(2026, 7, 15),
            null,
            2L, 1L, BigDecimal.ONE, 30,
            null, null, null, BigDecimal.ZERO,
            TaxCalculationMode.EXCLUSIVE,
            null,
            List.of(createDefaultLine())
    ))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining("msg.error.po.tax.required");
}
```

- [ ] **Step 2: Run the domain test to verify it fails**

Run: `./mvnw -q -Dtest=PurchaseOrderTest test`

Expected: FAIL with missing `msg.error.po.tax.required` validation.

- [ ] **Step 3: Implement minimal mandatory-tax validation**

```java
// PurchaseOrderSaveRequest.java
@NotNull(message = "{label.po.tax} {validation.notnull.suffix}")
private Long taxId;
```

```java
// PurchaseOrder.java
private static void validateHeaderTax(Long taxId, String taxCode, String taxName) {
    if (taxId == null || taxCode == null || taxCode.isBlank() || taxName == null || taxName.isBlank()) {
        throw new DomainException("msg.error.po.tax.required");
    }
}

public static PurchaseOrder createNew(..., Long taxId, String taxCode, String taxName, BigDecimal taxRate,
                                      TaxCalculationMode taxCalculationMode, String note, List<PurchaseOrderLine> lines) {
    validateExchangeRate(exchangeRate);
    validateExpectedDate(orderDate, expectedDate);
    validatePoType(poType, prId);
    validateHeaderTax(taxId, taxCode, taxName);
    ...
}

public void update(..., Long taxId, String taxCode, String taxName, BigDecimal taxRate,
                   TaxCalculationMode taxCalculationMode, String note, List<PurchaseOrderLine> lines) {
    if (!status.canUpdate()) {
        throw new DomainException("msg.error.po.update.not.draft");
    }
    validateExchangeRate(exchangeRate);
    validateExpectedDate(orderDate, expectedDate);
    validateHeaderTax(taxId, taxCode, taxName);
    ...
}
```

```html
<!-- purchase-orders/form.html -->
<label class="form-label required" th:text="#{label.po.tax}">Tax</label>
<select id="header-tax"
        name="taxId"
        th:disabled="${isLocked}"
        required
        data-lookup-path="master/taxes"
        class="form-select erp-input-ts">
```

- [ ] **Step 4: Run the tax-focused test again**

Run: `./mvnw -q -Dtest=PurchaseOrderTest test`

Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/solusi/erp/purchasing/purchaseorder/domain/model/PurchaseOrder.java \
        src/main/java/com/solusi/erp/purchasing/purchaseorder/web/dto/PurchaseOrderSaveRequest.java \
        src/main/resources/templates/purchasing/purchase-orders/form.html \
        src/test/java/com/solusi/erp/purchasing/purchaseorder/domain/model/PurchaseOrderTest.java
git commit -m "fix: require explicit tax selection for purchase orders"
```

### Task 2: Restore Send PO action in the real user flow

**Files:**
- Modify: `src/test/java/com/solusi/erp/purchasing/purchaseorder/web/controller/PurchaseOrderControllerTest.java`
- Modify: `src/test/java/com/solusi/erp/purchasing/purchaseorder/web/template/integration/PurchaseOrderViewIntegrationTest.java`
- Modify: `src/main/resources/templates/purchasing/purchase-orders/view.html`
- Modify: `src/main/resources/static/js/purchasing/purchase-order-form.js`

- [ ] **Step 1: Write the failing UI contract tests for Send PO**

```java
@Test
@DisplayName("view exposes send action when PO is approved")
void view_populatesSendFlagWhenPoApproved() {
    PurchaseOrder po = buildApprovedPo();
    PurchaseOrderDetailResponse detail = new PurchaseOrderDetailResponse();
    detail.setId(3L);
    detail.setStatus(PurchaseOrderStatus.APPROVED);

    when(editViewUc.execute(3L)).thenReturn(Optional.of(po));
    when(webMapper.toDetailResponse(po)).thenReturn(detail);
    when(countGoodsReceiptsByPoUseCase.execute(3L)).thenReturn(0L);

    Model model = new ExtendedModelMap();
    controller.view(3L, model, null);

    assertThat(model.getAttribute("canSendPurchaseOrder")).isEqualTo(true);
}
```

```java
@Test
@DisplayName("po detail template renders send button for approved status")
void poDetailTemplate_rendersSendButtonForApprovedStatus() throws Exception {
    String template = readResource(TEMPLATE);

    assertThat(template).contains("canSendPurchaseOrder");
    assertThat(template).contains("#{label.po.action.send}");
    assertThat(template).contains("hasAuthority('PO_SEND')");
    assertThat(template).contains("/purchasing/purchase-orders/");
}
```

- [ ] **Step 2: Run the controller/template tests to verify they fail**

Run: `./mvnw -q -Dtest=PurchaseOrderControllerTest,PurchaseOrderViewIntegrationTest test`

Expected: FAIL because the view model does not expose a send flag and the detail template does not render a send action.

- [ ] **Step 3: Implement Send PO visibility and click handling**

```java
// PurchaseOrderController.java
@GetMapping("/view/{id}")
@PreAuthorize("hasAuthority('PO_READ')")
public String view(@PathVariable Long id, Model model,
                   @AuthenticationPrincipal UserDetails principal) {
    PurchaseOrder domain = getPurchaseOrderEditViewUseCase.execute(id)
            .orElseThrow(() -> new RuntimeException("Purchase order not found"));
    model.addAttribute("po", webMapper.toDetailResponse(domain));
    model.addAttribute("goodsReceiptCount", countGoodsReceiptsByPoUseCase.execute(id));
    model.addAttribute("canCreateGoodsReceipt", domain.getStatus().canReceive());
    model.addAttribute("canSendPurchaseOrder", domain.getStatus().canSend());
    ...
    return "purchasing/purchase-orders/view";
}
```

```html
<!-- purchase-orders/view.html -->
<div class="btn-list mt-3">
    <button th:if="${canSendPurchaseOrder}"
            type="button"
            class="btn btn-primary"
            sec:authorize="hasAuthority('PO_SEND')"
            th:data-send-url="@{'/purchasing/purchase-orders/' + ${po.id} + '/send'}"
            onclick="ErpForm.postAction(this)">
        <i class="ti ti-truck me-1"></i>
        <span th:text="#{label.po.action.send}">Send to Supplier</span>
    </button>
</div>
```

```javascript
// purchase-order-form.js
var btnSendPo = document.getElementById('btn-send-po');
if (btnSendPo) {
    btnSendPo.addEventListener('click', function () {
        fetch(btnSendPo.dataset.sendUrl, {
            method: 'POST',
            headers: {
                'Accept': 'application/json',
                [config.csrfHeader]: config.csrfToken
            }
        })
            .then(function (res) { return res.json().then(function (data) { return { ok: res.ok, data: data }; }); })
            .then(function (result) {
                if (!result.ok) throw new Error(result.data.message || 'Error');
                window.location.href = '/purchasing/purchase-orders';
            })
            .catch(function (err) {
                if (window.ErpModal) ErpModal.showError(err.message);
                else alert(err.message);
            });
    });
}
```

- [ ] **Step 4: Run the focused PO UI tests again**

Run: `./mvnw -q -Dtest=PurchaseOrderControllerTest,PurchaseOrderViewIntegrationTest test`

Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/solusi/erp/purchasing/purchaseorder/web/controller/PurchaseOrderController.java \
        src/main/resources/templates/purchasing/purchase-orders/view.html \
        src/main/resources/static/js/purchasing/purchase-order-form.js \
        src/test/java/com/solusi/erp/purchasing/purchaseorder/web/controller/PurchaseOrderControllerTest.java \
        src/test/java/com/solusi/erp/purchasing/purchaseorder/web/template/integration/PurchaseOrderViewIntegrationTest.java
git commit -m "fix: restore send purchase order action in ui flow"
```

### Task 3: Generalize Goods Receipt aggregate and persistence to reference-aware fields

**Files:**
- Create: `src/main/java/com/solusi/erp/inventory/goodsreceipt/domain/model/GoodsReceiptReferenceType.java`
- Modify: `src/test/java/com/solusi/erp/inventory/goodsreceipt/domain/model/GoodsReceiptTest.java`
- Modify: `src/main/java/com/solusi/erp/inventory/goodsreceipt/domain/model/GoodsReceipt.java`
- Modify: `src/main/java/com/solusi/erp/inventory/goodsreceipt/infrastructure/persistence/GoodsReceiptEntity.java`
- Modify: `src/main/java/com/solusi/erp/inventory/goodsreceipt/infrastructure/persistence/GoodsReceiptJpaRepository.java`
- Modify: `src/main/java/com/solusi/erp/inventory/goodsreceipt/infrastructure/persistence/GoodsReceiptPersistenceMapper.java`
- Modify: `src/main/java/com/solusi/erp/inventory/goodsreceipt/domain/repository/GoodsReceiptRepository.java`
- Modify: `src/main/java/com/solusi/erp/inventory/goodsreceipt/infrastructure/adapter/GoodsReceiptRepositoryImpl.java`
- Create: `src/main/resources/db/migration/V51__Generalize_Goods_Receipt_Reference.sql`
- Modify: `src/test/java/com/solusi/erp/inventory/goodsreceipt/infrastructure/persistence/GoodsReceiptPersistenceMapperTest.java`

- [ ] **Step 1: Write failing tests for the new GR reference model**

```java
@Test
@DisplayName("goods receipt keeps purchase-order reference as typed reference")
void createNew_keepsTypedReference() {
    GoodsReceipt receipt = GoodsReceipt.createNew(
            "GR-202604-00001",
            LocalDate.of(2026, 4, 26),
            GoodsReceiptReferenceType.PURCHASE_ORDER,
            7L,
            11L, 3L, 1L, BigDecimal.ONE,
            List.of()
    );

    assertThat(receipt.getReferenceType()).isEqualTo(GoodsReceiptReferenceType.PURCHASE_ORDER);
    assertThat(receipt.getReferenceId()).isEqualTo(7L);
}
```

```java
@Test
void toEntity_mapsReferenceTypeAndReferenceId() {
    GoodsReceipt receipt = GoodsReceipt.createNew(
            "GR-202604-00001",
            LocalDate.of(2026, 4, 26),
            GoodsReceiptReferenceType.PURCHASE_ORDER,
            7L,
            11L, 3L, 1L, BigDecimal.ONE,
            List.of()
    );

    GoodsReceiptEntity entity = mapper.toEntity(receipt);

    assertThat(entity.getReferenceType()).isEqualTo(GoodsReceiptReferenceType.PURCHASE_ORDER);
    assertThat(entity.getReferenceId()).isEqualTo(7L);
}
```

- [ ] **Step 2: Run the domain/persistence tests to verify they fail**

Run: `./mvnw -q -Dtest=GoodsReceiptTest,GoodsReceiptPersistenceMapperTest test`

Expected: FAIL because `GoodsReceiptReferenceType`, `referenceType`, and `referenceId` do not exist yet.

- [ ] **Step 3: Implement the typed reference model and migration**

```java
// GoodsReceiptReferenceType.java
package com.solusi.erp.inventory.goodsreceipt.domain.model;

public enum GoodsReceiptReferenceType {
    PURCHASE_ORDER,
    SALES_RETURN,
    MANUAL,
    PRODUCTION
}
```

```java
// GoodsReceipt.java
private final GoodsReceiptReferenceType referenceType;
private final Long referenceId;

public static GoodsReceipt createNew(String code, LocalDate receiptDate,
                                     GoodsReceiptReferenceType referenceType, Long referenceId,
                                     Long supplierId, Long facilityId, Long currencyId,
                                     BigDecimal exchangeRate, List<GoodsReceiptLine> lines) {
    return new GoodsReceipt(
            AuditMetadata.empty(), code, receiptDate, referenceType, referenceId,
            supplierId, facilityId, currencyId, exchangeRate,
            GoodsReceiptStatus.DRAFT, null, lines
    );
}

public GoodsReceiptReferenceType getReferenceType() { return referenceType; }
public Long getReferenceId() { return referenceId; }
```

```java
// GoodsReceiptEntity.java
@Enumerated(EnumType.STRING)
@Column(name = "reference_type", nullable = false, length = 40)
private GoodsReceiptReferenceType referenceType;

@Column(name = "reference_id", nullable = false)
private Long referenceId;
```

```sql
-- V51__Generalize_Goods_Receipt_Reference.sql
ALTER TABLE pur_goods_receipts
    ADD COLUMN reference_type VARCHAR(40) NULL AFTER receipt_date,
    ADD COLUMN reference_id BIGINT NULL AFTER reference_type;

UPDATE pur_goods_receipts
SET reference_type = 'PURCHASE_ORDER',
    reference_id = po_id
WHERE reference_type IS NULL
  AND reference_id IS NULL;

ALTER TABLE pur_goods_receipts
    MODIFY COLUMN reference_type VARCHAR(40) NOT NULL,
    MODIFY COLUMN reference_id BIGINT NOT NULL;

CREATE INDEX idx_gr_reference ON pur_goods_receipts(reference_type, reference_id);
```

- [ ] **Step 4: Run the GR domain/persistence tests again**

Run: `./mvnw -q -Dtest=GoodsReceiptTest,GoodsReceiptPersistenceMapperTest test`

Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/solusi/erp/inventory/goodsreceipt/domain/model/GoodsReceiptReferenceType.java \
        src/main/java/com/solusi/erp/inventory/goodsreceipt/domain/model/GoodsReceipt.java \
        src/main/java/com/solusi/erp/inventory/goodsreceipt/domain/repository/GoodsReceiptRepository.java \
        src/main/java/com/solusi/erp/inventory/goodsreceipt/infrastructure/persistence/GoodsReceiptEntity.java \
        src/main/java/com/solusi/erp/inventory/goodsreceipt/infrastructure/persistence/GoodsReceiptJpaRepository.java \
        src/main/java/com/solusi/erp/inventory/goodsreceipt/infrastructure/persistence/GoodsReceiptPersistenceMapper.java \
        src/main/java/com/solusi/erp/inventory/goodsreceipt/infrastructure/adapter/GoodsReceiptRepositoryImpl.java \
        src/main/resources/db/migration/V51__Generalize_Goods_Receipt_Reference.sql \
        src/test/java/com/solusi/erp/inventory/goodsreceipt/domain/model/GoodsReceiptTest.java \
        src/test/java/com/solusi/erp/inventory/goodsreceipt/infrastructure/persistence/GoodsReceiptPersistenceMapperTest.java
git commit -m "refactor: generalize goods receipt references"
```

### Task 4: Keep GR create flow PO-only but render read-only reference header

**Files:**
- Modify: `src/test/java/com/solusi/erp/inventory/goodsreceipt/application/usecase/query/GetGoodsReceiptCreateViewUseCaseTest.java`
- Modify: `src/test/java/com/solusi/erp/inventory/goodsreceipt/web/controller/GoodsReceiptControllerTest.java`
- Modify: `src/test/java/com/solusi/erp/inventory/goodsreceipt/web/template/integration/GoodsReceiptFormIntegrationTest.java`
- Modify: `src/main/java/com/solusi/erp/inventory/goodsreceipt/application/usecase/query/GetGoodsReceiptCreateViewUseCase.java`
- Modify: `src/main/java/com/solusi/erp/inventory/goodsreceipt/application/usecase/query/GetGoodsReceiptCreateViewUseCaseImpl.java`
- Modify: `src/main/java/com/solusi/erp/inventory/goodsreceipt/application/usecase/command/CreateGoodsReceiptUseCase.java`
- Modify: `src/main/java/com/solusi/erp/inventory/goodsreceipt/application/usecase/command/CreateGoodsReceiptUseCaseImpl.java`
- Modify: `src/main/java/com/solusi/erp/inventory/goodsreceipt/web/dto/GoodsReceiptSaveRequest.java`
- Modify: `src/main/java/com/solusi/erp/inventory/goodsreceipt/web/dto/GoodsReceiptDetailResponse.java`
- Modify: `src/main/java/com/solusi/erp/inventory/goodsreceipt/web/mapper/GoodsReceiptWebMapper.java`
- Modify: `src/main/java/com/solusi/erp/inventory/goodsreceipt/web/controller/GoodsReceiptController.java`
- Modify: `src/main/resources/templates/inventory/goods-receipts/form.html`

- [ ] **Step 1: Write failing tests for read-only PO snapshot on GR draft**

```java
@Test
void buildDraftFromPo_prefillsTypedReferenceAndHeaderSnapshot() {
    PurchaseOrder po = sentPoWithOutstandingLines();
    when(purchaseOrderRepository.findById(7L)).thenReturn(Optional.of(po));

    GoodsReceipt draft = useCase.execute(7L);

    assertThat(draft.getReferenceType()).isEqualTo(GoodsReceiptReferenceType.PURCHASE_ORDER);
    assertThat(draft.getReferenceId()).isEqualTo(7L);
    assertThat(draft.getSupplierId()).isEqualTo(11L);
    assertThat(draft.getFacilityId()).isEqualTo(3L);
}
```

```java
@Test
@DisplayName("form template renders read-only reference header and no free reference editor")
void createFormTemplate_rendersReadonlyReferenceHeader() throws Exception {
    String template = readResource(CREATE_TEMPLATE);

    assertThat(template).contains("label.gr.referenceType");
    assertThat(template).contains("label.gr.referenceCode");
    assertThat(template).contains("label.gr.supplier");
    assertThat(template).contains("form-control-plaintext");
    assertThat(template).doesNotContain("name=\"referenceId\"");
}
```

- [ ] **Step 2: Run the GR create-view and template tests to verify they fail**

Run: `./mvnw -q -Dtest=GetGoodsReceiptCreateViewUseCaseTest,GoodsReceiptControllerTest,GoodsReceiptFormIntegrationTest test`

Expected: FAIL because the create use case and template still model GR as `poId` without typed reference fields.

- [ ] **Step 3: Implement PO-only create flow on top of typed references**

```java
// GetGoodsReceiptCreateViewUseCaseImpl.java
@Override
public GoodsReceipt execute(Long poId) {
    PurchaseOrder po = purchaseOrderRepository.findById(poId)
            .orElseThrow(() -> new DomainException("msg.error.po.notfound"));
    if (!po.getStatus().canReceive()) {
        throw new DomainException("msg.error.gr.po.invalid.status");
    }

    return GoodsReceipt.createNew(
            sequenceGeneratorService.next("GOODS_RECEIPT"),
            LocalDate.now(),
            GoodsReceiptReferenceType.PURCHASE_ORDER,
            po.getId(),
            po.getSupplierId(),
            po.getFacilityId(),
            po.getCurrencyId(),
            po.getExchangeRate(),
            buildOutstandingLines(po)
    );
}
```

```java
// GoodsReceiptDetailResponse.java
private GoodsReceiptReferenceType referenceType;
private Long referenceId;
private String referenceCode;
private Long supplierId;
private String supplierName;
private Long facilityId;
private String facilityName;
private String currencyCode;
```

```java
// GoodsReceiptWebMapper.java
@Mapping(target = "referenceCode", source = "referenceId", qualifiedByName = "getReferenceCode")
@Mapping(target = "supplierName", source = "supplierId", qualifiedByName = "getSupplierName")
@Mapping(target = "facilityName", source = "facilityId", qualifiedByName = "getFacilityName")
GoodsReceiptDetailResponse toDetailResponse(GoodsReceipt goodsReceipt);

@Named("getReferenceCode")
protected String getReferenceCode(Long referenceId, @Context GoodsReceipt goodsReceipt) {
    if (goodsReceipt.getReferenceType() != GoodsReceiptReferenceType.PURCHASE_ORDER || referenceId == null) {
        return null;
    }
    return purchaseOrderRepository.findById(referenceId).map(PurchaseOrder::getCode).orElse(null);
}
```

```html
<!-- inventory/goods-receipts/form.html -->
<div class="row g-3 mb-3">
    <div class="col-md-3">
        <label class="form-label text-secondary" th:text="#{label.gr.referenceType}">Reference Type</label>
        <div class="form-control-plaintext" th:text="${grRequest.referenceType}">PURCHASE_ORDER</div>
    </div>
    <div class="col-md-3">
        <label class="form-label text-secondary" th:text="#{label.gr.referenceCode}">Reference</label>
        <div class="form-control-plaintext" th:text="${grRequest.referenceCode ?: '-'}">PO-001</div>
    </div>
    <div class="col-md-3">
        <label class="form-label text-secondary" th:text="#{label.gr.supplier}">Supplier</label>
        <div class="form-control-plaintext" th:text="${grRequest.supplierName ?: '-'}">PT Supplier</div>
    </div>
    <div class="col-md-3">
        <label class="form-label text-secondary" th:text="#{label.gr.facility}">Facility</label>
        <div class="form-control-plaintext" th:text="${grRequest.facilityName ?: '-'}">Main Warehouse</div>
    </div>
</div>
```

- [ ] **Step 4: Run the GR create-view tests again**

Run: `./mvnw -q -Dtest=GetGoodsReceiptCreateViewUseCaseTest,GoodsReceiptControllerTest,GoodsReceiptFormIntegrationTest test`

Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/solusi/erp/inventory/goodsreceipt/application/usecase/query/GetGoodsReceiptCreateViewUseCase.java \
        src/main/java/com/solusi/erp/inventory/goodsreceipt/application/usecase/query/GetGoodsReceiptCreateViewUseCaseImpl.java \
        src/main/java/com/solusi/erp/inventory/goodsreceipt/application/usecase/command/CreateGoodsReceiptUseCase.java \
        src/main/java/com/solusi/erp/inventory/goodsreceipt/application/usecase/command/CreateGoodsReceiptUseCaseImpl.java \
        src/main/java/com/solusi/erp/inventory/goodsreceipt/web/dto/GoodsReceiptSaveRequest.java \
        src/main/java/com/solusi/erp/inventory/goodsreceipt/web/dto/GoodsReceiptDetailResponse.java \
        src/main/java/com/solusi/erp/inventory/goodsreceipt/web/mapper/GoodsReceiptWebMapper.java \
        src/main/java/com/solusi/erp/inventory/goodsreceipt/web/controller/GoodsReceiptController.java \
        src/main/resources/templates/inventory/goods-receipts/form.html \
        src/test/java/com/solusi/erp/inventory/goodsreceipt/application/usecase/query/GetGoodsReceiptCreateViewUseCaseTest.java \
        src/test/java/com/solusi/erp/inventory/goodsreceipt/web/controller/GoodsReceiptControllerTest.java \
        src/test/java/com/solusi/erp/inventory/goodsreceipt/web/template/integration/GoodsReceiptFormIntegrationTest.java
git commit -m "refactor: prefill goods receipt from typed purchase order reference"
```

### Task 5: Remove standalone create from GR list and keep PO-focused reference displays

**Files:**
- Modify: `src/test/java/com/solusi/erp/inventory/goodsreceipt/web/template/integration/GoodsReceiptListIntegrationTest.java`
- Modify: `src/test/java/com/solusi/erp/inventory/goodsreceipt/web/template/integration/GoodsReceiptViewIntegrationTest.java`
- Modify: `src/main/java/com/solusi/erp/inventory/goodsreceipt/web/dto/GoodsReceiptSummaryResponse.java`
- Modify: `src/main/resources/templates/inventory/goods-receipts/list.html`
- Modify: `src/main/resources/templates/inventory/goods-receipts/view.html`

- [ ] **Step 1: Write failing template tests for the new GR list/view contract**

```java
@Test
@DisplayName("list template does not expose standalone create button")
void listTemplate_hidesStandaloneCreateButton() throws Exception {
    String template = readResource(TEMPLATE);

    assertThat(template).doesNotContain("/inventory/goods-receipts/create");
    assertThat(template).contains("#{label.gr.referenceCode}");
}
```

```java
@Test
@DisplayName("view template renders reference labels instead of po-only labels")
void viewTemplate_rendersReferenceFields() throws Exception {
    String template = readResource(TEMPLATE);

    assertThat(template).contains("label.gr.referenceType");
    assertThat(template).contains("label.gr.referenceCode");
    assertThat(template).doesNotContain("label.gr.poCode");
}
```

- [ ] **Step 2: Run the GR list/view template tests to verify they fail**

Run: `./mvnw -q -Dtest=GoodsReceiptListIntegrationTest,GoodsReceiptViewIntegrationTest test`

Expected: FAIL because the list still contains a global create button and the templates still use PO-only labels.

- [ ] **Step 3: Implement the list/view template contract changes**

```java
// GoodsReceiptSummaryResponse.java
private GoodsReceiptReferenceType referenceType;
private String referenceCode;
private String supplierName;
private GoodsReceiptStatus status;
```

```html
<!-- inventory/goods-receipts/list.html -->
<div class="col">
    <h2 class="page-title" th:text="#{label.gr.title}">Goods Receipts</h2>
    <div class="text-secondary mt-1" th:text="#{label.gr.subtitle}">
        Manage goods receipt documents.
    </div>
</div>
```

```html
<!-- list table header -->
<th th:text="#{label.gr.column.referenceType}">Reference Type</th>
<th th:text="#{label.gr.column.referenceCode}">Reference Code</th>
```

```html
<!-- inventory/goods-receipts/view.html -->
<label class="form-label text-secondary" th:text="#{label.gr.referenceType}">Reference Type</label>
<div class="form-control-plaintext" th:text="${gr.referenceType}">PURCHASE_ORDER</div>

<label class="form-label text-secondary" th:text="#{label.gr.referenceCode}">Reference Code</label>
<div class="form-control-plaintext" th:text="${gr.referenceCode ?: '-'}">PO-001</div>
```

- [ ] **Step 4: Run the GR list/view template tests again**

Run: `./mvnw -q -Dtest=GoodsReceiptListIntegrationTest,GoodsReceiptViewIntegrationTest test`

Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/solusi/erp/inventory/goodsreceipt/web/dto/GoodsReceiptSummaryResponse.java \
        src/main/resources/templates/inventory/goods-receipts/list.html \
        src/main/resources/templates/inventory/goods-receipts/view.html \
        src/test/java/com/solusi/erp/inventory/goodsreceipt/web/template/integration/GoodsReceiptListIntegrationTest.java \
        src/test/java/com/solusi/erp/inventory/goodsreceipt/web/template/integration/GoodsReceiptViewIntegrationTest.java
git commit -m "fix: make goods receipt list audit-only and reference-aware"
```

### Task 6: Update i18n, module docs, and run full verification

**Files:**
- Modify: `src/main/resources/messages.properties`
- Modify: `src/main/resources/messages_id.properties`
- Modify: `docs/modules/procurement/purchase-order.md`
- Create: `docs/modules/inventory/goods-receipt.md`

- [ ] **Step 1: Write the doc/i18n gaps as failing checklist assertions**

```text
Need these new keys:
- msg.error.po.tax.required
- label.gr.referenceType
- label.gr.referenceCode
- label.gr.column.referenceType
- label.gr.column.referenceCode

Need these doc updates:
- purchase-order.md explicitly states Tax header is mandatory and Send PO is a user action after approval
- new goods-receipt.md explains typed reference model and PO-only create entry for this sprint
```

- [ ] **Step 2: Update i18n and docs**

```properties
# messages.properties
msg.error.po.tax.required=Purchase order tax must be selected.
label.gr.referenceType=Reference Type
label.gr.referenceCode=Reference Code
label.gr.column.referenceType=Reference Type
label.gr.column.referenceCode=Reference Code
```

```properties
# messages_id.properties
msg.error.po.tax.required=Pajak purchase order wajib dipilih.
label.gr.referenceType=Tipe Referensi
label.gr.referenceCode=Kode Referensi
label.gr.column.referenceType=Tipe Referensi
label.gr.column.referenceCode=Kode Referensi
```

```md
<!-- docs/modules/inventory/goods-receipt.md -->
# Goods Receipt (GR)

## Scope Sprint 4 Alignment

- Create GR tetap dimulai dari Purchase Order berstatus `SENT` atau `PARTIALLY_RECEIVED`
- Aggregate GR menyimpan `referenceType` dan `referenceId`
- Untuk sprint ini, nilai aktif `referenceType` hanya `PURCHASE_ORDER`
- Header draft GR menampilkan snapshot referensi secara read-only
- Halaman list GR adalah halaman monitoring/audit, bukan entry point create
```

- [ ] **Step 3: Run targeted tests and then the full suite**

Run: `./mvnw -q -Dtest=PurchaseOrderTest,PurchaseOrderControllerTest,PurchaseOrderViewIntegrationTest,GoodsReceiptTest,GoodsReceiptPersistenceMapperTest,GetGoodsReceiptCreateViewUseCaseTest,GoodsReceiptControllerTest,GoodsReceiptFormIntegrationTest,GoodsReceiptListIntegrationTest,GoodsReceiptViewIntegrationTest test`

Expected: PASS

Run: `./mvnw clean test -q`

Expected: PASS

- [ ] **Step 4: Commit**

```bash
git add src/main/resources/messages.properties \
        src/main/resources/messages_id.properties \
        docs/modules/procurement/purchase-order.md \
        docs/modules/inventory/goods-receipt.md
git commit -m "docs: align procurement and goods receipt references"
```

---

## Self-Review

### Spec coverage

- **SOL-53 tax required**: covered by Task 1.
- **SOL-53 send PO button**: covered by Task 2.
- **SOL-54 GR pre-add header info**: covered by Task 4.
- **SOL-54 remove create button from GR list**: covered by Task 5.
- **SOL-54 replace `poId` with `referenceId + referenceType`**: covered by Task 3 and Task 4.
- **Keep GR entry PO-only for now**: covered by Task 4 and Task 6 docs.

### Placeholder scan

- No `TODO`, `TBD`, or “similar to previous task” placeholders remain.
- Every task includes exact file paths, test commands, implementation snippets, and commit commands.

### Type consistency

- Domain enum name is `GoodsReceiptReferenceType` to avoid collision with inventory stock `ReferenceType`.
- Web layer still speaks in PO terms only when `referenceType == PURCHASE_ORDER`; otherwise it uses neutral reference labels.

