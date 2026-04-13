# PROPOSAL FINAL — Playwright E2E Test Strategy untuk Solusi Program ERP

> Synthesized dari 10 proposal kompetisi. Setiap keputusan desain mencantumkan sumber referensi.

---

## 1. Executive Summary (150 kata)

Proposal ini mengimplementasikan Playwright E2E test suite untuk ERP Spring Boot 4.0.3 + Thymeleaf, menggunakan H2 in-memory database dengan MODE=MySQL sebagai pengganti MariaDB. **Pendekatan utama**: dual Flyway migration location (`db/migration` + `db/migration-h2`) untuk menangani 12 incompatibility pattern MariaDB→H2 yang sudah dikatalogkan [Dari P-03], dikombinasikan dengan production-ready helper library untuk TomSelect, Flatpickr, AutoNumeric, dan HTMX [Dari P-04]. Security testing menggunakan 6 user personas dengan role berbeda [Dari P-05]. Anti-flakiness diterapkan melalui smart-wait patterns dan `uniqueId()` data factory [Dari P-08]. Arsitektur test menggunakan Page Object base class untuk extensibility 20+ modul [Dari P-10]. MVP dimulai dengan 9 core test specs [Dari P-01], dan CI pipeline terintegrasi dalam workflow existing dengan conditional triggers dan `needs: [fast-tests]` gating [Dari P-02 + P-09]. Target: confidence bahwa setiap modul CRUD berfungsi end-to-end setelah setiap code change.

---

## 2. Arsitektur Solusi

### 2.1 Overview Arsitektur [Dari P-03 + P-10]

```
┌───────────────────────────────────────────────────────────────┐
│                        CI Pipeline                             │
│   ┌──────────┐    ┌──────────────┐    ┌────────────────────┐  │
│   │ fast-test │───▶│ Build JAR    │───▶│ E2E Tests          │  │
│   │ (unit)   │    │ -Pe2e        │    │ (Playwright)       │  │
│   └──────────┘    └──────┬───────┘    └────────┬───────────┘  │
│                          │                     │               │
│                          ▼                     ▼               │
│   ┌──────────────────────────────────────────────────────────┐│
│   │           Spring Boot (profile: e2e)                     ││
│   │  ┌─────────┐  ┌──────────┐  ┌────────────────────────┐  ││
│   │  │Thymeleaf│  │ Security │  │ H2 in-memory           │  ││
│   │  │  SSR    │  │ (session)│  │ MODE=MySQL             │  ││
│   │  └─────────┘  └──────────┘  │ ┌────────────────────┐ │  ││
│   │                              │ │ Flyway migrations  │ │  ││
│   │                              │ │ db/migration (ori) │ │  ││
│   │                              │ │ db/migration-h2    │ │  ││
│   │                              │ │ (H2 overrides)     │ │  ││
│   │                              │ └────────────────────┘ │  ││
│   │                              │ ┌────────────────────┐ │  ││
│   │                              │ │ V9000 E2E Seed     │ │  ││
│   │                              │ └────────────────────┘ │  ││
│   │                              └────────────────────────┘  ││
│   └──────────────────────────────────────────────────────────┘│
└───────────────────────────────────────────────────────────────┘
```

### 2.2 Komponen Utama

1. **Spring Boot E2E Profile** — `--spring.profiles.active=e2e` memuat `application-e2e.yaml` yang menggunakan H2 in-memory, Flyway dual-location, dan mematikan secure cookie [Dari P-05]
2. **Flyway Dual-Location Migrations** — Original `db/migration/` untuk MariaDB + `db/migration-h2/` untuk H2 override patches [Dari P-03]
3. **V9000 Seed Data** — Single Flyway migration `V9000__e2e_seed_data.sql` yang berjalan terakhir [Dari P-10]
4. **Playwright Test Suite** — Subfolder `e2e-tests/` dengan @playwright/test, TypeScript, helper library [Dari P-04]
5. **CI Integration** — Job dalam existing `ci-java21.yml` dengan gating [Dari P-02 + P-09]

### 2.3 Alur Eksekusi

```
1. Maven build:  ./mvnw -B package -DskipTests -Pe2e
2. Start server: java -jar target/*.jar --spring.profiles.active=e2e
3. Health check: curl --retry 30 http://localhost:18080/login
4. Playwright:   cd e2e-tests && npx playwright test
5. Cleanup:      kill server process
```

### 2.4 Fallback Strategy: ddl-auto=create [Dari P-07]

Jika Flyway migration patching terlalu costly (>40% migration gagal di H2), gunakan:
```yaml
spring.jpa.hibernate.ddl-auto: create
spring.flyway.enabled: false
```
Trade-off: Schema dari @Entity ≠ schema dari Flyway migration (index names, CHECK constraints beda). Hanya gunakan sebagai Plan B, dan masukkan warning di CI log.

---

## 3. Spring Boot E2E Profile Design

### 3.1 Maven Profile untuk H2 Scope [Dari P-03]

```xml
<!-- pom.xml -->
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

> **Kritis**: `<scope>test</scope>` TIDAK cukup untuk `spring-boot:run -Pe2e` atau `java -jar`. Maven profile elevates scope ke `runtime` [Dari P-03].

### 3.2 application-e2e.yaml [Dari P-03 + P-05 + P-04]

```yaml
# src/main/resources/application-e2e.yaml

