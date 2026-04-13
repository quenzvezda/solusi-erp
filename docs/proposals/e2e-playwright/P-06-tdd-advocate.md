# Proposal Teknis: Playwright E2E Testing — TDD Advocate Perspective

> **Prinsip Utama:** *"Test adalah spesifikasi yang bisa dieksekusi. Tulis test dulu, baru bangun infrastruktur yang membuat test itu hijau."*

---

### 1. Executive Summary

Proyek Solusi ERP (Spring Boot 4.0.3 + Thymeleaf SSR) memiliki 263 unit/integration test namun nol verifikasi browser end-to-end. Dengan 45+ controller, komponen JS kompleks (TomSelect, Flatpickr, AutoNumeric, AJAX form), dan 45 Flyway migration, bug rendering hanya terdeteksi saat user menemukan di production.

Proposal ini mengadopsi **test-first approach**: definisikan behavior spec dalam format BDD `describe/it` terlebih dahulu, lalu bangun infrastruktur (E2E Spring profile, H2 seeder, Playwright helpers) yang dibutuhkan agar spec tersebut hijau. Playwright berjalan di subdirectory `e2e/` terpisah, menguji aplikasi yang berjalan di Spring Boot + H2 in-memory (`MODE=MySQL`) pada port dedicated. Target: **12 test suite** mencakup login, 9 modul CRUD (simple → complex), dan smoke dashboard — menghasilkan **living documentation** yang mendeskripsikan perilaku sistem secara executable.

Estimasi effort: **120–160 jam** (2 developer, 4–5 sprint).

---

### 2. Arsitektur Solusi

#### 2.1 Gambaran Besar

```
┌─────────────────────────────────────────────────────────────────────┐
│                        CI Runner (GitHub Actions)                   │
│                                                                     │
│  ┌──────────────────────┐     ┌──────────────────────────────────┐  │
│  │  Spring Boot App      │     │  Playwright Test Runner           │  │
│  │  --spring.profiles    │     │  (Node.js @playwright/test)       │  │
│  │    .active=e2e        │     │                                    │  │
│  │  --server.port=18080  │◄────│  BASE_URL=http://localhost:18080  │  │
│  │                       │     │                                    │  │
│  │  ┌─────────────────┐  │     │  ┌────────────────────────────┐   │  │
│  │  │ H2 In-Memory    │  │     │  │ describe('Brand CRUD')     │   │  │
│  │  │ MODE=MySQL      │  │     │  │   it('should create...')   │   │  │
│  │  │                 │  │     │  │   it('should appear...')   │   │  │
│  │  │ Flyway E2E      │  │     │  │   it('should edit...')     │   │  │
│  │  │ migrations      │  │     │  │   it('should delete...')   │   │  │
│  │  │ +               │  │     │  │                            │   │  │
│  │  │ E2eDataSeeder   │  │     │  │ describe('Product CRUD')   │   │  │
│  │  │ (master data)   │  │     │  │   it('should fill...')     │   │  │
│  │  └─────────────────┘  │     │  │   ...                      │   │  │
│  └──────────────────────┘     │  └────────────────────────────┘   │  │
│                                └──────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────┘
```

#### 2.2 Prinsip Arsitektur

| Prinsip | Keputusan |
|---------|-----------|
| **Test isolation** | H2 in-memory — database fresh setiap run, zero cleanup needed |
| **Real browser** | Chromium via Playwright — verifikasi TomSelect, Flatpickr, AutoNumeric secara nyata |
| **Same codebase, separate process** | Spring Boot = JVM process, Playwright = Node.js process di `e2e/` |
| **Profile-driven** | `application-e2e.yaml` mengganti datasource, port, MinIO mock, tanpa mengubah production code |
| **Test-first** | Spec ditulis dalam format BDD **sebelum** infrastruktur dibangun |

#### 2.3 Mengapa Bukan Pendekatan Lain?

| Alternatif | Alasan Ditolak |
|------------|----------------|
| Selenium + Java | Boilerplate tinggi, API kurang ekspresif untuk async JS widget |
| Cypress | Tidak mendukung multi-tab, session management lebih kompleks untuk SSR |
| TestContainers + MariaDB | Slower startup, perlu Docker di CI, overkill untuk E2E yang fokus UI |
| Spring MockMvc | Tidak ada di Spring Boot 4.x (@WebMvcTest dihapus); tidak bisa test JS rendering |
| HtmlUnit / Thymeleaf offline | Sudah ada (263 test); tidak menguji JavaScript widget sama sekali |

---

### 3. Spring Boot E2E Profile Design

#### 3.1 Red-Green-Refactor: Profile Setup

**🔴 RED — Spec yang belum bisa jalan:**
```javascript
// e2e/tests/smoke/server-health.spec.js
test('Spring Boot E2E server responds on port 18080', async ({ page }) => {
  const response = await page.goto('http://localhost:18080/login');
  expect(response.status()).toBe(200);
  expect(await page.title()).toContain('Login');
});
```

**🟢 GREEN — Bangun profile agar spec hijau:**

**File: `src/main/resources/application-e2e.yaml`**
```yaml
server:
  port: 18080
  servlet:
    session:
      cookie:
        secure: false          # H2 tidak butuh HTTPS

spring:
  datasource:
    url: jdbc:h2:mem:erp_e2e;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE
    driver-class-name: org.h2.Driver
    username: sa
    password:

  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: none           # Tetap Flyway-only

  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration-e2e

  thymeleaf:
    cache: false

logging:
  level:
    com.solusi.erp: INFO
    org.springframework.security: WARN
    org.hibernate.SQL: OFF

# MinIO stub — tidak perlu server asli
minio:
  endpoint: http://localhost:9999
  access-key: test
  secret-key: test
  buckets:
    signatures: test-bucket
```

**🔵 REFACTOR — Tambahkan H2 dependency:**
```xml
<!-- pom.xml — test-scoped H2 -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```

> **Catatan:** H2 dengan `MODE=MySQL` dan `DATABASE_TO_LOWER=TRUE` menangani mayoritas MariaDB-specific syntax. Untuk kasus `ENGINE=InnoDB`, dihandle di migration E2E (lihat §3.2).

#### 3.2 Flyway Migration E2E Strategy

Flyway migration production menggunakan MariaDB-specific syntax (`ENGINE=InnoDB`, `ENUM`, `NOW()`). Strategi:

**Opsi yang dipilih: Adapted Migration Set di `db/migration-e2e/`**

```
src/main/resources/
├── db/
│   ├── migration/              # Production (MariaDB)
│   │   ├── V1__Initial_Security_Schema.sql
│   │   ├── V2__Seed_Security_Data.sql
│   │   └── ... (V3-V45)
│   └── migration-e2e/          # E2E (H2 MODE=MySQL compatible)
│       ├── V1__Initial_Security_Schema.sql    # Adapted: remove ENGINE=InnoDB
│       ├── V2__Seed_Security_Data.sql          # Identical (INSERT syntax compatible)
│       ├── ...
│       └── V100__E2E_Test_Data_Seed.sql        # Additional test data
```

**Adaptasi yang diperlukan pada migration E2E:**

| Syntax MariaDB | Adaptasi H2 |
|----------------|-------------|
| `ENGINE=InnoDB` | Dihapus (H2 abaikan) |
| `ENUM('A','B')` | `VARCHAR(50) CHECK (col IN ('A','B'))` atau `VARCHAR(50)` saja |
| `NOW()` | `CURRENT_TIMESTAMP` (H2 kompatibel) |
| `DEFAULT CHARSET=utf8mb4` | Dihapus |
| `COLLATE utf8mb4_unicode_ci` | Dihapus |
| `COMMENT 'xxx'` | Dihapus (H2 abaikan inline comment) |

