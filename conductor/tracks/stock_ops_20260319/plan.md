# Implementation Plan: Stock Management Operations & Menu Refactor

## Phase 1: Menu Reorganization & Infrastructure [x]
- [x] Task: Update `docs/spec/menu-structure.md` to reflect the new hierarchy.
- [x] Task: Create Flyway migration `V28__Refactor_Inventory_Menu_And_Add_Groups.sql`.
    - [x] Update `permission_groups` breadcrumbs for `INV-01` to `INV-07`.
    - [x] Insert new `permission_groups`: `INV-08` (Stock Adjustment), `INV-09` (Stock Card), `INV-10` (On-Hand).
- [x] Task: Update `PermissionGroupServiceImpl.java` if any new Parent Icons are needed.
- [x] Task: Conductor - User Manual Verification 'Menu Reorganization' (Protocol in workflow.md)

## Phase 2: Stock Adjustment Entities & Database [x]
- [x] Task: Create JPA Entities `StockAdjustment` and `StockAdjustmentLine`.
    - [x] `StockAdjustment` extends `BaseModel`, includes `CurrencyAmount`.
    - [x] `StockAdjustmentLine` includes `product`, `container`, `quantity`, `unitCost`, `totalAmount`, `serialNumber`.
- [x] Task: Create repositories `StockAdjustmentRepository` and `StockAdjustmentLineRepository`.
- [x] Task: Create Flyway migration `V29__Stock_Adjustment_Schema.sql`.
- [x] Task: Conductor - User Manual Verification 'Entities & Database' (Protocol in workflow.md)

## Phase 3: Stock Adjustment Business Logic (TDD) [x]
- [x] Task: Create DTOs (`StockAdjustmentRequest`, `StockAdjustmentResponse`, etc.) and Mappers.
- [x] Task: Implement `StockAdjustmentService`.
    - [x] Implement `create`, `update`, `delete`.
    - [x] Implement `process(Long id)`: Validates status, calls `StockService.adjust()` for each line, updates status to `COMPLETED`.
- [x] Task: Write unit tests for `StockAdjustmentService` processing logic.
- [x] Task: Conductor - User Manual Verification 'Business Logic' (Protocol in workflow.md)

## Phase 4: Stock Adjustment UI [x]
- [x] Task: Create `StockAdjustmentController`.
- [x] Task: Create Thymeleaf templates:
    - [x] `list.html`: Searchable and paginated list of adjustments.
    - [x] `form.html`: Header form + dynamic table for lines (using JavaScript for row addition/deletion).
    - [x] `view.html`: Read-only view for `COMPLETED` documents with "Process" button for `DRAFT`.
- [x] Task: Conductor - User Manual Verification 'Adjustment UI' (Protocol in workflow.md)

## Phase 5: On-Hand Quantity & Stock Card [x]
- [x] Task: Implement `InventoryReportService`.
    - [x] `getOnHandSummary()`: Query for aggregated product stock.
    - [x] `getOnHandDetail(Long productId)`: Query for stock breakdown per location.
    - [x] `getStockCard(StockCardFilter filter)`: Query `InventoryMovement` with filters.
- [x] Task: Create `InventoryReportController`.
- [x] Task: Create Thymeleaf templates:
    - [x] `on-hand/list.html`: Product totals.
    - [x] `on-hand/detail.html`: Location breakdown.
    - [x] `stock-card/list.html`: Movement log with source document links.
- [x] Task: Conductor - User Manual Verification 'Reports' (Protocol in workflow.md)
