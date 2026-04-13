# Proposal Teknis: Playwright E2E Testing — Perspektif Security Engineer

### 1. Executive Summary

Proyek ERP ini memiliki arsitektur keamanan yang solid — session-based auth, permission-based RBAC dengan 112 permissions, CSRF protection, dan mandatory password change flow. Namun, **TIDAK ADA satupun E2E test yang memvalidasi bahwa security enforcement benar-benar bekerja di browser**. Unit test yang ada menguji komponen secara terisolasi; mereka tidak menangkap skenario dimana CSRF token gagal dikirim via AJAX, dimana `sec:authorize` salah menampilkan tombol kepada user tanpa izin, atau dimana `ForcePasswordChangeFilter` bisa di-bypass via direct URL.

Proposal ini merancang Playwright E2E test suite yang **login melalui form login sesungguhnya** (bukan bypass), menguji 403/redirect pada akses tanpa izin, memvalidasi CSRF pada setiap AJAX submission, dan membuktikan bahwa session management serta password-change enforcement bekerja end-to-end di atas H2 in-memory database.

---

### 2. Arsitektur Solusi

```
┌─────────────────────────────────────────────────────────────────────┐
│                        CI Pipeline (GitHub Actions)                 │
│                                                                     │
│  ┌──────────────────────┐       ┌────────────────────────────────┐  │
│  │  Spring Boot App      │       │   Playwright Test Runner       │  │
│  │  (Profile: e2e)       │       │   (Node.js / @playwright/test) │  │
│  │                       │ HTTP  │                                │  │
│  │  ┌─────────────────┐  │◄─────►│  ┌──────────────────────────┐ │  │
│  │  │ Spring Security  │  │      │  │ Auth Helper (real login)  │ │  │
│  │  │ (CSRF, Session,  │  │      │  │ CSRF Token Extractor      │ │  │
│  │  │  RBAC, Filters)  │  │      │  │ Permission Matrix Tests   │ │  │
│  │  └─────────────────┘  │      │  └──────────────────────────┘ │  │
│  │  ┌─────────────────┐  │      │  ┌──────────────────────────┐ │  │
│  │  │ H2 Database      │  │      │  │ storageState per role    │ │  │
│  │  │ (MODE=MySQL)     │  │      │  │ (auth cache, NO bypass)  │ │  │
│  │  │ + Flyway migrate │  │      │  └──────────────────────────┘ │  │
│  │  └─────────────────┘  │      └────────────────────────────────┘  │
│  └──────────────────────┘                                           │
└─────────────────────────────────────────────────────────────────────┘
```

**Prinsip Arsitektur Security-First:**

| Prinsip | Implementasi |
|---------|-------------|
| **No Auth Bypass** | Login SELALU via POST `/login` form. Tidak ada mock session, tidak ada injeksi cookie manual. `storageState` hanya menyimpan session cookie yang didapat dari login form yang sah. |
| **Real Security Filter Chain** | Spring Boot berjalan penuh dengan `SecurityConfig`, `ForcePasswordChangeFilter`, `CustomAuthenticationSuccessHandler`. Semua filter aktif. |
| **Real CSRF Enforcement** | Setiap AJAX request harus mengambil CSRF token dari `<meta name="_csrf">` atau hidden input. Test gagal jika token tidak dikirim. |
| **Real Permission Check** | `@PreAuthorize("hasAuthority('...')")` dan `sec:authorize` aktif. Test membuktikan enforcement di server DAN di template. |
| **Isolated H2 Database** | Setiap test run dimulai dari state bersih via Flyway migration + E2E seeder. Tidak ada data bocor antar run. |

**Komponen Utama:**

1. **Spring Boot E2E Profile (`application-e2e.yaml`)** — Override datasource ke H2 `MODE=MySQL`, disable secure cookie (HTTP untuk localhost), set logging level
2. **E2E Data Seeder (`E2eDataSeeder.java`)** — `CommandLineRunner` khusus profile `e2e`, membuat multiple user dengan role berbeda setelah Flyway migrasi selesai
3. **Playwright Test Suite (`e2e-tests/`)** — Subdirectory Node.js terpisah, menjalankan test melawan running Spring Boot instance
4. **Auth Setup Project (`e2e-tests/auth.setup.ts`)** — Login via form untuk setiap test user, simpan `storageState` ke file per role

---

### 3. Spring Boot E2E Profile Design

#### 3.1 `application-e2e.yaml`

```yaml
# src/main/resources/application-e2e.yaml
server:
  port: 8081
  servlet:
    session:
      timeout: 30m
      cookie:
        secure: false   # CRITICAL: H2/localhost uses HTTP, not HTTPS
        http-only: true
        same-site: lax

spring:
  datasource:
    url: jdbc:h2:mem:e2e_test_db;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE
    driver-class-name: org.h2.Driver
    username: sa
    password:
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: none  # Flyway handles schema
    show-sql: false
  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration
  h2:
    console:
      enabled: true
      path: /h2-console
  thymeleaf:
    cache: false

# MinIO stub — E2E tidak butuh real storage
minio:
  endpoint: http://localhost:9000
  presigned-endpoint: http://localhost:9000
  access-key: minioadmin
  secret-key: minioadmin
  buckets:
    signatures: test-signatures

logging:
  level:
    com.solusi.erp: INFO
    org.springframework.security: DEBUG  # Kritis untuk debug auth issues
    org.hibernate.SQL: OFF
```

**Keputusan Keamanan Kritis:**

| Setting | Value | Alasan Security |
|---------|-------|-----------------|
| `cookie.secure` | `false` | H2 test berjalan di HTTP localhost. Jika `true`, browser tidak mengirim cookie → login selalu gagal. Ini **HANYA** untuk profile `e2e`. |
| `cookie.http-only` | `true` | Tetap aktif! Ini memastikan JavaScript tidak bisa akses `JSESSIONID`. E2E test harus membuktikan bahwa `document.cookie` TIDAK mengandung session ID. |
| `cookie.same-site` | `lax` | Mencegah CSRF dari cross-origin, tetapi mengizinkan navigasi normal. |
| `spring.security: DEBUG` | Aktif | Memungkinkan diagnosis ketika test gagal karena auth issue. Log menunjukkan filter chain execution. |
| `session.timeout` | `30m` | Sama dengan production. Test session-timeout akan menggunakan Spring API untuk mempersingkat timeout per-test. |

#### 3.2 H2 Compatibility Notes

Flyway migration V1 menggunakan syntax MySQL (`BIGINT AUTO_INCREMENT`, `NOW()`). H2 `MODE=MySQL` mendukung ini. Namun perlu mitigasi:

```yaml
# Tambahan flag H2 yang diperlukan:
# DATABASE_TO_LOWER=TRUE    → H2 uppercase by default, MySQL lowercase
# CASE_INSENSITIVE_IDENTIFIERS=TRUE → Match MySQL behavior
# DB_CLOSE_DELAY=-1         → Keep DB alive selama JVM hidup
```

Jika ada migration yang menggunakan MySQL-specific syntax tidak didukung H2 (misalnya `FULLTEXT INDEX`, `ENGINE=InnoDB`), buat migration overlay:

```
src/main/resources/db/migration/          ← Shared migrations (V1–V45)
src/main/resources/db/migration-e2e/      ← H2-specific patches (jika perlu)
```

