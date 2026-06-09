# Implementation Report: Phase G Final Integration

> Plan: `docs/plans/2026-06-02-phase-g-final-integration.md`
> Source: `docs/brainstorming/2026-06-02-vendor-debit-memo.md`
> Created: 2026-06-09

## Findings

### Task 1 - Phase G Surface Contract Audit

Implemented:

- Audited the current Phase G surface against the brainstorming scope:
  - AP menu and permission seed already expose Debit Memo as AP-03 and Debit Memo Allocation as AP-04.
  - Debit Memo and DMA controller routes already cover list/detail/actions/selectors with method-level authorities.
  - Debit Memo detail already exposes Purchase Return, generated Goods Issue, allocation history, metadata, cancel, and allocate entry points.
  - DMA detail already exposes Debit Memo, Vendor Bill line links, apply/reversal journal links, lifecycle actions, and reversal modal wiring.
- Extended controller tests to lock Debit Memo list/detail/update metadata/cancel authorities and DMA list/create/edit/detail/create/update/confirm/cancel/reverse/selector authorities.
- Extended template tests to lock final page regions that already exist: list filters, date fields, pagination, detail summaries, cross-links, action buttons, allocation history, journal links, reversal modal fields, form selector shells, and redirect wiring.

Actionable gaps found for later Phase G tasks:

- Task 2:
  - Debit Memo list still uses a raw `vendorId` input instead of an operator-friendly vendor lookup/autocomplete pattern.
  - Debit Memo list has pagination and query filters but no sortable table headers.
  - DMA list is missing vendor filter support, vendor/currency columns, pagination fragment, and sortable table headers.
  - DMA selector tests should still be hardened around eligibility edge cases for vendor/currency/remaining/outstanding.
- Task 3:
  - DMA status badges are not yet styled as distinctly as the final lifecycle contract expects.
  - DMA detail links journals and has reversal modal input, but it does not yet present final view-only reversal metadata as a dedicated detail region.
  - Static scan still needs to verify no detail surface falls back to raw ids when display codes/names are already available.
- Task 4:
  - Existing Playwright specs cover DMA happy path, partial allocation, multi-VB, stale draft, DMA reversal, and confirmed Purchase Return reversal separately.
  - The combined browser scenario is still missing: confirmed DMA blocks confirmed Purchase Return reversal, DMA reversal restores Debit Memo availability, then Purchase Return reversal succeeds.
- Task 5:
  - `label.debit-memo.allocation-history.empty` still uses stale "next phase" wording in English and Indonesian message bundles.
  - Final docs/stale scans still need to separate shipped MVP behavior from intentionally deferred beyond-MVP items.

Validation:

- PASS: `mvn test -Dtest="DebitMemoControllerTest,DebitMemoAllocationControllerTest,DebitMemoTemplateTest,DebitMemoAllocationTemplateTest,DebitMemoCoreMigrationTest,DebitMemoAllocationMigrationTest"`
  - Tests run: 21
  - Failures: 0
  - Errors: 0
  - JaCoCo check: all coverage checks met for this run.

### Task 2 - List, Filter, Sorting, And Selector Hardening

Implemented:

- Replaced the raw Debit Memo `vendorId` list filter with the project lookup/TomSelect pattern (`data-lookup-path="parties"`) while preserving query-level `vendorId` filtering.
- Added sortable headers to supported Debit Memo list fields: code, memo date, vendor id, currency id, Purchase Return code, gross amount, and settlement status. Settled/remaining stay unsorted because they are computed recap values, not direct JPA fields.
- Added display enrichment for Debit Memo list rows via `PartyLookupProvider` and `CurrencyLookupProvider`, so operators see vendor name/code and currency display instead of raw ids when lookup data is available.
- Added query-level DMA vendor filtering through controller, use case, repository, and JPA query. Filtering joins `DebitMemoEntity` in JPQL, so it remains database-side.
- Added DMA list vendor lookup filter, vendor/currency columns, sortable headers for supported allocation fields, and the standard pagination fragment.
- Enriched DMA list rows with Debit Memo snapshot vendor/currency ids in the query use case, then resolved display labels in the controller via lookup providers.
- Kept eligible Vendor Bill and Debit Memo selectors query-level and paginated; controller/template tests lock both selector fragments, and selector use case tests lock outstanding/remaining amount propagation.

