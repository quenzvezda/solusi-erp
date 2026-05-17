# Implementation Plan: Bank Account Currency and COA Form

> Source: direct user brief on 2026-05-17
> Created: 2026-05-17
> Sprint: 5
> Status: IN_PROGRESS

## Summary

Add missing Currency and COA inputs to the Bank Account create/edit form so users can see and maintain the accounting references used by Vendor Payment journal posting. Currency must use the standard autocomplete fragment; COA must use the modal selector pattern. The implementation must keep web-layer boundaries clean: controllers may use application use cases and domain lookup providers, but must not inject JPA repositories.

## Scope

- Add Currency field to Bank Account form using autocomplete.
- Add COA field to Bank Account form using modal selector.
- Server-side prefill edit form with Trinity Data for Currency and display data for selected COA.
- Add a Bank Account-specific COA selector endpoint/fragment/JS consumer.
- Add controller/template tests and run web-layer dependency guard.

## Out of Scope

- Changing existing Bank Account schema/domain fields; `currencyId` and `coaId` already exist in domain, DTO, create/update use cases, lookup payload, and DB.
- Changing Vendor Payment journal logic; DB verification showed bank account id 1 maps to COA `1120 - Main Bank Account` and journal line already credits that COA.
- Adding new accounting rules or changing COA eligibility beyond selecting an account row from the existing COA selector use case.

## Tasks

### Task 1: Controller Prefill and Dependency Wiring [x]
Add lookup-provider based UI data for Currency and COA on Bank Account create/edit without repository dependencies.

**Depends on:** (none)
**Reference module:** `purchasing.purchaseorder`, `accounting.coa`, `master.bankaccount`

Steps:
- [x] Inject `CurrencyLookupProvider` and `CoaLookupProvider` into `BankAccountController`.
      ref: src/main/java/com/solusi/erp/purchasing/purchaseorder/web/controller/PurchaseOrderController.java:L263-L284 — `buildPOUI()` resolves autocomplete Trinity Data via lookup provider.
      ref: docs/spec/autocomplete-generic.md:L108-L173 — SSR prefill must use Lookup Provider Port, not JPA repositories.
- [x] Add private `buildBankAccountUI(BankAccount domain)` returning `Map<String, Object>` with `currencyText`, `currencySubtext`, `coaText`, and `coaSubtext`.
      ref: src/main/java/com/solusi/erp/accountspayable/vendorpayment/web/controller/VendorPaymentController.java:L226-L245 — local `buildVpUI()` pattern for currency/bank-account display data.
- [x] In `showCreateForm()`, add `bankAccountUI` as an empty map or omit only if template guards all access.
      ref: src/main/java/com/solusi/erp/master/bankaccount/web/controller/BankAccountController.java:L73-L79 — current create model setup.
- [x] In `showEditForm()`, add `bankAccountUI = buildBankAccountUI(domain)` after `bankAccountRequest` is mapped.
      ref: src/main/java/com/solusi/erp/master/bankaccount/web/controller/BankAccountController.java:L96-L111 — current edit setup only preloads city/party.
- [x] Preserve existing city/party prefill behavior; do not move cross-slice lookup resolution into `BankAccountWebMapper`.
      ref: docs/spec/autocomplete-generic.md:L143-L171 — controller owns SSR UI map; web mapper stays clean.
- [x] **TEST:** Update `BankAccountControllerTest` to construct the controller with new provider mocks and verify edit form contains `bankAccountUI`.
      ref: src/test/java/com/solusi/erp/master/bankaccount/web/controller/BankAccountControllerTest.java:L1-L75 — current Mockito controller test style.
- [x] **TEST:** Verify `@PreAuthorize` annotations remain unchanged for create/edit/selector methods.
      ref: src/test/java/com/solusi/erp/master/bankaccount/web/controller/BankAccountControllerTest.java:L1-L75 — reflection-style web controller assertions.

**Validation criteria:**
- `mvn test -q -pl . -Dtest="BankAccountControllerTest,WebLayerDependencyGuardTest" -DfailIfNoTests=false` passes.
- No `BankAccountController` repository imports or repository fields are introduced.

