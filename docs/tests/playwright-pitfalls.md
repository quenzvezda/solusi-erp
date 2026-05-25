# Playwright E2E Pitfalls — Known Issues & Patterns

> Catalog of failure patterns observed in Solusi ERP E2E specs. Every entry represents a real bug from a prior session. Read this before writing or modifying any spec under `e2e-tests/`.

> Last updated: 2026-05-20 (post Stream A+B SA/RBAC fix bundle, 4 rounds)

## How to Use This Doc

- **When writing a new spec:** scan the table of contents below. If your spec touches any of the listed concerns (TomSelect, modal confirm, storage state, version-bumped JAR), read the corresponding section before coding.
- **When a spec fails unexpectedly:** match the symptom to one of the entries. Each entry lists the root cause and the canonical fix.
- **When extending a helper:** check the "Helper Limitations" section. Some shared helpers have known bugs that are routed-around in spec code rather than fixed in the helper itself.

## Table of Contents

1. [`page.evaluate(fetch)` from `about:blank` returns nothing](#1-pageevaluatefetch-from-aboutblank)
2. [`setTomSelectValue` injects no payload — page handlers silently fail](#2-settomselectvalue-injects-no-payload)
3. [`selectTomSelect` helper has broken signature — promise never resolves](#3-selecttomselect-broken-signature)
4. [`page.on('dialog')` does not catch Bootstrap modal confirms](#4-pageondialog-does-not-catch-bootstrap-modal)
5. [Time-based `.auth/*.json` freshness check breaks on H2 server restart](#5-time-based-auth-freshness)
6. [Run script picks stale JAR by alphabetical order after version bump](#6-stale-jar-alphabetical-pickup)
7. [Status badge selector points to form/edit page that has no badge](#7-badge-selector-on-form-page)
8. [URL taken from entity name instead of `@RequestMapping`](#8-url-from-entity-name)
9. [`@ExceptionHandler` returns view name without `@ResponseStatus` — RBAC classifier breaks](#9-error-view-without-responsestatus)
10. [Marking task `[x]` without running the spec — runtime validation deferred](#10-runtime-validation-deferred)

---

## 1. `page.evaluate(fetch)` from `about:blank`

**Symptom:** Test fails in 100-300ms with a "not found" error from a lookup helper, before any user-visible interaction.

**Root cause:** `page.evaluate(() => fetch('/api/...'))` is dispatched from the page's JS context. If `page.goto(...)` has not been called yet, the page is still `about:blank` — which has no origin — and relative URLs cannot be resolved. Response is null/throws silently.

**Fix:** Use `page.request.get(...)` instead. Playwright's `APIRequestContext` carries storage-state cookies and resolves relative URLs against `playwright.config.ts` `baseURL`, regardless of whether the page has navigated.

```ts
// WRONG
const id = await page.evaluate(async () => {
  const res = await fetch('/api/lookup/inventory/products?q=X');  // about:blank → fails
  return (await res.json())?.[0]?.id;
});

// RIGHT
const res = await page.request.get('/api/lookup/inventory/products?q=X');
if (!res.ok()) throw new Error(`HTTP ${res.status()}`);
const id = (await res.json())?.[0]?.id;
```

**Reference:** Round 1 fix in `e2e-tests/tests/inventory/stock-adjustment.spec.ts` (`resolveProductLaptopId`).

---

## 2. `setTomSelectValue` injects no payload

**Symptom:** TomSelect option visually selected but downstream effects (auto-fill of derived fields like UoM, last cost, isSerialized) never fire. Subsequent `waitForFunction(el.value !== '')` times out.

**Root cause:** The shared helper `setTomSelectValue` (`e2e-tests/helpers/tomselect.ts`) injects an option as `{id, name, text}` only — no `payload` field. Many page-JS change handlers read `tsProd.options[val].payload.uomId` (or similar) to populate derived fields. Without payload, `p.uomId` is `undefined`.

**Fix:** For payload-dependent fields, fetch the LookupDto explicitly and inject the full option:

```ts
const res = await page.request.get('/api/lookup/inventory/products?q=E2E-PRD-LAPTOP');
const opt = (await res.json())?.[0];
await page.evaluate(({ sel, option }) => {
  const el = document.querySelector(sel) as any;
  el.tomselect.addOption(option);   // option has full payload
  el.tomselect.setValue(String(option.id));
}, { sel: lineSelector, option: opt });
```

**Identifying payload-dependent fields:** Search the page-specific JS for `tsXxx.on('change', ...)` and look at what it reads. If it reads `options[val].payload`, that field needs the explicit-fetch pattern.

**Reference:** Round 3 fix in SA spec (`selectProductOnLine` local helper). Page handler at `static/js/inventory/adjustment/stock-adjustment-form.js:204-212`.

---

## 3. `selectTomSelect` broken signature

**Symptom:** Test hangs at the helper call until full test timeout (30s default).

**Root cause:** `selectTomSelect(page, sel, query, idx)` in `helpers/tomselect.ts` calls `ts.load(query, callback)` — but TomSelect's `load(query)` does not accept a second-arg callback. The promise inside the helper never resolves.

**Status:** Helper is currently broken. Bug filed; awaiting follow-up to either fix the helper or remove it. Until then, all spec code MUST avoid `selectTomSelect`.

**Fix:** Use `setTomSelectValue` for fields where payload doesn't matter, or the explicit-fetch pattern from #2 for payload-dependent fields.

**Reference:** Round 4 finding in `docs/reports/e2e-sa-rbac-pr-reject.md`.

---

## 4. `page.on('dialog')` does not catch Bootstrap modal

**Symptom:** Action requiring confirmation (process to inventory, cancel PR, facility change) does nothing visible. Test times out at `waitForURL` or downstream assertion.

**Root cause:** Solusi ERP uses a Bootstrap modal (`#modal-global-confirm`) via `ErpModal.confirm` / `ErpAction.confirmAndSubmit` — NOT native `window.confirm`. The dialog handler `page.on('dialog', d => d.accept())` matches nothing; modal stays open.

**Fix:** Click `#confirm-modal-btn-yes` explicitly:

```ts
// WRONG
page.on('dialog', (d) => d.accept());
await page.locator('#btn-process-inventory').click();

// RIGHT
await page.locator('#btn-process-inventory').click();
await page.locator('#confirm-modal-btn-yes').click();
await page.waitForURL(/\/view\/\d+/);
```

**When `page.on('dialog')` IS correct:** Flatpickr month selectors and a few admin-only legacy flows still use native `window.confirm`. Check the page JS first — search for `ErpModal.confirm` or `ErpAction.confirmAndSubmit` to know which mechanism the page uses.

**Reference:** Round 4 fixes in SA Scenario C (process) and Scenario D (facility change). Mechanism at `static/js/shared/erp-common-handler.js:32-56`.

---

## 5. Time-based `.auth/` freshness

**Symptom:** Run #1 passes. Run #2, started within 30 minutes of #1, has 30+ tests fail with 11.5s timeout — all the failing tests are "authenticated" routes that redirect to `/login` because cookies are dead.

**Root cause:** `global.setup.ts` previously used `Date.now() - mtimeMs < FRESH_TTL_MS` to decide whether to skip re-login. This logic is correct for production but wrong for E2E because H2 is in-memory: every JVM restart wipes sessions instantly while the file mtime stays "fresh".

**Fix:** Probe the server with the saved storage state instead of trusting mtime. Accept only 2xx on `/dashboard`:

```ts
async function isStateValid(file: string, baseURL: string): Promise<boolean> {
  if (!fs.existsSync(file)) return false;
  const ctx = await request.newContext({ baseURL, storageState: file });
  try {
    const res = await ctx.get('/dashboard', { maxRedirects: 0 });
    return res.status() >= 200 && res.status() < 300;
  } finally {
    await ctx.dispose();
  }
}
```

**General principle:** Persistent state on disk + ephemeral state on server cannot sync via heuristic file age. Trust the server, not the filesystem.

**Reference:** Round 2 fix in `e2e-tests/global.setup.ts`.

---

## 6. Stale JAR alphabetical pickup

**Symptom:** Code change in main branch confirmed by `git diff`, but running spec against fresh server shows old behavior. Sometimes header line of run script logs `Starting server: ...solusi-program-erp-1.X.0.jar` when pom.xml shows 1.X.1.

**Root cause:** After `pom.xml` version bump (e.g. 1.7.0 → 1.7.1), running `mvnw package` produces `solusi-program-erp-1.7.1.jar` BUT leaves `solusi-program-erp-1.7.0.jar` in `target/` from the previous build. Run scripts using `(Get-ChildItem ...)[0]` (PowerShell) or `ls ...jar | head -1` (bash) pick alphabetically, getting the OLDER JAR.

**Fix in `run-poc.ps1`:**

```powershell
# Clean stale JARs first
Get-ChildItem "$ProjectRoot\target\solusi-program-erp-*.jar" -ErrorAction SilentlyContinue |
    Remove-Item -Force -ErrorAction SilentlyContinue
.\mvnw.cmd -B package -DskipTests -Pe2e -q
# Pick newest by mtime, defensive even after cleanup
$jar = (Get-ChildItem "$ProjectRoot\target\solusi-program-erp-*.jar" |
        Sort-Object LastWriteTime -Descending | Select-Object -First 1).FullName
```

**Fix in `run-poc.sh`:** Replace `ls target/*.jar | head -1` with `ls -t target/*.jar | head -1` (mtime sort) and add `rm -f target/solusi-program-erp-*.jar` before `mvnw package`.

**Reference:** Round 2 fix.

---

## 7. Badge selector on form page

**Symptom:** Spec creates a record successfully, navigates to `/edit/{id}`, then `expect(page.locator('.page-title .badge', {hasText: 'DRAFT'})).toBeVisible()` times out at 10s.

**Root cause:** Most ERP modules render the status badge ONLY in `view.html` (page-header section), not in `form.html`. The form/edit page just shows the code/title. Spec mirrored a PR pattern that happens to be the exception — most modules follow the SA convention.

**Fix:** Always check both templates:

```bash
# Where does the badge actually live?
grep -n "badge" src/main/resources/templates/{module}/{form,view}.html
```

Then assert on the page that has the badge:

```ts
// SA pattern (common case): badge on view page only
await navigateToModule(page, `/inventory/adjustments/view/${id}`);
await expect(
  page.locator('.page-header .badge', { hasText: 'DRAFT' })
).toBeVisible();
```

Note: badge is sibling of `.page-title`, not a child. Selector `.page-title .badge` (descendant combinator) is wrong; use `.page-header .badge`.

**Reference:** Round 4 fix in SA Scenarios A and C.

---

## 8. URL from entity name

**Symptom:** Spec's RBAC matrix entry expects `allow` for an admin role on a list page, but classifier returns `deny` because Spring throws `NoResourceFoundException` (404) on the URL.

**Root cause:** Many controllers are mounted at rebranded URLs that don't match the entity name. Examples observed:
- Entity `PermissionGroup` → controller `/security/menu-groups` (not `/security/permission-groups`)
- Several master-data modules use the modular naming (`menu-groups`) instead of the entity naming.

**Fix:** Always grep `@RequestMapping` for the actual route:

```bash
grep -r "@RequestMapping" src/main/java/com/solusi/erp/{module}/web/controller/
```

Note that `/api/...` URLs are JSON endpoints (separate `@RestController`), and the user-facing Thymeleaf URL is usually different. Don't conflate them.

**Reference:** Round 1 fix in `e2e-tests/tests/auth/rbac.spec.ts` (`RESOURCES.permGroup.listUrl`).

---

## 9. Error view without `@ResponseStatus`

**Symptom:** RBAC test deny case classified as `allow`. Spring renders the 403 page but HTTP status is 200; classifier checking `status >= 400` doesn't trigger.

**Root cause:** `GlobalExceptionHandler.handleAccessDeniedException` returned the view name `"error/403"` without `@ResponseStatus(HttpStatus.FORBIDDEN)`. Spring renders the view at default status 200. Compare with `handleNoResourceFoundException` in the same file, which correctly carries `@ResponseStatus(NOT_FOUND)`.

**Fix:** Pair view-returning error handlers with the matching `@ResponseStatus`:

```java
@ExceptionHandler(AccessDeniedException.class)
@ResponseStatus(HttpStatus.FORBIDDEN)            // <— add this
public Object handleAccessDeniedException(...) {
    if (isAjaxRequest(request)) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)...;
    }
    return "error/403";
}
```

The HTML view still renders normally; only the response status changes. AJAX/API branch already returns explicit `ResponseEntity.status(...)` so it is unaffected.

**Reference:** Round 1 fix in `src/main/java/com/solusi/erp/core/exception/GlobalExceptionHandler.java`.

---

## 10. Runtime validation deferred

**Symptom:** Plan task marked `[x]`, then the next session's full suite reveals 5+ failures in that task's spec.

**Root cause:** This is the meta-bug behind the other 9. When a plan task creates a Playwright spec but no live server is available (or building the JAR is slow), the agent compiles the TS, runs `--list`, and marks the task complete. The actual `npx playwright test` is "deferred to finalize". Compile + list cannot catch:
- Selector mismatches (badge on wrong page)
- Modal vs dialog confusion
- Storage state assumptions
- TomSelect helper mismatches with page handlers

All 9 prior pitfalls in this doc were caught only when an actual run happened. Smoke split (`@smoke` covers a small subset) means push-to-main stays green and the bugs surface only on cold full-suite runs — often days later.

**Rule:** A task that creates or modifies a Playwright spec is NOT complete until the spec has been run at least once. If the run cannot happen in the current session, the task stays `[~]` with a report finding noting "E2E run deferred — gate not executed". The next session must execute the gate before marking `[x]`. No exceptions.

**Reference:** Pattern note in Round 1 + Round 4 sections of `docs/reports/e2e-sa-rbac-pr-reject.md`.

---

## 11. Cold route first-hit can exceed the default test timeout

**Symptom:** Full-suite run fails on the first visit to a rarely used SSR route, then retry passes. The failing action is usually `page.goto(..., { waitUntil: 'domcontentloaded' })` with Playwright's default 30s test timeout.

**Root cause:** Spring/JIT/template initialization plus CDN-dependent assets can make the first request to a cold controller path exceed the per-test default. The retry is warm and passes, so the suite exits 0 with a flaky marker.

**Fix:** Add the route to `e2e-tests/scripts/run-e2e.ps1` warmup when it is part of the full suite, and give matrix-style navigation tests an explicit timeout budget (for example `test.setTimeout(60_000)`).

**Reference:** RBAC `PermissionGroup` first-hit flake on `/security/menu-groups` during PO E2E finalize.

---

## Helper Limitations (Known Issues, Not Yet Fixed)

| Helper | Issue | Workaround |
|--------|-------|------------|
| `e2e-tests/helpers/tomselect.ts::selectTomSelect` | `ts.load(query, callback)` signature mismatch — promise never resolves | Use `setTomSelectValue` (no payload) or local fetch+addOption pattern (#2) |
| `e2e-tests/helpers/tomselect.ts::setTomSelectValue` | No payload field in injected option — page change handlers reading `payload.xxx` get undefined | For payload-dependent fields, write a local helper that fetches the LookupDto explicitly |

When fixing a helper here, remove the entry. When adding a new known issue, append to this table with a workaround.

## Authoring Checklist

Before marking any E2E task `[x]`, verify:

- [ ] Read `view.html` of the target module (badge selector source of truth)
- [ ] Read page-specific JS for the target module (TomSelect change handlers, modal mechanism)
- [ ] Greped `@RequestMapping` for the actual URL (not entity name)
- [ ] Used `page.request.get` instead of `page.evaluate(fetch)` for any pre-navigation API call
- [ ] Used `setTomSelectValue` only for payload-independent fields (or local fetch+addOption for payload-dependent)
- [ ] Did NOT use `selectTomSelect` (broken)
- [ ] Used `#confirm-modal-btn-yes` click for ERP confirm flows (not `page.on('dialog')`)
- [ ] Ran `cd e2e-tests && npx tsc --noEmit` — clean
- [ ] Ran `npx playwright test {file}` — green at least once
- [ ] (For transactional specs) Ran `rm -rf .auth/ && npx playwright test {file}` — green on cold cache too
