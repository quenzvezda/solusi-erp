# Phase E Plan: Debit Memo Allocation
Status: READY
Created: 2026-06-05
Source Brainstorm: [docs/brainstorming/2026-06-02-vendor-debit-memo.md](../brainstorming/2026-06-02-vendor-debit-memo.md)
Depends on:
- Phase C: [Vendor Bill Settlement Refactor](2026-06-02-phase-c-vendor-bill-settlement-refactor.md)
- Phase D: [Vendor Debit Memo Core](2026-06-02-phase-d-debit-memo-core.md)
Progress Report: [docs/reports/2026-06-02-phase-e-debit-memo-allocation.md](../reports/2026-06-02-phase-e-debit-memo-allocation.md)

Phase E implements Debit Memo Allocation (DMA): a draftable AP settlement document that applies one Debit Memo to one or more confirmed Vendor Bills. Confirmation posts `DEBIT_MEMO_APPLICATION`, reduces Vendor Bill outstanding through the neutral settlement projection, updates Debit Memo settlement status, and supports linked journal reversal.

## Current Handoff Notes

- Latest migration in the codebase is `V73__Add_Debit_Memo_Core.sql`; Phase E should use `V74__Add_Debit_Memo_Allocation.sql` in both MariaDB and H2 migration folders.
  ref: [src/main/resources/db/migration/V73__Add_Debit_Memo_Core.sql:1](../../src/main/resources/db/migration/V73__Add_Debit_Memo_Core.sql)
- Phase D has a replaceable `DebitMemoAllocationConsumptionPort` stub. Phase E must replace `NoopDebitMemoAllocationConsumptionAdapter` with a real DMA-backed adapter.
  ref: [src/main/java/com/solusi/erp/accountspayable/debitmemo/domain/port/DebitMemoAllocationConsumptionPort.java:3](../../src/main/java/com/solusi/erp/accountspayable/debitmemo/domain/port/DebitMemoAllocationConsumptionPort.java)
  ref: [src/main/java/com/solusi/erp/accountspayable/debitmemo/infrastructure/config/DebitMemoConfig.java:39](../../src/main/java/com/solusi/erp/accountspayable/debitmemo/infrastructure/config/DebitMemoConfig.java)
- Phase D Debit Memo detail still calculates `settledAmount=0` and `remainingAmount=grossAmountOriginal`; Phase E must replace this with confirmed DMA consumption.
  ref: [src/main/java/com/solusi/erp/accountspayable/debitmemo/application/usecase/query/GetDebitMemoDetailUseCaseImpl.java:29](../../src/main/java/com/solusi/erp/accountspayable/debitmemo/application/usecase/query/GetDebitMemoDetailUseCaseImpl.java)
- Phase C Vendor Bill settlement summary currently includes only confirmed Vendor Payment lines; Phase E must include confirmed DMA lines.
  ref: [src/main/java/com/solusi/erp/accountspayable/vendorbill/infrastructure/adapter/VendorBillSettlementSummaryAdapter.java:30](../../src/main/java/com/solusi/erp/accountspayable/vendorbill/infrastructure/adapter/VendorBillSettlementSummaryAdapter.java)
- Vendor Payment stale-confirm guard also only subtracts confirmed payments. It must subtract confirmed active DMA lines too, otherwise Vendor Payment and DMA can over-settle the same bill.
  ref: [src/main/java/com/solusi/erp/accountspayable/vendorpayment/infrastructure/adapter/VendorBillPaymentUpdateAdapter.java:92](../../src/main/java/com/solusi/erp/accountspayable/vendorpayment/infrastructure/adapter/VendorBillPaymentUpdateAdapter.java)
- Scope deferred from Phase E: Vendor Refund, Purchase Return confirmed reversal, tax override on DMA, partial Purchase Return reversal, and legacy corrective journals.
  ref: [docs/brainstorming/2026-06-02-vendor-debit-memo.md:1697](../brainstorming/2026-06-02-vendor-debit-memo.md)

## Task 1: Database, Sequence, Permissions, And Accounting Contract [x]

Create the persistent DMA contract and journal schema event before app code depends on it.

**Depends on:** none
**Ref mod:** `accountspayable.vendorpayment`, `accountspayable.debitmemo`, `accounting.schema`

- [x] Add `SchemaEventType.DEBIT_MEMO_APPLICATION`.
      ref: [src/main/java/com/solusi/erp/accounting/schema/domain/model/SchemaEventType.java:7](../../src/main/java/com/solusi/erp/accounting/schema/domain/model/SchemaEventType.java)
- [x] Add `JournalVariable` values for `DMA_AP_AMT`, `DMA_GRIR_CLEARING_AMT`, `DMA_TAX_AMT`, `DMA_FX_LOSS_AMT`, and `DMA_FX_GAIN_AMT`, all bound to `DEBIT_MEMO_APPLICATION`.
      ref: [src/main/java/com/solusi/erp/accounting/journal/domain/model/JournalVariable.java:7](../../src/main/java/com/solusi/erp/accounting/journal/domain/model/JournalVariable.java)
      ref: [docs/brainstorming/2026-06-02-vendor-debit-memo.md:628](../brainstorming/2026-06-02-vendor-debit-memo.md)
- [x] Create `V74__Add_Debit_Memo_Allocation.sql` in `src/main/resources/db/migration` and `src/main/resources/db/migration-h2`.
      ref: [src/main/resources/db/migration/V63__Add_Vendor_Payment_Module.sql:4](../../src/main/resources/db/migration/V63__Add_Vendor_Payment_Module.sql)
      ref: [docs/reports/2026-06-02-phase-d-debit-memo-core.md:11](../reports/2026-06-02-phase-d-debit-memo-core.md)
