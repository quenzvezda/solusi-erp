# Implementation Report: Generic Reversal Foundation

> Plan: `docs/plans/2026-06-02-phase-a-generic-reversal-foundation.md`
> Source: `docs/brainstorming/2026-06-02-vendor-debit-memo.md`

This report is populated during plan execution.

## Task 1: Reversal Schema Migration
- **Status:** clean
- **Summary:** Added `V69__Add_Generic_Reversal_Foundation.sql` for MariaDB and H2 with linked movement/valuation reversal columns, FK/unique guards, indexes, and H2 migration/static parity coverage.
- **Validation:** `mvn test -Dtest="GoodsIssueMigrationTest,ReversalFoundationMigrationTest"` passed. JaCoCo ratio warnings are expected for focused subset runs because the build has non-halting coverage checks; final full-suite coverage remains Task 10.

## Task 2: Generic Linked Journal Reversal Use Case
- **Status:** decision
- **Summary:** Added internal `ReversePostedJournalUseCase`, generalized `JournalEntry.createReversal(...)` for auto-journals, kept manual UI reversal manual-only, and wired the new use case through `JournalConfig`.
- **Decision:** Linked reversal journals keep the original `eventType/sourceType` but use `sourceId/sourceCode = null`; `reversalOfId` is the audit/idempotency link. This preserves manual reversal metadata (`MANUAL/MANUAL`) and avoids the existing unique `(source_type, source_id)` guard for normal auto-posted journals.
- **Validation:** `mvn test -Dtest="JournalEntryTest,ReverseManualJournalUseCaseTest,ReversePostedJournalUseCaseTest,JournalConfigTest"` passed. Focused JaCoCo warnings are deferred to Task 10 full-suite gate.

## Task 3: Linked Stock Movement Reversal Primitive
- **Status:** decision
- **Summary:** Added nullable movement reversal linkage to stock payload/entity logging, repository lookup helpers, a transactional `StockMovementReversalService`, and focused coverage for validation and wiring.
- **Decision:** The stock reversal service uses `msg.error.stock.reversal.*` keys consistently with existing project i18n naming, even though the plan example used `msg.err.stock...`.
- **Decision:** Reversal payload cost is derived from the original movement `unitCost` snapshot. If original amount is unavailable, the service uses local amount with exchange rate `1` to avoid re-multiplying the historical local unit cost.
- **Validation:** `mvn test -Dtest="StockServiceTest,StockMovementReversalServiceTest,StockConfigTest"` passed. Focused JaCoCo warnings are deferred to Task 10 full-suite gate.

## Task 4: Valuation Layer Reversal With Historical Issue Cost
- **Status:** clean
- **Summary:** Added `reversalOfMovementId` to valuation layers, repository lookup support, FIFO add-layer overloads, and stock-service propagation so reversal receipts create distinct inbound layers linked to the original issue movement.
- **Validation:** `mvn test -Dtest="FifoValuationServiceTest,StockServiceTest"` passed. Focused JaCoCo warnings are deferred to Task 10 full-suite gate.

## Task 5: Refactor Goods Issue Cancellation Use Case
- **Status:** deviation
- **Summary:** Refactored GI cancellation to require `GoodsIssueCancelCommand`, reject source-owned GI, reverse linked stock movements, reverse the original posted GI journal, and persist cancellation audit metadata.
- **Deviation:** Added `V70__Add_Goods_Issue_Cancellation_Metadata.sql` for MariaDB and H2 because accepted `cancelledDate/cancelReason` audit metadata needs persistence, although the task file list did not explicitly call out a migration.
- **Validation:** `mvn test -Dtest="GoodsIssueTest,CancelGoodsIssueUseCaseTest,CompleteGoodsIssueUseCaseTest,GoodsIssueConfigTest"` passed. `mvn test -Dtest="GoodsIssueMigrationTest"` also passed for V70. Focused JaCoCo warnings are deferred to Task 10 full-suite gate.

