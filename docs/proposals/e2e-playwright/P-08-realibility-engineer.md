# Proposal Teknis: Playwright E2E Test — Perspektif Reliability Engineer

> **Persona:** Reliability Engineer — Zero Flaky Tests, Smart Waits, Deterministic Everything
> **Proyek:** Solusi ERP (Spring Boot 4.0.3 + Thymeleaf SSR)
> **Versi:** 1.0 | Tanggal: Juli 2025

---

### 1. Executive Summary

Proyek Solusi ERP memiliki 45 controller, 45 Flyway migration, dan UI kompleks (TomSelect, Flatpickr, AutoNumeric, HTMX). Saat ini tidak ada automated E2E test — bug frontend baru ketahuan saat runtime. Proposal ini merancang **Playwright E2E test suite yang reliability-first**: setiap test HARUS pass 100/100 run tanpa intermittent failure.

Arsitektur: Spring Boot berjalan di profil `e2e` dengan H2 `MODE=MySQL`, database di-reset per test suite via Flyway clean+migrate. Playwright berjalan di subdirektori `e2e-tests/` terpisah dari Maven. Setiap test terisolasi melalui **deterministic data seeding**, **smart wait** berbasis DOM/network state (bukan `setTimeout`), dan **retry-aware helper** dengan idempotency guarantee. CI menggunakan single Maven process yang start server, lalu trigger `npx playwright test`, dengan flakiness detection dan quarantine otomatis.

**Target: 0% flaky rate, full module coverage dalam 12 minggu.**

---

### 2. Arsitektur Solusi

#### 2.1 Topology Overview

```
┌─────────────────────────────────────────────────────────────────────┐
│                        CI RUNNER (GitHub Actions)                    │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ┌──────────────────────┐    HTTP     ┌──────────────────────────┐  │
│  │ Spring Boot (E2E)    │◄───────────►│ Playwright Test Runner   │  │
│  │ Profile: e2e         │  :18080     │ @playwright/test         │  │
│  │ H2 MODE=MySQL        │             │ Node.js 20+              │  │
│  │ Flyway migrations    │             │ Chromium headless        │  │
│  │ SystemInitializer    │             │                          │  │
│  │ MinIO: mock/disabled │             │ e2e-tests/               │  │
│  └──────────────────────┘             └──────────────────────────┘  │
│           │                                      │                  │
│           ▼                                      ▼                  │
│  ┌──────────────────┐              ┌───────────────────────────┐   │
│  │ H2 In-Memory DB  │              │ Test Reports              │   │
│  │ MODE=MySQL        │              │ - HTML report             │   │
│  │ Auto-reset per    │              │ - JUnit XML (CI parsing)  │   │
│  │   suite run       │              │ - Trace files (on fail)   │   │
│  └──────────────────┘              │ - Screenshots (on fail)   │   │
│                                    │ - Flakiness dashboard     │   │
│                                    └───────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────┘
```

#### 2.2 Separation of Concerns

| Layer | Teknologi | Lokasi | Tanggung Jawab |
|-------|-----------|--------|----------------|
| Application Under Test | Spring Boot 4.0.3 + H2 | `src/` + `application-e2e.yaml` | Server target, database in-memory |
| Test Runner | @playwright/test | `e2e-tests/` | Test execution, assertions, reporting |
| Test Helpers | TypeScript modules | `e2e-tests/src/helpers/` | Component interactions (TomSelect, Flatpickr, dll.) |
| Page Objects | TypeScript classes | `e2e-tests/src/pages/` | Encapsulate page navigation & elements |
| Test Data | SQL seed scripts | `e2e-tests/fixtures/` | Deterministic, idempotent seed data |
| CI Glue | Shell/Maven wrapper | `.github/workflows/` | Orchestrate: build → start → test → report |

#### 2.3 Direktori `e2e-tests/`

```
e2e-tests/
├── package.json
├── playwright.config.ts
├── tsconfig.json
├── .gitignore                          # node_modules/
├── src/
│   ├── helpers/
│   │   ├── reliability/
│   │   │   ├── smart-wait.ts           # DOM-aware wait utilities
│   │   │   ├── network-sentinel.ts     # Network idle/response interceptors
│   │   │   ├── retry-action.ts         # Idempotent retry wrapper
│   │   │   └── health-check.ts         # Server readiness verifier
│   │   ├── components/
│   │   │   ├── tom-select.helper.ts    # TomSelect with retry + readiness check
│   │   │   ├── flatpickr.helper.ts     # Flatpickr with instance verification
│   │   │   ├── auto-numeric.helper.ts  # AutoNumeric with existence guard
│   │   │   ├── htmx.helper.ts          # HTMX swap completion waiter
│   │   │   ├── ajax-form.helper.ts     # AJAX form submit + response interceptor
│   │   │   └── line-item.helper.ts     # Dynamic row add + component init waiter
│   │   ├── auth.helper.ts              # Login with session reuse + password change handler
│   │   └── data-factory.ts             # Deterministic unique test data generator
│   ├── pages/
│   │   ├── login.page.ts
│   │   ├── dashboard.page.ts
│   │   ├── security/
│   │   │   ├── user-list.page.ts
│   │   │   └── user-form.page.ts
│   │   ├── master/
│   │   │   ├── party-list.page.ts
│   │   │   ├── party-form.page.ts
│   │   │   ├── currency-list.page.ts
│   │   │   ├── currency-form.page.ts
│   │   │   ├── tax-list.page.ts
│   │   │   └── tax-form.page.ts
│   │   ├── inventory/
│   │   │   ├── brand-list.page.ts
│   │   │   ├── brand-form.page.ts
│   │   │   ├── product-list.page.ts
│   │   │   ├── product-form.page.ts
│   │   │   ├── product-category-list.page.ts
│   │   │   ├── product-category-form.page.ts
│   │   │   ├── uom-list.page.ts
│   │   │   ├── facility-list.page.ts
│   │   │   ├── facility-form.page.ts
│   │   │   └── stock-adjustment-form.page.ts
│   │   └── accounting/
│   │       ├── coa-list.page.ts
│   │       ├── coa-form.page.ts
│   │       └── period-list.page.ts
│   └── tests/
│       ├── auth/
│       │   └── login.spec.ts
│       ├── security/
│       │   ├── user-crud.spec.ts
│       │   └── role-crud.spec.ts
│       ├── master/
│       │   ├── party-crud.spec.ts
│       │   ├── currency-crud.spec.ts
│       │   └── tax-crud.spec.ts
│       ├── inventory/
│       │   ├── brand-crud.spec.ts
│       │   ├── product-category-crud.spec.ts
│       │   ├── product-crud.spec.ts
│       │   ├── uom-crud.spec.ts
│       │   ├── facility-crud.spec.ts
│       │   └── stock-adjustment-flow.spec.ts
│       └── accounting/
│           ├── coa-crud.spec.ts
│           └── period-management.spec.ts
├── fixtures/
│   └── e2e-seed.sql                    # Tambahan seed data beyond Flyway
└── scripts/
    ├── wait-for-server.ts              # Poll server health before tests
    └── flakiness-report.ts             # Analyze test result history
```

---

### 3. Spring Boot E2E Profile Design

#### 3.1 Profile: `application-e2e.yaml`

```yaml
# src/main/resources/application-e2e.yaml
server:
  port: 18080
  servlet:
    session:
      timeout: 30m
      cookie:
        secure: false        # KRITIS: H2 E2E berjalan di localhost HTTP
        http-only: true

spring:
  datasource:
    url: jdbc:h2:mem:erp_e2e;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;IGNORECASE=TRUE
    username: sa
    password:
    driver-class-name: org.h2.Driver
  
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: none        # Flyway controls schema
    show-sql: false          # Reduce noise in CI logs
  
  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration
    # H2 akan menjalankan semua 45 migration files
    # MariaDB-specific syntax yang perlu di-handle:
    #   ENGINE=InnoDB → diabaikan oleh H2 MODE=MySQL
    #   AUTO_INCREMENT → didukung oleh H2 MODE=MySQL
  
  h2:
    console:
      enabled: true          # Debug access di /h2-console
      path: /h2-console

  thymeleaf:
    cache: false

# MinIO: disabled untuk E2E (approval signatures tidak di-test secara file storage)
minio:
  endpoint: http://localhost:19000
  presigned-endpoint: http://localhost:19000
  access-key: minioadmin
  secret-key: minioadmin
  buckets:
    signatures: test-signatures

logging:
  level:
    com.solusi.erp: WARN     # Reduce noise
    org.springframework.security: WARN
    org.hibernate.SQL: OFF
    org.flywaydb: INFO
```

#### 3.2 H2 Compatibility Layer

Problem: 45 Flyway migration scripts menggunakan MariaDB-specific syntax (`ENGINE=InnoDB`, `NOW()`, dll). H2 `MODE=MySQL` sudah menangani sebagian besar, tapi beberapa edge case memerlukan perhatian.

**Strategi kompatibilitas:**

