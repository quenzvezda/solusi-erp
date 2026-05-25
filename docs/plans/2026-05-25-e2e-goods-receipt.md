# Implementation Plan: E2E Goods Receipt Flow

> Source: (no brainstorming doc — derived from autonomous exploration of inventory.goodsreceipt slice)
> Created: 2026-05-25
> Sprint: stabilization continuation after Playwright fix wave (commits 9fb4a83, ab6580c, cbc2c8d)
> Status: IN_PROGRESS

## Summary

Add a Playwright E2E spec for the Goods Receipt module mirroring the proven
shape of `tests/inventory/stock-adjustment.spec.ts`. Coverage stays smoke-level
per user direction: "yang penting flownya save selesai" — create-from-PO,
edit-persists, complete-transition, delete-API. Helpers and seeds extend what
the SA spec already exercises, so the marginal cost is one new spec file plus a
small V9000 seed delta.

## Context (essential references)

- Domain entity: `src/main/java/com/solusi/erp/inventory/goodsreceipt/domain/model/GoodsReceipt.java` — DRAFT → COMPLETED only (no APPROVED/CANCELLED); `complete()` requires at least one line with positive qty; `update()` rejected when COMPLETED.
- Status enum: `GoodsReceiptStatus.java` — only `DRAFT, COMPLETED`.
- Controller URL base: `/inventory/goods-receipts` (no `/view/` segment — view path is `/{id}`, not `/view/{id}` like SA).
- Status badge location: `templates/inventory/goods-receipts/view.html:14-18` — badge sits **inside** `.page-title`, NOT as a sibling like in SA's view.html. Use `.page-title .badge` selector.
- Form id: `#gr-form` (not `#adjustment-form`).
- PO-line modal selector endpoint: `GET /inventory/goods-receipts/selectors/purchase-order-lines?referenceType=PURCHASE_ORDER&referenceId={poId}` — HTMX fragment that the page JS opens (`po-line-selector-modal`).
- Page JS: `static/js/inventory/goods-receipt/goods-receipt-form.js` — same drawer pattern as SA (`#drawer-non-serial`, `.btn-save-drawer`, payload-driven UoM resolve via `productSelect.tomselect.options[id].payload`). All SA pitfalls apply here.
- Permissions: `GOODS-RECEIPT_{READ,CREATE,UPDATE,DELETE,COMPLETE}` seeded in `V50__Add_Goods_Receipt_Module.sql`. Cross-slice lookups required: `LOOKUP_INVENTORY` for product/uom (already granted to `warehouse1` per V9000 D010 / SA spec setup).
- Seed gap (CRITICAL): `V9000__e2e_seed_data.sql` does NOT seed any Purchase Order. Goods Receipt creation requires an existing PO with at least one OPEN line. Plan Task 1 adds a fixed-id seeded PO so the E2E spec has a deterministic source.

## Tasks

### Task 1: Seed deterministic PO + grant warehouse1 GOODS-RECEIPT permissions
Extend `V9000__e2e_seed_data.sql` to (a) insert one OPEN purchase order with two
lines (laptop + chair) bound to facility 9101 / supplier @p_sup1 / IDR, fixed id
9201, line ids 9201/9202; (b) grant the warehouse role the
`GOODS-RECEIPT_{READ,CREATE,UPDATE,DELETE,COMPLETE}` permissions if not already
granted via D-tier migrations.

**Depends on:** (none — must run first; all later tasks consume the seeded ids)
**Reference module:** existing V9000 PR/SA seed blocks

Steps:
- [ ] Read current V9000 file to confirm: (1) presence of `@p_sup1`, `@cur_idr`, `@prd_laptop`, `@prd_chair`, `@role_warehouse_id` SET vars; (2) absence of any `pur_purchase_orders` insert; (3) which permissions warehouse role already has (search `INSERT INTO role_permissions`).
      ref: src/main/resources/db/migration-h2/V9000__e2e_seed_data.sql — locate "facilities", "supplier_price_lists" sections to find good insertion point
