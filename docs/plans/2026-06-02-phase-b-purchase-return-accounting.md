# Implementation Plan: Purchase Return Accounting Replacement

> Source: `docs/brainstorming/2026-06-02-vendor-debit-memo.md`
> Created: 2026-06-04
> Sprint: 6 - Debit Memo Foundation
> Status: IN_PROGRESS
>
> **For agentic workers:** execute task-by-task. Explore references fresh before editing each task, update checkbox state incrementally, record deviations in `docs/reports/2026-06-02-phase-b-purchase-return-accounting.md`, and do not mark a task complete before its validation command passes.

## 1. Goal

Phase B replaces the temporary generic Goods Issue journal for Purchase Return confirmations with a source-specific `PURCHASE_RETURN` accounting event. Generic Goods Issue must continue posting `GOODS_ISSUE`, while Goods Issue generated from `referenceType=PURCHASE_RETURN` posts `DR GR/IR Clearing` and `CR Inventory` using historical Purchase Return/Goods Receipt inventory amount.

This phase is forward-only. It does not create Debit Memo, does not refactor Vendor Bill settlement, and does not implement confirmed Purchase Return reversal.

## 2. Locked Decisions

- Add `SchemaEventType.PURCHASE_RETURN`.
- Add journal variables `PR_GRIR_CLEARING_AMT` and `PR_INVENTORY_AMT`.
- Goods Issue stock movements remain physical-document movements with `ReferenceType.GOODS_ISSUE`.
- Purchase Return journal identity uses `sourceType="PURCHASE_RETURN"`, `sourceId=GoodsIssue.referenceId`, and `sourceCode=GoodsIssue.referenceCode` so retry idempotency and Phase F reversal can target the Purchase Return source.
- Generic/manual Goods Issue continues using `SchemaEventType.GOODS_ISSUE`, `sourceType="GOODS_ISSUE"`, and GI id/code.
- Purchase Return journal uses inventory DPP/historical inventory amount only: `PR_GRIR_CLEARING_AMT = PR_INVENTORY_AMT = sum(GoodsIssueLine.inventoryAmount)`.
- Input VAT reversal remains deferred to Debit Memo Allocation. Do not include `taxReversalAmount` or line `clearingAmount` in the Phase B Purchase Return journal.
- Dev/test data can be reset; no legacy corrective journal migration is needed.
- Actual schema line column name is `acc_schema_lines.variable`, not `var`.
- Actual dev seeder path is `docs/database/dev-seeder/D220__accounting_schema.sql`.
- Phase A's `ReversePostedJournalUseCase` creates linked reversal journals with original `eventType/sourceType` but null `sourceId/sourceCode`; Phase B only controls the original Purchase Return journal identity. Future Phase F reversal should use `reversalOfId` from Phase A, not negative schema posting.
- Phase A added GI detail journal links by direct journal id lookup. Because Phase B changes Purchase Return journal source identity from GI to Purchase Return, the GI journal link query must be taught to resolve source-owned journals.

## 3. Scope Boundary

### Included

- Accounting enum and journal variable contract changes.
- MariaDB and H2 migration for Purchase Return accounting schema seed.
- Dev seeder `D220` update for `PURCHASE_RETURN`.
- E2E seed `V9000` update so Purchase Return browser flow can post the new event in H2.
- Goods Issue completion routing for Purchase Return source.
- Focused unit/migration/template/message tests.
- Minimal docs update for Purchase Return, Goods Issue, and Accounting Schema behavior.
- E2E coverage that proves confirmed Purchase Return creates a `PURCHASE_RETURN` journal and no longer needs a `GOODS_ISSUE` schema line for that source.

### Deferred

- Debit Memo header/line creation. Phase D.
- Debit Memo Allocation and `DEBIT_MEMO_APPLICATION`. Phase E.
- Vendor Bill settlement refactor. Phase C.
- Confirmed Purchase Return reversal orchestration. Phase F.
- Tax reversal, AP reduction, FX gain/loss, and allocation proration. Phase E/F.
- Vendor Refund.

## 4. Target File Map

### Accounting Contract