| MariaDB Syntax | H2 MODE=MySQL Support | Action |
|---|---|---|
| `ENGINE=InnoDB` | ✅ Diabaikan otomatis | Tidak perlu modifikasi |
| `AUTO_INCREMENT` | ✅ Didukung | Tidak perlu modifikasi |
| `BIGINT AUTO_INCREMENT PRIMARY KEY` | ✅ Didukung | Tidak perlu modifikasi |
| `NOW()` | ✅ Didukung | Tidak perlu modifikasi |
| `ON DELETE CASCADE` | ✅ Didukung | Tidak perlu modifikasi |
| `DATETIME` | ✅ Mapped ke `TIMESTAMP` | Tidak perlu modifikasi |
| `BOOLEAN` | ✅ Didukung | Tidak perlu modifikasi |
| `TEXT`, `LONGTEXT` | ✅ Didukung | Tidak perlu modifikasi |
| MariaDB-specific functions | ⚠️ Case-by-case | Buat `afterMigrate` callback jika perlu |

**Jika migration gagal di H2**, opsi fallback:

```
src/main/resources/
├── db/migration/              # Production migrations (MariaDB)
└── db/migration-h2/           # H2-specific overrides (hanya jika diperlukan)
```

Dengan Flyway config:
```yaml
# application-e2e.yaml (hanya jika ada migration yang incompatible)
spring:
  flyway:
    locations: classpath:db/migration-h2,classpath:db/migration
```

**Rekomendasi: Coba jalankan semua 45 migration di H2 MODE=MySQL dulu.** Hanya buat override migration jika ada yang gagal. Dari analisis V1 dan V2, syntax yang digunakan seharusnya compatible.

#### 3.3 Maven Dependency Addition

```xml
<!-- pom.xml — tambah H2 untuk profile e2e -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```

> **Catatan:** Scope `test` sudah cukup karena E2E profile hanya aktif saat testing. Namun jika server dijalankan dengan `mvnw spring-boot:run -Dspring-boot.run.profiles=e2e`, scope harus diubah ke `runtime` dengan `<classifier>` atau gunakan Maven profile:

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

Start command:
```bash
./mvnw spring-boot:run -Pe2e -Dspring-boot.run.profiles=e2e
```

#### 3.4 MinIO Handling untuk E2E

Approval module menggunakan MinIO untuk signature storage. Opsi:

1. **Mock MinIO Bean** — Buat `@Profile("e2e")` bean yang return no-op MinioClient
2. **Jalankan MinIO container** — Overhead tambahan tapi realistic
3. **Skip approval signature tests** — Paling pragmatis untuk fase awal

**Rekomendasi: Opsi 1** — Buat conditional bean:

```java
@Configuration
@Profile("e2e")
public class E2eMinioConfig {
    @Bean
    public MinioClient minioClient() {
        // Return a MinIO client pointing to a non-existent server
        // Operations that fail will be caught by existing error handling
        return MinioClient.builder()
            .endpoint("http://localhost:19000")
            .credentials("minioadmin", "minioadmin")
            .build();
    }
}
```

---

### 4. Data Seeding Strategy

#### 4.1 Prinsip: Deterministic, Idempotent, Isolated

| Prinsip | Implementasi |
|---------|-------------|
| **Deterministic** | Setiap test run menghasilkan state yang sama. Tidak ada `Random`, `UUID.randomUUID()`, atau `NOW()` untuk business data |
| **Idempotent** | Flyway clean+migrate menghasilkan database identik di setiap run |
| **Isolated** | Setiap test suite dimulai dari state yang sama (fresh database) |
| **Unique per test** | Setiap test menggunakan suffix unik (timestamp/counter) untuk data yang dibuatnya |

#### 4.2 Database Lifecycle

```
┌─────────────────┐
│ Maven start     │
│ Profile: e2e    │
├─────────────────┤
│ Flyway migrate  │──► 45 migration scripts dijalankan
│ (automatic)     │    termasuk V2 seed: admin user, roles, permissions
├─────────────────┤
│ SystemInitializer│──► admin password → BCrypt("admin123")
│ (CommandLineRunner)  password_change_required = true
├─────────────────┤
│ Server Ready    │──► Port 18080 open, /login accessible
└─────────────────┘
         │
         ▼
┌─────────────────┐
│ Playwright      │
│ globalSetup     │
├─────────────────┤
│ 1. Health check │──► Poll GET /login hingga 200 (max 60s)
│ 2. Login admin  │──► POST /login (username=admin, password=admin123)
│ 3. Handle pwd   │──► Jika redirect /reset-password, set password baru
│    change        │    (admin123 → admin123, skip change requirement)
│ 4. Save session │──► storageState.json (cookies + localStorage)
│ 5. Seed extra   │──► Via HTTP API/form submission — buat master data dasar
│    data          │    yang dibutuhkan multiple tests
└─────────────────┘
         │
         ▼
┌─────────────────┐
│ Test Execution  │──► Setiap test reuse session dari storageState.json
│ (parallel-safe) │    Setiap test buat data sendiri dengan unique identifier
└─────────────────┘
```

#### 4.3 Extra Seed Data via Global Setup

Flyway V2 hanya membuat admin user + roles + permissions. Master data (UoM, brand, category) **belum ada**. Kita perlu seed sebelum tests berjalan.

**Strategi: Seed via HTTP API (bukan direct SQL insert)**

Kenapa HTTP API bukan direct SQL?
- Melewati semua business logic, validation, dan sequence generator
- Membuktikan bahwa create flow benar-benar bekerja
- Tidak perlu tahu internal schema/column names
- Decoupled dari database implementation

```typescript
// e2e-tests/src/helpers/global-seed.ts
import { request } from '@playwright/test';

export async function seedMasterData(baseURL: string, storageStatePath: string) {
  const ctx = await request.newContext({ 
    baseURL, 
    storageState: storageStatePath 
  });

  // Seed Brand
  await seedIfNotExists(ctx, '/inventory/brands', {
    name: 'E2E-BRAND-DEFAULT'
  });

  // Seed Product Category
  await seedIfNotExists(ctx, '/inventory/product-categories', {
    name: 'E2E-CATEGORY-DEFAULT',
    type: 'GOODS'
  });

  // Seed Unit of Measure (cek dulu apakah sudah ada dari migration)
  // V5 migration mungkin sudah membuat UoM dasar

  // Seed Facility / Warehouse
  await seedIfNotExists(ctx, '/inventory/facilities', {
    name: 'E2E-WAREHOUSE-DEFAULT',
    code: 'E2E-WH-01'
  });

  await ctx.dispose();
}
```

#### 4.4 Test Data Factory — Unique, Collision-Free

```typescript
// e2e-tests/src/helpers/data-factory.ts

/**
 * Generates unique, deterministic test data per test.
 * Uses test title hash + atomic counter to prevent collisions
 * even in parallel execution.
 */
let counter = 0;

export function uniqueId(prefix: string): string {
  const ts = Date.now().toString(36);   // compact timestamp
  const seq = (++counter).toString(36); // monotonic counter
  return `${prefix}-${ts}-${seq}`.toUpperCase();
}

export function testBrand() {
  return {
    name: uniqueId('BRAND'),
  };
}

export function testProductCategory() {
  return {
    name: uniqueId('CAT'),
    type: 'GOODS',
  };
}

export function testProduct(overrides?: Partial<ProductData>) {
  return {
    name: uniqueId('PROD'),
    sku: uniqueId('SKU'),
    ...overrides,
  };
}

export function testParty(overrides?: Partial<PartyData>) {
  return {
    name: uniqueId('PARTY'),
    type: 'COMPANY',
    roleType: 'SUPPLIER',
    ...overrides,
  };
}

export function testCurrency() {
  return {
    code: uniqueId('CUR').substring(0, 3),
    name: uniqueId('CURRENCY'),
    symbol: '$',
  };
}
```

#### 4.5 Password Change Required — First-Login Handler

`SystemInitializer` sets `password_change_required = true`. First login AKAN redirect ke `/reset-password`. Global setup HARUS menangani ini:

```typescript
// e2e-tests/src/helpers/auth.helper.ts

export async function loginAndHandlePasswordChange(
  page: Page, 
  username: string, 
  password: string
): Promise<void> {
  await page.goto('/login');
  await page.fill('input[name="username"]', username);
  await page.fill('input[name="password"]', password);
  await page.click('button[type="submit"]');

  // Wait for either dashboard or password change page
  await page.waitForURL(url => 
    url.pathname.includes('/dashboard') || 
    url.pathname.includes('/reset-password'),
    { timeout: 15000 }
  );

  // Handle forced password change
  if (page.url().includes('/reset-password')) {
    await page.fill('input[name="currentPassword"]', password);
    await page.fill('input[name="newPassword"]', password);       // Keep same for E2E
    await page.fill('input[name="confirmPassword"]', password);
    await page.click('button[type="submit"]');
    await page.waitForURL('**/dashboard**', { timeout: 10000 });
  }
}
```

---

### 5. Playwright Test Architecture

#### 5.1 RELIABILITY-FIRST: The 10 Commandments of Zero Flakiness

Inilah inti proposal. Setiap pattern di bawah dirancang untuk **mengeliminasi akar penyebab flakiness**, bukan menyembunyikan gejala.

---

##### Commandment 1: NEVER Use Arbitrary Timeouts

```typescript
// ❌ DILARANG — sumber flakiness #1
await page.waitForTimeout(500);
await page.waitForTimeout(1000);
await page.waitForTimeout(2000);

// ✅ WAJIB — wait for specific DOM/network state
await page.waitForSelector('.ts-wrapper.has-items');
await page.waitForFunction(() => document.querySelector('#el')?.tomselect);
await page.waitForResponse(r => r.url().includes('/api/') && r.ok());
```

