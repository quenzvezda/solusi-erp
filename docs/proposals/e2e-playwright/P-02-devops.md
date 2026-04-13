# Proposal Teknis: Playwright E2E Test untuk Solusi ERP

> **Perspektif:** DevOps Engineer — CI speed, reproducibility, zero-flakiness
> **Versi:** 1.0 | **Tanggal:** Juli 2025

---

### 1. Executive Summary

Proyek ini memiliki 263 test files yang mencakup unit test dan template integration test, namun tidak satupun memverifikasi alur CRUD end-to-end di browser nyata. Bug pada interaksi TomSelect, Flatpickr, AutoNumeric, dan AJAX form submission hanya terdeteksi saat runtime manual.

Proposal ini merancang **Playwright E2E test suite** yang berjalan di atas Spring Boot dengan **H2 in-memory database (MODE=MySQL)**, menghilangkan dependensi MariaDB di CI. Arsitekturnya: Spring Boot start dengan profil `e2e` → Flyway migrasi ke H2 → SystemInitializer seed admin → Playwright test suite di subdirektori `e2e/` memvalidasi full CRUD flow.

Target: **< 5 menit total CI time** untuk E2E job, **zero external dependencies** (no Docker, no MariaDB), **deterministic** — setiap run dimulai dari database kosong yang identik.

---

### 2. Arsitektur Solusi

```
┌─────────────────────────────────────────────────────────────────────┐
│                        CI Runner (ubuntu-latest)                     │
│                                                                      │
│  ┌──────────────────────┐         ┌──────────────────────────────┐  │
│  │   Spring Boot App    │         │     Playwright Test Suite    │  │
│  │   (profil: e2e)      │  HTTP   │     (e2e/ subdirectory)      │  │
│  │                      │◄───────►│                              │  │
│  │  ┌────────────────┐  │ :18080  │  @playwright/test            │  │
│  │  │   H2 Database  │  │         │  chromium (headless)         │  │
│  │  │  MODE=MySQL    │  │         │                              │  │
│  │  │  in-memory     │  │         │  ┌────────────────────────┐  │  │
│  │  └────────────────┘  │         │  │  Helpers:              │  │  │
│  │                      │         │  │  - TomSelect API       │  │  │
│  │  Flyway → H2         │         │  │  - Flatpickr API       │  │  │
│  │  SystemInitializer   │         │  │  - AutoNumeric API     │  │  │
│  │  (admin/admin123)    │         │  │  - AJAX form submit    │  │  │
│  │                      │         │  └────────────────────────┘  │  │
│  └──────────────────────┘         └──────────────────────────────┘  │
│                                                                      │
│  Lifecycle: mvnw spring-boot:run ──► npx playwright test ──► kill   │
└─────────────────────────────────────────────────────────────────────┘
```

**Komponen utama:**

| Komponen | Teknologi | Lokasi |
|----------|-----------|--------|
| Application under test | Spring Boot 4.0.3 + Thymeleaf | Root project (existing) |
| E2E database | H2 2.x, MODE=MySQL, in-memory | JVM embedded (no container) |
| Migration engine | Flyway (existing 45 scripts, adapted) | `src/main/resources/db/migration/` + `src/test/resources/db/migration/` |
| Test runner | @playwright/test (Node.js) | `e2e/` subdirectory |
| Browser | Chromium (headless, Playwright-managed) | CI cache |
| CI orchestration | GitHub Actions | `.github/workflows/ci-java21.yml` |

**Prinsip desain:**
1. **Hermetic** — Tidak ada state yang bocor antar test run. H2 in-memory dihancurkan saat JVM shutdown.
2. **Self-contained** — Semua yang dibutuhkan ada di repository. Tidak ada dependensi Docker/container.
3. **Parallel-safe** — E2E job berjalan independen dari fast-tests dan full-tests.
4. **Fail-fast** — Test dihentikan pada kegagalan pertama (`--max-failures=1` di CI).

---

### 3. Spring Boot E2E Profile Design

#### 3.1 File Konfigurasi: `application-e2e.yaml`

```yaml
# src/main/resources/application-e2e.yaml
server:
  port: 18080
  servlet:
    session:
      cookie:
        secure: false  # H2 mode tidak perlu HTTPS

spring:
  datasource:
    url: jdbc:h2:mem:erp_e2e;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;DB_CLOSE_DELAY=-1
    username: sa
    password:
    driver-class-name: org.h2.Driver

  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: none  # Tetap gunakan Flyway
    show-sql: false

  flyway:
    enabled: true
    baseline-on-migrate: true
    locations:
      - classpath:db/migration
      - classpath:db/migration-h2  # Override untuk H2 compatibility
    out-of-order: true

  thymeleaf:
    cache: true  # Enable cache untuk speed di E2E
    
  h2:
    console:
      enabled: false  # Tidak perlu console di CI

logging:
  level:
    root: WARN
    com.solusi.erp: INFO
    org.flywaydb: INFO
    org.springframework.security: WARN
    org.hibernate.SQL: OFF
```

#### 3.2 Dependensi Maven (scope test + runtime profil)

Tambahkan di `pom.xml`:

```xml
<!-- H2 Database for E2E testing -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>

<!-- Juga perlu runtime scope agar bisa dipakai saat spring-boot:run dengan profil e2e -->
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

#### 3.3 Kenapa H2 `MODE=MySQL`?

Flyway migration scripts di proyek ini menggunakan sintaks MariaDB-specific:

| Sintaks MariaDB | Support H2 MODE=MySQL? | Strategi |
|-----------------|----------------------|----------|
| `ENGINE=InnoDB` | ✅ Diabaikan oleh H2 | Aman |
| `BIGINT AUTO_INCREMENT` | ✅ Supported | Aman |
| `BOOLEAN` / `TINYINT(1)` | ✅ Supported | Aman |
| `DATETIME` | ✅ Supported | Aman |
| `VARCHAR`, `TEXT` | ✅ Supported | Aman |
| `NOW()` | ✅ Supported | Aman |
| `ON DELETE CASCADE` | ✅ Supported | Aman |
| `ENUM('A','B','C')` | ⚠️ H2 tidak support native `ENUM` | **Override migration** |
| `ADD COLUMN IF NOT EXISTS` | ⚠️ Sintaks MariaDB-specific | **Override migration** |
| `INSERT ... SELECT` (subquery seed) | ✅ Supported | Aman |
| `UPDATE ... SET ... WHERE ... IN (SELECT ...)` | ✅ Supported | Aman |

#### 3.4 Strategi Override Migration untuk H2

Buat direktori `src/test/resources/db/migration-h2/` yang berisi **hanya** migration scripts yang perlu di-override untuk H2 compatibility. Flyway akan load dari kedua lokasi; migration di `migration-h2/` dengan versi yang sama akan **menimpa** yang di `migration/`.

**File yang perlu override** (berisi `ENUM`):

| File | Alasan Override |
|------|-----------------|
| `V3__Inventory_Product_Category.sql` | `ENUM('STOCK','NON_STOCK','SERVICE')` → `VARCHAR(20) CHECK(...)` |
| `V4__System_Sequence_Generator.sql` | `ENUM('DAILY','MONTHLY','YEARLY','NEVER')` → `VARCHAR(20) CHECK(...)` |
| `V5__Inventory_Unit_Of_Measure.sql` | `ENUM('WEIGHT','LENGTH',...)` → `VARCHAR(20) CHECK(...)` |
| `V8__Master_Business_Partner.sql` | Multiple `ENUM` columns → `VARCHAR CHECK` |
| `V15__Master_Geographic_Indonesia_Data.sql` | Bulk insert 500+ rows; review untuk kompatibilitas |
| `V30__Align_Party_Role_Type_With_Refactor.sql` | `ADD COLUMN IF NOT EXISTS` → standard `ALTER TABLE` |
| `V40__Add_News_Code_And_Approval_Reference_Code.sql` | `ADD COLUMN IF NOT EXISTS`, `ADD UNIQUE KEY IF NOT EXISTS` |

**Contoh override V3:**

```sql
-- src/test/resources/db/migration-h2/V3__Inventory_Product_Category.sql
CREATE TABLE product_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL DEFAULT 'STOCK' CHECK (type IN ('STOCK', 'NON_STOCK', 'SERVICE')),
    note TEXT,
    created_by VARCHAR(100) NOT NULL,
    created_date DATETIME NOT NULL,
    updated_by VARCHAR(100),
    updated_date DATETIME,
    version INT NOT NULL DEFAULT 1
);

INSERT INTO permissions (name, description, created_by, created_date) VALUES
('PRODUCT-CATEGORY_READ', 'Melihat Daftar Kategori Produk', 'SYSTEM', NOW()),
('PRODUCT-CATEGORY_CREATE', 'Menambah Kategori Produk Baru', 'SYSTEM', NOW()),
('PRODUCT-CATEGORY_UPDATE', 'Mengubah Data Kategori Produk', 'SYSTEM', NOW()),
('PRODUCT-CATEGORY_DELETE', 'Menghapus Kategori Produk', 'SYSTEM', NOW());

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'ROLE_ADMIN'
AND p.name LIKE 'PRODUCT-CATEGORY_%';
```

#### 3.5 MinIO Stub untuk E2E

Aplikasi menggunakan MinIO (`StorageProvider` interface) untuk approval signatures. Di profil E2E, MinIO tidak tersedia. Solusi:

Buat `NoOpStorageProvider` yang di-register hanya pada profil `e2e`:

```java
// src/test/java/com/solusi/erp/e2e/config/E2eStorageConfig.java
package com.solusi.erp.e2e.config;

import com.solusi.erp.core.storage.domain.port.StorageProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("e2e")
public class E2eStorageConfig {

    @Bean
    @Primary
    public StorageProvider noOpStorageProvider() {
        return new StorageProvider() {
            @Override public void store(String bucket, String key, byte[] data, String contentType) { }
            @Override public String getUrl(String bucket, String key) { return "/img/placeholder.png"; }
            @Override public String getPresignedUrl(String bucket, String key, int expirySeconds) { return "/img/placeholder.png"; }
            @Override public byte[] getBytes(String bucket, String key) { return new byte[0]; }
            @Override public void delete(String bucket, String key) { }
        };
    }
}
```

#### 3.6 Startup Command

```bash
# Start Spring Boot dengan profil e2e
./mvnw spring-boot:run -Pe2e \
  -Dspring-boot.run.profiles=e2e \
  -Dspring-boot.run.jvmArguments="-Xmx512m"
