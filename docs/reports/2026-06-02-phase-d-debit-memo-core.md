# Phase D Report: Vendor Debit Memo Core

Status: IN_PROGRESS
Plan: [docs/plans/2026-06-02-phase-d-debit-memo-core.md](../plans/2026-06-02-phase-d-debit-memo-core.md)
Source Brainstorm: [docs/brainstorming/2026-06-02-vendor-debit-memo.md](../brainstorming/2026-06-02-vendor-debit-memo.md)

## Task Log

## Task 1: Add Database Contract, Sequence, Permissions, and Menu Entry

- **Status:** findings
- **Summary:** Added MariaDB/H2 `V73__Add_Debit_Memo_Core.sql` migrations for Debit Memo header/lines, `DEBIT_MEMO` sequence, AP-03 menu entry, permissions, admin grants, and migration contract tests.
- **Tests:** `mvn test -Dtest=DebitMemoCoreMigrationTest` passed.

### Finding: H2 migration location differs from plan target path

- **Type:** deviation
- **Severity:** info
- **Detail:** The plan listed `src/test/resources/db/migration/V73__Add_Debit_Memo_Core.sql`, but this project stores the H2 Flyway mirror under `src/main/resources/db/migration-h2`.
- **Action taken:** Added the H2 mirror to `src/main/resources/db/migration-h2/V73__Add_Debit_Memo_Core.sql`, matching existing Flyway test configuration.
- **Ref:** `src/main/resources/db/migration-h2`

### Finding: H2 exposes named unique constraints separately from indexes

- **Type:** decision
- **Severity:** info
- **Detail:** H2 did not expose named unique constraints through `information_schema.indexes` in the same way as named non-unique indexes.
- **Action taken:** The migration contract test now asserts unique constraints via `information_schema.table_constraints` and non-unique indexes via `information_schema.indexes`.
- **Ref:** `src/test/java/com/solusi/erp/accountspayable/debitmemo/infrastructure/persistence/DebitMemoCoreMigrationTest.java`

## Task 2: Implement Debit Memo Domain Model and Invariants

- **Status:** clean
- **Summary:** Added Debit Memo aggregate, line value object, settlement status enum, and domain tests for monetary invariants, metadata mutability, settlement transitions, cancellation, and defensive copies.
- **Tests:** `mvn test -Dtest=DebitMemoTest` passed with 11 tests.

## Task 3: Add Persistence, Query Models, and Configuration

- **Status:** findings
- **Summary:** Added Debit Memo repository port, JPA entities, MapStruct persistence mapper, repository adapter, filtered query repository, Phase D read models/use cases, and Spring config wiring.
- **Tests:** `mvn test "-Dtest=DebitMemoRepositoryImplTest,DebitMemoQueryUseCaseTest,DebitMemoConfigTest"` passed with 9 tests.

### Finding: Config package follows existing AP infrastructure convention

- **Type:** deviation
- **Severity:** info
- **Detail:** The plan target listed `src/main/java/com/solusi/erp/accountspayable/debitmemo/config/DebitMemoConfig.java`, but existing AP modules keep config under `infrastructure/config`.
- **Action taken:** Created `DebitMemoConfig` under `src/main/java/com/solusi/erp/accountspayable/debitmemo/infrastructure/config` to match `VendorBillConfig` and `VendorPaymentConfig`.
- **Ref:** `src/main/java/com/solusi/erp/accountspayable/vendorbill/infrastructure/config/VendorBillConfig.java`

### Finding: Phase D settlement recap is intentionally zero-consumption

- **Type:** decision
- **Severity:** info
- **Detail:** Allocation and vendor refund are deferred, so list/detail read models cannot calculate consumed amounts yet.
- **Action taken:** Query use cases return `settledAmount = 0`, `refundedAmount = 0`, and `remainingAmount = grossAmountOriginal` while preserving the stored settlement status for future Phase E integration.
- **Ref:** `src/main/java/com/solusi/erp/accountspayable/debitmemo/application/usecase/query`

## Verification

- Task 1: `mvn test -Dtest=DebitMemoCoreMigrationTest` passed.
- Task 2: `mvn test -Dtest=DebitMemoTest` passed.
- Task 3: `mvn test "-Dtest=DebitMemoRepositoryImplTest,DebitMemoQueryUseCaseTest,DebitMemoConfigTest"` passed.

## Notes

- Phase D plan only; implementation is intentionally pending.
- Phase E allocation behavior remains deferred.