server:
  port: 18080
  servlet:
    session:
      timeout: 30m
      cookie:
        secure: false          # KRITIS: H2/localhost uses HTTP [Dari P-05]
        http-only: true
        same-site: lax

spring:
  datasource:
    url: >-
      jdbc:h2:mem:erp_e2e;
      MODE=MySQL;
      DB_CLOSE_DELAY=-1;
      DB_CLOSE_ON_EXIT=FALSE;
      DATABASE_TO_LOWER=TRUE;
      CASE_INSENSITIVE_IDENTIFIERS=TRUE;
      NON_KEYWORDS=VALUE
    driver-class-name: org.h2.Driver
    username: sa
    password:

  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: none           # Flyway controls schema
    properties:
      hibernate:
        globally_quoted_identifiers: true  # [Dari P-04]

  flyway:
    enabled: true
    baseline-on-migrate: true
    out-of-order: true         # [Dari P-02] multi-location compat
    locations:
      - classpath:db/migration
      - classpath:db/migration-h2

  h2:
    console:
      enabled: true
      path: /h2-console
      settings:
        web-allow-others: false

  thymeleaf:
    cache: false               # Force reload templates

logging:
  level:
    root: WARN
    com.erp: INFO
    org.flywaydb: INFO
```

### 3.3 H2/MariaDB Incompatibility Catalog [Dari P-03]

Berdasarkan analisis 45 Flyway migrations (V1–V45), berikut 12 incompatibility patterns yang harus di-patch:

| # | Pattern MariaDB | Masalah di H2 | Resolusi |
|---|----------------|---------------|----------|
| 1 | `ENGINE=InnoDB` | Not recognized | Buat override migration: hapus `ENGINE=InnoDB` |
| 2 | `DEFAULT CHARSET=utf8mb4` | Not supported | Hapus klausa |
| 3 | `COLLATE utf8mb4_unicode_ci` | Not supported | Hapus klausa |
| 4 | `ENUM('A','B','C')` | Dikenali di MODE=MySQL tapi perilaku bisa beda | Test; fallback ke `VARCHAR(50)` jika perlu |
| 5 | `ON DUPLICATE KEY UPDATE` | Supported di MODE=MySQL | Test per migration |
| 6 | `INSERT IGNORE` | Supported di MODE=MySQL | Test per migration |
| 7 | `SELECT ... FROM DUAL` | H2 tidak butuh `DUAL` | Hapus `FROM DUAL` |
| 8 | `COMMENT 'text'` pada kolom | Not supported | Hapus klausa |
| 9 | `MODIFY COLUMN` | Gunakan `ALTER COLUMN` | Override migration |
| 10 | `DATETIME(6)` | H2 uses `TIMESTAMP(6)` | Ganti ke `TIMESTAMP(6)` atau `TIMESTAMP` |
| 11 | `TINYINT(1)` as boolean | H2 MODE=MySQL handles this | Test; gunakan `BOOLEAN` jika gagal |
| 12 | `ADD COLUMN IF NOT EXISTS` | H2 supports `IF NOT EXISTS` | Usually compatible, test per migration |

### 3.4 Strategi Override Migration [Dari P-03]

Folder `db/migration-h2/` berisi HANYA migration yang perlu di-override:

```
src/main/resources/
├── db/
│   ├── migration/           # Original MariaDB migrations (V1–V45)
│   │   ├── V1__initial_schema.sql
│   │   ├── V2__...
│   │   └── V45__...
│   └── migration-h2/        # H2 overrides (hanya yang perlu patch)
│       ├── V1__initial_schema.sql    # Patched: no ENGINE, no CHARSET
│       ├── V5__xxx.sql               # Patched: MODIFY → ALTER COLUMN
│       └── V9000__e2e_seed_data.sql  # Seed data [Dari P-10]
```

**Cara Flyway menangani multi-location**: Jika migration dengan version yang sama ada di kedua lokasi, Flyway menggunakan YANG PERTAMA ditemukan berdasarkan urutan `locations`. Karena `db/migration-h2` di-list setelah `db/migration`, kita harus me-**reverse** urutan atau menggunakan `flyway.locations` yang menaruh override lebih dulu:

```yaml
flyway:
  locations:
    - classpath:db/migration-h2   # Override FIRST
    - classpath:db/migration       # Original SECOND
```

> **Penting**: Dengan urutan ini, jika `V1__initial_schema.sql` ada di kedua folder, H2 version akan digunakan. Migration yang TIDAK perlu override hanya ada di `db/migration/` dan dijalankan as-is.

**Resolusi Konflik P-03 vs P-07**: P-07 mengusulkan `ddl-auto=create` untuk menghindari migration patching sepenuhnya. Kita TIDAK memilih ini sebagai primary strategy karena: (1) schema dari @Entity bisa berbeda dari Flyway (index, constraint, default), (2) kita ingin memvalidasi bahwa migrations sendiri bisa jalan. Namun, P-07 approach dipertahankan sebagai documented fallback jika >40% migration memerlukan patch.

---

## 4. Data Seeding Strategy

### 4.1 Arsitektur 3-Layer Seeding [Dari P-06 + P-10 + P-05]

```
Layer 1: Flyway Migrations (V1–V45)
  └─ Schema creation + initial data dari production migrations

