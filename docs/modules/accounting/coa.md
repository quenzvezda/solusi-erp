# Chart of Accounts (COA)

## Module Overview

The Chart of Accounts module is the **foundational reference data** for the entire accounting subsystem of Solusi ERP. It maintains a hierarchical registry of all ledger accounts used to classify financial transactions. Every journal entry, accounting schema rule, and financial report ultimately resolves to an account defined here.

The COA module is part of the **General Ledger** domain and must be fully seeded before any other accounting module (Purchasing, Sales, or Journal Entry) can be used in production.

**Menu path:** Finance & Accounting → General Ledger → Chart of Accounts  
**Base URL:** `/accounting/coa`

---

## Package Structure

```
accounting.coa
├── domain
│   ├── model
│   │   ├── ChartOfAccount.java       ← Aggregate Root (pure Java, no JPA)
│   │   ├── AccountType.java          ← Enum: ASSET | LIABILITY | EQUITY | REVENUE | EXPENSE
│   │   └── NormalBalance.java        ← Enum: DEBIT | CREDIT
│   ├── repository
│   │   └── CoaRepository.java        ← Domain repository interface
│   └── port
│       ├── CoaLookupProvider.java    ← Output port: autocomplete lookup
│       └── CoaInUseChecker.java      ← Output port: checks journal usage
├── application
│   └── usecase
│       ├── command
│       │   ├── CreateCoaUseCase / CreateCoaUseCaseImpl
│       │   ├── UpdateCoaUseCase / UpdateCoaUseCaseImpl
│       │   └── DeleteCoaUseCase / DeleteCoaUseCaseImpl
│       └── query
│           ├── FindCoaUseCase / FindCoaUseCaseImpl
│           ├── GetCoaEditViewUseCase / GetCoaEditViewUseCaseImpl
│           └── GetCoaLookupUseCase / GetCoaLookupUseCaseImpl
├── infrastructure
│   ├── persistence
│   │   ├── ChartOfAccount.java        ← JPA Entity (extends BaseModel)
│   │   ├── CoaJpaRepository.java      ← Spring Data JPA interface
│   │   └── CoaPersistenceMapper.java  ← JPA entity ↔ domain model mapper
│   ├── adapter
│   │   ├── CoaRepositoryImpl.java
│   │   ├── CoaLookupProviderImpl.java
│   │   └── CoaInUseCheckerImpl.java
│   └── config
│       └── CoaConfig.java             ← Bean composition root (@Configuration)
└── web
    ├── controller
    │   ├── CoaController.java         ← CRUD + list pages (Thymeleaf SSR)
    │   └── CoaLookupController.java   ← REST endpoint for autocomplete
    ├── dto
    │   ├── CoaSaveRequest.java        ← Create / update form payload
    │   ├── CoaDetailResponse.java     ← Single account detail view
    │   └── CoaSummaryResponse.java    ← Row in list / lookup result
    └── mapper
        └── CoaWebMapper.java          ← DTO ↔ domain model mapper
```

---

## Domain Model

### `ChartOfAccount` — Aggregate Root

```java
public class ChartOfAccount {
    private final AuditMetadata metadata;  // createdBy, createdDate, updatedBy, updatedDate, version
    private String        code;
    private String        name;
    private AccountType   accountType;
    private NormalBalance normalBalance;   // auto-derived; see AccountType
    private Long          parentId;
    private Integer       level;           // 1 = header group, 2 = sub-group, 3 = detail
    private Boolean       isHeader;        // true = cannot be posted to
    private String        note;
    private Boolean       isActive;
}
```

| Field           | Type           | Description                                                  |
|-----------------|----------------|--------------------------------------------------------------|
| `code`          | `String`       | Unique account code (e.g. `1111`). **Immutable once used in a journal entry.** |
| `name`          | `String`       | Human-readable account name (e.g. `Cash on Hand`)           |
| `accountType`   | `AccountType`  | Determines the account's financial category                  |
| `normalBalance` | `NormalBalance`| Auto-derived from `accountType`                             |
| `parentId`      | `Long`         | FK to parent account (`null` for root-level accounts)        |
| `level`         | `Integer`      | Hierarchy depth: 1, 2, or 3                                  |
| `isHeader`      | `Boolean`      | `true` = grouping account; may not receive journal postings  |
| `note`          | `String`       | Optional free-text annotation                                |
| `isActive`      | `Boolean`      | `false` = soft-deleted / deactivated                         |

#### Key Factory / Behaviour Methods

```java
// Factory — creates a new, valid aggregate
ChartOfAccount.createNew(code, name, accountType, parentId, level, isHeader, note, isActive)

// Mutate fields on an existing account
account.update(name, accountType, parentId, level, isHeader, note, isActive)

// Soft-delete: sets isActive = false
account.softDelete()
```

---

### `AccountType` Enum

