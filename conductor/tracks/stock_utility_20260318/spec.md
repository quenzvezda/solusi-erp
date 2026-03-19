# Specification: Standardized Stock Management Utility

## Overview
This track focuses on creating a robust and standardized Core Utility (`StockService`) for managing stock adjustments. This utility acts as the single source of truth for all modules (Sales, Procurement, etc.) to mutate stock, ensuring data integrity, strict non-negative constraints, atomic audit trails, and proper foundation for accounting (COGS).

## Functional Requirements
1.  **Core Stock Utility (`StockService`)**:
    -   Handle stock increments and decrements via a standardized contract: `StockMovementPayload`.
    -   Support multiple stock statuses: **On-Hand** (Physical), **Reserved** (Allocated), **Available** (To-Sell), and **In-Transit**.
    -   **Constraint**: Strongly prevent negative physical stock during issue/decrement operations.

2.  **Standardized Payload (`StockMovementPayload`)**:
    -   Must use IDs (`Long productId`, `Long containerId`) instead of full entities for performance and decoupling.
    -   **UoM Rule**: The utility assumes the `quantity` provided is already converted to the product's **Base UoM** by the calling module.
    -   Include: `MovementType` (RECEIPT, ISSUE, RESERVE, etc.), `ReferenceType` (Enum: SO, GR, ADJ), `referenceId`, `referenceCode`, and `netPrice` (for receipts).

3.  **Audit Trail & Dynamic Routing**:
    -   Generate immutable records in `inv_movements` for every adjustment.
    -   Establish a static Map / Routing mechanism (e.g., `Map<ReferenceType, String>`) in the presentation layer to generate dynamic hyperlinks for reports without requiring database JOINs.

4.  **Serial Number & Lot Foundation**:
    -   Differentiate logic for Serialized vs Non-Serialized items.
    -   **FIFO** is strictly enforced for calculating COGS on Non-Serialized items.
    -   Implement an auto-generation mechanism for Serial Numbers during Goods Receipt or manual Positive Stock Adjustment if the user leaves the serial input blank (Format: `SN-YYMM-XXXXX`).

5.  **Unit Testing Initialization**:
    -   Set up unit testing infrastructure using **JUnit 5** and **Mockito**.
    -   Ensure core utility validations (e.g., preventing negative stock) are thoroughly tested.

## Non-Functional Requirements
-   **Performance**: Utilize JPA `getReferenceById` to prevent unnecessary SELECT queries when passing IDs in the payload.
-   **Atomicity**: All operations (balance update, movement log, serial generation) must execute within a single `@Transactional` boundary.

## Acceptance Criteria
-   `StockService` successfully processes `StockMovementPayload`.
-   System throws an exception if an operation results in negative On-Hand stock.
-   `inv_movements` correctly logs all actions with appropriate Enum references.
-   Dynamic report routing structure is defined.
-   Initial JUnit 5 tests pass for standard stock in/out scenarios.