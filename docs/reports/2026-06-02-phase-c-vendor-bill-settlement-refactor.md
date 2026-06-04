# Implementation Report: Vendor Bill Settlement Refactor

> Plan: `docs/plans/2026-06-02-phase-c-vendor-bill-settlement-refactor.md`
> Source: `docs/brainstorming/2026-06-02-vendor-debit-memo.md`

## Task 1: Vendor Bill Settlement Migration

### Finding: legacy references remain outside migration scope
- **Type:** deviation
- **Severity:** info
- **Detail:** V72 migration now drops `ap_vendor_bills.status` after mapping legacy values into `document_status` and `settlement_status`, but the post-task scan still finds active `PARTIAL_PAID`/`PAID` references in Java, templates, and current module docs. Those files are explicitly assigned to Tasks 2, 4, 5, and 6.
- **Action taken:** Kept Task 1 scoped to schema migration and migration contract coverage. Recorded the scan result in the plan instead of broadening the migration commit into the domain/web refactor tasks.
- **Ref:** `src/main/java/com/solusi/erp/accountspayable/vendorbill/domain/model/VendorBillStatus.java`, `src/main/java/com/solusi/erp/accountspayable/vendorpayment/infrastructure/adapter/PayableVendorBillQueryAdapter.java`, `src/main/resources/templates/accountspayable/vendor-bills/detail.html`, `docs/modules/accountspayable/vendor-bill.md`, `docs/modules/accountspayable/vendor-payment.md`

- **Status:** findings
- **Summary:** Added MariaDB/H2 V72 migrations and a contract test that migrates legacy H2 rows from V71 to V72, asserts deterministic status mapping, verifies `status` is dropped, and checks selector indexes.
- **Validation:** `mvn test -Dtest=VendorBillSettlementMigrationTest` passed; `mvn test -Dtest=*MigrationTest` passed.

## Task 2: Vendor Bill Domain And Persistence Status Split

### Finding: query and controller signatures had to move with repository contract
- **Type:** deviation
- **Severity:** info
- **Detail:** Changing `VendorBillRepository.findAll(...)` to accept `documentStatus` and optional `settlementStatus` required updating `FindVendorBillsUseCase`, `FindVendorBillsUseCaseImpl`, `VendorBillConfig`, and the current controller filter type to keep the project compiling. Task 5 still owns the UI-level split into separate filters and labels.
- **Action taken:** Kept the web DTO/template contract unchanged for Task 2; only the Java enum type behind the existing `status` slot now represents document status.
- **Ref:** `src/main/java/com/solusi/erp/accountspayable/vendorbill/application/usecase/query/FindVendorBillsUseCase.java`, `src/main/java/com/solusi/erp/accountspayable/vendorbill/web/controller/VendorBillController.java`

### Finding: payment adapters still contain legacy paid statuses
- **Type:** deviation
- **Severity:** info
- **Detail:** Post-task Java scan shows no `VendorBillStatus` references remain, but `PARTIAL_PAID`/`PAID` still appear in Vendor Payment adapters that are explicitly assigned to Task 4.
- **Action taken:** Left those references untouched for Task 4 so this commit stays scoped to Vendor Bill domain/persistence.
- **Ref:** `src/main/java/com/solusi/erp/accountspayable/vendorpayment/infrastructure/adapter/PayableVendorBillQueryAdapter.java`, `src/main/java/com/solusi/erp/accountspayable/vendorpayment/infrastructure/adapter/VendorBillPaymentUpdateAdapter.java`

- **Status:** findings
- **Summary:** Replaced the legacy Vendor Bill status enum with document and settlement enums, mapped both fields through domain/entity/repository, updated draft-only guards, and added repository filter coverage.
- **Validation:** `mvn clean compile -q -pl .` passed; `mvn test -Dtest=VendorBillTest,VendorBillRepositoryImplTest` passed.

## Task 3: Neutral Settlement Projection For Vendor Bill

### Finding: web DTO exposure remains in Task 5
- **Type:** decision
- **Severity:** info
- **Detail:** The application query views now expose `documentStatus`, `settlementStatus`, `paidAmount`, `debitMemoAppliedAmount`, and `outstandingAmount`, but current web DTOs/templates still expose the old `status` string until the dedicated web/template task.
- **Action taken:** Kept `VendorBillWebMapper` mapping `documentStatus` into the existing `status` response field for compile compatibility. Task 5 will split the web DTO/template contract.
- **Ref:** `src/main/java/com/solusi/erp/accountspayable/vendorbill/web/mapper/VendorBillWebMapper.java`

