# Playwright Smoke Test Report — Inventory Module Refactoring

## Test Date
2026-03-31

## Environment
- Server: Spring Boot on `localhost:18080`
- Credentials: `admin` / `admin123`
- Browser: Playwright MCP (Chromium)

## Results Summary

| # | Feature | Test | Status | Notes |
|---|---------|------|--------|-------|
| 1 | Brand | List renders | ✅ PASS | HTTP 200, table with data, sorting links present |
| 2 | Brand | Create form renders | ✅ PASS | HTTP 200, AJAX form initialized |
| 3 | Product | List renders | ✅ PASS | HTTP 200, table with data |
| 4 | Product | Create form renders | ✅ PASS | HTTP 200, AJAX form initialized |
| 5 | ProductCategory | List renders | ✅ PASS | HTTP 200, table with data |
| 6 | ProductCategory | Create form renders | ✅ PASS | HTTP 200, AJAX form initialized |
| 7 | UOM | List renders | ✅ PASS | HTTP 200, table with data |
| 8 | UOM | Create form renders | ✅ PASS | HTTP 200, AJAX form initialized |
| 9 | UomConversion | List renders | ✅ PASS | HTTP 200, table with data |
| 10 | UomConversion | Create form renders | ✅ PASS | HTTP 200, AJAX form initialized |
| 11 | Facility | List renders | ✅ PASS | HTTP 200, table with data |
| 12 | Facility | Create form renders | ✅ PASS | HTTP 200, AJAX form initialized |
| 13 | Grid | List renders | ✅ PASS | HTTP 200, table with data |
| 14 | Grid | Create form renders | ✅ PASS | HTTP 200, AJAX form initialized |
| 15 | Container | List renders | ✅ PASS | HTTP 200, table with data |
| 16 | Container | Create form renders | ✅ PASS | HTTP 200, AJAX form initialized |
| 17 | Stock Adjustment | List renders | ✅ PASS | HTTP 200, table with data |
| 18 | Stock Adjustment | Create form renders | ✅ PASS | HTTP 200, form present |
| 19 | Report: Stock Card | Page renders | ✅ PASS | HTTP 200 (after template fix: `c.code` → `c.name`) |
| 20 | Report: On-Hand | Page renders | ✅ PASS | HTTP 200, table with data |

## Issues Found & Fixed During Testing

### 1. Stock Card Report Template Error (FIXED)
- **Symptom**: `/inventory/reports/stock-card` returned HTTP 500 / timeout
- **Root Cause**: Template `stock-card/list.html` line 36 used `${c.code}` for container dropdown, but `GetContainerLookupUseCase.findAll()` returns `LookupDto` which has `id`, `name`, `subText` — no `code` field
- **Fix**: Changed `th:text="${c.code}"` to `th:text="${c.name}"` in `templates/inventory/reports/stock-card/list.html`
- **Status**: ✅ Fixed and verified

## Tests NOT Performed (Recommended for Follow-up)

The following deeper tests were not automated in this smoke run and should be performed manually or in a dedicated E2E suite:

### Form Submission Tests
- [ ] Brand: Submit create form → verify data saved correctly
- [ ] Product: Submit create form with brand/category/UOM autocomplete → verify associations saved
- [ ] Stock Adjustment: Add line items with product/grid/container autocomplete → verify line items saved
- [ ] UoM Conversion: Create conversion for product → verify factor saved
- [ ] Grid: Create grid with facility autocomplete → verify facilityId saved

### Autocomplete/Lookup Tests
- [ ] Product form: Brand autocomplete returns results
- [ ] Product form: Category autocomplete returns results
- [ ] Product form: UOM autocomplete returns results
- [ ] Grid form: Facility autocomplete returns results
- [ ] Container form: Grid autocomplete returns results
- [ ] Stock Adjustment line: Product, Grid, Container autocompletes work

### Data Integrity Tests
- [ ] Edit existing Brand → verify version/audit fields preserved
- [ ] Edit existing Product → verify all FK relationships intact
- [ ] Process stock adjustment → verify stock balance/movement/valuation records created

### Pagination & Sorting Tests
- [ ] All list pages: Click column headers → verify sort order changes
- [ ] All list pages: Navigate pagination → verify correct page displayed
- [ ] All list pages: Search filter → verify results filtered

## Conclusion
All 20 Playwright render smoke tests **PASS**. The only issue found (stock-card template `c.code` → `c.name`) was fixed during testing. The refactoring from horizontal to vertical slice architecture is complete with no rendering regressions.
