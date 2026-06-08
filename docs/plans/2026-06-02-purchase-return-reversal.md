# Implementation Plan: Confirmed Purchase Return Reversal

> Source: `docs/brainstorming/2026-06-02-vendor-debit-memo.md`
> Created: 2026-06-09
> Sprint: 6 - Debit Memo Reversal
> Status: IN_PROGRESS
>
> **For agentic workers:** execute task-by-task. Explore referenced files fresh before editing each task, update checkbox state incrementally, record deviations in `docs/reports/2026-06-02-purchase-return-reversal.md`, and do not mark a task complete before its validation command passes.

## Summary

Phase F adds explicit reversal for Purchase Return documents that already reached `CONFIRMED`. The reversal is full-only for MVP: it reverses generated Goods Issue stock movements with linked inbound movements, reverses the `PURCHASE_RETURN` journal through the generic linked journal reversal use case, cancels the generated Debit Memo only when all DMA consumption is inactive, and marks the Purchase Return as `REVERSED`.

This phase does not implement partial Purchase Return reversal, cross-facility reversal, Vendor Refund, or direct cancellation of source-owned Goods Issues from the GI module.

## Locked Decisions

- `CONFIRMED -> REVERSED` is distinct from pre-confirm `CANCELLED`.
- Full reversal only: every outbound movement created by the generated GI must be reversed.
- User selects target container per original outbound movement; default target is the historical issue container.
- Target container must be active and in the same facility as the original movement.
- Serialized stock list is immutable; all original issued serials must be returned and none may already be on-hand at the target.
- Purchase Return reversal is rejected while any related Debit Memo Allocation is still `CONFIRMED`.
- Purchase Return reversal is allowed only after DMA records are `CANCELLED` or `REVERSED` and Debit Memo remaining balance is full again.
- Generic GI cancellation remains manual-only. Purchase Return reversal must not loosen the source-owned GI guard.
- Reversal date must be in an `OPEN` accounting period.

## Scope Boundary

### Included

- Migration for Purchase Return status/reversal audit fields, reversal line snapshots, and `PURCHASE-RETURN_REVERSE` permission.
- Domain transition `PurchaseReturnStatus.REVERSED`.
- Source-owned reversal orchestration use case for confirmed Purchase Return.
- Reversal-location view/form for confirmed Purchase Return.
- Web route, DTO, template, JavaScript, i18n, module docs, and focused Java/E2E tests.

### Deferred

- Partial Purchase Return reversal.
- Cross-facility reversal.
- Reversal document header separate from Purchase Return.
- Vendor Refund implementation.
- Legacy correction for local Phase 1 data.
- Direct auto-journal reversal from Journal Entry UI.

## Target File Map

### Database

- Create `src/main/resources/db/migration/V75__Add_Purchase_Return_Reversal.sql`
- Create `src/main/resources/db/migration-h2/V75__Add_Purchase_Return_Reversal.sql`
- Update `src/main/resources/db/migration-h2/V9000__e2e_seed_data.sql` only if the E2E role seed must receive `PURCHASE-RETURN_REVERSE`

### Purchase Return Domain And Persistence

- Modify `src/main/java/com/solusi/erp/purchasing/purchasereturn/domain/model/PurchaseReturnStatus.java`
- Modify `src/main/java/com/solusi/erp/purchasing/purchasereturn/domain/model/PurchaseReturn.java`
- Modify `src/main/java/com/solusi/erp/purchasing/purchasereturn/infrastructure/persistence/PurchaseReturnEntity.java`
- Modify `src/main/java/com/solusi/erp/purchasing/purchasereturn/infrastructure/persistence/PurchaseReturnJpaRepository.java`
- Modify `src/main/java/com/solusi/erp/purchasing/purchasereturn/infrastructure/adapter/PurchaseReturnRepositoryImpl.java`
- Add reversal line snapshot persistence under `purchasereturn/infrastructure/persistence` and repository adapter support as needed

### Application And Ports

