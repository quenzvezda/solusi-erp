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

## Task 5: Draft DMA Use Cases, Eligible Selectors, And Stale Snapshot Display

- **Status:** Completed.
- **Summary:** Added draft create/update/cancel use cases, command records, source snapshot port, JDBC source adapter for eligible DM/VB selectors, selector use case, and config wiring. Draft creation recalculates DPP/tax/base/FX snapshots but does not reserve DM or VB balance.
- **Tests:** `mvn test -Dtest="CreateDebitMemoAllocationUseCaseTest,UpdateDebitMemoAllocationUseCaseTest,CancelDebitMemoAllocationUseCaseTest,DebitMemoAllocationSelectorUseCaseTest,DebitMemoAllocationConfigTest"` passed with 7 tests.
- **Notes:** Allocation date accounting-period validation is intentionally left for confirm, matching the plan. Stale snapshots are represented by source current snapshots and confirm-time guard messages; web-level stale display can consume the same values when templates are added.

## Task 6: Confirm DMA Use Case And Journal Posting

- **Status:** Completed with notes.
- **Summary:** Added confirm use case and config wiring. Confirm now locks the Debit Memo and target Vendor Bills, validates the accounting period, current Debit Memo remaining balance, target Vendor Bill outstanding, and vendor/currency consistency before posting `DEBIT_MEMO_APPLICATION`, saving the confirmed DMA, and refreshing Debit Memo/Vendor Bill settlement statuses.
- **Tests:** `mvn test -Dtest="ConfirmDebitMemoAllocationUseCaseTest,DebitMemoAllocationConfigTest,VendorBillSettlementSummaryAdapterTest"` passed with 12 tests.
- **Notes:** `PostJournalForEventUseCase` still returns `void`; Task 7 resolves the apply-journal link by looking up the posted journal by DMA source after posting. Confirm uses base `values` only because original/base immutable snapshots already live on DMA lines and the current posting engine does not require original-currency audit maps for this event. No separate tax-mismatch confirm guard was added because the current source snapshots expose gross/DPP/tax amounts but no independent tax policy flag for Vendor Bills to compare.

## Task 7: Reverse Confirmed DMA

- **Status:** Completed.
- **Summary:** Added reverse command/use case and config wiring. Reversal requires a confirmed DMA, non-empty reversal date/reason, and an apply journal link; it locks the Debit Memo and Vendor Bills, calls `ReversePostedJournalUseCase`, stores reversal metadata, and refreshes Debit Memo/Vendor Bill settlement projections.
- **Tests:** `mvn test -Dtest="ReverseDebitMemoAllocationUseCaseTest,DebitMemoCommandUseCaseTest,ReversePostedJournalUseCaseTest"` passed with 18 tests. Additional regression check `mvn test -Dtest="ConfirmDebitMemoAllocationUseCaseTest,DebitMemoAllocationConfigTest,DebitMemoTest,JournalMessageBundleTest"` passed with 29 tests.
- **Notes:** Confirm now resolves `applyJournalEntryId` through `JournalEntryRepository.findBySource("DEBIT_MEMO_ALLOCATION", allocationId)` immediately after posting, avoiding a broad change to the journal posting contract. Debit Memo settlement restoration uses a new domain `refreshSettlementStatus(...)` method so reversal can move status back to `OPEN` or `PARTIALLY_SETTLED`. The cancel guard already treats only `CONFIRMED` DMA as active consumption, so reversed/cancelled DMA no longer blocks cancellation through the existing repository query.

## Task 8: Web Layer, Templates, JavaScript, And Shortcuts

- **Status:** Completed with notes.
- **Summary:** Added DMA web DTOs, mapper, controller routes, list/detail/form templates, selector fragments, form JavaScript, Debit Memo detail allocate shortcut/history, and Vendor Bill detail apply shortcut/history.
- **Tests:** `mvn test -Dtest="DebitMemoAllocationControllerTest,DebitMemoAllocationWebMapperTest,DebitMemoAllocationTemplateTest,DebitMemoTemplateTest,VendorBillTemplateTest"` passed with 22 tests.
- **Notes:** The list row destructive action uses draft `cancel` rather than `delete` because the Phase E permission contract provides `DEBIT-MEMO-ALLOCATION_CANCEL` and no delete permission. Vendor Bill detail computes `canApplyDebitMemo` through the eligible Debit Memo selector with page size 1 so the shortcut is hidden when no DM candidate exists. Existing Debit Memo template tests were updated because Phase E intentionally replaces the allocation-history placeholder with real `DEBIT-MEMO-ALLOCATION` links.
