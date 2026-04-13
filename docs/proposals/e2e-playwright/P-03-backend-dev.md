Agent completed. agent_id: proposal-p03-backend-dev, agent_type: general-purpose, status: completed, description: P-03 Backend Developer proposal, elapsed: 618s, total_turns: 0, model: claude-opus-4.6, duration: 609s

### 1. Executive Summary

Proposal ini mendesain infrastruktur E2E testing menggunakan Playwright (Node.js) terhadap Spring Boot 4.0.3 ERP yang berjalan di atas H2 in-memory (MODE=MySQL) sebagai pengganti MariaDB produksi. Tantangan utama: 45 Flyway migration scripts mengandung 12+ pola SQL MariaDB-specific (`ENGINE=InnoDB`, `ENUM`, `ON DUPLICATE KEY UPDATE`, `INSERT IGNORE`, `FROM DUAL`, `COMMENT`, `MODIFY COLUMN`, `ESCAPE '\\'`, `DATETIME(6)`, `DEFAULT CHARSET/COLLATE`, `TINYINT(1)`, `ADD COLUMN IF NOT EXISTS`). Strategi kami: **dual-location Flyway migration** — migration produksi tetap utuh di `classpath:db/migration`, sementara profile `e2e` menjalankan migration set yang sudah di-patch untuk H2 dari `classpath:db/migration-h2` beserta seeding script tambahan. SystemInitializer secara otomatis mengubah `INITIAL_PASSWORD_SETUP` menjadi BCrypt `admin123`, memastikan login Playwright tanpa intervensi manual. Pendekatan ini zero-touch pada kode produksi.

---

### 2. Arsitektur Solusi

```
┌──────────────────────────────────────────────────────────────────────┐
│                        CI / Developer Machine                        │
│                                                                      │
│  ┌────────────────────────┐      ┌─────────────────────────────────┐ │
│  │  Spring Boot App       │      │  Playwright Test Runner         │ │
│  │  (Profile: e2e)        │      │  (e2e-tests/ subdirectory)      │ │
│  │                        │      │                                 │ │
│  │  ┌──────────────────┐  │      │  ┌───────────────────────────┐  │ │
│  │  │ H2 In-Memory     │  │ HTTP │  │ @playwright/test          │  │ │
│  │  │ MODE=MySQL       │◄─┼──────┼──┤                           │  │ │
│  │  │                  │  │:8090 │  │ login → CRUD → assertions │  │ │
│  │  └──────────────────┘  │      │  └───────────────────────────┘  │ │
│  │                        │      │                                 │ │
│  │  Flyway Migrations:    │      │  Config:                        │ │
│  │  db/migration-h2/      │      │  playwright.config.ts           │ │
│  │  + db/migration-e2e/   │      │  baseURL: localhost:8090        │ │
│  │                        │      │                                 │ │
│  │  SystemInitializer:    │      │  Fixtures:                      │ │
│  │  admin/admin123        │      │  auth.setup.ts (login state)    │ │
│  │  password_change_      │      │  global-setup.ts (wait server)  │ │
│  │  required=true → false │      │  global-teardown.ts (kill srv)  │ │
│  └────────────────────────┘      └─────────────────────────────────┘ │
│                                                                      │
│  Lifecycle:                                                          │
│  1. mvnw spring-boot:run -Pe2e (background, port 8090)              │
│  2. Wait for health check: /login returns 200                        │
│  3. npx playwright test (dari e2e-tests/)                            │
│  4. Kill Spring Boot process                                         │
└──────────────────────────────────────────────────────────────────────┘
```

**Komponen kunci:**

| Komponen | Teknologi | Lokasi |
|----------|-----------|--------|
| Application Server | Spring Boot 4.0.3 + profile `e2e` | Root project (`./mvnw`) |
| Database | H2 2.x, `MODE=MySQL;DATABASE_TO_LOWER=TRUE` | In-memory, embedded |
| Schema Management | Flyway dual-location | `db/migration-h2` + `db/migration-e2e` |
| Test Runner | `@playwright/test` (Node.js) | `e2e-tests/` subdirectory |
| Browser | Chromium headless (CI) / headed (dev) | Playwright-managed |

---

### 3. Spring Boot E2E Profile Design

#### 3.1 Dependency Addition (`pom.xml`)

```xml
<!-- Test Database: H2 for E2E profile -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```

> **Catatan**: Scope `test` tidak cukup untuk `spring-boot:run -Pe2e`. Diperlukan Maven profile yang memindahkan scope menjadi `runtime`:

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

#### 3.2 Profile Configuration: `application-e2e.yaml`

```yaml
server:
  port: 8090
  servlet:
    session:
      cookie:
        secure: false  # H2 E2E tidak pakai HTTPS

spring:
  datasource:
    url: jdbc:h2:mem:solusi_erp_e2e;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;NON_KEYWORDS=VALUE
    driver-class-name: org.h2.Driver
    username: sa
    password:

  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: none  # Tetap Flyway-managed
    show-sql: false

  flyway:
    enabled: true
    baseline-on-migrate: true
    locations:
      - classpath:db/migration-h2
      - classpath:db/migration-e2e
    clean-disabled: false

  h2:
    console:
      enabled: true   # Debug aid: localhost:8090/h2-console
      path: /h2-console
      settings:
        web-allow-others: false

  thymeleaf:
    cache: false

# Disable MinIO untuk E2E (tidak perlu file storage)
minio:
  endpoint: http://localhost:19000
  presigned-endpoint: http://localhost:19000
  access-key: minioadmin
  secret-key: minioadmin

logging:
  level:
    com.solusi.erp: INFO
    org.flywaydb: DEBUG
    org.hibernate.SQL: OFF
```

