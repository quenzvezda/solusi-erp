# Implementation Report: Stock Card Improvement (SOL-7)

> Plan: docs/plans/2026-05-31-sol-7-stock-card-improvement.md
> Source: Linear SOL-7 — https://linear.app/solusi-program/issue/SOL-7/improvement-for-stock-card
> Created: 2026-05-31

## Findings

## Task 1: Extend filter + repository search
- **Status:** clean
- **Summary:** Added movement type, document type, and document-code keyword filtering to stock-card query flow with Mockito coverage for parameter forwarding and blank keyword normalization.

## Task 2: Response enrichment - total cost
- **Status:** clean
- **Summary:** Added `totalCostLocal` response enrichment as absolute quantity multiplied by local unit cost, with mapper coverage for computed and null-cost cases.

## Task 3: Controller - autocomplete prefill + enum dropdowns
- **Status:** clean
- **Summary:** Replaced stock-card preload dropdown data with lookup-provider Trinity prefill, added movement/reference enum lists, and covered model attributes plus `STOCK-CARD_READ` authorization.

## Task 4: i18n keys
- **Status:** clean
- **Summary:** Added matching English and Indonesian stock-card labels for new filters, serial/reference/cost columns, advanced toggle, and all-option dropdown text; verified movement/reference enum key coverage remains 16 keys per locale.

## Task 5: Template - HTML structure
- **Status:** clean
- **Summary:** Rebuilt the stock-card filter/table with autocomplete fragments, compact advanced filters, serial column, movement/reference labels, total/unit cost display, and GR/Stock Adjustment reference links.

## Task 6: Template - JS wiring for autocomplete

### Finding: Manual page script not required
- **Type:** decision
- **Severity:** info
- **Detail:** `layout/master.html` calls `initAllLookups()` on `DOMContentLoaded` and `htmx:afterSwap`; `erp-common-handler.js` initializes every `select[data-lookup-path]`, and the autocomplete fragment backs `productId`/`containerId` with real `<select th:field>` controls.
- **Action taken:** Kept stock-card without page-specific JavaScript and marked the plan step complete based on the global auto-initialization path.
- **Ref:** src/main/resources/static/js/shared/erp-common-handler.js

## Final Verification
- **Status:** clean
- **Summary:** On version `1.9.0`, `mvn clean test` completed with 1552 tests, 0 failures/errors/skips; `e2e-tests/scripts/run-e2e.ps1` completed with 73 Playwright tests passing.
