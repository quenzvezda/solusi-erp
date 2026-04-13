Agent completed. agent_id: proposal-p09-dx, agent_type: general-purpose, status: completed, description: P-09 DX Engineer proposal, elapsed: 309s, total_turns: 0, model: claude-opus-4.6, duration: 309s

Now I have comprehensive understanding of the codebase. Let me compile the proposal.

### 1. Executive Summary

Proyek Solusi ERP (Spring Boot 4.0.3 + Thymeleaf SSR) memiliki 48 controller, 45 Flyway migration, dan komponen UI kompleks (TomSelect, Flatpickr, AutoNumeric, HTMX) — namun **nol E2E test otomatis**. Setiap perubahan kode mengandalkan verifikasi manual di browser.

Proposal ini merancang **Playwright E2E test suite** di subdirektori `e2e/` terpisah (Node.js), berjalan melawan Spring Boot instance dengan **H2 in-memory (MODE=MySQL)**. Arsitektur didesain untuk **DX first**: satu perintah `npm test` menjalankan seluruh pipeline (start server → seed → test → cleanup), single test berjalan <30 detik, toggle `--headed` untuk debugging visual, dan kompatibel penuh dengan VS Code Playwright Extension. Target: 9 modul ter-cover dalam ~8 minggu effort.

---

### 2. Arsitektur Solusi

```
┌──────────────────────────────────────────────────────────────────┐
│                        Developer Workstation                      │
│                                                                    │
│  ┌──────────────────────┐     ┌────────────────────────────────┐  │
│  │      e2e/             │     │     Spring Boot (profile:e2e)  │  │
│  │  @playwright/test     │────▶│     Port: 18081 (configurable) │  │
│  │  Node.js 20+          │HTTP │     H2 MODE=MySQL in-memory    │  │
│  │                       │◀────│     Flyway migration on start  │  │
│  │  ┌─────────────────┐  │     │     SystemInitializer → admin  │  │
│  │  │ fixtures/        │  │     │     MinIO → mock/disabled      │  │
│  │  │ helpers/         │  │     │     Cookie secure=false        │  │
│  │  │ tests/           │  │     └────────────────────────────────┘  │
│  │  │   auth/          │  │                                        │
│  │  │   master/        │  │     ┌────────────────────────────────┐  │
│  │  │   inventory/     │  │     │     CI (GitHub Actions)         │  │
│  │  │   accounting/    │  │     │     Same e2e/ suite             │  │
│  │  │   system/        │  │     │     H2 (no external DB)         │  │
│  │  └─────────────────┘  │     │     Headless Chromium            │  │
│  └──────────────────────┘     └────────────────────────────────┘  │
└──────────────────────────────────────────────────────────────────┘
```

**Prinsip Arsitektur:**

1. **Isolated subdirectory** — `e2e/` memiliki `package.json` sendiri, tidak mencemari root Maven project
2. **Zero external dependency** — H2 in-memory menggantikan MariaDB, MinIO di-mock/disable; developer tidak perlu Docker untuk E2E
3. **Profile-driven** — Spring Boot profile `e2e` mengoverride datasource, port, security settings
4. **Parallel-safe** — Setiap test run menggunakan fresh H2 database (drop-create per server start)
5. **One process orchestration** — Script `npm test` mengelola lifecycle server (start → health check → run tests → kill)

**Struktur Direktori `e2e/`:**

```
e2e/
├── package.json
├── playwright.config.ts
├── tsconfig.json
├── .env.e2e                     # Override: BASE_URL, credentials
├── README.md                    # Onboarding doc (< 5 menit)
├── scripts/
│   ├── start-server.ps1         # Windows PowerShell
│   ├── start-server.sh          # Linux/macOS
│   └── wait-for-server.ts       # Health check poller
├── helpers/
│   ├── auth.ts                  # login(), loginAs(role)
│   ├── tomselect.ts             # selectTomSelect(), selectLineItemTS()
│   ├── flatpickr.ts             # setDate(), setDateTime()
│   ├── autonumeric.ts           # setNumeric(), setLineItemNumeric()
│   ├── form.ts                  # submitAndWait(), expectValidationError()
│   ├── navigation.ts            # gotoModule(), waitForPageLoad()
│   └── fixtures.ts              # loadFixture(), seedViaUI()
├── fixtures/
│   ├── brands.json
│   ├── product-categories.json
│   ├── products.json
│   └── parties.json
├── tests/
│   ├── auth/
│   │   ├── login.spec.ts
│   │   └── force-password-change.spec.ts
│   ├── master/
│   │   ├── geographic.spec.ts
│   │   ├── party.spec.ts
│   │   ├── currency.spec.ts
│   │   ├── tax.spec.ts
│   │   └── bank-account.spec.ts
│   ├── inventory/
│   │   ├── brand.spec.ts
│   │   ├── product-category.spec.ts
│   │   ├── uom.spec.ts
│   │   ├── facility.spec.ts
│   │   ├── product.spec.ts
│   │   └── stock-adjustment.spec.ts
│   ├── accounting/
│   │   ├── coa.spec.ts
│   │   ├── period.spec.ts
│   │   └── schema.spec.ts
│   └── system/
│       └── monitoring.spec.ts
└── test-results/                # .gitignore'd — screenshots, traces
```

