# Implementation Plan: Generic Goods Issue Core

> Source: `docs/brainstorming/2026-06-01-generic-goods-issue.md`
> Created: 2026-06-01
> Sprint: 6 - Inventory Outbound Core
> Status: IN_PROGRESS
>
> **For agentic workers:** execute task-by-task. Explore references fresh before editing each task, update checkbox state incrementally, record deviations in `docs/reports/2026-06-01-generic-goods-issue.md`, and do not mark a task complete before its validation command passes.

## 1. Goal

Membangun modul `inventory.goodsissue` sebagai dokumen fisik outbound generik yang mirror secara konsep terhadap Goods Receipt (GR). Modul ini menjadi sumber audit operasional untuk semua barang keluar, lalu memanggil primitive stock bawah `StockService.adjust()` dengan `MovementType.ISSUE` saat dokumen di-complete.

Core GI harus siap untuk Purchase Return sebagai source pertama, tetapi codebase saat plan ini dibuat belum memiliki modul `PurchaseReturn`. Karena itu implementasi core memuat kontrak resolver, reference metadata, dan integration seam untuk Purchase Return; adapter konkret Purchase Return dieksekusi setelah modul Purchase Return tersedia.

## 2. Locked Decisions

- Package baru: `com.solusi.erp.inventory.goodsissue`.
- DB table utama: `inv_goods_issues` dan `inv_goods_issue_lines`.
- Sequence: `GOODS_ISSUE` dengan format `GI-{date:yyyyMM}-{seq}`.
- Header memakai `reference_type`, `reference_id`, `reference_code`, `party_id`, `party_type`, `facility_id`, `currency_id`, `exchange_rate`, `status`, dan `note`.
- Line memakai `reference_line_id` untuk source line langsung, serta `valuation_ref_type`, `valuation_ref_id`, `valuation_ref_line_id` untuk origin costing layer.
- `serial_number` tetap CSV untuk core awal agar konsisten dengan GR.
- `valuation_ref_id` menunjuk dokumen inbound header, dan `valuation_ref_line_id` menunjuk line inbound. Untuk Purchase Return, targetnya GR header + GR line.
- `ReferenceType.GOODS_ISSUE` sudah ada dan harus dipakai pada `InventoryMovement`; source bisnis tetap disimpan di GI header.
- Complete GI mem-post stock issue, mengisi `unit_cost` dan `inventory_amount` snapshot dari valuation consumption, lalu mem-post journal sesuai source/event.
- Cancel GI disiapkan konservatif: hanya untuk `COMPLETED`, wajib period open, membuat reversal stock movement dan reversal journal, lalu status menjadi `CANCELLED`. Jika journal reversal helper belum tersedia, task cancel tetap belum boleh ditandai complete.
- Sales/Delivery resolver, Production resolver, Scrap resolver, WMS serial detail table, approval gudang, dan E2E penuh ditunda.

## 3. Coverage From Brainstorm

- Stock valuation reference enhancement: Task 1.
- GI domain/entity/migration: Task 2 and Task 3.
- GI use cases + resolver registry: Task 4 and Task 5.
- GI UI/list/detail/create-from-source: Task 6, Task 7, Task 8, and Task 9.
- Purchase Return integration as first source: Task 10, with adapter concrete gated by Purchase Return module availability.
- Accounting schema/event support: Task 11.

## 4. Target File Map

### Database

- Create `src/main/resources/db/migration/V65__Add_Goods_Issue_Core.sql`
- Create `src/main/resources/db/migration-h2/V65__Add_Goods_Issue_Core.sql`

### Stock Valuation Enhancement

- Modify `src/main/java/com/solusi/erp/inventory/stock/domain/model/ValuationLayer.java`
- Modify `src/main/java/com/solusi/erp/inventory/stock/domain/repository/ValuationLayerRepository.java`
- Modify `src/main/java/com/solusi/erp/inventory/stock/domain/service/FifoValuationService.java`
- Modify `src/main/java/com/solusi/erp/inventory/stock/infrastructure/persistence/ValuationLayerEntity.java`
- Modify `src/main/java/com/solusi/erp/inventory/stock/infrastructure/persistence/ValuationLayerJpaRepository.java`
- Modify `src/main/java/com/solusi/erp/inventory/stock/infrastructure/adapter/ValuationLayerRepositoryImpl.java`
- Modify `src/main/java/com/solusi/erp/inventory/stock/application/dto/StockMovementPayload.java`
- Modify `src/main/java/com/solusi/erp/inventory/stock/infrastructure/service/StockServiceImpl.java`

### Goods Issue Module

- Create `src/main/java/com/solusi/erp/inventory/goodsissue/domain/model/GoodsIssue.java`
- Create `src/main/java/com/solusi/erp/inventory/goodsissue/domain/model/GoodsIssueLine.java`
- Create `src/main/java/com/solusi/erp/inventory/goodsissue/domain/model/GoodsIssueStatus.java`
- Create `src/main/java/com/solusi/erp/inventory/goodsissue/domain/model/GoodsIssueReferenceType.java`
- Create `src/main/java/com/solusi/erp/inventory/goodsissue/domain/model/PartyType.java` or use existing party role enum if present
- Create `src/main/java/com/solusi/erp/inventory/goodsissue/domain/repository/GoodsIssueRepository.java`
- Create `src/main/java/com/solusi/erp/inventory/goodsissue/domain/port/GoodsIssueSourceResolver.java`
- Create `src/main/java/com/solusi/erp/inventory/goodsissue/domain/port/GoodsIssueReferenceLookupProvider.java`
- Create `src/main/java/com/solusi/erp/inventory/goodsissue/infrastructure/service/GoodsIssueSourceResolverRegistry.java`
- Create `src/main/java/com/solusi/erp/inventory/goodsissue/infrastructure/persistence/*`
- Create `src/main/java/com/solusi/erp/inventory/goodsissue/infrastructure/adapter/*`
- Create `src/main/java/com/solusi/erp/inventory/goodsissue/infrastructure/config/GoodsIssueConfig.java`
- Create `src/main/java/com/solusi/erp/inventory/goodsissue/application/usecase/command/*`
- Create `src/main/java/com/solusi/erp/inventory/goodsissue/application/usecase/query/*`
- Create `src/main/java/com/solusi/erp/inventory/goodsissue/web/controller/GoodsIssueController.java`
- Create `src/main/java/com/solusi/erp/inventory/goodsissue/web/dto/*`
- Create `src/main/java/com/solusi/erp/inventory/goodsissue/web/mapper/GoodsIssueWebMapper.java`

### Templates And JavaScript

- Create `src/main/resources/templates/inventory/goods-issues/list.html`
- Create `src/main/resources/templates/inventory/goods-issues/view.html`
- Create `src/main/resources/templates/inventory/goods-issues/form.html`
- Create `src/main/resources/templates/inventory/goods-issues/fragments/source-line-selector-modal.html`
- Create `src/main/resources/static/js/inventory/goods-issue/goods-issue-form.js`
- Update message bundles for `label.gi.*`, `msg.success.gi.*`, `msg.error.gi.*`, and `msg.warning.gi.*`