Di `application-e2e.yaml`:
```yaml
spring:
  flyway:
    locations: classpath:db/migration, classpath:db/migration-e2e
```

#### 3.3 Startup Command

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=e2e
```

Atau untuk CI:
```bash
./mvnw package -DskipTests
java -jar target/solusi-program-erp-*.jar --spring.profiles.active=e2e
```

---

### 4. Data Seeding Strategy

#### 4.1 E2E Data Seeder (Security Focus)

```java
// src/main/java/com/solusi/erp/config/E2eDataSeeder.java

@Component
@Profile("e2e")
@Order(Ordered.LOWEST_PRECEDENCE) // Run AFTER SystemInitializer
public class E2eDataSeeder implements CommandLineRunner {

    private final UserJpaRepository userRepository;
    private final RoleJpaRepository roleRepository;
    private final PermissionJpaRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final UserProfileJpaRepository profileRepository;
    private final PasswordEncoder passwordEncoder;

    // Constructor injection...

    @Override
    @Transactional
    public void run(String... args) {
        // ═══════════════════════════════════════
        // STEP 1: Prepare admin (from V2 + SystemInitializer)
        // Password "admin123", passwordChangeRequired=true
        // E2E tests will test the password change flow first
        // ═══════════════════════════════════════

        // ═══════════════════════════════════════
        // STEP 2: Create test roles with specific permission sets
        // ═══════════════════════════════════════
        Role readOnlyRole = createRole("ROLE_READONLY",
            "DASHBOARD_READ", "USERS_READ", "ROLES_READ", "PERMISSIONS_READ",
            "PRODUCT_READ", "BRAND_READ"
        );

        Role inventoryRole = createRole("ROLE_INVENTORY_MANAGER",
            "DASHBOARD_READ",
            "PRODUCT_READ", "PRODUCT_CREATE", "PRODUCT_UPDATE", "PRODUCT_DELETE",
            "BRAND_READ", "BRAND_CREATE", "BRAND_UPDATE",
            "PRODUCT-CATEGORY_READ", "PRODUCT-CATEGORY_CREATE",
            "UNIT-OF-MEASURE_READ",
            "LOOKUP_BRAND", "LOOKUP_PRODUCT-CATEGORY", "LOOKUP_UNIT-OF-MEASURE"
        );

        Role noPermRole = createRole("ROLE_NO_PERM"
            // Intentionally EMPTY — no permissions at all
        );

        // ═══════════════════════════════════════
        // STEP 3: Create test users (all with password already changed)
        // ═══════════════════════════════════════
        createUser("e2e_admin", "Test@2024!", "ROLE_ADMIN", false);
        // ^ Admin with password already changed. For fast login in most tests.

        createUser("e2e_readonly", "Test@2024!", readOnlyRole, false);
        createUser("e2e_inventory", "Test@2024!", inventoryRole, false);
        createUser("e2e_noperm", "Test@2024!", noPermRole, false);
        createUser("e2e_newuser", "Test@2024!", readOnlyRole, true);
        // ^ passwordChangeRequired=true for testing forced password change flow

        createUser("e2e_disabled", "Test@2024!", readOnlyRole, false);
        disableUser("e2e_disabled");
    }

    private Role createRole(String name, String... permissionNames) {
        Role role = new Role();
        role.setName(name);
        role.setDescription("E2E Test Role: " + name);
        role.setCreatedBy("E2E_SEEDER");
        role = roleRepository.save(role);

        for (String permName : permissionNames) {
            permissionRepository.findByName(permName).ifPresent(perm -> {
                // Add to role_permissions junction table
                rolePermissionRepository.save(new RolePermission(role, perm));
            });
        }
        return role;
    }

    private void createUser(String username, String password,
                            Object roleRef, boolean passwordChangeRequired) {
        Role role;
        if (roleRef instanceof String roleName) {
            role = roleRepository.findByName(roleName).orElseThrow();
        } else {
            role = (Role) roleRef;
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(username + "@e2e-test.com");
        user.setEnabled(true);
        user.setPasswordChangeRequired(passwordChangeRequired);
        user.setRole(role);
        user.setCreatedBy("E2E_SEEDER");
        if (!passwordChangeRequired) {
            user.setLastPasswordChange(LocalDateTime.now());
        }
        userRepository.save(user);

        UserProfile profile = new UserProfile();
        profile.setUser(user);
        profile.setFullName("E2E " + username);
        profile.setLanguageCode("id");
        profile.setDefaultPageSize(10);
        profile.setTheme("light");
        profile.setCreatedBy("E2E_SEEDER");
        profileRepository.save(profile);
    }

    private void disableUser(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setEnabled(false);
            userRepository.save(user);
        });
    }
}
```

#### 4.2 Test User Matrix (Security Perspective)

| Username | Role | Permissions | passwordChangeRequired | enabled | Test Purpose |
|----------|------|-------------|----------------------|---------|-------------|
| `admin` | ROLE_ADMIN | All 112 | `true` | `true` | Test mandatory password change flow (from V2 + SystemInitializer) |
| `e2e_admin` | ROLE_ADMIN | All 112 | `false` | `true` | Happy-path full-access testing |
| `e2e_readonly` | ROLE_READONLY | 6 (READ only) | `false` | `true` | Verify CREATE/UPDATE/DELETE buttons hidden + 403 on direct POST |
| `e2e_inventory` | ROLE_INVENTORY_MANAGER | ~15 (inventory module) | `false` | `true` | Cross-module access denial (can access inventory, cannot access security/accounting) |
| `e2e_noperm` | ROLE_NO_PERM | 0 | `false` | `true` | Extreme case — even dashboard should 403 |
| `e2e_newuser` | ROLE_READONLY | 6 (READ only) | `true` | `true` | Test ForcePasswordChangeFilter redirect |
| `e2e_disabled` | ROLE_READONLY | 6 (READ only) | `false` | `false` | Login must fail (account disabled) |

#### 4.3 Mengapa BUKAN SQL File untuk E2E Seeding?

Alasan menggunakan Java `CommandLineRunner` dan bukan file SQL tambahan:

1. **Password hashing** — BCrypt hash harus di-generate oleh `PasswordEncoder` bean. Hardcode hash di SQL rapuh (salt berbeda tiap run).
2. **Referensi dinamis** — Permission dan role ID bisa berbeda di H2 vs MariaDB karena auto-increment sequence. Java code menggunakan `findByName()` yang aman.
3. **Conditional execution** — `@Profile("e2e")` memastikan seeder TIDAK PERNAH jalan di production.
4. **Reusability** — Method `createUser()` dan `createRole()` bisa dipakai untuk menambah test scenario baru tanpa menulis SQL.

---

### 5. Playwright Test Architecture

#### 5.1 Directory Structure

```
solusi-program-erp/
├── e2e-tests/                          ← Subdirectory terpisah (own package.json)
│   ├── package.json
│   ├── playwright.config.ts
│   ├── tsconfig.json
│   │
│   ├── auth/                           ← storageState files (gitignored)
│   │   ├── e2e_admin.json
│   │   ├── e2e_readonly.json
│   │   ├── e2e_inventory.json
│   │   ├── e2e_noperm.json
│   │   └── e2e_newuser.json
│   │
│   ├── fixtures/                       ← Custom test fixtures
│   │   ├── auth.fixture.ts             ← Login via form, per-role page factories
│   │   ├── csrf.fixture.ts             ← CSRF token extraction helper
│   │   └── security.fixture.ts         ← Permission assertion helpers
│   │
│   ├── helpers/
│   │   ├── login.helper.ts             ← Reusable login-via-form function
│   │   ├── csrf.helper.ts              ← Extract CSRF from meta/input
│   │   └── api.helper.ts               ← Fetch wrapper with auto CSRF
│   │
│   └── tests/
│       ├── auth/                        ← Authentication tests
│       │   ├── login.spec.ts
│       │   ├── logout.spec.ts
│       │   ├── password-change.spec.ts
│       │   └── session.spec.ts
│       │
│       ├── authorization/               ← Authorization/RBAC tests
│       │   ├── admin-full-access.spec.ts
│       │   ├── readonly-restrictions.spec.ts
│       │   ├── inventory-scope.spec.ts
│       │   ├── no-permission.spec.ts
│       │   └── permission-matrix.spec.ts
│       │
│       ├── csrf/                        ← CSRF enforcement tests
│       │   ├── form-submission.spec.ts
│       │   ├── ajax-submission.spec.ts
│       │   └── htmx-requests.spec.ts
│       │
│       └── modules/                     ← Per-module security + functional
│           ├── users/
│           │   ├── users-crud.spec.ts
│           │   └── users-security.spec.ts
│           ├── roles/
│           ├── products/
│           └── ...
│
└── src/                                ← Existing Spring Boot source
```

#### 5.2 `playwright.config.ts`

```typescript
import { defineConfig, devices } from '@playwright/test';

