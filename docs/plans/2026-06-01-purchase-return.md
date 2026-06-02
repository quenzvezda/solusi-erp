# Purchase Return Phase 1 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use `subagent-driven-development` (recommended) or `executing-plans` to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.
>
> Source: `docs/brainstorming/2026-06-01-purchase-return.md`
>
> Status: IN_PROGRESS

**Goal:** Implement Phase 1 Purchase Return end-to-end: GR-scoped return drafts, generic inventory reservation, approval lifecycle, automatic Goods Issue completion, SSR UI, and comprehensive automated verification.

**Architecture:** Add inventory reservation as a reusable inventory capability, then build `purchasing.purchasereturn` as a Clean Architecture + DDD + CQRS vertical slice. Purchase Return owns its workflow and delegates physical outbound posting to the existing Goods Issue seam. Debit Memo and the final `PURCHASE_RETURN` accounting event remain explicitly deferred to Phase 2.

**Tech Stack:** Java 21, Spring Boot 4.0.3, Spring Data JPA, MariaDB, Flyway, Thymeleaf SSR, HTMX, Bootstrap 5 / Tabler, TomSelect, AutoNumeric, Flatpickr, JUnit 5, Mockito, JaCoCo.

---

## Scope Boundary

### Included

- Generic inventory reservation foundation.
- Purchase Return Phase 1 domain, persistence, application, approval, GI integration, web, UI, permissions, i18n, tests, and docs.
- Temporary accounting path through existing generic `GOODS_ISSUE`.

### Deferred to Phase 2

- Debit Memo.
- Dedicated journal event `PURCHASE_RETURN`.
- Tax reversal, AP adjustment, and FX variance policy.
- Cancellation or reversal after Purchase Return reaches `CONFIRMED`.

## Implementation Notes

- Tests are co-located with implementation tasks. Do not postpone unit tests until the final gate.
- Use TDD inside each task: add failing tests, run the focused test set, implement the smallest coherent change, then run the focused test set again.
- Keep web-layer dependencies clean: controllers and web mappers may inject use cases, read-only lookup/query ports, and web helpers only. They must not inject JPA repositories or cross-slice entities.
- Do not add Phase 2 accounting behavior while implementing Phase 1. Preserve the explicit deferred boundary.
- `StockBalance` already contains `reservedQuantity`, and `MovementType` already contains `RESERVE`, `RELEASE`, and `ISSUE_RESERVED`. Extend those primitives; do not create a parallel balance mechanism.
- The JaCoCo guide currently documents stale thresholds. The active Maven configuration is authoritative: `LINE >= 0.80` and `BRANCH >= 0.80`.

## Target File Map

### Database

- Create: `src/main/resources/db/migration/V67__Add_Purchase_Return_Phase_1.sql`
- Create: `src/main/resources/db/migration-h2/V67__Add_Purchase_Return_Phase_1.sql`
- Modify: `src/main/resources/db/migration-h2/V9000__e2e_seed_data.sql`

### Generic Inventory Reservation

- Modify: `src/main/java/com/solusi/erp/inventory/stock/domain/model/StockBalance.java`
- Modify: `src/main/java/com/solusi/erp/inventory/stock/domain/model/ReferenceType.java`
- Modify: `src/main/java/com/solusi/erp/inventory/stock/application/dto/StockMovementPayload.java`
- Modify: `src/main/java/com/solusi/erp/inventory/stock/infrastructure/service/StockServiceImpl.java`
- Create: `src/main/java/com/solusi/erp/inventory/stock/domain/model/InventoryReservation.java`
- Create: `src/main/java/com/solusi/erp/inventory/stock/domain/model/InventoryReservationStatus.java`
- Create: `src/main/java/com/solusi/erp/inventory/stock/domain/model/ReservationOwnerType.java`
- Create: `src/main/java/com/solusi/erp/inventory/stock/domain/model/InventoryReservationRequest.java`
- Create: `src/main/java/com/solusi/erp/inventory/stock/domain/repository/InventoryReservationRepository.java`
- Create: `src/main/java/com/solusi/erp/inventory/stock/domain/port/InventoryReservationService.java`
- Create: `src/main/java/com/solusi/erp/inventory/stock/infrastructure/persistence/InventoryReservationEntity.java`
- Create: `src/main/java/com/solusi/erp/inventory/stock/infrastructure/persistence/InventoryReservationJpaRepository.java`
- Create: `src/main/java/com/solusi/erp/inventory/stock/infrastructure/persistence/InventoryReservationPersistenceMapper.java`
- Create: `src/main/java/com/solusi/erp/inventory/stock/infrastructure/adapter/InventoryReservationRepositoryImpl.java`
- Create: `src/main/java/com/solusi/erp/inventory/stock/infrastructure/service/InventoryReservationServiceImpl.java`
- Modify: `src/main/java/com/solusi/erp/inventory/stock/infrastructure/config/StockConfig.java`

### Purchase Return Slice

- Create package: `src/main/java/com/solusi/erp/purchasing/purchasereturn/`
- Create domain model: `domain/model/PurchaseReturn.java`, `PurchaseReturnLine.java`, `PurchaseReturnStatus.java`, `PurchaseReturnReason.java`
- Create domain ports: `domain/port/PurchaseReturnEventPublisher.java`, `PurchaseReturnSourceQueryPort.java`, `PurchaseReturnApprovalCancellationPort.java`
- Create domain repository: `domain/repository/PurchaseReturnRepository.java`
- Create persistence files: `infrastructure/persistence/PurchaseReturnEntity.java`, `PurchaseReturnLineEntity.java`, `PurchaseReturnJpaRepository.java`, `PurchaseReturnPersistenceMapper.java`
- Create adapters: `infrastructure/adapter/PurchaseReturnRepositoryImpl.java`, `PurchaseReturnSourceQueryAdapter.java`, `PurchaseReturnEventPublisherAdapter.java`, `PurchaseReturnApprovalCancellationAdapter.java`, `PurchaseReturnGoodsIssueSourceAdapter.java`
- Create listeners: `infrastructure/listener/OnPurchaseReturnApprovedListener.java`, `OnPurchaseReturnRejectedListener.java`
- Create config: `infrastructure/config/PurchaseReturnConfig.java`
- Create command contracts and implementations:
  `application/usecase/command/PurchaseReturnLineCommand.java`,
  `CreatePurchaseReturnUseCase.java`,
  `CreatePurchaseReturnUseCaseImpl.java`,
  `UpdatePurchaseReturnUseCase.java`,
  `UpdatePurchaseReturnUseCaseImpl.java`,
  `CancelDraftPurchaseReturnUseCase.java`,
  `CancelDraftPurchaseReturnUseCaseImpl.java`,
  `SubmitPurchaseReturnUseCase.java`,
  `SubmitPurchaseReturnUseCaseImpl.java`,
  `CancelPurchaseReturnSubmissionUseCase.java`,
  `CancelPurchaseReturnSubmissionUseCaseImpl.java`,
  `CancelApprovedPurchaseReturnUseCase.java`,
  `CancelApprovedPurchaseReturnUseCaseImpl.java`,
  `ConfirmPurchaseReturnUseCase.java`,
  `ConfirmPurchaseReturnUseCaseImpl.java`
