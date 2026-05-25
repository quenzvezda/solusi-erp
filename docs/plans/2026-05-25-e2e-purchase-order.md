# Implementation Plan: E2E Purchase Order Flow (STANDARD)

> Source: (no brainstorming doc — derived from autonomous exploration of `purchasing.purchaseorder` slice + `docs/modules/procurement/purchase-order.md`)
> Created: 2026-05-25
> Sprint: stabilization continuation; sibling of `2026-05-25-e2e-goods-receipt.md`
> Status: IN_PROGRESS

## Summary

Add a Playwright E2E spec for Purchase Order — STANDARD type only, mirroring
the PR spec's approval shape and the GR spec's modal-selector shape. Coverage
walks the PO lifecycle DRAFT → SUBMITTED → APPROVED → SENT plus DRAFT cancel
and DELETE-via-API. DIRECT type is intentionally out of scope (overlap with
PR/SA pattern; no new permukaan).

## Context (essential references)

- Module spec: `docs/modules/procurement/purchase-order.md` — STANDARD PO derives
  supplier/facility/currency from referenced APPROVED PR; supplier/facility/currency
  locked in UI; line picker is a modal selector that excludes consumed lines.
- Controller URL base: `/purchasing/purchase-orders` (note: `/view/{id}` HAS `/view/` segment, unlike GR).
      ref: src/main/java/com/solusi/erp/purchasing/purchaseorder/web/controller/PurchaseOrderController.java:L52
- Status enum: 10 values (`PurchaseOrderStatus`); `canSubmit/canSend/canCancel/canDelete/canReceive` predicates encode lifecycle gates.
      ref: src/main/java/com/solusi/erp/purchasing/purchaseorder/domain/model/PurchaseOrderStatus.java
- Type enum: `DIRECT, STANDARD` only.
- Form id: `#po-form` (data-ajax-form, redirects to `/purchasing/purchase-orders` on success).
- PO type radios: `#po-type-direct`, `#po-type-standard`.
- PR selector endpoints (GET, modal HTMX fragments):
  - `/selectors/purchase-requisitions?supplierId=...` — pick PR header
  - `/selectors/purchase-requisition-lines?prId=...&excludePrLineIds=...` — pick PR line(s)
- Lifecycle action endpoints (POST):
  - `POST /create` — JSON, returns DetailResponse
  - `POST /edit/{id}` — JSON, returns DetailResponse
  - `DELETE /{id}` — HTMX response
  - `POST /{id}/submit?approverId={partyId}` — initiates approval flow
  - `POST /{id}/send` — APPROVED → SENT (PO_SEND permission)
  - `POST /{id}/cancel` — DRAFT/SUBMITTED → CANCELLED (PO_UPDATE permission)
- Approval mechanism: same as PR — uses `/common/approval/{id}/process` endpoint with `APPROVE_AND_FINISH` or `REJECTED` action. Approver decision wires back to PO via `OnPurchaseOrderApprovedListener` infrastructure listener.
      ref: src/main/java/com/solusi/erp/purchasing/purchaseorder/infrastructure/listener/OnPurchaseOrderApprovedListener.java
- Permissions to grant: `PO_{READ,CREATE,UPDATE,DELETE,SUBMIT,SEND}` plus `LOOKUP_PURCHASING` (for supplier/PR lookups) + `LOOKUP_INVENTORY` (already granted to warehouse via SA setup).
- Status badge selector on view page: `.page-title .badge` (badge sits inside the page-title heading; two badges actually — type Direct/Standard + status). For status assertion, use text matcher: `page.locator('.page-title .badge', { hasText: 'DRAFT' })`.
      ref: src/main/resources/templates/purchasing/purchase-orders/view.html:L14-L19
- Approval sidebar buttons on view page (visible when `${isCurrentApprover}` true) — same shape as PR view: `button[onclick="ApprovalUI.openApproveFinishModal()"]` and reject equivalent.