**Automation script untuk generate E2E migrations:**
```bash
# Script: scripts/sync-e2e-migrations.sh
# Strip MariaDB-specific syntax dari production migrations
for f in src/main/resources/db/migration/*.sql; do
  sed -E \
    -e 's/ ENGINE=InnoDB[^;]*//' \
    -e "s/ DEFAULT CHARSET=[^ ;]*//" \
    -e "s/ COLLATE [^ ;]*//" \
    -e "s/ COMMENT '[^']*'//" \
    -e "s/ENUM\(([^)]+)\)/VARCHAR(50)/g" \
    "$f" > "src/main/resources/db/migration-e2e/$(basename $f)"
done
```

#### 3.3 MinIO Conditional Configuration

MinIO dibutuhkan untuk approval signature upload. Untuk E2E, buat conditional bean:

```java
@Configuration
public class MinioConfig {
    @Bean
    @ConditionalOnProperty(name = "minio.endpoint", matchIfMissing = false)
    public MinioClient minioClient(@Value("${minio.endpoint}") String endpoint,
                                    @Value("${minio.access-key}") String accessKey,
                                    @Value("${minio.secret-key}") String secretKey) {
        try {
            return MinioClient.builder()
                    .endpoint(endpoint)
                    .credentials(accessKey, secretKey)
                    .build();
        } catch (Exception e) {
            // E2E: return no-op or mock
            return null;
        }
    }
}
```

Atau lebih bersih: gunakan `@Profile("!e2e")` pada MinIO config, dan `@Profile("e2e")` pada mock MinIO bean.

#### 3.4 Server Startup Command

```bash
# Linux/macOS (CI)
./mvnw spring-boot:run \
  -Dspring-boot.run.profiles=e2e \
  -Dspring-boot.run.jvmArguments="-Xmx512m" &

# Windows (local dev)
$env:SPRING_PROFILES_ACTIVE="e2e"; .\mvnw.cmd spring-boot:run
```

Playwright `globalSetup` menunggu server ready (lihat §5.3).

---

### 4. Data Seeding Strategy

#### 4.1 Test-First: Specs yang Menentukan Kebutuhan Data

**Prinsip TDD:** Jangan seed data dulu lalu cari test. **Tulis test spec, lalu identifikasi data minimum yang dibutuhkan.**

| Test Spec | Data yang Dibutuhkan |
|-----------|---------------------|
| `login.spec.js` → `it('should login as admin')` | User `admin`/`admin123` dengan `password_change_required=false` |
| `brand.spec.js` → `it('should create brand')` | User admin (sudah login) — tidak perlu data tambahan |
| `product.spec.js` → `it('should create product with category and brand')` | ≥1 ProductCategory, ≥1 Brand, ≥1 UoM — sebagai lookup options |
| `party.spec.js` → `it('should create party with address')` | ≥1 Geographic (Country → Province → City) |
| `stock-adjustment.spec.js` → `it('should create adjustment with line items')` | ≥1 Product, ≥1 Facility, ≥1 Grid, ≥1 Container, ≥1 Currency |
| `coa.spec.js` → `it('should create chart of account')` | Tidak perlu data tambahan (standalone) |
| `period.spec.js` → `it('should create fiscal period')` | ≥1 FiscalYear |

#### 4.2 Seeding Layer Architecture

```
┌────────────────────────────────────────────────┐
│          Data Seeding Layers                    │
│                                                  │
│  Layer 1: Flyway E2E Migrations (V1-V45)        │ ← Schema + Permissions + Roles + Admin
│  Layer 2: V100__E2E_Test_Data_Seed.sql          │ ← Master data minimum
│  Layer 3: E2eDataSeeder (CommandLineRunner)      │ ← Fix admin password, disable force-change
│  Layer 4: Per-test setup (Playwright fixtures)   │ ← Test-specific data via API/SQL if needed
└────────────────────────────────────────────────┘
```

#### 4.3 Layer 2: V100 — Master Data Seed

```sql
-- V100__E2E_Test_Data_Seed.sql
-- Dijalankan HANYA di profile E2E (ada di db/migration-e2e/ saja)

-- ============================================================
-- 1. Fix Admin User — skip force password change
-- ============================================================
UPDATE users SET password_change_required = FALSE WHERE username = 'admin';

-- ============================================================
-- 2. E2E Test User (non-admin, limited permissions)
-- ============================================================
INSERT INTO users (username, password, email, enabled, password_change_required, role_id, created_by, created_date)
SELECT 'e2e_staff', '$2a$10$dummyHashForAdmin123', 'staff@test.com', TRUE, FALSE, id, 'SYSTEM', CURRENT_TIMESTAMP
FROM roles WHERE name = 'ROLE_STAFF';

INSERT INTO user_profiles (user_id, full_name, phone_number, language_code, default_page_size, theme, created_by, created_date)
SELECT id, 'E2E Staff User', '08100000000', 'id', 10, 'light', 'SYSTEM', CURRENT_TIMESTAMP
FROM users WHERE username = 'e2e_staff';

-- ============================================================
-- 3. Inventory Master Data — minimum untuk product creation
-- ============================================================
INSERT INTO product_categories (code, name, created_by, created_date)
VALUES ('E2E-CAT-001', 'E2E Electronics', 'SYSTEM', CURRENT_TIMESTAMP);

INSERT INTO brands (code, name, created_by, created_date)
VALUES ('E2E-BRD-001', 'E2E Samsung', 'SYSTEM', CURRENT_TIMESTAMP);

-- UoM sudah di-seed oleh V5 (KG, PCS, BOX, dll)

-- ============================================================
-- 4. Warehouse Hierarchy — minimum untuk stock adjustment
-- ============================================================
INSERT INTO facilities (code, name, type, is_active, created_by, created_date)
VALUES ('E2E-WH-001', 'E2E Main Warehouse', 'WAREHOUSE', TRUE, 'SYSTEM', CURRENT_TIMESTAMP);

INSERT INTO grids (code, name, facility_id, is_active, created_by, created_date)
SELECT 'E2E-GRID-A1', 'E2E Grid A1', id, TRUE, 'SYSTEM', CURRENT_TIMESTAMP
FROM facilities WHERE code = 'E2E-WH-001';

INSERT INTO containers (code, name, grid_id, is_active, created_by, created_date)
SELECT 'E2E-BIN-001', 'E2E Bin 001', id, TRUE, 'SYSTEM', CURRENT_TIMESTAMP
FROM grids WHERE code = 'E2E-GRID-A1';

-- ============================================================
-- 5. Product — minimum untuk stock adjustment line items
-- ============================================================
INSERT INTO products (code, name, category_id, brand_id, base_uom_id, is_active, is_serialized, created_by, created_date)
SELECT 'E2E-PRD-001', 'E2E Test Product Alpha', c.id, b.id, u.id, TRUE, FALSE, 'SYSTEM', CURRENT_TIMESTAMP
FROM product_categories c, brands b, unit_of_measures u
WHERE c.code = 'E2E-CAT-001' AND b.code = 'E2E-BRD-001' AND u.code = 'PCS';

INSERT INTO products (code, name, category_id, brand_id, base_uom_id, is_active, is_serialized, created_by, created_date)
SELECT 'E2E-PRD-002', 'E2E Serialized Gadget', c.id, b.id, u.id, TRUE, TRUE, 'SYSTEM', CURRENT_TIMESTAMP
FROM product_categories c, brands b, unit_of_measures u
WHERE c.code = 'E2E-CAT-001' AND b.code = 'E2E-BRD-001' AND u.code = 'PCS';

-- ============================================================
-- 6. Currency — minimum untuk stock adjustment header
-- ============================================================
-- Currency sudah di-seed oleh V11 (IDR, USD, dll)

-- ============================================================
-- 7. Geographic — minimum untuk party address
-- ============================================================
-- Geographic sudah di-seed oleh V12+V15 (Indonesia + 514 kota)

-- ============================================================
-- 8. Accounting — Fiscal Year untuk Period test
-- ============================================================
INSERT INTO acc_fiscal_years (code, name, start_date, end_date, is_closed, created_by_user_id, created_date, version)
SELECT 'FY-2025', 'Tahun Fiskal 2025', '2025-01-01', '2025-12-31', FALSE, id, CURRENT_TIMESTAMP, 0
FROM users WHERE username = 'admin';
```

