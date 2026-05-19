# Implementation Plan: E2E Purchase Requisition + Approval Flow

> Source: Conversation on 2026-05-19 about expanding Playwright E2E to transactional modules
> Created: 2026-05-19
> Sprint: E2E Phase 2 — First Transactional Module
> Status: IN_PROGRESS

## Summary

Expand the Playwright E2E suite from master-data CRUD into the first transactional module: Purchase Requisition (PR) with full approval flow including digital signature. The plan also fixes a pre-existing dev-seeder gap (ROLE_APPROVER missing PR/SPL/PO permissions), mirrors expanded seed data into the H2 E2E migration, adds new helpers for Flatpickr / inline line editor / signature pad / role-based login switching, and produces an ambitious PR spec covering all five state transitions plus edit-DRAFT and SPL autofill verification.

## Decisions Locked from Discussion

- **Dev seeder permission file:** new `D011__role_permissions.sql` (between D010 roles and D030 users), not D040.
- **E2E seed strategy:** mirror dev seeder content into `V9000__e2e_seed_data.sql`, no second Flyway location.
- **Signature handling:** Layer 1 (real `mouse.move` draw on canvas) + Layer 2 (`signaturePad.isEmpty()===false` assert). Layer 3 backend bypass only as fallback if Layer 1 proves flaky.
- **Login switching:** storage state per role (`.auth/admin.json`, `.auth/approver1.json`, `.auth/employee1.json`) loaded via `test.use({ storageState })`. Login one-shot in a Playwright `setup` project.
- **First module:** Purchase Requisition (has approval but tests login-switching + signature pad). Stock Adjustment deferred until after PR pattern is stable.
- **MVP scope:** ambitious — 6 scenarios (5 state transitions + edit DRAFT + SPL autofill verification optional).
- **Reuse existing dev users:** admin, approver1, warehouse1, employee1. No new 6 personas.

## Current Findings from Exploration

- `src/main/resources/templates/purchasing/purchase-requisitions/form.html` confirms the inline line editor pattern (`#table-lines`, `#btn-add-line`, `.btn-remove-line`, `#row-template-source`). Form uses `data-ajax-form="true"` with `data-redirect-on-success="/purchasing/purchase-requisitions"`.
- The form has 4 header autocompletes (`requesterId`, `facilityId`, `suggestedSupplierId` via `parties` lookup; `header-facility` via `inventory/facilities`). Currency and priority are native `<select>`. `requestDate` is a date input with `data-picker="date"` (Flatpickr).
- Submit-for-approval is a **two-step flow**: (1) save DRAFT, (2) DRAFT page shows a green "Submit for Approval" button that opens `#modal-submit-approval` containing a TomSelect approver picker (`#submit-approver`, `data-lookup-path="parties/by-role-type?roleTypeCode=APPROVER"`).
- Approval action UI lives in `src/main/resources/templates/fragments/approval.html`. Approve/Reject buttons are wired to `ApprovalUI.openApproveFinishModal()` / `openRejectModal()`. Approve modal contains `<canvas id="sig-canvas-approve-finish">` driven by `signature_pad@4` (loaded globally in `layout/master.html:77`). Reject modal does NOT require a signature — only notes.
- `D010__security_roles.sql` shows `ROLE_APPROVER` currently only has `DASHBOARD_READ`, `NEWS_*`, `APPROVAL_READ`, `APPROVAL_PROCESS`, `LOOKUP_PARTY`, `DASHBOARD_NEWS`, `DASHBOARD_APPROVAL` — missing PR_READ / PR_UPDATE / SPL_*/ PO_* / LOOKUP_INVENTORY / LOOKUP_PURCHASING / LOOKUP_SUPPLIER-PRICE-LIST. Same gap blocks manual QA today.
- `V9000__e2e_seed_data.sql` currently only seeds UoM (id 9001-9003), Product Categories (9001-9002), Brands (9001-9002), and resets admin's `password_change_required`. No Products, Facilities, Parties (vendors/employees), Currencies (beyond prod default), or SPL.
- Existing E2E specs do NOT assert exact list size or seed-only data they can collide with: `product.spec.ts` uses `uniqueName('Product')` and does not assert specific seeded products. `brand.spec.ts` asserts `E2E Brand Beta` exists (id 9002, untouched by this plan). UoM list intentionally does not assert seeded labels.
- E2E helpers cover: TomSelect, AutoNumeric, auth, navigation, form, waits, data factory. Missing: Flatpickr, inline line editor, signature pad, multi-user storage state.
- `helpers/auth.ts` only exports `TEST_USERS.admin`. `loginAndSaveState()` and per-role storage are not implemented.

