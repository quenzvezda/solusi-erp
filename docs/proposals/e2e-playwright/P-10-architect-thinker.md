# Proposal Teknis: Playwright E2E Test — Solusi ERP

### 1. Executive Summary

Proposal ini mengusulkan implementasi **Playwright E2E test** yang berjalan di atas Spring Boot dengan profile `e2e`, menggunakan **H2 in-memory database (MODE=MySQL)** sebagai pengganti MariaDB. Pendekatan ini mengeliminasi dependensi database eksternal di CI pipeline sepenuhnya.

Strategi inti: **Flyway migration yang ada TIDAK dimodifikasi**. Sebagai gantinya, kita menyediakan migration set terpisah di `db/migration-e2e/` yang sudah di-transpile ke dialect H2-compatible. Satu `E2eDataSeeder` CommandLineRunner menyuntikkan data master minimum setelah schema terbentuk.

Cakupan test difokuskan pada **happy-path CRUD per modul** — operasi yang paling sering rusak setelah perubahan kode: create, update, dan (jika ada) approve/process. Dengan 8–10 test spec mencakup ~15 modul, tim mendapatkan confidence bahwa alur utama tidak broken, tanpa overhead maintenance test yang berlebihan.

---

### 2. Arsitektur Solusi

```
┌──────────────────────────────────────────────────────────────────┐
│                        CI Runner (GitHub Actions)                │
│                                                                  │
│  ┌───────────────────────────┐    ┌────────────────────────────┐ │
│  │   Spring Boot (profile=e2e)│    │    Playwright Test Suite   │ │
│  │                           │    │    (e2e/ subdirectory)      │ │
│  │  ┌─────────┐ ┌─────────┐ │    │                            │ │
│  │  │Thymeleaf│ │Security │ │    │  ┌────────────────────┐    │ │
│  │  │  SSR    │ │ Session │ │    │  │  @playwright/test  │    │ │
│  │  └─────────┘ └─────────┘ │    │  └────────────────────┘    │ │
│  │  ┌─────────┐ ┌─────────┐ │    │           │                │ │
│  │  │ Flyway  │ │  H2 DB  │ │    │    HTTP (localhost:18080)  │ │
│  │  │(e2e set)│ │(in-mem) │◄├────┤───────────┘                │ │
│  │  └─────────┘ └─────────┘ │    │                            │ │
│  │  ┌─────────────────────┐ │    │  helpers/                  │ │
│  │  │ E2eDataSeeder       │ │    │  ├─ tom-select.ts          │ │
│  │  │ (CommandLineRunner) │ │    │  ├─ flatpickr.ts           │ │
│  │  └─────────────────────┘ │    │  ├─ autonumeric.ts         │ │
│  │  ┌─────────────────────┐ │    │  ├─ ajax-form.ts           │ │
│  │  │ E2eMinioStub        │ │    │  └─ auth.ts                │ │
│  │  │ (NoOp Storage)      │ │    │                            │ │
│  │  └─────────────────────┘ │    └────────────────────────────┘ │
│  └───────────────────────────┘                                   │
└──────────────────────────────────────────────────────────────────┘
```

**Alur eksekusi:**

1. **Maven build** → `./mvnw clean package -DskipTests` menghasilkan JAR
2. **Spring Boot start** → `java -jar app.jar --spring.profiles.active=e2e --server.port=18080`
3. Flyway menjalankan migration set dari `db/migration-e2e/` ke H2 in-memory
4. `SystemInitializer` (existing) mengubah password placeholder → BCrypt hash
5. `E2eDataSeeder` menyuntikkan data master (Party, Category, Brand, UoM sudah dari migration V5/V6 seed, Currency dari V11 seed; seeder menambahkan Facility, Grid, Container, Product)
6. **Health check** → CI poll `GET /login` sampai response 200
7. **Playwright test** → `npx playwright test` di subdirectory `e2e/`
8. **Artifact collection** → screenshots, traces, HTML report di-upload sebagai CI artifact

**Mengapa arsitektur ini:**

- Spring Boot berjalan sebagai JAR standalone (bukan `@SpringBootTest` di JVM yang sama). Ini mensimulasikan deployment nyata.
- H2 MODE=MySQL memberikan kompatibilitas cukup untuk schema MariaDB sederhana.
- Migration terpisah menghindari perlu maintain dua dialect di satu file.
- Playwright berjalan di Node.js terpisah, berkomunikasi via HTTP — persis seperti user sungguhan.

---

### 3. Spring Boot E2E Profile Design

#### 3.1 File: `src/main/resources/application-e2e.yaml`

```yaml
server:
  port: 18080
  servlet:
    session:
      cookie:
        secure: false  # H2 tidak perlu HTTPS

spring:
  datasource:
    url: jdbc:h2:mem:erp_e2e;DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;NON_KEYWORDS=VALUE
    driver-class-name: org.h2.Driver
    username: sa
    password:

  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: none  # Tetap gunakan Flyway
    show-sql: false

  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration-e2e
    # Gunakan migration set terpisah yang H2-compatible

  h2:
    console:
      enabled: true
      path: /h2-console

  thymeleaf:
    cache: false

logging:
  level:
    com.solusi.erp: INFO
    org.springframework.security: WARN
    org.hibernate.SQL: OFF
    org.flywaydb: INFO

# Stub MinIO — tidak ada real MinIO di E2E
minio:
  endpoint: http://localhost:19000
  presigned-endpoint: http://localhost:19000
  access-key: test
  secret-key: test
  buckets:
    signatures: test-bucket
```