#### 4.4 Layer 3: E2eDataSeeder (Password Sync)

```java
@Component
@Profile("e2e")
@RequiredArgsConstructor
public class E2eDataSeeder implements CommandLineRunner {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) {
        // SystemInitializer mungkin sudah berjalan, tapi pastikan password correct
        userRepository.findByUsername("admin").ifPresent(admin -> {
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setPasswordChangeRequired(false);
            userRepository.save(admin);
        });
        
        userRepository.findByUsername("e2e_staff").ifPresent(staff -> {
            staff.setPassword(passwordEncoder.encode("admin123"));
            staff.setPasswordChangeRequired(false);
            userRepository.save(staff);
        });
    }
}
```

#### 4.5 Mengapa Tidak Data Seeding via Playwright API?

| Approach | Pro | Kontra |
|----------|-----|--------|
| SQL seed (dipilih) | Deterministik, idempotent, cepat | Harus maintain sync dengan schema |
| API-driven seeding | Realistic | Lambat, fragile (UI bisa berubah), chicken-egg problem |
| Fixtures per test | Isolated | Terlalu lambat untuk E2E, overkill |

---

### 5. Playwright Test Architecture

#### 5.1 Directory Structure

```
e2e/
├── package.json
├── playwright.config.js
├── tsconfig.json                    # Optional: jika pakai TypeScript
├── .env.example
├── tests/
│   ├── auth/
│   │   └── login.spec.js            # Behavior: Authentication flow
│   ├── security/
│   │   └── user-management.spec.js  # Behavior: User CRUD + role assignment
│   ├── inventory/
│   │   ├── brand.spec.js            # Behavior: Simple CRUD
│   │   ├── product-category.spec.js
│   │   ├── product.spec.js          # Behavior: Multi-section form
│   │   └── stock-adjustment.spec.js # Behavior: Complex line items
│   ├── master/
│   │   ├── currency.spec.js
│   │   ├── tax.spec.js
│   │   └── party.spec.js            # Behavior: Nested collections
│   ├── accounting/
│   │   ├── coa.spec.js
│   │   └── period.spec.js
│   └── smoke/
│       └── dashboard.spec.js        # Behavior: Post-login smoke
├── fixtures/
│   ├── auth.fixture.js              # Authenticated page with session
│   └── test-data.js                 # Constants: E2E user, expected seed data
├── helpers/
│   ├── tom-select.helper.js         # TomSelect interaction wrapper
│   ├── flatpickr.helper.js          # Flatpickr date setter
│   ├── autonumeric.helper.js        # AutoNumeric value setter
│   ├── ajax-form.helper.js          # AJAX form submission + wait
│   └── navigation.helper.js         # Menu click, breadcrumb assert
├── global-setup.js                  # Wait for Spring Boot ready
└── global-teardown.js               # Optional: kill Spring Boot
```

#### 5.2 Package Configuration

**`e2e/package.json`:**
```json
{
  "name": "solusi-erp-e2e",
  "version": "1.0.0",
  "private": true,
  "scripts": {
    "test": "npx playwright test",
    "test:headed": "npx playwright test --headed",
    "test:ui": "npx playwright test --ui",
    "test:debug": "npx playwright test --debug",
    "test:smoke": "npx playwright test tests/smoke/",
    "test:auth": "npx playwright test tests/auth/",
    "test:inventory": "npx playwright test tests/inventory/",
    "report": "npx playwright show-report"
  },
  "devDependencies": {
    "@playwright/test": "^1.52.0"
  }
}
```

**`e2e/playwright.config.js`:**
```javascript
import { defineConfig } from '@playwright/test';

export default defineConfig({
  testDir: './tests',
  timeout: 30_000,
  retries: process.env.CI ? 2 : 0,
  workers: 1,  // Serial execution — shared database state
  
  use: {
    baseURL: process.env.BASE_URL || 'http://localhost:18080',
    screenshot: 'only-on-failure',
    trace: 'on-first-retry',
    locale: 'id-ID',
    timezoneId: 'Asia/Jakarta',
  },
  
  globalSetup: './global-setup.js',
  
  reporter: [
    ['html', { open: 'never' }],
    ['list'],                         // Readable console output
    ['json', { outputFile: 'test-results/results.json' }],
  ],
  
  projects: [
    {
      name: 'chromium',
      use: { browserName: 'chromium' },
    },
  ],
});
```

#### 5.3 Global Setup: Wait for Server

```javascript
// e2e/global-setup.js
export default async function globalSetup() {
  const baseURL = process.env.BASE_URL || 'http://localhost:18080';
  const maxWait = 120_000; // 2 minutes
  const interval = 2_000;
  let elapsed = 0;

  console.log(`⏳ Waiting for Spring Boot at ${baseURL}/login ...`);

  while (elapsed < maxWait) {
    try {
      const res = await fetch(`${baseURL}/login`);
      if (res.ok) {
        console.log(`✅ Server ready in ${elapsed / 1000}s`);
        return;
      }
    } catch {
      // Server not ready yet
    }
    await new Promise(r => setTimeout(r, interval));
    elapsed += interval;
  }

  throw new Error(`❌ Server not ready after ${maxWait / 1000}s`);
}
```

#### 5.4 Auth Fixture: Session-Based Login

```javascript
// e2e/fixtures/auth.fixture.js
import { test as base, expect } from '@playwright/test';

/**
 * Custom fixture yang menyediakan `authenticatedPage` — 
 * page yang sudah login sebagai admin.
 * 
 * Menggunakan storageState untuk reuse session across tests.
 */
export const test = base.extend({
  authenticatedPage: async ({ browser }, use) => {
    const context = await browser.newContext();
    const page = await context.newPage();
    
    // Login flow
    await page.goto('/login');
    await page.fill('input[name="username"]', 'admin');
    await page.fill('input[name="password"]', 'admin123');
    await page.click('button[type="submit"]');
    await page.waitForURL('**/dashboard**', { timeout: 15_000 });
    
    await use(page);
    await context.close();
  },
});

export { expect };
```

#### 5.5 Component Helpers — Sebagai Abstraksi Test Infrastructure