- Create query records, contracts, and implementations:
  `application/usecase/query/EligibleGoodsReceiptRow.java`,
  `ReturnableGrLineSlice.java`,
  `ReturnableSerialRow.java`,
  `FindEligiblePurchaseReturnGoodsReceiptsUseCase.java`,
  `FindEligiblePurchaseReturnGoodsReceiptsUseCaseImpl.java`,
  `GetEligiblePurchaseReturnPurchaseOrderLookupUseCase.java`,
  `GetEligiblePurchaseReturnPurchaseOrderLookupUseCaseImpl.java`,
  `GetPurchaseReturnCreateViewUseCase.java`,
  `GetPurchaseReturnCreateViewUseCaseImpl.java`,
  `FindPurchaseReturnGrLineSlicesUseCase.java`,
  `FindPurchaseReturnGrLineSlicesUseCaseImpl.java`,
  `FindPurchaseReturnSerialsUseCase.java`,
  `FindPurchaseReturnSerialsUseCaseImpl.java`,
  `FindPurchaseReturnsUseCase.java`,
  `FindPurchaseReturnsUseCaseImpl.java`,
  `GetPurchaseReturnUseCase.java`,
  `GetPurchaseReturnUseCaseImpl.java`
- Create web files under: `web/controller/PurchaseReturnController.java`, `web/controller/PurchaseReturnLookupController.java`, `web/dto/`, `web/mapper/PurchaseReturnWebMapper.java`

### Approval Extension

- Modify: `src/main/java/com/solusi/erp/common/approval/domain/model/ApprovalAction.java`
- Modify: `src/main/java/com/solusi/erp/common/approval/domain/model/ApprovalRequest.java`
- Create: `src/main/java/com/solusi/erp/common/approval/application/usecase/CancelApprovalRequestUseCase.java`
- Create: `src/main/java/com/solusi/erp/common/approval/application/usecase/CancelApprovalRequestUseCaseImpl.java`
- Modify: `src/main/java/com/solusi/erp/common/approval/infrastructure/config/ApprovalConfig.java`

### Goods Issue Integration

- Create: `src/main/java/com/solusi/erp/inventory/goodsissue/infrastructure/adapter/PurchaseReturnGoodsIssueSourceResolver.java`
- Modify: `src/main/java/com/solusi/erp/inventory/goodsissue/application/usecase/command/CompleteGoodsIssueUseCaseImpl.java`
- Modify: `src/main/java/com/solusi/erp/inventory/goodsissue/infrastructure/config/GoodsIssueConfig.java`

### Thymeleaf, JavaScript, i18n, and Docs

- Create: `src/main/resources/templates/purchasing/purchase-returns/list.html`
- Create: `src/main/resources/templates/purchasing/purchase-returns/select-source.html`
- Create: `src/main/resources/templates/purchasing/purchase-returns/form.html`
- Create: `src/main/resources/templates/purchasing/purchase-returns/view.html`
- Create: `src/main/resources/templates/purchasing/purchase-returns/fragments/gr-line-selector-modal.html`
- Create: `src/main/resources/templates/purchasing/purchase-returns/fragments/serial-selector-modal.html`
- Create: `src/main/resources/static/js/purchasing/purchase-return/select-source.js`
- Create: `src/main/resources/static/js/purchasing/purchase-return/form.js`
- Modify: `src/main/resources/messages.properties`
- Modify: `src/main/resources/messages_id.properties`
- Create: `docs/modules/procurement/purchase-return.md`
- Modify: `docs/index.md`
- Modify: `docs/architecture/jacoco-coverage.md`

### E2E

- Create: `e2e-tests/tests/procurement/purchase-return.spec.ts`

## Tasks

### Task 1: Flyway Schema for Generic Reservation and Purchase Return [x]

Add MariaDB and H2 persistence schema before Java classes depend on it.

**Depends on:** none

**Reference modules:** Goods Issue migration, Goods Receipt migration, Purchase Order migration

- [x] Create `V67__Add_Purchase_Return_Phase_1.sql` in both migration folders with table `inv_stock_reservations`.
      Required columns: audit fields, optimistic `version`, `owner_ref_type`, `owner_ref_id`, `owner_ref_code`, `product_id`, `facility_id`, `grid_id`, `container_id`, nullable `serial_number`, `valuation_ref_type`, `valuation_ref_id`, `valuation_ref_line_id`, `quantity`, and `status`.
      Add indexes for owner lookup, active container quantity checks, valuation-reference availability, and serial lookup.
      ref: `src/main/resources/db/migration/V23__Inventory_Core_Valuation_And_Movements.sql:L3-L20` - existing stock balance schema and audit shape
      ref: `src/main/resources/db/migration/V65__Add_Valuation_Layer_Reference_Metadata.sql:L1-L8` - GR valuation reference metadata
- [x] Add `pur_purchase_returns` with `code`, `return_date`, canonical `reference_type/reference_id/reference_code`, PO snapshot id/code, supplier, facility, currency, exchange rate, status, required header `reason_code`, optional `note`, `submitted_by_user_id`, nullable `generated_gi_id`, and audit fields.
      Keep `reference_type='GOODS_RECEIPT'` in Phase 1 but retain generic columns for future return sources.
      ref: `src/main/resources/db/migration/V66__Add_Goods_Issue_Core.sql:L6-L37` - transaction header migration pattern
- [x] Add `pur_purchase_return_lines` with header FK, GR line id, product, serialized flag, quantity, UOM, base quantity, actual facility/grid/container, nullable serial CSV, required line `reason_code`, optional note, GR valuation references, and monetary snapshots (`unit_cost`, `inventory_amount`, `tax_reversal_amount`, `clearing_amount`).
      ref: `src/main/resources/db/migration/V66__Add_Goods_Issue_Core.sql:L42-L79` - outbound line snapshots and valuation references
- [x] Add a uniqueness guard so one Purchase Return can resolve at most one completed/generated GI. Add list/search indexes for code, return date, status, supplier, source GR, and source PO.
- [x] Register sequence `PURCHASE_RETURN` with format `PRT-{date:yyyyMM}-{seq}`.
      ref: `src/main/resources/db/migration/V66__Add_Goods_Issue_Core.sql:L82-L86` - transaction sequence registration
