import { test, expect, storageStatePath } from '../../fixtures/base';
import { navigateToModule } from '../../helpers/navigation';
import { setTomSelectValue } from '../../helpers/tomselect';
import { setAutoNumeric } from '../../helpers/autonumeric';
import { Page } from '@playwright/test';

/**
 * Purchase Order E2E flow — STANDARD type only (DIRECT skipped per plan
 * docs/plans/2026-05-25-e2e-purchase-order.md, overlaps PR/SA pattern).
 *
 * Lifecycle covered: DRAFT -> SUBMITTED -> APPROVED -> SENT
 *                    DRAFT -> CANCELLED
 *                    DRAFT -> (DELETE)
 *                    SUBMITTED -> REJECTED
 *
 * Logged in as warehouse1 — granted PO_{READ,CREATE,UPDATE,DELETE,SUBMIT,SEND}
 * + LOOKUP_{PR,PO,TAX,PARTY} via V9000.
 */

// Seeded ids from V9000__e2e_seed_data.sql.
const PR_ID = '9301';
const PR_LINE_LAPTOP_ID = '9301';
const PR_LINE_CHAIR_ID = '9302';
const FACILITY_ID = '9101';
const TAX_ID = '9001';

/**
 * Resolve dynamic seed ids (party ids, product id) at runtime via lookup
 * endpoints. Adapted from PR spec's resolveSeedIds.
 */
async function resolveSeedIds(page: Page): Promise<{
  approver1PartyId: string;
  supplierPartyId: string;
  productLaptopId: string;
}> {
  const fetchJson = async (path: string) => {
    return await page.evaluate(async (p) => {
      const res = await fetch(p, { credentials: 'same-origin' });
      return res.ok ? await res.json() : null;
    }, path);
  };

  const apr = await fetchJson('/api/lookup/parties/by-role-type?roleTypeCode=APPROVER&q=Budi');
  const sup = await fetchJson('/api/lookup/parties?q=Sumber');
  const prd = await fetchJson('/api/lookup/inventory/products?q=E2E-PRD-LAPTOP');

  return {
    approver1PartyId: String(apr?.[0]?.id ?? ''),
    supplierPartyId: String(sup?.[0]?.id ?? ''),
    productLaptopId: String(prd?.[0]?.id ?? ''),
  };
}

/**
 * Wait for a Bootstrap modal/offcanvas to fully transition (no .show, no
 * .hiding, no .showing classes). Replaces toBeHidden polling which is flaky
 * across the suite for Bootstrap 5 transitions.
 */
async function waitForModalSettled(page: Page, modalId: string, opts?: { hidden?: boolean }): Promise<void> {
  await page.waitForFunction(
    ({ id, expectHidden }) => {
      const el = document.getElementById(id);
      if (!el) return expectHidden === true;
      const classes = el.classList;
      const intermediate = classes.contains('hiding') || classes.contains('showing');
      if (intermediate) return false;
      if (expectHidden) return !classes.contains('show');
      return classes.contains('show');
    },
    { id: modalId, expectHidden: opts?.hidden ?? false },
    { timeout: 10_000 }
  );
}

/**
 * Open the PR selector modal, pick the row matching `prCode`, and click its
 * "Choose" button. Waits for the modal to close before returning.
 *
 * Note: STANDARD radio must already be selected — `#btn-select-pr` is disabled
 * while DIRECT is active.
 */
async function pickPrFromModal(page: Page, prCode: string): Promise<void> {
  // The page auto-opens this modal when STANDARD is selected on a fresh form
  // (purchase-order-form.js:584). Race-safe: wait briefly for the modal to
  // already be showing; if not, click #btn-select-pr to open it manually.
  const opened = await page
    .waitForFunction(
      () => {
        const el = document.getElementById('modal-po-pr-selector');
        return !!el && el.classList.contains('show');
      },
      undefined,
      { timeout: 2_000 }
    )
    .then(() => true)
    .catch(() => false);
  if (!opened) {
    await page.locator('#btn-select-pr').click();
  }
  await waitForModalSettled(page, 'modal-po-pr-selector');
  // Wait for HTMX results to load — table rows are inside #po-pr-selector-results.
  await page.waitForFunction(
    (code) => {
      const row = document.querySelector(`#po-pr-selector-results tr[data-pr-code="${code}"]`);
      return row !== null;
    },
    prCode,
    { timeout: 10_000 }
  );
  await page
    .locator(`#po-pr-selector-results tr[data-pr-code="${prCode}"] .js-pr-selector-pick`)
    .click();
  await waitForModalSettled(page, 'modal-po-pr-selector', { hidden: true });
}

/**
 * Open the PR-line selector modal (via "Add Line" button in STANDARD mode),
 * pick the row matching `productCode`, and click its choose button. Waits for
 * modal to close + new row to be appended to #line-container.
 */