## Critical Pitfalls Already Catalogued

These apply directly:
1. **Status badge selector**: `.page-title .badge` (NOT `.page-header .badge`). Two badges in page-title — type + status. Must use `hasText` matcher.
2. **Approval helper**: Reuse the PR spec's `processApproval(approverPage, approvalRequestId, 'APPROVE_AND_FINISH', notes)` pattern verbatim — `/common/approval/{id}/process` is generic.
      ref: e2e-tests/tests/procurement/purchase-requisition.spec.ts — `processApproval` helper
3. **Submit endpoint takes approverId**: PO submit is `POST /{id}/submit?approverId={partyId}`, not a body field. Test must set query param. Use `approver1` party id (resolved from V9000 seed).
4. **Modal selector pattern**: Same as GR — open modal via trigger, click row by code, wait class-based (no `toBeHidden`).
5. **`page.evaluate(fetch)` from `about:blank`**: Always navigate first.

## Cross-Slice Dependencies

- **PR seed needed**: V9000 currently does NOT seed any APPROVED PR. PR E2E spec creates PRs at runtime, but those don't survive into PO test scope. Must add fixed APPROVED PR + lines in V9000.
- **Supplier/facility/currency**: Already seeded (`@p_sup1`, facility 9101, IDR @cur_idr).
- **Tax**: Need a fixed Tax seed (e.g., PPN 0% / Non Tax). Check V9000 — if absent, add as part of seed task.
- **Approver**: `approver1` party id must match `current_approver_id` of approval requests. Confirm from existing PR spec setup that the binding works.

## Tasks

### Task 1: Seed APPROVED PR + tax + grant warehouse PO permissions [x]
Extend `V9000__e2e_seed_data.sql` to insert one APPROVED PR (fixed id 9301 with
2 lines), insert a fixed tax row if not seeded (PPN 0% — id 9001), and grant the
warehouse role the 6 PO permissions plus `LOOKUP_PURCHASING`.

**Depends on:** (none — first task; all later tasks consume seeded ids)
**Reference module:** existing V9000 PR/SA seed blocks

Steps:
- [ ] Read current V9000 to confirm: (1) does it already seed any PR? (search `pur_purchase_requisitions`); (2) does it seed any tax (search `master_taxes` or `taxes` table); (3) what permissions has the warehouse role been granted (search `@role_warehouse_id`).
      ref: src/main/resources/db/migration-h2/V9000__e2e_seed_data.sql — locate seed sections
- [ ] Read PR domain + persistence to learn the PR table schema (table name, status column values, lines table, FK columns). Determine which `status` literal indicates APPROVED.
      ref: src/main/java/com/solusi/erp/purchasing/purchaserequisition/infrastructure/persistence/PurchaseRequisitionEntity.java — column mapping
      ref: src/main/java/com/solusi/erp/purchasing/purchaserequisition/domain/model/PurchaseRequisitionStatus.java — status enum
- [ ] Insert PR header into `pur_purchase_requisitions` with explicit id 9301: status=APPROVED, requester=@p_employee1, supplier=@p_sup1, currency=@cur_idr, request_date='2026-05-19', code='E2E-PR-9301'. Skip optional fields.
      ref: src/main/resources/db/migration/V46__Add_Purchasing_Module.sql — column list for pur_purchase_requisitions
- [ ] Insert 2 PR lines (ids 9301, 9302): laptop qty 5 @ 8500000, chair qty 4 @ 1500000. Set `consumed_quantity=0` so all qty is open. Use base UoM 9001.
      ref: src/main/resources/db/migration/V46__Add_Purchasing_Module.sql — column list for pur_purchase_requisition_lines
- [ ] Check if a non-zero or zero tax exists in the master tax table. If absent in V9000, insert a fixed `master_taxes` (or whatever table name) row with id 9001: code='E2E-TAX-0', name='PPN 0% (E2E)', rate=0, calculation_mode='EXCLUSIVE'.
      ref: src/main/java/com/solusi/erp/master/tax/infrastructure/persistence/TaxEntity.java — table name + columns
