# Accounting Foundation — Sprint 1 Architecture

> **Module package root:** `com.solusi.erp.accounting`
> **Sprint:** 1 — General Ledger Foundation
> **Stack:** Java 21 · Spring Boot 4.0.3 · MariaDB · Flyway · Thymeleaf / Tabler

---

## Table of Contents

1. [Overview](#1-overview)
2. [Module Responsibilities](#2-module-responsibilities)
3. [Dependency Diagram](#3-dependency-diagram)
4. [Full Package Tree](#4-full-package-tree)
5. [Architecture Patterns](#5-architecture-patterns)
6. [Database Schema Summary](#6-database-schema-summary)
7. [Data Flow: Auto-Journal](#7-data-flow-auto-journal)
8. [Flyway Migration History](#8-flyway-migration-history)
9. [Key Design Decisions](#9-key-design-decisions)
10. [Relationships to Upcoming Sprints](#10-relationships-to-upcoming-sprints)

---

## 1. Overview

The **Accounting Foundation** is the prerequisite layer for every financial operation in Solusi ERP.
Before any business transaction can be recorded in the General Ledger, three questions must be answerable:

| Question | Answered by |
|---|---|
| *Which account does this money move into/out of?* | Chart of Accounts |
| *What debit/credit pair applies to this business event?* | Accounting Schema |
| *Is the accounting period currently open for posting?* | Fiscal Year & Period |

Sprint 1 delivers these three modules as a cohesive foundation. They carry no business logic of their
own beyond their own domain rules; their purpose is to be **referenced** by every subsequent sprint
that posts journal entries (Sprints 4, 5, 6 and beyond). Getting the structure right here avoids
costly schema migrations later.

---

## 2. Module Responsibilities

### 2.1 Chart of Accounts (`accounting.coa`)

The Chart of Accounts (COA) is the master list of all ledger accounts used by the company. Every
journal entry debit or credit must point to a leaf-level (non-header) account in this list. The COA
is organised in a self-referential three-level hierarchy — **Group → Sub-group → Detail** — where
only detail accounts (`is_header = false`) may be posted to. Each account carries an `AccountType`
(ASSET, LIABILITY, EQUITY, REVENUE, EXPENSE) from which the `NormalBalance` (DEBIT or CREDIT) is
automatically derived, ensuring the debit/credit convention is consistent across the entire ledger.
Soft-delete (`is_active`) allows accounts to be retired without breaking historical journal lines.

### 2.2 Accounting Schema (`accounting.schema`)

The Accounting Schema is a configuration table that maps each named business event (`SchemaEventType`)
to a dynamic set of journal lines (`AccountingSchemaLine`). It acts as the formula-driven lookup table
consumed by the `AutoJournalService` (Sprint 6+). When an event occurs (e.g., a Goods Receipt), the system
looks up the active schema for that event. The schema contains user-defined rules mapping specific
transaction amounts (`JournalVariable`, e.g., `GR_INVENTORY_AMT`, `GR_TAX_AMT`) to specific COA accounts
and positions (DEBIT/CREDIT).

This dynamic approach replaces hardcoded debit/credit accounts, allowing for extreme flexibility
(e.g., configuring multi-line journals for taxes or discounts without changing application code).
Only one active schema row may exist per event type (unique constraint), enforcing a single source of truth.
To prevent user configuration errors, schemas must pass a mathematical balance simulation before they can be saved.

### 2.3 Fiscal Year & Accounting Period (`accounting.period`)

A `FiscalYear` owns twelve child `AccountingPeriod` entities that are generated automatically at
fiscal-year creation time. Each period progresses through a strict state machine:
`NEVER_OPENED → OPEN → CLOSED`, with a privileged reopen path back to `OPEN`. The Period Guard
ensures that no journal entry may be posted unless its transaction date falls within a period whose
status is `OPEN`. This prevents accidental back-dating into closed books and is the runtime
enforcement mechanism relied upon by all posting use cases from Sprint 6 onwards.

---

## 3. Dependency Diagram

```
┌──────────────────────────────────────────────────────────────────┐
│                   ACCOUNTING FOUNDATION (Sprint 1)               │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │ Chart of Accounts (COA)          accounting.coa          │   │
│  │ acc_chart_of_accounts                                    │   │
│  │  • Account code + name + type + normal balance          │   │
│  │  • Hierarchy: Group → Sub-group → Detail (3 levels)     │   │
│  │  • is_header: header accounts cannot be posted to       │   │
│  │  • Soft delete: is_active                               │   │
│  └──────────────┬──────────────────────────────────────────┘   │
│                 │ FK (debit_account_id, credit_account_id)      │
│                 ▼                                                │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │ Accounting Schema                accounting.schema       │   │
│  │ acc_accounting_schemas                                   │   │
│  │  • SchemaEventType → (debit COA id, credit COA id)      │   │
│  │  • Lookup table for AutoJournalService (Sprint 6+)      │   │
│  │  • One active row per event type (unique constraint)    │   │
│  └──────────────────────────────────────────────────────────┘   │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │ Fiscal Year & Period             accounting.period       │   │
│  │ acc_fiscal_years + acc_accounting_periods                │   │
│  │  • 12 periods auto-generated per fiscal year            │   │
│  │  • State: NEVER_OPENED → OPEN → CLOSED (reopen OK)      │   │
│  │  • Period Guard: journal date must fall in OPEN period  │   │
│  └──────────────────────────────────────────────────────────┘   │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘
            │ Referenced by all future posting sprints
            ▼
┌─────────────────────────────────────────────────────────────────┐
│  Sprint 4  Goods Receipt  → GOODS_RECEIPT schema                │
│            DR Inventory / CR GR/IR Clearing                     │
│                                                                  │
│  Sprint 5  Vendor Bill    → VENDOR_BILL schema                  │
│            DR GR/IR Clearing / CR Accounts Payable              │
│                                                                  │
│  Sprint 6  Manual Journal Entry                                 │
│            AutoJournalService (uses schema lookup)              │
│            Trial Balance   (queries acc_journal_entries)        │
└─────────────────────────────────────────────────────────────────┘
```

**Inter-module port relationships within Sprint 1:**

```
accounting.coa                accounting.schema
  CoaLookupProvider  ◄──────── Create/UpdateSchemaUseCase
  (port interface)             (resolves display names
                                for debit/credit accounts)

  CoaInUseChecker    ◄──────── DeleteCoaUseCase
  (port interface)             (blocks delete if account
                                is referenced in a schema
                                or journal entry)

accounting.period
  FiscalYearInUseChecker ◄──── DeleteFiscalYearUseCase
  (port interface)             (blocks delete if journal
                                entries exist in a period)
```

---

## 4. Full Package Tree

```
com.solusi.erp.accounting
│
├── coa
│   ├── domain
│   │   ├── model
│   │   │   ├── ChartOfAccount.java          (Aggregate Root — pure Java)
│   │   │   ├── AccountType.java             (Enum: ASSET|LIABILITY|EQUITY|REVENUE|EXPENSE)
│   │   │   └── NormalBalance.java           (Enum: DEBIT|CREDIT — derived from AccountType)
│   │   ├── repository
│   │   │   └── CoaRepository.java           (domain port — no JPA)
│   │   └── port
│   │       ├── CoaLookupProvider.java       (cross-module read port)
│   │       └── CoaInUseChecker.java         (cross-module guard port)
│   │
│   ├── application
│   │   └── usecase
│   │       ├── command
│   │       │   ├── CreateCoaUseCase.java
│   │       │   ├── CreateCoaUseCaseImpl.java
│   │       │   ├── UpdateCoaUseCase.java
│   │       │   ├── UpdateCoaUseCaseImpl.java
│   │       │   ├── DeleteCoaUseCase.java
│   │       │   └── DeleteCoaUseCaseImpl.java
│   │       └── query
│   │           ├── FindCoaUseCase.java
│   │           ├── FindCoaUseCaseImpl.java
│   │           ├── GetCoaEditViewUseCase.java
│   │           ├── GetCoaEditViewUseCaseImpl.java
│   │           ├── GetCoaLookupUseCase.java
│   │           └── GetCoaLookupUseCaseImpl.java
│   │
│   ├── infrastructure
│   │   ├── persistence
│   │   │   ├── ChartOfAccount.java          (JPA Entity — extends BaseModel)
│   │   │   ├── CoaJpaRepository.java        (Spring Data JPA)
│   │   │   └── CoaPersistenceMapper.java    (Entity ↔ Domain model)
│   │   ├── adapter
│   │   │   ├── CoaRepositoryImpl.java       (implements CoaRepository)
│   │   │   ├── CoaLookupProviderImpl.java   (implements CoaLookupProvider)
│   │   │   └── CoaInUseCheckerImpl.java     (implements CoaInUseChecker)
│   │   └── config
│   │       └── CoaConfig.java              (Composition Root — @Configuration)
│   │
│   └── web
│       ├── controller
│       │   ├── CoaController.java           (CRUD + list views)
│       │   └── CoaLookupController.java     (AJAX lookup endpoint)
│       ├── dto
│       │   ├── CoaSaveRequest.java
│       │   ├── CoaDetailResponse.java
│       │   └── CoaSummaryResponse.java
│       └── mapper
│           └── CoaWebMapper.java
│
├── schema
│   ├── domain
│   │   ├── model
│   │   │   ├── AccountingSchema.java        (Aggregate Root — pure Java)
│   │   │   └── SchemaEventType.java         (Enum: GOODS_RECEIPT|VENDOR_BILL|
│   │   │                                     VENDOR_PAYMENT|CUSTOMER_INVOICE|
│   │   │                                     GOODS_ISSUE|CUSTOMER_RECEIPT|
│   │   │                                     STOCK_ADJUSTMENT_IN|STOCK_ADJUSTMENT_OUT)
│   │   ├── repository
│   │   │   └── SchemaRepository.java
│   │   └── port
│   │       └── SchemaInUseChecker.java
│   │
│   ├── application
│   │   └── usecase
│   │       ├── command
│   │       │   ├── CreateSchemaUseCase(Impl)
│   │       │   ├── UpdateSchemaUseCase(Impl)
│   │       │   └── DeleteSchemaUseCase(Impl)
│   │       └── query
│   │           ├── FindSchemasUseCase(Impl)
│   │           └── GetSchemaEditViewUseCase(Impl)
│   │
│   ├── infrastructure
│   │   ├── persistence
│   │   │   ├── AccountingSchema.java
│   │   │   ├── SchemaJpaRepository.java
│   │   │   └── SchemaPersistenceMapper.java
│   │   ├── adapter
│   │   │   ├── SchemaRepositoryImpl.java
│   │   │   └── SchemaInUseCheckerImpl.java
│   │   └── config
│   │       └── SchemaConfig.java            (Composition Root)
│   │
│   └── web
│       ├── controller
│       │   └── SchemaController.java
│       ├── dto
│       │   ├── SchemaSaveRequest.java
│       │   ├── SchemaDetailResponse.java
│       │   └── SchemaSummaryResponse.java
│       └── mapper
│           └── SchemaWebMapper.java
│
└── period
    ├── domain
    │   ├── model
    │   │   ├── FiscalYear.java              (Aggregate Root — pure Java)
    │   │   ├── AccountingPeriod.java        (Entity within FiscalYear aggregate)
    │   │   └── PeriodStatus.java            (Enum: NEVER_OPENED|OPEN|CLOSED)
    │   ├── repository
    │   │   └── FiscalYearRepository.java
    │   └── port
    │       ├── FiscalYearInUseChecker.java      (cross-module guard port)
    │       └── OpenAccountingPeriodLookup.java  (period guard lookup port)
    │
    ├── application
    │   └── usecase
    │       ├── command
    │       │   ├── CreateFiscalYearUseCase(Impl)
    │       │   ├── UpdateFiscalYearUseCase(Impl)
    │       │   ├── DeleteFiscalYearUseCase(Impl)
    │       │   ├── ClosePeriodUseCase(Impl)
    │       │   └── ReopenPeriodUseCase(Impl)
    │       └── query
    │           ├── FindFiscalYearsUseCase(Impl)
    │           ├── GetFiscalYearDetailUseCase(Impl)
    │           └── EnsureOpenPeriodForDateUseCase(Impl)   (Period Guard query)
    │
    ├── infrastructure
    │   ├── persistence
    │   │   ├── FiscalYear.java
    │   │   ├── AccountingPeriod.java
    │   │   ├── FiscalYearJpaRepository.java
    │   │   ├── AccountingPeriodJpaRepository.java
    │   │   ├── FiscalYearPersistenceMapper.java
    │   │   └── PeriodPersistenceMapper.java
    │   ├── adapter
    │   │   ├── FiscalYearRepositoryImpl.java
    │   │   ├── FiscalYearInUseCheckerImpl.java
    │   │   └── OpenAccountingPeriodLookupImpl.java
    │   └── config
    │       └── PeriodConfig.java            (Composition Root)
    │
    └── web
        ├── controller
        │   └── PeriodController.java
        ├── dto
        │   ├── FiscalYearSaveRequest.java
        │   ├── FiscalYearDetailResponse.java
        │   ├── FiscalYearSummaryResponse.java
        │   └── PeriodResponse.java
        └── mapper
            └── PeriodWebMapper.java
```

---

## 5. Architecture Patterns

### 5.1 Pure Domain Model (No Framework Annotations)

Every aggregate root and entity in `domain/model/` is **100% plain Java**. No `@Entity`, `@Column`,
`@Table`, or Spring annotations appear in the domain layer.

```
domain/model/ChartOfAccount.java   ← pure Java, business rules only
        │
        │  mapped by
        ▼
infrastructure/persistence/ChartOfAccount.java  ← @Entity, @Table, etc.
        │
        │  translated by
        ▼
infrastructure/persistence/CoaPersistenceMapper.java  ← toDomain() / toEntity()
```

**Why:** The domain model can be unit-tested without a Spring context or database. Business rule
changes never require JPA annotation changes, and JPA schema optimisations never pollute domain
logic.

The JPA entity in `infrastructure/persistence/` extends the project-wide `BaseModel`, which
contributes `id`, `version`, `createdBy`, `createdDate`, `updatedBy`, `updatedDate` at the
persistence layer. The equivalent audit data is encapsulated in the domain by `AuditMetadata`
(see §5.5).

---

### 5.2 Composition Root Pattern

Each module's `infrastructure/config/XxxConfig.java` is the **single wiring point** for that
module. It is annotated `@Configuration` and manually instantiates every use case as a `@Bean`,
wrapping them in `TransactionTemplate` where transactionality is required.

```java
// Illustrative — CoaConfig.java
@Configuration
public class CoaConfig {

    @Bean
    public CreateCoaUseCase createCoaUseCase(
            CoaRepository coaRepository,
            TransactionTemplate tx) {
        return new CreateCoaUseCaseImpl(coaRepository, tx);
    }

    // ... other use case beans
}
```

**Consequences:**
- No `@Service` or `@Transactional` on use case implementations — transaction boundaries are
  explicit and visible in one place.
- Adding a new dependency to a use case is a compile-time change in `Config.java`, not a hidden
  Spring magic injection.
- The entire module's object graph is readable in one file.

---

### 5.3 CQRS Separation

Commands and queries are split into separate use case hierarchies within each module's
`application/usecase/` layer.

```
application/usecase/
├── command/          ← state-changing operations
│   ├── CreateCoaUseCase(Impl)    — validates, creates domain object, persists
│   ├── UpdateCoaUseCase(Impl)    — loads aggregate, applies change, saves
│   └── DeleteCoaUseCase(Impl)    — checks CoaInUseChecker, soft-deletes
└── query/            ← read-only operations
    ├── FindCoaUseCase(Impl)      — list / search (may query JPA directly)
    ├── GetCoaEditViewUseCase(Impl) — fetch single record for edit form
    └── GetCoaLookupUseCase(Impl) — lightweight projection for dropdowns
```

**Command path:** web controller → command use case → domain aggregate → domain repository port →
persistence adapter → JPA entity → DB.

**Query path:** web controller → query use case → JPA repository (bypasses domain model) → DTO
projection → web response. Queries are optimised for read performance and do not need to
reconstruct the full domain aggregate.

---

### 5.4 Port Interfaces for Cross-Module Communication

When one module needs data or a guard from another module, it defines a **port interface** in its
own `domain/port/` package. The implementing class lives in the *providing* module's
`infrastructure/adapter/` package. Spring wires them together via the Composition Root.

```
accounting.coa.domain.port.CoaLookupProvider      (interface — owned by COA module)
        ▲
        │  implements
accounting.coa.infrastructure.adapter.CoaLookupProviderImpl

        ↑ injected into
accounting.schema.application.usecase.command.CreateSchemaUseCaseImpl
```

This means:
- The Schema module's domain/application layers have **zero import of COA infrastructure**.
- COA can be refactored or replaced without touching Schema application logic.
- The port contract is the only coupling between modules.

**Ports defined in Sprint 1:**

| Port Interface | Owner Module | Implementing Adapter | Consumed By |
|---|---|---|---|
| `CoaLookupProvider` | `accounting.coa` | `CoaLookupProviderImpl` | Schema write/read use cases |
| `CoaInUseChecker` | `accounting.coa` | `CoaInUseCheckerImpl` | `DeleteCoaUseCaseImpl` |
| `FiscalYearInUseChecker` | `accounting.period` | `FiscalYearInUseCheckerImpl` | `DeleteFiscalYearUseCaseImpl` |

---

### 5.5 AuditMetadata Value Object

All domain aggregate roots and entities carry audit fields via a shared `AuditMetadata` value
object rather than inheriting from a base class or repeating fields.

```java
// Conceptual structure
public final class AuditMetadata {
    private final Long id;
    private final Integer version;
    private final String createdBy;
    private final Instant createdDate;
    private final String updatedBy;
    private final Instant updatedDate;
}

public class ChartOfAccount {          // Aggregate Root
    private final AuditMetadata audit;
    private String code;
    private String name;
    // ... business fields
}
```

**Benefits:**
- Audit concern is expressed as a first-class domain concept, not a persistence concern.
- The domain model is immutable-friendly: reconstructing an aggregate from persistence creates a
  new `AuditMetadata` without mutating the domain object.
- Persistence mapper translates `BaseModel` fields (from JPA entity) into/from `AuditMetadata`
  in one place.

---

## 6. Database Schema Summary

All tables are created by Flyway migration `V43` (see §8).

| Table | Module | Typical Row Count | Key Constraints |
|---|---|---|---|
| `acc_chart_of_accounts` | COA | 50–200 | `UNIQUE (code)`; self-ref FK `parent_id → id`; `is_header TINYINT(1)`; `is_active TINYINT(1)` |
| `acc_accounting_schemas` | Schema | 8–10 | `UNIQUE (event_type, is_active)`; FK `debit_account_id → acc_chart_of_accounts.id`; FK `credit_account_id → acc_chart_of_accounts.id` |
| `acc_fiscal_years` | Period | 1–5 | `UNIQUE (code)`; `is_active TINYINT(1)` |
| `acc_accounting_periods` | Period | 12–60 | `UNIQUE (code)`; FK `fiscal_year_id → acc_fiscal_years.id`; `period_number INT`; `status ENUM('NEVER_OPENED','OPEN','CLOSED')` |

**Column highlights:**

`acc_chart_of_accounts`
```
id               BIGINT PK AUTO_INCREMENT
parent_id        BIGINT NULL → acc_chart_of_accounts.id
code             VARCHAR(20) NOT NULL UNIQUE
name             VARCHAR(100) NOT NULL
account_type     ENUM('ASSET','LIABILITY','EQUITY','REVENUE','EXPENSE')
normal_balance   ENUM('DEBIT','CREDIT')
is_header        TINYINT(1) DEFAULT 0
is_active        TINYINT(1) DEFAULT 1
-- + BaseModel audit columns
```

`acc_accounting_schemas`
```
id                BIGINT PK AUTO_INCREMENT
event_type        VARCHAR(50) NOT NULL
debit_account_id  BIGINT NOT NULL → acc_chart_of_accounts.id
credit_account_id BIGINT NOT NULL → acc_chart_of_accounts.id
is_active         TINYINT(1) DEFAULT 1
description       VARCHAR(255)
-- + BaseModel audit columns
UNIQUE KEY uk_schema_event_active (event_type, is_active)
```

`acc_fiscal_years`
```
id          BIGINT PK AUTO_INCREMENT
code        VARCHAR(20) NOT NULL UNIQUE
name        VARCHAR(100)
start_date  DATE NOT NULL
end_date    DATE NOT NULL
is_active   TINYINT(1) DEFAULT 1
-- + BaseModel audit columns
```

`acc_accounting_periods`
```
id             BIGINT PK AUTO_INCREMENT
fiscal_year_id BIGINT NOT NULL → acc_fiscal_years.id
code           VARCHAR(30) NOT NULL UNIQUE
name           VARCHAR(100)
period_number  INT NOT NULL
start_date     DATE NOT NULL
end_date       DATE NOT NULL
status         ENUM('NEVER_OPENED','OPEN','CLOSED') DEFAULT 'NEVER_OPENED'
-- + BaseModel audit columns
```

---

## 7. Data Flow: Auto-Journal

This section traces how a **Goods Receipt** confirmation in Sprint 4 uses all three Sprint 1
modules to generate a correctly formed journal entry.

```
Sprint 4: GoodsReceiptConfirmUseCase
│
│  Step 1 — PERIOD GUARD
│  ├─► EnsureOpenPeriodForDateUseCase (accounting.period)
│  │       SELECT * FROM acc_accounting_periods
│  │       WHERE status = 'OPEN'
│  │         AND start_date <= :receiptDate
│  │         AND end_date   >= :receiptDate
│  │
│  │   If no OPEN period → throw PeriodNotOpenException → rollback
│  │   If OPEN period found → periodId captured for journal header
│  │
│  Step 2 — SCHEMA LOOKUP
│  ├─► AutoJournalService (Sprint 6, accounting.journal)
│  │       SELECT * FROM acc_accounting_schemas
│  │       WHERE event_type = 'GOODS_RECEIPT'
│  │         AND is_active   = 1
│  │
│  │   Returns: debit_account_id  = <Inventory account id>
│  │            credit_account_id = <GR/IR Clearing account id>
│  │
│  Step 3 — COA VALIDATION
│  ├─► CoaLookupProvider (accounting.coa)
│  │       Verify both accounts exist, are active, and are NOT header accounts
│  │       (only leaf accounts with is_header = 0 may be posted to)
│  │
│  Step 4 — JOURNAL ENTRY CREATION
│  └─► JournalEntryCreateUseCase (Sprint 6, accounting.journal)
│           INSERT INTO acc_journal_entries (period_id, date, ...)
│           INSERT INTO acc_journal_lines
│             (journal_id, account_id=<Inventory>,    side='DEBIT',  amount=:cost)
│             (journal_id, account_id=<GR/IR>,        side='CREDIT', amount=:cost)
│
▼
acc_journal_entries  +  acc_journal_lines   (Sprint 6 tables)
```

**Summary of Sprint 1 module involvement:**

| Module | Role in auto-journal flow |
|---|---|
| `accounting.period` | Provides Period Guard — confirms the posting date is within an OPEN period |
| `accounting.schema` | Provides account pair lookup — translates event type to debit/credit account IDs |
| `accounting.coa` | Provides account validation — confirms both accounts are active, non-header accounts |

---

## 8. Flyway Migration History

| Migration | Description |
|---|---|
| `V43__Add_Accounting_Foundation.sql` | Creates `acc_chart_of_accounts`, `acc_accounting_schemas`, `acc_fiscal_years`, `acc_accounting_periods`. Inserts permission groups, individual permissions, and grants them to `ROLE_ACCOUNTING` and `ROLE_ADMIN`. |
| `V44__Alter_Period_Status_And_Add_Period_Number.sql` | Adds `period_number INT` column to `acc_accounting_periods`. Changes the default value of `status` from `OPEN` to `NEVER_OPENED`, aligning the column default with the domain state machine (a freshly auto-generated period must be explicitly opened). |
| `V45__Fix_Accounting_Role_Permissions.sql` | Corrects permission grant rows for `ROLE_ADMIN` that were missing or duplicated in V43. Ensures administrators have full CRUD access to all four accounting foundation tables from the moment the migration runs. |

---

## 9. Key Design Decisions

### 9.1 Why a Pure Domain Model with No Framework Annotations?

Framework annotations in domain classes create invisible coupling between business rules and
infrastructure concerns. A `@Entity`-annotated `ChartOfAccount` cannot be tested without loading a
JPA context, and changing a JPA column name forces a diff in the same file that contains business
invariants. By keeping the domain model annotation-free, unit tests for business rules are fast
(`mvn test -pl . -Dtest=ChartOfAccountTest` runs in milliseconds with no Spring context), and the
persistence strategy can be changed without touching domain logic.

### 9.2 Why the Composition Root Pattern Instead of @Service / @Transactional?

`@Transactional` on use case implementations is declarative magic: transaction boundaries are
invisible at the call site, and nesting behaviour depends on Spring proxy internals. The
Composition Root makes transaction management **explicit and auditable**: every use case that
participates in a transaction wraps its logic in a `TransactionTemplate.execute()` call, visible
in one `Config.java` file. Adding a new use case requires a conscious decision about whether it
needs a transaction, rather than inheriting one by default.

### 9.3 Why a Separate Accounting Schema Table Instead of Hardcoded Accounts?

Hardcoding account codes (e.g., `"1010"` for Inventory) in application logic means that any
chart-of-accounts restructuring requires a code deployment. A separate `acc_accounting_schemas`
table means the accounting team can reconfigure event-to-account mappings through the UI without
developer involvement. It also makes the accounting rules explicit, auditable, and testable: a
test can assert `GOODS_RECEIPT maps DR=Inventory CR=GR/IR` by querying the schema table, not by
reading source code.

### 9.4 Why Auto-Generate 12 Periods on Fiscal Year Creation?

Manual period creation is error-prone (gaps between periods break the Period Guard) and offers no
business value — a standard calendar fiscal year always has twelve monthly periods. Generating
them automatically at `FiscalYear` creation time in the domain aggregate means the invariant
"a fiscal year always has exactly twelve non-overlapping contiguous periods" is enforced in the
domain, not in a migration script or an operator checklist.

### 9.5 Why the `NEVER_OPENED` State Instead of Defaulting to `OPEN`?

Defaulting auto-generated periods to `OPEN` would allow posting to future periods immediately
after fiscal year creation, which violates standard accounting practice (period-close controls
exist for a reason). `NEVER_OPENED` as the initial state makes the transition to `OPEN` an
explicit act that can be access-controlled (e.g., only `ROLE_ACCOUNTING_MANAGER` may open a
period), and it distinguishes a period that was opened and then closed from one that was never
used at all — useful for auditing and reporting.

---

## 10. Relationships to Upcoming Sprints

| Sprint | Feature | Dependency on Sprint 1 |
|---|---|---|
| **Sprint 2** | Vendor Master | None (independent) |
| **Sprint 3** | Item & Inventory | None (independent) |
| **Sprint 4** | Goods Receipt | Reads `GOODS_RECEIPT` schema row; Period Guard on receipt confirmation date; COA validation for inventory and GR/IR accounts |
| **Sprint 5** | Vendor Bill & Payment | Reads `VENDOR_BILL` and `VENDOR_PAYMENT` schema rows; Period Guard on bill/payment date; COA validation for AP, GR/IR, and bank accounts |
| **Sprint 6** | Manual Journal Entry | Full dependency on Period Guard (open period required); COA validation (non-header, active account required); `AutoJournalService` iterates all `SchemaEventType` rows to post automated entries |
| **Sprint 6** | Trial Balance | Queries `acc_journal_lines` grouped by COA; needs `acc_chart_of_accounts` for account names, types, and normal balances to calculate debit/credit totals correctly |
| **Sprint 7** | Customer Invoice & Receipt | Reads `CUSTOMER_INVOICE` and `CUSTOMER_RECEIPT` schema rows; Period Guard |
| **Sprint 8** | Stock Adjustment | Reads `STOCK_ADJUSTMENT_IN` / `STOCK_ADJUSTMENT_OUT` schema rows; Period Guard |
| **Sprint 9+** | Financial Reports | P&L and Balance Sheet groupings derived from `account_type` in COA; period filtering via `acc_accounting_periods` |

**The rule of thumb:** any sprint that creates a financial transaction has a hard runtime dependency
on:
1. At least one `SchemaEventType` row being configured in `acc_accounting_schemas` (deploy-time).
2. The target accounting period being in `OPEN` status (runtime operator action).
3. The referenced COA accounts being active, non-header accounts (setup-time).

If any of the three conditions is not met, the use case throws a domain exception and the
transaction is rolled back — no partial ledger entries are created.
