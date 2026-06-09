# Implementation Plan: Phase G Final Integration

> Source: `docs/brainstorming/2026-06-02-vendor-debit-memo.md`
> Created: 2026-06-09
> Sprint: 6 - Debit Memo Final Integration
> Status: READY
>
> **For agentic workers:** execute task-by-task. Explore referenced files fresh before editing each task, update checkbox state incrementally, record deviations in `docs/reports/2026-06-02-phase-g-final-integration.md`, and do not mark a task complete before its validation command passes.

## 1. Goal

Phase G closes the Vendor Debit Memo MVP by reconciling the UI, query surface, cross-links, E2E coverage, documentation, and final regression gates across Phases A-F. The target is not to rebuild Debit Memo, DMA, or Purchase Return reversal; those are already implemented. The target is to prove the shipped surface satisfies the brainstorming contract end to end and to fix final integration gaps found during that proof.

The highest-risk remaining gap is the combined browser flow where a confirmed DMA blocks confirmed Purchase Return reversal, DMA reversal restores Debit Memo availability, and Purchase Return reversal then succeeds.

## 2. Locked Decisions

- Phase G is an integration hardening phase. It should not introduce Vendor Refund, tax override, partial Purchase Return reversal, cross-facility reversal, or legacy corrective journal automation.
- Debit Memo and DMA menu entries remain under Finance & Accounting > Accounts Payable as AP-03 and AP-04.
- Lists must use query-level filtering, pagination, and sorting. Do not load all rows and filter in JavaScript.
- Selectors must remain query-level and paginated.
- E2E tasks must run the actual Playwright scenario at least once and run cold-cache where storage state is involved.
- Confirmed DMA remains the active blocker for Debit Memo cancel and Purchase Return reversal; reversed/cancelled DMA must not block once Debit Memo remaining is full.
- Final documentation must distinguish shipped MVP behavior from explicitly deferred beyond-MVP items.

## 3. Scope Boundary

### Included

- Audit and close gaps in Debit Memo/DMA menu, permission, route, list, detail, selector, action, and cross-link contracts.
- Query/list hardening for Debit Memo and DMA filters, columns, pagination, and sorting.
- UI hardening for action visibility, status badges, reversal metadata, journal links, and display labels.
- Combined Playwright E2E for PR reversal blocked by active DMA, DMA reversal, then successful PR reversal.
- Documentation and i18n stale wording cleanup.
- Final backend and selected browser regression gates.

### Deferred

- Vendor Refund implementation.
- Tax override on DMA.
- Partial Purchase Return reversal.
- Cross-facility Purchase Return reversal.
- Legacy Phase 1 corrective journal automation.
- Direct auto-journal reversal from Journal Entry UI.

## 4. Target File Map

### Web, Query, And Templates

- Modify `DebitMemoController`, `DebitMemoAllocationController`, and related query use cases only where Phase G audit finds contract gaps.
- Modify `templates/accountspayable/debit-memos/list.html`
- Modify `templates/accountspayable/debit-memos/detail.html`
- Modify `templates/accountspayable/debit-memo-allocations/list.html`
- Modify `templates/accountspayable/debit-memo-allocations/detail.html`
- Modify `templates/accountspayable/debit-memo-allocations/form.html` and `static/js/accountspayable/debit-memo-allocations/form.js` only if selector/action defects are found.

### Tests

- Extend Debit Memo/DMA controller, query, template, migration, and message tests.
- Extend `e2e-tests/tests/procurement/purchase-return.spec.ts` or `e2e-tests/tests/accountspayable/debit-memo-allocation.spec.ts` with the combined blocker/reversal scenario.
- Update E2E runner warmup URLs if Phase G adds cold first-hit routes.

### Docs And i18n

- Modify `docs/modules/accountspayable/debit-memo.md`
- Modify `docs/modules/accountspayable/debit-memo-allocation.md`
- Modify `docs/modules/accountspayable/vendor-bill.md`
- Modify `docs/modules/procurement/purchase-return.md`
- Modify `docs/modules/inventory/goods-issue.md`
- Modify `docs/modules/accounting/accounting-schema.md`
- Modify `docs/index.md` if navigation descriptions need final wording.
- Modify `messages.properties` and `messages_id.properties` for stale labels or missing final UI keys.

