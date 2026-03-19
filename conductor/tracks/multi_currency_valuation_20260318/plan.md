# Implementation Plan: Multi-Currency COGS & UOM Conversion

## Phase 1: Foundation Entities [x]
- [x] Task: Create `CurrencyAmount` JPA `@Embeddable` class.
- [x] Task: Create `ProductUomConversion` entity and repository.
    - [x] Fields: `product`, `fromUom`, `toUom`, `conversionFactor`.
- [x] Task: Create Flyway migration for `product_uom_conversions` table.
- [x] Task: Conductor - User Manual Verification 'Foundation Entities' (Protocol in workflow.md)

## Phase 2: FIFO Storage & Audit Updates [x]
- [x] Task: Create `ValuationLayer` entity and repository.
    - [x] Fields: `product`, `container`, `serialNumber`, `initialQuantity`, `remainingQuantity`, `unitCost` (CurrencyAmount).
- [x] Task: Update `InventoryMovement` entity to include `unitCost` (CurrencyAmount).
- [x] Task: Create Flyway migration for `inv_valuation_layers` and `inv_movements` updates.
- [x] Task: Conductor - User Manual Verification 'FIFO Storage & Audit Updates' (Protocol in workflow.md)

## Phase 3: Core Logic - UOM Conversion [x]
- [x] Task: Implement `UomConversionService` to handle quantity conversion to Base UOM.
- [x] Task: Write unit tests for `UomConversionService`.
- [x] Task: Conductor - User Manual Verification 'Core Logic - UOM Conversion' (Protocol in workflow.md)

## Phase 4: Core Logic - FIFO Engine Integration [x]
- [x] Task: Create `ValuationService` to handle creation and consumption of valuation layers.
- [x] Task: Update `StockService` to integrate `UomConversionService` and `ValuationService`.
    - [x] Ensure atomic updates to `StockBalance`, `ValuationLayer`, and `InventoryMovement`.
- [x] Task: Write comprehensive unit tests for FIFO consumption logic covering edge cases:
    - [x] **Test Case**: Partial consumption of a single layer.
    - [x] **Test Case**: Exact consumption of a single layer (layer becomes empty).
    - [x] **Test Case**: Multi-layer spanning (consumption across 2 or more layers).
    - [x] **Test Case**: Insufficient total stock (across all layers).
    - [x] **Test Case**: Handling zero-cost items.
    - [x] **Test Case**: Sales Return (adding back a layer with specific cost).
- [x] Task: Conductor - User Manual Verification 'Core Logic - FIFO Engine Integration' (Protocol in workflow.md)
