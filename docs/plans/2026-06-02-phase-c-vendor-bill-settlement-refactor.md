# Implementation Plan: Vendor Bill Settlement Refactor

> Source: `docs/brainstorming/2026-06-02-vendor-debit-memo.md`
> Created: 2026-06-04
> Sprint: 6 - Debit Memo Foundation
> Status: IN_PROGRESS
>
> **For agentic workers:** execute task-by-task. Explore references fresh before editing each task, update checkbox state incrementally, record deviations in `docs/reports/2026-06-02-phase-c-vendor-bill-settlement-refactor.md`, and do not mark a task complete before its validation command passes.

## 1. Goal

Phase C separates Vendor Bill document lifecycle from settlement lifecycle. Vendor Bill document status becomes `DRAFT / CONFIRMED / CANCELLED`, while payment settlement becomes `OPEN / PARTIALLY_SETTLED / SETTLED` and is computed from confirmed settlement documents.

This phase keeps the current Vendor Payment flow working, adds pessimistic lock and revalidation before payment confirmation posts its journal, and prepares a neutral settlement seam for future Debit Memo Allocation. It does not create Debit Memo, does not create Debit Memo Allocation, and does not post `DEBIT_MEMO_APPLICATION`.

## 2. Locked Decisions

- Replace the single Vendor Bill status meaning with two concepts:
  - `documentStatus`: `DRAFT`, `CONFIRMED`, `CANCELLED`.
  - `settlementStatus`: `OPEN`, `PARTIALLY_SETTLED`, `SETTLED`.
- `PARTIAL_PAID` and `PAID` are no longer valid Vendor Bill document statuses.
- Existing data migration maps:
  - old `DRAFT` -> `document_status='DRAFT'`, `settlement_status=NULL`;
  - old `CANCELLED` -> `document_status='CANCELLED'`, `settlement_status=NULL`;
  - old `CONFIRMED` -> `document_status='CONFIRMED'`, `settlement_status='OPEN'`;
  - old `PARTIAL_PAID` -> `document_status='CONFIRMED'`, `settlement_status='PARTIALLY_SETTLED'`;
  - old `PAID` -> `document_status='CONFIRMED'`, `settlement_status='SETTLED'`.
- Draft and cancelled Vendor Bills are not payable; payable selectors require `document_status='CONFIRMED'` and `settlement_status IN ('OPEN','PARTIALLY_SETTLED')`.
- Outstanding Vendor Bill remains a read-side projection:
  `totalAmount - confirmedPaymentAmount - confirmedDebitMemoAppliedAmount`.
- Phase C only has confirmed Vendor Payment as a real settlement source. `confirmedDebitMemoAppliedAmount` must be surfaced as `0` seam data until Phase E adds DMA tables.
- Vendor Payment confirm must lock all target Vendor Bills, recalculate outstanding, reject stale/over-applied drafts, then post journal and update settlement status atomically.
- Vendor Bill detail should show `Document Status`, `Settlement Status`, `Paid Amount`, `Debit Memo Applied Amount`, and `Outstanding Amount`.
- Vendor Bill invoice creation remains gross from Goods Receipt; Purchase Return and Debit Memo behavior from Phase B/D/E stays out of this phase.
- No version bump is part of the plan file. If implementation is accepted later, follow the project SemVer protocol then.

## 3. Scope Boundary

### Included

- MariaDB and H2 migration for `document_status` and `settlement_status`.
- Vendor Bill domain, persistence, repository, query, web DTO, mapper, template, and tests for separated statuses.
- Settlement projection port/adapter with current confirmed Vendor Payment amount and a zero Debit Memo seam.
- Vendor Payment payable bill query update to use document/settlement statuses.
- Vendor Payment confirm lock and stale outstanding revalidation.
- i18n, docs, focused tests, migration tests, and E2E coverage for Vendor Bill/Vendor Payment settlement behavior.

### Deferred

- Debit Memo header/line creation. Phase D.
- Debit Memo Allocation and `DEBIT_MEMO_APPLICATION`. Phase E.
- Confirmed Purchase Return reversal orchestration. Phase F.
- Vendor Payment reversal/cancellation after confirmation.
- Vendor Bill cancellation after confirmation.
- Vendor Refund.
- Tax mismatch validation between Debit Memo and Vendor Bill.

## 4. Target File Map

### Database

- Create `src/main/resources/db/migration/V72__Refactor_Vendor_Bill_Settlement_Status.sql`
- Create `src/main/resources/db/migration-h2/V72__Refactor_Vendor_Bill_Settlement_Status.sql`
- Modify `src/main/resources/db/migration-h2/V9000__e2e_seed_data.sql` only if E2E seed needs explicit Vendor Bill settlement fixture data.

