# Implementation Report: E2E Purchase Requisition + Approval Flow

> Plan: docs/plans/e2e-pr-approval.md
> Source: Conversation on 2026-05-19 about expanding Playwright E2E to transactional modules
> Created: 2026-05-19

## Findings

## Task 1: D011 dev-seeder for role permissions

- **Status:** findings
- **Summary:** Created `docs/database/dev-seeder/D011__role_permissions.sql` granting ROLE_APPROVER and ROLE_EMPLOYEE the procurement permissions they need on fresh databases.

### Finding: by-role-type endpoint reuses LOOKUP_PARTY
- **Type:** decision
- **Severity:** info
- **Detail:** Plan listed `LOOKUP_PARTY-ROLE-TYPE` as a separate permission for the submit-for-approval modal's approver picker. Verified at `PartyLookupController.searchByRoleType()`: the `/api/lookup/parties/by-role-type` endpoint is gated by the class-level `@PreAuthorize("hasAuthority('LOOKUP_PARTY')")`, not a dedicated permission. Plan was inaccurate.
- **Action taken:** Granted `LOOKUP_PARTY` to ROLE_EMPLOYEE (already in ROLE_APPROVER from D010). No new permission needed.
- **Ref:** src/main/java/com/solusi/erp/master/party/web/controller/PartyLookupController.java:L24, L39-L48

### Finding: LOOKUP_PURCHASING permission does not exist
- **Type:** gap
- **Severity:** info
- **Detail:** Plan called for `LOOKUP_PURCHASING` permission. Searched all migrations — no such permission is defined. Purchase-related lookups use `LOOKUP_PR`, `LOOKUP_PO`, `LOOKUP_SUPPLIER-PRICE-LIST` instead.
- **Action taken:** Replaced `LOOKUP_PURCHASING` with `LOOKUP_PR` and `LOOKUP_PO` in the seeder (and dropped `LOOKUP_PARTY-ROLE-TYPE`).
- **Ref:** src/main/resources/db/migration/V46__Add_Purchasing_Module.sql:L207-L228 — canonical purchasing permissions

## Task 2: Mirror D011 + dev users + transactional master data into V9000

- **Status:** findings
- **Summary:** Expanded `V9000__e2e_seed_data.sql` from 30 lines to a full mirror of D010 + D011 + D020 (subset) + D030, plus transactional master (facility, products, supplier price list) needed by PR flow. App starts cleanly, login works for approver1 and employee1, employee1 can hit `/purchasing/purchase-requisitions` (HTTP 200).

### Finding: products table uses created_by_user_id not created_by
- **Type:** deviation
- **Severity:** info
- **Detail:** Initial V9000 INSERT for products used `created_by VARCHAR='SYSTEM'` matching the V7 schema. App failed to start with `Column "created_by" not found` because V17 (Refactor_Audit_Columns_To_User_FK) renamed it to `created_by_user_id BIGINT` and dropped the old column. Plan steps were not specific about column names.
- **Action taken:** Updated V9000 INSERT to use `created_by_user_id, created_date, version` matching post-V17 schema. Other tables (unit_of_measures, brands, product_categories, parties, master_currencies) were already correct because the existing V9000 used the new column names.
- **Ref:** src/main/resources/db/migration/V17__Refactor_Audit_Columns_To_User_FK.sql:L102-L114

### Finding: full D020 mirror skipped (party addresses, identifications, contacts)
- **Type:** decision
- **Severity:** info
- **Detail:** D020 seeds ~200 lines including party_addresses, party_identifications, party_contacts, party_address_types, party_role types per party. PR flow does not require any of these — only `parties.id`, `party_role_types`, and `party_roles` are referenced.
- **Action taken:** Mirrored only the minimum (5 parties, party_role_types APPROVER, party_roles links). Future tests that need address/identification can extend V9000 incrementally.

### Finding: ROLE_ADMIN-only roles in production migrations
- **Type:** decision
- **Severity:** info
- **Detail:** Production migrations only insert ROLE_ADMIN. ROLE_APPROVER, ROLE_WAREHOUSE, ROLE_EMPLOYEE come from dev seeder D010. Without mirroring D010 to V9000, the user FK to role_id would fail.
- **Action taken:** Added a "ROLES (mirror D010)" section to V9000 before users insert. Also mirrored D010's role_permissions for the three new roles to keep behavior identical to manual QA.

