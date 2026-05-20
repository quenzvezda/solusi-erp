# Implementation Report: E2E Stock Adjustment + RBAC + PR Reject Event Refactor

> Plan: docs/plans/e2e-sa-rbac-pr-reject.md
> Source: Discussion 2026-05-20 (lanjutan E2E Phase 2)
> Created: 2026-05-20
> Status: PENDING

## Findings

(Populated during execution. Format mengikuti `docs/reports/e2e-pr-approval.md`: per-task heading, status `findings|clean|deviation|bug`, summary, dan finding sub-section dengan `Type / Severity / Detail / Action taken / Ref`.)

## Task 1: Refactor `ApprovalCompletedEvent` → `ApprovalDecidedEvent`

- **Status:** findings (deviation)
- **Summary:** Plan changed mid-execution after exploration revealed existing port already has `publishCompleted` + `publishRejected` methods. Switched from "rename + decision field" to "add `ApprovalRejectedEvent` class + implement adapter `publishRejected`". Smaller blast radius, aligns with existing port design.

### Finding: Port `ApprovalEventPublisher` already separates Completed/Rejected
- **Type:** deviation
- **Severity:** info
- **Detail:** Plan assumed only 2 call sites and proposed renaming `ApprovalCompletedEvent` → `ApprovalDecidedEvent` + `ApprovalDecision` enum field. Exploration found:
  1. Port `com.solusi.erp.common.approval.application.port.ApprovalEventPublisher` (lokasi sebenarnya: `common.approval`, BUKAN `core.approval`) already declares `publishCompleted(...)` AND `publishRejected(...)` as separate methods.
  2. `ProcessApprovalUseCaseImpl.reject()` already calls `eventPublisher.publishRejected(...)`.
  3. Adapter `ApprovalEventPublisherAdapter.publishRejected()` is implemented as a NO-OP with comment "We could create a specific Rejected event in core if needed".
  4. **3 listeners** subscribe to `ApprovalCompletedEvent`, not 2: `OnPurchaseRequisitionApprovedListener`, `OnPurchaseOrderApprovedListener`, `OnNewsApprovedListener`.
- **Action taken:** Switched strategy to Option 1 (create separate `ApprovalRejectedEvent` class). This preserves port design intent, avoids breaking PO and News listeners (which would have needed adjustment under rename), and is still minimal:
  - Create `ApprovalRejectedEvent.java` (new class, mirror of ApprovalCompletedEvent shape).
  - Implement adapter `publishRejected()` to actually emit the new event.
  - Add `OnPurchaseRequisitionRejectedListener` (new component, subscribes to `ApprovalRejectedEvent` with `referenceType == 'PURCHASE_REQUISITION'`).
  - Add `markAsRejected()` to `PurchaseRequisition` domain if missing.
  - Keep `ApprovalCompletedEvent` and existing 3 listeners untouched.
- **Ref:** src/main/java/com/solusi/erp/common/approval/application/port/ApprovalEventPublisher.java:L1-L9 — port shape
- **Ref:** src/main/java/com/solusi/erp/common/approval/infrastructure/adapter/ApprovalEventPublisherAdapter.java:L18-L28 — adapter no-op for publishRejected
- **Ref:** src/main/java/com/solusi/erp/common/approval/application/usecase/ProcessApprovalUseCaseImpl.java:L33-L43 — caller for publishRejected exists

## Task 2: Update publisher untuk emit kedua decision

- **Status:** clean
- **Summary:** Adapter `publishRejected()` (sebelumnya no-op) sekarang emit `ApprovalRejectedEvent`. Port + use case `ProcessApprovalUseCaseImpl.reject()` tidak butuh perubahan — sudah memanggil `publishRejected` dari awal.

## Task 3: Update PR listener untuk dual-decision

- **Status:** findings
- **Summary:** Buat `OnPurchaseRequisitionRejectedListener` baru (component terpisah) yang subscribe ke `ApprovalRejectedEvent` dan call `pr.reject()`. Tidak modifikasi listener APPROVED existing. Domain method `reject()` sudah ada — tidak butuh ditambahkan.

### Finding: Domain methods bernama `approve()`/`reject()`, bukan `markAsApproved()`/`markAsRejected()`
- **Type:** deviation
- **Severity:** info
- **Detail:** Plan menyebut method `markAsRejected()` (mengikuti naming convention DDD `markAsXxx`). Implementasi aktual di `PurchaseRequisition` aggregate pakai naming pendek: `approve()` dan `reject()` (line 95-101). Listener APPROVED existing call `pr.approve()`, jadi listener REJECTED yang baru juga pakai `pr.reject()` agar konsisten.
- **Action taken:** Listener baru memanggil `pr.reject()`. Tidak rename domain method (tidak dalam scope plan, dan listener APPROVED akan ikut break).
- **Ref:** src/main/java/com/solusi/erp/purchasing/purchaserequisition/domain/model/PurchaseRequisition.java:L95-L101
- **Ref:** src/main/java/com/solusi/erp/purchasing/purchaserequisition/infrastructure/listener/OnPurchaseRequisitionApprovedListener.java:L25 — `pr.approve()` pattern