### Vendor Bill Domain And Persistence

- Modify `src/main/java/com/solusi/erp/accountspayable/vendorbill/domain/model/VendorBill.java`
- Replace or narrow `src/main/java/com/solusi/erp/accountspayable/vendorbill/domain/model/VendorBillStatus.java`
- Create `src/main/java/com/solusi/erp/accountspayable/vendorbill/domain/model/VendorBillDocumentStatus.java` if replacing the old enum cleanly
- Create `src/main/java/com/solusi/erp/accountspayable/vendorbill/domain/model/VendorBillSettlementStatus.java`
- Modify `src/main/java/com/solusi/erp/accountspayable/vendorbill/infrastructure/persistence/VendorBillEntity.java`
- Modify `src/main/java/com/solusi/erp/accountspayable/vendorbill/infrastructure/persistence/VendorBillPersistenceMapper.java`
- Modify `src/main/java/com/solusi/erp/accountspayable/vendorbill/infrastructure/persistence/VendorBillJpaRepository.java`
- Modify `src/main/java/com/solusi/erp/accountspayable/vendorbill/infrastructure/adapter/VendorBillRepositoryImpl.java`
- Modify `src/main/java/com/solusi/erp/accountspayable/vendorbill/domain/repository/VendorBillRepository.java`

### Settlement Projection And Payment Integration

- Replace or extend `src/main/java/com/solusi/erp/accountspayable/vendorbill/domain/port/VendorBillPaymentSummaryPort.java`
- Modify `src/main/java/com/solusi/erp/accountspayable/vendorbill/infrastructure/adapter/VendorBillPaymentSummaryAdapter.java`
- Modify `src/main/java/com/solusi/erp/accountspayable/vendorpayment/domain/port/PayableVendorBillQueryPort.java`
- Modify `src/main/java/com/solusi/erp/accountspayable/vendorpayment/infrastructure/adapter/PayableVendorBillQueryAdapter.java`
- Replace or extend `src/main/java/com/solusi/erp/accountspayable/vendorpayment/domain/port/VendorBillPaymentUpdatePort.java`
- Modify `src/main/java/com/solusi/erp/accountspayable/vendorpayment/infrastructure/adapter/VendorBillPaymentUpdateAdapter.java`
- Modify `src/main/java/com/solusi/erp/accountspayable/vendorpayment/application/usecase/command/ConfirmVendorPaymentUseCaseImpl.java`
- Modify `src/main/java/com/solusi/erp/accountspayable/vendorpayment/infrastructure/config/VendorPaymentConfig.java`

### Vendor Bill Queries And Web

- Modify `src/main/java/com/solusi/erp/accountspayable/vendorbill/application/usecase/query/FindVendorBillsUseCase.java`
- Modify `src/main/java/com/solusi/erp/accountspayable/vendorbill/application/usecase/query/FindVendorBillsUseCaseImpl.java`
- Modify `src/main/java/com/solusi/erp/accountspayable/vendorbill/application/usecase/query/GetVendorBillDetailUseCaseImpl.java`
- Modify `src/main/java/com/solusi/erp/accountspayable/vendorbill/application/usecase/query/VendorBillSummaryView.java`
- Modify `src/main/java/com/solusi/erp/accountspayable/vendorbill/application/usecase/query/VendorBillDetailView.java`
- Modify `src/main/java/com/solusi/erp/accountspayable/vendorbill/web/controller/VendorBillController.java`
- Modify `src/main/java/com/solusi/erp/accountspayable/vendorbill/web/dto/VendorBillSummaryResponse.java`
- Modify `src/main/java/com/solusi/erp/accountspayable/vendorbill/web/dto/VendorBillDetailResponse.java`
- Modify `src/main/java/com/solusi/erp/accountspayable/vendorbill/web/mapper/VendorBillWebMapper.java`
- Modify `src/main/resources/templates/accountspayable/vendor-bills/list.html`
- Modify `src/main/resources/templates/accountspayable/vendor-bills/detail.html`

### Tests And Docs

- Modify affected unit/controller/template tests under `src/test/java/com/solusi/erp/accountspayable/`
- Add or extend migration tests for V72.
- Modify `e2e-tests/tests/accountspayable/vendor-bill.spec.ts`
- Modify `e2e-tests/tests/accountspayable/vendor-payment.spec.ts`
- Modify `src/main/resources/messages.properties`
- Modify `src/main/resources/messages_id.properties`
- Modify `docs/modules/accountspayable/vendor-bill.md`
- Modify `docs/modules/accountspayable/vendor-payment.md`

