# Implementation Report: E2E CI Guardrails

> Plan: docs/plans/e2e-ci-guardrails.md
> Source: Conversation on 2026-05-18 about Playwright E2E follow-up guardrails
> Created: 2026-05-18

## Task 1: Add migration version parity check for MariaDB vs H2

- **Status:** clean
- **Summary:** Created `scripts/check-migration-parity.sh` with V9000 allowlist and added `migration-parity` CI job running on PR + push to main. Script verified locally: 62 MariaDB versions, 63 H2 versions (V9000 allowed), exit 0.

## Task 3: Improve E2E server log capture in Linux/macOS runner and CI

- **Status:** clean
- **Summary:** Updated `run-poc.sh` timeout block to print last 50 lines of both log files. CI now redirects Java stdout/stderr to `target/e2e-server.log` and `target/e2e-server-err.log`, prints tail on failure, and includes both in artifact upload.

## Task 4: Make E2E admin authentication deterministic

- **Status:** clean
- **Summary:** Added `@Order(100)` to `SystemInitializer` so it deterministically runs before `E2eDataSeeder` (`@Order(200)`). Production unchanged since E2eDataSeeder is `@Profile("e2e")` only. Login helper fallback kept for resilience. Compilation verified.

## Task 5: Document E2E selector convention for interactive forms

- **Status:** clean
- **Summary:** Added section 9 "Selector Convention for Interactive Forms" to `docs/tests/playwright-e2e-guide.md` with preference order table, widget ID naming convention, and reference to Product form. Updated status section to reflect 18/18 passing suite. Renumbered sections 10-14.

## Task 6: Add smoke E2E split for push-to-main confidence

- **Status:** clean
- **Summary:** Added `@smoke` tag to login describe block (3 tests), brand create test, and product create test. Added `test:smoke` npm script using `--grep @smoke`. Smoke subset = 5 tests covering auth + form + TomSelect.

## Task 2: Update CI schedule and E2E trigger strategy

- **Status:** clean
- **Summary:** Changed CI schedule from daily to twice weekly (`30 19 * * 0,3` = Mon/Thu 02:30 WIB). Push-to-main E2E now runs smoke subset only (`--grep @smoke`). Full E2E reserved for schedule and manual full dispatch. Job comment documents the strategy.