## 5. Tasks

### Task 1: Stock Valuation Reference Metadata

Add source reference metadata to valuation layers and stock movement payload so GI can consume a specific GR/GR line layer for Purchase Return.

**Depends on:** none
**Reference module:** `inventory.stock`, `inventory.goodsreceipt`

Steps:
- [x] Add nullable columns `reference_type`, `reference_id`, and `reference_line_id` to `inv_valuation_layers` in both MariaDB and H2 migrations.
      ref: `src/main/resources/db/migration/V23__Inventory_Core_Valuation_And_Movements.sql:L87-L114` - current `inv_valuation_layers` table shape
- [x] Add the same fields to `ValuationLayer` domain, factory methods, getters, entity, mapper, and repository adapter.
      ref: `src/main/java/com/solusi/erp/inventory/stock/domain/model/ValuationLayer.java:L12-L37` - current immutable valuation layer constructor and factory
      ref: `src/main/java/com/solusi/erp/inventory/stock/infrastructure/persistence/ValuationLayerEntity.java:L19-L44` - JPA entity columns and embedded `unitCost`
- [x] Extend `StockMovementPayload` with `valuationReferenceType`, `valuationReferenceId`, and `valuationReferenceLineId`; use these only for valuation layer creation/consumption, not for `InventoryMovement.referenceType`.
      ref: `src/main/java/com/solusi/erp/inventory/stock/application/dto/StockMovementPayload.java:L22-L55` - stock adjustment payload contract
- [x] Modify positive stock valuation so GR completion writes valuation layer reference metadata from payload.
      ref: `src/main/java/com/solusi/erp/inventory/stock/infrastructure/service/StockServiceImpl.java:L78-L86` - current add/consume FIFO valuation branch
      ref: `src/main/java/com/solusi/erp/inventory/goodsreceipt/application/usecase/command/CompleteGoodsReceiptUseCaseImpl.java:L261-L277` - GR stock payload builder to enrich with GR header/line reference
- [x] Add repository methods for specific layer lookup by `productId`, `containerId`, `referenceType`, `referenceId`, `referenceLineId`, and optional `serialNumber`.
      ref: `src/main/java/com/solusi/erp/inventory/stock/domain/repository/ValuationLayerRepository.java:L12-L26` - existing FIFO lookup methods
      ref: `src/main/java/com/solusi/erp/inventory/stock/infrastructure/persistence/ValuationLayerJpaRepository.java:L10-L24` - current Spring Data query naming pattern
- [x] Add `FifoValuationService.consumeSpecificLayers(...)` that consumes only matching reference layers and still throws insufficient stock if remaining target qty cannot be fulfilled.
      ref: `src/main/java/com/solusi/erp/inventory/stock/domain/service/FifoValuationService.java:L39-L70` - current FIFO consume algorithm and save behavior
- [x] **TEST:** Extend `FifoValuationServiceTest` for specific-layer success, insufficient specific layer, and serial-specific layer.
      ref: `src/test/java/com/solusi/erp/inventory/stock/domain/FifoValuationServiceTest.java` - pure domain service test location
- [x] **TEST:** Extend `StockServiceImplTest` to prove GR receipt creates layers with GR reference metadata and GI issue can request specific layer consumption.
      ref: `src/main/java/com/solusi/erp/inventory/stock/infrastructure/service/StockServiceImpl.java:L70-L86` - behavior under test

**Validation criteria:**
- `mvn test -Dtest="FifoValuationServiceTest,StockServiceImplTest,CompleteGoodsReceiptUseCaseTest"` passes.
- GR completion stock payload includes `valuationReferenceType=GOODS_RECEIPT`, `valuationReferenceId=receipt.id`, and `valuationReferenceLineId=line.id` or source line mapping that can be resolved to GR line.
- Specific-layer consumption never falls back silently to global FIFO when reference metadata is present.

### Task 2: Goods Issue Migration, Sequence, Permission, And Menu

Create the persistent skeleton for GI, including header/line tables, sequence registration, permission group, and RBAC permissions.

**Depends on:** Task 1
**Reference module:** `inventory.goodsreceipt`

Steps:
- [x] Create `V66__Add_Goods_Issue_Core.sql` in MariaDB and H2 migration folders.
      ref: `src/main/resources/db/migration/V50__Add_Goods_Receipt_Module.sql:L6-L62` - GR header/line DDL pattern
      ref: `src/main/resources/db/migration-h2/` - H2 migration mirror requirement for E2E profile
- [x] Add `inv_goods_issues` with `code`, `issue_date`, `reference_type`, `reference_id`, `reference_code`, `party_id`, `party_type`, `facility_id`, `currency_id`, `exchange_rate`, `status`, `note`, and audit fields.
      ref: `docs/brainstorming/2026-06-01-generic-goods-issue.md:L69-L96` - chosen GI header fields and single-facility rule
- [x] Add `inv_goods_issue_lines` with line source reference, product snapshot, qty/UOM/base qty, `facility_id`, `grid_id`, `container_id`, `serial_number`, monetary snapshots, and valuation references.
      ref: `docs/brainstorming/2026-06-01-generic-goods-issue.md:L97-L133` - chosen GI line fields and dual references
- [x] Add indexes for list/search and integration: `(code)`, `(status)`, `(issue_date)`, `(reference_type, reference_id)`, `(party_type, party_id)`, `(facility_id)`, and line `(valuation_ref_type, valuation_ref_id, valuation_ref_line_id)`.
      ref: `src/main/resources/db/migration/V51__Generalize_Goods_Receipt_Reference.sql:L1-L21` - GR generalized reference migration and index
- [x] Register sequence `GOODS_ISSUE` with `GI-{date:yyyyMM}-{seq}`.
      ref: `src/main/resources/db/migration/V50__Add_Goods_Receipt_Module.sql:L64-L69` - sequence registration pattern
- [x] Add permission group `INV-13` or next available inventory sort slot with URL `/inventory/goods-issues` and icon `ti-package-export`.
      ref: `src/main/resources/db/migration/V50__Add_Goods_Receipt_Module.sql:L72-L80` - GR permission group/menu pattern
      ref: `docs/spec/menu-structure.md` - menu hierarchy source of truth
- [x] Add permissions `GOODS-ISSUE_READ`, `GOODS-ISSUE_CREATE`, `GOODS-ISSUE_UPDATE`, `GOODS-ISSUE_DELETE`, `GOODS-ISSUE_COMPLETE`, and `GOODS-ISSUE_CANCEL`; grant all to `ROLE_ADMIN`.
      ref: `docs/AGENTS.md:L126-L146` - RBAC naming convention and wildcard safety
      ref: `src/main/resources/db/migration/V50__Add_Goods_Receipt_Module.sql:L82-L99` - permission seed pattern
- [x] Add i18n keys for enum reference type, GI status, menu, list, form, view, action messages, warnings, and validation errors using the repo i18n update protocol.
      ref: `docs/AGENTS.md:L83-L91` - i18n rules and replace-tool warning