- [ ] Grant warehouse role 6 PO permissions: `PO_READ`, `PO_CREATE`, `PO_UPDATE`, `PO_DELETE`, `PO_SUBMIT`, `PO_SEND`. Use `IN (...)` exact match — `PO_` prefix would also catch `POPUP_*` if any exist (search current permissions table).
      ref: src/main/resources/db/migration/V46__Add_Purchasing_Module.sql — PO permission seed names (canonical source)
      ref: docs/AGENTS.md section 7 — SQL Wildcard Safety
- [ ] Grant warehouse role `LOOKUP_PURCHASING` if not already in the role.
- [ ] Build + start server with profile e2e. Manual probe: `curl -b cookies.txt http://localhost:18080/purchasing/purchase-orders/selectors/purchase-requisitions?supplierId=<p_sup1_id>` returns HTML containing `E2E-PR-9301`.

**Validation criteria:**
- `bash scripts/check-migration-parity.sh` passes (or N/A — V9000 lives only on H2 side; the parity script ignores V9000+).
- After server start, an authenticated curl as warehouse1 to `/purchasing/purchase-orders/selectors/purchase-requisitions` returns the seeded PR row.
- Curl to `/purchasing/purchase-orders/selectors/purchase-requisition-lines?prId=9301` returns 2 lines.

### Task 2: Spec scaffold + warehouse1 sanity scenario [x]
Create `e2e-tests/tests/procurement/purchase-order.spec.ts`. File-level
describe, storage state for warehouse1, fixed seed-id constants, sanity scenario
that opens the list page.

**Depends on:** Task 1
**Reference module:** `e2e-tests/tests/procurement/purchase-requisition.spec.ts`

Steps:
- [ ] Create file `e2e-tests/tests/procurement/purchase-order.spec.ts`. Imports: `test, expect, storageStatePath` from `../../fixtures/base`, `navigateToModule` from `../../helpers/navigation`. Do NOT import `selectTomSelect` (broken).
      ref: e2e-tests/tests/procurement/purchase-requisition.spec.ts:L1-L20 — import block + base fixture
      ref: docs/tests/playwright-pitfalls.md section 3 — selectTomSelect broken signature
- [ ] Define seed-id constants from V9000 (Task 1 outputs): `const PR_ID = '9301'`, `const PR_LINE_LAPTOP_ID = '9301'`, `const PR_LINE_CHAIR_ID = '9302'`, `const FACILITY_ID = '9101'`, `const TAX_ID = '9001'`. Plus dynamic ids resolved at runtime: supplier party id, approver1 party id (use existing `resolveSeedIds` helper from PR spec, copy into this spec).
      ref: e2e-tests/tests/procurement/purchase-requisition.spec.ts — `resolveSeedIds` factoring; copy verbatim and trim to fields used here
- [ ] Wrap with `test.describe('Purchase Order flow', () => { test.use({ storageState: storageStatePath('warehouse1') }); ... })`. Tag at file level matches PR convention; per-scenario `@smoke` only on Scenario A.
- [ ] Add sanity scenario `test('sanity: warehouse1 can open purchase-orders list', ...)`: navigate `/purchasing/purchase-orders`, assert URL regex match, assert table visible. Proves seed grants from Task 1 worked.
      ref: e2e-tests/tests/procurement/purchase-requisition.spec.ts — sanity scenario shape (last test in describe)
- [ ] Run `cd e2e-tests && npx tsc --noEmit` clean.
- [ ] Run `npx playwright test tests/procurement/purchase-order.spec.ts -g "sanity"`. Confirm green.

**Validation criteria:**
- `npx tsc --noEmit` clean.
- `npx playwright test tests/procurement/purchase-order.spec.ts --list` shows 1 scenario.
- Sanity scenario runs green.

