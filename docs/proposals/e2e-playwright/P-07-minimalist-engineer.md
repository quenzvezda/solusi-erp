# Proposal Teknis: Playwright E2E Test — Minimalist Approach

> **Persona: Minimalist Engineer** — fewest moving parts, prove the concept first.

---

### 1. Executive Summary

Proyek ini membutuhkan E2E test untuk mendeteksi regresi UI yang tidak tertangkap oleh unit test. Pendekatan kami: **paling sedikit file, paling sedikit konfigurasi, paling cepat memberikan value.**

Kunci keputusan arsitektur: **`ddl-auto=create` menggantikan Flyway di profile E2E.** Hibernate membuat schema langsung dari JPA entity, lalu satu file `e2e-seed.sql` menyuntikkan data. Ini mengeliminasi kebutuhan mentranspile 45 migration file MariaDB ke H2 — penghematan terbesar dari sisi maintenance.

Di sisi Playwright: 4 spec file (login, brand, product, stock-adjustment) sudah mencakup seluruh spektrum kompleksitas UI — dari form sederhana hingga TomSelect cascade + line items + drawer. Semua helper digabung dalam **satu file** `helpers.ts`. Total file baru di repository: **~12 file.**

---

### 2. Arsitektur Solusi

```
┌─────────────────────────────────────────────────────┐
│                  CI Runner (GitHub Actions)          │
│                                                     │
│  ┌──────────────────────┐   ┌────────────────────┐  │
│  │  Spring Boot JAR     │   │  e2e/              │  │
│  │  --profile=e2e       │   │  @playwright/test  │  │
│  │                      │   │                    │  │
│  │  ┌────────────────┐  │   │  4 spec files      │  │
│  │  │ H2 in-memory   │  │   │  1 helpers.ts      │  │
│  │  │ ddl-auto=create│  │◄──│  1 config file     │  │
│  │  └────────────────┘  │   │                    │  │
│  │  ┌────────────────┐  │   └────────────────────┘  │
│  │  │ e2e-seed.sql   │  │          │                │
│  │  │ (satu file)    │  │    HTTP :18080            │
│  │  └────────────────┘  │                           │
│  └──────────────────────┘                           │
└─────────────────────────────────────────────────────┘
```

**Alur eksekusi:**

1. `./mvnw -B clean package -DskipTests -q` → JAR
2. `java -jar app.jar --spring.profiles.active=e2e` → Hibernate `ddl-auto=create` membuat schema dari entity → `e2e-seed.sql` insert data → `SystemInitializer` BCrypt admin password → `E2eBootstrap` set `password_change_required=FALSE`
3. Health check poll `GET /login` sampai 200/302
4. `npx playwright test` dari `e2e/`
5. Collect report + trace on failure

**Mengapa sesederhana ini cukup:**

- Entity JPA ADALAH source of truth untuk schema Java app. Tidak perlu Flyway di test.
- Satu `e2e-seed.sql` menggantikan 45 file migration yang harus di-transpile.
- Playwright berkomunikasi via HTTP — persis seperti user.
- 4 spec file sudah menjangkau setiap jenis komponen UI yang ada.

---

### 3. Spring Boot E2E Profile Design

#### 3.1 Dependency baru di `pom.xml`

```xml
<!-- H2 for E2E profile — schema dari Hibernate, bukan Flyway -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>
```

Hanya 1 dependency. `runtime` scope agar tersedia saat JAR dijalankan.

#### 3.2 File: `src/main/resources/application-e2e.yaml`

```yaml
server:
  port: 18080
  servlet:
    session:
      cookie:
        secure: false

spring:
  datasource:
    url: jdbc:h2:mem:erp_e2e;DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;NON_KEYWORDS=VALUE
    driver-class-name: org.h2.Driver
    username: sa
    password:

  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: create          # Hibernate buat schema dari @Entity
    defer-datasource-initialization: true  # e2e-seed.sql jalan SETELAH DDL
    show-sql: false

  flyway:
    enabled: false              # Tidak perlu Flyway — Hibernate handle schema

  sql:
    init:
      mode: always
      data-locations: classpath:db/e2e-seed.sql

  thymeleaf:
    cache: false

  h2:
    console:
      enabled: false            # Tidak perlu console

logging:
  level:
    com.solusi.erp: INFO
    org.hibernate.SQL: OFF
    org.springframework.security: WARN

minio:
  endpoint: http://localhost:19000
  presigned-endpoint: http://localhost:19000
  access-key: stub
  secret-key: stub
  buckets:
    signatures: stub-bucket
```

**Keputusan kunci:**

| Keputusan | Alasan |
|---|---|
| `ddl-auto=create` | Schema dari entity, selalu sinkron dengan kode Java. Nol file migration tambahan. |
| `flyway.enabled=false` | Flyway migration pakai syntax MariaDB (ENGINE=InnoDB, COLLATE, MODIFY). Mentranspile 45 file = effort besar + maintenance burden permanen. |
| `defer-datasource-initialization=true` | Memastikan `e2e-seed.sql` dieksekusi SETELAH Hibernate membuat tabel. |
| `MODE=MySQL` + flags | Kompatibilitas maksimal H2 dengan behavior MySQL/MariaDB. |

#### 3.3 MinIO Stub