**Validation criteria:**
- App starts with Flyway on MariaDB profile and H2/e2e profile.
- `GOODS_ISSUE` sequence can generate a GI code.
- Role UI groups GI permissions under a clean `GOODS-ISSUE` permission folder.

### Task 3: Goods Issue Domain, Repository, And Persistence

Implement pure domain model and persistence mapping for `GoodsIssue` and `GoodsIssueLine`.

**Depends on:** Task 2
**Reference module:** `inventory.goodsreceipt`

Steps:
- [x] Create `GoodsIssueStatus` with `DRAFT`, `COMPLETED`, and `CANCELLED`; expose lifecycle helpers such as `isEditable()`, `canComplete()`, and `canCancel()`.
      ref: `docs/brainstorming/2026-06-01-generic-goods-issue.md:L144-L178` - GI lifecycle rules
- [x] Create `GoodsIssueReferenceType` with at least `PURCHASE_RETURN`, `DELIVERY_ORDER`, `MANUAL`, `PRODUCTION`, `SCRAP`, and `INTERNAL_USE`, while only allowing implemented resolver types at use-case boundary.
      ref: `docs/brainstorming/2026-06-01-generic-goods-issue.md:L288-L315` - reference type candidates and ledger recommendation
- [x] Create `GoodsIssueLine` as an immutable value object with source reference, valuation reference, qty/UOM/base qty, location snapshot, serial CSV, and amount snapshots.
      ref: `src/main/java/com/solusi/erp/inventory/goodsreceipt/domain/model/GoodsReceiptLine.java:L5-L82` - immutable line object pattern
      ref: `docs/brainstorming/2026-06-01-generic-goods-issue.md:L316-L328` - data kept as historical snapshot
- [x] Create `GoodsIssue` aggregate with factory `createNew`, `update`, `complete`, and `cancel`, enforcing positive issue quantity and immutability after completed/cancelled.
      ref: `src/main/java/com/solusi/erp/inventory/goodsreceipt/domain/model/GoodsReceipt.java:L51-L86` - aggregate factory/update/complete pattern
- [x] Create repository interface with `save`, `findById`, `delete`, `findAll(keyword, referenceType, referenceId, pageable)`, `existsByCode`, and `existsByReference`.
      ref: `src/main/java/com/solusi/erp/inventory/goodsreceipt/domain/repository/GoodsReceiptRepository.java` - repository shape to mirror
- [x] Create JPA entities and MapStruct persistence mapper. Ensure `BaseModel` audit inheritance is used and no JPA entity leaks outside infrastructure.
      ref: `src/test/java/com/solusi/erp/inventory/goodsreceipt/infrastructure/persistence/GoodsReceiptPersistenceMapperTest.java:L20-L78` - mapper field coverage test pattern
- [x] Create repository adapter backed by `GoodsIssueJpaRepository`.
      ref: `src/main/java/com/solusi/erp/inventory/goodsreceipt/infrastructure/adapter/GoodsReceiptRepositoryImpl.java` - infrastructure adapter pattern
- [x] **TEST:** Add `GoodsIssueTest` for immutable line, complete requires positive qty, completed/cancelled immutable, cancel only after completed, and typed reference metadata.
      ref: `src/test/java/com/solusi/erp/inventory/goodsreceipt/domain/model/GoodsReceiptTest.java:L18-L84` - domain test style
- [x] **TEST:** Add persistence mapper test for all header fields, all line snapshots, audit fields, status enum mapping, and defensive line copies.
      ref: `src/test/java/com/solusi/erp/inventory/goodsreceipt/infrastructure/persistence/GoodsReceiptPersistenceMapperTest.java:L22-L107` - mapper assertions

**Validation criteria:**
- `mvn test -Dtest="GoodsIssueTest,GoodsIssuePersistenceMapperTest"` passes.
- Domain classes remain pure Java with no Spring/JPA annotations.
- `GoodsIssueLine` fields remain final and immutable.

### Task 4: Resolver Registry And Create/Edit View Query Use Cases

Build source resolver contracts so GI can be created from different source documents without binding core GI to Purchase Return, Sales, or manual future flows.

**Depends on:** Task 3
**Reference module:** `inventory.goodsreceipt`

Steps:
- [x] Create `GoodsIssueSourceResolver` with `getReferenceType()` and `resolve(Long referenceId)` returning a draft `GoodsIssue`.
      ref: `src/main/java/com/solusi/erp/inventory/goodsreceipt/infrastructure/adapter/PurchaseOrderGoodsReceiptSourceResolver.java:L17-L31` - existing resolver type contract usage
      ref: `docs/brainstorming/2026-06-01-generic-goods-issue.md:L267-L286` - GI resolver responsibilities
- [x] Create `GoodsIssueSourceResolverRegistry` backed by `EnumMap`; reject duplicate resolver and unsupported source types.
      ref: `src/main/java/com/solusi/erp/inventory/goodsreceipt/infrastructure/service/GoodsReceiptSourceResolverRegistry.java:L11-L30` - resolver registry pattern
- [x] Create `GoodsIssueReferenceLookupProvider` for reference code, reference line snapshots, and optional source line selector rows. The provider may return empty for unimplemented future source types, but must not throw in list/detail enrichment for historical rows.
      ref: `src/main/java/com/solusi/erp/inventory/goodsreceipt/domain/port/GoodsReceiptReferenceLookupProvider.java` - GR reference lookup provider shape
- [x] Create `GetGoodsIssueCreateViewUseCase` to call resolver when `referenceType/referenceId` are supplied and to reject unsupported source types with `msg.error.gi.reference.unsupported`.
      ref: `src/main/java/com/solusi/erp/inventory/goodsreceipt/application/usecase/query/GetGoodsReceiptCreateViewUseCaseImpl.java` - create-view resolver use case
- [x] Create `GetGoodsIssueEditViewUseCase`, `GetGoodsIssueUseCase`, and `FindGoodsIssuesUseCase`.
      ref: `src/main/java/com/solusi/erp/inventory/goodsreceipt/infrastructure/config/GoodsReceiptConfig.java:L69-L108` - query use-case bean map
- [x] Add a `PurchaseReturnGoodsIssueSourceResolver` only when the Purchase Return domain/repository exists. Until then, add a report finding and leave concrete PR adapter task pending.
      ref: `docs/brainstorming/2026-06-01-generic-goods-issue.md:L180-L210` - Purchase Return via GI and specific-layer valuation requirement
- [x] **TEST:** Add registry duplicate/unsupported tests.
      ref: `src/main/java/com/solusi/erp/inventory/goodsreceipt/infrastructure/service/GoodsReceiptSourceResolverRegistry.java:L15-L29` - error branches to cover
- [x] **TEST:** Add create-view use case tests for supported resolver, unsupported source, and missing source id.
      ref: `src/test/java/com/solusi/erp/inventory/goodsreceipt/application/usecase/query/` - query use case test package pattern