**Penjelasan parameter H2 URL:**

| Parameter | Alasan |
|-----------|--------|
| `MODE=MySQL` | Kompatibilitas syntax MySQL/MariaDB (ENUM, AUTO_INCREMENT, dll) |
| `DATABASE_TO_LOWER=TRUE` | MariaDB lowercase table names secara default |
| `CASE_INSENSITIVE_IDENTIFIERS=TRUE` | Konsistensi identifier case dengan MariaDB |
| `NON_KEYWORDS=VALUE` | `VALUE` digunakan sebagai nama kolom di beberapa migration |

#### 3.3 Hibernate Dialect Override

Di `application-e2e.yaml`, kita mengganti `MariaDBDialect` menjadi `H2Dialect`. Ini **kritis** karena tanpa override, Hibernate akan generate SQL yang tidak kompatibel dengan H2 (misalnya `LIMIT` syntax, identifier quoting). Profile-based override memastikan kode produksi (`application.yaml`) sama sekali tidak tersentuh.

#### 3.4 Running the E2E Server

```bash
# Dari root project
./mvnw spring-boot:run -Pe2e -Dspring-boot.run.profiles=e2e
```

Maven profile `-Pe2e` mengaktifkan H2 sebagai runtime dependency. Spring profile `-Dspring-boot.run.profiles=e2e` memuat `application-e2e.yaml`.

---

### 4. Data Seeding Strategy

Ini adalah section **paling kritis**. Dari analisis 45 migration scripts, saya mengidentifikasi **12 kategori incompatibility** yang harus ditangani.

#### 4.1 Strategi: Dual-Location Flyway dengan H2-Patched Migrations

```
src/main/resources/
├── db/
│   ├── migration/              # PRODUKSI — JANGAN DIUBAH
│   │   ├── V1__Initial_Security_Schema.sql
│   │   ├── V2__Seed_Security_Data.sql
│   │   └── ... (V3 s/d V45)
│   │
│   ├── migration-h2/           # H2-COMPATIBLE COPY (semua V1-V45, patched)
│   │   ├── V1__Initial_Security_Schema.sql
│   │   ├── V2__Seed_Security_Data.sql
│   │   └── ... (V3 s/d V45, setiap file di-patch)
│   │
│   └── migration-e2e/          # E2E-SPECIFIC SEEDING (V900+)
│       ├── V900__E2E_Disable_Password_Change.sql
│       ├── V901__E2E_Seed_Test_Parties.sql
│       ├── V902__E2E_Seed_Test_Products.sql
│       └── V903__E2E_Seed_Test_Accounting.sql
```

**Mengapa COPY, bukan patch?** Migration scripts produksi mengandung checksum Flyway. Jika kita memodifikasi file di `db/migration/`, checksum mismatch akan menyebabkan failure di lingkungan produksi. Duplikasi ke `db/migration-h2/` memastikan kedua set independen.

**Mengapa BUKAN `afterMigrate` callback?** Flyway callbacks (`afterMigrate.sql`) tidak mendukung conditional logic per-profile. Dual-location memberikan kontrol penuh.

#### 4.2 Katalog Incompatibility dan Patch Rules

Berikut tabel lengkap semua incompatibility yang ditemukan dan resolusinya:

| # | Pattern MariaDB | File Terdampak | Resolusi H2 | Severity |
|---|----------------|----------------|-------------|----------|
| 1 | `ENGINE=InnoDB` | V1, V3-V9, V10-V12, V16, V21, V23, V24, V28-V29, V33, V43 | **Hapus** — H2 mengabaikan storage engine syntax | Low |
| 2 | `DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci` | V21, V23, V24, V28-V29, V33, V43 | **Hapus** — H2 tidak support charset per-table | Low |
| 3 | `ENUM('A','B','C')` | V3 (product_categories), V4 (system_sequences), V5 (unit_of_measures), V8 (parties, party_addresses) | **H2 MODE=MySQL supports ENUM** sejak H2 2.x. Tetap bisa digunakan. Jika gagal: ganti dengan `VARCHAR(30) CHECK(col IN ('A','B','C'))` | Medium |
| 4 | `TINYINT(1)` | V21 (facilities, grids, containers) | **H2 MODE=MySQL maps TINYINT(1) → TINYINT**. Functionally OK karena JPA Hibernate memetakan ke `boolean`. Tidak perlu patch | Low |
| 5 | `DATETIME(6)` | V21, V23, V24, V29, V33 | **Ganti dengan `TIMESTAMP(6)`** atau cukup `DATETIME`. H2 MODE=MySQL menerima `DATETIME` tanpa precision spec. Safest: hapus `(6)` menjadi `DATETIME` | Medium |
| 6 | `COMMENT 'text'` pada kolom | V9 (party_contacts), V34 (approval_histories), V43 (COA, schemas, periods), V44 (periods) | **Hapus** — H2 tidak support inline COMMENT pada kolom. Informasinya documentary, tidak fungsional | Low |
| 7 | `ON DUPLICATE KEY UPDATE` | V40 (system_sequences), V43 (system_sequences) | **Ganti dengan:**<br>`MERGE INTO system_sequences KEY(module_code) VALUES (...)` | **High** |
| 8 | `INSERT IGNORE` | V45 (role_permissions) | **Ganti dengan:**<br>`MERGE INTO role_permissions KEY(role_id, permission_id) SELECT ...` | **High** |
| 9 | `FROM DUAL` | V37 (party_role_types) | **Hapus `FROM DUAL`** — H2 MODE=MySQL mendukung `SELECT ... WHERE NOT EXISTS(...)` tanpa `FROM DUAL` | Medium |
| 10 | `ESCAPE '\\'` dalam LIKE | V7, V8, V9, V10, V11, V14, V16, V19, V20, V22, V24, V25, V31, V32, V35, V36, V41, V42, V43 | **H2 MODE=MySQL mendukung ESCAPE clause**. Namun karena double-backslash, test dulu. Jika gagal: ganti `ESCAPE '\\'` dengan `ESCAPE '!'` dan ubah `\\_` menjadi `!_` | Medium |
| 11 | `MODIFY COLUMN` | V17 (semua tabel audit refactor), V44 (acc_accounting_periods) | **Ganti dengan `ALTER TABLE ... ALTER COLUMN ... SET NOT NULL`** — H2 tidak support MySQL `MODIFY COLUMN` syntax | **High** |
| 12 | `ADD COLUMN IF NOT EXISTS` | V15, V30, V40 | **H2 mendukung** `ADD COLUMN IF NOT EXISTS` sejak H2 2.x. Tidak perlu patch | Low |