Satu class kecil menggantikan MinIO untuk profile e2e:

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
    public StorageProvider stubStorageProvider() {
        return new StorageProvider() {
            @Override public void store(String b, String k, byte[] d, String ct) { }
            @Override public String getUrl(String b, String k) { return "http://stub/" + k; }
            @Override public String getPresignedUrl(String b, String k, int exp) { return "http://stub/" + k; }
            @Override public byte[] getBytes(String b, String k) { return new byte[0]; }
            @Override public void delete(String b, String k) { }
        };
    }
}
```

#### 3.4 E2eBootstrap — disable password change

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
@Order(200)  // Setelah SystemInitializer
@RequiredArgsConstructor
public class E2eBootstrap implements CommandLineRunner {

    private final JdbcTemplate jdbc;

    @Override
    public void run(String... args) {
        jdbc.update("UPDATE users SET password_change_required = FALSE WHERE username = 'admin'");
        log.info("E2eBootstrap: admin password_change_required disabled");
    }
}
```

Itu saja. Tidak ada logic seeding di Java — semua data ada di `e2e-seed.sql`.

---

### 4. Data Seeding Strategy

#### 4.1 Satu File: `src/main/resources/db/e2e-seed.sql`

File ini berisi **semua INSERT** yang dibutuhkan agar aplikasi bisa berjalan. Data dikonsolidasi dari V1–V45 migration ke satu file flat, tanpa DDL (schema dari Hibernate).

**Urutan eksekusi:**
```
Hibernate ddl-auto=create → semua tabel terbentuk dari @Entity
       ↓
e2e-seed.sql → semua INSERT data
       ↓
SystemInitializer (existing) → BCrypt admin password
       ↓
E2eBootstrap → password_change_required = FALSE
```

#### 4.2 Isi e2e-seed.sql (konsolidasi)

