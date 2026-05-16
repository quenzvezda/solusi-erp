# Implementation Plan: Vendor Payment FE Refactor

> Source: docs/brainstorming/2026-05-15-vendor-payment.md (Section 7: UI/UX Specification)
> Created: 2026-05-15
> Sprint: 5 (Accounts Payable)
> Status: IN_PROGRESS

## Summary

Refactor halaman FE Vendor Payment (form, list, detail) agar sesuai standard codebase project. Saat ini form menggunakan bare HTML input tanpa fragment, tanpa initLookup, tanpa modal selector, dan tanpa ErpNumeric. Plan ini memperbaiki semua komponen FE agar mengikuti pattern yang sama dengan PO form dan spec di `docs/spec/`.

## Tasks

### Task 1: Bank Account Lookup Endpoint & LookupProvider
Buat lookup endpoint untuk bank account (belum ada) dan LookupProvider port agar bisa dipakai autocomplete/modal selector dan controller `buildUI()`.

**Depends on:** (none)
**Reference module:** `master.currency` (CurrencyLookupController pattern)

Steps:
- [ ] Create `BankAccountLookupProvider` interface di `master.bankaccount.domain.port`
      ref: src/main/java/com/solusi/erp/master/party/domain/port/PartyLookupProvider.java — lookup provider port pattern
- [ ] Create `BankAccountLookupProviderImpl` adapter di `master.bankaccount.infrastructure.adapter`
      ref: src/main/java/com/solusi/erp/master/party/infrastructure/adapter/PartyLookupProviderImpl.java — adapter impl pattern
- [ ] Create `BankAccountLookupController` di `master.bankaccount.web.controller`:
  - `GET /api/lookup/master/bank-accounts?q={keyword}&currencyId={optional}&hasCoaOnly={optional}` → search with optional filters
  - `GET /api/lookup/master/bank-accounts/{id}` → detail (for SSR pre-fill)
  - Return `LookupDto` with payload: `{ paymentType, currencyCode, coaCode }`
      ref: src/main/java/com/solusi/erp/master/currency/web/controller/CurrencyLookupController.java — lookup controller pattern
      ref: docs/spec/autocomplete-generic.md — LookupDto structure and endpoint convention
- [ ] Register `BankAccountLookupProvider` bean in `BankAccountConfig`
- [ ] **TEST:** Write `BankAccountLookupControllerTest` (search returns filtered results, detail returns LookupDto with payload)

**Validation criteria:**
- `GET /api/lookup/master/bank-accounts?q=BCA` returns matching bank accounts
- `GET /api/lookup/master/bank-accounts?currencyId=1&hasCoaOnly=true` filters correctly
- Payload includes `paymentType`, `currencyCode`, `coaCode`

---

### Task 2: Bank Account Modal Selector (Endpoint + Fragment)
Buat selector endpoint dan Thymeleaf fragment untuk bank account modal selector (filtered by currency + coaId NOT NULL).

**Depends on:** Task 1
**Reference module:** `purchasing.purchaseorder` (PR selector pattern)

Steps:
- [ ] Create `BankAccountSelectorRow` DTO di `accountspayable.vendorpayment.web.dto`:
  - fields: id, accountName, accountNumber, bankName, paymentType, currencyCode, coaCode
      ref: docs/spec/modal-selector.md — section 4 (Kontrak data: Selector row DTO)
- [ ] Create selector endpoint in `VendorPaymentController`:
  - `GET /accounts-payable/vendor-payments/selectors/bank-accounts?q={keyword}&currencyId={required}`
  - Filter: `isActive=true`, `currencyId` match, `coaId IS NOT NULL`
  - Return fragment view
      ref: docs/spec/modal-selector.md — section 2 (Arsitektur standar: Controller endpoint selector)
- [ ] Create `templates/accountspayable/vendor-payments/fragments/bank-account-selector.html`:
  - Root element with `id="bank-account-selector-body"` AND `th:fragment="bank-account-selector-body"`
  - Search form with `hx-get` targeting same fragment
  - Table: Account Name, Account Number, Bank Name, Payment Type, Action (Select button)
  - `data-*` attributes on select button: `data-id`, `data-name`, `data-account-number`, `data-payment-type`
  - Empty state when no results
      ref: docs/spec/modal-selector.md — section 3 (Kontrak HTML)
      ref: src/main/resources/templates/purchasing/purchase-orders/fragments/ — existing selector fragment pattern