**Satu-satunya exception:** Setelah `page.click('#btn-add-line')` untuk menunggu DOM clone + JS initialization. Bahkan ini punya alternative yang lebih baik (lihat Commandment 7).

---

##### Commandment 2: Smart Wait Utilities

```typescript
// e2e-tests/src/helpers/reliability/smart-wait.ts

import { Page, Locator } from '@playwright/test';

/**
 * Wait for TomSelect to be fully initialized on a given <select> element.
 * Checks both DOM wrapper AND JS instance readiness.
 */
export async function waitForTomSelectReady(
  page: Page, 
  selector: string,
  options?: { timeout?: number }
): Promise<void> {
  const timeout = options?.timeout ?? 10_000;
  
  await page.waitForFunction(
    (sel) => {
      const el = document.querySelector(sel) as HTMLSelectElement;
      if (!el) return false;
      if (!el.tomselect) return false;
      // Ensure the wrapper is in the DOM (not just JS reference)
      if (!el.tomselect.wrapper) return false;
      // Ensure it's not in a loading state
      if (el.tomselect.isLocked) return false;
      return true;
    },
    selector,
    { timeout, polling: 100 }
  );
}

/**
 * Wait for Flatpickr to be initialized on an input element.
 */
export async function waitForFlatpickrReady(
  page: Page,
  selector: string,
  options?: { timeout?: number }
): Promise<void> {
  await page.waitForFunction(
    (sel) => {
      const el = document.querySelector(sel) as HTMLInputElement;
      return el && el._flatpickr && el._flatpickr.isOpen !== undefined;
    },
    selector,
    { timeout: options?.timeout ?? 5_000, polling: 100 }
  );
}

/**
 * Wait for AutoNumeric to be initialized on an input element.
 */
export async function waitForAutoNumericReady(
  page: Page,
  selector: string,
  options?: { timeout?: number }
): Promise<void> {
  await page.waitForFunction(
    (sel) => {
      const el = document.querySelector(sel) as HTMLInputElement;
      if (!el) return false;
      if (typeof AutoNumeric === 'undefined') return false;
      try {
        return !!AutoNumeric.getAutoNumericElement(el);
      } catch {
        return false;
      }
    },
    selector,
    { timeout: options?.timeout ?? 5_000, polling: 100 }
  );
}

/**
 * Wait for HTMX swap to complete on a target container.
 * Uses htmx:afterSwap event detection.
 */
export async function waitForHtmxSwap(
  page: Page,
  targetSelector: string,
  options?: { timeout?: number }
): Promise<void> {
  const timeout = options?.timeout ?? 10_000;
  
  await page.evaluate(
    ({ sel, ms }) => {
      return new Promise<void>((resolve, reject) => {
        const target = document.querySelector(sel);
        if (!target) { reject(new Error(`HTMX target not found: ${sel}`)); return; }
        
        const timer = setTimeout(() => {
          target.removeEventListener('htmx:afterSwap', handler);
          reject(new Error(`HTMX swap timeout on ${sel}`));
        }, ms);
        
        function handler() {
          clearTimeout(timer);
          target.removeEventListener('htmx:afterSwap', handler);
          // Give DOM one more tick to settle after swap
          requestAnimationFrame(() => resolve());
        }
        
        target.addEventListener('htmx:afterSwap', handler, { once: true });
      });
    },
    { sel: targetSelector, ms: timeout }
  );
}

/**
 * Wait for all pending HTMX requests to complete.
 * Inspects htmx internal request queue.
 */
export async function waitForHtmxIdle(
  page: Page,
  options?: { timeout?: number }
): Promise<void> {
  await page.waitForFunction(
    () => {
      // htmx exposes internal API: check no active XHR
      const htmx = (window as any).htmx;
      if (!htmx) return true; // no htmx = nothing to wait for
      // Internal: htmx tracks pending requests
      const indicator = document.querySelector('.htmx-request');
      return !indicator;
    },
    { timeout: options?.timeout ?? 10_000, polling: 200 }
  );
}

/**
 * Wait for page to be fully interactive:
 * - DOM loaded
 * - No pending network requests
 * - No HTMX in-flight
 * - No loading spinners visible
 */
export async function waitForPageReady(page: Page): Promise<void> {
  await page.waitForLoadState('domcontentloaded');
  await page.waitForFunction(() => {
    // No loading indicators visible
    const spinner = document.querySelector('.spinner-border:not([style*="display: none"])');
    const htmxActive = document.querySelector('.htmx-request');
    return !spinner && !htmxActive;
  }, { timeout: 15_000, polling: 200 });
}
```

---

##### Commandment 3: Network Sentinel — Intercept, Don't Guess

```typescript
// e2e-tests/src/helpers/reliability/network-sentinel.ts

import { Page, Response } from '@playwright/test';

/**
 * Wait for a specific AJAX response pattern.
 * Used after form submissions, TomSelect AJAX loads, etc.
 */
export async function waitForApiResponse(
  page: Page,
  urlPattern: string | RegExp,
  options?: { method?: string; timeout?: number }
): Promise<Response> {
  const method = options?.method ?? 'GET';
  const timeout = options?.timeout ?? 15_000;
  
  return page.waitForResponse(
    (resp) => {
      const urlMatch = typeof urlPattern === 'string'
        ? resp.url().includes(urlPattern)
        : urlPattern.test(resp.url());
      const methodMatch = resp.request().method() === method;
      return urlMatch && methodMatch;
    },
    { timeout }
  );
}

/**
 * Submit AJAX form and wait for JSON response.
 * Handles the ERP-specific data-ajax-form="true" pattern.
 * 
 * Returns parsed JSON body and response status.
 * IDEMPOTENT: if form was already submitted (button disabled), returns null.
 */
export async function submitAjaxFormAndWait(
  page: Page,
  submitSelector: string = 'button[type="submit"]',
  options?: { timeout?: number }
): Promise<{ success: boolean; message?: string; validationErrors?: Record<string, string> } | null> {
  const timeout = options?.timeout ?? 15_000;
  
  // Check if button is already disabled (prevents double-submit)
  const isDisabled = await page.locator(submitSelector).isDisabled();
  if (isDisabled) return null;
  
  // Set up response interceptor BEFORE clicking
  const responsePromise = page.waitForResponse(
    (resp) => {
      const contentType = resp.headers()['content-type'] ?? '';
      return resp.request().method() === 'POST' && contentType.includes('application/json');
    },
    { timeout }
  );
  
  // Click submit
  await page.click(submitSelector);
  
  // Wait for JSON response
  const response = await responsePromise;
  const body = await response.json();
  
  // If success with redirect, wait for navigation
  if (body.success) {
    const redirectUrl = await page.locator('form[data-ajax-form="true"]')
      .getAttribute('data-redirect-on-success');
    if (redirectUrl) {
      await page.waitForURL(`**${redirectUrl}**`, { timeout: 10_000 });
    }
  }
  
  return body;
}

/**
 * Wait for ALL pending network requests to complete.
 * More reliable than waitForLoadState('networkidle') because
 * it explicitly counts in-flight requests.
 */
export async function waitForNetworkSettled(
  page: Page,
  options?: { timeout?: number; idleTime?: number }
): Promise<void> {
  const timeout = options?.timeout ?? 10_000;
  const idleTime = options?.idleTime ?? 500;
  
  await page.waitForFunction(
    (ms) => {
      return new Promise<boolean>((resolve) => {
        let timer: ReturnType<typeof setTimeout>;
        const resetTimer = () => {
          clearTimeout(timer);
          timer = setTimeout(() => resolve(true), ms);
        };
        
        // Monitor XHR/fetch
        const origFetch = window.fetch;
        let pending = 0;
        
        window.fetch = async (...args) => {
          pending++;
          try {
            return await origFetch.apply(window, args);
          } finally {
            pending--;
            if (pending === 0) resetTimer();
          }
        };
        
        // Start initial timer
        resetTimer();
        
        // Restore after resolution
        setTimeout(() => {
          window.fetch = origFetch;
          resolve(true);
        }, ms + 100);
      });
    },
    idleTime,
    { timeout }
  );
}
```

---

##### Commandment 4: Retry with Idempotency Guarantee