#### 3.2 H2 Dependency (scope test → runtime untuk profile e2e)

Tambahkan di `pom.xml`:

```xml
<!-- H2 for E2E profile (in-memory database) -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>
```

> **Catatan:** `scope` diubah ke `runtime` (bukan `test`) agar tersedia saat JAR dijalankan dengan `--spring.profiles.active=e2e`. Alternatif: gunakan Maven profile untuk meng-include H2 hanya saat build E2E.

#### 3.3 Strategi H2 + Flyway Compatibility

**Problem:** 45 migration file menggunakan syntax MariaDB-specific:

| Syntax MariaDB | Frekuensi | H2 Compatibility |
|---|---|---|
| `ENGINE=InnoDB` | Setiap CREATE TABLE | ❌ Tidak dikenali H2 |
| `ENUM('A','B','C')` | 5 file (V3,V4,V5,V8) | ✅ H2 MODE=MySQL mendukung ENUM |
| `TINYINT(1)` | V21 (3 occurrence) | ✅ H2 support |
| `DATETIME(6)` | V21,V24,V29,etc | ✅ H2 support |
| `COLLATE utf8mb4_unicode_ci` | V21,V24,V29,V43 | ❌ H2 tidak support collation |
| `DEFAULT CHARSET=utf8mb4` | V21,V24,V29,V43 | ❌ H2 tidak support |
| `COMMENT 'xxx'` | V43,V9,V44 | ❌ H2 tidak support column COMMENT |
| `ESCAPE '\\'` di LIKE | 25+ occurrence | ✅ H2 support ESCAPE |
| `ALTER TABLE ... MODIFY col` | V17 (12+ occurrence) | ❌ H2 gunakan `ALTER COLUMN` |
| `ADD COLUMN ... AFTER col` | V9,V20,V27,V30,V40,V44 | ❌ H2 tidak support AFTER |
| `ADD COLUMN IF NOT EXISTS` | V30,V40 | ❌ Parsial (H2 punya syntax berbeda) |
| `ADD UNIQUE KEY IF NOT EXISTS` | V40 | ❌ H2 tidak support |
| `INDEX idx_name (col)` inline | V29 | ❌ H2 gunakan CREATE INDEX terpisah |
| `NOW()` | Semua seed INSERT | ✅ H2 MODE=MySQL support |
| `REPLACE()` function | V24 | ✅ H2 support |

**Solusi: Migration Set Terpisah (`db/migration-e2e/`)**

Kita membuat copy dari 45 migration file yang sudah di-transpile ke H2-compatible syntax. Proses transpile dilakukan **sekali** secara manual/scripted, kemudian di-maintain bersama source migration.

**Script transpile** (`scripts/transpile-migrations-h2.sh`):

```bash
#!/bin/bash
# Transpile MariaDB migrations to H2-compatible syntax
# Run from project root: bash scripts/transpile-migrations-h2.sh

SRC="src/main/resources/db/migration"
DST="src/main/resources/db/migration-e2e"

rm -rf "$DST"
mkdir -p "$DST"

for f in "$SRC"/*.sql; do
  filename=$(basename "$f")
  sed \
    -e 's/) ENGINE=InnoDB[^;]*/)/g' \
    -e 's/ENGINE=InnoDB//g' \
    -e 's/DEFAULT CHARSET=[a-zA-Z0-9_]*//g' \
    -e 's/COLLATE [a-zA-Z0-9_]*//g' \
    -e "s/COMMENT '[^']*'//g" \
    -e 's/MODIFY \([a-zA-Z_]*\) /ALTER COLUMN \1 SET DATA TYPE /g' \
    -e 's/AFTER [a-zA-Z_]*//g' \
    -e 's/ADD UNIQUE KEY IF NOT EXISTS \([a-zA-Z_]*\) /ADD CONSTRAINT IF NOT EXISTS \1 UNIQUE /g' \
    -e 's/ADD COLUMN IF NOT EXISTS/ADD COLUMN IF NOT EXISTS/g' \
    -e 's/INDEX `\?\([a-zA-Z_]*\)`\? (\(`[^)]*`\))/-- INDEX \1 moved to CREATE INDEX below/g' \
    "$f" > "$DST/$filename"
done

echo "Transpiled $(ls "$DST" | wc -l) files to $DST"
```

> **Catatan penting:** Script ini adalah starting point. Beberapa migration (terutama V17 dengan banyak `ALTER TABLE ... MODIFY`) memerlukan review manual karena syntax `ALTER COLUMN ... SET DATA TYPE` di H2 memiliki batasan (tidak bisa ganti nullability sekaligus). Migration ini perlu dipecah menjadi beberapa statement `ALTER TABLE ... ALTER COLUMN ... SET NOT NULL` terpisah.

**Alternatif yang dipertimbangkan tapi ditolak:**

| Alternatif | Alasan Ditolak |
|---|---|
| `ddl-auto=create` tanpa Flyway | Tidak menjalankan seed data (INSERT) dari migration; schema mungkin berbeda dari production |
| Satu migration set dengan `IF H2` conditional | SQL standar tidak mendukung conditional per-engine; Flyway tidak punya built-in dialect switching |
| TestContainers + MariaDB | Menambah dependensi Docker di CI, melanggar "tanpa dependensi database eksternal" |

#### 3.4 MinIO Stub

MinIO digunakan untuk approval signatures — bukan untuk core CRUD flow. Untuk E2E, kita membuat stub:

```java
package com.solusi.erp.config;

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
            // Implement semua method dengan no-op / return dummy value
            // Contoh: upload → return "dummy-key"
            //         getPresignedUrl → return "http://localhost/stub"
            //         delete → no-op
        };
    }
}
```

Ini mencegah `MinioClient` mencoba koneksi ke MinIO server yang tidak ada, sekaligus meng-override bean `MinioConfiguration` tanpa mengubah kode production.

---

### 4. Data Seeding Strategy

#### 4.1 Layer Seeding

Data seeding terjadi di **dua layer** yang sudah terurut secara natural:

```
Layer 1: Flyway Migrations (schema + seed statis)
├── V1  → permissions, roles, role_permissions, users (admin), user_profiles
├── V2  → seed security data (password placeholder: INITIAL_PASSWORD_SETUP)
├── V5  → unit_of_measures (35 UoM: KG, PCS, BOX, etc.)
├── V6  → brands (schema + sequence) — BELUM ADA seed data
├── V11 → master_currencies (8 currency: IDR, USD, EUR, etc.)
├── V12 → geographics (provinsi/kota)
├── V15 → indonesia geographic data (38 provinsi + 514 kab/kota)
└── V19 → permission_groups (menu structure)

Layer 2: SystemInitializer (existing CommandLineRunner)
└── Replace INITIAL_PASSWORD_SETUP → BCrypt("admin123")

Layer 3: E2eDataSeeder (new CommandLineRunner, @Profile("e2e"), @Order(200))
├── Brand: "E2E Test Brand" (BRD-E2E1)
├── Product Category: "E2E Test Category" (PCAT-E2E1)  
├── Party: "PT E2E Supplier" (owner for Facility)
├── Facility: "Gudang E2E" + Grid "GRD-E2E-A" + Container "BIN-E2E-001"
├── Product: "Produk E2E" (depends: Category, Brand, UoM=PCS)
├── User flag: SET password_change_required = FALSE untuk admin
└── Currency: pastikan IDR is_default = TRUE
```

#### 4.2 E2eDataSeeder Implementation

```java
package com.solusi.erp.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("e2e")
@Order(200) // Setelah SystemInitializer (default order)
@RequiredArgsConstructor
public class E2eDataSeeder implements CommandLineRunner {

    private final JdbcTemplate jdbc;

    @Override
    public void run(String... args) {
        log.info("E2eDataSeeder: Seeding E2E test data...");

        // 1. Disable password change requirement untuk admin
        jdbc.update("""
            UPDATE users SET password_change_required = FALSE 
            WHERE username = 'admin'
        """);

        // 2. Brand
        seedIfNotExists("brands", "code", "BRD-E2E1", """
            INSERT INTO brands (code, name, note, created_by_user_id, created_date, version)
            VALUES ('BRD-E2E1', 'E2E Test Brand', 'Seeded by E2eDataSeeder', 1, NOW(), 1)
        """);

        // 3. Product Category
        seedIfNotExists("product_categories", "code", "PCAT-E2E1", """
            INSERT INTO product_categories (code, name, type, note, created_by_user_id, created_date, version)
            VALUES ('PCAT-E2E1', 'E2E Test Category', 'STOCK', 'Seeded', 1, NOW(), 1)
        """);

        // 4. Party (untuk Facility owner)
        seedIfNotExists("parties", "code", "PTY-E2E1", """
            INSERT INTO parties (code, name, type, created_by_user_id, created_date, version)
            VALUES ('PTY-E2E1', 'PT E2E Supplier', 'ORGANIZATION', 1, NOW(), 1)
        """);

        // 5. Facility → Grid → Container (dependency chain)
        seedIfNotExists("inv_facilities", "code", "FAC-E2E1", """
            INSERT INTO inv_facilities (code, name, owner_id, is_active, created_by_user_id, created_date, version)
            VALUES ('FAC-E2E1', 'Gudang E2E', 
                    (SELECT id FROM parties WHERE code = 'PTY-E2E1'), 
                    1, 1, NOW(), 1)
        """);

        seedIfNotExists("inv_grids", "code", "GRD-E2E-A", """
            INSERT INTO inv_grids (facility_id, code, name, is_active, created_by_user_id, created_date, version)
            VALUES ((SELECT id FROM inv_facilities WHERE code = 'FAC-E2E1'),
                    'GRD-E2E-A', 'Grid E2E Alpha', 1, 1, NOW(), 1)
        """);

        seedIfNotExists("inv_containers", "code", "BIN-E2E-001", """
            INSERT INTO inv_containers (grid_id, code, name, is_active, created_by_user_id, created_date, version)
            VALUES ((SELECT id FROM inv_grids WHERE code = 'GRD-E2E-A'),
                    'BIN-E2E-001', 'Container E2E 001', 1, 1, NOW(), 1)
        """);

        // 6. Product (depends: Category, Brand, UoM)
        seedIfNotExists("products", "code", "PRD-E2E1", """
            INSERT INTO products (code, name, category_id, uom_id, brand_id, is_active, is_serialized,
                                  created_by_user_id, created_date, version)
            VALUES ('PRD-E2E1', 'Produk E2E Test',
                    (SELECT id FROM product_categories WHERE code = 'PCAT-E2E1'),
                    (SELECT id FROM unit_of_measures WHERE code = 'PCS'),
                    (SELECT id FROM brands WHERE code = 'BRD-E2E1'),
                    TRUE, FALSE, 1, NOW(), 1)
        """);

        log.info("E2eDataSeeder: Complete.");
    }

    private void seedIfNotExists(String table, String column, String value, String insertSql) {
        Integer count = jdbc.queryForObject(
            "SELECT COUNT(*) FROM " + table + " WHERE " + column + " = ?",
            Integer.class, value
        );
        if (count != null && count == 0) {
            jdbc.execute(insertSql);
            log.info("  Seeded {} where {} = {}", table, column, value);
        }
    }
}
```

