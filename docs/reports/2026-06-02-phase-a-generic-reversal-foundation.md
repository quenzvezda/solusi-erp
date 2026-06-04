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
