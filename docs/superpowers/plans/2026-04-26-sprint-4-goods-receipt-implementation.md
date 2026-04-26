# Sprint 4 Goods Receipt Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the full local Goods Receipt flow for Sprint 4 so warehouse users can create, edit, complete, and audit receipts sourced from Purchase Orders while updating stock, FIFO layers, and PO receiving status safely.

**Architecture:** Implement a new vertical slice at `inventory.goodsreceipt` that owns the warehouse-facing GR workflow, while reusing `purchasing.purchaseorder` as the source of truth for commercial values and outstanding quantities. Completion stays atomic and inventory-first: validate the period and latest PO state, write stock movements and valuation layers, update PO receipt status, and persist posting-ready financial snapshots without creating real journal entries yet.

**Tech Stack:** Java 21, Spring Boot 4.0.3, Spring Data JPA, Flyway, Thymeleaf SSR, Tabler, HTMX, AutoNumeric, TomSelect, MapStruct, JUnit 5, Mockito

---

## File Structure

### New inventory goods receipt slice

- Create: `src/main/java/com/solusi/erp/inventory/goodsreceipt/domain/model/GoodsReceipt.java` — aggregate root for GR draft/complete lifecycle.
- Create: `src/main/java/com/solusi/erp/inventory/goodsreceipt/domain/model/GoodsReceiptLine.java` — receipt line with container, quantity, serial snapshot, and posting-ready values.
- Create: `src/main/java/com/solusi/erp/inventory/goodsreceipt/domain/model/GoodsReceiptStatus.java` — `DRAFT | COMPLETED`.
- Create: `src/main/java/com/solusi/erp/inventory/goodsreceipt/domain/repository/GoodsReceiptRepository.java` — domain repository port.
- Create: `src/main/java/com/solusi/erp/inventory/goodsreceipt/application/usecase/command/GoodsReceiptLineCommand.java` — draft/edit line command model.
- Create: `src/main/java/com/solusi/erp/inventory/goodsreceipt/application/usecase/command/CreateGoodsReceiptUseCase.java`
- Create: `src/main/java/com/solusi/erp/inventory/goodsreceipt/application/usecase/command/CreateGoodsReceiptUseCaseImpl.java`
- Create: `src/main/java/com/solusi/erp/inventory/goodsreceipt/application/usecase/command/UpdateGoodsReceiptUseCase.java`
- Create: `src/main/java/com/solusi/erp/inventory/goodsreceipt/application/usecase/command/UpdateGoodsReceiptUseCaseImpl.java`
- Create: `src/main/java/com/solusi/erp/inventory/goodsreceipt/application/usecase/command/DeleteGoodsReceiptUseCase.java`
- Create: `src/main/java/com/solusi/erp/inventory/goodsreceipt/application/usecase/command/DeleteGoodsReceiptUseCaseImpl.java`
- Create: `src/main/java/com/solusi/erp/inventory/goodsreceipt/application/usecase/command/CompleteGoodsReceiptUseCase.java`
- Create: `src/main/java/com/solusi/erp/inventory/goodsreceipt/application/usecase/command/CompleteGoodsReceiptUseCaseImpl.java`
- Create: `src/main/java/com/solusi/erp/inventory/goodsreceipt/application/usecase/query/FindGoodsReceiptsUseCase.java`
- Create: `src/main/java/com/solusi/erp/inventory/goodsreceipt/application/usecase/query/FindGoodsReceiptsUseCaseImpl.java`
- Create: `src/main/java/com/solusi/erp/inventory/goodsreceipt/application/usecase/query/GetGoodsReceiptUseCase.java`
- Create: `src/main/java/com/solusi/erp/inventory/goodsreceipt/application/usecase/query/GetGoodsReceiptUseCaseImpl.java`
- Create: `src/main/java/com/solusi/erp/inventory/goodsreceipt/application/usecase/query/GetGoodsReceiptEditViewUseCase.java`
- Create: `src/main/java/com/solusi/erp/inventory/goodsreceipt/application/usecase/query/GetGoodsReceiptEditViewUseCaseImpl.java`
- Create: `src/main/java/com/solusi/erp/inventory/goodsreceipt/application/usecase/query/GetGoodsReceiptCreateViewUseCase.java`
- Create: `src/main/java/com/solusi/erp/inventory/goodsreceipt/application/usecase/query/GetGoodsReceiptCreateViewUseCaseImpl.java`
- Create: `src/main/java/com/solusi/erp/inventory/goodsreceipt/application/usecase/query/CountGoodsReceiptsByPoUseCase.java`
- Create: `src/main/java/com/solusi/erp/inventory/goodsreceipt/application/usecase/query/CountGoodsReceiptsByPoUseCaseImpl.java`
- Create: `src/main/java/com/solusi/erp/inventory/goodsreceipt/infrastructure/persistence/GoodsReceiptEntity.java`
- Create: `src/main/java/com/solusi/erp/inventory/goodsreceipt/infrastructure/persistence/GoodsReceiptLineEntity.java`
- Create: `src/main/java/com/solusi/erp/inventory/goodsreceipt/infrastructure/persistence/GoodsReceiptJpaRepository.java`
- Create: `src/main/java/com/solusi/erp/inventory/goodsreceipt/infrastructure/persistence/GoodsReceiptPersistenceMapper.java`
- Create: `src/main/java/com/solusi/erp/inventory/goodsreceipt/infrastructure/adapter/GoodsReceiptRepositoryImpl.java`
- Create: `src/main/java/com/solusi/erp/inventory/goodsreceipt/infrastructure/adapter/GoodsReceiptFacilityUsageChecker.java`
- Create: `src/main/java/com/solusi/erp/inventory/goodsreceipt/infrastructure/adapter/GoodsReceiptLineContainerUsageChecker.java`
- Create: `src/main/java/com/solusi/erp/inventory/goodsreceipt/infrastructure/config/GoodsReceiptConfig.java`
- Create: `src/main/java/com/solusi/erp/inventory/goodsreceipt/web/dto/GoodsReceiptSaveRequest.java`
- Create: `src/main/java/com/solusi/erp/inventory/goodsreceipt/web/dto/GoodsReceiptSaveLineRequest.java`
- Create: `src/main/java/com/solusi/erp/inventory/goodsreceipt/web/dto/GoodsReceiptDetailResponse.java`
- Create: `src/main/java/com/solusi/erp/inventory/goodsreceipt/web/dto/GoodsReceiptLineDetailResponse.java`
- Create: `src/main/java/com/solusi/erp/inventory/goodsreceipt/web/dto/GoodsReceiptSummaryResponse.java`
- Create: `src/main/java/com/solusi/erp/inventory/goodsreceipt/web/mapper/GoodsReceiptWebMapper.java`
- Create: `src/main/java/com/solusi/erp/inventory/goodsreceipt/web/controller/GoodsReceiptController.java`

### Accounting period lookup support

- Create: `src/main/java/com/solusi/erp/accounting/period/domain/port/OpenAccountingPeriodLookup.java` — period guard port for downstream modules.
- Create: `src/main/java/com/solusi/erp/accounting/period/infrastructure/adapter/OpenAccountingPeriodLookupImpl.java` — JPA-backed implementation.
- Create: `src/main/java/com/solusi/erp/accounting/period/application/usecase/query/EnsureOpenPeriodForDateUseCase.java`
- Create: `src/main/java/com/solusi/erp/accounting/period/application/usecase/query/EnsureOpenPeriodForDateUseCaseImpl.java`
- Modify: `src/main/java/com/solusi/erp/accounting/period/infrastructure/persistence/AccountingPeriodJpaRepository.java` — add `findOpenPeriodContaining(LocalDate)` query.
- Modify: `src/main/java/com/solusi/erp/accounting/period/infrastructure/config/PeriodConfig.java` — wire the new port and use case.

### Existing modules to modify

