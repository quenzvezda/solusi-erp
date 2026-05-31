# Implementation Plan: Stock Card Improvement (SOL-7)

> Source: Linear SOL-7 — https://linear.app/solusi-program/issue/SOL-7/improvement-for-stock-card
> Created: 2026-05-31
> Sprint: Inventory Feature
> Status: IN_PROGRESS

## Summary

Improve the Stock Card report (`/inventory/reports/stock-card`) with: hyperlinks to source documents (GR & Stock Adjustment only), a two-line Cost column (total on top, unit "Each" below), Product & Container converted to TomSelect autocomplete, a permanent Serial Number column, document-code search, a renamed "Movement Type" badge column, and filtering by both Movement Type and Document Type. The filter bar is reorganized into a compact two-tier layout with a collapsible Advanced section.

## Locked Decisions

1. **Two filters** — Movement Type (`movementType`) **and** Document Type (`referenceType`). The badge column currently labeled "Status" actually shows `movementType` → rename it to "Movement Type". Document Type is a new filter mapped to `referenceType`.
2. **Hyperlinks** — Only `GOODS_RECEIPT` → `/inventory/goods-receipts/{referenceId}` and `STOCK_ADJUSTMENT` → `/inventory/adjustments/view/{referenceId}`. All other reference types render as plain text (no dead links).
3. **Container RBAC** — No new seeder. `LOOKUP_CONTAINER` is already granted to `ROLE_ADMIN` in `V31__Add_Inventory_Lookup_Permissions.sql`. Non-admin roles get it manually via the Role UI later.
4. **Filter layout** — Two-tier: primary row (Product, Container, Search code, Filter button) + collapsible "Advanced" row (Date range, Document Type, Movement Type).

## Existing-State Notes (verified)

- `InventoryMovementEntity` already persists `serialNumber`, `movementType`, `referenceType`, `referenceId`, `referenceCode`, embedded `unitCost` — no schema/migration change needed.
      ref: src/main/java/com/solusi/erp/inventory/stock/infrastructure/persistence/InventoryMovementEntity.java:L26-L63
- `serialNumber` maps to the response automatically (matching field name) — no backend mapping change for serial.
      ref: src/main/java/com/solusi/erp/inventory/report/web/dto/InventoryMovementResponse.java:L31
- Lookups exist: `/api/lookup/inventory/products` (`isAuthenticated()`, payload has `isSerialized`) and `/api/lookup/inventory/containers` (`LOOKUP_CONTAINER`).
      ref: src/main/java/com/solusi/erp/inventory/product/web/controller/ProductLookupController.java:L18-L23
      ref: src/main/java/com/solusi/erp/inventory/container/web/controller/ContainerLookupController.java:L18-L25
- Trinity prefill providers exist: `ProductLookupProvider.resolve(id)` and `ContainerLookupProvider.resolve(id)`.
      ref: src/main/java/com/solusi/erp/inventory/product/domain/port/ProductLookupProvider.java:L5-L6
- The current template loads up to 1000 products into a `<select>` — autocomplete removes this inefficiency.
      ref: src/main/java/com/solusi/erp/inventory/report/web/controller/InventoryReportController.java:L62-L67

## Tasks

### Task 1: Extend filter + repository search (Movement Type, Document Type, doc-code search)
Add `movementType`, `referenceType`, and `keyword` to `StockCardFilter`; extend the repository query and use case to filter on them.

**Depends on:** (none)
**Reference module:** `inventory.report` (self), repository pattern in `InventoryMovementJpaRepository`

Steps:
- [x] Add fields to `StockCardFilter`: `MovementType movementType`, `ReferenceType referenceType`, `String keyword` (search by `referenceCode`).
      ref: src/main/java/com/solusi/erp/inventory/report/web/dto/StockCardFilter.java:L12-L19 — current 4-field DTO
- [x] Extend `InventoryMovementJpaRepository.search(...)` JPQL: add `(:movementType IS NULL OR m.movementType = :movementType)`, `(:referenceType IS NULL OR m.referenceType = :referenceType)`, and `(:keyword IS NULL OR LOWER(m.referenceCode) LIKE LOWER(CONCAT('%', :keyword, '%')))`. Keep existing `ORDER BY m.transactionDate DESC, m.id DESC`.
      ref: src/main/java/com/solusi/erp/inventory/stock/infrastructure/persistence/InventoryMovementJpaRepository.java:L18-L28 — current search query + params
- [x] Update `GetStockCardUseCaseImpl.execute(...)` to pass the new params (normalize blank `keyword` to null).
      ref: src/main/java/com/solusi/erp/inventory/report/application/usecase/query/GetStockCardUseCaseImpl.java:L21-L32 — current param wiring