```

Urutan startup:
1. H2 in-memory database di-create
2. Flyway jalankan 45 migration scripts (+ overrides dari `migration-h2/`)
3. `SystemInitializer` detect `INITIAL_PASSWORD_SETUP` → encode BCrypt → admin/admin123
4. Application ready di `:18080`
5. Playwright tests mulai

---

### 4. Data Seeding Strategy

#### 4.1 Layer 1: Flyway Migrations (Automatic)

Flyway migrations **sudah** melakukan seeding signifikan:

| Data | Migration | Volume |
|------|-----------|--------|
| Permissions | V2 + 20 migration files | ~100+ permissions |
| Roles (ROLE_ADMIN, ROLE_STAFF) | V2 | 2 roles |
| Admin user (admin/admin123) | V2 + SystemInitializer | 1 user + profile |
| Role-Permission mapping (ROLE_ADMIN = all) | V2 + subsequent | Full access |
| Geographic (Indonesia 38 provinsi + 514 kota) | V12, V15 | 550+ records |
| Permission Groups (with icons, sort order) | V19, V20, V39 | UI menu structure |

Ini cukup untuk **login** dan **navigasi** ke semua modul. Namun belum cukup untuk test yang membutuhkan data master (Product, Brand, Category, Facility).

#### 4.2 Layer 2: Playwright-Driven Seeding (Test Flow as Seed)

**Strategi utama:** Gunakan test itu sendiri sebagai mekanisme seeding. Test dijalankan secara **serial** dengan **urutan dependency**:

```
Login ──► Brand ──► Category ──► UoM ──► Product ──► Facility ──► Grid ──► Container ──► Stock Adjustment
                                                          └── Geographic (sudah ada dari Flyway)
                                                          └── Party (depends: Geographic)
```

Ini lebih baik daripada SQL seed files karena:
- Memvalidasi bahwa CREATE flow benar-benar bekerja
- Data yang di-seed melalui UI pasti konsisten (sequence codes, audit fields terisi)
- Tidak ada risiko seed SQL out-of-sync dengan schema terbaru

#### 4.3 Layer 3: `globalSetup` untuk Data Dependencies

Untuk test yang membutuhkan data prerequisite (e.g., Product membutuhkan Brand + Category + UoM), gunakan Playwright `globalSetup` yang melakukan initial CRUD via browser:

```javascript
// e2e/global-setup.ts
import { chromium, FullConfig } from '@playwright/test';

async function globalSetup(config: FullConfig) {
  const browser = await chromium.launch();
  const page = await browser.newPage();
  
  // Login
  await login(page);
  
  // Seed minimum master data via UI
  await seedBrand(page);
  await seedCategory(page);
  await seedUom(page);  // Jika belum ada dari migration
  
  // Save auth state untuk reuse
  await page.context().storageState({ path: 'e2e/.auth/admin.json' });
  
  await browser.close();
}
```

#### 4.4 Mengapa BUKAN SQL Seed File?

| Pendekatan | Pro | Kontra |
|------------|-----|--------|
| SQL seed file | Cepat, deterministik | Harus maintain sync dengan schema; bypass validasi; sequence codes manual |
| Playwright global setup | Validasi UI flow; auto-sync; realistic | Lebih lambat (~15 detik) |
| Spring `@Sql` annotation | N/A (test di luar JVM) | — |

**Keputusan:** Layer 2 (test-as-seed) + Layer 3 (globalSetup) karena:
- **Reproducibility**: Setiap CI run dimulai dari database kosong → Flyway → global setup → tests
- **Zero maintenance**: Tidak ada SQL seed yang bisa drift dari schema
- **Double verification**: Seeding itu sendiri adalah test bahwa CREATE works

---

### 5. Playwright Test Architecture

#### 5.1 Struktur Direktori

```
e2e/
├── package.json
├── package-lock.json
├── playwright.config.ts
├── tsconfig.json
├── .gitignore                    # node_modules/
├── global-setup.ts               # Seed data + auth state
├── global-teardown.ts            # Cleanup (optional)
├── helpers/
│   ├── auth.ts                   # Login helper + storage state
│   ├── tomselect.ts              # TomSelect interaction helpers
│   ├── flatpickr.ts              # Flatpickr date setter
│   ├── autonumeric.ts            # AutoNumeric value setter
│   ├── ajax-form.ts              # AJAX form submission + verification
│   ├── line-items.ts             # Dynamic row management
│   └── navigation.ts             # Menu navigation + HTMX wait
├── fixtures/
│   └── erp-fixtures.ts           # Custom Playwright fixtures
├── tests/
│   ├── 01-auth/
│   │   └── login.spec.ts
│   ├── 02-master/
│   │   ├── brand.spec.ts
│   │   ├── product-category.spec.ts
│   │   ├── uom.spec.ts
│   │   ├── geographic.spec.ts
│   │   ├── party.spec.ts
│   │   ├── currency.spec.ts
│   │   ├── tax.spec.ts
│   │   └── bank-account.spec.ts
│   ├── 03-inventory/
│   │   ├── facility.spec.ts
│   │   ├── grid.spec.ts
│   │   ├── container.spec.ts
│   │   ├── product.spec.ts
│   │   ├── uom-conversion.spec.ts
│   │   └── stock-adjustment.spec.ts
│   ├── 04-accounting/
│   │   ├── schema.spec.ts
│   │   ├── coa.spec.ts
│   │   └── period.spec.ts
│   ├── 05-common/
│   │   ├── news.spec.ts
│   │   └── approval.spec.ts
│   └── 06-security/
│       ├── role.spec.ts
│       ├── user.spec.ts
│       └── permission.spec.ts
└── .auth/                        # Generated auth state (gitignored)
    └── admin.json