Layer 2: V9000__e2e_seed_data.sql (Flyway)  [Dari P-10]
  └─ E2E-specific master data: users, roles, permissions, business entities
  └─ Runs AFTER all schema migrations, BEFORE application startup completes

Layer 3: E2eDataSeeder (Java CommandLineRunner)  [Dari P-05 + P-01]
  └─ Programmatic seeding yang butuh BCrypt atau business logic
  └─ @Profile("e2e") @Order(200) — setelah SystemInitializer @Order(100)
```

### 4.2 V9000 Seed SQL [Dari P-10 + P-05]

```sql
-- V9000__e2e_seed_data.sql
-- Konvensi: V9000+ reserved untuk E2E seed data

-- ====== ROLES & PERMISSIONS ======
-- (SystemInitializer sudah membuat initial admin, kita tambah test users)

-- ====== 6 TEST USERS [Dari P-05] ======
-- Password: 'Test1234!' (BCrypt hash)
-- password_change_required = false (skip force-change flow untuk test speed)

INSERT INTO users (username, email, password, password_change_required, enabled, created_by, updated_by)
VALUES
  ('e2e_admin', 'admin@e2e.test', '$2a$10$...bcrypt_hash...', false, true, 'system', 'system'),
  ('e2e_manager', 'manager@e2e.test', '$2a$10$...bcrypt_hash...', false, true, 'system', 'system'),
  ('e2e_operator', 'operator@e2e.test', '$2a$10$...bcrypt_hash...', false, true, 'system', 'system'),
  ('e2e_viewer', 'viewer@e2e.test', '$2a$10$...bcrypt_hash...', false, true, 'system', 'system'),
  ('e2e_warehouse', 'warehouse@e2e.test', '$2a$10$...bcrypt_hash...', false, true, 'system', 'system'),
  ('e2e_auditor', 'auditor@e2e.test', '$2a$10$...bcrypt_hash...', false, true, 'system', 'system');

-- ====== USER-ROLE ASSIGNMENTS [Dari P-05] ======
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE (u.username = 'e2e_admin' AND r.name = 'ROLE_ADMIN')
   OR (u.username = 'e2e_manager' AND r.name = 'ROLE_MANAGER')
   OR (u.username = 'e2e_operator' AND r.name = 'ROLE_OPERATOR')
   OR (u.username = 'e2e_viewer' AND r.name = 'ROLE_VIEWER')
   OR (u.username = 'e2e_warehouse' AND r.name = 'ROLE_WAREHOUSE')
   OR (u.username = 'e2e_auditor' AND r.name = 'ROLE_AUDITOR');

-- ====== BUSINESS MASTER DATA ======
-- Dependency order: Brand → Category → UoM → Product → Facility → ...

-- Brands
INSERT INTO brands (id, name, code, active, created_by, updated_by)
VALUES
  (9001, 'E2E Brand Alpha', 'E2E-BRA', true, 'system', 'system'),
  (9002, 'E2E Brand Beta', 'E2E-BRB', true, 'system', 'system');

-- Categories
INSERT INTO categories (id, name, code, active, created_by, updated_by)
VALUES
  (9001, 'E2E Category Alpha', 'E2E-CATA', true, 'system', 'system');

-- Units of Measure
INSERT INTO uoms (id, name, code, active, created_by, updated_by)
VALUES
  (9001, 'E2E Piece', 'E2E-PCS', true, 'system', 'system'),
  (9002, 'E2E Box', 'E2E-BOX', true, 'system', 'system');

-- Products (depends on Brand, Category, UoM)
INSERT INTO products (id, name, sku, brand_id, category_id, base_uom_id, active, created_by, updated_by)
VALUES
  (9001, 'E2E Product Alpha', 'E2E-PRD-001', 9001, 9001, 9001, true, 'system', 'system');

-- Facilities
INSERT INTO facilities (id, name, code, type, active, created_by, updated_by)
VALUES
  (9001, 'E2E Warehouse Main', 'E2E-WH01', 'WAREHOUSE', true, 'system', 'system');
```

### 4.3 Dependency Order [Dari P-03 + P-06]

```
Level 0: roles, permissions (dari SystemInitializer)
Level 1: users, user_roles, role_permissions (V9000 seed)
Level 2: brands, categories, uoms (V9000 seed)
Level 3: products (depends: brand, category, uom)
Level 4: facilities (independent)
Level 5: inventory/stock_adjustments (depends: product, facility)
Level 6: purchase_requests, sales_orders (depends: product, facility, user)
```

### 4.4 Idempotency Strategy [Dari P-08 + P-03]

- ID range 9000+ reserved untuk E2E data — tidak akan conflict dengan production auto-increment
- Seed SQL menggunakan `INSERT` tanpa conflict handling — karena V9000 berjalan SEKALI setelah schema creation pada fresh H2 database
- Jika perlu re-run: H2 in-memory database di-reset setiap server restart (clean slate)
- `uniqueId()` pattern di Playwright untuk per-test data: `const id = 'e2e-' + Date.now()` [Dari P-08]

### 4.5 E2eDataSeeder (Java) [Dari P-05 + P-01]

```java
@Component
@Profile("e2e")
@Order(200) // After SystemInitializer (100)
public class E2eDataSeeder implements CommandLineRunner {
    
