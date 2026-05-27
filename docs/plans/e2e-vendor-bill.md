# Implementation Plan: E2E Vendor Bill (Happy Path)

> Source: (direct request — no brainstorming doc)
> Created: 2026-05-27
> Status: IN_PROGRESS

## Summary

Add Playwright E2E tests for the Vendor Bill module covering the happy-path lifecycle: list, create from completed GR, view detail, confirm, cancel, and delete. Requires seeding accounting schema `VENDOR_BILL` and warmup URLs since these are missing from the current E2E data.

## Tasks

### Task 1: Seed E2E data for Vendor Bill

Add accounting schema, COA accounts, and warmup URLs so the Vendor Bill confirm flow works in the H2 E2E environment.

**Depends on:** none
**Reference:** `src/main/resources/db/migration-h2/V9000__e2e_seed_data.sql` (existing GR seed pattern)

Steps:
- [x] Add COA accounts to V9000 seed: `Accounts Payable` (LIABILITY/CREDIT), `Tax Receivable / Input VAT` (ASSET/DEBIT) — use IDs 9403, 9404
      ref: src/main/resources/db/migration-h2/V9000__e2e_seed_data.sql:L233-L238 — existing COA seed pattern (9401 Inventory, 9402 GR Accrual)
- [x] Add accounting schema for event `VENDOR_BILL` with schema lines: `VB_GRIR_CLEARING_AMT` → 9402 DEBIT, `VB_TAX_AMT` → 9404 DEBIT, `VB_AP_TOTAL` → 9403 CREDIT
      ref: src/main/resources/db/migration-h2/V9000__e2e_seed_data.sql:L240-L248 — existing GOODS_RECEIPT schema pattern
      ref: docs/modules/accountspayable/vendor-bill.md:L176-L189 — VENDOR_BILL journal variables
- [x] Add warmup URLs to `e2e-tests/scripts/warmup-urls.txt`: `/accounts-payable/vendor-bills`, `/accounts-payable/vendor-bills/select-references`
      ref: e2e-tests/scripts/warmup-urls.txt — existing warmup list

**Validation criteria:**
- Server starts cleanly with `--spring.profiles.active=e2e` (no Flyway errors)
- Admin can navigate to `/accounts-payable/vendor-bills` without 403/500

---

### Task 2: Write vendor-bill.spec.ts (happy path)

Create the E2E spec with scenarios covering the full DRAFT lifecycle plus confirm.

**Depends on:** Task 1
**Reference:** `e2e-tests/tests/inventory/goods-receipt.spec.ts` (closest pattern — transactional doc from PO)

**Scenarios:**

| # | Tag | Name | Description |
|---|---|---|---|
| sanity | — | admin can open vendor-bills list | Navigate, assert table visible |
| A | `@smoke` | Create DRAFT from completed GR | Complete a GR → select-references → create form → save → verify DRAFT in detail |
| B | — | Confirm DRAFT transitions to CONFIRMED | Create draft → navigate detail → confirm → assert CONFIRMED badge |
| C | — | Cancel DRAFT transitions to CANCELLED | Create draft → navigate detail → cancel → assert redirect to list |
| D | — | Delete DRAFT via API endpoint | Create draft → DELETE endpoint → verify removed from list |

Steps:
- [ ] Create `e2e-tests/tests/accountspayable/vendor-bill.spec.ts`
- [ ] Import fixtures: `test, expect, storageStatePath` from `../../fixtures/base`
- [ ] Import helpers: `navigateToModule`, `setFlatpickrDate`, `waitForNetworkIdle`
- [ ] Write helper `createCompletedGr(page)` — reuse pattern from goods-receipt.spec.ts (`createSampleDraftGr` + complete via `#btn-complete` + `#confirm-modal-btn-yes`)
      ref: e2e-tests/tests/inventory/goods-receipt.spec.ts:L123-L156 — createSampleDraftGr
      ref: e2e-tests/tests/inventory/goods-receipt.spec.ts:L199-L213 — complete flow (Scenario C)