**Validation criteria:**
- `mvn test -Dtest="*GoodsIssue*Resolver*Test,*GoodsIssue*CreateView*Test,*GoodsIssue*Query*Test"` passes.
- Core GI compiles without a hard dependency on non-existent Purchase Return classes.
- Adding a future source resolver only requires a new adapter bean and enum support, not GI controller rewrites.

### Task 5: Command Use Cases For Save, Complete, Delete, And Cancel

Implement command behavior for draft maintenance, stock issue posting, source-specific valuation, and conservative cancellation.

**Depends on:** Task 4
**Reference module:** `inventory.goodsreceipt`, `inventory.stock`, `accounting.journal`

Steps:
- [x] Create command records/classes for GI line input, complete request, and cancel request if cancel needs a note/reason.
      ref: `src/main/java/com/solusi/erp/inventory/goodsreceipt/application/usecase/command/GoodsReceiptLineCommand.java` - line command package pattern
- [x] Implement `CreateGoodsIssueUseCaseImpl`: generate code via `SequenceGeneratorService`, resolve draft from source resolver when source-based, validate header/line consistency, and save `DRAFT`.
      ref: `src/main/java/com/solusi/erp/inventory/goodsreceipt/application/usecase/command/CreateGoodsReceiptUseCaseImpl.java` - create command pattern
      ref: `src/main/java/com/solusi/erp/inventory/goodsreceipt/infrastructure/config/GoodsReceiptConfig.java:L37-L42` - sequence service wiring
- [x] Implement `UpdateGoodsIssueUseCaseImpl`: only `DRAFT`, preserve reference metadata, replace lines using immutable value objects, and revalidate facility/grid/container consistency.
      ref: `src/main/java/com/solusi/erp/inventory/goodsreceipt/domain/model/GoodsReceipt.java:L68-L75` - immutable update guard pattern
- [x] Implement `DeleteGoodsIssueUseCaseImpl`: allow delete only for `DRAFT`; completed/cancelled documents are audit records and must not be hard-deleted.
      ref: `docs/brainstorming/2026-06-01-generic-goods-issue.md:L152-L178` - status meaning
- [x] Implement `CompleteGoodsIssueUseCaseImpl` in a transaction: load GI, ensure period open, validate source still eligible, resolve valuation cost, call `StockService.adjust()` with `MovementType.ISSUE` and `ReferenceType.GOODS_ISSUE`, snapshot amounts, post source/event journal, then save GI.
      ref: `src/main/java/com/solusi/erp/inventory/goodsreceipt/application/usecase/command/CompleteGoodsReceiptUseCaseImpl.java:L54-L108` - completion transaction behavior to mirror
      ref: `src/main/java/com/solusi/erp/inventory/goodsreceipt/application/usecase/command/CompleteGoodsReceiptUseCaseImpl.java:L261-L277` - stock payload builder pattern
      ref: `src/main/java/com/solusi/erp/inventory/stock/domain/model/ReferenceType.java:L11-L19` - `GOODS_ISSUE` already exists for movement ledger
- [x] For specific-layer lines, pass valuation reference metadata into stock valuation so Purchase Return can consume the GR layer rather than global FIFO.
      ref: `docs/brainstorming/2026-06-01-generic-goods-issue.md:L189-L210` - specific GR layer costing rule
- [x] For serialized issue lines, require whole base quantity, parse CSV serials, and post one stock movement per serial where needed.
      ref: `src/main/java/com/solusi/erp/inventory/goodsreceipt/application/usecase/command/CompleteGoodsReceiptUseCaseImpl.java:L156-L230` - serialized GR line handling
- [x] Implement `CancelGoodsIssueUseCaseImpl`: validate `COMPLETED`, ensure open period, guard downstream dependencies via future `GoodsIssueInUseChecker`, post reversal stock movements and journal reversal, then set `CANCELLED`.
      ref: `docs/brainstorming/2026-06-01-generic-goods-issue.md:L170-L178` - cancellation requirement and conservative guard
      ref: `src/main/java/com/solusi/erp/accounting/journal/application/usecase/command/PostJournalForEventUseCaseImpl.java:L29-L70` - current journal post behavior; reversal may require extension
- [x] Wire command use cases in `GoodsIssueConfig` with `TransactionTemplate` for complete/cancel.
      ref: `src/main/java/com/solusi/erp/inventory/goodsreceipt/infrastructure/config/GoodsReceiptConfig.java:L55-L67` - transactional wrapper pattern
- [x] **TEST:** Add create/update/delete use case tests with Mockito.
      ref: `src/test/java/com/solusi/erp/inventory/goodsreceipt/application/usecase/command/CreateGoodsReceiptUseCaseTest.java` - command test package pattern
- [x] **TEST:** Add complete use case tests for stock payload, specific valuation metadata, journal command values, period closed, insufficient stock, serialized CSV, and immutable status.
      ref: `src/test/java/com/solusi/erp/inventory/goodsreceipt/application/usecase/command/CompleteGoodsReceiptUseCaseTest.java:L44-L92` - Mockito setup and stock payload verification
      ref: `src/test/java/com/solusi/erp/inventory/goodsreceipt/application/usecase/command/CompleteGoodsReceiptUseCaseTest.java:L247-L270` - amount snapshot verification pattern
- [x] **TEST:** Add cancel use case tests for happy path reversal and all guard failures.
      ref: `docs/brainstorming/2026-06-01-generic-goods-issue.md:L339-L343` - cancel and journal open questions resolved in this plan

**Validation criteria:**
- `mvn test -Dtest="*GoodsIssue*UseCaseTest"` passes.
- Complete is atomic: if stock or journal fails, GI status does not persist as completed.
- Purchase Return-style specific layer lines never consume valuation from unrelated GR lines.

### Task 6: Web DTO, Mapper, Controller, List, And Detail UI

Expose GI list/detail and JSON-backed command endpoints while keeping web layer dependent only on use cases and lookup providers.

**Depends on:** Task 5
**Reference module:** `inventory.goodsreceipt`

Steps:
- [x] Create DTOs extending `BaseAuditResponse`: `GoodsIssueSummaryResponse`, `GoodsIssueDetailResponse`, `GoodsIssueLineDetailResponse`, `GoodsIssueSaveRequest`, and `GoodsIssueSaveLineRequest`.
      ref: `src/main/java/com/solusi/erp/inventory/goodsreceipt/web/dto/GoodsReceiptSaveRequest.java:L15-L31` - request DTO inheritance and nested validation
      ref: `src/main/java/com/solusi/erp/inventory/goodsreceipt/web/dto/GoodsReceiptSummaryResponse.java:L11-L23` - summary DTO shape
- [x] Add `@DateTimeFormat(pattern = "yyyy-MM-dd")` to `issueDate` and validation annotations for mandatory header/line fields.
      ref: `docs/spec/datetime-standards.md:L65-L91` - backend date annotation requirement
