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
