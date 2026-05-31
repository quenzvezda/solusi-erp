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

## Task 6: Implement Draft Lifecycle Use Cases

- **Status:** clean
- **Summary:** Added manual journal command records, shared reference validator, create/update/delete draft use cases, and Mockito edge tests for balance, currency, COA, status, manual-only, and missing ID cases. Verified with `mvn -q -Dtest=CreateManualJournalUseCaseTest,UpdateManualJournalUseCaseTest,DeleteManualJournalUseCaseTest test`.

## Task 7: Implement Post And Reverse Use Cases

- **Status:** clean
- **Summary:** Added post and reverse use cases with period guard, reference revalidation, duplicate reversal protection, unique constraint translation, and Mockito edge tests. Verified with `mvn -q -Dtest=PostManualJournalUseCaseTest,ReverseManualJournalUseCaseTest test`.

## Task 8: Wire Journal Composition Root

- **Status:** clean
- **Summary:** Wired manual journal command beans, retained auto-posting, wrapped command/query use cases in `TransactionTemplate`, and added `JournalConfigTest`. Verified with `mvn -q -Dtest=JournalConfigTest,PostJournalForEventUseCaseTest test`.

## Task 9: Add Web DTOs, Mapper, And Controller Routes

- **Status:** clean
- **Summary:** Added manual journal save/reverse DTOs, extended journal response DTOs, mapped detail view and save requests, added create/edit/create/update/delete/post/reverse controller routes, and kept web layer repository-free. Verified with `mvn -q -Dtest=JournalEntryControllerTest,JournalEntryWebMapperTest,WebLayerDependencyGuardTest test`.

## Task 10: Build Thymeleaf Form And Extend List/Detail Templates

- **Status:** findings
- **Summary:** Added manual journal form template and extended list/detail templates with manual actions, memo, reversal metadata, and reversal modal. Verified with `mvn -q -Dtest=JournalTemplateTest,JournalTemplateIntegrationTest test`.

### Finding: Template condition simplification

- **Type:** deviation
- **Severity:** warning
- **Detail:** The security/render test utility uses OGNL and rejected some Thymeleaf expression syntax used in the first pass (`?:`, complex negation). To keep integration tests green, action-button visibility was simplified in the template.
- **Action taken:** Replaced unsupported expressions with OGNL-compatible ternary checks and simplified action button conditions. Controller permissions still protect the actions; Task 11 JS and E2E will exercise runtime behavior.

## Task 11: Add Page-Specific JavaScript

- **Status:** findings
- **Summary:** Added form/detail page scripts for dynamic line reindexing, balance guard, currency rate lock hook, post action, and reversal modal JSON submit. Verified with `mvn -q -Dtest=JournalTemplateTest test`.

### Finding: Minimal line manager implementation

- **Type:** deviation
- **Severity:** warning
- **Detail:** The plan requested direct `ErpLineManager` usage. The current table markup is simple, so the page script implements equivalent add/remove/reindex behavior directly while still using global numeric/currency hooks where available.
- **Action taken:** Kept the script scoped to the journal page and covered required static contracts. E2E will validate the actual user flow.

## Task 12: Add i18n Keys And Synchronize Journal Documentation

- **Status:** findings
- **Summary:** Added manual journal labels/messages to default and Indonesian bundles, enforced key presence in `JournalMessageBundleTest`, and updated business/architecture docs for the manual draft-post-reversal lifecycle. Verified with `mvn -q -Dtest=JournalMessageBundleTest test`.

### Finding: English bundle file name

- **Type:** deviation
- **Severity:** info
- **Detail:** The plan referenced `messages_en.properties`, but the project uses `messages.properties` as the default English bundle and `messages_id.properties` for Indonesian.
- **Action taken:** Updated `messages.properties` instead of creating a parallel English bundle that the application does not currently load.