```sql
-- ============================================================
-- E2E Seed Data — Konsolidasi dari V1-V45 Flyway Migrations
-- HANYA INSERT — schema dibuat oleh Hibernate ddl-auto=create
-- ============================================================

-- ── 1. Security: Permissions ──────────────────────────────
-- Dari V2, V3, V5, V6, V7, V8, V9, V10, V11, V12, V14, V16,
--      V19, V22, V24, V25, V31, V32, V35, V36, V41, V42, V43
INSERT INTO permissions (id, name, description, created_by_user_id, created_date, version) VALUES
(1,  'DASHBOARD_READ',           'Akses Dashboard',                    1, NOW(), 1),
(2,  'USERS_READ',               'Melihat Daftar Pengguna',            1, NOW(), 1),
(3,  'USERS_CREATE',             'Menambah Pengguna Baru',             1, NOW(), 1),
(4,  'USERS_UPDATE',             'Mengubah Data Pengguna',             1, NOW(), 1),
(5,  'USERS_DELETE',             'Menghapus Pengguna',                 1, NOW(), 1),
(6,  'ROLES_READ',               'Melihat Daftar Role',                1, NOW(), 1),
(7,  'ROLES_CREATE',             'Menambah Role',                      1, NOW(), 1),
(8,  'ROLES_UPDATE',             'Mengubah Role',                      1, NOW(), 1),
(9,  'ROLES_DELETE',             'Menghapus Role',                     1, NOW(), 1),
(10, 'PERMISSIONS_READ',         'Melihat Permission',                 1, NOW(), 1),
(11, 'PERMISSIONS_CREATE',       'Menambah Permission',                1, NOW(), 1),
(12, 'PERMISSIONS_UPDATE',       'Mengubah Permission',                1, NOW(), 1),
(13, 'PERMISSIONS_DELETE',       'Menghapus Permission',               1, NOW(), 1),
-- Inventory
(14, 'PRODUCT-CATEGORY_READ',    'Melihat Kategori Produk',            1, NOW(), 1),
(15, 'PRODUCT-CATEGORY_CREATE',  'Menambah Kategori Produk',           1, NOW(), 1),
(16, 'PRODUCT-CATEGORY_UPDATE',  'Mengubah Kategori Produk',           1, NOW(), 1),
(17, 'PRODUCT-CATEGORY_DELETE',  'Menghapus Kategori Produk',          1, NOW(), 1),
(18, 'UNIT-OF-MEASURE_READ',     'Melihat UoM',                       1, NOW(), 1),
(19, 'UNIT-OF-MEASURE_CREATE',   'Menambah UoM',                      1, NOW(), 1),
(20, 'UNIT-OF-MEASURE_UPDATE',   'Mengubah UoM',                      1, NOW(), 1),
(21, 'UNIT-OF-MEASURE_DELETE',   'Menghapus UoM',                     1, NOW(), 1),
(22, 'BRAND_READ',               'Melihat Brand',                     1, NOW(), 1),
(23, 'BRAND_CREATE',             'Menambah Brand',                    1, NOW(), 1),
(24, 'BRAND_UPDATE',             'Mengubah Brand',                    1, NOW(), 1),
(25, 'BRAND_DELETE',             'Menghapus Brand',                   1, NOW(), 1),
(26, 'PRODUCT_READ',             'Melihat Produk',                    1, NOW(), 1),
(27, 'PRODUCT_CREATE',           'Menambah Produk',                   1, NOW(), 1),
(28, 'PRODUCT_UPDATE',           'Mengubah Produk',                   1, NOW(), 1),
(29, 'PRODUCT_DELETE',           'Menghapus Produk',                  1, NOW(), 1),
(30, 'FACILITY_READ',            'Melihat Fasilitas',                 1, NOW(), 1),
(31, 'FACILITY_CREATE',          'Menambah Fasilitas',                1, NOW(), 1),
(32, 'FACILITY_UPDATE',          'Mengubah Fasilitas',                1, NOW(), 1),
(33, 'STOCK-ADJUSTMENT_READ',    'Melihat Stock Adjustment',          1, NOW(), 1),
(34, 'STOCK-ADJUSTMENT_CREATE',  'Menambah Stock Adjustment',         1, NOW(), 1),
(35, 'STOCK-ADJUSTMENT_UPDATE',  'Mengubah Stock Adjustment',         1, NOW(), 1),
-- Master
(36, 'PARTY_READ',               'Melihat Party',                     1, NOW(), 1),
(37, 'PARTY_CREATE',             'Menambah Party',                    1, NOW(), 1),
(38, 'TAX_READ',                 'Melihat Pajak',                     1, NOW(), 1),
(39, 'CURRENCY_READ',            'Melihat Mata Uang',                 1, NOW(), 1),
(40, 'GEOGRAPHIC_READ',          'Melihat Geografis',                 1, NOW(), 1),
-- Lookups (dibutuhkan TomSelect)
(41, 'LOOKUP_BRAND',             'Lookup Brand',                      1, NOW(), 1),
(42, 'LOOKUP_PRODUCT_CATEGORY',  'Lookup Kategori',                   1, NOW(), 1),
(43, 'LOOKUP_PRODUCT',           'Lookup Produk',                     1, NOW(), 1),
(44, 'LOOKUP_FACILITY',          'Lookup Fasilitas',                  1, NOW(), 1),
(45, 'LOOKUP_GEOGRAPHIC',        'Lookup Geografis',                  1, NOW(), 1),
(46, 'LOOKUP_PARTY',             'Lookup Party',                      1, NOW(), 1),
(47, 'LOOKUP_INVENTORY',         'Lookup Inventory',                  1, NOW(), 1),
(48, 'LOOKUP_UOM_CONVERSION',    'Lookup UoM Conversion',             1, NOW(), 1),
-- Warehouse hierarchy
(49, 'GRID_READ',                'Melihat Grid',                      1, NOW(), 1),
(50, 'CONTAINER_READ',           'Melihat Container',                 1, NOW(), 1),
-- Menu group (untuk sidebar)
(51, 'MENU-GROUP_READ',          'Melihat Grup Menu',                 1, NOW(), 1);
-- Catatan: daftar di atas adalah SUBSET yang cukup untuk E2E.
-- Tambahkan permission lain saat menambah spec file baru.

-- ── 2. Security: Roles ────────────────────────────────────
INSERT INTO roles (id, name, description, created_by_user_id, created_date, version) VALUES
(1, 'ROLE_ADMIN', 'Administrator Sistem', 1, NOW(), 1);

-- Grant ALL permissions ke admin
INSERT INTO role_permissions (role_id, permission_id)
SELECT 1, id FROM permissions;

-- ── 3. Security: Admin User ──────────────────────────────
INSERT INTO users (id, username, password, email, enabled, password_change_required, role_id, created_by_user_id, created_date, version)
VALUES (1, 'admin', 'INITIAL_PASSWORD_SETUP', 'admin@solusierp.com', TRUE, TRUE, 1, 1, NOW(), 1);

INSERT INTO user_profiles (id, user_id, full_name, phone_number, language_code, default_page_size, theme, created_by_user_id, created_date, version)
VALUES (1, 1, 'Administrator', '08123456789', 'id', 10, 'light', 1, NOW(), 1);

-- ── 4. Permission Groups (untuk sidebar/menu) ────────────
INSERT INTO permission_groups (id, code, name_id, name_en, breadcrumb_id, breadcrumb_en, url_path, created_by_user_id, created_date, version) VALUES
(1,  'INV-03', 'Brand',             'Brands',              'Inventori > Brand',             'Inventory > Brands',              '/inventory/brands',              1, NOW(), 1),
(2,  'INV-02', 'Kategori Produk',   'Product Categories',  'Inventori > Kategori',          'Inventory > Product Categories',  '/inventory/product-categories',  1, NOW(), 1),
(3,  'INV-01', 'Produk',            'Products',            'Inventori > Produk',            'Inventory > Products',            '/inventory/products',            1, NOW(), 1),
(4,  'INV-04', 'Satuan Ukur',       'Units of Measure',    'Inventori > UoM',               'Inventory > Units of Measure',    '/inventory/unit-of-measures',    1, NOW(), 1),
(5,  'INV-05', 'Stock Adjustment',  'Stock Adjustments',   'Inventori > Stock Adjustment',  'Inventory > Stock Adjustments',   '/inventory/adjustments',         1, NOW(), 1),
(6,  'SEC-01', 'Pengguna',          'Users',               'Keamanan > Pengguna',           'Security > Users',                '/security/users',                1, NOW(), 1),
(7,  'SEC-02', 'Peran',             'Roles',               'Keamanan > Peran',              'Security > Roles',                '/security/roles',                1, NOW(), 1),
(8,  'MST-01', 'Business Partner',  'Business Partners',   'Master > Partner',              'Master > Business Partners',      '/master/parties',                1, NOW(), 1);

-- Link permissions ke groups (subset — cukup untuk sidebar rendering)
UPDATE permissions SET permission_group_id = 1 WHERE name LIKE 'BRAND%';
UPDATE permissions SET permission_group_id = 2 WHERE name LIKE 'PRODUCT-CATEGORY%';
UPDATE permissions SET permission_group_id = 3 WHERE name LIKE 'PRODUCT_%';
UPDATE permissions SET permission_group_id = 4 WHERE name LIKE 'UNIT-OF-MEASURE%';
UPDATE permissions SET permission_group_id = 5 WHERE name LIKE 'STOCK-ADJUSTMENT%';
UPDATE permissions SET permission_group_id = 6 WHERE name LIKE 'USERS%';
UPDATE permissions SET permission_group_id = 7 WHERE name LIKE 'ROLES%';
UPDATE permissions SET permission_group_id = 8 WHERE name LIKE 'PARTY%';

-- ── 5. System Sequences (untuk auto-generate code) ───────
INSERT INTO system_sequences (module_code, format_pattern, current_value, pad_length, reset_cycle, version) VALUES
('BRAND',              'BRD-{seq}',   0, 4, 'NEVER', 1),
('PRODUCT_CATEGORY',   'PCAT-{seq}',  0, 4, 'NEVER', 1),
('PRODUCT',            'PRD-{seq}',   0, 4, 'NEVER', 1),
('PARTY',              'PTY-{seq}',   0, 4, 'NEVER', 1),
('FACILITY',           'FAC-{seq}',   0, 4, 'NEVER', 1),
('GRID',               'GRD-{seq}',   0, 4, 'NEVER', 1),
('CONTAINER',          'BIN-{seq}',   0, 4, 'NEVER', 1),
('STOCK_ADJUSTMENT',   'SA-{seq}',    0, 4, 'NEVER', 1),
('BANK_ACCOUNT',       'BA-{seq}',    0, 4, 'NEVER', 1);

-- ── 6. Master Data: UoM (subset — 5 cukup untuk E2E) ────
INSERT INTO unit_of_measures (id, code, name, type, created_by_user_id, created_date, version) VALUES
(1, 'PCS', 'Pieces', 'UNIT',   1, NOW(), 1),
(2, 'KG',  'Kilogram','WEIGHT', 1, NOW(), 1),
(3, 'BOX', 'Box',    'UNIT',   1, NOW(), 1),
(4, 'M',   'Meter',  'LENGTH', 1, NOW(), 1),
(5, 'L',   'Liter',  'VOLUME', 1, NOW(), 1);

-- ── 7. Master Data: Currency (IDR saja) ──────────────────
INSERT INTO master_currencies (id, code, name, symbol, is_default, is_active, created_by_user_id, created_date, version)
VALUES (1, 'IDR', 'Indonesian Rupiah', 'Rp', TRUE, TRUE, 1, NOW(), 1);

-- ── 8. E2E Test Data ─────────────────────────────────────
-- Brand (pre-exist untuk test product create)
INSERT INTO brands (id, code, name, note, created_by_user_id, created_date, version)
VALUES (1, 'BRD-E2E1', 'E2E Brand', 'Seeded', 1, NOW(), 1);

-- Product Category
INSERT INTO product_categories (id, code, name, type, created_by_user_id, created_date, version)
VALUES (1, 'PCAT-E2E1', 'E2E Category', 'STOCK', 1, NOW(), 1);

-- Party (untuk facility owner)
INSERT INTO parties (id, code, name, type, created_by_user_id, created_date, version)
VALUES (1, 'PTY-E2E1', 'PT E2E Supplier', 'ORGANIZATION', 1, NOW(), 1);

-- Facility → Grid → Container
INSERT INTO inv_facilities (id, code, name, owner_id, is_active, created_by_user_id, created_date, version)
VALUES (1, 'FAC-E2E1', 'Gudang E2E', 1, TRUE, 1, NOW(), 1);

INSERT INTO inv_grids (id, facility_id, code, name, is_active, created_by_user_id, created_date, version)
VALUES (1, 1, 'GRD-E2E-A', 'Grid Alpha', TRUE, 1, NOW(), 1);

INSERT INTO inv_containers (id, grid_id, code, name, is_active, created_by_user_id, created_date, version)
VALUES (1, 1, 'BIN-E2E-001', 'Container 001', TRUE, 1, NOW(), 1);

-- Product (depends: category, brand, uom)
INSERT INTO products (id, code, name, category_id, uom_id, brand_id, is_active, is_serialized, created_by_user_id, created_date, version)
VALUES (1, 'PRD-E2E1', 'Produk E2E', 1, 1, 1, TRUE, FALSE, 1, NOW(), 1);
```

