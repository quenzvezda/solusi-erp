# Phase D Plan: Vendor Debit Memo Core

Status: IN_PROGRESS
Created: 2026-06-04
Source Brainstorm: [docs/brainstorming/2026-06-02-vendor-debit-memo.md](../brainstorming/2026-06-02-vendor-debit-memo.md)
Progress Report: [docs/reports/2026-06-02-phase-d-debit-memo-core.md](../reports/2026-06-02-phase-d-debit-memo-core.md)

## Scope

Phase D introduces the core Vendor Debit Memo record generated from a confirmed Purchase Return.

In scope:

- Auto-create exactly one Debit Memo for each confirmed Purchase Return.  
  ref: [docs/brainstorming/2026-06-02-vendor-debit-memo.md:85](../brainstorming/2026-06-02-vendor-debit-memo.md)
- Add database idempotency with `UNIQUE (purchase_return_id)`.  
  ref: [docs/brainstorming/2026-06-02-vendor-debit-memo.md:104](../brainstorming/2026-06-02-vendor-debit-memo.md)
- Persist immutable Debit Memo header and line monetary snapshots.  
  ref: [docs/brainstorming/2026-06-02-vendor-debit-memo.md:1246](../brainstorming/2026-06-02-vendor-debit-memo.md)
- Implement settlement lifecycle states `OPEN`, `PARTIALLY_SETTLED`, `SETTLED`, and `CANCELLED`.  
  ref: [docs/brainstorming/2026-06-02-vendor-debit-memo.md:157](../brainstorming/2026-06-02-vendor-debit-memo.md)
- Allow editing only non-financial external metadata while the memo is not cancelled.  
  ref: [docs/brainstorming/2026-06-02-vendor-debit-memo.md:1232](../brainstorming/2026-06-02-vendor-debit-memo.md)
- Add friendly uniqueness validation for supplier memo number and tax document number.  
  ref: [docs/brainstorming/2026-06-02-vendor-debit-memo.md:1290](../brainstorming/2026-06-02-vendor-debit-memo.md)
- Add Debit Memo list/detail UI and Purchase Return detail cross-link.  
  ref: [docs/brainstorming/2026-06-02-vendor-debit-memo.md:690](../brainstorming/2026-06-02-vendor-debit-memo.md)

Out of scope:

- Debit Memo Allocation documents, allocation journal events, VB allocation shortcut, stale draft handling, and allocation locking. These are Phase E.  
  ref: [docs/brainstorming/2026-06-02-vendor-debit-memo.md:1632](../brainstorming/2026-06-02-vendor-debit-memo.md)
- Vendor Refund behavior. Refunded amount stays zero in Phase D read models.  
  ref: [docs/brainstorming/2026-06-02-vendor-debit-memo.md:1127](../brainstorming/2026-06-02-vendor-debit-memo.md)
- Posting any journal at Debit Memo creation time.  
  ref: [docs/brainstorming/2026-06-02-vendor-debit-memo.md:14](../brainstorming/2026-06-02-vendor-debit-memo.md)

## Baseline Code Patterns

- Purchase Return confirmation is currently centralized in `ConfirmPurchaseReturnUseCaseImpl`, which creates and completes the generated Goods Issue before confirming the Purchase Return. Phase D should add Debit Memo creation into this same transactional command path after Goods Issue completion and before persisting `CONFIRMED`.  
  ref: [src/main/java/com/solusi/erp/purchasing/purchasereturn/application/usecase/ConfirmPurchaseReturnUseCaseImpl.java](../../src/main/java/com/solusi/erp/purchasing/purchasereturn/application/usecase/ConfirmPurchaseReturnUseCaseImpl.java)
- Purchase Return command use cases are wired through `PurchaseReturnConfig` and wrapped with `TransactionTemplate`; keep Debit Memo creation inside that existing transaction boundary.  
  ref: [src/main/java/com/solusi/erp/purchasing/purchasereturn/config/PurchaseReturnConfig.java](../../src/main/java/com/solusi/erp/purchasing/purchasereturn/config/PurchaseReturnConfig.java)