### Finding: Separate listener component instead of dual-handler
- **Type:** decision
- **Severity:** info
- **Detail:** Plan asli (under rename strategy) akan extend listener existing dengan switch on decision field. Setelah strategi diubah ke event-class-per-decision, lebih natural buat component baru `OnPurchaseRequisitionRejectedListener` yang mirror struktur listener APPROVED. Mengikuti pattern Single Responsibility per listener.
- **Action taken:** Created `OnPurchaseRequisitionRejectedListener.java` di package yang sama. Listener APPROVED tidak diubah.
- **Ref:** src/main/java/com/solusi/erp/purchasing/purchaserequisition/infrastructure/listener/OnPurchaseRequisitionRejectedListener.java

## Task 4: Update PR Scenario B assertion + version bump PATCH

- **Status:** clean (E2E run deferred)
- **Summary:** Scenario B assertion direvert dari `getByText('REJECTED').first()` (workaround side-panel match) ke `.page-title .badge` dengan text REJECTED — mirror pattern Scenario A yang assert page-title badge APPROVED. Comment workaround dihapus dan diganti referensi ke Stream C resolution. pom.xml di-bump 1.6.1 → 1.6.2 (PATCH).
- **Note:** Full PR spec run (7/7 expected green) belum dieksekusi sebagai bagian dari execute-plan ini — butuh JAR build + start server. Validasi via E2E akan terjadi di session berikutnya atau saat user run smoke. Kode change minimal dan deterministic, risk regresi rendah.

## Task 4.5: Update dokumentasi terkait approval flow + PR status REJECTED

- **Status:** clean
- **Summary:** Sinkronkan 3 dokumen dengan implementasi baru:
  1. `docs/architecture/approval-arsitektur.md` — Section 3.3 menambahkan `ApprovalRejectedEvent(refType, refId)` ke flow event-driven. Code example "Langkah 4 — Listener" sekarang menampilkan dua listener terpisah (Completed + Rejected).
  2. `docs/modules/procurement/purchase-requisition.md` — Section 3.C "Alur Approval" menambahkan poin "Implementasi" yang menyebut dua listener: `OnPurchaseRequisitionApprovedListener` + `OnPurchaseRequisitionRejectedListener` dengan link ke approval-arsitektur.md.
  3. `docs/reports/e2e-pr-approval.md` — finding Task 10 "PR domain status does not flip on REJECTED" mendapat baris `Resolution:` baru yang link ke plan ini.
- **Note:** `docs/spec/` tidak punya file approval-related, jadi tidak ada yang di-sync di sana. `docs/AGENTS.md` Section 7/9 tidak menyebut `ApprovalCompletedEvent` secara eksplisit, hanya mention general approval pattern — tidak butuh perubahan.

## Task 5: Grant `STOCK-ADJUSTMENT_*` ke ROLE_WAREHOUSE

- **Status:** clean
- **Summary:** D010 sudah punya semua `STOCK-ADJUSTMENT_*` permission untuk ROLE_WAREHOUSE (READ/CREATE/UPDATE/DELETE/PROCESS) plus lookup pendukung. Tambahkan baris `STOCK-ADJUSTMENT_DELETE` yang sebelumnya tertinggal (D010 + V9000 mirror). Tidak ada D011 separate file untuk warehouse — D010 satu-satunya source. Verifikasi lookup matrix: `LOOKUP_INVENTORY/FACILITY/GRID/CONTAINER/UOM-CONVERSION/BRAND/PRODUCT-CATEGORY` semua sudah ter-grant.

## Task 6: V9000 seed Grid + Container + stock balance

- **Status:** clean (deviasi: skip initial balance/valuation seed)
- **Summary:** Append section "INVENTORY HIERARCHY" ke V9000: Grid id 9101 + Container id 9101 di facility 9101. Skip initial stock balance + valuation layer karena Scenario A jalur positive (quantity > 0) — Process to Inventory akan create balance + layer baru lewat aplikasi flow normal. Negative path (quantity < 0 yang butuh FIFO consumption) tidak masuk Scenario A-E scope sehingga seed minimal cukup.

### Finding: Initial stock balance opsional untuk SA positive flow
- **Type:** decision
- **Severity:** info
- **Detail:** Plan asli minta seed initial stock balance (10 × 8500000) + valuation layer agar negative SA tidak gagal di FIFO consumption. Setelah review scope: Scenario A-E semua DRAFT → COMPLETED dengan quantity positif (add stock). Negative flow (subtract stock) tidak ada di scope. Seed balance/layer awal tidak diperlukan. Jika scope berkembang (Scenario F negative), seed bisa ditambahkan saat itu.
- **Action taken:** V9000 hanya seed Grid + Container. Balance + valuation layer dibuat oleh Process to Inventory aplikasi saat first run.
- **Ref:** src/main/resources/db/migration-h2/V9000__e2e_seed_data.sql section "INVENTORY HIERARCHY"

## Task 7: Helper cascading TomSelect