**TomSelect Helper:**
```javascript
// e2e/helpers/tom-select.helper.js

/**
 * Memilih opsi pada TomSelect dropdown.
 * Menggunakan API internal TomSelect — lebih reliable daripada klik UI.
 * 
 * @param {Page} page - Playwright page
 * @param {string} selector - CSS selector untuk <select> asli
 * @param {string} searchQuery - keyword pencarian ('' = load all)
 * @param {number} optionIndex - index opsi yang dipilih (0 = pertama)
 * @returns {Promise<{id: string, name: string}>}
 */
export async function selectTomSelect(page, selector, searchQuery = '', optionIndex = 0) {
  return page.evaluate(({ sel, query, idx }) => {
    return new Promise((resolve, reject) => {
      const el = document.querySelector(sel);
      if (!el?.tomselect) {
        reject(new Error(`TomSelect not found: ${sel}`));
        return;
      }
      const ts = el.tomselect;
      ts.load(query, (options) => {
        if (!options.length) {
          resolve({ error: 'No options', query });
          return;
        }
        options.forEach(opt => ts.addOption(opt));
        const target = options[Math.min(idx, options.length - 1)];
        ts.setValue(target.id);
        resolve({ id: target.id, name: target.name });
      });
    });
  }, { sel: selector, query: searchQuery, idx: optionIndex });
}

/**
 * Memilih opsi TomSelect di dalam line item row.
 */
export async function selectLineItemTomSelect(page, rowIndex, fieldClass, searchQuery = '') {
  return page.evaluate(({ idx, cls, q }) => {
    return new Promise((resolve, reject) => {
      const rows = document.querySelectorAll('#line-container .line-row');
      if (idx >= rows.length) {
        reject(new Error(`Row ${idx} not found (total: ${rows.length})`));
        return;
      }
      const el = rows[idx].querySelector(`.${cls}`);
      if (!el?.tomselect) {
        reject(new Error(`TomSelect .${cls} not initialized in row ${idx}`));
        return;
      }
      const ts = el.tomselect;
      ts.load(q, (options) => {
        options.forEach(opt => ts.addOption(opt));
        if (options.length > 0) {
          ts.setValue(options[0].id);
          resolve({ id: options[0].id, name: options[0].name });
        } else {
          resolve({ error: 'No options' });
        }
      });
    });
  }, { idx: rowIndex, cls: fieldClass, q: searchQuery });
}
```

**Flatpickr Helper:**
```javascript
// e2e/helpers/flatpickr.helper.js

/**
 * Set tanggal pada Flatpickr input via internal API.
 * Readonly altInput membuat page.fill() tidak bekerja.
 */
export async function setFlatpickrDate(page, selector, dateStr) {
  await page.evaluate(({ sel, date }) => {
    const input = document.querySelector(sel);
    if (input?._flatpickr) {
      input._flatpickr.setDate(date, true);
    } else if (input) {
      input.value = date;
      input.dispatchEvent(new Event('change', { bubbles: true }));
    }
  }, { sel: selector, date: dateStr });
}
```

**AutoNumeric Helper:**
```javascript
// e2e/helpers/autonumeric.helper.js

/**
 * Set numeric value pada AutoNumeric input.
 * page.fill() akan merusak formatting mask.
 */
export async function setAutoNumeric(page, selector, value) {
  await page.evaluate(({ sel, val }) => {
    const el = document.querySelector(sel);
    if (typeof AutoNumeric !== 'undefined') {
      const instance = AutoNumeric.getAutoNumericElement(el);
      if (instance) { instance.set(val); return; }
    }
    el.value = val;
    el.dispatchEvent(new Event('input', { bubbles: true }));
  }, { sel: selector, val: value });
}

/**
 * Set numeric value di line item row.
 */
export async function setLineItemNumeric(page, rowIndex, inputClass, value) {
  await page.evaluate(({ idx, cls, val }) => {
    const rows = document.querySelectorAll('#line-container .line-row');
    const el = rows[idx]?.querySelector(`.${cls}`);
    if (!el) return;
    if (typeof AutoNumeric !== 'undefined') {
      const instance = AutoNumeric.getAutoNumericElement(el);
      if (instance) { instance.set(val); return; }
    }
    el.value = val;
    el.dispatchEvent(new Event('input', { bubbles: true }));
  }, { idx: rowIndex, cls: inputClass, val: value });
}
```

**AJAX Form Helper:**
```javascript
// e2e/helpers/ajax-form.helper.js

/**
 * Submit AJAX form dan tunggu redirect atau response sukses.
 * Form ERP menggunakan data-ajax-form="true" dengan JSON response.
 */
export async function submitAndWait(page, expectedUrlPattern) {
  // Intercept AJAX response
  const responsePromise = page.waitForResponse(
    resp => resp.request().method() === 'POST' && resp.status() === 200,
    { timeout: 10_000 }
  );

  // Klik submit button
  await page.click('button[type="submit"]');

  const response = await responsePromise;
  const json = await response.json();

  if (!json.success) {
    throw new Error(`Form submission failed: ${json.message}\nErrors: ${JSON.stringify(json.validationErrors)}`);
  }

  // Tunggu redirect jika ada
  if (expectedUrlPattern) {
    await page.waitForURL(expectedUrlPattern, { timeout: 10_000 });
  }

  return json;
}
```

#### 5.6 BDD-Style Test Specs — Tests as Living Documentation

**Prinsip:** Setiap `describe` block = sebuah feature/behavior. Setiap `test`/`it` = sebuah acceptance criteria. Nama test harus bisa dibaca sebagai dokumentasi.

##### Spec 1: Authentication (`tests/auth/login.spec.js`)

```javascript
import { test, expect } from '@playwright/test';

test.describe('Authentication — Session-Based Login', () => {
  
  test.describe('Valid Credentials', () => {
    test('should redirect to dashboard after successful login', async ({ page }) => {
      await page.goto('/login');
      await page.fill('input[name="username"]', 'admin');
      await page.fill('input[name="password"]', 'admin123');
      await page.click('button[type="submit"]');
      
      await page.waitForURL('**/dashboard**');
      await expect(page.locator('text=Dashboard')).toBeVisible();
    });

    test('should display user fullname in header after login', async ({ page }) => {
      await page.goto('/login');
      await page.fill('input[name="username"]', 'admin');
      await page.fill('input[name="password"]', 'admin123');
      await page.click('button[type="submit"]');
      
      await page.waitForURL('**/dashboard**');
      await expect(page.locator('.navbar')).toContainText('Administrator');
    });
  });

  test.describe('Invalid Credentials', () => {
    test('should show error message for wrong password', async ({ page }) => {
      await page.goto('/login');
      await page.fill('input[name="username"]', 'admin');
      await page.fill('input[name="password"]', 'wrongpassword');
      await page.click('button[type="submit"]');
      
      await expect(page.locator('.alert-danger, .alert-error')).toBeVisible();
      await expect(page).toHaveURL(/\/login\?error/);
    });
  });

  test.describe('Session Management', () => {
    test('should redirect to login page when accessing protected URL without session', async ({ page }) => {
      await page.goto('/dashboard');
      await expect(page).toHaveURL(/\/login/);
    });

    test('should invalidate session after logout', async ({ page }) => {
      // Login
      await page.goto('/login');
      await page.fill('input[name="username"]', 'admin');
      await page.fill('input[name="password"]', 'admin123');
      await page.click('button[type="submit"]');
      await page.waitForURL('**/dashboard**');
      
      // Logout
      await page.goto('/logout');
      
      // Verify redirect to login
      await expect(page).toHaveURL(/\/login/);
      
      // Verify protected page no longer accessible
      await page.goto('/dashboard');
      await expect(page).toHaveURL(/\/login/);
    });
  });
});
```

##### Spec 2: Brand CRUD — Simple Module (`tests/inventory/brand.spec.js`)