| Value       | Normal Balance (auto) | Typical accounts                        |
|-------------|----------------------|-----------------------------------------|
| `ASSET`     | `DEBIT`              | Cash, receivables, inventory, fixed assets |
| `LIABILITY` | `CREDIT`             | Payables, loans, accrued liabilities    |
| `EQUITY`    | `CREDIT`             | Owner capital, retained earnings        |
| `REVENUE`   | `CREDIT`             | Sales, service income                   |
| `EXPENSE`   | `DEBIT`              | COGS, operating expenses, depreciation  |

`AccountType.getDefaultNormalBalance()` returns the canonical `NormalBalance` for each type. This is called automatically during account creation.

---

### `NormalBalance` Enum

```java
public enum NormalBalance { DEBIT, CREDIT }
```

Increasing an account on its normal balance side is always a positive movement (e.g. debiting an asset account increases its balance).

---

## Database Schema

**Table:** `acc_chart_of_accounts`  
**Migration:** `V43__Add_Accounting_Foundation.sql`

| Column                | Type           | Null | Default | Notes                                |
|-----------------------|----------------|------|---------|--------------------------------------|
| `id`                  | `BIGINT`       | NO   | AUTO    | Primary key                          |
| `code`                | `VARCHAR(20)`  | NO   | —       | Unique. `uk_coa_code`                |
| `name`                | `VARCHAR(150)` | NO   | —       |                                      |
| `account_type`        | `VARCHAR(20)`  | NO   | —       | `ASSET \| LIABILITY \| EQUITY \| REVENUE \| EXPENSE` |
| `normal_balance`      | `VARCHAR(10)`  | NO   | —       | `DEBIT \| CREDIT`                    |
| `parent_id`           | `BIGINT`       | YES  | NULL    | Self-referencing FK (`fk_coa_parent`)|
| `level`               | `INT`          | NO   | `1`     | Hierarchy depth (1–3)                |
| `is_header`           | `BOOLEAN`      | NO   | `FALSE` |                                      |
| `note`                | `TEXT`         | YES  | NULL    |                                      |
| `is_active`           | `BOOLEAN`      | NO   | `TRUE`  |                                      |
| `version`             | `INT`          | NO   | `0`     | Optimistic locking                   |
| `created_by_user_id`  | `BIGINT`       | YES  | NULL    | Audit                                |
| `created_date`        | `DATETIME`     | YES  | NULL    | Audit                                |
| `updated_by_user_id`  | `BIGINT`       | YES  | NULL    | Audit                                |
| `updated_date`        | `DATETIME`     | YES  | NULL    | Audit                                |

**Constraints:**
- `PRIMARY KEY (id)`
- `UNIQUE KEY uk_coa_code (code)`
- `FOREIGN KEY fk_coa_parent (parent_id) REFERENCES acc_chart_of_accounts(id)`

---

## Business Rules

1. **Header accounts cannot be posted to.** Any account with `is_header = true` is a grouping node. Journal entries referencing a header account are rejected at validation time.

2. **Maximum hierarchy depth is 3 levels.**
   - Level 1 — Header group (e.g. `1100 – CURRENT ASSETS`)
   - Level 2 — Sub-group (e.g. `1110 – Cash & Bank`)
   - Level 3 — Detail / postable account (e.g. `1111 – Cash on Hand`)

3. **Account code is immutable once used in a journal entry.** The `code` field may be changed freely before any journal references it. Once `CoaInUseChecker.isInUse(id)` returns `true`, code updates are rejected.

4. **Normal balance is auto-derived from account type.** The `normalBalance` field is never set directly by the user; it is computed by `AccountType.getDefaultNormalBalance()` at create time and recalculated on type change.

5. **Deactivating a parent with active children is rejected.** If any child account in the subtree has `is_active = true`, deactivation of the parent throws a validation error with key `coa.error.has-active-children`.

6. **Deletion is soft-delete when the account has been used.** If the account exists in any journal entry (`CoaInUseChecker.isInUse(id)` = `true`), the delete use case sets `is_active = false` instead of removing the row. Hard delete is only performed when the account has never been journalised.

7. **Circular parent reference check.** Before saving a `parentId`, the application traverses the ancestor chain. If the target account already appears as an ancestor of the proposed parent, the operation is rejected with key `coa.error.circular-parent`.

---

## Status Lifecycle

```
  ┌─────────┐   deactivate    ┌──────────┐
  │  Active  │ ─────────────► │ Inactive │
  │(isActive │                │(isActive │
  │  = true) │ ◄───────────── │  = false)│
  └─────────┘    activate     └──────────┘
       │
       │ hard delete
       │ (only if never used in a journal entry)
       ▼
   [removed]
```

- **Activate / Deactivate** — toggle `isActive`; subject to Rule 5 on deactivation.
- **Hard delete** — only allowed when `CoaInUseChecker.isInUse(id)` returns `false`.
- **Soft delete** — `account.softDelete()` sets `isActive = false`; row is retained for audit.

---

## Use Cases

### Commands

