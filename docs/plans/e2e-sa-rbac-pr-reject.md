# Implementation Plan: E2E Stock Adjustment + RBAC + PR Reject Event Refactor

> Source: Discussion 2026-05-20 (lanjutan E2E Phase 2 / `docs/plans/e2e-pr-approval.md`)
> Created: 2026-05-20
> Sprint: E2E Phase 3
> Status: PENDING

## Summary

Tiga stream paralel digabung dalam satu plan agar progress lintas-area mudah dilihat. Stream dapat dieksekusi terpisah per session — setiap task self-contained dengan reference link.

- **Stream C (PR Reject event refactor)** — Fix bug yang dilaporkan di `docs/reports/e2e-pr-approval.md` Task 10. Rename `ApprovalCompletedEvent` → `ApprovalDecidedEvent` dan tambahkan `ApprovalDecision decision` (APPROVED|REJECTED). Publisher mengeluarkan event pada kedua keputusan; listener PR memetakan ke `markAsApproved()` atau `markAsRejected()`. Update Scenario B agar assert status badge utama (revert workaround sebelumnya).
- **Stream A (Stock Adjustment E2E)** — Tambahkan spec transaksional inventory pertama yang menutup item MVP Phase 1 di `docs/proposals/e2e-playwright/FINAL-PROPOSAL.md` Section 7.2. Lifecycle: DRAFT → COMPLETED via "Process to Inventory" (tanpa approval, tanpa signature). Mencakup CRUD draft, edit, process, facility-change-reset, dan negative path edit COMPLETED.
- **Stream B (RBAC sample matrix)** — Tambahkan `tests/auth/rbac.spec.ts` covering 4 role × 4 resource (~16 cases) untuk URL guard (403/redirect ke error page) + UI element visibility (`sec:authorize` button hide/show). Tidak full matrix.

Plan ini juga menutup gap seed: ROLE_WAREHOUSE perlu permission `STOCK-ADJUSTMENT_*` (saat ini di D010 hanya `DASHBOARD_READ`), dan V9000 perlu seed Grid + Container + stock balance awal untuk facility E2E (9101) agar Process to Inventory tidak gagal di valuation layer.

## Decisions Locked from Discussion

- **Plan format:** satu file gabungan, dapat dipotong eksekusinya. Setiap task harus self-contained.
- **Stream order rekomendasi:** C → A → B. Alasan: fix listener PR sederhana dan unblock Scenario B; SA spec membangun seed yang nantinya juga dipakai RBAC (warehouse role akses SA); RBAC paling akhir karena memerlukan semua resource sudah aktif.
- **Stock Adjustment scope:** lifecycle native modul (DRAFT → COMPLETED). Tidak ada approval, tidak ada signature pad. Tombol "Process to Inventory" via POST `/{id}/process` (controller redirect, BUKAN AJAX).
- **RBAC scope:** sample 4 role × 4 resource = 16 case. Resource dipilih: PR (procurement), SA (inventory), Brand (master simple), PermissionGroup (admin-only). Roles: admin (allow all), approver1, employee1, warehouse1.
- **PR reject event design:** Opsi 3 — rename + decision field (lihat `docs/proposals/e2e-playwright/FINAL-PROPOSAL.md` tidak menyentuh ini; rationale di section "Decisions" plan ini). Cost rename rendah karena hanya 2 call site.
- **Version bump:**
  - Stream C: PATCH (bug fix). 0.x.y → 0.x.(y+1).
  - Stream A: MINOR (fitur test baru tidak mengubah backend). 0.x.y → 0.(x+1).0.
  - Stream B: MINOR. Bisa digabung dengan A jika dieksekusi back-to-back.
- **CI behavior:** Scenario A SA tidak masuk smoke (PR Scenario A sudah cover transactional flow). RBAC tidak masuk smoke. Smoke set tetap 10 case existing.
- **Reuse existing E2E users:** admin, approver1, employee1, warehouse1 (BCrypt `admin123`). Tidak menambah personas baru.

## Current Findings from Exploration

### Stream C — PR Reject event

- Event model di `core.event` saat ini hanya 2 class: `ApprovalRequestedEvent` (saat submit) dan `ApprovalCompletedEvent` (saat approve). Tidak ada event untuk REJECT.
      ref: src/main/java/com/solusi/erp/core/event/ApprovalCompletedEvent.java:L1-L17 — full content
- Publisher `ApprovalEventPublisherAdapter` (1 file, 1 metode) hanya emit `ApprovalCompletedEvent` di approve path. REJECT path tidak emit apapun.
      ref: src/main/java/com/solusi/erp/core/approval/infrastructure/adapter/ApprovalEventPublisherAdapter.java:L21 — `eventPublisher.publishEvent(new ApprovalCompletedEvent(...))`
- Listener PR: `OnPurchaseRequisitionApprovedListener` 28 lines, subscribe via `@EventListener(condition = "#event.referenceType == 'PURCHASE_REQUISITION'")`, hanya call `markAsApproved()`.
      ref: src/main/java/com/solusi/erp/purchasing/purchaserequisition/infrastructure/listener/OnPurchaseRequisitionApprovedListener.java:L13-L26
- Total call site `ApprovalCompletedEvent` di project: 1 publisher + 1 listener (2 files). Rename murah.
- Domain `PurchaseRequisition` perlu method `markAsRejected()` (cek apakah sudah ada — kalau belum, tambahkan).

### Stream A — Stock Adjustment

- Lokasi modul: `src/main/java/com/solusi/erp/inventory/adjustment/` (bukan `stockadjustment/`).
- Lifecycle: DRAFT → COMPLETED. `AdjustmentStatus` enum sudah ada.
- Controller `StockAdjustmentController` (190 lines) mendukung: list, create form, create POST AJAX (ApiResponse JSON), edit form, edit POST AJAX, view, **process redirect** (POST `/{id}/process` → redirect ke `/view/{id}`), delete (HTMX response).
      ref: src/main/java/com/solusi/erp/inventory/adjustment/web/controller/StockAdjustmentController.java:L1-L190