- Sequence generation uses `SequenceGeneratorService.generate(moduleCode)`. Add module code `DEBIT_MEMO` with pattern `DM-{date:yyyyMM}-{seq}`.  
  ref: [src/main/java/com/solusi/erp/shared/sequence/SequenceGeneratorService.java](../../src/main/java/com/solusi/erp/shared/sequence/SequenceGeneratorService.java)
- Purchase Return detail already displays the generated Goods Issue link. Add the generated Debit Memo link in the same source-document area, resolved by `purchase_return_id` rather than mutating the Purchase Return aggregate.  
  ref: [src/main/resources/templates/purchasing/purchase-returns/view.html](../../src/main/resources/templates/purchasing/purchase-returns/view.html)
- Follow existing migration split between MariaDB and H2 test resources. Latest migration before Phase D is `V72`; Phase D should use `V73__Add_Debit_Memo_Core.sql`.

## Target Files

Expected new files:

- `src/main/java/com/solusi/erp/accountspayable/debitmemo/domain/*`
- `src/main/java/com/solusi/erp/accountspayable/debitmemo/application/*`
- `src/main/java/com/solusi/erp/accountspayable/debitmemo/infrastructure/persistence/*`
- `src/main/java/com/solusi/erp/accountspayable/debitmemo/web/*`
- `src/main/java/com/solusi/erp/accountspayable/debitmemo/config/DebitMemoConfig.java`
- `src/main/resources/db/migration/V73__Add_Debit_Memo_Core.sql`
- `src/test/resources/db/migration/V73__Add_Debit_Memo_Core.sql`
- `src/main/resources/templates/accountspayable/debit-memos/list.html`
- `src/main/resources/templates/accountspayable/debit-memos/view.html`
- `docs/modules/accountspayable/debit-memo.md`

Expected modified files:

- `src/main/java/com/solusi/erp/purchasing/purchasereturn/application/usecase/ConfirmPurchaseReturnUseCaseImpl.java`
- `src/main/java/com/solusi/erp/purchasing/purchasereturn/config/PurchaseReturnConfig.java`
- `src/main/java/com/solusi/erp/purchasing/purchasereturn/web/PurchaseReturnController.java`
- `src/main/java/com/solusi/erp/purchasing/purchasereturn/web/PurchaseReturnWebMapper.java`
- `src/main/resources/templates/purchasing/purchase-returns/view.html`
- `src/main/resources/messages*.properties`
- `docs/modules/procurement/purchase-return.md`
- E2E specs under `e2e-tests/tests/`

## Task 1: Add Database Contract, Sequence, Permissions, and Menu Entry [x]

Goal: create the persistent contract for Debit Memo core before introducing domain/application code.

Steps:

1. Create `V73__Add_Debit_Memo_Core.sql` in both main and test migration folders.
2. Add `ap_debit_memos` with:
   - `id`
   - `code`
   - `purchase_return_id`
   - `purchase_return_code`
   - `vendor_id`
   - `currency_id`
   - `memo_date`
   - `gross_amount_original`
   - `dpp_amount_original`
   - `tax_amount_original`
   - `gross_amount_base`
   - `dpp_amount_base`
   - `tax_amount_base`
   - `settlement_status`
   - `supplier_memo_number`
   - `supplier_memo_date`
   - `tax_document_number`
   - `tax_document_date`
   - `notes`
   - audit and optimistic locking columns matching local JPA conventions.
3. Add `ap_debit_memo_lines` with:
   - `id`
   - `debit_memo_id`
   - `purchase_return_line_id`
   - `product_id`
   - `quantity`
   - `uom_id`
   - `dpp_amount_original`
   - `tax_amount_original`
   - `dpp_amount_base`
   - `tax_amount_base`
   - audit and optimistic locking columns if line entities require them locally.