| Use Case                | Class                      | Description                                                                 |
|-------------------------|----------------------------|-----------------------------------------------------------------------------|
| Create COA              | `CreateCoaUseCase`         | Validates uniqueness of `code`, derives `normalBalance`, persists new account. |
| Update COA              | `UpdateCoaUseCase`         | Updates mutable fields; enforces immutability of `code` if account is in use; re-derives `normalBalance` on type change. |
| Delete COA              | `DeleteCoaUseCase`         | Hard-deletes if not in use; soft-deletes (`isActive = false`) otherwise.    |

### Queries

| Use Case                | Class                      | Description                                                                 |
|-------------------------|----------------------------|-----------------------------------------------------------------------------|
| Find COA (list)         | `FindCoaUseCase`           | Returns a paginated / filtered list of accounts as `CoaSummaryResponse`.   |
| Get COA Edit View       | `GetCoaEditViewUseCase`    | Loads a single account plus supporting reference data (parent list, type enum values) needed to render the edit form. |
| Get COA Lookup          | `GetCoaLookupUseCase`      | Returns a lightweight list of active, non-header accounts matching a search term. Used by the journal entry autocomplete via `CoaLookupController`. |

---

## RBAC Permissions

| Permission Key         | Grants                                                      |
|------------------------|-------------------------------------------------------------|
| `ACCOUNTING-COA_READ`  | View the COA list page and individual account detail pages  |
| `ACCOUNTING-COA_CREATE`| Access the create form and submit new accounts              |
| `ACCOUNTING-COA_UPDATE`| Access the edit form and submit updates                     |
| `ACCOUNTING-COA_DELETE`| Trigger delete (hard or soft) and activate/deactivate       |
| `LOOKUP_COA`           | Call the `/accounting/coa/lookup` autocomplete endpoint     |

> `LOOKUP_COA` is typically granted to all roles that can create journal entries (e.g. `JOURNAL_CREATE`), even if those roles do not have full `ACCOUNTING-COA_*` access.

---

## Integration Notes

### Used by: Accounting Schema (Sprint 3)

The Accounting Schema module (`accounting.schema`) defines automatic posting rules (e.g. "on goods receipt, debit account X, credit account Y"). All account references in schema rules are foreign-keyed to `acc_chart_of_accounts.id`. The `CoaLookupProvider` port is the canonical way for other modules to resolve account suggestions without depending on COA internals.

### Used by: Journal Entry (Sprint 6)

Journal Entry lines reference `coa_id` (FK to `acc_chart_of_accounts.id`). At line validation time:
- The account must be **active** (`is_active = true`).
- The account must **not be a header** (`is_header = false`).
- Once a journal line referencing an account is confirmed, that account's `code` becomes immutable (Rule 3).

### Used by: Financial Reports (Sprint 8+)

Balance Sheet and P&L reports group totals by `account_type` and traverse the `parent_id` hierarchy to produce subtotals per level-1 and level-2 nodes.

### Provides: `CoaLookupProvider` port

Any module needing an account picker (e.g. Purchasing settings, Sales settings) should inject `CoaLookupProvider` rather than depending on `CoaRepository` directly. This preserves the dependency boundary.

---

## COA Hierarchy Example

The following excerpt illustrates the 3-level structure used in the Indonesian SME seed data:

```
1000  ASSETS                              [level 1, header]
│
├── 1100  CURRENT ASSETS                  [level 1, header]
│   │
│   ├── 1110  Cash & Bank                 [level 2, header]
│   │   ├── 1111  Cash on Hand            [level 3, postable ✓]
│   │   └── 1112  Bank BCA                [level 3, postable ✓]
│   │
│   ├── 1120  Accounts Receivable         [level 2, header]
│   │   └── 1121  Trade Receivable        [level 3, postable ✓]
│   │
│   ├── 1130  Inventory                   [level 2, header]
│   │   └── 1131  Merchandise Inventory   [level 3, postable ✓]
│   │
│   └── 1150  Prepaid & Other             [level 2, header]
│       └── 1151  Prepaid Expenses        [level 3, postable ✓]
│
└── 1200  FIXED ASSETS                    [level 1, header]
    └── ...

2000  LIABILITIES                         [level 1, header]
│   └── ...

3000  EQUITY                              [level 1, header]
4000  REVENUE                             [level 1, header]
5000  EXPENSES                            [level 1, header]
```

> **Rule:** Only level-3 non-header accounts (marked `✓`) can be referenced in journal entry lines or accounting schema rules. Level-1 and level-2 accounts exist solely for grouping and reporting subtotals.

---

## Related Migrations

| File                                    | Description                              |
|-----------------------------------------|------------------------------------------|
| `V43__Add_Accounting_Foundation.sql`    | Creates `acc_chart_of_accounts` table    |
| `V44__Seed_COA_Template.sql` *(planned)*| Seeds standard Indonesian SME COA template |

---

*Last updated: Sprint 1 — Accounting Foundation*
