# Implementation Report: Generic Goods Issue Core

> Plan: `docs/plans/2026-06-01-generic-goods-issue.md`
> Source: `docs/brainstorming/2026-06-01-generic-goods-issue.md`
> Created: 2026-06-01

## Findings

(Populated during execution by execute-plan skill)

## Task 1: Stock Valuation Reference Metadata
- **Status:** findings
- **Summary:** Added valuation layer source-reference metadata, specific-layer consumption API, StockService delegation, GR valuation reference propagation, and focused unit coverage.

### Finding: Task order adjusted for TDD
- **Type:** deviation
- **Severity:** info
- **Detail:** The approved plan listed implementation steps before test steps, but the active TDD skill requires tests to be written and observed failing before production code.
- **Action taken:** Added RED tests in `FifoValuationServiceTest` and `StockServiceTest`, observed expected compile failures for missing API, then implemented the production changes.
- **Ref:** `src/test/java/com/solusi/erp/inventory/stock/domain/FifoValuationServiceTest.java`

### Finding: H2 migration syntax differs from MariaDB migration
- **Type:** decision
- **Severity:** info
- **Detail:** The MariaDB migration uses `AFTER` column placement for readability, but H2 migrations are safer without MySQL-specific column-position syntax.
- **Action taken:** Kept MariaDB migration with `AFTER`; used separate simple `ALTER TABLE ... ADD COLUMN` statements in `migration-h2`.
- **Ref:** `src/main/resources/db/migration-h2/V65__Add_Valuation_Layer_Reference_Metadata.sql`

## Task 2: Goods Issue Migration, Sequence, Permission, And Menu
- **Status:** findings
- **Summary:** Added GI core migrations for MariaDB and H2, sequence registration, menu/permission seeds, i18n keys, and a focused H2 Flyway migration test.

### Finding: Migration version shifted from V65 to V66
- **Type:** deviation
- **Severity:** info
- **Detail:** The plan named `V65__Add_Goods_Issue_Core.sql`, but Task 1 consumed `V65` for valuation layer reference metadata.
- **Action taken:** Used `V66__Add_Goods_Issue_Core.sql` in both `db/migration` and `db/migration-h2`, and updated the plan checklist text to match the actual migration.
- **Ref:** `src/main/resources/db/migration/V66__Add_Goods_Issue_Core.sql`

## Task 3: Goods Issue Domain, Repository, And Persistence
- **Status:** findings
- **Summary:** Added pure GI domain models, repository contract, JPA entities, MapStruct persistence mapper, repository adapter/configuration, and focused domain/mapper tests.

### Finding: Task order adjusted for TDD
- **Type:** deviation
- **Severity:** info
- **Detail:** The approved plan listed implementation steps before tests, but the active TDD workflow requires RED tests before production code.
- **Action taken:** Added `GoodsIssueTest` and `GoodsIssuePersistenceMapperTest`, observed expected compile failures for missing GI domain/persistence types, then implemented the production code and reran the focused tests successfully.
- **Ref:** `src/test/java/com/solusi/erp/inventory/goodsissue/domain/model/GoodsIssueTest.java`

### Finding: Focused verification run was interrupted once
- **Type:** execution note
- **Severity:** info
- **Detail:** One focused Maven run stopped with `^C` before producing a valid result.
- **Action taken:** Reran `mvn test -Dtest=GoodsIssueTest,GoodsIssuePersistenceMapperTest -DfailIfNoTests=false`; it passed with 9 tests, 0 failures, 0 errors.
- **Ref:** `src/test/java/com/solusi/erp/inventory/goodsissue/infrastructure/persistence/GoodsIssuePersistenceMapperTest.java`

## Task 4: Resolver Registry And Create/Edit View Query Use Cases
- **Status:** findings
- **Summary:** Added GI source resolver port, EnumMap resolver registry, no-op reference lookup provider for future sources, create/edit/get/find query use cases, Spring wiring, and focused registry/create/query tests.

### Finding: Purchase Return adapter deferred
- **Type:** pending dependency
- **Severity:** info
- **Detail:** The plan allows `PurchaseReturnGoodsIssueSourceResolver` only when a Purchase Return domain/repository exists. No `PurchaseReturn` production type is present under `src/main/java`.
- **Action taken:** Did not add a concrete Purchase Return resolver. Kept GI core source-agnostic through the resolver port/registry and a no-op lookup provider that returns empty data for unimplemented future source types.
- **Ref:** `src/main/java/com/solusi/erp/inventory/goodsissue/domain/port/GoodsIssueSourceResolver.java`

### Finding: Task order adjusted for TDD
- **Type:** deviation
- **Severity:** info
- **Detail:** The approved plan listed implementation steps before tests, but the active TDD workflow requires RED tests before production code.
- **Action taken:** Added registry/create-view/query tests first, observed expected compile failures for missing ports/use cases, then implemented production code and reran the focused tests successfully.
- **Ref:** `src/test/java/com/solusi/erp/inventory/goodsissue/application/usecase/query/GetGoodsIssueCreateViewUseCaseTest.java`

## Task 5: Command Use Cases For Save, Complete, Delete, And Cancel
- **Status:** findings
- **Summary:** Added GI command records/use cases for create, update, delete, complete, and cancel; wired complete/cancel through `TransactionTemplate`; forwarded specific valuation references into stock payloads; and covered draft, complete, serialized, and cancel behavior with focused Mockito tests.

### Finding: Journal reversal uses existing posting API
- **Type:** decision
- **Severity:** info
- **Detail:** The current journal command API does not expose a dedicated reversal operation for source documents.
- **Action taken:** `CancelGoodsIssueUseCaseImpl` posts a GOODS_ISSUE journal command with negative GI values as the reversal representation, while preserving stock reversal movements and setting GI status to `CANCELLED`.
- **Ref:** `src/main/java/com/solusi/erp/inventory/goodsissue/application/usecase/command/CancelGoodsIssueUseCaseImpl.java`

### Finding: Source-specific eligibility remains extension-based
- **Type:** pending dependency
- **Severity:** info
- **Detail:** Purchase Return source classes still do not exist, so concrete source eligibility validation cannot bind to PR rules yet.
- **Action taken:** Kept create source resolution behind `GoodsIssueSourceResolverRegistry` and implemented completion against the generic GI snapshot. Future PR resolver/validator can add source rules without changing the controller contract.
- **Ref:** `src/main/java/com/solusi/erp/inventory/goodsissue/application/usecase/command/CompleteGoodsIssueUseCaseImpl.java`

### Finding: Focused Maven run emitted a JaCoCo warning but exited successfully
- **Type:** execution note
- **Severity:** info
- **Detail:** `mvn test -Dtest=*GoodsIssue*UseCaseTest -DfailIfNoTests=false` reported 19 tests with 0 failures/errors and `BUILD SUCCESS`, while JaCoCo printed a branch-coverage warning in the focused subset run.
- **Action taken:** Treated the focused Task 5 validation as passed because Maven exited successfully; the final `mvn clean test` remains the authoritative full-suite gate.
- **Ref:** `src/test/java/com/solusi/erp/inventory/goodsissue/application/usecase/command/CompleteGoodsIssueUseCaseTest.java`
