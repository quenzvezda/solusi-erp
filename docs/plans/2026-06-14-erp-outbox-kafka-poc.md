# impl Plan: ERP Outbox Kafka POC

> src: `docs/brainstorming/2026-06-14-erp-outbox-kafka-poc.md`
> Created: 2026-06-15
> Status: IN_PROGRESS
>
> **For agentic workers:** execute task-by-task. Explore references fresh before editing each task, update checkbox state incrementally, record deviations in `docs/reports/2026-06-14-erp-outbox-kafka-poc.md`, and do not mark a task complete before its validation command passes.

## 1. Goal

Membangun foundation outbox Kafka di ERP monolith agar ERP dapat publish business event `PurchaseOrderApproved v1` ke topic domain `erp.procurement.events.v1` tanpa bergantung pada NotificationService.

ERP tetap menjadi monolith utama. Kafka hanya jalur integration event keluar. NotificationService atau service lain adalah subscriber bebas; ERP tidak tahu siapa yang consume event dan tidak membawa konsep "send email".

## 2. Locked Decisions

- ERP adalah producer/publisher untuk `PurchaseOrderApproved`.
- NotificationService adalah consumer terpisah; tidak ada dependency sinkron dari ERP ke NotificationService.
- Topic POC: `erp.procurement.events.v1`.
- DLQ bukan tanggung jawab ERP producer POC; DLQ consumer akan ditangani di NotificationService.
- Outbox memakai scheduled polling, bukan CDC/Debezium.
- Kafka integration memakai Spring Kafka native, bukan Spring Cloud Stream.
- Messaging default disabled agar deploy OCI tidak terganggu.
- ERP tetap membuat business event walaupun `requesterEmail` kosong. NotificationService nanti yang mencatat `SKIPPED` jika tidak ada recipient valid.
- `requesterEmail` berasal dari `User.email` milik user pembuat PO (`PurchaseOrder.metadata.createdBy`).
- `requesterName` fallback: `Party.name` dari `User.partyId` -> `UserProfile.fullName` -> `User.username`.
- `approverName` berasal dari `Party.name` milik `actorPartyId`; fallback ke string actor id jika tidak ada lookup.
- `ApprovalCompletedEvent` perlu membawa actor id approval agar PO listener dapat membuat payload approver.
- `outbox_events` tidak dihapus otomatis pada POC agar mudah diaudit.
- Event contract manual per repo; belum memakai shared library, Schema Registry, Avro, atau Protobuf.

## 3. Scope Boundary

### Included

- Dependency Spring Kafka di ERP.
- Feature toggle `erp.messaging.enabled`.
- Flyway MariaDB + H2 migration untuk `outbox_events`.
- `core.messaging` clean architecture package.
- Outbox persistence adapter.
- `IntegrationEventPublisher` yang menyimpan event ke outbox.
- Scheduled publisher yang publish `PENDING` event ke Kafka dan mark `PUBLISHED` / retry `FAILED`.
- `PurchaseOrderApproved v1` payload assembly dari PO, requester user, requester party, dan approver party.
- Hook pada flow approval PO setelah PO berubah `APPROVED`.
- Optional local Kafka KRaft compose profile for manual messaging runs.
- Focused unit/config tests.
- Dokumentasi teknis dan kontrak event.

### Deferred

- NotificationService implementation.
- Kafka DLQ consumer handling.
- Debezium CDC.
- Schema Registry, Avro, Protobuf.
- Shared event contract library.
- Production Kafka cluster deployment.
- Outbox monitoring UI.
- Event selain `PurchaseOrderApproved`.

## 4. Target File Map

### Build and Config

- Modify `pom.xml`
- Modify `src/main/resources/application.yaml`

### Database

- Create `src/main/resources/db/migration/V76__Add_Outbox_Events.sql`
- Create `src/main/resources/db/migration-h2/V76__Add_Outbox_Events.sql`

### Core Messaging