- Modify: `src/main/java/com/solusi/erp/purchasing/purchaseorder/domain/model/PurchaseOrder.java` — add receipt recording and status recomputation.
- Modify: `src/main/java/com/solusi/erp/purchasing/purchaseorder/domain/model/PurchaseOrderLine.java` — make `receivedQuantity` mutable through domain methods.
- Modify: `src/main/java/com/solusi/erp/purchasing/purchaseorder/web/controller/PurchaseOrderController.java` — inject receipt count and create-GR links into PO detail.
- Modify: `src/main/resources/templates/purchasing/purchase-orders/view.html` — add document-flow buttons.
- Modify: `src/main/java/com/solusi/erp/inventory/stock/infrastructure/service/StockServiceImpl.java` — normalize cost to base UOM before FIFO layer creation.
- Modify: `src/main/resources/messages.properties`
- Modify: `src/main/resources/messages_id.properties`
- Create: `src/main/resources/db/migration/V50__Add_Goods_Receipt_Module.sql` — new migration for GR tables, permission group, permissions, sequence, and grants.
- Create: `src/main/resources/templates/inventory/goods-receipts/list.html`
- Create: `src/main/resources/templates/inventory/goods-receipts/form.html`
- Create: `src/main/resources/templates/inventory/goods-receipts/view.html`
- Create: `src/main/resources/templates/inventory/goods-receipts/drawer-fragments.html`
- Create: `src/main/resources/static/js/inventory/goods-receipt/goods-receipt-form.js`
- Modify: `docs/modules/procurement/purchase-order.md`
- Modify: `docs/modules/inventory/stock-utility.md`

### Tests to create or update

- Create: `src/test/java/com/solusi/erp/inventory/goodsreceipt/domain/model/GoodsReceiptTest.java`
- Create: `src/test/java/com/solusi/erp/inventory/goodsreceipt/application/usecase/query/GetGoodsReceiptCreateViewUseCaseTest.java`
- Create: `src/test/java/com/solusi/erp/inventory/goodsreceipt/application/usecase/query/FindGoodsReceiptsUseCaseTest.java`
- Create: `src/test/java/com/solusi/erp/inventory/goodsreceipt/application/usecase/command/CreateGoodsReceiptUseCaseTest.java`
- Create: `src/test/java/com/solusi/erp/inventory/goodsreceipt/application/usecase/command/UpdateGoodsReceiptUseCaseTest.java`
- Create: `src/test/java/com/solusi/erp/inventory/goodsreceipt/application/usecase/command/CompleteGoodsReceiptUseCaseTest.java`
- Create: `src/test/java/com/solusi/erp/inventory/goodsreceipt/infrastructure/persistence/GoodsReceiptPersistenceMapperTest.java`
- Create: `src/test/java/com/solusi/erp/inventory/goodsreceipt/web/controller/GoodsReceiptControllerTest.java`
- Create: `src/test/java/com/solusi/erp/inventory/goodsreceipt/web/template/integration/GoodsReceiptFormIntegrationTest.java`
- Create: `src/test/java/com/solusi/erp/inventory/goodsreceipt/web/template/integration/GoodsReceiptListIntegrationTest.java`
- Create: `src/test/java/com/solusi/erp/inventory/goodsreceipt/web/template/integration/GoodsReceiptViewIntegrationTest.java`
- Create: `src/test/java/com/solusi/erp/accounting/period/application/usecase/query/EnsureOpenPeriodForDateUseCaseTest.java`
- Modify: `src/test/java/com/solusi/erp/purchasing/purchaseorder/domain/model/PurchaseOrderTest.java`
- Modify: `src/test/java/com/solusi/erp/purchasing/purchaseorder/web/controller/PurchaseOrderControllerTest.java`
- Modify: `src/test/java/com/solusi/erp/purchasing/purchaseorder/web/template/integration/PurchaseOrderViewIntegrationTest.java`
- Modify: `src/test/java/com/solusi/erp/inventory/stock/infrastructure/service/StockServiceTest.java`

---

### Task 1: Add receipt-aware PO domain behavior

**Files:**
- Modify: `src/main/java/com/solusi/erp/purchasing/purchaseorder/domain/model/PurchaseOrder.java`
- Modify: `src/main/java/com/solusi/erp/purchasing/purchaseorder/domain/model/PurchaseOrderLine.java`
- Test: `src/test/java/com/solusi/erp/purchasing/purchaseorder/domain/model/PurchaseOrderTest.java`

- [ ] **Step 1: Write the failing PO receipt tests**

```java
@Test
@DisplayName("recordReceipt partial updates received quantity and status")
void recordReceipt_partial_updatesReceivedQuantityAndStatus() {
    PurchaseOrderLine line = PurchaseOrderLine.rehydrate(
            new AuditMetadata(11L, 1L, null, null, null, null),
            1L, 10L, new BigDecimal("10.0000"), BigDecimal.ZERO, 1L,
            new BigDecimal("100.00"), BigDecimal.ZERO,
            new BigDecimal("1000.0000"), BigDecimal.ZERO, new BigDecimal("1000.0000"),
            null, null
    );
    PurchaseOrder po = createPOWithStatus(PurchaseOrderStatus.SENT, new ArrayList<>(List.of(line)));

    po.recordReceipt(Map.of(11L, new BigDecimal("4.0000")));

    assertThat(po.getLines().get(0).getReceivedQuantity()).isEqualByComparingTo("4.0000");
    assertThat(po.getStatus()).isEqualTo(PurchaseOrderStatus.PARTIALLY_RECEIVED);
}

@Test
@DisplayName("recordReceipt full closes PO into fully received")
void recordReceipt_full_marksPoFullyReceived() {
    PurchaseOrderLine line = PurchaseOrderLine.rehydrate(
            new AuditMetadata(12L, 1L, null, null, null, null),
            1L, 10L, new BigDecimal("10.0000"), BigDecimal.ZERO, 1L,
            new BigDecimal("100.00"), BigDecimal.ZERO,
            new BigDecimal("1000.0000"), BigDecimal.ZERO, new BigDecimal("1000.0000"),
            null, null
    );
    PurchaseOrder po = createPOWithStatus(PurchaseOrderStatus.SENT, new ArrayList<>(List.of(line)));

    po.recordReceipt(Map.of(12L, new BigDecimal("10.0000")));

    assertThat(po.getStatus()).isEqualTo(PurchaseOrderStatus.FULLY_RECEIVED);
}
```

- [ ] **Step 2: Run the PO domain test to verify it fails**

Run: `cd F:\solusi-program-erp; .\mvnw.cmd -q -Dtest=PurchaseOrderTest test`

Expected: FAIL with missing `recordReceipt(...)`, immutable `receivedQuantity`, or wrong status behavior.

- [ ] **Step 3: Implement minimal receipt mutation in the PO aggregate**

```java
// PurchaseOrderLine.java
private BigDecimal receivedQuantity;

public BigDecimal getOutstandingQuantity() {
    return quantity.subtract(receivedQuantity != null ? receivedQuantity : BigDecimal.ZERO);
}

public void receive(BigDecimal qty) {
    if (qty == null || qty.compareTo(BigDecimal.ZERO) <= 0) {
        throw new DomainException("msg.error.gr.line.quantity.positive");
    }
    BigDecimal next = (receivedQuantity != null ? receivedQuantity : BigDecimal.ZERO).add(qty);
    if (next.compareTo(quantity) > 0) {
        throw new DomainException("msg.error.gr.line.exceeds.outstanding");
    }
    this.receivedQuantity = next;
}

// PurchaseOrder.java
public void recordReceipt(Map<Long, BigDecimal> receivedByLineId) {
    if (!status.canReceive()) {
        throw new DomainException("msg.error.gr.po.invalid.status");
    }
    for (PurchaseOrderLine line : lines) {
        Long lineId = line.getId();
        if (lineId != null && receivedByLineId.containsKey(lineId)) {
            line.receive(receivedByLineId.get(lineId));
        }
    }
    boolean fullyReceived = lines.stream()
            .allMatch(line -> line.getOutstandingQuantity().compareTo(BigDecimal.ZERO) == 0);
    this.status = fullyReceived ? PurchaseOrderStatus.FULLY_RECEIVED : PurchaseOrderStatus.PARTIALLY_RECEIVED;
}
```