- Create `ReverseConfirmedPurchaseReturnUseCase`
- Create `ReverseConfirmedPurchaseReturnUseCaseImpl`
- Create `PurchaseReturnReverseCommand` and line command records
- Create `GetPurchaseReturnReverseViewUseCase` and view records
- Add Purchase Return reversal ports/adapters for:
  - generated GI status update and outbound movement lookup
  - Debit Memo lookup/lock/cancel and active DMA guard
  - original `PURCHASE_RETURN` journal lookup

### Web

- Modify `PurchaseReturnController`
- Add `PurchaseReturnReverseRequest` and line request DTOs
- Add `templates/purchasing/purchase-returns/reverse.html`
- Add `static/js/purchasing/purchase-return/reverse.js`
- Modify `templates/purchasing/purchase-returns/view.html`
- Modify `templates/purchasing/purchase-returns/list.html`

### Docs And Tests

- Modify `docs/modules/procurement/purchase-return.md`
- Modify `docs/modules/inventory/goods-issue.md`
- Modify `docs/modules/accountspayable/debit-memo.md`
- Modify `src/main/resources/messages.properties`
- Modify `src/main/resources/messages_id.properties`
- Extend `e2e-tests/tests/procurement/purchase-return.spec.ts`

## Tasks

### Task 1: Database Contract, Permission, And Reversal Audit Shape

Add schema support for confirmed Purchase Return reversal and seed the public permission.

**Depends on:** Phase A, Phase D, Phase E

**Reference module:** `purchasing.purchasereturn`, `inventory.goodsissue`, `accountspayable.debitmemoallocation`

Steps:
- [x] Create MariaDB and H2 `V75__Add_Purchase_Return_Reversal.sql` migrations.
      ref: `src/main/resources/db/migration/V67__Add_Purchase_Return_Phase_1.sql:L124` - Purchase Return sequence/menu/permission seed baseline
      ref: `src/main/resources/db/migration/V74__Add_Debit_Memo_Allocation.sql:L85-L98` - permission insertion and admin grant pattern
- [x] Add Purchase Return reversal audit columns: `reversal_date`, `reversal_reason`, `reversed_by_user_id`, and `reversal_journal_entry_id`.
      ref: `src/main/java/com/solusi/erp/purchasing/purchasereturn/infrastructure/persistence/PurchaseReturnEntity.java:L41-L58` - current header persistence fields
      ref: `src/main/java/com/solusi/erp/inventory/goodsissue/infrastructure/persistence/GoodsIssueEntity.java` - GI cancellation metadata pattern from Phase A
- [x] Add `pur_purchase_return_reversal_lines` for immutable target-location snapshots keyed by Purchase Return and original movement id.
      Include at least `purchase_return_id`, `purchase_return_line_id`, `original_movement_id`, `target_container_id`, product/serial/quantity snapshots, and audit fields.
      ref: `docs/brainstorming/2026-06-02-vendor-debit-memo.md:L1043-L1096` - location, serial, and full reversal rules
- [x] Seed `PURCHASE-RETURN_REVERSE` under the same PermissionGroup used by Purchase Return and grant it to admin.
      ref: `docs/brainstorming/2026-06-02-vendor-debit-memo.md:L958-L979` - permission boundary for cancel vs reverse
- [x] Update H2 mirror migration with equivalent business columns/constraints and update E2E seed grants only if the non-admin E2E flow needs to click Reverse.
      ref: `docs/tests/playwright-e2e-guide.md:L45-L74` - H2 migration mirror must be version-aligned
- [x] **TEST:** Extend `PurchaseReturnMigrationTest` to assert V75 MariaDB/H2 contract parity, permission seed, admin grant, and reversal line table shape.
      ref: `src/test/java/com/solusi/erp/purchasing/purchasereturn/infrastructure/persistence/PurchaseReturnMigrationTest.java:L1-L248` - current migration contract test style

**Validation criteria:**
- `mvn test -Dtest="PurchaseReturnMigrationTest,*MigrationTest"` passes.
- MariaDB and H2 V75 files contain the same business fields and constraints.
- `PURCHASE-RETURN_REVERSE` exists and is linked to a PermissionGroup.

