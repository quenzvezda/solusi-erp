# Implementation Plan: E2E CI Guardrails

> Source: Conversation on 2026-05-18 about Playwright E2E follow-up guardrails
> Created: 2026-05-18
> Sprint: E2E Prototype Follow-up
> Status: IN_PROGRESS

## Summary

This plan hardens the successful Playwright E2E prototype with small guardrails before expanding to more complex ERP flows. It adds a cheap PR/main migration parity check, captures backend logs for E2E debugging, documents selector conventions for interactive forms, and makes E2E authentication deterministic by fixing the ordering/profile behavior between `SystemInitializer` and `E2eDataSeeder`.

## Current Findings from Exploration

- CI currently runs `fast-tests` on PR and manual non-full dispatch, while `full-tests` and `e2e-tests` run on schedule, manual full, and push to `main`/`master`.
- Current schedule is daily at `0 2 * * *` (09:00 WIB), which may become expensive as the E2E suite grows; the chosen target is twice weekly at 02:30 WIB, represented in GitHub Actions cron as Sunday and Wednesday 19:30 UTC (`30 19 * * 0,3`).
- Scheduled CI should preserve the current dependency order: `full-tests` runs first, then full `e2e-tests` runs after `full-tests` succeeds.
- Push-to-main should be prepared to run a smoke E2E subset instead of the full suite once the smoke tags/script are added in this plan.
- Migration parity should be implemented as a separate CI job so failures are isolated and easy to understand.
- CI E2E starts Spring Boot with `java -jar ... &` but does not redirect stdout/stderr to `target/e2e-server.log` / `target/e2e-server-err.log`, even though the wait step tries to print `target/e2e-server.log` on startup failure.
- Linux/macOS runner `e2e-tests/scripts/run-poc.sh` also starts Java without log redirection.
- Windows runner `e2e-tests/scripts/run-poc.ps1` already redirects stdout/stderr and uses `-WindowStyle Hidden`.
- `src/main/resources/db/migration` has 62 migrations; `src/main/resources/db/migration-h2` has the same production versions plus H2-only `V9000__e2e_seed_data.sql`.
- `SystemInitializer` is a global `CommandLineRunner` with no `@Profile` or `@Order`; it can run after `E2eDataSeeder` and set `passwordChangeRequired(true)` when synchronizing the admin password.
- `E2eDataSeeder` is `@Profile("e2e")` and `@Order(200)`, but that order is earlier than unordered runners that default to lowest precedence.
- `docs/tests/playwright-e2e-guide.md` exists and already covers current E2E architecture, helpers, troubleshooting, and basic selector guidance, but it needs a dedicated selector convention section and updated status after the latest fixes.

## Tasks

### Task 1: Add migration version parity check for MariaDB vs H2

Add a cheap CI guardrail that fails PRs and push-to-main when a MariaDB Flyway migration version is missing from the H2 mirror.

**Depends on:** none
**Reference module:** CI workflow + Flyway migration folders

Steps:
- [ ] Create a script that compares migration versions in `src/main/resources/db/migration` and `src/main/resources/db/migration-h2`.
      ref: src/main/resources/db/migration/ — MariaDB Flyway migrations to mirror by version
      ref: src/main/resources/db/migration-h2/ — H2-compatible mirror plus H2-only `V9000__e2e_seed_data.sql`
- [ ] Allow H2-only `V9000__e2e_seed_data.sql` explicitly so the parity check does not fail on E2E seed data.
      ref: src/main/resources/db/migration-h2/V9000__e2e_seed_data.sql — H2-only seed migration that should be allowlisted
- [ ] Add a dedicated `migration-parity` CI job that runs on PR and push to `main`/`master`, separate from `fast-tests` for clearer failure reporting.
      ref: .github/workflows/ci-java21.yml:L39-L78 — current PR fast test job structure
      ref: .github/workflows/ci-java21.yml:L84-L115 — current push-main full test job structure
- [ ] Keep the check version-based only; do not diff SQL contents or attempt semantic SQL validation in this task.
- [ ] Make failure output list missing H2 versions and unexpected H2-only versions, with the allowlist shown in the message.

**Validation criteria:**
- Running the script locally exits 0 with the current migration folders.
- Temporarily simulating a missing H2 version causes non-zero exit and a clear error message.
- CI workflow syntax remains valid.

### Task 2: Update CI schedule and E2E trigger strategy

Adjust CI so full E2E is not run every night by default, while preserving cheap PR guardrails and using a smoke E2E subset for push-to-main confidence.

**Depends on:** Task 1
**Reference module:** GitHub Actions workflow

Steps:
- [ ] Change the current daily schedule to twice weekly at 02:30 WIB: Sunday and Wednesday 19:30 UTC in GitHub Actions cron (`30 19 * * 0,3`), yielding Monday and Thursday 02:30 WIB.
      ref: .github/workflows/ci-java21.yml:L16-L18 — current daily `0 2 * * *` schedule