- **Status:** clean
- **Summary:** Tambahkan dua helper di `e2e-tests/helpers/tomselect.ts`: `waitForTomSelectOptions(page, selector, predicateFn, timeout)` dan `setCascadingTomSelect(page, parentSelector, parentValue, childSelector, childValue, childOptionLoadHint)`. Pattern: set parent → trigger child reload via `clearOptions()` + `load()` → wait sampai child option target muncul → set child value. Tidak modifikasi helper existing.

## Task 8: SA spec skeleton

- **Status:** clean
- **Summary:** Buat `e2e-tests/tests/inventory/stock-adjustment.spec.ts` dengan storage state warehouse1, sanity test (navigate ke list + assert table visible), dan 5 `test.skip()` stub untuk Scenario A-E dengan komentar 1-line user journey. Tag describe `@inventory` (bukan @smoke). File compile bersih. Sanity test akan jalan saat E2E runner dijalankan.

## Task 9: SA Scenario A — Create DRAFT

- **Status:** clean (E2E run deferred)
- **Summary:** Implement Scenario A end-to-end. Wire helpers `resolveProductLaptopId(page)` (lookup-by-search), `pickIdrCurrency(page)` (DOM-pick IDR option then `setTomSelectValue` because page JS wraps `#header-currency` with TomSelect), `setQuantityViaDrawer(page, rowIndex, qty)` (open `#drawer-non-serial` via `.btn-edit-detail`, wait for UoM dropdown to populate from `/api/lookup/inventory/uom-conversions`, fill `.input-qty-target` AutoNumeric, click `.btn-save-drawer`). Test body: navigate create → set date/currency/facility → addLine → set product → wait for `.input-uom-id` populated by JS change handler → set Grid + Container via `select.select-grid`/`select.select-container` (cascading parents auto-resolve via TomSelect parent provider, no separate cascading helper needed because `setTomSelectValue` calls `addOption` if missing) → set quantity via drawer → set unitCost via AutoNumeric → submit → assert redirect → capture id from `a[href*="/edit/"]` highest id → reopen edit → assert page-title badge DRAFT.
- **Note:** Spec compiles (`npx tsc --noEmit` clean) and lists correctly (sanity + Scenario A active, B-E `.skip()`). Full E2E run deferred — butuh JAR build + `mvn spring-boot:run` dengan profile e2e. Validation akan terjadi saat user run `./e2e-tests/scripts/run-poc.sh` atau saat Stream A finalize di Task 15.

### Finding: Cascading TomSelect tidak butuh `setCascadingTomSelect` di Scenario A
- **Type:** decision
- **Severity:** info
- **Detail:** Plan Task 9 anticipate kebutuhan `setCascadingTomSelect` (helper Task 7) untuk Facility→Grid→Container. Setelah baca page JS (`stock-adjustment-form.js` line 196-199): grid TomSelect sudah pakai `parentProvider` callback yang baca `headerFacility.value` setiap kali load. Container TomSelect baca `tsGrid.getValue()`. Jadi setelah set Facility ke 9101, set Grid via `setTomSelectValue` akan trigger lookup dengan `facilityId=9101` query param → option Grid 9101 muncul karena seed V9000 sudah ada. Helper `setCascadingTomSelect` reserved untuk pattern lebih kompleks (mis. UI yang butuh wait pada AJAX response sebelum child options ready). Untuk Scenario A pattern langsung `setTomSelectValue` cukup karena `setTomSelectValue` sendiri call `addOption` kalau value tidak ditemukan.
- **Action taken:** Tidak pakai `setCascadingTomSelect` di Scenario A. Helper tetap eksis dan akan dipakai di Scenario D (facility change) atau saat user butuh Grid yang harus dilihat di dropdown options sebelum dipilih.
- **Ref:** src/main/resources/static/js/inventory/adjustment/stock-adjustment-form.js:L196-L199 — parentProvider pattern
- **Ref:** e2e-tests/helpers/tomselect.ts:L70-L83 — setTomSelectValue addOption fallback

### Finding: Quantity input readonly — drawer is the only commit path
- **Type:** decision
- **Severity:** info
- **Detail:** `lines[N].quantity` punya attribute `readonly` di template (form.html:L128, L164). Page JS hanya update via drawer save handler (`stock-adjustment-form.js:L171-L176`). `setAutoNumeric` ke `.input-qty` akan diabaikan oleh ErpNumeric karena field readonly. Karena itu helper `setQuantityViaDrawer` mandatory untuk SA — tidak ada bypass.
- **Action taken:** Buat helper `setQuantityViaDrawer` lokal di spec file (bukan helper umum) karena flow drawer ini SA-specific. Jika pattern muncul di modul lain (mis. Inventory Transfer), promote ke `helpers/`.
- **Ref:** src/main/resources/templates/inventory/adjustments/form.html:L128 — readonly attr
- **Ref:** src/main/resources/static/js/inventory/adjustment/stock-adjustment-form.js:L171-L176 — btn-save-drawer commit path

## Task 10: SA Scenario B — Edit DRAFT

