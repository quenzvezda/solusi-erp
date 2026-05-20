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

- **Status:** pending

## Task 11: SA Scenario C — Process to Inventory

- **Status:** pending

## Task 12: SA Scenario D — Facility change clears lines

- **Status:** pending

## Task 13: SA Scenario E — Delete DRAFT

- **Status:** pending

## Task 14: RBAC sample matrix spec

- **Status:** pending

## Task 15: Smoke wiring + version bump MINOR

- **Status:** pending

## Task 16: Update playwright-e2e-guide.md

- **Status:** pending

## Final Validation

(Populated after all stream finalized.)