- [x] Create `GoodsIssueWebMapper` using lookup providers for party, facility, grid, container, product, UOM, currency, and reference code enrichment.
      ref: `src/main/java/com/solusi/erp/inventory/goodsreceipt/web/mapper/GoodsReceiptWebMapper.java:L23-L73` - mapper and lookup provider enrichment pattern
      ref: `docs/spec/autocomplete-generic.md:L121-L173` - lookup provider boundary and Trinity display data
- [x] Create `GoodsIssueController` with `@DefaultRedirectUrl`, list, create form, edit form, view, save JSON, complete JSON action, cancel JSON action, and delete draft endpoint.
      ref: `src/main/java/com/solusi/erp/inventory/goodsreceipt/web/controller/GoodsReceiptController.java:L35-L83` - controller list shape and permission annotations
      ref: `src/main/java/com/solusi/erp/inventory/goodsreceipt/web/controller/GoodsReceiptController.java:L206-L235` - AJAX JSON save endpoint
      ref: `src/main/java/com/solusi/erp/inventory/goodsreceipt/web/controller/GoodsReceiptController.java:L271-L289` - document action and HTMX delete endpoint
- [x] Enforce permissions with `@PreAuthorize`: read/create/update/delete/complete/cancel. Do not use `hasRole()`.
      ref: `docs/AGENTS.md:L137-L143` - backend/frontend RBAC requirement
- [x] Build list template with Tabler card, right-aligned search bar, `div.input-icon`, `.form-control-sm`, generic sortable fragment, generic pagination fragment, active reference chip, status badge, action buttons with `.btn-white.btn-sm`, and delete modal fragment for draft rows.
      ref: `docs/spec/ui-standards.md:L8-L19` - standard list page layout
      ref: `src/main/resources/templates/inventory/goods-receipts/list.html:L29-L66` - GR search/filter/table container pattern
      ref: `src/main/resources/templates/inventory/goods-receipts/list.html:L69-L139` - sortable, action buttons, delete modal, pagination
- [x] Build detail template with header status badge, readonly header snapshot, journal link area for completed GI, line table with issue qty/base qty/location/serial/cost/amount, complete/cancel action buttons, and audit fragment.
      ref: `src/main/resources/templates/inventory/goods-receipts/view.html:L10-L65` - detail header/status/journal link pattern
      ref: `src/main/resources/templates/inventory/goods-receipts/view.html:L98-L143` - line detail table pattern
      ref: `src/main/resources/templates/inventory/goods-receipts/view.html:L145-L198` - action buttons and delete modal pattern
- [x] Use adaptive theme classes only: avoid manual `bg-light`, `bg-white`, and `text-dark`; use `bg-body-tertiary`, `bg-secondary-lt`, `text-body`, or default text.
      ref: `docs/spec/ui-standards.md:L77-L93` - dark/warm/green theme compatibility rules
- [x] **TEST:** Add controller tests for view names, model attributes, JSON save responses, action endpoints, delete endpoint, unsupported source handling, and `@PreAuthorize` values.
      ref: `src/test/java/com/solusi/erp/inventory/goodsreceipt/web/controller/GoodsReceiptControllerTest.java:L48-L84` - controller test setup
      ref: `src/test/java/com/solusi/erp/inventory/goodsreceipt/web/controller/GoodsReceiptControllerTest.java:L102-L181` - list/reference filter tests
- [x] **TEST:** Add list/detail template contract tests for search structure, sortable/pagination fragments, action button attributes, security guards, adaptive classes, and no hardcoded static text outside i18n.
      ref: `src/test/java/com/solusi/erp/inventory/goodsreceipt/web/template/integration/GoodsReceiptListIntegrationTest.java:L18-L57` - list contract test pattern
      ref: `src/test/java/com/solusi/erp/inventory/goodsreceipt/web/template/integration/GoodsReceiptViewIntegrationTest.java:L18-L64` - view contract test pattern

**Validation criteria:**
- `mvn test -Dtest="GoodsIssueControllerTest,GoodsIssueWebMapperTest,GoodsIssueListIntegrationTest,GoodsIssueViewIntegrationTest"` passes.
- `/inventory/goods-issues` supports keyword search, sorting, pagination, and reference filter retention.
- No controller injects JPA repository or entity from another slice.

### Task 7: GI Form HTML Structure With Header-Lines UI

Create the GI create/edit form as a complete transaction screen, with source snapshot header and line table designed for outbound issue.

**Depends on:** Task 6
**Reference module:** `inventory.goodsreceipt`, UI specs

Steps:
- [x] Read `docs/spec/index.md` before editing the form and keep the relevant component specs open: UI standards, header-lines form, autocomplete, modal selector, numeric, datetime, form submission, action buttons, and page-specific scripts.
      ref: `docs/spec/index.md:L5-L17` - relevant UI specs list
- [x] Create `templates/inventory/goods-issues/form.html` using native Thymeleaf layout slot `layout(~{:: .gi-form-content}, ~{:: #page-specific-scripts})`.
      ref: `src/main/resources/templates/inventory/goods-receipts/form.html:L1-L8` - native Thymeleaf layout usage
      ref: `docs/spec/page-specific-scripts.md:L40-L59` - page script slot pattern
- [x] Header area: show read-only `referenceType`, `referenceCode`, `partyType`, `partyName`, `facilityName`, `currencyCode`, and `exchangeRate`; use plaintext display for source-derived values so users do not edit source snapshots accidentally.
      ref: `src/main/resources/templates/inventory/goods-receipts/form.html:L55-L79` - readonly source snapshot header pattern
      ref: `docs/brainstorming/2026-06-01-generic-goods-issue.md:L90-L96` - generic party and facility decisions
- [x] Include hidden fields for `id`, `referenceType`, `referenceId`, `referenceCode`, `partyId`, `partyType`, `facilityId`, `currencyId`, `exchangeRate`, and line valuation references to preserve source data through AJAX submit.
      ref: `src/main/resources/templates/inventory/goods-receipts/form.html:L39-L50` - hidden reference fields pattern
- [x] Date field: render `issueDate` through the standard date fragment or `data-picker="date"` input; DTO must have `@DateTimeFormat("yyyy-MM-dd")`.
      ref: `docs/spec/datetime-standards.md:L21-L44` - `data-picker` HTML contract
      ref: `docs/spec/datetime-standards.md:L144-L153` - date form checklist
- [x] Form submission: mark the form with `data-ajax-form="true"` and `data-redirect-on-success="/inventory/goods-issues"`; include `.alert-container` and loading indicator.
      ref: `docs/spec/form-submission.md:L16-L50` - AJAX CRUD form contract
      ref: `src/main/resources/templates/inventory/goods-receipts/form.html:L39-L54` - GR form AJAX setup
- [x] Line table columns: product, issue qty, base qty, UOM, facility snapshot, grid, container, serial/detail, unit cost, inventory amount, and actions. Mandatory columns must show `required` marker.
      ref: `docs/spec/header-lines-form.md:L7-L15` - required header-lines structure
      ref: `docs/spec/header-lines-form.md:L55-L59` - mandatory marker and backend validation