- [x] Add `ap_debit_memo_allocations` header table with `code`, `debit_memo_id`, `debit_memo_code`, `allocation_date`, `status`, total gross/original/base/FX snapshots, `apply_journal_entry_id`, `reversal_journal_entry_id`, `reversal_date`, `reversal_reason`, audit columns, and optimistic `version`.
      ref: [docs/brainstorming/2026-06-02-vendor-debit-memo.md:1353](../brainstorming/2026-06-02-vendor-debit-memo.md)
- [x] Add `ap_debit_memo_allocation_lines` with one row per Vendor Bill header: `vendor_bill_id`, `vendor_bill_code`, draft remaining/outstanding snapshots, `applied_gross_original`, prorated DPP/tax original, GRIR/tax base reversal, Vendor Bill exchange rate, AP reduction base, FX gain/loss base.
      ref: [docs/brainstorming/2026-06-02-vendor-debit-memo.md:1374](../brainstorming/2026-06-02-vendor-debit-memo.md)
- [x] Add db guards and indexes: unique DMA code, FK to Debit Memo/Vendor Bill/journal entries, index by Debit Memo/status, index by Vendor Bill/status, list keyword index, and a unique `(debit_memo_allocation_id, vendor_bill_id)` line guard.
      ref: [docs/brainstorming/2026-06-02-vendor-debit-memo.md:208](../brainstorming/2026-06-02-vendor-debit-memo.md)
- [x] Add `system_sequences` row `DEBIT_MEMO_ALLOCATION` with `DMA-{date:yyyyMM}-{seq}`.
      ref: [docs/brainstorming/2026-06-02-vendor-debit-memo.md:183](../brainstorming/2026-06-02-vendor-debit-memo.md)
- [x] Add permission group/menu entry under Accounts Payable, probably `AP-04`, URL `/accounts-payable/debit-memo-allocations`, icon `ti-file-check`, sort after Debit Memo.
      ref: [src/main/resources/db/migration/V73__Add_Debit_Memo_Core.sql:86](../../src/main/resources/db/migration/V73__Add_Debit_Memo_Core.sql)
- [x] Add permissions: `DEBIT-MEMO-ALLOCATION_READ`, `CREATE`, `UPDATE`, `CONFIRM`, `CANCEL`, `REVERSE`, and grant them to `ROLE_ADMIN`.
      ref: [docs/brainstorming/2026-06-02-vendor-debit-memo.md:937](../brainstorming/2026-06-02-vendor-debit-memo.md)
- [x] Seed active accounting schema for `DEBIT_MEMO_APPLICATION`: AP debit, GRIR credit, Input VAT credit, FX loss debit, FX gain credit.
      ref: [src/main/resources/db/migration/V62__Vendor_Payment_Accounting_Schema.sql:4](../../src/main/resources/db/migration/V62__Vendor_Payment_Accounting_Schema.sql)
      ref: [docs/database/dev-seeder/D220__accounting_schema.sql:74](../database/dev-seeder/D220__accounting_schema.sql)
- [x] Update `docs/database/dev-seeder/D220__accounting_schema.sql` and H2 E2E seed refresh block for the new event and variables.
      ref: [src/main/resources/db/migration-h2/V9000__e2e_seed_data.sql:259](../../src/main/resources/db/migration-h2/V9000__e2e_seed_data.sql)
- [x] TEST: Add migration contract test similar to `DebitMemoCoreMigrationTest` for V74 tables, sequence, permissions, schema event, MariaDB/H2 token parity, and E2E seed tokens.
      ref: [src/test/java/com/solusi/erp/accountspayable/debitmemo/infrastructure/persistence/DebitMemoCoreMigrationTest.java:18](../../src/test/java/com/solusi/erp/accountspayable/debitmemo/infrastructure/persistence/DebitMemoCoreMigrationTest.java)
- [x] TEST: Extend journal variable/message tests to cover `DEBIT_MEMO_APPLICATION`.
      ref: [docs/plans/2026-06-02-phase-b-purchase-return-accounting.md](2026-06-02-phase-b-purchase-return-accounting.md)

**Validation criteria:**
- `mvn test -Dtest="DebitMemoAllocationMigrationTest,JournalVariableTest,JournalMessageBundleTest"`
- `mvn test -Dtest="*MigrationTest"`

## Task 2: DMA Domain Model, Lifecycle, And Allocation Math [x]

Model DMA as its own aggregate with draft snapshots, confirm/reverse immutability, line validation, proration, and FX snapshot fields.

**Depends on:** Task 1
**Ref mod:** `accountspayable.vendorpayment`, `accountspayable.debitmemo`

- [x] Create `accountspayable.debitmemoallocation` package with `domain/model`, `domain/repository`, `domain/port`, `application/usecase`, `infrastructure`, and `web`.
      ref: [docs/AGENTS.md:40](../AGENTS.md)
- [x] Add `DebitMemoAllocationStatus` enum: `DRAFT`, `CONFIRMED`, `CANCELLED`, `REVERSED`.
      ref: [docs/brainstorming/2026-06-02-vendor-debit-memo.md:183](../brainstorming/2026-06-02-vendor-debit-memo.md)
- [x] Add `DebitMemoAllocation` aggregate with one Debit Memo header identity, allocation date, status, journal ids, reversal metadata, and line collection.
      ref: [src/main/java/com/solusi/erp/accountspayable/vendorpayment/domain/model/VendorPayment.java:12](../../src/main/java/com/solusi/erp/accountspayable/vendorpayment/domain/model/VendorPayment.java)