### Task 2: Purchase Return Domain, Persistence, And Query Contract

Teach Purchase Return that confirmed reversal is a final lifecycle state with immutable audit metadata.

**Depends on:** Task 1

**Reference module:** `purchasing.purchasereturn`

Steps:
- [ ] Add `REVERSED` to `PurchaseReturnStatus` and add `canReverse()` returning true only for `CONFIRMED`.
      ref: `src/main/java/com/solusi/erp/purchasing/purchasereturn/domain/model/PurchaseReturnStatus.java:L3-L42` - current lifecycle helper methods
- [ ] Add `PurchaseReturn.reverse(reversalDate, reversalReason, reversedByUserId, reversalJournalEntryId)` with guards for status, non-null date, non-blank reason, actor, and journal id.
      ref: `src/main/java/com/solusi/erp/purchasing/purchasereturn/domain/model/PurchaseReturn.java:L173-L191` - confirm transition records generated GI and status
- [ ] Ensure `cancelApproved()` still rejects `CONFIRMED` and `REVERSED`; do not repurpose existing cancel methods for confirmed reversal.
      ref: `src/main/java/com/solusi/erp/purchasing/purchasereturn/domain/model/PurchaseReturn.java:L159-L166` - pre-confirm cancel transition
- [ ] Map new audit fields through entity, persistence mapper, repository save/load, web summary/detail DTOs, and list/detail mapper.
      ref: `src/main/java/com/solusi/erp/purchasing/purchasereturn/web/dto/PurchaseReturnDetailResponse.java:L17-L39` - current detail DTO fields
      ref: `src/main/java/com/solusi/erp/purchasing/purchasereturn/web/mapper/PurchaseReturnWebMapper.java:L75-L124` - summary/detail mapping path
- [ ] Add repository locking support for reversal if not already present, e.g. `findByIdForUpdate`, so two reverse requests cannot both pass status checks.
      ref: `src/main/java/com/solusi/erp/purchasing/purchasereturn/infrastructure/persistence/PurchaseReturnJpaRepository.java:L10-L55` - current repository queries
- [ ] Persist and query reversal line snapshots without changing original Purchase Return lines.
      ref: `docs/brainstorming/2026-06-02-vendor-debit-memo.md:L1082-L1096` - full reversal line snapshot direction
- [ ] **TEST:** Extend `PurchaseReturnTest` for `CONFIRMED -> REVERSED`, required reversal date/reason/actor/journal, invalid statuses, and immutability of pre-confirm cancel.
      ref: `src/test/java/com/solusi/erp/purchasing/purchasereturn/domain/model/PurchaseReturnTest.java:L1-L232` - aggregate lifecycle test pattern
- [ ] **TEST:** Extend persistence mapper/repository tests for round-tripping `REVERSED` and reversal audit fields.
      ref: `src/test/java/com/solusi/erp/purchasing/purchasereturn/infrastructure/persistence/PurchaseReturnPersistenceMapperTest.java` - mapper persistence contract location

**Validation criteria:**
- `mvn test -Dtest="PurchaseReturnTest,PurchaseReturnPersistenceMapperTest,PurchaseReturnRepositoryImplTest"` passes.
- List/detail queries can load `REVERSED` rows without losing lines or audit metadata.
- Existing `CONFIRMED` behavior remains unchanged.

### Task 3: Source-Owned Reversal Ports And Reverse Use Case

Implement the atomic orchestration for reversing a confirmed Purchase Return.

**Depends on:** Task 2

**Reference module:** `inventory.goodsissue`, `accounting.journal`, `accountspayable.debitmemo`, `accountspayable.debitmemoallocation`

Steps:
- [ ] Create `PurchaseReturnReverseCommand` with `purchaseReturnId`, `reversalDate`, `reversalReason`, `reversedByUserId`, and line commands keyed by original movement id plus target container id.
      ref: `src/main/java/com/solusi/erp/inventory/goodsissue/application/usecase/command/GoodsIssueCancelCommand.java:L1-L15` - reversal date/reason/line command shape
