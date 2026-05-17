# Dynamic Accounting Schema (Level 2)

**Date**: 2026-05-03
**Status**: Concept Defined -> Ready for Implementation Plan
**Goal**: Refactor `AccountingSchema` to be fully dynamic (Formula-Driven), allowing users to define custom journal line rules mapping specific transaction amounts (Variables) to specific Chart of Accounts (COA) based on the Event Type.

## Core Architectural Decisions

### 1. The Relationship: Event Type -> Variables -> Schema Lines
- **SchemaEventType (The "When"):** Represents the business transaction occurring (e.g., `GOODS_RECEIPT`, `VENDOR_BILL`).
- **JournalVariable (The "What"):** Fixed system enums representing the specific numeric values available during a particular event (e.g., `GR_SUBTOTAL`, `GR_TAX_AMOUNT`). 
    - *Why Fixed Enum, not CRUD?* Because the backend Java code (e.g., `CompleteGoodsReceiptUseCase`) is hard-coded to produce these specific numbers. The system must know exactly what values are being passed into the journal engine.
- **SchemaLine (The User Definition):** The user creates lines mapping a `JournalVariable` to a `COA Account` and a `Position` (Debit/Credit).

### 2. Handling Zero-Value Lines
- **Decision:** The system will automatically **skip (ignore)** any schema line where the evaluated variable amount is `0` or `null`.
- **Reasoning:** This is the industry standard (e.g., SAP, Odoo). It prevents polluting the general ledger with zero-value entries (e.g., a tax line for a non-taxable transaction) while allowing a single, generic schema to handle both taxable and non-taxable scenarios gracefully.

### 3. Schema Validation Strategy (Option B - Simulation)
- **Decision:** Implement a backend simulation endpoint and a frontend validation mechanism.
- **Mechanism:** Before saving an `AccountingSchema`, the UI will require the user to "Test" the schema. The UI will send dummy values for the event's variables to a simulation endpoint. The backend will calculate the lines and verify if Total Debit == Total Credit.
- **Why Option B?** It prevents runtime transaction failures caused by user configuration errors. The implementation is straightforward: a simple API that runs the schema logic in memory without saving to the database.

## Data Model Refactoring

### 1. `AccountingSchema` (Header)
```java
public class AccountingSchema {
    private Long id;
    private SchemaEventType eventType;
    private String description;
    private Boolean isActive;
    // Removed: debitAccountId, creditAccountId, taxAccountId
    private List<AccountingSchemaLine> lines; 
}
```

### 2. `AccountingSchemaLine` (Detail)
```java
public class AccountingSchemaLine {
    private Long id;
    private Long schemaId;
    private JournalVariable variable; // Enum e.g., GR_INVENTORY_AMT, GR_TAX_AMT
    private Long accountId;           // Reference to COA
    private JournalPosition position; // Enum: DEBIT, CREDIT
}
```

### 3. `JournalVariable` Enum Concept
Variables must be scoped to their respective events.
```java
public enum JournalVariable {
    // Goods Receipt Variables
    GR_INVENTORY_AMT(SchemaEventType.GOODS_RECEIPT),
    GR_TAX_AMT(SchemaEventType.GOODS_RECEIPT),
    GR_GRAND_TOTAL(SchemaEventType.GOODS_RECEIPT),
    
    // Future: Stock Adjustment Variables
    SA_ADJUSTMENT_AMT(SchemaEventType.STOCK_ADJUSTMENT);
    
    private final SchemaEventType supportedEvent;
    // ...
}
```

## Next Steps
1. Create a detailed implementation plan (`docs/superpowers/plans/YYYY-MM-DD-dynamic-accounting-schema-implementation.md`).
2. Rollback or refactor the current hardcoded schema implementation.
3. Implement the Header-Detail UI for schema creation with dynamic variable dropdowns based on Event Type.
4. Build the simulation/validation endpoint.