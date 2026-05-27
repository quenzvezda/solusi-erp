# Implementation Plan: E2E Vendor Payment (Happy Path)

> Source: (direct request — no brainstorming doc)
> Created: 2026-05-27
> Status: IN_PROGRESS

## Summary

Add Playwright E2E tests for the Vendor Payment module covering the happy-path lifecycle: list, create from confirmed Vendor Bill, view detail, confirm, cancel, and delete. Requires seeding bank account, additional COA accounts, and VENDOR_BILL accounting schema since the prerequisite chain (GR → VB → VP) needs all of these to function in the E2E H2 environment.

## Prerequisites Chain

```text
PO (SENT, seeded 9201)
  → GR (create + complete)
    → VB (create from GR + confirm)
      → VP (create allocating to VB)
```

## Tasks

### Task 1: Seed E2E data for Vendor Payment

Add COA accounts, bank account, VENDOR_BILL accounting schema, and ensure VENDOR_PAYMENT schema works in the E2E H2 environment. Also add warmup URLs.

**Depends on:** none
**Reference:** `src/main/resources/db/migration-h2/V9000__e2e_seed_data.sql`

**Current E2E seed state:**
- COA 9401 (code `1130`) = E2E Inventory (ASSET/DEBIT) ✓
- COA 9402 (code `2110`) = E2E GR Accrual (LIABILITY/CREDIT) ✓ — also used as AP by V62
- COA for Bank (`1120`) = **MISSING** — needed for VP_BANK_OUT_AMT
- COA for FX Loss (`5140`) = **MISSING** — needed for VP_FX_LOSS_AMT (optional for happy path)
- COA for FX Gain (`4240`) = **MISSING** — needed for VP_FX_GAIN_AMT (optional for happy path)
- Bank account with `currency_id` + `coa_id` = **MISSING**
- VENDOR_BILL accounting schema = **MISSING** — needed to confirm VB
- VENDOR_PAYMENT accounting schema = exists in V62 but schema lines may be empty if COA codes don't exist

Steps:
- [x] Add COA accounts to V9000 seed:
  - `9403` code `1120` 'E2E Bank' (ASSET/DEBIT) — for VP bank out
  - `9404` code `2120` 'E2E Accounts Payable' (LIABILITY/CREDIT) — for VB AP posting
  - `9405` code `1140` 'E2E Tax Receivable' (ASSET/DEBIT) — for VB Input VAT
      ref: src/main/resources/db/migration-h2/V9000__e2e_seed_data.sql:L233-L238 — existing COA seed pattern
- [x] Add VENDOR_BILL accounting schema (id 9402) with schema lines:
  - `VB_GRIR_CLEARING_AMT` → 9402 (GR/IR Clearing, code 2110) DEBIT
  - `VB_TAX_AMT` → 9405 (Tax Receivable) DEBIT
  - `VB_AP_TOTAL` → 9404 (Accounts Payable) CREDIT
      ref: docs/modules/accountspayable/vendor-bill.md:L176-L189 — VENDOR_BILL journal variables