## 5. Tasks

### Task 1: Vendor Bill Settlement Migration [x]

Add database columns and migrate old status data into document and settlement statuses.

**Depends on:** none
**Reference modules:** `accountspayable.vendorbill`, Flyway migrations

- [x] Create `V72__Refactor_Vendor_Bill_Settlement_Status.sql` in MariaDB migrations.
      Add `document_status VARCHAR(30)` and nullable `settlement_status VARCHAR(30)` to `ap_vendor_bills`, populate both from old `status`, then make `document_status` not null.
      ref: `src/main/resources/db/migration/V58__Add_Vendor_Bill_Module.sql:L13-L35` - current `ap_vendor_bills.status` column shape
- [x] Drop or stop exposing the legacy `status` column after the data migration is complete. Prefer dropping it if the entity no longer maps it, so `PARTIAL_PAID`/`PAID` cannot keep leaking into new code.
      ref: `src/main/java/com/solusi/erp/accountspayable/vendorbill/infrastructure/persistence/VendorBillEntity.java:L41-L50` - current single status mapping
- [x] Add indexes for list/payable selectors:
      `(document_status)`, `(settlement_status)`, and `(vendor_id, currency_id, document_status, settlement_status)`.
      ref: `src/main/java/com/solusi/erp/accountspayable/vendorpayment/infrastructure/adapter/PayableVendorBillQueryAdapter.java:L17-L31` - payable lookup filters by vendor/currency/status/outstanding
- [x] Create the H2 mirror `V72__Refactor_Vendor_Bill_Settlement_Status.sql` with H2-compatible `ALTER TABLE` and update syntax.
      ref: `docs/tests/playwright-e2e-guide.md:L57-L74` - H2 mirror migration rule
- [x] Add a migration contract test that migrates H2 and asserts:
      columns exist; old `CONFIRMED/PARTIAL_PAID/PAID` map correctly; old `DRAFT/CANCELLED` keep null settlement; indexes or searchable fields are present; MariaDB and H2 migration files both exist.
      ref: `src/test/java/com/solusi/erp/purchasing/purchasereturn/infrastructure/persistence/PurchaseReturnMigrationTest.java` - recent static/H2 migration contract style
- [x] Search the repo for `PARTIAL_PAID` and ` PAID` after implementation. Only historical migration assertions or docs about legacy mapping may remain. (scan run; active Java/template/doc references are covered by Tasks 2, 4, 5, and 6)
      ref: `docs/brainstorming/2026-06-02-vendor-debit-memo.md:L879-L883` - reason old paid statuses are no longer document statuses

**Validation criteria:**

- `mvn test -Dtest="*MigrationTest"` passes.
- Fresh H2 migration succeeds.
- Existing rows with old status values are mapped deterministically.
- No Java enum depends on `PARTIAL_PAID` or `PAID` as Vendor Bill document statuses.

### Task 2: Vendor Bill Domain And Persistence Status Split [x]

Refactor the Vendor Bill aggregate and persistence model to expose document and settlement statuses.

**Depends on:** Task 1
**Reference modules:** `accountspayable.vendorbill`

- [x] Create `VendorBillDocumentStatus` with `DRAFT`, `CONFIRMED`, `CANCELLED`, and `VendorBillSettlementStatus` with `OPEN`, `PARTIALLY_SETTLED`, `SETTLED`.
      ref: `src/main/java/com/solusi/erp/accountspayable/vendorbill/domain/model/VendorBillStatus.java:L1-L9` - old enum to replace
- [x] Update `VendorBill` fields, constructor, factory, getters, `confirm(...)`, and `cancel()` so document transitions stay document-only.
      Confirm should set `documentStatus=CONFIRMED` and `settlementStatus=OPEN`; cancel should set `documentStatus=CANCELLED` and clear or preserve null settlement for non-confirmed drafts.
      ref: `src/main/java/com/solusi/erp/accountspayable/vendorbill/domain/model/VendorBill.java:L20-L78` - current aggregate status transitions
- [x] Keep edit/update/delete/cancel guards keyed on `documentStatus == DRAFT`.
      ref: `src/main/java/com/solusi/erp/accountspayable/vendorbill/application/usecase/command/UpdateVendorBillUseCaseImpl.java:L50-L65` - draft-only update guard
      ref: `src/main/java/com/solusi/erp/accountspayable/vendorbill/application/usecase/command/DeleteVendorBillUseCaseImpl.java:L18-L24` - draft-only delete guard
