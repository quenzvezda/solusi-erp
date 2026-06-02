# Implementation Report: Purchase Return Phase 1

> Plan: `docs/plans/2026-06-01-purchase-return.md`
>
> Source: `docs/brainstorming/2026-06-01-purchase-return.md`

## Task 1: Flyway Schema for Generic Reservation and Purchase Return

### Finding: Goods Receipt line table uses procurement prefix
- **Type:** deviation
- **Severity:** info
- **Detail:** The initial Purchase Return line foreign key used `inv_goods_receipt_lines`, while the existing Goods Receipt schema names the table `pur_goods_receipt_lines`.
- **Action taken:** Corrected both MariaDB and H2 V67 migrations and verified the H2 migration end-to-end.
- **Ref:** `src/main/resources/db/migration/V67__Add_Purchase_Return_Phase_1.sql`

- **Status:** clean after correction
- **Summary:** Added generic reservation and Purchase Return schema, source indexes, generated-GI uniqueness guard, sequence registration, H2 mirror, and migration contract coverage.
- **Verification:** `mvn test -Dtest=PurchaseReturnMigrationTest`

## Task 2: Generic Inventory Reservation Domain
- **Status:** clean
- **Summary:** Enforced `reserved <= onHand`, added the generic reservation aggregate, ownership and service ports, serialized-stock validation, and focused domain coverage.
- **Verification:** `mvn test -Dtest=StockBalanceDomainTest,InventoryReservationTest`

## Task 3: Reservation Persistence and Inventory Availability Enforcement

### Finding: Stock payload already carries sufficient owner audit metadata
- **Type:** decision
- **Severity:** info
- **Detail:** Existing `StockMovementPayload.referenceType/referenceId/referenceCode` fields are sufficient to audit reservation movements against their Purchase Return owner.
- **Action taken:** Added `ReferenceType.PURCHASE_RETURN` and reused the existing payload contract instead of adding parallel owner fields.
- **Ref:** `src/main/java/com/solusi/erp/inventory/stock/application/dto/StockMovementPayload.java`

- **Status:** clean
- **Summary:** Added reservation JPA persistence, repository adapter, transactional reservation service, stock config wiring, outbound availability regression coverage, rollback expectation coverage, and a focused config test.
- **Verification:** `mvn test -Dtest=StockBalanceDomainTest,StockServiceTest,InventoryReservationServiceTest,StockConfigTest`