- **Status:** clean (E2E run deferred)
- **Summary:** Extract `createSampleDraftSa(page)` helper from Scenario A body. Body Scenario B: create draft → navigate edit → assert 1 line row visible → setQuantityViaDrawer 5 → 7 → AJAX submit → reopen edit → read `lines[0].quantity` and assert pattern `^7(\.0+)?$` (AutoNumeric formats with 2 decimals so accept "7" or "7.00").

## Task 11: SA Scenario C — Process to Inventory

- **Status:** clean (E2E run deferred)
- **Summary:** Body: create draft → navigate edit → assert `#btn-process-inventory` visible → install dialog accept handler (ErpAction.confirmAndSubmit may use native confirm) → click button → wait for URL `/inventory/adjustments/view/{id}` → assert page-title badge COMPLETED → second navigation `/edit/{id}` to validate controller redirect to `/view/{id}` per StockAdjustmentController.java:L122-L123.

## Task 12: SA Scenario D — Facility change clears lines

- **Status:** clean (E2E run deferred)
- **Summary:** Body: create draft → edit → assert 1 line → install both `page.on('dialog', accept)` AND a Bootstrap modal accept fallback (`.modal.show .btn-primary`) since ErpModal.confirm bisa pakai modal atau native confirm. Trigger change: clear `#header-facility` TomSelect → re-set to 9101 → modal/dialog accept fires → assert `#line-container tr.line-row` count 0 + `#empty-msg` visible.

### Finding: Only one E2E facility seeded — Scenario D triggers via clear+re-set
- **Type:** decision
- **Severity:** info
- **Detail:** V9000 hanya seed 1 facility (9101 E2E Main Warehouse). Plan Task 12 menyarankan "decision saat eksekusi: seed facility ke-2 ATAU ganti currency". Memilih: clear TomSelect dulu lalu set ulang ke 9101. Page JS (`stock-adjustment-form.js:L298-L308`) hook ke `change` event TomSelect facility — yang fire pada clear DAN pada set value baru. Lebih ekonomis dari nambah facility kedua di V9000, dan secara semantik tetap valid: user mengganti facility (operasi yang men-trigger reset rule).
- **Action taken:** Spec clear TomSelect via JS lalu setTomSelectValue ke 9101. Tidak modifikasi V9000 untuk facility kedua.
- **Ref:** src/main/resources/static/js/inventory/adjustment/stock-adjustment-form.js:L298-L308 — facility change handler

## Task 13: SA Scenario E — Delete DRAFT

- **Status:** findings (deviation: API call instead of list-page UI)
- **Summary:** Plan minta delete via tombol row di list page. Setelah baca `templates/inventory/adjustments/list.html`, list TIDAK punya tombol delete di per-row actions (hanya Edit + View). DELETE endpoint `DELETE /inventory/adjustments/{id}` tetap ada dengan `STOCK-ADJUSTMENT_DELETE` guard. Spec body: create draft → navigate list → fetch DELETE dengan CSRF → assert status < 300 → reload list → assert tidak ada `a[href="/inventory/adjustments/edit/{id}"]`.

### Finding: List page does not expose per-row delete
- **Type:** gap
- **Severity:** warning
- **Detail:** Plan Task 13 mengasumsikan ada delete button di list (mirror master-data CRUD pattern). `list.html` line 99-115 hanya render Edit (DRAFT only) + View di per-row actions. DELETE endpoint exists di controller (`StockAdjustmentController.java:L171-L178`) tapi tidak di-wire ke UI. Ada dua interpretasi: (1) memang tidak boleh delete dari list (current product decision), (2) bug — UI lupa wire delete button.
- **Action taken:** Spec men-test endpoint langsung via fetch karena: (a) endpoint exists dan ter-permission-guard, (b) ROLE_WAREHOUSE punya STOCK-ADJUSTMENT_DELETE per Task 5, (c) verifikasi server-side delete + permission tetap valuable. Naming test direvisi dari "delete from list page" → "delete via API endpoint" untuk akurat reflect what's tested. Jika di kemudian hari UI delete ditambahkan, switch test ke pattern click-button.
- **Ref:** src/main/resources/templates/inventory/adjustments/list.html:L99-L115 — actions block, no delete
- **Ref:** src/main/java/com/solusi/erp/inventory/adjustment/web/controller/StockAdjustmentController.java:L171-L178 — endpoint exists

## Task 14: RBAC sample matrix spec

- **Status:** clean (E2E run deferred)
- **Summary:** Buat `e2e-tests/tests/auth/rbac.spec.ts` parametric matrix 4 roles × 4 resources = 16 cases. Helper `classify(page, listUrl)` membandingkan final URL path + HTTP status untuk klasifikasi allow/deny. Per-case juga assert visibility tombol Create di list page (allow rows): `expect(btn).toBeVisible()` saat role punya `*_CREATE`, `expect(btn).toHaveCount(0)` saat tidak. Dual-context pattern (`browser.newContext` per case) untuk role switching tanpa storage state collision. Tag describe `@rbac` (bukan `@smoke`). File compile bersih, list correctly menampilkan 16 cases.
- **Note:** Behavior aktual deny case (403 page vs redirect ke /dashboard vs /error/403) baru bisa diverifikasi saat E2E runner jalan. Helper `classify` sengaja dibuat permissive: `deny` = final URL bukan resource path ATAU status >= 400. Jika ada role+resource case yang ternyata Spring redirect ke list parent (mis. /dashboard) instead of 403, classify akan tetap return deny — semantically correct.