---

### 3. Spring Boot E2E Profile Design

**File: `src/main/resources/application-e2e.yaml`**

```yaml
server:
  port: ${E2E_SERVER_PORT:18081}
  servlet:
    session:
      cookie:
        secure: false        # H2 tidak pakai HTTPS
        http-only: true

spring:
  datasource:
    url: jdbc:h2:mem:erp_e2e;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;DB_CLOSE_DELAY=-1
    username: sa
    password:
    driver-class-name: org.h2.Driver

  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: none          # Tetap pakai Flyway
    show-sql: false

  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration,classpath:db/e2e
    # db/e2e berisi seed data khusus E2E (admin password non-placeholder, demo data)

  h2:
    console:
      enabled: true           # Debug aid: http://localhost:18081/h2-console
      path: /h2-console
      settings:
        web-allow-others: false

  thymeleaf:
    cache: false

logging:
  level:
    com.solusi.erp: WARN      # Minimize noise saat test run
    org.springframework.security: ERROR
    org.hibernate.SQL: OFF
    org.flywaydb: INFO

# Disable MinIO (mock or stub)
minio:
  endpoint: http://localhost:19000    # Non-existent — will fail gracefully
  presigned-endpoint: http://localhost:19000
  access-key: test
  secret-key: test
  buckets:
    signatures: test-signatures
```

**Dependency tambahan di `pom.xml` (scope test/runtime):**

```xml
<!-- H2 Database for E2E profile -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>
```

**Flyway Compatibility Strategy:**

Semua 45 migration script menggunakan sintaks MariaDB (`ENGINE=InnoDB`, `COMMENT`, `COLLATE`). H2 dengan `MODE=MySQL` menangani sebagian besar, namun beberapa fitur tidak didukung. Strategi penanganan:

| Sintaks MariaDB | Kompatibilitas H2 MODE=MySQL | Solusi |
|---|---|---|
| `ENGINE=InnoDB` | ✅ Diabaikan (no-op) | Tidak perlu aksi |
| `COMMENT 'text'` | ✅ Didukung | Tidak perlu aksi |
| `DEFAULT CHARSET=utf8mb4 COLLATE=...` | ⚠️ Partial | Tambah init script `SET MODE MySQL` |
| `BIGINT AUTO_INCREMENT` | ✅ Didukung | Tidak perlu aksi |
| `NOW()` | ✅ Didukung | Tidak perlu aksi |
| `BOOLEAN` | ✅ Didukung | Tidak perlu aksi |

Jika ada migration yang gagal di H2, buat **shadow migration** di `db/e2e/` yang menjalankan `beforeMigrate` callback atau gunakan Flyway placeholder untuk skip MariaDB-only syntax:

**File: `src/main/resources/db/e2e/V0.1__H2_Compatibility.sql`**

```sql
-- H2 MODE=MySQL compatibility bootstrap
-- Runs before main migrations via Flyway ordering (V0 < V1)
SET MODE MySQL;
```

**Cara Aktivasi Profile:**

```bash
# Lokal — via Maven
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=e2e

# Lokal — via JAR
java -jar target/solusi-program-erp-1.2.0.jar --spring.profiles.active=e2e

# CI — environment variable
SPRING_PROFILES_ACTIVE=e2e java -jar app.jar
```

**MinIO Handling:**

MinIO digunakan untuk approval signatures. Untuk E2E, dua opsi:

1. **Conditional Bean (Recommended):** Buat `MinioConfig` yang return no-op stub ketika profile `e2e` aktif
2. **Catch-and-ignore:** Pastikan controller yang panggil MinIO handle `ConnectException` gracefully — ini sudah best practice untuk production resilience

```java
@Configuration
@Profile("e2e")
public class MinioE2eConfig {
    @Bean
    public MinioClient minioClient() {
        // Return a client pointing to nonexistent endpoint
        // Operations will fail fast — controllers should handle gracefully
        return MinioClient.builder()
                .endpoint("http://localhost:19000")
                .credentials("test", "test")
                .build();
    }
}
```

---

### 4. Data Seeding Strategy

**Pendekatan: Layered Seeding**

```
Layer 1: Flyway V1-V45 migration     → Schema + RBAC seed (permissions, roles, admin user)
Layer 2: db/e2e/ seed scripts         → Demo data untuk E2E (master data minimum)
Layer 3: Playwright test fixtures     → Per-test data via UI interaction
```

**Layer 1 — Sudah Ada (Flyway migrations):**

`V1__Initial_Security_Schema.sql` membuat tabel security, `V2__Seed_Security_Data.sql` membuat admin user dengan password `INITIAL_PASSWORD_SETUP`. `SystemInitializer` (CommandLineRunner) encode ke BCrypt saat aplikasi start. Ini **otomatis bekerja** dengan H2.

**Layer 2 — E2E Seed Data (`src/main/resources/db/e2e/`):**