- [ ] **Step 4: Run the PO domain test again**

Run: `cd F:\solusi-program-erp; .\mvnw.cmd -q -Dtest=PurchaseOrderTest test`

Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/solusi/erp/purchasing/purchaseorder/domain/model/PurchaseOrder.java src/main/java/com/solusi/erp/purchasing/purchaseorder/domain/model/PurchaseOrderLine.java src/test/java/com/solusi/erp/purchasing/purchaseorder/domain/model/PurchaseOrderTest.java
git commit -m "feat: add purchase order receipt domain behavior"
```

### Task 2: Create the goods receipt aggregate and line model

**Files:**
- Create: `src/main/java/com/solusi/erp/inventory/goodsreceipt/domain/model/GoodsReceiptStatus.java`
- Create: `src/main/java/com/solusi/erp/inventory/goodsreceipt/domain/model/GoodsReceiptLine.java`
- Create: `src/main/java/com/solusi/erp/inventory/goodsreceipt/domain/model/GoodsReceipt.java`
- Create: `src/main/java/com/solusi/erp/inventory/goodsreceipt/domain/repository/GoodsReceiptRepository.java`
- Test: `src/test/java/com/solusi/erp/inventory/goodsreceipt/domain/model/GoodsReceiptTest.java`

- [ ] **Step 1: Write the failing goods receipt domain tests**

```java
@Test
@DisplayName("complete requires at least one positive-quantity line")
void complete_requiresPositiveLine() {
    GoodsReceipt receipt = GoodsReceipt.createNew(
            "GR-202604-00001",
            LocalDate.of(2026, 4, 26),
            7L, 11L, 3L, 1L, BigDecimal.ONE,
            List.of(GoodsReceiptLine.prefill(
                    101L, 201L, 301L, false,
                    BigDecimal.ZERO, 1L, null,
                    new BigDecimal("150.00"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    null
            ))
    );

    assertThatThrownBy(receipt::complete)
            .isInstanceOf(DomainException.class)
            .hasMessageContaining("msg.error.gr.complete.no.lines");
}

@Test
@DisplayName("completed receipt cannot be updated")
void completedReceipt_cannotBeUpdated() {
    GoodsReceiptLine active = GoodsReceiptLine.prefill(
            101L, 201L, 301L, false,
            new BigDecimal("2.0000"), 1L, 99L,
            new BigDecimal("150.00"), new BigDecimal("2.0000"), new BigDecimal("300.0000"),
            BigDecimal.ZERO, new BigDecimal("300.0000"), null
    );
    GoodsReceipt receipt = GoodsReceipt.createNew("GR-202604-00001", LocalDate.of(2026, 4, 26), 7L, 11L, 3L, 1L, BigDecimal.ONE, List.of(active));
    receipt.complete();

    assertThatThrownBy(() -> receipt.update(LocalDate.of(2026, 4, 27), "late edit", List.of(active)))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining("msg.error.gr.completed.immutable");
}
```

- [ ] **Step 2: Run the goods receipt domain test to verify it fails**

Run: `cd F:\solusi-program-erp; .\mvnw.cmd -q -Dtest=GoodsReceiptTest test`

Expected: FAIL because the GR classes do not exist yet.

- [ ] **Step 3: Implement the aggregate skeleton**

```java
// GoodsReceiptStatus.java
public enum GoodsReceiptStatus {
    DRAFT,
    COMPLETED
}

// GoodsReceiptLine.java
public class GoodsReceiptLine {
    private final Long poLineId;
    private final Long productId;
    private final Long sourceFacilityId;
    private final Boolean serialized;
    private BigDecimal quantityReceived;
    private final Long uomId;
    private Long containerId;
    private BigDecimal unitPrice;
    private BigDecimal baseQuantity;
    private BigDecimal inventoryAmount;
    private BigDecimal taxBaseAmount;
    private BigDecimal taxAmount;
    private BigDecimal grIrAmount;
    private String serialNumber;

    public static GoodsReceiptLine prefill(Long poLineId, Long productId, Long sourceFacilityId, Boolean serialized,
                                           BigDecimal quantityReceived, Long uomId, Long containerId,
                                           BigDecimal unitPrice, BigDecimal baseQuantity, BigDecimal inventoryAmount,
                                           BigDecimal taxBaseAmount, BigDecimal taxAmount, BigDecimal grIrAmount,
                                           String serialNumber) {
        return new GoodsReceiptLine(poLineId, productId, sourceFacilityId, serialized, quantityReceived, uomId,
                containerId, unitPrice, baseQuantity, inventoryAmount, taxBaseAmount, taxAmount, grIrAmount, serialNumber);
    }

    public boolean hasReceiptQuantity() {
        return quantityReceived != null && quantityReceived.compareTo(BigDecimal.ZERO) > 0;
    }
}

// GoodsReceipt.java
public class GoodsReceipt {
    public static GoodsReceipt createNew(String code, LocalDate receiptDate, Long poId, Long supplierId,
                                         Long facilityId, Long currencyId, BigDecimal exchangeRate,
                                         List<GoodsReceiptLine> lines) {
        return new GoodsReceipt(AuditMetadata.empty(), code, receiptDate, poId, supplierId, facilityId,
                currencyId, exchangeRate, GoodsReceiptStatus.DRAFT, null, lines);
    }

    public void update(LocalDate receiptDate, String note, List<GoodsReceiptLine> lines) {
        if (status == GoodsReceiptStatus.COMPLETED) {
            throw new DomainException("msg.error.gr.completed.immutable");
        }
        this.receiptDate = receiptDate;
        this.note = note;
        this.lines = new ArrayList<>(lines);
    }

    public void complete() {
        if (status == GoodsReceiptStatus.COMPLETED) {
            throw new DomainException("msg.error.gr.completed.immutable");
        }
        boolean hasPositiveLine = lines.stream().anyMatch(GoodsReceiptLine::hasReceiptQuantity);
        if (!hasPositiveLine) {
            throw new DomainException("msg.error.gr.complete.no.lines");
        }
        this.status = GoodsReceiptStatus.COMPLETED;
    }
}
```

- [ ] **Step 4: Run the goods receipt domain test again**

Run: `cd F:\solusi-program-erp; .\mvnw.cmd -q -Dtest=GoodsReceiptTest test`

Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/solusi/erp/inventory/goodsreceipt/domain/model/GoodsReceiptStatus.java src/main/java/com/solusi/erp/inventory/goodsreceipt/domain/model/GoodsReceiptLine.java src/main/java/com/solusi/erp/inventory/goodsreceipt/domain/model/GoodsReceipt.java src/main/java/com/solusi/erp/inventory/goodsreceipt/domain/repository/GoodsReceiptRepository.java src/test/java/com/solusi/erp/inventory/goodsreceipt/domain/model/GoodsReceiptTest.java
git commit -m "feat: add goods receipt domain aggregate"
```

### Task 3: Build draft/query and save use cases around PO outstanding lines

**Files:**
- Create: `src/main/java/com/solusi/erp/inventory/goodsreceipt/application/usecase/command/GoodsReceiptLineCommand.java`
- Create: `src/main/java/com/solusi/erp/inventory/goodsreceipt/application/usecase/command/CreateGoodsReceiptUseCase.java`
- Create: `src/main/java/com/solusi/erp/inventory/goodsreceipt/application/usecase/command/CreateGoodsReceiptUseCaseImpl.java`
- Create: `src/main/java/com/solusi/erp/inventory/goodsreceipt/application/usecase/command/UpdateGoodsReceiptUseCase.java`
- Create: `src/main/java/com/solusi/erp/inventory/goodsreceipt/application/usecase/command/UpdateGoodsReceiptUseCaseImpl.java`
- Create: `src/main/java/com/solusi/erp/inventory/goodsreceipt/application/usecase/command/DeleteGoodsReceiptUseCase.java`
- Create: `src/main/java/com/solusi/erp/inventory/goodsreceipt/application/usecase/command/DeleteGoodsReceiptUseCaseImpl.java`
- Create: `src/main/java/com/solusi/erp/inventory/goodsreceipt/application/usecase/query/GetGoodsReceiptCreateViewUseCase.java`
- Create: `src/main/java/com/solusi/erp/inventory/goodsreceipt/application/usecase/query/GetGoodsReceiptCreateViewUseCaseImpl.java`
- Create: `src/main/java/com/solusi/erp/inventory/goodsreceipt/application/usecase/query/GetGoodsReceiptUseCase.java`
- Create: `src/main/java/com/solusi/erp/inventory/goodsreceipt/application/usecase/query/GetGoodsReceiptUseCaseImpl.java`
- Create: `src/main/java/com/solusi/erp/inventory/goodsreceipt/application/usecase/query/GetGoodsReceiptEditViewUseCase.java`
- Create: `src/main/java/com/solusi/erp/inventory/goodsreceipt/application/usecase/query/GetGoodsReceiptEditViewUseCaseImpl.java`
- Create: `src/main/java/com/solusi/erp/inventory/goodsreceipt/application/usecase/query/FindGoodsReceiptsUseCase.java`
- Create: `src/main/java/com/solusi/erp/inventory/goodsreceipt/application/usecase/query/FindGoodsReceiptsUseCaseImpl.java`
- Create: `src/main/java/com/solusi/erp/inventory/goodsreceipt/application/usecase/query/CountGoodsReceiptsByPoUseCase.java`
- Create: `src/main/java/com/solusi/erp/inventory/goodsreceipt/application/usecase/query/CountGoodsReceiptsByPoUseCaseImpl.java`
- Test: `src/test/java/com/solusi/erp/inventory/goodsreceipt/application/usecase/query/GetGoodsReceiptCreateViewUseCaseTest.java`
- Test: `src/test/java/com/solusi/erp/inventory/goodsreceipt/application/usecase/query/FindGoodsReceiptsUseCaseTest.java`
- Test: `src/test/java/com/solusi/erp/inventory/goodsreceipt/application/usecase/command/CreateGoodsReceiptUseCaseTest.java`
- Test: `src/test/java/com/solusi/erp/inventory/goodsreceipt/application/usecase/command/UpdateGoodsReceiptUseCaseTest.java`

- [ ] **Step 1: Write failing use case tests for prefill and stale-draft rejection**

```java
@Test
void buildDraftFromPo_prefillsOutstandingLinesWithZeroQty() {
    PurchaseOrder po = sentPoWithOutstandingLines();
    when(purchaseOrderRepository.findById(7L)).thenReturn(Optional.of(po));

    GoodsReceipt draft = useCase.execute(7L);

    assertThat(draft.getPoId()).isEqualTo(7L);
    assertThat(draft.getStatus()).isEqualTo(GoodsReceiptStatus.DRAFT);
    assertThat(draft.getLines()).hasSize(2);
    assertThat(draft.getLines()).allMatch(line -> line.getQuantityReceived().compareTo(BigDecimal.ZERO) == 0);
}

@Test
void updateDraft_whenRequestedQtyExceedsLatestOutstanding_throws() {
    GoodsReceipt existing = draftReceipt();
    PurchaseOrder po = sentPoWithOutstanding("3.0000");
    when(goodsReceiptRepository.findById(1L)).thenReturn(Optional.of(existing));
    when(purchaseOrderRepository.findById(existing.getPoId())).thenReturn(Optional.of(po));

    assertThatThrownBy(() -> useCase.execute(1L, LocalDate.of(2026, 4, 26), "save",
            List.of(new GoodsReceiptLineCommand(null, 101L, 201L, true, new BigDecimal("5.0000"), 1L, 99L, "SN-1,SN-2"))))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining("msg.error.gr.stale.po.changed");
}
```

- [ ] **Step 2: Run the goods receipt application tests to verify they fail**

Run: `cd F:\solusi-program-erp; .\mvnw.cmd -q -Dtest=GetGoodsReceiptCreateViewUseCaseTest,CreateGoodsReceiptUseCaseTest,UpdateGoodsReceiptUseCaseTest,FindGoodsReceiptsUseCaseTest test`

Expected: FAIL because the use case contracts and command model do not exist yet.

- [ ] **Step 3: Implement the save/query use cases**

```java
public record GoodsReceiptLineCommand(
        Long id,
        Long poLineId,
        Long productId,
        Boolean serialized,
        BigDecimal quantityReceived,
        Long uomId,
        Long containerId,
        String serialNumber
) {}

public class GetGoodsReceiptCreateViewUseCaseImpl implements GetGoodsReceiptCreateViewUseCase {
    @Override
    public GoodsReceipt execute(Long poId) {
        PurchaseOrder po = purchaseOrderRepository.findById(poId)
                .orElseThrow(() -> new DomainException("msg.error.po.notfound"));
        if (!po.getStatus().canReceive()) {
            throw new DomainException("msg.error.gr.po.invalid.status");
        }
        List<GoodsReceiptLine> lines = po.getLines().stream()
                .filter(line -> line.getOutstandingQuantity().compareTo(BigDecimal.ZERO) > 0)
                .map(line -> GoodsReceiptLine.prefill(
                        line.getId(),
                        line.getProductId(),
                        po.getFacilityId(),
                        Boolean.FALSE,
                        BigDecimal.ZERO,
                        line.getUomId(),
                        null,
                        line.getUnitPrice(),
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        null
                ))
                .toList();
        return GoodsReceipt.createNew(null, LocalDate.now(), po.getId(), po.getSupplierId(),
                po.getFacilityId(), po.getCurrencyId(), po.getExchangeRate(), lines);
    }
}

private void validateLatestOutstanding(PurchaseOrder po, List<GoodsReceiptLineCommand> commands) {
    Map<Long, BigDecimal> outstandingByPoLine = po.getLines().stream()
            .collect(Collectors.toMap(PurchaseOrderLine::getId, PurchaseOrderLine::getOutstandingQuantity));
    Map<Long, BigDecimal> requestedByPoLine = commands.stream()
            .filter(cmd -> cmd.quantityReceived() != null && cmd.quantityReceived().compareTo(BigDecimal.ZERO) > 0)
            .collect(Collectors.groupingBy(GoodsReceiptLineCommand::poLineId,
                    Collectors.mapping(GoodsReceiptLineCommand::quantityReceived,
                            Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))));
    for (Map.Entry<Long, BigDecimal> entry : requestedByPoLine.entrySet()) {
        BigDecimal outstanding = outstandingByPoLine.getOrDefault(entry.getKey(), BigDecimal.ZERO);
        if (entry.getValue().compareTo(outstanding) > 0) {
            throw new DomainException("msg.error.gr.stale.po.changed");
        }
    }
}
```

- [ ] **Step 4: Run the goods receipt application tests again**

Run: `cd F:\solusi-program-erp; .\mvnw.cmd -q -Dtest=GetGoodsReceiptCreateViewUseCaseTest,CreateGoodsReceiptUseCaseTest,UpdateGoodsReceiptUseCaseTest,FindGoodsReceiptsUseCaseTest test`

Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/solusi/erp/inventory/goodsreceipt/application/usecase src/test/java/com/solusi/erp/inventory/goodsreceipt/application/usecase
git commit -m "feat: add goods receipt draft and query use cases"
```

### Task 4: Add period guard lookup and receipt completion flow

**Files:**
- Create: `src/main/java/com/solusi/erp/accounting/period/domain/port/OpenAccountingPeriodLookup.java`
- Create: `src/main/java/com/solusi/erp/accounting/period/infrastructure/adapter/OpenAccountingPeriodLookupImpl.java`
- Create: `src/main/java/com/solusi/erp/accounting/period/application/usecase/query/EnsureOpenPeriodForDateUseCase.java`
- Create: `src/main/java/com/solusi/erp/accounting/period/application/usecase/query/EnsureOpenPeriodForDateUseCaseImpl.java`
- Modify: `src/main/java/com/solusi/erp/accounting/period/infrastructure/persistence/AccountingPeriodJpaRepository.java`
- Modify: `src/main/java/com/solusi/erp/accounting/period/infrastructure/config/PeriodConfig.java`
- Create: `src/main/java/com/solusi/erp/inventory/goodsreceipt/application/usecase/command/CompleteGoodsReceiptUseCase.java`
- Create: `src/main/java/com/solusi/erp/inventory/goodsreceipt/application/usecase/command/CompleteGoodsReceiptUseCaseImpl.java`
- Modify: `src/main/java/com/solusi/erp/inventory/stock/infrastructure/service/StockServiceImpl.java`
- Test: `src/test/java/com/solusi/erp/accounting/period/application/usecase/query/EnsureOpenPeriodForDateUseCaseTest.java`
- Test: `src/test/java/com/solusi/erp/inventory/goodsreceipt/application/usecase/command/CompleteGoodsReceiptUseCaseTest.java`
- Test: `src/test/java/com/solusi/erp/inventory/stock/infrastructure/service/StockServiceTest.java`

- [ ] **Step 1: Write failing tests for open-period enforcement and normalized FIFO cost**

```java
@Test
void execute_whenNoOpenPeriodForDate_throws() {
    when(openAccountingPeriodLookup.findOpenPeriodContaining(LocalDate.of(2026, 4, 26)))
            .thenReturn(Optional.empty());

    assertThatThrownBy(() -> useCase.execute(LocalDate.of(2026, 4, 26)))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining("msg.error.period.not.open");
}

@Test
void complete_callsStockServiceAndUpdatesPurchaseOrder() {
    GoodsReceipt receipt = draftReceiptWithOneActiveLine();
    when(goodsReceiptRepository.findById(1L)).thenReturn(Optional.of(receipt));
    when(purchaseOrderRepository.findById(receipt.getPoId())).thenReturn(Optional.of(sentPoWithOutstanding("10.0000")));

    completeUseCase.execute(1L);

    verify(stockService).adjust(argThat(payload ->
            payload.getReferenceType() == ReferenceType.GOODS_RECEIPT &&
            payload.getUomId().equals(1L) &&
            payload.getExchangeRate().compareTo(BigDecimal.ONE) == 0));
    verify(purchaseOrderRepository).save(any(PurchaseOrder.class));
    verify(goodsReceiptRepository).save(any(GoodsReceipt.class));
}

@Test
void receiptCost_isNormalizedToBaseUomBeforeLayerCreation() {
    StockMovementPayload payload = StockMovementPayload.builder()
            .productId(1L)
            .containerId(1L)
            .quantity(new BigDecimal("10"))
            .uomId(2L)
            .movementType(MovementType.RECEIPT)
            .currencyId(1L)
            .exchangeRate(new BigDecimal("15000"))
            .netPrice(new BigDecimal("120.00"))
            .build();

    when(uomConversionService.convertToBaseUom(1L, 2L, new BigDecimal("10"))).thenReturn(new BigDecimal("120"));

    stockService.adjust(payload);

    verify(fifoValuationService).addLayer(eq(1L), eq(1L), isNull(), eq(new BigDecimal("120")),
            argThat(cost -> cost.originalAmount().compareTo(new BigDecimal("10.000000")) == 0));
}
```

- [ ] **Step 2: Run the new tests to verify they fail**

Run: `cd F:\solusi-program-erp; .\mvnw.cmd -q -Dtest=EnsureOpenPeriodForDateUseCaseTest,CompleteGoodsReceiptUseCaseTest,StockServiceTest test`

Expected: FAIL because the period lookup, completion use case, and normalization logic do not exist yet.

- [ ] **Step 3: Implement the period lookup, completion flow, and cost normalization**

```java
// AccountingPeriodJpaRepository.java
@Query("SELECT p FROM AccountingPeriod p WHERE p.status = 'OPEN' AND :date BETWEEN p.startDate AND p.endDate")
Optional<AccountingPeriod> findOpenPeriodContaining(@Param("date") LocalDate date);

// EnsureOpenPeriodForDateUseCaseImpl.java
public void execute(LocalDate date) {
    openAccountingPeriodLookup.findOpenPeriodContaining(date)
            .orElseThrow(() -> new DomainException("msg.error.period.not.open"));
}

// CompleteGoodsReceiptUseCaseImpl.java
public void execute(Long id) {
    GoodsReceipt receipt = goodsReceiptRepository.findById(id)
            .orElseThrow(() -> new DomainException("msg.error.gr.notfound"));
    ensureOpenPeriodForDateUseCase.execute(receipt.getReceiptDate());

    PurchaseOrder po = purchaseOrderRepository.findById(receipt.getPoId())
            .orElseThrow(() -> new DomainException("msg.error.po.notfound"));
    validateLatestOutstanding(po, receipt.getLines());

    receipt.complete();
    for (GoodsReceiptLine line : receipt.getLines()) {
        if (!line.hasReceiptQuantity()) continue;
        if (Boolean.TRUE.equals(line.getSerialized())) {
            for (String serial : resolveSerials(line)) {
                stockService.adjust(basePayload(receipt, line, BigDecimal.ONE, serial));
            }
        } else {
            stockService.adjust(basePayload(receipt, line, line.getQuantityReceived(), line.getSerialNumber()));
        }
    }

    po.recordReceipt(sumByPoLine(receipt.getLines()));
    goodsReceiptRepository.save(receipt);
    purchaseOrderRepository.save(po);
}

// StockServiceImpl.java
private CostAmount resolveCostAmount(StockMovementPayload payload, BigDecimal baseQuantity) {
    BigDecimal exchangeRate = payload.getExchangeRate() != null ? payload.getExchangeRate() : BigDecimal.ONE;
    BigDecimal originalUnitPrice = payload.getNetPrice() != null ? payload.getNetPrice() : BigDecimal.ZERO;
    BigDecimal transactionQuantity = payload.getQuantity() != null ? payload.getQuantity() : BigDecimal.ONE;
    BigDecimal conversionFactor = baseQuantity.divide(transactionQuantity, 6, RoundingMode.HALF_UP);
    BigDecimal normalizedOriginal = originalUnitPrice.divide(conversionFactor, 6, RoundingMode.HALF_UP);
    return CostAmount.of(payload.getCurrencyId(), exchangeRate, normalizedOriginal);
}
```

- [ ] **Step 4: Run the period/completion/stock tests again**

Run: `cd F:\solusi-program-erp; .\mvnw.cmd -q -Dtest=EnsureOpenPeriodForDateUseCaseTest,CompleteGoodsReceiptUseCaseTest,StockServiceTest test`

Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/solusi/erp/accounting/period src/main/java/com/solusi/erp/inventory/goodsreceipt/application/usecase/command/CompleteGoodsReceiptUseCase.java src/main/java/com/solusi/erp/inventory/goodsreceipt/application/usecase/command/CompleteGoodsReceiptUseCaseImpl.java src/main/java/com/solusi/erp/inventory/stock/infrastructure/service/StockServiceImpl.java src/test/java/com/solusi/erp/accounting/period/application/usecase/query/EnsureOpenPeriodForDateUseCaseTest.java src/test/java/com/solusi/erp/inventory/goodsreceipt/application/usecase/command/CompleteGoodsReceiptUseCaseTest.java src/test/java/com/solusi/erp/inventory/stock/infrastructure/service/StockServiceTest.java
git commit -m "feat: complete goods receipts with period guard and normalized fifo cost"
```

### Task 5: Add persistence, migration, sequence, and permission wiring

**Files:**
- Create: `src/main/java/com/solusi/erp/inventory/goodsreceipt/infrastructure/persistence/GoodsReceiptEntity.java`
- Create: `src/main/java/com/solusi/erp/inventory/goodsreceipt/infrastructure/persistence/GoodsReceiptLineEntity.java`
- Create: `src/main/java/com/solusi/erp/inventory/goodsreceipt/infrastructure/persistence/GoodsReceiptJpaRepository.java`
- Create: `src/main/java/com/solusi/erp/inventory/goodsreceipt/infrastructure/persistence/GoodsReceiptPersistenceMapper.java`
- Create: `src/main/java/com/solusi/erp/inventory/goodsreceipt/infrastructure/adapter/GoodsReceiptRepositoryImpl.java`
- Create: `src/main/java/com/solusi/erp/inventory/goodsreceipt/infrastructure/adapter/GoodsReceiptFacilityUsageChecker.java`
- Create: `src/main/java/com/solusi/erp/inventory/goodsreceipt/infrastructure/adapter/GoodsReceiptLineContainerUsageChecker.java`
- Create: `src/main/java/com/solusi/erp/inventory/goodsreceipt/infrastructure/config/GoodsReceiptConfig.java`
- Create: `src/main/resources/db/migration/V50__Add_Goods_Receipt_Module.sql`
- Test: `src/test/java/com/solusi/erp/inventory/goodsreceipt/infrastructure/persistence/GoodsReceiptPersistenceMapperTest.java`

- [ ] **Step 1: Write the failing mapper test**

```java
@Test
void toDomain_mapsPostingReadySnapshotFields() {
    GoodsReceiptLineEntity line = new GoodsReceiptLineEntity();
    line.setPoLineId(101L);
    line.setProductId(201L);
    line.setQuantityReceived(new BigDecimal("2.0000"));
    line.setBaseQuantity(new BigDecimal("24.0000"));
    line.setInventoryAmount(new BigDecimal("300.0000"));
    line.setTaxAmount(new BigDecimal("33.0000"));
    line.setGrIrAmount(new BigDecimal("333.0000"));

    GoodsReceiptEntity entity = new GoodsReceiptEntity();
    entity.setCode("GR-202604-00001");
    entity.setStatus("DRAFT");
    entity.setLines(List.of(line));

    GoodsReceipt domain = mapper.toDomain(entity);

    assertThat(domain.getCode()).isEqualTo("GR-202604-00001");
    assertThat(domain.getLines().get(0).getBaseQuantity()).isEqualByComparingTo("24.0000");
    assertThat(domain.getLines().get(0).getGrIrAmount()).isEqualByComparingTo("333.0000");
}
```

- [ ] **Step 2: Run the persistence mapper test to verify it fails**

Run: `cd F:\solusi-program-erp; .\mvnw.cmd -q -Dtest=GoodsReceiptPersistenceMapperTest test`

Expected: FAIL because the entities, mapper, and repository wiring do not exist yet.

- [ ] **Step 3: Implement the JPA layer and Flyway migration**

```java
// GoodsReceiptEntity.java
@Entity
@Table(name = "pur_goods_receipts")
public class GoodsReceiptEntity extends BaseModel {
    @Column(name = "code", nullable = false, unique = true)
    private String code;
    @Column(name = "receipt_date", nullable = false)
    private LocalDate receiptDate;
    @Column(name = "po_id", nullable = false)
    private Long poId;
    @Column(name = "supplier_id", nullable = false)
    private Long supplierId;
    @Column(name = "facility_id", nullable = false)
    private Long facilityId;
    @Column(name = "currency_id", nullable = false)
    private Long currencyId;
    @Column(name = "exchange_rate", nullable = false, precision = 19, scale = 6)
    private BigDecimal exchangeRate;
    @Column(name = "status", nullable = false, length = 20)
    private String status;
    @OneToMany(mappedBy = "header", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GoodsReceiptLineEntity> lines = new ArrayList<>();
}

// GoodsReceiptLineEntity.java
@Entity
@Table(name = "pur_goods_receipt_lines")
public class GoodsReceiptLineEntity extends BaseModel {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "header_id", nullable = false)
    private GoodsReceiptEntity header;
    @Column(name = "po_line_id", nullable = false)
    private Long poLineId;
    @Column(name = "product_id", nullable = false)
    private Long productId;
    @Column(name = "quantity_received", nullable = false, precision = 19, scale = 4)
    private BigDecimal quantityReceived;
    @Column(name = "uom_id", nullable = false)
    private Long uomId;
    @Column(name = "container_id", nullable = false)
    private Long containerId;
    @Column(name = "base_quantity", nullable = false, precision = 19, scale = 4)
    private BigDecimal baseQuantity;
    @Column(name = "inventory_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal inventoryAmount;
    @Column(name = "tax_base_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal taxBaseAmount;
    @Column(name = "tax_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal taxAmount;
    @Column(name = "gr_ir_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal grIrAmount;
    @Column(name = "serial_number", columnDefinition = "TEXT")
    private String serialNumber;
}
```

```sql
-- V50__Add_Goods_Receipt_Module.sql
INSERT INTO permission_groups (code, name_id, name_en, breadcrumb_id, breadcrumb_en, url_path, icon_class, description_id, description_en, created_by_user_id, created_date)
VALUES ('INV-11', 'Penerimaan Barang', 'Goods Receipt', 'Operasional > Transaksi Inventaris > Penerimaan Barang', 'Operations > Inventory Transactions > Goods Receipt', '/inventory/goods-receipts', 'ti-package-import', 'Penerimaan barang dari purchase order', 'Warehouse receiving from purchase order', 1, NOW());

INSERT INTO permissions (name, description, created_by_user_id, created_date, permission_group_id) VALUES
('GOODS-RECEIPT_READ', 'Melihat penerimaan barang', 1, NOW(), (SELECT id FROM permission_groups WHERE code = 'INV-11')),
('GOODS-RECEIPT_CREATE', 'Membuat draft penerimaan barang', 1, NOW(), (SELECT id FROM permission_groups WHERE code = 'INV-11')),
('GOODS-RECEIPT_UPDATE', 'Mengubah draft penerimaan barang', 1, NOW(), (SELECT id FROM permission_groups WHERE code = 'INV-11')),
('GOODS-RECEIPT_DELETE', 'Menghapus draft penerimaan barang', 1, NOW(), (SELECT id FROM permission_groups WHERE code = 'INV-11')),
('GOODS-RECEIPT_COMPLETE', 'Menyelesaikan penerimaan barang ke inventaris', 1, NOW(), (SELECT id FROM permission_groups WHERE code = 'INV-11'));

CREATE TABLE pur_goods_receipts (...);
CREATE TABLE pur_goods_receipt_lines (...);

INSERT INTO system_sequences (module_code, format_pattern, pad_length, reset_cycle, updated_by_user_id, updated_date)
VALUES ('GOODS_RECEIPT', 'GR-{date:yyyyMM}-{seq}', 5, 'MONTHLY', 1, NOW());
```

- [ ] **Step 4: Run the mapper test and targeted migration-sensitive tests again**

Run: `cd F:\solusi-program-erp; .\mvnw.cmd -q -Dtest=GoodsReceiptPersistenceMapperTest,PurchaseOrderControllerTest test`

Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/solusi/erp/inventory/goodsreceipt/infrastructure src/main/resources/db/migration/V50__Add_Goods_Receipt_Module.sql src/test/java/com/solusi/erp/inventory/goodsreceipt/infrastructure/persistence/GoodsReceiptPersistenceMapperTest.java
git commit -m "feat: add goods receipt persistence and security migration"
```

### Task 6: Add the web DTOs, mapper, and controller

**Files:**
- Create: `src/main/java/com/solusi/erp/inventory/goodsreceipt/web/dto/GoodsReceiptSaveRequest.java`
- Create: `src/main/java/com/solusi/erp/inventory/goodsreceipt/web/dto/GoodsReceiptSaveLineRequest.java`
- Create: `src/main/java/com/solusi/erp/inventory/goodsreceipt/web/dto/GoodsReceiptDetailResponse.java`
- Create: `src/main/java/com/solusi/erp/inventory/goodsreceipt/web/dto/GoodsReceiptLineDetailResponse.java`
- Create: `src/main/java/com/solusi/erp/inventory/goodsreceipt/web/dto/GoodsReceiptSummaryResponse.java`
- Create: `src/main/java/com/solusi/erp/inventory/goodsreceipt/web/mapper/GoodsReceiptWebMapper.java`
- Create: `src/main/java/com/solusi/erp/inventory/goodsreceipt/web/controller/GoodsReceiptController.java`
- Test: `src/test/java/com/solusi/erp/inventory/goodsreceipt/web/controller/GoodsReceiptControllerTest.java`

- [ ] **Step 1: Write the failing controller tests**

```java
@Test
void createForm_fromPo_returnsInventoryGoodsReceiptForm() {
    GoodsReceipt draft = draftReceipt();
    when(getGoodsReceiptCreateViewUseCase.execute(7L)).thenReturn(draft);
    when(webMapper.toSaveRequest(draft)).thenReturn(new GoodsReceiptSaveRequest());

    Model model = new ExtendedModelMap();
    String view = controller.createForm(7L, model);

    assertThat(view).isEqualTo("inventory/goods-receipts/form");
    assertThat(model.getAttribute("goodsReceipt")).isInstanceOf(GoodsReceiptSaveRequest.class);
}

@Test
void complete_callsCompleteUseCaseAndRedirectsToView() {
    when(messageSource.getMessage(anyString(), any(), any())).thenReturn("Completed");

    RedirectAttributesModelMap ra = new RedirectAttributesModelMap();
    String view = controller.complete(1L, ra);

    assertThat(view).isEqualTo("redirect:/inventory/goods-receipts/view/1");
    verify(completeGoodsReceiptUseCase).execute(1L);
}
```

- [ ] **Step 2: Run the controller tests to verify they fail**

Run: `cd F:\solusi-program-erp; .\mvnw.cmd -q -Dtest=GoodsReceiptControllerTest test`

Expected: FAIL because the DTOs, mapper, and controller do not exist yet.

- [ ] **Step 3: Implement the web layer**

```java
@Controller
@RequestMapping("/inventory/goods-receipts")
@RequiredArgsConstructor
@DefaultRedirectUrl
public class GoodsReceiptController {
    private final CreateGoodsReceiptUseCase createGoodsReceiptUseCase;
    private final UpdateGoodsReceiptUseCase updateGoodsReceiptUseCase;
    private final DeleteGoodsReceiptUseCase deleteGoodsReceiptUseCase;
    private final CompleteGoodsReceiptUseCase completeGoodsReceiptUseCase;
    private final FindGoodsReceiptsUseCase findGoodsReceiptsUseCase;
    private final GetGoodsReceiptUseCase getGoodsReceiptUseCase;
    private final GetGoodsReceiptEditViewUseCase getGoodsReceiptEditViewUseCase;
    private final GetGoodsReceiptCreateViewUseCase getGoodsReceiptCreateViewUseCase;
    private final GoodsReceiptWebMapper webMapper;
    private final MessageSource messageSource;

    @GetMapping("/create")
    @PreAuthorize("hasAuthority('GOODS-RECEIPT_CREATE')")
    public String createForm(@RequestParam Long poId, Model model) {
        GoodsReceipt draft = getGoodsReceiptCreateViewUseCase.execute(poId);
        model.addAttribute("goodsReceipt", webMapper.toSaveRequest(draft));
        return "inventory/goods-receipts/form";
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAuthority('GOODS-RECEIPT_COMPLETE')")
    public String complete(@PathVariable Long id, RedirectAttributes ra) {
        completeGoodsReceiptUseCase.execute(id);
        ra.addFlashAttribute("message", messageSource.getMessage("msg.success.gr.completed", null, LocaleContextHolder.getLocale()));
        return "redirect:/inventory/goods-receipts/view/" + id;
    }
}
```

- [ ] **Step 4: Run the controller tests again**

Run: `cd F:\solusi-program-erp; .\mvnw.cmd -q -Dtest=GoodsReceiptControllerTest test`

Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/solusi/erp/inventory/goodsreceipt/web src/test/java/com/solusi/erp/inventory/goodsreceipt/web/controller/GoodsReceiptControllerTest.java
git commit -m "feat: add goods receipt web controller and dto layer"
```

### Task 7: Build Thymeleaf screens, drawer fragments, and page-specific JavaScript

**Files:**
- Create: `src/main/resources/templates/inventory/goods-receipts/list.html`
- Create: `src/main/resources/templates/inventory/goods-receipts/form.html`
- Create: `src/main/resources/templates/inventory/goods-receipts/view.html`
- Create: `src/main/resources/templates/inventory/goods-receipts/drawer-fragments.html`
- Create: `src/main/resources/static/js/inventory/goods-receipt/goods-receipt-form.js`
- Test: `src/test/java/com/solusi/erp/inventory/goodsreceipt/web/template/integration/GoodsReceiptFormIntegrationTest.java`
- Test: `src/test/java/com/solusi/erp/inventory/goodsreceipt/web/template/integration/GoodsReceiptListIntegrationTest.java`
- Test: `src/test/java/com/solusi/erp/inventory/goodsreceipt/web/template/integration/GoodsReceiptViewIntegrationTest.java`

- [ ] **Step 1: Write the failing template integration tests**

```java
@Test
void draftForm_rendersSplitRowAndCompleteButton() {
    String html = TemplateTestUtils.renderWithSecurity(
            "inventory/goods-receipts/form",
            Map.of("goodsReceipt", draftRequest()),
            auth("GOODS-RECEIPT_UPDATE", "GOODS-RECEIPT_COMPLETE")
    );

    assertThat(html).contains("id=\"btn-split-line\"");
    assertThat(html).contains("id=\"btn-complete-goods-receipt\"");
    assertThat(html).contains("id=\"drawer-serial\"");
}

@Test
void viewTemplate_rendersReadonlyStatusAndPoLink() {
    String html = TemplateTestUtils.renderWithSecurity(
            "inventory/goods-receipts/view",
            Map.of("goodsReceipt", completedDetail()),
            auth("GOODS-RECEIPT_READ")
    );

    assertThat(html).contains("/purchasing/purchase-orders/view/7");
    assertThat(html).contains("COMPLETED");
}
```

- [ ] **Step 2: Run the template integration tests to verify they fail**

Run: `cd F:\solusi-program-erp; .\mvnw.cmd -q -Dtest=GoodsReceiptFormIntegrationTest,GoodsReceiptListIntegrationTest,GoodsReceiptViewIntegrationTest test`

Expected: FAIL because the templates and JS entry point do not exist yet.

- [ ] **Step 3: Implement the form/list/view templates and JS**

```html
<!-- form.html -->
<form id="goods-receipt-form" th:object="${goodsReceipt}">
    <div class="card">
        <div class="card-header">
            <h3 class="card-title" th:text="#{label.gr.line.title}">Receipt Lines</h3>
            <div class="card-actions" th:if="!${isLocked}">
                <button type="button" class="btn btn-outline-primary btn-sm" id="btn-split-line">
                    <i class="ti ti-git-branch me-1"></i><span th:text="#{label.gr.action.split}">Split Row</span>
                </button>
            </div>
        </div>
        <div class="table-responsive">
            <table class="table table-vcenter card-table" id="table-lines">
                <!-- quantity, container lookup, readonly PO/UOM columns -->
            </table>
        </div>
        <div class="card-footer text-end">
            <button type="button" id="btn-complete-goods-receipt"
                    class="btn btn-primary"
                    sec:authorize="hasAuthority('GOODS-RECEIPT_COMPLETE')">Complete</button>
        </div>
    </div>
</form>
<div th:replace="~{inventory/goods-receipts/drawer-fragments :: goods-receipt-drawers}"></div>
<div id="page-specific-scripts" th:fragment="pageScripts">
    <script th:src="@{/js/inventory/goods-receipt/goods-receipt-form.js}"></script>
</div>
```

```javascript
// goods-receipt-form.js
(function () {
    const lineManager = new ErpLineManager('line-container', 'row-template-source');

    function splitRow(row) {
        const clone = lineManager.addRow();
        clone.querySelector('.input-po-line-id').value = row.querySelector('.input-po-line-id').value;
        clone.querySelector('.input-product-id').value = row.querySelector('.input-product-id').value;
        clone.querySelector('.input-uom-id').value = row.querySelector('.input-uom-id').value;
        ErpNumeric.set(clone.querySelector('.input-qty'), 0);
    }

    function setupSerialDrawer(drawer, row) {
        const qty = ErpNumeric.get(row.querySelector('.input-qty'));
        const container = drawer.querySelector('.serial-input-container');
        container.innerHTML = '';
        for (let i = 0; i < qty; i++) {
            const tr = document.createElement('tr');
            tr.innerHTML = `<td class="text-center small text-secondary">${i + 1}</td>
                            <td><input type="text" class="form-control form-control-sm input-sn-item"></td>
                            <td class="text-end text-secondary">1.00</td>`;
            container.appendChild(tr);
        }
        return true;
    }

    function openSerialDrawer(row) {
        const drawerId = 'drawer-serial';
        if (setupSerialDrawer(document.getElementById(drawerId), row)) {
            ErpDrawer.open(drawerId);
        }
    }

    document.getElementById('btn-complete-goods-receipt')?.addEventListener('click', () => {
        ErpAction.confirmAndSubmit('Complete this goods receipt?', {
            url: document.getElementById('btn-complete-goods-receipt').dataset.completeUrl
        });
    });
})();
```

- [ ] **Step 4: Run the template integration tests again**

Run: `cd F:\solusi-program-erp; .\mvnw.cmd -q -Dtest=GoodsReceiptFormIntegrationTest,GoodsReceiptListIntegrationTest,GoodsReceiptViewIntegrationTest test`

Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/resources/templates/inventory/goods-receipts src/main/resources/static/js/inventory/goods-receipt/goods-receipt-form.js src/test/java/com/solusi/erp/inventory/goodsreceipt/web/template/integration
git commit -m "feat: add goods receipt templates and form script"
```

### Task 8: Wire PO detail entry points, i18n, docs, and full verification

**Files:**
- Modify: `src/main/java/com/solusi/erp/purchasing/purchaseorder/web/controller/PurchaseOrderController.java`
- Modify: `src/main/resources/templates/purchasing/purchase-orders/view.html`
- Modify: `src/test/java/com/solusi/erp/purchasing/purchaseorder/web/controller/PurchaseOrderControllerTest.java`
- Modify: `src/test/java/com/solusi/erp/purchasing/purchaseorder/web/template/integration/PurchaseOrderViewIntegrationTest.java`
- Modify: `src/main/resources/messages.properties`
- Modify: `src/main/resources/messages_id.properties`
- Modify: `docs/modules/procurement/purchase-order.md`
- Modify: `docs/modules/inventory/stock-utility.md`

- [ ] **Step 1: Write the failing PO view tests for document flow**

```java
@Test
void view_populatesReceiptCountWhenReceiptsExist() {
    when(findGoodsReceiptCountUseCase.execute(1L)).thenReturn(3L);
    when(editViewUc.execute(1L)).thenReturn(Optional.of(buildSentPo()));

    Model model = new ExtendedModelMap();
    controller.view(1L, model, null);

    assertThat(model.getAttribute("goodsReceiptCount")).isEqualTo(3L);
    assertThat(model.getAttribute("canCreateGoodsReceipt")).isEqualTo(true);
}
```

```java
@Test
void template_containsGoodsReceiptButtons() throws Exception {
    String template = readResource("templates/purchasing/purchase-orders/view.html");
    assertThat(template).contains("/inventory/goods-receipts/create?poId=");
    assertThat(template).contains("goodsReceiptCount");
    assertThat(template).contains("Create Goods Receipt");
}
```

- [ ] **Step 2: Run the PO/controller/view tests to verify they fail**

Run: `cd F:\solusi-program-erp; .\mvnw.cmd -q -Dtest=PurchaseOrderControllerTest,PurchaseOrderViewIntegrationTest test`

Expected: FAIL because PO detail does not expose receipt actions yet.

- [ ] **Step 3: Implement the PO document-flow wiring, messages, and docs**

```java
// PurchaseOrderController.java
private final CountGoodsReceiptsByPoUseCase countGoodsReceiptsByPoUseCase;

@GetMapping("/view/{id}")
public String view(@PathVariable Long id, Model model, @AuthenticationPrincipal UserDetails principal) {
    PurchaseOrder domain = getPurchaseOrderEditViewUseCase.execute(id)
            .orElseThrow(() -> new RuntimeException("Purchase order not found"));
    model.addAttribute("po", webMapper.toDetailResponse(domain));
    model.addAttribute("goodsReceiptCount", countGoodsReceiptsByPoUseCase.execute(id));
    model.addAttribute("canCreateGoodsReceipt", domain.getStatus().canReceive());
    return "purchasing/purchase-orders/view";
}
```

```html
<!-- purchase-orders/view.html -->
<div class="btn-list mt-3" th:if="${canCreateGoodsReceipt}">
    <a class="btn btn-outline-primary"
       sec:authorize="hasAuthority('GOODS-RECEIPT_CREATE')"
       th:href="@{/inventory/goods-receipts/create(poId=${po.id})}">
        <i class="ti ti-package-import me-1"></i>
        <span th:text="#{label.gr.action.create}">Create Goods Receipt</span>
    </a>
    <a class="btn btn-outline-secondary"
       sec:authorize="hasAuthority('GOODS-RECEIPT_READ')"
       th:href="@{/inventory/goods-receipts(poId=${po.id})}">
        <i class="ti ti-list-details me-1"></i>
        <span th:text="#{label.gr.list.title}">Goods Receipts</span>
        <span class="badge ms-1" th:text="${goodsReceiptCount}">0</span>
    </a>
</div>
```

```properties
# messages.properties
label.gr.list.title=Goods Receipts
label.gr.action.create=Create Goods Receipt
label.gr.action.split=Split Row
msg.success.gr.completed=Goods receipt completed successfully.
msg.error.gr.stale.po.changed=Purchase order outstanding quantity changed. Refresh the goods receipt draft.
msg.error.gr.complete.no.lines=Add at least one received line before completing the goods receipt.
```

- [ ] **Step 4: Run the targeted PO tests and then the full suite**

Run: `cd F:\solusi-program-erp; .\mvnw.cmd -q -Dtest=PurchaseOrderControllerTest,PurchaseOrderViewIntegrationTest test`

Expected: PASS

Run: `cd F:\solusi-program-erp; Remove-Item -Recurse -Force target -ErrorAction SilentlyContinue; .\mvnw.cmd clean test -q`

Expected: BUILD SUCCESS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/solusi/erp/purchasing/purchaseorder/web/controller/PurchaseOrderController.java src/main/resources/templates/purchasing/purchase-orders/view.html src/test/java/com/solusi/erp/purchasing/purchaseorder/web/controller/PurchaseOrderControllerTest.java src/test/java/com/solusi/erp/purchasing/purchaseorder/web/template/integration/PurchaseOrderViewIntegrationTest.java src/main/resources/messages.properties src/main/resources/messages_id.properties docs/modules/procurement/purchase-order.md docs/modules/inventory/stock-utility.md
git commit -m "feat: wire purchase order document flow to goods receipts"
```

## Self-Review Notes

- Sprint 4 core coverage is mapped to Tasks 1–8: PO receiving domain, GR slice, period guard, completion transaction, persistence/migration, permissions, PO entry point, UI, serialized drawer, stock/FIFO, and docs.
- Deferred items from the discussion intentionally do **not** appear as implementation tasks: real journal posting, schema/COA validation on completion, import/PPh22/cukai, over-receipt tolerance, purchase return/reversal.
- The most fragile implementation points are called out explicitly in tests: stale draft rejection, period OPEN enforcement, serialized completion, and base-UOM cost normalization.