### Finding: PermissionGroup createUrl tidak diuji
- **Type:** decision
- **Severity:** info
- **Detail:** PermissionGroup hanya admin role yang bisa READ (untuk Stream B sample). Plan asli mention "admin only" di matrix. Spec set `createUrl: null` dan `createVisible: null` untuk PermissionGroup row — visibility check di-skip karena admin RBAC tidak butuh dibedakan create vs read di task ini. Jika di kemudian hari perlu, tambah `createUrl` + flag.
- **Action taken:** PermissionGroup test hanya validasi list URL access. 4 roles × 1 resource = 4 cases tanpa Create assertion.
- **Ref:** e2e-tests/tests/auth/rbac.spec.ts — RESOURCES.permGroup definition

## Task 15: Smoke wiring + version bump MINOR

- **Status:** clean (full suite cold run deferred)
- **Summary:** Verifikasi `npx playwright test --grep @smoke --list` hanya menampilkan 4 spec smoke + 4 setup auth (= 10 entries total) sesuai plan: login (3 cases), brand create (1), product create (1), PR Scenario A (1). SA spec tagged `@inventory`, RBAC spec tagged `@rbac` — tidak masuk smoke. pom.xml dibumb MINOR 1.6.2 → 1.7.0.
- **Note:** Full suite cold run (51 cases dalam ~4-5 menit) ditunda — butuh JAR build + start app dengan profile e2e + delete `.auth/`. User akan eksekusi manual saat siap merge.

## Task 16: Update playwright-e2e-guide.md

- **Status:** clean
- **Summary:** Update guide:
  1. Section 10 coverage table — tambah row `tests/inventory/stock-adjustment.spec.ts` (1 sanity + 5 scenario, tag `@inventory`) dan `tests/auth/rbac.spec.ts` (16 case 4×4 matrix, tag `@rbac`).
  2. Status terakhir di Section 10 — update dari "29/29 (~1.8m)" ke "51/51 target (~4-5m)" dengan note Stream A+B sudah terimplementasi tetapi run validation deferred.
  3. Section 11 — tambah subsection 11.6 "Drawer-driven readonly field (Stock Adjustment)" mendokumentasikan pola `setQuantityViaDrawer` dan kapan dipakai (input readonly + drawer commit handler), dan 11.7 "Cascading TomSelect" mendokumentasikan helper `setCascadingTomSelect` + kapan tidak butuh dipakai (saat page JS sudah pakai `parentProvider`).
  4. Section 12 baru "RBAC Matrix Pattern" — strukture matrix, per-case dual context, klasifikasi allow/deny, tag `@rbac`.
  5. Renumber Sections 12→13 (Troubleshooting), 13→14 (Checklist Spec Baru), 14→15 (Agent Handoff), 15→16 (Kapan Update). Sub-sections 12.1-12.10 → 13.1-13.10 ikut terupdate.
  6. Section 16 — tambah bullet baru cross-link ke `docs/plans/e2e-sa-rbac-pr-reject.md` agar agen berikutnya tahu sumber Stream A+B.
- **Note:** Update endpoint langsung Section 11.5 — sebut SA Delete sebagai contoh kedua (di samping PR Cancel) dan link ke report Task 13.

## Post-execution fixes (2026-05-20)

After Stream A+B implementation merged, full E2E run revealed 11 failures across SA + RBAC specs. Three independent root causes identified — none cascading. All fixed in this session before any further commits to the stream.

### Finding: SA Scenario A-E `resolveProductLaptopId` called from `about:blank`

- **Type:** bug
- **Severity:** warning
- **Detail:** `resolveProductLaptopId(page)` was invoked at the first line of Scenario A's body and inside `createSampleDraftSa()` BEFORE any `navigateToModule()` call. At that point the page is still on `about:blank`, so `fetch('/api/lookup/inventory/products?q=...', { credentials: 'same-origin' })` issued via `page.evaluate` cannot resolve the relative URL — it has no origin to anchor against. Result: response is null, helper throws `'E2E-PRD-LAPTOP not found via lookup'`. Sanity test #24 passed because it navigates first; tests 25-29 failed in 150-250ms (too fast to be a real UI flow). Plan Task 9 step 3 actually showed the correct sequence (`navigateToModule` → `resolveProductLaptopId`) but the implementation reordered them.
- **Action taken:** Replaced `page.evaluate(() => fetch(...))` with `page.request.get(...)` in `resolveProductLaptopId`. Playwright's `APIRequestContext` carries the storage-state cookies and resolves relative URLs against `baseURL`, so the helper works regardless of page navigation state. This is also more robust against future spec ordering changes.
- **Ref:** e2e-tests/tests/inventory/stock-adjustment.spec.ts:L17-L31 — pre-fix
- **Ref:** e2e-tests/tests/inventory/stock-adjustment.spec.ts — post-fix uses `page.request.get`