```typescript
// e2e-tests/src/helpers/reliability/retry-action.ts

import { Page } from '@playwright/test';

interface RetryOptions {
  maxAttempts?: number;
  delayMs?: number;
  backoff?: 'linear' | 'exponential';
  shouldRetry?: (error: Error) => boolean;
}

/**
 * Retry an action with idempotency awareness.
 * 
 * KEY PRINCIPLE: The action function MUST be idempotent.
 * If setting a TomSelect value, calling setValue() twice is safe.
 * If clicking a submit button, check if already submitted first.
 * 
 * This is NOT for hiding flakiness — it's for handling legitimate
 * race conditions in async UI components.
 */
export async function retryAction<T>(
  action: () => Promise<T>,
  options?: RetryOptions
): Promise<T> {
  const maxAttempts = options?.maxAttempts ?? 3;
  const baseDelay = options?.delayMs ?? 200;
  const backoff = options?.backoff ?? 'exponential';
  const shouldRetry = options?.shouldRetry ?? (() => true);
  
  let lastError: Error;
  
  for (let attempt = 1; attempt <= maxAttempts; attempt++) {
    try {
      return await action();
    } catch (error) {
      lastError = error as Error;
      
      if (attempt === maxAttempts || !shouldRetry(lastError)) {
        throw lastError;
      }
      
      const delay = backoff === 'exponential'
        ? baseDelay * Math.pow(2, attempt - 1)
        : baseDelay * attempt;
        
      await new Promise(resolve => setTimeout(resolve, delay));
    }
  }
  
  throw lastError!;
}

/**
 * Retry specifically for TomSelect operations.
 * TomSelect AJAX loads can race with user interaction,
 * so retry is legitimate here.
 */
export async function retryTomSelect(
  page: Page,
  selector: string,
  operation: () => Promise<void>
): Promise<void> {
  await retryAction(async () => {
    // Pre-condition: TomSelect must be initialized
    const isReady = await page.evaluate((sel) => {
      const el = document.querySelector(sel) as any;
      return el && el.tomselect && !el.tomselect.isLocked;
    }, selector);
    
    if (!isReady) {
      throw new Error(`TomSelect not ready: ${selector}`);
    }
    
    await operation();
  }, {
    maxAttempts: 3,
    delayMs: 300,
    backoff: 'exponential',
  });
}
```

---

##### Commandment 5: Component Helpers with Built-in Reliability

```typescript
// e2e-tests/src/helpers/components/tom-select.helper.ts

import { Page } from '@playwright/test';
import { waitForTomSelectReady } from '../reliability/smart-wait';
import { retryAction } from '../reliability/retry-action';
import { waitForApiResponse } from '../reliability/network-sentinel';

/**
 * Select a value in a TomSelect dropdown.
 * 
 * RELIABILITY GUARANTEES:
 * 1. Waits for TomSelect JS instance to be initialized
 * 2. Waits for AJAX options to load (not just DOM ready)
 * 3. Retries on race condition failures
 * 4. Validates that value was actually set (post-condition check)
 * 5. IDEMPOTENT: calling twice with same value is safe
 */
export async function selectTomSelectValue(
  page: Page,
  selector: string,
  searchQuery: string = '',
  optionIndex: number = 0
): Promise<{ id: string; name: string }> {
  // Step 1: Wait for TomSelect initialization
  await waitForTomSelectReady(page, selector);
  
  // Step 2: Perform selection with retry
  const result = await retryAction(async () => {
    const res = await page.evaluate(
      ({ sel, query, idx }) => {
        return new Promise<{ id: string; name: string }>((resolve, reject) => {
          const el = document.querySelector(sel) as any;
          if (!el?.tomselect) {
            reject(new Error(`TomSelect not found: ${sel}`));
            return;
          }
          
          const ts = el.tomselect;
          
          // Timeout safety
          const timer = setTimeout(
            () => reject(new Error(`TomSelect load timeout: ${sel}`)),
            8000
          );
          
          ts.load(query, (options: any[]) => {
            clearTimeout(timer);
            if (!options || options.length === 0) {
              reject(new Error(`No options for TomSelect ${sel} with query "${query}"`));
              return;
            }
            options.forEach((opt: any) => ts.addOption(opt));
            const target = options[Math.min(idx, options.length - 1)];
            ts.setValue(target.id);
            resolve({ id: String(target.id), name: target.name || target.text });
          });
        });
      },
      { sel: selector, query: searchQuery, idx: optionIndex }
    );
    
    return res;
  }, { maxAttempts: 3, delayMs: 500 });
  
  // Step 3: Post-condition validation — verify value was actually set
  const actualValue = await page.evaluate((sel) => {
    const el = document.querySelector(sel) as any;
    return el?.tomselect?.getValue();
  }, selector);
  
  if (String(actualValue) !== String(result.id)) {
    throw new Error(
      `TomSelect ${selector}: expected value ${result.id}, got ${actualValue}`
    );
  }
  
  return result;
}

/**
 * Select TomSelect in a dynamic line item row.
 * Extra reliability: waits for row DOM and TomSelect init after row clone.
 */
export async function selectLineItemTomSelect(
  page: Page,
  rowIndex: number,
  fieldClass: string,
  searchQuery: string = '',
  optionIndex: number = 0
): Promise<{ id: string; name: string }> {
  // Wait for the row to exist in DOM
  await page.waitForFunction(
    ({ idx, cls }) => {
      const rows = document.querySelectorAll('#line-container .line-row');
      if (idx >= rows.length) return false;
      const el = rows[idx].querySelector(`.${cls}`) as any;
      return el && el.tomselect && !el.tomselect.isLocked;
    },
    { idx: rowIndex, cls: fieldClass },
    { timeout: 10_000, polling: 200 }
  );
  
  // Now perform selection
  return await retryAction(async () => {
    const res = await page.evaluate(
      ({ idx, cls, query, optIdx }) => {
        return new Promise<{ id: string; name: string }>((resolve, reject) => {
          const rows = document.querySelectorAll('#line-container .line-row');
          const el = rows[idx]?.querySelector(`.${cls}`) as any;
          if (!el?.tomselect) {
            reject(new Error(`TomSelect .${cls} not ready in row ${idx}`));
            return;
          }
          const ts = el.tomselect;
          const timer = setTimeout(
            () => reject(new Error(`Load timeout for .${cls} row ${idx}`)),
            8000
          );
          ts.load(query, (options: any[]) => {
            clearTimeout(timer);
            if (!options.length) { reject(new Error('No options')); return; }
            options.forEach((o: any) => ts.addOption(o));
            const pick = options[Math.min(optIdx, options.length - 1)];
            ts.setValue(pick.id);
            resolve({ id: String(pick.id), name: pick.name || pick.text });
          });
        });
      },
      { idx: rowIndex, cls: fieldClass, query: searchQuery, optIdx: optionIndex }
    );
    return res;
  }, { maxAttempts: 3, delayMs: 500 });
}
```

```typescript
// e2e-tests/src/helpers/components/flatpickr.helper.ts

import { Page } from '@playwright/test';
import { waitForFlatpickrReady } from '../reliability/smart-wait';

/**
 * Set date on a Flatpickr input.
 * 
 * RELIABILITY:
 * 1. Waits for _flatpickr instance to exist
 * 2. Uses setDate() API with triggerChange=true
 * 3. Falls back to direct value set if Flatpickr not found
 * 4. Post-condition: verifies value was set
 */
export async function setFlatpickrDate(
  page: Page,
  selector: string,
  dateStr: string
): Promise<void> {
  await waitForFlatpickrReady(page, selector);
  
  await page.evaluate(
    ({ sel, date }) => {
      const input = document.querySelector(sel) as any;
      if (input?._flatpickr) {
        input._flatpickr.setDate(date, true);
      } else {
        input.value = date;
        input.dispatchEvent(new Event('change', { bubbles: true }));
      }
    },
    { sel: selector, date: dateStr }
  );
  
  // Post-condition: verify
  const actualValue = await page.evaluate((sel) => {
    const el = document.querySelector(sel) as HTMLInputElement;
    return el?.value;
  }, selector);
  
  if (!actualValue || !actualValue.includes(dateStr.split('T')[0])) {
    throw new Error(`Flatpickr ${selector}: expected "${dateStr}", got "${actualValue}"`);
  }
}
```

```typescript
// e2e-tests/src/helpers/components/htmx.helper.ts

import { Page } from '@playwright/test';
import { waitForHtmxSwap, waitForHtmxIdle } from '../reliability/smart-wait';

/**
 * Type into an HTMX-powered search input and wait for results swap.
 * 
 * HTMX search pattern di ERP:
 *   hx-trigger="keyup changed delay:500ms"
 *   hx-target="#table-container"
 * 
 * RELIABILITY:
 * 1. Fills input
 * 2. Waits for HTMX debounce (500ms in ERP config)
 * 3. Waits for HTMX swap to complete on target container
 * 4. Waits for DOM to settle (no pending requests)
 */
export async function htmxSearch(
  page: Page,
  inputSelector: string,
  query: string,
  targetSelector: string
): Promise<void> {
  // Clear existing value first
  await page.fill(inputSelector, '');
  
  // Type new query
  await page.fill(inputSelector, query);
  
  // Trigger keyup (Playwright fill doesn't always fire keyup)
  await page.dispatchEvent(inputSelector, 'keyup');
  
  // Wait for HTMX to pick up the event and complete the swap
  // HTMX debounce is 500ms + network time
  await waitForHtmxSwap(page, targetSelector, { timeout: 10_000 });
  
  // Wait for any remaining HTMX activity to finish
  await waitForHtmxIdle(page);
}

/**
 * Click an HTMX-powered pagination link and wait for table refresh.
 */
export async function htmxPaginate(
  page: Page,
  pageNumber: number,
  targetSelector: string
): Promise<void> {
  await page.click(`.pagination a:has-text("${pageNumber}")`);
  await waitForHtmxSwap(page, targetSelector, { timeout: 10_000 });
  await waitForHtmxIdle(page);
}
```