```sql
-- V99.1__E2E_Admin_Ready.sql
-- Override admin password_change_required agar test tidak stuck di force-change page
-- (SystemInitializer sudah set password, tapi flag masih true)
UPDATE users SET password_change_required = false WHERE username = 'admin';

-- V99.2__E2E_Master_Data.sql
-- Minimum master data agar form yang depend on lookup punya opsi
INSERT INTO currencies (code, name, symbol, ...) VALUES
('IDR', 'Indonesian Rupiah', 'Rp', ...),
('USD', 'US Dollar', '$', ...);

INSERT INTO tax_types (code, name, rate, ...) VALUES
('PPN', 'Pajak Pertambahan Nilai', 11.00, ...);

-- UoM base data
INSERT INTO uoms (code, name, ...) VALUES
('PCS', 'Pieces', ...), ('KG', 'Kilogram', ...), ('BOX', 'Box', ...);
```

**Layer 3 — Per-Test UI Seeding (via Playwright helpers):**

```typescript
// helpers/fixtures.ts
export async function seedBrand(page: Page, name: string): Promise<void> {
  await page.goto('/inventory/brands/create');
  await page.fill('input[name="name"]', name);
  await submitAndWait(page, '/inventory/brands');
}

export async function seedProductCategory(page: Page, name: string): Promise<void> {
  await page.goto('/inventory/product-categories/create');
  await page.fill('input[name="name"]', name);
  await submitAndWait(page, '/inventory/product-categories');
}
```

**Mengapa UI seeding, bukan SQL langsung?**

1. Validasi domain logic tetap berjalan (code generation, audit trail)
2. SystemInitializer dan event listeners tetap aktif
3. Tidak perlu maintain SQL insert yang sinkron dengan perubahan schema
4. Test fixture sekaligus test create flow

**Test Isolation Strategy:**

- Setiap `test.describe` block yang butuh data spesifik, seed di `test.beforeAll`
- Data diberi prefix unik: `E2E-BRAND-{timestamp}` untuk avoid collision
- Karena H2 in-memory, setiap server restart = database bersih

---

### 5. Playwright Test Architecture

**5.1 `package.json`**

```json
{
  "name": "solusi-erp-e2e",
  "private": true,
  "version": "1.0.0",
  "scripts": {
    "test": "npx playwright test",
    "test:headed": "npx playwright test --headed",
    "test:ui": "npx playwright test --ui",
    "test:debug": "npx playwright test --debug",
    "test:auth": "npx playwright test tests/auth/",
    "test:master": "npx playwright test tests/master/",
    "test:inventory": "npx playwright test tests/inventory/",
    "test:accounting": "npx playwright test tests/accounting/",
    "test:module": "npx playwright test --grep",
    "server:start": "node scripts/start-server.mjs",
    "server:wait": "node scripts/wait-for-server.mjs",
    "e2e": "npm run server:start && npm run server:wait && npm test; npm run server:stop",
    "e2e:ci": "npm run server:start && npm run server:wait && npm test -- --reporter=github; npm run server:stop",
    "server:stop": "node scripts/stop-server.mjs",
    "report": "npx playwright show-report"
  },
  "devDependencies": {
    "@playwright/test": "^1.52.0",
    "@types/node": "^20.0.0",
    "dotenv": "^16.4.0",
    "wait-on": "^7.2.0"
  }
}
```

**5.2 `playwright.config.ts`**

```typescript
import { defineConfig, devices } from '@playwright/test';
import dotenv from 'dotenv';
import path from 'path';

dotenv.config({ path: path.resolve(__dirname, '.env.e2e') });

const BASE_URL = process.env.BASE_URL || 'http://localhost:18081';

export default defineConfig({
  testDir: './tests',
  timeout: 60_000,
  expect: { timeout: 10_000 },
  retries: process.env.CI ? 2 : 0,
  workers: 1,  // Sequential — shared DB state
  
  reporter: process.env.CI
    ? [['github'], ['html', { open: 'never' }]]
    : [['list'], ['html', { open: 'on-failure' }]],

  use: {
    baseURL: BASE_URL,
    headless: !process.env.HEADED,
    screenshot: 'only-on-failure',
    trace: 'retain-on-failure',
    video: 'retain-on-failure',
    locale: 'id-ID',
    timezoneId: 'Asia/Jakarta',
    actionTimeout: 15_000,
    navigationTimeout: 30_000,
  },

  // Global setup: login once, save auth state
  globalSetup: './helpers/global-setup.ts',

  projects: [
    {
      name: 'setup',
      testMatch: /global\.setup\.ts/,
    },
    {
      name: 'chromium',
      use: {
        ...devices['Desktop Chrome'],
        storageState: './test-results/.auth/admin.json',
      },
      dependencies: ['setup'],
    },
  ],
});
```

**5.3 Global Setup — Authenticated State**