### Task 3: Helpers — pickPrFromModal + pickPrLineFromModal + processApproval reuse [x]
Three local helpers: (a) open PR selector modal, pick PR by code; (b) open PR
line selector modal, pick line by product code; (c) factor `processApproval`
identical to PR spec's helper. Place all in spec file (private). Reuse class-based
modal close pattern.

**Depends on:** Task 2
**Reference module:** PR spec `processApproval` + GR plan `pickPoLineFromModalSelector`

Steps:
- [ ] Read `templates/purchasing/purchase-orders/fragments/pr-selector-modal.html` and `pr-line-selector-modal.html` to find: row data attributes, the trigger button id on `form.html`, the modal outer id (used by HTMX swap), and the apply/select button selector.
      ref: src/main/resources/templates/purchasing/purchase-orders/fragments/pr-selector-modal.html
      ref: src/main/resources/templates/purchasing/purchase-orders/fragments/pr-line-selector-modal.html
- [ ] Read page-specific JS that wires the PR selector to the form. Identify: which fields get auto-filled (supplierId, facilityId, currencyId), whether they get locked via `disabled` or `readonly`, what dataset keys are read from row.
      ref: src/main/resources/static/js/purchasing/purchase-order/purchase-order-form.js — search for `pr-selector` and `pr-line-selector` consumers
- [ ] Implement `async function pickPrFromModal(page: Page, prCode: string): Promise<void>` — click PR-picker trigger, wait modal show, click row by `tr[data-code="..."]` or text matcher, click apply, wait class-based close (no `.show`/`.hiding`/`.showing`), assert auto-filled supplier field has expected text.
      ref: e2e-tests/tests/inventory/stock-adjustment.spec.ts:L100-L120 — class-based modal close pattern template
- [ ] Implement `async function pickPrLineFromModal(page: Page, productCode: string): Promise<void>` — click "Add Line" (which opens modal in STANDARD mode), wait modal, pick row by product code, click apply, wait close, assert new `tr.line-row` count incremented.
      ref: docs/modules/procurement/purchase-order.md section 4 "STANDARD Line Selector Modal" — multi-select semantics
- [ ] Copy `processApproval` from PR spec verbatim. The endpoint `/common/approval/{id}/process` is generic — only the `referenceType=PURCHASE_ORDER` differs at the lookup step (handled by `findApprovalRequestByReferenceUseCase`).
      ref: e2e-tests/tests/procurement/purchase-requisition.spec.ts — `processApproval` (around line 157-192)
- [ ] tsc --noEmit clean.

**Validation criteria:**
- `npx tsc --noEmit` clean after helpers added.
- Drop a temporary `test.only(...)` exercising pickPrFromModal in isolation; confirm it adds the expected supplier text + locks the supplier autocomplete. Remove `test.only` before commit.

### Task 4: Helper — createDraftStandardPo [x]
Factor a `createDraftStandardPo(page, seed)` that: opens create form, picks
STANDARD type, picks PR via modal, picks one PR line via modal, sets unit price
(AutoNumeric), picks tax via autocomplete, saves, captures new PO id from list.
Returns id.

**Depends on:** Task 3
**Reference module:** PR spec `createDraftPr` factory pattern

Steps:
- [ ] Navigate `/purchasing/purchase-orders/create` via `navigateToModule`. Assert `#po-form` visible.
      ref: src/main/resources/templates/purchasing/purchase-orders/form.html:L33 — form id
- [ ] Click `#po-type-standard` radio. Assert that PR-picker trigger button is visible. Verify default fields: orderDate today, exchangeRate 1, paymentTermDays 30 (controller defaults).
      ref: src/main/java/com/solusi/erp/purchasing/purchaseorder/web/controller/PurchaseOrderController.java:L96-L106 — showCreateForm defaults