```typescript
// e2e-tests/src/helpers/components/line-item.helper.ts

import { Page } from '@playwright/test';

/**
 * Add a new line item row and wait for ALL JS components to initialize.
 * 
 * This is the most flakiness-prone operation:
 * 1. Button click clones a template row
 * 2. TomSelect instances need to be created on new <select> elements
 * 3. AutoNumeric instances need to be created on new <input> elements
 * 4. Event listeners need to be attached
 * 
 * RELIABILITY: Wait for specific post-condition, not arbitrary timeout.
 */
export async function addLineItemRow(
  page: Page,
  addButtonSelector: string = '#btn-add-line'
): Promise<number> {
  // Count existing rows BEFORE adding
  const beforeCount = await page.evaluate(() =>
    document.querySelectorAll('#line-container .line-row').length
  );
  
  // Click add button
  await page.click(addButtonSelector);
  
  // Wait for new row to appear in DOM
  await page.waitForFunction(
    (expected) => {
      const rows = document.querySelectorAll('#line-container .line-row');
      return rows.length === expected;
    },
    beforeCount + 1,
    { timeout: 5_000, polling: 100 }
  );
  
  const newIndex = beforeCount; // 0-based
  
  // Wait for TomSelect initialization on ALL select fields in new row
  await page.waitForFunction(
    (idx) => {
      const rows = document.querySelectorAll('#line-container .line-row');
      const row = rows[idx];
      if (!row) return false;
      
      const selects = row.querySelectorAll('select.erp-input-ts-sm, select.erp-input-ts');
      for (const sel of selects) {
        if (!(sel as any).tomselect) return false;
      }
      return true;
    },
    newIndex,
    { timeout: 10_000, polling: 200 }
  );
  
  return newIndex;
}
```

---

##### Commandment 6: Server Health Verification

```typescript
// e2e-tests/src/helpers/reliability/health-check.ts

import { request } from '@playwright/test';

/**
 * Poll the Spring Boot server until it's ready to accept requests.
 * 
 * CHECKS:
 * 1. TCP connection succeeds
 * 2. GET /login returns 200
 * 3. CSRF token is present in response (proves Spring Security is initialized)
 * 4. SystemInitializer has completed (proven by successful login)
 * 
 * TIMEOUT: 90s (Spring Boot cold start + Flyway 45 migrations + BCrypt hash)
 */
export async function waitForServerReady(
  baseURL: string,
  options?: { timeout?: number; interval?: number }
): Promise<void> {
  const timeout = options?.timeout ?? 90_000;
  const interval = options?.interval ?? 2_000;
  const startTime = Date.now();
  
  while (Date.now() - startTime < timeout) {
    try {
      const ctx = await request.newContext({ baseURL });
      const response = await ctx.get('/login', { timeout: 5_000 });
      
      if (response.ok()) {
        const html = await response.text();
        // Verify CSRF token present → Spring Security fully initialized
        if (html.includes('_csrf') || html.includes('csrf')) {
          await ctx.dispose();
          console.log(`✅ Server ready at ${baseURL} (${Date.now() - startTime}ms)`);
          return;
        }
      }
      await ctx.dispose();
    } catch {
      // Connection refused or timeout — server still starting
    }
    
    await new Promise(resolve => setTimeout(resolve, interval));
  }
  
  throw new Error(`Server not ready at ${baseURL} after ${timeout}ms`);
}
```

---

##### Commandment 7: Authentication Session Reuse

```typescript
// e2e-tests/src/helpers/auth.helper.ts

import { Page, BrowserContext, Browser } from '@playwright/test';

const STORAGE_STATE_PATH = 'e2e-tests/.auth/storage-state.json';

/**
 * Global setup: login once, save session state.
 * All tests reuse this session — no login per test.
 * 
 * RELIABILITY:
 * - Eliminates N login requests (prevents session table bloat)
 * - Single point of failure for auth issues (easier debugging)
 * - Session timeout is 30m — far longer than any test suite
 */
export async function globalLogin(browser: Browser, baseURL: string): Promise<void> {
  const context = await browser.newContext();
  const page = await context.newPage();
  
  await loginAndHandlePasswordChange(page, 'admin', 'admin123');
  
  // Verify we're on dashboard
  await page.waitForSelector('text=Dashboard', { timeout: 10_000 });
  
  // Save session state
  await context.storageState({ path: STORAGE_STATE_PATH });
  await context.close();
}
```

---

##### Commandment 8: Test Isolation Without Database Reset

Database reset per test terlalu lambat (Flyway 45 migrations + SystemInitializer BCrypt). Strategi: **isolation via unique data + no shared mutation**.

```typescript
// Test isolation pattern

import { test, expect } from '@playwright/test';
import { testBrand, uniqueId } from '../helpers/data-factory';

test.describe('Brand CRUD', () => {
  // Each test creates its OWN data — never depends on other tests
  
  test('create brand', async ({ page }) => {
    const brand = testBrand(); // unique name per run
    
    await page.goto('/inventory/brands/create');
    await waitForPageReady(page);
    
    await page.fill('input[name="name"]', brand.name);
    
    const result = await submitAjaxFormAndWait(page);
    expect(result?.success).toBe(true);
    
    // Verify in list
    await page.waitForURL('**/inventory/brands**');
    await expect(page.locator(`text=${brand.name}`)).toBeVisible();
  });
  
  test('edit brand', async ({ page }) => {
    // Create own brand first — don't depend on "create brand" test
    const brand = testBrand();
    await page.goto('/inventory/brands/create');
    await waitForPageReady(page);
    await page.fill('input[name="name"]', brand.name);
    await submitAjaxFormAndWait(page);
    await page.waitForURL('**/inventory/brands**');
    
    // Now edit it
    await page.click(`tr:has-text("${brand.name}") a[href*="edit"]`);
    await waitForPageReady(page);
    
    const newName = uniqueId('BRAND-EDITED');
    await page.fill('input[name="name"]', newName);
    
    const result = await submitAjaxFormAndWait(page);
    expect(result?.success).toBe(true);
    
    // Verify updated
    await page.waitForURL('**/inventory/brands**');
    await expect(page.locator(`text=${newName}`)).toBeVisible();
  });
});
```

---

##### Commandment 9: Flakiness Detection & Quarantine

```typescript
// playwright.config.ts — flakiness detection built in

import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './src/tests',
  
  /* --- RELIABILITY SETTINGS --- */
  
  // Retry failed tests to distinguish flaky from broken
  retries: process.env.CI ? 2 : 0,
  
  // Global timeout per test (generous for SSR + async components)
  timeout: 60_000,
  
  // Expect timeout (individual assertion)
  expect: {
    timeout: 10_000,
  },
  
  // Run tests in single worker to avoid session conflicts
  // (H2 in-memory is shared by all tests via single server)
  workers: 1,
  
  // Fail the suite fast on first failure in CI
  // (prevents wasting CI minutes on cascading failures)
  maxFailures: process.env.CI ? 5 : undefined,
  
  /* --- REPORTING --- */
  reporter: [
    ['html', { open: 'never', outputFolder: 'reports/html' }],
    ['junit', { outputFile: 'reports/junit-results.xml' }],
    ['json', { outputFile: 'reports/test-results.json' }],
    // Custom flakiness tracker
    ['list'],
  ],
  
  /* --- GLOBAL SETUP/TEARDOWN --- */
  globalSetup: require.resolve('./src/helpers/global-setup.ts'),
  globalTeardown: require.resolve('./src/helpers/global-teardown.ts'),
  
  /* --- BROWSER CONFIGURATION --- */
  use: {
    baseURL: process.env.BASE_URL || 'http://localhost:18080',
    
    // Session reuse — login once in globalSetup
    storageState: '.auth/storage-state.json',
    
    // Trace: capture ONLY on failure — critical for debugging flaky tests
    trace: 'retain-on-failure',
    
    // Screenshot on failure
    screenshot: 'only-on-failure',
    
    // Video: only on retry (captures flaky behavior)
    video: 'on-first-retry',
    
    // Navigation timeout
    navigationTimeout: 30_000,
    
    // Action timeout (click, fill, etc.)
    actionTimeout: 10_000,
    
    // Locale and timezone consistency
    locale: 'id-ID',
    timezoneId: 'Asia/Jakarta',
  },
  
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
  ],
});
```

**Quarantine strategy:**

```typescript
// Tag flaky tests for quarantine without removing them
// File: e2e-tests/src/tests/quarantine/README.md

// Step 1: Detect — CI marks test as flaky if it passes on retry
// Step 2: Tag — Add @flaky annotation
test('known flaky: stock adjustment with 3+ line items @flaky', async ({ page }) => {
  test.skip(!!process.env.SKIP_FLAKY, 'Quarantined: investigating race condition');
  // ... test body
});

// Step 3: Run quarantine separately
// CI: npx playwright test --grep @flaky (separate job, non-blocking)
// Main: npx playwright test --grep-invert @flaky (must all pass)
```

---

##### Commandment 10: Monitoring & Metrics

```typescript
// e2e-tests/scripts/flakiness-report.ts

/**
 * Parse test-results.json from multiple CI runs to identify:
 * 1. Tests that fail intermittently (pass on retry)
 * 2. Tests with increasing duration trend
 * 3. Tests that consistently fail on specific days/times
 * 
 * Integrates with GitHub Actions workflow annotations.
 */

// CI pipeline reports:
//   - test_pass_rate: percentage of tests passing without retry
//   - flaky_rate: tests that fail then pass on retry / total tests
//   - avg_duration: trend over last 10 runs
//   - quarantine_count: number of tests in @flaky quarantine

// GitHub Actions output format:
// ::warning file=e2e-tests/src/tests/inventory/brand-crud.spec.ts::Flaky test detected: passed on retry 2/3 in last 10 runs
```

