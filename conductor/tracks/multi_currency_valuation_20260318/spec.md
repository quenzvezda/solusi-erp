# Specification: Multi-Currency COGS & UOM Conversion

## Overview
This track implements the core engine for inventory valuation and multi-unit support. It ensures that every stock movement is tracked with high precision for both quantity (UOM Conversion) and cost (FIFO Valuation Layers in Multi-Currency).

## Functional Requirements

### 1. Multi-Currency Data Structure
- **CurrencyAmount (Embeddable)**: Standard JPA class to store financial values.
  - `currencyId` (FK to Currency)
  - `exchangeRate` (Rate to Base Currency)
  - `originalAmount` (Value in transaction currency)
  - `localAmount` (Value in base currency)

### 2. UOM Conversion Management
- **ProductUomConversion (Entity)**: Stores factors to convert units to the product's Base UOM.
- **Mandate**: All `StockBalance` and `InventoryMovement` quantities MUST be stored in the product's **Base UOM**.

### 3. FIFO Valuation Engine
- **ValuationLayer (Entity)**: Tracks the cost of remaining stock layers.
  - `productId`, `containerId`, `serialNumber` (Optional).
  - `initialQuantity`, `remainingQuantity`.
  - `unitCost` (using `CurrencyAmount`).
- **Logic**:
  - **Inbound (Receipt/Adjustment+)**: Create a new layer.
  - **Outbound (Issue/Adjustment-)**: Reduce `remainingQuantity` of the oldest layer(s) for that product/container.
  - **Edge Case: Insufficient Stock**: If requested quantity exceeds total `remainingQuantity` across all layers, throw a specialized `InsufficientStockException`.
  - **Edge Case: Multiple Layers**: If a single issue transaction spans across multiple layers, the system must accurately calculate the total HPP by summing the fractional costs from each layer.
  - **Edge Case: Zero Cost**: Support layers with 0 cost (e.g., sample/bonus items).
  - **Edge Case: Returns**: Sales returns should create a new valuation layer using the cost from the original issue transaction to maintain accurate FIFO history.

### 4. Entity & Service Updates
- **InventoryMovement**: Add `unitCost` (CurrencyAmount) to log the cost at the time of transaction.
- **StockService**:
  - Integrate UOM conversion before processing adjustments.
  - Automate the creation and consumption of valuation layers.

## Non-Functional Requirements
- **Data Integrity**: All updates (Balance, Layers, Movements) must be atomic.
- **Precision**: Exchange rates use 6 decimal places; quantities and amounts use 4.

## Acceptance Criteria
- Buying 5 Boxes (factor 10) creates a `StockBalance` of 50 Pieces and a `ValuationLayer` of 50 Pieces.
- Selling 10 Pieces correctly calculates HPP based on the oldest purchase layer.
- `InventoryMovement` stores the original purchase price in foreign and local currency.
- All edge cases (insufficient stock, multi-layer split, returns) are validated by unit tests.
