# Implementation Report: Generic Approval Events

> Plan: docs/plans/2026-06-16-generic-approval-events.md
> Source: docs/brainstorming/2026-06-16-generic-approval-events.md
> Created: 2026-06-16

## Findings

(Populated during execution by execute-plan skill)

## Task 1: Add Document Path to Approval Request

- **Status:** findings
- **Summary:** Added nullable `document_path` migrations, domain field/getter, JPA entity mapping, persistence mapper mapping, and domain test coverage.

### Finding: Migration parity script needs UTF-8 locale under Git Bash
- **Type:** environment
- **Severity:** info
- **Detail:** Running `bash scripts/check-migration-parity.sh` from PowerShell invoked WSL without `/bin/bash`; running Git Bash without UTF-8 locale failed at `grep -P`.
- **Action taken:** Verified with Git Bash using `LC_ALL=C.UTF-8`; script exited 0. Also ran `OutboxEventsMigrationTest`.
- **Ref:** scripts/check-migration-parity.sh

### Finding: Backward-compatible constructor retained
- **Type:** decision
- **Severity:** info
- **Detail:** A test constructed `ApprovalRequest` directly with the old constructor signature.
- **Action taken:** Added an overload that delegates to the new constructor with `documentPath = null` so existing tests and call sites remain stable.
- **Ref:** src/main/java/com/solusi/erp/common/approval/domain/model/ApprovalRequest.java

## Task 2: Extend Approval Request Creation from Business Modules

- **Status:** findings
- **Summary:** Carried `documentPath` through `ApprovalRequestedEvent`, approval request creation, Spring listener/config wiring, and Purchase Order submit publication.

### Finding: Backward-compatible event constructor retained
- **Type:** decision
- **Severity:** info
- **Detail:** `ApprovalRequestedEvent` may still be constructed by older tests or module code while the generic event migration is in progress.
- **Action taken:** Kept the old constructor as an overload that delegates to the new constructor with `documentPath = null`.
- **Ref:** src/main/java/com/solusi/erp/core/event/ApprovalRequestedEvent.java

### Finding: Jacoco check warns during focused test run
- **Type:** gap
- **Severity:** info
- **Detail:** Focused Task 2 test run passed, but Jacoco reported branch coverage `0.79` against the `0.80` target while Maven still exited successfully.
- **Action taken:** Left coverage hardening for Task 8, where the approved plan explicitly adds focused tests to push coverage above the threshold.
- **Ref:** docs/plans/2026-06-16-generic-approval-events.md

## Task 3: Add Party/User Notification Target Resolution

- **Status:** clean
- **Summary:** Added `findByPartyId` user lookup support and approval-side notification target resolver with party-name fallback and nullable email behavior.

## Task 4: Create Generic Approval Event Contract and Factory

- **Status:** findings
- **Summary:** Added generic `ApprovalActionOccurred` payload/factory with approval-domain topic metadata, document labels, requester/history lookup, recipient rules, and deterministic clock-based `actedAt`.

### Finding: Final approvals should not expose stale current approver
- **Type:** decision
- **Severity:** info
- **Detail:** `ApprovalRequest.currentApproverId` remains set after `COMPLETED` or `REJECTED`, but the generic notification payload should not imply that a final document still has an active approver.
- **Action taken:** `ApprovalActionOccurredEventFactory` emits `currentApproverPartyId = null` unless request status is `PENDING`.
- **Ref:** src/main/java/com/solusi/erp/common/approval/application/service/ApprovalActionOccurredEventFactory.java

## Task 5: Wire Approval Use Cases to Publish Generic Events

- **Status:** findings
- **Summary:** Reworked approval publisher contract to action-aware methods, published generic events for requested/completed/rejected/forward/approve-and-forward, preserved Spring events for completed/rejected module reactions, and added adapter/config wiring tests.

### Finding: Messaging-disabled environments remain safe
- **Type:** decision
- **Severity:** info
- **Detail:** `ApprovalEventPublisherAdapter` now always calls `IntegrationEventPublisher`, but cloud/local environments may intentionally leave messaging disabled.
- **Action taken:** Confirmed `MessagingConfig.integrationEventPublisher(...)` provides a no-op publisher when `messaging.enabled=false`, so ERP behavior remains unaffected without Kafka.
- **Ref:** src/main/java/com/solusi/erp/core/messaging/infrastructure/config/MessagingConfig.java

## Task 6: Remove PO-Specific Kafka Publication

- **Status:** findings
- **Summary:** Removed PO-specific integration event factory/payload/publication from the PO approval listener while preserving PO status update via Spring `ApprovalCompletedEvent`.

### Finding: Search still finds non-obsolete references
- **Type:** deviation
- **Severity:** info
- **Detail:** `rg "PurchaseOrderApproved" src/main src/test` still finds the internal `OnPurchaseOrderApprovedListener` class/test names plus core messaging sample event names.
- **Action taken:** Removed the active PO-specific Kafka implementation. Kept the PO listener name because it still describes the internal PO-approved reaction; left core messaging sample renames to Task 7, which explicitly covers topic/event sample updates.
- **Ref:** src/main/java/com/solusi/erp/purchasing/purchaseorder/infrastructure/listener/OnPurchaseOrderApprovedListener.java

## Task 7: Update Topic Bootstrap, Runtime Config, and Documentation

- **Status:** clean
- **Summary:** Switched runtime/topic bootstrap defaults and active event docs/tests from `PurchaseOrderApproved` on `erp.procurement.events.v1` to `ApprovalActionOccurred` on `erp.approval.events.v1`.

### Finding: Local topic bootstrap verified against running Kafka
- **Type:** verification
- **Severity:** info
- **Detail:** The Kafka bootstrap script was run twice with local `kafka-erp`; the first run created `erp.approval.events.v1`, and the second completed without recreating it.
- **Action taken:** Confirmed script idempotency and `docker compose --profile messaging config` still renders `KAFKA_CFG_AUTO_CREATE_TOPICS_ENABLE: "false"`.
- **Ref:** scripts/kafka/create-topics.sh

## Task 8: Jacoco Coverage Hardening

- **Status:** clean
- **Summary:** Added branch-focused approval event/use case tests and verified full `mvn test` passes with Jacoco checks met.

### Finding: Coverage threshold already passes after focused approval tests
- **Type:** verification
- **Severity:** info
- **Detail:** Full suite result after Task 8 changes: 2058 tests, 0 failures/errors/skips, and Jacoco reported `All coverage checks have been met`.
- **Action taken:** Kept `pom.xml` thresholds unchanged and did not add unrelated coverage-only tests.
- **Ref:** pom.xml

## Task 9: Local Kafka Verification

- **Status:** findings
- **Summary:** Verified local Kafka readiness for the new generic approval topic and documented deferred manual E2E approval verification.

### Finding: Manual ERP approval flow deferred
- **Type:** deviation
- **Severity:** info
- **Detail:** The topic `erp.approval.events.v1` exists in local Kafka and `.env.dev` enables messaging with local bootstrap servers. The ERP server was not started and no PO approval was processed in this session to avoid mutating local data/state; this matches the prior note that Task 9 is not a blocker until the consumer/full local flow is ready.
- **Action taken:** Verified topic list with `docker exec kafka-erp kafka-topics.sh --list`, verified bootstrap script idempotency in Task 7, and left manual PO approval/Kafka payload/DB `PUBLISHED` checks as follow-up E2E.
- **Ref:** docs/plans/2026-06-16-generic-approval-events.md
