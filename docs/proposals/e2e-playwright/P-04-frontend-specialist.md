# Proposal Teknis: Implementasi Playwright E2E Test — Solusi ERP

> **Persona:** Frontend Specialist — Interaksi komponen UI & Selector Strategy
> **Versi:** 1.0
> **Tanggal:** Juli 2025
> **Tech Stack:** Java 21 · Spring Boot 4.0.3 · Thymeleaf · Playwright Node.js
> **Cakupan:** 34 controllers · 23 form pages · 26 list pages · 46 Flyway migrations

---

### 1. Executive Summary

Proyek Solusi ERP memiliki 23 halaman form CRUD dan 26 halaman list yang seluruhnya di-render server-side oleh Thymeleaf. Setiap form menggunakan kombinasi komponen UI yang tidak bisa diinteraksi dengan `page.fill()` biasa: TomSelect (autocomplete), Flatpickr (date picker), dan AutoNumeric (numeric formatter). Ditambah HTMX partial reload dan AJAX form submission — membuat confidence terhadap kualitas frontend sangat rendah tanpa automated browser testing.

Proposal ini merancang infrastruktur Playwright E2E test di subdirektori `e2e/` yang berjalan terhadap Spring Boot instance dengan H2 in-memory database (MODE=MySQL). Fokus utama: **helper library yang robust untuk ketiga komponen UI**, selector strategy yang tahan terhadap perubahan markup, dan debugging experience kelas satu via Playwright trace viewer. Target: **100% coverage untuk CRUD critical path** di 8 modul utama dalam 5.5 minggu.

---

### 2. Arsitektur Solusi

```
┌──────────────────────────────────────────────────────────────────┐
│                      CI PIPELINE (GitHub Actions)                 │
│                                                                    │
│  ┌──────────────────┐       ┌─────────────────────────────────┐  │
│  │  Spring Boot App  │       │     Playwright Test Runner       │  │
│  │  (E2E Profile)    │       │     (Node.js @playwright/test)   │  │
│  │                    │       │                                   │  │
│  │  • H2 MODE=MySQL  │◄─────►│  e2e/                            │  │
│  │  • Flyway migrate  │ HTTP  │  ├── playwright.config.ts        │  │
│  │  • Port 18080      │       │  ├── helpers/                    │  │
│  │  • data.sql seed   │       │  │   ├── auth.ts                 │  │
│  │  • CSRF enabled    │       │  │   ├── tomselect.ts     ★      │  │
│  │  • Session auth    │       │  │   ├── flatpickr.ts     ★      │  │
│  │                    │       │  │   ├── autonumeric.ts   ★      │  │
│  └──────────────────┘       │  │   ├── line-items.ts     ★      │  │
│                               │  │   ├── drawers.ts       ★      │  │
│                               │  │   ├── form.ts                 │  │
│                               │  │   ├── navigation.ts           │  │
│                               │  │   └── assertions.ts           │  │
│                               │  ├── fixtures/                   │  │
│                               │  │   └── erp-fixtures.ts         │  │
│                               │  ├── selectors/                  │  │
│                               │  │   └── selectors.ts            │  │
│                               │  └── tests/                      │  │
│                               │      ├── auth/                   │  │
│                               │      ├── master/                 │  │
│                               │      ├── inventory/              │  │
│                               │      ├── accounting/             │  │
│                               │      └── common/                 │  │
│                               └─────────────────────────────────┘  │
└──────────────────────────────────────────────────────────────────┘
                                 ★ = Fokus utama proposal ini
```

#### Prinsip Arsitektur

| # | Prinsip | Rasional |
|---|---------|----------|
| 1 | **Satu proses Spring Boot, satu browser Chromium** | Menghindari kompleksitas test container; H2 cukup reliable untuk DDL MariaDB via MODE=MySQL |
| 2 | **Helper per komponen UI, bukan per modul** | TomSelect helper dipakai di 23+ form; DRY mengurangi maintenance 10x |
| 3 | **JavaScript API over DOM simulation** | TomSelect/Flatpickr/AutoNumeric mem-hide elemen asli; klik pada wrapper tidak reliable lintas versi library |
| 4 | **Trace-first debugging** | Setiap test gagal menghasilkan trace zip yang bisa dibuka di trace.playwright.dev — mengandung DOM snapshot, network log, console log, screenshot per step |
| 5 | **Subdirektori terpisah (`e2e/`)** | Isolasi Node.js toolchain dari Java build; `.gitignore` cukup tambah `e2e/node_modules/` |

---

### 3. Spring Boot E2E Profile Design

#### 3.1 File: `application-e2e.yaml`

```yaml
# src/main/resources/application-e2e.yaml
server:
  port: 18080

spring:
  datasource:
    url: jdbc:h2:mem:erp_e2e;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;CASE_INSENSITIVE_IDENTIFIERS=true
    driver-class-name: org.h2.Driver
    username: sa
    password:
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: none  # Flyway handles schema
    properties:
      hibernate:
        globally_quoted_identifiers: true
  flyway:
    enabled: true
    baseline-on-migrate: true
    locations:
      - classpath:db/migration
      - classpath:db/e2e
    placeholders:
      ENGINE_INNODB: ""
      CURRENT_TIMESTAMP_6: "CURRENT_TIMESTAMP"
  h2:
    console:
      enabled: true
      path: /h2-console
  sql:
    init:
      mode: always
      data-locations: classpath:db/e2e/data-e2e.sql

minio:
  enabled: false

logging:
  level:
    com.solusi.erp: INFO
    org.hibernate.SQL: DEBUG
```

#### 3.2 Perubahan `pom.xml`

```xml
<profiles>
    <profile>
        <id>e2e</id>
        <dependencies>
            <dependency>
                <groupId>com.h2database</groupId>
                <artifactId>h2</artifactId>
                <scope>runtime</scope>
            </dependency>
        </dependencies>
    </profile>
</profiles>
```

Jalankan: `mvn spring-boot:run -Pe2e -Dspring-boot.run.profiles=e2e`

#### 3.3 Flyway Migration Compatibility

Dari 46 migration files (V1–V45), beberapa menggunakan syntax MariaDB-specific:

| Syntax MariaDB | Solusi H2 MODE=MySQL |
|----------------|---------------------|
| `ENGINE=InnoDB` | H2 mengabaikan secara otomatis di MODE=MySQL |
| `AUTO_INCREMENT` | Didukung H2 |
| `TINYINT(1)` untuk boolean | Didukung H2 |
| `ON UPDATE CURRENT_TIMESTAMP(6)` | Perlu migration shim atau conditional SQL |
| `FULLTEXT INDEX` | Tidak didukung — skip via conditional |

**Strategi:** Buat `db/e2e/V999__H2_Compatibility_Shim.sql` yang menjalankan workaround untuk syntax yang gagal di H2. Alternatif: gunakan Flyway callback `beforeMigrate` untuk preprocessing.

#### 3.4 Startup Command

```bash
# Lokal
./mvnw spring-boot:run -Pe2e -Dspring-boot.run.profiles=e2e

# CI (JAR sudah dibangun)
java -jar target/solusi-program-erp-1.2.0.jar --spring.profiles.active=e2e
```

---

### 4. Data Seeding Strategy

#### 4.1 Filosofi: Minimal Viable Seed

E2E test TIDAK menguji database migration — itu urusan integration test. E2E menguji **user journey di browser**. Maka seed data harus:

1. **Cukup untuk happy path** — setiap TomSelect dropdown harus punya ≥2 opsi
2. **Deterministic** — ID tetap, nama tetap, sehingga assertion di Playwright stabil
3. **Idempotent** — bisa dijalankan ulang tanpa error (gunakan `INSERT IGNORE` atau `MERGE`)

#### 4.2 File: `db/e2e/data-e2e.sql`

