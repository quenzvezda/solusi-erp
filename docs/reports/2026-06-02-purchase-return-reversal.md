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

## Task 3: Source-Owned Reversal Ports And Reverse Use Case

- **Status:** findings
- **Summary:** Added Purchase Return reverse command/view contracts, source-owned inventory reversal, journal reversal, Debit Memo guard/cancel ports, transactional reverse orchestration, and focused use-case/config coverage.
- **Validation:** `mvn test -Dtest="ReverseConfirmedPurchaseReturnUseCaseTest,PurchaseReturnConfigTest,StockMovementReversalServiceTest,ReversePostedJournalUseCaseTest,DebitMemoCommandUseCaseTest"` passed with 29 tests. Focused run emitted expected low-coverage JaCoCo warnings but Maven exited success.

### Finding: Debit Memo validation and cancellation are split
- **Type:** decision
- **Severity:** info
- **Detail:** The brainstorming flow requires DMA/full-balance/period guards to pass before stock and journal side effects, but Debit Memo cancellation should happen after stock reversal, journal reversal, and GI cancellation.
- **Action taken:** Added `PurchaseReturnDebitMemoReversalPort.validateReversibleAndLock(...)` and `cancelDebitMemo(...)` as separate calls. The use case validates and locks first, then cancels the Debit Memo only after downstream reversals succeed.
- **Ref:** `src/main/java/com/solusi/erp/purchasing/purchasereturn/application/usecase/command/ReverseConfirmedPurchaseReturnUseCaseImpl.java`

### Finding: Source-owned GI reversal avoids generic GI cancel use case
- **Type:** decision
- **Severity:** info
- **Detail:** Generic `CancelGoodsIssueUseCaseImpl` intentionally rejects non-manual source-owned Goods Issues. Purchase Return reversal must keep that guard intact.
- **Action taken:** Added a Purchase Return-owned inventory reversal adapter that validates the generated GI belongs to the Purchase Return, reverses outbound movements through `StockMovementReversalService`, then marks the generated GI `CANCELLED` directly through the GI aggregate/repository.
- **Ref:** `src/main/java/com/solusi/erp/purchasing/purchasereturn/infrastructure/adapter/PurchaseReturnInventoryReversalAdapter.java`

### Finding: Purchase Return line id on movement snapshots is resolved best-effort
- **Type:** decision
- **Severity:** info
- **Detail:** Inventory movement rows do not store the Goods Issue line id or Purchase Return line id directly. The generated GI lines keep `referenceLineId`, but movement rows only expose product/container/serial/quantity.
- **Action taken:** The inventory reversal adapter resolves `purchaseReturnLineId` by matching each outbound movement back to the generated GI line. The V75 `purchase_return_line_id` column remains nullable for cases where a movement cannot be matched unambiguously.
- **Ref:** `src/main/java/com/solusi/erp/purchasing/purchasereturn/infrastructure/adapter/PurchaseReturnInventoryReversalAdapter.java`

## Task 4: Reversal Location Form, Controller Routes, And Page JavaScript

- **Status:** clean
- **Summary:** Added Purchase Return reverse request DTOs, reverse GET/POST routes, AJAX reversal form, target-container lookup JavaScript, detail reverse action, `REVERSED` badge styling, reversal metadata/journal display, and controller/template/mapper tests.
- **Validation:** `mvn test -Dtest="PurchaseReturnControllerTest,PurchaseReturnViewIntegrationTest,PurchaseReturnReverseTemplateIntegrationTest,PurchaseReturnWebMapperTest"` passed with 22 tests. Additional `mvn test -Dtest="PurchaseReturnListIntegrationTest"` passed with 2 tests because list badge styling changed. Focused runs emitted expected low-coverage JaCoCo warnings but Maven exited success.

## Task 5: i18n, Documentation, And Stale Deferral Cleanup

- **Status:** clean
- **Summary:** Added bilingual Purchase Return reversal labels/errors/validation/success messages, updated Purchase Return/Goods Issue/Debit Memo module docs, and removed stale confirmed-reversal deferral wording from shipped module docs.
- **Validation:** `mvn test -Dtest="PurchaseReturnMessagesTest,*MessageBundleTest"` passed with 6 tests. Stale scans for `Phase F`, `Confirmed Purchase Return reversal deferred`, confirmed reversal deferral/cannot-reverse wording, and `deferred|ditunda` in updated module docs/messages found no stale deferral text. Focused run emitted expected low-coverage JaCoCo warnings but Maven exited success.