Validation:

- PASS: `mvn test -Dtest="DebitMemoQueryUseCaseTest,DebitMemoAllocationQueryUseCaseTest,DebitMemoAllocationSelectorUseCaseTest,DebitMemoControllerTest,DebitMemoAllocationControllerTest,DebitMemoTemplateTest,DebitMemoAllocationTemplateTest"`
  - Tests run: 27
  - Failures: 0
  - Errors: 0
  - JaCoCo check: all coverage checks met for this run.

### Task 3 - Cross-Link, Action, Badge, And Metadata Polish

Implemented:

- Hardened Debit Memo detail display so vendor and currency resolve through lookup providers; raw ids remain only as fallback when lookup data is unavailable.
- Kept Debit Memo cross-links to Purchase Return, generated Goods Issue, and DMA allocation history intact.
- Hid Debit Memo metadata save action when the Debit Memo is `CANCELLED`; allocate/cancel visibility already matched lifecycle rules.
- Added consistent DMA lifecycle badge styling on DMA list, DMA detail, Debit Memo allocation history, and Vendor Bill allocation history.
- Added a read-only DMA reversal summary region that appears when reversal metadata exists, showing reversal date, reversal reason, and reversal journal link.
- Verified cross-link chain remains present:
  - Purchase Return detail to Debit Memo.
  - Debit Memo detail to Purchase Return, generated Goods Issue, and DMA history.
  - Vendor Bill detail to DMA history and apply shortcut.
  - DMA detail to Debit Memo, Vendor Bills, original journal, and reversal journal.
- Verified source-owned Goods Issue direct cancel guard remains limited to `gi.referenceType == 'MANUAL'`.

Static scan:

- Raw id references on final AP surfaces are now fallback-only for vendor/currency display when lookup data is unavailable.
- Source-owned Goods Issue cancel guard still contains `gi.status == 'COMPLETED' && gi.referenceType == 'MANUAL'`.

Validation:

- PASS: `mvn test -Dtest="DebitMemoTemplateTest,DebitMemoAllocationTemplateTest,VendorBillTemplateTest,PurchaseReturnViewIntegrationTest,PurchaseReturnReverseTemplateIntegrationTest,GoodsIssueViewIntegrationTest"`
  - Tests run: 25
  - Failures: 0
  - Errors: 0
  - JaCoCo check: all coverage checks met for this run.

### Task 4 - Combined Cross-Module Playwright Scenario

Implemented:

- Added the Phase G browser scenario to `e2e-tests/tests/accountspayable/debit-memo-allocation.spec.ts` because the Debit Memo Allocation helpers already own the DM/DMA/VB lifecycle setup.
- Extended the generated Debit Memo fixture to capture Purchase Return id/code/link and generated Goods Issue link from the confirmed Purchase Return detail page.
- Added page-specific helpers for Purchase Return reversal attempts and journal debit/credit total checks.
- Covered the final integration chain in one browser scenario:
  - Confirm Purchase Return and capture generated Debit Memo/Goods Issue.
  - Create and confirm DMA against a confirmed Vendor Bill.
  - Assert Purchase Return reversal is rejected while confirmed DMA actively consumes the generated Debit Memo.
  - Reverse the DMA and assert Debit Memo and Vendor Bill availability return to open/outstanding.
  - Retry Purchase Return reversal and assert Purchase Return `REVERSED`, generated Goods Issue `CANCELLED`, generated Debit Memo `CANCELLED`, no further allocation action is visible, and original/reversal journals are balanced.