- [ ] Keep migration parity on PR and push to `main`/`master` because it is cheap and deterministic.
      ref: .github/workflows/ci-java21.yml:L3-L15 — workflow events for push and pull_request
- [ ] Keep full Maven tests on push to `main`/`master`, manual full dispatch, and twice-weekly schedule.
      ref: .github/workflows/ci-java21.yml:L84-L115 — current `full-tests` job
- [ ] Configure push-to-main E2E to use the smoke subset created in Task 6; keep full E2E for manual full dispatch and twice-weekly schedule.
      ref: e2e-tests/playwright.config.ts:L3-L29 — current Playwright config has no smoke project/tag split
      ref: e2e-tests/package.json:L6-L11 — current npm scripts only expose full Playwright commands
- [ ] Preserve scheduled CI order: `full-tests` must complete successfully before scheduled full `e2e-tests` starts.
      ref: .github/workflows/ci-java21.yml:L235-L246 — current E2E job depends on `full-tests`
- [ ] If a smoke subset is chosen, add explicit `test:smoke` script and tag/folder convention in Playwright tests.
      ref: e2e-tests/tests/ — current specs are not tagged with `@smoke`

**Validation criteria:**
- CI event matrix is documented in comments or guide: PR = cheap checks; push main = full Maven plus smoke E2E; manual full = full Maven plus full E2E; twice-weekly schedule = full audit.
- Workflow still runs the desired jobs for PR, push main, schedule, and workflow_dispatch conditions.

### Task 3: Capture E2E server logs in Linux/macOS runner and CI

Make backend logs available when local or CI E2E startup/browser tests fail.

**Depends on:** none
**Reference module:** Windows runner and existing E2E CI job

Steps:
- [ ] Redirect Linux/macOS runner Java stdout to `target/e2e-server.log` and stderr to `target/e2e-server-err.log`.
      ref: e2e-tests/scripts/run-poc.sh:L10-L14 — current Java start has no log redirection
      ref: e2e-tests/scripts/run-poc.ps1:L12-L15 — Windows runner already redirects logs and hides Java window
- [ ] On Linux/macOS runner startup timeout, print the tail of both server logs before exiting.
      ref: e2e-tests/scripts/run-poc.sh:L16-L25 — current readiness failure only prints a generic message
- [ ] Redirect CI E2E Java process stdout/stderr to the same log files.
      ref: .github/workflows/ci-java21.yml:L271-L274 — current CI Java start has no redirect
- [ ] Update CI startup failure logging to print both `target/e2e-server.log` and `target/e2e-server-err.log`.
      ref: .github/workflows/ci-java21.yml:L276-L287 — current wait step only cats `target/e2e-server.log`
- [ ] Add both server logs to the E2E artifact upload.
      ref: .github/workflows/ci-java21.yml:L301-L309 — current artifact upload only includes Playwright report/results

**Validation criteria:**
- Running `e2e-tests/scripts/run-poc.sh` creates `target/e2e-server.log` and `target/e2e-server-err.log`.
- CI artifact path includes Playwright report/results and both server logs.
- If server startup fails, log tail is visible in job output.

### Task 4: Make E2E admin authentication deterministic

Remove the current reliance on login-helper fallback by ensuring the E2E profile leaves admin `passwordChangeRequired=false` after all startup initialization.

**Depends on:** none
**Reference module:** security startup initializers

Steps:
- [ ] Decide the least-invasive fix: either order `E2eDataSeeder` after `SystemInitializer`, or make `SystemInitializer` skip forcing password change under `e2e` profile.
      ref: src/main/java/com/solusi/erp/security/user/security/SystemInitializer.java:L10-L38 — global initializer can set `passwordChangeRequired(true)` during admin sync
      ref: src/main/java/com/solusi/erp/security/user/security/E2eDataSeeder.java:L11-L31 — E2E seeder currently runs with `@Order(200)` and sets admin password change to false
- [ ] Prefer an ordering fix if it preserves production behavior unchanged: give `SystemInitializer` an explicit earlier order and `E2eDataSeeder` a later order.
      ref: src/main/java/com/solusi/erp/security/user/security/SystemInitializer.java:L10-L11 — currently no `@Order`
      ref: src/main/java/com/solusi/erp/security/user/security/E2eDataSeeder.java:L11-L14 — currently `@Order(200)`
- [ ] Add or update a focused test if practical to verify startup runner ordering or e2e post-condition; otherwise validate via E2E server log and login behavior.
      ref: e2e-tests/helpers/auth.ts — login helper currently contains fallback handling for password-change redirects
- [ ] Keep login helper fallback for resilience unless a later cleanup task explicitly removes it after repeated validation.

**Validation criteria:**
- Starting the app with `--spring.profiles.active=e2e` logs `SystemInitializer` before `E2eDataSeeder`, or otherwise guarantees admin ends with `passwordChangeRequired=false`.
- Full Playwright suite still passes.
- Production/non-e2e behavior remains unchanged.

### Task 5: Document E2E selector convention for interactive forms