- Modify `src/main/java/com/solusi/erp/accounting/schema/domain/model/SchemaEventType.java`
- Modify `src/main/java/com/solusi/erp/accounting/journal/domain/model/JournalVariable.java`
- Modify `src/main/resources/messages.properties`
- Modify `src/main/resources/messages_id.properties`

### Database And Seed

- Create `src/main/resources/db/migration/V71__Add_Purchase_Return_Accounting_Schema.sql`
- Create `src/main/resources/db/migration-h2/V71__Add_Purchase_Return_Accounting_Schema.sql`
- Modify `docs/database/dev-seeder/D220__accounting_schema.sql`
- Modify `src/main/resources/db/migration-h2/V9000__e2e_seed_data.sql`

### Posting Route

- Modify `src/main/java/com/solusi/erp/inventory/goodsissue/application/usecase/command/CompleteGoodsIssueUseCaseImpl.java`
- Modify `src/main/java/com/solusi/erp/inventory/goodsissue/application/usecase/query/GetGoodsIssueJournalLinksUseCaseImpl.java`

### Tests

- Modify `src/test/java/com/solusi/erp/accounting/journal/domain/model/JournalVariableTest.java`
- Modify `src/test/java/com/solusi/erp/accounting/journal/web/template/JournalMessageBundleTest.java`
- Modify `src/test/java/com/solusi/erp/inventory/goodsissue/application/usecase/command/CompleteGoodsIssueUseCaseTest.java`
- Modify `src/test/java/com/solusi/erp/inventory/goodsissue/application/usecase/query/GoodsIssueQueryUseCaseTest.java`
- Create or extend migration test under `src/test/java/com/solusi/erp/purchasing/purchasereturn/infrastructure/persistence/`
- Modify `e2e-tests/tests/procurement/purchase-return.spec.ts`

### Docs

- Modify `docs/modules/procurement/purchase-return.md`
- Modify `docs/modules/inventory/goods-issue.md`
- Modify `docs/modules/accounting/accounting-schema.md`

## 5. Tasks

### Task 1: Accounting Event And Variable Contract [x]

Add the Java enum contract for Purchase Return-specific posting.

**Depends on:** none
**Reference modules:** `accounting.schema`, `accounting.journal`

- [x] Add `PURCHASE_RETURN` to `SchemaEventType`.
      ref: `src/main/java/com/solusi/erp/accounting/schema/domain/model/SchemaEventType.java:L7-L15` - current auto-journal event enum list
- [x] Add `PR_GRIR_CLEARING_AMT(SchemaEventType.PURCHASE_RETURN)` and `PR_INVENTORY_AMT(SchemaEventType.PURCHASE_RETURN)` to `JournalVariable`.
      ref: `src/main/java/com/solusi/erp/accounting/journal/domain/model/JournalVariable.java:L7-L31` - existing event-owned variable pattern
- [x] Keep `GI_COGS_AMT` and `GI_INVENTORY_AMT` mapped to `GOODS_ISSUE`; do not repurpose generic variables for Purchase Return.
      ref: `src/main/java/com/solusi/erp/accounting/journal/domain/model/JournalVariable.java:L23-L24` - current generic GI variables
- [x] Add `label.schema.event.PURCHASE_RETURN` to English and Indonesian message bundles.
      ref: `src/main/resources/messages.properties:L1037-L1045` - event labels used by schema and journal templates
      ref: `src/main/resources/messages_id.properties:L1037-L1045` - Indonesian event labels
- [x] **TEST:** Extend `JournalVariableTest` to assert `getVariablesForEvent(PURCHASE_RETURN)` returns exactly the two PR variables.
      ref: `src/test/java/com/solusi/erp/accounting/journal/domain/model/JournalVariableTest.java:L10-L60` - event variable contract test
- [x] **TEST:** Keep `JournalMessageBundleTest` green after the new enum value by covering `label.schema.event.PURCHASE_RETURN`.
      ref: `src/test/java/com/solusi/erp/accounting/journal/web/template/JournalMessageBundleTest.java:L49-L75` - all `SchemaEventType` labels must exist

**Validation criteria:**

- `mvn test -Dtest="JournalVariableTest,JournalMessageBundleTest"` passes.
- Accounting Schema UI can render the new event type without missing i18n keys.
- No existing event loses its variables.