## Tasks

### Task 1: Add D011 dev-seeder for role permissions (PR/SPL/PO + lookups) [x]

Adds the missing `role_permissions` seeding so ROLE_APPROVER can review/approve procurement docs and ROLE_EMPLOYEE can create PR. Closes the manual-QA workaround where you currently log in as admin and tick permissions by hand.

**Depends on:** none
**Reference module:** dev seeder folder (mirrors are SQL only — no Java)

Steps:
- [x] Create `docs/database/dev-seeder/D011__role_permissions.sql` between D010 (roles) and D030 (users).
      ref: docs/database/dev-seeder/D010__security_roles.sql:L17-L24 — current ROLE_APPROVER permissions (gap)
      ref: src/main/resources/db/migration/V46__Add_Purchasing_Module.sql:L207-L228 — canonical permission names PR_*, SPL_*, PO_*, LOOKUP_*
- [x] Grant `ROLE_APPROVER`: PR_READ, PR_UPDATE, LOOKUP_PR, PO_READ, PO_UPDATE, LOOKUP_PO, SPL_READ, LOOKUP_SUPPLIER-PRICE-LIST, LOOKUP_INVENTORY, LOOKUP_BRAND, LOOKUP_PRODUCT-CATEGORY, LOOKUP_FACILITY, LOOKUP_UOM-CONVERSION. (LOOKUP_PARTY already in D010.) See report — `LOOKUP_PURCHASING` and `LOOKUP_PARTY-ROLE-TYPE` do not exist as permissions; the by-role-type endpoint reuses LOOKUP_PARTY.
      ref: docs/modules/procurement/purchase-requisition.md:L111-L123 — declared security matrix for PR
- [x] Grant `ROLE_EMPLOYEE`: PR_READ, PR_CREATE, PR_UPDATE, PR_DELETE, PR_SUBMIT, LOOKUP_PR, SPL_READ, LOOKUP_SUPPLIER-PRICE-LIST, LOOKUP_INVENTORY, LOOKUP_BRAND, LOOKUP_PRODUCT-CATEGORY, LOOKUP_FACILITY, LOOKUP_UOM-CONVERSION, LOOKUP_PARTY.
- [x] Use `INSERT INTO role_permissions (role_id, permission_id) SELECT @role_x_id, id FROM permissions WHERE name IN (...)` mirroring the D010 style. Do not duplicate rows already inserted by D010.
- [x] Verify SQL syntax compatible with both MariaDB (dev) and H2 MODE=MySQL (E2E mirror in Task 2). Same INSERT...SELECT pattern works identically in both.

**Validation criteria:**
- Fresh local DB run with dev seeder: log in as `approver1/admin123` → can open `/purchasing/purchase-requisitions` list without 403. (Will verify after Task 2 mirrors to V9000.)
- Log in as `employee1/admin123` → can open `/purchasing/purchase-requisitions/create` without 403, and can save+submit a PR. (Will verify in Task 9.)
- No `Cannot insert duplicate` error when running D011 after D010. (D011 adds permissions not in D010, so no conflict.)

### Task 2: Mirror D011 + dev users + transactional master data into V9000 [x]

Brings the H2 E2E database to dev parity (so tests can log in as approver/employee just like manual QA) and adds Products/Facilities/Parties/SPL needed for the PR flow. Uses ID range 9100+ for new transactional data to avoid colliding with existing 9001-9002 master-data seed.

**Depends on:** Task 1
**Reference module:** existing V9000 + dev seeder D020 (parties), D030 (users), D040+ (transactional data)

Steps:
- [x] Read current V9000 to confirm existing seed (UoM, Categories, Brands at id 9001-9003 / 9001-9002).
      ref: src/main/resources/db/migration-h2/V9000__e2e_seed_data.sql — current content