- [x] **TEST:** Update/extend `GetStockCardUseCaseTest` (Mockito `@Mock` repository + mapper): verify date-boundary conversion still works AND new params (`movementType`, `referenceType`, blank-keyword→null) are forwarded to `repository.search(...)` via `ArgumentCaptor` / `verify`.
      ref: docs/plans/2026-05-30-sol-55-autocomplete-forbidden-feedback.md — Mockito use-case test convention in this module

**Validation criteria:**
- `mvn -q -Dtest=GetStockCardUseCaseTest test` green.
- Passing `null` for all new params reproduces existing behavior (no regression).

### Task 2: Response enrichment — total cost (qty × unit)
Add a derived `totalCostLocal` to the response so the Cost column can show total (top) and unit/"Each" (bottom). Serial number already maps automatically.

**Depends on:** (none)
**Reference module:** `inventory.report` (self)

Steps:
- [x] Add `BigDecimal totalCostLocal` field to `InventoryMovementResponse`.
      ref: src/main/java/com/solusi/erp/inventory/report/web/dto/InventoryMovementResponse.java:L38-L41 — existing cost fields (unitCostOriginal/Local, currencyAlias)
- [x] In `InventoryMovementMapper.enrichResponse(...)` (`@AfterMapping`), compute `totalCostLocal = unitCostLocal × quantity.abs()` when both are non-null (use `quantity.abs()` so issues/negatives show a positive money figure; the sign stays on the Qty column).
      ref: src/main/java/com/solusi/erp/inventory/report/web/mapper/InventoryMovementMapper.java:L46-L67 — existing @AfterMapping enrichment block
- [x] **TEST:** Extend `InventoryMovementMapperTest` (or create if absent): given qty=5, unitCostLocal=5,000,000 → `totalCostLocal`=25,000,000; null unitCost → null total.
      ref: src/main/java/com/solusi/erp/inventory/report/web/mapper/InventoryMovementMapper.java:L69-L100 — resolveCurrencyAlias pattern to follow for test setup

**Validation criteria:**
- `mvn -q -Dtest=InventoryMovementMapperTest test` green (or use-case test asserting the computed value if no mapper test exists).
- `totalCostLocal` null-safe when `unitCost` absent.

### Task 3: Controller — autocomplete prefill + enum dropdowns, drop product/container preload
Wire the controller for autocomplete trinity prefill and supply enum lists for the two new dropdowns; remove the 1000-row product/container preload.

**Depends on:** Task 1
**Reference module:** `docs/spec/autocomplete-generic.md` §4 (buildXxxUI controller pattern)

Steps:
- [x] Inject `ProductLookupProvider` and `ContainerLookupProvider` into `InventoryReportController` (constructor via `@RequiredArgsConstructor`). Per AGENTS layer-boundary rule, use the lookup **ports**, not JPA repositories.
      ref: docs/AGENTS.md:L133 — web layer may inject lookup/query ports, not repositories
- [x] In `stockCard(...)`: remove `findProductsUseCase` preload (`products`) and `getContainerLookupUseCase.findAll()` (`containers`). Replace with a `buildFilterUI(filter)` helper that resolves trinity data only when `filter.productId`/`filter.containerId` is set, returning a `Map<String,Object>` with `productText/productSubtext/containerText/containerSubtext`.
      ref: src/main/java/com/solusi/erp/inventory/report/web/controller/InventoryReportController.java:L54-L69 — current stockCard method to refactor
      ref: docs/spec/autocomplete-generic.md:L143-L171 — buildXxxUI() controller pattern + template binding
- [x] Add model attributes for the two enum dropdowns: `movementTypes = MovementType.values()`, `referenceTypes = ReferenceType.values()`.
      ref: src/main/java/com/solusi/erp/inventory/stock/domain/model/MovementType.java:L11-L19
      ref: src/main/java/com/solusi/erp/inventory/stock/domain/model/ReferenceType.java:L11-L19
- [x] Remove now-unused `FindProductsUseCase`, `GetContainerLookupUseCase`, `ProductWebMapper`, `Pageable` imports/fields if no longer referenced elsewhere in the controller. (Keep `getProductUseCase` etc. used by on-hand methods.)
      ref: src/main/java/com/solusi/erp/inventory/report/web/controller/InventoryReportController.java:L26-L32 — field declarations
- [x] **TEST:** Update `InventoryReportControllerTest` (Mockito): assert `@PreAuthorize("hasAuthority('STOCK-CARD_READ')")` preserved on `stockCard`; assert model contains `movementTypes`, `referenceTypes`, `page`, and filter UI map; assert no `products`/`containers` preload attribute.