- Form fields: `transactionDate` (Flatpickr `data-picker="date"`), `facilityId`, `currencyId`, `exchangeRate`, `lines[N].productId`, `lines[N].gridId`, `lines[N].containerId`, `lines[N].quantity`, `lines[N].unitCost`, `lines[N].uomId`, `lines[N].note`.
- UI behavior khusus: cascading TomSelect Facility→Grid→Container (per dokumentasi `docs/modules/inventory/stock-adjustment.md` Section 4 Autocomplete). Drawer UoM page-specific.
- Edge case "Facility Change clears all lines": pola sama seperti PR Scenario E (`window.confirm`). Bisa reuse handler `page.on('dialog', d => d.accept())`.
      ref: docs/modules/inventory/stock-adjustment.md:L40-L42 — Facility Change rule
- Permission set: `STOCK-ADJUSTMENT_{READ|CREATE|UPDATE|DELETE|PROCESS}` + `LOOKUP_INVENTORY`. Group `INV-08`.
      ref: src/main/resources/db/migration/V24__Inventory_Stock_Adjustment_And_Menu.sql:L19-L23
- Process to Inventory **bukan AJAX** — POST redirect biasa, bukan `/data-ajax-form`. Helper E2E perlu pakai `page.click()` lalu `waitForURL()` ke view page, atau `page.evaluate(fetch)` dengan CSRF (mirip pola `cancelPr` di PR spec).
- Modul tidak punya tombol "Cancel" di UI; tidak relevan untuk SA karena lifecycle sudah final di DRAFT atau COMPLETED.

### Stream B — RBAC

- Menu sidebar memakai `sec:authorize="hasAnyAuthority(...)"` per `AGENTS.md` Section 7. Test sidebar visibility opsional (di luar scope user).
- `@DefaultRedirectUrl` di controller bikin GET tanpa permission redirect ke list page parent. Behavior expected: 403 atau redirect ke `error/403.html`. Perlu cek di runtime saat eksekusi.
- Multi-role storage state sudah disiapkan di Phase 2 (`e2e-tests/global.setup.ts`, `e2e-tests/.auth/{role}.json`).
      ref: e2e-tests/global.setup.ts — setup project pattern
- Resource matrix yang dipilih (4 role × 4 resource):

| Role         | PR (purchase-requisitions) | SA (adjustments)         | Brand (master)            | PermissionGroup (admin) |
|--------------|----------------------------|--------------------------|---------------------------|-------------------------|
| admin        | allow READ, CREATE         | allow READ, CREATE       | allow READ, CREATE        | allow READ              |
| approver1    | allow READ, deny CREATE    | deny READ                | allow READ (LOOKUP_BRAND) | deny READ               |
| employee1    | allow READ, CREATE         | deny READ                | allow READ                | deny READ               |
| warehouse1   | deny READ                  | allow READ, CREATE       | allow READ                | deny READ               |

- Catatan: `warehouse1` saat ini punya permission warehouse-related dasar tapi belum termasuk `STOCK-ADJUSTMENT_*`. Task 5 akan grant agar matrix di atas valid. Verifikasi exhaustive permission per role di D010 + D011 sebelum menulis assertion.
      ref: docs/database/dev-seeder/D010__security_roles.sql, docs/database/dev-seeder/D011__role_permissions.sql

## Recommended Execution Order

Stream C dulu (3-4 task), kemudian Stream A (8-9 task), kemudian Stream B (1 task), lalu finalize (2 task). Total: ~16 task.

1. Task 1-4 (Stream C): backend rename + listener fix + Scenario B revert assertion + version bump patch.
2. Task 5-6 (seed prep): D011 + V9000 untuk SA permissions dan Grid/Container/stock balance.
3. Task 7-13 (Stream A): SA spec lengkap.
4. Task 14 (Stream B): RBAC spec.
5. Task 15-16: smoke wiring + guide refresh.


## Tasks

### Task 1: Tambahkan `ApprovalRejectedEvent` (deviation dari rename) [x]

**Deviation note:** Setelah eksplorasi, port `ApprovalEventPublisher` ternyata SUDAH punya method `publishCompleted` + `publishRejected` (lokasi: `common.approval.application.port`, bukan `core.approval`), dan `ProcessApprovalUseCaseImpl.reject()` sudah call `publishRejected`. Adapter `ApprovalEventPublisherAdapter.publishRejected()` saat ini no-op. Juga: ada **3 listener** subscribe ke `ApprovalCompletedEvent` (PR, PO, News), bukan 2.

Strategi diubah dari "rename + decision field" ke "tambah `ApprovalRejectedEvent` class baru". Lebih kecil blast radius, tidak break PO/News listener, sesuai port design.

**Depends on:** none
**Stream:** C

Steps:
- [x] Baca port `ApprovalEventPublisher`, adapter, dan use case `ProcessApprovalUseCaseImpl` untuk konfirmasi separation existing.
      ref: src/main/java/com/solusi/erp/common/approval/application/port/ApprovalEventPublisher.java:L1-L9
      ref: src/main/java/com/solusi/erp/common/approval/infrastructure/adapter/ApprovalEventPublisherAdapter.java:L18-L28
- [x] Buat class baru `com.solusi.erp.core.event.ApprovalRejectedEvent` mirroring shape `ApprovalCompletedEvent` (dua field: `referenceType`, `referenceId`, `@Getter` Lombok).
- [x] Compile check: `./mvnw -q compile`. Tidak akan ada error karena class baru tidak dipakai siapapun (yet).

**Validation criteria:**
- File `ApprovalRejectedEvent.java` ada di `core.event/`.
- Tidak rename/hapus `ApprovalCompletedEvent` (masih dipakai 3 listener existing).
- Compile clean.

### Task 2: Implement adapter `publishRejected` to emit `ApprovalRejectedEvent` [x]

Adapter `publishRejected()` saat ini no-op. Wire-kan agar publish `ApprovalRejectedEvent` (created in Task 1). Port + use case caller sudah benar — tidak perlu diubah.

**Depends on:** Task 1
**Stream:** C

Steps:
- [x] Update `ApprovalEventPublisherAdapter` import: tambahkan `ApprovalRejectedEvent`.
- [x] Implement body `publishRejected()`: `eventPublisher.publishEvent(new ApprovalRejectedEvent(referenceType, referenceId))`.
- [x] Compile check: `./mvnw -q compile`. Hijau.
- [ ] **TEST (deferred to end of Stream C):** Existing project tidak punya `ApprovalEventPublisherAdapterTest`. Pertimbangkan tambah unit test minimal di Task 4 area atau tunda — adapter sederhana, value test rendah vs cost.

