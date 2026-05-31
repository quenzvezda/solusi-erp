# Implementation Report: Manual Journal Entry

> Plan: `docs/plans/2026-05-31-manual-journal-entry.md`
> Source: `docs/brainstorming/2026-05-31-manual-journal-entry.md`
> Status: IN_PROGRESS

## Task 1: Add MariaDB And H2 Migration V64

- **Status:** clean
- **Summary:** Added MariaDB/H2 V64 migrations for manual journal header fields, line memo, permissions, admin grants, nullable source ID, and unique reversal guard. Verified with `mvn -q -Pe2e -Dtest=JournalManualMigrationTest test`.

## Task 2: Refactor Journal Domain For Manual Lifecycle

### Finding: Persistence mapper compile compatibility

- **Type:** deviation
- **Severity:** info
- **Detail:** Refactoring `JournalEntry.eventType` from `SchemaEventType` to `String` made `JournalPersistenceMapper` fail compile because it called `domain.getEventType().name()` and parsed all rows through `SchemaEventType.valueOf(...)`.
- **Action taken:** Adjusted mapper minimally to store/read the raw event type string. Full persistence mapping for new header fields remains in Task 4 as planned.
- **Ref:** `src/main/java/com/solusi/erp/accounting/journal/infrastructure/persistence/JournalPersistenceMapper.java`

- **Status:** clean
- **Summary:** Added manual journal draft/update/post/reversal lifecycle, line memo support, transaction-currency balance validation, and domain edge tests. Verified with `mvn -q -Dtest=JournalEntryTest,JournalVariableTest test`.

## Task 3: Add Backend Posting Reference Validators

- **Status:** clean
- **Summary:** Added COA and currency posting validator ports, adapters, composition-root beans, and edge tests for active/header/missing/default cases. Verified with `mvn -q -Dtest=CoaPostingValidatorImplTest,CurrencyPostingValidatorImplTest test`.

## Task 4: Extend Journal Persistence Safely

- **Status:** clean
- **Summary:** Added V64 entity fields, raw string event mapping, line memo mapping, safe update mapper, repository find/delete/reversal methods, and adapter tests for existing-row updates. Verified with `mvn -q -Dtest=JournalPersistenceMapperTest,JournalEntryRepositoryImplTest test`.

## Task 5: Extend Journal Read Path And Detail Read Model

### Finding: Controller compile compatibility

- **Type:** deviation
- **Severity:** info
- **Detail:** Changing `GetJournalEntryDetailUseCase` to return `JournalEntryDetailView` required a minimal controller/test adjustment before the planned web task.
- **Action taken:** Mapped the detail view back to the entry in the existing controller path. Full UI exposure of reversal metadata remains in Task 9/10.
- **Ref:** `src/main/java/com/solusi/erp/accounting/journal/web/controller/JournalEntryController.java`

- **Status:** clean
- **Summary:** Switched journal filter source type to string, added reversal lookup to query port, introduced `JournalEntryDetailView`, and covered manual/unknown filters plus original/reversal detail behavior. Verified with `mvn -q -Dtest=JournalEntryFilterTest,JournalEntryQueryPortImplTest,JournalQueryUseCasesTest test`.