- [x] Append a "ROLE PERMISSIONS (mirror D011)" section to V9000 with the same `INSERT INTO role_permissions ... SELECT ...` blocks from D011. Also mirrored D010 since production migrations only seed ROLE_ADMIN — without D010 mirror the new roles would not exist.
- [x] Append a "USERS (mirror D030)" section: insert approver1, approver2, warehouse1, employee1 with same BCrypt hash as D030.
      ref: docs/database/dev-seeder/D030__users.sql:L1-L19 — BCrypt hash for `admin123`, party FK pattern
- [x] Append a "PARTIES (mirror D020)" section seeding minimal parties: BP-DEV-APR01, BP-DEV-APR02, BP-DEV-WH01, BP-DEV-EMP01 (referenced by D030 user FKs), plus one supplier party BP-DEV-SUP01. Skipped party_addresses, party_identifications, party_contacts to keep V9000 lean — not required by PR flow.
      ref: docs/database/dev-seeder/D020__parties.sql — party + party_role_types pattern
- [x] Seed party_role_types APPROVER + assign role types to each party so `parties/by-role-type?roleTypeCode=APPROVER` returns approver1/approver2.
- [x] Seed transactional master in id range 9100+: `inv_facilities` (9101 E2E Main Warehouse owned by SUP01), `products` (E2E-PRD-LAPTOP, E2E-PRD-CHAIR via auto-id since unique constraint on `code` is enough). Tied to existing brand 9001/9002, category 9001, uom 9001.
- [x] master_currencies already seeded by V11 (IDR has is_default=TRUE). Resolve via `SELECT id FROM master_currencies WHERE alias='IDR'`.
- [x] Seed one active `pur_supplier_price_lists` row (supplier=SUP01, product=Laptop, currency=IDR, price 8500000, effective_from 2026-01-01) for Scenario F.
- [x] Verify the migration runs cleanly: `mvnw -B package -DskipTests -Pe2e` then start app with `--spring.profiles.active=e2e` and confirm no Flyway error.

**Validation criteria:**
- [x] App starts with E2E profile, no migration error in `target/e2e-server.log`. Started in 11.51s.
- [x] `migration-parity` script still passes (62 MariaDB versions, 63 H2 versions, V9000 allowlisted).
- [x] HTTP POST `/login` with `approver1`/`admin123` returns 302 → /dashboard (verified).
- [x] HTTP POST `/login` with `employee1`/`admin123` returns 302 → /dashboard, GET /purchasing/purchase-requisitions returns 200 (verified).

### Task 3: Validate seed change does not break existing E2E specs [x]

Run the full Playwright suite against the new V9000 to catch any spec that implicitly depended on the previous (smaller) seed state.

**Depends on:** Task 2
**Reference module:** existing E2E specs

Steps:
- [x] Run `e2e-tests/scripts/run-poc.ps1` (Windows) or `run-poc.sh` (Linux/macOS). (Used direct `npx playwright test` instead since JAR was already built and server already running from Task 2 verification.)
      ref: e2e-tests/scripts/run-poc.ps1, e2e-tests/scripts/run-poc.sh — runner builds JAR, starts e2e profile, runs full suite
- [x] If any spec fails, diagnose. (No failures.)
- [x] If a regression is genuine, patch the affected spec rather than the seed. (No patches needed.)
- [x] Re-run until 18/18 (current baseline) green again, then move on. (18/18 passed on first run.)

**Validation criteria:**
- [x] `npm run test` in `e2e-tests/` is green: 18 passed (2.7m).
- [x] No transient flakes — all tests passed first run with 0 retries used.

### Task 4: Add `helpers/flatpickr.ts` for date picker interaction [x]

Creates a stable helper to set Flatpickr-managed date inputs (`data-picker="date"`). PR header `requestDate` and per-line `requiredDate` use this widget.

**Depends on:** none (can run in parallel with Task 1-3)
**Reference module:** existing `helpers/autonumeric.ts` and `helpers/tomselect.ts` (same evaluate-into-page pattern)

Steps:
- [ ] Create `e2e-tests/helpers/flatpickr.ts` exporting `setFlatpickrDate(page, selector, dateStr)` and `getFlatpickrDate(page, selector)`.
      ref: e2e-tests/helpers/autonumeric.ts — `page.evaluate` pattern for widget API access
      ref: docs/spec/datetime-standards.md — `data-picker="date"` convention and date format expectations