- [ ] Create a Purchase Return reversal view/use-case that loads the generated GI outbound movements and default target containers from historical issue containers.
      This must not reuse `GetGoodsIssueCancelViewUseCaseImpl` directly because it rejects source-owned GI.
      ref: `src/main/java/com/solusi/erp/inventory/goodsissue/application/usecase/query/GetGoodsIssueCancelViewUseCaseImpl.java:L35-L62` - useful movement-to-line mapping plus source-owned guard to avoid
- [ ] Add a source-owned inventory/GI adapter port that can:
      validate the generated GI belongs to the Purchase Return;
      fetch outbound movements by `ReferenceType.GOODS_ISSUE` and generated GI id;
      call `StockMovementReversalService.reverse(...)`;
      mark the generated GI `CANCELLED` with reversal metadata.
      ref: `src/main/java/com/solusi/erp/inventory/goodsissue/application/usecase/command/CancelGoodsIssueUseCaseImpl.java:L56-L92` - manual GI reversal sequence that Phase F mirrors selectively
      ref: `src/main/java/com/solusi/erp/inventory/stock/infrastructure/service/StockMovementReversalServiceImpl.java:L23-L170` - target container, serial, facility, and linked movement guards
- [ ] Add a journal adapter/port to find the original `PURCHASE_RETURN` journal by source and call `ReversePostedJournalUseCase`.
      ref: `src/main/java/com/solusi/erp/inventory/goodsissue/application/usecase/query/GetGoodsIssueJournalLinksUseCaseImpl.java:L31-L44` - source-owned GI journal lookup uses `PURCHASE_RETURN` + Purchase Return id
      ref: `src/main/java/com/solusi/erp/accounting/journal/application/usecase/command/ReversePostedJournalUseCase.java:L1-L7` - generic linked reversal API
- [ ] Add a Debit Memo reversal guard port that resolves and locks the Debit Memo by Purchase Return id, rejects any active confirmed DMA consumption, verifies confirmed applied total is zero, and cancels the Debit Memo.
      ref: `src/main/java/com/solusi/erp/accountspayable/debitmemo/application/usecase/command/CancelDebitMemoUseCaseImpl.java:L13-L26` - existing cancel guard for active DMA consumption
      ref: `src/main/java/com/solusi/erp/accountspayable/debitmemoallocation/application/usecase/command/ReverseDebitMemoAllocationUseCaseImpl.java:L62-L85` - reversed DMA restores DM remaining and VB statuses
      ref: `docs/reports/2026-06-02-phase-e-debit-memo-allocation.md:L53-L57` - Phase E behavior: reversed/cancelled DMA no longer blocks cancel
- [ ] Implement `ReverseConfirmedPurchaseReturnUseCaseImpl` in one transaction:
      lock Purchase Return;
      validate `CONFIRMED`, generated GI id, date/reason/actor;
      lock Debit Memo and validate DMA guard/full remaining;
      ensure reversal accounting period is open;
      reverse stock movements using user target containers;
      reverse the original `PURCHASE_RETURN` journal;
      mark generated GI `CANCELLED`;
      cancel Debit Memo;
      save Purchase Return reversal line snapshots;
      mark Purchase Return `REVERSED`.
      ref: `src/main/java/com/solusi/erp/purchasing/purchasereturn/application/usecase/command/ConfirmPurchaseReturnUseCaseImpl.java:L54-L84` - confirm transaction order and side-effect sequencing
      ref: `docs/brainstorming/2026-06-02-vendor-debit-memo.md:L1000-L1036` - required confirmed Purchase Return reversal flow
- [ ] Ensure failure ordering prevents side effects after failed guards: no stock reversal or journal reversal before DMA/full-balance/period validation passes.
      ref: `src/test/java/com/solusi/erp/purchasing/purchasereturn/application/usecase/command/ConfirmPurchaseReturnUseCaseTest.java:L98-L111` - guard-before-side-effect assertion pattern