## Task 6: Goods Issue Cancellation Web Flow
- **Status:** deviation
- **Summary:** Added GI cancellation request DTOs, cancel form route/template/JS, JSON cancel POST mapping, source-owned cancel hiding, target container lookup filtering, and controller/template coverage.
- **Deviation:** Added `GetGoodsIssueCancelViewUseCase` and cancel view records in this task, although the plan lists cancellation read model work in Task 7, because the Task 6 form cannot submit movement-keyed target containers without original movement ids.
- **Validation:** `mvn test -Dtest="GoodsIssueControllerTest,GoodsIssueViewIntegrationTest,GoodsIssueCancelTemplateIntegrationTest"` passed. `mvn test -Dtest="GoodsIssueFormIntegrationTest,GoodsIssueConfigTest"` also passed after changing form actions and config wiring. Focused JaCoCo warnings are deferred to Task 10 full-suite gate.

## Task 7: Persistence, Query, And Navigation Support
- **Status:** decision
- **Summary:** Added a GI journal-link query use case for original/reversal journal navigation, exposed `reversalOfMovementId` through inventory movement responses, and extended query/template/mapper/config/controller coverage.
- **Decision:** GI detail now links directly by journal entry id instead of filtering the journal list by source fields; `sourceType/sourceId/sourceCode` remain the physical document identity for stock card/report rows, while `reversalOfMovementId` carries the reversal audit relationship.
- **Validation:** `mvn clean test -Dtest="GoodsIssueQueryUseCaseTest,GoodsIssueWebMapperTest,GoodsIssueViewIntegrationTest,InventoryMovementMapperTest,GoodsIssueControllerTest,GoodsIssueConfigTest"` passed. Focused JaCoCo warnings are deferred to Task 10 full-suite gate.

## Task 8: i18n And Documentation Update
- **Status:** clean
- **Summary:** Added bilingual GI cancellation/journal reversal/stock reversal message keys, static bundle coverage, and updated GI and Journal Entry docs for linked reversal behavior and Phase F Purchase Return reversal deferral.
- **Validation:** `mvn test -Dtest="JournalMessageBundleTest,GoodsIssueMessageBundleTest"` passed. Focused JaCoCo warnings are deferred to Task 10 full-suite gate.

## Task 9: Playwright GI Cancellation Coverage
- **Status:** skipped
- **Summary:** Playwright GI cancellation spec was not created or run in this execution.
- **Reason:** User explicitly allowed E2E to be skipped until the related phases are implemented; Task 10 will use Maven regression gates only.

## Task 10: Regression Gate And Handoff
- **Status:** clean
- **Summary:** Ran the focused accounting, stock, GI, migration, and full Maven gates; bumped project version from `1.12.0` to `1.13.0` for the Phase A feature foundation.
- **Validation:** `mvn test -Dtest="JournalEntryTest,ReverseManualJournalUseCaseTest,ReversePostedJournalUseCaseTest,PostJournalForEventUseCaseTest,JournalConfigTest"` passed with 36 tests. `mvn test -Dtest="StockServiceTest,FifoValuationServiceTest,StockMovementReversalServiceTest,StockConfigTest"` passed with 35 tests. `mvn test -Dtest="GoodsIssueTest,CancelGoodsIssueUseCaseTest,CompleteGoodsIssueUseCaseTest,GoodsIssueControllerTest,*GoodsIssue*IntegrationTest"` passed with 54 tests. `mvn test -Dtest="*MigrationTest"` passed with 8 tests. `mvn clean test` passed with 1853 tests, 0 failures, 0 errors, and 0 skipped.
- **Skipped:** Playwright normal and cold-cache browser gates were not run because Task 9 was explicitly deferred.
- **Coverage:** Full Maven gate ended with `BUILD SUCCESS`; JaCoCo still reports a non-halting branch coverage warning (`0.78` vs configured `0.80`). No additional Task 10 code changes were needed because the Maven gate itself passes and focused owner tests cover the new reversal behavior.
- **Remaining dependencies:** Phase B still owns Purchase Return accounting event/schema replacement. Phase D/E still own Debit Memo and allocation. Phase F still owns confirmed Purchase Return reversal orchestration using these primitives.
