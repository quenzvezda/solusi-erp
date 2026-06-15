# impl Report: ERP Outbox Kafka POC

> Plan: `docs/plans/2026-06-14-erp-outbox-kafka-poc.md`
> src: `docs/brainstorming/2026-06-14-erp-outbox-kafka-poc.md`

Populated during execution by the execution agent.

## Task 1: Build Dependency, Messaging Toggle, and Outbox Schema
- **Status:** findings
- **Summary:** Added Spring Kafka dependencies, default-disabled ERP messaging config, V76 MariaDB/H2 outbox schema, static migration test, and bumped project version to 1.15.0.

### Finding: Audit column names follow BaseModel
- **Type:** deviation
- **Severity:** info
- **Detail:** The plan text listed `created_by` and `updated_by`, but the current project standard in `BaseModel` maps audit user columns to `created_by_user_id` and `updated_by_user_id`.
- **Action taken:** Used `created_by_user_id` and `updated_by_user_id` in both V76 migrations so the future JPA entity can extend `BaseModel` without column mismatch.
- **Ref:** `src/main/java/com/solusi/erp/core/model/BaseModel.java`

### Finding: Migration parity command needed Git Bash locale override
- **Type:** deviation
- **Severity:** info
- **Detail:** `bash scripts/check-migration-parity.sh` could not run through the default Windows `bash` because WSL bash is unavailable, and Git Bash initially failed `grep -P` with a locale error.
- **Action taken:** Ran the same script through `C:\Program Files\Git\bin\bash.exe` with `LC_ALL=C.UTF-8` and `LANG=C.UTF-8`; parity passed with 75 MariaDB versions and 76 H2 versions.
- **Ref:** `scripts/check-migration-parity.sh`

## Task 2: Core Messaging Domain and Outbox Save Port
- **Status:** findings
- **Summary:** Added pure messaging domain contracts, outbox repository/application ports, outbox lifecycle model, and an ObjectMapper-backed publisher that saves pending outbox rows without Kafka APIs.

### Finding: Envelope timestamp stored as ISO string
- **Type:** decision
- **Severity:** info
- **Detail:** The plan required an `occurredAt` envelope field but did not require a Java type. A first test draft used `JavaTimeModule`, but this Spring Boot 4 project did not have the old `com.fasterxml.jackson.datatype.jsr310` package on the test classpath.
- **Action taken:** Modeled `occurredAt` as an ISO-8601 string in `EventEnvelope`, generated from `OffsetDateTime.now(clock)`, keeping the JSON contract explicit and avoiding extra Jackson module coupling.
- **Ref:** `src/main/java/com/solusi/erp/core/messaging/domain/model/EventEnvelope.java`

### Finding: PowerShell requires quoted Maven test pattern
- **Type:** deviation
- **Severity:** info
- **Detail:** The unquoted command `mvn test -Dtest=OutboxIntegrationEventPublisherTest,OutboxEventTest` is parsed by PowerShell as a parameter list because of the comma.
- **Action taken:** Ran `mvn test "-Dtest=OutboxIntegrationEventPublisherTest,OutboxEventTest"`; focused Task 2 tests passed with 4 tests and 0 failures.
- **Ref:** `src/test/java/com/solusi/erp/core/messaging/infrastructure/publisher/OutboxIntegrationEventPublisherTest.java`

## Task 3: Outbox Persistence Adapter and Messaging Bean Wiring
- **Status:** findings
- **Summary:** Added outbox JPA entity/repository/adapter, messaging properties, composition root, and config tests for disabled no-op vs enabled outbox publisher.

### Finding: Mapper implemented manually
- **Type:** decision
- **Severity:** info
- **Detail:** The plan referenced MapStruct mapper patterns, but `OutboxEvent` is a pure domain model with static factories and lifecycle methods, not a DTO-style bean.
- **Action taken:** Implemented `OutboxEventPersistenceMapper` manually to keep the domain model pure and avoid shaping domain constructors around MapStruct.
- **Ref:** `src/main/java/com/solusi/erp/core/messaging/infrastructure/persistence/OutboxEventPersistenceMapper.java`

### Finding: Existing entity is updated on save
- **Type:** decision
- **Severity:** info
- **Detail:** Saving a detached `BaseModel` entity with an id but null JPA `@Version` can confuse Spring Data's new/existing detection.
- **Action taken:** `OutboxEventRepositoryAdapter.save` reloads an existing entity by id and copies mutable outbox fields before saving, preserving audit/version state for later publish/retry updates.
- **Ref:** `src/main/java/com/solusi/erp/core/messaging/infrastructure/adapter/OutboxEventRepositoryAdapter.java`