const BASE_URL = process.env.BASE_URL || 'http://localhost:8081';

export default defineConfig({
  testDir: './tests',
  timeout: 30_000,
  expect: { timeout: 5_000 },
  fullyParallel: false,             // Sequential — shared DB state
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 1 : 0,
  workers: 1,                       // Single worker — session-based app

  reporter: [
    ['html', { open: 'never' }],
    ['junit', { outputFile: 'test-results/junit.xml' }],
  ],

  use: {
    baseURL: BASE_URL,
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
  },

  projects: [
    // ─── AUTH SETUP (runs first, generates storageState files) ───
    {
      name: 'auth-setup',
      testMatch: /auth\.setup\.ts/,
    },

    // ─── SECURITY TESTS (use pre-authenticated sessions) ───
    {
      name: 'security',
      dependencies: ['auth-setup'],
      testMatch: /\/(auth|authorization|csrf)\//,
      use: { ...devices['Desktop Chrome'] },
    },

    // ─── MODULE TESTS (use admin session by default) ───
    {
      name: 'modules',
      dependencies: ['auth-setup'],
      testMatch: /\/modules\//,
      use: {
        ...devices['Desktop Chrome'],
        storageState: 'auth/e2e_admin.json',
      },
    },
  ],

  // ─── WEB SERVER (start Spring Boot if not already running) ───
  webServer: {
    command: process.platform === 'win32'
      ? 'cd .. && mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=e2e'
      : 'cd .. && ./mvnw spring-boot:run -Dspring-boot.run.profiles=e2e',
    url: `${BASE_URL}/login`,
    reuseExistingServer: !process.env.CI,
    timeout: 120_000,               // Spring Boot startup bisa lambat
    stdout: 'pipe',
    stderr: 'pipe',
  },
});
```

#### 5.3 Auth Setup — Login via Real Form (CRITICAL)

```typescript
// e2e-tests/auth.setup.ts
import { test as setup, expect } from '@playwright/test';
import { loginViaForm } from './helpers/login.helper';

const TEST_USERS = [
  { username: 'e2e_admin', password: 'Test@2024!' },
  { username: 'e2e_readonly', password: 'Test@2024!' },
  { username: 'e2e_inventory', password: 'Test@2024!' },
  { username: 'e2e_noperm', password: 'Test@2024!' },
];

for (const user of TEST_USERS) {
  setup(`authenticate as ${user.username}`, async ({ page }) => {
    // REAL FORM LOGIN — no shortcuts, no cookie injection
    await loginViaForm(page, user.username, user.password);

    // Verify login actually succeeded
    await expect(page).toHaveURL(/\/dashboard/);

    // Save authenticated session state
    await page.context().storageState({
      path: `auth/${user.username}.json`,
    });
  });
}
```

```typescript
// e2e-tests/helpers/login.helper.ts
import { Page, expect } from '@playwright/test';

export async function loginViaForm(
  page: Page,
  username: string,
  password: string
): Promise<void> {
  await page.goto('/login');

  // Verify we're on the login page (not already authenticated)
  await expect(page.locator('form[action="/login"]')).toBeVisible();

  // Fill credentials using real form inputs
  await page.fill('input[name="username"]', username);
  await page.fill('input[name="password"]', password);

  // Submit via the real form button (NOT page.goto or API call)
  await page.click('button[type="submit"]');

  // Wait for navigation away from login page
  await page.waitForURL(url => !url.pathname.includes('/login'));
}

export async function loginAndChangePassword(
  page: Page,
  username: string,
  currentPassword: string,
  newPassword: string
): Promise<void> {
  await page.goto('/login');
  await page.fill('input[name="username"]', username);
  await page.fill('input[name="password"]', currentPassword);
  await page.click('button[type="submit"]');

  // ForcePasswordChangeFilter should redirect to /reset-password
  await expect(page).toHaveURL(/\/reset-password/);

  await page.fill('input[name="password"]', newPassword);
  await page.fill('input[name="confirmPassword"]', newPassword);
  await page.click('button[type="submit"]');

  await expect(page).toHaveURL(/\/dashboard/);
}
```

**SECURITY RATIONALE — Mengapa WAJIB login via form:**

1. **CSRF token pada login form** — Spring Security meng-generate CSRF token saat GET `/login`. Token ini dikirim sebagai hidden field saat POST `/login`. Jika kita bypass form, CSRF login tidak di-test.
2. **Session fixation protection** — Spring Security secara default mengganti session ID setelah login sukses (`changeSessionId` strategy). Inject cookie manual tidak men-trigger ini.
3. **CustomAuthenticationSuccessHandler** — Handler ini membangun menu tree dan menyimpannya di session. Bypass login berarti `session.userMenu` kosong → sidebar tidak render → test halaman lain gagal dengan alasan yang salah.
4. **`ForcePasswordChangeFilter`** — Filter ini memeriksa `securityUser.user().isPasswordChangeRequired()`. Tanpa login via form, `SecurityUser` tidak pernah di-set di `SecurityContextHolder`.

#### 5.4 CSRF Helper (CRITICAL untuk AJAX Tests)

```typescript
// e2e-tests/helpers/csrf.helper.ts
import { Page } from '@playwright/test';

export interface CsrfTokens {
  token: string;
  headerName: string;
  parameterName: string;
}

/**
 * Extracts CSRF tokens from meta tags in <head>.
 * These are rendered by Thymeleaf: fragments/head.html lines 45-47.
 */