- Kept modal confirmation on `#confirm-modal-btn-yes`; no browser dialog hook is used.
- Avoided `selectTomSelect`; the scenario uses page-specific selectors and existing DMA helper behavior.

Unexpected validation/tooling issues:

- `lean-ctx` shell allowlist initially blocked the Windows runner path and `powershell`; resolved additively with `lean-ctx allow powershell`.
- A direct Playwright cold-cache attempt failed with `ERR_CONNECTION_REFUSED` because the previous runner server had already stopped. The scenario was rerun through `e2e-tests\scripts\run-e2e.ps1` as the E2E guide recommends.
- A temporary background wrapper first wrote redirect logs under `target`, which conflicted with the runner's Maven `clean`. The wrapper/logs were moved outside `target`; no application code change was required.
- The background wrapper changed working directory through the runner before writing its exit marker, but the runner log still captured the authoritative Playwright result and server shutdown.

Validation:

- PASS: `cd e2e-tests && npx tsc --noEmit`
- PASS: `cd e2e-tests && npx playwright test tests/accountspayable/debit-memo-allocation.spec.ts --list`
  - Combined scenario listed as `Scenario G - blocks purchase return reversal while DMA is confirmed, then allows it after DMA reversal`.
- PASS: `.\e2e-tests\scripts\run-e2e.ps1 tests/accountspayable/debit-memo-allocation.spec.ts -g "blocks purchase return reversal while DMA is confirmed"`
  - Tests run: 5
  - Failures: 0
  - Playwright summary: `5 passed (53.6s)`
- PASS: cold-cache targeted rerun after removing `e2e-tests\.auth`, through `.\e2e-tests\scripts\run-e2e.ps1 tests/accountspayable/debit-memo-allocation.spec.ts -g "blocks purchase return reversal while DMA is confirmed"`
  - Tests run: 5
  - Failures: 0
  - Playwright summary: `5 passed (56.3s)`

### Task 5 - Documentation, i18n, And Stale Deferred Cleanup

Implemented:

- Updated Debit Memo docs for final list/detail behavior: query-level filters, vendor lookup, sortable direct fields, pagination, display labels, Purchase Return/GI links, DMA history, metadata action rules, cancellation guard, and Vendor Refund as future scope.
- Updated DMA docs for final list filters, columns, pagination, query-level selectors, stale draft revalidation, lifecycle badges, reverse metadata, original/reversal journal links, and multi-Vendor Bill allocation behavior.
- Updated Vendor Bill docs so settlement projection references shipped Debit Memo Allocation directly, not a future placeholder.
- Updated Purchase Return docs so confirmed reversal describes the active DMA blocker and moves only actual beyond-MVP items to the future section.
- Updated Accounting Schema docs to include the Debit Memo Application schema milestone.
- Reviewed Vendor Payment and Goods Issue docs; their current wording already matches the Phase G behavior and source-owned GI cancellation boundary.
- Cleaned stale empty-state i18n:
  - `label.debit-memo.allocation-history.empty` in English now reads `No debit memo allocations yet.`
  - `label.debit-memo.allocation-history.empty` in Indonesian now reads `Belum ada alokasi debit memo.`

Stale scan:

- PASS: `src/main/resources` has no matches for shipped-feature wording that says DMA is `next phase`, `phase berikutnya`, `deferred`, `ditunda`, or `Phase G`.
- PASS: `docs/modules` and `docs/index.md` have no stale matches for those shipped-feature placeholders.
- PASS: AP/procurement/inventory/accounting module docs have no stale matches for `future Debit Memo`, future DMA blocker wording, or confirmed Purchase Return reversal deferred wording.
- PASS: `docs/index.md` still links both final module docs: Debit Memo and Debit Memo Allocation.

Validation:

- PASS: `mvn test -Dtest="*MessageBundleTest,PurchaseReturnMessagesTest"`
  - Tests run: 6
  - Failures: 0
  - Errors: 0
  - Maven result: `BUILD SUCCESS`
  - Note: this narrow message-only run still logs expected low-coverage JaCoCo warnings; the final Phase G gate remains `mvn clean test` for threshold enforcement.