- [x] Use `fragments/inputs :: table-autocomplete` for product, grid, and container fields where editable/manual; for source-derived lines, product/UOM/valuation fields are locked snapshot values.
      ref: `docs/spec/autocomplete-generic.md:L34-L49` - fragment usage and Trinity data
      ref: `docs/spec/header-lines-form.md:L47-L54` - derived document line locking rule
- [x] Every autocomplete-backed line DTO must carry initial Trinity data: `productName/productCode`, `gridName/gridCode`, `containerName/containerCode`, `uomName/uomCode`.
      ref: `docs/spec/autocomplete-generic.md:L42-L49` - required `initialValue`, `initialText`, `initialSubtext`
      ref: `docs/spec/ui-standards.md:L149-L156` - Trinity data rule in UI standards
- [x] Numeric inputs must use text inputs with `.erp-number-decimal` or standard numeric fragments. Do not use `<input type="number">`.
      ref: `docs/spec/numeric-standards.md:L7-L17` - numeric input rule
      ref: `docs/spec/numeric-standards.md:L65-L71` - precision consistency for dynamic lines
- [x] Add row template in hidden `#row-template-source` with clean `INDEX` placeholders, no stale TomSelect wrappers, and all hidden source/valuation fields.
      ref: `src/main/resources/templates/inventory/goods-receipts/form.html:L164-L195` - dynamic row template pattern
      ref: `docs/spec/header-lines-form.md:L18-L26` - `ErpLineManager` indexing expectation
- [x] Detail drawer/offcanvas: provide separate standard and serialized item drawers. Serialized drawer edits serial CSV through row-level inputs; standard drawer edits qty/UOM and computes base qty.
      ref: `src/main/resources/templates/inventory/goods-receipts/form.html:L197-L301` - drawer pattern for standard and serialized GR lines
      ref: `docs/spec/header-lines-form.md:L29-L37` - `ErpInventory` and serial sync requirement
- [x] Add action buttons: save, back/cancel link, complete button for draft persisted GI, and cancel button for completed GI. Complete/cancel must use `ErpForm.postAction` and modal confirmation.
      ref: `docs/spec/action-buttons.md:L5-L29` - document action button contract
      ref: `src/main/resources/templates/inventory/goods-receipts/form.html:L19-L31` - complete button attributes
- [x] Include modal selector shell for source line selector with stable `modal-gi-source-line-selector` and root/body id `gi-source-line-selector-results`.
      ref: `docs/spec/modal-selector.md:L28-L39` - modal shell/root id contract
      ref: `src/main/resources/templates/inventory/goods-receipts/form.html:L303-L319` - modal shell and script include pattern
- [x] Keep form dense and operational. Do not add marketing/instructional copy; use compact helper text only where it prevents transaction mistakes.
      ref: `docs/spec/ui-standards.md:L47-L60` - standard input heights
- [x] **TEST:** Add form template contract tests for AJAX attributes, hidden source/valuation fields, date picker, numeric class use, autocomplete Trinity params, source-derived lock indicators, drawer elements, modal shell ids, and action button data attributes.
      ref: `src/test/java/com/solusi/erp/inventory/goodsreceipt/web/template/integration/GoodsReceiptFormIntegrationTest.java:L54-L74` - form contract basics
      ref: `src/test/java/com/solusi/erp/inventory/goodsreceipt/web/template/integration/GoodsReceiptFormIntegrationTest.java:L167-L224` - serialized drawer contract tests

**Validation criteria:**
- `mvn test -Dtest="GoodsIssueFormIntegrationTest"` passes.
- Form renders in create and edit mode with at least one prefilled source line.
- Completed/cancelled GI inputs are locked from normal edit.
- Template does not contain `bg-light`, `bg-white`, `text-dark`, or `<input type="number">`.

### Task 8: GI Page-Specific JavaScript

Implement GI form behavior in a dedicated JS file instead of expanding global helpers.

**Depends on:** Task 7
**Reference module:** `inventory.goodsreceipt`, `inventory.adjustment`

Steps:
- [x] Create `static/js/inventory/goods-issue/goods-issue-form.js` and keep only small page config inline in the template.
      ref: `docs/spec/page-specific-scripts.md:L12-L36` - when to move JS into feature file
      ref: `src/main/resources/templates/inventory/goods-receipts/form.html:L306-L319` - inline config plus deferred script pattern
- [x] Initialize dynamic lines through existing global helpers where available (`window.ERP.initAutocompleteInContainer`, `initLookup`, `ErpNumeric`, `ErpDrawer`, `ErpModal`) instead of duplicating generic behavior.
      ref: `docs/spec/page-specific-scripts.md:L61-L70` - allowed global helper boundaries
      ref: `docs/spec/ui-standards.md:L142-L166` - global auto-initialization and `initLookup`
- [x] Use `initLookup` for cascading `facility -> grid -> container` or inverse `container -> grid/facility` behavior. Container lookup payload must include parent grid/facility data.
      ref: `docs/spec/autocomplete-generic.md:L50-L63` - cascading lookup pattern
      ref: `docs/spec/autocomplete-generic.md:L68-L97` - inverse auto-populate payload pattern
- [x] When user changes header facility, require confirmation if draft lines exist; after confirmation clear incompatible lines or reset grid/container fields. Use `ErpModal.confirm`, not `window.confirm`.
      ref: `docs/spec/header-lines-form.md:L62-L65` - header-to-line synchronization
      ref: `docs/spec/ui-standards.md:L96-L115` - global `ErpModal` rule
- [x] For source-derived lines, lock product/UOM/source valuation fields and open source line selector when Add Line is clicked. For manual future source, allow blank row only when `referenceType=MANUAL`.
      ref: `docs/spec/header-lines-form.md:L47-L54` - derived vs manual line behavior
      ref: `src/main/resources/static/js/inventory/goods-receipt/goods-receipt-form.js:L578-L586` - Add Line switches to selector when source-based
- [x] Implement source line selector apply: parse `data-*` payload, prevent duplicate `referenceLineId`, create clean row from template, fill product/UOM/valuation/location snapshots, and close modal through shared modal selector helper.
      ref: `docs/spec/modal-selector.md:L77-L90` - multi-select apply behavior
      ref: `src/main/resources/static/js/inventory/goods-receipt/goods-receipt-form.js:L597-L641` - selector apply and duplicate guard
- [x] Implement drawer save: write qty/UOM/base qty and serialized CSV back to hidden line inputs; for serialized items, require whole base qty.
      ref: `src/main/resources/static/js/inventory/goods-receipt/goods-receipt-form.js:L435-L479` - drawer setup and save pattern
      ref: `src/main/resources/static/js/inventory/goods-receipt/goods-receipt-form.js:L299-L333` - serial row sync pattern
- [x] Calculate summary totals: total base qty, total inventory amount, total tax amount when available, and line count. Display recap in `bg-secondary-lt` or neutral theme-safe class.
      ref: `docs/spec/header-lines-form.md:L11-L15` - summary card expectation
      ref: `docs/spec/ui-standards.md:L80-L93` - theme-safe background/text classes
