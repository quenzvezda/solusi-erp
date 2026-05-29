# Playwright E2E Guide — Solusi ERP

> Panduan operasional untuk developer dan AI agent yang akan menjalankan, memperbaiki, atau menambah test end-to-end Playwright di Solusi ERP.
>
> Fokus dokumen ini adalah **konteks repo aktual**: profile `e2e`, H2 migration mirror, struktur `e2e-tests`, helper yang wajib dipakai, pola test CRUD, dan troubleshooting yang sudah terbukti dari PoC.

## 1. Ringkasan Arsitektur

E2E Playwright di repo ini menjalankan aplikasi Spring Boot sungguhan dengan profile khusus `e2e`, lalu browser Chromium menguji UI Thymeleaf seperti user normal.

Alurnya:

1. Build JAR Spring Boot dengan Maven profile `e2e`.
2. Start aplikasi di `http://localhost:18080` dengan `--spring.profiles.active=e2e`.
3. Aplikasi memakai H2 in-memory database, bukan MariaDB.
4. Flyway menjalankan migration mirror dari `src/main/resources/db/migration-h2`.
5. Playwright menjalankan test dari `e2e-tests/tests`.
6. Test login menggunakan user seed `admin` / `admin123`.

File utama:

| Area | File / Folder | Fungsi |
|---|---|---|
| Spring profile | `src/main/resources/application-e2e.yaml` | Konfigurasi server port `18080`, H2, Flyway H2, Thymeleaf no-cache |
| H2 migrations | `src/main/resources/db/migration-h2/` | Mirror migration MariaDB yang sudah disesuaikan untuk H2 |
| Runtime dependency | `pom.xml` profile `e2e` | Memasukkan H2 runtime dependency saat build/run E2E |
| Data/auth seeder | `src/main/java/com/solusi/erp/security/user/security/E2eDataSeeder.java` | Mencoba menonaktifkan forced password change untuk profile `e2e` |
| Playwright config | `e2e-tests/playwright.config.ts` | Config test runner, base URL, retry, screenshot/video/trace |
| Test root | `e2e-tests/tests/` | Spec Playwright |
| Fixtures | `e2e-tests/fixtures/base.ts` | Auto-login untuk test yang memakai fixture ini |
| Helpers | `e2e-tests/helpers/` | Helper form, auth, navigation, TomSelect, AutoNumeric, wait |
| Local runner | `e2e-tests/scripts/run-e2e.ps1` / `.sh` | Build app, start server, install Playwright, run tests |
| IntelliJ runner | `.run/E2E Tests (Windows).run.xml` | One-click local E2E runner di JetBrains IDE |
| CI | `.github/workflows/ci-java21.yml` job `e2e-tests` | E2E di GitHub Actions setelah `full-tests` |

## 2. Prinsip Penting

### 2.1 Jangan pakai database production/dev untuk E2E

E2E profile memakai H2 in-memory:

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:erp_e2e;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;NON_KEYWORDS=VALUE
  flyway:
    locations:
      - classpath:db/migration-h2
```

Implikasi:

- Test harus deterministic dan tidak bergantung pada data lokal developer.
- Setiap run mulai dari database kosong, lalu Flyway + seeder membangun state awal.
- Jika migration MariaDB baru dibuat, migration H2 mirror juga wajib ditambahkan/disesuaikan.

### 2.2 H2 migration mirror harus 1:1 secara versi

Folder `db/migration-h2` adalah mirror dari migration MariaDB, tetapi syntax-nya bisa berbeda agar kompatibel dengan H2.

Hal yang pernah menyebabkan failure:

- Dua folder migration aktif sekaligus (`db/migration` + `db/migration-h2`) dengan versi sama → Flyway duplicate version.
- `ESCAPE '\\'` tidak kompatibel dengan H2 tertentu.
- `ALTER TABLE ... AFTER column_name` tidak didukung H2.
- Multi `ADD COLUMN` / `ADD CONSTRAINT` dalam satu `ALTER TABLE` perlu dipecah.
- `MODIFY COLUMN`, `CHANGE COLUMN`, `DROP FOREIGN KEY`, `UPDATE ... JOIN ... SET` perlu rewrite ke syntax H2.

Rule praktis:

- Untuk profile `e2e`, Flyway hanya boleh membaca `classpath:db/migration-h2`.
- Jangan mengaktifkan `db/migration` bersamaan untuk E2E.
- Saat menambah migration MariaDB, buat versi yang sama di `migration-h2`.

### 2.3 Test harus serial

`e2e-tests/playwright.config.ts` menetapkan:

```ts
fullyParallel: false,
workers: 1,
```

Alasannya:

- Test CRUD menulis ke database yang sama.
- Beberapa test mengedit seeded record dengan ID stabil.
- Parallel run bisa membuat data race dan flakiness.

Jangan menaikkan `workers` tanpa merombak strategi data isolation.

## 3. Cara Menjalankan

### 3.1 Prerequisites lokal

Minimal:

- Java 21.
- Maven wrapper dari repo (`mvnw` / `mvnw.cmd`).
- Node.js 20+.
- npm.
- Chromium dependency akan diinstall oleh Playwright script.

Windows direkomendasikan memakai PowerShell 7 (`pwsh`) bila tersedia.

### 3.2 Windows one-shot runner

Dari root project:

```powershell
.\e2e-tests\scripts\run-e2e.ps1
```

Script melakukan:

1. `mvnw.cmd -B clean package -DskipTests -Pe2e -q`
2. Start JAR dengan `--spring.profiles.active=e2e`
3. Wait `http://localhost:18080/login` sampai HTTP 200
4. `npm ci --silent`
5. `npx playwright install chromium`
6. `npx playwright test`
7. Stop process Java di akhir