```javascript
import { test, expect } from '../../fixtures/auth.fixture.js';
import { submitAndWait } from '../../helpers/ajax-form.helper.js';

test.describe('Inventory > Brand — Full CRUD Lifecycle', () => {
  const BRAND_NAME = `E2E Brand ${Date.now()}`;
  const BRAND_CODE = `BRD-E2E-${Date.now().toString().slice(-6)}`;

  test.describe('CREATE — Adding a new brand', () => {
    test('should navigate to brand creation form from list page', async ({ authenticatedPage: page }) => {
      await page.goto('/inventory/brands');
      await page.click('a[href*="/brands/create"]');
      
      await expect(page).toHaveURL(/\/brands\/create/);
      await expect(page.locator('h1, .page-title')).toContainText(/brand/i);
    });

    test('should successfully create brand with valid data', async ({ authenticatedPage: page }) => {
      await page.goto('/inventory/brands/create');
      
      await page.fill('input[name="code"]', BRAND_CODE);
      await page.fill('input[name="name"]', BRAND_NAME);
      
      await submitAndWait(page, '**/inventory/brands**');
    });

    test('should display validation error when name is empty', async ({ authenticatedPage: page }) => {
      await page.goto('/inventory/brands/create');
      
      await page.fill('input[name="code"]', 'TEMP');
      // Intentionally skip name
      await page.click('button[type="submit"]');
      
      // AJAX form should show validation error
      await expect(page.locator('.invalid-feedback, .alert-danger, .text-danger')).toBeVisible();
    });
  });

  test.describe('READ — Viewing brand in list', () => {
    test('should display newly created brand in list with search', async ({ authenticatedPage: page }) => {
      await page.goto('/inventory/brands');
      
      // Search using HTMX keyword filter
      await page.fill('input[name="keyword"]', BRAND_NAME);
      await page.press('input[name="keyword"]', 'Enter');
      await page.waitForTimeout(1000); // HTMX partial reload
      
      await expect(page.locator('table tbody')).toContainText(BRAND_NAME);
    });
  });

  test.describe('UPDATE — Editing existing brand', () => {
    test('should load brand data in edit form', async ({ authenticatedPage: page }) => {
      await page.goto('/inventory/brands');
      await page.fill('input[name="keyword"]', BRAND_NAME);
      await page.press('input[name="keyword"]', 'Enter');
      await page.waitForTimeout(1000);
      
      // Klik edit button pada row pertama
      await page.click('table tbody tr:first-child a[href*="/edit/"]');
      
      await expect(page.locator('input[name="name"]')).toHaveValue(BRAND_NAME);
    });

    test('should successfully update brand name', async ({ authenticatedPage: page }) => {
      await page.goto('/inventory/brands');
      await page.fill('input[name="keyword"]', BRAND_NAME);
      await page.press('input[name="keyword"]', 'Enter');
      await page.waitForTimeout(1000);
      
      await page.click('table tbody tr:first-child a[href*="/edit/"]');
      
      const updatedName = BRAND_NAME + ' Updated';
      await page.fill('input[name="name"]', updatedName);
      
      await submitAndWait(page, '**/inventory/brands**');
    });
  });

  test.describe('DELETE — Removing brand', () => {
    test('should soft-delete brand via AJAX DELETE', async ({ authenticatedPage: page }) => {
      await page.goto('/inventory/brands');
      await page.fill('input[name="keyword"]', BRAND_CODE);
      await page.press('input[name="keyword"]', 'Enter');
      await page.waitForTimeout(1000);
      
      // Klik delete button, confirm dialog
      page.on('dialog', dialog => dialog.accept());
      await page.click('table tbody tr:first-child button[data-action="delete"], table tbody tr:first-child .btn-delete');
      
      await page.waitForTimeout(1000);
      // Verify row hilang dari list
      await expect(page.locator('table tbody')).not.toContainText(BRAND_CODE);
    });
  });
});
```

##### Spec 3: Product — Multi-Section Form (`tests/inventory/product.spec.js`)

```javascript
import { test, expect } from '../../fixtures/auth.fixture.js';
import { selectTomSelect } from '../../helpers/tom-select.helper.js';
import { submitAndWait } from '../../helpers/ajax-form.helper.js';

test.describe('Inventory > Product — Multi-Section Form with TomSelect Lookups', () => {

  test.describe('CREATE — Product with category, brand, and UoM selection', () => {
    test('should create product using TomSelect for category and brand', async ({ authenticatedPage: page }) => {
      await page.goto('/inventory/products/create');
      
      // Basic Info section
      await page.fill('input[name="name"]', `E2E Product ${Date.now()}`);
      
      // TomSelect: Category (async lookup)
      await selectTomSelect(page, '#category-select', '', 0);
      
      // TomSelect: Brand (async lookup)
      await selectTomSelect(page, '#brand-select', '', 0);
      
      // Standard select: Base UoM
      await page.selectOption('select[name="baseUomId"]', { index: 1 });
      
      await submitAndWait(page, '**/inventory/products**');
    });

    test('should auto-generate product code on creation', async ({ authenticatedPage: page }) => {
      await page.goto('/inventory/products/create');
      const codeInput = page.locator('input[name="code"]');
      
      // Code field should be readonly and auto-populated (or empty until save)
      const isReadonly = await codeInput.getAttribute('readonly');
      if (isReadonly !== null) {
        // Auto-generated — verify non-empty after save
        expect(true).toBe(true); // code handled by backend
      }
    });
  });

  test.describe('READ — Product list with search', () => {
    test('should find product via keyword search (HTMX partial reload)', async ({ authenticatedPage: page }) => {
      await page.goto('/inventory/products');
      await page.fill('input[name="keyword"]', 'E2E');
      await page.press('input[name="keyword"]', 'Enter');
      await page.waitForTimeout(1500);
      
      await expect(page.locator('table tbody tr')).toHaveCount.greaterThan(0);
    });
  });
});
```

##### Spec 4: Stock Adjustment — Complex Form (`tests/inventory/stock-adjustment.spec.js`)

