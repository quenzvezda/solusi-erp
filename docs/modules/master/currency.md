# Currency Module Documentation

## Overview
The Currency feature is a core master data module within the ERP system. It replaces the old "Money" placeholder and is responsible for managing a list of active currencies available for international and localized financial transactions across the platform.

## Architecture

This feature follows the **Clean Architecture + DDD + CQRS** pattern (standard for all modules).

```text
master.currency
├── domain              (100% Pure Java)
│   ├── model           Currency.java (Aggregate Root, uses AuditMetadata)
│   └── repository      CurrencyRepository.java (interface)
├── application         (100% Pure Java)
│   └── usecase
│       ├── command     CreateCurrencyUseCase, UpdateCurrencyUseCase, DeleteCurrencyUseCase
│       └── query       FindCurrenciesUseCase, FindCurrencyByIdUseCase, GetCurrencyEditViewUseCase
├── infrastructure      (Framework-dependent)
│   ├── persistence     Currency (JPA Entity, extends BaseModel), CurrencyJpaRepository
│   ├── adapter         CurrencyRepositoryAdapter (implements domain CurrencyRepository)
│   └── config          CurrencyConfig.java (Composition Root, TransactionTemplate)
└── web
    ├── controller      CurrencyController.java
    ├── dto             CurrencySaveRequest, CurrencyDetailResponse, CurrencySummaryResponse
    └── mapper          CurrencyWebMapper.java (MapStruct)
```

**Domain highlights:**
- `Currency` domain model menggunakan `AuditMetadata` (bukan `extends BaseModel`) untuk menjaga kemurnian domain.
- Business method `setAsDefault()` dan `revokeDefault()` berada di domain model untuk menerapkan aturan "hanya satu default aktif".
- Bean registration dan `TransactionTemplate` dikonfigurasi di `CurrencyConfig.java`.

---

## Database Schema (`master_currencies`)

| Column Name  | Data Type      | Constraints | Description |
| ------------ | -------------  | ----------  | ----------- |
| `id`         | `BIGINT`       | `PK`, Auto  | Primary Key |
| `symbol`     | `VARCHAR(10)`  | `NOT NULL`  | The identifying symbol (e.g., `$`, `€`, `Rp`, `¥`) |
| `alias`      | `VARCHAR(10)`  | `UNIQUE`    | A unique identifiable alias (e.g., `USD`, `IDR`) |
| `name`       | `VARCHAR(150)` | `NOT NULL`  | Full descriptive name (e.g., `US Dollar`) |
| `note`       | `TEXT`         |             | Optional remarks |
| `is_active`  | `BOOLEAN`      | Default `1` | Indicates if the currency can be currently used |
| `is_default` | `BOOLEAN`      | Default `0` | If `true`, this is the default currency of the app |

*(Note: Fields injected by `BaseModel` such as `version`, `created_date`, and `updated_by` are also included beneath these columns.)*

---

## Permissions (RBAC)
This module enforces Role-Based Access Control using the following standardized permissions:

- `CURRENCY_READ`: Grants access to view the Currency Menu and List Interface.
- `CURRENCY_CREATE`: Grants access to the 'Add New' form and POST action hook.
- `CURRENCY_UPDATE`: Grants access to the 'Edit' form and POST action hook.
- `CURRENCY_DELETE`: Grants access to execute the soft-delete transaction.

By default upon system start via `Flyway`, all 4 permissions are provisioned entirely to the `ROLE_ADMIN`.

---

## Notable Behaviors

1. **Soft Delete**: When attempting to delete a currency, the record is not permanently dropped from the database. Instead, its `is_active` status is set to `false`, and its `is_default` status is revoked.
2. **Default Currency Conflict Resolution**: When creating or editing a currency and selecting "Jadikan Default" (Set as Default), the background service locates the current existing active default and reverts it to non-default, avoiding constraint violations without manual user intervention.
3. **No Sequence Autogeneration**: Unlike modules generating sequential string codes (`PRD-0001`), Currencies strictly adhere to manually defined mathematical/geographical symbols (`symbol`).
