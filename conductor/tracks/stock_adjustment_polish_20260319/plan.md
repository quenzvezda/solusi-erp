# Implementation Plan: Stock Adjustment UI Polish & Autocomplete

## Phase 1: Backend Infrastructure & Lookup API [x]
- [x] Task: Create `LookupDto` record in `com.solusi.erp.core.dto`.
- [x] Task: Create Flyway migration `V30__Add_Facility_To_Adjustment_And_Lookup_Permissions.sql`.
    - [x] Add `facility_id` to `inv_stock_adjustments`.
    - [x] Add `grid_id` to `inv_stock_adjustment_lines`.
    - [x] Create `LOOKUP_INVENTORY` permission and grant to `ROLE_ADMIN`.
- [x] Task: Update `Inventory` repositories/services to support lookup queries.
    - [x] Add search methods with limit for Product, Facility, Grid, and Container.
- [x] Task: Implement `InventoryLookupController`.
    - [x] Endpoints for: `/api/lookup/products`, `/api/lookup/facilities`, `/api/lookup/grids?facilityId=x`, `/api/lookup/containers?gridId=y`.
- [x] Task: Conductor - User Manual Verification 'Backend Lookup API' (Protocol in workflow.md)

## Phase 2: DTO & Service Updates [x]
- [x] Task: Update `StockAdjustmentRequest` and `StockAdjustmentResponse` to include `facilityId` and `gridId`.
- [x] Task: Update `StockAdjustmentMapper` to map new fields.
- [x] Task: Update `StockAdjustmentServiceImpl` to persist the Header Facility.
- [x] Task: Conductor - User Manual Verification 'DTO & Service Updates' (Protocol in workflow.md)

## Phase 3: UI Polish & TomSelect Integration [x]
- [x] Task: Add reusable i18n keys to `messages.properties`.
    - [x] `msg.error.please_select_first`
- [x] Task: Refactor `form.html` to use TomSelect for all lookups.
    - [x] Implement dependent filtering logic (Grid filter by Facility, Container filter by Grid).
    - [x] Implement "Add Line" validation with the `msg.error.please_select_first` alert.
- [x] Task: Update `view.html` to display Facility and Grid information.
- [x] Task: Conductor - User Manual Verification 'UI Polish' (Protocol in workflow.md)

## Phase 4: Final Testing & Synchronization [x]
- [x] Task: Verify hierarchical filtering works across multiple rows.
- [x] Task: Update project documentation if necessary.
- [x] Task: Conductor - User Manual Verification 'Final Review' (Protocol in workflow.md)