- [x] Add VENDOR_PAYMENT accounting schema (id 9403) with schema lines:
  - `VP_AP_AMT` → 9404 (Accounts Payable) DEBIT
  - `VP_BANK_OUT_AMT` → 9403 (Bank) CREDIT
  - `VP_FX_LOSS_AMT` → 9403 (Bank) DEBIT (placeholder, won't fire in happy path with rate=1)
  - `VP_FX_GAIN_AMT` → 9403 (Bank) CREDIT (placeholder, won't fire in happy path with rate=1)
      ref: src/main/resources/db/migration-h2/V62__Vendor_Payment_Accounting_Schema.sql — production VP schema
- [x] Add bank account seed (id 9501):
  - `code`: 'E2E-BA-001'
  - `bank_name`: 'E2E Bank'
  - `account_name`: 'E2E Operational'
  - `account_no`: '1234567890'
  - `account_type`: 'BANK_TRANSFER'
  - `currency_id`: @cur_idr
  - `coa_id`: 9403 (E2E Bank COA)
  - `party_id`: use existing E2E party
  - `city_id`: use existing E2E geographic
      ref: src/main/resources/db/migration-h2/V16__Master_Bank_Account.sql — bank_accounts table structure
      ref: src/main/resources/db/migration-h2/V61__Bank_Account_Refactor_For_Payment.sql — currency_id + coa_id columns
- [x] Add warmup URLs to `e2e-tests/scripts/warmup-urls.txt`:
  - `/accounts-payable/vendor-bills`
  - `/accounts-payable/vendor-bills/select-references`
  - `/accounts-payable/vendor-payments`
  - `/accounts-payable/vendor-payments/create`
      ref: e2e-tests/scripts/warmup-urls.txt — existing warmup list

**Validation criteria:**
- Server starts cleanly with `--spring.profiles.active=e2e` (no Flyway errors)
- Admin can navigate to `/accounts-payable/vendor-payments` without 403/500
- Admin can navigate to `/accounts-payable/vendor-payments/create` and see the form

---

### Task 2: Write vendor-payment.spec.ts (happy path)

Create the E2E spec with scenarios covering the full DRAFT lifecycle plus confirm. The spec builds the full prerequisite chain (GR → VB → VP) in helper functions.

**Depends on:** Task 1
**Reference:** `e2e-tests/tests/inventory/goods-receipt.spec.ts` (GR create+complete pattern)

**Scenarios:**

| # | Tag | Name | Description |
|---|---|---|---|
| sanity | — | admin can open vendor-payments list | Navigate, assert table visible |
| A | `@smoke` | Create DRAFT vendor payment | Complete GR → confirm VB → create VP with allocation → verify DRAFT in detail |
| B | — | Confirm DRAFT transitions to CONFIRMED | Create draft → detail → confirm → assert CONFIRMED badge |
| C | — | Cancel DRAFT transitions to CANCELLED | Create draft → detail → cancel → assert CANCELLED badge |
| D | — | Delete DRAFT via API endpoint | Create draft → DELETE endpoint → verify removed from list |

Steps:
- [x] Create `e2e-tests/tests/accountspayable/vendor-payment.spec.ts`
- [x] Import fixtures: `test, expect, storageStatePath` from `../../fixtures/base`
- [x] Import helpers: `navigateToModule`, `setFlatpickrDate`, `setAutoNumeric`, `waitForNetworkIdle`, `setTomSelectValue`
- [x] Write helper `createCompletedGr(page)` — create GR from PO 9201, complete it, return GR id
      ref: e2e-tests/tests/inventory/goods-receipt.spec.ts:L123-L213 — createSampleDraftGr + complete pattern
- [x] Write helper `createConfirmedVendorBill(page)` — uses completed GR:
  1. Navigate to `/accounts-payable/vendor-bills/select-references`
  2. Check first `.js-reference-checkbox`
  3. Click `#btn-continue-references`
  4. Wait for create form URL
  5. Fill `vendorInvoiceNumber` with unique value
  6. Set `billDate` and `dueDate` via `setFlatpickrDate`
  7. Submit form (AJAX, wait redirect to list)
  8. Extract new VB id from list
  9. Navigate to detail `/{id}`
  10. Click confirm button → `#confirm-modal-btn-yes`
  11. Assert CONFIRMED badge
  12. Return VB id
      ref: src/main/resources/templates/accountspayable/vendor-bills/select-references.html — checkbox `.js-reference-checkbox`, button `#btn-continue-references`
      ref: src/main/resources/templates/accountspayable/vendor-bills/detail.html:L139-L145 — confirm button uses `ErpForm.postAction`
      ref: docs/tests/playwright-pitfalls.md §4 — use `#confirm-modal-btn-yes`, NOT `page.on('dialog')`
- [x] Write helper `createDraftVendorPayment(page)`:
  1. Call `createCompletedGr(page)` + `createConfirmedVendorBill(page)`
  2. Navigate to `/accounts-payable/vendor-payments/create`
  3. Set vendor via TomSelect `#vp-vendor` (use `setTomSelectValue` with supplier id from seed)
  4. Wait for allocation lines to load (payable bills API response)
  5. Set bank account: click `#btn-select-bank-account` → wait modal visible → click `.js-bank-account-select` in modal
  6. Set `paymentAmount` via AutoNumeric (match the VB total)
  7. Set `paidAmount` on allocation line via AutoNumeric (match payment amount)
  8. Set `paymentDate` via `setFlatpickrDate`
  9. Submit form via `#vendor-payment-form button[type="submit"]`
  10. Wait redirect to list
  11. Extract new VP id
  12. Return VP id
      ref: src/main/resources/templates/accountspayable/vendor-payments/form.html — form id `#vendor-payment-form`, TomSelect ids, bank account modal
      ref: src/main/resources/static/js/accountspayable/vendor-payments/form.js:L106-L128 — loadPayableBills triggered by vendor+currency change
      ref: src/main/resources/templates/accountspayable/vendor-payments/fragments/bank-account-selector.html:L49 — `.js-bank-account-select` button
- [x] Write **sanity test**: navigate to list, assert table visible
- [x] Write **Scenario A** (`@smoke`): call `createDraftVendorPayment`, navigate to detail `/{id}`, assert `.badge` with text `DRAFT`
      ref: src/main/resources/templates/accountspayable/vendor-payments/detail.html:L14-L18 — badge in `.page-title`
- [x] Write **Scenario B**: create draft → navigate detail → click `.btn-confirm-payment` → `#confirm-modal-btn-yes` → wait for page reload → assert badge `CONFIRMED`
      ref: src/main/resources/templates/accountspayable/vendor-payments/detail.html:L27-L33 — confirm button with `data-action-url` and `data-redirect-url`
      ref: docs/tests/playwright-pitfalls.md §4 — ErpForm.postAction uses Bootstrap modal, not native dialog
- [x] Write **Scenario C**: create draft → navigate detail → click `.btn-cancel-payment` → `#confirm-modal-btn-yes` → assert badge `CANCELLED`
      ref: src/main/resources/templates/accountspayable/vendor-payments/detail.html:L35-L42 — cancel button
- [x] Write **Scenario D**: create draft → navigate to list → `page.evaluate(fetch DELETE)` → reload → assert row gone
      ref: e2e-tests/tests/inventory/goods-receipt.spec.ts:L215-L239 — delete via API pattern
      ref: docs/tests/playwright-pitfalls.md §1 — use page.evaluate AFTER page.goto (not from about:blank)
- [x] Set `test.describe.configure({ timeout: 180_000 })` — VP flow is multi-step (GR + complete + VB + confirm + VP)
- [x] Use `test.use({ storageState: storageStatePath('admin') })` — admin has all permissions

**Key pitfall mitigations:**
- Confirm/cancel uses `ErpForm.postAction` → click `#confirm-modal-btn-yes` (pitfall §4)
- Delete via `page.evaluate(fetch)` only AFTER navigating to list page (pitfall §1)
- TomSelect vendor: `setTomSelectValue` is sufficient since cascading only needs the value (no payload dependency for loading payable bills — it just uses vendorId)
- Currency TomSelect: default currency (IDR) is pre-filled on create form, no need to change it
- Bank account: modal selector pattern — click trigger button, wait modal, click select button
- Allocation lines load via AJAX after vendor is set — wait for `#allocation-lines tr` to appear
- AutoNumeric on `paymentAmount` and `.paid-amount-input` — use `setAutoNumeric` helper
- Form submit is AJAX (`data-ajax-form`) → wait for redirect URL pattern `/\/accounts-payable\/vendor-payments(\?.*)?$/`

**Validation criteria:**
- `cd e2e-tests && npx tsc --noEmit` clean
- `npx playwright test tests/accountspayable/vendor-payment.spec.ts --list` shows 5 scenarios
- `npx playwright test tests/accountspayable/vendor-payment.spec.ts` green at least once
- Cold-cache run: `rm -rf .auth/ && npx playwright test tests/accountspayable/vendor-payment.spec.ts` also green