- [x] Use `ErpNumeric.get/set` when manipulating AutoNumeric values. Avoid ad-hoc parsing except as fallback.
      ref: `docs/spec/numeric-standards.md:L33-L40` - `ErpNumeric` helper requirement
- [x] Register submit validation in capture phase. Validate at least one line, product, positive qty, UOM, container, grid/facility consistency, and serial count for serialized item.
      ref: `docs/spec/form-submission.md:L52-L58` - capture-phase custom validation
      ref: `src/main/resources/static/js/inventory/goods-receipt/goods-receipt-form.js:L645-L682` - line-level submit guard pattern
- [x] Add dirty form beforeunload guard and suppress it for intentional complete/cancel action redirects.
      ref: `docs/spec/form-submission.md:L59-L67` - intentional navigation and beforeunload rule
      ref: `src/main/resources/static/js/inventory/goods-receipt/goods-receipt-form.js:L684-L699` - dirty form guard pattern
- [x] **TEST:** Add static JS contract tests in `GoodsIssueFormIntegrationTest`: verifies selector ids, duplicate guard, hidden valuation field population, `ErpModal.confirm`, `ErpNumeric.get/set`, capture submit listener, and no direct `new bootstrap.Modal()`.
      ref: `src/test/java/com/solusi/erp/inventory/goodsreceipt/web/template/integration/GoodsReceiptFormIntegrationTest.java:L226-L240` - script block contract test pattern
      ref: `docs/spec/ui-standards.md:L96-L115` - no direct bootstrap modal rule

**Validation criteria:**
- `mvn test -Dtest="GoodsIssueFormIntegrationTest"` passes.
- Manual browser smoke can add/remove source lines, edit qty drawer, set serial CSV, submit draft, complete, and cancel without losing TomSelect/AutoNumeric state.
- No GI-specific flow is added to `shared/erp-common-handler.js`.

### Task 9: Source Line Selector Endpoint And Fragment

Add selector infrastructure for source lines so GI can pull eligible outbound lines from Purchase Return later without rewriting the UI.

**Depends on:** Task 8
**Reference module:** `inventory.goodsreceipt`

Steps:
- [x] Create selector row DTO such as `GoodsIssueSourceLineSelectorRow` with only data needed by the UI: source line id, product Trinity data, qty remaining, UOM Trinity data, facility/grid/container snapshot, serialized flag, valuation reference, and monetary source values.
      ref: `docs/spec/modal-selector.md:L49-L65` - row DTO vs selection payload contract
- [x] Add controller endpoint `GET /inventory/goods-issues/selectors/source-lines` with `referenceType`, `referenceId`, `keyword`, and `excludeReferenceLineIds`.
      ref: `src/main/java/com/solusi/erp/inventory/goodsreceipt/web/controller/GoodsReceiptController.java:L99-L143` - GR PO line selector endpoint pattern
- [x] Apply query-level exclusion before rendering: no exhausted lines, no ineligible source status, no lines already selected in current draft.
      ref: `docs/spec/modal-selector.md:L67-L76` - query-level exclusion rules
- [x] Create `templates/inventory/goods-issues/fragments/source-line-selector-modal.html` with root id and fragment name `gi-source-line-selector-results`, HTMX search form, table, empty state, generic pagination, and footer Apply button.
      ref: `docs/spec/modal-selector.md:L40-L48` - selector fragment minimum structure
      ref: `src/main/resources/templates/inventory/goods-receipts/fragments/po-line-selector-modal.html:L4-L28` - selector search form pattern
      ref: `src/main/resources/templates/inventory/goods-receipts/fragments/po-line-selector-modal.html:L31-L92` - selector table, payload, pagination, apply button
- [x] Put selection payload in `data-*` attributes, including valuation refs: `data-reference-line-id`, `data-product-id`, `data-product-name`, `data-product-subtext`, `data-uom-id`, `data-uom-name`, `data-uom-subtext`, `data-facility-id`, `data-grid-id`, `data-container-id`, `data-valuation-ref-type`, `data-valuation-ref-id`, `data-valuation-ref-line-id`, `data-serialized`, and `data-remaining-quantity`.
      ref: `src/main/resources/templates/inventory/goods-receipts/fragments/po-line-selector-modal.html:L47-L56` - row `data-*` payload pattern
- [x] If Purchase Return is unavailable, endpoint should return empty page with a clear i18n warning for unsupported reference type. Do not fake Purchase Return data.
      ref: `docs/brainstorming/2026-06-01-generic-goods-issue.md:L345-L348` - Purchase Return confirm flow can be automatic later
- [x] **TEST:** Add controller tests for endpoint model attributes, unsupported reference type, exclusion ids, and HTMX fragment view.
      ref: `src/test/java/com/solusi/erp/inventory/goodsreceipt/web/controller/GoodsReceiptControllerTest.java:L203-L220` - selector enrichment test setup pattern
- [x] **TEST:** Add template contract tests for root id/fragment matching, search `hx-target`, payload attributes, pagination fragment, and apply button class.
      ref: `docs/spec/modal-selector.md:L116-L135` - selector consumer/testing checklist

**Validation criteria:**
- `mvn test -Dtest="GoodsIssueControllerTest,GoodsIssueFormIntegrationTest"` passes.
- Selector fragment root id, body id, and HTMX target are identical enough to avoid `htmx:targetError`.
- Exclusion happens in the query/use-case layer before render, not by hiding rows in HTML.

### Task 10: Purchase Return Integration Seam

Prepare the source-specific integration points required for Purchase Return without coupling GI core to a module that is not present yet.

**Depends on:** Task 9
**Reference module:** future `purchasing.purchasereturn`, `inventory.goodsreceipt`, `inventory.stock`

Steps:
- [ ] Define the minimum port contract that Purchase Return must expose to GI: header lookup, eligible return lines, GR/GR line valuation refs, supplier/party id, facility, currency/rate from original GR, tax reversal amount, and bill-posted/clearing target.
      ref: `docs/brainstorming/2026-06-01-generic-goods-issue.md:L180-L210` - Purchase Return via GI and valuation source
      ref: `docs/brainstorming/2026-06-01-generic-goods-issue.md:L211-L233` - original GR rate and no FX variance decision
- [ ] Add a `GoodsIssueSourceResolver` implementation only after the Purchase Return repository/read port exists; it should return a draft GI with `referenceType=PURCHASE_RETURN`, source reference code, `partyType=SUPPLIER`, source facility, original currency/rate, line qty, container, serial CSV, and valuation GR refs.
      ref: `src/main/java/com/solusi/erp/inventory/goodsreceipt/infrastructure/adapter/PurchaseOrderGoodsReceiptSourceResolver.java:L33-L78` - source resolver maps source doc to draft document
      ref: `docs/brainstorming/2026-06-01-generic-goods-issue.md:L271-L286` - resolver fills header and line data
