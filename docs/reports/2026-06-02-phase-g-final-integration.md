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