#### 4.3 Dependency Order

```
permissions ← roles ← role_permissions ← users ← user_profiles
                                              ↑
                                         (admin user id=1)
                                              │
unit_of_measures (V5, 35 records)             │
master_currencies (V11, 8 records)            │
geographics (V12+V15)                         │
                                              ▼
brands ──────────┐                    parties ─────────┐
product_categories┤                                     │
                  ▼                                     ▼
              products                          inv_facilities
                                                      │
                                                inv_grids
                                                      │
                                                inv_containers
                                                      │
                                               (Stock Adjustment
                                                depends on all above)
```

#### 4.4 Idempotency

- `seedIfNotExists()` memeriksa keberadaan record berdasarkan `code` sebelum INSERT
- Semua seeded data menggunakan prefix `E2E` pada code untuk menghindari collision dengan data dari Flyway migration
- Jika E2E server di-restart tanpa wipe DB (H2 in-memory akan wipe otomatis), seeder tidak akan duplikasi data

#### 4.5 Password Change Required = FALSE

Critical: `SystemInitializer` men-set `password_change_required = TRUE` untuk admin. `ForcePasswordChangeFilter` akan redirect ke `/reset-password` setiap request. E2eDataSeeder **WAJIB** menset flag ini ke `FALSE` agar Playwright test bisa langsung navigate setelah login tanpa harus handle password reset flow.

---

### 5. Playwright Test Architecture

#### 5.1 Struktur Folder

```
e2e/                              ← Subdirectory terpisah (bukan root repo)
├── package.json
├── playwright.config.ts
├── tsconfig.json
├── .gitignore                    ← node_modules/
│
├── helpers/
│   ├── auth.ts                   ← Login, session management
│   ├── tom-select.ts             ← selectTomSelect(), setTomSelectValue()
│   ├── flatpickr.ts              ← setFlatpickrDate()
│   ├── autonumeric.ts            ← setAutoNumeric()
│   ├── ajax-form.ts              ← submitAndWait(), verifySuccess()
│   ├── line-item.ts              ← addLineItem(), removeLineItem()
│   ├── navigation.ts             ← gotoModule(), waitForPageLoad()
│   └── assertions.ts             ← assertToastSuccess(), assertListContains()
│
├── fixtures/
│   └── erp-test.ts               ← Custom test fixture dengan auto-login
│
├── tests/
│   ├── auth/
│   │   └── login.spec.ts
│   ├── inventory/
│   │   ├── brand.spec.ts
│   │   ├── product-category.spec.ts
│   │   ├── uom.spec.ts
│   │   ├── product.spec.ts
│   │   ├── facility.spec.ts
│   │   └── stock-adjustment.spec.ts
│   ├── master/
│   │   ├── tax.spec.ts
│   │   ├── currency.spec.ts
│   │   ├── geographic.spec.ts
│   │   └── party.spec.ts
│   ├── security/
│   │   ├── user.spec.ts
│   │   └── role.spec.ts
│   └── accounting/
│       ├── coa.spec.ts
│       └── period.spec.ts
│
└── playwright-report/            ← Generated, gitignored
```

#### 5.2 package.json

```json
{
  "name": "solusi-erp-e2e",
  "private": true,
  "scripts": {
    "test": "playwright test",
    "test:headed": "playwright test --headed",
    "test:ui": "playwright test --ui",
    "report": "playwright show-report"
  },
  "devDependencies": {
    "@playwright/test": "^1.52.0"
  }
}
```

#### 5.3 playwright.config.ts

```typescript
import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './tests',
  timeout: 60_000,
  expect: { timeout: 10_000 },
  retries: process.env.CI ? 1 : 0,
  workers: 1,  // Sequential — shared H2 DB state, dependency antar modul
  
  reporter: [
    ['list'],
    ['html', { open: 'never' }],
    ...(process.env.CI ? [['github'] as const] : []),
  ],

  use: {
    baseURL: process.env.BASE_URL || 'http://localhost:18080',
    screenshot: 'only-on-failure',
    trace: 'retain-on-failure',
    video: 'retain-on-failure',
    locale: 'id-ID',
    timezoneId: 'Asia/Jakarta',
  },

  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
  ],

  // Ordering: test files run in alphabetical order within workers:1
  // Naming convention: 01-login, 02-brand, etc.
});
```

> **`workers: 1` adalah kunci.** Karena semua test berbagi satu H2 in-memory database dan satu session state, parallelism akan menyebabkan race condition. Trade-off: total runtime lebih lama (~3-5 menit), tapi deterministic.

#### 5.4 Custom Test Fixture dengan Auto-Login

```typescript
// fixtures/erp-test.ts
import { test as base, expect, Page } from '@playwright/test';

const BASE_URL = process.env.BASE_URL || 'http://localhost:18080';
const ADMIN = { username: 'admin', password: 'admin123' };

type ErpFixtures = {
  authedPage: Page;
};

export const test = base.extend<ErpFixtures>({
  authedPage: async ({ page }, use) => {
    // Login
    await page.goto(`${BASE_URL}/login`);
    await page.fill('input[name="username"]', ADMIN.username);
    await page.fill('input[name="password"]', ADMIN.password);
    await page.click('button[type="submit"]');
    
    // Tunggu redirect ke dashboard atau halaman lain (bukan /login)
    await expect(page).not.toHaveURL(/\/login/);
    
    await use(page);
  },
});

export { expect };
```