```

#### 5.2 `playwright.config.ts`

```typescript
import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './tests',
  
  // SERIAL execution — test order matters (data dependencies)
  fullyParallel: false,
  workers: 1,
  
  // CI-optimized settings
  retries: process.env.CI ? 1 : 0,
  timeout: 30_000,
  expect: { timeout: 5_000 },
  
  // Reporter
  reporter: process.env.CI 
    ? [['html', { open: 'never' }], ['github']] 
    : [['html', { open: 'on-failure' }]],
  
  // Global setup: seed data + auth
  globalSetup: './global-setup.ts',
  
  use: {
    baseURL: 'http://localhost:18080',
    
    // Auth state reuse
    storageState: '.auth/admin.json',
    
    // Performance & reliability
    actionTimeout: 10_000,
    navigationTimeout: 15_000,
    
    // Headless in CI, headed in local dev
    headless: !!process.env.CI,
    
    // Artifacts on failure only
    screenshot: 'only-on-failure',
    trace: 'retain-on-failure',
    video: 'retain-on-failure',
  },
  
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
  ],
  
  // TIDAK menggunakan webServer karena Spring Boot di-start terpisah oleh CI
  // Ini memberi kontrol penuh atas startup + health check
});
```

#### 5.3 Helper: TomSelect (`helpers/tomselect.ts`)

```typescript
import { Page } from '@playwright/test';

/**
 * Interaksi dengan TomSelect via JavaScript API.
 * Diperlukan karena TomSelect me-wrap <select> asli menjadi custom widget.
 */
