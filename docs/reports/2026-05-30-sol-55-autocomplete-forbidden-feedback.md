# Implementation Report: Autocomplete Access/Error Feedback (SOL-55)

> Plan: docs/plans/2026-05-30-sol-55-autocomplete-forbidden-feedback.md
> Source: Linear SOL-55
> Created: 2026-05-30

## Findings

## Task 1: i18n keys + expose ke `window.ErpI18n`
- **Status:** clean
- **Summary:** Added localized lookup forbidden, generic failure, and no-results messages; exposed them through `window.ErpI18n`; added a static regression test for both bundles and the layout export.
- **Verification:** `mvn -q -Dtest=LookupFeedbackTemplateTest test`; `mvn -q compile -pl .`

## Task 2: Fix shared `initLookup`

### Finding: TomSelect does not render `no_results` for focus preload with an empty query
- **Type:** decision
- **Severity:** warning
- **Detail:** A headless Chromium probe loaded the real TomSelect CDN bundle, initialized the shared lookup handler, and returned HTTP 403 for `/api/lookup/parties?q=&limit=10`. The request sent `Accept: application/json`, but `dropdown_content` remained empty after `callback([])`.
- **Action taken:** Kept the inline `no_results` renderer for query states where TomSelect renders it, and enabled the approved fallback `ErpModal.showWarning(...)` with a once-per-dropdown-open throttle. A second browser probe verified one warning on empty focus, no duplicate warning for a retry in the same open dropdown, and a new warning after close/reopen.
- **Ref:** `src/main/resources/static/js/shared/erp-common-handler.js`

- **Status:** findings
- **Summary:** Added status-aware shared lookup loading, localized error selection, JSON content negotiation, inline no-results feedback, and throttled modal fallback.
- **Verification:** `mvn -q -Dtest=LookupFeedbackTemplateTest test`; `mvn -q compile -pl .`; headless Chromium TomSelect probe with stubbed 403 responses.

## Task 3: Audit dan fix loader non-shared

### Finding: Party edit memakai select UI sebagai nilai submit canonical
- **Type:** bug
- **Severity:** high
- **Detail:** Nilai `cityId` edit sebelumnya bergantung pada select TomSelect. Saat hierarchy prefill gagal karena 403, nilai kota lama berisiko hilang ketika user menyimpan perubahan unrelated.
- **Action taken:** Added a hidden canonical `cityId` field for existing and dynamic rows, synchronized it on city changes and clears, and preserved it when hierarchy prefill fails.
- **Ref:** `src/main/resources/templates/master/parties/form.html`

### Finding: Stock Adjustment masih memiliki salinan private lookup handler
- **Type:** deviation
- **Severity:** info
- **Detail:** Form Stock Adjustment tidak memakai shared `initLookup`, sehingga fix global harus diterapkan dua kali.
- **Action taken:** Applied the same lookup feedback behavior locally. Refactoring the duplicate into the shared handler remains out of scope for this patch.
- **Ref:** `src/main/resources/static/js/inventory/adjustment/stock-adjustment-form.js`

### Finding: Fetch payload produk lama bukan dropdown loader
- **Type:** debt
- **Severity:** info
- **Detail:** Purchase Order dan Purchase Requisition masih memiliki supporting product-detail fetch yang tidak mengeraskan semua status handling. Keduanya bukan `TomSelect.load` dan tidak memengaruhi preservasi submit Party.
- **Action taken:** Audited and left unchanged as explicitly out of scope.
- **Ref:** `src/main/resources/static/js/purchasing/purchase-order-form.js`; `src/main/resources/static/js/purchasing/purchase-requisition-form.js`

- **Status:** findings
- **Summary:** Added status-aware feedback to Stock Adjustment, menu search, and all three Party geographic dropdowns; hardened Party hierarchy loading; preserved canonical edit `cityId`; confirmed remaining lookup consumers use shared `initLookup`.
- **Verification:** `mvn -q -Dtest=LookupFeedbackTemplateTest,PartyTemplateTest,StockAdjustmentTemplateTest test`; `mvn -q compile -pl .`; headless Chromium Party probe with stubbed 403 hierarchy and dropdown responses.