- [x] Add `DebitMemoAllocationLine` value object with Vendor Bill identity, draft snapshots, applied gross original, prorated DPP/tax, base amounts, exchange-rate snapshot, AP reduction base, and FX gain/loss base.
      ref: [src/main/java/com/solusi/erp/accountspayable/vendorpayment/domain/model/VendorPaymentLine.java:5](../../src/main/java/com/solusi/erp/accountspayable/vendorpayment/domain/model/VendorPaymentLine.java)
- [x] Enforce domain invariants: one DMA references exactly one DM, at least one line, no duplicate Vendor Bill in one DMA, applied gross > 0, total applied <= draft/current DM remaining when command supplies current values, line applied <= current VB outstanding when command supplies current values.
      ref: [docs/brainstorming/2026-06-02-vendor-debit-memo.md:1423](../brainstorming/2026-06-02-vendor-debit-memo.md)
- [x] Implement lifecycle methods: update only while `DRAFT`, cancel only while `DRAFT`, confirm only from `DRAFT`, reverse only from `CONFIRMED`, no edit/delete once confirmed.
      ref: [docs/brainstorming/2026-06-02-vendor-debit-memo.md:183](../brainstorming/2026-06-02-vendor-debit-memo.md)
- [x] Add allocation math service/policy that prorates gross into DPP/tax using Debit Memo totals and uses remainder on the last line/last confirmed consumption so cumulative original/base amounts equal DM snapshots.
      ref: [docs/brainstorming/2026-06-02-vendor-debit-memo.md:484](../brainstorming/2026-06-02-vendor-debit-memo.md)
- [x] Add FX calculation: AP reduction base = applied gross original * Vendor Bill exchange rate; GRIR/tax base use DM historical base ratio; difference becomes FX gain/loss.
      ref: [docs/brainstorming/2026-06-02-vendor-debit-memo.md:521](../brainstorming/2026-06-02-vendor-debit-memo.md)
- [x] Add message keys for every domain guard; do not hardcode Indonesian/English in use cases.
      ref: [docs/AGENTS.md:91](../AGENTS.md)
- [x] TEST: Add pure JUnit domain tests for create/update/cancel/confirm/reverse lifecycle, duplicate bill guard, non-positive amount guard, over-remaining guard, over-outstanding guard, proration remainder, zero tax, and FX gain/loss.
      ref: [src/test/java/com/solusi/erp/accountspayable/debitmemo/domain/model/DebitMemoTest.java:17](../../src/test/java/com/solusi/erp/accountspayable/debitmemo/domain/model/DebitMemoTest.java)

**Validation criteria:**
- `mvn test -Dtest="DebitMemoAllocationTest,DebitMemoAllocationProrationTest"`

## Task 3: Persistence, Repository, Query Read Models, And Config Wiring [x]

Persist DMA and provide read models for list/detail/history/selectors without coupling web to JPA.

**Depends on:** Tasks 1-2
**Ref mod:** `accountspayable.debitmemo`, `accountspayable.vendorpayment`

- [x] Add JPA entities and MapStruct mapper for DMA header/lines, including audit fields and journal reference ids.
      ref: [src/main/java/com/solusi/erp/accountspayable/debitmemo/infrastructure/persistence/DebitMemoEntity.java](../../src/main/java/com/solusi/erp/accountspayable/debitmemo/infrastructure/persistence/DebitMemoEntity.java)
- [x] Add `DebitMemoAllocationRepository` with `save`, `findById`, filtered list, `existsActiveConsumptionByDebitMemoId`, `sumConfirmedAppliedByDebitMemoIds`, and history queries for DM/VB details.
      ref: [src/main/java/com/solusi/erp/accountspayable/debitmemo/domain/repository/DebitMemoRepository.java](../../src/main/java/com/solusi/erp/accountspayable/debitmemo/domain/repository/DebitMemoRepository.java)
- [x] Add query use cases/read models: DMA list, DMA detail, allocation history for Debit Memo detail, allocation history for Vendor Bill detail, and eligible selectors.
      ref: [src/main/java/com/solusi/erp/accountspayable/debitmemo/application/usecase/query/DebitMemoDetailView.java:9](../../src/main/java/com/solusi/erp/accountspayable/debitmemo/application/usecase/query/DebitMemoDetailView.java)
- [x] Add `DebitMemoAllocationConfig` under `infrastructure/config`, matching AP convention, and wire transactional command/query beans.
      ref: [src/main/java/com/solusi/erp/accountspayable/debitmemo/infrastructure/config/DebitMemoConfig.java:30](../../src/main/java/com/solusi/erp/accountspayable/debitmemo/infrastructure/config/DebitMemoConfig.java)
- [x] Replace `NoopDebitMemoAllocationConsumptionAdapter` in `DebitMemoConfig` with a real adapter backed by DMA confirmed/non-reversed records.
      ref: [src/main/java/com/solusi/erp/accountspayable/debitmemo/infrastructure/config/DebitMemoConfig.java:39](../../src/main/java/com/solusi/erp/accountspayable/debitmemo/infrastructure/config/DebitMemoConfig.java)
- [x] Extend Debit Memo detail/list summary calculation to include confirmed, non-reversed DMA totals and update remaining/status display.
      ref: [src/main/java/com/solusi/erp/accountspayable/debitmemo/application/usecase/query/GetDebitMemoDetailUseCaseImpl.java:29](../../src/main/java/com/solusi/erp/accountspayable/debitmemo/application/usecase/query/GetDebitMemoDetailUseCaseImpl.java)
- [x] Add locking/query ports for confirm/reverse: lock Debit Memo header, lock all target Vendor Bills, compute current DM remaining, compute current VB outstanding, and update settlement statuses atomically.
      ref: [docs/brainstorming/2026-06-02-vendor-debit-memo.md:566](../brainstorming/2026-06-02-vendor-debit-memo.md)