- [x] Add migration contract test `src/test/java/com/solusi/erp/purchasing/purchasereturn/infrastructure/persistence/PurchaseReturnMigrationTest.java` that reads both migration files and asserts required tables, source indexes, reservation ownership indexes, sequence registration, and H2 mirror presence.
      ref: `src/test/java/com/solusi/erp/inventory/goodsissue/infrastructure/persistence/GoodsIssueMigrationTest.java` - migration static contract pattern

**Validation criteria:**

- `mvn test -Dtest=PurchaseReturnMigrationTest` passes.
- MariaDB and H2 migration files contain equivalent business columns.

### Task 2: Generic Inventory Reservation Domain [x]

Model reservation ownership and enforce stock-balance invariants in pure Java.

**Depends on:** Task 1

**Reference modules:** `inventory.stock`

- [x] Extend `StockBalance.validate()` to reject `reservedQuantity > quantity`, not only negative totals. This closes the existing gap where `RESERVE`, normal `ISSUE`, or `TRANSFER_OUT` can leave negative available stock while on-hand remains non-negative.
      ref: `src/main/java/com/solusi/erp/inventory/stock/domain/model/StockBalance.java:L42-L83` - movement application and current invariants
- [x] Create enums `InventoryReservationStatus { ACTIVE, CONSUMED, RELEASED }` and `ReservationOwnerType { PURCHASE_RETURN }`.
- [x] Create immutable `InventoryReservationRequest` containing product, actual facility/grid/container, optional serial, valuation reference triple, and quantity.
- [x] Create aggregate/entity `InventoryReservation` with factories and transitions:
      `createActive(...)`, `release()`, and `consume()`.
      Reject non-positive quantities, repeated terminal transitions, serialized reservations without serial number, and serialized quantity other than `1`.
- [x] Create repository port `InventoryReservationRepository` with save/find operations by owner and active serial ownership.
- [x] Create service port `InventoryReservationService` with:
      `reserve(ownerType, ownerId, ownerCode, requests)`,
      `release(ownerType, ownerId)`,
      `assertActiveCoverage(ownerType, ownerId, requests)`,
      `consume(ownerType, ownerId)`.
- [x] Extend `StockBalanceDomainTest` for reserve exceeding available qty, normal issue touching reserved qty, transfer touching reserved qty, valid release, and valid `ISSUE_RESERVED`.
      ref: `src/test/java/com/solusi/erp/inventory/stock/domain/StockBalanceDomainTest.java:L32-L152` - existing reservation primitive tests
- [x] Add `InventoryReservationTest` covering status transitions, duplicate terminal transition failure, serialized quantity `1`, serialized missing serial failure, non-serial quantity greater than `1`, and required valuation reference.

**Validation criteria:**

- `mvn test -Dtest=StockBalanceDomainTest,InventoryReservationTest` passes.
- Normal outbound cannot reduce `availableQty` below zero.

### Task 3: Reservation Persistence and Inventory Availability Enforcement [x]

Persist ownership rows and connect reservation movements to stock balances atomically.

**Depends on:** Task 2

**Reference modules:** stock repository adapters, `StockServiceImpl`

- [x] Create reservation JPA entity, Spring Data repository, MapStruct mapper, and repository adapter.
      ref: `src/main/java/com/solusi/erp/inventory/stock/infrastructure/persistence/StockBalanceEntity.java:L15-L49` - inventory entity style
      ref: `src/main/java/com/solusi/erp/inventory/stock/infrastructure/adapter/ValuationLayerRepositoryImpl.java:L17-L78` - domain repository adapter style
- [x] Implement `InventoryReservationServiceImpl.reserve(...)`: reject an owner that already has active reservations; reject duplicate serial requests; call `StockService.adjust()` using `MovementType.RESERVE`; persist ACTIVE ownership rows only when every movement succeeds.
- [x] Implement `release(...)`: load ACTIVE rows, call `StockService.adjust()` with `MovementType.RELEASE`, then mark rows RELEASED.
- [x] Implement `assertActiveCoverage(...)`: compare owner, valuation reference, actual container, serial, and quantity. Reject missing, extra, or mismatched rows.
- [x] Implement `consume(...)`: mark matching ACTIVE rows CONSUMED after GI has consumed balances with `ISSUE_RESERVED`.
- [x] Extend `StockMovementPayload` only if reservation owner metadata is needed for audit diagnostics. Keep stock ledger `referenceType/referenceId/referenceCode` populated with the owning Purchase Return for RESERVE/RELEASE movements. (no payload extension needed)
      ref: `src/main/java/com/solusi/erp/inventory/stock/application/dto/StockMovementPayload.java:L22-L60` - existing movement payload
- [x] Add `ReferenceType.PURCHASE_RETURN` for RESERVE/RELEASE inventory-movement audit rows. Completed physical outbound movements generated through GI continue to use `ReferenceType.GOODS_ISSUE`.
      ref: `src/main/java/com/solusi/erp/inventory/stock/domain/model/ReferenceType.java:L11-L19` - existing inventory ledger reference types
- [x] Wire the reservation repository and service in `StockConfig`.
      ref: `src/main/java/com/solusi/erp/inventory/stock/infrastructure/config/StockConfig.java` - existing stock bean configuration
- [x] Extend `StockServiceTest`: reserve above available fails, normal ISSUE cannot consume reserved qty, TRANSFER_OUT cannot consume reserved qty, RELEASE above owner reserve fails, and ISSUE_RESERVED decrements both on-hand and reserved.
      ref: `src/test/java/com/solusi/erp/inventory/stock/infrastructure/service/StockServiceTest.java:L102-L228` - existing movement coverage
- [x] Add `InventoryReservationServiceTest` using Mockito for atomic reserve success, second reserve rejection, partial movement failure rollback expectation, serial duplicate rejection, release, active coverage mismatch, and consume.

**Validation criteria:**

- `mvn test -Dtest=StockBalanceDomainTest,StockServiceTest,InventoryReservationServiceTest` passes.
- Every outbound movement respects `availableQty`, except `ISSUE_RESERVED`, which consumes its owning hold.

### Task 4: Purchase Return Domain Aggregate [x]

Implement the Purchase Return aggregate independently of persistence and web concerns.

**Depends on:** Task 1

**Reference modules:** `purchasing.purchaseorder`, `inventory.goodsissue`