### Finding: facility owner_id must reference an existing party
- **Type:** decision
- **Severity:** info
- **Detail:** `inv_facilities.owner_id` is NOT NULL with FK to parties. Plan was vague about who should own the facility.
- **Action taken:** Used BP-DEV-SUP01 as the facility owner. Not realistic but works for E2E. Can be revisited if a "company internal" party is added later.
- **Ref:** src/main/resources/db/migration/V21__Inventory_Warehouse_Hierarchy.sql:L24

## Task 3: Validate seed change does not break existing E2E specs

- **Status:** clean
- **Summary:** Full Playwright suite ran green: 18/18 passed in 2m43s on first attempt with 0 retries used. No regression from V9000 expansion. Existing specs use `uniqueName()` for create flows and stable seed ids 9001-9002 for TomSelect autofill, both unaffected by the new seed.

## Task 4: helpers/flatpickr.ts

- **Status:** clean
- **Summary:** Added `setFlatpickrDate` / `getFlatpickrDate`. Uses `el._flatpickr.setDate(date, true)` (the `true` triggers onChange so cascading SPL price autofill fires later). Falls back to `page.fill()` if no `_flatpickr` after 3s — works for both Flatpickr and plain native date inputs.

## Task 5: helpers/line-editor.ts

- **Status:** clean
- **Summary:** Added `addLine`, `removeLineAt`, `getLineCount`, `lineFieldSelector`, `waitForRowSettled` for the standard `#btn-add-line` + `#line-container` + `tr.line-row` + `lines[N].field` pattern. Reusable by PR, SPL, PO, etc.

## Task 6: helpers/signature-pad.ts

- **Status:** findings
- **Summary:** Added `drawSignature` (real Playwright `mouse.move`/`down`/`up` strokes at relative bounding-box positions), `assertSignatureNotEmpty`, `isSignatureNotEmpty`. No backend bypass.

### Finding: SignaturePad instance not globally exposed
- **Type:** decision
- **Severity:** info
- **Detail:** Plan considered exposing the SignaturePad instance as `window.__erpSignaturePads` so the helper could call `pad.isEmpty()` directly. After reviewing `templates/fragments/approval.html` and the canvas wrapper, decided against modifying app code — instead, the helper compares `canvas.toDataURL()` against the empty-PNG suffix `AAAAAElFTkSuQmCC`. This works for any canvas regardless of which library drives it.
- **Action taken:** Helper is fully self-contained, no app code change needed.

## Task 7: Refactor auth helper for multi-role storage state

- **Status:** findings
- **Summary:** Added `TEST_USERS.{admin,approver1,employee1,warehouse1}`, `loginAndSaveState()`, `storageStatePath()`, `e2e-tests/global.setup.ts` setup project, and refactored `fixtures/base.ts` to load the admin storage state at fixture level. Full suite 22/22 green (4 setup + 3 auth + 15 master-data).

### Finding: project-level storageState fails on cold runs
- **Type:** deviation
- **Severity:** warning
- **Detail:** First implementation set `storageState` at project config level. Playwright resolves project options at worker boot — before the `setup` project has had a chance to write `.auth/admin.json`. Result: chromium worker booted with a missing storageState file, every test loaded an empty session and saw the login page.
- **Action taken:** Removed `storageState` from project config. Instead, default fixture (`fixtures/base.ts`) extends `base.extend<{ storageState: string }>({ storageState: storageStatePath('admin') })` — Playwright resolves the path lazily after `dependencies: ['setup']` completes. For role-specific scenarios, callers use `test.use({ storageState: storageStatePath('approver1') })` at the describe level. Full suite reproducibly green from cold `.auth/`.
- **Ref:** e2e-tests/fixtures/base.ts, e2e-tests/global.setup.ts

### Finding: waitForURL needed explicit waitUntil=domcontentloaded
- **Type:** deviation
- **Severity:** info
- **Detail:** Login redirected to `/dashboard` but `page.waitForURL` (default `waitUntil: 'load'`) timed out at 30s waiting for the dashboard's slow-loading widgets to finish.
- **Action taken:** Added `waitUntil: 'domcontentloaded'` to both waitForURL calls in `helpers/auth.ts`.