Turn the lessons from Product/TomSelect into a clear convention so future module templates are easier for agents and humans to test.

**Depends on:** none
**Reference module:** E2E guide, frontend specs, Product form

Steps:
- [ ] Add a dedicated selector convention section to `docs/tests/playwright-e2e-guide.md`.
      ref: docs/tests/playwright-e2e-guide.md:L691-L718 — current checklist mentions reading templates and special components
- [ ] Specify that interactive fields should have stable explicit IDs, especially TomSelect/autocomplete, modal-selector triggers, AutoNumeric inputs, date pickers, and dynamic line rows.
      ref: src/main/resources/templates/inventory/products/form.html:L65-L83 — Product uses explicit `#category-select` and `#brand-select`
      ref: docs/spec/autocomplete-generic.md — autocomplete standards for TomSelect fields
      ref: docs/spec/modal-selector.md — modal selector standards
      ref: docs/spec/numeric-standards.md — AutoNumeric standards
      ref: docs/spec/datetime-standards.md — date picker standards
      ref: docs/spec/header-lines-form.md — dynamic line standards
- [ ] Document Playwright selector preference order: explicit ID for widgets, `name` for plain input/select/textarea, table-scoped text for list assertions, and avoid wrapper-generated CSS classes where possible.
      ref: e2e-tests/tests/master-data/product.spec.ts — Product spec now relies on explicit widget IDs
      ref: e2e-tests/helpers/tomselect.ts — TomSelect helper expects the original `<select>` element, not the wrapper
- [ ] Update stale E2E guide status that still says Product/UoM failures may be unresolved.
      ref: docs/tests/playwright-e2e-guide.md:L482-L501 — current status section predates latest 18/18 passing run

**Validation criteria:**
- The guide explicitly tells future implementers how to name/select interactive fields.
- The guide no longer claims Product/UoM failures are still unresolved if the latest suite is green.
- The guide references existing specs instead of duplicating all frontend component details.

### Task 6: Add smoke E2E split for push-to-main confidence

Introduce a smoke subset separate from full E2E so push-to-main has useful confidence without paying the full E2E cost every merge.

**Depends on:** Task 2
**Reference module:** Playwright config and current specs

Steps:
- [ ] Use Playwright `@smoke` tags as the smoke selection mechanism.
      ref: e2e-tests/playwright.config.ts:L3-L29 — current single Chromium project with no grep/project split
      ref: e2e-tests/package.json:L6-L11 — scripts can add `test:smoke`
- [ ] Mark existing representative tests with `@smoke`: successful login, Brand create, and Product create with TomSelect.
      ref: e2e-tests/tests/auth/login.spec.ts — auth coverage
      ref: e2e-tests/tests/master-data/brand.spec.ts — simple CRUD baseline
      ref: e2e-tests/tests/master-data/product.spec.ts — TomSelect/autocomplete coverage
- [ ] Add npm script `test:smoke` and use it in push-to-main E2E if selected.
      ref: e2e-tests/package.json:L6-L11 — script section
- [ ] Keep full E2E available via manual dispatch and reduced-frequency schedule.
      ref: .github/workflows/ci-java21.yml:L16-L25 — schedule/workflow_dispatch controls

**Validation criteria:**
- `npm run test:smoke` runs only the intended subset.
- Full `npm run test` remains unchanged.
- CI comments or documentation explain smoke vs full E2E usage.

## Recommended Execution Order

1. Task 1 — Migration parity check, because it is cheap and immediately useful on PR.
2. Task 3 — Server log capture, because it improves debugging before changing E2E strategy.
3. Task 4 — Deterministic E2E auth, because it removes a known startup-order ambiguity.
4. Task 5 — Selector convention documentation, because it prevents repeating Product/TomSelect issues.
5. Task 6 — Smoke E2E split, because Task 2 now targets smoke on push-to-main.
6. Task 2 — CI trigger/schedule adjustment, after Task 6 provides the smoke script/tags.

## Decisions

- Full scheduled E2E should run twice weekly at 02:30 WIB. Because GitHub Actions cron uses UTC, use Sunday and Wednesday 19:30 UTC (`30 19 * * 0,3`) to run Monday and Thursday 02:30 WIB.
- Scheduled full E2E should preserve the existing order: run `full-tests` first, then run full `e2e-tests` only after `full-tests` succeeds.
- Push-to-main should run full Maven tests plus a smoke E2E subset once this plan adds `@smoke` tags/scripts.
- Manual full dispatch and the twice-weekly schedule should keep running full E2E.
- Migration parity should be a separate CI job for clearer ownership and failure output.

## Final Validation

After all selected tasks:

- Run migration parity script locally.
- Run Maven test scope impacted by `SystemInitializer` / `E2eDataSeeder` changes.
- Run `e2e-tests/scripts/run-poc.ps1` on Windows or `e2e-tests/scripts/run-poc.sh` on Linux/macOS.
- Confirm full Playwright suite remains green.
- Confirm CI workflow syntax and job conditions are coherent.