#### 4.3 Contoh Patch per Incompatibility

**Patch #7 — ON DUPLICATE KEY UPDATE (V40, V43):**

```sql
-- SEBELUM (MariaDB):
INSERT INTO system_sequences (module_code, format_pattern, pad_length, reset_cycle, 
    updated_by_user_id, updated_date)
VALUES ('NEWS', 'NEWS-{seq}', 4, 'NEVER', 1, NOW())
ON DUPLICATE KEY UPDATE module_code = module_code;

-- SESUDAH (H2):
MERGE INTO system_sequences (module_code, format_pattern, pad_length, reset_cycle,
    updated_by_user_id, updated_date)
KEY (module_code)
VALUES ('NEWS', 'NEWS-{seq}', 4, 'NEVER', 1, NOW());
```

**Patch #8 — INSERT IGNORE (V45):**

```sql
-- SEBELUM (MariaDB):
INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'ROLE_ADMIN' AND p.name LIKE 'ACCOUNTING-%';

-- SESUDAH (H2):
MERGE INTO role_permissions (role_id, permission_id)
KEY (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'ROLE_ADMIN' AND p.name LIKE 'ACCOUNTING-%';
```

**Patch #11 — MODIFY COLUMN (V17):**

```sql
-- SEBELUM (MariaDB):
ALTER TABLE permissions MODIFY created_by_user_id BIGINT NOT NULL;

-- SESUDAH (H2):
ALTER TABLE permissions ALTER COLUMN created_by_user_id SET NOT NULL;
```

**Patch #6 — COMMENT (V43):**

```sql
-- SEBELUM (MariaDB):
account_type VARCHAR(20) NOT NULL COMMENT 'ASSET | LIABILITY | EQUITY | REVENUE | EXPENSE',

-- SESUDAH (H2):
account_type VARCHAR(20) NOT NULL,
```

#### 4.4 E2E-Specific Seeding Scripts (V900+)

Nomor versi V900+ memastikan seeding berjalan SETELAH semua schema migration selesai.

**V900__E2E_Disable_Password_Change.sql:**

```sql
-- SystemInitializer sudah mengubah INITIAL_PASSWORD_SETUP → BCrypt(admin123)
-- Tapi password_change_required=true akan redirect ke /reset-password
-- E2E test butuh login langsung tanpa forced password change

UPDATE users SET password_change_required = FALSE WHERE username = 'admin';
```

> **Catatan penting**: Script ini TIDAK mengubah password. Password sudah di-handle oleh `SystemInitializer` yang berjalan di startup sebagai `CommandLineRunner`. Urutan: Flyway (V1-V45) → SystemInitializer (admin123) → Flyway E2E (V900+). **SALAH** — Flyway E2E migrations V900+ dijalankan oleh Flyway SEBELUM CommandLineRunner. Jadi urutan sebenarnya: Flyway (semua lokasi termasuk V900+) → Spring context fully ready → CommandLineRunner (SystemInitializer). Ini berarti V900 harus cukup set `password_change_required=FALSE`, dan password akan diganti oleh SystemInitializer setelahnya.

**V901__E2E_Seed_Test_Parties.sql:**

```sql
-- Seed business partner untuk testing Party CRUD dan lookup
-- Dependencies: parties, party_role_types, geographics (sudah seeded di V8, V12-V15)

INSERT INTO parties (code, name, type, is_active, created_by_user_id, created_date, version)
VALUES
('BP-E2E-001', 'PT Test Supplier', 'ORGANIZATION', TRUE, 1, NOW(), 1),
('BP-E2E-002', 'John Doe Customer', 'PERSON', TRUE, 1, NOW(), 1);

INSERT INTO party_roles (party_id, role_type_id)
SELECT p.id, prt.id FROM parties p, party_role_types prt
WHERE p.code = 'BP-E2E-001' AND prt.code = 'SUPPLIER';

INSERT INTO party_roles (party_id, role_type_id)
SELECT p.id, prt.id FROM parties p, party_role_types prt
WHERE p.code = 'BP-E2E-002' AND prt.code = 'CUSTOMER';
```

**V902__E2E_Seed_Test_Products.sql:**