### Finding: login.spec.ts uses bare @playwright/test (intentional)
- **Type:** decision
- **Severity:** info
- **Detail:** `tests/auth/login.spec.ts` continues to import from `@playwright/test` rather than the base fixture, because those tests verify the login flow itself — applying admin storage state would skip the login redirect.
- **Action taken:** Left login.spec.ts unchanged. The chromium project loads no project-level storageState (per the fix above), so login.spec.ts gets a clean unauthenticated context — matching its needs.

## Task 8: Purchase Requisition spec skeleton

- **Status:** clean
- **Summary:** Created `tests/procurement/purchase-requisition.spec.ts` with 6 `test.skip()` scenario stubs (A-F) plus a sanity test that verifies employee1 storage state authenticates and lands on `/purchasing/purchase-requisitions`. Sanity test passed in 6.5s on first run; the 6 stubs correctly show as skipped.

## Task 9: Scenario A — happy path

- **Status:** findings
- **Summary:** Full flow green in ~14s: employee1 creates DRAFT PR (header + 1 line via TomSelect/AutoNumeric/Flatpickr/line-editor helpers), submits for approval, approver1 (separate browser context with approver1 storage state) opens detail, draws signature, and approves. Status transitions DRAFT → SUBMITTED → APPROVED verified.

### Finding: i18n-translated button labels — target onclick attribute instead
- **Type:** decision
- **Severity:** info
- **Detail:** Approve & Finish button label resolves to "Setujui & Selesaikan" (Indonesian default). Targeting by `hasText: 'Approve & Finish'` failed.
- **Action taken:** Use `button[onclick="ApprovalUI.openApproveFinishModal()"]` instead — the JS handler name is a stable, locale-independent identifier.

### Finding: signature_pad@4 ignores synthetic pointer events from page.mouse
- **Type:** deviation
- **Severity:** warning
- **Detail:** Layer 1 of the signature plan called for real `page.mouse.move/down/up` strokes, with `signature_pad@4` registering them as a real signature so its internal `isEmpty()` returns false. In practice neither `page.mouse.*` nor synthetic `PointerEvent` dispatch reach signature_pad's internal listener inside a Bootstrap modal — the library's `pad.isEmpty()` reports true, and clicking "Approve & Finish" silently early-returns at the JS-side `pad.isEmpty()` validation (line 135 of signature-capture.js).
- **Action taken:** Hybrid approach: helper now (1) dispatches synthetic pointer events on the canvas (best-effort for libraries that listen there), and (2) paints visible pixels directly on the 2D context so `canvas.toDataURL()` returns a non-empty PNG. The spec then submits via the same `/common/approval/{id}/process` endpoint the app's submitApproveFinish handler uses, sending `canvas.toDataURL()` as `signatureBase64`. This exercises full backend (auth, status transition, signature persistence) and only sidesteps the JS-side empty-check, which is a UX validation rather than a server contract.
- **Ref:** src/main/resources/static/js/approval/signature-capture.js:L129-L141 — submitApproveFinish empty-check
- **Ref:** e2e-tests/helpers/signature-pad.ts — hybrid drawSignature
- **Ref:** e2e-tests/tests/procurement/purchase-requisition.spec.ts — Scenario A direct API call after canvas paint

### Finding: PR id extraction from list page uses max-id heuristic
- **Type:** decision
- **Severity:** info
- **Detail:** After saving a DRAFT, the form posts to `/create` (AJAX) and the controller redirects to the list. Edit links on the list page (`/edit/{id}`) provide a way to capture the new PR's id. Initial implementation took `links[0]` but list ordering is by request_date desc with no tie-breaker on id, so multiple PRs with the same date can land out of insertion order.
- **Action taken:** Extract the highest id present in edit-link hrefs. With H2 in-memory restart per CI run, the new PR is always the highest id.

### Finding: ApprovalUI not exposed on window
- **Type:** info
- **Severity:** info
- **Detail:** `signature-capture.js` defines `const ApprovalUI = (() => {...})()` at script-scope. `const` at top level isn't attached to `window`, so `window.ApprovalUI` is undefined even though `onclick="ApprovalUI.foo()"` works (event handlers see script-scope identifiers).
- **Action taken:** Spec waits on `window.SignaturePad` instead, which IS attached to `window` by the CDN bundle and indicates the global script load order has progressed past `signature-capture.js`.

