# Implementation Report: Debit Memo Allocation

> Plan: [docs/plans/2026-06-02-phase-e-debit-memo-allocation.md](../plans/2026-06-02-phase-e-debit-memo-allocation.md)
> Source: [docs/brainstorming/2026-06-02-vendor-debit-memo.md](../brainstorming/2026-06-02-vendor-debit-memo.md)

This report is populated during plan execution.

## Task 1: Database, Sequence, Permissions, And Accounting Contract

- **Status:** Completed.
- **Summary:** Added `DEBIT_MEMO_APPLICATION` and DMA journal variables, introduced V74 MariaDB/H2 migrations for DMA header/line tables, sequence, AP-04 menu/permissions/admin grants, and accounting schema registration. Updated dev and H2 E2E accounting seeds for DMA variables.
- **Tests:** `mvn test -Dtest="DebitMemoAllocationMigrationTest,JournalVariableTest,JournalMessageBundleTest"` passed with 5 tests. `mvn test -Dtest="*MigrationTest"` passed with 15 tests.
- **Notes:** The DB migration registers DMA schema lines using existing COA codes, matching prior V62/V71 behavior. The H2 migration contract asserts the active schema header at target 74, while token checks and the V9000 refresh block lock the E2E line mappings where E2E-only COA rows exist.

## Task 2: DMA Domain Model, Lifecycle, And Allocation Math

- **Status:** Completed.
- **Summary:** Added the DMA domain aggregate, immutable allocation line snapshot, lifecycle status enum, and proration service for DPP/tax/base/FX allocation math with last-line remainder handling. Added i18n message keys for domain guard failures in English and Indonesian bundles.
- **Tests:** `mvn test -Dtest="DebitMemoAllocationTest,DebitMemoAllocationProrationTest"` passed with 17 tests.
- **Notes:** Empty package-info skeletons for future repository/port/usecase/infrastructure/web packages were avoided because the compiler plugin warns when no package-info class is emitted. Those packages will be materialized by concrete classes in the following tasks.

## Task 3: Persistence, Repository, Query Read Models, And Config Wiring

- **Status:** Completed.
- **Summary:** Added DMA JPA header/line entities, MapStruct persistence mapper, JPA repository, domain repository adapter, history projections, list/detail/history query use cases, `DebitMemoAllocationConfig`, and a real Debit Memo consumption adapter. Debit Memo list/detail recaps now use confirmed DMA applied totals.
- **Tests:** `mvn test -Dtest="DebitMemoAllocationRepositoryImplTest,DebitMemoAllocationQueryUseCaseTest,DebitMemoAllocationConfigTest,DebitMemoCommandUseCaseTest,DebitMemoQueryUseCaseTest,DebitMemoConfigTest"` passed with 22 tests.
- **Notes:** Lock-specific confirm/reverse behavior remains implemented in the later confirm/reverse workflow tasks; Task 3 provides the repository and consumption surfaces needed by those flows.

## Task 4: Cross-Slice Vendor Bill Settlement Integration

- **Status:** Completed.
- **Summary:** Vendor Bill settlement summary, Vendor Payment payable selector, and Vendor Payment stale-confirm guard now subtract confirmed DMA line applications alongside confirmed payments. Settlement status recalculation now considers paid amount plus DMA applied amount, and stale outstanding failures use a specific race-condition message key.
- **Tests:** `mvn test -Dtest="VendorBillSettlementSummaryAdapterTest,*Payable*Test,ConfirmVendorPaymentUseCaseTest,VendorPaymentConfigTest,VendorBillPaymentUpdateAdapterTest"` passed with 15 tests.
- **Notes:** The existing `VendorBillPaymentUpdatePort` name is kept for compatibility during Phase E; the implementation is now settlement-source aware and can be generalized later if more AP settlement sources are added.