Output server ditulis ke:

- `target/e2e-server.log`
- `target/e2e-server-err.log`

Catatan Windows UX:

- Jika Java window mengganggu, start process sebaiknya memakai `-WindowStyle Hidden` saat menjalankan Java.
- Pastikan perubahan ini diverifikasi di script runner karena runner saat ini adalah sumber kebenaran lokal.

### 3.3 Linux/macOS one-shot runner

Dari root project:

```bash
./e2e-tests/scripts/run-e2e.sh
```

Jika file belum executable:

```bash
chmod +x e2e-tests/scripts/run-e2e.sh
```

Script melakukan:

1. `./mvnw -B clean package -DskipTests -Pe2e -q`
2. Start JAR dengan `--spring.profiles.active=e2e`
3. Redirect output server ke `target/e2e-server.log` dan `target/e2e-server-err.log`
4. Wait `http://localhost:18080/login` sampai HTTP 200
5. `npm ci --silent`
6. `npx playwright install chromium`
7. `npx playwright test`
8. Stop process Java di akhir

Untuk Linux fresh machine yang belum punya dependency OS Chromium, jalankan sekali dengan:

```bash
INSTALL_PLAYWRIGHT_DEPS=1 ./e2e-tests/scripts/run-e2e.sh
```

Mode ini menjalankan `npx playwright install chromium --with-deps` dan dapat meminta password `sudo` karena Playwright perlu menginstall package sistem. Untuk daily run atau IntelliJ runner, gunakan mode default tanpa `INSTALL_PLAYWRIGHT_DEPS` agar fokus output tetap pada Playwright.

### 3.4 Menjalankan Playwright saja

Jika server sudah hidup di `http://localhost:18080`:

```bash
cd e2e-tests
npm ci
npx playwright test
```

Atau pakai npm script:

```bash
cd e2e-tests
npm run test
npm run test:headed
npm run test:debug
npm run test:ui
npm run report
```

`E2E_BASE_URL` bisa dioverride:

```bash
E2E_BASE_URL=http://localhost:18080 npx playwright test
```

Di Windows PowerShell:

```powershell
$env:E2E_BASE_URL = 'http://localhost:18080'
npx playwright test
```

### 3.5 CI behavior

Job `e2e-tests` di `.github/workflows/ci-java21.yml`:

- Berjalan setelah `full-tests` (`needs: [full-tests]`).
- Berjalan untuk schedule, manual dispatch full, atau push ke `main`/`master`.
- Tidak berjalan untuk PR biasa.
- Menggunakan Java 21 dan Node.js 20.
- Build: `./mvnw -B clean package -DskipTests -Pe2e -q`.
- Start app dengan profile `e2e`.
- Menjalankan `npx playwright test` di folder `e2e-tests`.
- Upload artifact:
  - `e2e-tests/playwright-report/`
  - `e2e-tests/test-results/`

## 4. Struktur Playwright

```text
e2e-tests/
  fixtures/
    base.ts
  helpers/
    auth.ts
    autonumeric.ts
    data-factory.ts
    form.ts
    navigation.ts
    tomselect.ts
    waits.ts
  tests/
    auth/
      login.spec.ts
    master-data/
      brand.spec.ts
      product-category.spec.ts
      product.spec.ts
      uom.spec.ts
  package.json
  playwright.config.ts
  tsconfig.json
```

### 4.1 Config runner

`e2e-tests/playwright.config.ts`:

- `testDir: './tests'`
- `timeout: 30_000`
- `expect.timeout: 10_000`
- `workers: 1`
- `retries: process.env.CI ? 1 : 0`
- `baseURL: process.env.E2E_BASE_URL || 'http://localhost:18080'`
- `trace: 'on-first-retry'`
- `screenshot: 'only-on-failure'`
- `video: 'retain-on-failure'`
- Browser project: Chromium Desktop Chrome

### 4.2 Fixture auto-login

CRUD specs memakai:

```ts
import { test, expect } from '../../fixtures/base';
```

Fixture `base.ts` meng-override `page` agar login sebelum setiap test.

Untuk test auth/login sendiri, gunakan Playwright asli:

```ts
import { test, expect } from '@playwright/test';
```

Jangan memakai auto-login fixture untuk test yang memang menguji login atau unauthenticated redirect.

## 5. Auth dan Password Change

Credential E2E saat ini:

```ts
admin / admin123
```

Helper `e2e-tests/helpers/auth.ts`:

1. `page.goto('/login')`
2. Isi username/password.
3. Submit.
4. Tunggu URL keluar dari `/login`.
5. Jika diarahkan ke halaman password/change-password, helper mengisi semua input password dengan password yang sama, submit, lalu tunggu keluar dari halaman password.

Kenapa helper tetap menangani forced password change?

- Ada `E2eDataSeeder` profile `e2e` yang men-set `passwordChangeRequired(false)` untuk admin.
- Namun urutan `CommandLineRunner` bisa membuat initializer utama tetap membuat admin dengan forced password change.
- Jadi fallback di login helper diperlukan agar test tidak fragile.

Jika login test gagal:

- Cek apakah `/login` bisa diakses di `localhost:18080`.
- Cek `target/e2e-server.log` dan `target/e2e-server-err.log`.
- Jalankan auth spec saja:

```bash
cd e2e-tests
npx playwright test tests/auth/login.spec.ts --headed
```

## 6. Data Test dan Seeded ID

Pattern yang dipakai saat ini:

- Data baru memakai `uniqueName(prefix)` dari `helpers/data-factory.ts`.
- Edit test memakai seeded ID stabil seperti `9001`.
- List test kadang assert seeded label, misalnya:
  - Brand: `E2E Brand Beta`
  - Product Category: `E2E Category Service`
- UoM list tidak assert nama spesifik karena data seeded bisa banyak dan pagination bisa memindahkan data E2E ke halaman lain.

Rule praktis:

- Untuk create test, gunakan nama unik agar tidak bentrok antar run.
- Untuk edit test, gunakan record seeded stabil.
- Jangan assert data created muncul di halaman pertama jika modul punya pagination/data seed banyak.
- Jika list assertion menemukan duplicate text, scope ke table dan pakai `.first()`.

Contoh aman:

```ts
await expect(page.locator('table').locator(`text=${name}`).first()).toBeVisible();
```

Contoh yang rawan strict mode:

```ts
await expect(page.locator(`text=${name}`)).toBeVisible();
```

## 7. Pola CRUD Test

### 7.1 List page

```ts
await navigateToModule(page, '/inventory/brands');
await expect(page.locator('table')).toBeVisible();
```

Jika ada seeded record yang pasti terlihat di halaman pertama:

```ts
await expect(page.locator('table').locator('text=E2E Brand Beta').first()).toBeVisible();
```

### 7.2 Create page

```ts
await navigateToModule(page, '/inventory/brands/create');

const name = uniqueName('Brand');
await fillField(page, 'name', name);

await submitAndExpectRedirect(page, /\/inventory\/brands(\?.*)?$/);
await expect(page.locator('table').locator(`text=${name}`).first()).toBeVisible();
```

Penting:

- Redirect regex harus anchored sampai akhir path.
- Jangan pakai `/\/inventory\/brands/` saja karena itu juga match `/inventory/brands/create`.
- Gunakan `/(\?.*)?$/` agar tetap match jika list page menambahkan query string.

### 7.3 Edit page

```ts
await navigateToModule(page, '/inventory/brands/edit/9001');

const newName = uniqueName('Brand-Edit');
await fillField(page, 'name', newName);

await submitAndExpectRedirect(page, /\/inventory\/brands(\?.*)?$/);
await expect(page.locator('table').locator(`text=${newName}`).first()).toBeVisible();
```

### 7.4 Validation error

```ts
await navigateToModule(page, '/inventory/brands/create');
await fillField(page, 'name', '');

const submitBtn = page.locator('[data-ajax-form] button[type="submit"]').first();
await submitBtn.click();

await expectFormError(page);
```

Jika field spesifik perlu dicek:

```ts
await expectFormError(page, 'name');
```

## 8. Helper yang Wajib Dipakai

### 8.1 Navigation

`navigateToModule(page, url)`:

- `page.goto(url)`
- `page.waitForLoadState('networkidle', { timeout: 15_000 })`

Gunakan ini untuk pindah module daripada langsung `page.goto()` di CRUD spec.

### 8.2 Form fields

`fillField(page, name, value)`:

- Mencari `input[name="..."]` atau `textarea[name="..."]`.
- Clear lalu fill.

`selectDropdown(page, name, value)`:

- Memakai `page.selectOption` untuk native `<select>`.
- Jangan pakai untuk TomSelect.

### 8.3 AJAX form submit

`submitAndExpectRedirect(page, urlPattern)`:

- Mencari submit button dengan selector `[data-ajax-form] button[type="submit"]`.
- Wait visible.
- Click.
- Wait URL sesuai pattern.

Kenapa selector ini spesifik?

- Banyak form punya lebih dari satu button submit/action.
- Selector generic `button[type="submit"]` pernah menyebabkan strict mode violation.

Jika submit timeout:

- Pastikan form punya `data-ajax-form`.
- Pastikan page-specific/global JS form handler sudah initialized.
- Pastikan click tidak terjadi sebelum JS siap.
- Cek browser console dan network di trace/video.

### 8.4 TomSelect

`page.fill()` tidak reliable untuk TomSelect karena UI yang terlihat bukan `<select>` asli.

Gunakan helper:

```ts
await selectTomSelect(page, 'select[name="categoryId"]', '', 0);
await setTomSelectValue(page, 'select[name="categoryId"]', 9001);
```