---

#### 5.2 Global Setup & Teardown

```typescript
// e2e-tests/src/helpers/global-setup.ts

import { chromium, FullConfig } from '@playwright/test';
import { waitForServerReady } from './reliability/health-check';

async function globalSetup(config: FullConfig) {
  const baseURL = config.projects[0].use.baseURL || 'http://localhost:18080';
  
  // Step 1: Verify server is running and healthy
  console.log('⏳ Waiting for Spring Boot server...');
  await waitForServerReady(baseURL, { timeout: 90_000 });
  
  // Step 2: Login and save session state
  console.log('🔑 Authenticating...');
  const browser = await chromium.launch();
  const context = await browser.newContext();
  const page = await context.newPage();
  
  // Navigate to login
  await page.goto(`${baseURL}/login`);
  await page.fill('input[name="username"]', 'admin');
  await page.fill('input[name="password"]', 'admin123');
  await page.click('button[type="submit"]');
  
  // Handle password change requirement
  await page.waitForURL(url =>
    url.pathname.includes('/dashboard') || url.pathname.includes('/reset-password'),
    { timeout: 15_000 }
  );
  
  if (page.url().includes('/reset-password')) {
    console.log('🔄 Handling forced password change...');
    await page.fill('input[name="currentPassword"]', 'admin123');
    await page.fill('input[name="newPassword"]', 'admin123');
    await page.fill('input[name="confirmPassword"]', 'admin123');
    await page.click('button[type="submit"]');
    await page.waitForURL('**/dashboard**', { timeout: 10_000 });
  }
  
  // Save authentication state
  await context.storageState({ path: '.auth/storage-state.json' });
  console.log('✅ Session saved to .auth/storage-state.json');
  
  // Step 3: Seed master data needed by multiple tests
  // (via browser actions, not direct DB — tests the create flow too)
  console.log('🌱 Seeding master data...');
  // Seed is done via page interactions to ensure full stack works
  
  await browser.close();
}

export default globalSetup;
```

```typescript
// e2e-tests/src/helpers/global-teardown.ts

async function globalTeardown() {
  // Cleanup: remove auth state file
  const fs = await import('fs');
  try {
    fs.unlinkSync('.auth/storage-state.json');
  } catch { /* ignore */ }
  
  console.log('🧹 Cleanup complete');
}

export default globalTeardown;
```

#### 5.3 Page Object Model — Reliability-Enhanced

```typescript
// e2e-tests/src/pages/inventory/brand-form.page.ts

import { Page, expect } from '@playwright/test';
import { waitForPageReady } from '../../helpers/reliability/smart-wait';
import { submitAjaxFormAndWait } from '../../helpers/reliability/network-sentinel';

export class BrandFormPage {
  constructor(private page: Page) {}
  
  // Locators (lazy, re-evaluated each access)
  private get nameInput() { return this.page.locator('input[name="name"]'); }
  private get submitButton() { return this.page.locator('button[type="submit"]'); }
  
  async goto(mode: 'create' | 'edit' = 'create', id?: number) {
    const url = mode === 'create' 
      ? '/inventory/brands/create' 
      : `/inventory/brands/edit/${id}`;
    await this.page.goto(url);
    await waitForPageReady(this.page);
  }
  
  async fillName(name: string) {
    await this.nameInput.fill(name);
  }
  
  async submit(): Promise<{ success: boolean; message?: string }> {
    const result = await submitAjaxFormAndWait(this.page);
    return result ?? { success: false, message: 'Submit button was disabled' };
  }
  
  async createBrand(name: string): Promise<void> {
    await this.goto('create');
    await this.fillName(name);
    const result = await this.submit();
    expect(result.success, `Failed to create brand "${name}": ${result.message}`).toBe(true);
  }
}
```

#### 5.4 Example Test Spec — Full Reliability Pattern

```typescript
// e2e-tests/src/tests/inventory/brand-crud.spec.ts

import { test, expect } from '@playwright/test';
import { BrandFormPage } from '../../pages/inventory/brand-form.page';
import { testBrand, uniqueId } from '../../helpers/data-factory';
import { waitForPageReady } from '../../helpers/reliability/smart-wait';
import { htmxSearch } from '../../helpers/components/htmx.helper';

test.describe('Brand CRUD Operations', () => {
  
  test('should create a new brand and verify in list', async ({ page }) => {
    const brand = testBrand();
    const brandForm = new BrandFormPage(page);
    
    // Create
    await brandForm.createBrand(brand.name);
    
    // Verify redirect to list
    await page.waitForURL('**/inventory/brands**');
    await waitForPageReady(page);
    
    // Verify brand appears in list (search to be sure)
    await htmxSearch(page, 'input[name="keyword"]', brand.name, '#brand-table-container');
    await expect(page.locator(`td:has-text("${brand.name}")`)).toBeVisible();
  });
  
  test('should edit an existing brand', async ({ page }) => {
    // Setup: create brand first (self-contained)
    const brand = testBrand();
    const brandForm = new BrandFormPage(page);
    await brandForm.createBrand(brand.name);
    await page.waitForURL('**/inventory/brands**');
    
    // Find and click edit
    await htmxSearch(page, 'input[name="keyword"]', brand.name, '#brand-table-container');
    await page.click(`tr:has-text("${brand.name}") a[href*="edit"]`);
    await waitForPageReady(page);
    
    // Edit
    const newName = uniqueId('BRAND-EDIT');
    await page.fill('input[name="name"]', newName);
    
    const result = await import('../../helpers/reliability/network-sentinel')
      .then(m => m.submitAjaxFormAndWait(page));
    expect(result?.success).toBe(true);
    
    // Verify
    await page.waitForURL('**/inventory/brands**');
    await htmxSearch(page, 'input[name="keyword"]', newName, '#brand-table-container');
    await expect(page.locator(`td:has-text("${newName}")`)).toBeVisible();
  });
  
  test('should delete a brand', async ({ page }) => {
    // Setup
    const brand = testBrand();
    const brandForm = new BrandFormPage(page);
    await brandForm.createBrand(brand.name);
    await page.waitForURL('**/inventory/brands**');
    
    // Search for brand
    await htmxSearch(page, 'input[name="keyword"]', brand.name, '#brand-table-container');
    
    // Click delete
    await page.click(`tr:has-text("${brand.name}") button[data-bs-toggle="modal"]`);
    
    // Confirm in modal
    await page.waitForSelector('.modal.show', { state: 'visible' });
    await page.click('.modal.show button:has-text("Hapus"), .modal.show button:has-text("Delete")');
    
    // Wait for HTMX delete + swap
    await page.waitForResponse(resp => 
      resp.request().method() === 'DELETE' && resp.ok()
    );
    
    // Verify removed from list
    await waitForPageReady(page);
    await expect(page.locator(`td:has-text("${brand.name}")`)).not.toBeVisible();
  });
});
```

#### 5.5 Complex Test: Stock Adjustment with Line Items

```typescript
// e2e-tests/src/tests/inventory/stock-adjustment-flow.spec.ts

import { test, expect } from '@playwright/test';
import { waitForPageReady } from '../../helpers/reliability/smart-wait';
import { selectTomSelectValue, selectLineItemTomSelect } from '../../helpers/components/tom-select.helper';
import { setFlatpickrDate } from '../../helpers/components/flatpickr.helper';
import { addLineItemRow } from '../../helpers/components/line-item.helper';
import { submitAjaxFormAndWait } from '../../helpers/reliability/network-sentinel';

test.describe('Stock Adjustment Flow', () => {
  
  test('should create stock adjustment with line items', async ({ page }) => {
    await page.goto('/inventory/adjustments/create');
    await waitForPageReady(page);
    
    // Header: set date via Flatpickr
    await setFlatpickrDate(page, 'input[name="transactionDate"]', '2025-07-15');
    
    // Header: select facility via TomSelect
    await selectTomSelectValue(page, '#header-facility', '', 0);
    
    // Header: set adjustment type
    const typeSelect = page.locator('select[name="adjustmentType"]');
    if (await typeSelect.isVisible()) {
      await typeSelect.selectOption({ index: 1 });
    }
    
    // Add line item 1
    const row0 = await addLineItemRow(page);
    await selectLineItemTomSelect(page, row0, 'select-product', '', 0);
    
    // Wait for cascade: product → grid options load
    await page.waitForTimeout(300); // legitimate: cascade event propagation
    
    // Try selecting grid if available
    const hasGrid = await page.evaluate((idx) => {
      const rows = document.querySelectorAll('#line-container .line-row');
      const gridSelect = rows[idx]?.querySelector('.select-grid') as any;
      return gridSelect?.tomselect?.options && 
             Object.keys(gridSelect.tomselect.options).length > 0;
    }, row0);
    
    if (hasGrid) {
      await selectLineItemTomSelect(page, row0, 'select-grid', '', 0);
    }
    
    // Set quantity via drawer interaction
    // (Stock adjustment qty is set through drawer, not directly on table)
    const pencilBtn = page.locator(`#line-container .line-row >> nth=${row0}`)
      .locator('button:has(.ti-pencil), button.btn-edit-qty');
    
    if (await pencilBtn.isVisible()) {
      await pencilBtn.click();
      
      // Wait for drawer/dialog to open
      await page.waitForSelector('.offcanvas.show, dialog[open], .modal.show', { 
        state: 'visible', 
        timeout: 5_000 
      });
      
      // Set qty via AutoNumeric
      await page.evaluate(() => {
        const dialog = document.querySelector('.offcanvas.show, dialog[open], .modal.show');
        const qtyInput = dialog?.querySelector('.input-qty-target') as any;
        if (qtyInput && typeof AutoNumeric !== 'undefined') {
          const an = AutoNumeric.getAutoNumericElement(qtyInput);
          if (an) {
            an.set(10);
            qtyInput.dispatchEvent(new Event('change', { bubbles: true }));
          }
        }
      });
      
      // Click Apply
      await page.click('.offcanvas.show button:has-text("Apply"), dialog[open] button:has-text("Apply")');
      
      // Wait for drawer to close
      await page.waitForSelector('.offcanvas.show', { state: 'hidden', timeout: 5_000 })
        .catch(() => {}); // may not have offcanvas
    }
    
    // Submit form
    const result = await submitAjaxFormAndWait(page, 'button[type="submit"]', { timeout: 20_000 });
    expect(result?.success).toBe(true);
    
    // Verify redirect to list
    await page.waitForURL('**/inventory/adjustments**');
    await waitForPageReady(page);
  });
});
```

---

### 6. CI/CD Integration

#### 6.1 GitHub Actions Workflow

```yaml
# .github/workflows/e2e-tests.yml