async function pickPrLineFromModal(page: Page, productCode: string): Promise<void> {
  const prevCount = await page.locator('#line-container tr.line-row').count();
  await page.locator('#btn-add-line').click();
  await waitForModalSettled(page, 'modal-po-pr-line-selector');
  await page.waitForFunction(
    (code) => {
      const rows = Array.from(
        document.querySelectorAll('#po-pr-line-selector-results tr[data-pr-line-id]')
      ) as HTMLElement[];
      return rows.some((r) => (r.dataset.productSubtext ?? '').includes(code));
    },
    productCode,
    { timeout: 10_000 }
  );
  // Modal uses multi-select: tick the checkbox in the matching row, then click
  // the global "Apply" button at the modal footer.
  const prLineId = await page.evaluate((code) => {
    const rows = Array.from(
      document.querySelectorAll('#po-pr-line-selector-results tr[data-pr-line-id]')
    ) as HTMLElement[];
    const target = rows.find((r) => (r.dataset.productSubtext ?? '').includes(code));
    return target ? target.dataset.prLineId : null;
  }, productCode);
  if (!prLineId) throw new Error(`pickPrLineFromModal: no row matched ${productCode}`);
  await page
    .locator(`#po-pr-line-selector-results tr[data-pr-line-id="${prLineId}"] .js-pr-line-selector-item`)
    .check();
  await page.locator('#po-pr-line-selector-results .js-pr-line-selector-apply').click();
  await waitForModalSettled(page, 'modal-po-pr-line-selector', { hidden: true });
  await expect(page.locator('#line-container tr.line-row')).toHaveCount(prevCount + 1, { timeout: 5_000 });
}

/**
 * Fetch CSRF header name + token from page meta tags. Test must have navigated
 * to a logged-in page first.
 */
async function readCsrf(page: Page): Promise<{ headerName: string; token: string }> {
  return await page.evaluate(() => {
    const headerMeta = document.querySelector('meta[name="_csrf_header"]') as HTMLMetaElement | null;
    const tokenMeta = document.querySelector('meta[name="_csrf"]') as HTMLMetaElement | null;
    return {
      headerName: headerMeta?.content ?? 'X-XSRF-TOKEN',
      token: tokenMeta?.content ?? '',
    };
  });
}

/**
 * Process an approval decision via /common/approval/{id}/process. Verbatim
 * port of PR spec's helper — endpoint is generic across reference types.
 */
async function processApproval(
  approverPage: Page,
  approvalRequestId: string,
  action: 'APPROVE_AND_FINISH' | 'REJECTED',
  notes: string
): Promise<{ status: number; body: string }> {
  const { headerName, token } = await readCsrf(approverPage);
  return await approverPage.evaluate(
    async ({ id, headerName, token, action, notes }) => {
      const headers: Record<string, string> = {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
      };
      if (headerName && token) headers[headerName] = token;
      const body: Record<string, unknown> = { action, notes };
      if (action === 'APPROVE_AND_FINISH') {
        const canvas = document.getElementById('sig-canvas-approve-finish') as HTMLCanvasElement | null;
        if (canvas) body.signatureBase64 = canvas.toDataURL('image/png');
      }
      const r = await fetch(`/common/approval/${id}/process`, {
        method: 'POST',
        headers,
        body: JSON.stringify(body),
      });
      return { status: r.status, body: await r.text() };
    },
    { id: approvalRequestId, headerName, token, action, notes }
  );
}

/**
 * Create a DRAFT STANDARD PO from the seeded APPROVED PR (id 9301):
 * - opens create form
 * - selects STANDARD radio
 * - picks PR via modal -> auto-fills supplier/facility/currency (locked)
 * - picks tax via TomSelect
 * - picks the laptop PR line via modal -> appends row, qty defaults to remaining
 * - sets unit price via AutoNumeric
 * - submits, waits for redirect
 * - returns the new PO id captured from the list page
 */