## Task 4: Carry Approval Actor Through Internal Approval Event
- **Status:** clean
- **Summary:** Added nullable `actorId` to `ApprovalCompletedEvent`, forwarded approval actor id through `ApprovalEventPublisher`, and preserved the old two-argument constructor for existing listeners/tests.

## Task 5: PurchaseOrderApproved Event Factory and PO Listener Hook
- **Status:** findings
- **Summary:** Added `PurchaseOrderApproved` payload/factory, wired PO approval listener to save/publish integration events atomically, and covered requester/approver fallback behavior.

### Finding: Factory bean needed explicit PO config wiring
- **Type:** deviation
- **Severity:** info
- **Detail:** The task target map did not list `PurchaseOrderConfig`, but the new factory is intentionally pure Java without Spring annotations, consistent with the Clean Architecture standard.
- **Action taken:** Registered `PurchaseOrderApprovedEventFactory` in `PurchaseOrderConfig` and injected `UserRepository`, `PartyLookupProvider`, `CurrencyLookupProvider`, and `Clock` there.
- **Ref:** `src/main/java/com/solusi/erp/purchasing/purchaseorder/infrastructure/config/PurchaseOrderConfig.java`

### Finding: Currency code resolved from lookup payload alias
- **Type:** decision
- **Severity:** info
- **Detail:** `PurchaseOrder` stores `currencyId`, not a direct currency code. Existing `CurrencyLookupProvider` exposes alias in lookup payload.
- **Action taken:** Factory uses `payload.alias` as `currencyCode`, falling back to `subText` parsing or lookup name if alias is unavailable.
- **Ref:** `src/main/java/com/solusi/erp/master/currency/infrastructure/adapter/CurrencyLookupProviderImpl.java`

## Task 6: Scheduled Kafka Outbox Publisher
- **Status:** findings
- **Summary:** Added scheduled Kafka outbox publisher with disabled guard, synchronous send for deterministic marking, retry metadata, max-attempt handling, and focused publisher tests.

### Finding: Max attempts keeps failed row audit-able
- **Type:** decision
- **Severity:** info
- **Detail:** The plan allowed either excluding max-attempt rows or keeping them `FAILED` with a later `nextAttemptAt`.
- **Action taken:** On a failure that reaches `maxAttempts`, the publisher keeps the row `FAILED` and moves `nextAttemptAt` far into the future. Rows are not deleted, preserving POC auditability.
- **Ref:** `src/main/java/com/solusi/erp/core/messaging/infrastructure/publisher/ScheduledOutboxKafkaPublisher.java`

### Finding: Surgical config test needs KafkaTemplate mock
- **Type:** decision
- **Severity:** info
- **Detail:** `MessagingConfigTest` loads only messaging config, not Spring Boot Kafka auto-configuration.
- **Action taken:** Added a mocked `KafkaTemplate<String, String>` to the test context so the scheduled publisher bean can be wired without requiring a broker.
- **Ref:** `src/test/java/com/solusi/erp/core/messaging/infrastructure/config/MessagingConfigTest.java`

## Task 7: Documentation and Contract Updates
- **Status:** clean
- **Summary:** Added outbox Kafka architecture docs, manual event contract docs, PO module integration-event notes, and docs index links.
- **Validation:** Link target check passed; focused test bundle passed with 25 tests; migration parity passed with 75 MariaDB versions and 76 H2 versions.

## Task 8: Final Focused Build Verification
- **Status:** findings
- **Summary:** Ran final ERP verification gates through Task 8.
- **Validation:** Migration parity passed with 75 MariaDB versions and 76 H2 versions. Focused wrapper test passed with 25 tests, 0 failures, 0 errors. Fast subset `./mvnw -B -e test -DexcludedGroups=integration-template` completed with BUILD SUCCESS.

### Finding: Fast subset still emits existing coverage warning
- **Type:** warning
- **Severity:** info
- **Detail:** Fast subset reported JaCoCo branch coverage ratio `0.79` below configured minimum `0.80`, but the project configuration keeps `haltOnFailure=false`, so Maven finished with BUILD SUCCESS.
- **Action taken:** No code change. Recorded the warning for audit because the verification gate exit code was 0.
- **Ref:** `pom.xml`