```sql
-- ═══════════════════════════════════════════════════════
-- E2E SEED DATA — Deterministic, Minimal, Idempotent
-- ═══════════════════════════════════════════════════════

-- Security: Admin user sudah ada dari V2 Flyway seed
-- SystemInitializer hash INITIAL_PASSWORD_SETUP → admin123 otomatis

-- Master: Currency
INSERT INTO currencies (id, code, name, alias, symbol, is_active) VALUES
  (1, 'IDR', 'Indonesian Rupiah', 'IDR', 'Rp', true),
  (2, 'USD', 'US Dollar', 'USD', '$', true)
ON DUPLICATE KEY UPDATE code=code;

-- Master: Tax
INSERT INTO taxes (id, code, name, rate, is_active) VALUES
  (1, 'PPN-11', 'PPN 11%', 11.00, true),
  (2, 'NO-TAX', 'Tanpa Pajak', 0.00, true)
ON DUPLICATE KEY UPDATE code=code;

-- Inventory: Brand
INSERT INTO brands (id, code, name, is_active) VALUES
  (901, 'BRD-E2E-001', 'E2E Brand Alpha', true),
  (902, 'BRD-E2E-002', 'E2E Brand Beta', true)
ON DUPLICATE KEY UPDATE code=code;

-- Inventory: Product Category
INSERT INTO product_categories (id, code, name, type, is_active) VALUES
  (901, 'CAT-E2E-001', 'E2E Category Goods', 'GOODS', true),
  (902, 'CAT-E2E-002', 'E2E Category Service', 'SERVICE', true)
ON DUPLICATE KEY UPDATE code=code;

-- Inventory: Facility (Warehouse)
INSERT INTO facilities (id, code, name, is_active) VALUES
  (901, 'WH-E2E-001', 'E2E Warehouse Main', true)
ON DUPLICATE KEY UPDATE code=code;

-- Inventory: Grid
INSERT INTO grids (id, code, name, facility_id, is_active) VALUES
  (901, 'GRD-E2E-001', 'E2E Grid A1', 901, true)
ON DUPLICATE KEY UPDATE code=code;

-- Inventory: Container
INSERT INTO containers (id, code, name, grid_id, is_active) VALUES
  (901, 'BIN-E2E-001', 'E2E Bin A1-01', 901, true)
ON DUPLICATE KEY UPDATE code=code;

-- Inventory: Product
INSERT INTO products (id, code, name, category_id, brand_id, uom_id, is_active, is_serialized) VALUES
  (901, 'PRD-E2E-001', 'E2E Product Standard', 901, 901, 1, true, false),
  (902, 'PRD-E2E-002', 'E2E Product Serialized', 901, 902, 1, true, true)
ON DUPLICATE KEY UPDATE code=code;

-- Party Role Types
INSERT INTO party_role_types (id, code, name, is_active) VALUES
  (901, 'SUPPLIER', 'Supplier', true),
  (902, 'CUSTOMER', 'Customer', true)
ON DUPLICATE KEY UPDATE code=code;
```

#### 4.3 Konvensi ID

| Range | Penggunaan |
|-------|-----------|
| 1–100 | Data dari Flyway migration seed (V2, V5, V15) |
| 901–999 | Data E2E seed — TIDAK BOLEH bentrok dengan production seed |
| 1000+ | Data yang dibuat oleh test (auto-increment) |

#### 4.4 Reset Strategy

Setiap CI run memulai Spring Boot fresh → H2 in-memory di-create ulang → Flyway + data-e2e.sql dijalankan. **Tidak perlu cleanup script.** Untuk parallelism, gunakan unique Spring port per worker.

---

### 5. Playwright Test Architecture

> **Ini adalah section inti dari proposal. Detail terbanyak ada di sini.**

#### 5.1 Direktori Struktur

```
e2e/
├── package.json
├── tsconfig.json
├── playwright.config.ts
├── .gitignore                    # node_modules/
│
├── helpers/                      # ★ KOMPONEN UI HELPERS
│   ├── auth.ts                   # Login/logout utilities
│   ├── tomselect.ts              # TomSelect interaction API
│   ├── flatpickr.ts              # Flatpickr date setting API
│   ├── autonumeric.ts            # AutoNumeric value setting API
│   ├── line-items.ts             # ErpLineManager interactions
│   ├── drawers.ts                # Offcanvas drawer interactions
│   ├── form.ts                   # AJAX form submission & validation
│   ├── navigation.ts             # Page navigation & waiting
│   ├── htmx.ts                   # HTMX-specific waits
│   └── assertions.ts             # Custom ERP assertions
│
├── fixtures/
│   └── erp-fixtures.ts           # Playwright custom fixtures
│
├── selectors/
│   └── selectors.ts              # Centralized selector constants
│
└── tests/
    ├── auth/
    │   └── login.spec.ts
    ├── master/
    │   ├── geographic.spec.ts
    │   ├── party.spec.ts
    │   ├── currency.spec.ts
    │   ├── tax.spec.ts
    │   ├── bank-account.spec.ts
    │   └── party-role-type.spec.ts
    ├── inventory/
    │   ├── brand.spec.ts
    │   ├── product-category.spec.ts
    │   ├── uom.spec.ts
    │   ├── uom-conversion.spec.ts
    │   ├── facility.spec.ts
    │   ├── grid.spec.ts
    │   ├── container.spec.ts
    │   ├── product.spec.ts
    │   └── stock-adjustment.spec.ts
    ├── accounting/
    │   ├── coa.spec.ts
    │   ├── period.spec.ts
    │   └── schema.spec.ts
    └── common/
        └── news.spec.ts
```

#### 5.2 Playwright Configuration

```typescript
// e2e/playwright.config.ts
import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './tests',
  timeout: 60_000,                 // 60s per test — AJAX forms can be slow
  expect: { timeout: 10_000 },     // 10s per assertion
  retries: process.env.CI ? 2 : 0, // Retry on CI only
  workers: 1,                      // Sequential — tests share DB state via seed dependency
  fullyParallel: false,            // CRUD tests depend on order (create before edit)

  reporter: [
    ['html', { open: 'never' }],
    ['list'],
    ...(process.env.CI ? [['github'] as const] : []),
  ],

  use: {
    baseURL: process.env.BASE_URL || 'http://localhost:18080',
    screenshot: 'only-on-failure',
    trace: 'retain-on-failure',    // ★ Trace zip pada kegagalan — key debugging tool
    video: 'retain-on-failure',
    locale: 'id-ID',
    timezoneId: 'Asia/Jakarta',
    actionTimeout: 10_000,
    navigationTimeout: 30_000,
    viewport: { width: 1366, height: 768 },
    ignoreHTTPSErrors: true,
  },

  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
  ],

  // ★ Web server management: otomatis start Spring Boot
  webServer: {
    command: process.platform === 'win32'
      ? 'cmd /c "set ENV_FILE=.env.dev && .\\mvnw.cmd spring-boot:run -Pe2e -Dspring-boot.run.profiles=e2e"'
      : './mvnw spring-boot:run -Pe2e -Dspring-boot.run.profiles=e2e',
    url: 'http://localhost:18080/login',
    timeout: 120_000,
    reuseExistingServer: !process.env.CI,
    cwd: '..',
    stdout: 'pipe',
    stderr: 'pipe',
  },
});
```

#### 5.3 Custom Fixtures

```typescript
// e2e/fixtures/erp-fixtures.ts
import { test as base, Page, expect } from '@playwright/test';

type ErpFixtures = {
  authenticatedPage: Page;
};

export const test = base.extend<ErpFixtures>({
  authenticatedPage: async ({ page }, use) => {
    // Capture console errors for debugging
    page.on('console', msg => {
      if (msg.type() === 'error') {
        console.error(`[BROWSER] ${msg.text()}`);
      }
    });
    page.on('pageerror', error => {
      console.error(`[PAGE ERROR] ${error.message}`);
    });

    // Login
    await page.goto('/login');
    await page.fill('input[name="username"]', 'admin');
    await page.fill('input[name="password"]', 'admin123');
    await page.click('button[type="submit"]');

    // Handle force password change pada cold start
    if (page.url().includes('change-password')) {
      await page.fill('input[name="currentPassword"]', 'admin123');
      await page.fill('input[name="newPassword"]', 'admin123');
      await page.fill('input[name="confirmPassword"]', 'admin123');
      await page.click('button[type="submit"]');
    }

    await page.waitForURL('**/dashboard**', { timeout: 15_000 });
    await use(page);
  },
});

export { expect };
```

#### 5.4 Selector Strategy — ★ CRITICAL SECTION

