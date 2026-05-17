# Implementation Plan: Playwright E2E — Proof of Concept

> Source: docs/proposals/e2e-playwright/FINAL-PROPOSAL.md
> Created: 2026-05-17
> Status: PENDING

## Summary

Proof of Concept untuk memvalidasi feasibility Playwright E2E test dengan H2 in-memory database. Fokus pada 3 hal: (1) apakah Flyway migrations bisa jalan di H2 dengan patching minimal, (2) apakah Spring Boot bisa start dengan profile e2e, (3) apakah satu test login bisa pass end-to-end. Jika PoC berhasil, lanjut ke full implementation. Jika H2 patching >40% migration gagal, pivot ke Testcontainers.

## Tasks

### Task 1: Maven Profile & H2 Dependency
Tambahkan Maven profile `e2e` yang meng-include H2 database sebagai runtime dependency.

**Depends on:** (none)
**Reference:** pom.xml (current, no profiles section exists)

Steps:
- [ ] Tambahkan `<profiles>` section di pom.xml dengan profile id `e2e`
- [ ] Tambahkan H2 dependency (`com.h2database:h2`) dengan scope `runtime` di dalam profile
- [ ] Verifikasi build: `mvnw -B compile -Pe2e` harus sukses tanpa error

**Validation criteria:**
- `mvnw -B compile -Pe2e` exit code 0
- H2 jar muncul di classpath saat profile aktif

---

### Task 2: application-e2e.yaml Configuration
Buat Spring profile configuration untuk E2E yang menggunakan H2 in-memory dengan MODE=MySQL.

**Depends on:** Task 1
**Reference:** src/main/resources/application.yaml (existing production config)

Steps:
- [ ] Buat file `src/main/resources/application-e2e.yaml`
- [ ] Konfigurasi datasource H2 in-memory:
      - URL: `jdbc:h2:mem:erp_e2e;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;NON_KEYWORDS=VALUE`
      - Driver: `org.h2.Driver`
      - Username: `sa`, password kosong
- [ ] Set server port: `18080`
- [ ] Set session cookie secure: `false` (HTTP localhost)
- [ ] Set JPA platform: `org.hibernate.dialect.H2Dialect`
- [ ] Set `hibernate.globally_quoted_identifiers: true`
- [ ] Set Flyway locations (override first):
      - `classpath:db/migration-h2`
      - `classpath:db/migration`
- [ ] Set `flyway.out-of-order: true`
- [ ] Set `flyway.baseline-on-migrate: true`
- [ ] Disable thymeleaf cache
- [ ] Set logging: root WARN, com.solusi.erp INFO, org.flywaydb INFO

**Validation criteria:**
- File exists dan YAML syntax valid
- Tidak ada referensi ke MariaDB di file ini

---

### Task 3: H2 Migration Compatibility Audit & Patching
Jalankan Flyway migrations di H2 dan patch yang gagal. Ini adalah task kritis yang menentukan feasibility.

**Depends on:** Task 2
**Reference:** src/main/resources/db/migration/ (63 migration files, V1–V63)

**Known incompatibilities dari audit:**
- `ENGINE=InnoDB` — 15+ migrations (V1, V10, V11, V12, V13, V16, V19, V21, V23, dll)
- `DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci` — V21, V23
- `DATETIME(6)` — V21, V23
- `MODIFY COLUMN` — V44, V51
- `COMMENT '...'` — V44
- `FROM DUAL` — V37
- `ON DUPLICATE KEY UPDATE` — V40, V43, V46, V48, V50, V55, V58 (should work in MODE=MySQL)
- `INSERT IGNORE` — V45, V46, V50, V55, V58 (should work in MODE=MySQL)

Steps:
- [ ] Buat folder `src/main/resources/db/migration-h2/`
- [ ] Attempt pertama: jalankan app dengan profile e2e, catat SEMUA migration yang gagal
      ```
      mvnw spring-boot:run -Pe2e -Dspring-boot.run.profiles=e2e
      ```
- [ ] Untuk setiap migration yang gagal, buat override di `db/migration-h2/` dengan nama file IDENTIK:
      - Hapus `ENGINE=InnoDB`
      - Hapus `DEFAULT CHARSET=...` dan `COLLATE ...`
      - Ganti `DATETIME(6)` → `TIMESTAMP(6)` jika H2 menolak
      - Ganti `MODIFY COLUMN x TYPE` → `ALTER COLUMN x SET DATA TYPE TYPE`
      - Hapus `COMMENT '...'` clause
      - Hapus `FROM DUAL` (H2 tidak butuh)
- [ ] Re-run app, iterasi sampai SEMUA migration pass atau tentukan fallback
- [ ] **DECISION POINT:** Jika >25 migration perlu patch (>40%), dokumentasikan dan pivot ke fallback (ddl-auto=create atau Testcontainers)
- [ ] Catat jumlah final: berapa migration di-override vs total

**Validation criteria:**
- Spring Boot start dengan profile e2e tanpa Flyway error
- Semua 63 migrations executed successfully (cek log: `Successfully applied X migrations`)
- ATAU: decision documented untuk fallback approach

---

### Task 4: E2E Seed Data (V9000)
Buat minimal seed data untuk PoC — cukup 1 test user yang bisa login.

**Depends on:** Task 3 (migrations harus pass dulu)
**Reference:** SystemInitializer.java (handles admin password), V2__Seed_Security_Data.sql (existing seed pattern)

Steps:
- [ ] Buat `src/main/resources/db/migration-h2/V9000__e2e_seed_data.sql`
- [ ] Seed 1 test user `e2e_admin` dengan:
      - BCrypt hash untuk password `Test1234!` (pre-computed: `$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy` atau generate fresh)
      - `password_change_required = false`
      - `enabled = true`