- Create `src/main/java/com/solusi/erp/core/messaging/application/port/IntegrationEventPublisher.java`
- Create `src/main/java/com/solusi/erp/core/messaging/domain/model/EventEnvelope.java`
- Create `src/main/java/com/solusi/erp/core/messaging/domain/model/IntegrationEvent.java`
- Create `src/main/java/com/solusi/erp/core/messaging/domain/model/OutboxEvent.java`
- Create `src/main/java/com/solusi/erp/core/messaging/domain/model/OutboxStatus.java`
- Create `src/main/java/com/solusi/erp/core/messaging/domain/repository/OutboxEventRepository.java`
- Create `src/main/java/com/solusi/erp/core/messaging/infrastructure/persistence/OutboxEventEntity.java`
- Create `src/main/java/com/solusi/erp/core/messaging/infrastructure/persistence/OutboxEventJpaRepository.java`
- Create `src/main/java/com/solusi/erp/core/messaging/infrastructure/persistence/OutboxEventPersistenceMapper.java`
- Create `src/main/java/com/solusi/erp/core/messaging/infrastructure/adapter/OutboxEventRepositoryAdapter.java`
- Create `src/main/java/com/solusi/erp/core/messaging/infrastructure/publisher/OutboxIntegrationEventPublisher.java`
- Create `src/main/java/com/solusi/erp/core/messaging/infrastructure/publisher/ScheduledOutboxKafkaPublisher.java`
- Create `src/main/java/com/solusi/erp/core/messaging/infrastructure/config/MessagingProperties.java`
- Create `src/main/java/com/solusi/erp/core/messaging/infrastructure/config/MessagingConfig.java`

### Approval and Purchase Order

- Modify `src/main/java/com/solusi/erp/core/event/ApprovalCompletedEvent.java`
- Modify `src/main/java/com/solusi/erp/common/approval/application/port/ApprovalEventPublisher.java`
- Modify `src/main/java/com/solusi/erp/common/approval/application/usecase/ProcessApprovalUseCaseImpl.java`
- Modify `src/main/java/com/solusi/erp/common/approval/infrastructure/adapter/ApprovalEventPublisherAdapter.java`
- Modify `src/main/java/com/solusi/erp/purchasing/purchaseorder/infrastructure/listener/OnPurchaseOrderApprovedListener.java`
- Create `src/main/java/com/solusi/erp/purchasing/purchaseorder/domain/model/PurchaseOrderApprovedPayload.java`
- Create `src/main/java/com/solusi/erp/purchasing/purchaseorder/application/service/PurchaseOrderApprovedEventFactory.java`

### Tests

- Create `src/test/java/com/solusi/erp/core/messaging/infrastructure/persistence/OutboxEventsMigrationTest.java`
- Create `src/test/java/com/solusi/erp/core/messaging/infrastructure/publisher/OutboxIntegrationEventPublisherTest.java`
- Create `src/test/java/com/solusi/erp/core/messaging/infrastructure/publisher/ScheduledOutboxKafkaPublisherTest.java`
- Create `src/test/java/com/solusi/erp/core/messaging/infrastructure/config/MessagingConfigTest.java`
- Modify `src/test/java/com/solusi/erp/common/approval/application/usecase/ProcessApprovalUseCaseImplTest.java`
- Create `src/test/java/com/solusi/erp/purchasing/purchaseorder/application/service/PurchaseOrderApprovedEventFactoryTest.java`
- Create or modify `src/test/java/com/solusi/erp/purchasing/purchaseorder/infrastructure/listener/OnPurchaseOrderApprovedListenerTest.java`

### Docs

- Create `docs/architecture/outbox-kafka-messaging.md`
- Create `docs/spec/event-contracts.md`
- Modify `docs/modules/procurement/purchase-order.md`
- Modify `docs/index.md`
- Modify `docs/spec/index.md`

### Local Dev Compose

- Modify `docker-compose.yml`

## 5. Tasks

### Task 1: Build Dependency, Messaging Toggle, and Outbox Schema [x]

Add Spring Kafka dependency, safe default config, and mirrored MariaDB/H2 outbox schema.

**Depends on:** none
**Reference modules:** `core.model.BaseModel`, existing Flyway migrations