- [ ] **TEST:** Write test for selector endpoint in `VendorPaymentControllerTest` (model attrs, filter logic)

**Validation criteria:**
- Selector fragment renders table with bank accounts filtered by currency + coaId NOT NULL
- HTMX search within modal works (hx-target matches fragment root id)
- `data-*` payload on select button contains all needed fields

---

### Task 3: Refactor form.html — Autocomplete Fragments & Modal Shell
Replace bare HTML inputs with proper Thymeleaf fragments for autocomplete, numeric, date, and add modal selector shell.

**Depends on:** Task 2
**Reference module:** `purchasing.purchaseorder` (form.html)

Steps:
- [ ] Update `VendorPaymentController.createForm()`: add empty `vpUI` map to model
- [ ] Update `VendorPaymentController.editForm()`: add `buildVpUI(payment)` method that resolves:
  - vendorName/vendorSubtext via `PartyLookupProvider`
  - currencyName/currencySubtext via `CurrencyLookupProvider` (or similar)
  - bankAccountName/bankAccountSubtext via `BankAccountLookupProvider`
  - Add `vpUI` map to model
      ref: docs/spec/autocomplete-generic.md — section 4 (Server-Side Pre-fill: Lookup Provider Port, buildXxxUI pattern)
      ref: src/main/resources/templates/purchasing/purchase-orders/form.html:L97-L110 — autocomplete fragment with poUI
- [ ] Refactor `form.html` — replace vendor input with autocomplete fragment:
  ```html
  <div th:replace="~{fragments/inputs :: autocomplete(field='vendorId', label=#{label.vp.vendor}, path='parties',
      required=true, id='vp-vendor',
      initialValue=${paymentRequest.vendorId}, initialText=${vpUI != null ? vpUI['vendorText'] : ''},
      initialSubtext=${vpUI != null ? vpUI['vendorSubtext'] : ''})}"></div>
  ```
- [ ] Refactor `form.html` — replace currency input with autocomplete fragment:
  ```html
  <div th:replace="~{fragments/inputs :: autocomplete(field='currencyId', label=#{label.vp.currency}, path='master/currencies',
      required=true, id='vp-currency',
      initialValue=${paymentRequest.currencyId}, initialText=${vpUI != null ? vpUI['currencyText'] : ''},
      initialSubtext=${vpUI != null ? vpUI['currencySubtext'] : ''})}"></div>
  ```
- [ ] Refactor `form.html` — replace bank account input with modal selector trigger pattern:
  - Hidden input `name="bankAccountId"`
  - Readonly display input with button group (search + clear)
  - Pre-fill display from `vpUI['bankAccountText']`
      ref: src/main/resources/templates/purchasing/purchase-orders/form.html:L66-L86 — PR reference modal trigger pattern
- [ ] Add modal selector shell include at bottom of form:
  ```html
  <div th:replace="~{fragments/modal-selector :: modal(title=#{label.vp.selectBankAccount}, bodyId='bank-account-selector-body')}"></div>
  ```
      ref: src/main/resources/templates/fragments/modal-selector.html — shared modal shell
- [ ] Refactor `form.html` — replace payment date input with date fragment:
  ```html
  <div th:replace="~{fragments/inputs :: date(field='paymentDate', value=${paymentRequest.paymentDate}, label=#{label.vp.paymentDate}, required=true)}"></div>
  ```
      ref: docs/spec/datetime-standards.md — data-picker="date" auto-init
- [ ] Refactor `form.html` — replace exchange rate input with decimal fragment:
  ```html
  <div th:replace="~{fragments/inputs :: decimal(field='exchangeRate', label=#{label.vp.exchangeRate}, required=true)}"></div>
  ```
- [ ] Refactor `form.html` — replace payment amount input with decimal fragment:
  ```html
  <div th:replace="~{fragments/inputs :: decimal(field='paymentAmount', label=#{label.vp.paymentAmount}, required=true)}"></div>
  ```
      ref: docs/spec/numeric-standards.md — fragment decimal usage
