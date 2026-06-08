# Implementation Report: Confirmed Purchase Return Reversal

> Plan: `docs/plans/2026-06-02-purchase-return-reversal.md`
> Source: `docs/brainstorming/2026-06-02-vendor-debit-memo.md`
> Created: 2026-06-09

## Findings

## Task 1: Database Contract, Permission, And Reversal Audit Shape

- **Status:** clean
- **Summary:** Added V75 MariaDB/H2 migrations for Purchase Return reversal audit columns, reversal line snapshots, `PURCHASE-RETURN_REVERSE` permission/admin grant, and migration contract coverage.
- **Validation:** `mvn test -Dtest="PurchaseReturnMigrationTest"` passed with 5 tests. `mvn test -Dtest="*MigrationTest"` passed with 15 tests. Focused migration runs emitted expected low-coverage JaCoCo warnings but Maven exited success.

## Task 2: Purchase Return Domain, Persistence, And Query Contract

- **Status:** findings
- **Summary:** Added `REVERSED` lifecycle support, reversal audit metadata, immutable reversal line snapshots, pessimistic repository locking, summary/detail DTO mapping, and focused domain/mapper/repository tests.
- **Validation:** `mvn test -Dtest="PurchaseReturnTest,PurchaseReturnPersistenceMapperTest,PurchaseReturnRepositoryImplTest,PurchaseReturnWebMapperTest"` passed with 39 tests. Focused run emitted expected low-coverage JaCoCo warnings but Maven exited success.

### Finding: Reversal snapshots use a set-backed JPA child collection
- **Type:** decision
- **Severity:** info
- **Detail:** Purchase Return already fetches original lines as a `List`. Adding reversal snapshots as another `List` would create two fetch-joined bag collections in Hibernate query paths.
- **Action taken:** Mapped `PurchaseReturnEntity.reversalLines` as `Set<PurchaseReturnReversalLineEntity>` and sorted by id in the persistence mapper before converting to the domain list, preserving deterministic domain order without changing original Purchase Return lines.
- **Ref:** `src/main/java/com/solusi/erp/purchasing/purchasereturn/infrastructure/persistence/PurchaseReturnEntity.java`

### Finding: Repository adapter test did not exist before Task 2
- **Type:** deviation
- **Severity:** info
- **Detail:** The plan validation names `PurchaseReturnRepositoryImplTest`, but the project did not have that test class yet.
- **Action taken:** Added a narrow adapter test for `save` child attachment and `findByIdForUpdate` delegation to the locked JPA query.
- **Ref:** `src/test/java/com/solusi/erp/purchasing/purchasereturn/infrastructure/adapter/PurchaseReturnRepositoryImplTest.java`