- [ ] Wire the new use case and ports in `PurchaseReturnConfig` with `TransactionTemplate`, following existing command use cases.
      ref: `src/main/java/com/solusi/erp/purchasing/purchasereturn/infrastructure/config/PurchaseReturnConfig.java:L145-L166` - confirm use case wiring with transaction template
- [ ] **TEST:** Add `ReverseConfirmedPurchaseReturnUseCaseTest` for happy path, invalid status, missing GI, active confirmed DMA blocker, full remaining guard failure, closed period, missing original journal, already reversed stock movement, serial already on-hand, and rollback/no downstream calls on guard failure.
      ref: `src/test/java/com/solusi/erp/purchasing/purchasereturn/application/usecase/command/ConfirmPurchaseReturnUseCaseTest.java:L43-L194` - Mockito use case test pattern
- [ ] **TEST:** Extend `PurchaseReturnConfigTest` to prove reversal use case and view use case wire with the new dependencies.
      ref: `src/test/java/com/solusi/erp/purchasing/purchasereturn/infrastructure/config/PurchaseReturnConfigTest.java:L1-L183` - config integration test pattern

**Validation criteria:**
- `mvn test -Dtest="ReverseConfirmedPurchaseReturnUseCaseTest,PurchaseReturnConfigTest,StockMovementReversalServiceTest,ReversePostedJournalUseCaseTest,DebitMemoCommandUseCaseTest"` passes.
- Reversal never calls generic `CancelGoodsIssueUseCase` for a source-owned GI.
- Confirmed DMA blocks reversal until the DMA is reversed or cancelled.

### Task 4: Reversal Location Form, Controller Routes, And Page JavaScript

Expose a dedicated Purchase Return reversal form that captures reversal date, reason, and target container per movement.

**Depends on:** Task 3

**Reference module:** `inventory.goodsissue` cancellation form

Steps:
- [ ] Add `PurchaseReturnReverseRequest` and line request DTOs with `@DateTimeFormat(pattern = "yyyy-MM-dd")`, `@NotNull` reversal date, `@NotBlank` reason, and non-empty target lines.
      ref: `src/main/java/com/solusi/erp/inventory/goodsissue/web/dto/GoodsIssueCancelRequest.java:L14-L44` - date/reason/line DTO pattern
      ref: `docs/spec/datetime-standards.md:L73-L110` - backend date DTO contract
- [ ] Add `GET /purchasing/purchase-returns/{id}/reverse` to render the reversal form only for `CONFIRMED` Purchase Return with `PURCHASE-RETURN_REVERSE`.
      ref: `src/main/java/com/solusi/erp/purchasing/purchasereturn/web/controller/PurchaseReturnController.java:L231-L248` - existing status action routes
- [ ] Add `POST /purchasing/purchase-returns/{id}/reverse` accepting AJAX JSON `@RequestBody PurchaseReturnReverseRequest`, mapping to the use case command, and returning `ApiResponse`.
      ref: `docs/spec/form-submission.md:L116-L137` - AJAX JSON controller contract
- [ ] Build `templates/purchasing/purchase-returns/reverse.html` using `data-ajax-form="true"`, `data-redirect-on-success` back to `/purchasing/purchase-returns/view/{id}`, `data-picker="date"`, historical issue container display, and target container TomSelect lookups filtered by facility.
      ref: `src/main/resources/templates/inventory/goods-issues/cancel.html:L24-L169` - source template for reversal location form
      ref: `docs/spec/form-submission.md:L22-L69` - AJAX form attributes and alert container
- [ ] Add `static/js/purchasing/purchase-return/reverse.js` to initialize target container lookups with `initLookup()`, update line/target summary, and validate all target containers at submit capture phase.
      ref: `src/main/resources/static/js/inventory/goods-issue/goods-issue-cancel.js:L1-L54` - lookup init and capture-phase validation pattern
      ref: `docs/spec/form-submission.md:L54-L63` - custom validation must run before global AJAX handler