name: E2E Tests (Playwright)

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

concurrency:
  group: e2e-${{ github.ref }}
  cancel-in-progress: true

jobs:
  e2e:
    runs-on: ubuntu-latest
    timeout-minutes: 30
    
    steps:
      - name: Checkout
        uses: actions/checkout@v4
      
      - name: Setup Java 21
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '21'
          cache: 'maven'
      
      - name: Setup Node.js 20
        uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: 'npm'
          cache-dependency-path: e2e-tests/package-lock.json
      
      - name: Install Playwright
        working-directory: e2e-tests
        run: |
          npm ci
          npx playwright install --with-deps chromium
      
      - name: Build Spring Boot (skip tests)
        run: ./mvnw package -DskipTests -Pe2e -q
      
      - name: Start Spring Boot (E2E profile)
        run: |
          java -jar target/solusi-program-erp-*.jar \
            --spring.profiles.active=e2e \
            --server.port=18080 &
          
          # Wait for server readiness
          echo "Waiting for Spring Boot..."
          for i in $(seq 1 60); do
            if curl -sf http://localhost:18080/login > /dev/null 2>&1; then
              echo "✅ Server ready after ${i}s"
              break
            fi
            if [ $i -eq 60 ]; then
              echo "❌ Server not ready after 60s"
              cat logs/erp.log || true
              exit 1
            fi
            sleep 1
          done
      
      - name: Run Playwright E2E Tests
        working-directory: e2e-tests
        run: npx playwright test
        env:
          BASE_URL: http://localhost:18080
          CI: true
      
      - name: Upload Test Reports
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: e2e-reports
          path: |
            e2e-tests/reports/
            e2e-tests/test-results/
          retention-days: 14
      
      - name: Upload Traces (on failure)
        if: failure()
        uses: actions/upload-artifact@v4
        with:
          name: e2e-traces
          path: e2e-tests/test-results/**/*.zip
          retention-days: 7
      
      - name: Annotate Flaky Tests
        if: always()
        working-directory: e2e-tests
        run: |
          # Parse test results and annotate flaky tests
          node -e "
            const results = require('./reports/test-results.json');
            const suites = results.suites || [];
            for (const suite of suites) {
              for (const spec of suite.specs || []) {
                const retried = spec.tests?.some(t => t.results?.length > 1 && t.status === 'expected');
                if (retried) {
                  console.log('::warning file=' + spec.file + '::FLAKY: ' + spec.title + ' passed on retry');
                }
              }
            }
          " || true
