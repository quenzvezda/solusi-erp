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