- [ ] Purchase Return confirm use case should call GI create+complete instead of `StockService.adjust()` directly. This is out of core GI until Purchase Return exists, but the plan must keep this as the integration acceptance target.
      ref: `docs/brainstorming/2026-06-01-generic-goods-issue.md:L14-L15` - decision to route Purchase Return through GI
      ref: `docs/brainstorming/2026-06-01-generic-goods-issue.md:L345-L347` - initial recommendation to auto create+complete GI on confirm
- [ ] Add idempotency guard: one completed GI per Purchase Return unless explicit cancel/reversal rules allow retry.
      ref: `src/main/resources/db/migration/V55__Add_Journal_Core.sql:L1-L16` - source uniqueness pattern for journal entries
- [ ] Add tests in the Purchase Return task later: confirm PR creates completed GI, stock movement reference points to GI, valuation layer consumed from GR line, and no FX variance is posted.
      ref: `docs/brainstorming/2026-06-01-generic-goods-issue.md:L243-L265` - Purchase Return journal amount rules

**Validation criteria:**
- Core GI compiles and tests pass without Purchase Return classes.
- Report records whether concrete Purchase Return resolver was implemented or deferred because module is absent.
- Future Purchase Return task has a clear port contract and acceptance checklist.

### Task 11: Accounting Schema And Journal Support

Add journal variables/schema support for GI and Purchase Return-specific return accounting without breaking existing GR/VB/VP auto-posting.

**Depends on:** Task 5
**Reference module:** `accounting.journal`, `accounting.schema`, `inventory.goodsreceipt`

Steps:
- [ ] Review current `SchemaEventType.GOODS_ISSUE` and existing GI variables. Keep generic `GOODS_ISSUE` for sales/COGS-style future flows, but add Purchase Return-specific variables/event only if journal differs from generic GI.
      ref: `src/main/java/com/solusi/erp/accounting/schema/domain/model/SchemaEventType.java:L7-L15` - existing auto-journal event enum
      ref: `src/main/java/com/solusi/erp/accounting/journal/domain/model/JournalVariable.java:L23-L30` - existing GI and stock adjustment variables
      ref: `docs/brainstorming/2026-06-01-generic-goods-issue.md:L234-L242` - source-specific journal recommendation
- [ ] For core generic GI completion, post journal command with source type `GOODS_ISSUE`, source id `gi.id`, source code `gi.code`, and values such as inventory credit amount and debit counterpart variable.
      ref: `src/main/java/com/solusi/erp/inventory/goodsreceipt/application/usecase/command/CompleteGoodsReceiptUseCaseImpl.java:L92-L104` - journal command construction pattern
      ref: `src/main/java/com/solusi/erp/accounting/journal/application/usecase/command/PostJournalForEventUseCaseImpl.java:L29-L70` - schema-driven posting behavior
- [ ] For Purchase Return, plan event/source-specific schema such as `PURCHASE_RETURN` if generic `GOODS_ISSUE` cannot represent debit GR/IR or AP, credit Inventory, and credit Input VAT.
      ref: `docs/brainstorming/2026-06-01-generic-goods-issue.md:L243-L265` - Purchase Return journal variants
- [ ] Add migration seed for default accounting schema lines only if the existing schema seed approach supports it. Otherwise, add menu/admin configuration notes and leave schema setup as manual admin data.
      ref: `docs/modules/accounting/accounting-schema.md` - accounting schema business documentation
- [ ] Ensure `PostJournalForEventUseCase` idempotency by source type/id still works for GI.
      ref: `src/main/java/com/solusi/erp/accounting/journal/application/usecase/command/PostJournalForEventUseCaseImpl.java:L29-L35` - duplicate source guard
- [ ] **TEST:** Add complete GI use case assertion that `PostJournalForEventUseCase` receives the expected event/source/values and does not post FX variance for Purchase Return source.
      ref: `src/test/java/com/solusi/erp/inventory/goodsreceipt/application/usecase/command/CompleteGoodsReceiptUseCaseTest.java:L3-L8` - journal imports and verification style
- [ ] **TEST:** Add schema variable unit tests if new `JournalVariable` entries are introduced.
      ref: `src/main/java/com/solusi/erp/accounting/journal/domain/model/JournalVariable.java:L42-L46` - `getVariablesForEvent` filter behavior

**Validation criteria:**
- `mvn test -Dtest="*GoodsIssue*UseCaseTest,*Journal*Test,*Schema*Test"` passes.
- Generic GI and Purchase Return journal decisions are not conflated silently.
- No existing GR/VB/VP journal tests regress.

### Task 12: Documentation, Module Index, And Final Verification

Document GI core behavior and run focused verification before handoff.

**Depends on:** Tasks 1-11
**Reference module:** project docs

Steps:
- [ ] Create `docs/modules/inventory/goods-issue.md` describing purpose, lifecycle, fields, source resolver model, stock posting, valuation, Purchase Return seam, cancel rules, and UI behavior.
      ref: `docs/index.md:L30-L32` - inventory module docs placement
      ref: `docs/brainstorming/2026-06-01-generic-goods-issue.md:L6-L15` - executive summary to preserve in module docs
- [ ] Update `docs/index.md` inventory module list with Goods Issue.
      ref: `docs/index.md:L30-L32` - current inventory docs list
- [ ] Update `docs/spec/index.md` only if a new reusable component pattern was added. GI-specific JS should not be added to spec docs.
      ref: `docs/spec/page-specific-scripts.md:L5-L10` - keep feature-specific logic out of shared helper docs
- [ ] Verify i18n keys for Indonesian and English messages. No hardcoded labels in templates except intentionally data-driven enum/status values.
      ref: `docs/AGENTS.md:L83-L91` - i18n implementation rules
- [ ] Run focused tests for inventory stock, GI domain/use cases/web/template, accounting journal, and migration-sensitive config tests.
      ref: `docs/tests/web-layer-testing.md` - web layer test guidance
- [ ] Run full test suite if focused tests pass and time permits: `mvn test`.
- [ ] Record final verification output, skipped items, and deferred Purchase Return adapter status in `docs/reports/2026-06-01-generic-goods-issue.md`.

**Validation criteria:**
- Focused GI-related tests pass.
- Documentation links are updated.
- Report clearly states implemented core, deferred source adapters, and any manual accounting schema setup.

## 6. Dependency Notes

- Task 1 must land before Task 5, otherwise Purchase Return-specific valuation cannot be implemented correctly.
- Task 2 and Task 3 can be developed together, but migrations must exist before persistence integration tests run.
- Task 7 and Task 8 should be reviewed together because the template contract and JS selectors are tightly coupled.
- Task 10 is intentionally a seam unless the Purchase Return module is added in the same development branch.
- Task 11 may split into generic GI schema and Purchase Return-specific schema depending on accounting event decisions during implementation.

## 7. Deferred Items

- Sales/Delivery Order resolver.
- Production/Consumption, Scrap, Internal Use resolvers.
- Manual GI free-form entry beyond structural readiness.
- Dedicated serial detail table per line.
- Warehouse approval workflow before completion.
- Picking/shipping workflow and WMS mobile UI.
- Full Playwright E2E specs. Add after core page behavior stabilizes.