- [ ] Use `page.waitForFunction` to wait for `el._flatpickr !== undefined` before calling `el._flatpickr.setDate(date, true)`. The `true` flag triggers onChange handlers.
- [ ] Accept ISO format `YYYY-MM-DD` as the canonical input; let Flatpickr handle display formatting.
- [ ] Add a fallback: if `_flatpickr` is not present after 5s wait (input might be a plain `<input type="date">`), fall back to `page.fill(selector, dateStr)` so the helper works for non-Flatpickr date inputs too.
- [ ] Add a small smoke test inline in the spec that uses it (Task 9) — no separate test file (Playwright project has no JS unit framework).

**Validation criteria:**
- Helper passes TypeScript compile (`tsc --noEmit` if running locally or via `npx playwright test --list`).
- When used in a spec, `getFlatpickrDate` returns the same value that was set.

### Task 5: Add `helpers/line-editor.ts` for inline line CRUD [x]

Generic helper for header-lines forms following the `#row-template-source` + `#line-container` pattern (PR, SO, PO, etc.). Encapsulates add-line, remove-line-at-index, and per-cell field setters that resolve the right `lines[N].field` selector.

**Depends on:** none
**Reference module:** PR form template

Steps:
- [ ] Create `e2e-tests/helpers/line-editor.ts` with:
  - `addLine(page, addBtnSelector = '#btn-add-line')` — clicks the add button and waits for `#line-container tr.line-row` count to increment.
  - `removeLineAt(page, index)` — clicks `.btn-remove-line` inside the row at the given index.
  - `getLineCount(page)` — returns `await page.locator('#line-container tr.line-row').count()`.
  - `lineFieldSelector(index, field)` — returns `[name="lines[${index}].${field}"]` (escaping brackets carefully for Playwright).
      ref: src/main/resources/templates/purchasing/purchase-requisitions/form.html:L114-L131 — line row markup
      ref: src/main/resources/templates/purchasing/purchase-requisitions/form.html:L173-L187 — `#row-template-source` pattern
      ref: docs/spec/header-lines-form.md — ErpLineManager conventions and dynamic index rewriting
- [ ] Use Playwright's `page.locator` with attribute selectors. Brackets in `name` must be CSS-escaped or wrapped in attribute selector form.
- [ ] Add `waitForRowSettled(page, index)` that waits for the row's TomSelect cells to be initialized (each cell `select.tomselect` available).

**Validation criteria:**
- Used by Task 9 spec without flake; add-line increments count, remove-line decrements, per-field setters resolve correctly.

### Task 6: Add `helpers/signature-pad.ts` for canvas signature [x]

Layer 1+2 strategy: real pointer drawing on the canvas, then assert `signaturePad.isEmpty() === false`. No backend bypass.

**Depends on:** none
**Reference module:** approval modals fragment

Steps:
- [ ] Create `e2e-tests/helpers/signature-pad.ts` exporting `drawSignature(page, canvasSelector)` and `assertSignatureNotEmpty(page, canvasSelector)`.
      ref: src/main/resources/templates/fragments/approval.html:L226-L246 — `#sig-canvas-approve-finish` canvas + signature_pad@4 widget
      ref: src/main/resources/templates/layout/master.html:L76-L77 — global signature_pad script load
- [ ] In `drawSignature`: locate the canvas, get its bounding box via `boundingBox()`, then issue 3-4 `mouse.move` + `mouse.down`/`mouse.up` strokes at relative positions (e.g. 20%/30% to 80%/70%). Use `page.mouse` directly for precise control.
- [ ] Be defensive about device pixel ratio: use `boundingBox()` (CSS pixels) not canvas internal width.
- [ ] In `assertSignatureNotEmpty`: `await page.evaluate((sel) => { const el = document.querySelector(sel); const pad = window._signaturePadInstances?.[el.id] ?? null; return pad?.isEmpty?.() ?? !el.toDataURL().endsWith('AAAAAElFTkSuQmCC'); }, canvasSelector)`. The toDataURL fallback is safe because an empty PNG has a known suffix.
- [ ] If app does not expose the SignaturePad instance globally, inspect `templates/fragments/approval.html` and `js/common/approval-ui.js` to find where the instance is held; either add a small hook in app code (`window.__erpSignaturePads = ...` in profile-aware way) **or** rely solely on the toDataURL non-empty heuristic.
- [ ] Document in helper comment that the helper draws a real signature so backend stores a real PNG. No bypass needed.