##### 5.4.1 Filosofi: Hierarchical Selector Resilience

```
PALING STABIL ─────────────────────────────────► PALING RAPUH
  [name="field"]  >  #specific-id  >  .erp-*  >  .col-md-4 input
     ✅ Tied to        ✅ Explicit      ⚠️ Semi     ❌ Layout
     data model        in template       stable      dependent
```

**Aturan selector dalam proyek ini:**

| Prioritas | Selector Pattern | Contoh | Kapan Dipakai |
|-----------|-----------------|--------|---------------|
| 1 | `input[name="..."]` | `input[name="name"]` | Semua text/hidden input — terikat ke DTO field |
| 2 | `#explicit-id` | `#category-select`, `#header-facility` | TomSelect & elemen yang punya ID eksplisit di template |
| 3 | `[data-ajax-form="true"]` | Form container | Identifikasi form AJAX |
| 4 | `.erp-input-ts`, `.erp-input-ts-sm` | TomSelect class | Fallback untuk TomSelect tanpa ID |
| 5 | `[data-picker="date"]` | Flatpickr | Semua date input |
| 6 | `.erp-number-decimal`, `.erp-number-integer` | AutoNumeric | Semua numeric input |
| 7 | `.select-product`, `.select-grid`, `.select-container` | Line item TomSelect | Class spesifik di line item template |
| 8 | `.input-qty`, `.input-price`, `.input-sn-single` | Line item numeric | Class spesifik di line item |
| 9 | `#btn-add-line`, `.btn-remove-line` | Line item buttons | Tombol aksi line item |
| 10 | `button[type="submit"]` | Form submission | Universal submit |

##### 5.4.2 Centralized Selector Registry

```typescript
// e2e/selectors/selectors.ts

// ─── FORM INFRASTRUCTURE ─────────────────────────────
export const FORM = {
  ajaxForm: 'form[data-ajax-form="true"]',
  submitButton: 'button[type="submit"]',
  csrfToken: 'input[name="_csrf"]',
  alertSuccess: '.alert-success',
  alertDanger: '.alert-danger',
  invalidField: '.is-invalid',
  invalidFeedback: '.invalid-feedback-ajax',
} as const;

// ─── TOMSELECT ───────────────────────────────────────
export const TOMSELECT = {
  wrapper: '.ts-wrapper',
  control: '.ts-control',
  dropdown: '.ts-dropdown',
  dropdownContent: '.ts-dropdown-content',
  option: '.option',
  item: '.item',
  regular: '.erp-input-ts',
  small: '.erp-input-ts-sm',
  categorySelect: '#category-select',
  brandSelect: '#brand-select',
  headerFacility: '#header-facility',
  uomSelect: '#uom-select',
} as const;

// ─── FLATPICKR ───────────────────────────────────────
export const FLATPICKR = {
  dateInput: 'input[data-picker="date"]',
  datetimeInput: 'input[data-picker="datetime"]',
  timeInput: 'input[data-picker="time"]',
  altInput: '.flatpickr-input + input[readonly]',
  byName: (field: string) => `input[name="${field}"]`,
} as const;

// ─── AUTONUMERIC ─────────────────────────────────────
export const AUTONUMERIC = {
  decimal: '.erp-number-decimal',
  integer: '.erp-number-integer',
  byName: (field: string) => `input[name="${field}"]`,
} as const;

// ─── LINE ITEMS ──────────────────────────────────────
export const LINE_ITEM = {
  container: '#line-container',
  row: '.line-row',
  addButton: '#btn-add-line',
  removeButton: '.btn-remove-line',
  selectProduct: '.select-product',
  selectGrid: '.select-grid',
  selectContainer: '.select-container',
  inputQty: '.input-qty',
  inputPrice: '.input-price',
  inputUomId: '.input-uom-id',
  inputUomFactor: '.input-uom-factor',
  inputUomAlias: '.input-uom-alias',
  inputSnSingle: '.input-sn-single',
  editQtyButton: '.btn-edit-qty',
} as const;

// ─── DRAWERS ─────────────────────────────────────────
export const DRAWER = {
  nonSerial: '#drawer-non-serial',
  serial: '#drawer-serial',
  qtyTarget: '.input-qty-target',
  qtyBase: '.input-qty-base',
  uomTarget: '.select-uom-target',
  saveButton: '.btn-save-drawer',
  serialInputContainer: '.serial-input-container',
  serialInput: '.input-sn-item',
  activeDrawer: '.offcanvas.show',
} as const;

// ─── HTMX / LIST ────────────────────────────────────
export const LIST = {
  searchInput: 'input[name="keyword"]',
  tableBody: 'tbody',
  pagination: '.pagination',
  sortableHeader: '.table-sort',
  editLink: (index: number) => `tbody tr:nth-child(${index + 1}) a[href*="edit"]`,
  rowByText: (text: string) => `tbody tr:has-text("${text}")`,
} as const;

// ─── AUTH ────────────────────────────────────────────
export const AUTH = {
  usernameInput: 'input[name="username"]',
  passwordInput: 'input[name="password"]',
  loginButton: 'button[type="submit"]',
} as const;
```

##### 5.4.3 Mengapa BUKAN `data-testid`?

Saat ini **TIDAK ADA SATUPUN** `data-testid` atau `data-test-id` di 23 form template (telah diverifikasi via grep pada seluruh `src/main/resources/templates/`). Menambahkan `data-testid` ke seluruh template Thymeleaf berarti:
- Mengubah 23 form.html + 26 list.html + 10 fragment files = **59 file**
- Memerlukan koordinasi dengan backend developer untuk setiap perubahan DTO
- Duplikasi informasi yang sudah ada di `name=`, `id=`, dan class attributes

**Keputusan:** Gunakan existing `name`, `id`, dan `class` attributes. Investasi `data-testid` hanya jika selector saat ini terbukti fragile setelah 3+ bulan penggunaan.

#### 5.5 Component Helpers — ★ CORE OF THE PROPOSAL

##### 5.5.1 TomSelect Helper