```

#### 6.2 CI Pipeline Flow

```
┌──────────────────────────────────────────────────────┐
│                  GitHub Actions                       │
│                                                      │
│  1. checkout ──► 2. setup java+node ──► 3. npm ci    │
│                                                      │
│  4. mvnw package -DskipTests -Pe2e                   │
│     (compile + package Spring Boot JAR)              │
│                                                      │
│  5. java -jar target/*.jar --profiles=e2e            │
│     (H2 in-memory, port 18080)                       │
│     ├── Flyway: 45 migrations                        │
│     ├── SystemInitializer: BCrypt admin password     │
│     └── Server ready: /login returns 200             │
│                                                      │
│  6. npx playwright test                              │
│     ├── globalSetup: health check + login + seed     │
│     ├── tests: sequential, isolated, deterministic   │
│     └── globalTeardown: cleanup                      │
│                                                      │
│  7. Upload reports + traces                          │
│     ├── HTML report (browsable)                      │
│     ├── JUnit XML (CI parsing)                       │
│     ├── Trace files (failure debugging)              │
│     └── Screenshots (failure evidence)               │
└──────────────────────────────────────────────────────┘
```

#### 6.3 Flakiness Dashboard (Long-term Monitoring)

Setelah stable, tambahkan job terpisah untuk **multi-run flakiness detection**:

```yaml
  flakiness-check:
    if: github.ref == 'refs/heads/main'
    runs-on: ubuntu-latest
    needs: [e2e]
    steps:
      - name: Run E2E 3x for flakiness detection
        run: |
          for i in 1 2 3; do
            echo "=== Run $i/3 ==="
            npx playwright test --reporter=json 2>/dev/null | \
              jq '.suites[].specs[].tests[].status' >> /tmp/results.txt
          done
          
          # Check for inconsistency
          FLAKY=$(sort /tmp/results.txt | uniq -c | sort -rn | head -5)
          echo "Results: $FLAKY"
```

---

### 7. Cakupan Test Minimum per Modul

#### 7.1 Coverage Matrix

| Modul | Prioritas | Test Specs | Skenario | Komponen UI Kompleks |
|-------|-----------|------------|----------|---------------------|
| **Auth** | P0 | `login.spec.ts` | Login sukses, login gagal (wrong password), force password change redirect, session persistence | Form standard |
| **Security: Users** | P1 | `user-crud.spec.ts` | List users, create user with role (TomSelect), edit user, toggle active status | TomSelect (role select) |
| **Security: Roles** | P1 | `role-crud.spec.ts` | List roles, create role with permissions (multi-select), edit role | Checkbox multi-select |
| **Master: Party** | P1 | `party-crud.spec.ts` | Create supplier, create customer, add address (dynamic row + Geographic TomSelect cascade), add contact (dynamic row) | TomSelect cascade, dynamic rows |
| **Master: Currency** | P2 | `currency-crud.spec.ts` | CRUD standard form | Standard form |
| **Master: Tax** | P2 | `tax-crud.spec.ts` | CRUD standard form | Standard form |
| **Inventory: Brand** | P1 | `brand-crud.spec.ts` | CRUD, HTMX search, pagination | HTMX search |
| **Inventory: Product Category** | P1 | `product-category-crud.spec.ts` | CRUD standard form | Standard form |
| **Inventory: Product** | P1 | `product-crud.spec.ts` | Create product (TomSelect: category, brand), edit, list with search | TomSelect multiple |
| **Inventory: UoM** | P2 | `uom-crud.spec.ts` | CRUD, UoM conversion | Standard form |
| **Inventory: Facility** | P1 | `facility-crud.spec.ts` | Create warehouse, grid, container (hierarchy) | Standard form |
| **Inventory: Stock Adjustment** | P0 | `stock-adjustment-flow.spec.ts` | Create adjustment with line items, set qty via drawer, serial number input, submit, view detail | TomSelect cascade, AutoNumeric, dynamic rows, drawer, AJAX submit |
| **Accounting: COA** | P2 | `coa-crud.spec.ts` | CRUD chart of accounts, parent TomSelect | TomSelect (parent) |
| **Accounting: Period** | P2 | `period-management.spec.ts` | Create period, close period (HTMX), reopen period | HTMX post actions |

**Total: 14 test spec files, ~45 test cases**

#### 7.2 Execution Order (Dependency-Respecting)

```
Phase 1 (No Dependencies):
  ├── login.spec.ts
  ├── brand-crud.spec.ts
  ├── product-category-crud.spec.ts
  ├── uom-crud.spec.ts
  ├── currency-crud.spec.ts
  └── tax-crud.spec.ts

Phase 2 (Depends on Master Data from Phase 1 OR self-seeded):
  ├── user-crud.spec.ts
  ├── role-crud.spec.ts
  ├── facility-crud.spec.ts
  ├── product-crud.spec.ts        # needs category, brand, UoM
  ├── party-crud.spec.ts          # needs geographic data
  └── coa-crud.spec.ts

Phase 3 (Depends on Inventory Data):
  ├── stock-adjustment-flow.spec.ts  # needs product, facility
  └── period-management.spec.ts
```

> **Catatan:** Karena setiap test self-contained (buat data sendiri), urutan ini hanya rekomendasi. Tidak ada hard dependency antar spec files.

---

### 8. Risiko & Mitigasi

| # | Risiko | Likelihood | Impact | Mitigasi |
|---|--------|-----------|--------|----------|
| 1 | **H2 migration failure** — Flyway scripts menggunakan MariaDB syntax yang incompatible dengan H2 MODE=MySQL | Sedang | Tinggi | Jalankan semua 45 migrations di H2 di awal development. Buat `migration-h2/` override hanya untuk scripts yang gagal. Audit setiap migration baru untuk H2 compat. |
| 2 | **TomSelect race condition** — AJAX options belum loaded saat test mencoba select | Tinggi | Tinggi | `waitForTomSelectReady()` + retry pattern + post-condition validation. Lihat Commandment 2 & 5. |
| 3 | **HTMX swap timing** — Test melanjutkan sebelum HTMX swap selesai | Tinggi | Sedang | `waitForHtmxSwap()` listener pada target selector. `waitForHtmxIdle()` before assertions. |
| 4 | **Session cookie `secure:true`** — H2 profile berjalan di HTTP localhost | Tinggi | Tinggi | `application-e2e.yaml` sudah set `cookie.secure: false`. Kritis — tanpa ini semua test gagal login. |
| 5 | **Spring Boot cold start lambat** — Flyway 45 migrations + BCrypt hash bisa 30s+ | Sedang | Sedang | Health check polling 90s timeout di `globalSetup`. Maven caching di CI. JAR pre-built sebelum test. |
| 6 | **`password_change_required=true`** — First login redirect ke `/reset-password` | Pasti | Sedang | `globalSetup` handles password change flow. Session state disimpan setelah password changed. |
| 7 | **Test data collision** — Parallel tests membuat data dengan nama sama | Rendah | Tinggi | `workers: 1` + `uniqueId()` factory. Jika scale ke parallel, setiap worker perlu isolated server instance. |
| 8 | **MinIO unavailable** — Approval signature test gagal karena no MinIO | Pasti | Rendah | Mock MinIO bean di `@Profile("e2e")`. Atau skip approval signature upload tests di fase awal. |
| 9 | **Dynamic row JS init delay** — TomSelect/AutoNumeric belum ready setelah row clone | Tinggi | Tinggi | `addLineItemRow()` waits for ALL component initialization via `waitForFunction()`. |
| 10 | **CI environment differences** — Test pass locally but fail on GitHub Actions | Sedang | Sedang | Identical config: headless Chromium, fixed locale `id-ID`, timezone `Asia/Jakarta`. Trace + video artifacts on failure. |
| 11 | **Database state pollution** — Test A modifies data that Test B relies on | Sedang | Tinggi | Setiap test self-contained: create → operate → verify own data. Tidak ada cross-test dependency. Unique data factory per test. |
| 12 | **CSRF token expiry** — Long-running test suite session expires | Rendah | Sedang | Session timeout 30m. Suite biasanya selesai <10m. If needed, implement session refresh middleware. |

---

### 9. Effort Estimate

#### 9.1 Breakdown per Sprint (2-week sprints)

| Fase | Minggu | Deliverable | Effort (person-days) |
|------|--------|-------------|---------------------|
| **Sprint 1: Foundation** | W1-W2 | E2E profile setup, H2 migration validation, Playwright project scaffold, health check, auth helper, global setup | 8 |
| **Sprint 2: Reliability Layer** | W3-W4 | Smart wait utils, network sentinel, retry-action, component helpers (TomSelect, Flatpickr, HTMX, line-item), data factory | 10 |
| **Sprint 3: Core Tests** | W5-W6 | Login test, Brand CRUD, Product Category CRUD, UoM CRUD, Product CRUD (with TomSelect), CI pipeline | 8 |
| **Sprint 4: Complex Tests** | W7-W8 | Party CRUD (dynamic rows + cascade TomSelect), Facility hierarchy, Stock Adjustment (drawer + line items) | 10 |
| **Sprint 5: Full Coverage** | W9-W10 | Security module tests, Accounting tests, Currency/Tax CRUD, HTMX search/pagination tests | 6 |
| **Sprint 6: Hardening** | W11-W12 | Flakiness detection run (3x repeat), quarantine setup, CI monitoring, documentation, knowledge transfer | 5 |
| **TOTAL** | **12 minggu** | **14 spec files, ~45 test cases, full CI pipeline** | **47 person-days** |

#### 9.2 Prerequisites (Before Sprint 1)

| Item | Status | Action Required |
|------|--------|----------------|
| H2 dependency di pom.xml | Belum ada | Tambah `<dependency>` dengan scope `runtime` di profile `e2e` |
| `application-e2e.yaml` | Belum ada | Buat sesuai Section 3.1 |
| `e2e-tests/` directory | Belum ada | `npm init`, install `@playwright/test` |
| Node.js 20+ | Perlu verifikasi | CI: `actions/setup-node@v4` |
| Playwright Chromium | Perlu install | `npx playwright install chromium` |

---

### 10. Trade-offs

#### 10.1 Keputusan Arsitektur & Trade-offs

| Keputusan | Dipilih | Alternatif | Alasan |
|-----------|---------|-----------|--------|
| **Database E2E** | H2 in-memory MODE=MySQL | Testcontainers MariaDB | H2 lebih cepat start (0s vs 10s), zero infra dependency, cukup untuk E2E yang test UI behavior. Trade-off: beberapa MariaDB-specific behavior tidak ter-cover. |
| **Test isolation** | Unique data per test + shared server | Database reset per test | Reset 45 Flyway migrations per test = ~5s overhead × 45 tests = 225s wasted. Shared server + unique data = near-zero isolation overhead. Trade-off: DB bisa accumulate data over suite run, tapi setiap test hanya assert on its own data. |
| **Workers** | Single worker (`workers: 1`) | Parallel workers | Satu H2 instance = shared state. Parallel workers akan conflict. Trade-off: suite runs sequentially (~8-12 min total). Jika terlalu lambat, bisa scale ke multiple server instances di future. |
| **Component interaction** | JavaScript API (TomSelect `ts.setValue()`, Flatpickr `fp.setDate()`) | DOM click simulation | JS API deterministic 100% — click simulation bisa miss timing. Trade-off: tidak test exact user mouse/keyboard flow, tapi reliability > realism untuk CI. |
| **Auth** | Single admin session reuse via `storageState` | Login per test | 1 login vs 45 logins. Saves ~90s total. Trade-off: jika session expires, all subsequent tests fail. Mitigasi: session timeout 30m > suite duration. |
| **Test language** | TypeScript (Node.js @playwright/test) | Java (Playwright for Java) | Tim frontend familiar dengan TS, lebih mature helper ecosystem, community larger. Trade-off: Java team perlu belajar TS, tapi syntaxnya sederhana. |
| **Reporting** | HTML + JUnit XML + JSON | Allure Report | Built-in Playwright reporter cukup kaya. Allure menambah complexity. Trade-off: less pretty dashboard, tapi zero additional dependency. |
| **Seed data** | HTTP API via browser actions | Direct SQL INSERT ke H2 | HTTP API = tes full stack (controller → service → repo). Direct SQL bypass business logic dan bisa break jika schema berubah. Trade-off: seeding lebih lambat (~5s) tapi lebih robust. |
| **Test scope** | Happy path CRUD + major error case | Exhaustive permutation testing | E2E bukan tempat untuk test setiap edge case. 80% coverage dari 20% tests. Trade-off: edge case regressions missed, tapi covered by unit/integration tests. |
| **Flakiness handling** | Retry 2x in CI + quarantine @flaky | Zero retry + fail fast | Pure "zero retry" sounds ideal tapi ignores reality: async UI WILL have rare timing issues. 2 retries di CI = distinguish flaky from broken. Trade-off: flaky test bisa "hide" behind retries, sehingga butuh quarantine monitoring. |

#### 10.2 Apa yang TIDAK Dicakup E2E (By Design)

| Area | Mengapa Tidak Di-cover | Cara Cover |
|------|----------------------|------------|
| Business logic detail (kalkulasi, validasi rule) | Terlalu lambat dan fragile di E2E level | Unit tests (263 existing test files) |
| API contract / response format | E2E menggunakan browser, bukan API client | Integration tests |
| Performance / load testing | E2E single-user sequential | JMeter / Gatling terpisah |
| Visual regression (pixel-perfect) | High maintenance, low ROI untuk internal ERP | Manual QA + optional Playwright visual comparison |
| Multi-user concurrent scenario | Single H2 instance + single session | Dedicated load test environment |
| Mobile responsive | ERP internal = desktop-only | Manual verification |
| Cross-browser | Chromium cukup untuk CI, ERP target = Chrome | Manual cross-browser verification |
| MinIO file upload/download | Butuh MinIO instance, approval signature = niche feature | Integration test dengan Testcontainers MinIO |

#### 10.3 Success Criteria

| Metric | Target | Measurement |
|--------|--------|-------------|
| Flaky rate | **0%** (0 tests fail-then-pass in 10 consecutive runs) | `npx playwright test --repeat-each=10` |
| Suite duration | **< 15 minutes** total (CI, including server start) | GitHub Actions job duration |
| Test count | **≥ 45 test cases** across 14 spec files | `npx playwright test --list` |
| Module coverage | **100% of modules** with at least 1 CRUD test each | Manual audit |
| CI green rate | **≥ 98%** on main branch (excl. infra failures) | GitHub Actions history |
| Mean time to debug failure | **< 5 minutes** per failure (via trace + screenshot) | Developer feedback |
| Quarantine queue | **≤ 3 tests** in @flaky quarantine at any time | Weekly review |
