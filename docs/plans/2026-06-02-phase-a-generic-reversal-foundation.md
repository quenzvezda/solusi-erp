# Implementation Plan: Generic Reversal Foundation

> Source: `docs/brainstorming/2026-06-02-vendor-debit-memo.md`
> Created: 2026-06-03
> Sprint: 6 - Debit Memo Foundation
> Status: IN_PROGRESS
>
> **For agentic workers:** execute task-by-task. Explore references fresh before editing each task, update checkbox state incrementally, record deviations in `docs/reports/2026-06-02-phase-a-generic-reversal-foundation.md`, and do not mark a task complete before its validation command passes.

## 1. Goal

Phase A membangun primitive reversal generik sebelum Debit Memo dan Purchase Return accounting dipasang. Targetnya: journal reversal memakai linked reversal journal, stock movement reversal memakai linked reversal movement, valuation inbound reversal membentuk layer baru dengan historical issue cost, dan Generic Goods Issue cancellation pindah dari negative amount posting menjadi full linked reversal.

Phase ini tidak membuat Debit Memo, tidak menambah event `PURCHASE_RETURN`, dan tidak mengubah settlement Vendor Bill.

## 2. Locked Decisions

- Reversal journal tidak mem-post amount negatif ke accounting schema.
- Reversal journal membalik final `JournalLine` dari journal asal dan menyimpan `reversalOfId`.
- `ReversePostedJournalUseCase` adalah internal application API yang dapat dipakai manual journal dan auto-journal. Permission publik `JOURNAL-ENTRY_REVERSE` tetap hanya untuk manual journal UI.
- Satu posted journal hanya boleh direverse sekali.
- Reversal chain tidak boleh dibuat.
- `reversalDate` wajib berada pada accounting period `OPEN`.
- Stock movement reversal memakai `inv_movements.reversal_of_movement_id`, bukan enum reference type baru seperti `GOODS_ISSUE_REVERSAL`.
- Valuation reversal membuat inbound valuation layer baru dengan historical issue cost dari movement asal; tidak mengembalikan quantity ke GR layer lama secara diam-diam.
- Generic GI cancellation bersifat full reversal only.
- GI source-based seperti `PURCHASE_RETURN` tidak boleh dibatalkan langsung dari GI. Source module harus memanggil primitive reversal melalui use case bisnisnya sendiri pada phase berikutnya.
- Target lokasi reversal GI boleh container aktif lain dalam facility yang sama; default UI memakai lokasi issue historis.

## 3. Scope Boundary

### Included

- MariaDB dan H2 migration untuk linked stock movement reversal dan valuation layer reversal metadata.
- Generic journal reversal use case untuk manual dan auto-journal.
- Stock movement reversal primitive dan repository access untuk original movements.
- Valuation layer inbound reversal dengan historical issue unit cost.
- Refactor Generic GI cancellation agar memakai `reversalDate`, target container per line, linked stock reversal, dan linked journal reversal.
- UI cancellation form untuk GI manual/generic.
- Backend, domain, persistence, web, template, i18n, dan focused tests untuk Phase A.

### Deferred

- `SchemaEventType.PURCHASE_RETURN` dan route journal Purchase Return. Masuk Phase B.
- Debit Memo core dan allocation. Masuk Phase D dan E.
- Confirmed Purchase Return reversal orchestration. Masuk Phase F.
- Partial stock movement reversal. MVP Phase A hanya full reversal.
- Cross-facility reversal. MVP Phase A hanya facility yang sama.
- Direct auto-journal reversal dari Journal Entry UI. UI tetap manual-only.

## 4. Target File Map

### Database

- Create `src/main/resources/db/migration/V69__Add_Generic_Reversal_Foundation.sql`
- Create `src/main/resources/db/migration-h2/V69__Add_Generic_Reversal_Foundation.sql`

### Accounting Journal

- Modify `src/main/java/com/solusi/erp/accounting/journal/domain/model/JournalEntry.java`
- Modify `src/main/java/com/solusi/erp/accounting/journal/application/usecase/command/ReverseManualJournalUseCaseImpl.java`
- Create `src/main/java/com/solusi/erp/accounting/journal/application/usecase/command/ReversePostedJournalUseCase.java`
- Create `src/main/java/com/solusi/erp/accounting/journal/application/usecase/command/ReversePostedJournalUseCaseImpl.java`
- Modify `src/main/java/com/solusi/erp/accounting/journal/infrastructure/config/JournalConfig.java`

### Inventory Stock