#### 5.5 Helper Functions

**helpers/tom-select.ts**

```typescript
import { Page } from '@playwright/test';

/**
 * Pilih opsi TomSelect via JS API.
 * Cara PALING RELIABLE — bypass DOM interaction complexity.
 */
export async function selectTomSelect(
  page: Page,
  selector: string,
  searchQuery = '',
  optionIndex = 0
): Promise<{ id: string; name: string } | { error: string }> {
  // Tunggu TomSelect ter-initialize
  await page.waitForFunction(
    (sel) => {
      const el = document.querySelector(sel) as any;
      return el && el.tomselect;
    },
    selector,
    { timeout: 5000 }
  );

  return await page.evaluate(
    ({ sel, query, idx }) => {
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
          options.forEach((opt) => ts.addOption(opt));
          const pick = options[Math.min(idx, options.length - 1)];
          ts.setValue(pick.id);
          resolve({ id: pick.id, name: pick.name });
        });
      });
    },
    { sel: selector, query: searchQuery, idx: optionIndex }
  );
}

export async function setTomSelectValue(
  page: Page,
  selector: string,
  valueId: string | number
): Promise<void> {
  await page.evaluate(
    ({ sel, val }) => {
      return new Promise<void>((resolve) => {
        const el = document.querySelector(sel) as any;
        const ts = el.tomselect;
        ts.load('', (options: any[]) => {
          options.forEach((opt) => ts.addOption(opt));
          ts.setValue(String(val));
          resolve();
        });
      });
    },
    { sel: selector, val: valueId }
  );
}
```

**helpers/flatpickr.ts**

```typescript
import { Page } from '@playwright/test';

export async function setFlatpickrDate(
  page: Page,
  selector: string,
  dateStr: string
): Promise<void> {
  await page.evaluate(
    ({ sel, date }) => {
      const input = document.querySelector(sel) as any;
      if (input?._flatpickr) {
        input._flatpickr.setDate(date, true);
      } else if (input) {
        input.value = date;
        input.dispatchEvent(new Event('change', { bubbles: true }));
      }
    },
    { sel: selector, date: dateStr }
  );
}
```

**helpers/autonumeric.ts**

```typescript
import { Page } from '@playwright/test';

export async function setAutoNumeric(
  page: Page,
  selector: string,
  value: number
): Promise<void> {
  await page.evaluate(
    ({ sel, val }) => {
      const el = document.querySelector(sel) as any;
      if (typeof AutoNumeric !== 'undefined') {
        const instance = (AutoNumeric as any).getAutoNumericElement(el);
        if (instance) {
          instance.set(val);
          return;
        }
      }
      el.value = val;
      el.dispatchEvent(new Event('input', { bubbles: true }));
    },
    { sel: selector, val: value }
  );
}
```

**helpers/ajax-form.ts**

```typescript
import { Page, expect } from '@playwright/test';

/**
 * Submit form AJAX dan tunggu response.
 * Form ERP menggunakan data-ajax-form="true" → POST JSON response.
 */
export async function submitAndWait(
  page: Page,
  expectedRedirectPath?: string
): Promise<{ success: boolean; message?: string; errors?: any }> {
  const responsePromise = page.waitForResponse(
    (resp) =>
      resp.request().method() === 'POST' &&
      (resp.headers()['content-type']?.includes('json') ?? false),
    { timeout: 15_000 }
  );

  await page.click('button[type="submit"]');
  const response = await responsePromise;
  const body = await response.json();

  if (body.success && expectedRedirectPath) {
    await page.waitForURL(`**${expectedRedirectPath}**`, { timeout: 5000 });
  }

  return body;
}

export async function assertSubmitSuccess(
  page: Page,
  expectedRedirectPath: string
): Promise<void> {
  const result = await submitAndWait(page, expectedRedirectPath);
  expect(result.success).toBeTruthy();
}
```

#### 5.6 Strategi Selector yang Stabil

Mengikuti best practice Playwright dan realita codebase:

| Prioritas | Strategi | Contoh | Alasan |
|---|---|---|---|
| 1 | `name` attribute | `input[name="name"]` | Stabil — terikat ke DTO binding |
| 2 | `id` attribute | `#category-select` | Stabil — dipakai TomSelect init |
| 3 | `data-testid` (tambahkan gradual) | `[data-testid="btn-save"]` | Best practice, tapi butuh perubahan template |
| 4 | Role-based | `button[type="submit"]` | Aksesibel, stabil |
| 5 | Text content | `button:has-text("Simpan")` | Brittle jika i18n berubah, tapi practical |

**Rekomendasi:** Mulai dengan selector #1–#4. Tambahkan `data-testid` secara gradual pada elemen yang sering berubah.

#### 5.7 Contoh Test Spec: Brand CRUD