```typescript
// e2e/helpers/tomselect.ts
import { Page } from '@playwright/test';

interface TomSelectResult {
  id: string;
  name: string;
  error?: string;
}

/**
 * Interaksi dengan TomSelect widget via JavaScript API.
 *
 * MENGAPA JS API BUKAN KLIK:
 * 1. TomSelect menyembunyikan <select> asli → page.selectOption() tidak bekerja
 * 2. DOM wrapper (.ts-wrapper) berubah structure antar versi TomSelect
 * 3. Dropdown positioning bisa off-screen di headless → klik miss
 * 4. JS API langsung memanggil ts.load() → ts.setValue() — zero flakiness
 *
 * TRADE-OFF: Tidak menguji bahwa user benar-benar bisa klik dropdown.
 * Ini acceptable karena TomSelect library sudah di-test oleh maintainernya.
 * Yang kita test adalah: data flow (setValue → form submit → server persist).
 */

/**
 * Pilih opsi pada TomSelect field.
 *
 * @param page - Playwright page
 * @param selector - CSS selector untuk elemen <select> ASLI (hidden), e.g. '#category-select'
 * @param query - Keyword pencarian. '' = load semua opsi
 * @param optionIndex - Index opsi yang dipilih (0 = pertama)
 * @returns Object { id, name } dari opsi terpilih
 */
export async function selectTomSelect(
  page: Page,
  selector: string,
  query: string = '',
  optionIndex: number = 0,
): Promise<TomSelectResult> {
  // Tunggu TomSelect ter-initialize (deferred script loading)
  await page.waitForFunction(
    (sel) => {
      const el = document.querySelector(sel) as HTMLSelectElement & { tomselect?: any };
      return el && el.tomselect;
    },
    selector,
    { timeout: 10_000 },
  );

  return await page.evaluate(
    ({ sel, q, idx }) => {
      return new Promise<TomSelectResult>((resolve, reject) => {
        const el = document.querySelector(sel) as HTMLSelectElement & { tomselect?: any };
        if (!el?.tomselect) {
          reject(new Error(`TomSelect not found: ${sel}`));
          return;
        }
        const ts = el.tomselect;
        ts.load(q, (options: any[]) => {
          if (options.length === 0) {
            resolve({ id: '', name: '', error: `No options for query="${q}" on ${sel}` });
            return;
          }
          options.forEach((opt) => ts.addOption(opt));
          const target = options[Math.min(idx, options.length - 1)];
          ts.setValue(target.id);
          resolve({ id: String(target.id), name: target.name });
        });
      });
    },
    { sel: selector, q: query, idx: optionIndex },
  );
}

/**
 * Set TomSelect ke value ID yang sudah diketahui.
 * Lebih cepat dari selectTomSelect() karena tidak perlu search result.
 */
export async function setTomSelectValue(
  page: Page,
  selector: string,
  valueId: string | number,
): Promise<void> {
  await page.waitForFunction(
    (sel) => {
      const el = document.querySelector(sel) as HTMLSelectElement & { tomselect?: any };
      return el && el.tomselect;
    },
    selector,
    { timeout: 10_000 },
  );

  await page.evaluate(
    ({ sel, val }) => {
      return new Promise<void>((resolve) => {
        const el = document.querySelector(sel) as HTMLSelectElement & { tomselect?: any };
        const ts = el.tomselect;
        ts.load('', (options: any[]) => {
          options.forEach((opt) => ts.addOption(opt));
          ts.setValue(String(val));
          resolve();
        });
      });
    },
    { sel: selector, val: String(valueId) },
  );
}

/**
 * Baca value terpilih dari TomSelect (untuk assertion).
 */
export async function getTomSelectValue(
  page: Page,
  selector: string,
): Promise<{ id: string; text: string }> {
  return await page.evaluate((sel) => {
    const el = document.querySelector(sel) as HTMLSelectElement & { tomselect?: any };
    if (!el?.tomselect) return { id: '', text: '' };
    const ts = el.tomselect;
    const val = ts.getValue();
    const item = ts.options[val];
    return { id: String(val), text: item?.name || '' };
  }, selector);
}

/**
 * Tunggu TomSelect cascade selesai reload setelah parent berubah.
 * Kasus: Facility dipilih → Grid dropdown reload → Container dropdown reload.
 */
export async function waitForCascadeReload(
  page: Page,
  childSelector: string,
  timeoutMs: number = 5_000,
): Promise<void> {
  await page.waitForFunction(
    (sel) => {
      const el = document.querySelector(sel) as HTMLSelectElement & { tomselect?: any };
      if (!el?.tomselect) return false;
      return !el.tomselect.isLoading;
    },
    childSelector,
    { timeout: timeoutMs },
  );
  await page.waitForTimeout(300);
}
```

##### 5.5.2 Flatpickr Helper

```typescript
// e2e/helpers/flatpickr.ts
import { Page } from '@playwright/test';

/**
 * Set tanggal pada Flatpickr-wrapped input.
 *
 * MENGAPA JS API BUKAN KLIK KALENDER:
 * 1. altInput bersifat readonly → page.fill() gagal
 * 2. Input asli bersifat hidden (display:none) → page.fill() gagal
 * 3. Klik kalender membutuhkan koordinat piksel → flaky di headless
 * 4. API setDate(value, triggerChange) reliable 100%
 *
 * @param page - Playwright page
 * @param selector - CSS selector untuk input ASLI (hidden), e.g. 'input[name="transactionDate"]'
 * @param dateStr - Format ISO: 'YYYY-MM-DD', 'YYYY-MM-DDTHH:mm', atau 'HH:mm'
 */
export async function setFlatpickrDate(
  page: Page,
  selector: string,
  dateStr: string,
): Promise<void> {
  await page.waitForFunction(
    (sel) => {
      const input = document.querySelector(sel) as HTMLInputElement & { _flatpickr?: any };
      return input && input._flatpickr;
    },
    selector,
    { timeout: 10_000 },
  );

  await page.evaluate(
    ({ sel, date }) => {
      const input = document.querySelector(sel) as HTMLInputElement & { _flatpickr?: any };
      if (input._flatpickr) {
        input._flatpickr.setDate(date, true);
      } else {
        input.value = date;
        input.dispatchEvent(new Event('change', { bubbles: true }));
      }
    },
    { sel: selector, date: dateStr },
  );
}

/**
 * Baca tanggal dari Flatpickr (ISO format) untuk assertion.
 */
export async function getFlatpickrDate(
  page: Page,
  selector: string,
): Promise<string> {
  return await page.evaluate((sel) => {
    const input = document.querySelector(sel) as HTMLInputElement & { _flatpickr?: any };
    if (input?._flatpickr && input._flatpickr.selectedDates.length > 0) {
      return input._flatpickr.formatDate(input._flatpickr.selectedDates[0], 'Y-m-d');
    }
    return input?.value || '';
  }, selector);
}

/**
 * Set Flatpickr pada line item (dynamic row).
 */
export async function setLineItemDate(
  page: Page,
  rowIndex: number,
  fieldName: string,
  dateStr: string,
): Promise<void> {
  await page.evaluate(
    ({ idx, name, date }) => {
      const rows = document.querySelectorAll('.line-row');
      const input = rows[idx]?.querySelector(
        `input[name*="${name}"]`,
      ) as HTMLInputElement & { _flatpickr?: any };
      if (input?._flatpickr) {
        input._flatpickr.setDate(date, true);
      } else if (input) {
        input.value = date;
        input.dispatchEvent(new Event('change', { bubbles: true }));
      }
    },
    { idx: rowIndex, name: fieldName, date: dateStr },
  );
}
```

##### 5.5.3 AutoNumeric Helper

```typescript
// e2e/helpers/autonumeric.ts
import { Page } from '@playwright/test';

/**
 * Set value pada AutoNumeric-wrapped input.
 *
 * MENGAPA JS API BUKAN page.fill():
 * 1. page.fill('1000') → AutoNumeric tampilkan "1,000.00" tapi raw value "1000" hilang
 * 2. page.fill() bypass AutoNumeric internal state → form submit kirim formatted string
 * 3. instance.set(1000) → raw=1000, formatted="1,000.00" — keduanya benar
 *
 * @param page - Playwright page
 * @param selector - CSS selector, e.g. 'input[name="exchangeRate"]' atau '.input-price'
 * @param value - Numeric value (tanpa formatting)
 */
export async function setAutoNumeric(
  page: Page,
  selector: string,
  value: number,
): Promise<void> {
  await page.evaluate(
    ({ sel, val }) => {
      const el = document.querySelector(sel) as HTMLInputElement;
      if (!el) throw new Error(`Element not found: ${sel}`);

      if (typeof AutoNumeric !== 'undefined') {
        try {
          const instance = AutoNumeric.getAutoNumericElement(el);
          if (instance) {
            instance.set(val);
            return;
          }
        } catch { /* AutoNumeric not initialized — fallback */ }
      }
      el.value = String(val);
      el.dispatchEvent(new Event('input', { bubbles: true }));
      el.dispatchEvent(new Event('change', { bubbles: true }));
    },
    { sel: selector, val: value },
  );
}

/**
 * Baca raw numeric value dari AutoNumeric (untuk assertion).
 */
export async function getAutoNumericValue(
  page: Page,
  selector: string,
): Promise<number | null> {
  return await page.evaluate((sel) => {
    const el = document.querySelector(sel) as HTMLInputElement;
    if (!el) return null;
    if (typeof AutoNumeric !== 'undefined') {
      try {
        const instance = AutoNumeric.getAutoNumericElement(el);
        if (instance) return instance.getNumber();
      } catch { /* fallthrough */ }
    }
    const parsed = parseFloat(el.value.replace(/,/g, ''));
    return isNaN(parsed) ? null : parsed;
  }, selector);
}

/**
 * Set AutoNumeric pada line item row.
 */
export async function setLineItemNumeric(
  page: Page,
  rowIndex: number,
  inputClass: string,
  value: number,
): Promise<void> {
  await page.evaluate(
    ({ idx, cls, val }) => {
      const rows = document.querySelectorAll('#line-container .line-row');
      const el = rows[idx]?.querySelector(`.${cls}`) as HTMLInputElement;
      if (!el) throw new Error(`Element .${cls} not found in row ${idx}`);

      if (typeof AutoNumeric !== 'undefined') {
        try {
          const instance = AutoNumeric.getAutoNumericElement(el);
          if (instance) { instance.set(val); return; }
        } catch { /* fallthrough */ }
      }
      el.value = String(val);
      el.dispatchEvent(new Event('input', { bubbles: true }));
    },
    { idx: rowIndex, cls: inputClass, val: value },
  );
}
```