- [x] Create `PurchaseReturnStatus` with `DRAFT`, `SUBMITTED`, `APPROVED`, `REJECTED`, `CANCELLED`, and `CONFIRMED`, plus helpers for editability and allowed actions.
- [x] Create `PurchaseReturnReason` with `DAMAGED`, `WRONG_ITEM`, `QUALITY_ISSUE`, `OVER_RECEIPT`, `EXPIRED`, and `OTHER`. Keep stable codes in persistence; UI labels must use i18n keys.
- [x] Create immutable `PurchaseReturnLine`. Store one actual container per row. Permit repeated GR line ids only when rows represent different actual container slices or serialized selections.
- [x] Create `PurchaseReturn` with factory and transitions:
      `updateDraft(...)`,
      `submit(submitterUserId)`,
      `approve()`,
      `reject()`,
      `cancelDraft()`,
      `cancelSubmission(actorUserId)`,
      `cancelApproved()`,
      `confirm(generatedGiId)`.
- [x] Enforce invariants:
      one source GR per header;
      positive persisted line qty;
      header and line reason required;
      note required when reason is `OTHER`;
      serial CSV count equals base qty for serialized rows;
      serialized base qty is whole;
      no edit after submit;
      creator-only cancel submission;
      no cancel after confirmed in Phase 1.
      ref: `docs/brainstorming/2026-06-01-purchase-return.md` - locked lifecycle, serial, reason, and Phase 1 cancellation rules
- [x] Add `PurchaseReturnTest` and `PurchaseReturnLineTest` for every state transition and invariant branch.
      ref: `src/test/java/com/solusi/erp/purchasing/purchaseorder/domain/model/PurchaseOrderTest.java` - transaction aggregate test style
      ref: `src/test/java/com/solusi/erp/inventory/goodsissue/domain/model/GoodsIssueTest.java` - immutable outbound aggregate style

**Edge cases required:**

- Header `OTHER` without note.
- Line `OTHER` without note.
- Zero quantity row.
- Serialized qty `1.5`.
- Serialized qty `2` with one serial.
- Cancel Submission by a different user.
- Approve from DRAFT.
- Confirm from SUBMITTED.
- Cancel from CONFIRMED.

**Validation criteria:**

- `mvn test -Dtest=PurchaseReturnTest,PurchaseReturnLineTest` passes.

### Task 5: Purchase Return Persistence and Spring Wiring [x]

Add repository implementation and configuration while keeping the aggregate pure.

**Depends on:** Task 4

**Reference modules:** `purchasing.purchaseorder`, `accountspayable.vendorbill`

- [x] Create JPA header/line entities extending `BaseModel`, with enum string persistence and cascade/orphan-removal line mapping.
      ref: `src/main/java/com/solusi/erp/inventory/goodsissue/infrastructure/persistence/GoodsIssueEntity.java` - audited header entity pattern
      ref: `src/main/java/com/solusi/erp/inventory/goodsissue/infrastructure/persistence/GoodsIssueLineEntity.java` - audited line entity pattern
- [x] Create `PurchaseReturnPersistenceMapper` and ensure reconstitution preserves status, source snapshots, `submittedByUserId`, generated GI id, valuation references, reasons, notes, and audit metadata.
- [x] Create repository port and adapter with:
      `save`,
      `findById`,
      `findAll(keyword, status, pageable)`,
      `existsByCode`,
      `existsConfirmedOrOpenBySource`,
      `findByGeneratedGoodsIssueId`.
- [x] Create `PurchaseReturnConfig` and wire repository. Extend it with pure use cases and `TransactionTemplate` wrappers as those multi-write operations are added in Tasks 6-9.
      ref: `src/main/java/com/solusi/erp/inventory/goodsissue/infrastructure/config/GoodsIssueConfig.java:L45-L127` - bean and transaction wrapper style
- [x] Add `PurchaseReturnPersistenceMapperTest` for round-trip field coverage, line copies, enums, audit metadata, and generated GI link.
- [x] Add `PurchaseReturnConfigTest` with mocked external ports to prove Spring wiring.
      ref: `src/test/java/com/solusi/erp/accountspayable/vendorbill/infrastructure/config/VendorBillConfigTest.java` - focused config test pattern

**Validation criteria:**

- `mvn test -Dtest=PurchaseReturnPersistenceMapperTest,PurchaseReturnConfigTest` passes.

### Task 6: Returnable GR Query and Selector Read Models [x]

Implement read-side eligibility using valuation-layer remaining stock and active reservation ownership.

**Depends on:** Task 3, Task 5

**Reference modules:** Vendor Bill billable GR query, PO modal selector

- [x] Create read records:
      `EligibleGoodsReceiptRow`,
      `ReturnableGrLineSlice`,
      `ReturnableSerialRow`.
- [x] Create `PurchaseReturnSourceQueryPort` and JDBC adapter using `NamedParameterJdbcTemplate`.
      ref: `src/main/java/com/solusi/erp/accountspayable/vendorbill/infrastructure/adapter/BillableGrQueryAdapter.java:L18-L71` - source eligibility query pattern
- [x] Implement eligible GR header query:
      GR status must be `COMPLETED`;
      at least one original GR valuation layer has remaining quantity after active reservation;
      expose GR hyperlink id/code, PO hyperlink id/code, supplier, receipt date, facility, currency, eligible line count, and total returnable qty.
- [x] Implement non-serial slice query aggregated by `grLineId + actual containerId`, not only `grLineId`.
      This preserves valid stock when one GR line was transferred into multiple containers.
- [x] Implement serial query by GR context and actual current serial location. Include only on-hand, unreserved serials from the selected GR valuation origin. A serial moved after receipt remains eligible if the GR valuation reference is preserved.
- [x] Use a stable selection key:
      non-serial: `grLineId:containerId`;
      serialized: `grLineId:containerId:serialNumber`.
      Query-level exclusion receives selection keys, so selecting one container slice does not hide valid stock in another container.
      ref: `docs/spec/modal-selector.md:L67-L90` - exclusion must happen before render
- [x] Add query use cases:
      `FindEligiblePurchaseReturnGoodsReceiptsUseCase`,
      `GetEligiblePurchaseReturnPurchaseOrderLookupUseCase`,
      `GetPurchaseReturnCreateViewUseCase`,
      `FindPurchaseReturnGrLineSlicesUseCase`,
      `FindPurchaseReturnSerialsUseCase`.
- [x] Add `PurchaseReturnSourceQueryAdapterTest` using mocked JDBC interactions or H2 slice fixtures. Cover completed-only GR, exhausted layer exclusion, active reservation subtraction, non-serial multi-container split, serial moved-container eligibility, serial reservation exclusion, PO filter, supplier filter, and selection-key exclusion.
      ref: `src/test/java/com/solusi/erp/accountspayable/vendorbill/infrastructure/adapter/BillableGrQueryAdapterTest.java` - JDBC query adapter test style
- [x] Add query use-case tests for pagination mapping and empty result behavior.

**Validation criteria:**

