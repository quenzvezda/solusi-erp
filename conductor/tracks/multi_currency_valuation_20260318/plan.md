# Implementation Plan: Multi-Currency COGS & UOM Conversion

## Phase 1: Foundation Entities
- [ ] Task: Create `CurrencyAmount` JPA `@Embeddable` class.
- [ ] Task: Create `ProductUomConversion` entity and repository.
    - [ ] Fields: `product`, `fromUom`, `toUom`, `conversionFactor`.
- [ ] Task: Create Flyway migration for `product_uom_conversions` table.
- [ ] Task: Conductor - User Manual Verification 'Foundation Entities' (Protocol in workflow.md)

## Phase 2: FIFO Storage & Audit Updates
- [ ] Task: Create `ValuationLayer` entity and repository.
    - [ ] Fields: `product`, `container`, `serialNumber`, `initialQuantity`, `remainingQuantity`, `unitCost` (CurrencyAmount).
- [ ] Task: Update `InventoryMovement` entity to include `unitCost` (CurrencyAmount).
- [ ] Task: Create Flyway migration for `inv_valuation_layers` and `inv_movements` updates.
- [ ] Task: Conductor - User Manual Verification 'FIFO Storage & Audit Updates' (Protocol in workflow.md)

## Phase 3: Core Logic - UOM Conversion
- [ ] Task: Implement `UomConversionService` to handle quantity conversion to Base UOM.
- [ ] Task: Write unit tests for `UomConversionService`.
- [ ] Task: Conductor - User Manual Verification 'Core Logic - UOM Conversion' (Protocol in workflow.md)

## Phase 4: Core Logic - FIFO Engine Integration
- [ ] Task: Create `ValuationService` to handle creation and consumption of valuation layers.
- [ ] Task: Update `StockService` to integrate `UomConversionService` and `ValuationService`.
    - [ ] Ensure atomic updates to `StockBalance`, `ValuationLayer`, and `InventoryMovement`.
- [ ] Task: Write comprehensive unit tests for FIFO consumption logic covering edge cases:
    - [ ] **Test Case**: Partial consumption of a single layer.
    - [ ] **Test Case**: Exact consumption of a single layer (layer becomes empty).
    - [ ] **Test Case**: Multi-layer spanning (consumption across 2 or more layers).
    - [ ] **Test Case**: Insufficient total stock (across all layers).
    - [ ] **Test Case**: Handling zero-cost items.
    - [ ] **Test Case**: Sales Return (adding back a layer with specific cost).
- [ ] Task: Conductor - User Manual Verification 'Core Logic - FIFO Engine Integration' (Protocol in workflow.md)