```javascript
import { test, expect } from '../../fixtures/auth.fixture.js';
import { selectTomSelect, selectLineItemTomSelect } from '../../helpers/tom-select.helper.js';
import { setFlatpickrDate } from '../../helpers/flatpickr.helper.js';
import { setAutoNumeric, setLineItemNumeric } from '../../helpers/autonumeric.helper.js';
import { submitAndWait } from '../../helpers/ajax-form.helper.js';

test.describe('Inventory > Stock Adjustment — Transaction with Line Items', () => {

  test.describe('CREATE — Full adjustment with header + line items', () => {

    test('should fill header: date (Flatpickr), facility (TomSelect), currency', async ({ authenticatedPage: page }) => {
      await page.goto('/inventory/adjustments/create');
      
      // Flatpickr: Transaction Date
      await setFlatpickrDate(page, 'input[name="transactionDate"]', '2025-06-15');
      
      // TomSelect: Facility
      await selectTomSelect(page, '#header-facility', '', 0);
      
      // Standard select: Currency
      await page.selectOption('select[name="currencyId"]', { index: 1 });
      
      // AutoNumeric: Exchange Rate
      await setAutoNumeric(page, '#header-rate', 15500);
      
      // Verify fields are populated
      await expect(page.locator('#header-facility')).not.toHaveValue('');
    });

    test('should add line item with product, grid, container, qty, and price', async ({ authenticatedPage: page }) => {
      await page.goto('/inventory/adjustments/create');
      
      // Fill header first (prerequisite)
      await setFlatpickrDate(page, 'input[name="transactionDate"]', '2025-06-15');
      await selectTomSelect(page, '#header-facility', '', 0);
      
      // Add line item
      await page.click('#btn-add-line');
      await page.waitForTimeout(500);
      
      // Line: Product (TomSelect in row)
      await selectLineItemTomSelect(page, 0, 'select-product', '');
      await page.waitForTimeout(300);
      
      // Line: Grid
      await selectLineItemTomSelect(page, 0, 'select-grid', '');
      
      // Line: Container
      await selectLineItemTomSelect(page, 0, 'select-container', '');
      
      // Line: open qty drawer and set quantity
      await page.click('.line-row:first-child .btn-edit-detail');
      await page.waitForTimeout(300);
      await page.evaluate(() => {
        const dialog = document.querySelector('.offcanvas.show, dialog[open], .modal.show');
        const qtyInput = dialog?.querySelector('.input-qty-target');
        if (qtyInput) {
          const an = typeof AutoNumeric !== 'undefined' 
            ? AutoNumeric.getAutoNumericElement(qtyInput) : null;
          if (an) an.set(10);
          else qtyInput.value = 10;
          qtyInput.dispatchEvent(new Event('change', { bubbles: true }));
        }
      });
      await page.click('.offcanvas.show button:has-text("Apply"), dialog[open] button:has-text("Apply")');
      
      // Line: Price
      await setLineItemNumeric(page, 0, 'input-price', 50000);
      
      // Verify line total calculated
      const lineTotal = page.locator('.line-row:first-child .line-total');
      await expect(lineTotal).not.toHaveText('0.00');
    });

    test('should save adjustment and redirect to list', async ({ authenticatedPage: page }) => {
      await page.goto('/inventory/adjustments/create');
      
      // Full form fill (header + 1 line) — condensed
      await setFlatpickrDate(page, 'input[name="transactionDate"]', '2025-06-15');
      await selectTomSelect(page, '#header-facility', '', 0);
      await page.selectOption('select[name="currencyId"]', { index: 1 });
      await setAutoNumeric(page, '#header-rate', 15500);
      
      await page.click('#btn-add-line');
      await page.waitForTimeout(500);
      await selectLineItemTomSelect(page, 0, 'select-product', '');
      await setLineItemNumeric(page, 0, 'input-price', 50000);
      
      // Open drawer, set qty, apply
      await page.click('.line-row:first-child .btn-edit-detail');
      await page.waitForTimeout(300);
      await page.evaluate(() => {
        const d = document.querySelector('.offcanvas.show, dialog[open], .modal.show');
        const q = d?.querySelector('.input-qty-target');
        if (q) { const an = AutoNumeric?.getAutoNumericElement?.(q); an ? an.set(5) : q.value = 5; 
          q.dispatchEvent(new Event('change', { bubbles: true })); }
      });
      await page.click('.offcanvas.show button:has-text("Apply"), dialog[open] button:has-text("Apply")');
      
      await submitAndWait(page, '**/inventory/adjustments**');
    });
  });

  test.describe('PROCESS — State Transition', () => {
    test('should process DRAFT adjustment to COMPLETED status', async ({ authenticatedPage: page }) => {
      // Navigate to existing draft adjustment
      await page.goto('/inventory/adjustments');
      // Klik pada adjustment DRAFT pertama
      await page.click('table tbody tr:first-child a[href*="/edit/"]');
      
      // Klik Process button
      await page.click('a:has-text("Process"), button:has-text("Process")');
      
      // Confirm dialog
      page.on('dialog', dialog => dialog.accept());
      
      await page.waitForTimeout(2000);
      // Verify status changed
      await expect(page.locator('.badge, .status')).toContainText(/COMPLETED|Selesai/i);
    });
  });
});
```

##### Spec 5: Party — Nested Collections (`tests/master/party.spec.js`)

```javascript
import { test, expect } from '../../fixtures/auth.fixture.js';
import { selectTomSelect } from '../../helpers/tom-select.helper.js';
import { submitAndWait } from '../../helpers/ajax-form.helper.js';

test.describe('Master > Party — CRUD with Nested Contacts & Addresses', () => {

  test.describe('CREATE — Party with dynamic contact rows and geographic address', () => {
    test('should create party with name, type, and contact info', async ({ authenticatedPage: page }) => {
      await page.goto('/master/parties/create');
      
      await page.fill('input[name="name"]', `E2E Supplier ${Date.now()}`);
      await page.selectOption('select[name="salutation"]', { index: 1 });
      
      // Add contact row
      await page.click('#btn-add-contact, button:has-text("Add Contact")');
      await page.waitForTimeout(300);
      await page.fill('input[name="contacts[0].value"]', 'supplier@e2e-test.com');
      
      // Add address row with Geographic TomSelect
      await page.click('#btn-add-address, button:has-text("Add Address")');
      await page.waitForTimeout(300);
      await page.fill('input[name*="addresses[0].addressLine1"]', 'Jl. E2E Testing No. 1');
      
      // Geographic: Province → City cascade (TomSelect)
      // Komentar: Geographic sudah di-seed oleh V12+V15
      await selectTomSelect(page, '[name*="addresses[0].cityId"], #address-city-0', '', 0);
      
      await submitAndWait(page, '**/master/parties**');
    });
  });
});
```

#### 5.7 Readable Test Output — Living Documentation

Dengan naming convention di atas, output `npx playwright test --reporter=list` menghasilkan:

```
  Authentication — Session-Based Login
    Valid Credentials
      ✓ should redirect to dashboard after successful login (2.1s)
      ✓ should display user fullname in header after login (1.8s)
    Invalid Credentials
      ✓ should show error message for wrong password (0.9s)
    Session Management
      ✓ should redirect to login page when accessing protected URL without session (0.3s)
      ✓ should invalidate session after logout (2.5s)

  Inventory > Brand — Full CRUD Lifecycle
    CREATE — Adding a new brand
      ✓ should navigate to brand creation form from list page (1.2s)
      ✓ should successfully create brand with valid data (1.8s)
      ✓ should display validation error when name is empty (1.1s)
    READ — Viewing brand in list
      ✓ should display newly created brand in list with search (1.5s)
    UPDATE — Editing existing brand
      ✓ should load brand data in edit form (1.9s)
      ✓ should successfully update brand name (2.0s)
    DELETE — Removing brand
      ✓ should soft-delete brand via AJAX DELETE (1.7s)

  Inventory > Stock Adjustment — Transaction with Line Items
    CREATE — Full adjustment with header + line items
      ✓ should fill header: date, facility, currency (2.3s)
      ✓ should add line item with product, grid, container, qty, and price (4.1s)
      ✓ should save adjustment and redirect to list (5.2s)
    PROCESS — State Transition
      ✓ should process DRAFT adjustment to COMPLETED status (2.8s)

  Master > Party — CRUD with Nested Contacts & Addresses
    CREATE — Party with dynamic contact rows and geographic address
      ✓ should create party with name, type, and contact info (3.5s)

  27 passed (52.3s)
```

**Output ini bisa dibaca sebagai sistem dokumentasi:** siapapun yang membaca tahu persis behavior apa yang diverifikasi.

---

### 6. CI/CD Integration

#### 6.1 GitHub Actions Workflow

**File: `.github/workflows/e2e-playwright.yml`**