```sql
-- Seed product dan kategori untuk testing Inventory modules
-- Dependencies: product_categories, unit_of_measures, brands (sudah seeded V3, V5, V6)

-- Pastikan ada minimal 1 brand
MERGE INTO brands (code, name, is_active, created_by_user_id, created_date, version)
KEY (code)
VALUES ('BRD-E2E-001', 'Test Brand', TRUE, 1, NOW(), 1);

-- Pastikan ada minimal 1 product category
MERGE INTO product_categories (code, name, type, is_active, created_by_user_id, created_date, version)
KEY (code)
VALUES ('PCAT-E2E-001', 'Test Category', 'STOCK', TRUE, 1, NOW(), 1);

-- Seed product
INSERT INTO products (code, name, description, category_id, uom_id, brand_id,
    is_active, created_by_user_id, created_date, version)
SELECT 'PRD-E2E-001', 'Test Product Alpha', 'Product untuk E2E testing',
    pc.id,
    (SELECT id FROM unit_of_measures WHERE code = 'PCS' LIMIT 1),
    b.id,
    TRUE, 1, NOW(), 1
FROM product_categories pc, brands b
WHERE pc.code = 'PCAT-E2E-001' AND b.code = 'BRD-E2E-001';
```

**V903__E2E_Seed_Test_Accounting.sql:**

```sql
-- Seed accounting master data
INSERT INTO acc_chart_of_accounts (code, name, account_type, normal_balance, level, is_header,
    is_active, created_by_user_id, created_date, version)
VALUES
('1000', 'Aset', 'ASSET', 'DEBIT', 1, TRUE, TRUE, 1, NOW(), 1),
('1100', 'Kas & Bank', 'ASSET', 'DEBIT', 2, FALSE, TRUE, 1, NOW(), 1),
('2000', 'Liabilitas', 'LIABILITY', 'CREDIT', 1, TRUE, TRUE, 1, NOW(), 1),
('4000', 'Pendapatan', 'REVENUE', 'CREDIT', 1, TRUE, TRUE, 1, NOW(), 1),
('5000', 'Beban', 'EXPENSE', 'DEBIT', 1, TRUE, TRUE, 1, NOW(), 1);

-- Set parent references
UPDATE acc_chart_of_accounts SET parent_id = (SELECT id FROM acc_chart_of_accounts WHERE code = '1000')
WHERE code = '1100';

-- Fiscal year & period
INSERT INTO acc_fiscal_years (code, name, start_date, end_date, is_active,
    created_by_user_id, created_date, version)
VALUES ('FY-2025', 'Tahun Fiskal 2025', '2025-01-01', '2025-12-31', TRUE, 1, NOW(), 1);

INSERT INTO acc_accounting_periods (code, name, period_number, fiscal_year_id, start_date, end_date,
    status, created_by_user_id, created_date, version)
SELECT 'FY2025-01', 'Januari 2025', 1, fy.id, '2025-01-01', '2025-01-31', 'OPEN', 1, NOW(), 1
FROM acc_fiscal_years fy WHERE fy.code = 'FY-2025';
```

#### 4.5 Entity Dependency Ordering (Topological Sort)

Urutan seeding HARUS mengikuti dependency graph berikut. Violation akan menyebabkan FK constraint failure:

```
Layer 0 (No Dependencies):
  permissions, roles, party_role_types, party_id_types, geographics(root),
  taxes, master_currencies, permission_groups

Layer 1 (Depends on Layer 0):
  role_permissions         → roles, permissions
  users                    → roles
  geographics(children)    → geographics(parent)

Layer 2 (Depends on Layer 1):
  user_profiles            → users
  product_categories       → users (audit FK)
  unit_of_measures         → users (audit FK)
  brands                   → users (audit FK)
  system_sequences         → users (audit FK)
  parties                  → users (audit FK)
  acc_fiscal_years         → users (audit FK)
  acc_chart_of_accounts    → users (audit FK)

Layer 3 (Depends on Layer 2):
  products                 → product_categories, unit_of_measures, brands
  party_roles              → parties, party_role_types
  party_identifications    → parties, party_id_types
  party_addresses          → parties, geographics
  party_contacts           → parties
  bank_accounts            → parties, geographics
  inv_facilities           → parties, geographics
  acc_accounting_periods   → acc_fiscal_years
  acc_accounting_schemas   → acc_chart_of_accounts

Layer 4 (Depends on Layer 3):
  inv_grids                → inv_facilities
  product_uom_conversions  → products, unit_of_measures
  common_news              → (standalone with code sequence)
  appr_requests            → users

Layer 5 (Depends on Layer 4):
  inv_containers           → inv_grids
  appr_histories           → appr_requests, users
  appr_signatures          → appr_requests

Layer 6 (Depends on Layer 5):
  inv_stock_balances       → products, inv_containers
  inv_movements            → products, inv_containers, master_currencies
  inv_valuation_layers     → products, inv_containers, master_currencies
  inv_stock_adjustments    → inv_facilities, master_currencies

Layer 7 (Depends on Layer 6):
  inv_stock_adjustment_lines → inv_stock_adjustments, products,
                               inv_grids, inv_containers, unit_of_measures
```

#### 4.6 H2 Gotchas yang HARUS Diperhatikan

**1. AuditorAware Chicken-and-Egg Problem:**
Flyway migrations INSERT data dengan `created_by_user_id = 1` (hardcoded). Ini bekerja karena V2 membuat user admin dengan id=1 terlebih dahulu. Di H2, `AUTO_INCREMENT` dimulai dari 1 — konsisten dengan MariaDB.