- Modify `src/main/java/com/solusi/erp/inventory/stock/application/dto/StockMovementPayload.java`
- Modify `src/main/java/com/solusi/erp/inventory/stock/infrastructure/persistence/InventoryMovementEntity.java`
- Modify `src/main/java/com/solusi/erp/inventory/stock/infrastructure/persistence/InventoryMovementJpaRepository.java`
- Modify `src/main/java/com/solusi/erp/inventory/stock/infrastructure/service/StockServiceImpl.java`
- Modify `src/main/java/com/solusi/erp/inventory/stock/domain/model/ValuationLayer.java`
- Modify `src/main/java/com/solusi/erp/inventory/stock/infrastructure/persistence/ValuationLayerEntity.java`
- Modify `src/main/java/com/solusi/erp/inventory/stock/domain/repository/ValuationLayerRepository.java`
- Modify `src/main/java/com/solusi/erp/inventory/stock/infrastructure/persistence/ValuationLayerJpaRepository.java`
- Modify `src/main/java/com/solusi/erp/inventory/stock/domain/service/FifoValuationService.java`
- Create stock reversal request/service files under `src/main/java/com/solusi/erp/inventory/stock/domain/port/` and `infrastructure/service/`

### Goods Issue

- Modify `src/main/java/com/solusi/erp/inventory/goodsissue/domain/model/GoodsIssue.java`
- Modify `src/main/java/com/solusi/erp/inventory/goodsissue/application/usecase/command/CancelGoodsIssueUseCase.java`
- Modify `src/main/java/com/solusi/erp/inventory/goodsissue/application/usecase/command/CancelGoodsIssueUseCaseImpl.java`
- Create `GoodsIssueCancelCommand` and `GoodsIssueCancelLineCommand`
- Modify `src/main/java/com/solusi/erp/inventory/goodsissue/infrastructure/config/GoodsIssueConfig.java`
- Modify `src/main/java/com/solusi/erp/inventory/goodsissue/web/controller/GoodsIssueController.java`
- Create `src/main/java/com/solusi/erp/inventory/goodsissue/web/dto/GoodsIssueCancelRequest.java`
- Create `src/main/java/com/solusi/erp/inventory/goodsissue/web/dto/GoodsIssueCancelLineRequest.java`
- Create `src/main/resources/templates/inventory/goods-issues/cancel.html`
- Create `src/main/resources/static/js/inventory/goods-issue/goods-issue-cancel.js`

### Docs And Tests

- Modify `docs/modules/inventory/goods-issue.md`
- Modify `src/main/resources/messages.properties`
- Modify `src/main/resources/messages_id.properties`
- Add or update focused tests under accounting journal, inventory stock, and inventory goods issue packages.
- Add `e2e-tests/tests/inventory/goods-issue-cancellation.spec.ts` if browser coverage is implemented in this phase.

## 5. Tasks

### Task 1: Reversal Schema Migration [x]

Add database primitives for linked stock movement reversal and valuation layer reversal.

**Depends on:** none
**Reference modules:** `accounting.journal`, `inventory.stock`

- [x] Create `V69__Add_Generic_Reversal_Foundation.sql` in MariaDB and H2 migration folders.
      ref: `src/main/resources/db/migration/V64__Add_Manual_Journal.sql:L1-L11` - linked journal `reversal_of_id` and unique guard pattern
      ref: `src/main/resources/db/migration/V23__Inventory_Core_Valuation_And_Movements.sql:L29-L114` - current movement and valuation table shape
- [x] Add nullable `reversal_of_movement_id` to `inv_movements`, with FK to `inv_movements(id)` and unique constraint so one movement can be reversed at most once.
      ref: `src/main/java/com/solusi/erp/inventory/stock/infrastructure/persistence/InventoryMovementEntity.java:L18-L63` - current movement entity fields
- [x] Add indexes for reversal lookup and source movement fetch: `(reference_type, reference_id)`, `(reversal_of_movement_id)`, and any existing search indexes needed by `InventoryMovementJpaRepository`.
      ref: `src/main/java/com/solusi/erp/inventory/stock/infrastructure/persistence/InventoryMovementJpaRepository.java:L19-L38` - current movement search filters
- [x] Add nullable `reversal_of_movement_id` to `inv_valuation_layers` so inbound reversal layers can be tied to the original issue movement.
      ref: `src/main/resources/db/migration/V65__Add_Valuation_Layer_Reference_Metadata.sql:L1-L8` - valuation reference metadata migration pattern
- [x] Add static migration contract tests for MariaDB and H2 files covering new columns, FK, unique guard, indexes, and mirror parity.
      ref: `src/test/java/com/solusi/erp/inventory/goodsissue/infrastructure/persistence/GoodsIssueMigrationTest.java` - migration static contract test pattern

**Validation criteria:**

- `mvn test -Dtest="GoodsIssueMigrationTest,*Reversal*MigrationTest"` passes.
- MariaDB and H2 migrations contain equivalent business columns and constraints.
- No new enum reference type is introduced for stock reversal.

### Task 2: Generic Linked Journal Reversal Use Case [x]

Generalize journal reversal so auto-journals can be reversed internally without exposing public auto-reversal from Journal Entry UI.