```typescript
// helpers/global-setup.ts
import { chromium, FullConfig } from '@playwright/test';
import path from 'path';
import fs from 'fs';

const AUTH_FILE = path.join(__dirname, '../test-results/.auth/admin.json');

async function globalSetup(config: FullConfig) {
  const baseURL = config.projects[0].use?.baseURL || 'http://localhost:18081';
  
  // Ensure directory exists
  fs.mkdirSync(path.dirname(AUTH_FILE), { recursive: true });

  const browser = await chromium.launch();
  const page = await browser.newPage();

  // Login
  await page.goto(`${baseURL}/login`);
  await page.fill('input[name="username"]', 'admin');
  await page.fill('input[name="password"]', 'admin123');
  await page.click('button[type="submit"]');
  
  // Handle force password change if needed
  if (page.url().includes('/reset-password')) {
    await page.fill('input[name="currentPassword"]', 'admin123');
    await page.fill('input[name="newPassword"]', 'admin123');
    await page.fill('input[name="confirmPassword"]', 'admin123');
    await page.click('button[type="submit"]');
  }
  
  await page.waitForURL('**/dashboard**', { timeout: 15_000 });

  // Save authenticated state
  await page.context().storageState({ path: AUTH_FILE });
  await browser.close();
}

export default globalSetup;
```

**5.4 Helper: Component Interaction Library**

```typescript
// helpers/tomselect.ts
import { Page } from '@playwright/test';

/**
 * Pilih opsi di TomSelect via JS API.
 * Reliable untuk semua TomSelect: header form dan line item.
 */
export async function selectTomSelect(
  page: Page,
  selector: string,
  query: string = '',
  index: number = 0
): Promise<{ id: string; name: string } | { error: string }> {
  return await page.evaluate(
    ({ sel, q, idx }) => {
      return new Promise((resolve, reject) => {
        const el = document.querySelector(sel) as any;
        if (!el?.tomselect) {
          reject(new Error(`TomSelect not found: ${sel}. Check selector or wait for init.`));
          return;
        }
        const ts = el.tomselect;
        ts.load(q, (opts: any[]) => {
          if (!opts.length) { resolve({ error: `No options for query "${q}"` }); return; }
          opts.forEach((o: any) => ts.addOption(o));
          const pick = opts[Math.min(idx, opts.length - 1)];
          ts.setValue(pick.id);
          resolve({ id: pick.id, name: pick.name });
        });
      });
    },
    { sel: selector, q: query, idx: index }
  );
}

/**
 * Wait hingga TomSelect terinisialisasi pada selector.
 */
export async function waitForTomSelect(page: Page, selector: string, timeout = 5000) {
  await page.waitForFunction(
    (sel) => {
      const el = document.querySelector(sel) as any;
      return el && el.tomselect;
    },
    selector,
    { timeout }
  );
}

// helpers/flatpickr.ts
export async function setFlatpickrDate(page: Page, selector: string, dateStr: string) {
  await page.evaluate(
    ({ sel, d }) => {
      const input = document.querySelector(sel) as any;
      if (input?._flatpickr) input._flatpickr.setDate(d, true);
      else if (input) {
        input.value = d;
        input.dispatchEvent(new Event('change', { bubbles: true }));
      }
    },
    { sel: selector, d: dateStr }
  );
}

// helpers/autonumeric.ts
export async function setAutoNumeric(page: Page, selector: string, value: number) {
  await page.evaluate(
    ({ sel, v }) => {
      const el = document.querySelector(sel) as any;
      if (typeof AutoNumeric !== 'undefined') {
        const inst = (AutoNumeric as any).getAutoNumericElement(el);
        if (inst) { inst.set(v); return; }
      }
      el.value = v;
      el.dispatchEvent(new Event('input', { bubbles: true }));
    },
    { sel: selector, v: value }
  );
}

// helpers/form.ts
import { Page, expect } from '@playwright/test';

export async function submitAndExpectSuccess(page: Page, expectedListUrl: string) {
  const responsePromise = page.waitForResponse(
    (r) => r.request().method() === 'POST' && (r.headers()['content-type'] ?? '').includes('json'),
    { timeout: 15_000 }
  );
  await page.click('button[type="submit"]');
  const resp = await responsePromise;
  const body = await resp.json();
  
  expect(body.success, `Form submit failed: ${JSON.stringify(body)}`).toBeTruthy();
  await page.waitForURL(`**${expectedListUrl}**`, { timeout: 10_000 });
  return body;
}

export async function submitAndExpectError(page: Page) {
  const responsePromise = page.waitForResponse(
    (r) => r.request().method() === 'POST' && (r.headers()['content-type'] ?? '').includes('json'),
    { timeout: 15_000 }
  );
  await page.click('button[type="submit"]');
  const resp = await responsePromise;
  const body = await resp.json();

  expect(body.success, 'Expected validation error but got success').toBeFalsy();
  return body;
}
```

**5.5 Contoh Test: Inventory Brand CRUD**