- [ ] Call `pickPrFromModal(page, 'E2E-PR-9301')`. Verify supplier/facility/currency autocompletes are now filled and disabled.
- [ ] Pick tax via header tax autocomplete. Use `setTomSelectValue(page, '#header-tax', TAX_ID)` (no payload needed — page JS reads taxRate/taxName from another path).
      ref: docs/spec/autocomplete-generic.md — header tax pattern
      ref: docs/modules/procurement/purchase-order.md section 4 "Header Tax Selector" — mandatory field
- [ ] Click "Add Line" (which is the line-selector trigger in STANDARD mode). Call `pickPrLineFromModal(page, 'E2E-PRD-LAPTOP')`.
- [ ] Set unit price on the new line (qty defaults from PR remaining). Use AutoNumeric helper: `await setAutoNumeric(page, '#line-container tr.line-row >> nth=0 >> .input-price', 8500000)`.
      ref: e2e-tests/helpers/autonumeric.ts — setAutoNumeric helper
- [ ] Submit: `Promise.all([page.waitForURL(/\/purchasing\/purchase-orders(\?.*)?$/, { timeout: 15_000, waitUntil: 'domcontentloaded' }), page.locator('#po-form button[type="submit"]').first().click()])`.
- [ ] Capture new PO id from list page using highest-id-from-`a[href*="/purchasing/purchase-orders/view/"]` pattern.
      ref: e2e-tests/tests/inventory/stock-adjustment.spec.ts — newId capture pattern (adapt URL prefix)
- [ ] Return `{ poId, prId: 9301 }` for downstream scenarios.

**Validation criteria:**
- Helper compiles, returns a numeric id > 0.
- Manual trace via `test.only`: server log shows `POST /purchasing/purchase-orders/create` returned 201; the new PO has `prId=9301`, `poType=STANDARD`, status DRAFT.

### Task 5: Scenario A — `@smoke` create STANDARD DRAFT from PR
Happy-path create: STANDARD type → PR modal → line modal → save → verify
DRAFT badge + supplier locked + prLineId persisted (assert via API GET).

**Depends on:** Task 4
**Reference module:** PR Scenario A pattern

Steps:
- [ ] Add `test('@smoke Scenario A — create STANDARD DRAFT from PR', ...)`. Set `test.setTimeout(120_000)` to match PR happy-path timeout (approval contexts can be slow).
- [ ] Navigate to `/purchasing/purchase-orders` first so subsequent fetch() calls have a same-origin base URL.
      ref: e2e-tests/tests/procurement/purchase-requisition.spec.ts:L227-L235 — same-origin landing pattern
- [ ] Resolve seed dynamic ids (supplier party id, approver1 party id) via `resolveSeedIds`. Even though Scenario A doesn't approve, resolving here proves seed wiring before later scenarios.
- [ ] Call `const { poId } = await createDraftStandardPo(page, seed)`. Expect `poId > 0`.
- [ ] Navigate to `/purchasing/purchase-orders/view/{poId}` (note: HAS `/view/` segment, unlike GR).
- [ ] Assert status badge: `await expect(page.locator('.page-title .badge', { hasText: 'DRAFT' })).toBeVisible({ timeout: 10_000 })`.
- [ ] Assert type badge: `await expect(page.locator('.page-title .badge', { hasText: /Standard/i })).toBeVisible()`.
- [ ] Verify line persistence via API: `await page.request.get('/purchasing/purchase-orders/view/' + poId)` is overkill — instead, navigate to edit page, read `[name="lines[0].prLineId"]` value, assert equals `'9301'` (laptop PR line id).
      ref: src/main/java/com/solusi/erp/purchasing/purchaseorder/web/dto/PurchaseOrderLineRequest.java — has `prLineId` field
- [ ] Run `npx playwright test tests/procurement/purchase-order.spec.ts -g "Scenario A"`. Confirm green.