**Depends on:** Task 1
**Reference modules:** `accounting.journal`

- [x] Refactor `JournalEntry.createReversal(...)` so it can reverse any `POSTED` non-reversal journal while preserving manual journal behavior.
      Keep the manual-only public guard in `ReverseManualJournalUseCaseImpl`, not in the domain primitive.
      ref: `src/main/java/com/solusi/erp/accounting/journal/domain/model/JournalEntry.java:L131-L163` - current reversal is posted/manual-only and flips lines
- [x] Decide and implement reversal source metadata so `uk_acc_journal_source` is not violated. The existing manual reversal uses `sourceId = null`; if auto reversal also leaves source id null, ensure `reversalOfId` is the primary audit link.
      ref: `src/main/resources/db/migration/V55__Add_Journal_Core.sql:L1-L16` - unique source guard for normal auto-journal
      ref: `src/main/java/com/solusi/erp/accounting/journal/domain/model/JournalEntry.java:L149-L159` - current reversal constructor source fields
- [x] Create `ReversePostedJournalUseCase` with command fields `originalJournalEntryId`, `reversalDate`, and `description`.
      ref: `src/main/java/com/solusi/erp/accounting/journal/application/usecase/command/ReverseManualJournalUseCase.java:L1-L8` - current manual reversal interface shape
- [x] Implement `ReversePostedJournalUseCaseImpl`: validate non-null date, load original, require `POSTED`, reject reversal chain, reject already reversed via `existsReversalOf`, ensure period open, create linked reversal, save, and translate unique constraint to friendly `DomainException`.
      ref: `src/main/java/com/solusi/erp/accounting/journal/application/usecase/command/ReverseManualJournalUseCaseImpl.java:L24-L49` - current guard and unique translation pattern
- [x] Refactor `ReverseManualJournalUseCaseImpl` to delegate to `ReversePostedJournalUseCase` after it validates `orig.isManual()`, or keep a thin wrapper that shares a private reversal service. Do not duplicate reversal guard logic.
      ref: `src/main/java/com/solusi/erp/accounting/journal/application/usecase/command/ReverseManualJournalUseCaseImpl.java:L30-L44` - manual-specific guard currently mixed with generic guard
- [x] Wire the new internal use case in `JournalConfig` without adding a new public permission.
      ref: `src/main/java/com/solusi/erp/accounting/journal/infrastructure/config/JournalConfig.java:L96-L118` - current manual reversal bean wiring
- [x] **TEST:** Add `ReversePostedJournalUseCaseTest` for auto-journal reversal, manual journal reversal, null date, missing original, draft original, reversal chain, already reversed, closed period, and duplicate constraint fallback.
      ref: `src/test/java/com/solusi/erp/accounting/journal/application/usecase/command/ReverseManualJournalUseCaseTest.java:L20-L97` - Mockito guard coverage pattern
- [x] **TEST:** Extend `JournalEntryTest` for reversing posted auto-journal lines and rejecting reversal chain at domain level.
      ref: `src/test/java/com/solusi/erp/accounting/journal/domain/model/JournalEntryTest.java` - domain model test location
- [x] **TEST:** Extend `JournalConfigTest` to prove the new bean is available and manual reversal still wires.
      ref: `src/test/java/com/solusi/erp/accounting/journal/infrastructure/config/JournalConfigTest.java` - config test pattern

**Validation criteria:**

- `mvn test -Dtest="JournalEntryTest,ReverseManualJournalUseCaseTest,ReversePostedJournalUseCaseTest,JournalConfigTest"` passes.
- Manual journal UI behavior remains manual-only.
- Auto-journal reversal does not call accounting schema and does not use negative amount posting.

### Task 3: Linked Stock Movement Reversal Primitive

Add a reusable stock reversal service that mirrors original issue movements with inbound movements linked by `reversalOfMovementId`.

**Depends on:** Task 1
**Reference modules:** `inventory.stock`, `inventory.goodsissue`

- [ ] Extend `StockMovementPayload` with optional `reversalOfMovementId` and keep it null for normal stock movements.
      ref: `src/main/java/com/solusi/erp/inventory/stock/application/dto/StockMovementPayload.java:L20-L60` - current stock adjustment payload contract
- [ ] Add `reversalOfMovementId` to `InventoryMovementEntity` and persist it in `StockServiceImpl.logMovement(...)`.
      ref: `src/main/java/com/solusi/erp/inventory/stock/infrastructure/persistence/InventoryMovementEntity.java:L18-L63` - current movement columns
      ref: `src/main/java/com/solusi/erp/inventory/stock/infrastructure/service/StockServiceImpl.java:L164-L176` - current movement logging path
- [ ] Add repository methods to fetch original movements by `referenceType/referenceId`, fetch by ids, and check `existsByReversalOfMovementId`.
      Use locked query methods if confirm/cancel orchestration needs pessimistic protection.
      ref: `src/main/java/com/solusi/erp/inventory/stock/infrastructure/persistence/InventoryMovementJpaRepository.java:L19-L39` - current repository search shape