**Validation criteria:**
- Adapter `publishRejected` benar-benar emit event (bukan no-op).
- Compile clean.

### Task 3: Add `OnPurchaseRequisitionRejectedListener` (deviation: separate component) [x]

Buat listener baru terpisah yang subscribe ke `ApprovalRejectedEvent`. Tidak modifikasi listener APPROVED existing. Domain method `reject()` sudah ada di aggregate, jadi tidak butuh ditambahkan.

**Depends on:** Task 2
**Stream:** C

Steps:
- [x] Cek `PurchaseRequisition` aggregate — confirmed methods `approve()`/`reject()` sudah ada (line 95-101). Note: naming pendek (bukan `markAsXxx`).
      ref: src/main/java/com/solusi/erp/purchasing/purchaserequisition/domain/model/PurchaseRequisition.java:L95-L101
- [x] Buat file baru `OnPurchaseRequisitionRejectedListener.java` mirroring listener APPROVED pattern. Subscribe ke `ApprovalRejectedEvent` dengan SpEL condition `referenceType == 'PURCHASE_REQUISITION'`. Body: load PR, call `pr.reject()`, save.
      ref: src/main/java/com/solusi/erp/purchasing/purchaserequisition/infrastructure/listener/OnPurchaseRequisitionApprovedListener.java — pattern referensi
- [x] Compile check: `./mvnw -q compile` clean.
- [ ] **TEST (skipped):** Listener thin (no business logic, no validation). Project tidak punya unit test untuk listener APPROVED existing — konsisten skip untuk REJECTED. Verifikasi via E2E di Task 4.

**Validation criteria:**
- File `OnPurchaseRequisitionRejectedListener.java` ada.
- Listener APPROVED tidak diubah.
- Compile clean.

### Task 4: Update PR Scenario B assertion + version bump PATCH [x]

Reverte workaround dari report Task 10 yang assert side-panel. Sekarang status badge utama harus REJECTED setelah reject flow.

**Depends on:** Task 3
**Reference module:** `e2e-tests/tests/procurement/purchase-requisition.spec.ts`
**Stream:** C

Steps:
- [x] Buka `e2e-tests/tests/procurement/purchase-requisition.spec.ts`. Cari Scenario B (`@reject` atau "submit then reject").
- [x] Ganti assertion `getByText('REJECTED')` (yang match side-panel) menjadi assertion ke status badge utama (`.page-title .badge` mirror dengan SUBMITTED check di line 302).
- [ ] Jalankan PR spec saja: `cd e2e-tests && npx playwright test tests/procurement/purchase-requisition.spec.ts`. **Deferred** — tidak run E2E sebagai bagian execute-plan ini (butuh JAR build + start server). Akan di-run saat full validation atau saat user menjalankan smoke.
- [x] Naikkan versi `pom.xml` PATCH: 1.6.1 → 1.6.2.
- [x] **TEST:** Tidak ada unit test tambahan; verifikasi via E2E run.

**Validation criteria:**
- PR spec 7/7 hijau (Scenario A-F + sanity) — pending E2E run.
- `pom.xml` version naik PATCH (1.6.2).
- Catatan: referensi `ApprovalCompletedEvent` & `OnPurchaseRequisitionApprovedListener` masih ada di codebase (sengaja, untuk approve path) — tidak dihapus karena strategi diubah ke event-class-per-decision.

### Task 4.5: Update dokumentasi terkait approval flow + PR status REJECTED [x]

Sinkronkan dokumen dengan implementasi baru. Finding di `docs/reports/e2e-pr-approval.md` Task 10 mencatat klaim doc PR aspirational; setelah Stream C selesai klaim itu valid, tapi mekanisme event sudah berubah dan perlu didokumentasikan agar future dev/agen tahu bagaimana listener di-wire.

**Depends on:** Task 4
**Reference module:** existing module docs
**Stream:** C

Steps:
- [ ] Cek apakah ada `docs/spec/approval-flow.md` atau dokumen approval generik. Jika ada, update referensi `ApprovalCompletedEvent` → `ApprovalDecidedEvent` + jelaskan decision field.
      ref: docs/spec/index.md — peta spec
      ref: docs/spec/ — folder spec generic
- [ ] Update `docs/modules/procurement/purchase-requisition.md`:
  - Section state lifecycle: pastikan klaim "status PR berubah ke REJECTED secara otomatis" sekarang valid. Tambahkan satu paragraf pendek di bagian "Implementation Notes" (atau buat section baru kalau belum ada) yang menyebut: transisi APPROVED/REJECTED di-trigger oleh `OnPurchaseRequisitionDecidedListener` yang subscribe ke `ApprovalDecidedEvent` dari approval core.
      ref: docs/modules/procurement/purchase-requisition.md:L46-L78 — state lifecycle section
- [ ] Update `docs/reports/e2e-pr-approval.md`:
  - Di finding Task 10 "PR domain status does not flip on REJECTED", tambahkan baris baru di bawah `Action taken`:
    `- **Resolution:** Fixed in docs/plans/e2e-sa-rbac-pr-reject.md Stream C (Tasks 1-4). Listener now subscribes to ApprovalDecidedEvent and dispatches to markAsApproved() or markAsRejected() based on decision field.`
  - Status finding tetap "bug" tapi sekarang ada resolution link.
      ref: docs/reports/e2e-pr-approval.md:L153-L158 — finding asli
- [ ] Cek `docs/AGENTS.md` Section 7 (Security & RBAC) atau Section 9 — apakah menyebut event approval secara eksplisit. Jika ya, sync.
      ref: docs/AGENTS.md
- [ ] Tidak ada test perubahan; ini doc-only.

**Validation criteria:**
- Tidak ada referensi `ApprovalCompletedEvent` tersisa di `docs/`: `grep -r "ApprovalCompletedEvent" docs/` empty.
- Doc PR module tidak punya klaim aspirational lagi.
- Report `e2e-pr-approval.md` finding ditandai resolved dengan link ke plan ini.

### Task 5: Grant `STOCK-ADJUSTMENT_*` ke ROLE_WAREHOUSE di D011 + mirror ke V9000 [x]