## 5. Tasks

### Task 1: Phase G Surface Contract Audit

Create a concise, test-backed audit of the shipped Debit Memo, DMA, and Purchase Return reversal UI/contracts against the brainstorming Phase G scope.

**Depends on:** Phase A through Phase F
**Reference module:** `accountspayable.debitmemo`, `accountspayable.debitmemoallocation`, `purchasing.purchasereturn`

Steps:
- [ ] Compare Phase G scope against current menu, routes, permissions, controllers, templates, and E2E specs; record only actionable gaps in the report.
      ref: `docs/brainstorming/2026-06-02-vendor-debit-memo.md:L1678-L1697` - Phase G scope
      ref: `src/main/resources/db/migration/V73__Add_Debit_Memo_Core.sql:L90-L112` - AP-03 menu and Debit Memo permissions
      ref: `src/main/resources/db/migration/V74__Add_Debit_Memo_Allocation.sql:L77-L100` - AP-04 menu and DMA permissions
- [ ] Extend controller tests to lock final route and permission contracts for Debit Memo list/detail/metadata/cancel and DMA list/create/edit/detail/confirm/cancel/reverse/selectors.
      ref: `src/main/java/com/solusi/erp/accountspayable/debitmemo/web/controller/DebitMemoController.java:L40-L121` - current Debit Memo route and permission surface
      ref: `src/main/java/com/solusi/erp/accountspayable/debitmemoallocation/web/controller/DebitMemoAllocationController.java:L28-L191` - current DMA route, action, and selector surface
      ref: `src/test/java/com/solusi/erp/accountspayable/debitmemo/web/controller/DebitMemoControllerTest.java:L39` - Debit Memo controller test location
      ref: `src/test/java/com/solusi/erp/accountspayable/debitmemoallocation/web/controller/DebitMemoAllocationControllerTest.java:L33` - DMA controller test location
- [ ] Extend template tests to assert final required page regions exist: list filters, detail summaries, cross-links, action buttons, allocation history, journal links, and permission-gated actions.
      ref: `src/main/resources/templates/accountspayable/debit-memos/detail.html:L21-L180` - Debit Memo detail action/cross-link/history surface
      ref: `src/main/resources/templates/accountspayable/debit-memo-allocations/detail.html:L12-L88` - DMA detail actions, journal links, and reversal modal
      ref: `src/test/java/com/solusi/erp/accountspayable/debitmemo/web/template/DebitMemoTemplateTest.java:L12` - Debit Memo template test location
      ref: `src/test/java/com/solusi/erp/accountspayable/debitmemoallocation/web/template/DebitMemoAllocationTemplateTest.java:L10` - DMA template test location
- [ ] TEST: Run the surface contract tests and write the audit summary to `docs/reports/2026-06-02-phase-g-final-integration.md`.

**Validation criteria:**
- `mvn test -Dtest="DebitMemoControllerTest,DebitMemoAllocationControllerTest,DebitMemoTemplateTest,DebitMemoAllocationTemplateTest,DebitMemoCoreMigrationTest,DebitMemoAllocationMigrationTest"` passes.
- Report lists every actionable gap found before Task 2, or explicitly states that no gap was found.

### Task 2: List, Filter, Sorting, And Selector Hardening

Bring Debit Memo and DMA list/filter/selector pages to the final UI contract: query-level filters, sortable columns, pagination, and user-readable columns.

**Depends on:** Task 1
**Reference module:** existing AP lists plus table specs

Steps:
- [ ] Harden Debit Memo list filters so keyword, vendor, settlement status, and memo date range are query-level and operator-friendly. Replace raw vendor id input with the project lookup/autocomplete pattern if audit confirms the current field is still raw.
      ref: `docs/brainstorming/2026-06-02-vendor-debit-memo.md:L775-L784` - Debit Memo list filters
      ref: `src/main/resources/templates/accountspayable/debit-memos/list.html:L27-L59` - current Debit Memo filter form
      ref: `docs/spec/autocomplete-generic.md` - lookup/autocomplete standards