- [ ] Create a stock reversal command/request containing original movement id, target container id, reversal date, and optional reason/context.
- [ ] Create `StockMovementReversalService` that validates:
      original movement exists;
      original movement is outbound for Phase A (`ISSUE`, `ISSUE_RESERVED`, `TRANSFER_OUT`, or negative `ADJUSTMENT` if supported);
      original movement has not been reversed;
      target container belongs to the same facility as the original movement container;
      serialized stock is not already on-hand;
      reversal quantity is full quantity for MVP.
      ref: `docs/brainstorming/2026-06-02-vendor-debit-memo.md` - Generic Stock Movement Reversal Standard
- [ ] For each valid original issue movement, call `StockService.adjust()` with `MovementType.RECEIPT`, positive full quantity, original `ReferenceType` and source id/code, target container, original serial, original unit cost as net price, `transactionDate = reversalDate.atStartOfDay()`, and `reversalOfMovementId = original.id`.
      ref: `src/main/java/com/solusi/erp/inventory/goodsissue/application/usecase/command/CancelGoodsIssueUseCaseImpl.java:L62-L80` - current ad-hoc reversal payload to replace
- [ ] Ensure the service catches unique constraint races on `reversalOfMovementId` and returns a friendly DomainException such as `msg.err.stock.reversal.already.reversed`.
      ref: `src/main/java/com/solusi/erp/accounting/journal/application/usecase/command/ReverseManualJournalUseCaseImpl.java:L45-L49` - unique constraint fallback pattern
- [ ] Wire the service in `StockConfig`.
      ref: `src/main/java/com/solusi/erp/inventory/stock/infrastructure/config/StockConfig.java` - stock bean configuration
- [ ] **TEST:** Add `StockMovementReversalServiceTest` with Mockito/in-memory doubles for happy path, already reversed, target facility mismatch, serial already on hand, non-outbound movement rejection, and duplicate constraint fallback.
      ref: `src/test/java/com/solusi/erp/inventory/stock/infrastructure/service/StockServiceTest.java` - stock service test style
- [ ] **TEST:** Extend `StockServiceTest` to assert `reversalOfMovementId` is persisted on logged reversal movement payloads.
      ref: `src/test/java/com/solusi/erp/inventory/stock/infrastructure/service/StockServiceTest.java:L102-L228` - movement coverage

**Validation criteria:**

- `mvn test -Dtest="StockServiceTest,StockMovementReversalServiceTest,StockConfigTest"` passes.
- Reversal stock movements point to the same physical document reference (`ReferenceType.GOODS_ISSUE` for GI), with reversal identity represented by `reversalOfMovementId`.
- One original movement cannot be reversed twice, including under retry.

### Task 4: Valuation Layer Reversal With Historical Issue Cost

Ensure stock reversal creates a new inbound valuation layer using the historical issue cost from the original movement.

**Depends on:** Task 3
**Reference modules:** `inventory.stock`

- [ ] Extend `ValuationLayer` and `ValuationLayerEntity` with nullable `reversalOfMovementId`.
      ref: `src/main/java/com/solusi/erp/inventory/stock/domain/model/ValuationLayer.java:L20-L57` - current valuation reference fields and factory
      ref: `src/main/java/com/solusi/erp/inventory/stock/infrastructure/persistence/ValuationLayerEntity.java:L18-L55` - current valuation entity fields
- [ ] Extend `FifoValuationService.addLayer(...)` to accept optional `reversalOfMovementId`, preserving existing overloads for normal receipt paths.
      ref: `src/main/java/com/solusi/erp/inventory/stock/domain/service/FifoValuationService.java:L24-L41` - current add layer overloads
- [ ] Modify `StockServiceImpl.handleValuation(...)` so positive reversal movements create inbound layers linked to `reversalOfMovementId`.
      ref: `src/main/java/com/solusi/erp/inventory/stock/infrastructure/service/StockServiceImpl.java:L75-L112` - current positive and negative valuation branches
- [ ] For reversal receipt cost, use original movement `unitCost.localAmount` and currency metadata. Do not recalculate from current master price or current exchange rate.
      ref: `src/main/java/com/solusi/erp/inventory/stock/infrastructure/persistence/InventoryMovementEntity.java:L52-L63` - stored unit cost snapshot on movement
- [ ] Add repository lookup for valuation layers by `reversalOfMovementId` if needed for audit checks and tests.
      ref: `src/main/java/com/solusi/erp/inventory/stock/domain/repository/ValuationLayerRepository.java:L1-L42` - valuation repository port shape
- [ ] **TEST:** Extend `FifoValuationServiceTest` for add reversal layer and confirm it is a new layer, not mutation of the consumed GR layer.
      ref: `src/test/java/com/solusi/erp/inventory/stock/domain/FifoValuationServiceTest.java` - FIFO valuation domain tests
