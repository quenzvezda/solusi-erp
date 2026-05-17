# Implementation Plan: E2E Test Coverage Expansion (CRUD Modules)

> Source: conversation (user request to expand from PoC)
> Created: 2026-05-17
> Status: PENDING

## Summary

Expand Playwright E2E test coverage from login-only to 4 CRUD modules: UoM (simplest), Product Category, Brand, then Product (mid-complexity with TomSelect autocomplete + AutoNumeric). Includes building reusable helper library for TomSelect and AutoNumeric interactions, plus seed data expansion.

## Tasks

### Task 1: Expand V9000 Seed Data for CRUD Tests
Add master data (UoM, Category, Brand) to V9000 so Product create test has lookup data available.

**Depends on:** (none)
**Reference:** src/main/resources/db/migration-h2/V9000__e2e_seed_data.sql

Steps:
- [ ] Read existing V2 seed and V9000 to understand current data state
- [ ] Add seed data to V9000:
      - 2 UoMs: `E2E-PCS` (Piece, type UNIT), `E2E-KG` (Kilogram, type WEIGHT)
      - 2 Product Categories: `E2E-CAT-STOCK` (type STOCK), `E2E-CAT-SVC` (type SERVICE)
      - 2 Brands: `E2E-BRAND-A`, `E2E-BRAND-B`
      - Use ID range 9001+ to avoid conflicts
- [ ] Verify app starts with new seed: `mvnw spring-boot:run -Pe2e -Dspring-boot.run.profiles=e2e`

**Validation criteria:**
- App starts without Flyway error
- Seed data accessible via UI (visible in list pages after login)

---

### Task 2: Auth Helper & Test Fixture
Create reusable login helper and Playwright fixture so all CRUD tests share authenticated state.

**Depends on:** (none)
**Reference:** e2e-tests/tests/auth/login.spec.ts, docs/tests/playwright-smoke-test-guide.md Section 3

Steps:
- [ ] Create `e2e-tests/helpers/auth.ts`:
      - `login(page, username, password)` function
      - `TEST_USERS` constant with admin credentials
- [ ] Create `e2e-tests/fixtures/base.ts`:
      - Extended test fixture that auto-logs in before each test
      - Exports `test` and `expect` with auth pre-applied
- [ ] Create `e2e-tests/helpers/navigation.ts`:
      - `navigateToModule(page, url)` — goto + wait for page load
      - `waitForPageReady(page)` — wait for HTMX idle + no spinners
- [ ] Update login.spec.ts to use the new helper (verify no regression)

**Validation criteria:**
- `npx playwright test tests/auth/login.spec.ts` still passes
- Helper functions exported and importable

---

### Task 3: AJAX Form Helper
Create helper for AJAX JSON form submission pattern used by all 4 modules.

**Depends on:** Task 2
**Reference:** docs/tests/playwright-smoke-test-guide.md Section 4.7

Steps:
- [ ] Create `e2e-tests/helpers/form.ts`:
      - `submitAjaxForm(page)` — click submit, wait for AJAX response, verify success toast/redirect
      - `fillField(page, name, value)` — fill input by name attribute
      - `selectDropdown(page, name, value)` — select standard `<select>` by name
      - `expectFormError(page, fieldName?)` — assert validation error visible
      - `expectSuccessRedirect(page, urlPattern)` — assert redirect after save
- [ ] Create `e2e-tests/helpers/waits.ts`:
      - `waitForHtmx(page)` — wait for `.htmx-request` to disappear
      - `waitForToast(page, text?)` — wait for success/error toast
      - `waitForNetworkIdle(page)` — wait for network settle

**Validation criteria:**
- Helpers compile without TypeScript errors
- Functions are generic enough for all 4 modules

---

### Task 4: UoM CRUD Test Spec
Simplest CRUD — plain form with text inputs + select dropdown, no TomSelect.

**Depends on:** Task 2, Task 3
**Reference:** UoM Controller URLs: `/inventory/unit-of-measures`