    @Override
    public void run(String... args) {
        // Programmatic seeding yang butuh BCrypt atau business logic
        // Contoh: update password_change_required = false untuk semua e2e users
        // (jika tidak bisa dilakukan via SQL karena BCrypt hashing)
        log.info("E2E Data Seeder: completed");
    }
}
```

**Resolusi Konflik P-02 vs P-05**: P-02 mengusulkan "test-as-seed" (test creates own data via UI). P-05 mengusulkan pre-seeded users. Kita pilih P-05 karena: test-as-seed membuat test order tightly coupled dan satu failure meng-cascade ke semua downstream tests. Pre-seeded master data = stable foundation.

---

## 5. Playwright Test Architecture

### 5.1 Struktur Folder [Dari P-04 + P-10]

```
e2e-tests/                     # Terpisah dari root — DILARANG install di root [constraint]
├── package.json
├── tsconfig.json
├── playwright.config.ts
├── .gitignore
├── helpers/                   # [Dari P-04] shared utilities
│   ├── auth.ts               # login(), logout(), getStorageState()
│   ├── tomselect.ts          # TomSelect interactions
│   ├── flatpickr.ts          # Flatpickr date picker
│   ├── autonumeric.ts        # AutoNumeric formatter
│   ├── htmx.ts               # HTMX wait & assertion
│   ├── form.ts               # Generic form submission (AJAX + native POST)
│   ├── navigation.ts         # Menu navigation, drawer
│   ├── data-factory.ts       # uniqueId(), test data generators [Dari P-08]
│   └── waits.ts              # Smart wait patterns [Dari P-08]
├── fixtures/                  # [Dari P-10] Playwright fixtures
│   └── base.ts               # Extended test fixture with auth + helpers
├── page-objects/              # [Dari P-10] Page Object pattern
│   ├── base.page.ts          # Base page with common methods
│   ├── login.page.ts
│   ├── brand.page.ts
│   ├── product.page.ts
│   └── ...
├── tests/                     # Test specs organized by module
│   ├── auth/
│   │   ├── login.spec.ts
│   │   └── rbac.spec.ts      # [Dari P-05]
│   ├── master-data/
│   │   ├── brand.spec.ts
│   │   ├── category.spec.ts
│   │   ├── uom.spec.ts
│   │   └── product.spec.ts
│   ├── inventory/
│   │   ├── stock-adjustment.spec.ts
│   │   └── stock-opname.spec.ts
│   ├── procurement/
│   │   ├── purchase-request.spec.ts
│   │   └── spl.spec.ts
│   └── sales/
│       └── sales-order.spec.ts
└── scripts/
    ├── start-server.sh        # [Dari P-09] One-command server start
    └── run-e2e.sh             # Full pipeline script
```

### 5.2 playwright.config.ts [Dari P-01 + P-08]

```typescript
import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './tests',
  timeout: 30_000,
  expect: { timeout: 10_000 },
  fullyParallel: false,            // Serial — shared H2 state [Dari P-01]
  workers: 1,                       // Single worker [Dari P-01 + P-08]
  retries: process.env.CI ? 1 : 0, // 1 retry in CI only [Dari P-08]
  reporter: [
    ['html', { open: 'never' }],
    ['json', { outputFile: 'results.json' }],
    process.env.CI ? ['github'] : ['list'],
  ],
  
  use: {
    baseURL: process.env.E2E_BASE_URL || 'http://localhost:18080',
    trace: 'on-first-retry',       // [Dari P-08] trace only on retry
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
    actionTimeout: 10_000,
    navigationTimeout: 15_000,
  },

  projects: [
    {
      name: 'setup',
      testMatch: /global-setup\.ts/,
    },
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
      dependencies: ['setup'],
    },
  ],
});
```

### 5.3 Helper: TomSelect [Dari P-04 + P-08]

```typescript
// helpers/tomselect.ts
import { Page, Locator } from '@playwright/test';

export async function selectTomSelect(
  page: Page, 
  selector: string, 
  searchText: string,
  optionText?: string
): Promise<void> {
  const target = optionText || searchText;
  
  // Wait for TomSelect to be initialized [Dari P-08]
  await page.waitForFunction(
    (sel) => {
      const el = document.querySelector(sel) as any;
      return el?.tomselect !== undefined;
    },
    selector,
    { timeout: 10_000 }
  );

  // Use TomSelect API directly — page.fill() WILL NOT WORK [constraint]
  await page.evaluate(
    ({ sel, search, opt }) => {
      const el = document.querySelector(sel) as any;
      const ts = el.tomselect;
      
      // Load options if remote
      ts.load(search, () => {
        // Find matching option
        const options = Object.values(ts.options) as any[];
        const match = options.find(
          (o: any) => o.text?.includes(opt) || o.name?.includes(opt)
        );
        if (match) {
          ts.setValue(match.value);
        }
      });
    },
    { sel: selector, search: searchText, opt: target }
  );

  // Verify selection was made
  await page.waitForFunction(
    (sel) => {
      const el = document.querySelector(sel) as any;
      return el.tomselect?.getValue() !== '';
    },
    selector,
    { timeout: 5_000 }
  );
}
```

### 5.4 Helper: Flatpickr [Dari P-04 + P-08]

```typescript
// helpers/flatpickr.ts
import { Page } from '@playwright/test';