- [ ] **TEST:** Extend `StockServiceTest` for reversal receipt creating a layer with `referenceType=GOODS_ISSUE`, `referenceId=gi.id`, and `reversalOfMovementId=originalMovement.id`.
      ref: `src/main/java/com/solusi/erp/inventory/stock/infrastructure/service/StockServiceImpl.java:L75-L112` - valuation behavior under test

**Validation criteria:**

- `mvn test -Dtest="FifoValuationServiceTest,StockServiceTest"` passes.
- Original GR valuation layer remaining quantity is not silently increased.
- Reversal layer can be consumed by later FIFO outbound like a normal inbound layer.

### Task 5: Refactor Goods Issue Cancellation Use Case

Replace current GI cancellation with full linked stock and journal reversal, plus source-based guard.

**Depends on:** Tasks 2, 3, and 4
**Reference modules:** `inventory.goodsissue`, `accounting.journal`, `inventory.stock`

- [ ] Create `GoodsIssueCancelCommand` with `goodsIssueId`, `reversalDate`, `reason`, and line-level target container overrides.
      ref: `src/main/java/com/solusi/erp/inventory/goodsissue/application/usecase/command/CancelGoodsIssueUseCase.java:L1-L4` - current command is too small for target location flow
- [ ] Create line command keyed by GI line id or original movement id. Include target container id and enough display-safe metadata for validation.
- [ ] Extend `GoodsIssue` domain with cancellation metadata if accepted for audit: `cancelledDate` and `cancelReason`. Keep status `CANCELLED`.
      ref: `src/main/java/com/solusi/erp/inventory/goodsissue/domain/model/GoodsIssue.java:L70-L90` - current cancel transition
- [ ] Refactor `CancelGoodsIssueUseCaseImpl` to:
      load `COMPLETED` GI;
      require `reversalDate`;
      ensure reversal period open;
      reject source-based GI (`referenceType != MANUAL`) from direct GI cancellation;
      run `GoodsIssueInUseChecker`;
      collect original stock movements for `ReferenceType.GOODS_ISSUE` and `referenceId = gi.id`;
      call stock reversal service with full target locations;
      find original posted GI journal;
      call `ReversePostedJournalUseCase`;
      set GI `CANCELLED`;
      save atomically.
      ref: `src/main/java/com/solusi/erp/inventory/goodsissue/application/usecase/command/CancelGoodsIssueUseCaseImpl.java:L35-L58` - current cancel sequence
      ref: `src/main/java/com/solusi/erp/inventory/goodsissue/application/usecase/command/CancelGoodsIssueUseCaseImpl.java:L62-L80` - current ad-hoc receipt payload
- [ ] Remove the negative amount journal path from GI cancellation.
      ref: `src/main/java/com/solusi/erp/inventory/goodsissue/application/usecase/command/CancelGoodsIssueUseCaseImpl.java:L50-L55` - current negative inventory total posting
- [ ] Keep `CompleteGoodsIssueUseCaseImpl` unchanged except where necessary for journal lookup/source id compatibility.
      ref: `src/main/java/com/solusi/erp/inventory/goodsissue/application/usecase/command/CompleteGoodsIssueUseCaseImpl.java:L203-L226` - current GI journal command builder
- [ ] Wire the new dependencies in `GoodsIssueConfig`.
      ref: `src/main/java/com/solusi/erp/inventory/goodsissue/infrastructure/config/GoodsIssueConfig.java:L97-L110` - current cancel use case bean
- [ ] **TEST:** Rewrite `CancelGoodsIssueUseCaseTest` for linked stock reversal service invocation, linked journal reversal invocation, `reversalDate` period guard, source-based direct cancel rejection, in-use guard, already reversed movement, and rollback on journal reversal failure.
      ref: `src/test/java/com/solusi/erp/inventory/goodsissue/application/usecase/command/CancelGoodsIssueUseCaseTest.java:L33-L84` - current negative posting assertions to replace
- [ ] **TEST:** Add `GoodsIssueConfigTest` if still absent, proving the refactored cancel use case wires with `ReversePostedJournalUseCase` and stock reversal service.
      ref: `src/main/java/com/solusi/erp/inventory/goodsissue/infrastructure/config/GoodsIssueConfig.java:L73-L110` - current complete/cancel bean area
- [ ] **TEST:** Extend `GoodsIssueTest` for cancellation metadata and illegal cancel from non-completed statuses.
      ref: `src/test/java/com/solusi/erp/inventory/goodsissue/domain/model/GoodsIssueTest.java` - aggregate tests

**Validation criteria:**