- [x] Add `spring-kafka` dependency to `pom.xml` and `spring-kafka-test` as test dependency.
      ref: `pom.xml:L25-L155` - existing dependency grouping and test dependency style
- [x] Add default-disabled messaging config to `application.yaml`.
      Required default:
      ```yaml
      erp:
        messaging:
          enabled: ${ERP_MESSAGING_ENABLED:false}
          outbox:
            batch-size: ${ERP_MESSAGING_OUTBOX_BATCH_SIZE:25}
            fixed-delay-ms: ${ERP_MESSAGING_OUTBOX_FIXED_DELAY_MS:5000}
            retry-delay-seconds: ${ERP_MESSAGING_OUTBOX_RETRY_DELAY_SECONDS:60}
            max-attempts: ${ERP_MESSAGING_OUTBOX_MAX_ATTEMPTS:10}
          kafka:
            default-topic: ${ERP_MESSAGING_KAFKA_DEFAULT_TOPIC:erp.procurement.events.v1}
      spring:
        kafka:
          bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
      ```
      ref: `src/main/resources/application.yaml:L16-L71` - current Spring config layout
- [x] Create MariaDB migration `V76__Add_Outbox_Events.sql` with table `outbox_events`.
      Required columns:
      `id BIGINT AUTO_INCREMENT`, `event_id CHAR(36)`, `event_type`, `event_version`, `aggregate_type`, `aggregate_id`, `topic`, `message_key`, `payload_json LONGTEXT`, `status`, `attempt_count`, `last_error`, `next_attempt_at`, `published_at`, `created_date`, `created_by`, `updated_date`, `updated_by`, `version`.
      ref: `docs/brainstorming/2026-06-14-erp-outbox-kafka-poc.md:L110-L143` - outbox table contract
      ref: `src/main/resources/db/migration/V46__Add_Purchasing_Module.sql` - transaction table naming and column style
- [x] Add MariaDB constraints/indexes: unique `event_id`, index `(status, next_attempt_at)`, index `(topic, status)`, index `(aggregate_type, aggregate_id)`.
      ref: `docs/brainstorming/2026-06-14-erp-outbox-kafka-poc.md:L138-L143` - required index baseline
- [x] Create H2 mirror migration `src/main/resources/db/migration-h2/V76__Add_Outbox_Events.sql` with equivalent business columns and constraints.
      ref: `scripts/check-migration-parity.sh:L1-L83` - CI requires MariaDB/H2 migration version parity
- [x] **TEST:** Add static migration test `OutboxEventsMigrationTest` that reads both V76 files and asserts table name, key columns, unique event id, retry indexes, and status column exist.
      ref: `docs/plans/2026-06-02-phase-a-generic-reversal-foundation.md:L110-L125` - static migration test expectation pattern

**Validation criteria:**

- `mvn test -Dtest=OutboxEventsMigrationTest`
- `bash scripts/check-migration-parity.sh`

### Task 2: Core Messaging Domain and Outbox Save Port [x]

Create pure Java messaging model and outbox publisher that saves integration events without Kafka.

**Depends on:** Task 1
**Reference modules:** `core.domain.model`, `inventory.brand`, `master.tax`

- [x] Create `EventEnvelope<T>` record with fields `eventId`, `eventType`, `eventVersion`, `source`, `occurredAt`, `correlationId`, `aggregateType`, `aggregateId`, `payload`.
      ref: `docs/brainstorming/2026-06-14-erp-outbox-kafka-poc.md:L145-L160` - event envelope standard
- [x] Create `IntegrationEvent` interface or record contract exposing topic, message key, event type/version, aggregate metadata, and payload object.
      ref: `docs/brainstorming/2026-06-14-erp-outbox-kafka-poc.md:L163-L196` - PurchaseOrderApproved is first implementation
- [x] Create `OutboxStatus` enum with `PENDING`, `PUBLISHED`, `FAILED`.
      ref: `docs/brainstorming/2026-06-14-erp-outbox-kafka-poc.md:L120-L136` - required status values