export async function extractCsrfFromMeta(page: Page): Promise<CsrfTokens> {
  return page.evaluate(() => {
    const token = document.querySelector('meta[name="_csrf"]')
                    ?.getAttribute('content') || '';
    const headerName = document.querySelector('meta[name="_csrf_header"]')
                    ?.getAttribute('content') || 'X-CSRF-TOKEN';
    const parameterName = document.querySelector('meta[name="_csrf_parameter"]')
                    ?.getAttribute('content') || '_csrf';
    return { token, headerName, parameterName };
  });
}

/**
 * Extracts CSRF token from hidden input in a form.
 * Spring Security auto-injects this via RequestDataValueProcessor.
 */
export async function extractCsrfFromForm(
  page: Page,
  formSelector: string
): Promise<string> {
  return page.evaluate((sel) => {
    const form = document.querySelector(sel);
    const input = form?.querySelector('input[name="_csrf"]') as HTMLInputElement;
    return input?.value || '';
  }, formSelector);
}

/**
 * Extracts CSRF header value from <body hx-headers="...">.
 * master.html line 17 sets this for all HTMX requests.
 */
export async function extractCsrfFromHxHeaders(page: Page): Promise<string> {
  return page.evaluate(() => {
    const hxHeaders = document.body.getAttribute('hx-headers');
    if (!hxHeaders) return '';
    try {
      const parsed = JSON.parse(hxHeaders);
      // Key is dynamic (from ${_csrf.headerName}), typically "X-CSRF-TOKEN"
      return Object.values(parsed)[0] as string || '';
    } catch { return ''; }
  });
}
```

#### 5.5 Security Assertion Fixture

```typescript
// e2e-tests/fixtures/security.fixture.ts
import { test as base, expect, Page } from '@playwright/test';

interface SecurityAssertions {
  /** Assert user gets redirected to login page */
  expectRedirectToLogin: (page: Page, url: string) => Promise<void>;
  /** Assert 403 response on direct URL access */
  expect403OrRedirect: (page: Page, url: string) => Promise<void>;
  /** Assert button/link is NOT visible (sec:authorize enforcement) */
  expectElementHiddenByPermission: (
    page: Page, selector: string
  ) => Promise<void>;
  /** Assert AJAX request without CSRF fails with 403 */
  expectCsrfRequired: (
    page: Page, url: string, method: string
  ) => Promise<void>;
}

export const test = base.extend<{ security: SecurityAssertions }>({
  security: async ({}, use) => {
    await use({
      expectRedirectToLogin: async (page, url) => {
        await page.goto(url);
        await expect(page).toHaveURL(/\/login/);
      },

      expect403OrRedirect: async (page, url) => {
        const response = await page.goto(url);
        const status = response?.status() || 0;
        const finalUrl = page.url();
        // Spring Security either returns 403 or redirects to /error or /login
        const isBlocked = status === 403 ||
                          finalUrl.includes('/login') ||
                          finalUrl.includes('/error');
        expect(isBlocked).toBeTruthy();
      },

      expectElementHiddenByPermission: async (page, selector) => {
        const count = await page.locator(selector).count();
        expect(count).toBe(0);
      },

      expectCsrfRequired: async (page, url, method) => {
        // Send request WITHOUT CSRF token
        const status = await page.evaluate(
          async ({ url, method }) => {
            const resp = await fetch(url, {
              method,
              headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json',
                // Deliberately NO X-CSRF-TOKEN header
              },
              body: method !== 'GET' ? '{}' : undefined,
            });
            return resp.status;
          },
          { url, method }
        );
        expect(status).toBe(403);
      },
    });
  },
});
```

#### 5.6 Core Security Test Specs

##### 5.6.1 Authentication Tests

```typescript
// e2e-tests/tests/auth/login.spec.ts
import { test, expect } from '@playwright/test';

test.describe('Authentication - Login Flow', () => {
  test('should show login form on /login', async ({ page }) => {
    await page.goto('/login');
    await expect(page.locator('input[name="username"]')).toBeVisible();
    await expect(page.locator('input[name="password"]')).toBeVisible();
    await expect(page.locator('button[type="submit"]')).toBeVisible();
  });

  test('should reject invalid credentials', async ({ page }) => {
    await page.goto('/login');
    await page.fill('input[name="username"]', 'admin');
    await page.fill('input[name="password"]', 'wrongpassword');
    await page.click('button[type="submit"]');

    await expect(page).toHaveURL(/\/login\?error=true/);
    await expect(page.locator('.alert-danger')).toBeVisible();
  });

  test('should reject disabled user login', async ({ page }) => {
    await page.goto('/login');
    await page.fill('input[name="username"]', 'e2e_disabled');
    await page.fill('input[name="password"]', 'Test@2024!');
    await page.click('button[type="submit"]');

    await expect(page).toHaveURL(/\/login\?error=true/);
  });

  test('should redirect unauthenticated user to login', async ({ page }) => {
    await page.goto('/dashboard');
    await expect(page).toHaveURL(/\/login/);
  });

  test('should redirect to login for any protected URL', async ({ page }) => {
    const protectedUrls = [
      '/dashboard',
      '/security/users',
      '/security/roles',
      '/inventory/products',
      '/accounting/coa',
    ];
    for (const url of protectedUrls) {
      await page.goto(url);
      await expect(page).toHaveURL(/\/login/);
    }
  });

  test('should enforce maximum 1 session per user', async ({ browser }) => {
    // Login in first context
    const ctx1 = await browser.newContext();
    const page1 = await ctx1.newPage();
    await page1.goto('/login');
    await page1.fill('input[name="username"]', 'e2e_admin');
    await page1.fill('input[name="password"]', 'Test@2024!');
    await page1.click('button[type="submit"]');
    await expect(page1).toHaveURL(/\/dashboard/);

    // Login in second context (same user, different session)
    const ctx2 = await browser.newContext();
    const page2 = await ctx2.newPage();
    await page2.goto('/login');
    await page2.fill('input[name="username"]', 'e2e_admin');
    await page2.fill('input[name="password"]', 'Test@2024!');
    await page2.click('button[type="submit"]');
    await expect(page2).toHaveURL(/\/dashboard/);

    // First session should be invalidated (maxSessionsPreventsLogin=false)
    await page1.reload();
    await expect(page1).toHaveURL(/\/login/);

    await ctx1.close();
    await ctx2.close();
  });
});
```

##### 5.6.2 Password Change Flow Tests

```typescript
// e2e-tests/tests/auth/password-change.spec.ts
import { test, expect } from '@playwright/test';