- [ ] Harden DMA list filters so keyword, vendor, status, and allocation date range are query-level. Add vendor filter support if still missing from the controller/use case/query adapter.
      ref: `docs/brainstorming/2026-06-02-vendor-debit-memo.md:L786-L793` - DMA list filters
      ref: `src/main/java/com/solusi/erp/accountspayable/debitmemoallocation/web/controller/DebitMemoAllocationController.java:L45-L69` - current DMA list controller filter contract
      ref: `src/main/resources/templates/accountspayable/debit-memo-allocations/list.html:L21-L36` - current DMA filter form
- [ ] Ensure Debit Memo list columns match the brainstorming contract and use sortable table headers where supported by query fields.
      ref: `docs/brainstorming/2026-06-02-vendor-debit-memo.md:L702-L717` - Debit Memo list columns
      ref: `docs/spec/sorting.md:L1-L44` - sortable table fragment and query param pattern
      ref: `src/main/resources/templates/accountspayable/debit-memos/list.html:L62-L115` - current Debit Memo table
- [ ] Ensure DMA list columns include code, allocation date, Debit Memo, vendor, currency, applied gross, status, and actions; add pagination fragment if still missing.
      ref: `docs/brainstorming/2026-06-02-vendor-debit-memo.md:L736-L749` - DMA list columns
      ref: `docs/spec/pagination.md` - pagination fragment standard
      ref: `src/main/resources/templates/accountspayable/debit-memo-allocations/list.html:L37-L76` - current DMA table without final column/pagination coverage
- [ ] Verify selectors remain query-level and paginated for eligible Vendor Bills and Debit Memos; add tests for vendor/currency/remaining/outstanding eligibility if missing.
      ref: `docs/brainstorming/2026-06-02-vendor-debit-memo.md:L795-L843` - selector eligibility and columns
      ref: `src/main/java/com/solusi/erp/accountspayable/debitmemoallocation/web/controller/DebitMemoAllocationController.java:L160-L191` - selector routes
      ref: `src/test/java/com/solusi/erp/accountspayable/debitmemoallocation/application/usecase/query/DebitMemoAllocationSelectorUseCaseTest.java` - selector test location
- [ ] TEST: Extend query/controller/template tests for new filters, sortable headers, pagination, list columns, and selector eligibility.

**Validation criteria:**
- `mvn test -Dtest="DebitMemoQueryUseCaseTest,DebitMemoAllocationQueryUseCaseTest,DebitMemoAllocationSelectorUseCaseTest,DebitMemoControllerTest,DebitMemoAllocationControllerTest,DebitMemoTemplateTest,DebitMemoAllocationTemplateTest"` passes.
- Debit Memo and DMA list pages preserve filter/sort params through pagination.
- Selector endpoints return only eligible rows and never rely on client-side filtering of all data.

### Task 3: Cross-Link, Action, Badge, And Metadata Polish

Close final UI gaps on detail pages and action surfaces so operators can navigate the complete PR -> GI -> DM -> DMA -> VB -> Journal chain.

**Depends on:** Task 2
**Reference module:** Debit Memo, DMA, Vendor Bill, Purchase Return, journal detail pages

Steps:
- [ ] Verify and harden all cross-links listed in the brainstorming: Purchase Return detail to Debit Memo, Debit Memo detail to Purchase Return/generated GI/DMA history, Vendor Bill detail to DMA history/apply shortcut, and DMA detail to Debit Memo/Vendor Bills/journals.
      ref: `docs/brainstorming/2026-06-02-vendor-debit-memo.md:L766-L773` - required cross-links
      ref: `src/main/resources/templates/accountspayable/debit-memos/detail.html:L65-L68` - Debit Memo to Purchase Return/GI links
      ref: `src/main/resources/templates/accountspayable/debit-memo-allocations/detail.html:L38-L43` - DMA to Debit Memo/journal links
      ref: `src/main/resources/templates/accountspayable/debit-memo-allocations/detail.html:L53-L55` - DMA to Vendor Bill line links