- `mvn test -Dtest="GoodsIssueTest,CancelGoodsIssueUseCaseTest,CompleteGoodsIssueUseCaseTest,GoodsIssueConfigTest"` passes.
- Cancelling GI no longer calls `PostJournalForEventUseCase` with negative values.
- Direct cancellation of `PURCHASE_RETURN` GI fails in backend even if the user has `GOODS-ISSUE_CANCEL`.

### Task 6: Goods Issue Cancellation Web Flow

Replace direct cancel button for eligible GI with a dedicated cancellation form that captures reversal date and target location per line.

**Depends on:** Task 5
**Reference modules:** `inventory.goodsissue`, UI specs

- [ ] Add `GoodsIssueCancelRequest` and line request DTOs. Add `@DateTimeFormat(pattern = "yyyy-MM-dd")` on `reversalDate` and validation for required reason and target containers.
      ref: `docs/spec/datetime-standards.md:L73-L110` - backend date DTO contract
      ref: `src/main/java/com/solusi/erp/inventory/goodsissue/web/dto/GoodsIssueSaveRequest.java:L12-L40` - existing GI date DTO pattern
- [ ] Add `GET /inventory/goods-issues/{id}/cancel` to render the form only when GI is `COMPLETED` and direct cancel is allowed for the source type.
      ref: `src/main/java/com/solusi/erp/inventory/goodsissue/web/controller/GoodsIssueController.java:L124-L135` - current GI view route
- [ ] Change `POST /inventory/goods-issues/{id}/cancel` to accept JSON `@RequestBody GoodsIssueCancelRequest`, not only `reason` request param.
      ref: `src/main/java/com/solusi/erp/inventory/goodsissue/web/controller/GoodsIssueController.java:L197-L206` - current cancel endpoint contract
      ref: `docs/spec/form-submission.md:L116-L137` - AJAX form controller contract
- [ ] Hide cancel action for source-based GI in `view.html` and `form.html`. Show source-specific warning or no action instead.
      ref: `src/main/resources/templates/inventory/goods-issues/view.html:L20-L28` - current unconditional completed cancel button
      ref: `src/main/resources/templates/inventory/goods-issues/form.html:L28-L41` - current completed cancel button on form
- [ ] Create `templates/inventory/goods-issues/cancel.html` using standard layout, `data-ajax-form="true"`, alert container, `reversalDate` date picker, required reason, and line table.
      ref: `docs/spec/form-submission.md:L16-L50` - AJAX form contract
      ref: `docs/spec/header-lines-form.md:L7-L15` - header-lines structure
- [ ] For each line, display original product, serial, issued quantity, historical container, and target container autocomplete with Trinity data. Default target container to historical container.
      ref: `docs/spec/autocomplete-generic.md:L34-L49` - autocomplete fragment and Trinity data requirement
      ref: `docs/brainstorming/2026-06-02-vendor-debit-memo.md` - Generic GI Cancellation Ownership target location rule
- [ ] Use query-level lookup filtering for target containers so only active containers in the same facility can be selected.
      ref: `docs/spec/modal-selector.md:L67-L76` - query-level filtering principle
- [ ] Create `static/js/inventory/goods-issue/goods-issue-cancel.js` for target container lookup initialization, capture-phase validation, and summary counts. Keep flow out of shared JS.
      ref: `docs/spec/page-specific-scripts.md:L5-L36` - page-specific JS boundary
      ref: `docs/spec/form-submission.md:L52-L67` - capture validation and beforeunload
- [ ] Action button on GI detail should navigate to cancel form instead of posting immediate cancel.
      Use icon/button styling consistent with Tabler and `sec:authorize`.
      ref: `docs/spec/ui-standards.md:L7-L24` - action button list styling
- [ ] **TEST:** Extend `GoodsIssueControllerTest` for GET cancel form, source-based cancel rejection, JSON POST mapping, permission annotations, and model attrs.
      ref: `src/test/java/com/solusi/erp/inventory/goodsissue/web/controller/GoodsIssueControllerTest.java` - controller test pattern
- [ ] **TEST:** Add `GoodsIssueCancelTemplateIntegrationTest` or extend existing GI template tests for AJAX attrs, date picker, reason field, target container autocomplete Trinity data, source-based cancel button hidden, and no forbidden theme classes.
      ref: `src/test/java/com/solusi/erp/inventory/goodsissue/web/template/integration/GoodsIssueViewIntegrationTest.java` - existing GI view template test location

**Validation criteria:**

- `mvn test -Dtest="GoodsIssueControllerTest,GoodsIssueViewIntegrationTest,GoodsIssueCancelTemplateIntegrationTest"` passes.
- Completed manual/generic GI exposes cancel form entry point.
- Completed source-based GI does not expose direct GI cancel action and backend rejects direct cancel.

### Task 7: Persistence, Query, And Navigation Support

Expose the data needed by cancellation form and journal/stock audit links without violating web-layer boundaries.

**Depends on:** Tasks 3, 5, and 6
**Reference modules:** `inventory.goodsissue`, `inventory.report`, `accounting.journal`