- [ ] Add `pur_purchase_orders` insert with explicit id 9201, status `APPROVED` (the status that allows GR creation per `PurchaseOrderGoodsReceiptSourceResolver`), supplier @p_sup1, currency @cur_idr, exchange_rate 1.0000, facility_id 9101, code `E2E-PO-9201`, transaction_date `2026-05-19`, `total_amount` matching the lines.
      ref: src/main/resources/db/migration/V46__Add_Purchasing_Module.sql — column list for pur_purchase_orders + pur_purchase_order_lines
      ref: src/main/java/com/solusi/erp/inventory/goodsreceipt/infrastructure/adapter/PurchaseOrderGoodsReceiptSourceResolver.java — what PO statuses qualify as a valid GR source
- [ ] Add two `pur_purchase_order_lines` rows (ids 9201, 9202): laptop qty 5 @ 8500000 IDR, chair qty 4 @ 1500000 IDR. Both with received_to_date_qty=0 so the entire qty is open. Use base UoM 9001 (set by V9000 product seed).
      ref: src/main/resources/db/migration-h2/V9000__e2e_seed_data.sql — match column ordering of existing seed-line patterns; H2 uses MySQL mode + DATABASE_TO_LOWER, so quote-free identifiers.
- [ ] Grant warehouse role the 5 GOODS-RECEIPT permissions. Use the existing role-permission insert pattern in V9000 (search for `(@role_warehouse_id, p.id)`). Use `LIKE 'GOODS\\_-\\_RECEIPT\\_%' ESCAPE '\\'` if a prefix wildcard is used — but safer is exact `IN (...)` because the permission name uses both `-` and `_` separators.
      ref: src/main/resources/db/migration-h2/V9000__e2e_seed_data.sql — search "role_permissions" insert blocks
      ref: docs/AGENTS.md section 7 — SQL Wildcard Safety note about ESCAPE
- [ ] Run `bash scripts/check-migration-parity.sh` to confirm H2 seed still parses cleanly (V9000 lives only in migration-h2; the parity script ensures versioned migrations stay aligned, V9000 is on the H2 side only).
      ref: scripts/check-migration-parity.sh — what it checks

**Validation criteria:**
- After `mvnw -B package -DskipTests -Pe2e -q` and JAR start, `curl http://localhost:18080/inventory/goods-receipts/create?referenceType=PURCHASE_ORDER&referenceId=9201` (after warehouse1 login) returns 200 with non-empty `#line-container` empty-state hidden — proving the seed wiring resolves.
- A second curl to `/inventory/goods-receipts/selectors/purchase-order-lines?referenceType=PURCHASE_ORDER&referenceId=9201` returns the modal fragment HTML containing both `E2E-PRD-LAPTOP` and `E2E-PRD-CHAIR`.

### Task 2: Spec scaffold + warehouse1 sanity scenario
Create `e2e-tests/tests/inventory/goods-receipt.spec.ts` with the file-level
`test.describe`, the `storageState: storageStatePath('warehouse1')` use, fixed
seed-id constants, helpers stub block, and one sanity scenario that just opens
the list.

**Depends on:** Task 1
**Reference module:** `e2e-tests/tests/inventory/stock-adjustment.spec.ts`