---

### Task 2: COA Selector Endpoint for Bank Account [x]
Expose a Bank Account-scoped selector endpoint that reuses existing COA selector use case and returns a Bank Account fragment.

**Depends on:** Task 1
**Reference module:** `accounting.coa`, `purchasing.purchaseorder`

Steps:
- [x] Inject `FindCoaSelectorUseCase` into `BankAccountController`.
      ref: src/main/java/com/solusi/erp/accounting/coa/web/controller/CoaController.java:L75-L83 — COA controller uses `FindCoaSelectorUseCase` to build selector page.
      ref: src/main/java/com/solusi/erp/accounting/coa/application/usecase/query/CoaSelectorRow.java:L1-L14 — existing selector row DTO fields.
- [x] Add `GET /master/bank-accounts/selectors/coa` endpoint with `@PreAuthorize("hasAnyAuthority('BANK-ACCOUNT_CREATE', 'BANK-ACCOUNT_UPDATE')")`.
      ref: src/main/java/com/solusi/erp/purchasing/purchaseorder/web/controller/PurchaseOrderController.java:L108-L131 — selector endpoints return modal fragments and model attributes.
- [x] Accept `keyword`, optional `accountType`, and `Pageable`; call `findCoaSelectorUseCase.execute(keyword, accountType, PageableMapper.toDomain(pageable))` or use the method’s expected pageable type if already Spring-compatible.
      ref: src/main/java/com/solusi/erp/accounting/coa/web/controller/CoaController.java:L75-L83 — exact `FindCoaSelectorUseCase` invocation pattern.
- [x] Add model attributes `page`, `keyword`, and `accountType`.
      ref: docs/spec/modal-selector.md:L40-L48 — selector fragment needs search form, target root, table, empty state, pagination.
- [x] Return `master/bank-accounts/fragments/coa-selector-modal`.
      ref: docs/spec/modal-selector.md:L30-L38 — modal shell bodyId must match fragment root id to avoid `htmx:targetError`.
- [x] **TEST:** Add controller test for selector endpoint view name and model attributes.
      ref: src/test/java/com/solusi/erp/master/bankaccount/web/controller/BankAccountControllerTest.java:L1-L75 — current controller unit test style.

**Validation criteria:**
- `mvn test -q -pl . -Dtest="BankAccountControllerTest,WebLayerDependencyGuardTest" -DfailIfNoTests=false` passes.
- Selector endpoint uses use case only; no direct COA JPA/repository dependency in web layer.

---

### Task 3: Bank Account Form HTML [x]
Render Currency autocomplete and COA selector controls in the Bank Account form using project UI standards.

**Depends on:** Task 1, Task 2
**Reference module:** `master.bankaccount`, `purchasing.purchaseorder`

Steps:
- [x] Add Currency field in `templates/master/bank-accounts/form.html` using `fragments/inputs :: autocomplete` with path `master/currencies`.
      ref: src/main/resources/templates/master/bank-accounts/form.html:L41-L52 — current city/party autocomplete fragment usage.
      ref: docs/spec/autocomplete-generic.md:L34-L49 — autocomplete must provide `initialValue`, `initialText`, and `initialSubtext`.
- [x] Bind Currency field to `bankAccountRequest.currencyId` and prefill from `bankAccountUI.currencyText` / `bankAccountUI.currencySubtext`.
      ref: src/main/java/com/solusi/erp/master/bankaccount/web/dto/BankAccountSaveRequest.java:L49-L53 — DTO already has `currencyId` and `coaId`.
- [x] Add COA hidden input bound to `*{coaId}` plus readonly display input for selected COA.
      ref: docs/spec/modal-selector.md:L49-L65 — selection payload must contain only needed data and map to form state.
- [x] Add a “Select COA” button that opens the Bank Account COA selector modal.
      ref: docs/spec/modal-selector.md:L77-L82 — single-select uses a per-row/button action.