- [ ] Add query/read model for GI cancellation form containing GI header, line snapshots, original issue movements, default target containers, and whether direct cancel is allowed.
      ref: `src/main/java/com/solusi/erp/inventory/goodsissue/application/usecase/query/GetGoodsIssueUseCase.java` - existing detail query pattern
- [ ] Do not inject `InventoryMovementJpaRepository` or cross-slice JPA repositories into `GoodsIssueController` or web mapper. Put movement lookup in application/query or infrastructure adapter.
      ref: `docs/AGENTS.md:L156-L157` - web-layer dependency boundary
- [ ] Add journal lookup support if needed to find original GI journal by `sourceType/sourceId` before calling `ReversePostedJournalUseCase`.
      ref: `src/main/java/com/solusi/erp/accounting/journal/domain/repository/JournalEntryRepository.java:L7-L14` - existing source lookup and reversal methods
- [ ] Add detail links or badges on GI detail for original journal and reversal journal when available.
      ref: `src/main/resources/templates/inventory/goods-issues/view.html:L53-L60` - current journal entry link
- [ ] Add stock card/report visibility of reversal movements by keeping `referenceType/referenceId/referenceCode` as physical document source and adding `reversalOfMovementId` display if the report mapper supports it.
      ref: `src/main/java/com/solusi/erp/inventory/report/application/usecase/query/GetStockCardUseCaseImpl.java:L18-L33` - stock card query path
- [ ] **TEST:** Add query/use case tests for cancellation form read model and source-based direct-cancel eligibility.
      ref: `src/test/java/com/solusi/erp/inventory/goodsissue/application/usecase/query/GoodsIssueQueryUseCaseTest.java` - GI query test package
- [ ] **TEST:** Add mapper/template test for reversal journal link visibility if implemented.
      ref: `src/test/java/com/solusi/erp/inventory/goodsissue/web/mapper/GoodsIssueWebMapperTest.java` - GI web mapper tests

**Validation criteria:**

- `mvn test -Dtest="GoodsIssueQueryUseCaseTest,GoodsIssueWebMapperTest,GoodsIssueViewIntegrationTest"` passes.
- Web layer still injects use cases, lookup providers, and web helpers only.
- GI detail can distinguish original posting from linked reversal without relying on negative journal lines.

### Task 8: i18n And Documentation Update

Document the new reversal standard and update user-facing messages.

**Depends on:** Tasks 5 and 6
**Reference modules:** project docs, i18n bundles

- [ ] Add Indonesian and English messages for journal reversal, stock reversal, GI cancellation form labels, source-based cancel rejection, target container validation, already reversed movement, and reversal period guard.
      Use targeted replace edits; do not append with shell echo.
      ref: `docs/spec/i18n-guide.md` - i18n update protocol
- [ ] Update `docs/modules/inventory/goods-issue.md` with linked reversal cancellation behavior, direct source-based cancel restriction, reversal date rule, target location rule, and valuation reversal layer behavior.
      ref: `docs/modules/inventory/goods-issue.md:L1-L129` - current GI module docs
- [ ] Add or update accounting journal documentation to explain that auto-journal reversal uses linked reversal journal and does not call accounting schema with negative values.
      ref: `docs/modules/accounting/journal-entry.md` - journal module docs referenced by brainstorming
- [ ] Add a note that Purchase Return confirmed reversal remains deferred to Phase F even though generic primitives now exist.
      ref: `docs/brainstorming/2026-06-02-vendor-debit-memo.md` - Phase F boundary
- [ ] **TEST:** Add message bundle/static documentation tests if existing test style supports it.
      ref: `src/test/java/com/solusi/erp/accounting/journal/web/template/JournalMessageBundleTest.java` - message bundle test pattern

**Validation criteria:**

- `mvn test -Dtest="JournalMessageBundleTest,*GoodsIssue*Message*Test"` passes if message tests exist.
- Docs clearly say Phase A is a foundation only and does not implement Debit Memo.
- No UI text is hardcoded in new templates.

### Task 9: Playwright GI Cancellation Coverage

Prove the browser flow for manual/generic GI cancellation after backend and template tests pass.

**Depends on:** Tasks 6, 7, and 8
**Reference modules:** Playwright guide and GI templates/JS

- [ ] Read `docs/tests/playwright-pitfalls.md`, `GoodsIssueController @RequestMapping`, `view.html`, `cancel.html`, and `goods-issue-cancel.js` before writing the spec.
      ref: `docs/tests/playwright-pitfalls.md:L1-L293` - mandatory E2E pitfalls and runtime gate
      ref: `src/main/java/com/solusi/erp/inventory/goodsissue/web/controller/GoodsIssueController.java:L48-L206` - actual GI route
- [ ] Add deterministic H2 seed data if current seed does not contain a completed manual/generic GI with stock movement and posted journal ready for cancellation.
      ref: `src/main/resources/db/migration-h2/V9000__e2e_seed_data.sql` - E2E seed location