#### 4.3 Prinsip Desain Seed

| Prinsip | Penjelasan |
|---|---|
| **Minimal viable data** | Hanya permission, role, user, UoM, dan test entity yang dibutuhkan oleh 4 spec file. |
| **Explicit ID** | Semua INSERT pakai explicit ID (`id = 1, 2, ...`) agar FK reference deterministik. |
| **Satu file, urut** | Tidak ada dependency resolution — cukup tulis INSERT dalam urutan FK. |
| **Extendable** | Saat menambah spec baru (misal: Tax, Accounting), tambahkan INSERT di file yang sama. |

#### 4.4 Catatan tentang Column Mismatch

Hibernate `ddl-auto=create` membuat kolom berdasarkan `@Entity`. Jika migration production menambah kolom yang TIDAK ada di entity (misal: kolom legacy), kolom itu tidak akan ada di H2. Ini bukan masalah karena:
- Kolom yang tidak ada di entity tidak dipakai oleh aplikasi Java.
- E2E menguji behavior aplikasi, bukan schema fidelity.

**Jika ada column di seed SQL yang tidak ada di entity**, Hibernate akan membuat tabel tanpa kolom itu, dan INSERT akan gagal. Solusi: hapus kolom itu dari seed SQL. Error akan terdeteksi saat pertama kali menjalankan `java -jar --spring.profiles.active=e2e`.