Helper bekerja dengan JavaScript API:

- Wait sampai original `<select>` punya property `tomselect`.
- Panggil `ts.load(query, callback)`.
- Add options ke instance.
- Set value via `ts.setValue(target.id)`.

Jika gagal:

- Pastikan selector menunjuk original `<select>`, bukan wrapper TomSelect.
- Inspect HTML template. Field mungkin punya `id` tertentu, misalnya `#category-select`, bukan `select[name="categoryId"]`.
- Pastikan page-specific JS benar-benar menginisialisasi TomSelect.
- Tambahkan wait khusus untuk selector itu sebelum helper dipanggil bila perlu.

### 8.5 AutoNumeric

`page.fill()` tidak reliable untuk input AutoNumeric karena nilai display dan raw value dikelola library.

Gunakan:

```ts
await setAutoNumeric(page, 'input[name="price"]', 15000);
const value = await getAutoNumericValue(page, 'input[name="price"]');
```

Helper bekerja dengan:

```ts
window.AutoNumeric.getAutoNumericElement(el).set(value)
```

Jika gagal:

- Pastikan input sudah punya AutoNumeric instance.
- Pastikan selector menunjuk input asli.
- Pastikan page-specific/global initializer sudah selesai.

### 8.6 HTMX / network waits

`helpers/waits.ts` menyediakan:

- `waitForHtmx(page)` — tunggu `.htmx-request` hilang.
- `waitForNetworkIdle(page)` — tunggu Playwright networkidle.
- `waitForToast(page, type)` — tunggu toast/alert success/error.
- `waitForPageLoad(page)` — domcontentloaded + HTMX idle.

Gunakan ini saat test berinteraksi dengan search/filter/pagination HTMX.

## 9. Selector Convention for Interactive Forms

Stable selectors are critical for E2E resilience. This section defines the naming convention for HTML templates so Playwright tests can target elements reliably.

### 9.1 Selector preference order

From most stable to most fragile:

| Priority | Selector Type | Use For | Example |
|---|---|---|---|
| 1 | Explicit `id` attribute | TomSelect/autocomplete, modal-selector triggers, AutoNumeric inputs, date pickers, dynamic line containers | `#category-select`, `#brand-select`, `#line-container` |
| 2 | `name` attribute | Plain input, textarea, native select | `input[name="name"]`, `select[name="status"]` |
| 3 | `[data-ajax-form]` scoped | Submit buttons (avoid strict mode violations) | `[data-ajax-form] button[type="submit"]` |
| 4 | Table-scoped text | List page assertions | `page.locator('table').locator('text=E2E Brand')` |
| 5 | Role-based | Buttons with visible label | `page.getByRole('button', { name: 'Save' })` |

**DILARANG:**
- XPath selectors
- CSS class selectors tied to styling (`.btn-primary`, `.ts-wrapper`)
- `nth-child` or positional selectors
- TomSelect wrapper classes (`.ts-control`, `.ts-dropdown`) — always target the original `<select>`

### 9.2 Widget ID convention in templates

When creating or editing Thymeleaf templates with interactive widgets, assign explicit IDs:

| Widget Type | ID Pattern | Example |
|---|---|---|
| TomSelect / Autocomplete | `{field}-select` | `id='category-select'`, `id='brand-select'` |
| Modal selector trigger | `{field}-modal-btn` | `id='vendor-modal-btn'` |
| AutoNumeric input | `{field}-input` | `id='amount-input'`, `id='price-input'` |
| Flatpickr date picker | `{field}-date` | `id='transaction-date'` |
| Dynamic line container | `{entity}-lines` | `id='payment-lines'`, `id='order-lines'` |
| Dynamic line add button | `{entity}-add-line` | `id='payment-add-line'` |

### 9.3 Why explicit IDs matter

- TomSelect wraps the original `<select>` in generated markup. The Playwright helper (`helpers/tomselect.ts`) targets the **original** `<select>` element, not the wrapper. Without an explicit ID, the selector must rely on `name` which can conflict with wrapper-generated elements.
- AutoNumeric manages display vs raw value. The helper needs the exact input element.
- Modal selectors have trigger buttons that open popups — a stable ID prevents matching unrelated buttons.

### 9.4 Reference implementation

Product form (`templates/inventory/products/form.html`) demonstrates the pattern:
- `id='category-select'` for TomSelect autocomplete
- `id='brand-select'` for TomSelect autocomplete  
- `id='uom-select'` for native select with TomSelect potential

Product spec (`e2e-tests/tests/master-data/product.spec.ts`) uses these IDs directly.

### 9.5 Existing specs and related documentation

For detailed component interaction patterns, refer to:
- `docs/spec/autocomplete-generic.md` — TomSelect/autocomplete fragment standards
- `docs/spec/modal-selector.md` — Modal selector standards
- `docs/spec/numeric-standards.md` — AutoNumeric input standards
- `docs/spec/datetime-standards.md` — Flatpickr date picker standards
- `docs/spec/header-lines-form.md` — Dynamic line row standards

## 10. Status Coverage

Spec yang ada:

| Spec | Coverage | Catatan |
|---|---|---|
| `tests/auth/login.spec.ts` | Login valid, login invalid, unauthenticated redirect | Tidak memakai auto-login fixture; bare `@playwright/test` |
| `tests/master-data/uom.spec.ts` | List, create, edit, validation | Simple CRUD |
| `tests/master-data/product-category.spec.ts` | List, create, edit, validation | Simple CRUD dengan native select |
| `tests/master-data/brand.spec.ts` | List, create, edit, validation | Simple CRUD |
| `tests/master-data/product.spec.ts` | List, create required fields, validation | Mid-level CRUD; memakai TomSelect untuk category/brand |
| `tests/procurement/purchase-requisition.spec.ts` | 6 scenario CRUD + approval flow + signature + sanity | Scenario A `@smoke` (happy path); B reject; C DRAFT cancel; D APPROVED cancel; E header reset; F SPL autofill |
| `tests/procurement/purchase-order.spec.ts` | 1 sanity + 6 STANDARD PO lifecycle scenarios | Scenario A `@smoke`; B edit DRAFT; C submit→approve→send; D submit→reject approval request; E cancel DRAFT; F delete DRAFT via API |
| `tests/inventory/stock-adjustment.spec.ts` | 1 sanity + 5 scenario lifecycle | Tag `@inventory`. A create DRAFT; B edit; C process to inventory (DRAFT→COMPLETED); D facility-change clears lines; E delete via API |
| `tests/inventory/goods-receipt.spec.ts` | 1 sanity + 4 PO-sourced GR lifecycle scenarios | Scenario A `@smoke`; B edit DRAFT qty; C complete DRAFT→COMPLETED; D delete DRAFT via API |
| `tests/accountspayable/vendor-bill.spec.ts` | 1 sanity + 4 PO/GR-sourced Vendor Bill lifecycle scenarios | Scenario A `@smoke`; B confirm DRAFT→CONFIRMED; C cancel DRAFT→CANCELLED; D delete DRAFT via API |
| `tests/accountspayable/vendor-payment.spec.ts` | 1 sanity + 4 PO/GR/VB-sourced Vendor Payment lifecycle scenarios | Scenario A `@smoke`; B confirm DRAFT→CONFIRMED; C cancel DRAFT→CANCELLED; D delete DRAFT via API |
| `tests/auth/rbac.spec.ts` | 16 case (4 role × 4 resource) | Tag `@rbac`. URL guard allow/deny + Create button visibility |

Status terakhir (2026-05-27): Full suite 73/73 passing via `e2e-tests/scripts/run-e2e.ps1` (cold run 7.0m). Smoke listing includes Goods Receipt Scenario A, Vendor Bill Scenario A, Vendor Payment Scenario A, plus existing smoke specs.

- Modul transaksional pertama (PR + approval) sudah hijau end-to-end termasuk signature pad.
- Jika suite mulai gagal lagi, jalankan spec tunggal dengan `--headed --debug` dan cek troubleshooting di bawah.

## 11. Helper Tambahan untuk Modul Transaksional

Helper di luar Section 8 yang khusus dibutuhkan modul transaksional dengan approval flow:

### 11.1 Flatpickr (`helpers/flatpickr.ts`)

- Pakai untuk input dengan `data-picker="date"` atau plain `<input type="date">`.
- Helper menggunakan `el._flatpickr.setDate(date, true)` agar `onChange` handler Flatpickr ter-trigger (penting untuk cascading seperti SPL price autofill).
- Fallback otomatis ke `page.fill()` kalau input bukan Flatpickr-managed.

### 11.2 Inline line editor (`helpers/line-editor.ts`)

Header-lines form di Solusi ERP mengikuti pola:
- `<button id="btn-add-line">` clone dari `#row-template-source` ke `#line-container`
- Per-row remove via `.btn-remove-line`
- Field name `lines[N].fieldName`

```ts
import { addLine, removeLineAt, getLineCount, lineFieldSelector } from '../../helpers/line-editor';

await addLine(page);                                            // klik default #btn-add-line, tunggu count naik
await setTomSelectValue(page, `[name="lines[0].productId"]`, '9101');
await setAutoNumeric(page, lineFieldSelector(0, 'quantity'), 5);
await removeLineAt(page, 0);                                    // klik .btn-remove-line di row index 0
expect(await getLineCount(page)).toBe(0);
```

### 11.3 Signature pad (`helpers/signature-pad.ts`)

Approval modal pakai `signature_pad@4` di canvas (`#sig-canvas-approve-finish`, `#sig-canvas-approve-forward`).

```ts
import { drawSignature, assertSignatureNotEmpty } from '../../helpers/signature-pad';

await drawSignature(page, '#sig-canvas-approve-finish');
await assertSignatureNotEmpty(page, '#sig-canvas-approve-finish');
```

Strategi yang dipakai (Layer 1 + 2 hybrid, _bukan_ backend bypass):

1. Dispatch synthetic `pointerdown/move/up` di canvas — best-effort untuk library yang listen pointer events di canvas.
2. Paint pixels langsung lewat `getContext('2d').stroke()` — ini yang menjamin `canvas.toDataURL()` non-empty PNG.

