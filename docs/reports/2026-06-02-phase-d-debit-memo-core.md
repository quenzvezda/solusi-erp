# Phase D Report: Vendor Debit Memo Core

Status: COMPLETED
Plan: [docs/plans/2026-06-02-phase-d-debit-memo-core.md](../plans/2026-06-02-phase-d-debit-memo-core.md)
Source Brainstorm: [docs/brainstorming/2026-06-02-vendor-debit-memo.md](../brainstorming/2026-06-02-vendor-debit-memo.md)

## Task Log

## Task 1: Add Database Contract, Sequence, Permissions, and Menu Entry

- **Status:** findings
- **Summary:** Added MariaDB/H2 `V73__Add_Debit_Memo_Core.sql` migrations for Debit Memo header/lines, `DEBIT_MEMO` sequence, AP-03 menu entry, permissions, admin grants, and migration contract tests.
- **Tests:** `mvn test -Dtest=DebitMemoCoreMigrationTest` passed.

### Finding: H2 migration location differs from plan target path

- **Type:** deviation
- **Severity:** info
- **Detail:** The plan listed `src/test/resources/db/migration/V73__Add_Debit_Memo_Core.sql`, but this project stores the H2 Flyway mirror under `src/main/resources/db/migration-h2`.
- **Action taken:** Added the H2 mirror to `src/main/resources/db/migration-h2/V73__Add_Debit_Memo_Core.sql`, matching existing Flyway test configuration.
- **Ref:** `src/main/resources/db/migration-h2`

### Finding: H2 exposes named unique constraints separately from indexes

- **Type:** decision
- **Severity:** info
- **Detail:** H2 did not expose named unique constraints through `information_schema.indexes` in the same way as named non-unique indexes.
- **Action taken:** The migration contract test now asserts unique constraints via `information_schema.table_constraints` and non-unique indexes via `information_schema.indexes`.
- **Ref:** `src/test/java/com/solusi/erp/accountspayable/debitmemo/infrastructure/persistence/DebitMemoCoreMigrationTest.java`

## Task 2: Implement Debit Memo Domain Model and Invariants

- **Status:** clean
- **Summary:** Added Debit Memo aggregate, line value object, settlement status enum, and domain tests for monetary invariants, metadata mutability, settlement transitions, cancellation, and defensive copies.
- **Tests:** `mvn test -Dtest=DebitMemoTest` passed with 11 tests.

## Task 3: Add Persistence, Query Models, and Configuration

- **Status:** findings
- **Summary:** Added Debit Memo repository port, JPA entities, MapStruct persistence mapper, repository adapter, filtered query repository, Phase D read models/use cases, and Spring config wiring.
- **Tests:** `mvn test "-Dtest=DebitMemoRepositoryImplTest,DebitMemoQueryUseCaseTest,DebitMemoConfigTest"` passed with 9 tests.

### Finding: Config package follows existing AP infrastructure convention

- **Type:** deviation
- **Severity:** info
- **Detail:** The plan target listed `src/main/java/com/solusi/erp/accountspayable/debitmemo/config/DebitMemoConfig.java`, but existing AP modules keep config under `infrastructure/config`.
- **Action taken:** Created `DebitMemoConfig` under `src/main/java/com/solusi/erp/accountspayable/debitmemo/infrastructure/config` to match `VendorBillConfig` and `VendorPaymentConfig`.
- **Ref:** `src/main/java/com/solusi/erp/accountspayable/vendorbill/infrastructure/config/VendorBillConfig.java`

### Finding: Phase D settlement recap is intentionally zero-consumption

- **Type:** decision
- **Severity:** info
- **Detail:** Allocation and vendor refund are deferred, so list/detail read models cannot calculate consumed amounts yet.
- **Action taken:** Query use cases return `settledAmount = 0`, `refundedAmount = 0`, and `remainingAmount = grossAmountOriginal` while preserving the stored settlement status for future Phase E integration.
- **Ref:** `src/main/java/com/solusi/erp/accountspayable/debitmemo/application/usecase/query`

## Task 4: Auto-Create Debit Memo During Purchase Return Confirmation

- **Status:** findings
- **Summary:** Added `CreateDebitMemoFromPurchaseReturnUseCase`, wired it into Debit Memo config, and invoked it from Purchase Return confirmation after Goods Issue completion and before marking the Purchase Return confirmed.
- **Tests:** `mvn test "-Dtest=CreateDebitMemoFromPurchaseReturnUseCaseTest,ConfirmPurchaseReturnUseCaseTest,DebitMemoConfigTest,PurchaseReturnConfigTest"` passed with 11 tests.

### Finding: Purchase Return stores one DPP/tax snapshot pair, not separate original/base values