##### 5.5.4 Line Items Helper

```typescript
// e2e/helpers/line-items.ts
import { Page } from '@playwright/test';
import { LINE_ITEM } from '../selectors/selectors';

interface LineItemData {
  product?: string;
  grid?: string;
  container?: string;
  price?: number;
}

/**
 * Tambah satu line item baru dan isi field-fieldnya.
 *
 * FLOW:
 * 1. Klik #btn-add-line → template row di-clone oleh ErpLineManager
 * 2. Tunggu TomSelect/AutoNumeric init (MutationObserver-driven)
 * 3. Isi product via selectLineItemTomSelect → triggers cascade load
 * 4. Isi grid & container (cascade dari facility header)
 * 5. Isi price via setLineItemNumeric
 *
 * @returns Index row yang baru ditambahkan (0-based)
 */
export async function addLineItem(
  page: Page,
  data: LineItemData,
): Promise<number> {
  await page.click(LINE_ITEM.addButton);
  await page.waitForTimeout(800);

  const rowCount = await page.evaluate(
    (sel) => document.querySelectorAll(`${sel.container} ${sel.row}`).length,
    LINE_ITEM,
  );
  const newIndex = rowCount - 1;

  if (data.product !== undefined) {
    await selectLineItemTomSelect(page, newIndex, 'select-product', data.product);
    await page.waitForTimeout(500);
  }

  if (data.grid !== undefined) {
    await selectLineItemTomSelect(page, newIndex, 'select-grid', data.grid);
    await page.waitForTimeout(300);
  }

  if (data.container !== undefined) {
    await selectLineItemTomSelect(page, newIndex, 'select-container', data.container);
  }

  if (data.price !== undefined) {
    const { setLineItemNumeric } = await import('./autonumeric');
    await setLineItemNumeric(page, newIndex, 'input-price', data.price);
  }

  return newIndex;
}

/**
 * Pilih TomSelect di dalam line item row.
 */
export async function selectLineItemTomSelect(
  page: Page,
  rowIndex: number,
  fieldClass: string,
  query: string = '',
  optionIndex: number = 0,
): Promise<{ id: string; name: string }> {
  await page.waitForFunction(
    ({ idx, cls }) => {
      const rows = document.querySelectorAll('#line-container .line-row');
      const el = rows[idx]?.querySelector(`.${cls}`) as HTMLSelectElement & { tomselect?: any };
      return el && el.tomselect;
    },
    { idx: rowIndex, cls: fieldClass },
    { timeout: 10_000 },
  );

  return await page.evaluate(
    ({ idx, cls, q, optIdx }) => {
      return new Promise<{ id: string; name: string }>((resolve, reject) => {
        const rows = document.querySelectorAll('#line-container .line-row');
        const el = rows[idx]?.querySelector(`.${cls}`) as HTMLSelectElement & { tomselect?: any };
        if (!el?.tomselect) {
          reject(new Error(`TomSelect .${cls} not found in row ${idx}`));
          return;
        }
        const ts = el.tomselect;
        ts.load(q, (options: any[]) => {
          if (options.length === 0) { resolve({ id: '', name: '' }); return; }
          options.forEach((opt) => ts.addOption(opt));
          const target = options[Math.min(optIdx, options.length - 1)];
          ts.setValue(target.id);
          resolve({ id: String(target.id), name: target.name });
        });
      });
    },
    { idx: rowIndex, cls: fieldClass, q: query, optIdx: optionIndex },
  );
}

/**
 * Hapus line item row.
 */
export async function removeLineItem(page: Page, rowIndex: number): Promise<void> {
  await page.evaluate((idx) => {
    const rows = document.querySelectorAll('#line-container .line-row');
    const btn = rows[idx]?.querySelector('.btn-remove-line') as HTMLButtonElement;
    if (btn) btn.click();
  }, rowIndex);
  await page.waitForTimeout(300);
}

/**
 * Hitung jumlah line item rows yang visible.
 */
export async function getLineItemCount(page: Page): Promise<number> {
  return await page.evaluate(
    () => document.querySelectorAll('#line-container .line-row').length,
  );
}
```

##### 5.5.5 Drawer Helper (Stock Adjustment Qty/Serial)

```typescript
// e2e/helpers/drawers.ts
import { Page } from '@playwright/test';

interface DrawerQtyOptions {
  qty: number;
  uomIndex?: number;
  serialNumbers?: string[];
}

/**
 * Buka drawer qty pada line item dan isi qty + serial numbers.
 *
 * ANATOMY:
 * - Non-serial: #drawer-non-serial → .input-qty-target (AutoNumeric) → .btn-save-drawer
 * - Serial: #drawer-serial → .input-qty-target → serial rows auto-gen → .btn-save-drawer
 *
 * CATATAN: Qty di table utama bersifat READONLY. HARUS diisi via drawer.
 */
export async function setLineItemQtyViaDrawer(
  page: Page,
  rowIndex: number,
  options: DrawerQtyOptions,
): Promise<void> {
  // 1. Klik pencil icon untuk buka drawer
  await page.evaluate((idx) => {
    const rows = document.querySelectorAll('#line-container .line-row');
    const btn = rows[idx]?.querySelector(
      '.btn-edit-qty, .btn-open-drawer, [data-action="open-drawer"]'
    );
    if (btn) (btn as HTMLElement).click();
  }, rowIndex);

  // 2. Tunggu drawer muncul
  await page.waitForSelector('.offcanvas.show, .offcanvas.showing', { timeout: 5_000 });
  await page.waitForTimeout(500);

  // 3. Set UoM jika bukan base
  if (options.uomIndex && options.uomIndex > 0) {
    await page.evaluate(
      ({ uomIdx }) => {
        const drawer = document.querySelector('.offcanvas.show') as HTMLElement;
        const uomSelect = drawer?.querySelector('.select-uom-target') as HTMLSelectElement;
        if (uomSelect && uomSelect.options.length > uomIdx) {
          uomSelect.selectedIndex = uomIdx;
          uomSelect.dispatchEvent(new Event('change', { bubbles: true }));
        }
      },
      { uomIdx: options.uomIndex },
    );
    await page.waitForTimeout(200);
  }

  // 4. Set qty via AutoNumeric
  await page.evaluate(
    ({ qty }) => {
      const drawer = document.querySelector('.offcanvas.show') as HTMLElement;
      const qtyInput = drawer?.querySelector('.input-qty-target') as HTMLInputElement;
      if (!qtyInput) throw new Error('Qty input not found in drawer');

      if (typeof AutoNumeric !== 'undefined') {
        const instance = AutoNumeric.getAutoNumericElement(qtyInput);
        if (instance) {
          instance.set(qty);
          qtyInput.dispatchEvent(new Event('change', { bubbles: true }));
          return;
        }
      }
      qtyInput.value = String(qty);
      qtyInput.dispatchEvent(new Event('change', { bubbles: true }));
    },
    { qty: options.qty },
  );

  // 5. Isi serial numbers
  if (options.serialNumbers && options.serialNumbers.length > 0) {
    await page.waitForTimeout(500);
    await page.evaluate(
      ({ serials }) => {
        const drawer = document.querySelector('.offcanvas.show') as HTMLElement;
        const inputs = drawer?.querySelectorAll('.input-sn-item');
        serials.forEach((sn, i) => {
          if (inputs[i]) (inputs[i] as HTMLInputElement).value = sn;
        });
      },
      { serials: options.serialNumbers },
    );
  }

  // 6. Klik Apply/Save
  await page.click('.offcanvas.show .btn-save-drawer');

  // 7. Tunggu drawer tertutup
  await page.waitForSelector('.offcanvas.show', { state: 'hidden', timeout: 3_000 })
    .catch(() => { /* drawer mungkin sudah hilang */ });
  await page.waitForTimeout(300);
}

/**
 * Shortcut: Set qty tanpa buka drawer (bypass drawer, langsung set hidden inputs).
 * HANYA untuk test yang tidak perlu menguji drawer interaction.
 */
export async function setLineItemQtyDirect(
  page: Page,
  rowIndex: number,
  qty: number,
): Promise<void> {
  await page.evaluate(
    ({ idx, q }) => {
      const rows = document.querySelectorAll('#line-container .line-row');
      const row = rows[idx];
      if (!row) throw new Error(`Row ${idx} not found`);
      const qtyEl = row.querySelector('.input-qty') as HTMLInputElement;

      if (typeof AutoNumeric !== 'undefined') {
        try {
          const instance = AutoNumeric.getAutoNumericElement(qtyEl);
          if (instance) {
            instance.set(q);
            qtyEl.dispatchEvent(new Event('input', { bubbles: true }));
            return;
          }
        } catch { /* fallthrough */ }
      }
      qtyEl.value = String(q);
      qtyEl.dispatchEvent(new Event('input', { bubbles: true }));
    },
    { idx: rowIndex, q: qty },
  );
}
```