- [x] Include shared modal shell fragment with stable body id, e.g. `bank-account-coa-selector-modal-body`.
      ref: docs/spec/modal-selector.md:L30-L38 — modal shell bodyId must equal selector fragment root id.
- [x] Ensure form remains AJAX CRUD (`data-ajax-form="true"`, `data-redirect-on-success`) and no HTMX submit is introduced.
      ref: docs/spec/form-submission.md:L7-L28 — CRUD forms with TomSelect/stateful JS use AJAX JSON.
- [x] Add/verify i18n keys for Bank Account currency and COA labels/placeholders if missing.
      ref: src/main/resources/templates/master/bank-accounts/form.html:L35-L72 — current label key namespace `master.bank-account.*`.
- [x] **TEST:** Update `BankAccountTemplateTest` to read form template and assert `currencyId`, `coaId`, modal shell, selector button class/id, and COA display input exist.
      ref: src/test/java/com/solusi/erp/master/bankaccount/web/template/BankAccountTemplateTest.java:L46-L58 — existing static template assertion style.

**Validation criteria:**
- Create form shows empty Currency autocomplete and empty COA selector.
- Edit form pre-populates both Currency and COA display values.
- `mvn test -q -pl . -Dtest="BankAccountTemplateTest" -DfailIfNoTests=false` passes.

---

### Task 4: COA Selector Fragment [x]
Create the HTMX fragment that renders selectable COA rows for Bank Account form.

**Depends on:** Task 2, Task 3
**Reference module:** `purchasing.purchaseorder`, `accounting.coa`

Steps:
- [x] Create `src/main/resources/templates/master/bank-accounts/fragments/coa-selector-modal.html`.
      ref: docs/spec/modal-selector.md:L40-L48 — selector fragment minimum structure.
- [x] Root element id and fragment name must both be `bank-account-coa-selector-modal-body`.
      ref: docs/spec/modal-selector.md:L30-L38 — id/fragment/bodyId consistency avoids HTMX target errors.
- [x] Add HTMX search form with `hx-get="/master/bank-accounts/selectors/coa"`, `hx-target="#bank-account-coa-selector-modal-body"`, and keyword/accountType fields.
      ref: docs/spec/form-submission.md:L70-L87 — HTMX passive search form structure.
- [x] Render a table with COA code, name, account type, level, and header/body marker.
      ref: src/main/java/com/solusi/erp/accounting/coa/application/usecase/query/CoaSelectorRow.java:L3-L13 — fields available for selector rows.
- [x] Add a pick button per row with class `.js-bank-account-coa-pick` and `data-coa-id`, `data-coa-code`, `data-coa-name`, `data-coa-account-type` attributes.
      ref: docs/spec/modal-selector.md:L57-L65 — selection payload via `data-*`.
- [x] Add empty state and pagination fragment if existing page pagination fragment is available for selector pages.
      ref: docs/spec/modal-selector.md:L40-L48 — selector fragments need empty state and pagination for `Page<T>`.
- [x] **TEST:** Update `BankAccountTemplateTest` or add a dedicated static test to assert fragment root id, HTMX target, pick button class, and data attributes.
      ref: docs/spec/modal-selector.md:L129-L135 — template contract test for modal selector wiring.

**Validation criteria:**
- `/master/bank-accounts/selectors/coa` renders a fragment without target mismatch.
- Searching/pagination updates `#bank-account-coa-selector-modal-body`.
- Static template tests pass.

---

### Task 5: Page-Specific JavaScript Wiring [x]
Add Bank Account page JavaScript to open the COA selector and map selected COA into the form.

**Depends on:** Task 3, Task 4
**Reference module:** `purchasing.purchaseorder`

Steps:
- [x] Create `src/main/resources/static/js/master/bank-accounts/form.js` if no page-specific JS exists.
      ref: docs/spec/modal-selector.md:L18-L27 — page-specific JS is the modal selector consumer.
- [x] Include `erp-modal-selector.js` and the new page script in `#page-scripts` of Bank Account form.
      ref: docs/spec/modal-selector.md:L116-L127 — consumer checklist requires shared helper and page-specific mapping.
