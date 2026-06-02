# Implementation Report: Purchase Return Phase 1

> Plan: `docs/plans/2026-06-01-purchase-return.md`
>
> Source: `docs/brainstorming/2026-06-01-purchase-return.md`

## Task 1: Flyway Schema for Generic Reservation and Purchase Return

### Finding: Goods Receipt line table uses procurement prefix
- **Type:** deviation
- **Severity:** info
- **Detail:** The initial Purchase Return line foreign key used `inv_goods_receipt_lines`, while the existing Goods Receipt schema names the table `pur_goods_receipt_lines`.
- **Action taken:** Corrected both MariaDB and H2 V67 migrations and verified the H2 migration end-to-end.
- **Ref:** `src/main/resources/db/migration/V67__Add_Purchase_Return_Phase_1.sql`

- **Status:** clean after correction
- **Summary:** Added generic reservation and Purchase Return schema, source indexes, generated-GI uniqueness guard, sequence registration, H2 mirror, and migration contract coverage.
- **Verification:** `mvn test -Dtest=PurchaseReturnMigrationTest`

## Task 2: Generic Inventory Reservation Domain
- **Status:** clean
- **Summary:** Enforced `reserved <= onHand`, added the generic reservation aggregate, ownership and service ports, serialized-stock validation, and focused domain coverage.
- **Verification:** `mvn test -Dtest=StockBalanceDomainTest,InventoryReservationTest`

## Task 3: Reservation Persistence and Inventory Availability Enforcement

### Finding: Stock payload already carries sufficient owner audit metadata
- **Type:** decision
- **Severity:** info
- **Detail:** Existing `StockMovementPayload.referenceType/referenceId/referenceCode` fields are sufficient to audit reservation movements against their Purchase Return owner.
- **Action taken:** Added `ReferenceType.PURCHASE_RETURN` and reused the existing payload contract instead of adding parallel owner fields.
- **Ref:** `src/main/java/com/solusi/erp/inventory/stock/application/dto/StockMovementPayload.java`

- **Status:** clean
- **Summary:** Added reservation JPA persistence, repository adapter, transactional reservation service, stock config wiring, outbound availability regression coverage, rollback expectation coverage, and a focused config test.
- **Verification:** `mvn test -Dtest=StockBalanceDomainTest,StockServiceTest,InventoryReservationServiceTest,StockConfigTest`

## Task 4: Purchase Return Domain Aggregate

### Finding: Mixed-GR line validation belongs to the source read boundary
- **Type:** decision
- **Severity:** info
- **Detail:** The approved schema stores canonical GR ownership on the Purchase Return header and `goods_receipt_line_id` on each line, without duplicating GR header ID per line.
- **Action taken:** The aggregate enforces a canonical `GOODS_RECEIPT` header source. Task 7 will validate each selected line against slices returned for that GR before constructing domain lines.
- **Ref:** `src/main/java/com/solusi/erp/purchasing/purchasereturn/domain/model/PurchaseReturn.java`

- **Status:** clean
- **Summary:** Added Purchase Return reason/status enums, immutable line snapshots, aggregate lifecycle transitions, source and serial invariants, and branch-focused domain tests.
- **Verification:** `mvn test -Dtest=PurchaseReturnTest,PurchaseReturnLineTest`

## Task 5: Purchase Return Persistence and Spring Wiring

### Finding: Composition root must grow with later tasks
- **Type:** deviation
- **Severity:** info
- **Detail:** Task 5 requests repository and pure use-case wiring, but Purchase Return use-case classes are introduced only in Tasks 6-9.
- **Action taken:** Added and verified the repository bean now. The same `PurchaseReturnConfig` will be extended with query and transactional command beans when their implementations land.
- **Ref:** `src/main/java/com/solusi/erp/purchasing/purchasereturn/infrastructure/config/PurchaseReturnConfig.java`

- **Status:** clean
- **Summary:** Added audited Purchase Return JPA entities, MapStruct persistence mapping, repository port and adapter, composition root, and round-trip/config tests.
- **Verification:** `mvn test -Dtest=PurchaseReturnPersistenceMapperTest,PurchaseReturnConfigTest`

## Task 6: Returnable GR Query and Selector Read Models