export async function selectTomSelect(
  page: Page, 
  selector: string, 
  searchQuery = '', 
  optionIndex = 0
): Promise<{ id: string; name: string } | { error: string }> {
  // Tunggu TomSelect terinisialisasi
  await page.waitForFunction(
    (sel) => {
      const el = document.querySelector(sel);
      return el && (el as any).tomselect;
    },
    selector,
    { timeout: 5000 }
  );

  return page.evaluate(({ sel, query, idx }) => {
    return new Promise<any>((resolve, reject) => {
      const el = document.querySelector(sel) as any;
      if (!el?.tomselect) {
        reject(new Error(`TomSelect not found: ${sel}`));
        return;
      }
      const ts = el.tomselect;
      ts.load(query, (options: any[]) => {
        if (!options.length) {
          resolve({ error: 'No options available' });
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
 * Set TomSelect ke value ID tertentu.
 */
export async function setTomSelectValue(
  page: Page, 
  selector: string, 
  valueId: string | number
): Promise<void> {
  await page.evaluate(({ sel, val }) => {
    return new Promise<void>((resolve) => {
      const el = document.querySelector(sel) as any;
      const ts = el.tomselect;
      ts.load('', (options: any[]) => {
        options.forEach(opt => ts.addOption(opt));
        ts.setValue(String(val));
        resolve();
      });
    });
  }, { sel: selector, val: String(valueId) });
}
```

#### 5.4 Helper: AJAX Form Submit (`helpers/ajax-form.ts`)

```typescript
import { Page, expect } from '@playwright/test';

export interface FormSubmitResult {
  success: boolean;
  message?: string;
  errors?: Record<string, string>;
}

/**
 * Submit form AJAX dan tunggu response.
 * Form ERP menggunakan data-ajax-form="true" yang return JSON.
 */
export async function submitFormAndVerify(
  page: Page,
  expectedRedirectPath?: string
): Promise<FormSubmitResult> {
  const responsePromise = page.waitForResponse(
    resp => resp.request().method() === 'POST' 
      && (resp.headers()['content-type']?.includes('json') ?? false),
    { timeout: 15_000 }
  );

  await page.click('button[type="submit"]');
  
  const response = await responsePromise;
  const body = await response.json();
  
  if (body.success && expectedRedirectPath) {
    await page.waitForURL(`**${expectedRedirectPath}**`, { timeout: 10_000 });
  }
  
  return {
    success: body.success,
    message: body.message,
    errors: body.validationErrors,
  };
}
```

#### 5.5 Contoh Test Spec: Brand CRUD (`tests/02-master/brand.spec.ts`)

```typescript
import { test, expect } from '@playwright/test';
import { submitFormAndVerify } from '../../helpers/ajax-form';

const BRAND_NAME = `E2E-Brand-${Date.now()}`;
const BRAND_NAME_UPDATED = `${BRAND_NAME}-Updated`;

test.describe.serial('Brand CRUD', () => {
  
  test('should create a new brand', async ({ page }) => {
    await page.goto('/inventory/brands/create');
    await page.waitForLoadState('networkidle');
    
    await page.fill('input[name="name"]', BRAND_NAME);
    
    const result = await submitFormAndVerify(page, '/inventory/brands');
    expect(result.success).toBe(true);
  });

  test('should show brand in list', async ({ page }) => {
    await page.goto('/inventory/brands');
    await page.waitForLoadState('networkidle');
    
    // Search
    await page.fill('input[name="keyword"]', BRAND_NAME);
    await page.press('input[name="keyword"]', 'Enter');
    await page.waitForLoadState('networkidle');
    
    // Verify brand appears
    await expect(page.locator('table')).toContainText(BRAND_NAME);
  });

  test('should edit the brand', async ({ page }) => {
    await page.goto('/inventory/brands');
    await page.fill('input[name="keyword"]', BRAND_NAME);
    await page.press('input[name="keyword"]', 'Enter');
    await page.waitForLoadState('networkidle');
    
    // Click edit
    await page.click(`tr:has-text("${BRAND_NAME}") a[href*="/edit"]`);
    await page.waitForLoadState('networkidle');
    
    // Update name
    await page.fill('input[name="name"]', BRAND_NAME_UPDATED);
    
    const result = await submitFormAndVerify(page, '/inventory/brands');
    expect(result.success).toBe(true);
  });

  test('should show updated brand in list', async ({ page }) => {
    await page.goto('/inventory/brands');
    await page.fill('input[name="keyword"]', BRAND_NAME_UPDATED);
    await page.press('input[name="keyword"]', 'Enter');
    await page.waitForLoadState('networkidle');
    
    await expect(page.locator('table')).toContainText(BRAND_NAME_UPDATED);
  });
});
```

#### 5.6 Anti-Flakiness Measures

| Teknik | Implementasi | Alasan |
|--------|-------------|--------|
| **Serial execution** | `workers: 1`, `test.describe.serial` | Data dependencies antar test |
| **Explicit waits** | `waitForLoadState('networkidle')` | HTMX partial reload selesai |
| **TomSelect wait** | `waitForFunction` sebelum interact | Defer script init delay |
| **Unique test data** | `Date.now()` suffix di setiap data | Tidak clash antar run |
| **Retry on CI** | `retries: 1` (hanya CI) | Mitigasi browser timing quirk |
| **No `waitForTimeout`** | Diganti `waitForFunction` / `waitForResponse` | Deterministic wait |
| **Clean DB per run** | H2 in-memory, hilang saat JVM stop | Zero state leakage |
| **Auth state reuse** | `storageState` saved di globalSetup | Skip login per test |

---

### 6. CI/CD Integration

#### 6.1 GitHub Actions Job: `e2e-tests`

```yaml
# Tambahkan ke .github/workflows/ci-java21.yml

  # ──────────────────────────────────────────────────────────────────────
  # Job 3: E2E — Playwright browser tests against Spring Boot + H2
  # Runs on: push to main, nightly schedule, manual dispatch
  # Target time: < 5 minutes
  # ──────────────────────────────────────────────────────────────────────
  e2e-tests:
    name: E2E Tests (Playwright)
    runs-on: ubuntu-latest
    timeout-minutes: 15
    if: |
      github.event_name == 'schedule' ||
      (github.event_name == 'workflow_dispatch' && inputs.run_full == true) ||
      (github.event_name == 'push' && (github.ref == 'refs/heads/main' || github.ref == 'refs/heads/master'))

    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Set up JDK ${{ env.JAVA_VERSION }}
        uses: actions/setup-java@v4
        with:
          distribution: ${{ env.JAVA_DISTRIBUTION }}
          java-version: ${{ env.JAVA_VERSION }}
          cache: maven

      - name: Set up Node.js
        uses: actions/setup-node@v4
        with:
          node-version: '20'

      - name: Make Maven Wrapper Executable
        run: chmod +x mvnw

      # Build tanpa test terlebih dahulu, lalu jalankan
      - name: Build application (skip tests)
        run: ./mvnw -B -q package -DskipTests -Pe2e

      # Start Spring Boot di background
      - name: Start Spring Boot (E2E profile)
        run: |
          java -jar target/solusi-program-erp-*.jar \
            --spring.profiles.active=e2e \
            --server.port=18080 \
            -Xmx512m &
          echo $! > spring-boot.pid
        
      # Health check: tunggu sampai app ready
      - name: Wait for application startup
        run: |
          echo "Waiting for Spring Boot to start..."
          for i in $(seq 1 60); do
            if curl -s -o /dev/null -w "%{http_code}" http://localhost:18080/login | grep -q "200\|302"; then
              echo "✅ Application is ready (attempt $i)"
              exit 0
            fi
            echo "  Attempt $i/60 - not ready yet..."
            sleep 2
          done
          echo "❌ Application failed to start within 120 seconds"
          cat logs/erp.log || true
          exit 1

      # Install Playwright dependencies
      - name: Install Playwright dependencies
        working-directory: e2e
        run: |
          npm ci
          npx playwright install chromium --with-deps

      # Cache Playwright browsers
      - name: Cache Playwright browsers
        uses: actions/cache@v4
        with:
          path: ~/.cache/ms-playwright
          key: playwright-${{ hashFiles('e2e/package-lock.json') }}
          restore-keys: playwright-

      # Jalankan E2E tests
      - name: Run E2E tests
        working-directory: e2e
        run: npx playwright test --max-failures=3
        env:
          CI: true

      # Stop Spring Boot
      - name: Stop Spring Boot
        if: always()
        run: |
          if [ -f spring-boot.pid ]; then
            kill $(cat spring-boot.pid) || true
          fi

      # Upload artifacts on failure
      - name: Upload Playwright report
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: playwright-report
          path: e2e/playwright-report/
          retention-days: 14

      - name: Upload test results
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: playwright-results
          path: e2e/test-results/
          retention-days: 7
```

#### 6.2 Dependency Graph Antar Jobs

```
                    push to feature/*
                          │
                          ▼
                    ┌─────────────┐
                    │ fast-tests  │ ← Setiap push (< 60s)
                    └──────┬──────┘
                           │
           push to main    │
          ┌────────────────┼────────────────┐
          ▼                ▼                ▼
  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐
  │  full-tests  │  │  e2e-tests   │  │              │
  │ (unit+tmpl)  │  │ (Playwright) │  │              │
  └──────┬───────┘  └──────┬───────┘  │              │
         │                 │          │              │
         └────────┬────────┘          │              │
                  ▼                   │              │
           ┌──────────┐              │              │
           │  deploy   │ ← needs: [fast-tests, full-tests, e2e-tests]
           └──────────┘
```

#### 6.3 CI Time Budget

| Phase | Target | Notes |
|-------|--------|-------|
| Checkout + Java setup | 15s | Cached Maven |
| Node.js setup | 10s | — |
| Maven build (`-DskipTests`) | 45s | Skip test, build only |
| Spring Boot startup (H2) | 30s | In-memory, no Docker |
| Playwright install | 20s | Cached browser |
| E2E tests (~20 scenarios) | 120s | Serial, 6s avg per test |
| Cleanup + upload | 10s | — |
| **Total** | **~4 min 10s** | **Target: < 5 min** |

#### 6.4 `e2e/package.json`

```json
{
  "name": "solusi-erp-e2e",
  "version": "1.0.0",
  "private": true,
  "description": "E2E tests for Solusi ERP using Playwright",
  "scripts": {
    "test": "playwright test",
    "test:headed": "playwright test --headed",
    "test:debug": "playwright test --debug",
    "test:ui": "playwright test --ui",
    "report": "playwright show-report"
  },
  "devDependencies": {
    "@playwright/test": "^1.52.0",
    "typescript": "^5.7.0",
    "@types/node": "^22.0.0"
  }
}
```

---

### 7. Cakupan Test Minimum per Modul

#### 7.1 Matrix Cakupan

Setiap modul wajib memiliki minimal test berikut:

| Modul | List | Create | Edit | Delete/Deactivate | Approve | Search | TomSelect | Line Items | Prioritas |
|-------|------|--------|------|--------------------|---------|--------|-----------|------------|-----------|
| **Login** | — | — | — | — | — | — | — | — | P0 |
| **Brand** | ✅ | ✅ | ✅ | — | — | ✅ | — | — | P0 |
| **Product Category** | ✅ | ✅ | ✅ | — | — | ✅ | — | — | P0 |
| **UoM** | ✅ | ✅ | ✅ | — | — | — | — | — | P0 |
| **Product** | ✅ | ✅ | ✅ | — | — | ✅ | ✅ (Category, Brand) | — | P0 |
| **Facility** | ✅ | ✅ | ✅ | — | — | — | — | — | P1 |
| **Grid** | ✅ | ✅ | ✅ | — | — | — | ✅ (Facility) | — | P1 |
| **Container** | ✅ | ✅ | ✅ | — | — | — | ✅ (Facility, Grid) | — | P1 |
| **Stock Adjustment** | ✅ | ✅ | ✅ | — | ✅ | — | ✅ (Facility, Product) | ✅ (Drawer: qty, serial) | P0 |
| **Geographic** | ✅ | — | — | — | — | ✅ | — | — | P2 |
| **Party** | ✅ | ✅ | ✅ | — | — | — | ✅ (Geographic) | ✅ (Address, Contact, ID) | P1 |
| **Currency** | ✅ | ✅ | ✅ | — | — | — | — | — | P2 |
| **Tax** | ✅ | ✅ | ✅ | — | — | — | — | — | P2 |
| **COA** | ✅ | ✅ | ✅ | — | — | ✅ | ✅ (Schema, Parent) | — | P1 |
| **Role** | ✅ | ✅ | ✅ | — | — | — | — | ✅ (Permission checklist) | P2 |
| **User** | ✅ | ✅ | ✅ | — | — | — | ✅ (Role) | — | P2 |
| **News** | ✅ | ✅ | ✅ | — | ✅ | — | — | — | P2 |

#### 7.2 Prioritas Implementasi

**P0 (Sprint 1 — Week 1-2):** Login, Brand, Product Category, UoM, Product, Stock Adjustment
- Alasan: Ini adalah core inventory flow yang paling sering diubah dan paling berisiko regresi
- Target: 12 test cases

**P1 (Sprint 2 — Week 3-4):** Facility, Grid, Container, Party, COA
- Alasan: Dependencies untuk Stock Adjustment (Warehouse Hierarchy) dan Accounting
- Target: +15 test cases

**P2 (Sprint 3 — Week 5-6):** Geographic, Currency, Tax, Role, User, News
- Alasan: Modul stabil, jarang berubah
- Target: +12 test cases

#### 7.3 Contoh Test Case Detail: Stock Adjustment (Modul Paling Kompleks)

```typescript
test.describe.serial('Stock Adjustment CRUD + Approve', () => {
  
  test('should create stock adjustment with 2 line items', async ({ page }) => {
    await page.goto('/inventory/stock-adjustments/create');
    await page.waitForLoadState('networkidle');
    
    // Header: select Facility via TomSelect
    await selectTomSelect(page, '#header-facility', '', 0);
    
    // Header: set date via Flatpickr
    await setFlatpickrDate(page, 'input[name="transactionDate"]', '2025-07-15');
    
    // Add Line 1 (serialized product)
    await page.click('#btn-add-line');
    await page.waitForTimeout(500);
    await selectLineItemTomSelect(page, 0, 'select-product', '');
    // Open drawer → set qty → fill serial numbers → apply
    // ... (drawer interaction helpers)
    
    // Add Line 2 (non-serialized product)
    await page.click('#btn-add-line');
    await page.waitForTimeout(500);
    await selectLineItemTomSelect(page, 1, 'select-product', '');
    // ... set qty, price
    
    // Submit
    const result = await submitFormAndVerify(page, '/inventory/stock-adjustments');
    expect(result.success).toBe(true);
  });

  test('should show stock adjustment in list with DRAFT status', async ({ page }) => {
    await page.goto('/inventory/stock-adjustments');
    await expect(page.locator('table')).toContainText('DRAFT');
  });

  test('should edit stock adjustment', async ({ page }) => {
    // Navigate, edit, verify
    // ...
  });

  test('should approve stock adjustment', async ({ page }) => {
    // Navigate to detail → click Approve → verify status change to APPROVED
    // Verify: stock balance updated, inventory movements created
    // ...
  });
});
```

---

### 8. Risiko & Mitigasi

| # | Risiko | Severity | Probability | Mitigasi |
|---|--------|----------|-------------|----------|
| **R1** | Flyway migration tidak compatible dengan H2 meski MODE=MySQL (terutama ENUM, IF NOT EXISTS) | 🔴 High | 🟡 Medium | Override migrations di `migration-h2/`. Saat ini teridentifikasi 7 file yang perlu override. Unit test: jalankan semua migration terhadap H2 sebagai smoke test tersendiri |
| **R2** | Hibernate query yang menggunakan MariaDB-specific function (e.g., `MATCH ... AGAINST`) gagal di H2 | 🟡 Medium | 🟢 Low | Audit semua `@Query` native queries. Codebase saat ini menggunakan JPQL/Criteria, bukan native SQL. Risiko rendah |
| **R3** | TomSelect interaction flaky karena timing (defer script, AJAX options loading) | 🟡 Medium | 🟡 Medium | Helper `waitForFunction` memastikan TomSelect terinisialisasi sebelum interact. Retry 1x di CI. Jika persistent: switch ke klik-based approach (snapshot + click combobox + click option) |
| **R4** | Spring Boot startup time di CI > 60 detik | 🟡 Medium | 🟢 Low | H2 in-memory lebih cepat dari MariaDB. Pre-build JAR (`mvn package -DskipTests`). Startup target: 20-30 detik. Jika lebih: profiling + lazy init |
| **R5** | Playwright browser cache miss di CI → install 200MB+ setiap run | 🟡 Medium | 🟡 Medium | GitHub Actions cache pada `~/.cache/ms-playwright` dengan hash key dari `package-lock.json`. Cache hit rate target: > 95% |
| **R6** | V15 (550+ geographic records) memperlambat H2 seeding | 🟢 Low | 🟡 Medium | Profil e2e bisa punya override V15 yang hanya insert ~10 provinsi + 20 kota. Test geographic tidak membutuhkan seluruh 514 kota |
| **R7** | `SystemInitializer` gagal di H2 karena beda behavior `findByUsername` query | 🟢 Low | 🟢 Low | `findByUsername` adalah standard Spring Data JPA derivation query. Portable antar database. Test di lokal terlebih dahulu |
| **R8** | MinIO tidak tersedia → fitur approval signature error | 🟡 Medium | 🔴 High | `NoOpStorageProvider` (section 3.5) sudah dirancang. Di-activate hanya pada profil `e2e` via `@Profile("e2e")` + `@Primary` |
| **R9** | Perubahan UI (field name, CSS class, TomSelect selector) membreak E2E test | 🟡 Medium | 🟡 Medium | Gunakan semantic selectors (`[name="..."]`, `role=button`), bukan CSS class. Tambahkan `data-testid` attributes pada elemen kritis. Maintenance cost: ~2 jam/sprint |
| **R10** | H2 auto-increment behavior berbeda dari MariaDB (ID values berbeda) | 🟢 Low | 🟡 Medium | Test TIDAK boleh hardcode ID. Selalu reference by name/code, bukan by numeric ID |

#### Mitigation Validation Plan

Sebelum implementasi penuh, jalankan **proof-of-concept** berikut:

1. **H2 Migration POC** (2 jam): Jalankan semua 45 Flyway migrations terhadap H2 MODE=MySQL. Identifikasi semua failure. Buat override files.
2. **Boot + Login POC** (1 jam): Start Spring Boot dengan profil e2e + H2. Pastikan login admin/admin123 berhasil via browser.
3. **TomSelect POC** (1 jam): Jalankan 1 Playwright test yang create Product (uses TomSelect for Category + Brand). Verifikasi helper works.

---

### 9. Effort Estimate

#### 9.1 Breakdown per Work Item

| # | Work Item | Effort | Dependency |
|---|-----------|--------|------------|
| **W1** | Spring Boot E2E profile (`application-e2e.yaml`) | 2h | — |
| **W2** | H2 dependency di Maven (profile `e2e`) | 0.5h | — |
| **W3** | Flyway H2 override migrations (7 files) | 4h | W1 |
| **W4** | `NoOpStorageProvider` + `E2eStorageConfig` | 1h | W1 |
| **W5** | POC: Boot + Login + 1 CRUD test | 3h | W1-W4 |
| **W6** | Scaffold `e2e/` directory + config | 1h | — |
| **W7** | Helpers: auth, TomSelect, Flatpickr, AutoNumeric | 4h | W6 |
| **W8** | Helpers: AJAX form submit, line items, navigation | 3h | W7 |
| **W9** | Global setup (seed master data via UI) | 3h | W5, W7 |
| **W10** | P0 tests: Login, Brand, Category, UoM, Product | 6h | W9 |
| **W11** | P0 tests: Stock Adjustment (complex: drawer, serial, approve) | 8h | W10 |
| **W12** | CI/CD integration (GitHub Actions job) | 3h | W10 |
| **W13** | CI stabilization (flakiness elimination, cache tuning) | 4h | W12 |
| **W14** | P1 tests: Facility, Grid, Container, Party, COA | 8h | W11 |
| **W15** | P2 tests: Remaining modules | 6h | W14 |
| **W16** | Documentation + runbook | 2h | W15 |

#### 9.2 Timeline

| Sprint | Duration | Deliverable | Total Tests |
|--------|----------|-------------|-------------|
| **Sprint 1** | Week 1-2 | W1-W13: Infrastructure + P0 tests + CI | ~12 tests |
| **Sprint 2** | Week 3-4 | W14: P1 tests | ~27 tests |
| **Sprint 3** | Week 5-6 | W15-W16: P2 tests + docs | ~39 tests |

#### 9.3 Total Effort

| Category | Hours |
|----------|-------|
| Infrastructure (W1-W6) | 11.5h |
| Helpers (W7-W9) | 10h |
| Test authoring (W10-W11, W14-W15) | 28h |
| CI/CD (W12-W13) | 7h |
| Documentation (W16) | 2h |
| **Grand Total** | **~58.5h (~7.5 person-days)** |

---

### 10. Trade-offs

#### 10.1 Keputusan Arsitektural dan Konsekuensinya

| Keputusan | Yang Didapat ✅ | Yang Dikorbankan ❌ |
|-----------|-----------------|---------------------|
| **H2 in-memory vs MariaDB Docker** | Zero external dependency di CI; startup < 30s; hermetic runs; no Docker license worry | H2 ≠ MariaDB 100% — ENUM perlu override; edge case behavior bisa beda (collation, lock, deadlock). E2E test TIDAK menggantikan staging test terhadap MariaDB riil |
| **Serial execution (workers: 1)** | Deterministic data ordering; no race condition; simple reasoning tentang state | Total E2E time lebih lama (~2-3 menit vs ~1 menit jika parallel). Acceptable karena target < 5 menit |
| **Playwright-driven seeding (bukan SQL seed)** | Seeding = test (double value); auto-sync dengan schema; realistic | Seeding lebih lambat (~15s vs ~1s SQL). Global setup menambah waktu tapi mengurangi maintenance cost |
| **Single browser (Chromium only)** | Simple config; Chromium = Chrome market share; lebih cepat | Tidak catch Firefox/Safari-specific bugs. Acceptable: ini ERP internal, bukan public website. Bisa tambah browser nanti |
| **TypeScript untuk test** | Type safety; autocomplete; better maintainability | Node.js + TypeScript toolchain overhead di CI (install ~20s, compile). Acceptable: lebih baik daripada JS tanpa type |
| **E2E job hanya di main + nightly (bukan setiap push)** | CI tetap cepat untuk feature branch (<60s fast-tests). E2E hanya block deployment | Developer tidak dapat feedback E2E instant di feature branch. Mitigasi: bisa trigger manual via `workflow_dispatch` |
| **Auth state reuse via storageState** | Login hanya sekali di globalSetup; setiap test langsung authenticated | Jika session management berubah, storageState bisa stale. Mitigasi: globalSetup selalu fresh login |
| **No visual regression testing** | Simpler setup; no baseline management; no screenshot comparison flakiness | Tidak catch CSS regressions. Trade-off acceptable: priority saat ini adalah functional correctness, bukan pixel-perfect |
| **Test data menggunakan `Date.now()` suffix** | Test isolation — tidak clash antar run | Data "sampah" menumpuk jika DB persistent (not our case — H2 in-memory hilang saat stop) |
| **Override migration files terpisah (bukan conditional SQL)** | Clean separation; Flyway native mechanism; jelas mana yang original vs override | Maintenance burden: setiap kali migration berubah, override harus di-update juga. Mitigasi: CI fast-fail jika migration inconsistent; hanya 7 dari 45 migration perlu override |

#### 10.2 Apa yang TIDAK Di-cover oleh Solusi Ini

| Aspek | Status | Rekomendasi |
|-------|--------|-------------|
| **Performance testing** | ❌ Out of scope | Gunakan JMeter/Gatling terpisah |
| **Visual regression** | ❌ Out of scope | Pertimbangkan Playwright visual comparison di fase 2 |
| **Multi-browser testing** | ❌ Hanya Chromium | Tambahkan Firefox jika ERP dipakai di browser beragam |
| **Mobile responsive** | ❌ Desktop only | Tambahkan viewport mobile jika diperlukan |
| **Load testing dengan concurrent users** | ❌ | Session-based security + optimistic locking perlu load test terpisah |
| **Database-level assertion** | ❌ Hanya UI assertion | Jika perlu, bisa tambah JDBC connection ke H2 dari Node.js via REST endpoint khusus profil e2e |
| **Negative testing (validation errors)** | ⚠️ Minimal | Sprint 1 fokus happy path. Tambahkan negative cases di sprint 2+ |

#### 10.3 Exit Criteria untuk Go/No-Go

| Criteria | Threshold |
|----------|-----------|
| POC berhasil: Spring Boot + H2 + Login + 1 CRUD | ✅ Mandatory |
| CI E2E job total time | < 5 menit |
| Flakiness rate setelah sprint 1 | < 5% (< 1 flaky run per 20 CI runs) |
| All P0 tests pass consistently | 10 consecutive green runs |
| No increase in fast-tests job time | 0 detik impact (E2E terpisah) |