- [ ] Assign role ADMIN ke user tersebut (gunakan existing role dari V2 seed)
- [ ] Pastikan SQL compatible dengan H2 syntax

**Validation criteria:**
- App start tanpa error pada V9000 migration
- User `e2e_admin` ada di database (verifikasi via H2 console jika perlu)

---

### Task 5: Playwright Project Setup (Minimal)
Setup project Node.js minimal di `e2e-tests/` — hanya yang dibutuhkan untuk 1 test.

**Depends on:** (none, bisa paralel dengan Task 1-4)

Steps:
- [ ] Buat folder `e2e-tests/`
- [ ] Inisialisasi `package.json` dengan:
      - name: `erp-e2e-tests`
      - scripts: `test`, `test:headed`, `test:debug`
      - devDependencies: `@playwright/test` (latest)
      - TypeScript: `typescript`, `@types/node`
- [ ] Buat `tsconfig.json` (strict, ES2020, NodeNext)
- [ ] Buat `playwright.config.ts`:
      - baseURL: `http://localhost:18080`
      - timeout: 30s
      - workers: 1
      - fullyParallel: false
      - retries: 0 (PoC, no retry)
      - reporter: list
      - projects: chromium only
- [ ] Buat `.gitignore` (node_modules, test-results, playwright-report, .auth)
- [ ] Run `npm install` dan `npx playwright install chromium`

**Validation criteria:**
- `cd e2e-tests && npx playwright test --list` runs tanpa error (0 tests found is OK)

---

### Task 6: Login Test Spec (The Actual PoC Test)
Tulis 1 test spec yang membuktikan full E2E flow: browser → login form → authenticated dashboard.

**Depends on:** Task 4 + Task 5

Steps:
- [ ] Buat `e2e-tests/tests/auth/login.spec.ts`
- [ ] Test case 1: "should login successfully with valid credentials"
      - Navigate ke `/login`
      - Fill username: `e2e_admin`
      - Fill password: `Test1234!`
      - Click submit button
      - Assert: URL berubah ke `/dashboard` atau halaman utama
      - Assert: ada elemen yang menunjukkan user logged in (misal: username di navbar)
- [ ] Test case 2: "should show error with invalid credentials"
      - Navigate ke `/login`
      - Fill username: `wrong_user`
      - Fill password: `wrong_pass`
      - Click submit
      - Assert: tetap di halaman login
      - Assert: ada error message visible
- [ ] Test case 3: "should redirect unauthenticated user to login"
      - Navigate langsung ke `/dashboard` tanpa login
      - Assert: redirect ke `/login`

**Validation criteria:**
- Semua 3 test cases PASS saat dijalankan terhadap running Spring Boot e2e instance
- `npx playwright test tests/auth/login.spec.ts` exit code 0

---

### Task 7: End-to-End Validation Script
Buat script yang menjalankan full pipeline: build → start server → run tests → cleanup.

**Depends on:** Task 6

Steps:
- [ ] Buat `e2e-tests/scripts/run-poc.ps1` (Windows PowerShell untuk local dev):
      - Build JAR: `mvnw -B package -DskipTests -Pe2e -q`
      - Start server sebagai background process
      - Wait for health check (poll `/login` sampai HTTP 200, max 60s)
      - Run playwright tests
      - Kill server process
      - Exit dengan test exit code
- [ ] Buat `e2e-tests/scripts/run-poc.sh` (Bash untuk CI/Linux):
      - Same flow as PowerShell version
- [ ] Jalankan script end-to-end dan verifikasi semua pass

**Validation criteria:**
- Script berjalan dari awal sampai akhir tanpa manual intervention
- Exit code 0 jika semua test pass
- Server process terminated setelah test selesai

---

### Task 8: CI Integration (Minimal)
Tambahkan job E2E ke existing CI workflow — conditional, hanya pada main push dan schedule.

**Depends on:** Task 7
**Reference:** .github/workflows/ci-java21.yml (existing: fast-tests, full-tests, deploy jobs)

Steps:
- [ ] Tambahkan job `e2e-tests` di ci-java21.yml:
      - `needs: [full-tests]`
      - Trigger: hanya pada `schedule`, `workflow_dispatch`, dan `push` ke main
      - Setup: JDK 21 + Node 20
      - Build JAR dengan `-Pe2e`
      - Start server background
      - Health check (wait for `/login` 200)
      - Install playwright + chromium
      - Run tests
      - Upload artifacts (playwright-report) always
      - Kill server in cleanup
- [ ] Set `timeout-minutes: 15`
- [ ] Set `concurrency` group untuk E2E

**Validation criteria:**
- YAML syntax valid (bisa di-lint)
- Job hanya trigger pada kondisi yang benar (tidak setiap PR push)
- Artifact upload configured

---

## Decision Points & Exit Criteria

### PoC SUCCESS jika:
1. Spring Boot start dengan H2 + Flyway migrations (Task 3 pass)
2. Login test pass end-to-end (Task 6 pass)
3. Total H2 migration patches ≤ 25 files (manageable maintenance burden)

### PoC FAIL → Pivot jika:
1. >40% migration (>25 files) perlu patch → Pivot ke **Testcontainers + MariaDB**
2. H2 MODE=MySQL tidak support critical SQL patterns (stored procedures, triggers) → Pivot ke **Testcontainers**
3. Spring Security session handling berbeda di H2 → Investigate, mungkin fixable

### Setelah PoC berhasil:
- Lanjut ke full implementation sesuai FINAL-PROPOSAL.md
- Expand helpers (TomSelect, Flatpickr, AutoNumeric)
- Expand test specs (Brand CRUD, Product, Stock Adjustment)
- Expand seed data (6 users, master data)