##### 5.5.6 Form Submission Helper

```typescript
// e2e/helpers/form.ts
import { Page, expect } from '@playwright/test';
import { FORM } from '../selectors/selectors';

interface SubmitResult {
  success: boolean;
  message?: string;
  validationErrors?: Record<string, string>;
  httpStatus: number;
}

/**
 * Submit AJAX form dan tunggu response JSON.
 *
 * FLOW (dari erp-form-handler.js):
 * 1. Form POST (JSON body + X-CSRF-TOKEN header) → Controller
 * 2. Success: { success: true, message } → redirect via sessionStorage + window.location.href
 * 3. Error: { success: false, validationErrors } → field.classList.add('is-invalid')
 */
export async function submitFormAndWait(
  page: Page,
  expectedRedirectPath?: string,
): Promise<SubmitResult> {
  const responsePromise = page.waitForResponse(
    (resp) =>
      resp.request().method() === 'POST' &&
      (resp.headers()['content-type']?.includes('json') ?? false),
    { timeout: 15_000 },
  );

  await page.click(FORM.submitButton);

  const response = await responsePromise;
  const body = await response.json();

  if (body.success && expectedRedirectPath) {
    await page.waitForURL(`**${expectedRedirectPath}**`, { timeout: 10_000 });
  }

  return {
    success: body.success ?? false,
    message: body.message,
    validationErrors: body.validationErrors,
    httpStatus: response.status(),
  };
}

/**
 * Assert success alert muncul di list page setelah redirect.
 */
export async function expectSuccessAlert(page: Page): Promise<void> {
  await expect(page.locator('.alert-success, .alert-ajax-global')).toBeVisible({ timeout: 5_000 });
}
```

##### 5.5.7 HTMX Helper

```typescript
// e2e/helpers/htmx.ts
import { Page } from '@playwright/test';

/**
 * Search di list page via HTMX (debounced input).
 */
export async function searchInList(
  page: Page,
  keyword: string,
): Promise<void> {
  const searchInput = page.locator('input[name="keyword"]');
  await searchInput.fill(keyword);
  await searchInput.press('Enter');

  await page.waitForResponse(
    (resp) => resp.url().includes('keyword=') && resp.status() === 200,
    { timeout: 10_000 },
  );
  await page.waitForTimeout(300);
}

/**
 * Tunggu HTMX swap selesai via htmx:afterSwap event listener.
 */
export async function waitForHtmxSwap(
  page: Page,
  timeoutMs: number = 10_000,
): Promise<void> {
  await page.evaluate(
    (timeout) => {
      return new Promise<void>((resolve, reject) => {
        const timer = setTimeout(() => reject(new Error('HTMX swap timeout')), timeout);
        document.body.addEventListener(
          'htmx:afterSwap',
          () => { clearTimeout(timer); resolve(); },
          { once: true },
        );
      });
    },
    timeoutMs,
  );
}
```

#### 5.6 Test Examples — Concrete Implementations

##### 5.6.1 Simple CRUD: Brand (baseline pattern)

```typescript
// e2e/tests/inventory/brand.spec.ts
import { test, expect } from '../../fixtures/erp-fixtures';
import { submitFormAndWait, expectSuccessAlert } from '../../helpers/form';
import { searchInList } from '../../helpers/htmx';

const MODULE_URL = '/inventory/brands';
const BRAND_NAME = `E2E-Brand-${Date.now()}`;

test.describe('Inventory > Brand CRUD', () => {
  test('01 - Create brand', async ({ authenticatedPage: page }) => {
    await page.goto(`${MODULE_URL}/create`);
    await page.waitForLoadState('networkidle');
    await page.fill('input[name="name"]', BRAND_NAME);

    const result = await submitFormAndWait(page, MODULE_URL);
    expect(result.success).toBe(true);
    await expectSuccessAlert(page);
  });

  test('02 - Verify brand in list', async ({ authenticatedPage: page }) => {
    await page.goto(MODULE_URL);
    await searchInList(page, BRAND_NAME);
    await expect(page.locator(`tbody tr:has-text("${BRAND_NAME}")`)).toBeVisible();
  });

  test('03 - Edit brand', async ({ authenticatedPage: page }) => {
    await page.goto(MODULE_URL);
    await searchInList(page, BRAND_NAME);
    await page.locator(`tbody tr:has-text("${BRAND_NAME}") a[href*="edit"]`).click();
    await page.waitForLoadState('networkidle');

    const editedName = `${BRAND_NAME}-EDITED`;
    await page.fill('input[name="name"]', editedName);

    const result = await submitFormAndWait(page, MODULE_URL);
    expect(result.success).toBe(true);
  });

  test('04 - Validation error on empty name', async ({ authenticatedPage: page }) => {
    await page.goto(`${MODULE_URL}/create`);
    await page.waitForLoadState('networkidle');
    await page.fill('input[name="name"]', '');

    const result = await submitFormAndWait(page);
    expect(result.success).toBe(false);
    expect(result.validationErrors).toBeDefined();
  });
});
```

##### 5.6.2 Complex CRUD: Product (TomSelect + AutoNumeric)

```typescript
// e2e/tests/inventory/product.spec.ts
import { test, expect } from '../../fixtures/erp-fixtures';
import { selectTomSelect, getTomSelectValue } from '../../helpers/tomselect';
import { setAutoNumeric } from '../../helpers/autonumeric';
import { submitFormAndWait } from '../../helpers/form';

const MODULE_URL = '/inventory/products';
const PRODUCT_NAME = `E2E-Product-${Date.now()}`;

test.describe('Inventory > Product CRUD', () => {
  test('01 - Create product with TomSelect fields', async ({ authenticatedPage: page }) => {
    await page.goto(`${MODULE_URL}/create`);
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(500);

    await page.fill('input[name="name"]', PRODUCT_NAME);

    // TomSelect: Category
    const category = await selectTomSelect(page, '#category-select', '', 0);
    expect(category.id).toBeTruthy();

    // Standard select: UoM
    await page.selectOption('#uom-select', { index: 1 });

    // TomSelect: Brand
    await selectTomSelect(page, '#brand-select', '', 0);

    // AutoNumeric fields
    await setAutoNumeric(page, 'input[name="minStock"]', 10);
    await setAutoNumeric(page, 'input[name="maxStock"]', 100);

    const result = await submitFormAndWait(page, MODULE_URL);
    expect(result.success).toBe(true);
  });

  test('02 - Edit product retains TomSelect values', async ({ authenticatedPage: page }) => {
    await page.goto(`${MODULE_URL}/edit/901`);
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(500);

    // Assert TomSelect pre-populated
    const category = await getTomSelectValue(page, '#category-select');
    expect(category.id).toBeTruthy();
    expect(category.text).toBeTruthy();

    await page.fill('input[name="name"]', 'E2E Product Standard - Edited');
    const result = await submitFormAndWait(page, MODULE_URL);
    expect(result.success).toBe(true);
  });
});
```

##### 5.6.3 Most Complex: Stock Adjustment (Line Items + Drawers + Cascade + Serial)