export async function setFlatpickrDate(
  page: Page,
  selector: string,
  dateStr: string   // format: 'YYYY-MM-DD' or 'DD/MM/YYYY'
): Promise<void> {
  // Wait for Flatpickr to be initialized
  await page.waitForFunction(
    (sel) => {
      const el = document.querySelector(sel) as any;
      return el?._flatpickr !== undefined;
    },
    selector,
    { timeout: 10_000 }
  );

  // Use Flatpickr API directly — page.fill() WILL NOT WORK on readonly input
  await page.evaluate(
    ({ sel, date }) => {
      const el = document.querySelector(sel) as any;
      el._flatpickr.setDate(date, true); // true = trigger onChange
    },
    { sel: selector, date: dateStr }
  );
}
```

### 5.5 Helper: AutoNumeric [Dari P-04 + P-08]

```typescript
// helpers/autonumeric.ts
import { Page } from '@playwright/test';

export async function setAutoNumeric(
  page: Page,
  selector: string,
  value: number
): Promise<void> {
  // Wait for AutoNumeric to be initialized
  await page.waitForFunction(
    (sel) => {
      const el = document.querySelector(sel) as any;
      try {
        return window.AutoNumeric.getAutoNumericElement(el) !== null;
      } catch { return false; }
    },
    selector,
    { timeout: 10_000 }
  );

  // Use AutoNumeric API directly
  await page.evaluate(
    ({ sel, val }) => {
      const el = document.querySelector(sel) as any;
      const an = window.AutoNumeric.getAutoNumericElement(el);
      an.set(val);
    },
    { sel: selector, val: value }
  );
}
```

### 5.6 Helper: Auth & Session [Dari P-05 + P-09]

```typescript
// helpers/auth.ts
import { Page, BrowserContext } from '@playwright/test';
import path from 'path';

const STORAGE_DIR = path.join(__dirname, '..', '.auth');

export interface TestUser {
  username: string;
  password: string;
  role: string;
}

export const TEST_USERS: Record<string, TestUser> = {
  admin:     { username: 'e2e_admin',     password: 'Test1234!', role: 'ADMIN' },
  manager:   { username: 'e2e_manager',   password: 'Test1234!', role: 'MANAGER' },
  operator:  { username: 'e2e_operator',  password: 'Test1234!', role: 'OPERATOR' },
  viewer:    { username: 'e2e_viewer',    password: 'Test1234!', role: 'VIEWER' },
  warehouse: { username: 'e2e_warehouse', password: 'Test1234!', role: 'WAREHOUSE' },
  auditor:   { username: 'e2e_auditor',   password: 'Test1234!', role: 'AUDITOR' },
};

export async function login(page: Page, user: TestUser): Promise<void> {
  await page.goto('/login');
  await page.fill('input[name="username"]', user.username);
  await page.fill('input[name="password"]', user.password);
  await page.click('button[type="submit"]');
  
  // Verify login success — should redirect to dashboard
  await page.waitForURL('**/dashboard**', { timeout: 10_000 });
}

export async function loginAndSaveState(
  page: Page, 
  context: BrowserContext,
  user: TestUser
): Promise<string> {
  await login(page, user);
  const storagePath = path.join(STORAGE_DIR, `${user.role.toLowerCase()}.json`);
  await context.storageState({ path: storagePath });
  return storagePath;
}
```

### 5.7 Helper: Data Factory [Dari P-08]

```typescript
// helpers/data-factory.ts

let counter = 0;

export function uniqueId(prefix: string = 'e2e'): string {
  counter++;
  return `${prefix}-${Date.now()}-${counter}`;
}

export function uniqueName(entity: string): string {
  return `Test ${entity} ${uniqueId()}`;
}

export function uniqueCode(prefix: string): string {
  return `${prefix}-${Date.now().toString(36).toUpperCase()}`;
}
```

### 5.8 Helper: Smart Waits [Dari P-08]

```typescript
// helpers/waits.ts
import { Page } from '@playwright/test';

/** Wait for HTMX request to complete [Dari P-08] */
export async function waitForHtmx(page: Page): Promise<void> {
  await page.waitForFunction(() => {
    return document.querySelectorAll('.htmx-request').length === 0;
  }, { timeout: 15_000 });
}

/** Wait for all pending AJAX requests to settle */
export async function waitForNetworkIdle(page: Page): Promise<void> {
  await page.waitForLoadState('networkidle', { timeout: 15_000 });
}

/** Wait for toast/notification to appear (success feedback) */
export async function waitForToast(page: Page, text?: string): Promise<void> {
  if (text) {
    await page.waitForSelector(`.toast:has-text("${text}")`, { timeout: 10_000 });
  } else {
    await page.waitForSelector('.toast', { timeout: 10_000 });
  }
}
```

### 5.9 Selector Strategy [Dari P-04]

Hierarchical selector priority (paling stabil ke paling fragile):

1. **`[name="fieldName"]`** — HTML form name attributes, paling stabil
2. **`#elementId`** — ID attributes jika ada
3. **`[data-testid="xxx"]`** — Khusus ditambahkan untuk E2E (jika perlu)
4. **Role-based selectors** — `page.getByRole('button', { name: 'Save' })`
5. **Text-based** — `page.getByText('Submit')` — paling fragile, hindari untuk form fields