```typescript
// tests/inventory/brand.spec.ts
import { test, expect } from '@playwright/test';
import { submitAndExpectSuccess } from '../../helpers/form';

const BRAND_NAME = `E2E-Brand-${Date.now()}`;

test.describe('Inventory > Brand CRUD', () => {
  test.describe.configure({ mode: 'serial' });

  test('should display brand list page', async ({ page }) => {
    await page.goto('/inventory/brands');
    await expect(page.locator('h1, h2, .page-title')).toContainText(/brand/i);
  });

  test('should create a new brand', async ({ page }) => {
    await page.goto('/inventory/brands/create');
    await page.waitForLoadState('networkidle');

    await page.fill('input[name="name"]', BRAND_NAME);
    await submitAndExpectSuccess(page, '/inventory/brands');
    
    // Verify in list
    await expect(page.locator('table')).toContainText(BRAND_NAME);
  });

  test('should edit the brand', async ({ page }) => {
    await page.goto('/inventory/brands');
    
    // Find the row with our brand and click edit
    const row = page.locator('tr', { hasText: BRAND_NAME });
    await row.locator('a[href*="edit"]').click();
    await page.waitForLoadState('networkidle');

    const updatedName = `${BRAND_NAME}-UPDATED`;
    await page.fill('input[name="name"]', updatedName);
    await submitAndExpectSuccess(page, '/inventory/brands');

    await expect(page.locator('table')).toContainText(updatedName);
  });

  test('should show validation error on empty name', async ({ page }) => {
    await page.goto('/inventory/brands/create');
    await page.waitForLoadState('networkidle');

    // Submit without filling name
    await page.fill('input[name="name"]', '');
    await page.click('button[type="submit"]');

    // Expect validation indicator
    await expect(page.locator('.is-invalid, .invalid-feedback, .alert-danger')).toBeVisible();
  });
});
```

**5.6 Contoh Test: Stock Adjustment (Komponen Kompleks)**

```typescript
// tests/inventory/stock-adjustment.spec.ts
import { test, expect } from '@playwright/test';
import { selectTomSelect, waitForTomSelect } from '../../helpers/tomselect';
import { setFlatpickrDate } from '../../helpers/flatpickr';
import { setAutoNumeric } from '../../helpers/autonumeric';
import { submitAndExpectSuccess } from '../../helpers/form';

test.describe('Inventory > Stock Adjustment', () => {
  test.describe.configure({ mode: 'serial' });

  test('should create stock adjustment with line items', async ({ page }) => {
    await page.goto('/inventory/adjustments/create');
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(1000); // TomSelect init

    // Header: Facility
    await waitForTomSelect(page, '#header-facility');
    await selectTomSelect(page, '#header-facility', '', 0);

    // Header: Date
    await setFlatpickrDate(page, 'input[name="transactionDate"]', '2026-01-15');

    // Header: Notes
    await page.fill('textarea[name="note"]', 'E2E test adjustment');

    // Add line item
    await page.click('#btn-add-line');
    await page.waitForTimeout(500);

    // Line: Product
    await selectTomSelect(page, '.line-row:last-child .select-product', '', 0);
    await page.waitForTimeout(300);

    // Line: Grid & Container (cascade)
    await selectTomSelect(page, '.line-row:last-child .select-grid', '', 0);
    await page.waitForTimeout(200);
    await selectTomSelect(page, '.line-row:last-child .select-container', '', 0);

    // Line: Qty via drawer
    await page.click('.line-row:last-child .btn-edit-qty');
    await page.waitForTimeout(500);
    await setAutoNumeric(page, '.offcanvas.show .input-qty-target, dialog[open] .input-qty-target', 10);
    await page.click('.offcanvas.show .btn-apply, dialog[open] .btn-apply');
    await page.waitForTimeout(300);

    // Line: Price
    await setAutoNumeric(page, '.line-row:last-child .input-price', 50000);

    // Submit
    await submitAndExpectSuccess(page, '/inventory/adjustments');
  });
});
```

**5.7 Server Lifecycle Scripts**

```javascript
// scripts/start-server.mjs
import { spawn } from 'child_process';
import { writeFileSync } from 'fs';
import path from 'path';

const ROOT = path.resolve(import.meta.dirname, '../..');
const isWindows = process.platform === 'win32';
const port = process.env.E2E_SERVER_PORT || '18081';

const mvnw = isWindows ? '.\\mvnw.cmd' : './mvnw';
const args = [
  'spring-boot:run',
  `-Dspring-boot.run.profiles=e2e`,
  `-Dspring-boot.run.arguments=--server.port=${port}`,
];

console.log(`🚀 Starting Spring Boot (profile=e2e, port=${port})...`);

const child = spawn(mvnw, args, {
  cwd: ROOT,
  stdio: 'pipe',
  shell: isWindows,
  detached: !isWindows,
  env: { ...process.env, ENV_FILE: '.env.e2e' },
});

// Save PID for cleanup
writeFileSync(
  path.join(import.meta.dirname, '.server.pid'),
  String(child.pid)
);

child.stdout?.on('data', (d) => {
  const line = d.toString();
  if (line.includes('Started') || line.includes('ERROR')) {
    process.stdout.write(line);
  }
});

child.stderr?.on('data', (d) => process.stderr.write(d));

// Detach — let parent exit
child.unref();
console.log(`   PID: ${child.pid}`);
```

```javascript
// scripts/wait-for-server.mjs
const BASE = process.env.BASE_URL || 'http://localhost:18081';
const MAX_WAIT = 120_000; // 2 minutes
const INTERVAL = 2_000;

async function waitForServer() {
  const start = Date.now();
  console.log(`⏳ Waiting for server at ${BASE}/login ...`);
  
  while (Date.now() - start < MAX_WAIT) {
    try {
      const resp = await fetch(`${BASE}/login`, { redirect: 'manual' });
      if (resp.status === 200 || resp.status === 302) {
        console.log(`✅ Server ready! (${Math.round((Date.now() - start) / 1000)}s)`);
        process.exit(0);
      }
    } catch { /* not ready yet */ }
    await new Promise(r => setTimeout(r, INTERVAL));
  }
  
  console.error(`❌ Server did not start within ${MAX_WAIT / 1000}s`);
  process.exit(1);
}

waitForServer();
```