test.describe('ForcePasswordChangeFilter', () => {
  test('should redirect to /reset-password when passwordChangeRequired=true',
    async ({ page }) => {
    // e2e_newuser has passwordChangeRequired=true
    await page.goto('/login');
    await page.fill('input[name="username"]', 'e2e_newuser');
    await page.fill('input[name="password"]', 'Test@2024!');
    await page.click('button[type="submit"]');

    // Should be redirected to reset-password, NOT dashboard
    await expect(page).toHaveURL(/\/reset-password/);
  });

  test('should block navigation to ANY page until password is changed',
    async ({ page }) => {
    await page.goto('/login');
    await page.fill('input[name="username"]', 'e2e_newuser');
    await page.fill('input[name="password"]', 'Test@2024!');
    await page.click('button[type="submit"]');
    await expect(page).toHaveURL(/\/reset-password/);

    // Try to navigate to dashboard directly
    await page.goto('/dashboard');
    // ForcePasswordChangeFilter should redirect back to /reset-password
    await expect(page).toHaveURL(/\/reset-password/);

    // Try security module
    await page.goto('/security/users');
    await expect(page).toHaveURL(/\/reset-password/);
  });

  test('should require matching password confirmation', async ({ page }) => {
    await page.goto('/login');
    await page.fill('input[name="username"]', 'e2e_newuser');
    await page.fill('input[name="password"]', 'Test@2024!');
    await page.click('button[type="submit"]');
    await expect(page).toHaveURL(/\/reset-password/);

    // Submit with mismatched passwords
    await page.fill('input[name="password"]', 'NewPass@2024!');
    await page.fill('input[name="confirmPassword"]', 'DifferentPass@2024!');
    await page.click('button[type="submit"]');

    // Should stay on reset-password with error
    await expect(page).toHaveURL(/\/reset-password/);
    await expect(page.locator('.alert-danger, .invalid-feedback')).toBeVisible();
  });

  test('should allow dashboard access after password change', async ({ page }) => {
    await page.goto('/login');
    await page.fill('input[name="username"]', 'e2e_newuser');
    await page.fill('input[name="password"]', 'Test@2024!');
    await page.click('button[type="submit"]');
    await expect(page).toHaveURL(/\/reset-password/);

    // Change password
    await page.fill('input[name="password"]', 'Changed@2024!');
    await page.fill('input[name="confirmPassword"]', 'Changed@2024!');
    await page.click('button[type="submit"]');

    // Should redirect to dashboard after successful password change
    await expect(page).toHaveURL(/\/dashboard/);
  });
});
```

##### 5.6.3 Authorization / Permission Matrix Tests

```typescript
// e2e-tests/tests/authorization/readonly-restrictions.spec.ts
import { test, expect } from '@playwright/test';

test.describe('ROLE_READONLY — Read-Only Access Enforcement', () => {
  test.use({ storageState: 'auth/e2e_readonly.json' });

  test('should access dashboard', async ({ page }) => {
    await page.goto('/dashboard');
    await expect(page).toHaveURL(/\/dashboard/);
  });

  test('should access user list (USERS_READ)', async ({ page }) => {
    await page.goto('/security/users');
    await expect(page.locator('table, .page-body')).toBeVisible();
  });

  test('should NOT see create button on user list', async ({ page }) => {
    await page.goto('/security/users');
    // sec:authorize="hasAuthority('USERS_CREATE')" hides this
    const createBtn = page.locator('a[href*="/users/create"]');
    await expect(createBtn).toHaveCount(0);
  });

  test('should NOT see edit/delete actions on user list', async ({ page }) => {
    await page.goto('/security/users');
    const editBtn = page.locator('[sec\\:authorize*="USERS_UPDATE"], a[href*="/edit"]');
    const deleteBtn = page.locator('[sec\\:authorize*="USERS_DELETE"], button:has-text("Delete")');
    // Template sec:authorize should prevent rendering
    await expect(editBtn).toHaveCount(0);
    await expect(deleteBtn).toHaveCount(0);
  });

  test('should get 403 on direct access to create form', async ({ page }) => {
    const response = await page.goto('/security/users/create');
    const status = response?.status() || 0;
    // @PreAuthorize("hasAuthority('USERS_CREATE')") blocks this
    expect(status === 403 || page.url().includes('/error')).toBeTruthy();
  });

  test('should get 403 on POST to create endpoint via fetch', async ({ page }) => {
    await page.goto('/security/users'); // Get valid CSRF token
    const csrfToken = await page.evaluate(() =>
      document.querySelector('meta[name="_csrf"]')?.getAttribute('content') || ''
    );

    const status = await page.evaluate(
      async ({ csrf }) => {
        const resp = await fetch('/security/users/create', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'X-CSRF-TOKEN': csrf,
          },
          body: JSON.stringify({ username: 'hacker', email: 'h@h.com' }),
        });
        return resp.status;
      },
      { csrf: csrfToken }
    );
    expect(status).toBe(403);
  });
});
```

```typescript
// e2e-tests/tests/authorization/inventory-scope.spec.ts
import { test, expect } from '@playwright/test';

test.describe('ROLE_INVENTORY_MANAGER — Module Scope Enforcement', () => {
  test.use({ storageState: 'auth/e2e_inventory.json' });

  // ALLOWED: Inventory module
  test('should access product list', async ({ page }) => {
    await page.goto('/inventory/products');
    await expect(page.locator('table, .page-body')).toBeVisible();
  });

  test('should see create button on product list (has PRODUCT_CREATE)',
    async ({ page }) => {
    await page.goto('/inventory/products');
    const createBtn = page.locator('a[href*="/products/create"]');
    await expect(createBtn).toBeVisible();
  });

  // DENIED: Security module
  test('should get 403 on user management', async ({ page }) => {
    const response = await page.goto('/security/users');
    const status = response?.status() || 0;
    expect(status === 403 || page.url().includes('/error')).toBeTruthy();
  });

  // DENIED: Accounting module
  test('should get 403 on COA management', async ({ page }) => {
    const response = await page.goto('/accounting/coa');
    const status = response?.status() || 0;
    expect(status === 403 || page.url().includes('/error')).toBeTruthy();
  });

  // DENIED: Delete products (has PRODUCT_CREATE/UPDATE but NOT DELETE)
  test('should NOT see delete button on product list', async ({ page }) => {
    await page.goto('/inventory/products');
    // NOTE: Adjust selector based on actual template
    const deleteBtn = page.locator('button:has-text("Delete"), button:has-text("Hapus")');
    // Brand delete button should not appear (has BRAND_READ/CREATE/UPDATE but NOT BRAND_DELETE)
  });
});
```

```typescript
// e2e-tests/tests/authorization/no-permission.spec.ts
import { test, expect } from '@playwright/test';

test.describe('ROLE_NO_PERM — Zero Permission User', () => {
  test.use({ storageState: 'auth/e2e_noperm.json' });

  test('should be blocked from dashboard', async ({ page }) => {
    const response = await page.goto('/dashboard');
    const status = response?.status() || 0;
    // DASHBOARD_READ not granted → 403
    expect(status === 403 || page.url().includes('/error')).toBeTruthy();
  });

  test('should be blocked from every module', async ({ page }) => {
    const urls = [
      '/security/users', '/security/roles',
      '/inventory/products', '/inventory/brands',
      '/accounting/coa', '/master/parties',
    ];
    for (const url of urls) {
      const response = await page.goto(url);
      const status = response?.status() || 0;
      expect(
        status === 403 || page.url().includes('/error'),
        `Expected 403 for ${url}, got ${status}`
      ).toBeTruthy();
    }
  });

  test('sidebar should be empty (no menu items)', async ({ page }) => {
    // Navigate to any accessible page (the app will still render layout)
    // Even if all pages 403, the error page might have a sidebar
    // Verify no menu items are rendered
    await page.goto('/dashboard');
    // If redirected to error page with sidebar, verify it's empty
    const menuItems = page.locator('.nav-item .nav-link[href]:not([href="#"])');
    const count = await menuItems.count();
    // Should have 0 or only static items (no dynamic permission-based items)
    expect(count).toBeLessThanOrEqual(0);
  });
});
```

##### 5.6.4 CSRF Enforcement Tests

```typescript
// e2e-tests/tests/csrf/ajax-submission.spec.ts
import { test, expect } from '@playwright/test';
import { extractCsrfFromMeta } from '../../helpers/csrf.helper';