### Task 2: Purchase Return Accounting Schema Seeds [x]

Seed the new accounting schema header and lines for MariaDB, H2, dev seeder, and E2E.

**Depends on:** Task 1
**Reference modules:** `accounting.schema`, database migrations

- [x] Create `V71__Add_Purchase_Return_Accounting_Schema.sql` in MariaDB migrations. Follow the existing idempotent schema-seed pattern: find/create active `PURCHASE_RETURN` schema, delete its seeded lines, then insert two lines.
      ref: `src/main/resources/db/migration/V62__Vendor_Payment_Accounting_Schema.sql:L4-L28` - active schema upsert and line refresh pattern
- [x] Map `PR_GRIR_CLEARING_AMT` to COA code `2120` (`GR/IR Clearing`) as `DEBIT`.
      ref: `docs/database/dev-seeder/D210__accounting_coa.sql:L82-L91` - standard COA code references for inventory, AP, and GR/IR
- [x] Map `PR_INVENTORY_AMT` to COA code `1310` (`Merchandise Inventory`) as `CREDIT`.
      ref: `docs/brainstorming/2026-06-02-vendor-debit-memo.md` - Phase B conceptual mapping and no double-reduction example
- [x] Create the H2 mirror with the same version number. Split syntax where H2 needs it; do not use MariaDB-only constructs that have caused prior H2 migration failures.
      ref: `docs/tests/playwright-e2e-guide.md:L57-L74` - H2 migration mirror rules
      ref: `src/main/resources/db/migration-h2/V69__Add_Generic_Reversal_Foundation.sql:L1-L20` - H2-compatible migration style
- [x] Update `docs/database/dev-seeder/D220__accounting_schema.sql` to include `PURCHASE_RETURN` in header upsert, cleanup target list, schema id variable, line insert, and validation query expected counts.
      ref: `docs/database/dev-seeder/D220__accounting_schema.sql:L1-L97` - current dev seeder accounting schema refresh
- [x] Use `acc_schema_lines (schema_id, variable, account_id, position)` everywhere. Do not use `var`.
      ref: `src/main/resources/db/migration/V56__Refactor_Schema_To_Dynamic_Lines.sql:L4-L12` - actual schema line column name
- [x] Update `src/main/resources/db/migration-h2/V9000__e2e_seed_data.sql` to ensure H2 has an active `PURCHASE_RETURN` schema after E2E COA rows exist.
      Use E2E inventory account `9401` for `PR_INVENTORY_AMT`; use the existing E2E GR accrual/GRIR account used by Vendor Bill for `PR_GRIR_CLEARING_AMT`.
      ref: `src/main/resources/db/migration-h2/V9000__e2e_seed_data.sql:L365-L430` - E2E accounting COA/schema seed area
- [x] Preserve the existing `GOODS_ISSUE` schema in migration, dev seeder, and E2E seed for generic/manual GI.
      ref: `src/main/resources/db/migration-h2/V9000__e2e_seed_data.sql:L423-L430` - current E2E generic GI schema
- [x] **TEST:** Add/extend a migration contract test to migrate H2 and assert:
      `PURCHASE_RETURN` schema exists;
      it has exactly two lines;
      `PR_GRIR_CLEARING_AMT` is `DEBIT`;
      `PR_INVENTORY_AMT` is `CREDIT`;
      `GOODS_ISSUE` schema still exists with its two generic lines.
      ref: `src/test/java/com/solusi/erp/inventory/goodsissue/infrastructure/persistence/GoodsIssueMigrationTest.java:L14-L69` - H2 Flyway migration assertion pattern

**Validation criteria:**

- `mvn test -Dtest="*MigrationTest"` passes.
- Fresh H2 E2E migration can post Purchase Return journal without missing schema.
- Dev seeder validation query documents expected line counts including `PURCHASE_RETURN=2`.

### Task 3: Goods Issue Posting Route For Purchase Return [ ]

Route only Purchase Return-sourced Goods Issue to the new accounting event, while preserving generic Goods Issue behavior.

**Depends on:** Tasks 1 and 2
**Reference modules:** `inventory.goodsissue`, `purchasing.purchasereturn`, `accounting.journal`