- [x] Update `VendorBillEntity` to map `document_status` and `settlement_status` with the new enum types.
      ref: `src/main/java/com/solusi/erp/accountspayable/vendorbill/infrastructure/persistence/VendorBillEntity.java:L41-L50` - old single status entity field
- [x] Update `VendorBillPersistenceMapper` so both statuses round-trip in domain and entity mapping.
      ref: `src/main/java/com/solusi/erp/accountspayable/vendorbill/infrastructure/persistence/VendorBillPersistenceMapper.java:L12-L36` - current constructor mapping
- [x] Update repository port/JPA filter signatures from single `VendorBillStatus status` to separate `documentStatus` and optional `settlementStatus`.
      ref: `src/main/java/com/solusi/erp/accountspayable/vendorbill/domain/repository/VendorBillRepository.java:L1-L19` - current repository port filter contract
      ref: `src/main/java/com/solusi/erp/accountspayable/vendorbill/infrastructure/persistence/VendorBillJpaRepository.java:L11-L25` - current JPA filter by `vb.status`
- [x] **TEST:** Update `VendorBillTest` for confirm, cancel, non-draft guards, and defensive copies using the new enums.
      ref: `src/test/java/com/solusi/erp/accountspayable/vendorbill/domain/model/VendorBillTest.java:L1-L107` - current aggregate transition tests
- [x] **TEST:** Update `VendorBillRepositoryImplTest` and mapper tests to prove both statuses persist and filter correctly.
      ref: `src/test/java/com/solusi/erp/accountspayable/vendorbill/infrastructure/adapter/VendorBillRepositoryImplTest.java` - repository mapping/filter tests

**Validation criteria:**

- `mvn test -Dtest="VendorBillTest,VendorBillRepositoryImplTest"` passes.
- `DRAFT`, `CONFIRMED`, and `CANCELLED` remain the only document lifecycle states.
- `PARTIAL_PAID` and `PAID` no longer compile as Vendor Bill domain statuses.

### Task 3: Neutral Settlement Projection For Vendor Bill [x]

Replace payment-only summary naming with a neutral settlement projection that can include future Debit Memo Allocation.

**Depends on:** Task 2
**Reference modules:** `accountspayable.vendorbill`, `accountspayable.vendorpayment`

- [x] Replace or extend `VendorBillPaymentSummaryPort` into a neutral settlement summary port returning `paidAmount`, `debitMemoAppliedAmount`, `outstandingAmount`, and computed `settlementStatus`.
      For Phase C, `debitMemoAppliedAmount` is always zero.
      ref: `src/main/java/com/solusi/erp/accountspayable/vendorbill/domain/port/VendorBillPaymentSummaryPort.java:L1-L15` - current payment-only projection port
- [x] Update `VendorBillPaymentSummaryAdapter` SQL to calculate confirmed payment total, zero DMA total, outstanding amount, and settlement status using `vb.total_amount`.
      ref: `src/main/java/com/solusi/erp/accountspayable/vendorbill/infrastructure/adapter/VendorBillPaymentSummaryAdapter.java:L18-L41` - current confirmed-payment projection SQL
- [x] Clamp outstanding at zero or reject over-settlement upstream. Do not let UI projections show a negative outstanding amount.
      ref: `docs/brainstorming/2026-06-02-vendor-debit-memo.md:L911-L927` - concurrency and over-settlement prevention
- [x] Update `FindVendorBillsUseCaseImpl` and `GetVendorBillDetailUseCaseImpl` to use the neutral projection and expose `debitMemoAppliedAmount` and settlement status.
      ref: `src/main/java/com/solusi/erp/accountspayable/vendorbill/application/usecase/query/FindVendorBillsUseCaseImpl.java:L18-L52` - current summary uses payment-only port
      ref: `src/main/java/com/solusi/erp/accountspayable/vendorbill/application/usecase/query/GetVendorBillDetailUseCaseImpl.java:L21-L49` - current detail uses payment-only port
- [x] Update `VendorBillSummaryView` and `VendorBillDetailView` with `documentStatus`, `settlementStatus`, `paidAmount`, `debitMemoAppliedAmount`, and `outstandingAmount`.
      ref: `src/main/java/com/solusi/erp/accountspayable/vendorbill/application/usecase/query/VendorBillSummaryView.java:L1-L20` - current single status response model
      ref: `src/main/java/com/solusi/erp/accountspayable/vendorbill/application/usecase/query/VendorBillDetailView.java:L1-L28` - current detail response model
