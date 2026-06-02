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

## Task 4: Purchase Return Domain Aggregate

### Finding: Mixed-GR line validation belongs to the source read boundary
- **Type:** decision
- **Severity:** info
- **Detail:** The approved schema stores canonical GR ownership on the Purchase Return header and `goods_receipt_line_id` on each line, without duplicating GR header ID per line.
- **Action taken:** The aggregate enforces a canonical `GOODS_RECEIPT` header source. Task 7 will validate each selected line against slices returned for that GR before constructing domain lines.
- **Ref:** `src/main/java/com/solusi/erp/purchasing/purchasereturn/domain/model/PurchaseReturn.java`

- **Status:** clean
- **Summary:** Added Purchase Return reason/status enums, immutable line snapshots, aggregate lifecycle transitions, source and serial invariants, and branch-focused domain tests.
- **Verification:** `mvn test -Dtest=PurchaseReturnTest,PurchaseReturnLineTest`

## Task 5: Purchase Return Persistence and Spring Wiring

### Finding: Composition root must grow with later tasks
- **Type:** deviation
- **Severity:** info
- **Detail:** Task 5 requests repository and pure use-case wiring, but Purchase Return use-case classes are introduced only in Tasks 6-9.
- **Action taken:** Added and verified the repository bean now. The same `PurchaseReturnConfig` will be extended with query and transactional command beans when their implementations land.
- **Ref:** `src/main/java/com/solusi/erp/purchasing/purchasereturn/infrastructure/config/PurchaseReturnConfig.java`

- **Status:** clean
- **Summary:** Added audited Purchase Return JPA entities, MapStruct persistence mapping, repository port and adapter, composition root, and round-trip/config tests.
- **Verification:** `mvn test -Dtest=PurchaseReturnPersistenceMapperTest,PurchaseReturnConfigTest`