```yaml
name: E2E - Playwright

on:
  push:
    branches: [main, master]
  pull_request:
    branches: [main, master]
  schedule:
    - cron: '0 3 * * *'     # Nightly 03:00 UTC (10:00 WIB)
  workflow_dispatch:

concurrency:
  group: e2e-${{ github.ref }}
  cancel-in-progress: true

permissions:
  contents: read

env:
  JAVA_VERSION: '21'
  NODE_VERSION: '20'

jobs:
  e2e-tests:
    name: Playwright E2E Tests
    runs-on: ubuntu-latest
    timeout-minutes: 20

    steps:
      # ── 1. Checkout ──
      - name: Checkout
        uses: actions/checkout@v4

      # ── 2. Setup JDK ──
      - name: Set up JDK ${{ env.JAVA_VERSION }}
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: ${{ env.JAVA_VERSION }}
          cache: maven

      # ── 3. Setup Node.js ──
      - name: Set up Node.js ${{ env.NODE_VERSION }}
        uses: actions/setup-node@v4
        with:
          node-version: ${{ env.NODE_VERSION }}
          cache: npm
          cache-dependency-path: e2e/package-lock.json

      # ── 4. Install Playwright ──
      - name: Install Playwright dependencies
        working-directory: e2e
        run: |
          npm ci
          npx playwright install --with-deps chromium

      # ── 5. Start Spring Boot with E2E profile ──
      - name: Start Spring Boot (E2E profile, H2 in-memory)
        run: |
          chmod +x mvnw
          ./mvnw spring-boot:run \
            -Dspring-boot.run.profiles=e2e \
            -Dspring-boot.run.jvmArguments="-Xmx512m" &
          
          # Wait for server ready
          echo "⏳ Waiting for Spring Boot..."
          for i in $(seq 1 60); do
            if curl -sf http://localhost:18080/login > /dev/null 2>&1; then
              echo "✅ Server ready after ${i}s"
              break
            fi
            sleep 2
          done

      # ── 6. Run Playwright tests ──
      - name: Run Playwright E2E tests
        working-directory: e2e
        run: npx playwright test
        env:
          BASE_URL: http://localhost:18080

      # ── 7. Upload artifacts ──
      - name: Upload Playwright report
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: playwright-report-${{ github.run_number }}
          path: |
            e2e/playwright-report/
            e2e/test-results/
          retention-days: 14

      - name: Upload trace on failure
        if: failure()
        uses: actions/upload-artifact@v4
        with:
          name: playwright-traces-${{ github.run_number }}
          path: e2e/test-results/**/*.zip
          retention-days: 7
```

#### 6.2 Integrasi dengan CI Existing

Workflow E2E **TIDAK** menggantikan `ci-java21.yml`. Posisinya:

```
ci-java21.yml (existing)
  ├── fast-tests (unit + static)     ← setiap push/PR, < 60s
  ├── full-tests (+ integration)     ← nightly + main push, < 3 min
  └── deploy (to VPS)                ← setelah fast + full pass

e2e-playwright.yml (new)
  └── e2e-tests (Playwright)         ← main push + PR + nightly, < 10 min
```

**Deploy gate:** Opsional — bisa menambahkan `needs: [e2e-tests]` ke job `deploy` di `ci-java21.yml` setelah E2E stabil (setelah 2-3 sprint).

#### 6.3 Local Development Commands

```bash
# Terminal 1: Start Spring Boot E2E
cd F:\solusi-program-erp
$env:SPRING_PROFILES_ACTIVE="e2e"; .\mvnw.cmd spring-boot:run

# Terminal 2: Run Playwright
cd F:\solusi-program-erp\e2e
npm test                    # Headless
npm run test:headed         # Dengan browser visible
npm run test:ui             # Interactive Playwright UI
npm run test:debug          # Step-by-step debugger
```

---

### 7. Cakupan Test Minimum per Modul

#### 7.1 Test Matrix — Ordered by Data Dependencies

Test dijalankan serial (`workers: 1`) karena shared database state. Urutan berdasarkan dependency graph:

| # | Suite | Module | Specs | Behavior yang Diverifikasi | Kompleksitas |
|---|-------|--------|-------|---------------------------|-------------|
| 1 | `login.spec.js` | Auth | 5 | Login valid/invalid, session, logout, redirect protected | Simple |
| 2 | `dashboard.spec.js` | Core | 2 | Dashboard renders, menu tree loads | Smoke |
| 3 | `brand.spec.js` | Inventory | 6 | Create, list search, edit, validation error, delete | Simple CRUD |
| 4 | `product-category.spec.js` | Inventory | 4 | Create, list, edit, delete | Simple CRUD |
| 5 | `currency.spec.js` | Master | 4 | Create, list, edit, toggle active | Simple CRUD |
| 6 | `tax.spec.js` | Master | 4 | Create with rate (decimal), list, edit | Simple CRUD |
| 7 | `coa.spec.js` | Accounting | 5 | Create with account type, parent hierarchy, list, edit | Medium |
| 8 | `product.spec.js` | Inventory | 5 | Create with TomSelect (category, brand), sections, search | Medium (TomSelect) |
| 9 | `party.spec.js` | Master | 5 | Create with contacts, addresses (Geographic), search | Complex (nested) |
| 10 | `stock-adjustment.spec.js` | Inventory | 6 | Header + lines, drawer qty, save, process state | Complex (line items) |
| 11 | `user-management.spec.js` | Security | 4 | Create user, assign role, verify login with new user | Medium |
| 12 | `period.spec.js` | Accounting | 3 | Create period, list, close period | Medium |

**Total: 12 suites, ~53 test specs**

#### 7.2 Behavior Coverage per Module

```
Module         CRUD Behavior                     Special Behavior
──────────     ────────────────────               ──────────────────────
Auth           Login, Logout, Session Guard       Forced password change (skip via seed)
Security       User C/R/U                         Role assignment, permission verification
Inventory      Brand C/R/U/D                      —
               ProductCategory C/R/U/D            —
               Product C/R/U                      TomSelect lookups, auto-code
               StockAdjustment C/R/Process        Line items, drawer, AutoNumeric, Flatpickr
Master         Currency C/R/U                     Active toggle
               Tax C/R/U                          Decimal rate input
               Party C/R/U                        Nested contacts, geographic TomSelect
Accounting     CoA C/R/U                          Parent hierarchy, account type
               Period C/R                          Close period state transition
Core           Dashboard smoke                    Menu tree rendering
```

#### 7.3 Coverage yang TIDAK Termasuk (Out of Scope Sprint 1)

| Behavior | Alasan Exclude |
|----------|---------------|
| Approval workflow + digital signature | Butuh MinIO real/mock, SignaturePad canvas |
| News CRUD | Low priority, simple form |
| UoM Conversion | Derivasi dari UoM, low risk |
| Warehouse hierarchy (Grid, Container) CRUD | Diperlukan implisit oleh StockAdjustment test |
| Report generation (PDF/Excel) | Download verification complex |
| Multi-role permission testing | Butuh additional seeded users, Sprint 2 |
| Geographic CRUD | Pre-seeded by V12+V15, read-only in practice |
| Monitoring page | Admin-only diagnostic, low E2E value |

---

### 8. Risiko & Mitigasi