- [x] **TEST:** Update query use case tests for open, partially settled, settled, and zero-DMA seam values.
      ref: `src/test/java/com/solusi/erp/accountspayable/vendorbill/application/usecase/query/FindVendorBillsUseCaseTest.java` - summary projection test pattern
      ref: `src/test/java/com/solusi/erp/accountspayable/vendorbill/application/usecase/query/GetVendorBillDetailUseCaseTest.java` - detail projection test pattern
- [x] **TEST:** Update adapter tests to assert SQL only includes confirmed Vendor Payments now and includes explicit zero Debit Memo seam fields for Phase E.
      ref: `src/test/java/com/solusi/erp/accountspayable/vendorbill/infrastructure/adapter/VendorBillPaymentSummaryAdapterTest.java:L1-L76` - current SQL projection test style

**Validation criteria:**

- `mvn test -Dtest="FindVendorBillsUseCaseTest,GetVendorBillDetailUseCaseTest,VendorBillPaymentSummaryAdapterTest"` passes.
- Vendor Bill detail/list no longer infer payment state from document status.
- Projection shape can accept DMA values later without changing UI contracts again.

### Task 4: Vendor Payment Payable Query And Confirm Revalidation [x]

Make Vendor Payment confirmation safe against stale outstanding amounts and future settlement concurrency.

**Depends on:** Tasks 2 and 3
**Reference modules:** `accountspayable.vendorpayment`, `accountspayable.vendorbill`

- [x] Update `PayableVendorBillQueryAdapter` to filter by `document_status='CONFIRMED'`, `settlement_status IN ('OPEN','PARTIALLY_SETTLED')`, vendor, currency, and computed outstanding amount.
      ref: `src/main/java/com/solusi/erp/accountspayable/vendorpayment/infrastructure/adapter/PayableVendorBillQueryAdapter.java:L17-L35` - current payable query uses old status values
- [x] Update `PayableVendorBillQueryPort.PayableVendorBillView` and response DTO only if a settlement status or DMA seam field is needed by the payment form. Keep the payment form narrowly scoped if no UI behavior changes are required. (no DTO change needed)
      ref: `src/main/java/com/solusi/erp/accountspayable/vendorpayment/domain/port/PayableVendorBillQueryPort.java:L1-L16` - current payable bill read model
- [x] Replace or extend `VendorBillPaymentUpdatePort` with a settlement update/guard port that can:
      lock all target Vendor Bills for update;
      validate document status, settlement status, vendor, currency, duplicate bill lines, current outstanding, and over-application;
      update settlement statuses after payment confirmation.
      ref: `src/main/java/com/solusi/erp/accountspayable/vendorpayment/domain/port/VendorBillPaymentUpdatePort.java:L1-L7` - current status update-only port
      ref: `docs/brainstorming/2026-06-02-vendor-debit-memo.md:L911-L927` - lock and revalidation rules
- [x] Implement the guard in the existing adapter or a renamed adapter using `SELECT ... FOR UPDATE` on `ap_vendor_bills` rows and current confirmed payment totals.
      H2-compatible query behavior must be covered by tests or isolated SQL.
      ref: `src/main/java/com/solusi/erp/accountspayable/vendorpayment/infrastructure/adapter/VendorBillPaymentUpdateAdapter.java:L15-L42` - current direct status update SQL to replace
- [x] Refactor `ConfirmVendorPaymentUseCaseImpl` flow:
      load draft payment;
      validate payment domain totals;
      lock/revalidate target Vendor Bills before journal posting;
      post `VENDOR_PAYMENT` journal;
      save confirmed payment;
      refresh Vendor Bill settlement statuses.
      ref: `src/main/java/com/solusi/erp/accountspayable/vendorpayment/application/usecase/command/ConfirmVendorPaymentUseCaseImpl.java:L31-L86` - current confirm sequence
- [x] Keep journal amount calculation unchanged. Phase C changes settlement eligibility and status, not Vendor Payment accounting.
      ref: `docs/modules/accountspayable/vendor-payment.md:L100-L126` - current `VENDOR_PAYMENT` journal variables
- [x] Add friendly errors for stale outstanding, settled target bill, wrong vendor/currency, duplicate target bill, and over-applied amount.
      Use `MessageSource`-resolved keys where existing use cases do so; at minimum add message bundle entries for new `DomainException` keys.
      ref: `docs/spec/i18n-guide.md:L1-L90` - i18n key naming and update protocol