- [x] Wire COA button click to open modal with URL `/master/bank-accounts/selectors/coa` and body id `bank-account-coa-selector-modal-body`.
      ref: docs/spec/modal-selector.md:L20-L27 — shared helper loads initial selector fragment into modal shell.
- [x] Add delegated click handler for `.js-bank-account-coa-pick` that sets hidden `coaId` and readonly display input to `code - name`, then closes modal.
      ref: docs/spec/modal-selector.md:L57-L65 — payload mapping stays in page-specific JS.
- [x] Add clear behavior only if the form UI includes an explicit clear button; do not silently clear COA on unrelated changes.
      ref: docs/spec/modal-selector.md:L61-L65 — payload only what consumer needs; avoid unnecessary hidden coupling.
- [x] Do not manually initialize Currency autocomplete if fragment auto-init is sufficient; avoid duplicate TomSelect initialization.
      ref: docs/spec/autocomplete-generic.md:L34-L40 and L99-L104 — global auto-init handles fragment `data-lookup-path`.
- [x] **TEST:** Extend static template test to assert page script is included and expected JS hook class/id strings are present in template/JS.
      ref: src/test/java/com/solusi/erp/master/bankaccount/web/template/BankAccountTemplateTest.java:L46-L58 — static resource assertions pattern.

**Validation criteria:**
- Clicking Select COA opens selector modal.
- Picking a COA updates hidden `coaId` and visible display text.
- Saving create/edit submits `currencyId` and `coaId` through existing AJAX JSON handler.

---

### Task 6: Final Verification and Regression Guard [~]
Run focused tests and manual UI checks for the Bank Account form and VP journal dependency.

**Depends on:** Task 1, Task 2, Task 3, Task 4, Task 5
**Reference module:** project test standards

Steps:
- [x] Run compile check.
      ref: docs/AGENTS.md — project verification expectations if present.
- [x] Run Bank Account-focused tests: `BankAccountControllerTest`, `BankAccountTemplateTest`.
      ref: src/test/java/com/solusi/erp/master/bankaccount/web/controller/BankAccountControllerTest.java:L1-L75 — controller coverage.
      ref: src/test/java/com/solusi/erp/master/bankaccount/web/template/BankAccountTemplateTest.java:L1-L59 — template coverage.
- [x] Run dependency guard: `WebLayerDependencyGuardTest`.
      ref: src/test/java/com/solusi/erp/architecture/WebLayerDependencyGuardTest.java:L1-L120 — web layer must not depend on repositories.
- [ ] Manually open `/master/bank-accounts/create`: Currency autocomplete works; COA selector opens and fills form.
- [ ] Manually open `/master/bank-accounts/edit/1`: Currency and COA are prefilled from current DB values.
- [ ] Save edit without changing COA and verify Bank Account id 1 still maps to `1120 - Main Bank Account`.
- [ ] Confirm VP journal behavior is unchanged; bank account COA remains the source for bank credit line.

**Validation criteria:**
- `mvn compile -q -pl .` passes.
- `mvn test -q -pl . -Dtest="BankAccountControllerTest,BankAccountTemplateTest,WebLayerDependencyGuardTest" -DfailIfNoTests=false` passes.
- Manual create/edit form flow works in browser.
- No regression in VP journal account mapping.

## Risk Notes

- Do not inject `CurrencyJpaRepository`, `CoaJpaRepository`, or any repository into `BankAccountController`; use lookup providers and selector use cases only.
- Do not add COA lookup logic to `BankAccountWebMapper`; mapper should remain pure DTO mapping.
- Do not use a bare `data-autocomplete` field without the `fragments/inputs :: autocomplete(...)` fragment or proper `initLookup()` wiring.
- Do not submit the CRUD form via HTMX; keep `data-ajax-form="true"` because the form contains TomSelect and modal-driven hidden state.
- Keep COA selector fragment id, modal body id, and `hx-target` identical.
