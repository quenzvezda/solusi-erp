# Currency Module Documentation

## Overview
The Currency feature is a core master data module within the ERP system. It replaces the old "Money" placeholder and is responsible for managing a list of active currencies available for international and localized financial transactions across the platform.

## Architecture

This feature follows the standard Layered Architecture pattern of the system:

1. **Database Layer (MySQL/Flyway)**
   * Manages the schema setup (`master_currencies` table).
   * Automatically seeds commonly used international currency data and role-based permissions (`CURRENCY_READ`, etc.) into the system.

2. **Domain/Entity Layer (Hibernate/JPA)**
   * The `Currency.java` entity defines the structural mapping to the database table extending the auditable `BaseModel` (which captures `createdBy`, `createdDate`, etc.).

3. **Data Transfer Objects (DTO) & Mappers**
   * Uses `CurrencyDto.java` containing Jakarta Bean validations (`@NotBlank`, `@Size`) ensuring data integrity arriving from the frontend.
   * Employs `CurrencyMapper.java` via MapStruct to seamlessly translate data between `Currency` and `CurrencyDto` layers.

4. **Service & Repository Layer (Spring Data JPA)**
   * Provides persistence operations through `CurrencyRepository.java`.
   * Encompasses domain logic and validation rules in `CurrencyServiceImpl.java` (e.g., ensuring alias uniqueness and protecting constraints).
   * **Default Currency Management**: Handles logic where there can only ever be **one** system-wide default currency active at a time. If a user sets a new currency as default, all other currencies are automatically unmarked.

5. **Controller Layer (Spring Web MVC & Security)**
   * The `CurrencyController.java` intercepts web requests.
   * Utilizes Spring Security method-level annotations (`@PreAuthorize("hasAuthority('...')")`) to ensure only individuals with specific permissions can read, create, update, or soft-delete currencies.

6. **Presentation Layer (Thymeleaf & Tabler UI)**
   * Renders the List views with Sortable Paginations (`currencies/list.html`).
   * Renders Form views for creation and updates with reactive form validation states (`currencies/form.html`).
   * Provides UI localization via `messages.properties` and `messages_id.properties`.

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