**2. SystemInitializer vs. Flyway Execution Order:**
```
Flyway (semua lokasi, termasuk V900+)
  ↓
Spring ApplicationContext fully initialized
  ↓
CommandLineRunner (SystemInitializer)
  → Detects 'INITIAL_PASSWORD_SETUP'
  → Replaces with BCrypt('admin123')
  → Sets password_change_required = TRUE
  ↓
V900 sudah set password_change_required = FALSE
  ⚠ MASALAH: SystemInitializer OVERRIDE kembali ke TRUE!
```

**Solusi**: Modifikasi `V900` agar tidak mengandalkan timing. Sebagai gantinya, buat **E2E-specific SystemInitializer override** atau tambahkan logic di `SystemInitializer`:

```java
// Di SystemInitializer.java, tambahkan cek profile
@Value("${spring.profiles.active:}")
private String activeProfiles;

@Override
public void run(String... args) {
    // ... existing logic ...
    if (isPlaceholder || isOldBadHash) {
        user.setPassword(passwordEncoder.encode("admin123"));
        // Jika profile E2E aktif, langsung disable password change
        boolean isE2e = activeProfiles.contains("e2e");
        user.setPasswordChangeRequired(!isE2e);
        userRepository.save(user);
    }
}
```

**Alternatif tanpa mengubah kode produksi** — lebih direkomendasikan:

Buat `E2eInitializer` yang berjalan SETELAH `SystemInitializer` via `@Order`:

```java
@Component
@Profile("e2e")
@Order(Ordered.LOWEST_PRECEDENCE) // Run after SystemInitializer
public class E2eInitializer implements CommandLineRunner {
    
    private final UserJpaRepository userRepository;
    
    @Override
    public void run(String... args) {
        userRepository.findByUsername("admin").ifPresent(user -> {
            user.setPasswordChangeRequired(false);
            userRepository.save(user);
        });
    }
}
```

**3. ENUM Type di H2:**
H2 `MODE=MySQL` sejak versi 2.x mendukung `ENUM`. Namun perlu divalidasi bahwa JPA `@Enumerated(EnumType.STRING)` tetap berfungsi. Entity seperti `Party` (`PERSON`/`ORGANIZATION`), `ProductCategory` (`STOCK`/`NON_STOCK`/`SERVICE`) harus ditest INSERT + SELECT round-trip.

**4. Large Geographic Data (V15 — 514 rows):**
File V15 berisi 514 INSERT statements untuk kota/kabupaten Indonesia. Setiap INSERT menggunakan subquery `SELECT ... FROM geographics WHERE code = 'ID-XX'`. Di H2, ini berfungsi identik. Namun execution time bisa lambat karena 514 individual INSERT. Jika startup E2E terlalu lambat, pertimbangkan batch INSERT.

---

### 5. Playwright Test Architecture

#### 5.1 Directory Structure

```
e2e-tests/
├── package.json
├── playwright.config.ts
├── tsconfig.json
├── .env.example
│
├── fixtures/
│   ├── auth.setup.ts          # Login & save storageState
│   └── base-page.ts           # BasePage dengan common helpers
│
├── pages/                      # Page Object Model
│   ├── login.page.ts
│   ├── dashboard.page.ts
│   ├── sidebar.page.ts
│   │
│   ├── security/
│   │   ├── users-list.page.ts
│   │   ├── user-form.page.ts
│   │   └── roles-list.page.ts
│   │
│   ├── inventory/
│   │   ├── brands-list.page.ts
│   │   ├── brand-form.page.ts
│   │   ├── products-list.page.ts
│   │   ├── product-form.page.ts
│   │   ├── product-categories-list.page.ts
│   │   ├── product-category-form.page.ts
│   │   ├── uom-list.page.ts
│   │   └── adjustments-list.page.ts
│   │
│   ├── master/
│   │   ├── parties-list.page.ts
│   │   ├── party-form.page.ts
│   │   ├── tax-list.page.ts
│   │   └── currency-list.page.ts
│   │
│   └── accounting/
│       ├── coa-list.page.ts
│       └── period-list.page.ts
│
├── tests/
│   ├── auth/
│   │   ├── login.spec.ts
│   │   └── logout.spec.ts
│   │
│   ├── security/
│   │   ├── users-crud.spec.ts
│   │   └── roles-crud.spec.ts
│   │
│   ├── inventory/
│   │   ├── brands-crud.spec.ts
│   │   ├── products-crud.spec.ts
│   │   ├── product-categories-crud.spec.ts
│   │   └── adjustments-flow.spec.ts
│   │
│   ├── master/
│   │   ├── parties-crud.spec.ts
│   │   ├── tax-crud.spec.ts
│   │   └── currency-crud.spec.ts
│   │
│   └── accounting/
│       ├── coa-crud.spec.ts
│       └── period-crud.spec.ts
│
├── helpers/
│   ├── wait-for-server.ts     # HTTP polling until /login 200
│   ├── tomselect.helper.ts    # TomSelect interaction utilities
│   ├── flatpickr.helper.ts    # Flatpickr date picker utilities
│   ├── autonumeric.helper.ts  # AutoNumeric input utilities
│   └── htmx.helper.ts         # HTMX response waiting utilities
│
└── global-setup.ts            # Start Spring Boot if not running
```

#### 5.2 Playwright Config (`playwright.config.ts`)