---

### 5. Playwright Test Architecture

#### 5.1 Struktur (7 file)

```
e2e/
├── package.json
├── playwright.config.ts
├── helpers.ts               ← SATU file, semua helper
├── .gitignore               ← node_modules/, playwright-report/, test-results/
└── tests/
    ├── 01-login.spec.ts
    ├── 02-brand.spec.ts
    ├── 03-product.spec.ts
    └── 04-stock-adjustment.spec.ts
```

Tidak ada folder `fixtures/`, `helpers/` (plural), atau abstraksi tambahan. Satu `helpers.ts` export semua function yang dibutuhkan.

#### 5.2 `package.json`

```json
{
  "name": "solusi-erp-e2e",
  "private": true,
  "scripts": {
    "test": "playwright test",
    "report": "playwright show-report"
  },
  "devDependencies": {
    "@playwright/test": "^1.52.0"
  }
}
```

Satu dependency. Dua script.

#### 5.3 `playwright.config.ts`

```typescript
import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './tests',
  timeout: 60_000,
  expect: { timeout: 10_000 },
  retries: process.env.CI ? 1 : 0,
  workers: 1,               // Sequential — shared DB state

  reporter: [
    ['list'],
    ['html', { open: 'never' }],
  ],

  use: {
    baseURL: process.env.BASE_URL || 'http://localhost:18080',
    screenshot: 'only-on-failure',
    trace: 'retain-on-failure',
    locale: 'id-ID',
    timezoneId: 'Asia/Jakarta',
  },

  projects: [
    { name: 'chromium', use: { ...devices['Desktop Chrome'] } },
  ],
});
```

`workers: 1` karena semua test share satu H2 in-memory — parallelism = race condition.

#### 5.4 `helpers.ts` — Satu file, semua helper

```typescript
import { Page, expect } from '@playwright/test';

const BASE = process.env.BASE_URL || 'http://localhost:18080';

// ── Auth ──────────────────────────────────────────────────
export async function login(page: Page): Promise<void> {
  await page.goto(`${BASE}/login`);
  await page.fill('input[name="username"]', 'admin');
  await page.fill('input[name="password"]', 'admin123');
  await page.click('button[type="submit"]');
  await expect(page).not.toHaveURL(/\/login/);
}

// ── TomSelect ─────────────────────────────────────────────
export async function selectTomSelect(
  page: Page, selector: string, query = '', index = 0
): Promise<{ id: string; name: string } | { error: string }> {
  await page.waitForFunction(
    (sel: string) => {
      const el = document.querySelector(sel) as any;
      return el && el.tomselect;
    },
    selector,
    { timeout: 5000 }
  );
  return page.evaluate(({ sel, q, idx }) => {
    return new Promise<any>((resolve, reject) => {
      const el = document.querySelector(sel) as any;
      if (!el?.tomselect) { reject(`TomSelect not found: ${sel}`); return; }
      const ts = el.tomselect;
      ts.load(q, (opts: any[]) => {
        if (!opts.length) { resolve({ error: 'No options' }); return; }
        opts.forEach((o: any) => ts.addOption(o));
        const pick = opts[Math.min(idx, opts.length - 1)];
        ts.setValue(pick.id);
        resolve({ id: pick.id, name: pick.name });
      });
    });
  }, { sel: selector, q: query, idx: index });
}

// Line item TomSelect
export async function selectLineItemTomSelect(
  page: Page, rowIndex: number, fieldClass: string, query = ''
): Promise<any> {
  return page.evaluate(({ idx, cls, q }) => {
    return new Promise<any>((resolve, reject) => {
      const rows = document.querySelectorAll('#line-container .line-row');
      if (idx >= rows.length) { reject(`Row ${idx} not found`); return; }
      const el = rows[idx].querySelector(`.${cls}`) as any;
      if (!el?.tomselect) { reject(`TomSelect .${cls} not init`); return; }
      const ts = el.tomselect;
      ts.load(q, (opts: any[]) => {
        opts.forEach((o: any) => ts.addOption(o));
        if (opts.length > 0) {
          ts.setValue(opts[0].id);
          resolve({ id: opts[0].id, name: opts[0].name });
        } else { resolve({ error: 'empty' }); }
      });
    });
  }, { idx: rowIndex, cls: fieldClass, q: query });
}

// ── Flatpickr ─────────────────────────────────────────────
export async function setFlatpickrDate(
  page: Page, selector: string, dateStr: string
): Promise<void> {
  await page.evaluate(({ sel, d }) => {
    const input = document.querySelector(sel) as any;
    if (input?._flatpickr) input._flatpickr.setDate(d, true);
    else if (input) {
      input.value = d;
      input.dispatchEvent(new Event('change', { bubbles: true }));
    }
  }, { sel: selector, d: dateStr });
}

// ── AutoNumeric ───────────────────────────────────────────
export async function setAutoNumeric(
  page: Page, selector: string, value: number
): Promise<void> {
  await page.evaluate(({ sel, v }) => {
    const el = document.querySelector(sel) as any;
    if (typeof AutoNumeric !== 'undefined') {
      const inst = (AutoNumeric as any).getAutoNumericElement(el);
      if (inst) { inst.set(v); return; }
    }
    el.value = v;
    el.dispatchEvent(new Event('input', { bubbles: true }));
  }, { sel: selector, v: value });
}

// ── AJAX Form Submit ──────────────────────────────────────
export async function submitAndExpectSuccess(
  page: Page, expectedPath?: string
): Promise<{ success: boolean; message?: string }> {
  const responsePromise = page.waitForResponse(
    (r) => r.request().method() === 'POST' &&
           (r.headers()['content-type']?.includes('json') ?? false),
    { timeout: 15_000 }
  );
  await page.click('button[type="submit"]');
  const resp = await responsePromise;
  const body = await resp.json();
  if (body.success && expectedPath) {
    await page.waitForURL(`**${expectedPath}**`, { timeout: 5000 });
  }
  expect(body.success).toBeTruthy();
  return body;
}
```