**Penting**: `signature_pad@4` di Bootstrap modal sering tidak menerima synthetic pointer events, jadi `pad.isEmpty()` (JS-side validation) bisa tetap return `true`. Kalau spec perlu mem-bypass JS-side empty-check sambil tetap mengirim signature beneran ke backend, panggil endpoint approval langsung dari `page.evaluate` dengan `canvas.toDataURL('image/png')` sebagai `signatureBase64`. Lihat `tests/procurement/purchase-requisition.spec.ts` `processApproval` helper untuk pola lengkap.

### 11.4 Multi-role storage state

Login satu kali per role di `setup` project, lalu spec pakai storage state file yang dihasilkan.

`fixtures/base.ts` default ke admin storage state. Override per `describe`:

```ts
import { test, expect, storageStatePath } from '../../fixtures/base';

test.describe('My approval scenario', () => {
  test.use({ storageState: storageStatePath('employee1') });

  test('flow', async ({ page, browser }) => {
    // employee1 page
    // ... bikin PR, submit ...

    // switch ke approver1 lewat second context
    const approverContext = await browser.newContext({ storageState: storageStatePath('approver1') });
    const approverPage = await approverContext.newPage();
    // ... approve ...
    await approverContext.close();
  });
});
```

Kenapa storage state level fixture (bukan project-level): Playwright resolve project options saat worker boot, sebelum `setup` project menulis `.auth/{role}.json`. Project-level path bakal stale di cold run. Fixture-level path di-resolve saat test mulai eksekusi, setelah dependency `setup` selesai.

### 11.5 Endpoint langsung untuk gap UI

Beberapa transition tidak punya UI button (mis. PR Cancel, SA Delete) tapi controller-nya tetap ada. Pola yang dipakai di `purchase-requisition.spec.ts` dan `stock-adjustment.spec.ts`:

```ts
async function cancelPr(page, prId) {
  const csrfHeaderName = await page.evaluate(() => 
    (document.querySelector('meta[name="_csrf_header"]') as HTMLMetaElement)?.content);
  const csrfToken = await page.evaluate(() => 
    (document.querySelector('meta[name="_csrf"]') as HTMLMetaElement)?.content);
  return await page.evaluate(async ({ id, headerName, token }) => {
    const r = await fetch(`/purchasing/purchase-requisitions/${id}/cancel`, {
      method: 'POST', headers: { Accept: 'application/json', [headerName]: token },
    });
    return { status: r.status };
  }, { id: prId, headerName: csrfHeaderName, token: csrfToken });
}
```

Ini sah dipakai sebagai E2E sepanjang endpoint adalah kontrak yang sebenarnya — kita exercise auth + status transition tanpa nunggu UI button. Catat di report saat dipakai (lihat `docs/reports/e2e-pr-approval.md` Task 10-14, `docs/reports/e2e-sa-rbac-pr-reject.md` Task 13).

### 11.6 Drawer-driven readonly field (Stock Adjustment)

Beberapa modul mengunci field readonly dan hanya menerima commit dari drawer. Contoh: `lines[N].quantity` di SA form readonly, drawer `#drawer-non-serial` menyimpan lewat `.btn-save-drawer`.

Pattern di `stock-adjustment.spec.ts`:

```ts
async function setQuantityViaDrawer(page, rowIndex, qty) {
  await page.locator(`#line-container tr.line-row >> nth=${rowIndex} >> .btn-edit-detail`).click();
  const drawer = page.locator('#drawer-non-serial');
  await expect(drawer).toBeVisible();
  // UoM dropdown populates async dari /api/lookup/inventory/uom-conversions
  await page.waitForFunction(() => {
    const sel = document.querySelector('#drawer-non-serial .select-uom-target');
    return sel && sel.options.length > 0;
  });
  await setAutoNumeric(page, '#drawer-non-serial .input-qty-target', qty);
  await drawer.locator('.btn-save-drawer').click();
  await expect(drawer).toBeHidden();
}
```

Kapan dipakai: kalau template menandai input `readonly` dan page JS hanya mengupdate via drawer save handler. Helper biasanya spec-local — promote ke `helpers/` kalau pattern muncul di >1 modul.

### 11.7 Cascading TomSelect (Facility → Grid → Container)

`helpers/tomselect.ts` menyediakan `setCascadingTomSelect(page, parentSel, parentValue, childSel, childValue, hint?)`:

- Set parent.
- `clearOptions()` + `load(hint)` di child untuk trigger AJAX reload dengan parent context.
- `waitForTomSelectOptions` sampai child option target tersedia.
- Set child value.

Banyak modul tidak butuh helper ini secara eksplisit karena page JS sudah pakai `parentProvider` callback (mis. SA grid TomSelect membaca `headerFacility.value` setiap load) dan `setTomSelectValue` sendiri call `addOption` saat value tidak ada di dropdown. Pakai `setCascadingTomSelect` saat: (1) butuh memverifikasi option benar-benar dimuat dari server, atau (2) child harus terlihat di dropdown sebelum dipilih (mis. assertion list option).

## 12. RBAC Matrix Pattern

`tests/auth/rbac.spec.ts` mendemonstrasikan parametric matrix yang reusable untuk modul lain.

### 12.1 Strukture matrix

```ts
type Role = 'admin' | 'approver1' | 'employee1' | 'warehouse1';
type ResourceKey = 'pr' | 'sa' | 'brand' | 'permGroup';