**Validation criteria:**
- After draw, the approval modal's submit succeeds (i.e. server accepts the signature blob).
- `assertSignatureNotEmpty` returns true after draw, false before draw.

### Task 7: Refactor auth helper for multi-role storage state [x]

Move from "login per test" to "login once, replay storage state per test". Adds approver1 and employee1 to `TEST_USERS`, introduces a `setup` Playwright project that produces `.auth/{role}.json`, and lets specs declare `test.use({ storageState })`.

**Depends on:** Task 2 (users seeded), Task 3 (existing specs unbroken)
**Reference module:** Playwright authentication recipes (referenced in proposal section 5.6)

Steps:
- [ ] Extend `e2e-tests/helpers/auth.ts`:
  - Add `TEST_USERS.approver1`, `TEST_USERS.employee1` (and admin already there).
      ref: e2e-tests/helpers/auth.ts:L1-L32 — current single-user shape
  - Add `loginAndSaveState(page, context, user, storagePath)` that calls `login()` then `context.storageState({ path: storagePath })`.
- [ ] Create `e2e-tests/global.setup.ts` (Playwright `setup` project file) that:
  - Logs in as each user once, saves storage to `e2e-tests/.auth/{role}.json`.
  - Skips if `.auth/{role}.json` exists and is fresh (mtime within 30 min) — speeds up local re-runs.
- [ ] Update `e2e-tests/playwright.config.ts`:
  - Add a `projects` entry `{ name: 'setup', testMatch: /global\.setup\.ts/ }`.
  - Update the chromium project to `dependencies: ['setup']`.
- [ ] Add `e2e-tests/.auth/` to `.gitignore` (or extend existing `.gitignore`).
- [ ] Existing fixture `fixtures/base.ts` currently does auto-login on every test using admin. Refactor to:
  - Default fixture loads `.auth/admin.json` storage state.
  - Export additional fixtures `approverPage` and `employeePage` that load the corresponding storage states.
      ref: e2e-tests/fixtures/base.ts:L1-L11 — current auto-login fixture
- [ ] Verify existing CRUD specs (which import `from '../../fixtures/base'`) still pass with the new admin storage-state fixture (no behavior change for them).

**Validation criteria:**
- Full suite still green (admin path unchanged).
- A trivial sanity test with `test.use({ storageState: '.auth/approver1.json' })` lands on `/dashboard` without going through `/login`.

### Task 8: Add `tests/procurement/purchase-requisition.spec.ts` skeleton

Wires the file with role-specific fixtures and TODO stubs for each scenario. Implementation arrives in Tasks 9-14.

**Depends on:** Tasks 4, 5, 6, 7
**Reference module:** existing `tests/master-data/product.spec.ts`

Steps:
- [ ] Create `e2e-tests/tests/procurement/purchase-requisition.spec.ts` with the skeleton:
  - Import role fixtures from `../../fixtures/base`.
  - One outer `test.describe('Purchase Requisition flow', ...)`.
  - Six (or seven) `test()` stubs matching scenarios A through F (and optional SPL autofill).
  - Each stub starts with `test.skip()` so the skeleton commit is green.
- [ ] Each scenario header includes a one-line comment of the user journey:
  - A: employee1 creates PR → submits → approver1 approves & finishes (with signature)
  - B: employee1 creates PR → submits → approver1 rejects
  - C: employee1 creates PR → edits header + lines while DRAFT → cancels DRAFT
  - D: employee1 creates PR → submits → approver1 approves → employee1 cancels APPROVED
  - E: employee1 creates PR → adds 1 line → changes supplier → expects line cleared
  - F (optional): employee1 creates PR with SPL-matching supplier+currency → picks product → estimatedUnitPrice auto-fills
      ref: docs/modules/procurement/purchase-requisition.md:L46-L78 — state lifecycle and update/delete rules
      ref: docs/modules/procurement/purchase-requisition.md:L82-L89 — UI behaviors (header reset, SPL autofill)