- [ ] Add a Reverse action to Purchase Return detail for `CONFIRMED` only, guarded by `PURCHASE-RETURN_REVERSE`, linking to the reversal form rather than using `ErpForm.postAction` because the action needs payload lines.
      ref: `src/main/resources/templates/purchasing/purchase-returns/view.html:L131-L163` - existing sidebar status action placement
      ref: `docs/spec/action-buttons.md:L1-L49` - status-changing actions require backend permission and clear UX
- [ ] Update list/detail status badge styling for `REVERSED` and avoid treating it as generic warning by falling through the existing final else.
      ref: `src/main/resources/templates/purchasing/purchase-returns/list.html:L70-L82` - current badge mapping
      ref: `src/main/resources/templates/purchasing/purchase-returns/view.html:L12-L22` - current detail badge mapping
- [ ] Show reversal metadata and reversal journal link on detail when present.
      ref: `src/main/resources/templates/purchasing/purchase-returns/view.html:L54-L87` - header/source link area
      ref: `src/main/java/com/solusi/erp/inventory/goodsissue/application/usecase/query/GetGoodsIssueJournalLinksUseCaseImpl.java:L45-L49` - reversal journal link lookup pattern
- [ ] **TEST:** Extend `PurchaseReturnControllerTest` for GET/POST reverse routes, `@PreAuthorize("hasAuthority('PURCHASE-RETURN_REVERSE')")`, model attributes, DTO mapping, and JSON success response.
      ref: `src/test/java/com/solusi/erp/purchasing/purchasereturn/web/controller/PurchaseReturnControllerTest.java:L55-L244` - controller unit test pattern
- [ ] **TEST:** Extend `PurchaseReturnViewIntegrationTest` and add a reverse template integration test for reverse button visibility, AJAX form attributes, `data-picker`, target container lookup attributes, status badge `REVERSED`, and security guards.
      ref: `src/test/java/com/solusi/erp/purchasing/purchasereturn/web/template/integration/PurchaseReturnViewIntegrationTest.java:L1-L39` - current template contract test location

**Validation criteria:**
- `mvn test -Dtest="PurchaseReturnControllerTest,PurchaseReturnViewIntegrationTest,PurchaseReturnReverseTemplateIntegrationTest,PurchaseReturnWebMapperTest"` passes.
- The reverse form has stable IDs for Playwright: `#purchase-return-reverse-form`, `#reversal-date`, `#reversal-reason`, and `#purchase-return-reverse-lines`.
- Source-owned GI still does not show direct cancel from GI detail.

### Task 5: i18n, Documentation, And Stale Deferral Cleanup

Update user-facing text and module documentation so Phase F behavior is documented as shipped.

**Depends on:** Task 4

**Reference module:** `docs/modules/procurement/purchase-return.md`

Steps:
- [ ] Add bilingual message keys for `REVERSED` status, reverse action labels, form headings, target container validation, success message, and domain errors.
      ref: `src/main/resources/messages.properties` - English message bundle
      ref: `src/main/resources/messages_id.properties` - Indonesian message bundle
- [ ] Update `docs/modules/procurement/purchase-return.md` to describe `CONFIRMED -> REVERSED`, full-only behavior, target location per movement, DMA blocker, DM cancel, generated GI cancellation, and linked journal/stock reversal.
      ref: `docs/modules/procurement/purchase-return.md:L18-L72` - current lifecycle/accounting docs still mark reversal as next phase
- [ ] Update `docs/modules/inventory/goods-issue.md` to state Purchase Return source-owned GI is cancelled by Purchase Return reversal, not direct GI cancel, and stock reversal remains linked via `reversalOfMovementId`.
      ref: `docs/modules/inventory/goods-issue.md:L12-L20` - source-owned GI cancellation boundary
      ref: `docs/modules/inventory/goods-issue.md:L54-L63` - stock reversal/valuation behavior
- [ ] Update `docs/modules/accountspayable/debit-memo.md` to describe that Purchase Return reversal cancels an unapplied Debit Memo and is blocked by active confirmed DMA.
      ref: `docs/modules/accountspayable/debit-memo.md:L57-L64` - existing Debit Memo cancel guard
