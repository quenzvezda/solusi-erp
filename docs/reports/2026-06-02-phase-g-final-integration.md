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