```javascript
// scripts/stop-server.mjs
import { readFileSync, unlinkSync } from 'fs';
import { execSync } from 'child_process';
import path from 'path';

const pidFile = path.join(import.meta.dirname, '.server.pid');
try {
  const pid = readFileSync(pidFile, 'utf8').trim();
  const isWindows = process.platform === 'win32';
  
  if (isWindows) {
    execSync(`taskkill /PID ${pid} /T /F`, { stdio: 'ignore' });
  } else {
    execSync(`kill -TERM -${pid}`, { stdio: 'ignore' });
  }
  
  unlinkSync(pidFile);
  console.log(`🛑 Server stopped (PID: ${pid})`);
} catch {
  console.log('ℹ️  No server process to stop');
}
```

**5.8 DX Features Summary**

| Fitur DX | Implementasi | Perintah |
|---|---|---|
| One-command setup | `cd e2e && npm install` | Install semua deps + browser |
| One-command run (full) | `npm run e2e` | Start → wait → test → stop |
| Single test | `npx playwright test brand.spec.ts` | <30s (server sudah jalan) |
| Headed debugging | `npm run test:headed` | Toggle env `HEADED=1` |
| Step-by-step debug | `npm run test:debug` | Playwright Inspector |
| VS Code integration | Install "Playwright Test for VS Code" extension | Klik ▶ di gutter |
| Interactive UI mode | `npm run test:ui` | Playwright UI + time travel |
| Module-specific | `npm run test:inventory` | Filter by folder |
| Grep filter | `npx playwright test --grep "brand"` | Filter by name |
| View report | `npm run report` | HTML report with screenshots |
| Failure artifacts | Auto screenshot + trace | `test-results/` folder |

---

### 6. CI/CD Integration

**File: `.github/workflows/ci-java21.yml` — E2E Job (tambahan)**

```yaml
  # ──────────────────────────────────────────────────────────────
  # Job 3: E2E — Playwright tests against Spring Boot + H2
  # Runs on: push to main, nightly schedule, manual dispatch
  # ──────────────────────────────────────────────────────────────
  e2e-tests:
    name: E2E Tests (Playwright)
    runs-on: ubuntu-latest
    timeout-minutes: 20
    if: |
      github.event_name == 'schedule' ||
      (github.event_name == 'workflow_dispatch' && inputs.run_full == true) ||
      (github.event_name == 'push' && (github.ref == 'refs/heads/main' || github.ref == 'refs/heads/master'))
    needs: [fast-tests]    # Only run if unit tests pass

    steps:
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
          cache: 'npm'
          cache-dependency-path: 'e2e/package-lock.json'

      - name: Install E2E dependencies
        working-directory: e2e
        run: npm ci

      - name: Install Playwright browsers
        working-directory: e2e
        run: npx playwright install chromium --with-deps

      - name: Build Spring Boot JAR (skip tests)
        run: |
          chmod +x mvnw
          ./mvnw -B package -DskipTests -q

      - name: Start Spring Boot (H2 profile)
        run: |
          java -jar target/solusi-program-erp-*.jar \
            --spring.profiles.active=e2e \
            --server.port=18081 &
          echo $! > .server.pid

      - name: Wait for server
        working-directory: e2e
        run: node scripts/wait-for-server.mjs
        env:
          BASE_URL: http://localhost:18081

      - name: Run Playwright tests
        working-directory: e2e
        run: npx playwright test --reporter=github,html
        env:
          BASE_URL: http://localhost:18081
          CI: true

      - name: Stop Spring Boot
        if: always()
        run: |
          if [ -f .server.pid ]; then
            kill $(cat .server.pid) || true
          fi

      - name: Upload E2E report
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: e2e-playwright-report
          path: e2e/playwright-report/
          retention-days: 14

      - name: Upload E2E traces (on failure)
        if: failure()
        uses: actions/upload-artifact@v4
        with:
          name: e2e-traces
          path: e2e/test-results/
          retention-days: 7
```

**Deploy job update — tambahkan dependency:**

```yaml
  deploy:
    needs: [fast-tests, full-tests, e2e-tests]
    # ... existing deploy config
```

**CI vs Local Parity:**

| Aspek | Local | CI |
|---|---|---|
| Database | H2 MODE=MySQL in-memory | H2 MODE=MySQL in-memory |
| Browser | Chromium (headed/headless) | Chromium headless |
| Spring Profile | `e2e` | `e2e` |
| Port | 18081 | 18081 |
| Retries | 0 | 2 |
| Reporter | list + html (open on failure) | github + html (artifact) |
| Artifacts | `test-results/` lokal | Upload ke GitHub Artifacts |

---

### 7. Cakupan Test Minimum per Modul

**Prioritas implementasi berdasarkan dependency chain dan kompleksitas UI:**