Steps:
- [ ] Create `e2e-tests/tests/master-data/uom.spec.ts`
- [ ] Test: "should display UoM list page"
      - Navigate to `/inventory/unit-of-measures`
      - Assert: page title or heading contains "Unit of Measure"
      - Assert: table is visible
- [ ] Test: "should create new UoM"
      - Navigate to `/inventory/unit-of-measures/create`
      - Fill `name` with unique value (e.g., `E2E-UoM-{timestamp}`)
      - Select `type` = "UNIT"
      - Submit form (AJAX)
      - Assert: redirected to list page
      - Assert: new UoM visible in list (search for it)
- [ ] Test: "should edit existing UoM"
      - Navigate to list, click edit on seeded `E2E-PCS`
      - Change `name` to `E2E-PCS-Edited`
      - Submit form
      - Assert: change reflected in list
      - Revert name back (or use unique name per run)
- [ ] Test: "should show validation error for empty name"
      - Navigate to create
      - Leave `name` empty, submit
      - Assert: validation error visible

**Validation criteria:**
- `npx playwright test tests/master-data/uom.spec.ts` — all pass
- Tests are independent (can run in any order)

---

### Task 5: Product Category CRUD Test Spec
Similar to UoM — plain form with text + select + textarea.

**Depends on:** Task 2, Task 3
**Reference:** Product Category Controller URLs: `/inventory/product-categories`

Steps:
- [ ] Create `e2e-tests/tests/master-data/product-category.spec.ts`
- [ ] Test: "should display category list page"
      - Navigate to `/inventory/product-categories`
      - Assert: table visible with data
- [ ] Test: "should create new category"
      - Navigate to create
      - Fill `name` with unique value
      - Select `type` = "STOCK"
      - Optionally fill `note`
      - Submit (AJAX)
      - Assert: redirected to list, new category visible
- [ ] Test: "should edit existing category"
      - Navigate to edit seeded `E2E-CAT-STOCK`
      - Change name
      - Submit, verify change
- [ ] Test: "should show validation error for empty name"
      - Submit empty form
      - Assert: error visible

**Validation criteria:**
- `npx playwright test tests/master-data/product-category.spec.ts` — all pass

---

### Task 6: Brand CRUD Test Spec
Simplest of all — just code (auto), name, note.

**Depends on:** Task 2, Task 3
**Reference:** Brand Controller URLs: `/inventory/brands`

Steps:
- [ ] Create `e2e-tests/tests/master-data/brand.spec.ts`
- [ ] Test: "should display brand list page"
      - Navigate to `/inventory/brands`
      - Assert: table visible
- [ ] Test: "should create new brand"
      - Navigate to create
      - Fill `name` with unique value
      - Optionally fill `note`
      - Submit (AJAX)
      - Assert: redirected to list, new brand visible
- [ ] Test: "should edit existing brand"
      - Edit seeded `E2E-BRAND-A`
      - Change name, submit, verify
- [ ] Test: "should show validation error for empty name"
      - Submit empty, assert error

**Validation criteria:**
- `npx playwright test tests/master-data/brand.spec.ts` — all pass

---

### Task 7: TomSelect Helper
Build reusable helper for TomSelect autocomplete interaction — critical for Product test.

**Depends on:** Task 2
**Reference:** docs/tests/playwright-smoke-test-guide.md Section 4.1 (Node.js technique)

Steps:
- [ ] Create `e2e-tests/helpers/tomselect.ts`
- [ ] Implement `selectTomSelect(page, selector, searchQuery?, optionIndex?)`:
      - Wait for TomSelect to be initialized on the element
      - Use `page.evaluate()` to call `ts.load()` + `ts.setValue()`
      - Verify selection was made (getValue() !== '')
      - Return selected value info
      ref: docs/tests/playwright-smoke-test-guide.md:L290-L330 — Node.js TomSelect helper pattern
- [ ] Implement `setTomSelectValue(page, selector, valueId)`:
      - Direct set by known ID (for seeded data)
      - Useful when we know the exact ID from seed