const RESOURCES: Record<ResourceKey, Resource> = {
  pr: { listUrl: '/purchasing/purchase-requisitions', createBtnSelector: 'a[href="/purchasing/purchase-requisitions/create"]' },
  // ...
};

const MATRIX: Expectation[] = [
  { role: 'admin', resource: 'pr', list: 'allow', createVisible: true },
  { role: 'warehouse1', resource: 'pr', list: 'deny', createVisible: null },
  // ...
];
```

### 12.2 Per-case dual context

```ts
for (const exp of MATRIX) {
  test(`${exp.role} :: ${RESOURCES[exp.resource].label} -> ${exp.list}`, async ({ browser }) => {
    const ctx = await browser.newContext({ storageState: storageStatePath(exp.role) });
    const page = await ctx.newPage();
    try {
      const outcome = await classify(page, RESOURCES[exp.resource].listUrl);
      expect(outcome).toBe(exp.list);
      // optional: createVisible assertion
    } finally {
      await ctx.close();
    }
  });
}
```

### 12.3 Klasifikasi allow vs deny

`classify(page, listUrl)` membandingkan final URL path + status:

- **Allow** = final path masih di resource path AND status < 400.
- **Deny** = final path bukan resource path (mis. `/error/403`, `/dashboard`, `/login`) OR status >= 400.

Klasifikasi ini permissive by design — Spring Security dapat respond dengan beberapa cara (403 page, redirect, error template). Treat any non-resource final path sebagai deny.

### 12.4 Tag dan smoke

Tag describe `@rbac` (bukan `@smoke`). 16 case akan menambah ~30-60 detik ke full suite tetapi tidak masuk smoke push-to-main.

## 13. Troubleshooting Berdasarkan Gejala

### 13.1 Server tidak ready dalam 60 detik

Cek:

- `target/e2e-server.log`
- `target/e2e-server-err.log`
- Apakah port `18080` sudah dipakai proses lain.
- Apakah build `-Pe2e` sukses.
- Apakah migration H2 gagal.

Command lokal:

```bash
./mvnw -B clean package -DskipTests -Pe2e -q
java -jar target/solusi-program-erp-*.jar --spring.profiles.active=e2e
```

Di Windows:

```powershell
.\mvnw.cmd -B clean package -DskipTests -Pe2e -q
java -jar target\solusi-program-erp-*.jar --spring.profiles.active=e2e
```

### 13.2 Flyway duplicate version

Gejala:

- Error `Found more than one migration with version ...`

Penyebab umum:

- `application-e2e.yaml` membaca `db/migration` dan `db/migration-h2` bersamaan.

Fix:

- Pastikan profile `e2e` hanya memakai:

```yaml
spring.flyway.locations:
  - classpath:db/migration-h2