| # | Modul | Test Cases | Kompleksitas | Prioritas |
|---|---|---|---|---|
| 1 | **Auth** | Login, logout, force password change, invalid creds, session expiry | Low | P0 — Prerequisite |
| 2 | **Inventory > Brand** | List, create, edit, validation error, search | Low | P0 — Simplest CRUD |
| 3 | **Inventory > Product Category** | List, create, edit, validation | Low | P0 |
| 4 | **Inventory > UoM** | List, create, edit | Low | P0 |
| 5 | **Master > Geographic** | List, create (province/city/district), edit | Medium (hierarchy) | P1 |
| 6 | **Master > Party** | List, create (contacts + addresses inline), edit, type filter | High (line items, TomSelect cascade) | P1 |
| 7 | **Inventory > Facility** | List, create hierarchy (Warehouse > Grid > Container), edit | Medium (hierarchy CRUD) | P1 |
| 8 | **Inventory > Product** | List, create (TomSelect: category, brand; select: UoM), edit | Medium (TomSelect) | P1 |
| 9 | **Inventory > Stock Adjustment** | Create (header + line items + drawer + serial), submit, list verify | Very High (all UI components) | P2 — Most complex |
| 10 | **Accounting > COA** | List, create (parent TomSelect, hierarchy), edit | Medium (TomSelect) | P2 |
| 11 | **Accounting > Period** | List, create, fiscal year | Low | P2 |
| 12 | **Security > User/Role** | List, create user with role, edit | Medium (TomSelect) | P2 |

**Detail Test Case per Modul:**

**Auth (5 tests)**
- `login.spec.ts`: Login sukses → dashboard, login gagal → error message, logout → redirect login
- `force-password-change.spec.ts`: Redirect ke `/reset-password`, setelah change → dashboard

**Brand — Template CRUD (4 tests)**
- List page loads, create with valid data, edit existing, submit empty name → validation error

**Product (5 tests)**
- List page loads, create with TomSelect (category, brand) + standard select (UoM), edit, search by name, validation error

**Stock Adjustment (6 tests)**
- Create with 1 line item (non-serialized), create with serialized product + serial numbers, multi-line item, drawer qty edit, validation (no lines → error), list shows created adjustment

**Party (5 tests)**
- Create company type, create individual type, add inline contact row, add inline address with geographic TomSelect, edit party

**Total minimum: ~50 test cases across 12 modules**

---

### 8. Risiko & Mitigasi

| # | Risiko | Likelihood | Impact | Mitigasi |
|---|---|---|---|---|
| 1 | **H2 vs MariaDB incompatibility** — Flyway migration gagal di H2 karena syntax MariaDB-specific (COLLATE, stored procedures) | **High** | High | Shadow migration di `db/e2e/` yang patch syntax. Jika terlalu banyak, fallback ke Testcontainers MariaDB (lihat Trade-offs). Buat script validator yang compare H2 schema vs MariaDB schema. |
| 2 | **Flaky tests** — TomSelect/AJAX timing unpredictable | **Medium** | Medium | Gunakan `waitForResponse` + `waitForFunction` (bukan `waitForTimeout`). Helper function standar dengan retry logic. CI retries=2. |
| 3 | **Slow server startup** — Maven compile + Spring Boot start bisa 30-60 detik | **Medium** | Low | Untuk iterasi cepat: developer start server sekali, jalankan test berulang tanpa restart. Script `npm test` deteksi server yang sudah jalan. CI: build JAR dulu (`mvn package -DskipTests`), lalu `java -jar`. |
| 4 | **MinIO dependency** — Approval signature upload gagal | **Low** | Low | E2E profile disable MinIO atau mock. Test approval flow skip signature upload. |
| 5 | **Data ordering** — Test depend pada data dari test lain | **Medium** | High | Gunakan `test.describe.configure({ mode: 'serial' })` per module. Setiap module suite seed datanya sendiri. Cross-module dependency via shared seeding di `globalSetup`. |
| 6 | **Browser version drift** — CI dan local pakai browser berbeda | **Low** | Low | Lock Playwright version di `package-lock.json`. `npx playwright install chromium` memastikan browser version konsisten. |
| 7 | **Developer adoption rendah** — Tim tidak menjalankan E2E | **Medium** | High | DX-first design: one-command, headed mode, VS Code integration. README dengan GIF/screenshot. Wajibkan E2E untuk modul baru. Code review checklist include E2E. |

---

### 9. Effort Estimate

**Fase 1 — Foundation (Minggu 1–2): 5 hari**

| Task | Effort | Output |
|---|---|---|
| Spring Boot `application-e2e.yaml` + H2 setup | 1 hari | Profile working, Flyway migrates to H2 |
| Fix H2/MariaDB migration incompatibilities | 1 hari | All 45 migrations pass on H2 |
| `e2e/` scaffolding (package.json, config, scripts) | 0.5 hari | `npm install` → ready |
| Server lifecycle scripts (start/wait/stop) | 0.5 hari | `npm run e2e` → end-to-end |
| Helper library (auth, tomselect, flatpickr, autonumeric, form) | 1.5 hari | All helpers tested |
| Global setup (login state) + README | 0.5 hari | Onboarding < 5 menit |