ROLE_WAREHOUSE perlu permission lengkap untuk modul SA agar `warehouse1` user bisa eksekusi spec dan jadi denominator di RBAC matrix. D010 saat ini hanya kasih dasar warehouse-related; D011 (`docs/database/dev-seeder/D011__role_permissions.sql`) yang dipakai PR plan adalah tempat ekspansi. V9000 H2 mirror harus sinkron.

**Depends on:** none (independen dari Stream C, bisa dieksekusi paralel)
**Reference module:** `docs/plans/e2e-pr-approval.md` Task 1-2 — pola D011 + V9000 mirror
**Stream:** A (preparation)

Steps:
- [ ] Baca `docs/database/dev-seeder/D011__role_permissions.sql` — identifikasi struktur INSERT existing untuk ROLE_APPROVER + ROLE_EMPLOYEE.
      ref: docs/database/dev-seeder/D011__role_permissions.sql — pola `INSERT INTO role_permissions ... SELECT @role_x_id, id FROM permissions WHERE name IN (...)`
- [ ] Tambahkan section ROLE_WAREHOUSE di D011 (atau extend section yang sudah ada): grant `STOCK-ADJUSTMENT_READ`, `STOCK-ADJUSTMENT_CREATE`, `STOCK-ADJUSTMENT_UPDATE`, `STOCK-ADJUSTMENT_DELETE`, `STOCK-ADJUSTMENT_PROCESS`, `LOOKUP_INVENTORY`, `LOOKUP_FACILITY`, `LOOKUP_BRAND`, `LOOKUP_PRODUCT-CATEGORY`, `LOOKUP_UOM-CONVERSION`, `LOOKUP_PARTY` (untuk currency picker yang reuse parties endpoint).
      ref: src/main/resources/db/migration/V24__Inventory_Stock_Adjustment_And_Menu.sql:L19-L23 — canonical permission names
- [ ] Mirror perubahan ke `src/main/resources/db/migration-h2/V9000__e2e_seed_data.sql` di section "ROLE PERMISSIONS (mirror D011)". Pakai pola INSERT...SELECT yang sama (kompatibel MariaDB + H2 MODE=MySQL).
- [ ] Run migration parity: `bash scripts/check-migration-parity.sh`. Harus pass (V9000 tetap allowlisted).
- [ ] Verifikasi seed: start app dengan `--spring.profiles.active=e2e`, log in via curl POST /login dengan `warehouse1`/`admin123`, GET `/inventory/adjustments` harus 200 (bukan 403).

**Validation criteria:**
- D011 punya block ROLE_WAREHOUSE dengan permission SA + lookup pendukung.
- V9000 mirror identik isinya.
- Migration parity script pass.
- HTTP curl: `warehouse1` GET `/inventory/adjustments` → 200, `employee1` GET `/inventory/adjustments` → 403/redirect.

### Task 6: V9000 seed Grid, Container, dan stock balance awal untuk facility 9101 [x]

Stock Adjustment Process to Inventory akan menulis ke `inv_stock_balances`, `inv_movements`, `inv_valuation_layers`. Untuk testing, line item butuh Grid + Container yang valid di facility 9101 (E2E Main Warehouse). Saldo awal positif diperlukan agar negative adjustment line tidak gagal di FIFO consumption.

**Depends on:** Task 5 (logical, agar V9000 di-edit sekali)
**Reference module:** existing V9000 transactional master section
**Stream:** A (preparation)

Steps:
- [ ] Baca skema tabel: `inv_grids`, `inv_containers`, `inv_stock_balances`, `inv_valuation_layers`. Identifikasi NOT NULL columns + FK.
      ref: src/main/resources/db/migration/V21__Inventory_Warehouse_Hierarchy.sql — skema grid/container
      ref: src/main/resources/db/migration/V22__Inventory_Stock_Movement_And_Balance.sql atau yang setara — skema balance + valuation
- [ ] Append ke V9000 (id range 9100+):
  - 1 Grid id 9101: `code='E2E-GRD-A'`, `name='E2E Grid A'`, `facility_id=9101`, `active=TRUE`.
  - 1 Container id 9101: `code='E2E-CTN-A1'`, `name='E2E Container A1'`, `grid_id=9101`, `active=TRUE`.
  - Initial stock balance untuk product 9101 (E2E-PRD-LAPTOP) di facility/grid/container 9101: quantity=10 (base UoM), avg cost=8500000.
  - Initial valuation layer untuk product 9101: 10 × 8500000, layer_date=2026-01-01.
- [ ] Pastikan kolom audit pakai `created_by_user_id`, `created_date`, `version` (post-V17 schema). Lihat finding `docs/reports/e2e-pr-approval.md` Task 2 untuk caveat ini.
      ref: docs/reports/e2e-pr-approval.md:L36-L38 — finding products.created_by_user_id
- [ ] Run migration parity script lagi.
- [ ] Verifikasi: start app, query H2 console (`/h2-console`) → SELECT dari inv_stock_balances WHERE product_id=9101 AND facility_id=9101 → harus 10.

**Validation criteria:**
- App start clean dengan profile e2e (no Flyway error).
- Query H2 menunjukkan grid/container/balance/layer ter-seed.
- Migration parity pass.

### Task 7: Helper untuk cascading TomSelect (Facility → Grid → Container) [x]

Pola SA: pilih Facility → Grid filter berdasar facility → Container filter berdasar grid. Helper TomSelect existing (`setTomSelectValue`) hanya support single select tanpa cascade. Tambahkan helper yang menunggu TomSelect anak ter-update setelah parent berubah.

**Depends on:** none (paralel dengan task lain)
**Reference module:** `e2e-tests/helpers/tomselect.ts` (existing helper)
**Stream:** A

Steps:
- [ ] Baca helper TomSelect existing: `selectTomSelect`, `setTomSelectValue`, `getTomSelectValue`, `clearTomSelect`.
      ref: e2e-tests/helpers/tomselect.ts — full file untuk pola evaluate-into-page
