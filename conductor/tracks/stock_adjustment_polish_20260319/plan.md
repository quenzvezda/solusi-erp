# Implementation Plan: Stock Adjustment UI Polish & Autocomplete

## Phase 1: Backend Infrastructure & Lookup API
- [ ] Task: Create `LookupDto` record in `com.solusi.erp.core.dto`.
- [ ] Task: Create Flyway migration `V30__Add_Facility_To_Adjustment_And_Lookup_Permissions.sql`.
    - [ ] Add `facility_id` to `inv_stock_adjustments`.
    - [ ] Add `grid_id` to `inv_stock_adjustment_lines`.
    - [ ] Create `LOOKUP_INVENTORY` permission and grant to `ROLE_ADMIN`.
- [ ] Task: Update `Inventory` repositories/services to support lookup queries.
    - [ ] Add search methods with limit for Product, Facility, Grid, and Container.
- [ ] Task: Implement `InventoryLookupController`.
    - [ ] Endpoints for: `/api/lookup/products`, `/api/lookup/facilities`, `/api/lookup/grids?facilityId=x`, `/api/lookup/containers?gridId=y`.
- [ ] Task: Conductor - User Manual Verification 'Backend Lookup API' (Protocol in workflow.md)

## Phase 2: DTO & Service Updates
- [ ] Task: Update `StockAdjustmentRequest` and `StockAdjustmentResponse` to include `facilityId` and `gridId`.
- [ ] Task: Update `StockAdjustmentMapper` to map new fields.
- [ ] Task: Update `StockAdjustmentServiceImpl` to persist the Header Facility.
- [ ] Task: Conductor - User Manual Verification 'DTO & Service Updates' (Protocol in workflow.md)

## Phase 3: UI Polish & TomSelect Integration
- [ ] Task: Add reusable i18n keys to `messages.properties`.
    - [ ] `msg.error.please_select_first`
- [ ] Task: Refactor `form.html` to use TomSelect for all lookups.
    - [ ] Implement dependent filtering logic (Grid filter by Facility, Container filter by Grid).
    - [ ] Implement "Add Line" validation with the `msg.error.please_select_first` alert.
- [ ] Task: Update `view.html` to display Facility and Grid information.
- [ ] Task: Conductor - User Manual Verification 'UI Polish' (Protocol in workflow.md)

## Phase 4: Final Testing & Synchronization
- [ ] Task: Verify hierarchical filtering works across multiple rows.
- [ ] Task: Update project documentation if necessary.
- [ ] Task: Conductor - User Manual Verification 'Final Review' (Protocol in workflow.md)