- [x] TEST: Add repository tests for persist/reload, line mapping, filtered list, history queries, active-consumption check, and sum consumption query.
      ref: [src/test/java/com/solusi/erp/accountspayable/debitmemo/infrastructure/persistence/DebitMemoRepositoryImplTest.java](../../src/test/java/com/solusi/erp/accountspayable/debitmemo/infrastructure/persistence/DebitMemoRepositoryImplTest.java)
- [x] TEST: Add config test proving DMA repository, adapters, and use cases are wired.
      ref: [src/test/java/com/solusi/erp/accountspayable/debitmemo/infrastructure/config/DebitMemoConfigTest.java:27](../../src/test/java/com/solusi/erp/accountspayable/debitmemo/infrastructure/config/DebitMemoConfigTest.java)

**Validation criteria:**
- `mvn test -Dtest="DebitMemoAllocationRepositoryImplTest,DebitMemoAllocationQueryUseCaseTest,DebitMemoAllocationConfigTest,DebitMemoCommandUseCaseTest"`

## Task 4: Cross-Slice Vendor Bill Settlement Integration [x]

Make Vendor Bill outstanding and Vendor Payment revalidation aware of confirmed DMA consumption before DMA confirmation is exposed.

**Depends on:** Task 3
**Ref mod:** `accountspayable.vendorbill`, `accountspayable.vendorpayment`

- [x] Update `VendorBillSettlementSummaryAdapter` SQL to subtract confirmed, non-reversed DMA lines in `debitMemoAppliedAmount` and `outstandingAmount`.
      ref: [src/main/java/com/solusi/erp/accountspayable/vendorbill/infrastructure/adapter/VendorBillSettlementSummaryAdapter.java:30](../../src/main/java/com/solusi/erp/accountspayable/vendorbill/infrastructure/adapter/VendorBillSettlementSummaryAdapter.java)
- [x] Update `PayableVendorBillQueryAdapter` so Vendor Payment selectors exclude DMA-applied amounts and show accurate outstanding after DMA confirmation/reversal.
      ref: [src/main/java/com/solusi/erp/accountspayable/vendorpayment/infrastructure/adapter/PayableVendorBillQueryAdapter.java:20](../../src/main/java/com/solusi/erp/accountspayable/vendorpayment/infrastructure/adapter/PayableVendorBillQueryAdapter.java)
- [x] Update `VendorBillPaymentUpdateAdapter.lockAndValidatePayment(...)` to lock and recalc outstanding including confirmed DMA lines.
      ref: [src/main/java/com/solusi/erp/accountspayable/vendorpayment/infrastructure/adapter/VendorBillPaymentUpdateAdapter.java:92](../../src/main/java/com/solusi/erp/accountspayable/vendorpayment/infrastructure/adapter/VendorBillPaymentUpdateAdapter.java)
- [x] Update `VendorBillPaymentUpdateAdapter.updateSettlementStatus(...)` so settlement status considers paid amount + DMA applied amount.
      ref: [src/main/java/com/solusi/erp/accountspayable/vendorpayment/infrastructure/adapter/VendorBillPaymentUpdateAdapter.java:53](../../src/main/java/com/solusi/erp/accountspayable/vendorpayment/infrastructure/adapter/VendorBillPaymentUpdateAdapter.java)
- [x] Add or rename a neutral port if the existing `VendorBillPaymentUpdatePort` name becomes misleading after DMA owns the same logic; keep web/controllers dependent only on use cases/ports.
      ref: [docs/AGENTS.md:184](../AGENTS.md)
- [x] Add specific stale messages for payment/DMA races: Vendor Bill outstanding changed, Debit Memo remaining changed.
      ref: [docs/brainstorming/2026-06-02-vendor-debit-memo.md:591](../brainstorming/2026-06-02-vendor-debit-memo.md)
- [x] TEST: Extend settlement summary adapter tests to cover paid-only, DMA-only, mixed paid+DMA, over-applied clamp, and reversal restored outstanding.
      ref: [docs/reports/2026-06-02-phase-c-vendor-bill-settlement-refactor.md:31](../reports/2026-06-02-phase-c-vendor-bill-settlement-refactor.md)
- [x] TEST: Extend Vendor Payment confirm tests so a stale payment draft fails when a confirmed DMA already consumed the outstanding.
      ref: [src/test/java/com/solusi/erp/accountspayable/vendorpayment/application/usecase/command/ConfirmVendorPaymentUseCaseTest.java:66](../../src/test/java/com/solusi/erp/accountspayable/vendorpayment/application/usecase/command/ConfirmVendorPaymentUseCaseTest.java)

**Validation criteria:**
- `mvn test -Dtest="VendorBillSettlementSummaryAdapterTest,*Payable*Test,ConfirmVendorPaymentUseCaseTest,VendorPaymentConfigTest"`

## Task 5: Draft DMA Use Cases, Eligible Selectors, And Stale Snapshot Display

Implement create/update/cancel draft behavior and query-level eligible DM/VB selectors.

**Depends on:** Tasks 2-4
**Ref mod:** `accountspayable.vendorpayment`, `purchasing.purchaseorder` modal selector pattern

- [ ] Add `CreateDebitMemoAllocationUseCase` supporting two entry points: from Debit Memo detail with DM preselected, and from Vendor Bill detail after user selects eligible DM.
      ref: [docs/brainstorming/2026-06-02-vendor-debit-memo.md:728](../brainstorming/2026-06-02-vendor-debit-memo.md)
