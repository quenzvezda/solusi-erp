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
10. [Integration with AutoJournalService](#10-integration-with-autojournalservice)

---

## 1. Module Overview

The **Accounting Schema** module is the auto-journaling configuration hub of Solusi ERP. It maintains a table of mappings that associates each operational business event (e.g. Goods Receipt, Vendor Payment) with a specific **debit account** and **credit account** from the Chart of Accounts (COA).

Whenever a business event fires in any other module, the `AutoJournalService` performs a lookup against this table to determine which accounts to post to — without any hardcoded account logic in application code.

Key characteristics:

- **One active record per event type** — uniqueness is enforced at the database level.
- **Configurable by business users** — account assignments can change without a code deployment.
- **Soft-delete by deactivation** — historical mappings are preserved for audit purposes.
- **Postable accounts only** — both debit and credit accounts must be leaf (non-header) COA entries.

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
    private Long debitAccountId;          // FK → acc_chart_of_accounts.id
    private Long creditAccountId;         // FK → acc_chart_of_accounts.id
    private Boolean isActive;
}
```

| Field | Type | Notes |
|---|---|---|
| `id` | `Long` | System-generated surrogate key |
| `eventType` | `SchemaEventType` | Immutable; identifies the business event |
| `description` | `String` | Optional human-readable label |
| `debitAccountId` | `Long` | Must reference a postable (non-header) COA account |
| `creditAccountId` | `Long` | Must reference a postable (non-header) COA account |
| `isActive` | `Boolean` | `true` = in use; `false` = soft-deleted |
| `metadata` | `AuditMetadata` | `createdBy`, `createdDate`, `updatedBy`, `updatedDate`, `version` |

**Factory method:**

```java
AccountingSchema.createNew(eventType, description, debitAccountId, creditAccountId, isActive)
```

**Mutation methods:**

```java
schema.update(description, debitAccountId, creditAccountId, isActive);
schema.softDelete(); // sets isActive = false
```

> `eventType` is **immutable**. To reassign accounts for a different event, create a new schema record.

---

### 4.2 Enum: `SchemaEventType`

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

**Table:** `acc_accounting_schemas`
**Migration:** `V43__Add_Accounting_Foundation.sql`

```sql
CREATE TABLE acc_accounting_schemas (
    id                  BIGINT       NOT NULL AUTO_INCREMENT,
    event_type          VARCHAR(50)  NOT NULL COMMENT 'e.g. GOODS_RECEIPT, VENDOR_BILL',
    description         VARCHAR(255) NULL,
    debit_account_id    BIGINT       NOT NULL,
    credit_account_id   BIGINT       NOT NULL,
    is_active           BOOLEAN      NOT NULL DEFAULT TRUE,
    version             INT          NOT NULL DEFAULT 0,
    created_by_user_id  BIGINT       NULL,
    created_date        DATETIME     NULL,
    updated_by_user_id  BIGINT       NULL,
    updated_date        DATETIME     NULL,

    PRIMARY KEY (id),

    -- Ensures only ONE active mapping per event type
    UNIQUE KEY uk_schema_event_active (event_type, is_active),

    CONSTRAINT fk_schema_debit
        FOREIGN KEY (debit_account_id)  REFERENCES acc_chart_of_accounts(id),
    CONSTRAINT fk_schema_credit
        FOREIGN KEY (credit_account_id) REFERENCES acc_chart_of_accounts(id)
);
```

### Column Reference

| Column | Type | Nullable | Notes |
|---|---|---|---|
| `id` | `BIGINT` | No | Auto-increment primary key |
| `event_type` | `VARCHAR(50)` | No | Matches `SchemaEventType` enum name |
| `description` | `VARCHAR(255)` | Yes | Free-text label |
| `debit_account_id` | `BIGINT` | No | FK to `acc_chart_of_accounts` |
| `credit_account_id` | `BIGINT` | No | FK to `acc_chart_of_accounts` |
| `is_active` | `BOOLEAN` | No | `true` = active; `false` = deactivated |
| `version` | `INT` | No | Optimistic locking counter |
| `created_by_user_id` | `BIGINT` | Yes | Audit: creator user ID |
| `created_date` | `DATETIME` | Yes | Audit: creation timestamp |
| `updated_by_user_id` | `BIGINT` | Yes | Audit: last modifier user ID |
| `updated_date` | `DATETIME` | Yes | Audit: last modification timestamp |

### Key Constraint

```
UNIQUE KEY uk_schema_event_active (event_type, is_active)
```

This constraint enforces that **at most one active record exists per event type**. When a schema is soft-deleted (`is_active = false`), the uniqueness no longer applies for that pair, so a new active record for the same event type can be created. Multiple inactive (historical) records for the same event type are permitted.

---

## 6. Business Rules

### Rule 1 — Unique active schema per event type

Only **one active schema** is allowed per `event_type` at any time. This is enforced by the database unique key `uk_schema_event_active (event_type, is_active)`. Attempting to create a second active schema for the same event type will raise a constraint violation.

### Rule 2 — Both accounts must be postable

The `debit_account_id` and `credit_account_id` must each reference a COA account where `is_header = false`. Header accounts exist only for structural grouping and cannot receive journal postings. This check is enforced at the application layer before persistence.

### Rule 3 — Soft delete, never hard delete

Deactivating a schema (`is_active = false`) is the only deletion mechanism. The record is retained in the database permanently to preserve a complete audit trail of which accounts were used for auto-journaling at any point in time. Physical row deletion is prohibited.

### Rule 4 — Auto-journal lookup at runtime

When a business event fires, `AutoJournalService` queries `findActiveByEventType(eventType)`. If no active schema is found, the operation is halted with a domain exception. This design makes the absence of a schema configuration a hard, explicit failure rather than a silent posting error.

### Rule 5 — Immutable event type

The `event_type` of an existing schema record cannot be modified. If the event type needs to be changed, the old schema must be deactivated and a new one created. This preserves referential integrity between historical journal entries and the schema that generated them.

---

## 7. Standard Schema Mappings

The following mappings are the baseline configuration seeded with each Solusi ERP installation. Account codes reference the standard Chart of Accounts.

| Event Type | Debit Account | Credit Account | Sprint |
|---|---|---|---|
| `GOODS_RECEIPT` | 1131 — Merchandise Inventory | 2150 — GR/IR Clearing | Sprint 4 |
| `VENDOR_BILL` | 2150 — GR/IR Clearing | 2100 — Accounts Payable | Sprint 5 |
| `VENDOR_PAYMENT` | 2100 — Accounts Payable | 1112 — Bank Account | Sprint 5 |
| `PURCHASE_RETURN` | 2150 — GR/IR Clearing | 1131 — Merchandise Inventory | Sprint 5 |
| `STOCK_ADJUSTMENT_IN` | 1131 — Merchandise Inventory | 4140 — Inventory Adjustment Gain | Sprint 6 |
| `STOCK_ADJUSTMENT_OUT` | 5140 — Inventory Adjustment Loss | 1131 — Merchandise Inventory | Sprint 6 |

> O2C events (`CUSTOMER_INVOICE`, `GOODS_ISSUE`, `CUSTOMER_RECEIPT`) are reserved for the Order-to-Cash sprint and require manual configuration once the corresponding COA accounts are established.

---

## 8. Use Cases

### 8.1 Command Use Cases

| Use Case | Description | Input | Outcome |
|---|---|---|---|
| `CreateAccountingSchemaUseCase` | Creates a new active schema mapping | `eventType`, `description`, `debitAccountId`, `creditAccountId`, `isActive` | New `AccountingSchema` persisted |
| `UpdateAccountingSchemaUseCase` | Updates description and/or account assignments | Schema `id`, `description`, `debitAccountId`, `creditAccountId`, `isActive` | Schema updated in place |
| `DeleteAccountingSchemaUseCase` | Soft-deletes a schema by setting `isActive = false` | Schema `id` | Schema deactivated; record retained |

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

## 10. Integration with AutoJournalService

The `AutoJournalService` (scheduled for implementation in **Sprint 6**) is the primary consumer of Accounting Schema data. It uses the schema table as a runtime lookup to resolve accounts for each automated journal entry.

### Lookup Contract

```java
// AutoJournalService — pseudocode
AccountingSchema schema = schemaRepository
    .findActiveByEventType(eventType)
    .orElseThrow(() -> new DomainException(
        "No active accounting schema configured for event: " + eventType));

// Use resolved accounts to build the journal entry:
// DR: schema.getDebitAccountId()   → amount
// CR: schema.getCreditAccountId()  → amount
```

### Integration Flow

```
Business Event Fires (e.g. Goods Receipt posted)
        │
        ▼
AutoJournalService.process(eventType, amount, reference)
        │
        ▼
AccountingSchemaRepository.findActiveByEventType(GOODS_RECEIPT)
        │
        ├─ Found   → Build JournalEntry(DR: debitAccountId, CR: creditAccountId, amount)
        │                    │
        │                    └─ Persist via JournalEntryRepository
        │
        └─ Not Found → throw DomainException (blocks the originating operation)
```

### Design Benefits

| Benefit | Description |
|---|---|
| **Zero hardcoded accounts** | `AutoJournalService` contains no account codes; all routing is data-driven |
| **Runtime reconfiguration** | Business users can update mappings without a code deployment |
| **Auditability** | Every journal entry can be traced back to the schema record that was active at the time of posting |
| **Fail-fast** | Missing schema configuration surfaces immediately as a hard error, preventing silent mis-postings |

---

*Last updated: Sprint 3 — Accounting Foundation*
*Owner: Accounting Module Team*