- [x] Create pure `OutboxEvent` domain model with factory `pending(...)`, `markPublished(...)`, and `markFailed(...)`.
      Required behavior:
      - new event starts `PENDING`;
      - attempt count defaults to 0;
      - `markPublished` sets status and `publishedAt`;
      - `markFailed` increments attempt count, stores truncated error message, and sets `nextAttemptAt`.
      ref: `docs/architecture/clean-ddd-cqrs-standard.md` - domain layer must be pure Java
- [x] Create `OutboxEventRepository` port with `save`, `findPublishableBatch`, and `findByEventId`.
      ref: `src/main/java/com/solusi/erp/purchasing/purchaseorder/domain/repository/PurchaseOrderRepository.java` - domain repository port pattern
- [x] Create `IntegrationEventPublisher` app port with `publish(IntegrationEvent event)`.
      ref: `src/main/java/com/solusi/erp/purchasing/purchaseorder/domain/port/PurchaseOrderEventPublisher.java:L1-L5` - simple publisher port pattern
- [x] Create `OutboxIntegrationEventPublisher` that builds an envelope, serializes payload via injected JSON serializer abstraction or `ObjectMapper`, and saves a pending outbox event.
      Keep this class free of Kafka APIs.
      ref: `docs/brainstorming/2026-06-14-erp-outbox-kafka-poc.md:L67-L108` - use case sees port; implementation saves outbox
- [x] **TEST:** Add `OutboxIntegrationEventPublisherTest` verifying a sample event saves one `PENDING` outbox row with topic, message key, aggregate metadata, event version, and JSON envelope containing payload.
- [x] **TEST:** Add domain tests for `OutboxEvent.markPublished` and `markFailed` retry metadata.

**Validation criteria:**

- `mvn test -Dtest=OutboxIntegrationEventPublisherTest,*OutboxEvent*Test`
- No class under `core.messaging.domain` or `core.messaging.application` imports Spring Kafka.

### Task 3: Outbox Persistence Adapter and Messaging Bean Wiring [x]

Persist outbox domain rows and wire messaging beans behind `erp.messaging.enabled`.

**Depends on:** Task 2
**Reference modules:** `inventory.brand`, `security.user`

- [x] Create `OutboxEventEntity` extending `BaseModel` and mapping to `outbox_events`.
      Use `@Column` mappings for all outbox fields and keep `payloadJson` as `LONGTEXT`/`TEXT` mapping.
      ref: `src/main/java/com/solusi/erp/core/model/BaseModel.java` - audit/version superclass pattern
      ref: `src/main/java/com/solusi/erp/security/user/infrastructure/persistence/User.java` - entity mapping style with BaseModel
- [x] Create `OutboxEventJpaRepository` with query method for publishable events:
      `status in (PENDING, FAILED)` and `nextAttemptAt <= now`, ordered by `createdDate`, limited by pageable.
      ref: `src/main/java/com/solusi/erp/common/approval/infrastructure/persistence/ApprovalRequestJpaRepository.java` - Spring Data query style
- [x] Create `OutboxEventPersistenceMapper` for entity/domain conversion.
      ref: `src/main/java/com/solusi/erp/security/user/infrastructure/persistence/UserPersistenceMapper.java:L12-L67` - MapStruct + AuditMetadata mapping pattern
- [x] Create `OutboxEventRepositoryAdapter` implementing `OutboxEventRepository`.
      ref: `src/main/java/com/solusi/erp/security/user/infrastructure/adapter/UserRepositoryAdapter.java:L16-L95` - repository adapter wrapping JPA + mapper
- [x] Create `MessagingProperties` with nested `Outbox` and `Kafka` properties.
      ref: `src/main/resources/application.yaml:L1-L96` - property naming conventions
- [x] Create `MessagingConfig` composition root to register:
      - `OutboxEventRepository`;
      - `IntegrationEventPublisher` as `OutboxIntegrationEventPublisher` when enabled;
      - no-op `IntegrationEventPublisher` when disabled.
      ref: `src/main/java/com/solusi/erp/purchasing/purchaseorder/infrastructure/config/PurchaseOrderConfig.java:L24-L87` - explicit bean registration and transaction wrapper style