test.describe('CSRF Token Enforcement', () => {
  test.use({ storageState: 'auth/e2e_admin.json' });

  test('CSRF meta tags present on authenticated pages', async ({ page }) => {
    await page.goto('/dashboard');
    const csrf = await extractCsrfFromMeta(page);

    expect(csrf.token).toBeTruthy();
    expect(csrf.token.length).toBeGreaterThan(20);
    expect(csrf.headerName).toBe('X-CSRF-TOKEN');
    expect(csrf.parameterName).toBe('_csrf');
  });

  test('CSRF token present in hx-headers on body', async ({ page }) => {
    await page.goto('/dashboard');
    const hxHeaders = await page.getAttribute('body', 'hx-headers');
    expect(hxHeaders).toBeTruthy();

    const parsed = JSON.parse(hxHeaders!);
    const token = Object.values(parsed)[0] as string;
    expect(token).toBeTruthy();
    expect(token.length).toBeGreaterThan(20);
  });

  test('AJAX POST without CSRF token returns 403', async ({ page }) => {
    await page.goto('/security/users');

    const status = await page.evaluate(async () => {
      const resp = await fetch('/security/users/create', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          // Deliberately NO X-CSRF-TOKEN
        },
        body: JSON.stringify({}),
      });
      return resp.status;
    });

    expect(status).toBe(403);
  });

  test('AJAX POST with valid CSRF token succeeds (or 400 validation)',
    async ({ page }) => {
    await page.goto('/security/users');
    const csrf = await extractCsrfFromMeta(page);

    const status = await page.evaluate(
      async ({ token, headerName }) => {
        const resp = await fetch('/security/users/create', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            [headerName]: token,
          },
          body: JSON.stringify({}),
        });
        return resp.status;
      },
      { token: csrf.token, headerName: csrf.headerName }
    );

    // With valid CSRF, server processes request.
    // Empty body → 400 (validation error), NOT 403
    expect(status).not.toBe(403);
    expect([400, 201]).toContain(status);
  });

  test('HTMX DELETE with CSRF should work', async ({ page }) => {
    await page.goto('/security/users');
    // hx-headers on <body> automatically injects CSRF for HTMX
    // This test verifies that htmx respects the body hx-headers attribute
    const hxHeaders = await page.getAttribute('body', 'hx-headers');
    expect(hxHeaders).toBeTruthy();
    expect(hxHeaders).toContain('X-CSRF-TOKEN');
  });
});
```

##### 5.6.5 Session Security Tests

```typescript
// e2e-tests/tests/auth/session.spec.ts
import { test, expect } from '@playwright/test';

test.describe('Session Security', () => {
  test('JSESSIONID cookie should be httpOnly', async ({ page }) => {
    await page.goto('/login');
    await page.fill('input[name="username"]', 'e2e_admin');
    await page.fill('input[name="password"]', 'Test@2024!');
    await page.click('button[type="submit"]');
    await expect(page).toHaveURL(/\/dashboard/);

    // httpOnly cookies are NOT accessible via document.cookie
    const visibleCookies = await page.evaluate(() => document.cookie);
    expect(visibleCookies).not.toContain('JSESSIONID');
  });

  test('logout should invalidate session and redirect to login', async ({ page }) => {
    await page.goto('/login');
    await page.fill('input[name="username"]', 'e2e_admin');
    await page.fill('input[name="password"]', 'Test@2024!');
    await page.click('button[type="submit"]');
    await expect(page).toHaveURL(/\/dashboard/);

    // Perform logout
    await page.goto('/logout');
    await expect(page).toHaveURL(/\/login\?logout=true/);

    // Try to access protected page — should redirect to login
    await page.goto('/dashboard');
    await expect(page).toHaveURL(/\/login/);
  });

  test('logout success message displayed', async ({ page }) => {
    await page.goto('/login');
    await page.fill('input[name="username"]', 'e2e_admin');
    await page.fill('input[name="password"]', 'Test@2024!');
    await page.click('button[type="submit"]');
    await page.goto('/logout');

    await expect(page).toHaveURL(/\/login\?logout=true/);
    await expect(page.locator('.alert-success')).toBeVisible();
  });
});
```

##### 5.6.6 Permission Matrix (Data-Driven)

```typescript
// e2e-tests/tests/authorization/permission-matrix.spec.ts
import { test, expect } from '@playwright/test';

/**
 * Data-driven permission matrix test.
 * Exhaustively verifies which role can access which URL.
 */
interface PermissionEntry {
  url: string;
  method: 'GET' | 'POST';
  admin: boolean;    // e2e_admin expected result
  readonly: boolean; // e2e_readonly expected result
  inventory: boolean;// e2e_inventory expected result
}

const MATRIX: PermissionEntry[] = [
  // URL                          Method  Admin  ReadOnly  Inventory
  { url: '/dashboard',            method: 'GET',  admin: true,  readonly: true,  inventory: true  },
  { url: '/security/users',       method: 'GET',  admin: true,  readonly: true,  inventory: false },
  { url: '/security/users/create',method: 'GET',  admin: true,  readonly: false, inventory: false },
  { url: '/security/roles',       method: 'GET',  admin: true,  readonly: true,  inventory: false },
  { url: '/inventory/products',   method: 'GET',  admin: true,  readonly: true,  inventory: true  },
  { url: '/inventory/brands',     method: 'GET',  admin: true,  readonly: true,  inventory: true  },
  { url: '/accounting/coa',       method: 'GET',  admin: true,  readonly: false, inventory: false },
  { url: '/accounting/periods',   method: 'GET',  admin: true,  readonly: false, inventory: false },
];

const ROLES = [
  { name: 'admin',     storageState: 'auth/e2e_admin.json',     key: 'admin'     },
  { name: 'readonly',  storageState: 'auth/e2e_readonly.json',  key: 'readonly'  },
  { name: 'inventory', storageState: 'auth/e2e_inventory.json', key: 'inventory' },
] as const;

for (const role of ROLES) {
  test.describe(`Permission Matrix — ${role.name}`, () => {
    test.use({ storageState: role.storageState });

    for (const entry of MATRIX) {
      const expected = entry[role.key as keyof PermissionEntry] as boolean;
      const label = expected ? '✓ allowed' : '✗ denied';

      test(`${entry.method} ${entry.url} → ${label}`, async ({ page }) => {
        const response = await page.goto(entry.url);
        const status = response?.status() || 0;

        if (expected) {
          expect(status).toBeLessThan(400);
        } else {
          expect(
            status === 403 || page.url().includes('/error'),
            `Expected 403 for ${role.name} on ${entry.url}, got ${status}`
          ).toBeTruthy();
        }
      });
    }
  });
}
```

---

### 6. CI/CD Integration

#### 6.1 GitHub Actions Workflow

```yaml
# .github/workflows/e2e-security.yml
name: E2E Security Tests

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