**Validation criteria:**
- Spec file exists and `npx playwright test --list` shows the new test cases (all skipped).
- No TypeScript compile error.

### Task 9: Implement Scenario A — happy path create → submit → approve & finish

Most important test: validates the entire stack — multi-role login switching, line editor, Flatpickr, signature pad, approval state machine. If this passes, B-D are mostly variations.

**Depends on:** Task 8
**Reference module:** existing CRUD specs + new helpers

Steps:
- [ ] As `employeePage`: `navigateToModule('/purchasing/purchase-requisitions/create')`.
- [ ] Fill header:
  - `setFlatpickrDate('input[name="requestDate"]', '2026-05-19')`.
  - `setTomSelectValue('#header-requester', <party id of employee1>)` — employee1's party id from V9000 seed (Task 2).
  - `setTomSelectValue('#header-facility', '9101')`.
  - `setTomSelectValue('#header-supplier', <supplier party id>)`.
  - Native select: `selectDropdown('currencyId', <IDR id>)`.
  - Optional: select Priority `HIGH`.
      ref: src/main/resources/templates/purchasing/purchase-requisitions/form.html:L62-L97 — header fields and selectors
- [ ] Add one line via `addLine()`:
  - In line 0, set `productId` via TomSelect (`select-product-0`) to product 9101.
  - Set `quantity` via AutoNumeric to `5`.
  - Confirm UoM auto-filled (read TomSelect value, expect 9001).
  - Set `requiredDate` via Flatpickr to `2026-06-01`.
  - Set `estimatedUnitPrice` via AutoNumeric to `8500000`.