```

### 13.3 H2 migration syntax error

Gejala:

- App gagal start.
- Error SQL dari H2 saat Flyway migrate.

Yang perlu dicek:

- `AFTER column` di `ALTER TABLE`.
- Multi operation dalam satu `ALTER TABLE`.
- MySQL-only syntax seperti `MODIFY COLUMN`, `CHANGE COLUMN`, `DROP FOREIGN KEY`.
- `UPDATE ... JOIN ... SET`.
- Escape string yang berbeda antara MariaDB dan H2.

Fix terbaik:

- Jangan mengubah migration MariaDB untuk menyesuaikan H2.
- Patch file mirror di `db/migration-h2` dengan versi Flyway yang sama.

### 13.4 Login diarahkan ke password change

Ini normal dan sudah ditangani oleh `helpers/auth.ts` untuk CRUD specs.

Jika test auth manual gagal:

- Pastikan test auth memang mengantisipasi redirect keluar dari `/login`.
- Jika perlu assert dashboard spesifik, handle password change flow juga.

### 13.5 Strict mode violation pada button submit

Gejala:

- Playwright bilang selector match lebih dari satu element.

Fix:

```ts
const submitBtn = page.locator('[data-ajax-form] button[type="submit"]').first();
```

Jangan pakai selector generic:

```ts
page.locator('button[type="submit"]')
```

### 13.6 Redirect regex langsung match URL create/edit

Gejala:

- Test tampak melewati submit padahal masih di `/create` atau `/edit`.

Penyebab:

```ts
/\/inventory\/brands/
```

Regex ini match `/inventory/brands/create`.

Fix:

```ts
/\/inventory\/brands(\?.*)?$/
```

### 13.7 Item created tidak terlihat di list

Kemungkinan:

- Item masuk halaman 2 karena pagination.
- Search/sort default berbeda.
- Text muncul di lebih dari satu element.

Fix opsi:

- Untuk modul dengan data banyak, validasi redirect + table visible sudah cukup.
- Jika perlu assert item, gunakan search/filter dulu.
- Scope assertion ke table dan `.first()`.

### 13.8 Submit AJAX timeout

Gejala:

- `submitAndExpectRedirect` timeout di `page.waitForURL`.
- Button berhasil diklik tetapi halaman tidak redirect.

Cek:

1. Apakah form punya `data-ajax-form`.
2. Apakah handler JS AJAX form sudah terpasang saat click.
3. Apakah response server valid atau 4xx/5xx.
4. Apakah validation error muncul tetapi test menunggu redirect.
5. Apakah ada browser console error.

Debug langkah cepat:

```bash
cd e2e-tests
npx playwright test tests/master-data/uom.spec.ts --headed --debug
```

Lihat juga trace/video dari failed run.

### 13.9 TomSelect timeout

Gejala:

- Timeout di `waitForFunction` helper TomSelect.
- Error `TomSelect not found on selector`.
- Test stuck sebelum submit product.

Cek:

1. Selector mengarah ke original `<select>`, bukan wrapper.
2. Name/id field sesuai HTML aktual.
3. JS initializer page-specific sudah memanggil TomSelect/initLookup.
4. Data endpoint lookup mengembalikan options.
5. Field mungkin belum visible karena tab/conditional render.

Debug di browser console:

```js
document.querySelector('select[name="categoryId"]')
document.querySelector('select[name="categoryId"]')?.tomselect
```

Jika selector by name gagal, inspect template dan coba id selector.

### 13.10 AutoNumeric tidak terset

Gejala:

- Input terlihat berubah tapi submit mengirim nilai kosong/0.
- Validation numeric gagal.

Fix:

- Pakai `setAutoNumeric`, bukan `fillField`.
- Pastikan selector input asli.
- Pastikan AutoNumeric sudah initialized sebelum set.

## 14. Checklist Menambah Spec Baru

Sebelum menulis test:

- [ ] Baca form template module untuk tahu field name/id aktual.
- [ ] Identifikasi komponen khusus: TomSelect, AutoNumeric, Flatpickr, HTMX, modal selector, dynamic lines.
- [ ] Cari apakah record seeded stabil tersedia untuk edit/delete.
- [ ] Pastikan route list/create/edit benar.
- [ ] Tentukan apakah created item pasti muncul di halaman pertama.

Saat menulis test:

- [ ] Import dari `../../fixtures/base` untuk test authenticated CRUD.
- [ ] Pakai `navigateToModule` untuk navigasi module.
- [ ] Pakai `fillField` untuk input/textarea biasa.
- [ ] Pakai `selectDropdown` hanya untuk native select.
- [ ] Pakai `selectTomSelect` / `setTomSelectValue` untuk TomSelect.
- [ ] Pakai `setAutoNumeric` untuk numeric input yang memakai AutoNumeric.
- [ ] Pakai `[data-ajax-form] button[type="submit"]` untuk submit manual.
- [ ] Pakai redirect regex anchored: `/\/path(\?.*)?$/`.
- [ ] Scope text assertion ke `table` dan gunakan `.first()` jika perlu.

Setelah menulis test:

- [ ] Jalankan spec tunggal dulu.
- [ ] Jalankan full Playwright suite.
- [ ] Cek trace/video/screenshot jika gagal.
- [ ] Jangan menaikkan timeout sebagai fix utama sebelum memahami root cause.

## 15. Checklist Agent Handoff

Agent baru yang melanjutkan E2E harus membaca minimal:

1. `docs/tests/playwright-e2e-guide.md` — dokumen ini.
2. `e2e-tests/playwright.config.ts` — config runner aktual.
3. `e2e-tests/fixtures/base.ts` — auto-login fixture.
4. `e2e-tests/helpers/form.ts` — submit dan validation helper.
5. `e2e-tests/helpers/tomselect.ts` — jika test menyentuh autocomplete.
6. `e2e-tests/helpers/autonumeric.ts` — jika test menyentuh numeric money/decimal.
7. Spec terdekat yang sudah passing, misalnya `brand.spec.ts` untuk simple CRUD.
8. Template HTML module yang sedang dites untuk memastikan field selector aktual.

Jika melanjutkan failure terakhir:

- Untuk UoM create, fokus pada readiness AJAX form handler dan response submit.
- Untuk Product create, fokus pada selector/initialization TomSelect category dan brand.
- Jalankan spec tunggal dengan headed/debug sebelum full suite.

## 16. Kapan Memperbarui Dokumen Ini

Update dokumen ini saat:

- Ada helper E2E baru.
- Ada perubahan runner/script/CI.
- Ada pola flakiness baru yang sudah ditemukan root cause-nya.
- Ada migration compatibility rule H2 baru.
- Ada perubahan strategi data seed atau ID stabil.
- Product CRUD/TomSelect issue terakhir sudah fixed dan status coverage berubah.
- Ada plan E2E baru selesai (referensikan plan + report dari `docs/plans/` dan `docs/reports/`, mis. `docs/plans/e2e-pr-approval.md`, `docs/plans/e2e-sa-rbac-pr-reject.md`).

Jangan isi dokumen ini dengan progress harian atau log eksekusi. Progress task tetap di `docs/plans/` dan laporan eksekusi tetap di `docs/reports/` bila berasal dari plan.