```typescript
// tests/inventory/brand.spec.ts
import { test, expect } from '../../fixtures/erp-test';
import { submitAndWait } from '../../helpers/ajax-form';

const MODULE_URL = '/inventory/brands';

test.describe('Brand CRUD', () => {
  test('create new brand', async ({ authedPage: page }) => {
    await page.goto(`${MODULE_URL}/create`);
    await page.waitForLoadState('networkidle');

    await page.fill('input[name="name"]', 'Playwright Brand Test');
    await page.fill('input[name="note"]', 'Created by E2E test');

    const result = await submitAndWait(page, MODULE_URL);
    expect(result.success).toBeTruthy();

    // Verify di list page
    await expect(page.locator('table')).toContainText('Playwright Brand Test');
  });

  test('edit existing brand', async ({ authedPage: page }) => {
    await page.goto(MODULE_URL);
    await page.waitForLoadState('networkidle');

    // Klik edit pada brand yang baru dibuat
    const row = page.locator('tr', { hasText: 'Playwright Brand Test' });
    await row.locator('a[href*="edit"]').click();
    await page.waitForLoadState('networkidle');

    await page.fill('input[name="name"]', 'Playwright Brand Updated');
    
    const result = await submitAndWait(page, MODULE_URL);
    expect(result.success).toBeTruthy();

    await expect(page.locator('table')).toContainText('Playwright Brand Updated');
  });
});
```

---

### 6. CI/CD Integration

#### 6.1 GitHub Actions Workflow

File: `.github/workflows/e2e-playwright.yml`

```yaml
name: E2E - Playwright

on:
  push:
    branches: [main, master]
  pull_request:
    branches: [main, master]
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
      # ── Setup ──────────────────────────────
      - name: Checkout
        uses: actions/checkout@v4

      - name: Set up JDK ${{ env.JAVA_VERSION }}
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: ${{ env.JAVA_VERSION }}
          cache: maven

      - name: Set up Node.js ${{ env.NODE_VERSION }}
        uses: actions/setup-node@v4
        with:
          node-version: ${{ env.NODE_VERSION }}

      # ── Build Spring Boot JAR ──────────────
      - name: Build application JAR
        run: |
          chmod +x mvnw
          ./mvnw -B clean package -DskipTests -q

      # ── Start Spring Boot with E2E profile ─
      - name: Start Spring Boot (e2e profile)
        run: |
          java -jar target/solusi-program-erp-*.jar \
            --spring.profiles.active=e2e \
            --server.port=18080 &
          echo $! > spring-boot.pid

      - name: Wait for server readiness
        run: |
          echo "Waiting for Spring Boot to start..."
          for i in $(seq 1 60); do
            if curl -s -o /dev/null -w "%{http_code}" http://localhost:18080/login | grep -q "200\|302"; then
              echo "Server ready after ${i}s"
              exit 0
            fi
            sleep 2
          done
          echo "Server failed to start within 120s"
          cat logs/erp.log || true
          exit 1

      # ── Playwright Setup & Run ─────────────
      - name: Install Playwright dependencies
        working-directory: e2e
        run: |
          npm ci
          npx playwright install --with-deps chromium

      - name: Run Playwright tests
        working-directory: e2e
        env:
          BASE_URL: http://localhost:18080
          CI: true
        run: npx playwright test

      # ── Cleanup & Artifacts ────────────────
      - name: Stop Spring Boot
        if: always()
        run: |
          if [ -f spring-boot.pid ]; then
            kill $(cat spring-boot.pid) || true
          fi

      - name: Upload Playwright report
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: playwright-report
          path: e2e/playwright-report/
          retention-days: 14

      - name: Upload test traces
        if: failure()
        uses: actions/upload-artifact@v4
        with:
          name: playwright-traces
          path: e2e/test-results/
          retention-days: 7

      - name: Upload Spring Boot logs
        if: failure()
        uses: actions/upload-artifact@v4
        with:
          name: spring-boot-logs
          path: logs/
          retention-days: 7
```

#### 6.2 Server Startup Strategy

1. **Build JAR** (`./mvnw clean package -DskipTests`) — ~60 detik dengan cache
2. **Start as background process** (`java -jar ... &`) — bukan `spring-boot:run` (lebih cepat, tidak perlu Maven overhead)
3. **Health check loop** — Poll `GET /login` setiap 2 detik, max 120 detik. Status 200 atau 302 = ready.
4. **Graceful shutdown** — `kill $(cat spring-boot.pid)` di step `if: always()`

#### 6.3 Parallelism Strategy

- **Test level:** `workers: 1` (sequential) — mandatory karena shared H2 state
- **CI level:** E2E job berjalan **parallel** dengan existing `fast-tests` job, bukan sequentially. Kedua job independent.
- **Browser:** Hanya Chromium — menambah Firefox/Safari memberikan diminishing returns untuk SSR app

#### 6.4 Artifact Collection

| Artifact | Kondisi | Retensi |
|---|---|---|
| HTML Report | Selalu | 14 hari |
| Traces (`.zip`) | Hanya saat fail | 7 hari |
| Screenshots | Hanya saat fail | Di dalam traces |
| Video | Hanya saat fail | Di dalam traces |
| Spring Boot logs | Hanya saat fail | 7 hari |

---

### 7. Cakupan Test Minimum per Modul

**Prinsip "Definition of Done":** Setiap modul harus punya minimal **1 create test** dan **1 update test** yang memverifikasi happy path end-to-end (form load → fill data → submit AJAX → redirect ke list → data muncul di list).