**Validation criteria:**
- Scenario A green.
- Server log shows `POST /purchasing/purchase-orders/create` 201; payload includes `poType=STANDARD`, `prId=9301`, lines with `prLineId=9301`.
- DB query (manual via H2 console if needed) shows `pur_purchase_order_lines.pr_line_id = 9301` for the laptop line.

### Task 6: Scenario B — Edit DRAFT persists changes (header + line note)
Get id from `createDraftStandardPo`, navigate to edit page, change `paymentTermDays`
30 → 45 and a line note, save, reopen, assert persisted.

**Depends on:** Task 5
**Reference module:** SA Scenario B + PR Scenario C edit pattern

Steps:
- [ ] Add `test('Scenario B — edit DRAFT persists changes', ...)`. `test.setTimeout(60_000)`.
- [ ] Navigate `/purchasing/purchase-orders` for same-origin base. Resolve seed. Call `createDraftStandardPo`.
- [ ] Navigate `/purchasing/purchase-orders/edit/{poId}`. Assert `#po-form` visible. Confirm STANDARD radio is checked + disabled (per "Edit Header Parity" spec rule — fields locked but visually identical to create form).
      ref: docs/modules/procurement/purchase-order.md section 4 "Edit Header Parity"
- [ ] Change paymentTermDays input value 30 → 45. Use `page.fill` since this is a plain numeric input.
- [ ] Update line[0] note via `page.fill('input[name="lines[0].note"]', 'Edited via E2E')`.
- [ ] Submit form, wait for redirect to list.
- [ ] Reopen edit page. Assert paymentTermDays === '45' and line note === 'Edited via E2E' via `page.evaluate` reads on `input[name="paymentTermDays"]` and `input[name="lines[0].note"]`.
- [ ] Run `npx playwright test tests/procurement/purchase-order.spec.ts -g "Scenario B"` green.

**Validation criteria:**
- Scenario B green.
- DB row in `pur_purchase_orders` shows `payment_term_days=45` for the test PO id; line note column = 'Edited via E2E'.

### Task 7: Scenario C — Submit → approver approves → SENT
Get id, submit via `POST /{id}/submit?approverId={approver1PartyId}`. Switch
context to approver1, call `processApproval(...APPROVE_AND_FINISH...)`. Reopen
PO view, assert APPROVED badge. Switch back to warehouse1, click "Kirim ke
Supplier" (or POST `/{id}/send`), assert SENT badge.

**Depends on:** Task 5
**Reference module:** PR Scenario A approval flow

Steps:
- [ ] Add `test('Scenario C — submit then approve then send', ...)`. `test.setTimeout(120_000)`.
- [ ] Navigate `/purchasing/purchase-orders` for same-origin base. Resolve seed. Call `createDraftStandardPo`.
- [ ] Submit via `page.evaluate(fetch('/purchasing/purchase-orders/{id}/submit?approverId={approver1PartyId}', { method: 'POST', headers: { CSRF... } }))`. Assert status < 400.
      ref: e2e-tests/tests/procurement/purchase-requisition.spec.ts — `submitPrForApproval` helper for fetch+CSRF pattern; PO submit uses query param `approverId`, not request body
      ref: src/main/java/com/solusi/erp/purchasing/purchaseorder/web/controller/PurchaseOrderController.java:L200-L210 — submit endpoint signature
- [ ] Verify status: navigate to view page, assert badge "SUBMITTED".
- [ ] Open new browser context with approver1 storage state. Navigate `/purchasing/purchase-orders/view/{poId}`. Read `cur-approval-req-id` hidden field for approvalRequestId.
      ref: e2e-tests/tests/procurement/purchase-requisition.spec.ts:L274-L278 — approvalRequestId hidden field read
- [ ] Call `processApproval(approverPage, approvalRequestId, 'APPROVE_AND_FINISH', 'E2E approve')`. Assert resp.status < 400. Note: PO approval requires signature like PR — same `signatureBase64` mechanism via `processApproval` helper.
- [ ] Reload approver page on view URL. Assert badge "APPROVED" (PR proves listener fires; PO has `OnPurchaseOrderApprovedListener` that flips PO status to APPROVED).
      ref: src/main/java/com/solusi/erp/purchasing/purchaseorder/infrastructure/listener/OnPurchaseOrderApprovedListener.java