```typescript
// e2e/tests/inventory/stock-adjustment.spec.ts
import { test, expect } from '../../fixtures/erp-fixtures';
import { selectTomSelect } from '../../helpers/tomselect';
import { setFlatpickrDate } from '../../helpers/flatpickr';
import { setAutoNumeric, setLineItemNumeric } from '../../helpers/autonumeric';
import { addLineItem, getLineItemCount, selectLineItemTomSelect } from '../../helpers/line-items';
import { setLineItemQtyViaDrawer } from '../../helpers/drawers';
import { submitFormAndWait } from '../../helpers/form';

const MODULE_URL = '/inventory/adjustments';

test.describe('Inventory > Stock Adjustment (Full Flow)', () => {
  test('01 - Create adjustment with non-serial product', async ({ authenticatedPage: page }) => {
    await page.goto(`${MODULE_URL}/create`);
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(800);

    // ── HEADER ──
    await setFlatpickrDate(page, 'input[name="transactionDate"]', '2026-01-15');
    await page.selectOption('#header-currency', { index: 0 });
    await setAutoNumeric(page, '#header-rate', 1);
    await selectTomSelect(page, '#header-facility', '', 0);
    await page.fill('textarea[name="note"]', 'E2E test adjustment');

    // ── LINE ITEM ──
    const rowIndex = await addLineItem(page, {
      product: 'E2E Product Standard',
      grid: '',
      container: '',
    });

    await setLineItemQtyViaDrawer(page, rowIndex, { qty: 5 });
    await setLineItemNumeric(page, rowIndex, 'input-price', 50000);

    expect(await getLineItemCount(page)).toBe(1);

    // ── SUBMIT ──
    const result = await submitFormAndWait(page, MODULE_URL);
    expect(result.success).toBe(true);
  });

  test('02 - Create adjustment with serialized product', async ({ authenticatedPage: page }) => {
    await page.goto(`${MODULE_URL}/create`);
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(800);

    await setFlatpickrDate(page, 'input[name="transactionDate"]', '2026-01-16');
    await page.selectOption('#header-currency', { index: 0 });
    await setAutoNumeric(page, '#header-rate', 1);
    await selectTomSelect(page, '#header-facility', '', 0);

    await page.click('#btn-add-line');
    await page.waitForTimeout(800);
    await selectLineItemTomSelect(page, 0, 'select-product', 'E2E Product Serialized');
    await page.waitForTimeout(500);
    await selectLineItemTomSelect(page, 0, 'select-grid', '');
    await selectLineItemTomSelect(page, 0, 'select-container', '');

    await setLineItemQtyViaDrawer(page, 0, {
      qty: 2,
      serialNumbers: ['SN-E2E-001', 'SN-E2E-002'],
    });

    await setLineItemNumeric(page, 0, 'input-price', 1500000);

    const result = await submitFormAndWait(page, MODULE_URL);
    expect(result.success).toBe(true);
  });

  test('03 - Edit existing adjustment preserves line items', async ({ authenticatedPage: page }) => {
    await page.goto(MODULE_URL);
    await page.waitForLoadState('networkidle');

    const editLink = page.locator('tbody tr:first-child a[href*="edit"]');
    if (await editLink.isVisible()) {
      await editLink.click();
      await page.waitForLoadState('networkidle');
      await page.waitForTimeout(800);

      expect(await getLineItemCount(page)).toBeGreaterThan(0);
    }
  });

  test('04 - Add multiple lines', async ({ authenticatedPage: page }) => {
    await page.goto(`${MODULE_URL}/create`);
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(800);

    await setFlatpickrDate(page, 'input[name="transactionDate"]', '2026-01-17');
    await page.selectOption('#header-currency', { index: 0 });
    await setAutoNumeric(page, '#header-rate', 1);
    await selectTomSelect(page, '#header-facility', '', 0);

    await addLineItem(page, { product: '', grid: '', container: '', price: 10000 });
    await addLineItem(page, { product: '', grid: '', container: '', price: 20000 });

    expect(await getLineItemCount(page)).toBe(2);
  });
});
```

#### 5.7 Debugging Experience

##### 5.7.1 Trace Viewer (Primary Debug Tool)

Konfigurasi `trace: 'retain-on-failure'` menghasilkan trace zip:

| Data | Kegunaan |
|------|----------|
| DOM snapshot per action | Lihat state DOM saat `selectTomSelect()` dipanggil |
| Network log | Verifikasi `/api/lookup/*` responses |
| Console log | Lihat `[ERP-FORM]` messages dari erp-form-handler.js |
| Screenshot per step | Visual state sebelum dan sesudah klik |
| Action timeline | Urutan klik/fill/evaluate dengan timing |

Buka trace: `npx playwright show-trace test-results/*/trace.zip` atau upload ke https://trace.playwright.dev.

##### 5.7.2 TomSelect Debug Helper

```typescript
// Dump TomSelect state saat error — tambahkan ke helpers/assertions.ts
export async function debugTomSelectState(page: Page, selector: string): Promise<void> {
  const state = await page.evaluate((sel) => {
    const el = document.querySelector(sel) as HTMLSelectElement & { tomselect?: any };
    if (!el?.tomselect) return { error: 'Not initialized', selector: sel };
    const ts = el.tomselect;
    return {
      value: ts.getValue(),
      optionCount: Object.keys(ts.options).length,
      items: ts.items,
      isLoading: ts.isLoading,
      isDisabled: ts.isDisabled,
      loadedSearches: Object.keys(ts.loadedSearches || {}),
    };
  }, selector);
  console.log(`[DEBUG TomSelect] ${selector}:`, JSON.stringify(state, null, 2));
}
```

##### 5.7.3 Screenshot on Custom Assertions

```typescript
export async function assertWithScreenshot(
  page: Page,
  assertion: () => Promise<void>,
  label: string,
): Promise<void> {
  try {
    await assertion();
  } catch (error) {
    await page.screenshot({
      path: `test-results/debug-${label}-${Date.now()}.png`,
      fullPage: true,
    });
    throw error;
  }
}
```

---

### 6. CI/CD Integration

#### 6.1 GitHub Actions Workflow

```yaml
# .github/workflows/e2e-tests.yml
name: E2E Tests

on:
  pull_request:
    branches: [main, develop]
  push:
    branches: [main]

jobs:
  e2e:
    runs-on: ubuntu-latest
    timeout-minutes: 30

    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: 'maven'

      - name: Build Spring Boot JAR
        run: ./mvnw package -DskipTests -Pe2e -q

      - name: Start Spring Boot
        run: |
          java -jar target/solusi-program-erp-*.jar \
            --spring.profiles.active=e2e &
          echo $! > spring-boot.pid

      - name: Wait for Spring Boot
        run: |
          for i in $(seq 1 60); do
            curl -sf http://localhost:18080/login > /dev/null 2>&1 && break
            sleep 1
          done

      - name: Set up Node.js
        uses: actions/setup-node@v4
        with:
          node-version: '20'

      - name: Install Playwright
        working-directory: e2e
        run: |
          npm ci
          npx playwright install chromium --with-deps

      - name: Run E2E Tests
        working-directory: e2e
        env:
          BASE_URL: http://localhost:18080
          CI: true
        run: npx playwright test

      - name: Upload report on failure
        if: failure()
        uses: actions/upload-artifact@v4
        with:
          name: playwright-report
          path: |
            e2e/playwright-report/
            e2e/test-results/
          retention-days: 14

      - name: Stop Spring Boot
        if: always()
        run: kill $(cat spring-boot.pid) || true
```

#### 6.2 Estimated CI Time

| Phase | Durasi |
|-------|--------|
| Maven build (cached) | ~60s |
| Spring Boot startup (H2) | ~15s |
| Node.js + Playwright install | ~30s |
| Test execution (20 specs, serial) | ~5–8 min |
| **Total** | **~8–10 min** |

---

### 7. Cakupan Test Minimum per Modul

#### 7.1 Prioritas Tier