- [ ] Refactor `CompleteGoodsIssueUseCaseImpl` so the journal command is selected by `GoodsIssue.referenceType`.
      `MANUAL`/generic sources continue calling the current `GOODS_ISSUE` journal builder.
      ref: `src/main/java/com/solusi/erp/inventory/goodsissue/application/usecase/command/CompleteGoodsIssueUseCaseImpl.java:L64-L75` - current complete flow around stock and journal side effects
      ref: `src/main/java/com/solusi/erp/inventory/goodsissue/application/usecase/command/CompleteGoodsIssueUseCaseImpl.java:L203-L219` - current generic `goodsIssueJournal(...)`
- [ ] Add a Purchase Return-specific journal builder that returns:
      `eventType=SchemaEventType.PURCHASE_RETURN`,
      `sourceType="PURCHASE_RETURN"`,
      `sourceId=issue.getReferenceId()`,
      `sourceCode=issue.getReferenceCode()`,
      `postingDate=issue.getIssueDate()`,
      `PR_GRIR_CLEARING_AMT=inventoryTotal`,
      `PR_INVENTORY_AMT=inventoryTotal`.
      ref: `src/main/java/com/solusi/erp/inventory/goodsissue/domain/model/GoodsIssueReferenceType.java:L1-L11` - source reference enum includes `PURCHASE_RETURN`
      ref: `src/main/java/com/solusi/erp/accounting/journal/application/usecase/command/JournalPostingCommand.java:L7-L30` - journal command identity fields
- [ ] Continue using `ReferenceType.GOODS_ISSUE` for stock movement payloads even when the source is Purchase Return.
      ref: `src/main/java/com/solusi/erp/inventory/goodsissue/application/usecase/command/CompleteGoodsIssueUseCaseImpl.java:L177-L190` - physical stock movement reference currently points to GI
- [ ] Keep `MovementType.ISSUE_RESERVED` and reservation coverage/consume behavior unchanged for Purchase Return.
      ref: `src/main/java/com/solusi/erp/inventory/goodsissue/application/usecase/command/CompleteGoodsIssueUseCaseImpl.java:L70-L75` - reservation coverage before stock movement
      ref: `src/main/java/com/solusi/erp/inventory/goodsissue/application/usecase/command/CompleteGoodsIssueUseCaseImpl.java:L191-L196` - consume reservation only after journal succeeds
- [ ] Do not include `taxAmount`, `taxReversalAmount`, or `clearingAmount` in the Purchase Return journal values.
      ref: `src/main/java/com/solusi/erp/inventory/goodsissue/infrastructure/adapter/PurchaseReturnGoodsIssueSourceResolver.java:L52-L65` - PR resolver currently carries tax/clearing snapshot but Phase B should not post tax
      ref: `docs/brainstorming/2026-06-02-vendor-debit-memo.md` - no double-reduction example: PR journal reverses inventory/GRIR only
- [ ] Ensure idempotency now protects by Purchase Return source. `PostJournalForEventUseCaseImpl` skips when `existsBySource(sourceType, sourceId)` is true.
      ref: `src/main/java/com/solusi/erp/accounting/journal/application/usecase/command/PostJournalForEventUseCaseImpl.java:L28-L33` - existing idempotency guard
- [ ] Update `GetGoodsIssueJournalLinksUseCaseImpl` so generic GI still resolves journals by `sourceType="GOODS_ISSUE", sourceId=goodsIssueId`, while Purchase Return GI resolves the original journal by `sourceType="PURCHASE_RETURN", sourceId=issue.referenceId`.
      This may require the query use case to receive enough GI reference metadata instead of only `goodsIssueId`; keep repository access inside application/query, not the web layer.
      ref: `src/main/java/com/solusi/erp/inventory/goodsissue/application/usecase/query/GetGoodsIssueJournalLinksUseCaseImpl.java:L7-L25` - Phase A direct journal-id link lookup currently assumes `GOODS_ISSUE` source identity
      ref: `docs/reports/2026-06-02-phase-a-generic-reversal-foundation.md:L46-L52` - Phase A decision that GI detail uses direct journal entry id links
