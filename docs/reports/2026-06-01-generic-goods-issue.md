# Implementation Report: Generic Goods Issue Core

> Plan: `docs/plans/2026-06-01-generic-goods-issue.md`
> Source: `docs/brainstorming/2026-06-01-generic-goods-issue.md`
> Created: 2026-06-01

## Findings

(Populated during execution by execute-plan skill)

## Task 1: Stock Valuation Reference Metadata
- **Status:** findings
- **Summary:** Added valuation layer source-reference metadata, specific-layer consumption API, StockService delegation, GR valuation reference propagation, and focused unit coverage.

### Finding: Task order adjusted for TDD
- **Type:** deviation
- **Severity:** info
- **Detail:** The approved plan listed implementation steps before test steps, but the active TDD skill requires tests to be written and observed failing before production code.
- **Action taken:** Added RED tests in `FifoValuationServiceTest` and `StockServiceTest`, observed expected compile failures for missing API, then implemented the production changes.
- **Ref:** `src/test/java/com/solusi/erp/inventory/stock/domain/FifoValuationServiceTest.java`

### Finding: H2 migration syntax differs from MariaDB migration
- **Type:** decision
- **Severity:** info
- **Detail:** The MariaDB migration uses `AFTER` column placement for readability, but H2 migrations are safer without MySQL-specific column-position syntax.
- **Action taken:** Kept MariaDB migration with `AFTER`; used separate simple `ALTER TABLE ... ADD COLUMN` statements in `migration-h2`.
- **Ref:** `src/main/resources/db/migration-h2/V65__Add_Valuation_Layer_Reference_Metadata.sql`

## Task 2: Goods Issue Migration, Sequence, Permission, And Menu
- **Status:** findings
- **Summary:** Added GI core migrations for MariaDB and H2, sequence registration, menu/permission seeds, i18n keys, and a focused H2 Flyway migration test.

### Finding: Migration version shifted from V65 to V66
- **Type:** deviation
- **Severity:** info
- **Detail:** The plan named `V65__Add_Goods_Issue_Core.sql`, but Task 1 consumed `V65` for valuation layer reference metadata.
- **Action taken:** Used `V66__Add_Goods_Issue_Core.sql` in both `db/migration` and `db/migration-h2`, and updated the plan checklist text to match the actual migration.
- **Ref:** `src/main/resources/db/migration/V66__Add_Goods_Issue_Core.sql`