- [ ] Implement `clearTomSelect(page, selector)`:
      - Clear current selection
- [ ] Implement `getTomSelectValue(page, selector)`:
      - Read current value for assertions
- [ ] Write a quick smoke test that verifies TomSelect helper works:
      - Navigate to Product create form
      - Use helper to select a category
      - Assert value was set

**Validation criteria:**
- Helper compiles without errors
- Smoke test passes against running e2e server

---

### Task 8: AutoNumeric Helper
Build reusable helper for AutoNumeric decimal inputs — needed for Product stock/weight fields.

**Depends on:** Task 2
**Reference:** docs/tests/playwright-smoke-test-guide.md Section 4.4

Steps:
- [ ] Create `e2e-tests/helpers/autonumeric.ts`
- [ ] Implement `setAutoNumeric(page, selector, value)`:
      - Wait for AutoNumeric to be initialized
      - Use `page.evaluate()` to call `AutoNumeric.getAutoNumericElement(el).set(value)`
      - Verify value was set
      ref: docs/proposals/e2e-playwright/FINAL-PROPOSAL.md:L516-L540 — AutoNumeric helper pattern
- [ ] Implement `getAutoNumericValue(page, selector)`:
      - Read raw numeric value (not formatted display)
- [ ] Implement `clearAutoNumeric(page, selector)`:
      - Reset to 0 or empty

**Validation criteria:**
- Helper compiles without errors
- Can be used in Product test for stock fields

---

### Task 9: Product CRUD Test Spec (Mid-Complexity)
Uses TomSelect (category, brand), AutoNumeric (stock fields), conditional visibility, standard selects (UoM).

**Depends on:** Task 1, Task 7, Task 8, Task 3
**Reference:** Product Controller URLs: `/inventory/products`

Steps:
- [ ] Create `e2e-tests/tests/master-data/product.spec.ts`
- [ ] Test: "should display product list page"
      - Navigate to `/inventory/products`
      - Assert: table visible
- [ ] Test: "should create new product with all required fields"
      - Navigate to `/inventory/products/create`
      - Fill `name` with unique value
      - Use TomSelect helper to select `categoryId` (seeded E2E-CAT-STOCK)
      - Use standard select for `uomId` (seeded E2E-PCS)
      - Use TomSelect helper to select `brandId` (seeded E2E-BRAND-A)
      - Use AutoNumeric helper to set `minStock` = 10
      - Use AutoNumeric helper to set `maxStock` = 100
      - Submit (AJAX)
      - Assert: redirected to list, new product visible
- [ ] Test: "should show/hide stock fields based on category type"
      - Navigate to create
      - Select category with type STOCK → stock fields visible
      - Select category with type SERVICE → stock fields hidden
- [ ] Test: "should edit existing product"
      - Navigate to edit (use a product from list or create one first)
      - Change name, change brand via TomSelect
      - Submit, verify changes
- [ ] Test: "should show validation error for missing required fields"
      - Submit without name or category
      - Assert: validation errors visible

**Validation criteria:**
- `npx playwright test tests/master-data/product.spec.ts` — all pass
- TomSelect interactions are stable (no flakiness)
- AutoNumeric values submitted correctly

---

### Task 10: Full Suite Validation & Cleanup
Run all tests together, ensure no conflicts, update validation script.

**Depends on:** Task 4, Task 5, Task 6, Task 9

Steps:
- [ ] Run full suite: `npx playwright test` — all specs pass together
- [ ] Verify test isolation: run in different order, still pass
- [ ] Update `e2e-tests/package.json` scripts:
      - Add `"test:crud": "npx playwright test tests/master-data/"`
      - Add `"test:all": "npx playwright test"`
- [ ] Clean up any temporary test data patterns (ensure uniqueId usage)
- [ ] Final commit with all passing

**Validation criteria:**
- `npx playwright test` — all tests pass (login + 4 CRUD modules)
- Total run time < 30 seconds
- No flaky tests on 3 consecutive runs