async function createDraftStandardPo(page: Page): Promise<number> {
  await navigateToModule(page, '/purchasing/purchase-orders/create');
  await expect(page.locator('#po-form')).toBeVisible();

  // Switch to STANDARD type. The label triggers the hidden radio input.
  await page.locator('label[for="po-type-standard"]').click();
  await expect(page.locator('#btn-select-pr')).toBeEnabled({ timeout: 5_000 });

  // Pick PR -> page JS auto-fills supplier/facility/currency and locks them.
  await pickPrFromModal(page, 'E2E-PR-9301');
  // Wait for currency lock effect (proves cascade ran).
  await page.waitForFunction(
    () => {
      const el = document.querySelector('#input-pr-id') as HTMLInputElement | null;
      return !!el && el.value !== '';
    },
    undefined,
    { timeout: 5_000 }
  );

  // Pick tax. Use payload-aware helper because the page JS reads
  // option.payload.code/rate/calculationMode to fill hidden form fields.
  await pickHeaderTax(page, TAX_ID);

  // Pick laptop line via modal selector.
  await pickPrLineFromModal(page, 'E2E-PRD-LAPTOP');

  // Set unit price on the new line.
  await setAutoNumeric(page, '#line-container tr.line-row:nth-of-type(1) .input-unit-price', 8500000);

  // Submit (data-ajax-form -> redirect on success).
  await Promise.all([
    page.waitForURL(/\/purchasing\/purchase-orders(\?.*)?$/, { timeout: 15_000, waitUntil: 'domcontentloaded' }),
    page.locator('#po-form button[type="submit"]').first().click(),
  ]);

  // Wait for list rows to render before extracting id. DRAFT POs render an
  // Edit link (not View) per list.html condition.
  await page.locator('a[href*="/purchasing/purchase-orders/edit/"]').first().waitFor({ timeout: 10_000 });

  // Capture the new PO id from the highest /edit/{id} link on the list.
  const newId = await page.evaluate(() => {
    const links = Array.from(document.querySelectorAll('a[href*="/purchasing/purchase-orders/edit/"]'));
    let max = 0;
    for (const link of links) {
      const m = (link as HTMLAnchorElement).href.match(/\/edit\/(\d+)/);
      if (m) {
        const n = Number(m[1]);
        if (n > max) max = n;
      }
    }
    return max;
  });
  if (!newId) throw new Error('createDraftStandardPo: could not determine new PO id from list');
  return newId;
}

/**
 * Pick a tax via TomSelect with full payload. The page JS reads
 * `option.payload.code/rate/calculationMode` to populate the hidden taxCode/
 * taxRate/taxCalculationMode form fields (purchase-order-form.js:syncHeaderTaxSelection).
 * setTomSelectValue alone leaves payload undefined, causing the
 * `msg.error.po.tax.required` validation on submit.
 */
async function pickHeaderTax(page: Page, taxId: string): Promise<void> {
  await page.waitForFunction(
    () => {
      const el = document.querySelector('#header-tax') as (HTMLSelectElement & { tomselect?: unknown }) | null;
      return !!el && !!el.tomselect;
    },
    undefined,
    { timeout: 10_000 }
  );
  const opt = await page.evaluate(async (id) => {
    // Tax lookup is keyword-based (name/code). Query empty to get all taxes,
    // then match by id locally. The seed has only a handful of taxes so this
    // is fine.
    const res = await fetch(`/api/lookup/master/taxes?q=`, { credentials: 'same-origin' });
    if (!res.ok) return null;
    const list = await res.json();
    return Array.isArray(list) ? list.find((o: { id: string | number }) => String(o.id) === id) ?? null : null;
  }, taxId);
  if (!opt) throw new Error(`pickHeaderTax: tax id ${taxId} not found via lookup`);
  await page.evaluate(({ option }) => {
    const el = document.querySelector('#header-tax') as HTMLSelectElement & { tomselect?: any };
    if (!el?.tomselect) throw new Error('TomSelect not initialized: #header-tax');
    el.tomselect.addOption(option);
    el.tomselect.setValue(String(option.id));
  }, { option: opt });
}

test.describe('Purchase Order flow', () => {
  test.use({ storageState: storageStatePath('warehouse1') });

  test('sanity: warehouse1 can open purchase-orders list', async ({ page }) => {
    await navigateToModule(page, '/purchasing/purchase-orders');
    await expect(page).toHaveURL(/\/purchasing\/purchase-orders(\?.*)?$/);
    await expect(page.locator('table')).toBeVisible();
  });

  test('@smoke Scenario A — create STANDARD DRAFT from PR', async ({ page }) => {
    test.setTimeout(120_000);

    // Land on the list first so subsequent fetch() calls have a same-origin
    // base URL (resolveSeedIds does GET /api/lookup/...).
    await navigateToModule(page, '/purchasing/purchase-orders');
    const seed = await resolveSeedIds(page);
    expect(seed.supplierPartyId, 'supplier seed').toBeTruthy();
    expect(seed.productLaptopId, 'product seed').toBeTruthy();

    const poId = await createDraftStandardPo(page);
    expect(poId, 'expected new PO id').toBeGreaterThan(0);

    // View page: badges live in .page-title (status + type).
    await navigateToModule(page, `/purchasing/purchase-orders/view/${poId}`);
    await expect(page.locator('.page-title .badge', { hasText: 'DRAFT' })).toBeVisible({ timeout: 10_000 });
    await expect(page.locator('.page-title .badge', { hasText: /Standar/i })).toBeVisible();

    // Reopen the edit page to assert prLineId persisted on the line. The
    // hidden input lives at name="lines[0].prLineId" with class .input-pr-line-id.
    await navigateToModule(page, `/purchasing/purchase-orders/edit/${poId}`);
    const persistedPrLineId = await page.evaluate(() => {
      const el = document.querySelector('input[name="lines[0].prLineId"]') as HTMLInputElement | null;
      return el?.value ?? '';
    });
    expect(persistedPrLineId).toBe(PR_LINE_LAPTOP_ID);
  });
});