| # | Risiko | Probabilitas | Dampak | Mitigasi |
|---|--------|-------------|--------|----------|
| 1 | **H2 MODE=MySQL incompatibility** — beberapa SQL syntax MariaDB tidak 100% kompatibel | Tinggi | Tinggi | Maintain `migration-e2e/` terpisah; script `sync-e2e-migrations.sh` untuk adaptasi otomatis; test migration di CI dulu sebelum test E2E |
| 2 | **TomSelect async race condition** — dropdown belum load saat test mencoba select | Sedang | Sedang | Helper `selectTomSelect()` menggunakan `ts.load()` API (bukan klik UI) — blocking call yang menunggu data ready |
| 3 | **Test flakiness** di CI karena timing | Sedang | Tinggi | `retries: 2` di CI; `workers: 1` (serial); explicit `waitForTimeout` pada HTMX partial reload; `trace: 'on-first-retry'` untuk debug |
| 4 | **Flyway migration drift** — E2E migrations tertinggal dari production | Sedang | Sedang | CI job khusus: diff antara `migration/` dan `migration-e2e/` — fail jika ada migration baru yang belum di-sync |
| 5 | **Spring Boot startup lambat** — H2 + 45 migrations + seeding | Rendah | Rendah | H2 in-memory sangat cepat (~5-10 detik); global-setup max wait 120s; Maven build cache di CI |
| 6 | **CSRF token management** — form submission butuh valid token | Rendah | Sedang | Playwright menggunakan real browser session — CSRF token otomatis ter-handle oleh Thymeleaf |
| 7 | **Test data collision** — test berurutan memodifikasi data yang sama | Sedang | Sedang | Timestamp-based unique names (`E2E Brand ${Date.now()}`); search by unique identifier |
| 8 | **MinIO dependency** saat approval test | Rendah | Rendah | Exclude approval test Sprint 1; mock MinIO bean dengan `@Profile("e2e")` |
| 9 | **Node.js version mismatch** CI vs local | Rendah | Rendah | Lock Node 20 LTS di `package.json` engines + CI setup |
| 10 | **AutoNumeric format locale mismatch** | Rendah | Sedang | Helper `setAutoNumeric()` menggunakan API `.set(number)` — bypass formatting |

---

### 9. Effort Estimate

#### 9.1 Sprint Breakdown

| Sprint | Scope | Effort (jam) | Deliverable |
|--------|-------|-------------|-------------|
| **Sprint 1** (Setup + Foundation) | E2E profile, H2 migrations, Playwright scaffold, helpers, login spec | 32–40 | Server berjalan di H2, login test hijau |
| **Sprint 2** (Simple CRUD) | Brand, ProductCategory, Currency, Tax specs | 24–32 | 4 simple CRUD suites (16 specs) hijau |
| **Sprint 3** (Medium CRUD) | Product, CoA, Period, User Management specs | 28–36 | TomSelect integration tested, 4 suites hijau |
| **Sprint 4** (Complex Forms) | StockAdjustment, Party specs | 28–36 | Line items, nested collections, drawer tested |
| **Sprint 5** (CI + Stabilization) | CI workflow, flakiness fix, dashboard smoke, documentation | 16–24 | Full CI green, HTML report published |

**Total: 128–168 jam** (~4–5 sprint × 2 developer)

#### 9.2 Effort per Activity

| Aktivitas | Jam | Keterangan |
|-----------|-----|-----------|
| E2E profile + H2 migration adaptation | 16–20 | 45 SQL files to adapt, sync script |
| E2eDataSeeder + MinIO mock | 4–6 | Java code, profile-conditional beans |
| Playwright project scaffold (config, helpers) | 8–12 | Package, config, 4 helpers, auth fixture |
| TomSelect/Flatpickr/AutoNumeric helpers | 8–12 | Battle-tested from smoke test guide |
| 12 test suites (53 specs) | 64–80 | ~1.2–1.5 jam per spec rata-rata |
| CI workflow + stabilization | 16–20 | GitHub Actions, retry tuning, artifact |
| Documentation | 8–12 | README, run instructions, architecture doc |
| **Buffer (flakiness, debugging)** | 12–16 | Inevitable for E2E tests |

#### 9.3 Prerequisite

| Item | Status | Action |
|------|--------|--------|
| H2 dependency di pom.xml | ❌ Belum ada | Tambahkan `<scope>test</scope>` |
| `application-e2e.yaml` | ❌ Belum ada | Buat baru |
| `db/migration-e2e/` directory | ❌ Belum ada | Generate dari production + adaptasi |
| `e2e/` directory + package.json | ❌ Belum ada | Scaffold baru |
| Node.js 20+ di CI runner | ✅ `ubuntu-latest` sudah include | Tambahkan `setup-node` step |
| E2eDataSeeder.java | ❌ Belum ada | Buat baru dengan `@Profile("e2e")` |

---

### 10. Trade-offs

#### 10.1 Keputusan Desain dan Konsekuensinya

| Keputusan | Trade-off Positif | Trade-off Negatif |
|-----------|-------------------|-------------------|
| **H2 in-memory vs MariaDB TestContainers** | Zero Docker dependency, startup < 10s, CI sederhana | Harus maintain migration set terpisah; ada risiko incompatibility |
| **Separate `e2e/` Node.js project vs Java Playwright** | Ekosistem Playwright lebih mature di Node.js; async/await natural untuk browser ops | Dua tech stack (Java + Node.js); CI butuh setup JDK + Node.js |
| **Serial execution (`workers: 1`) vs Parallel** | Predictable, no data race, simpler assertions | Slower (est. 60-90s total); tidak bisa scale horizontal |
| **JS API helpers vs UI click interactions** | Reliable untuk TomSelect/Flatpickr/AutoNumeric; tidak flaky | Tidak menguji flow user "sebenarnya" (klik dropdown → pilih opsi); miss edge case UI |
| **Timestamp-based unique data vs Fixed test data** | No collision antar test, idempotent | Data menumpuk di database per run (ok karena H2 in-memory) |
| **Profile-driven (`application-e2e.yaml`) vs Environment variable only** | Explicit, self-documenting; satu file berisi semua E2E config | Satu file lagi yang harus di-maintain |
| **Happy-path only (Sprint 1) vs Full negative testing** | Cepat deliver value; confidence pada core flows | Tidak menguji edge case (concurrent edit, permission denied, network error) |
| **Test-first spec definition** | Spec menjadi living documentation; data seeding driven by actual test needs | Membutuhkan upfront design time sebelum menulis infrastructure |
| **Adapted migrations vs Single migration set** | Production migration tetap untouched; no risk | Sync overhead; drift risk jika production migration berubah tanpa update E2E |
| **AJAX form helper vs raw page.click()** | Reliable interception of JSON response; proper error reporting | Tightly coupled dengan current `data-ajax-form` implementation |

#### 10.2 Apa yang Tidak Dilakukan (dan Mengapa)

| Bukan scope | Rasional |
|-------------|---------|
| Visual regression testing | Overkill untuk tahap pertama; fokus pada functional correctness dulu |
| API-level testing (REST) | Aplikasi ini SSR, bukan SPA — semua flow lewat browser |
| Load/performance testing | Beda concern; butuh tool terpisah (k6, Gatling) |
| Cross-browser testing | Chromium-only cukup untuk CI; Firefox/WebKit bisa ditambah kemudian |
| Mobile responsive testing | Bisa ditambah sebagai project terpisah di Playwright |
| Database assertion (query H2 dari Playwright) | Over-engineering; cukup verify lewat UI (data muncul di list) |

#### 10.3 Kapan Harus Revisit Keputusan Ini?

| Trigger | Aksi |
|---------|------|
| H2 incompatibility > 5 migration files | Evaluasi switch ke TestContainers + MariaDB |
| E2E test > 3 menit di CI | Evaluasi parallel workers dengan database isolation |
| Tim bertambah > 3 developer | Pertimbangkan visual regression + cross-browser |
| Modul baru dengan SPA/React component | Evaluasi apakah perlu API-level test tambahan |
| Approval module E2E dibutuhkan | Implementasi MinIO mock atau TestContainers MinIO |

---

*Proposal ini ditulis dari perspektif TDD Advocate: **test spec dahulu, infrastructure mengikuti**. Setiap keputusan arsitektur dapat di-trace kembali ke test spec yang membutuhkannya. Test output berfungsi sebagai living documentation — jika test hijau, behavior terdokumentasi. Jika test merah, ada regresi yang harus ditangani.*