- [x] Ensure outbox save participates in the caller transaction. Do not start a separate transaction inside `OutboxIntegrationEventPublisher`.
      ref: `docs/architecture/clean-ddd-cqrs-standard.md` - transaction boundaries belong in composition root / use case wrappers
- [x] **TEST:** Add `MessagingConfigTest` with mocked repository/object mapper dependencies to prove disabled config provides a no-op publisher and enabled config provides outbox publisher.
      ref: `.claude/skills/plan-from-brainstorm/SKILL.md` - config integration tests are required for infrastructure config

**Validation criteria:**

- `mvn test -Dtest=MessagingConfigTest`
- `mvn test -Dtest=OutboxIntegrationEventPublisherTest`

### Task 4: Carry Approval Actor Through Internal Approval Event [x]

Extend internal approval completed event so PO approval listener knows who approved.

**Depends on:** Task 3
**Reference modules:** `common.approval`

- [x] Change `ApprovalCompletedEvent` to include nullable `actorId`.
      Keep backward-compatible constructor `(String referenceType, Long referenceId)` delegating to new constructor with `actorId=null`, because other tests/listeners instantiate it directly.
      ref: `src/main/java/com/solusi/erp/core/event/ApprovalCompletedEvent.java:L1-L17` - current event has only reference type/id
      ref: `src/test/java/com/solusi/erp/purchasing/purchasereturn/infrastructure/listener/OnPurchaseReturnApprovedListenerTest.java:L22-L23` - existing direct constructor usage to preserve
- [x] Update `ApprovalEventPublisher.publishCompleted` signature to accept `actorId`, and update adapter to publish the enriched event.
      ref: `src/main/java/com/solusi/erp/common/approval/infrastructure/adapter/ApprovalEventPublisherAdapter.java:L17-L24` - current Spring ApplicationEvent adapter
- [x] Update `ProcessApprovalUseCaseImpl.approve` to call `eventPublisher.publishCompleted(saved.getReferenceType(), saved.getReferenceId(), actorId)`.
      ref: `src/main/java/com/solusi/erp/common/approval/application/usecase/ProcessApprovalUseCaseImpl.java:L19-L30` - current approval event publish point
- [x] Keep reject event unchanged for this POC.
      ref: `docs/brainstorming/2026-06-14-erp-outbox-kafka-poc.md:L251-L260` - events other than PO approved are deferred
- [x] **TEST:** Update `ProcessApprovalUseCaseImplTest.shouldApproveAndPublishEvent` to verify actor id is forwarded.
      ref: `src/test/java/com/solusi/erp/common/approval/application/usecase/ProcessApprovalUseCaseImplTest.java:L39-L56` - current expected publish call
- [x] **TEST:** Ensure tests that instantiate `new ApprovalCompletedEvent("PURCHASE_RETURN", 1L)` still compile and pass.
      ref: `src/test/java/com/solusi/erp/purchasing/purchasereturn/infrastructure/listener/OnPurchaseReturnApprovedListenerTest.java:L16-L27` - backward compatibility coverage

**Validation criteria:**

- `mvn test -Dtest=ProcessApprovalUseCaseImplTest,OnPurchaseReturnApprovedListenerTest`
- Existing purchase return approval listener behavior remains unchanged.

### Task 5: PurchaseOrderApproved Event Factory and PO Listener Hook [x]

Build `PurchaseOrderApproved v1` business event from PO approval with user/party fallback rules, then save it to outbox.

**Depends on:** Task 4
**Reference modules:** `purchasing.purchaseorder`, `security.user`, `master.party`

- [x] Create `PurchaseOrderApprovedPayload` record under PO domain model with fields:
      `poId`, `poNumber`, `requesterUserId`, `requesterPartyId`, `requesterName`, `requesterEmail`, `approverPartyId`, `approverName`, `approvedAt`, `totalAmount`, `currencyCode`.
      ref: `docs/brainstorming/2026-06-14-erp-outbox-kafka-poc.md:L177-L196` - baseline payload with recipient snapshot