**Fase 2 — Core Test Suite (Minggu 3–5): 8 hari**

| Task | Effort | Output |
|---|---|---|
| Auth tests (login, logout, force-change) | 0.5 hari | 5 tests |
| Brand + Product Category + UoM (simple CRUD template) | 1 hari | 12 tests |
| Geographic + Party (hierarchy + inline rows) | 2 hari | 10 tests |
| Facility hierarchy (Warehouse > Grid > Container) | 1 hari | 5 tests |
| Product (TomSelect + standard select combo) | 1 hari | 5 tests |
| Stock Adjustment (drawer, serial, multi-line) | 2 hari | 6 tests |
| E2E seed data (`db/e2e/` SQL files) | 0.5 hari | Master data ready |

**Fase 3 — CI Integration + Polish (Minggu 6): 3 hari**

| Task | Effort | Output |
|---|---|---|
| GitHub Actions workflow (e2e job) | 0.5 hari | CI pipeline |
| Fix flaky tests + timing issues | 1.5 hari | Stable suite |
| Accounting modules (COA, Period, Schema) | 0.5 hari | 8 tests |
| Security modules (User, Role management) | 0.5 hari | 5 tests |

**Fase 4 — Onboarding + Handoff (Minggu 7): 2 hari**

| Task | Effort | Output |
|---|---|---|
| README.md (dengan contoh, troubleshooting) | 0.5 hari | Onboarding doc |
| Team walkthrough + pair writing 1 test | 1 hari | Team confidence |
| VS Code workspace settings + recommended extensions | 0.5 hari | .vscode/extensions.json |

**Total: ~18 hari kerja (3.5 minggu efektif), ~56 test cases**

**Ongoing maintenance estimate: 0.5–1 hari per sprint** untuk fix flaky tests dan tambah test untuk fitur baru.

---

### 10. Trade-offs

| Keputusan | Dipilih ✅ | Alternatif ❌ | Alasan |
|---|---|---|---|
| **Database E2E** | H2 in-memory MODE=MySQL | Testcontainers MariaDB | H2 = zero Docker dependency, startup <3 detik, developer tidak perlu install Docker. Trade-off: ada risiko syntax incompatibility. Jika >5 migration gagal di H2, fallback ke Testcontainers. |
| **Test runner** | @playwright/test (Node.js) | Selenium Java, Cypress | Playwright: auto-wait, multi-browser, trace viewer, VS Code integration, TypeScript. Cypress: no SSR-friendly. Selenium Java: verbose, poor DX. Node.js Playwright terpisah dari Java codebase = separation of concerns. |
| **Test language** | TypeScript | JavaScript | Type safety untuk helper functions, better IDE autocomplete, minimal overhead (just tsconfig). |
| **Workers** | Single worker (sequential) | Parallel workers | Shared H2 database = parallel tests bisa clash. Single worker menghindari flakiness. Saat suite tumbuh >100 tests, bisa switch ke parallel + per-worker database instance. |
| **Auth strategy** | Global setup → storageState reuse | Login per test | Login per test = +2 detik/test. Dengan 50 tests = +100 detik. Global storageState = login sekali, semua test pakai session cookie. Trade-off: jika session expire mid-suite, beberapa test fail. Mitigasi: set session timeout 30m di profile E2E. |
| **Data seeding** | UI-based seeding (Layer 3) | Direct SQL insert via JDBC | UI seeding validates the create flow itself. SQL insert bypasses validation, code generation, audit trail. Trade-off: UI seeding lebih lambat (+2-3 detik per entity). Untuk large seed, gunakan Layer 2 SQL. |
| **Subdirectory terpisah** | `e2e/` di root project | Folder di `src/test/` | Separation: Node.js project tidak campur dengan Maven. Developer bisa `cd e2e && npm test` tanpa tahu Java. Trade-off: dua package manager (Maven + npm). |
| **Headed default** | Headless (CI-friendly) | Headed (debug-friendly) | CI = headless mandatory. Lokal: toggle via `HEADED=1 npm test` atau `npm run test:headed`. VS Code extension handles this automatically. |
| **Retry strategy** | 0 retries local, 2 retries CI | Uniform retry | Local: fail-fast agar developer fix immediately. CI: retry untuk absorb infra flakiness (network, resource contention). |
| **MinIO** | Disable/stub di E2E profile | Run MinIO container | Approval signature upload bukan core CRUD flow. Menambah container dependency = melanggar "zero external dependency". Test approval flow tanpa signature. |
| **Browser scope** | Chromium only | Chromium + Firefox + WebKit | ERP internal tool, user pakai Chrome/Edge. Multi-browser testing menambah CI time 3x tanpa value signifikan. Tambahkan Firefox jika ada user request. |

**Kapan Harus Pivot ke Testcontainers:**

Jika dalam Fase 1 ditemukan **>5 Flyway migration yang memerlukan modifikasi signifikan** agar jalan di H2, sebaiknya pivot ke Testcontainers MariaDB. Impact: developer butuh Docker installed, startup +10 detik, tapi zero migration compatibility issues. Keputusan ini harus diambil di akhir Minggu 1.