jobs:
  e2e-security:
    runs-on: ubuntu-latest
    timeout-minutes: 20

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

      - name: Build Spring Boot (skip tests)
        run: ./mvnw package -DskipTests -q

      - name: Start Spring Boot (E2E profile)
        run: |
          java -jar target/solusi-program-erp-*.jar \
            --spring.profiles.active=e2e &
          # Wait for app to be ready
          timeout 90 bash -c 'until curl -sf http://localhost:8081/login; do sleep 2; done'

      - name: Install Playwright
        working-directory: e2e-tests
        run: |
          npm ci
          npx playwright install --with-deps chromium

      - name: Run E2E Security Tests
        working-directory: e2e-tests
        env:
          BASE_URL: http://localhost:8081
          CI: true
        run: npx playwright test

      - name: Upload Test Report
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: playwright-report
          path: e2e-tests/playwright-report/
          retention-days: 14

      - name: Upload Test Results (JUnit)
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: test-results
          path: e2e-tests/test-results/
          retention-days: 14
```

#### 6.2 Pipeline Security Safeguards

| Concern | Mitigation |
|---------|-----------|
| **E2E seeder jalan di production** | `@Profile("e2e")` + CI hanya menjalankan dengan `--spring.profiles.active=e2e` |
| **Test password di-commit** | Password `Test@2024!` hanya ada di `E2eDataSeeder.java` (profile-guarded) dan test helper files. TIDAK dipakai di environment lain. |
| **H2 console terbuka** | H2 console hanya aktif di profile `e2e`. Production menggunakan MariaDB tanpa H2 dependency. |
| **storageState file mengandung session** | `.gitignore` entry: `e2e-tests/auth/*.json`. CI generates fresh setiap run. |
| **Sensitive data di test report** | Playwright trace/screenshot mungkin menampilkan data. Artifact retention 14 hari, akses terbatas ke repo collaborators. |

---

### 7. Cakupan Test Minimum per Modul

#### 7.1 Authentication Module (WAJIB — Prioritas 1)

| # | Test Case | Security Aspect |
|---|-----------|----------------|
| 1 | Login sukses dengan kredensial valid | Auth chain end-to-end |
| 2 | Login gagal — password salah | Brute force visibility |
| 3 | Login gagal — user disabled | `isEnabled()` enforcement |
| 4 | Login gagal — username tidak ada | Error message tidak leak info |
| 5 | Redirect ke login untuk URL protected | `anyRequest().authenticated()` |
| 6 | Redirect ke login untuk 5+ URL berbeda | URL pattern coverage |
| 7 | Concurrent session enforcement (max 1) | `maximumSessions(1)` |
| 8 | Logout invalidates session | `invalidateHttpSession(true)` |
| 9 | Logout deletes JSESSIONID cookie | `deleteCookies("JSESSIONID")` |
| 10 | Post-logout access redirects to login | Session termination |

#### 7.2 Password Change Module (WAJIB — Prioritas 1)

| # | Test Case | Security Aspect |
|---|-----------|----------------|
| 1 | User baru redirect ke `/reset-password` | `ForcePasswordChangeFilter` |
| 2 | Navigasi ke URL lain tetap redirect ke reset | Filter bypass prevention |
| 3 | Password mismatch ditolak | Input validation |
| 4 | Password terlalu pendek ditolak | Minimum length enforcement |
| 5 | Password change sukses → dashboard | Flow completion |
| 6 | Setelah change, `passwordChangeRequired=false` | State persistence |

#### 7.3 Authorization/RBAC Module (WAJIB — Prioritas 1)

| # | Test Case | Security Aspect |
|---|-----------|----------------|
| 1 | Admin sees all CRUD buttons | `sec:authorize` template rendering |
| 2 | ReadOnly sees NO create/edit/delete buttons | `sec:authorize` negative case |
| 3 | ReadOnly gets 403 on direct POST to create | `@PreAuthorize` server enforcement |
| 4 | ReadOnly gets 403 on direct DELETE | `@PreAuthorize` for DELETE |
| 5 | Inventory user accesses inventory module | Cross-module positive |
| 6 | Inventory user blocked from security module | Cross-module negative |
| 7 | Inventory user blocked from accounting | Cross-module negative |
| 8 | No-permission user blocked from dashboard | Zero-permission edge case |
| 9 | No-permission user blocked from all modules | Comprehensive denial |
| 10 | Sidebar menus match user permissions | `session.userMenu` filtering |
| 11 | Permission matrix (data-driven, 24+ URLs) | Systematic RBAC verification |

#### 7.4 CSRF Module (WAJIB — Prioritas 1)

| # | Test Case | Security Aspect |
|---|-----------|----------------|
| 1 | CSRF meta tags present on all authenticated pages | Token availability |
| 2 | CSRF token in `<body hx-headers>` for HTMX | HTMX integration |
| 3 | AJAX POST without CSRF → 403 | Server enforcement |
| 4 | AJAX POST with valid CSRF → processed | Positive validation |
| 5 | AJAX DELETE without CSRF → 403 | Method-specific enforcement |
| 6 | Form submission includes CSRF hidden input | RequestDataValueProcessor |
| 7 | ErpFormHandler sends X-CSRF-TOKEN header | JS integration |

#### 7.5 Session Security Module (WAJIB — Prioritas 1)

| # | Test Case | Security Aspect |
|---|-----------|----------------|
| 1 | JSESSIONID not accessible via `document.cookie` | `httpOnly=true` |
| 2 | Logout invalidates session completely | Session destruction |
| 3 | Post-logout, old session cookie rejected | Session fixation prevention |
| 4 | Concurrent login replaces old session | `maxSessionsPreventsLogin(false)` |

#### 7.6 Per-Module Functional + Security (Prioritas 2 — per module)

Setiap modul bisnis (Users, Roles, Products, Brands, COA, dll.) memiliki minimum:

| # | Test Case | Security Aspect |
|---|-----------|----------------|
| 1 | List page renders with data | Authenticated access |
| 2 | Create form renders (if authorized) | Permission-gated form |
| 3 | Create submission succeeds with CSRF | CSRF + permission |
| 4 | Edit form renders with existing data | Permission + data isolation |
| 5 | Delete confirmation and execution | Permission + CSRF |
| 6 | Unauthorized role gets 403 | Permission negative case |

**Total minimum test cases: ~80 (security-focused) + ~30 per business module**

---

### 8. Risiko & Mitigasi

| # | Risiko | Severity | Mitigasi |
|---|--------|----------|----------|
| 1 | **H2 incompatibility dengan MySQL syntax di migration V3–V45** | HIGH | Jalankan Flyway migration di H2 sebagai smoke test pertama. Buat `migration-e2e/` overlay untuk patch syntax yang gagal (contoh: `FULLTEXT INDEX` → diabaikan, `ENGINE=InnoDB` → dihapus). Tracking table: setiap migration yang perlu patch di-document. |
| 2 | **`cookie.secure=false` di-deploy ke production secara tidak sengaja** | CRITICAL | Setting ini di `application-e2e.yaml` SAJA. Default `application.yaml` tetap `secure: true`. CI environment TIDAK pernah merge profile e2e ke production deployment. Tambahkan startup warning log jika `cookie.secure=false` terdeteksi. |
| 3 | **E2E seeder menciptakan user `e2e_admin` di production** | CRITICAL | `@Profile("e2e")` memastikan class TIDAK di-load di profile lain. Tambahkan integration test: jika `spring.profiles.active` TIDAK mengandung `e2e`, assert bahwa `E2eDataSeeder` bean TIDAK ada di ApplicationContext. |
| 4 | **Test flaky karena timing (HTMX partial update, TomSelect load)** | MEDIUM | Gunakan `page.waitForResponse()` untuk HTMX requests. Gunakan `page.locator().waitFor()` untuk elemen dinamis. JANGAN gunakan `page.waitForTimeout()` (fixed delay). |
| 5 | **Session timeout test membutuhkan 30 menit** | LOW | JANGAN test real timeout. Test session invalidation via logout. Jika timeout test diperlukan, buat `@Profile("e2e")` bean yang override session timeout ke 5 detik untuk test spesifik (via custom endpoint `/e2e/session-config`). |
| 6 | **MinIO dependency gagal di CI** | MEDIUM | E2E profile stub MinIO config. Untuk modul yang butuh file upload (approval signatures), mock MinIO dengan testcontainers atau skip test tersebut. Mayoritas security test tidak butuh file storage. |
| 7 | **Password `Test@2024!` terekspos di repository** | LOW | Password ini hanya untuk E2E test users di database H2 in-memory. Database tidak persist. Sama seperti `admin123` yang sudah ada di `SystemInitializer.java`. Risiko diterima. |
| 8 | **CSRF token rotasi membuat storageState stale** | MEDIUM | `storageState` menyimpan cookies (session), bukan CSRF token. CSRF token di-extract fresh dari `<meta>` tags pada setiap page load. Ini sudah benar by design. |

---

### 9. Effort Estimate

| Phase | Task | Person-Days | Keterangan |
|-------|------|-------------|------------|
| **1. Infrastructure** | `application-e2e.yaml` + H2 compatibility | 2 | Fix Flyway migrations untuk H2 |
| | `E2eDataSeeder.java` | 1 | Multi-role user seeding |
| | Playwright setup (`package.json`, config) | 1 | Subdirectory, dependencies |
| | Auth helper + CSRF helper | 1 | Login via form, token extraction |
| | **Subtotal** | **5** | |
| **2. Security Tests** | Authentication tests (10 cases) | 2 | Login/logout/session |
| | Password change tests (6 cases) | 1 | ForcePasswordChangeFilter |
| | Authorization tests (11+ cases) | 3 | RBAC per role |
| | CSRF tests (7 cases) | 1.5 | AJAX + HTMX + form |
| | Session security tests (4 cases) | 1 | Cookie, invalidation |
| | Permission matrix (data-driven) | 1.5 | 24+ URL combinations |
| | **Subtotal** | **10** | |
| **3. CI/CD** | GitHub Actions workflow | 1 | Build + start + test + artifacts |
| | Pipeline hardening & caching | 0.5 | Maven cache, Playwright cache |
| | **Subtotal** | **1.5** | |
| **4. Module Tests** | Users CRUD + security (pilot) | 2 | First full module |
| | Roles CRUD + security | 1.5 | Second module |
| | Products + Brands (pattern established) | 2 | Template for remaining |
| | **Subtotal** | **5.5** | |
| | | | |
| **TOTAL** | | **22 person-days** | ~4.5 minggu (1 engineer) |

**Rekomendasi phasing:**
- **Sprint 1 (Minggu 1–2):** Phase 1 + Phase 2 → Security test suite berjalan di local
- **Sprint 2 (Minggu 3):** Phase 3 → CI/CD pipeline green
- **Sprint 3 (Minggu 4–5):** Phase 4 → Pilot module tests, template untuk tim

---

### 10. Trade-offs

| Keputusan | Dipilih | Alternatif yang Ditolak | Alasan |
|-----------|---------|------------------------|--------|
| **Login via real form** | ✅ Ya — setiap test login via POST `/login` | Inject session cookie langsung | Real form login menguji CSRF pada login, `CustomAuthenticationSuccessHandler` (menu building, locale sync), dan `ForcePasswordChangeFilter`. Cookie injection melewati semua ini → security bypass yang berbahaya untuk test suite. |
| **`storageState` caching** | ✅ Ya — login sekali per role di setup, reuse session | Login ulang di setiap test | Setup login 5 user memakan ~10 detik. Tanpa caching, 80+ test × 3 detik login = 4+ menit overhead sia-sia. `storageState` menyimpan cookie yang didapat dari real login — ini BUKAN bypass. |
| **H2 in-memory** (bukan TestContainers MariaDB) | ✅ H2 MODE=MySQL | TestContainers + real MariaDB | H2 lebih cepat startup (~3 detik vs ~15 detik), tidak butuh Docker di CI runner, cukup untuk E2E yang fokus UI+security. Trade-off: mungkin ada SQL incompatibility yang perlu patch. Untuk database-heavy test, TestContainers bisa ditambahkan nanti. |
| **Single worker** | ✅ workers: 1 | Parallel workers | Session-based app dengan shared database. Parallel workers menyebabkan race condition pada session management test dan data mutation. Trade-off: test suite lebih lambat (~3-5 menit vs ~1-2 menit), tapi hasilnya deterministic. |
| **Subdirectory terpisah** (bukan monorepo plugin) | ✅ `e2e-tests/` directory | Embed di Maven test phase | Decoupling Node.js toolchain dari Maven build. Tim frontend bisa menjalankan dan develop test tanpa Maven expertise. Maven build time tidak terpengaruh. Trade-off: dua step di CI (build Java → run Playwright). |
| **No auth bypass endpoint** | ✅ Tidak ada `/e2e/login` shortcut | Custom endpoint untuk fast login | Setiap shortcut adalah attack surface. Meskipun di-guard dengan `@Profile("e2e")`, keberadaan bypass endpoint menciptakan false confidence — test "lulus" tapi security chain tidak diuji. |
| **`cookie.secure=false` di E2E** | ✅ Ya — untuk HTTP localhost | Gunakan self-signed HTTPS | HTTPS setup di CI menambah kompleksitas (cert generation, trust store). `secure=false` di-scope ketat ke profile `e2e`. Trade-off: kita tidak menguji bahwa production benar-benar enforce secure cookie. Mitigasi: audit `application.yaml` secara terpisah. |
| **CSRF tetap aktif di E2E** | ✅ Ya — CSRF enforcement aktif | Disable CSRF untuk test simplicity | CSRF adalah bagian integral security. Mematikannya di test berarti kita tidak menguji path kritis: AJAX form submission, HTMX request, `ErpFormHandler.js` token extraction. Kode test sedikit lebih kompleks (harus extract token), tapi coverage jauh lebih baik. |
| **Data-driven permission matrix** | ✅ Ya — tabel URL×Role | Test individual per skenario | Satu perubahan di security config → satu baris di matrix table. Lebih maintainable daripada 50+ test function terpisah. Trade-off: error message kurang deskriptif saat gagal (generic "Expected 403 for readonly on /url"). Mitigasi: custom error message di assertion. |
| **Tidak men-test session timeout real** | ✅ Tidak test 30-menit timeout | Sleep 30 menit di test | Impractical. Session timeout adalah konfigurasi server, bukan logic yang butuh E2E test. Diverifikasi via: (1) audit `application.yaml`, (2) test logout + session invalidation, (3) opsional: endpoint yang set timeout pendek untuk test spesifik. |