**DILARANG:** XPath, nth-child, CSS class selectors yang terikat styling.

---

## 6. CI/CD Integration

### 6.1 GitHub Actions Job [Dari P-02 + P-09]

```yaml
# Ditambahkan ke .github/workflows/ci-java21.yml (existing workflow)

  e2e-tests:
    name: E2E Tests (Playwright)
    runs-on: ubuntu-latest
    timeout-minutes: 20
    needs: [fast-tests]              # [Dari P-09] Only run if unit tests pass
    if: |
      github.event_name == 'schedule' ||
      github.event_name == 'workflow_dispatch' ||
      (github.event_name == 'push' && github.ref == 'refs/heads/main')
    
    concurrency:                     # [Dari P-02]
      group: e2e-${{ github.ref }}
      cancel-in-progress: true

    steps:
      # ── Setup ──
      - name: Checkout
        uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '21'
          cache: maven

      - name: Set up Node.js 20
        uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: npm
          cache-dependency-path: e2e-tests/package-lock.json

      # ── Build ──
      - name: Build Spring Boot JAR
        run: ./mvnw -B package -DskipTests -Pe2e -q

      # ── Start Server ──
      - name: Start Spring Boot (E2E profile)
        run: |
          java -jar target/solusi-program-erp-*.jar \
            --spring.profiles.active=e2e &
          echo $! > spring-boot.pid

      - name: Wait for server to be ready
        run: |
          for i in $(seq 1 60); do
            if curl -s -o /dev/null -w "%{http_code}" http://localhost:18080/login | grep -q "200"; then
              echo "Server ready after ${i}s"
              exit 0
            fi
            sleep 1
          done
          echo "Server failed to start within 60s"
          exit 1

      # ── Test ──
      - name: Install Playwright dependencies
        working-directory: e2e-tests
        run: |
          npm ci
          npx playwright install chromium --with-deps

      - name: Run Playwright tests
        working-directory: e2e-tests
        run: npx playwright test
        env:
          E2E_BASE_URL: http://localhost:18080

      # ── Artifacts ──
      - name: Upload test results
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: playwright-report
          path: |
            e2e-tests/playwright-report/
            e2e-tests/test-results/
          retention-days: 7

      # ── Cleanup ──
      - name: Stop Spring Boot
        if: always()
        run: |
          if [ -f spring-boot.pid ]; then
            kill $(cat spring-boot.pid) || true
          fi
```

### 6.2 CI Design Decisions

| Keputusan | Penjelasan | Sumber |
|-----------|-----------|--------|
| `needs: [fast-tests]` | E2E hanya jalan jika unit tests pass — fail fast | P-09 |
| Conditional trigger | Hanya pada push main, schedule, manual dispatch — tidak setiap PR push | P-02 |
| `concurrency` + `cancel-in-progress` | Jangan run E2E paralel untuk branch yang sama | P-02 |
| `timeout-minutes: 20` | Safety limit — normal run < 10 menit | P-02 |
| Single Chromium browser | SSR app tidak butuh multi-browser testing | P-01 |
| `retries: 1` di CI | Satu kali retry untuk menangani transient failures | P-08 |
| Artifact upload `always()` | Report/trace tersedia bahkan saat test gagal | P-02 |

### 6.3 Local Development Pipeline [Dari P-09]

```json
// e2e-tests/package.json — scripts
{
  "scripts": {
    "test": "npx playwright test",
    "test:headed": "npx playwright test --headed",
    "test:debug": "npx playwright test --debug",
    "test:ui": "npx playwright test --ui",
    "test:module": "npx playwright test --grep",
    "report": "npx playwright show-report"
  }
}
```

```bash
# One-command local run [Dari P-09]
# scripts/run-e2e.sh
#!/bin/bash
set -e

echo "Building JAR..."
./mvnw -B package -DskipTests -Pe2e -q

echo "Starting server..."
java -jar target/solusi-program-erp-*.jar --spring.profiles.active=e2e &
SERVER_PID=$!
trap "kill $SERVER_PID 2>/dev/null" EXIT

echo "Waiting for server..."
for i in $(seq 1 60); do
  curl -s http://localhost:18080/login > /dev/null && break
  sleep 1
done

echo "Running tests..."
cd e2e-tests
npm ci --silent
npx playwright install chromium --with-deps
npx playwright test "$@"
```

---

## 7. Cakupan Test Minimum per Modul

### 7.1 Definisi "Done" per Modul [Dari P-01 + P-05]

| # | Modul | Test Cases (Minimum) | UI Components Tested |
|---|-------|---------------------|---------------------|
| 1 | **Auth** | Login success, login gagal, logout, RBAC unauthorized access | Form POST, redirect |
| 2 | **Brand** | List, create, edit, (soft) delete | Form, TomSelect (jika ada parent) |
| 3 | **Category** | List, create, edit | Form |
| 4 | **UoM** | List, create, edit | Form |
| 5 | **Product** | List, create (with brand/category/uom), edit | TomSelect (3 fields), AutoNumeric (price) |
| 6 | **Facility** | List, create, edit | Form |
| 7 | **Stock Adjustment** | Create draft, approve | TomSelect (product, facility), AutoNumeric (qty), Flatpickr (date) |
| 8 | **Purchase Request** | Create, submit for approval, approve | TomSelect, AutoNumeric, line items |
| 9 | **SPL** | Create, edit, approve | TomSelect, Flatpickr, line items, AJAX |
| 10 | **Sales Order** | Create, edit | TomSelect, AutoNumeric, line items |

