# Implementation Report: E2E Purchase Order Flow (STANDARD)

> Plan: docs/plans/2026-05-25-e2e-purchase-order.md
> Source: (no brainstorming doc — derived from autonomous exploration)
> Created: 2026-05-25

## Findings

(Populated during execution by execute-plan skill)

## Task 1: Seed APPROVED PR + tax + grant warehouse PO permissions
- **Status:** clean (one finding noted below)
- **Summary:** Extended V9000 with tax id 9001, PR id 9301 + 2 lines (9301 laptop, 9302 chair), and 8 permission grants on ROLE_WAREHOUSE.

## Task 4: Helper — createDraftStandardPo
- **Status:** clean (one finding noted)
- **Summary:** Added `createDraftStandardPo(page)` that opens the create form, switches to STANDARD via `label[for="po-type-standard"]` (clicking the visible label drives the hidden radio), picks PR via modal, picks tax via TomSelect, picks laptop line via modal (multi-select pattern), sets unit price, submits, returns new PO id from list page.

### Finding: Multi-select PR-line modal — checkbox + apply, not per-row pick
- **Type:** gap
- **Severity:** info
- **Detail:** Plan assumed pickPrLineFromModal would click a per-row "Choose" button (mirroring `js-pr-selector-pick` in PR header modal). Actual fragment uses `js-pr-line-selector-item` checkboxes per row + a single `js-pr-line-selector-apply` button at the modal footer (multi-select semantics per spec). Helper updated.
- **Action taken:** Helper now ticks the matching row's checkbox then clicks `.js-pr-line-selector-apply`.
- **Ref:** src/main/resources/templates/purchasing/purchase-orders/fragments/pr-line-selector-modal.html:L62-L88

### Finding: Runtime trace deferred to Scenario A
- **Type:** decision
- **Detail:** `createDraftStandardPo` is fully exercised by Scenario A (Task 5) which is the first consumer. Avoiding standalone `test.only` trace to keep iteration tight; if Scenario A fails on the helper steps, fix lives in Task 5 PR.
- **Action:** tsc --noEmit clean; helper committed for Task 5 to consume.