| # | Modul | Operasi Wajib di-Test | Kompleksitas UI | Prioritas |
|---|---|---|---|---|
| 1 | **Login** | Login → dashboard visible | Rendah (standard form) | P0 |
| 2 | **Brand** | Create, Update | Rendah (text fields only) | P0 |
| 3 | **Product Category** | Create, Update | Rendah (text + ENUM select) | P0 |
| 4 | **UoM** | Create, Update | Rendah (text + ENUM select) | P1 |
| 5 | **Tax** | Create, Update | Rendah (text + decimal) | P1 |
| 6 | **Currency** | Create, Update | Rendah (text + boolean) | P1 |
| 7 | **Product** | Create, Update | **Tinggi** (TomSelect: category, brand) | P0 |
| 8 | **Facility** | Create, Update | Sedang (TomSelect: owner/party) | P1 |
| 9 | **Party** | Create, Update | **Tinggi** (multi-section: contact, address, ID) | P1 |
| 10 | **Geographic** | Create | Rendah (text + parent select) | P2 |
| 11 | **Stock Adjustment** | Create (header + line items), Process/Approve | **Sangat Tinggi** (TomSelect cascade, drawer qty, AutoNumeric, Flatpickr) | P0 |
| 12 | **User** | Create, Update | Sedang (TomSelect: role) | P1 |
| 13 | **Role** | Create, Update | Sedang (checkbox matrix) | P2 |
| 14 | **COA** | Create, Update | Sedang (parent TomSelect, ENUM) | P2 |
| 15 | **Accounting Period** | Create | Sedang (Flatpickr date range) | P2 |

**Minimum Viable Test Suite (P0 saja):**

| Test File | Test Cases | Est. Duration |
|---|---|---|
| `login.spec.ts` | 1: login → dashboard | ~5s |
| `brand.spec.ts` | 2: create + update | ~10s |
| `product-category.spec.ts` | 2: create + update | ~10s |
| `product.spec.ts` | 2: create + update (with TomSelect) | ~15s |
| `stock-adjustment.spec.ts` | 2: create with line items + verify | ~30s |

**Total P0: 9 test cases, ~70 detik runtime.**

Ini sudah memberikan confidence bahwa:
- ✅ Form rendering tidak error (Thymeleaf fragment intact)
- ✅ TomSelect, Flatpickr, AutoNumeric berfungsi
- ✅ AJAX form submission bekerja (CSRF token, validation, redirect)
- ✅ Line item management (add, fill, cascade) berfungsi
- ✅ Security (login, session) bekerja

---

### 8. Risiko & Mitigasi

#### Risiko 1: H2 Compatibility Drift

**Deskripsi:** Setiap kali developer menambah migration baru dengan MariaDB-specific syntax, versi H2-compatible di `db/migration-e2e/` bisa tertinggal.

**Dampak:** E2E test gagal di CI karena migration error, padahal production code benar.

**Mitigasi:**
- CI job yang berjalan otomatis akan mendeteksi drift segera (fail fast).
- Tambahkan check di CI: bandingkan jumlah file di `db/migration/` vs `db/migration-e2e/`. Jika tidak sama, fail dengan pesan jelas.
- Dokumentasikan di CONTRIBUTING.md: "Setiap migration baru WAJIB disertai versi H2-compatible."
- Script `transpile-migrations-h2.sh` bisa dijalankan ulang sebagai starting point, lalu review manual.

```yaml
# Tambahkan step di CI sebelum start server:
- name: Verify migration parity
  run: |
    PROD_COUNT=$(ls src/main/resources/db/migration/*.sql | wc -l)
    E2E_COUNT=$(ls src/main/resources/db/migration-e2e/*.sql | wc -l)
    if [ "$PROD_COUNT" != "$E2E_COUNT" ]; then
      echo "::error::Migration count mismatch! Production: $PROD_COUNT, E2E: $E2E_COUNT"
      echo "Run: bash scripts/transpile-migrations-h2.sh"
      exit 1
    fi
```

#### Risiko 2: Flaky Test akibat Timing

**Deskripsi:** TomSelect, Flatpickr, AutoNumeric di-initialize via deferred JS. Jika Playwright berinteraksi sebelum init selesai, test gagal sporadis.

**Dampak:** False negative di CI, developer kehilangan kepercayaan pada E2E suite.

**Mitigasi:**
- Semua helper function sudah include `waitForFunction()` yang menunggu widget ter-initialize sebelum interaksi.
- `networkidle` digunakan setelah navigasi halaman.
- Retry `retries: 1` di CI sebagai safety net.
- Jika flakiness persistent pada komponen tertentu, tambahkan explicit wait: `page.waitForFunction(() => el.tomselect, { timeout: 5000 })`.

#### Risiko 3: MinIO Dependency di Approval Flow

**Deskripsi:** Approval flow (submit, approve, reject) memerlukan upload tanda tangan ke MinIO. Tanpa MinIO, approval test akan gagal.

**Dampak:** Stock Adjustment "Process/Approve" test tidak bisa berjalan di E2E.

**Mitigasi:**
- `E2eStorageConfig` menyediakan no-op `StorageProvider` yang return dummy values.
- Test approval flow hanya sampai "submit for approval" — tidak sampai upload signature.
- Jika tim ingin full approval E2E di masa depan, pertimbangkan MinIO testcontainer atau mock file upload endpoint.

#### Risiko 4: V17 Migration Complexity di H2

**Deskripsi:** V17 melakukan `ALTER TABLE ... MODIFY` di 8+ tabel untuk mengubah column type dan nullability sekaligus. H2 tidak mendukung `MODIFY` — perlu dipecah menjadi beberapa statement `ALTER COLUMN ... SET DATA TYPE` + `ALTER COLUMN ... SET NOT NULL`.