- `mvn test -Dtest=PurchaseReturnSourceQueryAdapterTest,*PurchaseReturn*Selector*UseCaseTest,GetPurchaseReturnCreateViewUseCaseTest` passes.

### Task 7: Purchase Return Draft Application Use Cases [x]

Implement code generation, draft save/update, list, view, and draft cancellation.

**Depends on:** Task 5, Task 6

**Reference modules:** Goods Receipt create-from-source, Goods Issue draft commands

- [x] Create command records for header and line input. Keep UI-only labels out of domain commands.
- [x] Implement `CreatePurchaseReturnUseCaseImpl`:
      generate `PURCHASE_RETURN` code;
      resolve selected GR through query port;
      reject stale/ineligible GR;
      keep only positive qty rows;
      validate every line against current valuation-layer/container/serial availability;
      snapshot GR value and rate;
      save DRAFT.
      ref: `src/main/java/com/solusi/erp/inventory/goodsissue/application/usecase/command/CreateGoodsIssueUseCaseImpl.java:L29-L112` - sequence and positive-line filtering
- [x] Implement `UpdatePurchaseReturnUseCaseImpl` with the same stale-draft checks and DRAFT-only guard.
- [x] Implement `CancelDraftPurchaseReturnUseCaseImpl` as a status transition to `CANCELLED`, never hard delete.
- [x] Implement `FindPurchaseReturnsUseCaseImpl`, `GetPurchaseReturnUseCaseImpl`, and edit-view query.
- [x] Add Mockito tests:
      create happy path;
      selected GR no longer eligible;
      mixed GR line input;
      qty above slice available;
      serialized selection no longer available;
      zero rows filtered leaving no persisted lines;
      update non-DRAFT rejected;
      draft cancel;
      list pagination and keyword/status forwarding.
      ref: `src/test/java/com/solusi/erp/inventory/goodsissue/application/usecase/command/GoodsIssueDraftCommandUseCaseTest.java` - outbound draft use-case style

**Validation criteria:**

- `mvn test -Dtest=*PurchaseReturn*Draft*Test,CreatePurchaseReturnUseCaseTest,UpdatePurchaseReturnUseCaseTest,CancelDraftPurchaseReturnUseCaseTest,FindPurchaseReturnsUseCaseTest` passes.

### Task 8: Submit, Approval, Reject, and Cancel Submission Integration

Reserve inventory at submit and follow the existing generic approval flow.

**Depends on:** Task 3, Task 7

**Reference modules:** Purchase Order submit, Purchase Requisition reject listener, generic approval aggregate

- [x] Extend generic approval with `ApprovalAction.CANCELLED` and `ApprovalRequest.cancel(actorId, notes)`. Only PENDING requests can be cancelled; write a history entry.
      ref: `src/main/java/com/solusi/erp/common/approval/domain/model/ApprovalRequest.java:L40-L93` - transition and history pattern
      ref: `src/main/java/com/solusi/erp/common/approval/domain/model/ApprovalStatus.java:L6-L10` - `CANCELLED` status already exists
- [x] Add `CancelApprovalRequestUseCase` keyed by polymorphic reference type/id and wire it in `ApprovalConfig`.
- [x] Create `PurchaseReturnEventPublisher` and adapter that publishes `ApprovalRequestedEvent("PURCHASE_RETURN", ...)`.
      ref: `src/main/java/com/solusi/erp/purchasing/purchaseorder/application/usecase/command/SubmitPurchaseOrderUseCaseImpl.java:L20-L31` - submit event publishing
- [x] Implement `SubmitPurchaseReturnUseCaseImpl` transaction:
      load DRAFT;
      rebuild reservation requests from current lines;
      call `InventoryReservationService.reserve(...)`;
      transition to SUBMITTED with authenticated submitter user id;
      save;
      publish approval request with authenticated requester party id and selected approver id.
      If reserve fails, the transaction must leave the Purchase Return DRAFT and publish no approval request.
- [x] Create approved/rejected event listeners:
      approved transitions SUBMITTED to APPROVED and keeps reservation ACTIVE;
      rejected transitions SUBMITTED to REJECTED and releases reservation.
      ref: `src/main/java/com/solusi/erp/purchasing/purchaseorder/infrastructure/listener/OnPurchaseOrderApprovedListener.java:L18-L27` - approved listener
      ref: `src/main/java/com/solusi/erp/purchasing/purchaserequisition/infrastructure/listener/OnPurchaseRequisitionRejectedListener.java:L18-L27` - rejected listener
- [x] Implement `CancelPurchaseReturnSubmissionUseCaseImpl`:
      only SUBMITTED;
      actor user id must equal stored submitter user id;
      cancel generic approval request;
      release reservation;
      transition Purchase Return to CANCELLED.
- [x] Implement `CancelApprovedPurchaseReturnUseCaseImpl`: only APPROVED, release reservation, transition CANCELLED.
- [x] Add approval aggregate tests and Purchase Return Mockito tests for:
      reserve success;
      reserve failure atomicity;
      duplicate submit;
      approver id required;
      approved listener;
      rejected listener release;
      creator cancel submission;
      non-creator cancel rejection;
      approved cancel release;
      confirmed cancel rejection.

**Validation criteria:**

- `mvn test -Dtest=ApprovalRequestTest,CancelApprovalRequestUseCaseTest,SubmitPurchaseReturnUseCaseTest,OnPurchaseReturnApprovedListenerTest,OnPurchaseReturnRejectedListenerTest,CancelPurchaseReturnSubmissionUseCaseTest,CancelApprovedPurchaseReturnUseCaseTest` passes.

### Task 9: Purchase Return Confirm and Goods Issue Resolver

Confirm an approved Purchase Return by creating and completing one GI that consumes its reservation.

**Depends on:** Task 8

**Reference modules:** Goods Issue seam and completion flow

- [ ] Implement `PurchaseReturnGoodsIssueSourceAdapter` for the existing `PurchaseReturnGoodsIssueSourcePort`.
      Header snapshot supplies Purchase Return code, supplier, facility, original GR currency/rate, `billPosted`, and temporary clearing target metadata.
      Line snapshots supply actual container, serial CSV, original GR references, value snapshots, and valuation refs.
      ref: `src/main/java/com/solusi/erp/inventory/goodsissue/domain/port/PurchaseReturnGoodsIssueSourcePort.java:L7-L52` - existing contract
- [ ] Implement `PurchaseReturnGoodsIssueSourceResolver` in GI infrastructure. Resolve `GoodsIssueReferenceType.PURCHASE_RETURN` into a draft GI.
      ref: `src/main/java/com/solusi/erp/inventory/goodsissue/domain/port/GoodsIssueSourceResolver.java:L6-L10` - resolver contract
      ref: `src/main/java/com/solusi/erp/inventory/goodsissue/infrastructure/service/GoodsIssueSourceResolverRegistry.java:L15-L30` - registry wiring