### 7.2 MVP Scope (Phase 1) [Dari P-01]

Phase 1 fokus pada 9 test specs yang mencakup SEMUA jenis UI component interaction:

```
tests/
├── auth/login.spec.ts              # Login/logout flow
├── master-data/brand.spec.ts       # Simple CRUD (form only)
├── master-data/product.spec.ts     # TomSelect + AutoNumeric
├── inventory/stock-adjustment.spec.ts  # All components + approval
├── procurement/spl.spec.ts         # AJAX form + line items + approval
```

### 7.3 Module Tagging [Dari P-09]

```typescript
// Brand test with tags
test.describe('@master-data @brand Brand CRUD', () => {
  test('should create new brand', async ({ page }) => { ... });
  test('should edit existing brand', async ({ page }) => { ... });
});
```

```bash
# Run only inventory tests
npx playwright test --grep @inventory

# Run only tests that use TomSelect
npx playwright test --grep @tomselect
```

---

## 8. Risiko & Mitigasi

### 8.1 Risiko Teridentifikasi [Dari P-08 + P-03 + P-05]

| # | Risiko | Severity | Likelihood | Mitigasi |
|---|--------|----------|-----------|----------|
| 1 | **H2 incompatibility** — MariaDB SQL syntax gagal di H2 | High | High | Katalog 12 patterns + dual migration override [P-03]. Fallback: ddl-auto=create [P-07] |
| 2 | **Flaky tests** — timing issues pada TomSelect/HTMX | High | Medium | Smart wait patterns [P-08], no `waitForTimeout()`, component-aware waits |
| 3 | **Migration drift** — H2 override out of sync dengan original | Medium | High | CI check: script yang membandingkan version numbers antara db/migration/ dan db/migration-h2/ [P-03] |
| 4 | **Seed data staleness** — entity model berubah tapi seed SQL tidak diupdate | Medium | Medium | CI fail-fast: Flyway error saat V9000 gagal. Seed SQL di-review setiap migration baru [P-06] |
| 5 | **Security bypass** — test tidak benar-benar melewati auth flow | Medium | Low | Login via real form POST, verifikasi CSRF token, RBAC tests [P-05] |
| 6 | **CI timeout** — server startup lambat atau test suite membesar | Medium | Medium | `timeout-minutes: 20`, module-specific test run, caching [P-02] |
| 7 | **password_change_required** — force-change flow mengganggu test | Low | High | Seed users dengan `password_change_required=false` [P-01 + P-05] |
| 8 | **Thymeleaf fragment error** — baru terdeteksi saat render | High | Medium | E2E test mem-visit setiap halaman utama — fragment error muncul sebagai HTTP 500 [P-06] |

### 8.2 Mitigasi Proaktif [Dari P-08]

**"10 Commandments" adapted:**
1. JANGAN gunakan `page.waitForTimeout()` — selalu gunakan smart waits
2. JANGAN gunakan CSS class selectors — gunakan name/id/role
3. JANGAN hard-code data — gunakan `uniqueId()` factory
4. SELALU cek HTMX request selesai sebelum assert
5. SELALU trace on first retry — untuk debugging CI failures
6. SELALU upload artifacts — report + screenshots + traces

---

## 9. Effort Estimate

### 9.1 Breakdown per Komponen [Dari P-01 + P-09]

| # | Komponen | Effort | Catatan |
|---|----------|--------|---------|
| 1 | H2 migration patches (analyze + create db/migration-h2/) | 3 hari | Perlu trial-and-error per migration |
| 2 | application-e2e.yaml + Maven profile | 0.5 hari | Straightforward config |
| 3 | V9000 seed SQL + E2eDataSeeder | 1 hari | 6 users + master data |
| 4 | e2e-tests/ project setup (package.json, playwright.config.ts) | 0.5 hari | Boilerplate |
| 5 | Helper library (tomselect, flatpickr, autonumeric, auth, waits) | 2 hari | Referensi: smoke-test-guide.md |
| 6 | MVP test specs (5 specs, ~20 test cases) | 3 hari | Iterative: write → run → fix |
| 7 | CI pipeline (GitHub Actions job) | 0.5 hari | Existing workflow extension |
| 8 | Documentation + onboarding README | 0.5 hari | e2e-tests/README.md |
| | **Total MVP** | **~11 hari** | ~2.5 minggu kerja |

### 9.2 Phase 2 Expansion

| # | Komponen | Effort | Catatan |
|---|----------|--------|---------|
| 9 | Additional module specs (5 more modules) | 3 hari | Leverage existing helpers |
| 10 | Page Object refactoring | 2 hari | After patterns stabilize |
| 11 | RBAC/security test suite | 2 hari | Permission matrix [P-05] |
| | **Total Phase 2** | **~7 hari** | ~1.5 minggu |

### 9.3 Total: ~18 hari (~3.5-4 minggu)

---

## 10. Trade-offs

### 10.1 Keputusan Utama dan Konsekuensinya