- [ ] Add `data-ajax-form="true"` and `data-redirect-on-success="/accounts-payable/vendor-payments"` to form tag (verify consistent with form-submission spec)
- [ ] Add alert-container div inside form for AJAX error display
      ref: docs/spec/form-submission.md — section 2A (Atribut HTML)
- [ ] **TEST:** Update `VendorPaymentTemplateTest` — verify fragment includes, modal shell presence, autocomplete field names, data-ajax-form attribute

**Validation criteria:**
- Form renders with TomSelect autocompletes for vendor and currency
- Bank account shows modal trigger (search button + display input)
- Date field uses Flatpickr (data-picker="date")
- Numeric fields use AutoNumeric (via fragment)
- Edit form pre-fills all autocomplete trinity data correctly

---

### Task 4: Refactor form.js — Interactive Wiring (Autocomplete, Modal, Cascading)
Rewrite page-specific JS to properly wire autocompletes, modal selector, cascading behavior, and ErpNumeric.

**Depends on:** Task 3
**Reference module:** `purchasing.purchaseorder` (purchase-order-form.js)

Steps:
- [ ] Rewrite `form.js` structure: IIFE with `"use strict"`, element references, guard clause
      ref: src/main/resources/static/js/purchasing/purchase-order-form.js:L1-L30 — JS structure pattern
- [ ] Wire vendor autocomplete cascading: on change → call `/accounts-payable/vendor-payments/payable-bills?vendorId={}&currencyId={}` → reload allocation lines
  - Use `waitForLookupReady` pattern or `tomselect.on('change', ...)` event
  - On vendor change: clear existing allocation lines, reload if both vendor+currency selected
      ref: src/main/resources/static/js/purchasing/purchase-order-form.js:L58-L60 — waitForLookupReady pattern
- [ ] Wire currency autocomplete cascading: on change → clear allocation lines, reload payable bills if vendor also selected
  - Same pattern as vendor change
- [ ] Wire bank account modal selector:
  - Open modal on search button click via `ErpModalSelector.open('bank-account-selector-body', url)` or equivalent shared helper
  - URL: `/accounts-payable/vendor-payments/selectors/bank-accounts?currencyId={currentCurrencyId}`
  - On select (listen for `.js-bank-account-select` click inside modal):
    - Read `data-id`, `data-name`, `data-payment-type` from button
    - Set hidden input `bankAccountId` value
    - Set display input text
    - Auto-fill payment type readonly field (if present)
    - Close modal
      ref: docs/spec/modal-selector.md — section 2 & 8 (Consumer checklist)
      ref: src/main/resources/static/js/purchasing/purchase-order-form.js — PR selector consumer pattern
- [ ] Wire payable bills loading:
  - Function `loadPayableBills(vendorId, currencyId)` → fetch `/accounts-payable/vendor-payments/payable-bills?vendorId=&currencyId=`
  - On success: render allocation lines in `#allocation-lines` tbody
  - Each line: bill code (readonly), outstanding (readonly, formatted), paid amount (editable, init ErpNumeric)
  - Init `ErpNumeric` on dynamically added paid-amount inputs via `initNumericInputs()` or manual AutoNumeric init
      ref: docs/spec/numeric-standards.md — section 2 (ErpNumeric.get/set for dynamic lines)
- [ ] Wire recap calculation using `ErpNumeric.get()`:
  - `updateRecap()`: read paymentAmount via `ErpNumeric.get(paymentAmountInput)`, sum paid amounts via `ErpNumeric.get(each paidInput)`
  - Update recap display elements with formatted values
  - Toggle text-danger/text-success on unapplied based on value
- [ ] Wire line removal: remove button removes `<tr>`, triggers `updateRecap()`
- [ ] Wire form submission validation (capture phase):
  - Validate at least 1 line with paidAmount > 0
  - Validate unapplied = 0 (sum matches payment amount)
  - If invalid: `event.preventDefault()` + `event.stopImmediatePropagation()` + show error via `ErpModal.showWarning()`
      ref: docs/spec/form-submission.md — section 2C (Urutan Validasi)