- [ ] Write helper `createDraftVendorBill(page)`:
  1. Call `createCompletedGr(page)` to get a billable GR
  2. Navigate to `/accounts-payable/vendor-bills/select-references`
  3. Check the first `.js-reference-checkbox` checkbox
  4. Click `#btn-continue-references`
  5. Wait for create form URL (`/accounts-payable/vendor-bills/create`)
  6. Fill `vendorInvoiceNumber` via `page.fill('input[name="vendorInvoiceNumber"]', 'E2E-INV-...')`
  7. Set `billDate` and `dueDate` via `setFlatpickrDate`
  8. Submit form via `[data-ajax-form] button[type="submit"]` or `#vendor-bill-form button[type="submit"]`
  9. Wait for redirect to list URL
  10. Extract new bill ID from list page links
      ref: src/main/resources/templates/accountspayable/vendor-bills/select-references.html — checkbox class `.js-reference-checkbox`, button `#btn-continue-references`
      ref: src/main/resources/templates/accountspayable/vendor-bills/form.html — form id `#vendor-bill-form`, field names
- [ ] Write **sanity test**: navigate to list, assert table visible
- [ ] Write **Scenario A** (`@smoke`): call `createDraftVendorBill`, navigate to detail `/{id}`, assert `.badge` with text `DRAFT`
      ref: src/main/resources/templates/accountspayable/vendor-bills/detail.html:L14-L18 — badge in `.page-title`
- [ ] Write **Scenario B**: create draft → navigate detail → click confirm button (uses `ErpForm.postAction`) → handle confirm modal (`#confirm-modal-btn-yes`) → assert badge `CONFIRMED`
      ref: src/main/resources/templates/accountspayable/vendor-bills/detail.html:L139-L145 — confirm button with `onclick="ErpForm.postAction(this)"`
      ref: docs/tests/playwright-pitfalls.md §4 — Bootstrap modal confirm, NOT native dialog
- [ ] Write **Scenario C**: create draft → navigate detail → click cancel button → handle confirm modal → assert redirect to list
      ref: src/main/resources/templates/accountspayable/vendor-bills/detail.html:L131-L138 — cancel button with `data-redirect-url`
- [ ] Write **Scenario D**: create draft → extract CSRF → `DELETE /accounts-payable/vendor-bills/{id}` via `page.evaluate(fetch)` → reload list → assert row gone
      ref: e2e-tests/tests/inventory/goods-receipt.spec.ts:L215-L239 — delete via API pattern
      ref: docs/tests/playwright-pitfalls.md §1 — use page.evaluate AFTER page.goto (not from about:blank)
- [ ] Set `test.describe.configure({ timeout: 120_000 })` — vendor bill flow is multi-step (create GR + complete + create VB)
- [ ] Use `test.use({ storageState: storageStatePath('admin') })` — admin has all VENDOR-BILL_* permissions

**Key pitfall mitigations:**
- Confirm/cancel uses `ErpForm.postAction` → click `#confirm-modal-btn-yes` (pitfall §4)
- Delete via `page.evaluate(fetch)` only AFTER navigating to list page (pitfall §1)
- Form submit is AJAX (`data-ajax-form`) → wait for redirect URL pattern `/\/accounts-payable\/vendor-bills(\?.*)?$/`
- Lines are pre-filled from GR selection — no need to manually add lines or set qty
- Exchange rate field uses `data-autonumeric="decimal"` but default value from GR is fine for happy path

**Validation criteria:**
- `cd e2e-tests && npx tsc --noEmit` clean
- `npx playwright test tests/accountspayable/vendor-bill.spec.ts --list` shows 5 scenarios
- `npx playwright test tests/accountspayable/vendor-bill.spec.ts` green at least once
- Cold-cache run: `rm -rf .auth/ && npx playwright test tests/accountspayable/vendor-bill.spec.ts` also green