- [ ] Modify GI completion so Purchase Return source lines use `MovementType.ISSUE_RESERVED`; generic/manual GI remains `MovementType.ISSUE`.
      Before posting Purchase Return GI movements, assert reservation coverage for the source owner. After successful movements and temporary generic `GOODS_ISSUE` journal posting, mark reservations CONSUMED.
      ref: `src/main/java/com/solusi/erp/inventory/goodsissue/application/usecase/command/CompleteGoodsIssueUseCaseImpl.java:L64-L79` - current stock issue loop
      ref: `src/main/java/com/solusi/erp/inventory/goodsissue/application/usecase/command/CompleteGoodsIssueUseCaseImpl.java:L157-L178` - current payload hardcodes `ISSUE`
- [ ] Wire resolver and reservation service into GI configuration.
      ref: `src/main/java/com/solusi/erp/inventory/goodsissue/infrastructure/config/GoodsIssueConfig.java:L73-L107` - completion and resolver registry beans
- [ ] Implement `ConfirmPurchaseReturnUseCaseImpl` transaction:
      require APPROVED;
      require OPEN period for `returnDate`;
      guard `hasCompletedGoodsIssue`;
      create GI from Purchase Return source;
      complete GI;
      persist generated GI id;
      transition Purchase Return CONFIRMED.
- [ ] Keep journal Phase 1 behavior explicit: GI posts generic `SchemaEventType.GOODS_ISSUE`. Do not add dedicated `PURCHASE_RETURN` journal event in this task.
- [ ] Add tests:
      approved return creates one completed GI;
      second confirm rejected idempotently;
      GI uses `ISSUE_RESERVED`;
      reservation mismatch rejects completion;
      OPEN period required;
      serial rows consume one movement per serial;
      non-serial same-GR multi-container rows consume their actual containers;
      original GR valuation reference forwarded;
      generic GI remains `ISSUE`;
      generated GI link persisted;
      journal failure rolls back owner consumption and Purchase Return confirmation.
      ref: `src/test/java/com/solusi/erp/inventory/goodsissue/application/usecase/command/CompleteGoodsIssueUseCaseTest.java` - GI completion Mockito test style

**Validation criteria:**

- `mvn test -Dtest=CompleteGoodsIssueUseCaseTest,PurchaseReturnGoodsIssueSourceAdapterTest,PurchaseReturnGoodsIssueSourceResolverTest,ConfirmPurchaseReturnUseCaseTest` passes.

### Task 10: Web DTOs, Mapper, Controller, and Lookup Boundary

Expose SSR and JSON endpoints without leaking repositories or entities into web.

**Depends on:** Task 7, Task 8, Task 9

**Reference modules:** Vendor Bill multi-step create, Purchase Order selectors, Goods Issue controller

- [ ] Create request/response DTOs:
      `PurchaseReturnSaveRequest`,
      `PurchaseReturnSaveLineRequest`,
      `PurchaseReturnSummaryResponse`,
      `PurchaseReturnDetailResponse`,
      `PurchaseReturnLineDetailResponse`.
      Request/response DTOs extend `BaseAuditResponse` where required by project convention.
- [ ] Add `@DateTimeFormat(pattern = "yyyy-MM-dd")` to `returnDate`.
      ref: `docs/spec/datetime-standards.md:L73-L110` - backend date contract
- [ ] Create `PurchaseReturnWebMapper`. Keep cross-slice label resolution in read ports/lookup providers, not JPA repository injection.
      ref: `docs/AGENTS.md` - web-layer injection boundary
      ref: `docs/spec/autocomplete-generic.md:L108-L171` - SSR Trinity-data boundary
- [ ] Create controller route `/purchasing/purchase-returns` with:
      list,
      `/select-source`,
      `/create-from-reference`,
      `/create`,
      `/edit/{id}`,
      `/view/{id}`,
      `/selectors/goods-receipt-lines`,
      `/selectors/serials`,
      `/{id}/submit`,
      `/{id}/cancel-submission`,
      `/{id}/confirm`,
      `/{id}/cancel`.
- [ ] Create `PurchaseReturnLookupController` endpoint `GET /api/lookup/purchasing/purchase-return-source-pos?q=...&limit=10`.
      Back it with `GetEligiblePurchaseReturnPurchaseOrderLookupUseCase`; return standard `LookupDto(id, name, subText, payload)` where PO name is the human-readable PO label and `subText` is the PO code. Do not add a repository dependency to the lookup controller.
      ref: `docs/spec/autocomplete-generic.md:L7-L25` - standard lookup DTO and endpoint shape
- [ ] Implement Pre-add 1 as a regular page:
      filter keyword, supplier, PO, receipt date range;
      return only eligible GR rows;
      provide GR and PO links.
      ref: `src/main/java/com/solusi/erp/accountspayable/vendorbill/web/controller/VendorBillController.java:L72-L124` - regular-page multi-step create
- [ ] Implement modal selector endpoints with Spring `Pageable`, model attrs for retained filters/exclusions, and selector fragment names.
      ref: `src/main/java/com/solusi/erp/purchasing/purchaseorder/web/controller/PurchaseOrderController.java:L108-L132` - modal selector endpoints
- [ ] Implement `/view/{id}` approval panel attrs using `FindApprovalRequestByReferenceUseCase("PURCHASE_RETURN", id)` and `SecurityUser`.
      ref: `src/main/java/com/solusi/erp/purchasing/purchaseorder/web/controller/PurchaseOrderController.java:L232-L260` - approval panel attrs
- [ ] Apply permissions:
      READ, CREATE, UPDATE, SUBMIT, CONFIRM, CANCEL.
      Status-specific guards remain in use cases even when buttons are hidden.
- [ ] Add controller tests for every route, `@PreAuthorize` values, view names, model attrs, paging retention, links, stale selector behavior, submit requester/approver extraction, and view approval attrs.
      ref: `src/test/java/com/solusi/erp/purchasing/purchaseorder/web/controller/PurchaseOrderControllerTest.java` - Mockito controller test style
- [ ] Add mapper tests for source snapshots, reasons, current container labels, serial CSV, outstanding qty, and generated GI link.

**Validation criteria:**

- `mvn test -Dtest=PurchaseReturnControllerTest,PurchaseReturnWebMapperTest` passes.
- No Purchase Return web class injects `JpaRepository` or cross-slice entity.

### Task 11: Thymeleaf Pages and Frontend Interaction Wiring

Build the two-step UX using mandatory project UI standards and browser-safe JS wiring.

**Depends on:** Task 10

**Reference modules:** Vendor Bill select page, Goods Receipt form, PO modal selectors, GI form