- [ ] **TEST:** Extend `CompleteGoodsIssueUseCaseTest.complete_postsIssueStockPayloadWithSpecificValuationReferenceAndJournal` to prove generic GI still posts `GOODS_ISSUE` with `GI_COGS_AMT` and `GI_INVENTORY_AMT`.
      ref: `src/test/java/com/solusi/erp/inventory/goodsissue/application/usecase/command/CompleteGoodsIssueUseCaseTest.java:L69-L108` - current generic GI journal assertions
- [ ] **TEST:** Extend `CompleteGoodsIssueUseCaseTest.complete_purchaseReturn_usesReservedIssueAndConsumesReservationAfterJournal` to capture the journal command and assert event/source/variables are `PURCHASE_RETURN`, source id is PR id, and amounts are based on inventory total.
      ref: `src/test/java/com/solusi/erp/inventory/goodsissue/application/usecase/command/CompleteGoodsIssueUseCaseTest.java:L272-L289` - current PR reserved issue test
- [ ] **TEST:** Add a Purchase Return-specific journal failure test that verifies reservation is not consumed and Purchase Return source idempotency is preserved.
      ref: `src/test/java/com/solusi/erp/inventory/goodsissue/application/usecase/command/CompleteGoodsIssueUseCaseTest.java:L327-L340` - current journal failure rollback assertion
- [ ] **TEST:** Keep `ConfirmPurchaseReturnUseCaseTest` green; it should not need to know accounting internals because it delegates to `CompleteGoodsIssueUseCase`.
      ref: `src/test/java/com/solusi/erp/purchasing/purchasereturn/application/usecase/command/ConfirmPurchaseReturnUseCaseTest.java:L45-L69` - confirm orchestration delegates GI completion
- [ ] **TEST:** Extend `GoodsIssueQueryUseCaseTest` for journal links:
      generic GI returns original/reversal journal ids from `GOODS_ISSUE` source;
      Purchase Return GI returns original/reversal journal ids from `PURCHASE_RETURN` source;
      missing journal still returns empty links.
      ref: `src/test/java/com/solusi/erp/inventory/goodsissue/application/usecase/query/GoodsIssueQueryUseCaseTest.java:L231-L256` - current Phase A journal link assertions

**Validation criteria:**

- `mvn test -Dtest="CompleteGoodsIssueUseCaseTest,ConfirmPurchaseReturnUseCaseTest,GoodsIssueQueryUseCaseTest"` passes.
- Generic GI still posts `GOODS_ISSUE`.
- Purchase Return GI posts one `PURCHASE_RETURN` journal with equal debit/credit inventory amount.
- GI detail journal links still work for generic GI and for Purchase Return-generated GI.
- Reservation consumption still happens only after successful journal posting.

### Task 4: Accounting UI Labels And Source Visibility [ ]

Make the new event visible in existing Accounting Schema and Journal Entry screens without adding new UI flows.

**Depends on:** Tasks 1 and 3
**Reference modules:** `accounting.schema`, `accounting.journal`

- [ ] Verify Accounting Schema create/edit form renders `PURCHASE_RETURN` in the event type select through `SchemaEventType.values()`.
      ref: `src/main/resources/templates/accounting/schema/form.html:L31-L39` - event select uses `label.schema.event.{type}`
- [ ] Verify Journal Entry list filter and table can display `PURCHASE_RETURN`.
      ref: `src/main/java/com/solusi/erp/accounting/journal/web/controller/JournalEntryController.java:L181-L185` - event filter is built from `SchemaEventType.values()`
      ref: `src/main/resources/templates/accounting/journal/journal-entry-list.html:L26-L32` - filter label usage
      ref: `src/main/resources/templates/accounting/journal/journal-entry-list.html:L68-L71` - table event label usage
- [ ] Optionally add a Purchase Return source link in Journal Entry detail if the route is stable. If added, link `sourceType == 'PURCHASE_RETURN'` to `/purchasing/purchase-returns/view/{sourceId}`.
      ref: `src/main/resources/templates/accounting/journal/journal-entry-detail.html:L62-L77` - existing source links for GR and Vendor Bill
      ref: `src/main/java/com/solusi/erp/purchasing/purchasereturn/web/controller/PurchaseReturnController.java` - verify actual PR `@RequestMapping` before adding a link