**6 function. 1 file. Mencakup seluruh kompleksitas UI yang ada:**
- `login` → session-based auth
- `selectTomSelect` / `selectLineItemTomSelect` → widget yang paling problematik
- `setFlatpickrDate` → date picker
- `setAutoNumeric` → numeric input
- `submitAndExpectSuccess` → AJAX form flow

#### 5.5 Contoh Spec: `01-login.spec.ts`

```typescript
import { test, expect } from '@playwright/test';
import { login } from '../helpers';

test('login as admin redirects to dashboard', async ({ page }) => {
  await login(page);
  await expect(page).toHaveURL(/\/(dashboard|home)/);
  await expect(page.locator('body')).not.toContainText('Error');
});
```

#### 5.6 Contoh Spec: `02-brand.spec.ts`

```typescript
import { test, expect } from '@playwright/test';
import { login, submitAndExpectSuccess } from '../helpers';

test.beforeEach(async ({ page }) => { await login(page); });

test('create brand', async ({ page }) => {
  await page.goto('/inventory/brands/create');
  await page.waitForLoadState('networkidle');

  await page.fill('input[name="name"]', 'PW Brand Test');
  await submitAndExpectSuccess(page, '/inventory/brands');

  await expect(page.locator('table')).toContainText('PW Brand Test');
});

test('edit brand', async ({ page }) => {
  await page.goto('/inventory/brands');
  const row = page.locator('tr', { hasText: 'PW Brand Test' });
  await row.locator('a[href*="edit"]').click();
  await page.waitForLoadState('networkidle');

  await page.fill('input[name="name"]', 'PW Brand Updated');
  await submitAndExpectSuccess(page, '/inventory/brands');

  await expect(page.locator('table')).toContainText('PW Brand Updated');
});
```

#### 5.7 Contoh Spec: `03-product.spec.ts`

```typescript
import { test, expect } from '@playwright/test';
import { login, selectTomSelect, submitAndExpectSuccess } from '../helpers';

test.beforeEach(async ({ page }) => { await login(page); });

test('create product with TomSelect fields', async ({ page }) => {
  await page.goto('/inventory/products/create');
  await page.waitForLoadState('networkidle');
  await page.waitForTimeout(500); // TomSelect init

  await page.fill('input[name="name"]', 'PW Product Test');
  await selectTomSelect(page, '#category-select', '', 0);
  await page.selectOption('#uom-select', { index: 1 });
  await selectTomSelect(page, '#brand-select', '', 0);

  await submitAndExpectSuccess(page, '/inventory/products');
  await expect(page.locator('table')).toContainText('PW Product Test');
});
```

#### 5.8 Contoh Spec: `04-stock-adjustment.spec.ts`

```typescript
import { test, expect } from '@playwright/test';
import {
  login, selectTomSelect, selectLineItemTomSelect,
  setFlatpickrDate, setAutoNumeric, submitAndExpectSuccess
} from '../helpers';

test.beforeEach(async ({ page }) => { await login(page); });

test('create stock adjustment with line items', async ({ page }) => {
  await page.goto('/inventory/adjustments/create');
  await page.waitForLoadState('networkidle');
  await page.waitForTimeout(500);

  // Header
  await selectTomSelect(page, '#header-facility', '', 0);
  await setFlatpickrDate(page, 'input[name="transactionDate"]', '2026-01-15');

  // Add line item
  await page.click('#btn-add-line');
  await page.waitForTimeout(500);
  await selectLineItemTomSelect(page, 0, 'select-product', '');
  await page.waitForTimeout(300);  // cascade: grid, container

  // Qty via drawer
  await page.click('.line-row:first-child .btn-edit-qty');
  await page.waitForTimeout(300);
  await setAutoNumeric(page, '.offcanvas.show .input-qty-target, dialog[open] .input-qty-target', 10);
  await page.click('.offcanvas.show .btn-apply, dialog[open] .btn-apply');
  await page.waitForTimeout(300);

  // Unit cost
  await setAutoNumeric(page, '.line-row:first-child .input-price', 50000);

  await submitAndExpectSuccess(page, '/inventory/adjustments');
});
```