4. Add constraints and indexes:
   - unique `code`
   - unique `purchase_return_id`
   - unique `(vendor_id, supplier_memo_number)` with nullable supplier memo number support
   - unique `tax_document_number` with nullable tax document number support
   - FK to Purchase Return, vendor, currency, product, UOM
   - list filters: vendor, status, memo date, source PR code, supplier memo number
5. Add system sequence row:
   - module `DEBIT_MEMO`
   - pattern `DM-{date:yyyyMM}-{seq}`
6. Add permission group/menu entry under Finance & Accounting > Accounts Payable > Debit Memos.  
   ref: [docs/brainstorming/2026-06-02-vendor-debit-memo.md:690](../brainstorming/2026-06-02-vendor-debit-memo.md)  
   ref: [docs/spec/menu-structure.md](../spec/menu-structure.md)
7. Add permissions:
   - `DEBIT-MEMO_READ`
   - `DEBIT-MEMO_UPDATE-METADATA`
   - `DEBIT-MEMO_CANCEL`
8. Grant the new permissions to the admin role following existing migration style.
9. Add or update migration tests to verify:
   - tables exist
   - important columns and constraints exist
   - sequence row exists
   - permissions and grants exist
   - MariaDB/H2 migration parity for names and required columns.

References:

- Debit Memo header and line fields: [docs/brainstorming/2026-06-02-vendor-debit-memo.md:1246](../brainstorming/2026-06-02-vendor-debit-memo.md)
- Metadata uniqueness rules: [docs/brainstorming/2026-06-02-vendor-debit-memo.md:1290](../brainstorming/2026-06-02-vendor-debit-memo.md)
- Permission baseline: [docs/brainstorming/2026-06-02-vendor-debit-memo.md:937](../brainstorming/2026-06-02-vendor-debit-memo.md)
- Existing vendor payment migration pattern: [src/main/resources/db/migration/V63__Add_Vendor_Payment_Module.sql](../../src/main/resources/db/migration/V63__Add_Vendor_Payment_Module.sql)
- Existing Purchase Return migration pattern: [src/main/resources/db/migration/V67__Add_Purchase_Return_Phase_1.sql](../../src/main/resources/db/migration/V67__Add_Purchase_Return_Phase_1.sql)

Validation:

- Run the migration test class for Debit Memo.
- Run Flyway-backed repository/application slice tests that boot the schema.

Commit:

- `schema(debit-memo): add core tables sequence and permissions`

## Task 2: Implement Debit Memo Domain Model and Invariants [x]

Goal: model Debit Memo as an AP aggregate with immutable source/financial snapshots and controlled settlement metadata.

Steps:

1. Add `DebitMemo`, `DebitMemoLine`, and `DebitMemoSettlementStatus`.
2. Add factory behavior for creation from a confirmed Purchase Return snapshot:
   - require source Purchase Return id/code
   - require vendor and currency
   - require memo date from Purchase Return return date
   - require one or more lines
   - calculate header DPP/tax/gross sums from lines
   - set settlement status to `OPEN`.
3. Add settlement state behavior:
   - `OPEN -> PARTIALLY_SETTLED`
   - `PARTIALLY_SETTLED -> SETTLED`
   - `OPEN -> CANCELLED`
   - no cancellation after confirmed allocation in future phases.
4. Add metadata update behavior:
   - editable fields: `supplierMemoNumber`, `supplierMemoDate`, `taxDocumentNumber`, `taxDocumentDate`, `notes`
   - allowed for `OPEN`, `PARTIALLY_SETTLED`, and `SETTLED`
   - rejected for `CANCELLED`
   - no mutation of source, vendor, currency, memo date, line, or amount fields.
