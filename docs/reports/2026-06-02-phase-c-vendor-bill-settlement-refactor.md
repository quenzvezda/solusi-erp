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
