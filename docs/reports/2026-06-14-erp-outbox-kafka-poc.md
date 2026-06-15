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
