# Fiscal Year & Accounting Period Module

## Table of Contents

1. [Module Overview](#1-module-overview)
2. [Package Structure](#2-package-structure)
3. [Domain Models](#3-domain-models)
4. [Database Schema](#4-database-schema)
5. [Sequence Generation](#5-sequence-generation)
6. [Business Rules](#6-business-rules)
7. [Period Status Lifecycle](#7-period-status-lifecycle)
8. [Use Cases](#8-use-cases)
9. [RBAC Permissions](#9-rbac-permissions)
10. [Period Guard Pattern](#10-period-guard-pattern)
11. [Auto-Generation Example](#11-auto-generation-example)

---

## 1. Module Overview

The **Fiscal Year & Accounting Period** module is the foundational building block of the Solusi ERP accounting subsystem. It defines the temporal structure within which all financial transactions are recorded.

### What It Does

- Maintains a registry of **Fiscal Years** — each representing one annual accounting cycle (typically 1 January–31 December for Indonesian companies).
- Automatically generates **12 Accounting Periods** (one per calendar month) whenever a new Fiscal Year is created.
- Enforces the **Period Guard**: every journal entry, goods receipt, vendor bill, and any other financial document posted in the system must target an `OPEN` accounting period. Documents cannot be posted to `NEVER_OPENED` or `CLOSED` periods.

### Critical Role: Period Guard

The Period Guard is the accounting integrity mechanism that prevents backdating or future-dating transactions into unauthorized periods. All transactional modules (Sprint 4+) must call the Period Guard before persisting any financial document. This module is therefore a **prerequisite for all downstream accounting and inventory flows**.

### Navigation

**Menu path:** Finance & Accounting › General Ledger › Accounting Period  
**Base URL:** `/accounting/periods`

---

## 2. Package Structure

The module follows **Pure Clean Architecture** with vertical slicing. All classes live under `accounting.period`.

```
accounting.period
├── domain
│   ├── model
│   │   ├── FiscalYear.java               ← Aggregate Root (Pure Java, no framework deps)
│   │   ├── AccountingPeriod.java         ← Entity within FiscalYear aggregate
│   │   └── PeriodStatus.java             ← Enum: NEVER_OPENED | OPEN | CLOSED
│   ├── repository
│   │   └── FiscalYearRepository.java     ← Domain repository interface (port)
│   └── port
│       └── FiscalYearInUseChecker.java   ← Port: checks if FY has posted journals
│
├── application
│   └── usecase
│       ├── command
│       │   ├── CreateFiscalYearUseCase.java / Impl   ← Creates FY + 12 periods
│       │   ├── UpdateFiscalYearUseCase.java / Impl
│       │   ├── DeleteFiscalYearUseCase.java / Impl
│       │   ├── ClosePeriodUseCase.java / Impl
│       │   └── ReopenPeriodUseCase.java / Impl
│       └── query
│           ├── FindFiscalYearsUseCase.java / Impl
│           └── GetFiscalYearDetailUseCase.java / Impl
│
├── infrastructure
│   ├── persistence
│   │   ├── FiscalYear.java                    ← JPA Entity (acc_fiscal_years)
│   │   ├── AccountingPeriod.java              ← JPA Entity (acc_accounting_periods)
│   │   ├── FiscalYearJpaRepository.java       ← Spring Data JPA interface
│   │   ├── AccountingPeriodJpaRepository.java ← Spring Data JPA interface
│   │   ├── FiscalYearPersistenceMapper.java   ← Domain ↔ JPA mapping
│   │   └── PeriodPersistenceMapper.java       ← Domain ↔ JPA mapping
│   ├── adapter
│   │   ├── FiscalYearRepositoryImpl.java      ← Implements domain repository port
│   │   └── FiscalYearInUseCheckerImpl.java    ← Implements in-use checker port
│   └── config
│       └── PeriodConfig.java                 ← Spring @Configuration for this module
│
└── web
    ├── controller
    │   └── PeriodController.java             ← Thymeleaf SSR controller
    ├── dto
    │   ├── FiscalYearSaveRequest.java        ← Form input DTO (create / update)
    │   ├── FiscalYearDetailResponse.java     ← Single FY with period list
    │   ├── FiscalYearSummaryResponse.java    ← Lightweight row for list view
    │   └── PeriodResponse.java              ← Single period row DTO
    └── mapper
        └── PeriodWebMapper.java             ← Domain ↔ Web DTO mapping
```

### Layer Dependency Rules

| Layer | May depend on | Must NOT depend on |
|---|---|---|
| `domain` | Nothing (pure Java) | `application`, `infrastructure`, `web` |
| `application` | `domain` only | `infrastructure`, `web` |
| `infrastructure` | `domain`, `application` | `web` |
| `web` | `application` (via use-case interfaces) | `infrastructure` directly |

---

## 3. Domain Models

### 3.1 `FiscalYear` — Aggregate Root

`FiscalYear` is the aggregate root that owns and controls all `AccountingPeriod` children. No period may be created, closed, or reopened except through the `FiscalYear` aggregate.

```java
public class FiscalYear {
    private final AuditMetadata metadata;
    private String  code;       // Auto-generated: FY-{seq padded 4}, e.g. "FY-0001"
    private String  name;       // Human-readable label, e.g. "Fiscal Year 2025"
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isActive;
    private List<AccountingPeriod> periods;

    /** Factory — creates a new, unpersisted FiscalYear. */
    public static FiscalYear createNew(
            String code, String name,
            LocalDate startDate, LocalDate endDate,
            Boolean isActive) { ... }

    /**
     * Auto-generates one AccountingPeriod per calendar month
     * within [startDate, endDate].
     * Period codes: FY-0001-01, FY-0001-02, … FY-0001-12
     * Period names: "Jan 2025", "Feb 2025", … "Dec 2025"
     * Initial status: NEVER_OPENED
     */
    public List<AccountingPeriod> generateMonthlyPeriods(Long fiscalYearId) { ... }

    /** Transitions the target period to CLOSED status. */
    public AccountingPeriod closePeriod(Long periodId) { ... }

    /** Transitions the target period back to OPEN status (privileged operation). */
    public AccountingPeriod reopenPeriod(Long periodId) { ... }

    /** Returns all periods whose status is OPEN. */
    public List<AccountingPeriod> getOpenPeriods() { ... }
}
```

**Key fields:**

| Field | Type | Description |
|---|---|---|
| `code` | `String` | Auto-generated via `SequenceGeneratorService`. Pattern: `FY-{seq}`. |
| `name` | `String` | User-supplied label (max 100 chars). |
| `startDate` | `LocalDate` | Inclusive start of the fiscal year. |
| `endDate` | `LocalDate` | Inclusive end of the fiscal year. |
| `isActive` | `Boolean` | Soft flag; inactive years are hidden from transaction dropdowns. |
| `periods` | `List<AccountingPeriod>` | Owned collection; populated by `generateMonthlyPeriods`. |

---

### 3.2 `AccountingPeriod` — Entity

`AccountingPeriod` represents a single accounting period (one calendar month). It is always owned by a `FiscalYear` and cannot exist independently.

```java
public class AccountingPeriod {
    private final AuditMetadata metadata;
    private String       code;          // e.g. "FY-0001-01"
    private String       name;          // e.g. "Jan 2025"
    private int          periodNumber;  // 1–12
    private Long         fiscalYearId;
    private LocalDate    startDate;
    private LocalDate    endDate;
    private PeriodStatus status;        // NEVER_OPENED | OPEN | CLOSED

    public void open()   { this.status = PeriodStatus.OPEN;   }
    public void close()  { this.status = PeriodStatus.CLOSED; }
    public void reopen() { this.status = PeriodStatus.OPEN;   }

    /** Returns true only when status == OPEN (used by the Period Guard). */
    public boolean isOpen() { return this.status == PeriodStatus.OPEN; }
}
```

**Key fields:**

| Field | Type | Description |
|---|---|---|
| `code` | `String` | Composite code: `{FY-code}-{periodNumber padded 2}`. |
| `name` | `String` | Short month label, e.g. `"Jan 2025"`. |
| `periodNumber` | `int` | Ordinal position within the fiscal year (1–12). |
| `fiscalYearId` | `Long` | FK reference to the owning `FiscalYear`. |
| `startDate` | `LocalDate` | First day of the calendar month. |
| `endDate` | `LocalDate` | Last day of the calendar month. |
| `status` | `PeriodStatus` | Current lifecycle state. |

---

### 3.3 `PeriodStatus` — Enum

```java
public enum PeriodStatus {
    NEVER_OPENED,  // Period was created but has never been opened.
                   // No journal entries allowed.

    OPEN,          // Period is accepting journal entries.
                   // Normal operational state.

    CLOSED,        // Period has been closed for new postings.
                   // No new journal entries allowed.
                   // Can be transitioned back to OPEN by ACCOUNTING_ADMIN
                   // (all subsequent journals receive is_adjustment = true).
}
```

---

## 4. Database Schema

Schema is managed by Flyway. Relevant migrations:

- `V43__Add_Accounting_Foundation.sql` — creates both tables.
- `V44__Alter_Period_Status_And_Add_Period_Number.sql` — adds `period_number` column and updates the `status` default.

### 4.1 `acc_fiscal_years`

```sql
CREATE TABLE acc_fiscal_years (
    id                  BIGINT       NOT NULL AUTO_INCREMENT,
    code                VARCHAR(20)  NOT NULL,          -- Auto: FY-{seq}
    name                VARCHAR(100) NOT NULL,
    start_date          DATE         NOT NULL,
    end_date            DATE         NOT NULL,
    is_active           BOOLEAN      NOT NULL DEFAULT TRUE,
    version             INT          NOT NULL DEFAULT 0, -- Optimistic lock
    created_by_user_id  BIGINT       NULL,
    created_date        DATETIME     NULL,
    updated_by_user_id  BIGINT       NULL,
    updated_date        DATETIME     NULL,

    PRIMARY KEY (id),
    UNIQUE KEY uk_fy_code (code)
);
```

| Column | Type | Notes |
|---|---|---|
| `id` | `BIGINT AUTO_INCREMENT` | Surrogate PK |
| `code` | `VARCHAR(20) UNIQUE` | Application-generated via sequence. Never null. |
| `name` | `VARCHAR(100)` | Human label. |
| `start_date` | `DATE` | Inclusive start. |
| `end_date` | `DATE` | Inclusive end. |
| `is_active` | `BOOLEAN` | Defaults to `TRUE`. |
| `version` | `INT` | JPA optimistic locking (`@Version`). |
| `created_by_user_id` | `BIGINT NULL` | Audit — creating user. |
| `created_date` | `DATETIME NULL` | Audit — creation timestamp. |
| `updated_by_user_id` | `BIGINT NULL` | Audit — last modifier. |
| `updated_date` | `DATETIME NULL` | Audit — last modified timestamp. |

---

### 4.2 `acc_accounting_periods`

```sql
CREATE TABLE acc_accounting_periods (
    id                  BIGINT       NOT NULL AUTO_INCREMENT,
    code                VARCHAR(20)  NOT NULL,          -- e.g. FY-0001-01
    name                VARCHAR(100) NOT NULL,          -- e.g. "Jan 2025"
    fiscal_year_id      BIGINT       NOT NULL,
    start_date          DATE         NOT NULL,
    end_date            DATE         NOT NULL,
    status              VARCHAR(20)  NOT NULL DEFAULT 'NEVER_OPENED',
    period_number       INT          NOT NULL,          -- 1–12
    version             INT          NOT NULL DEFAULT 0,
    created_by_user_id  BIGINT       NULL,
    created_date        DATETIME     NULL,
    updated_by_user_id  BIGINT       NULL,
    updated_date        DATETIME     NULL,

    PRIMARY KEY (id),
    UNIQUE KEY uk_period_code (code),
    CONSTRAINT fk_period_fy
        FOREIGN KEY (fiscal_year_id)
        REFERENCES acc_fiscal_years(id)
);
```

| Column | Type | Notes |
|---|---|---|
| `id` | `BIGINT AUTO_INCREMENT` | Surrogate PK |
| `code` | `VARCHAR(20) UNIQUE` | Composite: `{FY-code}-{padded period number}`. |
| `name` | `VARCHAR(100)` | Month label, e.g. `"Jan 2025"`. |
| `fiscal_year_id` | `BIGINT NOT NULL` | FK → `acc_fiscal_years.id`. |
| `start_date` | `DATE` | First day of the month. |
| `end_date` | `DATE` | Last day of the month. |
| `status` | `VARCHAR(20)` | One of: `NEVER_OPENED`, `OPEN`, `CLOSED`. Default: `NEVER_OPENED`. |
| `period_number` | `INT` | Ordinal 1–12 within the fiscal year. |
| `version` | `INT` | JPA optimistic locking. |

---

## 5. Sequence Generation

Fiscal Year codes are auto-generated by `SequenceGeneratorService` using the `system_sequences` table, which is shared across all modules that need sequential codes.

### Sequence Registration (inserted by migration)

```sql
INSERT INTO system_sequences (module_code, format_pattern, pad_length, reset_cycle)
VALUES ('FISCAL_YEAR', 'FY-{seq}', 4, 'NEVER');
```

| Parameter | Value | Description |
|---|---|---|
| `module_code` | `FISCAL_YEAR` | Lookup key used by the service. |
| `format_pattern` | `FY-{seq}` | `{seq}` is replaced by the next padded integer. |
| `pad_length` | `4` | Zero-padded to 4 digits: `0001`, `0002`, … |
| `reset_cycle` | `NEVER` | Counter never resets; codes are globally unique forever. |

### Generation Flow

```
CreateFiscalYearUseCase
  └─→ SequenceGeneratorService.next("FISCAL_YEAR")
         └─→ SELECT + UPDATE system_sequences (atomic, row-locked)
               └─→ returns "FY-0001"
  └─→ FiscalYear.createNew("FY-0001", ...)
  └─→ FiscalYear.generateMonthlyPeriods(savedId)
         └─→ period codes: "FY-0001-01" … "FY-0001-12"
```

Period codes are **not** drawn from the sequence table — they are derived deterministically from the fiscal year code and the `periodNumber` (01–12).

---

## 6. Business Rules

The following rules are enforced at the **domain layer** (inside aggregate methods) and additionally at the **application layer** (use-case validations). They are not delegatable to the database alone.

**Rule 1 — 12 periods auto-generated**  
When a Fiscal Year is created, `generateMonthlyPeriods()` is called immediately. It produces exactly one `AccountingPeriod` per calendar month within `[startDate, endDate]`. All periods are initialized with status `NEVER_OPENED`. Periods cannot be added or removed individually after creation.

**Rule 2 — No overlapping fiscal years**  
Before saving a new Fiscal Year, the application layer checks that its date range does not intersect any existing Fiscal Year. The boundary check is performed on `DATE` values only (not timestamps). Adjacent years (one ends 31-Dec, next starts 1-Jan) are valid and non-overlapping.

**Rule 3 — Period Guard**  
Journal entries and all financial documents may only target an accounting period whose `status` is `OPEN`. Any attempt to post to a `NEVER_OPENED` or `CLOSED` period throws a `DomainException` with message key `accounting.error.period-not-open`.

**Rule 4 — Reopen privilege**  
Only users with the `ACCOUNTING_ADMIN` role may reopen a `CLOSED` period. The use case enforces this at the application layer via the security context. All financial documents posted into a reopened period automatically receive the `is_adjustment = true` flag.

**Rule 5 — Auto-generated fiscal year code**  
The fiscal year code is never user-supplied. It is always generated by `SequenceGeneratorService` with pattern `FY-{seq}` (4-digit zero-padded, never reset). The user sees the generated code after saving.

**Rule 6 — Default fiscal year alignment**  
The default start/end dates in the creation form are pre-filled to 1 January–31 December of the current calendar year, matching the Indonesian tax year (Tahun Pajak).

**Rule 7 — Delete protection**  
A Fiscal Year cannot be deleted if any of its periods contain at least one posted journal entry. The `FiscalYearInUseChecker` port is called by `DeleteFiscalYearUseCase` before attempting deletion. If any journal entry references a period belonging to the fiscal year, a `DomainException` is thrown.

---

## 7. Period Status Lifecycle

Each `AccountingPeriod` starts in `NEVER_OPENED` and moves through a defined set of transitions. The lifecycle is enforced inside the `FiscalYear` aggregate.

```
                    ┌──────────────────┐
    (on creation)   │                  │
  ─────────────────▶│  NEVER_OPENED    │
                    │                  │
                    └────────┬─────────┘
                             │
                          open()   (any authorized user)
                             │
                             ▼
                    ┌──────────────────┐
                    │                  │◀──────────────────┐
                    │      OPEN        │                   │
                    │                  │                   │
                    └────────┬─────────┘               reopen()
                             │                  (ACCOUNTING_ADMIN only)
                          close()                          │
                             │                            │
                             ▼                            │
                    ┌──────────────────┐                  │
                    │                  │                  │
                    │     CLOSED       │──────────────────┘
                    │                  │
                    └──────────────────┘
```

### Transition Rules

| From | Transition | To | Who | Notes |
|---|---|---|---|---|
| `NEVER_OPENED` | `open()` | `OPEN` | Authorized user | First time period is activated. |
| `OPEN` | `close()` | `CLOSED` | Authorized user | No more journal entries accepted. |
| `CLOSED` | `reopen()` | `OPEN` | `ACCOUNTING_ADMIN` only | High-risk; audit log entry written. |

> **Note:** There is no direct transition from `NEVER_OPENED` to `CLOSED`. A period must pass through `OPEN` first.

### Audit for High-Risk Operations

| Operation | Severity | Logged Data |
|---|---|---|
| Period reopen (`CLOSED` → `OPEN`) | 🔴 HIGH | User ID, timestamp, period code, reason supplied by user |

---

## 8. Use Cases

### 8.1 Command Use Cases

| Use Case Class | Trigger | Description |
|---|---|---|
| `CreateFiscalYearUseCase` | `POST /accounting/periods` | Generates next FY code, creates `FiscalYear`, calls `generateMonthlyPeriods`, persists all 13 records in one transaction. |
| `UpdateFiscalYearUseCase` | `PUT /accounting/periods/{id}` | Updates `name`, `isActive`. Dates and code are immutable after creation. |
| `DeleteFiscalYearUseCase` | `DELETE /accounting/periods/{id}` | Calls `FiscalYearInUseChecker`; deletes the FY and its 12 periods if no journals exist. |
| `ClosePeriodUseCase` | `POST /accounting/periods/{fyId}/periods/{periodId}/close` | Calls `FiscalYear.closePeriod(periodId)`, persists updated period. |
| `ReopenPeriodUseCase` | `POST /accounting/periods/{fyId}/periods/{periodId}/reopen` | Requires `ACCOUNTING_ADMIN` role; calls `FiscalYear.reopenPeriod(periodId)`, writes audit log, persists. |

### 8.2 Query Use Cases

| Use Case Class | Trigger | Description |
|---|---|---|
| `FindFiscalYearsUseCase` | `GET /accounting/periods` | Returns paginated list of `FiscalYearSummaryResponse` (id, code, name, date range, active flag, period counts). |
| `GetFiscalYearDetailUseCase` | `GET /accounting/periods/{id}` | Returns `FiscalYearDetailResponse` containing the full FY record plus all 12 `PeriodResponse` items. |

---

## 9. RBAC Permissions

| Permission Constant | Grants Access To |
|---|---|
| `ACCOUNTING-PERIOD_READ` | View fiscal year list and period detail pages. |
| `ACCOUNTING-PERIOD_CREATE` | Submit the create-fiscal-year form (auto-generates 12 periods). |
| `ACCOUNTING-PERIOD_UPDATE` | Edit fiscal year name / active flag; open, close, or reopen a period. Reopen additionally requires the `ACCOUNTING_ADMIN` role. |
| `ACCOUNTING-PERIOD_DELETE` | Delete a fiscal year and its periods (only permitted when no journal entries exist). |

> Permissions follow the Solusi ERP convention: `{MODULE}-{RESOURCE}_{ACTION}`.  
> Role `ACCOUNTING_ADMIN` is a super-role within the accounting domain and implicitly holds all four permissions above.

---

## 10. Period Guard Pattern

The Period Guard is the integration contract between this module and every downstream transactional module. It must be called before any financial document is persisted.

### Contract

```java
/**
 * Returns true if there is at least one OPEN AccountingPeriod
 * whose [startDate, endDate] range contains the given date.
 *
 * Called by all transactional use cases before persisting a document.
 */
boolean existsOpenPeriodForDate(LocalDate transactionDate);
// defined on: FiscalYearRepository (domain port)
```

### Usage Pattern (pseudocode for downstream modules)

```java
// Example: inside GoodsReceiptUseCase, VendorBillUseCase, JournalEntryUseCase, etc.

LocalDate transactionDate = command.getDocumentDate();

boolean hasOpenPeriod = fiscalYearRepository.existsOpenPeriodForDate(transactionDate);
if (!hasOpenPeriod) {
    throw new DomainException("accounting.error.period-not-open");
}

// Proceed to create / persist the financial document...
```

### Modules That Must Enforce the Period Guard

| Module | Sprint | Document Type |
|---|---|---|
| Goods Receipt | Sprint 4 | Purchase receipt |
| Vendor Bill | Sprint 5 | Supplier invoice |
| Journal Entry | Sprint 6 | Manual GL journal |
| All future financial documents | Sprint 7+ | Any document that posts to the general ledger |

### SQL-Level Guard (infrastructure implementation)

```sql
SELECT COUNT(*) > 0
FROM   acc_accounting_periods
WHERE  status       = 'OPEN'
  AND  start_date  <= :transactionDate
  AND  end_date    >= :transactionDate;
```

---

## 11. Auto-Generation Example

The following illustrates what happens when a user creates **Fiscal Year 2025** (January–December).

### Input

| Field | Value |
|---|---|
| Name | `Fiscal Year 2025` |
| Start Date | `2025-01-01` |
| End Date | `2025-12-31` |
| Is Active | `true` |

### Generated Fiscal Year Record

| Field | Value |
|---|---|
| `id` | `1` (auto-increment) |
| `code` | `FY-0001` (from sequence) |
| `name` | `Fiscal Year 2025` |
| `start_date` | `2025-01-01` |
| `end_date` | `2025-12-31` |
| `is_active` | `true` |

### Generated Accounting Period Records (12 rows)

| `period_number` | `code` | `name` | `start_date` | `end_date` | `status` |
|---|---|---|---|---|---|
| 1 | `FY-0001-01` | `Jan 2025` | `2025-01-01` | `2025-01-31` | `NEVER_OPENED` |
| 2 | `FY-0001-02` | `Feb 2025` | `2025-02-01` | `2025-02-28` | `NEVER_OPENED` |
| 3 | `FY-0001-03` | `Mar 2025` | `2025-03-01` | `2025-03-31` | `NEVER_OPENED` |
| 4 | `FY-0001-04` | `Apr 2025` | `2025-04-01` | `2025-04-30` | `NEVER_OPENED` |
| 5 | `FY-0001-05` | `May 2025` | `2025-05-01` | `2025-05-31` | `NEVER_OPENED` |
| 6 | `FY-0001-06` | `Jun 2025` | `2025-06-01` | `2025-06-30` | `NEVER_OPENED` |
| 7 | `FY-0001-07` | `Jul 2025` | `2025-07-01` | `2025-07-31` | `NEVER_OPENED` |
| 8 | `FY-0001-08` | `Aug 2025` | `2025-08-01` | `2025-08-31` | `NEVER_OPENED` |
| 9 | `FY-0001-09` | `Sep 2025` | `2025-09-01` | `2025-09-30` | `NEVER_OPENED` |
| 10 | `FY-0001-10` | `Oct 2025` | `2025-10-01` | `2025-10-31` | `NEVER_OPENED` |
| 11 | `FY-0001-11` | `Nov 2025` | `2025-11-01` | `2025-11-30` | `NEVER_OPENED` |
| 12 | `FY-0001-12` | `Dec 2025` | `2025-12-01` | `2025-12-31` | `NEVER_OPENED` |

> All 13 records (1 fiscal year + 12 periods) are persisted within a **single database transaction**. If any part fails, the entire creation is rolled back.

### Subsequent Fiscal Year

If a second fiscal year is created (e.g., 2026), the sequence yields `FY-0002`, and its periods become `FY-0002-01` through `FY-0002-12`. The sequence counter never resets, ensuring global uniqueness regardless of deletions.