- [ ] Switch back to warehouse1 page. Reload `/view/{poId}`. Click `button[onclick*="ErpAction.confirmAndSubmit"][data-confirm-message]` for "Kirim ke Supplier" (or use POST `/{id}/send` directly via fetch). Click `#confirm-modal-btn-yes` if button uses ErpAction modal.
      ref: src/main/resources/templates/purchasing/purchase-orders/view.html:L56 — `data-confirm-message="#{msg.confirm.po.send}"`
- [ ] Wait for reload, assert badge "SENT".
- [ ] Run `npx playwright test tests/procurement/purchase-order.spec.ts -g "Scenario C"` green.

**Validation criteria:**
- Scenario C green.
- Server log shows successful POST submit + `/common/approval/{id}/process` 200 + POST `{id}/send` 200.
- Final PO state in DB: `status=SENT`.

### Task 8: Scenario D — Submit → approver rejects → REJECTED
Get id, submit. Switch to approver, `processApproval(...REJECTED...)`. Assert
REJECTED badge on PO view. Note: PO has no resubmit-after-reject flow per
status enum — test ends after rejection assertion.

**Depends on:** Task 7 (helpers + submit pattern reuse)
**Reference module:** PR Scenario B reject pattern

Steps:
- [ ] Add `test('Scenario D — submit then reject', ...)`. `test.setTimeout(120_000)`.
- [ ] Setup: same-origin landing, resolveSeed, createDraftStandardPo, submit via fetch (mirror Task 7 submit step).
- [ ] Open approver context. Read approvalRequestId. Call `processApproval(approverPage, approvalRequestId, 'REJECTED', 'Estimasi terlalu tinggi.')`. Assert status < 400.
- [ ] Reload `/view/{poId}` in approver context. Assert badge "REJECTED".
      ref: src/main/java/com/solusi/erp/purchasing/purchaseorder/infrastructure/listener/OnPurchaseOrderApprovedListener.java — confirms listener handles both APPROVED and REJECTED branches
- [ ] Assert no further action visible: `await expect(approverPage.locator('button[onclick*="ApprovalUI.openApproveFinishModal"]')).toHaveCount(0)`. PR Scenario B has identical assertion.
- [ ] Run `npx playwright test tests/procurement/purchase-order.spec.ts -g "Scenario D"` green.

**Validation criteria:**
- Scenario D green.
- DB row: `status=REJECTED`. Approval request state: REJECTED.

### Task 9: Scenario E — Cancel DRAFT
Get id, click cancel button on edit form (or POST `/{id}/cancel`), assert
CANCELLED badge on view.

**Depends on:** Task 5

Steps:
- [ ] Add `test('Scenario E — cancel DRAFT', ...)`. `test.setTimeout(60_000)`.
- [ ] Setup: same-origin landing, resolveSeed, createDraftStandardPo.
- [ ] Cancel via fetch with CSRF: `POST /purchasing/purchase-orders/{id}/cancel`. Assert status < 400. (Form button could also be clicked, but API path is more deterministic and `PO_UPDATE` permission already granted.)
      ref: src/main/java/com/solusi/erp/purchasing/purchaseorder/web/controller/PurchaseOrderController.java:L222-L230 — cancel endpoint
- [ ] Navigate to `/view/{poId}`. Assert badge "CANCELLED".
- [ ] Run `npx playwright test tests/procurement/purchase-order.spec.ts -g "Scenario E"` green.

**Validation criteria:**
- Scenario E green.

### Task 10: Scenario F — Delete DRAFT via API
CSRF + DELETE pattern, mirror SA Scenario E and GR Scenario D.

**Depends on:** Task 5