- [ ] Before editing UI files, re-read:
      `docs/spec/ui-standards.md`,
      `docs/spec/modal-selector.md`,
      `docs/spec/header-lines-form.md`,
      `docs/spec/form-submission.md`,
      `docs/spec/datetime-standards.md`,
      `docs/spec/numeric-standards.md`,
      `docs/spec/autocomplete-generic.md`,
      `docs/spec/action-buttons.md`,
      `docs/spec/page-specific-scripts.md`.
- [ ] Create `list.html` with standard right-aligned search bar, generic sorting fragment, generic pagination fragment, HTMX retention, and exactly one row action: `View`.
      ref: `docs/spec/ui-standards.md:L7-L24` - list-page layout
- [ ] Create `select-source.html` as a regular page, not a modal. Render filters and eligible GR table with hyperlinks to GR and PO view pages. Continue with one selected GR.
      ref: `src/main/resources/templates/accountspayable/vendor-bills/select-references.html:L7-L85` - regular pre-add page pattern
- [ ] Use autocomplete for simple Pre-add 1 filters:
      supplier through the existing party lookup contract;
      PO through `purchasing/purchase-return-source-pos`.
      Carry Trinity data (`id`, `name`, `subText`) and keep date range/keyword as standard compact inputs.
      ref: `docs/spec/autocomplete-generic.md:L30-L48` - autocomplete selection and Trinity data
- [ ] Create `form.html` with native Thymeleaf layout slot, AJAX form attributes, alert container, loading indicator, source header snapshots, `returnDate` date picker, reason enum selector rendered via i18n, optional note, summary card, dynamic line table, modal shells, and page script config.
      ref: `docs/spec/form-submission.md:L16-L50` - AJAX form contract
      ref: `docs/spec/header-lines-form.md:L7-L36` - header-lines structure and helpers
      ref: `docs/spec/datetime-standards.md:L21-L44` - `data-picker="date"`
- [ ] Prefill all eligible GR slices with qty `0`. Persist only positive rows. Permit row removal and keep Add Line available so removed rows can be restored from selector.
- [ ] For non-serial rows:
      show readonly outstanding returnable;
      show actual container slice;
      allow qty up to slice outstanding;
      allow multiple rows for the same GR line when containers differ.
- [ ] For serialized rows:
      use serial modal selector;
      show original GR context and actual current container;
      exclude already-selected serials at query level;
      group applied serial selections by actual container into separate Purchase Return rows;
      derive qty from selected serial count.
- [ ] Create `gr-line-selector-modal.html` and `serial-selector-modal.html`.
      Root `id`, `th:fragment`, modal shell `bodyId`, search `hx-target`, and pagination target must match exactly.
      ref: `docs/spec/modal-selector.md:L28-L47` - stable root contract
      ref: `src/main/resources/templates/purchasing/purchase-orders/fragments/pr-line-selector-modal.html:L4-L89` - multi-select payload fragment
- [ ] Put selector payload in `data-*` attrs only:
      stable selection key;
      GR line;
      product Trinity data;
      UOM Trinity data;
      actual facility/grid/container Trinity data;
      serialized flag;
      serial number where applicable;
      outstanding qty;
      valuation reference triple;
      unit cost and amount snapshots.
      ref: `docs/spec/modal-selector.md:L49-L65` - selector DTO and payload split
- [ ] Create `form.js` using `ErpLineManager`, `ErpNumeric.get/set`, `window.ERP.ModalSelector`, `ErpModal`, and capture-phase submit validation.
      Do not add Purchase Return flow into shared JS.
      ref: `docs/spec/page-specific-scripts.md:L5-L20` - feature JS boundary
      ref: `docs/spec/form-submission.md:L52-L67` - capture validation and beforeunload
- [ ] Implement JS guards:
      duplicate selection key prevention;
      qty above outstanding;
      required reason for positive rows;
      required note when reason OTHER;
      serial count derived from selected serials;
      reindex after remove;
      preserve hidden fields needed after SSR pre-edit without relying only on TomSelect payload.
      ref: `docs/spec/page-specific-scripts.md:L79-L90` - SSR TomSelect payload trap
- [ ] Create `view.html` with status badge, source hyperlinks, line table, reservation summary, GI link after confirmation, approval panel/history drawer, and status-aware actions.
- [ ] Status-changing actions use `ErpForm.postAction` and modal confirmation. Do not use `window.confirm()` or direct `new bootstrap.Modal()`.
      ref: `docs/spec/action-buttons.md:L5-L29` - document action contract
      ref: `docs/spec/ui-standards.md:L96-L115` - `ErpModal` rule
- [ ] Keep theme compatibility:
      no `bg-light`, `bg-white`, or `text-dark`;
      use `bg-body-tertiary`, `bg-secondary-lt`, `text-body`, and default readonly styling.
      ref: `docs/spec/ui-standards.md:L72-L94` - dark-mode requirements
- [ ] Add template contract tests:
      `PurchaseReturnListIntegrationTest`,
      `PurchaseReturnSelectSourceIntegrationTest`,
      `PurchaseReturnFormIntegrationTest`,
      `PurchaseReturnViewIntegrationTest`.
      Assert HTMX ids, modal root/fragment consistency, payload attrs, AJAX form attributes, date picker, numeric classes, i18n reason rendering, no forbidden theme classes, no raw enum labels, `View`-only list action, approval fragment, action-button attrs, and JS helper usage.
      ref: `src/test/java/com/solusi/erp/inventory/goodsreceipt/web/template/integration/GoodsReceiptFormIntegrationTest.java` - transaction form contracts

**Validation criteria:**

- `mvn test -Dtest=PurchaseReturnListIntegrationTest,PurchaseReturnSelectSourceIntegrationTest,PurchaseReturnFormIntegrationTest,PurchaseReturnViewIntegrationTest` passes.
- Search/pagination HTMX swaps do not produce `htmx:targetError`.

### Task 12: Permissions, Menu, i18n, and Documentation

Register the module and document the Phase 1 boundary.

**Depends on:** Task 10, Task 11

**Reference modules:** GI migration seeding, i18n guide, module docs

- [ ] Extend both `V67` migrations with PermissionGroup row for Purchase Return and permissions:
      `PURCHASE-RETURN_READ`,
      `PURCHASE-RETURN_CREATE`,
      `PURCHASE-RETURN_UPDATE`,
      `PURCHASE-RETURN_SUBMIT`,
      `PURCHASE-RETURN_CONFIRM`,
      `PURCHASE-RETURN_CANCEL`.
      Grant all to `ROLE_ADMIN`.
      ref: `src/main/resources/db/migration/V66__Add_Goods_Issue_Core.sql:L88-L117` - PermissionGroup and admin grants
      ref: `docs/AGENTS.md` - dash/underscore permission naming convention and wildcard safety