- [ ] Run stale-text scans for `Phase F`, `Confirmed Purchase Return reversal deferred`, and any message implying confirmed Purchase Return cannot be reversed.
      ref: `docs/modules/procurement/purchase-return.md:L66-L72` - known stale deferral section after implementation
- [ ] **TEST:** Extend `PurchaseReturnMessagesTest` or add message bundle coverage for new keys.
      ref: `src/test/java/com/solusi/erp/purchasing/purchasereturn/web/template/PurchaseReturnMessagesTest.java` - Purchase Return message test location

**Validation criteria:**
- `mvn test -Dtest="PurchaseReturnMessagesTest,*MessageBundleTest"` passes.
- Docs no longer list confirmed Purchase Return reversal as deferred.
- English and Indonesian bundles both include all new labels and errors.

### Task 6: Playwright E2E For Confirmed Purchase Return Reversal

Prove the browser flow and cross-module side effects work against the real Spring Boot E2E profile.

**Depends on:** Task 5

**Reference module:** existing Purchase Return and Debit Memo Allocation specs

Steps:
- [ ] Before editing the spec, re-read the target templates and JS: `purchase-returns/view.html`, `purchase-returns/reverse.html`, `purchase-return/reverse.js`, and generated GI/Debit Memo detail templates for status badge selectors.
      ref: `docs/tests/playwright-pitfalls.md:L214-L229` - authoring checklist and badge selector pitfalls
- [ ] Extend `e2e-tests/tests/procurement/purchase-return.spec.ts` with a focused scenario for confirmed Purchase Return reversal without DMA consumption.
      ref: `e2e-tests/tests/procurement/purchase-return.spec.ts:L129-L336` - current confirmed Purchase Return flow and generated DM assertions
- [ ] Scenario A: create and confirm a Purchase Return, open `/purchasing/purchase-returns/{id}/reverse`, keep historical containers selected, set reversal date/reason, submit AJAX form, and assert redirect to detail with status `REVERSED`.
      ref: `docs/tests/playwright-e2e-guide.md:L520-L536` - Flatpickr helper guidance
      ref: `docs/spec/form-submission.md:L22-L69` - AJAX form behavior
- [ ] Assert generated GI detail now shows `CANCELLED` and original GI outbound stock has a reversal journal/link visible where the UI exposes it.
      ref: `e2e-tests/tests/procurement/purchase-return.spec.ts:L268-L335` - current GI and journal assertions
- [ ] Assert generated Debit Memo detail now shows `CANCELLED` and its allocation history remains empty/inactive.
      ref: `e2e-tests/tests/procurement/purchase-return.spec.ts:L273-L308` - current generated Debit Memo detail assertions
- [ ] Assert the original `PURCHASE_RETURN` journal has a linked reversal journal and both original/reversal totals are balanced.
      ref: `e2e-tests/tests/procurement/purchase-return.spec.ts:L310-L331` - existing Purchase Return journal verification
- [ ] Add a blocker scenario if feasible within the same spec: create confirmed DMA for the generated DM, verify Purchase Return reverse fails, reverse the DMA, then verify Purchase Return reverse succeeds.
      If this is too broad for a stable Phase F spec, record the reason in the report and leave the combined DMA blocker flow for Phase G integration.
      ref: `docs/reports/2026-06-02-phase-e-debit-memo-allocation.md:L43-L57` - DMA confirm/reverse behavior to integrate
- [ ] Do not use `selectTomSelect`; use `setTomSelectValue` or payload-aware local option injection only when needed.
      ref: `docs/tests/playwright-pitfalls.md:L31-L46` - TomSelect helper limitations
- [ ] For modal confirms, click `#confirm-modal-btn-yes`; do not use `page.on('dialog')`.
      ref: `docs/tests/playwright-pitfalls.md:L59-L70` - Bootstrap modal confirm pattern