**Validation criteria:**
- Vendor autocomplete works (TomSelect search, select, clear)
- Currency autocomplete works with cascading (change clears lines)
- Bank account modal opens, filters by currency, select fills form
- Payable bills load automatically when vendor+currency selected
- Paid amount inputs use ErpNumeric (formatted, get/set works)
- Recap updates in real-time using ErpNumeric values
- Form validation prevents submit if unapplied != 0

---

### Task 5: Refactor list.html & detail.html
Update list and detail templates to follow project standards (HTMX delete, action buttons, status badges, numeric formatting).

**Depends on:** (none)
**Reference module:** `accountspayable.vendorbill` (list.html, detail.html)

Steps:
- [ ] Verify `list.html` uses HTMX row delete pattern correctly:
  - Delete button: `hx-post` with `hx-target="#row-{id}"` and `hx-swap="outerHTML swap:500ms"`
  - Table container has `hx-trigger="refresh-table from:body delay:500ms"`
      ref: docs/spec/form-submission.md — section 3B (Inline Actions: Delete - Always Fresh)
- [ ] Verify `list.html` search/filter uses HTMX pattern:
  - Form with `hx-get`, `hx-target="#table-container"`, `hx-trigger="keyup changed delay:500ms"`
      ref: docs/spec/form-submission.md — section 3A (Passive Updates)
- [ ] Verify `detail.html` has action buttons (Confirm, Cancel) using `ErpForm.postAction` pattern:
  - Confirm button: calls `ErpForm.postAction('/accounts-payable/vendor-payments/{id}/confirm', ...)`
  - Cancel button: calls `ErpForm.postAction('/accounts-payable/vendor-payments/{id}/cancel', ...)`
  - Both with confirmation dialog
      ref: docs/spec/action-buttons.md — ErpForm.postAction pattern
- [ ] Verify `detail.html` shows VB code as hyperlink to `/accounts-payable/vendor-bills/{id}`
- [ ] Verify `detail.html` shows journal entry link (if payment is CONFIRMED)
- [ ] Add numeric formatting on detail page amounts (use `th:text="${#numbers.formatDecimal(...)}"` or similar)
- [ ] **TEST:** Update `VendorPaymentTemplateTest` — verify HTMX attributes on list, action button scripts on detail, VB hyperlinks

**Validation criteria:**
- List: HTMX delete works, search filters without page reload
- Detail: Confirm/Cancel buttons use ErpForm.postAction with confirmation
- Detail: VB codes link to vendor bill detail page
- Detail: Journal entry link present for CONFIRMED payments

---

### Task 6: i18n Keys & Final Integration Test
Add missing i18n keys and verify end-to-end form flow in browser.

**Depends on:** Task 4, Task 5
**Reference module:** (none)

Steps:
- [ ] Audit `messages_en.properties` and `messages_id.properties` for all VP keys:
  - `label.vp.title`, `label.vp.create`, `label.vp.edit`
  - `label.vp.vendor`, `label.vp.currency`, `label.vp.bankAccount`, `label.vp.paymentDate`
  - `label.vp.exchangeRate`, `label.vp.paymentAmount`, `label.vp.reference`, `label.vp.notes`
  - `label.vp.selectBankAccount` (modal title)
  - `label.vp.allocation`, `label.vp.billCode`, `label.vp.outstandingAmount`, `label.vp.paidAmount`
  - `label.vp.recap.paymentAmount`, `label.vp.recap.applied`, `label.vp.recap.unapplied`
  - `msg.success.vp.confirmed`, `msg.success.vp.cancelled`
  - `msg.err.vp.lines.required`, `msg.err.vp.amount.mismatch`
      ref: docs/spec/i18n-guide.md — naming convention
- [ ] Add any missing keys to both property files
- [ ] Verify `mvn compile -q` passes
- [ ] Run `mvn test -Dtest="*VendorPayment*"` — all tests pass
- [ ] Manual browser test: create form → select vendor → select currency → bills load → select bank account via modal → fill amounts → recap updates → save

**Validation criteria:**
- No `??key??` displayed in any VP page
- All existing VP tests still pass
- End-to-end flow works in browser (create, edit, confirm, cancel, delete)