| Keputusan | Dipilih | Alternatif Ditolak | Alasan |
|-----------|---------|-------------------|--------|
| **H2 + Flyway patches** | ✅ Dual migration override | ddl-auto=create (P-07), Testcontainers MariaDB | Flyway validates migration compatibility; ddl-auto diverges from prod schema; Testcontainers butuh Docker di CI |
| **Serial execution** (workers: 1) | ✅ Sequential tests | Parallel workers | Shared H2 state; parallelism adds complexity tanpa significant gain (suite < 5 menit) [P-01 + P-08] |
| **Separate Node.js project** | ✅ e2e-tests/ subfolder | Maven Playwright plugin | Node.js Playwright ecosystem lebih mature; TypeScript auto-complete; VS Code extension [P-09] |
| **Pre-seeded data** | ✅ V9000 SQL seed | Test-as-seed via UI (P-02) | Pre-seed = stable foundation; test-as-seed = brittle chain dependencies |
| **Chromium only** | ✅ Single browser | Multi-browser | SSR app → browser-specific issues minimal; Chromium covers 80%+ users [P-01] |
| **Login via real form** | ✅ POST /login | Direct cookie injection | Real login tests CSRF, security filters, password flow [P-05] |
| **Flyway dual-location** | ✅ db/migration-h2/ overrides | Single modified migration set | Production migrations untouched; overrides are additive [P-03] |

### 10.2 Yang Dikorbankan

1. **Migration maintenance burden** — Setiap migration baru perlu dicek H2 compatibility. Mitigasi: CI script yang alerts pada migration tanpa H2 override.
2. **Schema fidelity** — H2 MODE=MySQL ≠ 100% MariaDB. E2E test verifikasi UI behavior, BUKAN database behavior. Staging test terhadap MariaDB riil tetap dibutuhkan.
3. **No multi-browser testing** — Firefox/Safari bugs tidak terdeteksi. Acceptable untuk SSR app.
4. **No visual regression** — Hanya functional testing. Screenshot comparison bisa ditambahkan Phase 3.

---

## Synthesis Notes

### Keputusan Synthesis Paling Kritis

#### 1. Flyway Dual-Location vs ddl-auto=create (P-03 vs P-07)
**Dipilih: Flyway dual-location (P-03).** P-07's `ddl-auto=create` sangat tempting karena menghilangkan SELURUH migration patching problem (~3 hari effort). Namun, dipilih P-03 karena:
- Schema dari Hibernate @Entity tidak identik dengan Flyway-created schema (index names, CHECK constraints, column defaults dari migration-specific logic)
- Jika test pass dengan ddl-auto schema tapi fail dengan Flyway schema, kita punya false positive
- Flyway dual-location memvalidasi bahwa migration SENDIRI bisa jalan (infrastructure test)
- P-07 approach dipertahankan sebagai **documented fallback** dengan clear trigger condition (>40% migration gagal)

#### 2. Pre-seeded Data vs Test-as-Seed (P-05 vs P-02)
**Dipilih: Pre-seeded data (P-05).** P-02's test-as-seed approach sebenarnya elegant (tests create their own data via UI, so seeding IS the test). Namun ditolak karena:
- Single point of failure: jika Brand creation test gagal, SEMUA downstream tests fail
- Test ordering becomes mandatory, menghilangkan ability to run tests independently
- Debugging menjadi harder: "is Product test failing because of Product code or Brand test?"
- Pre-seed master data + `uniqueId()` per-test dynamic data = best of both worlds

#### 3. Helper Approach vs Page Object Only (P-04 vs P-10)
**Dipilih: Both.** P-04's helper library ditaruh di `helpers/` (stateless utility functions). P-10's Page Object pattern ditaruh di `page-objects/` (stateful page representations). Mereka komplementer:
- Helpers = HOW to interact with TomSelect/Flatpickr (shared across all pages)
- Page Objects = WHERE to interact (specific to each module's page structure)
- Test spec menggunakan Page Object yang internally calls Helpers

#### 4. CI Integration Strategy (P-02 vs P-09 vs standalone)
**Dipilih: Integrated into existing ci-java21.yml (P-09) with P-02's conditional logic.** P-05's dedicated `e2e-security.yml` workflow terlalu narrow. Standalone workflow menambah maintenance. Integrated approach:
- `needs: [fast-tests]` gating (P-09) = fail-fast, no wasted CI minutes
- Conditional triggers (P-02) = only on main/schedule/dispatch, not every PR push
- Still uploadable artifacts per P-02's artifact strategy

#### 5. Effort Estimation (P-01 ~2 minggu vs P-08 ~12 minggu)
**Dipilih: ~11 hari MVP (closer to P-01/P-09).** P-08's 47 person-day estimate includes extensive anti-flakiness infrastructure yang bisa ditambahkan incrementally. MVP harus shipping value ASAP. Phase 2 adds security testing (P-05) dan additional module coverage. Anti-flakiness patterns dari P-08 diintegrasikan ke helpers dari awal (smart waits, no waitForTimeout), tapi advanced patterns (quarantine, monitoring) ditunda ke Phase 3.

---

*Proposal ini di-synthesize dari 10 proposal kompetisi (P-01 s/d P-10). Setiap keputusan desain mencantumkan referensi sumber [Dari P-XX] untuk traceability. Generated sebagai bagian dari Parallel Proposal Tournament.*