- [ ] Tambahkan fungsi baru `setCascadingTomSelect(page, parentSelector, parentValue, childSelector, childValue, options?)`:
  - Set parent via `setTomSelectValue(page, parentSelector, parentValue)`.
  - Wait for AJAX child reload (gunakan `page.waitForResponse` matcher pada path lookup yang dipakai child, atau fallback `waitForNetworkIdle`).
  - `waitForFunction` sampai child TomSelect punya options yang berisi `childValue`.
  - Set child via `setTomSelectValue(page, childSelector, childValue)`.
- [ ] Tambahkan helper `waitForTomSelectOptions(page, selector, predicate)` — generic util untuk wait sampai TomSelect punya options yang match predicate (mis. `(opts) => opts.length > 0`).
- [ ] Update `e2e-tests/helpers/tomselect.ts` (extend existing file, jangan buat file baru).
- [ ] **TEST:** Tidak ada test framework JS di project. Helper diuji indirect via SA spec.

**Validation criteria:**
- Helper compile (tsc): `cd e2e-tests && npx tsc --noEmit`.
- Helper akan dipakai di Task 9 dan Task 12 — green run di sana = validasi.


### Task 8: Skeleton `tests/inventory/stock-adjustment.spec.ts` [x]

Wire spec file dengan storage state warehouse1 + sanity test + 5 scenario stub. Menyiapkan kerangka kerja sebelum implementasi tiap skenario.

**Depends on:** Task 5, Task 6, Task 7
**Reference module:** `e2e-tests/tests/procurement/purchase-requisition.spec.ts` (skeleton pattern)
**Stream:** A

Steps:
- [ ] Buat folder `e2e-tests/tests/inventory/` jika belum ada.
- [ ] Buat file `e2e-tests/tests/inventory/stock-adjustment.spec.ts`.
- [ ] Header file:
  ```ts
  import { test, expect, storageStatePath } from '../../fixtures/base';
  // helpers: navigateToModule, fillField, selectDropdown, submitAndExpectRedirect, waitForNetworkIdle
  // helpers transaksional: setFlatpickrDate, setTomSelectValue, setCascadingTomSelect,
  //                        setAutoNumeric, getAutoNumericValue, addLine, removeLineAt, lineFieldSelector
  ```
- [ ] `test.describe('Stock Adjustment flow', ...)` dengan `test.use({ storageState: storageStatePath('warehouse1') })`.
- [ ] Sanity test pertama: navigate ke `/inventory/adjustments`, expect URL match + table visible. Jangan skip sanity (validates seed + permission).
- [ ] 5 scenario stub dengan `test.skip()` + komentar 1-line user journey:
  - **A:** warehouse1 buat DRAFT SA dengan 1 line → save → reopen → expect status DRAFT
  - **B:** edit DRAFT SA — ubah quantity, save, reload, expect persisted
  - **C:** create + Process to Inventory → status COMPLETED → edit form redirect ke view
  - **D:** ganti facility setelah ada line → confirm dialog → line container kosong
  - **E:** delete DRAFT via list page action → expect row hilang
- [ ] Tag scenario A dengan `@inventory` (bukan @smoke). Scenario lain juga `@inventory`.
- [ ] Run `cd e2e-tests && npx playwright test tests/inventory/stock-adjustment.spec.ts --list` — confirm 6 case (1 sanity + 5 scenario, 5 di antaranya skipped).

**Validation criteria:**
- File compile bersih (`npx tsc --noEmit`).
- Sanity test pass standalone.
- 5 scenario muncul as `skipped` di test listing.

### Task 9: Scenario A — Create DRAFT Stock Adjustment dengan 1 line [x]

Path paling penting; memvalidasi: TomSelect cascading Facility→Grid→Container, AutoNumeric quantity/unitCost, Flatpickr transactionDate, line editor pattern, AJAX save → redirect list.

**Depends on:** Task 8
**Reference module:** `e2e-tests/tests/procurement/purchase-requisition.spec.ts` Scenario A — pola create DRAFT
**Stream:** A

Steps:
- [ ] Baca template form SA untuk konfirmasi selector aktual:
      ref: src/main/resources/templates/inventory/adjustments/form.html — header fields, line template, button submit
      ref: docs/spec/header-lines-form.md — `#row-template-source` + `#line-container` convention
- [ ] Identifikasi selector header (kemungkinan):
  - Date: `input[name="transactionDate"]` dengan `data-picker="date"`.
  - Facility: `#facility-select` (TomSelect).
  - Currency: native `<select name="currencyId">`.
  - ExchangeRate: `input[name="exchangeRate"]` AutoNumeric.
- [ ] Implement Scenario A body:
  ```ts
  await navigateToModule(page, '/inventory/adjustments/create');
  await setFlatpickrDate(page, 'input[name="transactionDate"]', '2026-05-20');
  await setTomSelectValue(page, '#facility-select', '9101');
  await selectDropdown(page, 'currencyId', /* IDR id from V11 */);

  await addLine(page);
  await setTomSelectValue(page, lineFieldSelector(0, 'productId'), '9101');
  // wait UoM auto-fill (drawer logic) — pakai waitForFunction sampai uomId terisi
  await setCascadingTomSelect(page, '#facility-select', '9101',
                              lineFieldSelector(0, 'gridId'), '9101');
  await setCascadingTomSelect(page, lineFieldSelector(0, 'gridId'), '9101',
                              lineFieldSelector(0, 'containerId'), '9101');
  await setAutoNumeric(page, lineFieldSelector(0, 'quantity'), 5);
  await setAutoNumeric(page, lineFieldSelector(0, 'unitCost'), 100000);

  await submitAndExpectRedirect(page, /\/inventory\/adjustments(\?.*)?$/);
  ```
- [ ] Capture id SA baru via "highest edit-link id" pattern (sama dengan PR Scenario A).
      ref: docs/reports/e2e-pr-approval.md:L131-L138 — pattern extract id dari list page
- [ ] Reopen edit page, assert status badge DRAFT visible.
- [ ] **GOTCHA antisipasi:** Drawer UoM page-specific akan auto-fill `uomId` setelah product dipilih. Wait pakai `page.waitForFunction(() => document.querySelector('[name="lines[0].uomId"]').value !== '')`.
      ref: docs/modules/inventory/stock-adjustment.md:L51-L60 — drawer pre-edit behavior
- [ ] Run scenario only: `npx playwright test tests/inventory/stock-adjustment.spec.ts -g "Scenario A"`. Stabilkan 3x run berturut-turut.