- [ ] Improve status badge styling for DMA statuses and ensure Debit Memo settlement badges remain visually distinct for `OPEN`, `PARTIALLY_SETTLED`, `SETTLED`, and `CANCELLED`.
      ref: `src/main/resources/templates/accountspayable/debit-memos/detail.html:L13-L18` - Debit Memo detail badge
      ref: `src/main/resources/templates/accountspayable/debit-memo-allocations/detail.html:L9-L10` - current DMA detail badge
      ref: `src/main/resources/templates/accountspayable/debit-memo-allocations/list.html:L55-L56` - current DMA list badge
- [ ] Show final DMA reversal metadata when present: reversal date, reversal reason, reversal journal, and final view-only state.
      ref: `docs/brainstorming/2026-06-02-vendor-debit-memo.md:L1368-L1390` - DMA reversal shape
      ref: `src/main/resources/templates/accountspayable/debit-memo-allocations/detail.html:L41-L75` - current journal and reversal modal area
- [ ] Ensure action visibility matches lifecycle rules: no Allocate on cancelled/settled DM, no metadata save on cancelled DM, no edit/cancel on confirmed/reversed DMA, reverse only for confirmed DMA, and source-owned GI still has no direct cancel button.
      ref: `docs/brainstorming/2026-06-02-vendor-debit-memo.md:L989-L1024` - cancellation and reversal policy
      ref: `src/main/resources/templates/accountspayable/debit-memos/detail.html:L21-L36` - Debit Memo allocate/cancel action visibility
      ref: `src/main/resources/templates/accountspayable/debit-memo-allocations/detail.html:L12-L28` - DMA detail action visibility
      ref: `src/main/resources/templates/inventory/goods-issues/view.html:L20-L27` - source-owned GI direct cancel guard
- [ ] TEST: Extend template/security render tests for cross-links, action visibility, badge classes, and reversal metadata.

**Validation criteria:**
- `mvn test -Dtest="DebitMemoTemplateTest,DebitMemoAllocationTemplateTest,VendorBillTemplateTest,PurchaseReturnViewIntegrationTest,PurchaseReturnReverseTemplateIntegrationTest"` passes.
- Manual static scans show no final UI surface still uses raw ids where a shipped display name/code is available and already present in the DTO.
- Source-owned GI remains non-cancellable from the GI detail page.

### Task 4: Combined Cross-Module Playwright Scenario

Add the final high-value browser scenario that spans confirmed Purchase Return, generated Debit Memo, confirmed DMA, DMA reversal, and confirmed Purchase Return reversal.

**Depends on:** Task 3
**Reference module:** existing Purchase Return and DMA Playwright specs

Steps:
- [ ] Re-read target templates and page JS before editing the spec: Purchase Return view/reverse, Debit Memo detail, DMA form/detail, and DMA form JS.
      ref: `docs/tests/playwright-pitfalls.md:L280-L293` - E2E authoring checklist
      ref: `src/main/resources/templates/purchasing/purchase-returns/view.html` - Purchase Return status/action source of truth
      ref: `src/main/resources/templates/purchasing/purchase-returns/reverse.html` - Purchase Return reverse form source of truth
      ref: `src/main/resources/templates/accountspayable/debit-memo-allocations/form.html:L16-L95` - DMA form modal selector and AJAX surface
      ref: `src/main/resources/static/js/accountspayable/debit-memo-allocations/form.js:L78-L133` - DMA selector/action JS
- [ ] Extract or share helpers carefully between `purchase-return.spec.ts` and `debit-memo-allocation.spec.ts` only if it reduces duplication without hiding page-specific behavior.
      ref: `e2e-tests/tests/procurement/purchase-return.spec.ts:L454-L511` - current confirmed PR reversal scenario
      ref: `e2e-tests/tests/accountspayable/debit-memo-allocation.spec.ts:L393-L509` - current DMA create/confirm/reverse helpers
- [ ] Add scenario: create and confirm a Purchase Return, capture generated Debit Memo/GI, create or reuse a confirmed Vendor Bill, create and confirm DMA consuming the generated Debit Memo, then attempt Purchase Return reversal and assert it is rejected because active confirmed DMA exists.
      ref: `docs/brainstorming/2026-06-02-vendor-debit-memo.md:L996-L1048` - active DMA blocks PR reversal/cancel
      ref: `docs/reports/2026-06-02-purchase-return-reversal.md:L97-L98` - Phase F remaining Phase G note