### Finding: RBAC deny cases mis-classified — `error/403` view rendered with HTTP 200

- **Type:** bug
- **Severity:** critical
- **Detail:** `GlobalExceptionHandler.handleAccessDeniedException` returned the view name `"error/403"` without a `@ResponseStatus` annotation. Spring rendered the 403 page with the default 200 OK status, and the URL stayed on the requested resource path (no redirect). The spec's `classify()` helper used `status >= 400` and "URL still on resource" as deny signals — neither fired, so deny cases were tagged `'allow'` and failed the matrix expectation. Compare with `handleNoResourceFoundException` (line 192) which correctly carries `@ResponseStatus(HttpStatus.NOT_FOUND)`. Plan Task 14 had flagged this as a runtime-discovery item ("verify behavior at runtime — 403 vs redirect vs error page") but discovery was deferred along with the E2E run.
- **Action taken:** Added `@ResponseStatus(HttpStatus.FORBIDDEN)` to `handleAccessDeniedException`, mirroring the 404 handler. The HTML view still renders normally; only the response status flips to 403. AJAX/API branch already returned `ResponseEntity.status(FORBIDDEN)` so it is unaffected. This restores correct HTTP semantics — 403 pages bring 403 status — and unblocks RBAC test classification.
- **Ref:** src/main/java/com/solusi/erp/core/exception/GlobalExceptionHandler.java:L159-L171 — pre-fix
- **Ref:** src/main/java/com/solusi/erp/core/exception/GlobalExceptionHandler.java — post-fix mirrors handleNoResourceFoundException pattern

### Finding: RBAC `admin :: PermissionGroup list -> allow` pointed to API-only URL

- **Type:** bug
- **Severity:** warning
- **Detail:** Spec resource `permGroup` declared `listUrl: '/security/permission-groups'` — but that path is exclusively the API endpoint (`PermissionGroupApiController` at `/api/security/permission-groups` plus a no-op alias at `/security/permission-groups`). The Thymeleaf controller (`PermissionGroupController`) is mounted at `/security/menu-groups` because the module was rebranded as "Menu Groups" in the UI (per docs/modules/security/permission-groups.md and migration V19 which seeds `MENU-GROUP_*` permissions). Spring threw `NoResourceFoundException`, handler returned `error/404` view with status 404, classify returned `deny` — failing the admin/allow expectation. Implementation took the URL from the entity name in the plan rather than from `@RequestMapping`.
- **Action taken:** Updated `RESOURCES.permGroup.listUrl` to `/security/menu-groups` with a comment explaining the API/view split. ROLE_ADMIN already has `MENU-GROUP_READ` via V19 wildcard grant (`WHERE p.name LIKE 'MENU-GROUP\_%'`), so admin row will pass; other three roles have no MENU-GROUP grants in V9000, so deny rows remain valid.
- **Ref:** e2e-tests/tests/auth/rbac.spec.ts:L64-L70 — RESOURCES.permGroup
- **Ref:** src/main/java/com/solusi/erp/security/permissiongroup/web/PermissionGroupController.java:L35 — `@RequestMapping("/security/menu-groups")`
- **Ref:** src/main/resources/db/migration/V19__Add_Localized_Permission_Groups.sql:L46-L56 — admin auto-grant

### Pattern note: runtime-validation gap

All three bugs share one cause: end-to-end runtime validation was deferred per the report's "E2E run deferred" notes on Tasks 4, 9-14, and 15. Plan had explicit GOTCHAs for two of them (Task 14 deny-behavior, Task 9 navigation order). Smoke split (`@smoke` covers PR Scenario A only) means push-to-main stays green and these only surface during the cold full-suite run. Mitigation for next stream: at minimum sanity-run each new spec once before marking the task complete, even if the full suite is left for finalize.

### Version bump

`pom.xml` 1.7.0 → 1.7.1 (PATCH, bug fix per AGENTS.md Section 9.A).

## Post-execution fixes — Round 2 (2026-05-20)

After Round 1 fixes were applied, the next `run-poc.ps1` run regressed across 30+ tests including specs that were previously green (master-data CRUD, PR scenarios A-F, login redirect). Diagnosis revealed two infrastructure hygiene bugs unrelated to the original SA/RBAC stream — but they masked whether Round 1 fixes worked because the running server was the wrong build.

### Finding: `run-poc.ps1` boots stale JAR after version bump

