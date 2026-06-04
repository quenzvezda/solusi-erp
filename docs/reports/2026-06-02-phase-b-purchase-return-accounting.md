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

## Task 3: Goods Issue Posting Route For Purchase Return
- **Status:** clean
- **Summary:** Routed Purchase Return-sourced Goods Issue completion to a `PURCHASE_RETURN` journal with PR source identity and PR-only inventory/GRIR variables, while keeping stock movements referenced to the physical Goods Issue. Updated GI journal links to resolve source-owned Purchase Return journals from the GI reference metadata.
- **Validation:** `mvn test -Dtest="CompleteGoodsIssueUseCaseTest,ConfirmPurchaseReturnUseCaseTest,GoodsIssueQueryUseCaseTest"` passed with 35 tests; `mvn test -Dtest="GoodsIssueConfigTest"` passed with 1 test. JaCoCo checks met for both focused runs.

## Task 4: Accounting UI Labels And Source Visibility
- **Status:** clean
- **Summary:** Added Purchase Return source link support in Journal Entry detail and expanded template coverage so schema forms, journal filters, and journal details expose the `PURCHASE_RETURN` event/source.
- **Deviation:** Template integration tests render message keys as `??...??` placeholders in this harness, so assertions lock on the stable `PURCHASE_RETURN` event key/value and source route instead of localized display text. `JournalMessageBundleTest` continues to verify the actual bundle keys.
- **Validation:** `mvn test -Dtest="SchemaFormIntegrationTest,JournalMessageBundleTest,*Journal*Template*Test"` passed with 15 tests after the assertion adjustment. JaCoCo checks met.

## Task 5: Documentation Update
- **Status:** clean
- **Summary:** Updated Purchase Return, Goods Issue, and Accounting Schema docs to describe current Phase B behavior: Purchase Return-sourced GI remains the physical stock document, while accounting posts `PURCHASE_RETURN` inventory/GRIR variables and leaves Debit Memo/AP/Input VAT/FX concerns deferred.
- **Validation:** Reviewed the affected doc diffs and ran a stale-text scan for old Phase 1/generic GI accounting statements and incorrect seeder paths; no stale matches remained.
