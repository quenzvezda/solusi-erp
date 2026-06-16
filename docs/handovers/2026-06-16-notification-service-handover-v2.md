# NotificationService Handover v2

Date: 2026-06-16

## Purpose

This document is the handover context for building the separate NotificationService repository after the ERP producer was refactored from a PO-specific event to a generic approval event.

NotificationService must consume the generic approval topic and send local email notifications. The service must not depend on ERP database access and must not call ERP synchronously to enrich the event during the MVP.

## Current ERP Producer State

ERP is the Kafka producer. NotificationService is only a consumer.

ERP publishes approval business facts through the outbox pattern:

```text
Approval use case transaction
-> outbox_events row
-> scheduled Kafka publisher
-> Kafka topic
-> NotificationService consumer
-> Mailpit/SMTP email
```

The ERP transaction must not depend on NotificationService availability. If NotificationService is down, ERP approval flow should still work. If Kafka publish fails, ERP records retry state in `outbox_events`.

## Topic

NotificationService must consume:

```text
erp.approval.events.v1
```

Do not consume the old POC topic for notifications:

```text
erp.procurement.events.v1
```

The old topic/event `PurchaseOrderApproved v1` is superseded and must not be used for the new notification flow.

## Event Contract

Event type:

```text
ApprovalActionOccurred
```

Event version:

```text
1
```

Source:

```text
erp-monolith
```

Aggregate type:

```text
ApprovalRequest
```

Kafka message key:

```text
approvalRequestId as string
```

Envelope shape:

```json
{
  "eventId": "3f4d8f3a-7f2a-4d37-92f0-111111111111",
  "eventType": "ApprovalActionOccurred",
  "eventVersion": 1,
  "source": "erp-monolith",
  "occurredAt": "2026-06-16T07:30:00Z",
  "correlationId": "55",
  "aggregateType": "ApprovalRequest",
  "aggregateId": "55",
  "payload": {}
}
```

Consumer rules:

- Use `eventId` for idempotency.
- Route by `eventType` + `eventVersion`.
- Ignore unknown optional fields.
- Treat breaking changes as a new `eventVersion`.
- Do not assume every event has a valid email recipient.

## Payload Shape

Example payload for completed PO approval:

```json
{
  "approvalRequestId": 55,
  "referenceType": "PURCHASE_ORDER",
  "referenceId": 42,
  "referenceCode": "PO-202606-00004",
  "documentLabel": "Purchase Order",
  "documentPath": "/purchasing/purchase-orders/view/42",
  "action": "APPROVE_AND_FINISH",
  "status": "COMPLETED",
  "actorPartyId": 3,
  "actorName": "Bpk. Budi Santoso",
  "targetApproverPartyId": null,
  "targetApproverName": null,
  "targetApproverEmail": null,
  "currentApproverPartyId": null,
  "requesterPartyId": 2,
  "requesterName": "Bpk. Administrator Utama",
  "requesterEmail": "admin@solusierp.com",
  "notificationTarget": {
    "role": "REQUESTER",
    "partyId": 2,
    "name": "Bpk. Administrator Utama",
    "email": "admin@solusierp.com"
  },
  "notes": "Approved",
  "actedAt": "2026-06-16T07:30:00Z"
}
```

Important fields for email MVP:

- `payload.action`
- `payload.referenceType`
- `payload.referenceCode`
- `payload.documentLabel`
- `payload.documentPath`
- `payload.actorName`
- `payload.requesterName`
- `payload.notificationTarget.name`
- `payload.notificationTarget.email`

Business-specific fields such as PO total amount and currency are intentionally not included in v1.

## Approval Actions

NotificationService should handle these actions:

| Action | Recipient | Email meaning |
| --- | --- | --- |
| `REQUESTED` | `notificationTarget` approver | A document needs approval/review. |
| `FORWARD` | `notificationTarget` new approver | A document was forwarded to the recipient. |
| `APPROVE_AND_FORWARD` | `notificationTarget` new approver | A document was approved by one approver and forwarded to the recipient. |
| `APPROVE_AND_FINISH` | `notificationTarget` requester | The document was fully approved. |
| `REJECTED` | `notificationTarget` requester | The document was rejected. |

