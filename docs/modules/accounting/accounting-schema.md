# Accounting Schema

**Module:** `accounting.schema`
**Layer:** Finance & Accounting → General Ledger
**URL:** `/accounting/schemas`
**Breadcrumb:** Finance & Accounting > General Ledger > Accounting Schema

---

## Table of Contents

1. [Module Overview](#1-module-overview)
2. [The Problem It Solves](#2-the-problem-it-solves)
3. [Package Structure](#3-package-structure)
4. [Domain Model](#4-domain-model)
5. [Database Schema](#5-database-schema)
6. [Business Rules](#6-business-rules)
7. [Standard Schema Mappings](#7-standard-schema-mappings)
8. [Use Cases](#8-use-cases)
9. [RBAC Permissions](#9-rbac-permissions)
10. [Integration with Journal Posting](#10-integration-with-journal-posting)

---

## 1. Module Overview

The **Accounting Schema** module is the auto-journaling configuration hub of Solusi ERP. Each business event (e.g. Goods Receipt, Vendor Payment) is mapped to a set of **schema lines** — each line binds a `JournalVariable` (a named amount slot) to a specific COA account and journal position (DEBIT or CREDIT).

Whenever a business event fires, `PostJournalForEventUseCaseImpl` looks up the active schema for that event, iterates the schema lines, reads the amount for each variable from the caller-supplied map, and builds the journal entry generically — no event-specific posting logic lives in the journal module.

Key characteristics:

- **One active schema per event type** — uniqueness is enforced at the database level.
- **Multiple schema lines per schema** — each line maps a `JournalVariable` to a COA account and a position (DEBIT or CREDIT).
- **Configurable by business users** — account assignments can change without a code deployment.
- **Soft-delete by deactivation** — historical mappings are preserved for audit purposes.
- **Postable accounts only** — each line's account must be a leaf (non-header) COA entry.
- **Variables must match the event type** — each `JournalVariable` declares which event it belongs to; mismatched lines are rejected at save time.

---

## 2. The Problem It Solves

In a naive ERP implementation, the accounts used for automated journal entries are hardcoded inside service classes. This creates several problems:

| Problem | Impact |
|---|---|
| Account codes differ between companies or fiscal periods | Requires code changes and redeployment |
| Chart of Accounts restructuring breaks journal logic | High regression risk |
| Auditors cannot verify which accounts were used for a past event | Audit trail gaps |
| Business users cannot adjust mappings without developer involvement | High operational cost |

The Accounting Schema module eliminates all of these by externalising the debit/credit mapping into a managed configuration table. The auto-journal engine reads this table at runtime, so changes take effect immediately and are fully auditable.

---

## 3. Package Structure

```
accounting.schema
├── domain
│   ├── model
│   │   ├── AccountingSchema.java          ← Aggregate Root (Pure Java, no framework)
│   │   └── SchemaEventType.java           ← Enum of all auto-journal event types
│   ├── repository
│   │   └── AccountingSchemaRepository.java  ← Domain repository interface
│   └── port
│       └── (lookup ports consumed by other modules)
│
├── application
│   └── usecase
│       ├── command
│       │   ├── CreateAccountingSchemaUseCase.java / Impl
│       │   ├── UpdateAccountingSchemaUseCase.java / Impl
│       │   └── DeleteAccountingSchemaUseCase.java / Impl
│       └── query
│           ├── FindAccountingSchemasUseCase.java / Impl
│           └── GetAccountingSchemaEditViewUseCase.java / Impl
│
├── infrastructure
│   ├── persistence
│   │   ├── AccountingSchema.java              ← JPA Entity
│   │   ├── AccountingSchemaJpaRepository.java ← Spring Data JPA interface
│   │   └── AccountingSchemaPersistenceMapper.java
│   ├── adapter
│   │   └── AccountingSchemaRepositoryImpl.java ← Implements domain repository
│   └── config
│       └── AccountingSchemaConfig.java         ← Spring bean wiring
│
└── web
    ├── controller
    │   └── AccountingSchemaController.java     ← Thymeleaf SSR controller
    ├── dto
    │   ├── AccountingSchemaSaveRequest.java     ← Create / update form payload
    │   ├── AccountingSchemaDetailResponse.java  ← Single-record view model
    │   └── AccountingSchemaSummaryResponse.java ← List view model
    └── mapper
        └── AccountingSchemaWebMapper.java
```

> **Architecture note:** The `domain` layer has zero dependencies on Spring or JPA. All framework concerns are isolated to `infrastructure` and `web`. The `application` layer orchestrates domain objects through use case interfaces, following Clean Architecture + CQRS (Command / Query segregation).

---

## 4. Domain Model

### 4.1 Aggregate Root: `AccountingSchema`

```java
public class AccountingSchema {
    private final AuditMetadata metadata; // createdBy, createdDate, updatedBy, updatedDate, version
    private SchemaEventType eventType;    // Immutable after creation
    private String description;
    private Boolean isActive;
    private List<AccountingSchemaLine> lines; // at least one required
}
```

| Field | Type | Notes |
|---|---|---|
| `id` | `Long` | System-generated surrogate key |
| `eventType` | `SchemaEventType` | Immutable; identifies the business event |
| `description` | `String` | Optional human-readable label |
| `isActive` | `Boolean` | `true` = in use; `false` = soft-deleted |
| `lines` | `List<AccountingSchemaLine>` | Ordered list of variable-to-account mappings; must not be empty |
| `metadata` | `AuditMetadata` | `createdBy`, `createdDate`, `updatedBy`, `updatedDate`, `version` |

**Factory method:**

```java
AccountingSchema.createNew(eventType, description, isActive, lines)
```

**Mutation methods:**

```java
schema.update(description, isActive, lines);
schema.softDelete(); // sets isActive = false
```

**Validation (enforced in constructor and `update`):**
- `lines` must not be empty → `msg.error.schema.lines.empty`
- Every `line.variable().getSupportedEvent()` must equal `eventType` → `msg.error.schema.variable.unsupported`

> `eventType` is **immutable**. To reassign accounts for a different event, create a new schema record.

---

### 4.2 Value Object: `AccountingSchemaLine`

```java
public record AccountingSchemaLine(
    Long id,                  // null for new lines; assigned by DB on persist
    JournalVariable variable, // which amount slot this line covers
    Long accountId,           // FK → acc_chart_of_accounts.id (must be postable)
    JournalPosition position  // DEBIT or CREDIT
) {}
```

Each line represents one row in `acc_schema_lines`. When the schema is persisted, all lines are written; when the schema is updated, all existing lines are replaced.

---

### 4.3 Enum: `JournalVariable`

`JournalVariable` is the bridge between the journal posting layer and the schema configuration. Each value names an amount slot and declares which event type it belongs to.

```java
public enum JournalVariable {
    GR_INVENTORY_AMT(SchemaEventType.GOODS_RECEIPT), // inventory value (qty × unit cost)
    GR_TAX_AMT(SchemaEventType.GOODS_RECEIPT),       // input VAT on the purchase
    GR_GRAND_TOTAL(SchemaEventType.GOODS_RECEIPT);   // sum of inventory + tax

    // Additional variables are added here as new event types gain journal support.
}
```

The Schema UI reads `JournalVariable.getVariablesForEvent(eventType)` to populate the variable dropdown for a given schema. Callers (e.g. `CompleteGoodsReceiptUseCaseImpl`) build a `Map<JournalVariable, BigDecimal>` and pass it to `PostJournalForEventUseCaseImpl`, which resolves accounts generically via the schema lines.

---

---

### 4.4 Enum: `SchemaEventType`

```java
public enum SchemaEventType {
    GOODS_RECEIPT,       // DR Inventory         CR GR/IR Clearing     (Sprint 4)
    VENDOR_BILL,         // DR GR/IR Clearing    CR Accounts Payable   (Sprint 5)
    VENDOR_PAYMENT,      // DR Accounts Payable  CR Bank Account       (Sprint 5)
    CUSTOMER_INVOICE,    // DR Accounts Rec.     CR Revenue            (Future O2C)
    GOODS_ISSUE,         // DR COGS              CR Inventory          (Future O2C)
    CUSTOMER_RECEIPT,    // DR Bank Account      CR Accounts Rec.      (Future O2C)
    STOCK_ADJUSTMENT_IN, // DR Inventory         CR Inventory Adj Gain (Sprint 6)
    STOCK_ADJUSTMENT_OUT // DR Inventory Adj Loss CR Inventory         (Sprint 6)
}
```

Each enum value corresponds to exactly one active `AccountingSchema` row at any given time.

---

## 5. Database Schema

**Tables:** `acc_accounting_schemas`, `acc_schema_lines`
**Original migration:** `V43__Add_Accounting_Foundation.sql`
**Refactor migration:** `V56__Refactor_Schema_To_Dynamic_Lines.sql`

```sql
-- Schema header: one row per event type.
CREATE TABLE acc_accounting_schemas (
    id                  BIGINT       NOT NULL AUTO_INCREMENT,
    event_type          VARCHAR(50)  NOT NULL COMMENT 'Matches SchemaEventType enum name',
    description         VARCHAR(255) NULL,
    is_active           BOOLEAN      NOT NULL DEFAULT TRUE,
    version             INT          NOT NULL DEFAULT 0,
    created_by_user_id  BIGINT       NULL,
    created_date        DATETIME     NULL,
    updated_by_user_id  BIGINT       NULL,
    updated_date        DATETIME     NULL,

    PRIMARY KEY (id),
    UNIQUE KEY uk_schema_event_active (event_type, is_active)
);

-- Schema lines: one row per variable-to-account mapping.
CREATE TABLE acc_schema_lines (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    schema_id   BIGINT      NOT NULL,
    variable    VARCHAR(50) NOT NULL COMMENT 'Matches JournalVariable enum name',
    account_id  BIGINT      NOT NULL,
    position    VARCHAR(10) NOT NULL COMMENT 'DEBIT or CREDIT',

    PRIMARY KEY (id),
    CONSTRAINT fk_schema_line_schema  FOREIGN KEY (schema_id)  REFERENCES acc_accounting_schemas(id) ON DELETE CASCADE,
    CONSTRAINT fk_schema_line_account FOREIGN KEY (account_id) REFERENCES acc_chart_of_accounts(id)
);
```

### `acc_accounting_schemas` Column Reference

| Column | Type | Nullable | Notes |
|---|---|---|---|
| `id` | `BIGINT` | No | Auto-increment primary key |
| `event_type` | `VARCHAR(50)` | No | Matches `SchemaEventType` enum name |
| `description` | `VARCHAR(255)` | Yes | Free-text label |
| `is_active` | `BOOLEAN` | No | `true` = active; `false` = deactivated |
| `version` | `INT` | No | Optimistic locking counter |
| `created_by_user_id` | `BIGINT` | Yes | Audit: creator user ID |
| `created_date` | `DATETIME` | Yes | Audit: creation timestamp |
| `updated_by_user_id` | `BIGINT` | Yes | Audit: last modifier user ID |
| `updated_date` | `DATETIME` | Yes | Audit: last modification timestamp |

### `acc_schema_lines` Column Reference

| Column | Type | Nullable | Notes |
|---|---|---|---|
| `id` | `BIGINT` | No | Auto-increment primary key |
| `schema_id` | `BIGINT` | No | FK to `acc_accounting_schemas`; cascades on delete |
| `variable` | `VARCHAR(50)` | No | Matches `JournalVariable` enum name |
| `account_id` | `BIGINT` | No | FK to `acc_chart_of_accounts` (must be postable) |
| `position` | `VARCHAR(10)` | No | `DEBIT` or `CREDIT` |

### Key Constraint

```
UNIQUE KEY uk_schema_event_active (event_type, is_active)
```

This constraint enforces that **at most one active record exists per event type**. When a schema is soft-deleted (`is_active = false`), the uniqueness no longer applies for that pair, so a new active record for the same event type can be created. Multiple inactive (historical) records for the same event type are permitted.

---

## 6. Business Rules

### Rule 1 — Unique active schema per event type

Only **one active schema** is allowed per `event_type` at any time. This is enforced by the database unique key `uk_schema_event_active (event_type, is_active)`. Attempting to create a second active schema for the same event type will raise a constraint violation.

### Rule 2 — Each line's account must be postable

Every `AccountingSchemaLine.accountId` must reference a COA account where `is_header = false`. Header accounts exist only for structural grouping and cannot receive journal postings. This check is enforced at the application layer before persistence.

### Rule 3 — Soft delete, never hard delete

Deactivating a schema (`is_active = false`) is the only deletion mechanism. The record is retained in the database permanently to preserve a complete audit trail of which accounts were used for auto-journaling at any point in time. Physical row deletion is prohibited.

### Rule 4 — Auto-journal lookup at runtime

When a business event fires, `PostJournalForEventUseCaseImpl` queries `findByEventTypeAndIsActiveTrue(eventType)`. If no active schema is found, the operation is halted with a domain exception. This design makes the absence of a schema configuration a hard, explicit failure rather than a silent posting error.

### Rule 5 — Immutable event type

The `event_type` of an existing schema record cannot be modified. If the event type needs to be changed, the old schema must be deactivated and a new one created. This preserves referential integrity between historical journal entries and the schema that generated them.

### Rule 6 — Schema lines must not be empty

A schema must have at least one `AccountingSchemaLine`. Saving a schema with an empty `lines` list is rejected with `msg.error.schema.lines.empty`.

### Rule 7 — Variable must match event type

Each `AccountingSchemaLine.variable` must belong to the same `SchemaEventType` as the parent schema (`variable.getSupportedEvent() == schema.getEventType()`). Mismatched lines are rejected with `msg.error.schema.variable.unsupported`.

---

## 7. Standard Schema Mappings

The following mappings are the baseline configuration seeded with each Solusi ERP installation. Account codes reference the standard Chart of Accounts.

| Event Type | Variable | Account | Position |
|---|---|---|---|
| `GOODS_RECEIPT` | `GR_INVENTORY_AMT` | 1310 — Merchandise Inventory | DEBIT |
| `GOODS_RECEIPT` | `GR_TAX_AMT` | 1230 — Tax Receivable (Input VAT) | DEBIT |
| `GOODS_RECEIPT` | `GR_GRAND_TOTAL` | 2120 — GR/IR Clearing | CREDIT |

`GR_TAX_AMT` lines with a zero value (no tax on the purchase order) are automatically skipped by `PostJournalForEventUseCaseImpl` — the journal entry remains balanced as DR Inventory = CR GR/IR Clearing.

> All other event types (`VENDOR_BILL`, `VENDOR_PAYMENT`, `CUSTOMER_INVOICE`, `GOODS_ISSUE`, `CUSTOMER_RECEIPT`, `STOCK_ADJUSTMENT_IN`, `STOCK_ADJUSTMENT_OUT`) are registered as schema headers without lines. Their `JournalVariable` entries and schema lines will be added in future sprints as those event types gain journal posting support.

---

## 8. Use Cases

### 8.1 Command Use Cases

| Use Case | Description | Input | Outcome |
|---|---|---|---|
| `CreateAccountingSchemaUseCase` | Creates a new active schema mapping | `eventType`, `description`, `isActive`, `lines` (List of variable + accountId + position) | New `AccountingSchema` persisted with its lines |
| `UpdateAccountingSchemaUseCase` | Updates description and/or schema lines | Schema `id`, `description`, `isActive`, `lines` | Schema and all its lines updated in place |
| `DeleteAccountingSchemaUseCase` | Soft-deletes a schema by setting `isActive = false` | Schema `id` | Schema deactivated; record and lines retained |

### 8.2 Query Use Cases

| Use Case | Description | Output |
|---|---|---|
| `FindAccountingSchemasUseCase` | Returns the full list of schemas (active and inactive) for the admin list view | `List<AccountingSchemaSummaryResponse>` |
| `GetAccountingSchemaEditViewUseCase` | Returns a single schema populated with all COA options for the edit form | `AccountingSchemaDetailResponse` |

---

## 9. RBAC Permissions

| Permission | Description |
|---|---|
| `ACCOUNTING-SCHEMA_READ` | View the schema list and individual schema details |
| `ACCOUNTING-SCHEMA_CREATE` | Create a new event-to-account mapping |
| `ACCOUNTING-SCHEMA_UPDATE` | Modify description or account assignments on an existing schema |
| `ACCOUNTING-SCHEMA_DELETE` | Deactivate (soft-delete) a schema |

Permissions are assigned to roles in the standard Solusi ERP RBAC configuration. A user must hold `ACCOUNTING-SCHEMA_READ` to access any endpoint under `/accounting/schemas`.

---

## 10. Integration with Journal Posting

`PostJournalForEventUseCaseImpl` is the primary consumer of Accounting Schema data. It uses the schema lines as a runtime lookup to resolve accounts for each automated journal entry. No event-specific policy classes exist — the generic loop handles all event types uniformly.

### Lookup Contract

```java
// PostJournalForEventUseCaseImpl — simplified
AccountingSchema schema = schemaRepository
    .findByEventTypeAndIsActiveTrue(command.eventType())
    .orElseThrow(() -> new DomainException("msg.error.journal.schema.notfound"));

// Build journal lines by iterating schema lines:
List<JournalLine> lines = schema.getLines().stream()
    .map(schemaLine -> {
        BigDecimal value = command.values()
            .getOrDefault(schemaLine.variable(), BigDecimal.ZERO);
        if (value.compareTo(BigDecimal.ZERO) == 0) return null; // skip zero-value lines
        return schemaLine.position() == JournalPosition.DEBIT
            ? JournalLine.debit(schemaLine.accountId(), value)
            : JournalLine.credit(schemaLine.accountId(), value);
    })
    .filter(Objects::nonNull)
    .toList();

JournalEntry entry = JournalEntry.createPosted(..., lines);
entry.validateBalanced(); // throws if DR total ≠ CR total
journalEntryRepository.save(entry);
```

### Integration Flow

```
Business Event Fires (e.g. Goods Receipt completed)
        │
        ▼
CompleteGoodsReceiptUseCaseImpl builds:
  Map<JournalVariable, BigDecimal> values = Map.of(
      GR_INVENTORY_AMT → inventoryTotal,
      GR_TAX_AMT       → taxTotal,
      GR_GRAND_TOTAL   → inventoryTotal + taxTotal
  )
        │
        ▼
PostJournalForEventUseCaseImpl.execute(JournalPostingCommand)
        │
        ▼
AccountingSchemaRepository.findByEventTypeAndIsActiveTrue(GOODS_RECEIPT)
        │
        ├─ Found   → iterate schema.getLines()
        │             for each line: look up value from command.values()
        │             skip zero-value variables (e.g. GR_TAX_AMT when no tax)
        │             build JournalLine(debit or credit, accountId, value)
        │             validateBalanced() → persist via JournalEntryRepository
        │
        └─ Not Found → throw DomainException (blocks the originating operation)
```

### Adding a New Variable

To add a new journalable amount to an existing event type (e.g. freight cost on Goods Receipt):

1. Add a new `JournalVariable` enum value pointing to `SchemaEventType.GOODS_RECEIPT`
2. In `CompleteGoodsReceiptUseCaseImpl`, add the new variable to the `Map.of(...)` call
3. In the Accounting Schema UI, add a new line for that variable pointing to the correct COA account
4. Run D220 seeder or migrate the production schema accordingly

No changes are required in `PostJournalForEventUseCaseImpl` — the generic loop handles new variables automatically.

### Design Benefits

| Benefit | Description |
|---|---|
| **Zero hardcoded accounts** | `PostJournalForEventUseCaseImpl` contains no account codes; all routing is data-driven |
| **Runtime reconfiguration** | Business users can update account mappings without a code deployment |
| **Extensible** | New event types require only a new `JournalVariable` entry and schema configuration — no new policy classes |
| **Auditability** | Every journal entry can be traced back to the schema record that was active at the time of posting |
| **Fail-fast** | Missing schema configuration surfaces immediately as a hard error, preventing silent mis-postings |

---

*Last updated: Refactor — Dynamic Schema Lines (V56) + Generic Journal Posting*
*Owner: Accounting Module Team*
