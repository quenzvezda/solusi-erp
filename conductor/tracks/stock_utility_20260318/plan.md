# Implementation Plan: Standardized Stock Management Utility

## Phase 1: Database & Entities [x]
- [x] Task: Create Flyway migration script for `inv_stock_balances` and `inv_movements`.
    - [x] Sub-task: Define `inv_stock_balances` with `product_id`, `container_id`, `quantity`, `reserved_quantity`, and unique constraints.
    - [x] Sub-task: Define `inv_movements` with `transaction_date`, `product_id`, `container_id`, `quantity`, `movement_type`, `reference_type`, `reference_id`, `reference_code`, and audit columns.
- [x] Task: Create JPA Entities for `StockBalance` and `InventoryMovement`.
    - [x] Sub-task: Create `StockBalance entity extending BaseModel`.
    - [x] Sub-task: Create `InventoryMovement entity extending BaseModel`.
- [x] Task: Create Enums `MovementType` and `ReferenceType`.
- [x] Task: Conductor - User Manual Verification 'Database & Entities' (Protocol in workflow.md)

## Phase 2: DTOs & Repositories [x]
- [x] Task: Create `StockMovementPayload` DTO.
    - [x] Sub-task: Define fields: `productId`, `containerId`, `quantity` (BigDecimal), `movementType`, `referenceType`, `referenceId`, `referenceCode`, `netPrice`.
- [x] Task: Create JPA Repositories.
    - [x] Sub-task: Create `StockBalanceRepository` with method to find by product and container.
    - [x] Sub-task: Create `InventoryMovementRepository`.
- [x] Task: Conductor - User Manual Verification 'DTOs & Repositories' (Protocol in workflow.md)

## Phase 3: Core Stock Utility Implementation (TDD) [x]
- [x] Task: Initialize Unit Testing Infrastructure.
    - [x] Sub-task: Create `StockServiceTest` class using JUnit 5 and Mockito.
    - [x] Sub-task: Write failing test: `shouldThrowExceptionIfStockBecomesNegative`.
    - [x] Sub-task: Write failing test: `shouldIncreaseOnHandAndAvailableForReceipt`.
    - [x] Sub-task: Write failing test: `shouldDecreaseAvailableAndIncreaseReservedForReserve`.
- [x] Task: Implement `StockService`.
    - [x] Sub-task: Implement `@Transactional` method `adjust(StockMovementPayload payload)`.
    - [x] Sub-task: Implement logic to update `StockBalance` based on `MovementType` (Receipt, Issue, Reserve).
    - [x] Sub-task: Implement strict validation to prevent negative physical stock.
    - [x] Sub-task: Implement logic to create and save `InventoryMovement` log.
- [x] Task: Make tests pass.
    - [x] Sub-task: Run unit tests and refactor `StockService` until all initial tests are green.
- [x] Task: Conductor - User Manual Verification 'Core Stock Utility Implementation' (Protocol in workflow.md)

## Phase 4: Serial Number Foundation [x]
- [x] Task: Create `SerialNumberGenerator` Utility.
    - [x] Sub-task: Write unit test for generating serial number format `SN-YYMM-XXXXX`.
    - [x] Sub-task: Implement generation logic.
- [x] Task: Integrate Serial Generation into `StockService`.
    - [x] Sub-task: Add logic: If `payload.quantity == 1` and product is serialized, auto-generate SN if not provided. (Placeholder logic for future full serialization phase).
- [x] Task: Conductor - User Manual Verification 'Serial Number Foundation' (Protocol in workflow.md)

## Phase 5: Documentation & Roadmap Updates [x]
- [x] Task: Update `docs/roadmap/inventory-module.md`.
    - [x] Sub-task: Add "Expiration Tracking", "Lot/Batch Management", and "Cycle Counting/Opname" to future phases.
- [x] Task: Create `docs/modules/inventory/stock-utility.md`.
    - [x] Sub-task: Document how other modules should use `StockService` and `StockMovementPayload`.
    - [x] Sub-task: Document the dynamic routing map concept for reporting.
- [x] Task: Conductor - User Manual Verification 'Documentation & Roadmap Updates' (Protocol in workflow.md)