- [ ] **TEST:** Extend schema/journal template tests only if link markup changes. At minimum, `SchemaFormIntegrationTest` and `JournalMessageBundleTest` must remain green after enum addition.
      ref: `src/test/java/com/solusi/erp/accounting/schema/web/template/integration/SchemaFormIntegrationTest.java:L63-L90` - schema form renders all event types
      ref: `src/test/java/com/solusi/erp/accounting/journal/web/template/JournalMessageBundleTest.java:L49-L75` - event label coverage

**Validation criteria:**

- `mvn test -Dtest="SchemaFormIntegrationTest,JournalMessageBundleTest,*Journal*Template*Test"` passes.
- No missing Thymeleaf message key for `PURCHASE_RETURN`.
- Journal screens can filter/display the new event type.

### Task 5: Documentation Update [ ]

Update module docs so Phase B behavior is the documented current behavior.

**Depends on:** Tasks 2 and 3
**Reference modules:** project docs

- [ ] Update `docs/modules/procurement/purchase-return.md`: replace Phase 1 placeholder statement with Phase B behavior, noting that confirmed Purchase Return posts `PURCHASE_RETURN` journal and Debit Memo remains deferred.
      ref: `docs/modules/procurement/purchase-return.md:L49-L64` - current Phase 1/Phase 2 boundary text
- [ ] Update `docs/modules/inventory/goods-issue.md`: clarify that core GI uses `GOODS_ISSUE` only for generic/manual outbound; source-specific GI can route accounting by source while stock movement still references physical GI.
      ref: `docs/modules/inventory/goods-issue.md:L88-L119` - current accounting and Purchase Return seam sections
- [ ] Update `docs/modules/accounting/accounting-schema.md`: add `PURCHASE_RETURN` and its variables to the event/variable documentation and standard mappings.
      ref: `docs/modules/accounting/accounting-schema.md:L184-L205` - current event list
      ref: `docs/modules/accounting/accounting-schema.md:L316-L319` - current standard mapping note for remaining events
- [ ] Mention that tax reversal is still deferred to Debit Memo Allocation and must not be posted in Purchase Return journal.
      ref: `docs/brainstorming/2026-06-02-vendor-debit-memo.md` - Accounting Boundary and Why There Is No Double Reduction sections

**Validation criteria:**

- Docs consistently say Purchase Return no longer uses generic `GOODS_ISSUE` journal.
- Docs still say Debit Memo, AP reduction, VAT reversal, and FX are deferred to later phases.
- No stale reference to `docs/db/dev-seeder`; use actual `docs/database/dev-seeder` when mentioning seeders.

### Task 6: Playwright Purchase Return Accounting Coverage [ ]

Extend E2E coverage to prove the browser Purchase Return confirmation now creates the source-specific journal.

**Depends on:** Tasks 2, 3, and 4
**Reference modules:** Playwright Purchase Return spec, Journal Entry routes

- [ ] Before editing the spec, re-read `docs/tests/playwright-pitfalls.md`, `PurchaseReturnController @RequestMapping`, the Purchase Return view template, and the current purchase-return Playwright spec.
      ref: `docs/tests/playwright-pitfalls.md:L1-L293` - mandatory E2E pitfalls and runtime gate
      ref: `e2e-tests/tests/procurement/purchase-return.spec.ts:L253-L266` - current confirm flow ends at GI detail
- [ ] Keep setup probes using `page.request.get`, not `page.evaluate(fetch(...))` before navigation.
      ref: `docs/tests/playwright-pitfalls.md:L25-L50` - about:blank fetch pitfall
      ref: `e2e-tests/tests/procurement/purchase-return.spec.ts:L105-L111` - existing APIRequestContext selector probe pattern
- [ ] After confirming Purchase Return, navigate to `/accounting/journal-entries?sourceType=PURCHASE_RETURN&sourceCode={purchaseReturnCode}` and assert one row is visible with event label Purchase Return and the created PR code.
      ref: `src/main/java/com/solusi/erp/accounting/journal/web/controller/JournalEntryController.java:L46-L76` - actual journal list route and filter params
      ref: `src/main/resources/templates/accounting/journal/journal-entry-list.html:L26-L32` - source type filter markup