- **Type:** bug
- **Severity:** warning
- **Detail:** Round 1 bumped `pom.xml` 1.7.0 → 1.7.1. After `mvnw package`, both `target/solusi-program-erp-1.7.0.jar` (left over from the previous build) AND the new `1.7.1.jar` lived side by side. Script picked JAR via `(Get-ChildItem "target\solusi-program-erp-*.jar")[0]` which is alphabetical order — so `1.7.0.jar` (the OLD build) was started. Run output proved this: header line read `=== Starting server: F:\solusi-program-erp\target\solusi-program-erp-1.7.0.jar ===`. None of the Round 1 fixes (handler `@ResponseStatus(FORBIDDEN)`, etc.) were active.
- **Action taken:** `e2e-tests/scripts/run-poc.ps1` now (1) deletes any pre-existing `solusi-program-erp-*.jar` in `target/` before invoking `mvnw package`, and (2) selects the JAR by `LastWriteTime -Descending | Select-Object -First 1` instead of alphabetical first. Defensive: even if a developer drops a stray JAR mid-run, the newest is picked.
- **Ref:** e2e-tests/scripts/run-poc.ps1:L7-L20 — pre/post fix

### Finding: `global.setup.ts` reuses dead storage state when server restarts

- **Type:** bug
- **Severity:** critical
- **Detail:** Setup tests use a 30-minute time-based freshness check on `.auth/<role>.json`:
  ```ts
  function isStateFresh(file: string): boolean {
    const stat = fs.statSync(file);
    return Date.now() - stat.mtimeMs < FRESH_TTL_MS;
  }
  ```
  This is fundamentally wrong for an H2 in-memory backend. Every server restart wipes session storage instantly, so saved cookies become invalid the moment the JVM dies — but the file mtime stays "fresh" for 30 min. Run #2 happened within 30 min of run #1, so all four setup tests skipped login and reused dead cookies. Every authenticated test then ran un-authenticated → got redirected to `/login` → `expect(...).toBeVisible()` waited the full `expect.timeout: 10_000` ms before failing. That is the exact `~11.5s` signature seen in 11 master-data + sanity tests.
- **Action taken:** Replaced `isStateFresh` with `isStateValid(file, baseURL)` — opens an `APIRequestContext` with the saved storage state, GETs `/dashboard` with `maxRedirects: 0`, accepts only 2xx as "still authenticated". Any 3xx (especially redirect to `/login`) or error → fall through to `loginAndSaveState`. The probe costs ~50ms per role (4 roles, ~200ms total) and replaces the file-age heuristic with a ground-truth signal.
- **Ref:** e2e-tests/global.setup.ts — full file rewrite of the freshness check

### Pattern note: H2 in-memory + persisted on-disk state

H2 in-memory wipes on every JVM restart, but Playwright `.auth/` state lives on disk across runs. Any persistence mechanism that reads disk state without revalidating against the live server will desync after the first server restart. The original time-based check assumed sessions outlive 30 min — true for production cookies, false for E2E ephemeral DB. Future helpers that cache anything backed by H2 (e.g., user IDs, generated codes) should follow the same probe-don't-trust-mtime pattern.

### Version bump (Round 2)

No additional `pom.xml` bump — Round 2 fixes are infrastructure (test harness + script) outside the Maven build artifact. The 1.7.1 bump from Round 1 still covers the underlying handler change. If a single combined commit bundles Round 1 + Round 2, 1.7.1 is correct.

## Post-execution fixes — Round 3 (2026-05-20)

After Round 1 + 2 applied, full run regressed only the five SA scenarios (sanity passed; RBAC + master-data + PR all green). Failure timing: ~6.5s = 5s `waitForFunction` timeout in spec + ~1.5s overhead. Diagnosis below.

### Finding: `setTomSelectValue` injects options without `payload`, breaking SA product change handler

- **Type:** bug
- **Severity:** critical
- **Detail:** Page JS `stock-adjustment-form.js:204-212` binds `tsProd.on('change', ...)` and reads `tsProd.options[val].payload.uomId` to populate `.input-uom-id`. The lookup API (`ProductLookupController` + `LookupDto`) returns each option as `{id, name, subText, payload: { uomId, uomName, isSerialized, lastCost }}` — payload is the carrier of all secondary product metadata. The spec's `setTomSelectValue` helper (`tomselect.ts:71-83`) only injects `{id, name, text}` when adding a missing option — no payload. So `tsProd.options[val].payload` is `undefined`, `p.uomId` is `undefined`, `.input-uom-id.value` stays empty, and the spec's next `waitForFunction(el.value !== '', { timeout: 5_000 })` times out. All five scenarios share `createSampleDraftSa()` which fails at the same step, hence the uniform 6.5s signature.
- **Action taken:** In Scenario A and `createSampleDraftSa`, swapped `setTomSelectValue` for `selectTomSelect(page, ..., 'E2E-PRD-LAPTOP')` on the product field only. `selectTomSelect` calls TomSelect's actual `load()` function, which routes through the lookup AJAX endpoint and returns the full payload — so the product change handler can fill `.input-uom-id` correctly. Other TomSelect interactions (facility, grid, container) keep `setTomSelectValue` because their page handlers do not depend on payload.
- **Cleanup:** Removed two now-unused `const productId = await resolveProductLaptopId(page)` calls. The helper `resolveProductLaptopId` itself stays — kept for future scenarios that need the id outside of the form interaction.
- **Ref:** src/main/resources/static/js/inventory/adjustment/stock-adjustment-form.js:L204-L212 — product change handler reads payload
- **Ref:** e2e-tests/helpers/tomselect.ts:L71-L83 — setTomSelectValue addOption shape (no payload)
- **Ref:** e2e-tests/tests/inventory/stock-adjustment.spec.ts — fixed call sites in createSampleDraftSa + Scenario A