- **Type:** decision
- **Severity:** info
- **Detail:** Purchase Return lines expose `clearingAmount` and `taxReversalAmount`, but do not expose separate original/base Debit Memo monetary columns.
- **Action taken:** Debit Memo creation uses `clearingAmount` as DPP and `taxReversalAmount` as tax, storing the same values to original and base fields for Phase D. This preserves the available PR snapshot and keeps Phase E free to refine allocation/tax consumption if separate currency treatment is introduced.
- **Ref:** `src/main/java/com/solusi/erp/purchasing/purchasereturn/domain/model/PurchaseReturnLine.java`

### Finding: Debit Memo creation is idempotent before sequence generation

- **Type:** decision
- **Severity:** info
- **Detail:** The brainstorm requires exactly one Debit Memo per confirmed Purchase Return and retry safety.
- **Action taken:** `CreateDebitMemoFromPurchaseReturnUseCaseImpl` first checks `findByPurchaseReturnId`; if a Debit Memo exists, it returns the existing record without generating a new `DM-*` code.
- **Ref:** `src/main/java/com/solusi/erp/accountspayable/debitmemo/application/usecase/command/CreateDebitMemoFromPurchaseReturnUseCaseImpl.java`

## Task 5: Implement Debit Memo Commands and Friendly Validation

- **Status:** findings
- **Summary:** Added metadata update and cancel commands, duplicate metadata validation, a Phase D allocation-consumption port stub, and command/config tests.
- **Tests:** `mvn test "-Dtest=DebitMemoCommandUseCaseTest,DebitMemoConfigTest"` passed with 8 tests.

### Finding: Allocation consumption check is a replaceable Phase D stub

- **Type:** decision
- **Severity:** info
- **Detail:** Phase D owns Debit Memo core only; confirmed Debit Memo Allocation rows do not exist yet.
- **Action taken:** Added `DebitMemoAllocationConsumptionPort` and wired `NoopDebitMemoAllocationConsumptionAdapter` returning false. Phase E can replace this adapter with the real DMA consumption check without changing `CancelDebitMemoUseCaseImpl`.
- **Ref:** `src/main/java/com/solusi/erp/accountspayable/debitmemo/domain/port/DebitMemoAllocationConsumptionPort.java`

### Finding: Friendly metadata messages are represented as message keys until UI/i18n task

- **Type:** decision
- **Severity:** info
- **Detail:** Existing use cases throw `DomainException` with message keys; message bundle text is owned by the web/i18n task.
- **Action taken:** Duplicate validations throw `msg.error.debit-memo.supplier-memo-number-duplicate` and `msg.error.debit-memo.tax-document-number-duplicate`; Task 6 should add the exact Indonesian text from the brainstorm to message bundles.
- **Ref:** `src/main/java/com/solusi/erp/accountspayable/debitmemo/application/usecase/command/UpdateDebitMemoMetadataUseCaseImpl.java`

## Task 6: Build Debit Memo Web UI and Purchase Return Cross-Link

- **Status:** findings
- **Summary:** Added Debit Memo list/detail web UI, JSON metadata/cancel endpoints, message bundle keys, source document links, and Purchase Return detail cross-link.
- **Tests:** `mvn test "-Dtest=DebitMemoControllerTest,DebitMemoWebMapperTest,DebitMemoTemplateTest,DebitMemoQueryUseCaseTest,DebitMemoConfigTest,PurchaseReturnControllerTest,PurchaseReturnViewIntegrationTest"` passed with 27 tests.

### Finding: Detail template uses the local detail naming convention

- **Type:** deviation
- **Severity:** info
- **Detail:** The plan listed `view.html`, while the implemented controller/view path uses `detail.html` to align with the new Debit Memo route and response naming.
- **Action taken:** Added `src/main/resources/templates/accountspayable/debit-memos/detail.html` and covered the route/template contract in controller and template tests.
- **Ref:** `src/main/resources/templates/accountspayable/debit-memos/detail.html`

### Finding: Generated Goods Issue link is resolved through a read port

- **Type:** decision
- **Severity:** info
- **Detail:** Debit Memo stores Purchase Return identity, but not the generated Goods Issue id. The Purchase Return table already owns that source-document relationship.
- **Action taken:** Added `DebitMemoSourceDocumentPort` and a JDBC adapter that reads `pur_purchase_returns.generated_gi_id` for detail rendering without mutating the Debit Memo aggregate.
- **Ref:** `src/main/java/com/solusi/erp/accountspayable/debitmemo/domain/port/DebitMemoSourceDocumentPort.java`

### Finding: Allocation history remains intentionally non-actionable