- [ ] Add Indonesian and English keys for labels, statuses, reason codes, selector empty states, confirmations, success messages, validation failures, reservation failures, and Phase 1 warnings.
      Use targeted replace edits; do not append with `echo`.
      ref: `docs/spec/i18n-guide.md` - i18n update protocol
- [ ] Add `docs/modules/procurement/purchase-return.md` describing actual implementation, reservation rules, UI flow, temporary generic GI journal, and mandatory Phase 2 migration to `PURCHASE_RETURN` event plus Debit Memo.
- [ ] Update `docs/index.md` procurement module list.
- [ ] Correct `docs/architecture/jacoco-coverage.md` thresholds to match active `pom.xml`: LINE 80%, BRANCH 80%, `haltOnFailure=false`.
      ref: `pom.xml:L285-L299` - active JaCoCo check configuration
- [ ] Add static message bundle test `PurchaseReturnMessagesTest` asserting every template and domain key exists in both bundles.

**Validation criteria:**

- `mvn test -Dtest=PurchaseReturnMessagesTest` passes.
- Permission folders remain grouped as `PURCHASE-RETURN`, and admin receives all Phase 1 actions.

### Task 13: Playwright Happy Path and Frontend Edge Cases

Prove real browser wiring after unit/controller/template tests are green.

**Depends on:** Task 12

**Reference modules:** Goods Receipt and Purchase Order Playwright specs

- [ ] Read `docs/tests/playwright-pitfalls.md`, the final Purchase Return templates, `form.js`, controller `@RequestMapping`, and helper implementations before writing selectors.
      ref: `docs/tests/playwright-pitfalls.md` - known runtime failures
      ref: `e2e-tests/helpers/tomselect.ts` - helper behavior; use payload-aware local helper when onchange needs payload
- [ ] Extend `src/main/resources/db/migration-h2/V9000__e2e_seed_data.sql` with deterministic eligible completed GR fixtures:
      non-serial GR layer available in two containers;
      one serialized item originating from selected GR but currently stored in a moved container;
      OPEN accounting period;
      configured generic `GOODS_ISSUE` schema required by temporary Phase 1 confirm.
- [ ] Create `e2e-tests/tests/procurement/purchase-return.spec.ts` with scenarios:
      Pre-add 1 filters and GR/PO links;
      create draft from one GR;
      remove and restore line through modal;
      non-serial same-GR multi-container return;
      serial selector uses current moved container;
      reason OTHER requires note;
      submit reserves qty;
      outbound availability excludes held qty;
      creator cancel submission releases qty;
      approval then confirm creates linked completed GI;
      list exposes `View` action only.
- [ ] Use HTTP setup probes through Playwright `request`, not `page.evaluate(fetch(...))` before navigation.
- [ ] On first runtime failure, retain screenshot/video and record diagnosis in `docs/reports/2026-06-01-purchase-return.md`.
- [ ] Run TypeScript compile and list:
      `cd e2e-tests && npx tsc --noEmit`
      `npx playwright test tests/procurement/purchase-return.spec.ts --list`
- [ ] Run focused browser spec:
      `cd e2e-tests && npx playwright test tests/procurement/purchase-return.spec.ts`
- [ ] Run cold-cache browser spec:
      `cd e2e-tests && Remove-Item -Recurse -Force .auth -ErrorAction SilentlyContinue`
      `npx playwright test tests/procurement/purchase-return.spec.ts`

**Validation criteria:**

- TypeScript compile passes.
- Playwright spec passes at least once normally and once after `.auth` removal.
- No new known-issue entry is required in `docs/tests/playwright-pitfalls.md`.

### Task 14: Regression Suite, Edge-Case Matrix, and Final Quality Gate

Finish only when the entire Maven suite and actual JaCoCo ratios satisfy the build target.

**Depends on:** Tasks 1-13

- [ ] Run focused backend regression:
      `mvn test -Dtest=StockBalanceDomainTest,StockServiceTest,InventoryReservationServiceTest,PurchaseReturnTest,PurchaseReturnLineTest,SubmitPurchaseReturnUseCaseTest,ConfirmPurchaseReturnUseCaseTest,PurchaseReturnControllerTest`
- [ ] Run focused frontend contracts:
      `mvn test -Dtest=PurchaseReturnListIntegrationTest,PurchaseReturnSelectSourceIntegrationTest,PurchaseReturnFormIntegrationTest,PurchaseReturnViewIntegrationTest,PurchaseReturnMessagesTest`
- [ ] Run related module regressions:
      `mvn test -Dtest=*GoodsIssue*,*GoodsReceipt*,*PurchaseOrder*,*Approval*`
- [ ] Run the required final gate:
      `mvn clean test`
- [ ] Inspect `target/site/jacoco/jacoco.xml` root counters and assert:
      `LINE covered / (covered + missed) >= 0.80`;
      `BRANCH covered / (covered + missed) >= 0.80`.
      Do not treat `BUILD SUCCESS` alone as sufficient because current `pom.xml` uses `<haltOnFailure>false</haltOnFailure>`.
      ref: `pom.xml:L285-L299` - active thresholds and non-halting check
- [ ] If either ratio is below `0.80`, use the JaCoCo HTML report to locate uncovered Purchase Return/reservation branches, add focused tests in the owning task's test class, rerun `mvn clean test`, and re-check ratios.
- [ ] Update `pom.xml` with the accepted MINOR SemVer bump after implementation and verification succeed, following project protocol for a new module.
- [ ] Record test commands, JaCoCo LINE/BRANCH ratios, Playwright result, version bump, and deferred Phase 2 boundary in `docs/reports/2026-06-01-purchase-return.md`.

**Final goal:**

- `mvn clean test` passes.
- JaCoCo LINE coverage is at least 80%.
- JaCoCo BRANCH coverage is at least 80%.
- Purchase Return Phase 1 browser happy path passes.
- Phase 2 Debit Memo and dedicated `PURCHASE_RETURN` accounting event remain documented and intentionally unimplemented.

## Dependency Notes

- Tasks 1-3 land first because Purchase Return submit cannot be correct without generic reservation ownership.
- Tasks 4-7 build the Purchase Return slice without approval or GI side effects, keeping failures local.
- Task 8 adds approval and hold lifecycle only after draft invariants are stable.
- Task 9 is the cross-slice integration point: PR confirm routes through GI and consumes its own reservation.
- Tasks 10-11 deliberately separate web/controller contracts from frontend browser behavior.
- Task 13 runs after UI and seeds exist. It must remain incomplete if Playwright was not executed.
- Task 14 is the only completion gate for the implementation plan.