- [ ] Add `UpdateDebitMemoAllocationUseCase` for `DRAFT` only; allow changing allocation date, notes, and line amounts, but not swapping the Debit Memo header unless a clear product decision is made during implementation.
      ref: [docs/brainstorming/2026-06-02-vendor-debit-memo.md:183](../brainstorming/2026-06-02-vendor-debit-memo.md)
- [ ] Add `CancelDebitMemoAllocationUseCase` for `DRAFT -> CANCELLED`.
      ref: [docs/brainstorming/2026-06-02-vendor-debit-memo.md:960](../brainstorming/2026-06-02-vendor-debit-memo.md)
- [ ] Implement eligible Vendor Bill selector port: confirmed Vendor Bills, settlement `OPEN/PARTIALLY_SETTLED`, current outstanding > 0, same vendor/currency as Debit Memo, query-level search/pagination.
      ref: [docs/brainstorming/2026-06-02-vendor-debit-memo.md:782](../brainstorming/2026-06-02-vendor-debit-memo.md)
      ref: [docs/spec/modal-selector.md:50](../spec/modal-selector.md)
- [ ] Implement eligible Debit Memo selector port for Vendor Bill shortcut: status `OPEN/PARTIALLY_SETTLED`, current remaining > 0, same vendor/currency as Vendor Bill, query-level search/pagination.
      ref: [docs/brainstorming/2026-06-02-vendor-debit-memo.md:807](../brainstorming/2026-06-02-vendor-debit-memo.md)
- [ ] Store draft snapshots but never treat draft as reservation. If current values differ from draft snapshots, expose `stale` flags/messages in form read model.
      ref: [docs/brainstorming/2026-06-02-vendor-debit-memo.md:1402](../brainstorming/2026-06-02-vendor-debit-memo.md)
- [ ] Add command/request validation for allocation date period only at confirm, not draft save, unless existing project pattern proves draft dates are period-guarded elsewhere.
      ref: [docs/brainstorming/2026-06-02-vendor-debit-memo.md:1204](../brainstorming/2026-06-02-vendor-debit-memo.md)
- [ ] TEST: Add use case tests for DM/VB eligibility, draft create from DM, draft create from VB shortcut, update recalculation, duplicate bill guard, stale snapshot display, and cancel draft.
      ref: [src/test/java/com/solusi/erp/accountspayable/vendorpayment/application/usecase/command/ConfirmVendorPaymentUseCaseTest.java:32](../../src/test/java/com/solusi/erp/accountspayable/vendorpayment/application/usecase/command/ConfirmVendorPaymentUseCaseTest.java)

**Validation criteria:**
- `mvn test -Dtest="CreateDebitMemoAllocationUseCaseTest,UpdateDebitMemoAllocationUseCaseTest,CancelDebitMemoAllocationUseCaseTest,DebitMemoAllocationSelectorUseCaseTest"`

## Task 6: Confirm DMA Use Case And Journal Posting

Confirm a draft DMA atomically: lock current records, revalidate latest balances, post journal, persist snapshots, and update settlement statuses.

**Depends on:** Tasks 1-5
**Ref mod:** `accountspayable.vendorpayment`, `accounting.journal`

- [ ] Add `ConfirmDebitMemoAllocationUseCase` that loads the draft, calls `confirm()`, locks Debit Memo + all target Vendor Bills, recalculates current remaining/outstanding, and rejects stale/overapplied drafts before posting.
      ref: [docs/brainstorming/2026-06-02-vendor-debit-memo.md:566](../brainstorming/2026-06-02-vendor-debit-memo.md)
- [ ] Use `PostJournalForEventUseCase` with `SchemaEventType.DEBIT_MEMO_APPLICATION`, source type `DEBIT_MEMO_ALLOCATION`, source id/code, `allocationDate`, and DMA variables.
      ref: [src/main/java/com/solusi/erp/accountspayable/vendorpayment/application/usecase/command/ConfirmVendorPaymentUseCaseImpl.java:72](../../src/main/java/com/solusi/erp/accountspayable/vendorpayment/application/usecase/command/ConfirmVendorPaymentUseCaseImpl.java)
- [ ] Use original/base maps only if the existing journal posting engine requires original-currency audit for this event; otherwise store immutable original/base snapshots on DMA lines and pass base `values` to journal.
      ref: [src/main/java/com/solusi/erp/accounting/journal/application/usecase/command/JournalPostingCommand.java:10](../../src/main/java/com/solusi/erp/accounting/journal/application/usecase/command/JournalPostingCommand.java)
- [ ] Persist `applyJournalEntryId` if `PostJournalForEventUseCase` currently returns or can expose the posted id; if it remains void, add a focused query-by-source adapter or update the journal use case contract in a separate, tested step.
      ref: [src/main/java/com/solusi/erp/accounting/journal/application/usecase/command/PostJournalForEventUseCase.java:3](../../src/main/java/com/solusi/erp/accounting/journal/application/usecase/command/PostJournalForEventUseCase.java)
- [ ] Update Debit Memo settlement status to `OPEN/PARTIALLY_SETTLED/SETTLED` based on confirmed, non-reversed DMA consumption.
      ref: [src/main/java/com/solusi/erp/accountspayable/debitmemo/domain/model/DebitMemo.java:118](../../src/main/java/com/solusi/erp/accountspayable/debitmemo/domain/model/DebitMemo.java)
- [ ] Update Vendor Bill settlement statuses for target bills after journal posting and save.
      ref: [src/main/java/com/solusi/erp/accountspayable/vendorpayment/application/usecase/command/ConfirmVendorPaymentUseCaseImpl.java:86](../../src/main/java/com/solusi/erp/accountspayable/vendorpayment/application/usecase/command/ConfirmVendorPaymentUseCaseImpl.java)