5. Enforce monetary invariants:
   - line quantity > 0
   - line DPP/tax original/base >= 0
   - header values equal sum of lines
   - header gross original = DPP + tax
   - gross original > 0
   - no Debit Memo without lines
   - zero tax is allowed
   - DPP zero with positive tax is rejected.
6. Use `BigDecimal` consistently with existing monetary patterns and persist DECIMAL(19,4).
7. Add domain tests for:
   - happy path creation
   - header amount calculation
   - zero/negative invalid lines
   - gross zero rejected
   - immutable financial values
   - metadata update statuses
   - cancel transition.

References:

- Lifecycle states: [docs/brainstorming/2026-06-02-vendor-debit-memo.md:157](../brainstorming/2026-06-02-vendor-debit-memo.md)
- Read-only and editable fields: [docs/brainstorming/2026-06-02-vendor-debit-memo.md:1221](../brainstorming/2026-06-02-vendor-debit-memo.md)
- Monetary invariants: [docs/brainstorming/2026-06-02-vendor-debit-memo.md:1324](../brainstorming/2026-06-02-vendor-debit-memo.md)

Validation:

- Run Debit Memo domain unit tests.

Commit:

- `feat(debit-memo): add core aggregate and invariants`

## Task 3: Add Persistence, Query Models, and Configuration [x]

Goal: make Debit Memo durable and queryable without coupling controllers directly to JPA entities.

Steps:

1. Add JPA entities and mapper for Debit Memo header and lines.
2. Add repository port and JPA adapter:
   - `save`
   - `findById`
   - `findByPurchaseReturnId`
   - `existsByPurchaseReturnId`
   - metadata uniqueness checks
   - paged/filter list query
3. Add read DTOs for list/detail:
   - list columns: code, memo date, vendor, currency, source Purchase Return, gross, settled, remaining, settlement status.
   - detail header, source links, metadata, line snapshots, settlement recap.
4. In Phase D, calculate settlement recap as:
   - gross = stored gross
   - settled = zero
   - refunded = zero
   - remaining = gross
   - status = stored settlement status.
5. Add `DebitMemoConfig` for command/query use cases and repository adapters, matching local module configuration patterns.
6. Add config tests to prove all Debit Memo beans are wired.
7. Add repository tests for:
   - save and reload aggregate
   - unique `purchase_return_id`
   - list filters
   - detail lookup
   - metadata uniqueness checks.

References:

- Debit Memo list/detail expectations: [docs/brainstorming/2026-06-02-vendor-debit-memo.md:699](../brainstorming/2026-06-02-vendor-debit-memo.md)
- Phase D refunded amount deferral: [docs/brainstorming/2026-06-02-vendor-debit-memo.md:1127](../brainstorming/2026-06-02-vendor-debit-memo.md)
- Existing config pattern: [src/main/java/com/solusi/erp/purchasing/purchasereturn/config/PurchaseReturnConfig.java](../../src/main/java/com/solusi/erp/purchasing/purchasereturn/config/PurchaseReturnConfig.java)

Validation:

- Run Debit Memo repository tests.
- Run Debit Memo config tests.

Commit:

- `feat(debit-memo): persist and query core records`

## Task 4: Auto-Create Debit Memo During Purchase Return Confirmation

Goal: integrate Debit Memo creation into the Purchase Return confirmation transaction.

Steps:

1. Add an application use case or port in the AP Debit Memo module for `createFromPurchaseReturn`.
2. Inject that use case/port into `ConfirmPurchaseReturnUseCaseImpl`.
3. Keep the confirmation order aligned with the brainstorm:
   - validate Purchase Return is approved
   - create and complete Goods Issue
   - post the Purchase Return journal through existing Goods Issue completion flow
   - create Debit Memo `OPEN`
   - mark Purchase Return `CONFIRMED`
   - commit.  
   ref: [docs/brainstorming/2026-06-02-vendor-debit-memo.md:108](../brainstorming/2026-06-02-vendor-debit-memo.md)