- **Type:** decision
- **Severity:** info
- **Detail:** Phase E owns Debit Memo Allocation, so shipping an Allocate action in Phase D would expose behavior that does not exist yet.
- **Action taken:** Rendered an empty allocation-history placeholder and added template coverage that no allocation permission/action is exposed.
- **Ref:** `src/main/resources/templates/accountspayable/debit-memos/detail.html`

## Task 7: Update Documentation and Regression Notes

- **Status:** clean
- **Summary:** Added Debit Memo module documentation, updated Purchase Return docs and docs index navigation, and recorded Phase E handoff notes.
- **Tests:** Documentation review only; no automated test was required for markdown-only changes.

### Commit Trace

| Task | Commit |
|---|---|
| Task 1 | `a4a6b95` |
| Task 2 | `24918c5` |
| Task 3 | `f422756` |
| Task 4 | `6fd6298` |
| Task 5 | `d442fc7` |
| Task 6 | `4d9990c` |
| Task 7 | `6cc02c3` |

## Task 8: Add E2E Coverage and Full Verification Gates

- **Status:** findings
- **Summary:** Extended Purchase Return E2E to verify generated Debit Memo detail/source links/metadata update, hardened existing E2E navigation waits, added Debit Memo warmup, and completed full backend/E2E gates.
- **Tests:** `npx tsc --noEmit` passed; `npx playwright test tests/procurement/purchase-return.spec.ts --list` passed; `mvn clean test` passed with 1925 tests and JaCoCo checks met; final `.\e2e-tests\scripts\run-e2e.ps1` passed with 76/76 tests.

### Finding: JaCoCo branch threshold needed extra Debit Memo guard coverage

- **Type:** test adjustment
- **Severity:** info
- **Detail:** The first full backend run passed Maven but emitted a JaCoCo branch coverage warning (`0.79` vs `0.80`) after the new Phase D classes were added.
- **Action taken:** Added domain guard and source-document adapter tests for Debit Memo, then reran `mvn clean test`; final output reported `All coverage checks have been met`.
- **Ref:** `src/test/java/com/solusi/erp/accountspayable/debitmemo/domain/model/DebitMemoTest.java`

### Finding: E2E first run had route-load flakes outside Debit Memo flow

- **Type:** bug
- **Severity:** warning
- **Detail:** Initial full E2E run exited 0 but reported two flaky tests on `/purchasing/purchase-requisitions` route loading. The likely root cause was waiting on heavy SSR/navigation for deny/sanity checks instead of using the existing best-effort navigation patterns.
- **Action taken:** Hardened RBAC deny classification with an authenticated request probe before rendering, and changed the PR sanity check to use `navigateToModule`.
- **Ref:** `e2e-tests/tests/auth/rbac.spec.ts`

### Finding: E2E second run exposed stale auth/navigation wait assumptions

- **Type:** bug
- **Severity:** warning
- **Detail:** The unauthenticated login test was already on `/login` before `waitForURL` was registered, so the redirect event could be missed. A Purchase Requisition helper also treated `networkidle` as a hard gate even though the page was already rendered and usable.
- **Action taken:** Changed the auth test to assert final URL state with `expect(page).toHaveURL(...)`, and made shared `waitForNetworkIdle` best-effort to match `navigateToModule`.
- **Ref:** `e2e-tests/tests/auth/login.spec.ts`

## Verification

- Task 1: `mvn test -Dtest=DebitMemoCoreMigrationTest` passed.
- Task 2: `mvn test -Dtest=DebitMemoTest` passed.
- Task 3: `mvn test "-Dtest=DebitMemoRepositoryImplTest,DebitMemoQueryUseCaseTest,DebitMemoConfigTest"` passed.
- Task 4: `mvn test "-Dtest=CreateDebitMemoFromPurchaseReturnUseCaseTest,ConfirmPurchaseReturnUseCaseTest,DebitMemoConfigTest,PurchaseReturnConfigTest"` passed.
- Task 5: `mvn test "-Dtest=DebitMemoCommandUseCaseTest,DebitMemoConfigTest"` passed.
- Task 6: `mvn test "-Dtest=DebitMemoControllerTest,DebitMemoWebMapperTest,DebitMemoTemplateTest,DebitMemoQueryUseCaseTest,DebitMemoConfigTest,PurchaseReturnControllerTest,PurchaseReturnViewIntegrationTest"` passed.
- Task 7: Markdown documentation review completed; no automated test required.
- Task 8: `npx tsc --noEmit` passed.
- Task 8: `npx playwright test tests/procurement/purchase-return.spec.ts --list` passed.
- Final backend gate: `mvn clean test` passed with 1925 tests and `All coverage checks have been met`.
- Final E2E gate: `.\e2e-tests\scripts\run-e2e.ps1` passed with 76 tests.

## Notes

- Phase E allocation behavior remains deferred.
- Debit Memo Allocation UI/action remains intentionally inactive in Phase D.