**Validation criteria:**
- `mvn -q -Dtest=InventoryReportControllerTest test` green.
- Controller injects no JPA repository (layer-boundary compliance).

### Task 4: i18n keys (en + id)
Add all new message keys for column headers, the "Each" suffix, filter labels, and Advanced toggle.

**Depends on:** (none — but values consumed by Task 5)
**Reference module:** `docs/spec/i18n-guide.md`

Steps:
- [ ] Using the **replace tool** (NOT echo — per AGENTS §5 i18n protocol), add to `messages.properties` (English): `label.stock-card.movement-type=Movement Type`, `label.stock-card.document-type=Document Type`, `label.stock-card.serial=Serial No.`, `label.stock-card.cost-local=Cost (Local)`, `label.stock-card.cost.each=Each`, `label.stock-card.reference=Reference`, `label.stock-card.search.placeholder=Search document no.`, `label.stock-card.filter.advanced=Advanced`, `label.stock-card.all-movement-types=-- All Movement Types --`, `label.stock-card.all-document-types=-- All Document Types --`.
      ref: src/main/resources/messages.properties:L802-L803 — existing stock-card.title/subtitle keys (insert near here)
      ref: docs/spec/i18n-guide.md#7-ai-guidelines-for-updating-i18n-files-critical — replace-tool protocol
- [ ] Add the Indonesian equivalents to `messages_id.properties` with the same keys (e.g. `...movement-type=Tipe Pergerakan`, `...document-type=Tipe Dokumen`, `...serial=No. Seri`, `...cost.each=Per Unit`, `...filter.advanced=Lanjutan`).
      ref: src/main/resources/messages_id.properties — mirror the en keys (find the stock-card block)
- [ ] Verify enum keys already exist (no add needed): `enum.reference.type.*` and `enum.movement.type.*`.
      ref: src/main/resources/messages.properties:L747-L754 — reference.type keys confirmed present

**Validation criteria:**
- Both files contain identical key sets (no missing-key warnings at render).
- App boots without `NoSuchMessageException` on the stock-card page.

### Task 5: Template — HTML structure (filter two-tier, autocomplete, cost two-line, serial, hyperlinks, rename)
Rebuild `list.html` filter bar and table per the locked layout. **Read `docs/spec/autocomplete-generic.md` before editing.**

**Depends on:** Task 3, Task 4
**Reference module:** `docs/spec/autocomplete-generic.md` (fragment usage), `fragments/inputs :: autocomplete`

Steps:
- [ ] Replace Product `<select>` with autocomplete fragment: `~{fragments/inputs :: autocomplete(field='productId', label=#{label.product}, path='inventory/products', initialValue=${filter.productId}, initialText=${filterUI != null ? filterUI.productText : ''}, initialSubtext=${filterUI != null ? filterUI.productSubtext : ''})}`.
      ref: docs/spec/autocomplete-generic.md:L37-L48 — autocomplete fragment + Trinity Data rule
      ref: src/main/resources/templates/fragments/inputs.html:L33-L41 — autocomplete fragment signature
- [ ] Replace Container `<select>` with the same fragment pattern: `field='containerId'`, `path='inventory/containers'`, `initialText/Subtext` from `filterUI`.
- [ ] Wrap filter form in two-tier layout: **primary row** = Product, Container, Search input (`name=keyword`, placeholder `#{label.stock-card.search.placeholder}`), Filter button. **Advanced collapsible** (`<a data-bs-toggle="collapse" href="#advFilters">` + `<div class="collapse" id="advFilters">`) = From/To date, Document Type `<select th:field="*{referenceType}">`, Movement Type `<select th:field="*{movementType}">`. Auto-expand the collapse when any advanced filter is active (`th:classappend="${filter.startDate != null or filter.endDate != null or filter.movementType != null or filter.referenceType != null} ? 'show'"`).
      ref: src/main/resources/templates/inventory/reports/stock-card/list.html:L21-L55 — current filter card to replace
- [ ] Build Document Type & Movement Type `<select>` options from `${referenceTypes}` / `${movementTypes}`, label via `#{__${t.messageKey}__}`, with an "all" option (`#{label.stock-card.all-document-types}` / `...all-movement-types}`).
      ref: src/main/java/com/solusi/erp/inventory/stock/domain/model/ReferenceType.java:L21 — messageKey getter
- [ ] Add a permanent **Serial No.** column header (`#{label.stock-card.serial}`) and cell (`th:text="${item.serialNumber} ?: '-'"`).
      ref: src/main/resources/templates/inventory/reports/stock-card/list.html:L60-L69 — thead to extend
