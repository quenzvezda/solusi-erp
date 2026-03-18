# Specification: Stock Management Operations & Menu Refactor

## Overview
This track focuses on implementing the user-facing inventory transaction and reporting system. It includes the **Stock Adjustment** document, the **On-Hand Quantity** view, and a **Stock Card** report. It also involves a major refactoring of the **Inventory Management** menu to separate Setup, Transactions, and Reports.

## 1. Menu Refactoring
Reorganize the `Operations` menu into three logical sub-modules:
- **Inventory Setup:** Products, Categories, Brands, UOMs, Warehouse Hierarchy (Facility, Grid, Bin).
- **Inventory Transactions:** Stock Adjustment (`INV-08`).
- **Inventory Reports:** On-Hand Quantity (`INV-10`), Stock Card (`INV-09`).

## 2. Stock Adjustment Document (`INV-08`)
- **Structure:** Header-Detail.
- **Model (Header):**
  - `code`: Auto-generated (e.g., `ADJ-2026-0001`).
  - `transaction_date`: Date of adjustment.
  - `status`: `DRAFT` (default), `COMPLETED`.
  - `note`: Text description.
  - `CurrencyAmount`: (Embeddable) Stores total original amount and total local amount calculated from lines.
- **Model (Lines):**
  - `product_id`, `container_id`, `quantity` (BigDecimal).
  - `unitCost`: Stores the per-unit cost.
  - `totalAmount`: Qty * UnitCost.
  - `serialNumber`: User input or auto-generated if left blank for serialized items.
- **Workflow:**
  - **Draft:** Document can be edited. No stock changes.
  - **Process:** Finalizes the document. Status becomes `COMPLETED` (read-only). Calls `StockService.adjust()` for each line.

## 3. On-Hand Quantity View (`INV-10`)
- **Main View:** Paginated list of all products showing:
  - `Product Code/Name`, `Base UOM`.
  - Aggregated `On-Hand`, `Reserved`, `Available`, `In-Transit`.
- **Detail View (Separate Page):** Breakdown of the selected product's stock per **Facility > Grid > Container**.

## 4. Stock Card Report (`INV-09`)
- **List View:** Chronological log of all `inv_movements`.
- **Filters:** Product, Container, Date Range.
- **Navigation:** Reference codes are clickable links to source documents.

## Acceptance Criteria
- Menu hierarchy follows the Setup/Transactions/Reports split.
- `StockAdjustment` correctly updates physical stock and FIFO layers upon processing.
- `On-Hand Quantity` accurately displays totals and detail breakdowns.
- `Stock Card` provides a clear audit trail of all transactions.