### Pattern note: helper choice depends on page-handler payload dependency

`setTomSelectValue` is fine when the page only cares about the selected `value` (e.g., a hidden form field that is read on submit). It breaks when the page registers a `change` listener that reads `options[val].payload`. Future helpers should either (a) accept an optional `payload` arg and merge it into `addOption`, or (b) document the shape limitation. For now, the rule is: if the page wires a derived field (UoM, last price, serialized flag) off the selected option, route through `selectTomSelect` so the option arrives via the real lookup load.

## Post-execution fixes — Round 4 (2026-05-20)

Round 3 dropped failure count from 5 to 3. Final round addressed three distinct UI-shape mismatches in SA scenarios A, C, D. Final result: **51/51 green** in ~2.0 minutes (validated via `run-poc.ps1`).

### Finding: `selectTomSelect` helper has broken signature

- **Type:** bug
- **Severity:** warning
- **Detail:** Round 3 swapped `setTomSelectValue` for `selectTomSelect` on the product field. Post-fix, all 5 SA scenarios timed out at 30s (test-level timeout). Inspection of `e2e-tests/helpers/tomselect.ts:37` showed `ts.load(query, callback)` — but TomSelect's `load(query)` API does not accept a second-arg callback; the helper's Promise never resolves. The helper was effectively unused before Round 3 (all other specs use `setTomSelectValue`), so the bug had no detection signal.
- **Action taken:** Stopped using `selectTomSelect` in this spec. Added a local helper `selectProductOnLine(page, lineSelector, code)` that issues `page.request.get('/api/lookup/inventory/products?q=...')` (returns `LookupDto[]` with full payload), then injects the option via `tomselect.addOption(opt)` + `setValue(id)`. Bypasses both broken helpers and missing payloads in one round-trip. Removed unused `selectTomSelect` import.
- **Note:** `e2e-tests/helpers/tomselect.ts` `selectTomSelect` should be either fixed or removed in a follow-up — out of scope for this fix bundle. Filing as known issue.
- **Ref:** e2e-tests/helpers/tomselect.ts:L29-L48 — broken `ts.load(query, callback)` shape
- **Ref:** e2e-tests/tests/inventory/stock-adjustment.spec.ts — local `selectProductOnLine` helper

### Finding: Scenario A asserts badge on form/edit page that has no badge

- **Type:** bug
- **Severity:** warning
- **Detail:** Spec called `navigateToModule(page, '/inventory/adjustments/edit/{newId}')` then asserted `.page-title .badge` with text `DRAFT`. Reading `templates/inventory/adjustments/form.html` showed `.page-title` contains only `<span>` elements for label/code — no badge. The status badge lives in `templates/inventory/adjustments/view.html:L13-L17` as a `<div class="mt-1"><span class="badge">` sibling of `.page-title`. Spec mirrored the PR pattern blindly; PR's edit page has a badge, SA's does not.
- **Action taken:** Switched assertion to `/view/{newId}` page and selector `.page-header .badge`. Cross-cuts the same fix needed for Scenario C (badge for COMPLETED).
- **Ref:** src/main/resources/templates/inventory/adjustments/view.html:L13-L17 — badge location

### Finding: Scenario C/D handle `confirmAndSubmit` as native dialog instead of Bootstrap modal

- **Type:** bug
- **Severity:** warning
- **Detail:** Spec installed `page.on('dialog', d => d.accept())` for both scenarios. `ErpAction.confirmAndSubmit` (and `ErpModal.confirm` directly in the facility change handler) does NOT call `window.confirm` — it shows a Bootstrap modal `#modal-global-confirm` and binds the action to `#confirm-modal-btn-yes`. The native dialog handler matches nothing; modal stays open; no submit; test times out.
- **Action taken:** Replaced dialog handler with explicit click on `#confirm-modal-btn-yes` after triggering the action. For Scenario D specifically, `clear()` on the facility TomSelect is enough to fire the change event (no need to re-set a value or seed a second facility), and the Bootstrap modal callback wipes `#line-container`.
- **Ref:** src/main/resources/static/js/shared/erp-common-handler.js:L32-L56 — `ErpModal.confirm` modal mechanism
- **Ref:** src/main/resources/static/js/inventory/adjustment/stock-adjustment-form.js:L301-L304 — facility change wires `ErpModal.confirm`

### Final result

51/51 green in 2.0 minutes (cold run, fresh `.auth/`):
- 4 setup auth + 3 login + 16 RBAC + 1 SA sanity + 5 SA scenarios + 4 brand + 4 category + 3 product + 4 UoM + 7 PR scenarios = 51
- Smoke subset still 10/10 (3 login + 1 brand + 1 product + 1 PR Scenario A + 4 setup auth)