#### 5.9 Selector Strategy

Satu aturan: **gunakan `name` attribute dan `id` yang sudah ada.**

| Prioritas | Selector | Contoh |
|---|---|---|
| 1 | `input[name="..."]` | `input[name="name"]` — terikat ke DTO |
| 2 | `#id` | `#category-select` — dipakai TomSelect |
| 3 | `button[type="submit"]` | Hanya 1 per form |
| 4 | `a[href*="edit"]` | Link edit di table row |

Tidak perlu `data-testid`. Selector di atas sudah stabil karena terikat ke Java DTO binding dan TomSelect initialization.

#### 5.10 `.gitignore`

```
node_modules/
playwright-report/
test-results/
```

---

### 6. CI/CD Integration

#### 6.1 File: `.github/workflows/e2e-playwright.yml`

```yaml
name: E2E - Playwright

on:
  push:
    branches: [main]
  pull_request:
    branches: [main]
  workflow_dispatch:

concurrency:
  group: e2e-${{ github.ref }}
  cancel-in-progress: true

permissions:
  contents: read

jobs:
  e2e:
    name: E2E Tests
    runs-on: ubuntu-latest
    timeout-minutes: 15

    steps:
      - uses: actions/checkout@v4

      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '21'
          cache: maven

      - uses: actions/setup-node@v4
        with:
          node-version: '20'

      - name: Build JAR
        run: chmod +x mvnw && ./mvnw -B clean package -DskipTests -q

      - name: Start app (e2e profile)
        run: |
          java -jar target/solusi-program-erp-*.jar \
            --spring.profiles.active=e2e &
          echo $! > app.pid

      - name: Wait for readiness
        run: |
          for i in $(seq 1 60); do
            if curl -sf http://localhost:18080/login > /dev/null 2>&1; then
              echo "Ready after ${i}s"; exit 0
            fi
            sleep 2
          done
          echo "Timeout"; exit 1

      - name: Install Playwright
        working-directory: e2e
        run: npm ci && npx playwright install --with-deps chromium

      - name: Run E2E tests
        working-directory: e2e
        env:
          BASE_URL: http://localhost:18080
        run: npx playwright test

      - name: Stop app
        if: always()
        run: kill $(cat app.pid) || true

      - name: Upload report
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: playwright-report
          path: e2e/playwright-report/
          retention-days: 7

      - name: Upload traces (on failure)
        if: failure()
        uses: actions/upload-artifact@v4
        with:
          name: playwright-traces
          path: e2e/test-results/
          retention-days: 7
```

**Ini berjalan PARALLEL dengan existing `fast-tests` job** — bukan sequential. Dua job independent.

#### 6.2 Tidak ada yang lain

- Tidak perlu step "verify migration parity" (kita tidak punya migration-e2e).
- Tidak perlu Docker/Docker Compose.
- Tidak perlu environment variables selain `BASE_URL`.

---

### 7. Cakupan Test Minimum per Modul

**Hanya 4 spec file. 7 test case. Ini sudah mencakup semua jenis komponen UI:**

| # | Spec File | Test Cases | Komponen UI yang Di-cover |
|---|---|---|---|
| 1 | `01-login.spec.ts` | Login → dashboard | Session auth, redirect, CSRF |
| 2 | `02-brand.spec.ts` | Create, Edit | Form dasar, AJAX submit, HTMX list |
| 3 | `03-product.spec.ts` | Create (with TomSelect) | **TomSelect**, standard select, form complexities |
| 4 | `04-stock-adjustment.spec.ts` | Create with line items | **TomSelect cascade**, **Flatpickr**, **AutoNumeric**, **drawer**, line item manager |

**Estimasi runtime: ~45 detik.**

**Apa yang TIDAK di-test (dan mengapa):**

| Modul | Alasan Skip |
|---|---|
| UoM, Tax, Currency, Geographic | Form sederhana — pola sama persis dengan Brand. Zero insight tambahan. |
| Party | Kompleks tapi pola sama (TomSelect + inline table). Tambahkan nanti. |
| User, Role, Permission | Admin-only. Form sederhana. |
| COA, Accounting Period | Sprint 2+ features. Belum prioritas. |
| Approval flow | Butuh MinIO real/stub upload. Out of scope MVP. |

**Kapan menambah spec baru:** Hanya saat ada modul dengan **pola UI baru** yang belum ter-cover. Jangan menambah spec yang hanya mengulang pola yang sudah ada.

---

### 8. Risiko & Mitigasi

| # | Risiko | Dampak | Mitigasi |
|---|---|---|---|
| 1 | **Schema drift: `ddl-auto=create` ≠ Flyway schema** | Kolom H2 beda nama/tipe dari production. Seed SQL INSERT gagal. | E2E menguji behavior UI, bukan SQL. Error seed SQL terdeteksi saat server start (fail fast). Fix seed SQL → commit → selesai. |
| 2 | **H2 behavioral gap vs MariaDB** | Query yang work di MariaDB gagal di H2 (collation, case sensitivity). | `MODE=MySQL` + `CASE_INSENSITIVE_IDENTIFIERS=TRUE` menutupi mayoritas gap. Jika ada query spesifik yang gagal, bisa ditambah `@Profile("!e2e")` guard — tapi hanya jika terjadi. |
| 3 | **Flaky test: timing komponen JS** | TomSelect/Flatpickr belum init saat Playwright berinteraksi. False negative. | Helper `selectTomSelect` sudah include `waitForFunction`. Retry `retries: 1` di CI. Hindari `waitForTimeout` tanpa batas waktu. |
| 4 | **Seed SQL tertinggal saat entity berubah** | Server E2E gagal start karena INSERT mismatch. | CI langsung gagal (fail fast). Fix seed SQL saat itu juga — jauh lebih mudah daripada transpile 45 migration. |