`CANCELLED` is out of scope for v1. ERP currently does not publish `ApprovalActionOccurred` for `CANCELLED`.

## Recipient Rules

ERP already calculates `notificationTarget`. NotificationService must not recalculate recipient business rules from approval history.

Use:

```text
payload.notificationTarget.email
```

If `notificationTarget.email` is `null`, blank, or invalid:

- Do not fail the Kafka message forever.
- Record the notification as skipped, or log a structured warning.
- Commit the consumer offset after the skip decision.
- Include `eventId`, `approvalRequestId`, `action`, `referenceType`, and `referenceCode` in the log.

## Document Link

ERP sends only a relative path:

```text
payload.documentPath
```

NotificationService owns the public ERP base URL:

```properties
ERP_PUBLIC_BASE_URL=http://localhost:8080
```

Build the document URL as:

```text
{ERP_PUBLIC_BASE_URL}{payload.documentPath}
```

If `documentPath` is null or blank, email can still be sent without a document link.

## Recommended Email Template Strategy

For MVP, keep templates as hardcoded HTML files/resources in NotificationService.

Recommended template split:

- `approval-requested.html`
- `approval-forwarded.html`
- `approval-approved.html`
- `approval-rejected.html`

Template selection can be action-based:

| Action | Template |
| --- | --- |
| `REQUESTED` | `approval-requested.html` |
| `FORWARD` | `approval-forwarded.html` |
| `APPROVE_AND_FORWARD` | `approval-forwarded.html` |
| `APPROVE_AND_FINISH` | `approval-approved.html` |
| `REJECTED` | `approval-rejected.html` |

Minimum variables:

- recipient name
- actor name
- document label
- reference code
- action label
- notes
- document URL

## Suggested NotificationService Architecture

Keep the new service simple but clean-architecture friendly.

Suggested packages:

```text
com.solusi.notification
  approval
    application
      port
        EmailSender
        NotificationLogRepository
      service
        ApprovalNotificationService
        ApprovalEmailTemplateRenderer
    domain
      model
        ApprovalActionOccurredEvent
        NotificationDelivery
        NotificationStatus
    infrastructure
      kafka
        ApprovalActionOccurredConsumer
        KafkaConsumerConfig
      mail
        SmtpEmailSender
      persistence
        NotificationLogEntity
        NotificationLogJpaRepository
        NotificationLogRepositoryAdapter
      config
        NotificationProperties
```

MVP can start without a database if desired, but a small notification log table is recommended for idempotency and learning consumer reliability.

## Idempotency

Use `eventId` as the idempotency key.

Recommended behavior:

1. Consumer receives Kafka record.
2. Validate envelope.
3. Check whether `eventId` was already processed.
4. If already processed, commit offset and do nothing.
5. If new, render/send email or skip due to missing recipient.
6. Persist final delivery status.
7. Commit offset.

Suggested statuses:

- `SENT`
- `SKIPPED_NO_RECIPIENT`
- `FAILED_RETRYABLE`
- `FAILED_PERMANENT`

## Consumer Retry and DLQ

DLQ is the responsibility of NotificationService, not ERP.

Suggested local topics:

```text
erp.approval.events.v1
erp.approval.events.v1.dlt
```

For Spring Kafka native, recommended approach:

- Use `@KafkaListener` for `erp.approval.events.v1`.
- Configure `DefaultErrorHandler` with bounded retries.
- Use `DeadLetterPublishingRecoverer` to publish failed records to `.dlt`.
- Treat missing recipient as business skip, not DLQ.
- Treat invalid JSON/unknown contract as permanent failure and route to DLT.
- Treat SMTP temporary failures as retryable.

## Local Infrastructure

ERP repo owns local Kafka compose for now.

Start Kafka and Kafka UI from ERP repo:

```powershell
docker compose --profile messaging up -d
```

Kafka UI:

```text
http://localhost:8085
```

Local broker from host:

```text
localhost:9092
```

Kafka UI connects internally through:

```text
kafka:29092
```

Topic bootstrap script in ERP repo:

```powershell
$env:KAFKA_BOOTSTRAP_SERVERS='localhost:9092'
$env:KAFKA_DOCKER_CONTAINER='kafka-erp'
& 'C:\Program Files\Git\bin\bash.exe' scripts/kafka/create-topics.sh
```

On Windows PowerShell, plain `bash scripts/kafka/create-topics.sh` may resolve to WSL bash and fail if WSL has no `/bin/bash`. Use Git Bash explicitly if needed.

## Suggested NotificationService Local Env

```properties
SERVER_PORT=8082
SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092
APP_KAFKA_APPROVAL_TOPIC=erp.approval.events.v1
APP_KAFKA_APPROVAL_DLT_TOPIC=erp.approval.events.v1.dlt
APP_KAFKA_CONSUMER_GROUP_ID=notification-service-local
ERP_PUBLIC_BASE_URL=http://localhost:8080
SPRING_MAIL_HOST=localhost
SPRING_MAIL_PORT=1025
SPRING_MAIL_USERNAME=
SPRING_MAIL_PASSWORD=
```

Mailpit or Mailhog can be owned by NotificationService repo. Suggested local ports:

```text
SMTP: 1025
Web UI: 8025
```

## ERP Producer Config Relevant to Handover

ERP local dev should have:

```properties
ERP_MESSAGING_ENABLED=true
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
ERP_MESSAGING_KAFKA_DEFAULT_TOPIC=erp.approval.events.v1
```

Cloud/default env intentionally keeps messaging disabled:

```properties
ERP_MESSAGING_ENABLED=false
```

This protects ERP deployment when Kafka/NotificationService is not ready in OCI.

## Manual E2E Acceptance Criteria

After NotificationService MVP exists:

1. Start ERP local dependencies with Kafka.
2. Start NotificationService with Mailpit.
3. Create/submit a PO approval in ERP.
4. Verify Kafka receives `ApprovalActionOccurred` on `erp.approval.events.v1`.
5. Verify NotificationService consumes the message.
6. Verify email appears in Mailpit.
7. Verify `REQUESTED` email goes to the approver.
8. Approve and finish the PO.
9. Verify `APPROVE_AND_FINISH` email goes to the requester.
10. Reject another approval request.
11. Verify `REJECTED` email goes to the requester.
12. Verify no new notification flow depends on `erp.procurement.events.v1`.

## Known ERP References

Useful ERP docs:

- `docs/spec/event-contracts.md`
- `docs/architecture/outbox-kafka-messaging.md`
- `docs/brainstorming/2026-06-16-generic-approval-events.md`
- `docs/reports/2026-06-16-generic-approval-events.md`
- `docs/modules/procurement/purchase-order.md`

Useful ERP code:

- `src/main/java/com/solusi/erp/common/approval/application/service/ApprovalActionOccurredPayload.java`
- `src/main/java/com/solusi/erp/common/approval/application/service/ApprovalActionOccurredEventFactory.java`
- `src/main/java/com/solusi/erp/common/approval/infrastructure/adapter/ApprovalEventPublisherAdapter.java`
- `src/main/java/com/solusi/erp/core/messaging/infrastructure/publisher/ScheduledOutboxKafkaPublisher.java`
- `src/main/java/com/solusi/erp/core/messaging/infrastructure/publisher/OutboxIntegrationEventPublisher.java`

## Build Recommendation for NotificationService

Use Java Spring Boot with Spring Kafka native, not Spring Cloud Stream, because the learning goal is Kafka fundamentals.

Recommended first implementation order:

1. Scaffold Spring Boot app.
2. Add Mailpit compose and SMTP sender.
3. Add event DTOs matching this contract.
4. Add Kafka consumer for `erp.approval.events.v1`.
5. Add template renderer.
6. Add idempotency log.
7. Add retry/DLT config.
8. Run local E2E with ERP.