- [x] Create `PurchaseOrderApprovedEventFactory` in PO application service layer.
      Dependencies:
      - `UserRepository`;
      - `PartyLookupProvider`;
      - optional currency lookup if `currencyCode` is not available from PO directly.
      ref: `src/main/java/com/solusi/erp/security/user/domain/repository/UserRepository.java:L1-L24` - user lookup port
      ref: `src/main/java/com/solusi/erp/master/party/domain/port/PartyLookupProvider.java:L1-L12` - party lookup provider port
- [x] Implement requester fallback:
      - load user by `po.getMetadata().createdBy()`;
      - `requesterEmail = user.email`;
      - if `user.partyId` resolves to party lookup, `requesterName = lookup.name`;
      - else if `user.profile.fullName` is nonblank, use it;
      - else use `user.username`;
      - if no user found, keep `requesterEmail=null` and use fallback `"User " + createdBy`.
      ref: `src/main/java/com/solusi/erp/security/user/domain/model/User.java:L119-L177` - user id, email, party id, profile getters
      ref: `src/main/java/com/solusi/erp/security/user/domain/model/UserProfile.java` - profile full name getter
- [x] Implement approver fallback:
      - resolve `event.actorId` through `PartyLookupProvider`;
      - if lookup exists, use `lookup.name`;
      - else if actor id exists, use `"Party " + actorId`;
      - else use `"Approver"`.
      ref: `src/main/java/com/solusi/erp/core/dto/LookupDto.java:L1-L21` - lookup DTO name field
- [x] Build `IntegrationEvent` with:
      - `eventType=PurchaseOrderApproved`;
      - `eventVersion=1`;
      - `source=erp-monolith`;
      - `aggregateType=PurchaseOrder`;
      - `aggregateId=po.id`;
      - `topic=erp.procurement.events.v1`;
      - `messageKey=po.id`.
      ref: `docs/brainstorming/2026-06-14-erp-outbox-kafka-poc.md:L163-L175` - topic/key contract
- [x] Modify `OnPurchaseOrderApprovedListener` to inject `PurchaseOrderApprovedEventFactory` and `IntegrationEventPublisher`.
      After `po.approve()` and `purchaseOrderRepository.save(po)`, build and publish the integration event.
      ref: `src/main/java/com/solusi/erp/purchasing/purchaseorder/infrastructure/listener/OnPurchaseOrderApprovedListener.java:L18-L27` - current PO approval status update hook
- [x] Add `@Transactional` to `OnPurchaseOrderApprovedListener.handle` to make PO status save and outbox save atomic.
      ref: `src/main/java/com/solusi/erp/purchasing/purchasereturn/infrastructure/listener/OnPurchaseReturnApprovedListener.java:L17-L24` - purchase return listener already uses transactional event listener method
- [x] Do not skip event if `requesterEmail` is blank or null.
      ref: `docs/brainstorming/2026-06-14-erp-outbox-kafka-poc.md:L22-L23` - ERP publishes business fact, not email command
- [x] **TEST:** Add `PurchaseOrderApprovedEventFactoryTest` covering:
      - requester party name wins over user profile;
      - profile full name fallback when party missing;
      - username fallback when profile missing;
      - null email still produces event;
      - approver party name fallback.
- [x] **TEST:** Add/modify `OnPurchaseOrderApprovedListenerTest` to verify listener approves PO, saves PO, and calls `IntegrationEventPublisher.publish(...)` exactly once.
      Use mocked `PurchaseOrderApprovedEventFactory` to keep listener test focused.
      ref: `src/test/java/com/solusi/erp/purchasing/purchasereturn/infrastructure/listener/OnPurchaseReturnApprovedListenerTest.java:L16-L27` - listener test style

**Validation criteria:**

- `mvn test -Dtest=PurchaseOrderApprovedEventFactoryTest,OnPurchaseOrderApprovedListenerTest`
- Event is produced even when requester email is null.
- PO listener remains independent from Kafka APIs.

### Task 6: Scheduled Kafka Outbox Publisher [x]

Publish pending outbox rows to Kafka with retry metadata and feature toggle.

**Depends on:** Task 5
**Reference modules:** `core.messaging`