4. Build the Debit Memo snapshot from Purchase Return header and lines:
   - source id/code
   - supplier/vendor id
   - currency id
   - return date as memo date
   - Purchase Return line id, product, quantity, UOM
   - DPP/tax original and base amounts from the Purchase Return line's Phase B monetary fields.
5. Confirm the exact DPP/tax source fields before implementation:
   - prefer existing Purchase Return line fields created for Phase B accounting
   - report and adjust the plan if those fields do not contain enough tax/DPP data for an auditable Debit Memo snapshot.
6. Use `SequenceGeneratorService.generate("DEBIT_MEMO")` for the code.
7. Add idempotency:
   - pre-check existing Debit Memo by `purchaseReturnId`
   - preserve the database unique guard
   - if a retry reaches the create step, return the existing Debit Memo or fail with a controlled duplicate-source message instead of creating a second record.
8. Ensure transaction rollback if Debit Memo creation fails after Goods Issue completion.
9. Update Purchase Return config tests for the new dependency.
10. Update Purchase Return confirmation tests for:
    - Debit Memo created once on happy path
    - creation occurs after Goods Issue completion
    - Debit Memo failure leaves Purchase Return unconfirmed
    - duplicate Debit Memo guard
    - closed period still stops before side effects.

References:

- One Purchase Return to one Debit Memo: [docs/brainstorming/2026-06-02-vendor-debit-memo.md:85](../brainstorming/2026-06-02-vendor-debit-memo.md)
- Confirmation atomicity and idempotency: [docs/brainstorming/2026-06-02-vendor-debit-memo.md:108](../brainstorming/2026-06-02-vendor-debit-memo.md)
- Current confirmation use case: [src/main/java/com/solusi/erp/purchasing/purchasereturn/application/usecase/ConfirmPurchaseReturnUseCaseImpl.java](../../src/main/java/com/solusi/erp/purchasing/purchasereturn/application/usecase/ConfirmPurchaseReturnUseCaseImpl.java)
- Current confirmation tests: [src/test/java/com/solusi/erp/purchasing/purchasereturn/application/usecase/ConfirmPurchaseReturnUseCaseTest.java](../../src/test/java/com/solusi/erp/purchasing/purchasereturn/application/usecase/ConfirmPurchaseReturnUseCaseTest.java)

Validation:

- Run Purchase Return confirmation tests.
- Run Debit Memo creation/application tests.

Commit:

- `feat(purchase-return): create debit memo on confirmation`

## Task 5: Implement Debit Memo Commands and Friendly Validation

Goal: expose Phase D behavior through application commands with business validation before database constraint failures.

Steps:

1. Add list/detail query use cases:
   - list by keyword, vendor, settlement status, and memo date range
   - detail by id
   - source lookup by Purchase Return id for cross-linking.
2. Add metadata update command:
   - require `DEBIT-MEMO_UPDATE-METADATA`
   - validate supplier memo uniqueness per vendor before save
   - validate tax document uniqueness before save
   - return friendly validation messages:
     - `Nomor debit memo supplier sudah digunakan untuk vendor ini.`
     - `Nomor dokumen pajak sudah digunakan.`
3. Add cancel command:
   - require `DEBIT-MEMO_CANCEL`
   - allow only `OPEN`
   - reject if future confirmed allocation consumption exists
   - in Phase D this allocation check can be a zero-result port/stub that Phase E will replace.
4. Add tests for:
   - filter query behavior
   - metadata happy path
   - duplicate supplier memo number for same vendor
   - same supplier memo number for different vendor
   - duplicate tax document number
   - metadata rejected for cancelled memo
   - cancel happy path from `OPEN`
   - cancel rejected from non-open status.

References:

- Metadata fields and uniqueness: [docs/brainstorming/2026-06-02-vendor-debit-memo.md:1232](../brainstorming/2026-06-02-vendor-debit-memo.md)
- Friendly messages: [docs/brainstorming/2026-06-02-vendor-debit-memo.md:1297](../brainstorming/2026-06-02-vendor-debit-memo.md)
- Lifecycle cancellation rule: [docs/brainstorming/2026-06-02-vendor-debit-memo.md:172](../brainstorming/2026-06-02-vendor-debit-memo.md)

Validation:

- Run Debit Memo command/query tests.

Commit:

- `feat(debit-memo): add commands and metadata validation`

## Task 6: Build Debit Memo Web UI and Purchase Return Cross-Link

Goal: make Debit Memo visible and maintainable from the AP UI while keeping Phase E allocation actions inactive.

Steps:

1. Add `DebitMemoController` under `/accounts-payable/debit-memos`.
2. Add DTOs/request models with Spring date binding:
   - use `@DateTimeFormat(pattern = "yyyy-MM-dd")`
   - use `data-picker="date"` in templates.  
   ref: [docs/spec/datetime-standards.md](../spec/datetime-standards.md)
3. Add list page:
   - columns: Code, Memo Date, Vendor, Currency, Source Purchase Return, Gross, Settled, Remaining, Settlement Status, Actions View
   - filters: keyword, vendor, status, memo date range.
4. Add detail page:
   - header summary
   - source Purchase Return link
   - generated Goods Issue link
   - external metadata form
   - line snapshot table
   - settlement recap
   - empty allocation history placeholder, clearly non-actionable until Phase E
   - actions: Update Metadata, Cancel.
5. Do not expose a working Allocate action in Phase D; the brainstorm places allocation under Phase E.  
   ref: [docs/brainstorming/2026-06-02-vendor-debit-memo.md:1632](../brainstorming/2026-06-02-vendor-debit-memo.md)
6. Follow form/action conventions:
   - AJAX forms use `data-ajax-form="true"`, JSON, and redirect-on-success.
   - cancel uses existing Bootstrap confirmation/action-button pattern.
   - no `type="number"` for monetary or quantity fields; render read-only formatted numbers.
7. Add permissions on controller methods:
   - read/list/detail require `DEBIT-MEMO_READ`
   - metadata update requires `DEBIT-MEMO_UPDATE-METADATA`
   - cancel requires `DEBIT-MEMO_CANCEL`.
8. Add Purchase Return detail cross-link:
   - resolve Debit Memo by Purchase Return id through query port
   - show link near generated Goods Issue link
   - do not add Debit Memo identity into the Purchase Return aggregate unless implementation proves a strong need.
9. Add i18n keys in message files for all new labels, statuses, validation messages, menu entries, and buttons.
10. Add controller/template/mapper tests for:
    - permissions
    - list model and filters
    - detail model
    - metadata form attributes
    - cancel action button attributes
    - Purchase Return detail contains Debit Memo link when present.

References:

- UI list/detail: [docs/brainstorming/2026-06-02-vendor-debit-memo.md:699](../brainstorming/2026-06-02-vendor-debit-memo.md)
- Cross-links: [docs/brainstorming/2026-06-02-vendor-debit-memo.md:766](../brainstorming/2026-06-02-vendor-debit-memo.md)
- Form submission standard: [docs/spec/form-submission.md](../spec/form-submission.md)
- Action button standard: [docs/spec/action-buttons.md](../spec/action-buttons.md)
- Numeric standard: [docs/spec/numeric-standards.md](../spec/numeric-standards.md)
- Layout standard: [docs/spec/layout-standard.md](../spec/layout-standard.md)
- i18n standard: [docs/spec/i18n-guide.md](../spec/i18n-guide.md)
- Existing Purchase Return view: [src/main/resources/templates/purchasing/purchase-returns/view.html](../../src/main/resources/templates/purchasing/purchase-returns/view.html)

Validation:

- Run Debit Memo controller, mapper, and template tests.
- Run Purchase Return controller/view tests.