- [ ] Run the required E2E commands and keep failure screenshots/videos for diagnosis on first failure.
      ref: `docs/tests/playwright-e2e-guide.md:L85-L127` - one-shot runner and Playwright-only commands

**Validation criteria:**
- `cd e2e-tests && npx tsc --noEmit` clean.
- `cd e2e-tests && npx playwright test tests/procurement/purchase-return.spec.ts --list` shows the reversal scenario.
- `cd e2e-tests && npx playwright test tests/procurement/purchase-return.spec.ts -g "reverses confirmed purchase return"` passes.
- `cd e2e-tests && rm -rf .auth/ && npx playwright test tests/procurement/purchase-return.spec.ts -g "reverses confirmed purchase return"` passes.
- If the DMA blocker scenario is implemented, its `-g` targeted run also passes normally and cold-cache.

### Task 7: Regression Gate And Handoff

Close Phase F with focused backend gates, E2E targeted gates, and a handoff report.

**Depends on:** Task 6

**Reference module:** Phase A-E reports

Steps:
- [ ] Run focused Purchase Return domain/application/web tests.
      ref: `docs/reports/2026-06-02-phase-d-debit-memo-core.md:L176-L196` - Phase D final gate format
- [ ] Run focused reversal primitive regression tests for stock movement reversal and journal reversal.
      ref: `docs/reports/2026-06-02-phase-a-generic-reversal-foundation.md:L62-L65` - Phase A final primitive gates
- [ ] Run focused Debit Memo/DMA guard regression tests to ensure confirmed DMA still blocks DM cancel and reversed DMA does not.
      ref: `docs/reports/2026-06-02-phase-e-debit-memo-allocation.md:L53-L57` - Phase E cancel/reverse guard result
- [ ] Run all migration tests.
      ref: `src/test/java/com/solusi/erp/purchasing/purchasereturn/infrastructure/persistence/PurchaseReturnMigrationTest.java:L1-L248` - migration gate location
- [ ] Run `mvn clean test` and record total tests plus JaCoCo result in the report.
      ref: `docs/reports/2026-06-02-phase-e-debit-memo-allocation.md:L73-L77` - backend gate reporting style
- [ ] Run the targeted Playwright commands from Task 6 and record normal/cold-cache outcomes.
      ref: `docs/tests/playwright-pitfalls.md:L287-L293` - E2E completion checklist
- [ ] Update `docs/reports/2026-06-02-purchase-return-reversal.md` with decisions, deviations, skipped checks, and remaining Phase G integration notes.

**Validation criteria:**
- `mvn test -Dtest="PurchaseReturnTest,ReverseConfirmedPurchaseReturnUseCaseTest,PurchaseReturnControllerTest,PurchaseReturn*IntegrationTest,PurchaseReturnConfigTest"` passes.
- `mvn test -Dtest="StockMovementReversalServiceTest,ReversePostedJournalUseCaseTest,DebitMemoCommandUseCaseTest,ReverseDebitMemoAllocationUseCaseTest"` passes.
- `mvn test -Dtest="*MigrationTest"` passes.
- `mvn clean test` passes.
- Targeted Purchase Return Playwright normal and cold-cache runs pass.

## Final Completion Checklist

- [ ] V75 MariaDB and H2 migrations are in sync.
- [ ] `PURCHASE-RETURN_REVERSE` is seeded and granted to admin.
- [ ] Purchase Return status includes `REVERSED` and detail/list badges render it correctly.
- [ ] Confirmed Purchase Return reversal is rejected while any DMA is still `CONFIRMED`.
- [ ] Reversed/cancelled DMA no longer blocks Purchase Return reversal once Debit Memo remaining is full.
- [ ] Source-owned GI is still not directly cancellable from the GI module.
- [ ] Stock reversal uses linked inbound movements and new valuation layers with historical cost.
- [ ] Purchase Return journal reversal uses `ReversePostedJournalUseCase`, not negative schema posting.
- [ ] Generated Debit Memo is cancelled during successful reversal.
- [ ] Playwright targeted normal and cold-cache runs validate the browser flow.