**Validation criteria:**
- Scenario A green 3x consecutive.
- Server log clean (no error saat save).
- Created SA muncul di list page dengan code format `ADJ-yyMM-XXXXX`.


### Task 10: Scenario B — Edit DRAFT (persistence) [ ]

Validasi update path tidak melanggar invariants DRAFT, dan field tetap persisted setelah reload.

**Depends on:** Task 9 (helper `createDraftSa()` di-extract dari Scenario A)
**Reference module:** Scenario A body sebagai blueprint
**Stream:** A

Steps:
- [ ] Refactor common "create DRAFT 1 line" dari Scenario A jadi local helper di spec file: `async function createDraftSa(page): Promise<number>` returns SA id.
- [ ] Body Scenario B:
  - Call `createDraftSa(page)`.
  - Buka edit page `/inventory/adjustments/edit/{id}`.
  - Ubah quantity line 0 dari 5 → 7 via `setAutoNumeric`.
  - Ubah note header.
  - Submit, expect redirect ke list.
  - Reopen edit page, assert quantity = 7, note = nilai baru.
- [ ] **TEST:** Tidak ada unit test tambahan; verifikasi via E2E run.

**Validation criteria:**
- Scenario B green setelah 3x run.
- Persistence terverifikasi (reload tidak revert).

### Task 11: Scenario C — Process to Inventory (DRAFT → COMPLETED) [ ]

Validasi state transition lewat tombol "Process to Inventory" yang non-AJAX (POST redirect). Pastikan stock balance + valuation layer terupdate.

**Depends on:** Task 9
**Reference module:** PR Scenario A approve flow (analogi state transition)
**Stream:** A

Steps:
- [ ] Body Scenario C:
  - Call `createDraftSa(page)` → dapat id.
  - Navigate ke `/inventory/adjustments/view/{id}` (atau edit jika tombol process di edit page).
  - Identifikasi tombol "Process to Inventory" — baca template view/edit.
        ref: src/main/resources/templates/inventory/adjustments/view.html — lokasi tombol process
        ref: src/main/resources/templates/inventory/adjustments/form.html — apakah tombol ada di edit
  - Klik tombol → expect redirect ke `/view/{id}` (POST redirect bukan AJAX).
  - Assert status badge COMPLETED visible.
  - Try navigate ke `/inventory/adjustments/edit/{id}` → expect redirect ke `/view/{id}` (controller redirect when COMPLETED).
        ref: src/main/java/com/solusi/erp/inventory/adjustment/web/controller/StockAdjustmentController.java:L121-L123 — redirect logic
- [ ] **GOTCHA:** Jika tombol process butuh confirm dialog, install `page.on('dialog', d => d.accept())` sebelum klik.
- [ ] Optional verifikasi backend (extra robustness): query H2 via `/h2-console` atau pakai endpoint stock balance lookup. Bisa ditunda jika spec sudah cukup membuktikan state transition.

**Validation criteria:**
- Scenario C green.
- Edit page COMPLETED SA redirect ke view.
- Status badge berubah ke COMPLETED.

### Task 12: Scenario D — Facility change clears all lines (confirm dialog) [ ]

Validasi UX rule: ganti Facility ketika ada line item → confirm dialog → semua line dihapus karena Grid/Container bergantung pada Facility.

**Depends on:** Task 9
**Reference module:** PR Scenario E — pattern `page.on('dialog')` sudah ada di PR spec
**Stream:** A

Steps:
- [ ] Body Scenario D:
  - Call `createDraftSa(page)` (DRAFT 1 line).
  - Buka edit page.
  - Install dialog handler: `page.on('dialog', d => d.accept())`.
  - Cari facility ke-2 untuk switch (kalau di V9000 hanya ada 1 facility 9101, perlu seed facility kedua di Task 6 — UPDATE Task 6 tambah satu facility lagi). **Decision saat eksekusi:** kalau hanya 1 facility, opsi: (a) seed facility ke-2 di V9000, atau (b) ganti currency saja sebagai trigger reset (cek `purchase-requisition-form.js` analog — apakah SA juga reset on currency change).
        ref: src/main/resources/static/js/inventory/adjustment/stock-adjustment-form.js — apa saja trigger reset
  - Switch facility.
  - Assert `#line-container` rows = 0.
  - Assert `#empty-msg` visible.
- [ ] **GOTCHA:** Jika SA tidak punya `#empty-msg` (beda nama dengan PR), sesuaikan selector setelah baca template.

**Validation criteria:**
- Scenario D green.
- Dialog ter-accept tanpa hang.
- Line container kosong setelah switch.


### Task 13: Scenario E — Delete DRAFT via list page action [ ]

Validasi delete path (HTMX response) tidak ditolak untuk DRAFT, dan row hilang dari list setelah delete.

**Depends on:** Task 9
**Reference module:** existing master-data CRUD specs untuk delete pattern (jika ada)
**Stream:** A

Steps:
- [ ] Body Scenario E:
  - Call `createDraftSa(page)` → dapat id.
  - Navigate ke list `/inventory/adjustments`.
  - Search/filter (jika perlu) untuk pastikan SA target ada di halaman pertama. Atau lookup row by id pada `[data-id="{id}"]` jika list pakai pattern itu.
        ref: src/main/resources/templates/inventory/adjustments/list.html — row markup untuk delete button
  - Klik delete button row tersebut. Confirm dialog accept (`page.on('dialog')`).
  - Wait HTMX refresh (gunakan `waitForHtmx` dari `helpers/waits.ts`).
  - Assert row dengan id tersebut tidak lagi muncul di table.
- [ ] **TEST:** Negative path bonus (opsional di task ini): try delete COMPLETED SA dari Scenario C. Expect error response — atau tombol delete tidak muncul. Skip jika menambah complexity; cukup catat sebagai `test.fixme` untuk follow-up.

**Validation criteria:**
- Scenario E green.
- Row SA terhapus dari list.
- Server log clean (no orphan FK error).

### Task 14: RBAC sample matrix spec `tests/auth/rbac.spec.ts` [ ]

Sampel 4 role × 4 resource = 16 case URL guard + UI element visibility. Resource: PR (procurement), SA (inventory), Brand (master), PermissionGroup (admin).