### Finding: Reservation subtraction must happen after valuation fragment aggregation
- **Type:** decision
- **Severity:** warning
- **Detail:** A GR valuation origin can be represented by multiple layer fragments for the same actual container after stock movement. Joining reservation rows directly to raw layers would subtract the same reservation once per fragment.
- **Action taken:** Aggregated valuation layers by origin, product, actual container, and serial before joining active reservation ownership. Selector grouping remains `grLineId + actualContainerId`.
- **Ref:** `src/main/java/com/solusi/erp/purchasing/purchasereturn/infrastructure/adapter/PurchaseReturnSourceQueryAdapter.java`

- **Status:** clean
- **Summary:** Added eligible GR, PO lookup, non-serial slice, and serial read models; implemented SQL-backed source queries with query-level exclusion; wired paged query use cases and contract coverage.
- **Verification:** `mvn test -Dtest=PurchaseReturnSourceQueryAdapterTest,*PurchaseReturn*Selector*UseCaseTest,GetPurchaseReturnCreateViewUseCaseTest,FindEligiblePurchaseReturnGoodsReceiptsUseCaseTest,PurchaseReturnConfigTest`

## Task 7: Purchase Return Draft Application Use Cases

### Finding: Browser base quantity must not become a stock snapshot authority
- **Type:** decision
- **Severity:** warning
- **Detail:** Returnable selector quantities originate from valuation layers and are already expressed in base inventory units. Trusting a hidden browser `baseQuantity` would allow a stale or tampered payload to reserve a different amount later.
- **Action taken:** Rebuild product, UOM, actual location, valuation, monetary snapshots, and non-serial base quantity from current selector data. Serialized quantities are derived from selected serial count.
- **Ref:** `src/main/java/com/solusi/erp/purchasing/purchasereturn/application/usecase/command/PurchaseReturnDraftLineFactory.java`

- **Status:** clean
- **Summary:** Added draft create/update/cancel, list/detail/edit queries, sequence generation, stale selector validation, snapshot rebuilding, transactional wiring, and focused Mockito coverage.
- **Verification:** `mvn test -Dtest=*PurchaseReturn*Draft*Test,CreatePurchaseReturnUseCaseTest,UpdatePurchaseReturnUseCaseTest,CancelDraftPurchaseReturnUseCaseTest,FindPurchaseReturnsUseCaseTest,PurchaseReturnConfigTest`

## Task 8: Submit, Approval, Reject, and Cancel Submission Integration

### Finding: Submission validation must run before reservation side effects
- **Type:** decision
- **Severity:** warning
- **Detail:** Calling the reservation service before validating Purchase Return status or submitter user ID would allow duplicate or malformed submissions to touch stock before failing.
- **Action taken:** Extracted aggregate submission validation and invoked it before reserving inventory. A reservation failure still leaves the persisted Purchase Return in DRAFT and publishes no approval event.
- **Ref:** `src/main/java/com/solusi/erp/purchasing/purchasereturn/application/usecase/command/SubmitPurchaseReturnUseCaseImpl.java`

- **Status:** clean
- **Summary:** Added generic approval cancellation, Purchase Return approval publishing, submit-time reservation, approved/rejected listeners, submitted and approved cancellation flows, transactional wiring, and lifecycle coverage.
- **Verification:** `mvn test -Dtest=ApprovalRequestTest,CancelApprovalRequestUseCaseTest,SubmitPurchaseReturnUseCaseTest,OnPurchaseReturnApprovedListenerTest,OnPurchaseReturnRejectedListenerTest,CancelPurchaseReturnSubmissionUseCaseTest,CancelApprovedPurchaseReturnUseCaseTest,PurchaseReturnConfigTest`

## Task 9: Purchase Return Confirm and Goods Issue Resolver

### Finding: Goods Issue source snapshot needed Purchase Return date
- **Type:** deviation
- **Severity:** warning
- **Detail:** The existing Purchase Return GI seam carried header accounting snapshots but omitted `returnDate`, while the locked date rule requires generated GI `issueDate` to copy it.
- **Action taken:** Extended `HeaderSnapshot` with `returnDate` and used it in the resolver and confirm path.
- **Ref:** `src/main/java/com/solusi/erp/inventory/goodsissue/domain/port/PurchaseReturnGoodsIssueSourcePort.java`