---

### 9. Effort Estimate

| # | Komponen | Deliverable | Estimasi |
|---|---|---|---|
| 1 | Spring Profile | `application-e2e.yaml`, `E2eStorageConfig.java`, `E2eBootstrap.java`, H2 dep di pom.xml | **1 hari** |
| 2 | Seed SQL | `e2e-seed.sql` — konsolidasi INSERT dari V1–V45 | **2 hari** |
| 3 | Playwright setup | `e2e/` folder, `package.json`, `playwright.config.ts`, `helpers.ts`, `.gitignore` | **0.5 hari** |
| 4 | Spec files | 4 spec files (login, brand, product, stock-adjustment) | **2–3 hari** |
| 5 | CI workflow | `.github/workflows/e2e-playwright.yml` | **0.5 hari** |
| 6 | Debug & stabilize | Fix seed SQL mismatch, timing issues, selector adjustments | **1–2 hari** |
| **Total MVP** | | **12 file baru, 7 test cases** | **~1 minggu** |

> Satu developer full-time. Tidak butuh parallelism karena scope kecil.

---

### 10. Trade-offs

#### Yang Dikorbankan

| Trade-off | Dampak | Justifikasi |
|---|---|---|
| **`ddl-auto=create` bukan Flyway** | Schema di H2 mungkin subtly beda (index names, constraint naming). Tidak memvalidasi migration script itu sendiri. | E2E test menguji **UI behavior**, bukan schema fidelity. Menghapus 45-file maintenance burden lebih valuable. |
| **Seed SQL manual vs Flyway migration replay** | Saat migration baru ditambah, seed SQL mungkin perlu diupdate. | Update 1 file (tambah INSERT) jauh lebih mudah daripada transpile SQL per-migration. Fail fast di CI. |
| **Hanya 4 spec (7 test cases)** | Banyak modul tidak ter-cover. | 4 spec ini cover **semua jenis komponen UI** (form biasa, TomSelect, Flatpickr, AutoNumeric, line items, drawer, AJAX). Modul lain pakai pola yang sama — zero insight tambahan. |
| **Hanya Chromium** | Bug Firefox/Safari-specific tidak terdeteksi. | SSR app — browser variance minimal. Effort ≠ reward. |
| **`workers: 1` (sequential)** | Runtime lebih lambat (~45 detik vs ~15 detik parallel). | Shared H2 state. 45 detik = acceptable. |
| **No approval/process flow** | Gap pada alur bisnis kritis. | MinIO stub = no-op. Approval test butuh real upload. Tambahkan nanti saat worth it. |

#### Yang TIDAK Dilakukan (dan Mengapa)

| Pendekatan Alternatif | Mengapa Ditolak |
|---|---|
| **Flyway + `db/migration-e2e/` (45 file transpile)** | Maintenance burden terbesar. Setiap migration baru = transpile lagi. Untuk E2E, overkill. |
| **TestContainers + MariaDB** | Docker dependency di CI. Startup lambat. Melanggar prinsip "tanpa dependensi eksternal". |
| **8 helper files terpisah** | Over-engineering. 6 function cukup di 1 file. Split saat file > 200 baris. |
| **15 spec files untuk semua modul** | Diminishing returns. 4 spec sudah cover semua pola UI. Tambah spec = tambah maintenance tanpa insight baru. |
| **TypeScript config (`tsconfig.json`)** | Playwright menjalankan `.ts` langsung tanpa tsconfig. Tidak perlu. |
| **Custom test fixture (`base.extend`)** | `login()` di `beforeEach` sudah cukup. Fixture = indirection yang belum dibutuhkan. |
| **`data-testid` attribute** | Butuh modifikasi template Thymeleaf. Selector `name` dan `id` sudah stabil. Tambahkan nanti jika selector mulai rapuh. |

---

### Rangkuman: File Inventory

```
File baru (12 total):
├── src/main/resources/application-e2e.yaml         ← Spring profile
├── src/main/resources/db/e2e-seed.sql               ← Seed data (satu file)
├── src/main/java/.../config/E2eStorageConfig.java   ← MinIO stub
├── src/main/java/.../config/E2eBootstrap.java       ← Disable password change
├── .github/workflows/e2e-playwright.yml             ← CI workflow
├── e2e/package.json                                 ← Node deps
├── e2e/playwright.config.ts                         ← Playwright config
├── e2e/helpers.ts                                   ← ALL helpers
├── e2e/.gitignore                                   ← Ignore node_modules
├── e2e/tests/01-login.spec.ts                       ← Login test
├── e2e/tests/02-brand.spec.ts                       ← CRUD dasar
├── e2e/tests/03-product.spec.ts                     ← TomSelect test
└── e2e/tests/04-stock-adjustment.spec.ts            ← Full complexity test

File dimodifikasi (1):
└── pom.xml                                          ← +H2 dependency
```

**12 file baru. 1 file dimodifikasi. 7 test cases. ~1 minggu effort.**