| Tier | Modul | Kompleksitas UI | Tests | Justification |
|------|-------|----------------|-------|---------------|
| **P0** | Auth (Login) | Low | 2 | Gate untuk semua test lain |
| **P0** | Brand | Low (text only) | 4 | Baseline CRUD pattern |
| **P0** | Product Category | Low | 4 | Seed dependency untuk Product |
| **P0** | Product | Medium (TomSelect+AutoNumeric) | 4 | Core inventory entity |
| **P0** | Stock Adjustment | High (LineItems+Drawer+Serial+Cascade) | 5 | Paling kompleks, paling rawan |
| **P1** | Facility / Grid / Container | Medium (cascade) | 3+3+3 | Warehouse hierarchy |
| **P1** | Party | Medium (inline tables, TomSelect) | 4 | Core master data |
| **P1** | Geographic | Low-Medium | 3 | Cascade hierarchy |
| **P1** | Currency | Low + AutoNumeric | 3 | Rate testing |
| **P1** | UoM + Conversion | Medium | 3+3 | Dependency untuk Stock Adj |
| **P2** | Tax, Bank Account | Low | 3+3 | Simple CRUD |
| **P2** | CoA, Period, Schema | Low-Medium | 3+3+3 | Accounting foundation |
| **P2** | News, Approval | Medium | 3+4 | Workflow testing |
| **P2** | Users, Roles | Medium | 3+3 | Permission matrix |

#### 7.2 Test Matrix per Modul (P0 — Detail)

##### Brand (4 tests)
| # | Test | Helpers Used | Assertion |
|---|------|-------------|-----------|
| 1 | Create | form.submitFormAndWait | success=true, redirect to list |
| 2 | Verify in list | htmx.searchInList | Row visible with correct name |
| 3 | Edit | form.submitFormAndWait | success=true, name changed |
| 4 | Validation (empty) | form.submitFormAndWait | success=false, validationErrors |

##### Product (4 tests)
| # | Test | Helpers Used | Assertion |
|---|------|-------------|-----------|
| 1 | Create with TomSelect | tomselect×2, autonumeric×2 | success=true |
| 2 | Verify in list | htmx.searchInList | Row with name + category |
| 3 | Edit retains TomSelect | tomselect.getTomSelectValue | Pre-populated values |
| 4 | Validation | form | Required fields validated |

##### Stock Adjustment (5 tests)
| # | Test | Helpers Used | Assertion |
|---|------|-------------|-----------|
| 1 | Create (non-serial) | flatpickr, tomselect, lineItems, drawers, autonumeric | success=true |
| 2 | Create (serialized) | All above + serialNumbers | success=true |
| 3 | Edit preserves lines | lineItems.getLineItemCount | count > 0 |
| 4 | Add multiple lines | lineItems × 3 | count correct |
| 5 | Remove line | lineItems.removeLineItem | count decremented |

#### 7.3 Total Test Count

| Tier | Modules | Tests |
|------|---------|-------|
| P0 | 5 | 19 |
| P1 | 7 | 22 |
| P2 | 7 | 25 |
| **Total** | **19** | **~66** |

---

### 8. Risiko & Mitigasi

| # | Risiko | Probabilitas | Dampak | Mitigasi |
|---|--------|-------------|--------|----------|
| 1 | **H2 incompatible** dengan beberapa Flyway migration (syntax MariaDB-specific: `ON UPDATE CURRENT_TIMESTAMP(6)`, `FULLTEXT INDEX`) | **Tinggi** | E2E profile gagal start | Buat `V999__H2_Compatibility_Shim.sql`. Jika >5 migration gagal → pivot ke TestContainers + MariaDB image |
| 2 | **TomSelect API berubah** saat upgrade versi library | Sedang | Helper perlu update | Pin versi TomSelect. Helper pakai stable API (`load`, `setValue`, `getValue`) yang tidak berubah sejak v1 |
| 3 | **Flaky test** karena timing (TomSelect init, drawer animation, HTMX swap) | **Tinggi** | CI merah false positive | Semua helper include `waitForFunction()` sebelum interact. Retry 2x di CI. Explicit `waitForTimeout()` pada transisi CSS |
| 4 | **Seed data bentrok** dengan Flyway migration update | Sedang | FK violation | ID range 901–999 tidak overlap production seed (1–100) |
| 5 | **CI time membengkak** saat suite bertambah | Rendah | Developer skip E2E | Sequential 1 worker; optimize via shared storageState login session |
| 6 | **Spring Boot 4 breaking changes** pada CSRF handling | Rendah | Login fixture gagal | CSRF token dari hidden input `_csrf` adalah standard Spring pattern |
| 7 | **AutoNumeric locale mismatch** antara dev (id-ID) dan CI (en-US) | Sedang | Formatted value assertion gagal | SELALU assert raw value via `getAutoNumericValue()`, BUKAN formatted display |
| 8 | **Drawer DOM structure berubah** | Rendah | Drawer helper gagal | Selector di-centralize di `selectors.ts` — perubahan hanya 1 file |

---

### 9. Effort Estimate

| Phase | Task | Effort | Dependencies |
|-------|------|--------|-------------|
| **Setup** | application-e2e.yaml + H2 profile + Maven profile | 2 hari | DevOps |
| **Setup** | H2 Flyway migration compatibility shim (46 migration audit) | 3 hari | DBA/Backend |
| **Setup** | e2e/ project scaffold (package.json, tsconfig, config) | 0.5 hari | — |
| **Setup** | data-e2e.sql seed file | 1 hari | Schema knowledge |
| **Core** | Helper library (tomselect, flatpickr, autonumeric, form, htmx) | 3 hari | ★ Frontend Specialist |
| **Core** | Line items + drawer helpers | 2 hari | ★ Frontend Specialist |
| **Core** | Custom fixtures + selector registry | 1 hari | ★ Frontend Specialist |
| **Tests** | P0 tests (Auth, Brand, Category, Product, StockAdj) — 19 tests | 3 hari | Core helpers done |
| **Tests** | P1 tests (Facility chain, Party, Geographic, Currency, UoM) — 22 tests | 4 hari | P0 done |
| **Tests** | P2 tests (Tax, Bank, Accounting, News, Security) — 25 tests | 4 hari | P1 done |
| **CI** | GitHub Actions workflow + artifact upload | 1 hari | Setup done |
| **QA** | Stabilize flaky tests + tune timeouts | 2 hari | All tests written |
| | | **~26.5 hari** | **~5.5 minggu (1 developer)** |

**Rekomendasi Tim:**
- 1 Frontend Specialist (helper library + P0 tests) — 2.5 minggu
- 1 Backend Developer (H2 profile + seed + Flyway shim) — 1 minggu
- P1/P2 tests parallelizable setelah helper library selesai

---

### 10. Trade-offs

| Keputusan | Dipilih | Tidak Dipilih | Alasan |
|-----------|---------|---------------|--------|
| **Database** | H2 in-memory MODE=MySQL | TestContainers + MariaDB | H2 lebih cepat (15s vs 40s startup), zero Docker dependency di CI. Trade-off: perlu compatibility shim |
| **Interaksi komponen** | JavaScript API (`ts.setValue()`, `fp.setDate()`, `an.set()`) | DOM simulation (klik dropdown → klik option) | JS API: 0% flakiness, 100% speed. Trade-off: tidak menguji bahwa dropdown muncul saat diklik user |
| **Test runner** | @playwright/test (Node.js, TypeScript) | Selenium + Java | Playwright: trace viewer, auto-wait, modern API. Trade-off: polyglot stack (Java + TS) |
| **Parallelism** | Sequential (1 worker) | Parallel (multiple workers) | CRUD tests punya implicit dependency (create sebelum edit). Trade-off: CI ~8 menit bukan ~3 menit |
| **Login strategy** | Login per test (fresh session) | Shared storageState | Isolation lebih baik, no state leak. Trade-off: ~2s overhead per test |
| **Selector approach** | Existing `name`/`id`/`class` | Custom `data-testid` di 59 file | Menghindari massive template refactor. Trade-off: jika fragment refactored, beberapa ID bisa hilang |
| **Scope: Approval flow** | P2 (deferred) | P0 | Multi-user + signature terlalu kompleks untuk phase 1. Trade-off: modul bisnis kritikal belum ter-cover |
| **Video recording** | Only on failure | Always | Always-on = ~50MB per run. Trade-off: sedikit lebih sulit debug rare race conditions |
| **H2 fallback plan** | Pivot ke TestContainers jika >5 migration gagal | — | Keputusan setelah audit 46 Flyway migrations |