### Finding: Phase 1 clearing metadata has no persisted Debit Memo model yet
- **Type:** decision
- **Severity:** info
- **Detail:** `billPosted` and clearing account targeting belong to the deferred Debit Memo and dedicated Purchase Return accounting flow.
- **Action taken:** Kept the GI source contract explicit and returned `false` / `null` placeholders in Phase 1. Generated GI continues to post generic `SchemaEventType.GOODS_ISSUE`.
- **Ref:** `src/main/java/com/solusi/erp/purchasing/purchasereturn/infrastructure/adapter/PurchaseReturnGoodsIssueSourceAdapter.java`

- **Status:** clean
- **Summary:** Added approved-return GI snapshot adapter and resolver, OPEN-period confirm flow, completed-GI idempotency guard, generated-GI linking, reserved outbound movement coverage assertion, post-journal reservation consumption, and generic GI regression coverage.
- **Verification:** `mvn test -Dtest=CompleteGoodsIssueUseCaseTest,PurchaseReturnGoodsIssueSourceAdapterTest,PurchaseReturnGoodsIssueSourceResolverTest,ConfirmPurchaseReturnUseCaseTest,PurchaseReturnConfigTest,PurchaseReturnGoodsIssueSourcePortTest`

## Task 10: Web DTOs, Mapper, Controller, and Lookup Boundary

### Finding: Submit identity has two distinct meanings
- **Type:** decision
- **Severity:** warning
- **Detail:** Purchase Return stores the authenticated submitting user ID for creator-only cancellation, while generic approval stores the authenticated requester's party ID.
- **Action taken:** Extracted and passed both values separately from `SecurityUser` in the submit endpoint.
- **Ref:** `src/main/java/com/solusi/erp/purchasing/purchasereturn/web/controller/PurchaseReturnController.java`

- **Status:** clean
- **Summary:** Added Purchase Return request/response DTOs, ISO date binding, lookup-provider mapper, SSR/JSON controller routes, PO source autocomplete endpoint, source and serial selector endpoints, approval panel attributes, and permission contracts.
- **Verification:** `mvn test -Dtest=PurchaseReturnControllerTest,PurchaseReturnWebMapperTest`
- **Boundary check:** `rg "JpaRepository|infrastructure\\.persistence|domain\\.repository" src/main/java/com/solusi/erp/purchasing/purchasereturn/web -n` returned no matches.

## Task 11: Thymeleaf Pages and Frontend Interaction Wiring

### Finding: Serialized return lines need selector context before individual serial selection
- **Type:** decision
- **Severity:** warning
- **Detail:** The source slice query originally returned non-serial rows only. That left the create form without an original GR-line context from which to open the serialized-item selector.
- **Action taken:** Included serialized source slices grouped by actual container while preserving individual serial selection in the dedicated serial endpoint. The page script groups applied serials by current container and derives quantity from selected serial count.
- **Ref:** `src/main/java/com/solusi/erp/purchasing/purchasereturn/infrastructure/adapter/PurchaseReturnSourceQueryAdapter.java`

### Finding: Selector Trinity payload needed grid and container names
- **Type:** decision
- **Severity:** info
- **Detail:** Facility carried a display name, but grid and container slices exposed only IDs and codes. This was insufficient for consistent `id`, `name`, `subText` payloads.
- **Action taken:** Added grid and container names to source read models, DTO snapshots, mapper prefill, and selector `data-*` attributes.
- **Ref:** `src/main/java/com/solusi/erp/purchasing/purchasereturn/application/usecase/query/ReturnableGrLineSlice.java`

- **Status:** clean
- **Summary:** Added list, regular source-selection page, form, GR-slice and serial modal fragments, detail page, feature-local JS, SSR hidden snapshots, capture-phase validation, theme-safe action wiring, and four template contract suites.
- **Verification:** `node --check src/main/resources/static/js/purchasing/purchase-return/form.js`
- **Verification:** `mvn test -Dtest=PurchaseReturnListIntegrationTest,PurchaseReturnSelectSourceIntegrationTest,PurchaseReturnFormIntegrationTest,PurchaseReturnViewIntegrationTest`
- **Verification:** `mvn test -Dtest='com.solusi.erp.purchasing.purchasereturn.**.*Test'` passed 94 tests.
- **Boundary check:** No forbidden fixed theme classes, `window.confirm`, or direct `new bootstrap.Modal` usage under Purchase Return templates/scripts.