- [x] **TEST:** Extend `ConfirmVendorPaymentUseCaseTest` to verify guard invocation before journal posting, no journal when guard rejects, status update after save, and unchanged journal variables.
      ref: `src/test/java/com/solusi/erp/accountspayable/vendorpayment/application/usecase/command/ConfirmVendorPaymentUseCaseTest.java:L31-L119` - current confirm use case tests
- [x] **TEST:** Add or extend adapter tests for payable bill SQL and payment settlement guard SQL/parameter behavior.
      ref: `src/test/java/com/solusi/erp/accountspayable/vendorbill/infrastructure/adapter/VendorBillPaymentSummaryAdapterTest.java:L1-L76` - JDBC adapter test pattern
- [x] **TEST:** Update `VendorPaymentConfigTest` so new/renamed guard/update port wiring is covered.
      ref: `src/test/java/com/solusi/erp/accountspayable/vendorpayment/infrastructure/config/VendorPaymentConfigTest.java:L1-L89` - config wiring test pattern

**Validation criteria:**

- `mvn test -Dtest="ConfirmVendorPaymentUseCaseTest,VendorPaymentConfigTest,*VendorBill*AdapterTest,*Payable*Test"` passes.
- Confirmed payment cannot over-settle a Vendor Bill when outstanding changed after draft creation.
- Vendor Payment accounting command stays unchanged except for rejection timing.

### Task 5: Vendor Bill Web, Labels, And Templates [x]

Expose document and settlement statuses cleanly in Vendor Bill list/detail without mixing lifecycle and settlement labels.

**Depends on:** Tasks 2 and 3
**Reference modules:** `accountspayable.vendorbill`, UI specs

- [x] Update `VendorBillController.list(...)` to accept separate optional filters for document status and settlement status, populate both enum lists in the model, and pass both filters to the query use case.
      ref: `src/main/java/com/solusi/erp/accountspayable/vendorbill/web/controller/VendorBillController.java:L45-L71` - current single `status` filter
- [x] Update `VendorBillSummaryResponse`, `VendorBillDetailResponse`, and `VendorBillWebMapper` to expose `documentStatus`, `settlementStatus`, `paidAmount`, `debitMemoAppliedAmount`, and `outstandingAmount`.
      ref: `src/main/java/com/solusi/erp/accountspayable/vendorbill/web/dto/VendorBillSummaryResponse.java:L1-L24` - current summary DTO
      ref: `src/main/java/com/solusi/erp/accountspayable/vendorbill/web/dto/VendorBillDetailResponse.java:L1-L32` - current detail DTO
      ref: `src/main/java/com/solusi/erp/accountspayable/vendorbill/web/mapper/VendorBillWebMapper.java:L1-L70` - current mapper copies single status and amounts
- [x] Update list template filters and table columns:
      document status filter;
      settlement status filter;
      document status badge;
      settlement status badge;
      outstanding amount.
      Use generic pagination fragment unchanged.
      ref: `src/main/resources/templates/accountspayable/vendor-bills/list.html:L35-L127` - current list filter/table
      ref: `docs/spec/pagination.md:L48-L76` - pagination fragment placement
- [x] Update detail template header and payment summary:
      header badge is document status;
      settlement summary shows settlement status, paid amount, debit memo applied amount, and outstanding amount;
      journal link visibility depends on `documentStatus == 'CONFIRMED'`.
      ref: `src/main/resources/templates/accountspayable/vendor-bills/detail.html:L12-L95` - current header/status/payment summary
- [x] Update action visibility to use `documentStatus == 'DRAFT'` for confirm/cancel/delete/edit behavior.
      ref: `src/main/resources/templates/accountspayable/vendor-bills/detail.html:L136-L154` - current draft action footer
      ref: `docs/spec/action-buttons.md:L1-L50` - post action button contract
- [x] Add English and Indonesian labels for document status, settlement status, debit memo applied amount, outstanding amount, and new stale payment validation messages.
      Use targeted replace edits; do not append via shell echo.
      ref: `docs/spec/i18n-guide.md:L70-L90` - i18n update protocol
- [x] **TEST:** Update `VendorBillControllerTest` for filter parameters/model attrs and detail response mapping.
      ref: `src/test/java/com/solusi/erp/accountspayable/vendorbill/web/controller/VendorBillControllerTest.java:L1-L282` - controller/reflection test pattern
- [x] **TEST:** Update `VendorBillWebMapperTest` for both statuses and DMA seam field.
      ref: `src/test/java/com/solusi/erp/accountspayable/vendorbill/web/mapper/VendorBillWebMapperTest.java:L1-L132` - web mapper test pattern