- [ ] Open the journal detail and assert it shows the Purchase Return source code and posted balanced debit/credit totals.
      ref: `src/main/resources/templates/accounting/journal/journal-entry-detail.html:L62-L84` - source and status display
      ref: `src/main/resources/templates/accounting/journal/journal-entry-detail.html:L124-L147` - journal lines and totals display
- [ ] Assert the generated GI detail still opens and remains `COMPLETED`; the accounting event change must not alter physical GI behavior.
      ref: `e2e-tests/tests/procurement/purchase-return.spec.ts:L260-L265` - current generated GI assertion
- [ ] If the spec needs the PR code after creation, read it from the Purchase Return detail page or list, not from guessed sequence values.
      ref: `docs/tests/playwright-pitfalls.md:L190-L211` - avoid URL/entity assumptions; assert against actual UI
- [ ] Run:
      `cd e2e-tests && npx tsc --noEmit`
      `npx playwright test tests/procurement/purchase-return.spec.ts --list`
      `npx playwright test tests/procurement/purchase-return.spec.ts`
- [ ] Run cold-cache transactional check:
      `cd e2e-tests && rm -rf .auth/ && npx playwright test tests/procurement/purchase-return.spec.ts`
- [ ] On first runtime failure, retain screenshot/video and record diagnosis in `docs/reports/2026-06-02-phase-b-purchase-return-accounting.md`.

**Validation criteria:**

- TypeScript compile passes.
- `purchase-return.spec.ts --list` shows the expected scenario.
- `purchase-return.spec.ts` passes at least once normally and once after `.auth` removal.
- The task remains incomplete if the live Playwright run is not executed.

### Task 7: Regression Gate And Handoff [ ]

Run focused and final verification, then record exact results.

**Depends on:** Tasks 1-6

- [ ] Run accounting contract tests:
      `mvn test -Dtest="JournalVariableTest,JournalMessageBundleTest,SchemaFormIntegrationTest"`
- [ ] Run migration-sensitive tests:
      `mvn test -Dtest="*MigrationTest"`
- [ ] Run posting route tests:
      `mvn test -Dtest="CompleteGoodsIssueUseCaseTest,ConfirmPurchaseReturnUseCaseTest,GoodsIssueQueryUseCaseTest"`
- [ ] Run journal/schema affected tests if Task 4 changed templates or controller-visible behavior:
      `mvn test -Dtest="*Journal*Test,*Schema*Test"`
- [ ] Run full Maven gate:
      `mvn clean test`
- [ ] Run Playwright gates from Task 6 if the spec was modified.
- [ ] Update `docs/reports/2026-06-02-phase-b-purchase-return-accounting.md` with commands, outcomes, skipped gates, and deviations from this plan.
- [ ] Apply project SemVer protocol only after implementation is accepted. Phase B is a feature behavior change, so the likely bump is MINOR unless later implementation proves it is docs/tests only.
      ref: `docs/AGENTS.md:L101-L123` - SemVer automation and commit protocol

**Validation criteria:**

- Focused accounting, migration, and Goods Issue/Purchase Return tests pass.
- `mvn clean test` passes.
- Playwright Purchase Return spec passes if modified.
- Report contains enough detail for Phase C/D planning without re-reading raw command output.

## 6. Dependency Notes

- Task 1 must land before Java code can compile with `PURCHASE_RETURN` and `PR_*` variables.
- Task 2 should land before Task 3 runtime verification; otherwise posting will fail with missing schema in seeded databases.
- Task 3 is the core behavior change and should stay narrowly scoped to journal routing, not source resolver or stock movement semantics.
- Task 6 must stay incomplete if the Playwright spec is changed but not executed.

## 7. Deferred Items

- Phase C: Vendor Bill settlement refactor.
- Phase D: Debit Memo Core and automatic DM creation from confirmed Purchase Return.
- Phase E: Debit Memo Allocation, AP reduction, VAT reversal, FX gain/loss, and DMA reversal.
- Phase F: Confirmed Purchase Return reversal using Phase A linked stock/journal reversal primitives.
- Phase G: broader UI, menu, selectors, permissions, full E2E matrix, and integration docs.