```typescript
import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './tests',
  fullyParallel: false,          // Sequential — shared H2 state
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 1 : 0,
  workers: 1,                    // Single worker — H2 is one DB
  reporter: [
    ['html', { open: 'never' }],
    ['junit', { outputFile: 'results/junit.xml' }],
  ],
  
  use: {
    baseURL: process.env.BASE_URL || 'http://localhost:8090',
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
  },

  // Login setup: runs once, saves session cookie
  projects: [
    {
      name: 'auth-setup',
      testMatch: /auth\.setup\.ts/,
    },
    {
      name: 'e2e-tests',
      dependencies: ['auth-setup'],
      use: {
        ...devices['Desktop Chrome'],
        storageState: '.auth/admin.json',
      },
    },
  ],
});
```

**Catatan kritis: `workers: 1` dan `fullyParallel: false`** — karena H2 in-memory adalah satu instance, parallel test akan menyebabkan data race. Setiap CRUD test berpotensi memodifikasi state yang dibutuhkan test lain.

#### 5.3 Authentication Setup (`fixtures/auth.setup.ts`)

```typescript
import { test as setup, expect } from '@playwright/test';

setup('authenticate as admin', async ({ page }) => {
  await page.goto('/login');
  await page.getByLabel('Username').fill('admin');
  await page.getByLabel('Password').fill('admin123');
  await page.getByRole('button', { name: /masuk|login/i }).click();
  
  // Verify redirect to dashboard (bukan /reset-password)
  await expect(page).toHaveURL(/.*dashboard.*/);
  
  // Save storage state (JSESSIONID cookie)
  await page.context().storageState({ path: '.auth/admin.json' });
});
```

#### 5.4 Component Interaction Helpers

Karena UI menggunakan library JavaScript kompleks, helper untuk interaksi non-native diperlukan:

**TomSelect Helper (`helpers/tomselect.helper.ts`):**

```typescript
import { Locator, Page } from '@playwright/test';

export async function selectTomOption(page: Page, selectId: string, searchText: string) {
  // TomSelect renders custom dropdown, bukan native <select>
  const wrapper = page.locator(`#${selectId}`).locator('..').locator('.ts-wrapper');
  await wrapper.locator('.ts-control').click();
  await wrapper.locator('input').fill(searchText);
  // Wait for AJAX dropdown results
  await page.waitForResponse(resp => resp.url().includes('/lookup') && resp.status() === 200);
  await wrapper.locator('.ts-dropdown .option').first().click();
}
```

**HTMX Response Helper (`helpers/htmx.helper.ts`):**

```typescript
import { Page } from '@playwright/test';

export async function submitFormAndWait(page: Page, buttonSelector: string) {
  // HTMX forms return JSON via XHR, then trigger HX-Trigger events
  const responsePromise = page.waitForResponse(
    resp => resp.request().method() === 'POST' && resp.status() < 400
  );
  await page.click(buttonSelector);
  await responsePromise;
  // Wait for HTMX swap to complete
  await page.waitForTimeout(500); // HTMX swap delay
}

export async function waitForHtmxSwap(page: Page) {
  // Wait for htmx:afterSwap event
  await page.evaluate(() => {
    return new Promise<void>(resolve => {
      document.addEventListener('htmx:afterSwap', () => resolve(), { once: true });
      setTimeout(resolve, 2000); // fallback timeout
    });
  });
}
```

**AutoNumeric Helper (`helpers/autonumeric.helper.ts`):**

```typescript
import { Page } from '@playwright/test';