- [ ] Click main `Save` button (form submit). Assert redirect back to list AND that the new PR's row exists in the table (search by code prefix `PR-`).
- [ ] Open the saved PR (the form view will show the green "Submit for Approval" button only on edit page).
- [ ] Click `#btn-submit-pr`, then in modal pick approver via `#submit-approver` TomSelect (approver1's party id), click `#btn-confirm-submit-approval`.
- [ ] Assert PR status badge changes to SUBMITTED (refresh detail page if needed).
- [ ] Switch to `approverPage` (storage state of approver1). Navigate to the same PR detail.
- [ ] Click "Approve & Finish" button (`onclick="ApprovalUI.openApproveFinishModal()"`). Wait for `#modal-approve-finish` visible.
- [ ] Fill `#approve-finish-notes` with `"E2E approval"`.
- [ ] Call `drawSignature(page, '#sig-canvas-approve-finish')`, then `assertSignatureNotEmpty(...)`.
- [ ] Click the modal's submit button (text "Approve & Finish").
- [ ] Assert PR status changes to APPROVED.
- [ ] Open Approval History drawer; assert the latest entry shows actor approver1 + action APPROVE_AND_FINISH.

**Validation criteria:**
- Test passes deterministically (run 3 times in a row, all green).
- Server log shows `ApprovalRequest` created on submit and decision recorded on approve.
- Tag this scenario `@smoke` so push-to-main CI runs it.

### Task 10: Implement Scenario B — submit then reject

Same setup as A through submit. Diverges at approver action.

**Depends on:** Task 9
**Reference module:** Task 9 reuses 90% of code; consider extracting a `createAndSubmitDraft(employeePage)` helper inside the spec file itself.

Steps:
- [ ] Refactor common "create + submit DRAFT as employee1" code from Task 9 into a local helper function in the spec file (not in `helpers/` — module-specific).
- [ ] Scenario B body: call helper to create+submit, then switch to approverPage.
- [ ] Click "Reject" button. Wait for `#modal-reject-approval` visible. Note: reject modal has NO signature, only `#reject-notes` textarea.
      ref: src/main/resources/templates/fragments/approval.html:L380-L408 — reject modal markup (notes only)
- [ ] Fill notes with `"Estimasi terlalu tinggi"`.
- [ ] Click submit (`onclick="ApprovalUI.submitReject()"`).
- [ ] Assert PR status changes to REJECTED.
- [ ] Assert REJECTED PR has no Submit/Approve action available (locked).
      ref: docs/modules/procurement/purchase-requisition.md:L60 — REJECTED has no further actions

**Validation criteria:**
- Status badge REJECTED visible in form view header.
- No action banner is displayed for either employee or approver after reject.

### Task 11: Implement Scenario C — DRAFT edit then cancel

Verifies the DRAFT update path and the cancel-from-DRAFT transition.

**Depends on:** Task 9
**Reference module:** PR doc state machine

Steps:
- [ ] As `employeePage`: create a PR (skip submit). Save and stay on edit page.
- [ ] Modify header: change priority to URGENT, change note text.
- [ ] Modify line 0: change quantity from 5 to 7.
- [ ] Save. Assert success redirect / toast.
- [ ] Reopen the PR; assert the changes persisted.
- [ ] Cancel the PR via the cancel action (find the actual control — likely on list page row actions or a button on form). If the action is missing, mark this part as `test.fixme` with TODO comment pointing at the gap.
      ref: docs/modules/procurement/purchase-requisition.md:L57 — DRAFT actions include Cancel
      ref: src/main/resources/templates/purchasing/purchase-requisitions/list.html — list row action buttons (read when expanding)
- [ ] Assert PR status is CANCELLED and no further actions available.

**Validation criteria:**
- Edits persist across reload.
- CANCELLED state is final.

### Task 12: Implement Scenario D — APPROVED cancel

Tests the "Approved → Cancelled" transition explicitly mentioned in the doc lifecycle.

**Depends on:** Task 9
**Reference module:** Task 9 helper

Steps:
- [ ] Run the "create + submit + approve" path via helper from Task 10's refactor.
- [ ] After APPROVED status, switch back to employeePage (or whichever role has cancel permission per Task 1's seed — verify `PR_UPDATE` covers cancel).
      ref: docs/modules/procurement/purchase-requisition.md:L120 — `PR_UPDATE` is also used for Cancel
- [ ] Trigger cancel action on APPROVED PR.
- [ ] Assert status changes to CANCELLED.
- [ ] Assert no further actions available.

**Validation criteria:**
- Approved PR can transition to CANCELLED.
- If the app blocks this transition (i.e. doc is wrong), mark `test.fixme` and surface to user — do not silently weaken the test.

### Task 13: Implement Scenario E — header-change-resets-lines guard

Verifies the UX rule: changing requester/facility/supplier/currency on header clears existing lines.

**Depends on:** Task 9
**Reference module:** PR doc UX standards

Steps:
- [ ] As `employeePage`: create a DRAFT with one line. Save.
- [ ] On the saved DRAFT, change supplier (TomSelect on `#header-supplier`) to a different party.
- [ ] Confirm any warning/confirm dialog if present (read `msg.warning.pr.line.reset` message text).
      ref: src/main/resources/templates/purchasing/purchase-requisitions/form.html:L233 — `headerChangeWarning` config
      ref: src/main/resources/static/js/purchasing/purchase-requisition-form.js — page-specific JS handling header change (read when expanding)
- [ ] Assert `#line-container` has 0 line rows after the reset.

**Validation criteria:**
- Line count goes from 1 to 0 after supplier change.
- `#empty-msg` becomes visible again.

### Task 14: Optional Scenario F — SPL price autofill

Verifies that when an active SPL row matches the header supplier+currency+product, the line `estimatedUnitPrice` auto-populates.

**Depends on:** Task 9, plus SPL seed from Task 2 (the optional step there).
**Reference module:** PR doc SPL integration

Steps:
- [ ] As `employeePage`: create PR with header supplier = BP-DEV-SUP01, currency = IDR (matching SPL seed).
- [ ] Add a line, pick product 9101 via TomSelect.
- [ ] Wait for the page-specific JS to issue the SPL lookup AJAX (use `waitForNetworkIdle` from `helpers/waits.ts`).
- [ ] Read the line's `estimatedUnitPrice` via `getAutoNumericValue`.
- [ ] Assert it equals the seeded SPL price (`8500000`).
- [ ] If SPL seed was deferred (Task 2 optional step skipped), mark this scenario `test.skip` with reason.

**Validation criteria:**
- Auto-filled price matches seeded SPL value.
- Test is robust to small UI delay (use waitForNetworkIdle, not fixed timeout).

### Task 15: Wire smoke tag and CI behavior for PR spec

Decides which PR scenarios run on push-to-main vs. only on schedule/full dispatch.

**Depends on:** Task 9 (at minimum)
**Reference module:** existing smoke split

Steps:
- [ ] Tag Scenario A test name with `@smoke` (Task 9 step already calls this out — confirm here).
      ref: e2e-tests/tests/master-data/product.spec.ts:L14 — `@smoke` tag pattern
- [ ] Do NOT tag B-F as smoke. The full PR spec runs on schedule/full-dispatch only.
- [ ] Run `npm run test:smoke` locally; confirm Scenario A is included and B-F are excluded.
      ref: e2e-tests/package.json:L8 — `test:smoke` script
- [ ] No CI workflow change needed — `--grep @smoke` already used by `e2e-tests` job for push-to-main.
      ref: .github/workflows/ci-java21.yml:L325-L329 — current smoke run on push-to-main

**Validation criteria:**
- Smoke run includes exactly login + 1 brand create + 1 product create + PR Scenario A (4 tests, give or take based on existing smoke set).

### Task 16: Update Playwright E2E guide with new helpers and PR spec status

Document what changed so future agents and humans can pick up without re-discovering the patterns.

**Depends on:** Tasks 4-14 (whichever subset shipped)
**Reference module:** existing guide

Steps:
- [ ] Add a "Date picker (Flatpickr)" subsection to `docs/tests/playwright-e2e-guide.md` Section 8 (Helper yang Wajib Dipakai).
      ref: docs/tests/playwright-e2e-guide.md:L402-L502 — current Section 8 helper list
- [ ] Add a "Inline line editor" subsection with usage example for header-lines forms.
- [ ] Add a "Signature pad" subsection explaining Layer 1+2 strategy and `drawSignature` helper.
- [ ] Add a "Multi-role auth" subsection explaining storage state files, fixtures `approverPage` / `employeePage`, and the `setup` project.
- [ ] Update the coverage status table in Section 10 to add a Procurement row for `purchase-requisition.spec.ts` with the actual scenario count merged.
      ref: docs/tests/playwright-e2e-guide.md:L568-L580 — coverage table
- [ ] Update "Status terakhir" date and counts.
      ref: docs/tests/playwright-e2e-guide.md:L576-L580 — last-status note
- [ ] Cross-link to the V9000 seed expectation (so future module tests know to extend it).

**Validation criteria:**
- Guide reads coherently to a new agent who has not seen this conversation.
- All referenced helpers actually exist at documented paths.
- No stale claims about "Product/UoM may be unresolved" remain.

## Decisions

- Single dev-seeder file for permissions (`D011`) instead of extending `D010` — preserves D010 single responsibility (roles only) and matches the numbering gap convention.
- V9000 mirrors dev seeder content rather than Flyway adding a second location — explicit, single-file, no config drift.
- Storage-state-per-role over re-login-per-test — significant CI speedup and idiomatic Playwright.
- Layer 1+2 signature handling without backend bypass — keeps the test honest; only fall back if proven flaky after Task 9 stabilizes.
- Smoke set kept small (only Scenario A) — push-to-main stays cheap.
- Cancel transitions covered explicitly (Tasks 11, 12) so any gap between doc lifecycle and runtime gets surfaced as `test.fixme` rather than silently passing.

## Final Validation

After all selected tasks:

- Run `bash scripts/check-migration-parity.sh` — should pass (V9000 still allowlisted).
- Start app with `--spring.profiles.active=e2e` and confirm no Flyway error.
- Run `e2e-tests/scripts/run-poc.ps1` (or `.sh`) — full suite green.
- Run `npm run test:smoke` — smoke subset green.
- Manually log in as approver1 against a fresh local MariaDB (after dev-seeder D011) and confirm the manual-QA workaround is no longer needed.

## Recommended Execution Order

1. Task 1 — dev-seeder fix unblocks manual QA + E2E.
2. Task 2 — seed H2 with dev parity + transactional master.
3. Task 3 — guard existing specs before building on top.
4. Tasks 4, 5, 6 — helpers (can run in parallel after Task 3).
5. Task 7 — auth refactor (can run in parallel with helpers).
6. Task 8 — wire PR spec skeleton.
7. Task 9 — happy path first (validates entire stack).
8. Tasks 10–13 — additional scenarios after happy path is green.
9. Task 14 — optional SPL autofill.
10. Task 15 — smoke tagging.
11. Task 16 — guide refresh.