- [ ] Continue the same scenario by reversing the DMA, verifying Debit Memo settlement returns to open/full remaining, then retrying Purchase Return reversal and asserting PR `REVERSED`, generated GI `CANCELLED`, generated DM `CANCELLED`, and original/reversal journals balanced.
      ref: `e2e-tests/tests/accountspayable/debit-memo-allocation.spec.ts:L627-L638` - DMA reversal E2E assertions
      ref: `e2e-tests/tests/procurement/purchase-return.spec.ts:L484-L508` - PR reversal side-effect assertions
- [ ] Do not use `selectTomSelect`; use page-specific selectors, `setTomSelectValue` only where payload is irrelevant, and `page.request.get` for pre-navigation API lookups.
      ref: `docs/tests/playwright-pitfalls.md:L271-L289` - helper limitations and page.request rule
- [ ] For modal confirms, click `#confirm-modal-btn-yes`; do not use `page.on('dialog')`.
      ref: `docs/tests/playwright-pitfalls.md:L290-L290` - ERP confirm modal checklist
- [ ] TEST: Run TypeScript compile, Playwright list, targeted scenario normal run, and cold-cache run through the Windows runner. Keep first-failure screenshots/videos for diagnosis.

**Validation criteria:**
- `cd e2e-tests && npx tsc --noEmit` clean.
- `cd e2e-tests && npx playwright test tests/procurement/purchase-return.spec.ts --list` or the chosen spec `--list` shows the combined Phase G scenario.
- `.\e2e-tests\scripts\run-e2e.ps1 tests/procurement/purchase-return.spec.ts -g "blocks purchase return reversal while dma is confirmed"` passes.
- Remove `e2e-tests\.auth`, rerun the same targeted command, and it passes cold-cache.

### Task 5: Documentation, i18n, And Stale Deferred Cleanup

Finalize operator-facing and developer-facing documentation so it describes shipped MVP behavior without stale phase placeholders.

**Depends on:** Task 4
**Reference module:** final docs from Phases D-F

Steps:
- [ ] Update Debit Memo docs for final list/detail/filter/actions, DMA history, cancellation guard, Purchase Return reversal interaction, and Vendor Refund as explicitly future scope.
      ref: `docs/modules/accountspayable/debit-memo.md:L1-L126` - current Debit Memo docs
      ref: `docs/brainstorming/2026-06-02-vendor-debit-memo.md:L719-L734` - Debit Memo detail contract
- [ ] Update DMA docs for final list/detail/filter/selectors, multi-VB allocation, stale draft guard, reverse flow, journal links, and Phase G combined integration behavior.
      ref: `docs/modules/accountspayable/debit-memo-allocation.md:L1-L120` - current DMA docs
      ref: `docs/brainstorming/2026-06-02-vendor-debit-memo.md:L736-L843` - DMA list/detail/selector contract
- [ ] Update Vendor Bill, Vendor Payment, Purchase Return, Goods Issue, and Accounting Schema docs where Phase G hardening changed wording or cross-module behavior.
      ref: `docs/modules/accountspayable/vendor-bill.md:L175-L182` - Vendor Bill DMA settlement summary docs
      ref: `docs/modules/procurement/purchase-return.md:L71-L95` - Purchase Return reversal and deferred scope docs
      ref: `docs/modules/inventory/goods-issue.md:L123-L146` - source-owned GI reversal docs
      ref: `docs/modules/accounting/accounting-schema.md:L328` - accounting boundary docs
- [ ] Clean stale i18n text that still implies DMA is future work, especially empty-state or helper labels that now render in shipped pages.
      ref: `src/main/resources/messages.properties:L1906` - current English allocation-history empty state wording
      ref: `src/main/resources/messages_id.properties:L1906` - current Indonesian allocation-history empty state wording
- [ ] Run stale scans for `next phase`, `phase berikutnya`, `deferred`, `ditunda`, `Phase G`, and old no-allocation placeholders; keep only intentional beyond-MVP references.
      ref: `docs/brainstorming/2026-06-02-vendor-debit-memo.md:L1729-L1735` - legitimate beyond-MVP deferred list