export async function fillNumericField(page: Page, selector: string, value: string) {
  // AutoNumeric intercepts input — must set via AutoNumeric API
  await page.evaluate(({ sel, val }) => {
    const el = document.querySelector(sel) as HTMLInputElement;
    if ((window as any).AutoNumeric) {
      const an = (window as any).AutoNumeric.getAutoNumericElement(el);
      if (an) { an.set(val); return; }
    }
    // Fallback: native input
    el.value = val;
    el.dispatchEvent(new Event('input', { bubbles: true }));
  }, { sel: selector, val: value });
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

      - name: Set up Node.js
        uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: 'npm'
          cache-dependency-path: e2e-tests/package-lock.json

      - name: Install Playwright
        working-directory: e2e-tests
        run: |
          npm ci
          npx playwright install chromium --with-deps

      - name: Build Spring Boot JAR
        run: ./mvnw package -Pe2e -DskipTests -q

      - name: Start Spring Boot (background)
        run: |
          java -jar target/solusi-program-erp-1.2.0.jar \
            --spring.profiles.active=e2e &
          echo $! > spring-boot.pid

      - name: Wait for server ready
        run: |
          for i in $(seq 1 60); do
            if curl -s -o /dev/null -w "%{http_code}" http://localhost:8090/login | grep -q "200"; then
              echo "Server ready after ${i}s"
              exit 0
            fi
            sleep 1
          done
          echo "Server failed to start"
          exit 1

      - name: Run Playwright tests
        working-directory: e2e-tests
        run: npx playwright test --reporter=junit,html
        env:
          BASE_URL: http://localhost:8090

      - name: Stop Spring Boot
        if: always()
        run: |
          if [ -f spring-boot.pid ]; then
            kill $(cat spring-boot.pid) || true
          fi

      - name: Upload test results
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: playwright-report
          path: |
            e2e-tests/results/
            e2e-tests/playwright-report/
            e2e-tests/test-results/
          retention-days: 14
```

#### 6.2 Developer Local Workflow

```bash
# Terminal 1: Start E2E server
./mvnw spring-boot:run -Pe2e -Dspring-boot.run.profiles=e2e

# Terminal 2: Run tests (setelah server ready)
cd e2e-tests
npm test                    # Headless
npx playwright test --ui    # Interactive UI mode untuk debugging
```

---

### 7. Cakupan Test Minimum per Modul

Berikut daftar test cases minimum yang WAJIB ada untuk setiap modul. Total: **~45 test cases**.

| Modul | Test File | Test Cases | Prioritas |
|-------|-----------|------------|-----------|
| **Auth** | `login.spec.ts` | (1) Login valid admin/admin123, (2) Login invalid credentials → error message, (3) Logout → redirect /login | **P0** |
| **Security > Users** | `users-crud.spec.ts` | (4) List users menampilkan admin, (5) Create user baru via form, (6) Edit user (ubah email), (7) Delete user non-admin | **P0** |
| **Security > Roles** | `roles-crud.spec.ts` | (8) List roles menampilkan ROLE_ADMIN, (9) Create role baru dengan permission, (10) Edit role (toggle permissions) | **P1** |
| **Inventory > Brands** | `brands-crud.spec.ts` | (11) List brands, (12) Create brand, (13) Edit brand, (14) Delete brand, (15) Validasi: nama duplikat ditolak | **P0** |
| **Inventory > Product Categories** | `product-categories-crud.spec.ts` | (16) List, (17) Create dengan type STOCK, (18) Edit, (19) Delete | **P0** |
| **Inventory > Products** | `products-crud.spec.ts` | (20) List products, (21) Create product (TomSelect category + UoM + brand), (22) Edit product, (23) Delete | **P1** |
| **Inventory > UoM** | `uom-list.spec.ts` | (24) List unit of measures (42 seeded items), (25) Verify pagination | **P2** |
| **Inventory > Adjustments** | `adjustments-flow.spec.ts` | (26) Create adjustment draft, (27) Add line items, (28) Submit for approval, (29) Verify status change | **P1** |
| **Master > Parties** | `parties-crud.spec.ts` | (30) List parties, (31) Create ORGANIZATION party, (32) Add address with geographic lookup (TomSelect), (33) Add contact, (34) Edit party | **P1** |
| **Master > Tax** | `tax-crud.spec.ts` | (35) List taxes, (36) Create tax, (37) Edit, (38) Delete | **P0** |
| **Master > Currency** | `currency-crud.spec.ts` | (39) List currencies (8 seeded), (40) Verify IDR as default | **P2** |
| **Accounting > COA** | `coa-crud.spec.ts` | (41) List COA tree, (42) Create akun baru dengan parent (TomSelect), (43) Edit | **P1** |
| **Accounting > Period** | `period-crud.spec.ts` | (44) Create fiscal year, (45) Verify auto-generated periods | **P1** |

**Coverage target Phase 1:** Semua P0 (18 test cases) — confidence bahwa modul inti tidak broken.

---

### 8. Risiko & Mitigasi

| # | Risiko | Probabilitas | Dampak | Mitigasi |
|---|--------|-------------|--------|----------|
| 1 | **H2 ENUM behavior berbeda** — `@Enumerated(EnumType.STRING)` tidak mapped ke H2 ENUM column correctly | Medium | High | Validasi di awal: buat integration test Java sederhana yang INSERT + SELECT setiap entity dengan ENUM field. Jika gagal, patch migration mengganti ENUM → VARCHAR + CHECK constraint |
| 2 | **Flyway checksum drift** — developer lupa sync migration-h2 setelah modifikasi migration produksi | High | High | CI check: tambahkan step yang membandingkan jumlah file dan nama file di `db/migration/` vs `db/migration-h2/`. Script `sync-migrations.sh` untuk automate patching |
| 3 | **SystemInitializer race condition** — password_change_required di-set ulang setelah V900 | High | High | Gunakan `E2eInitializer` dengan `@Order(LOWEST_PRECEDENCE)` yang berjalan SETELAH SystemInitializer (lihat Section 4.6) |
| 4 | **Flaky tests karena H2 speed** — H2 in-memory terlalu cepat, HTMX swap belum selesai saat assertion dijalankan | Medium | Medium | Semua assertion menggunakan Playwright auto-wait (`toBeVisible`, `toHaveText`) daripada hardcoded timeout. Helper `waitForHtmxSwap()` untuk operasi HTMX |
| 5 | **V15 geographic data (514 rows) membuat startup lambat** | Low | Low | Monitor startup time. Jika >30s, buat V15 H2-version yang hanya seed 10 provinsi + 20 kota (cukup untuk testing) |
| 6 | **MinIO dependency di E2E** — approval signatures memerlukan object storage | Medium | Medium | Profile E2E menggunakan dummy MinIO config. Controller yang upload ke MinIO akan fail gracefully. Jika perlu: mock MinIO endpoint atau gunakan testcontainers MinIO |
| 7 | **H2 MODE=MySQL MODIFY COLUMN syntax** — V17 (audit refactor) memiliki ~30 MODIFY COLUMN statements, semua harus di-patch | Low (one-time) | High | Automated sed/script untuk konversi massal `MODIFY col_name TYPE NOT NULL` → `ALTER COLUMN col_name SET NOT NULL`. Validasi dengan Flyway dry-run |
| 8 | **Browser-specific rendering** — Playwright Chromium mungkin render Thymeleaf+TomSelect berbeda dari Firefox | Low | Low | Fase awal gunakan Chromium only. Tambahkan Firefox/WebKit nanti jika diperlukan |

---

### 9. Effort Estimate

| Fase | Task | Effort (man-days) | Dependencies |
|------|------|--------------------|--------------|
| **Fase 1: Infrastructure** | | **8 MD** | |
| | Patch 45 migration files → migration-h2/ | 3 MD | Katalog incompatibility selesai |
| | Create application-e2e.yaml + Maven profile | 0.5 MD | — |
| | Create E2eInitializer.java | 0.5 MD | — |
| | Create V900-V903 seeding scripts | 1 MD | migration-h2 selesai |
| | Validasi: Spring Boot startup sukses dengan H2 | 1 MD | Semua di atas |
| | Debug + fix H2 incompatibility yang terlewat | 2 MD | Buffer |
| **Fase 2: Playwright Setup** | | **4 MD** | |
| | Init e2e-tests/ project (package.json, config, tsconfig) | 0.5 MD | — |
| | Auth setup + login/logout test | 1 MD | Fase 1 selesai |
| | Page Object Models (LoginPage, Sidebar, Dashboard) | 1 MD | — |
| | Component helpers (TomSelect, Flatpickr, AutoNumeric, HTMX) | 1.5 MD | — |
| **Fase 3: P0 Test Cases** | | **5 MD** | |
| | Auth tests (login, logout, invalid credentials) | 0.5 MD | Fase 2 |
| | Users CRUD tests | 1 MD | Fase 2 |
| | Brands CRUD tests | 0.5 MD | Fase 2 |
| | Product Categories CRUD tests | 0.5 MD | Fase 2 |
| | Tax CRUD tests | 0.5 MD | Fase 2 |
| | Debug + stabilize flaky tests | 2 MD | Buffer |
| **Fase 4: P1 Test Cases** | | **5 MD** | |
| | Products CRUD (TomSelect heavy) | 1.5 MD | Fase 3 |
| | Parties CRUD (complex form + sub-entities) | 1.5 MD | Fase 3 |
| | Accounting COA + Periods | 1 MD | Fase 3 |
| | Adjustments flow (multi-step) | 1 MD | Fase 3 |
| **Fase 5: CI/CD** | | **2 MD** | |
| | GitHub Actions workflow | 1 MD | Fase 3 minimal |
| | Migration sync check script | 0.5 MD | — |
| | Documentation | 0.5 MD | — |
| **TOTAL** | | **24 MD** | ~5 minggu (1 developer) |

---

### 10. Trade-offs

| Keputusan | Pro | Kontra | Justifikasi |
|-----------|-----|--------|-------------|
| **H2 in-memory vs. Testcontainers MariaDB** | Zero external dependency, startup <10s, CI tanpa Docker | SQL behavior mungkin berbeda di edge case; dual migration maintenance | H2 dipilih karena constraint CI tanpa Docker. Testcontainers bisa ditambahkan sebagai alternatif nanti tanpa mengubah test Playwright |
| **Dual migration copy vs. Flyway callbacks** | Full control per-migration, clear separation | 45 file duplikasi, maintenance burden saat migration baru ditambahkan | Callbacks tidak mendukung per-statement patching. Duplikasi satu kali, maintenance berikutnya incremental (setiap migration baru di-patch juga) |
| **Single worker vs. parallel workers** | Deterministic, no data race, simpler seeding | Slower test execution (~5min untuk 45 tests) | H2 single in-memory database tidak support multiple simultaneous workers. Parallel memerlukan database-per-worker atau reset strategy yang jauh lebih kompleks |
| **Session cookie reuse vs. login per test** | Fast — login sekali, reuse JSESSIONID | Session state leaks between tests | Playwright `storageState` pattern standar. Test isolation dijaga melalui test ordering dan unique entity codes (E2E- prefix) |
| **V900+ versioning vs. afterMigrate.sql** | Clear execution order, Flyway checksum protection | Extra files | afterMigrate.sql terlalu fragile — dijalankan setiap kali Flyway migrate, termasuk di produksi jika tidak hati-hati. V900+ eksplisit, hanya dijalankan di lokasi migration-e2e |
| **E2eInitializer @Component vs. modifikasi SystemInitializer** | Zero perubahan pada kode produksi | Satu class tambahan hanya untuk E2E | Prinsip: kode produksi TIDAK BOLEH memiliki awareness terhadap testing concern. `@Profile("e2e")` memastikan bean ini tidak pernah aktif di prod |
| **Chromium-only vs. multi-browser** | Faster CI, less flakiness | Tidak mendeteksi Firefox/Safari bug | 95%+ user ERP menggunakan Chrome. Multi-browser bisa ditambahkan di fase lanjut tanpa refactoring |
| **Page Object Model vs. inline selectors** | Maintainable, reusable, test refactoring mudah | Lebih banyak kode awal | Investasi awal terbayar saat template berubah — cukup update satu POM file, bukan semua test |
| **Full seeding via SQL vs. API-based seeding** | Database state konsisten, no dependency pada API correctness | Tight coupling ke schema, harus update saat schema berubah | Aplikasi ini SSR, bukan API-first. Tidak ada public API endpoints untuk seeding. SQL seeding adalah satu-satunya opsi reliable yang idempotent |
| **Stripped V15 geographic (10 provinsi) vs. full 514 rows** | Startup 2x lebih cepat | Kurang representatif untuk geographic lookup testing | Rekomendasi: mulai dengan full data. Jika startup >20s, switch ke stripped version. Geographic lookup di TomSelect hanya perlu beberapa opsi untuk verifikasi |