- [ ] Ensure transaction order prevents side effects on rejected stale drafts: validate locks before journal posting, save only after journal succeeds, then settlement projection update.
      ref: [src/test/java/com/solusi/erp/accountspayable/vendorpayment/application/usecase/command/ConfirmVendorPaymentUseCaseTest.java:58](../../src/test/java/com/solusi/erp/accountspayable/vendorpayment/application/usecase/command/ConfirmVendorPaymentUseCaseTest.java)
- [ ] TEST: Add confirm use case tests for happy path, AP/GRIR/tax/FX variable values, period closed rejection, stale DM remaining, stale VB outstanding, vendor/currency mismatch, tax mismatch policy, duplicate bill, and no journal on guard failure.
      ref: [src/test/java/com/solusi/erp/accountspayable/vendorpayment/application/usecase/command/ConfirmVendorPaymentUseCaseTest.java:84](../../src/test/java/com/solusi/erp/accountspayable/vendorpayment/application/usecase/command/ConfirmVendorPaymentUseCaseTest.java)

**Validation criteria:**
- `mvn test -Dtest="ConfirmDebitMemoAllocationUseCaseTest,DebitMemoAllocationConfigTest,VendorBillSettlementSummaryAdapterTest"`

## Task 7: Reverse Confirmed DMA

Reverse a confirmed allocation using the generic linked journal reversal and restore settlement projections.

**Depends on:** Task 6
**Ref mod:** `accounting.journal`, `inventory.goodsissue` cancellation reversal

- [ ] Add `ReverseDebitMemoAllocationUseCase` for `CONFIRMED -> REVERSED` with required `reversalDate` and `reversalReason`.
      ref: [docs/brainstorming/2026-06-02-vendor-debit-memo.md:960](../brainstorming/2026-06-02-vendor-debit-memo.md)
- [ ] Validate `applyJournalEntryId` exists, DMA is not already reversed, and reversal date accounting period is open through `ReversePostedJournalUseCase`.
      ref: [src/main/java/com/solusi/erp/accounting/journal/application/usecase/command/ReversePostedJournalUseCaseImpl.java:23](../../src/main/java/com/solusi/erp/accounting/journal/application/usecase/command/ReversePostedJournalUseCaseImpl.java)
- [ ] Call `ReversePostedJournalUseCase` using the DMA apply journal id; persist `reversalJournalEntryId`, `reversalDate`, and `reversalReason`.
      ref: [src/main/java/com/solusi/erp/accounting/journal/application/usecase/command/ReversePostedJournalCommand.java:5](../../src/main/java/com/solusi/erp/accounting/journal/application/usecase/command/ReversePostedJournalCommand.java)
- [ ] Recompute Debit Memo settlement status and target Vendor Bill settlement statuses after marking DMA reversed.
      ref: [docs/brainstorming/2026-06-02-vendor-debit-memo.md:969](../brainstorming/2026-06-02-vendor-debit-memo.md)
- [ ] Ensure Debit Memo cancel guard sees any active confirmed DMA as consumption, while reversed/cancelled DMA no longer blocks cancel.
      ref: [src/main/java/com/solusi/erp/accountspayable/debitmemo/application/usecase/command/CancelDebitMemoUseCaseImpl.java](../../src/main/java/com/solusi/erp/accountspayable/debitmemo/application/usecase/command/CancelDebitMemoUseCaseImpl.java)
- [ ] TEST: Add reverse use case tests for happy path, required reason, closed period, already reversed, missing apply journal, settlement restoration, and Debit Memo cancel blocker behavior.
      ref: [docs/reports/2026-06-02-phase-a-generic-reversal-foundation.md:13](../reports/2026-06-02-phase-a-generic-reversal-foundation.md)

**Validation criteria:**
- `mvn test -Dtest="ReverseDebitMemoAllocationUseCaseTest,DebitMemoCommandUseCaseTest,ReversePostedJournalUseCaseTest"`

## Task 8: Web Layer, Templates, JavaScript, And Shortcuts

Expose list/detail/form flows, DM/VB shortcuts, selector fragments, action buttons, and interactive allocation recap.

**Depends on:** Tasks 5-7
**Ref mod:** `accountspayable.vendorpayment`, `accountspayable.vendorbill`, `accountspayable.debitmemo`

- [ ] Add `DebitMemoAllocationController` at `/accounts-payable/debit-memo-allocations` with list/detail/create/edit/save/update/confirm/cancel/reverse routes and `@PreAuthorize` guards.
      ref: [src/main/java/com/solusi/erp/accountspayable/vendorpayment/web/controller/VendorPaymentController.java:40](../../src/main/java/com/solusi/erp/accountspayable/vendorpayment/web/controller/VendorPaymentController.java)
      ref: [docs/tests/playwright-pitfalls.md:110](../tests/playwright-pitfalls.md)
- [ ] Add DTOs for save request, line request, summary/detail response, selector rows, reverse request, and form view state. Date fields must use `@DateTimeFormat(pattern = "yyyy-MM-dd")`.
      ref: [docs/spec/datetime-standards.md:86](../spec/datetime-standards.md)
- [ ] Add list page with keyword/status filters, pagination/sorting, and actions View/Edit Draft/Delete or Cancel Draft according to local convention.
      ref: [docs/AGENTS.md:64](../AGENTS.md)
- [ ] Add detail page with header, linked Debit Memo/Vendor Bills/journal entries, line snapshots, status badge, confirm/cancel/reverse buttons using `ErpForm.postAction`.
      ref: [docs/spec/action-buttons.md:14](../spec/action-buttons.md)
      ref: [src/main/resources/templates/accountspayable/vendor-payments/detail.html:27](../../src/main/resources/templates/accountspayable/vendor-payments/detail.html)