- [ ] Create `e2e-tests/tests/inventory/goods-issue-cancellation.spec.ts` with scenarios:
      completed manual/generic GI opens cancel form;
      default target containers are historical containers;
      target container can be changed within the same facility;
      reversal date required;
      confirm cancellation creates `CANCELLED` GI;
      source-based `PURCHASE_RETURN` GI has no direct cancel action.
- [ ] Use `page.request.get` for pre-navigation setup probes. Do not use `page.evaluate(fetch(...))` before `page.goto`.
      ref: `docs/tests/playwright-pitfalls.md:L25-L50` - about:blank fetch pitfall
- [ ] If TomSelect target container payload is needed by page JS, fetch LookupDto and inject full option. Do not use broken `selectTomSelect`.
      ref: `docs/tests/playwright-pitfalls.md:L55-L113` - TomSelect helper limitations
- [ ] Click `#confirm-modal-btn-yes` for ERP modal confirmations. Do not use `page.on('dialog')` for Bootstrap confirm.
      ref: `docs/tests/playwright-pitfalls.md:L115-L143` - modal confirm pitfall
- [ ] Run:
      `cd e2e-tests && npx tsc --noEmit`
      `npx playwright test tests/inventory/goods-issue-cancellation.spec.ts --list`
      `npx playwright test tests/inventory/goods-issue-cancellation.spec.ts`
- [ ] Run cold-cache transactional check:
      `cd e2e-tests && rm -rf .auth/ && npx playwright test tests/inventory/goods-issue-cancellation.spec.ts`
- [ ] On first runtime failure, retain screenshot/video and record diagnosis in `docs/reports/2026-06-02-phase-a-generic-reversal-foundation.md`.

**Validation criteria:**

- TypeScript compile passes.
- Playwright spec passes at least once normally and once after `.auth` removal.
- If the spec cannot be run in the same task, keep this task incomplete and record the skipped runtime gate in the report.

### Task 10: Regression Gate And Handoff

Run focused and final verification, then record the exact outcome.

**Depends on:** Tasks 1-9

- [ ] Run accounting focused tests:
      `mvn test -Dtest="JournalEntryTest,ReverseManualJournalUseCaseTest,ReversePostedJournalUseCaseTest,PostJournalForEventUseCaseTest,JournalConfigTest"`
- [ ] Run stock focused tests:
      `mvn test -Dtest="StockServiceTest,FifoValuationServiceTest,StockMovementReversalServiceTest,StockConfigTest"`
- [ ] Run GI focused tests:
      `mvn test -Dtest="GoodsIssueTest,CancelGoodsIssueUseCaseTest,CompleteGoodsIssueUseCaseTest,GoodsIssueControllerTest,*GoodsIssue*IntegrationTest"`
- [ ] Run migration-sensitive tests:
      `mvn test -Dtest="*MigrationTest"`
- [ ] Run full Maven gate:
      `mvn clean test`
- [ ] If Playwright task is in scope and implemented, run the normal and cold-cache browser gates from Task 9.
- [ ] Inspect JaCoCo if new code lowers coverage. Add focused tests in the owning task before marking complete.
      ref: `pom.xml:L285-L299` - active JaCoCo thresholds and non-halting check
- [ ] Update `docs/reports/2026-06-02-phase-a-generic-reversal-foundation.md` with commands, outcomes, skipped gates, deviations from plan, and remaining Phase B/F dependencies.
- [ ] Apply project SemVer protocol after implementation is accepted. Phase A is a feature foundation, so the likely bump is MINOR unless the implementation only changes docs/tests.
      ref: `docs/AGENTS.md:L101-L123` - SemVer automation and commit protocol

**Validation criteria:**

- Focused accounting, stock, and GI tests pass.
- `mvn clean test` passes.
- E2E gate passes if E2E was created.
- Report contains enough detail for Phase B planning without re-reading all command output.

## 6. Dependency Notes

- Task 1 must land before stock and valuation persistence changes.
- Task 2 must land before GI cancellation can reverse journals correctly.
- Task 3 and Task 4 should be implemented together because a reversal stock movement must create the matching valuation layer.
- Task 5 is the core integration point and should not start until journal and stock reversal tests are green.
- Task 6 depends on Task 5 because the request DTO and form fields must match the real cancellation command.
- Task 9 must remain incomplete if the actual Playwright run is not executed.

## 7. Deferred Items

- Phase B: Purchase Return accounting replacement with `SchemaEventType.PURCHASE_RETURN`.
- Phase D/E: Debit Memo and Debit Memo allocation.
- Phase F: Confirmed Purchase Return reversal orchestration using these primitives.
- Vendor Refund and refund-based Debit Memo settlement.
- Partial stock reversal, cross-facility reversal, and advanced tax override behavior.