- [ ] Rename the badge column header from `#{label.stock-adjustment.status}` to `#{label.stock-card.movement-type}` (still binds `item.movementType`). Keep the Reference header as `#{label.stock-card.reference}`.
      ref: src/main/resources/templates/inventory/reports/stock-card/list.html:L66 — current "Status" header
- [ ] Convert the Cost cell to two lines: top = `totalCostLocal` (formatted), bottom = `unitCostLocal` + ` ` + `#{label.stock-card.cost.each}` in muted small text. Null-guard both. Use `#numbers.formatDecimal(..., 1, 'COMMA', 2, 'POINT')`.
      ref: src/main/resources/templates/inventory/reports/stock-card/list.html:L92-L96 — current single-line cost cell
- [ ] Convert the Reference cell to a hyperlink via `th:switch="${item.referenceType}"`: case `GOODS_RECEIPT` → `@{/inventory/goods-receipts/{id}(id=${item.referenceId})}`; case `STOCK_ADJUSTMENT` → `@{/inventory/adjustments/view/{id}(id=${item.referenceId})}`; default → plain text. Show `referenceCode` as link text, `enum.reference.type.*` as subtitle.
      ref: src/main/resources/templates/inventory/reports/stock-card/list.html:L88-L91 — current reference cell
- [ ] Update empty-state `colspan` to match the new column count (was 7 → now 8 with Serial).
      ref: src/main/resources/templates/inventory/reports/stock-card/list.html:L98-L100
- [ ] **TEST:** Add/extend a template test (`TemplateTestUtils` static read): assert presence of autocomplete fragment markers (`data-lookup-path`), serial header key, `cost.each` key, and hyperlink `th:switch`.
      ref: docs/spec/autocomplete-generic.md:L37-L41 — data-lookup-path attribute to assert

**Validation criteria:**
- Page renders with no Thymeleaf parse errors.
- Filter retains selected Product/Container labels after GET submit (trinity prefill works).
- Advanced section auto-expands when a date/type filter is active.

### Task 6: Template — JS wiring for autocomplete
Add page-specific JS to initialize the two TomSelect autocompletes (filter context, not a line form).

**Depends on:** Task 5
**Reference module:** `docs/spec/autocomplete-generic.md` §2B (global auto-init) / §2C (`initLookup`)

Steps:
- [ ] Prefer **global auto-initialization**: the `autocomplete` fragment with `data-lookup-path` is auto-initialized by the shared handler — confirm no manual JS is needed for a plain (non-cascading) filter. If the filter `<select>`s are inside the GET form and submit correctly as `productId`/`containerId`, no custom script is required.
      ref: docs/spec/autocomplete-generic.md:L34-L48 — Global Auto-Initialization
- [ ] If auto-init does not fire in this page's layout slot, add a minimal page script using `initLookup(el, 'inventory/products')` and `initLookup(el, 'inventory/containers')` in the layout's JS slot.
      ref: docs/spec/autocomplete-generic.md:L50-L62 — initLookup manual init
      ref: src/main/resources/templates/layout/master.html — JS slot / HTMX re-init hook
- [ ] Ensure the GET filter form serializes the TomSelect values (TomSelect backs a real `<select th:field>`, so standard form GET works — verify the submitted query string includes `productId`/`containerId`).

**Validation criteria:**
- Typing in Product/Container triggers async search (network call to `/api/lookup/...`).
- Submitting the filter carries the selected ids; results filter correctly.
- (Manual) Container autocomplete works for admin (already holds `LOOKUP_CONTAINER`).

## Coverage Check (vs SOL-7 + user additions)

| Requirement | Covered by |
|---|---|
| Hyperlink to source document | Task 5 (th:switch, GR + SA only) |
| Cost column: total (top) + Each (bottom), i18n | Task 2 (totalCostLocal) + Task 4 (`cost.each`) + Task 5 (two-line cell) |
| Product & Container → Autocomplete | Task 3 (prefill) + Task 5 (fragment) + Task 6 (JS) |
| Serial number permanent column | Task 5 (already-mapped field) |
| Search by document code/no | Task 1 (keyword→referenceCode) + Task 5 (search input) |
| "Status" column is really Type → rename | Task 5 (header → Movement Type) |
| Filter by Movement Type + Document Type | Task 1 (DTO/repo) + Task 3 (enum lists) + Task 5 (dropdowns) |
| Compact filter layout | Task 5 (two-tier + Advanced collapse) |

## Versioning Note (per AGENTS §9.A)

This is a feature enhancement (new filters, autocomplete, columns) — bump **MINOR** in `pom.xml` (current `1.8.1` → suggest `1.9.0`) after implementation + tests pass. Confirm with user before committing.