Commit:

- `feat(debit-memo): add web screens and purchase return link`

## Task 7: Update Documentation and Regression Notes

Goal: document what Phase D adds and what remains deferred.

Steps:

1. Add `docs/modules/accountspayable/debit-memo.md` covering:
   - Debit Memo creation source
   - no journal on creation
   - immutable source/financial snapshot
   - editable external metadata
   - settlement statuses
   - cancellation rules
   - allocation and refund deferrals.
2. Update Purchase Return module docs to mention confirmed Purchase Returns now generate a Debit Memo.
3. Update AP module index/navigation docs if present.
4. Update this plan's report file after each implementation task with:
   - status
   - commit hash
   - tests run
   - implementation notes
   - deviations from brainstorm/plan.
5. Add a Phase E handoff note:
   - Debit Memo Allocation should consume these core records
   - settlement recap currently uses zero allocated/refunded
   - allocation history UI is intentionally empty in Phase D.

References:

- Phase D roadmap: [docs/brainstorming/2026-06-02-vendor-debit-memo.md:1611](../brainstorming/2026-06-02-vendor-debit-memo.md)
- Phase E roadmap: [docs/brainstorming/2026-06-02-vendor-debit-memo.md:1632](../brainstorming/2026-06-02-vendor-debit-memo.md)

Validation:

- Review docs for consistency with implementation and deferred scope.

Commit:

- `docs(debit-memo): document core phase d behavior`

## Task 8: Add E2E Coverage and Full Verification Gates

Goal: prove Phase D works through the browser flow and keep the full regression suite green.

Steps:

1. Read the Playwright guide and pitfalls before editing E2E specs.  
   ref: [docs/tests/playwright-e2e-guide.md](../tests/playwright-e2e-guide.md)  
   ref: [docs/tests/playwright-pitfalls.md](../tests/playwright-pitfalls.md)
2. Extend the Purchase Return E2E flow or add a focused Debit Memo spec:
   - create or reuse an approved Purchase Return
   - confirm it
   - assert Purchase Return detail shows generated Debit Memo link
   - open Debit Memo detail
   - assert status `OPEN`
   - assert source Purchase Return link
   - assert generated Goods Issue link
   - assert gross and remaining amounts are visible
   - assert line snapshot table has returned items.
3. Add E2E coverage for metadata update:
   - update supplier memo number/date and tax document number/date
   - assert values persist on detail reload
   - assert duplicate metadata validation if the setup can create a deterministic second memo without making the spec brittle.
4. Avoid Phase E allocation actions in E2E.
5. Follow Playwright pitfalls:
   - use request API for pre-navigation setup where required
   - do not rely on absent helper payload fields
   - run the edited spec before marking the task done.
6. Run targeted backend tests touched by Phase D.
7. Run full backend gate:
   - `mvn clean test`
   - JaCoCo thresholds must pass.
8. Run full E2E gate:
   - `.\e2e-tests\scripts\run-e2e.ps1`
9. Record all commands and outcomes in the report.

References:

- E2E pitfalls and "run before done" rule: [docs/tests/playwright-pitfalls.md](../tests/playwright-pitfalls.md)
- User requirement for final gate: `mvn clean test` and `e2e-tests\scripts\run-e2e.ps1` must pass.

Validation:

- `mvn clean test`
- `.\e2e-tests\scripts\run-e2e.ps1`

Commit:

- `test(debit-memo): cover phase d browser flow`

## Final Completion Checklist

- [ ] Every task has a commit.
- [ ] Report file contains commit hashes and verification output summary.
- [ ] `mvn clean test` passes with JaCoCo threshold.
- [ ] `.\e2e-tests\scripts\run-e2e.ps1` passes.
- [ ] Phase E deferrals are explicit in docs and UI.
- [ ] No active Debit Memo Allocation UI/action is shipped in Phase D.