Steps:
- [ ] Create file `e2e-tests/tests/inventory/goods-receipt.spec.ts`. Imports: `test, expect, storageStatePath` from `../../fixtures/base`, `navigateToModule` from `../../helpers/navigation`. Do NOT import `selectTomSelect` (broken — see pitfalls #3).
      ref: e2e-tests/tests/inventory/stock-adjustment.spec.ts:L1-L10 — import block pattern
      ref: docs/tests/playwright-pitfalls.md section 3 — selectTomSelect broken signature
- [ ] Define seed-id constants matching V9000 from Task 1: `const PO_ID = '9201'`, `const PO_LINE_LAPTOP_ID = '9201'`, `const PO_LINE_CHAIR_ID = '9202'`, `const FACILITY_ID = '9101'`. Match the `string` type used in SA spec because TomSelect injection always stringifies.
      ref: e2e-tests/tests/inventory/stock-adjustment.spec.ts:L13-L15 — seed-id const block
- [ ] Wrap whole file with `test.describe('@inventory Goods Receipt flow', () => { test.use({ storageState: storageStatePath('warehouse1') }); ... });`. Tag stays `@inventory` to match the existing convention.
      ref: e2e-tests/tests/inventory/stock-adjustment.spec.ts:L155-L165 — describe + storageState pattern
- [ ] Add sanity scenario `test('sanity: warehouse1 can open goods-receipts list', ...)`: navigate to `/inventory/goods-receipts`, assert URL matches `/inventory/goods-receipts(\?.*)?$/`, assert `page.locator('table')` visible. This proves the storage state + permission grant from Task 1 worked end-to-end before the harder scenarios run.
      ref: e2e-tests/tests/inventory/stock-adjustment.spec.ts:L167-L172 — sanity scenario shape
- [ ] Run TypeScript compile check: `cd e2e-tests && npx tsc --noEmit`. Run the sanity test in isolation: `npx playwright test tests/inventory/goods-receipt.spec.ts -g "sanity"`. Confirm green.

**Validation criteria:**
- `cd e2e-tests && npx tsc --noEmit` clean.
- `npx playwright test tests/inventory/goods-receipt.spec.ts --list` shows the sanity scenario only at this point (no skipped Scenario A-D yet — they get added in Task 4-7).
- The sanity scenario runs green at least once.

### Task 3: Helper — pickPoLineFromModalSelector
Add a local helper that opens the PO-line selector modal, picks the row by
product code, and clicks "Tambah ke daftar" (or equivalent). The helper goes in
the spec file (private) — we do NOT promote to `helpers/` until a second spec
needs it.

**Depends on:** Task 2
**Reference module:** `goods-receipt-form.js` `appendSelectedPoLine()` flow + `po-line-selector-modal.html` template

Steps:
- [ ] Read the modal selector template `templates/inventory/goods-receipts/fragments/po-line-selector-modal.html` to identify: row data attributes (data-payload? data-product-code? data-line-id?), submit button selector, the modal's outer id (used by HTMX to replace).
      ref: src/main/resources/templates/inventory/goods-receipts/fragments/po-line-selector-modal.html — full file is the read target
- [ ] Read JS fn `parsePoLinePayload(row)` in goods-receipt-form.js to confirm exactly which row dataset keys it reads (productId, productName, productSubtext, uomId, uomName, uomSubtext, serialized). The helper must NOT bypass this — it should click whatever button the user clicks.
      ref: src/main/resources/static/js/inventory/goods-receipt/goods-receipt-form.js:L210-L227 — parsePoLinePayload() shows the dataset contract
- [ ] Find the trigger button on form.html that opens the modal (likely `id` attribute is something like `btn-pick-po-line`). The helper has to click that first, then wait for `#poLineSelectorModal` (or whatever the modal id is) to be visible.
      ref: src/main/resources/templates/inventory/goods-receipts/form.html — search for `selectors/purchase-order-lines` or button text key `label.gr.line.pick`
- [ ] Implement helper signature `async function pickPoLineFromModalSelector(page: Page, productCode: string): Promise<void>` that: (a) clicks the trigger button to open the modal, (b) `expect(page.locator('#poLineSelectorModal.show')).toBeVisible({ timeout: 5_000 })` (use the actual id), (c) finds the row by `tr[data-product-code="..."]` or via `text=` matcher fallback, (d) clicks the row's "Pilih" (or equivalent) button, (e) waits for the modal to close using the same Bootstrap state-class technique used in `setQuantityViaDrawer` (no `.show`, no `.hiding`, no `.showing`).
      ref: e2e-tests/tests/inventory/stock-adjustment.spec.ts:L102-L116 — drawer state-class wait pattern (paste-template for modal close wait)
      ref: docs/tests/playwright-pitfalls.md — never use `toBeHidden` on Bootstrap state-class transitions
- [ ] After modal close, wait for the new line row to appear: `await expect(page.locator('#line-container tr.line-row')).toHaveCount(prevCount + 1, { timeout: 5_000 })`. Capture prevCount before clicking the trigger button.

**Validation criteria:**
- Helper compiles (`tsc --noEmit`).
- Manual trace: drop a temporary `test.only(...)` that calls only the helper after navigating to `/inventory/goods-receipts/create?...&referenceId=9201` — confirm one new `tr.line-row` appears with `data-product-code="E2E-PRD-LAPTOP"` after helper returns.
- Remove the temporary `test.only` before committing.

### Task 4: Scenario A — Create DRAFT from PO line
Open `/inventory/goods-receipts/create?referenceType=PURCHASE_ORDER&referenceId=9201`,
pick the laptop line via modal, set quantity via drawer, save, assert redirect
to list and a new row exists.

**Depends on:** Task 3
**Reference module:** `e2e-tests/tests/inventory/stock-adjustment.spec.ts` Scenario A

Steps:
- [ ] Add `test('@smoke Scenario A — create DRAFT from PO line', ...)`. Tag is `@smoke` because this is the create-happy-path; matches PR convention where Scenario A is also tagged `@smoke`.
      ref: e2e-tests/tests/procurement/purchase-requisition.spec.ts — happy-path Scenario A is @smoke tagged
- [ ] Navigate via `await navigateToModule(page, '/inventory/goods-receipts/create?referenceType=PURCHASE_ORDER&referenceId=9201')`. Assert `expect(page.locator('#gr-form')).toBeVisible()`. Note: form id is `#gr-form`, not `#adjustment-form`.
      ref: src/main/resources/templates/inventory/goods-receipts/form.html:L41 — `id="gr-form"`
- [ ] Header fields: receipt date is pre-filled by controller; supplier/facility/currency are derived from PO and rendered readonly. Skip header interactions — only thing to set is the line.
      ref: src/main/java/com/solusi/erp/inventory/goodsreceipt/web/controller/GoodsReceiptController.java:L87-L96 — createForm() pre-populates draft from PO via getGoodsReceiptCreateViewUseCase
- [ ] Pick the laptop line: `await pickPoLineFromModalSelector(page, 'E2E-PRD-LAPTOP')`. Confirm the row's `data-product-code` matches via assertion.
- [ ] Set quantity via drawer using a copy-adapted `setQuantityViaDrawer` from SA spec — copy verbatim, then verify the GR drawer fragment uses the same `#drawer-non-serial`, `.input-qty-target`, `.btn-save-drawer`, `.select-uom-target` selectors. They do (drawer-fragments.html in goods-receipts mirrors the SA fragment shape).
      ref: e2e-tests/tests/inventory/stock-adjustment.spec.ts:L87-L120 — `setQuantityViaDrawer` source
      ref: src/main/resources/templates/inventory/goods-receipts/drawer-fragments.html — confirm same selectors
- [ ] Submit: `await Promise.all([page.waitForURL(/\/inventory\/goods-receipts(\?.*)?$/, { timeout: 15_000, waitUntil: 'domcontentloaded' }), page.locator('#gr-form button[type="submit"]').first().click()])`.
- [ ] Capture new GR id from the redirected list page using highest-id-from-`a[href*="/inventory/goods-receipts/edit/"]` pattern.
      ref: e2e-tests/tests/inventory/stock-adjustment.spec.ts:L137-L152 — newId capture pattern
- [ ] Open `/inventory/goods-receipts/{newId}` (note: NO `/view/` segment — view path is `/{id}` for GR). Assert `expect(page.locator('.page-title .badge', { hasText: 'DRAFT' })).toBeVisible({ timeout: 10_000 })`. Selector is `.page-title .badge` (badge is INSIDE page-title) NOT `.page-header .badge` like SA.
      ref: src/main/resources/templates/inventory/goods-receipts/view.html:L14-L18 — badge nested inside h2.page-title
      ref: src/main/java/com/solusi/erp/inventory/goodsreceipt/web/controller/GoodsReceiptController.java:L171-L180 — view URL is `/{id}`, not `/view/{id}`

**Validation criteria:**
- `npx playwright test tests/inventory/goods-receipt.spec.ts -g "Scenario A"` green.
- The test runs cold-cache too (delete .auth/ first, run setup + this scenario): also green.

### Task 5: Scenario B — Edit DRAFT persists changes
Reuse the new GR id from Scenario A (or factor a `createSampleDraftGr` helper).
Open `/edit/{id}`, change qty 1 → 2 via drawer, save, reload, assert persisted
qty.

**Depends on:** Task 4
**Reference module:** `e2e-tests/tests/inventory/stock-adjustment.spec.ts` Scenario B + `createSampleDraftSa` factoring

Steps:
- [ ] Factor `createSampleDraftGr(page)` from Scenario A's body. The function navigates create URL → picks laptop via helper → sets qty 1 → submits → captures + returns new id. Mirrors `createSampleDraftSa` exactly.
      ref: e2e-tests/tests/inventory/stock-adjustment.spec.ts:L122-L153 — `createSampleDraftSa` factoring template
- [ ] Refactor Scenario A to call `createSampleDraftGr(page)` then assert badge — keeps Scenario A still green and proves the helper works before B-D consume it.
- [ ] Add `test('Scenario B — edit DRAFT persists changes', ...)`: call `createSampleDraftGr(page)` to get id, navigate to `/inventory/goods-receipts/edit/{id}`, assert `#gr-form` visible + line count is 1.
- [ ] Change quantity 1 → 2 via drawer using `setQuantityViaDrawer(page, 0, 2)`. Click submit, wait for redirect to list.
- [ ] Reopen `/inventory/goods-receipts/edit/{id}`, read line 0 quantity from the hidden input or the visible AutoNumeric input, assert it parses to 2.
      ref: e2e-tests/tests/inventory/stock-adjustment.spec.ts:L260-L278 — Scenario B assertion pattern (raw value extraction + regex match for "2" or "2.00")

**Validation criteria:**
- `npx playwright test tests/inventory/goods-receipt.spec.ts -g "Scenario B"` green.
- Scenario A still green after refactor: `npx playwright test tests/inventory/goods-receipt.spec.ts -g "Scenario A"`.

### Task 6: Scenario C — Complete DRAFT transitions to COMPLETED
Open `/edit/{id}`, click `#btn-complete`, accept Bootstrap confirm modal,
navigate to view page, assert badge "COMPLETED" via `.page-title .badge`.

**Depends on:** Task 5
**Reference module:** SA Scenario C (Process to Inventory) — same `ErpAction.confirmAndSubmit` pattern

Steps:
- [ ] Read goods-receipt-form.js to find the complete-button trigger and confirm whether it uses `ErpAction.confirmAndSubmit` (Bootstrap modal `#modal-global-confirm` + `#confirm-modal-btn-yes`) or a different flow. If different, document the exact selector path.
      ref: src/main/resources/static/js/inventory/goods-receipt/goods-receipt-form.js — search for `btn-complete` or `complete` handler
      ref: e2e-tests/tests/inventory/stock-adjustment.spec.ts:L304-L327 — Scenario C uses `#confirm-modal-btn-yes`; if GR uses native confirm() instead, must use `page.on('dialog')` (less common path)
      ref: docs/tests/playwright-pitfalls.md — modal confirm vs native dialog distinction
- [ ] Add `test('Scenario C — Complete DRAFT transitions to COMPLETED', ...)`: get id from `createSampleDraftGr`, navigate to edit page, assert `#btn-complete` is visible. Click it.
- [ ] Click `#confirm-modal-btn-yes` (assuming ErpAction pattern). Wait for navigation to view page: `await page.waitForURL(/\/inventory\/goods-receipts\/\d+(\?.*)?$/, { waitUntil: 'domcontentloaded' })`.
- [ ] Assert badge: `await expect(page.locator('.page-title .badge', { hasText: 'COMPLETED' })).toBeVisible({ timeout: 10_000 })`.
- [ ] Assert immutability: navigate to `/edit/{id}` should redirect — but the controller actually serves the form and rejects on save (controller doesn't redirect COMPLETED to view like SA does). Skip the SA-style edit-redirect check; instead assert that the form's submit, if attempted, would 400. Simpler: just trust the badge + skip the immutability assertion, document this in pitfalls if relevant.

**Validation criteria:**
- `npx playwright test tests/inventory/goods-receipt.spec.ts -g "Scenario C"` green.
- Server log shows `POST /inventory/goods-receipts/{id}/complete` returned 200.

### Task 7: Scenario D — Delete DRAFT via API
Same pattern as SA Scenario E: list page is the CSRF carrier, `fetch()` calls
`DELETE /inventory/goods-receipts/{id}`, assert 2xx, reload list, assert row gone.

**Depends on:** Task 5
**Reference module:** SA Scenario E

Steps:
- [ ] Add `test('Scenario D — delete DRAFT via API endpoint', ...)`: get id from `createSampleDraftGr`.
- [ ] Navigate to `/inventory/goods-receipts` so the page has CSRF meta tags + same-origin context.
- [ ] Inside `page.evaluate`, read CSRF header name + token from `meta[name="_csrf_header"]` and `meta[name="_csrf"]`. Issue `fetch('/inventory/goods-receipts/{id}', { method: 'DELETE', credentials: 'same-origin', headers: { Accept: 'application/json', [headerName]: token } })`. Return `res.status`.
      ref: e2e-tests/tests/inventory/stock-adjustment.spec.ts:L356-L382 — exact CSRF + DELETE pattern
- [ ] Assert `expect(status).toBeLessThan(300)`.
- [ ] Reload list (`page.reload({ waitUntil: 'domcontentloaded' })`), assert `a[href="/inventory/goods-receipts/edit/{id}"]` count is 0.
- [ ] Note: the delete endpoint returns 200 with `HX-Refresh-Table` header. The test only asserts status — don't assert HX header from `fetch`.
      ref: src/main/java/com/solusi/erp/inventory/goodsreceipt/web/controller/GoodsReceiptController.java:L269-L275 — delete endpoint shape

**Validation criteria:**
- `npx playwright test tests/inventory/goods-receipt.spec.ts -g "Scenario D"` green.
- Pre-deletion DRAFT id no longer in list after reload.

### Task 8: Tag @smoke + finalize + first green run
Apply `@smoke` tag to Scenario A only (matches SA convention — only happy-path
create gets smoke tag). Run the full GR spec at least once via
`run-e2e.ps1`. Update `docs/tests/playwright-e2e-guide.md` coverage table.

**Depends on:** Tasks 4, 5, 6, 7

Steps:
- [ ] Confirm only Scenario A carries `@smoke` (already added in Task 4). Sanity, B, C, D do NOT have `@smoke` because they are not the create-happy-path. Push-to-main CI runs `--grep @smoke` so only A executes there; nightly runs everything.
      ref: .github/workflows/ci-java21.yml:L327-L329 — `--grep @smoke` selection logic
- [ ] Run full e2e suite once via `.\e2e-tests\scripts\run-e2e.ps1` (or `.sh` on Linux). Confirm 56/56 (51 existing + 5 new GR scenarios). If anything flaky, capture error-context.md and update pitfalls catalog before marking task done.
- [ ] Update `docs/tests/playwright-e2e-guide.md` coverage table: add `tests/inventory/goods-receipt.spec.ts` row with scenarios listed.
      ref: docs/tests/playwright-e2e-guide.md — find the "spec coverage" or equivalent table
- [ ] Bump `pom.xml` version 1.7.2 → 1.7.3 (PATCH — additive E2E coverage, no behavior change).
      ref: pom.xml:L13 — version line
      ref: docs/AGENTS.md section 9.A — semantic versioning protocol
- [ ] Commit with conventional message `test(e2e): add goods-receipt smoke flow (5 scenarios)`.

**Validation criteria:**
- `.\e2e-tests\scripts\run-e2e.ps1` green: 56 passed (or 56 passed + N flaky if retries kicked in).
- `npx playwright test --grep @smoke` includes the new GR Scenario A.
- Working tree clean except for the planned files; no test-results/ leakage.

## Out-of-Scope (deferred)

The following are intentionally excluded to keep this plan smoke-only per user direction:
- Multi-line Goods Receipt (only laptop line is exercised; chair line stays unused as filler in PO).
- Serialized GR flow (drawer-serial). Existing SA spec also skips this; pattern can be lifted later.
- Negative cases (insufficient permissions, attempt to edit COMPLETED, attempt to complete with zero lines). These belong in domain unit tests.
- Cross-slice billing-status enrichment shown on view page (`UNBILLED|PARTIAL_BILLED|FULLY_BILLED`). Requires VendorBill seed which is out of scope here.
- HTMX list filtering by `referenceType=PURCHASE_ORDER&referenceId=...` query params on list page.
- RBAC matrix expansion to include GoodsReceipt resource in `tests/auth/rbac.spec.ts`. Can be a separate one-line task in a follow-up plan.