**Dampak:** Transpile V17 ke H2 bisa error jika tidak dilakukan dengan hati-hati.

**Mitigasi:**
- V17 di `migration-e2e/` di-rewrite manual (bukan automated transpile).
- Setiap `ALTER TABLE t MODIFY col TYPE NOT NULL` dipecah menjadi:
  ```sql
  ALTER TABLE t ALTER COLUMN col SET DATA TYPE BIGINT;
  ALTER TABLE t ALTER COLUMN col SET NOT NULL;
  ```
- Test migration di local H2 console sebelum commit.

#### Risiko 5: CSRF Token dan Session State

**Deskripsi:** Spring Security CSRF token di-embed di form sebagai hidden input. Jika Playwright mengakses form tanpa session yang valid, submit akan gagal 403.

**Dampak:** Semua form submission test gagal.

**Mitigasi:**
- Custom fixture `authedPage` melakukan login di `beforeEach`, memastikan session valid.
- Playwright secara otomatis maintain cookies (termasuk JSESSIONID) per browser context.
- Helper `submitAndWait()` sudah handle response checking.

---

### 9. Effort Estimate

| Komponen | Detail | Estimasi |
|---|---|---|
| **1. H2 Migration Transpile** | Transpile 45 file, manual fix V17/V9/V13 | 3–4 hari |
| **2. Spring Boot E2E Profile** | `application-e2e.yaml`, `E2eStorageConfig`, `E2eDataSeeder`, pom.xml H2 dep | 1–2 hari |
| **3. Playwright Setup** | `e2e/` folder, `package.json`, `playwright.config.ts`, `tsconfig.json` | 0.5 hari |
| **4. Helper Functions** | 8 helper files (auth, TomSelect, Flatpickr, AutoNumeric, AJAX form, line item, navigation, assertions) | 2–3 hari |
| **5. P0 Test Suite** | Login + Brand + Product Category + Product + Stock Adjustment (9 test cases) | 3–4 hari |
| **6. P1 Test Suite** | UoM + Tax + Currency + Facility + Party + User (12 test cases) | 3–4 hari |
| **7. CI/CD Workflow** | GitHub Actions workflow, health check, artifact upload | 1 hari |
| **8. Documentation** | README di `e2e/`, update CONTRIBUTING.md, migration guide | 1 hari |
| **Total P0 (MVP)** | Komponen 1–5, 7–8 | **~2 minggu** |
| **Total P0 + P1** | Semua komponen | **~3 minggu** |

> Estimasi berdasarkan 1 developer full-time. Jika 2 developer parallel (satu fokus Java/Spring, satu fokus Playwright/Node), timeline bisa dicompress ~40%.

---

### 10. Trade-offs

#### Yang Dikorbankan

| Trade-off | Dampak | Justifikasi |
|---|---|---|
| **Migration duplikasi** (`db/migration/` + `db/migration-e2e/`) | Maintenance overhead: setiap migration baru harus di-transpile | Menghindari perubahan migration production yang sudah proven di MariaDB. Transpile bisa semi-automated. |
| **Sequential test execution** (`workers: 1`) | Runtime lebih lama (~3-5 menit vs ~1 menit parallel) | Shared H2 state membuat parallelism unreliable. 5 menit masih acceptable untuk CI. |
| **Hanya Chromium** | Tidak mendeteksi bug Firefox/Safari-specific | Untuk SSR app, browser compatibility issues sangat jarang. Effort menambah browser tidak sepadan. |
| **No approval flow E2E** | Gap pada alur bisnis kritis (approve/reject) | MinIO stub membuat upload signature tidak possible. Bisa ditambahkan nanti dengan fake upload endpoint. |
| **H2 behavioral differences** | Beberapa edge case H2 vs MariaDB: collation, case sensitivity, strict mode | `MODE=MySQL` + `CASE_INSENSITIVE_IDENTIFIERS=TRUE` meminimalkan gap. E2E test fokus pada UI behavior, bukan SQL edge case. |

#### Alternatif yang Tidak Dipilih

| Alternatif | Mengapa Tidak Dipilih |
|---|---|
| **TestContainers + MariaDB** | Menambah dependensi Docker di CI. Kontradiksi dengan requirement "tanpa dependensi database eksternal". Lebih lambat (startup MariaDB container ~10-15 detik). |
| **Cypress** | Tidak se-reliable Playwright untuk `page.evaluate()` (dibutuhkan TomSelect/Flatpickr/AutoNumeric API calls). Playwright lebih baik untuk SSR app. |
| **Selenium** | API lebih verbose, community momentum menurun. Playwright memiliki auto-wait, trace, dan fixture system yang superior. |
| **`@SpringBootTest` + HtmlUnit** | Tidak menjalankan JavaScript — TomSelect, Flatpickr, AutoNumeric tidak berfungsi. Bukan true E2E test. |
| **Satu migration set untuk semua (conditional SQL)** | SQL standar tidak mendukung conditional per-database. Flyway tidak punya built-in dialect switching. Akan mencemari migration production. |
| **`ddl-auto=create` (Hibernate auto-generate)** | Tidak menjalankan seed data dari migration INSERT. Schema mungkin subtly berbeda dari Flyway-generated schema (index names, constraint names, column order). Kehilangan validasi bahwa migration script benar. |
| **Shared browser context (satu login untuk semua test)** | Lebih cepat, tapi satu test fail bisa cascade ke semua test berikutnya. Isolasi per-test lebih reliable. |