- [x] **TEST:** Update `VendorBillTemplateTest` for new labels, filters, badge bindings, action visibility, and no stale `PARTIAL_PAID`/`PAID` template dependency.
      ref: `src/test/java/com/solusi/erp/accountspayable/vendorbill/web/template/VendorBillTemplateTest.java:L1-L149` - static and render template tests

**Validation criteria:**

- `mvn test -Dtest="VendorBillControllerTest,VendorBillWebMapperTest,VendorBillTemplateTest"` passes.
- Vendor Bill UI distinguishes document lifecycle from settlement state.
- No template checks rely on `PARTIAL_PAID` or `PAID`.

### Task 6: Documentation Update [x]

Update module docs so Phase C behavior becomes the documented current behavior.

**Depends on:** Tasks 3, 4, and 5
**Reference modules:** project docs

- [x] Update `docs/modules/accountspayable/vendor-bill.md` to replace the single status lifecycle with:
      `documentStatus: DRAFT -> CONFIRMED / CANCELLED` and
      `settlementStatus: OPEN -> PARTIALLY_SETTLED -> SETTLED`.
      ref: `docs/modules/accountspayable/vendor-bill.md:L17-L31` - stale status lifecycle text
- [x] Update Vendor Bill model docs to include `documentStatus`, `settlementStatus`, `paidAmount`, `debitMemoAppliedAmount`, and `outstandingAmount`.
      ref: `docs/modules/accountspayable/vendor-bill.md:L64-L78` - current model data table
- [x] Update payment visibility/accounting sections to state settlement is projection-based and currently includes confirmed Vendor Payments plus a zero Debit Memo seam until Phase E.
      ref: `docs/modules/accountspayable/vendor-bill.md:L160-L168` - current payment-only summary text
- [x] Update `docs/modules/accountspayable/vendor-payment.md` so payable bills require confirmed document status and open/partially-settled settlement status, and confirm revalidates under lock before posting journal.
      ref: `docs/modules/accountspayable/vendor-payment.md:L25-L35` - stale update-to-PARTIAL_PAID/PAID text
      ref: `docs/modules/accountspayable/vendor-payment.md:L52-L58` - current payable criteria
- [x] Mention that Debit Memo Allocation will later contribute to `debitMemoAppliedAmount` and outstanding calculation in Phase E, but no DMA table or journal exists in Phase C.
      ref: `docs/brainstorming/2026-06-02-vendor-debit-memo.md:L863-L879` - target settlement projection and terminology
- [x] Run stale-text scans for `PARTIAL_PAID`, `PAID`, and `paymentStatus` in Vendor Bill/Payment docs and templates; update only current docs, not historical brainstorming decisions.

**Validation criteria:**

- Docs consistently use document/settlement terminology.
- Docs clearly state Debit Memo/DMA are deferred.
- No current module doc says Vendor Payment writes Vendor Bill `PAID` or `PARTIAL_PAID`.

### Task 7: Playwright Settlement Coverage [ ]

Update browser coverage for the separated statuses and stale outstanding revalidation.

**Depends on:** Tasks 4, 5, and 6
**Reference modules:** Playwright AP specs

- [ ] Before editing specs, re-read Vendor Bill/Vendor Payment controllers, list/detail templates, page-specific JS, and the current AP specs.
      ref: `docs/tests/playwright-pitfalls.md:L1-L293` - mandatory E2E pitfalls
      ref: `src/main/java/com/solusi/erp/accountspayable/vendorbill/web/controller/VendorBillController.java:L37-L286` - actual Vendor Bill routes
      ref: `src/main/java/com/solusi/erp/accountspayable/vendorpayment/web/controller/VendorPaymentController.java:L45-L251` - actual Vendor Payment routes
      ref: `e2e-tests/tests/accountspayable/vendor-bill.spec.ts` - current Vendor Bill E2E flow
      ref: `e2e-tests/tests/accountspayable/vendor-payment.spec.ts` - current Vendor Payment E2E flow
- [ ] Update Vendor Bill spec assertions so confirmed bills show `Document Status = CONFIRMED` and `Settlement Status = OPEN`, not a payment lifecycle document status.
- [ ] Update Vendor Payment confirm scenario so after payment confirmation the target Vendor Bill detail shows `Settlement Status = SETTLED` or `PARTIALLY_SETTLED` according to payment amount, while `Document Status` remains `CONFIRMED`.
- [ ] Add a stale outstanding scenario if feasible with existing UI/API setup:
      create payment draft for a bill;
      settle the same bill with another payment or direct API contract;
      attempt to confirm the stale draft;
      assert user-visible error and no journal/status mutation.
      Keep this scenario incomplete in the plan/report if the runtime setup cannot create deterministic stale data in the same task.
