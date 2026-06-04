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