- [ ] Add form page using AJAX JSON (`data-ajax-form="true"`) with stable IDs, allocation date Flatpickr, line table, stale warning, gross input per line using AutoNumeric, and recap Applied/Unapplied/Remaining.
      ref: [docs/spec/form-submission.md:22](../spec/form-submission.md)
      ref: [docs/spec/numeric-standards.md:5](../spec/numeric-standards.md)
      ref: [src/main/resources/templates/accountspayable/vendor-payments/form.html:26](../../src/main/resources/templates/accountspayable/vendor-payments/form.html)
- [ ] Add modal selector fragments for eligible Vendor Bills and eligible Debit Memos. Root fragment id must match `hx-target`; all filtering must be query-level.
      ref: [docs/spec/modal-selector.md:20](../spec/modal-selector.md)
      ref: [src/main/resources/templates/accountspayable/vendor-payments/fragments/bank-account-selector.html:4](../../src/main/resources/templates/accountspayable/vendor-payments/fragments/bank-account-selector.html)
- [ ] Add page-specific JS under `static/js/accountspayable/debit-memo-allocations/form.js` to open selectors, map `data-*` payload to lines, prevent duplicate bills, reindex after removal, initialize numeric inputs, calculate recap, and validate at submit capture phase.
      ref: [docs/spec/page-specific-scripts.md:13](../spec/page-specific-scripts.md)
      ref: [src/main/resources/static/js/accountspayable/vendor-payments/form.js:67](../../src/main/resources/static/js/accountspayable/vendor-payments/form.js)
- [ ] Add Debit Memo detail Allocate action when status is `OPEN/PARTIALLY_SETTLED` and remaining > 0; link to DMA create with DM preselected.
      ref: [src/main/resources/templates/accountspayable/debit-memos/detail.html:153](../../src/main/resources/templates/accountspayable/debit-memos/detail.html)
- [ ] Replace Debit Memo allocation-history placeholder with actual DMA history table and links.
      ref: [src/main/resources/templates/accountspayable/debit-memos/detail.html:153](../../src/main/resources/templates/accountspayable/debit-memos/detail.html)
- [ ] Add Vendor Bill detail Apply Debit Memo shortcut only when document status is `CONFIRMED`, settlement is `OPEN/PARTIALLY_SETTLED`, outstanding > 0, and at least one eligible DM exists.
      ref: [docs/brainstorming/2026-06-02-vendor-debit-memo.md:756](../brainstorming/2026-06-02-vendor-debit-memo.md)
      ref: [src/main/resources/templates/accountspayable/vendor-bills/detail.html:51](../../src/main/resources/templates/accountspayable/vendor-bills/detail.html)
- [ ] Add allocation history section to Vendor Bill detail showing confirmed/reversed DMA lines.
      ref: [docs/brainstorming/2026-06-02-vendor-debit-memo.md:766](../brainstorming/2026-06-02-vendor-debit-memo.md)
- [ ] TEST: Add controller tests for routes, model attrs, request mapping, `@PreAuthorize` values, and JSON action responses.
      ref: [src/main/java/com/solusi/erp/accountspayable/debitmemo/web/controller/DebitMemoController.java:52](../../src/main/java/com/solusi/erp/accountspayable/debitmemo/web/controller/DebitMemoController.java)
- [ ] TEST: Add template tests for form attributes, selector shell/fragment ids, action button data attributes, permission visibility, DM detail allocate action, and VB detail shortcut visibility.
      ref: [src/test/java/com/solusi/erp/accountspayable/vendorpayment/web/template/VendorPaymentTemplateTest.java:55](../../src/test/java/com/solusi/erp/accountspayable/vendorpayment/web/template/VendorPaymentTemplateTest.java)

**Validation criteria:**
- `mvn test -Dtest="DebitMemoAllocationControllerTest,DebitMemoAllocationWebMapperTest,DebitMemoAllocationTemplateTest,DebitMemoTemplateTest,VendorBillTemplateTest"`

## Task 9: i18n, Module Documentation, And Accounting Docs

Document the final Phase E behavior and update user-facing labels/messages.

**Depends on:** Tasks 1-8
**Ref mod:** docs and message bundles

- [ ] Add bilingual message keys for DMA labels, statuses, selector headings, stale warnings, confirm/cancel/reverse success messages, and domain validation errors.
      ref: [src/main/resources/messages.properties:1842](../../src/main/resources/messages.properties)
      ref: [src/main/resources/messages_id.properties:1842](../../src/main/resources/messages_id.properties)
- [ ] Update [docs/modules/accountspayable/debit-memo.md](../modules/accountspayable/debit-memo.md) to replace Phase D deferrals with Phase E allocation behavior, settlement recap, and reversal rules.
      ref: [docs/modules/accountspayable/debit-memo.md:53](../modules/accountspayable/debit-memo.md)
- [ ] Add or update `docs/modules/accountspayable/debit-memo-allocation.md` if the allocation module needs its own page; otherwise keep all DMA detail in debit-memo doc and link from docs index.
      ref: [docs/index.md:34](../index.md)
- [ ] Update Vendor Bill docs to include Debit Memo Applied history, Apply Debit Memo shortcut, and payment/DMA settlement projection.
      ref: [docs/modules/accountspayable/vendor-bill.md](../modules/accountspayable/vendor-bill.md)
- [ ] Update Vendor Payment docs to note outstanding revalidation now subtracts confirmed DMA lines.
      ref: [docs/modules/accountspayable/vendor-payment.md](../modules/accountspayable/vendor-payment.md)