- [x] Create `ScheduledOutboxKafkaPublisher` under `core.messaging.infrastructure.publisher`.
      It depends on `OutboxEventRepository`, `KafkaTemplate<String, String>`, `MessagingProperties`, and a clock/time provider if useful for tests.
      ref: `docs/brainstorming/2026-06-14-erp-outbox-kafka-poc.md:L241-L249` - conservative producer retry flow
- [x] Add `@Scheduled(fixedDelayString = "${erp.messaging.outbox.fixed-delay-ms:5000}")` and guard method body with `if (!properties.enabled()) return`.
      ref: `src/main/resources/application.yaml:L1-L96` - config property style
- [x] Fetch batch of publishable events using repository method from Task 2/3.
      Include `PENDING` and retryable `FAILED` where `nextAttemptAt <= now`.
      ref: `docs/brainstorming/2026-06-14-erp-outbox-kafka-poc.md:L120-L136` - outbox retry fields
- [x] Publish `payloadJson` to `event.topic` with `event.messageKey` using `KafkaTemplate.send(topic, key, payloadJson)`.
      Use synchronous result wait for POC so marking `PUBLISHED` is deterministic.
      ref: `docs/brainstorming/2026-06-14-erp-outbox-kafka-poc.md:L43-L45` - native Spring Kafka chosen for explicit producer behavior
- [x] On success, mark event `PUBLISHED` and save.
- [x] On exception, mark event `FAILED`, increment attempt count, store error, set `nextAttemptAt`, and save.
- [x] If `attemptCount >= maxAttempts`, keep `FAILED` with a later `nextAttemptAt` or stop retrying by excluding it from publishable query. Document the chosen behavior in `docs/architecture/outbox-kafka-messaging.md`.
- [x] Register publisher bean only when Spring Kafka is present and messaging enabled, or keep bean always present with disabled guard and no required Kafka connection until scheduled execution.
      The safer prod default is disabled guard plus optional Kafka bootstrap configuration.
- [x] **TEST:** Add `ScheduledOutboxKafkaPublisherTest` for:
      - disabled properties do not call repository or Kafka;
      - successful send marks published;
      - failed send marks failed and schedules retry;
      - batch size from properties is honored.

**Validation criteria:**

- `mvn test -Dtest=ScheduledOutboxKafkaPublisherTest`
- App can start with `erp.messaging.enabled=false` without a running Kafka broker.

### Task 7: Documentation and Contract Updates [x]

Document business meaning, technical architecture, and event contract for future fresh-chat agents.

**Depends on:** Task 6
**Reference modules:** docs index, brainstorming doc

- [x] Create `docs/architecture/outbox-kafka-messaging.md`.
      Include:
      - outbox pattern purpose;
      - ERP producer responsibility;
      - NotificationService independence;
      - enabled/disabled behavior;
      - retry producer behavior;
      - why DLQ belongs to consumer side for this POC.
      ref: `docs/brainstorming/2026-06-14-erp-outbox-kafka-poc.md:L24-L49` - locked architecture decisions
- [x] Create `docs/spec/event-contracts.md`.
      Include event envelope standard and `PurchaseOrderApproved v1` JSON example with nullable `requesterEmail`.
      ref: `docs/brainstorming/2026-06-14-erp-outbox-kafka-poc.md:L145-L196` - event contract baseline
- [x] Update `docs/modules/procurement/purchase-order.md` with PO approved integration event behavior.
      State that approval creates a business event regardless of email availability.
      ref: `docs/index.md:L25-L31` - procurement module docs index
- [x] Update `docs/index.md` to link the new architecture doc and event contract spec.
      ref: `docs/index.md:L9-L45` - documentation folder index structure
- [x] Update `docs/spec/index.md` to link `event-contracts.md`.
      ref: `docs/spec/index.md:L20-L32` - data/logic standards section
- [x] **TEST:** No automated test for docs, but run grep/static check that links point to existing files.

**Validation criteria:**

