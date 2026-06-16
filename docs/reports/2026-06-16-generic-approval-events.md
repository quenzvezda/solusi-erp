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