- **Status:** findings
- **Summary:** Replaced the payment-only projection with `VendorBillSettlementSummaryPort`, added zero-DMA seam data, clamped negative outstanding values, and computed settlement status from confirmed Vendor Payments.
- **Validation:** `mvn test -Dtest=FindVendorBillsUseCaseTest,GetVendorBillDetailUseCaseTest,VendorBillSettlementSummaryAdapterTest` passed.

## Task 4: Vendor Payment Payable Query And Confirm Revalidation

### Finding: existing billable GR SQL still referenced dropped `vb.status`
- **Type:** bug
- **Severity:** warning
- **Detail:** After V72 drops `ap_vendor_bills.status`, billable Goods Receipt queries would fail at runtime because they still filtered confirmed Vendor Bills with `vb.status = :confirmedStatus`.
- **Action taken:** Updated the billable GR query adapter and reference provider to filter `vb.document_status = :confirmedStatus`.
- **Ref:** `src/main/java/com/solusi/erp/accountspayable/vendorbill/infrastructure/adapter/BillableGrQueryAdapter.java`, `src/main/java/com/solusi/erp/accountspayable/vendorbill/infrastructure/adapter/GoodsReceiptBillableReferenceProvider.java`

### Finding: guard port was extended, not renamed
- **Type:** decision
- **Severity:** info
- **Detail:** The existing `VendorBillPaymentUpdatePort` was extended with `lockAndValidatePayment(...)` and `updateSettlementStatus(...)` instead of introducing a new port name, keeping configuration and dependent use cases smaller for this task.
- **Action taken:** Added adapter tests around lock/query/update SQL and use-case tests around guard ordering.
- **Ref:** `src/main/java/com/solusi/erp/accountspayable/vendorpayment/domain/port/VendorBillPaymentUpdatePort.java`

- **Status:** findings
- **Summary:** Updated payable bill selection, added locked outstanding revalidation before journal posting, changed Vendor Bill updates to settlement status only, and added i18n messages for stale/invalid target bill errors.
- **Validation:** `mvn test -Dtest=ConfirmVendorPaymentUseCaseTest,VendorPaymentConfigTest,*VendorBill*AdapterTest,*Payable*Test` passed; AP Java scan for `PARTIAL_PAID`, `PAID`, and `vb.status` returned 0 matches.

## Task 5: Vendor Bill Web, Labels, And Templates
- **Status:** clean
- **Summary:** Split Vendor Bill web DTO/template status display into document and settlement status, added debit memo applied and outstanding fields, and updated list/detail filters and labels.
- **Validation:** `mvn test -Dtest=VendorBillControllerTest,VendorBillWebMapperTest,VendorBillTemplateTest` passed; Vendor Bill template scan for `PARTIAL_PAID`, `PAID`, `bill.status`, and `item.status` returned 0 matches.

## Task 6: Documentation Update
- **Status:** clean
- **Summary:** Updated Vendor Bill and Vendor Payment module docs to describe document status, settlement status, projection-based outstanding, zero Debit Memo seam in Phase C, and locked Vendor Payment confirm revalidation.
- **Validation:** `docs/modules/accountspayable` scan for `PARTIAL_PAID`, `PAID`, and `paymentStatus` returned 0 matches. Vendor Bill template stale-status scan was already clean in Task 5.

## Task 7: Playwright Settlement Coverage
- **Status:** clean
- **Summary:** Added Vendor Bill detail settlement assertions, Vendor Payment confirm cross-checks that the target bill remains `CONFIRMED` and becomes `SETTLED`, a deterministic stale draft payment scenario, and replaced the supplier lookup pre-navigation probe with `page.request.get`.
- **Validation:** `cd e2e-tests && npx tsc --noEmit` passed; `npx playwright test tests/accountspayable/vendor-bill.spec.ts --list` passed and listed 5 AP Vendor Bill scenarios; `npx playwright test tests/accountspayable/vendor-payment.spec.ts --list` passed and listed 6 AP Vendor Payment scenarios including stale Scenario E. `.\e2e-tests\scripts\run-e2e.ps1 tests/accountspayable/vendor-bill.spec.ts tests/accountspayable/vendor-payment.spec.ts` passed with 15/15 tests. Cold-cache run after removing `e2e-tests\.auth` passed with 15/15 tests.