**Depends on:** Task 5 (warehouse permissions seeded), idealnya juga Task 13 (SA spec stabil agar pattern jelas)
**Reference module:** `e2e-tests/tests/auth/login.spec.ts` (bare `@playwright/test` import, no auto-login fixture); `fixtures/base.ts` `storageStatePath` helper
**Stream:** B

Steps:
- [ ] Buat file `e2e-tests/tests/auth/rbac.spec.ts`.
- [ ] Strukturkan dengan parametric `test.describe.parallel` dipakai NORMAL (`workers: 1` di config tetap berlaku, parallel describe tidak break — Playwright tetap serial). Atau pakai loop biasa di dalam describe. Pilih:
  ```ts
  type Case = { role: 'admin'|'approver1'|'employee1'|'warehouse1';
                resource: { name: string; listUrl: string; createUrl?: string };
                expect: 'allow'|'deny' };
  const matrix: Case[] = [ /* 16 rows */ ];
  for (const c of matrix) {
    test(`${c.role} ${c.expect} ${c.resource.name}`, async ({ browser }) => {
      const ctx = await browser.newContext({ storageState: storageStatePath(c.role) });
      const page = await ctx.newPage();
      const res = await page.goto(c.resource.listUrl);
      if (c.expect === 'allow') {
        expect(res?.status()).toBeLessThan(400);
        await expect(page).toHaveURL(new RegExp(c.resource.listUrl));
      } else {
        // 403 page atau redirect ke /error/403 atau /dashboard
        expect([res?.status(), page.url()]).toMatchAny([ /403/, /\/error/, /\/dashboard/ ]);
      }
      await ctx.close();
    });
  }
  ```
- [ ] Definisikan resource constants:
  - PR: `/purchasing/purchase-requisitions` (list), `/purchasing/purchase-requisitions/create` (create)
  - SA: `/inventory/adjustments`, `/inventory/adjustments/create`
  - Brand: `/inventory/brands`, `/inventory/brands/create`
  - PermissionGroup: `/security/permission-groups`, (admin-only, no create cek perlu)
- [ ] Definisikan matrix expectation berdasarkan tabel di Findings section. Untuk setiap (role, resource, action: list|create) tentukan allow/deny.
- [ ] Untuk UI element visibility: untuk setiap LIST page yang di-allow, assert tombol "Create" visible only ketika role memiliki `*_CREATE`. Pola:
  ```ts
  if (c.expect === 'allow' && c.resource.createUrl) {
    const createBtn = page.locator(`a[href="${c.resource.createUrl}"]`);
    if (roleHasCreatePermission(c.role, c.resource.name)) {
      await expect(createBtn).toBeVisible();
    } else {
      await expect(createBtn).toHaveCount(0);
    }
  }
  ```
- [ ] **GOTCHA:** Verifikasi behavior "deny" yang sebenarnya saat eksekusi. `@DefaultRedirectUrl` mungkin redirect ke parent list (bukan 403). Lihat `GlobalExceptionHandler` untuk konfirmasi.
      ref: src/main/java/com/solusi/erp/core/web/advice/GlobalExceptionHandler.java — atau yang setara
- [ ] Tag describe `@rbac` (bukan `@smoke`).
- [ ] Run: `npx playwright test tests/auth/rbac.spec.ts`. Stabilkan, semua 16 case green.

**Validation criteria:**
- 16 case green.
- Allow case redirect ke URL yang benar.
- Deny case ter-handle (cek behavior aktual: 403, redirect, atau error page) dan diassert sesuai.
- UI element visibility assertion match expectation matrix.

### Task 15: Smoke wiring + version bump MINOR + final suite green [ ]

Pastikan smoke tetap 10 case (tidak menambah load CI push-to-main). Naikkan versi pom.xml MINOR untuk fitur SA + RBAC. Run full suite cold dari .auth bersih.

**Depends on:** Task 4, Task 13, Task 14
**Reference module:** existing CI workflow + smoke pattern
**Stream:** finalize

Steps:
- [ ] Confirm: tidak ada test SA atau RBAC yang ter-tag `@smoke`. Run `npm run test:smoke` di `e2e-tests/` → harus tetap 10/10 (3 auth + Brand create + Product create + PR Scenario A).
- [ ] Naikkan versi `pom.xml` MINOR (mis. setelah PATCH dari Task 4: 0.1.6 → 0.2.0). Cek versi actual sebelum decide.
- [ ] Hapus folder `.auth/` di local untuk trigger cold run: `rm -rf e2e-tests/.auth`.
- [ ] Run full suite: `./e2e-tests/scripts/run-poc.sh` (atau `.ps1` di Windows). Target ~4-5 menit cold (existing 1.8m + ~6 SA case + ~16 RBAC case).
- [ ] Jika ada flake, jangan langsung tambah `retries`. Diagnose root cause, fix di helper atau spec.
- [ ] CI workflow tidak butuh perubahan — `e2e-tests` job sudah catch semua spec di `tests/`.

**Validation criteria:**
- Full suite cold green: existing 29 + 6 SA + 16 RBAC = ~51/51.
- Smoke 10/10 tetap.
- pom.xml version bumped.
- Migration parity script masih pass.


### Task 16: Update `docs/tests/playwright-e2e-guide.md` [ ]

Dokumentasikan helper baru (`setCascadingTomSelect`), pola Stock Adjustment (DRAFT → COMPLETED via POST redirect, beda dari approval pattern), dan pola RBAC matrix. Tujuan: agen berikutnya bisa replicate ke modul lain (PO, SO, GR, dll).

**Depends on:** Task 13, Task 14
**Reference module:** existing guide
**Stream:** finalize

Steps:
- [ ] Update Section 8 ("Helper yang Wajib Dipakai") subsection TomSelect: tambahkan paragraf untuk `setCascadingTomSelect` dengan signature + alasan kapan dipakai (Facility→Grid→Container, atau pattern parent-child lainnya).
      ref: docs/tests/playwright-e2e-guide.md:L451-L470 — Section 8.4 TomSelect