- `mvn test -Dtest=OutboxEventsMigrationTest,OutboxIntegrationEventPublisherTest,ScheduledOutboxKafkaPublisherTest,MessagingConfigTest,PurchaseOrderApprovedEventFactoryTest,OnPurchaseOrderApprovedListenerTest,ProcessApprovalUseCaseImplTest`
- `bash scripts/check-migration-parity.sh`
- Manual doc link check: every new link in `docs/index.md` and `docs/spec/index.md` points to an existing file.

### Task 8: Final Focused Build Verification [x]

Run the focused backend verification needed before handing off to NotificationService work.

**Depends on:** Task 7
**Reference modules:** CI workflow

- [x] Run migration parity check.
      ref: `scripts/check-migration-parity.sh:L1-L83` - parity command used by CI
- [x] Run focused messaging/approval/PO tests.
      Command:
      ```bash
      ./mvnw -B -e test -Dtest=OutboxEventsMigrationTest,OutboxIntegrationEventPublisherTest,ScheduledOutboxKafkaPublisherTest,MessagingConfigTest,PurchaseOrderApprovedEventFactoryTest,OnPurchaseOrderApprovedListenerTest,ProcessApprovalUseCaseImplTest
      ```
- [x] Run fast test subset if focused tests pass.
      Command:
      ```bash
      ./mvnw -B -e test -DexcludedGroups=integration-template
      ```
      ref: `.github/workflows/ci-java21.yml` - fast tests are CI gate for PR/push
- [x] Record actual command output summary and any deviations in `docs/reports/2026-06-14-erp-outbox-kafka-poc.md`.

**Validation criteria:**

- Migration parity passes.
- Focused messaging tests pass.
- Fast test subset passes, or any failure is documented with exact failing test and reason.

### Task 9: Optional Local Kafka KRaft Compose Profile

Add local Kafka infrastructure to this ERP repo for manual messaging tests, but do not make it a blocker for ERP producer implementation.

**Depends on:** Task 6
**Reference modules:** root `docker-compose.yml`, NotificationService POC docs

- [ ] Inspect current `docker-compose.yml` and preserve existing database/dev services.
      ref: `docker-compose.yml` - current local infrastructure file
- [ ] Add Kafka KRaft single-node service under Docker Compose profile `messaging`.
      Requirements:
      - service is not started by plain `docker compose up`;
      - service exposes `localhost:9092`;
      - service works for local Spring Kafka producer config `KAFKA_BOOTSTRAP_SERVERS=localhost:9092`.
      ref: `docs/brainstorming/2026-06-14-erp-outbox-kafka-poc.md:L226-L239` - KRaft single-node decision
- [ ] Optionally add Kafka UI under the same `messaging` profile if it keeps manual inspection simpler.
      Keep it optional and local-only; ERP must not depend on Kafka UI.
- [ ] Add comments or docs in `docs/architecture/outbox-kafka-messaging.md` showing the local command:
      ```bash
      docker compose --profile messaging up -d
      ```
- [ ] Document that Mailpit belongs to the NotificationService/local full-stack phase, not ERP producer verification.
      ref: `F:\solusi-program-notification-service\2026-06-14-notification-service-kafka-poc.md` - NotificationService local stack includes Mailpit
- [ ] Do not require this task for Task 8 focused verification. Full E2E proof waits until NotificationService can consume `PurchaseOrderApproved`.

**Validation criteria:**

- `docker compose --profile messaging config` succeeds.
- Full manual proof is deferred until NotificationService exists:
  `ERP approve PO -> Kafka topic -> NotificationService consumes -> Mailpit email`.

## 6. Coverage Check

- Outbox schema covered by Task 1.
- Event envelope and manual contract covered by Task 2 and Task 7.
- Clean Architecture boundary covered by Tasks 2, 3, and 5.
- Feature toggle covered by Tasks 1, 3, and 6.
- PurchaseOrderApproved event covered by Task 5.
- Scheduled Kafka polling covered by Task 6.
- Retry producer behavior covered by Task 6.
- Documentation for fresh-chat agents covered by Task 7.
- Verification covered by Task 8.
- Optional local Kafka compose covered by non-blocking Task 9.
- Deferred items from brainstorming are not included as implementation tasks.