- [ ] Use `page.request.get` for pre-navigation probes and click `#confirm-modal-btn-yes` for ERP modal confirmation.
      ref: `docs/tests/playwright-pitfalls.md:L25-L50` - avoid `page.evaluate(fetch)` from about:blank
      ref: `docs/tests/playwright-pitfalls.md:L115-L143` - Bootstrap modal confirm pattern
- [ ] Run:
      `cd e2e-tests && npx tsc --noEmit`
      `npx playwright test tests/accountspayable/vendor-bill.spec.ts --list`
      `npx playwright test tests/accountspayable/vendor-payment.spec.ts --list`
      `npx playwright test tests/accountspayable/vendor-bill.spec.ts`
      `npx playwright test tests/accountspayable/vendor-payment.spec.ts`
- [ ] Run cold-cache transactional check:
      `cd e2e-tests && rm -rf .auth/ && npx playwright test tests/accountspayable/vendor-bill.spec.ts tests/accountspayable/vendor-payment.spec.ts`
- [ ] On first runtime failure, retain screenshot/video and record diagnosis in `docs/reports/2026-06-02-phase-c-vendor-bill-settlement-refactor.md`.

**Validation criteria:**

- TypeScript compile passes.
- AP specs list expected scenarios.
- Vendor Bill and Vendor Payment specs pass at least once normally and once after `.auth` removal.
- Any E2E task that modifies specs remains incomplete until the live Playwright runs are green.

### Task 8: Regression Gate And Handoff [ ]

Run focused and final verification, then record exact results.

**Depends on:** Tasks 1-7

- [ ] Run migration-sensitive tests:
      `mvn test -Dtest="*MigrationTest"`
- [ ] Run Vendor Bill domain/persistence/query/web focused tests:
      `mvn test -Dtest="VendorBillTest,VendorBillRepositoryImplTest,FindVendorBillsUseCaseTest,GetVendorBillDetailUseCaseTest,VendorBillControllerTest,VendorBillWebMapperTest,VendorBillTemplateTest"`
- [ ] Run settlement/payment focused tests:
      `mvn test -Dtest="ConfirmVendorPaymentUseCaseTest,VendorPaymentConfigTest,*VendorBill*AdapterTest,*Payable*Test,VendorPaymentControllerTest,VendorPaymentTemplateTest"`
- [ ] Run full Maven gate:
      `mvn clean test`
- [ ] Run Playwright gates from Task 7 if any AP spec changed.
- [ ] Inspect JaCoCo if new code lowers coverage. Add focused tests in the owning task before marking complete.
      ref: `docs/architecture/jacoco-coverage.md` - project coverage guide
- [ ] Update `docs/reports/2026-06-02-phase-c-vendor-bill-settlement-refactor.md` with commands, outcomes, skipped gates, deviations from this plan, and remaining Phase D/E dependencies.
- [ ] Record project SemVer deferral until implementation is accepted. Phase C is a feature/refactor behavior change, so likely MINOR when version bump is intentionally requested.
      ref: `docs/AGENTS.md:L101-L123` - SemVer automation and commit protocol

**Validation criteria:**

- Focused migration, Vendor Bill, and Vendor Payment tests pass.
- `mvn clean test` passes.
- Playwright AP specs pass if modified.
- Report contains enough detail for Phase D/E planning without reading raw command output.

## 6. Dependency Notes

- Task 1 must land before Java code can remove the old single `status` mapping.
- Task 2 should land before query/web changes so compile errors reveal every stale single-status dependency.
- Task 3 gives the UI and future DMA a stable settlement projection contract.
- Task 4 depends on Task 3 because confirm revalidation must use the same outstanding formula as the UI.
- Task 7 must remain incomplete if the actual Playwright runs are not executed.

## 7. Deferred Items

- Phase D: Debit Memo Core and automatic DM creation from confirmed Purchase Return.
- Phase E: Debit Memo Allocation, AP reduction, VAT reversal, FX gain/loss, and DMA reversal.
- Phase F: Confirmed Purchase Return reversal using Phase A linked stock/journal reversal primitives and Phase D/E downstream guards.
- Phase G: broader UI/menu/selector integration and full Debit Memo E2E matrix.
- Vendor Refund and refund-based Debit Memo settlement.