- [ ] TEST: Extend message/doc tests where present and record stale-scan results in the report.

**Validation criteria:**
- `mvn test -Dtest="*MessageBundleTest,PurchaseReturnMessagesTest"` passes.
- Stale scan has no shipped-feature wording that still says Debit Memo Allocation, Purchase Return confirmed reversal, or Phase G behavior is future/deferred.
- Docs index still links the final Debit Memo and DMA module docs.

### Task 6: Final Regression Gate And Handoff

Close the Vendor Debit Memo MVP with backend, browser, documentation, and git hygiene gates.

**Depends on:** Task 5
**Reference module:** Phase A-F reports

Steps:
- [ ] Run focused Debit Memo, DMA, Purchase Return reversal, Vendor Bill settlement, stock reversal, and journal reversal backend gates.
      ref: `docs/reports/2026-06-02-phase-e-debit-memo-allocation.md:L73-L77` - Phase E final selected gate format
      ref: `docs/reports/2026-06-02-purchase-return-reversal.md:L90-L98` - Phase F final gate format
- [ ] Run all migration tests.
      ref: `src/test/java/com/solusi/erp/accountspayable/debitmemo/infrastructure/persistence/DebitMemoCoreMigrationTest.java` - Debit Memo migration test location
      ref: `src/test/java/com/solusi/erp/accountspayable/debitmemoallocation/infrastructure/persistence/DebitMemoAllocationMigrationTest.java` - DMA migration test location
      ref: `src/test/java/com/solusi/erp/purchasing/purchasereturn/infrastructure/persistence/PurchaseReturnMigrationTest.java` - Purchase Return migration test location
- [ ] Run `mvn clean test` and record total tests plus JaCoCo result.
- [ ] Run selected Phase G Playwright gates: DMA spec, Purchase Return spec with combined scenario, and any AP/procurement regression spec touched by Task 2/3.
      ref: `docs/tests/playwright-pitfalls.md:L241-L253` - runtime validation cannot be deferred
- [ ] Run cold-cache targeted browser gates for storage-state-dependent specs by removing `e2e-tests\.auth` before rerun.
- [ ] Update `docs/reports/2026-06-02-phase-g-final-integration.md` with decisions, skipped checks, remaining beyond-MVP scope, and final command outcomes.
- [ ] Commit the final plan/report/test/docs changes task-by-task, then leave `git status --short` clean.

**Validation criteria:**
- `mvn test -Dtest="DebitMemo*Test,DebitMemoAllocation*Test,VendorBillSettlementSummaryAdapterTest,VendorBillPaymentUpdateAdapterTest,ReverseConfirmedPurchaseReturnUseCaseTest,PurchaseReturnControllerTest,PurchaseReturn*IntegrationTest,StockMovementReversalServiceTest,ReversePostedJournalUseCaseTest"` passes.
- `mvn test -Dtest="*MigrationTest"` passes.
- `mvn clean test` passes with JaCoCo checks met.
- `cd e2e-tests && npx tsc --noEmit` passes.
- Selected Playwright normal and cold-cache runs pass for Phase G changed specs.
- `docs/reports/2026-06-02-phase-g-final-integration.md` contains the final handoff summary.

## 6. Final Completion Checklist

- [ ] AP menu and permissions expose Debit Memo and DMA correctly.
- [ ] Debit Memo list supports final filters, pagination, sorting, and columns.
- [ ] DMA list supports final filters, pagination, sorting, and columns.
- [ ] Eligible Vendor Bill and Debit Memo selectors are query-level, paginated, and eligibility-correct.
- [ ] All cross-links in the PR/GI/DM/DMA/VB/journal chain are present and permission-safe.
- [ ] Status badges and action visibility match lifecycle rules.
- [ ] Confirmed DMA blocks Purchase Return reversal in browser E2E.
- [ ] Reversed DMA restores Debit Memo availability and no longer blocks Purchase Return reversal.
- [ ] Purchase Return reversal after DMA reversal cancels generated GI and Debit Memo and links reversal journals.
- [ ] Docs and i18n no longer contain stale shipped-feature deferral wording.
- [ ] `mvn clean test` passes.
- [ ] Selected normal and cold-cache Playwright gates pass.