Steps:
- [ ] Add `test('Scenario F — delete DRAFT via API endpoint', ...)`.
- [ ] Setup: createDraftStandardPo. Navigate `/purchasing/purchase-orders` for CSRF carrier.
- [ ] In `page.evaluate`, fetch `DELETE /purchasing/purchase-orders/{id}` with CSRF headers. Assert status < 300.
      ref: e2e-tests/tests/inventory/stock-adjustment.spec.ts — Scenario E DELETE pattern (verbatim adapt URL)
- [ ] Reload list, assert `a[href="/purchasing/purchase-orders/view/{id}"]` count is 0.
- [ ] Run `npx playwright test tests/procurement/purchase-order.spec.ts -g "Scenario F"` green.

**Validation criteria:**
- Scenario F green.

### Task 11: Tag @smoke + finalize + first green run
Confirm only Scenario A carries `@smoke`. Run full suite, target 62/62 (51 + 5
GR if GR plan ran first + 6 PO; if GR not yet implemented, 57/57). Update
coverage doc. Bump pom.xml. Commit.

**Depends on:** Tasks 5-10

Steps:
- [ ] Confirm only Scenario A has `@smoke`. Sanity, B, C, D, E, F do NOT — Scenario A as create-happy-path matches the project convention (PR Scenario A is also the only `@smoke` in PR spec).
      ref: .github/workflows/ci-java21.yml:L327-L329 — `--grep @smoke` selection on push to main
- [ ] Run full e2e suite once via `.\e2e-tests\scripts\run-e2e.ps1` (or `.sh`). Expected: 51 baseline + however many GR scenarios are in tree + 7 PO scenarios (6 numbered + sanity). Confirm all green; if any flaky, capture error-context.md and update `docs/tests/playwright-pitfalls.md` BEFORE marking task done.
- [ ] Update `docs/tests/playwright-e2e-guide.md` coverage table: add `tests/procurement/purchase-order.spec.ts` row.
- [ ] Bump `pom.xml` version per AGENTS.md section 9.A. PATCH if no other behavior changes since last bump (1.7.2 or 1.7.3 depending on order with GR plan).
      ref: pom.xml:L13 — version line
- [ ] Commit with conventional message `test(e2e): add purchase-order STANDARD smoke flow (7 scenarios)`.

**Validation criteria:**
- `.\e2e-tests\scripts\run-e2e.ps1` green (full suite passing or only retry-recovered flakes).
- `npx playwright test --grep @smoke` includes the new PO Scenario A.
- Working tree clean except planned files; no test-results/ leakage.

## Out-of-Scope (deferred)

The following are intentionally excluded:
- **DIRECT type PO** — no new permukaan vs PR/SA pattern; manual supplier+product autocomplete already covered there. Add later as 1 alt-path scenario if needed.
- **Multi-line PO** with both laptop + chair lines exercised together (would catch multi-line tax calc edge cases) — domain unit tests in `PurchaseOrderTest.java` already cover.
- **PR auto-fill of unit price from SPL** — interesting but adds complexity; SPL already seeded but the SPL→PO autofill path has been covered manually elsewhere.
- **Edit DRAFT line addition via PR Line modal** (the "STANDARD Edit Line Expansion" rule) — adds significant test surface; defer.
- **Cancel SUBMITTED PO** — currently only DRAFT cancel is exercised; SUBMITTED cancel is the same code path on the backend.
- **PARTIALLY_RECEIVED / FULLY_RECEIVED / BILLED / CLOSED** transitions — these are GR/VB-driven and out of PO E2E concern.
- **RBAC matrix expansion** to include PurchaseOrder resource in `tests/auth/rbac.spec.ts` — separate one-line task.
- **Audit field assertions** (createdBy, updatedBy, version) — domain/repository layer responsibility.
- **DELETE on REJECTED PO** — domain model rejects this; no E2E coverage needed since the controller's `@PreAuthorize` + status check catch it.
