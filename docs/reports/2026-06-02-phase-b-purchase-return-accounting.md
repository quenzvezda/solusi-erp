# Implementation Report: Purchase Return Accounting Replacement

> Plan: `docs/plans/2026-06-02-phase-b-purchase-return-accounting.md`
> Source: `docs/brainstorming/2026-06-02-vendor-debit-memo.md`

Populated during execution.

## Task 1: Accounting Event And Variable Contract
- **Status:** clean
- **Summary:** Added `PURCHASE_RETURN` accounting event, `PR_GRIR_CLEARING_AMT` and `PR_INVENTORY_AMT` journal variables, bilingual event labels, and focused contract/message coverage.
- **Validation:** `mvn test -Dtest="JournalVariableTest,JournalMessageBundleTest"` passed with 3 tests; JaCoCo checks met for the focused run.

## Task 2: Purchase Return Accounting Schema Seeds
- **Status:** clean
- **Summary:** Added MariaDB/H2 V71 seed for active `PURCHASE_RETURN` accounting schema, refreshed dev seeder `D220`, and updated H2 E2E seed to reuse the V71 schema while replacing its lines with E2E COA accounts after those accounts exist.
- **Deviation:** E2E seed could not insert a hard-coded `PURCHASE_RETURN` schema because V71 already creates the active schema and id `9404` is an E2E Input VAT account. The implementation follows the existing `VENDOR_PAYMENT` refresh pattern with `@prt_schema_id`.
- **Validation:** `mvn test -Dtest="PurchaseReturnMigrationTest"` passed with 5 tests; `mvn test -Dtest="*MigrationTest"` passed with 9 tests. JaCoCo checks met for both focused runs.