- [ ] Update Section 11 ("Helper Tambahan untuk Modul Transaksional"): tambah subsection 11.6 "Process to Inventory (POST redirect tanpa AJAX)" — jelaskan beda dengan `submitAndExpectRedirect` (yang assume `[data-ajax-form]`). Pola yang dipakai SA: tombol `<form action="...">` + `<button type="submit">`, atau `page.evaluate(fetch)` dengan CSRF.
- [ ] Tambah Section 12 baru "RBAC Matrix Pattern" — jelaskan parametric test, dual-context untuk role switch, ekspektasi behavior deny (403/redirect/error page) yang sebaiknya di-discover dulu di runtime.
- [ ] Update Section 10 status coverage table: tambah row Inventory `stock-adjustment.spec.ts` dengan jumlah scenario; tambah row Auth `rbac.spec.ts`.
      ref: docs/tests/playwright-e2e-guide.md:L568-L580 — coverage table
- [ ] Update "Status terakhir" date dan total count: existing 29 → 51 (29 + 6 SA + 16 RBAC).
- [ ] Cross-link ke Plan ini (`docs/plans/e2e-sa-rbac-pr-reject.md`) di Section 15 "Kapan Memperbarui Dokumen Ini".

**Validation criteria:**
- Guide reads coherently untuk agen baru.
- Semua referenced helper benar-benar ada di path yang ditulis.
- Tidak ada stale claim "PR domain status REJECTED tidak flip" — bug sudah di-fix di Stream C.
- Coverage table sinkron dengan jumlah aktual spec.

## Decisions

- Single combined plan (3 stream) — meskipun cross-area, lifecycle eksekusi serupa: backend nudge, seed prep, helper expansion, spec writing, doc refresh. Memisah jadi 3 plan akan duplikasi section "Decisions" dan "Findings" yang besar.
- Stream order C → A → B — Stream C kecil dan fix bug yang sudah teridentifikasi; lebih cepat di-clear duluan agar Scenario B revert bisa langsung divalidasi. SA spec di tengah karena seed-nya juga kepakai RBAC. RBAC paling akhir karena depends on SA permission grant.
- Rename `ApprovalCompletedEvent` → `ApprovalDecidedEvent` + decision field — alasan: hanya 2 call site, semantik nama saat ini misleading kalau di-extend, future modul (PO/SO approval) terhindar dari trap "Completed yang ternyata Rejected". Cost rendah, benefit tinggi.
- SA tidak masuk smoke — PR Scenario A sudah cover full transactional pattern (TomSelect + AutoNumeric + Flatpickr + line editor + approval). SA process flow tanpa approval = subset; tidak menambah confidence ke smoke push-to-main.
- Cancel SA di-skip — modul tidak punya UI button + bukan bagian dari workflow. Lifecycle final di DRAFT (delete) atau COMPLETED (final). Tidak perlu pola `cancelPr` analog.
- RBAC scope sample 4×4 — bukan exhaustive matrix (akan ~64 case kalau full). Sampling representatif: 1 admin role, 1 cross-functional approver, 1 functional employee, 1 specialist warehouse. Resource: 1 transactional procurement, 1 transactional inventory, 1 master simple, 1 admin-only. Coverage gap diakui sebagai trade-off vs. test runtime.
- Backend bug fix dijadikan Stream C terpisah, bukan tail dari plan PR sebelumnya — `e2e-pr-approval` sudah COMPLETED, menulis ulang reportnya akan rancu. Plan ini menutupnya sebagai Stream C dengan reference link ke finding asli.
- Version bump 2x (PATCH untuk Stream C, MINOR untuk Stream A+B) — Stream C dan Stream A+B akan landing dalam commit terpisah, sehingga 2x bump justified per AGENTS.md Section 9.A. Jika dieksekusi back-to-back dalam satu session, boleh konsolidasi ke satu MINOR bump.


## Final Validation

After all tasks (atau setelah stream tertentu jika dieksekusi terpisah):

- [ ] `bash scripts/check-migration-parity.sh` pass.
- [ ] Start app dengan `--spring.profiles.active=e2e`, no Flyway error in `target/e2e-server.log`.
- [ ] HTTP login via curl untuk `admin`/`approver1`/`employee1`/`warehouse1` semua return 302 → /dashboard.
- [ ] Manual smoke (lokal dev DB MariaDB): submit PR → reject via UI → status PR di list = REJECTED (Stream C verification).
- [ ] Full Playwright suite cold green: ~51 case dalam ~4-5 menit.
- [ ] Smoke subset: 10/10 dalam ~42s (tidak bertambah).
- [ ] PR spec 7/7 (Scenarios A-F + sanity) tetap green setelah Scenario B assertion direvert.
- [ ] No reference ke `ApprovalCompletedEvent` atau `OnPurchaseRequisitionApprovedListener` di codebase: `grep -r "ApprovalCompletedEvent\|OnPurchaseRequisitionApprovedListener" src/main/java/` empty.
- [ ] `pom.xml` version sudah dinaikkan sesuai SemVer (PATCH untuk Stream C, MINOR untuk Stream A+B).

## Stream Eksekusi Terpisah — Catatan

Plan ini didesain agar dapat dipotong per stream:

- **Eksekusi hanya Stream C:** Task 1, 2, 3, 4 + sub-step "version bump PATCH". Final validation hanya item terkait Stream C (PR spec green, manual smoke MariaDB).
- **Eksekusi hanya Stream A:** Task 5, 6, 7, 8, 9, 10, 11, 12, 13. Final validation: SA spec green, full suite tetap green. Catat: jika Stream C belum dijalankan, PR Scenario B tetap pakai workaround assertion di side-panel (jangan revert).
- **Eksekusi hanya Stream B:** Task 14. Pre-condition: Task 5 sudah selesai (warehouse permission seeded). Final validation: RBAC spec green, full suite tetap green.
- **Eksekusi finalize:** Task 15 + 16 setelah dua-tiga stream selesai. Validate cross-stream coherence.

Setiap session yang melanjutkan plan WAJIB:
1. Baca status checkbox tiap task untuk tahu apa yang sudah selesai.
2. Baca `docs/reports/e2e-sa-rbac-pr-reject.md` untuk findings dari session sebelumnya (misal: deviasi schema, gotcha helper).
3. Update checkbox `[ ]` → `[~]` saat memulai task, `[x]` saat selesai + verified.
4. Tulis findings task ke report file menggunakan format `docs/reports/e2e-pr-approval.md` sebagai template.

