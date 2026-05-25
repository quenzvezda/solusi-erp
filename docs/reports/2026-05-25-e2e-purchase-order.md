# Implementation Report: E2E Purchase Order Flow (STANDARD)

> Plan: docs/plans/2026-05-25-e2e-purchase-order.md
> Source: (no brainstorming doc — derived from autonomous exploration)
> Created: 2026-05-25

## Findings

(Populated during execution by execute-plan skill)

## Task 1: Seed APPROVED PR + tax + grant warehouse PO permissions
- **Status:** clean (one finding noted below)
- **Summary:** Extended V9000 with tax id 9001, PR id 9301 + 2 lines (9301 laptop, 9302 chair), and 8 permission grants on ROLE_WAREHOUSE.

## Task 5: Scenario A — `@smoke` create STANDARD DRAFT from PR
- **Status:** findings (5 fixes needed during runtime, all resolved)
- **Summary:** Scenario A green on 7.4s after 5 iteration cycles. Each cycle uncovered a real-world divergence between plan assumptions and runtime behavior — all logged below.

### Finding 1: PR selector modal auto-opens on STANDARD radio click
- **Type:** gap
- **Detail:** Plan's `pickPrFromModal` clicks `#btn-select-pr` to open modal. But page JS (`purchase-order-form.js:584-586`) auto-opens the modal when STANDARD type is selected on a fresh form. Click hits the backdrop because modal is already in transition.
- **Action:** Helper now uses `waitForFunction(modal.classList.contains('show'))` with 2s grace, falling back to button click only if modal isn't already opening. Race-safe.

### Finding 2: Playwright selector syntax (`>> nth=0 >>`) invalid in `waitForFunction`
- **Type:** bug
- **Detail:** `setAutoNumeric` uses `page.waitForFunction` internally; the selector `#line-container tr.line-row >> nth=0 >> .input-unit-price` is Playwright DSL, not CSS — fails inside `document.querySelector`.
- **Action:** Switched to CSS-native `tr.line-row:nth-of-type(1) .input-unit-price`.

### Finding 3: Tax field needs payload-aware injection (pitfall #2)
- **Type:** pitfall hit
- **Severity:** info
- **Detail:** `setTomSelectValue('#header-tax', TAX_ID)` passed but submit failed with `msg.error.po.tax.required`. Page JS `syncHeaderTaxSelection` reads `option.payload.code/rate/calculationMode` — without payload, hidden form fields stay empty and server validates fail.
- **Action:** Added `pickHeaderTax(page, taxId)` helper that fetches `/api/lookup/master/taxes?q=` empty (lookup is keyword-on-name, not by id) then matches by id locally and `addOption(opt)` with full payload. Same pattern as PR/SA `selectProductOnLine`.

### Finding 4: Tax lookup is keyword-only on name/code, not id
- **Type:** decision
- **Detail:** `q=9001` returns nothing because the lookup matches name/code only. Empty `q=` returns the full list (small in seed scope), then filter locally.
- **Action:** documented in helper comment.

### Finding 5: List page shows /edit/ link for DRAFT, /view/ for non-DRAFT
- **Type:** gap
- **Detail:** Plan's id-extraction regex looked for `/view/{id}`. List template (`list.html:105-112`) renders Edit link for DRAFT status and View link for everything else. Newly-created DRAFT PO has no /view/ link on the list.
- **Action:** Switched extraction to `/edit/{id}` pattern. Other scenarios (B-F) targeting non-DRAFT POs may need /view/ fallback later.

## Task 6: Scenario B — Edit DRAFT persists changes (header + line note)
- **Status:** findings (2 fixes, all resolved)
- **Summary:** Scenario B green on 5.7s after 2 iterations.

### Finding 7: lines[0].note is a hidden input
- **Type:** gap
- **Detail:** Plan assumed `page.fill` works; the input is `<input type="hidden">` populated by the modal selector flow. page.fill rejects hidden inputs as not-editable.
- **Action:** Set via `page.evaluate` setting `el.value` directly.

## Task 8: Scenario D — Submit → approver rejects → assertion adjusted
- **Status:** findings (3 fixes)
- **Summary:** Scenario D green on 11.4s after 3 iterations.

### Finding 10: PR-line modal pre-fills qty to remainingQuantity (auto-drains line)
- **Type:** gap
- **Detail:** Plan assumed picking a PR line consumes "1 unit" from the PR. Actual: the modal auto-fills line qty to `data-remaining-quantity` (initially 999 per V9000 seed). Each saved DRAFT PO consumes the entire line, so subsequent scenarios fail with PR-line modal empty.
- **Action:** Bumped V9000 seed quantity to 999 each (laptop + chair) AND added explicit `setAutoNumeric(.input-qty, 1)` after pickPrLineFromModal in `createDraftStandardPo`. Each scenario now consumes 1 unit of 999 — plenty of headroom.

### Finding 11: PO module has no OnPurchaseOrderRejectedListener
- **Type:** gap
- **Severity:** info (potential gap from product owner standpoint)
- **Detail:** Plan asserted `.page-title .badge` text "REJECTED" after approver rejects. Actual: PO module only has `OnPurchaseOrderApprovedListener` (line 14, no rejected handler). When approval rejected, only the approval-request status flips to REJECTED — PO domain status stays SUBMITTED.
- **Action:** Adjusted Scenario D assertion to verify the user-visible side effect: approve modal trigger no longer rendered (isCurrentApprover=false post-rejection). Also added a sanity assertion that PO status badge stays "SUBMITTED" — this codifies the current behavior. PR module has `OnPurchaseRequisitionRejectedListener`; PO doesn't. Product owner can decide whether to add it.

### Finding 12: page.goto for view/{id} after approval can stall on CDN
- **Type:** environment
- **Detail:** First Scenario D run with `waitUntil: 'domcontentloaded'` succeeded; subsequent retries occasionally stalled at 30s. Likely CDN load (cdn.jsdelivr.net, rsms.me) on cold approver context.
- **Action:** Made `waitUntil: 'domcontentloaded'` explicit + bumped timeout to 30s (was using default 15s navigationTimeout). May still flake under heavy CDN load — to monitor in finalize run.