### Task 6 - Final Regression Gate And Handoff

Implemented:

- Closed the Phase G plan as completed and checked the final completion checklist after all backend and browser gates passed.
- Ran the focused backend regression gates for Debit Memo, DMA, Vendor Bill settlement adapters, Purchase Return reversal, stock movement reversal, and journal reversal.
- Ran all migration tests.
- Ran the full backend suite with `mvn clean test`; the suite passed and JaCoCo reported all coverage checks met.
- Ran TypeScript compile for Playwright specs.
- Ran selected Phase G browser gates through `e2e-tests\scripts\run-e2e.ps1` for the changed AP and inventory/procurement regression specs.
- Ran the final full Playwright suite through `e2e-tests\scripts\run-e2e.ps1` after removing `e2e-tests\.auth`; all browser tests passed.

Unexpected validation issues fixed:

- The Phase G DMA E2E scenario originally asserted exactly two raw journal links. The final UI intentionally renders the reversal journal in both the journal summary and reversal metadata region, so the assertion now checks two unique journal hrefs instead of raw link count.
- Vendor Bill Scenario C asserted `CANCELLED` from the unfiltered first list page. Full-suite data can push that bill off page 1, so the spec now filters by its unique invoice number before asserting status.
- Stock Adjustment Scenario B/D used brittle list id discovery for newly created draft adjustments. The helper now writes a unique note, filters the list by that note, and extracts the id from the matching edit link.

Validation:

- PASS: `mvn test -Dtest="DebitMemo*Test,DebitMemoAllocation*Test,VendorBillSettlementSummaryAdapterTest,VendorBillPaymentUpdateAdapterTest,ReverseConfirmedPurchaseReturnUseCaseTest,PurchaseReturnControllerTest,PurchaseReturn*IntegrationTest,StockMovementReversalServiceTest,ReversePostedJournalUseCaseTest"`
  - Tests run: 141
  - Failures: 0
  - Errors: 0
  - Maven result: `BUILD SUCCESS`
  - Note: this focused subset logs expected low-coverage warnings because it is not the full JaCoCo gate.
- PASS: `mvn test -Dtest="*MigrationTest"`
  - Tests run: 15
  - Failures: 0
  - Errors: 0
  - Maven result: `BUILD SUCCESS`
  - Note: this migration subset logs expected low-coverage warnings because it is not the full JaCoCo gate.
- PASS: `mvn clean test`
  - Tests run: 2013
  - Failures: 0
  - Errors: 0
  - Skipped: 0
  - JaCoCo: `All coverage checks have been met.`
  - Maven result: `BUILD SUCCESS`
- PASS: `cd e2e-tests && npx tsc --noEmit`
- PASS: `.\\e2e-tests\\scripts\\run-e2e.ps1 tests/accountspayable/debit-memo-allocation.spec.ts -g "Scenario F"`
  - Playwright summary: `5 passed (1.1m)`
- PASS: `.\\e2e-tests\\scripts\\run-e2e.ps1 tests/accountspayable/vendor-bill.spec.ts -g "Scenario C"`
  - Playwright summary: `5 passed (30.1s)`
- PASS: `.\\e2e-tests\\scripts\\run-e2e.ps1 tests/inventory/stock-adjustment.spec.ts -g "Scenario B|Scenario D"`
  - Playwright summary: `6 passed (44.5s)`
- PASS: full cold-cache Playwright run after removing `e2e-tests\.auth`, through `.\\e2e-tests\\scripts\\run-e2e.ps1`
  - Playwright summary: `85 passed (15.7m)`

Handoff:

- No beyond-MVP work was implemented in Phase G. Vendor Refund, tax override, partial Purchase Return reversal, cross-facility reversal, and legacy corrective journal automation remain outside the shipped MVP boundary.
- Temporary `.lean-ctx-run` wrapper logs were used only to work around long-running command output limits and were removed before final commit.