- [ ] Update Accounting Schema docs with `DEBIT_MEMO_APPLICATION` variables and standard mapping.
      ref: [docs/modules/accounting/accounting-schema.md:312](../modules/accounting/accounting-schema.md)
- [ ] Update E2E warmup routes if new DMA routes are part of full suite and cold first-hit proves slow.
      ref: [docs/tests/playwright-pitfalls.md:235](../tests/playwright-pitfalls.md)
- [ ] TEST: Add message bundle/static doc scan tests if local pattern exists; otherwise record docs review in report.

**Validation criteria:**
- `mvn test -Dtest="*MessageBundleTest"`
- Manual doc scan for stale text: `Phase E deferred`, `allocation-history.empty`, and `debitMemoAppliedAmount = 0` in AP docs/templates should be intentional only.

## Task 10: Playwright E2E And Regression Gate

Prove the browser flow and cross-module settlement behavior work end-to-end.

**Depends on:** Tasks 1-9
**Ref mod:** existing AP E2E

- [ ] Read Playwright guide and pitfalls before editing specs; do not use `selectTomSelect`, do not rely on `page.on('dialog')` for ERP confirms, and run the spec before marking complete.
      ref: [docs/tests/playwright-pitfalls.md:24](../tests/playwright-pitfalls.md)
      ref: [docs/tests/playwright-e2e-guide.md:14](../tests/playwright-e2e-guide.md)
- [ ] Add `e2e-tests/tests/accountspayable/debit-memo-allocation.spec.ts` or extend AP specs with a focused DMA scenario file.
      ref: [e2e-tests/tests/accountspayable/vendor-payment.spec.ts:316](../../e2e-tests/tests/accountspayable/vendor-payment.spec.ts)
- [ ] Scenario A: create Purchase Return -> generated Debit Memo -> create confirmed Vendor Bill gross -> create DMA draft from Debit Memo detail -> confirm -> verify DM `SETTLED`, VB outstanding reduced/settled, and `DEBIT_MEMO_APPLICATION` journal visible.
      ref: [e2e-tests/tests/procurement/purchase-return.spec.ts](../../e2e-tests/tests/procurement/purchase-return.spec.ts)
- [ ] Scenario B: partial allocation leaves Debit Memo `PARTIALLY_SETTLED` and Vendor Bill `PARTIALLY_SETTLED` or `OPEN` according to remaining outstanding.
      ref: [docs/brainstorming/2026-06-02-vendor-debit-memo.md:484](../brainstorming/2026-06-02-vendor-debit-memo.md)
- [ ] Scenario C: one Debit Memo allocated across two Vendor Bills.
      ref: [docs/brainstorming/2026-06-02-vendor-debit-memo.md:145](../brainstorming/2026-06-02-vendor-debit-memo.md)
- [ ] Scenario D: Vendor Bill detail Apply Debit Memo shortcut opens DM selector and creates a draft with the bill line preselected.
      ref: [docs/brainstorming/2026-06-02-vendor-debit-memo.md:756](../brainstorming/2026-06-02-vendor-debit-memo.md)
- [ ] Scenario E: stale DMA draft cannot confirm after another DMA or Vendor Payment consumes the same Vendor Bill outstanding.
      ref: [e2e-tests/tests/accountspayable/vendor-payment.spec.ts:380](../../e2e-tests/tests/accountspayable/vendor-payment.spec.ts)
- [ ] Scenario F: reverse confirmed DMA restores DM remaining and VB outstanding, and the reversal journal link is visible.
      ref: [docs/brainstorming/2026-06-02-vendor-debit-memo.md:960](../brainstorming/2026-06-02-vendor-debit-memo.md)
- [ ] Add RBAC coverage for DMA list/create visibility if the existing RBAC matrix is being extended for AP resources.
      ref: [docs/tests/playwright-e2e-guide.md:609](../tests/playwright-e2e-guide.md)
- [ ] Run TypeScript compile, list, targeted DMA spec, and cold-cache targeted spec.
      ref: [docs/tests/playwright-pitfalls.md:274](../tests/playwright-pitfalls.md)
- [ ] Run focused Maven gates touched by Phase E.
- [ ] Run final backend gate: `mvn clean test`.
- [ ] Run final E2E gate: `./e2e-tests/scripts/run-e2e.sh` on Linux or `.\\e2e-tests\\scripts\\run-e2e.ps1` on Windows.

**Validation criteria:**
- `cd e2e-tests && npx tsc --noEmit`
- `cd e2e-tests && npx playwright test tests/accountspayable/debit-memo-allocation.spec.ts --list`
- `cd e2e-tests && npx playwright test tests/accountspayable/debit-memo-allocation.spec.ts`
- `cd e2e-tests && rm -rf .auth/ && npx playwright test tests/accountspayable/debit-memo-allocation.spec.ts`
- `mvn clean test`
- Full Playwright runner passes.

## Final Completion Checklist

- [ ] V74 MariaDB and H2 migrations are in sync.
- [ ] `DEBIT_MEMO_APPLICATION` event and schema are seeded in migration, dev seeder, and E2E seed.
- [ ] DMA confirm/reverse are atomic and never leave journal/settlement side effects after failed validation.
- [ ] Vendor Bill outstanding includes confirmed Vendor Payments and confirmed, non-reversed DMA.
- [ ] Vendor Payment confirm revalidation includes confirmed DMA consumption.
- [ ] Debit Memo cancel guard rejects active confirmed DMA consumption and allows cancel after all related DMA are cancelled/reversed.
- [ ] Debit Memo detail and Vendor Bill detail show allocation history.
- [ ] DMA E2E spec was run live, including cold-cache run.
- [ ] `mvn clean test` and full Playwright runner pass before marking Phase E complete.
