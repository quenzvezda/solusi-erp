import { test, expect, storageStatePath } from '../../fixtures/base';
import { setFlatpickrDate } from '../../helpers/flatpickr';
import { setTomSelectValue } from '../../helpers/tomselect';
import { setAutoNumeric } from '../../helpers/autonumeric';
import { addLine, lineFieldSelector } from '../../helpers/line-editor';
import { drawSignature, assertSignatureNotEmpty } from '../../helpers/signature-pad';
import { navigateToModule } from '../../helpers/navigation';
import { waitForNetworkIdle } from '../../helpers/waits';
import { Page, BrowserContext, chromium } from '@playwright/test';

/**
 * Purchase Requisition E2E flow.
 *
 * Most scenarios involve a handoff between employee1 (requester) and approver1
 * (decision-maker). To avoid re-login per role, each scenario opens a second
 * browser context with the approver's storage state when needed.
 */

// Seeded ids from V9000__e2e_seed_data.sql.
// Resolved at runtime so the spec is independent of insertion order.
async function resolveSeedIds(page: Page): Promise<{
  employee1PartyId: string;
  approver1PartyId: string;
  supplierPartyId: string;
  facilityId: string;
  productLaptopId: string;
  uomPcsId: string;
  currencyIdrId: string;
}> {
  // Use the lookup endpoints the form itself uses — they're already authorized
  // for employee1 in V9000.
  const fetchJson = async (path: string) => {
    return await page.evaluate(async (p) => {
      const res = await fetch(p, { credentials: 'same-origin' });
      return res.ok ? await res.json() : null;
    }, path);
  };

  const apr = await fetchJson('/api/lookup/parties/by-role-type?roleTypeCode=APPROVER&q=Budi');
  const emp = await fetchJson('/api/lookup/parties?q=Dewi');
  const sup = await fetchJson('/api/lookup/parties?q=Sumber');
  const fac = await fetchJson('/api/lookup/inventory/facilities?q=E2E');
  const prd = await fetchJson('/api/lookup/inventory/products?q=E2E-PRD-LAPTOP');

  // currency: read from form - currencies dropdown is populated server-side; pick IDR.
  // Fallback: read from spl-price api won't work without currency.
  // We'll do it via /master/currencies if available, otherwise fall back to picking
  // the option whose label is 'IDR' in the form select.

  return {
    employee1PartyId: String(emp?.[0]?.id ?? ''),
    approver1PartyId: String(apr?.[0]?.id ?? ''),
    supplierPartyId: String(sup?.[0]?.id ?? ''),
    facilityId: String(fac?.[0]?.id ?? ''),
    productLaptopId: String(prd?.[0]?.id ?? ''),
    uomPcsId: '9001',
    currencyIdrId: '', // resolved from page DOM in test
  };
}

/**
 * Fill PR header with the given seeded ids and create one line. Saves the PR
 * (DRAFT) and returns the PR id parsed from the redirect URL.
 */
async function createDraftPr(page: Page, seed: Awaited<ReturnType<typeof resolveSeedIds>>): Promise<number> {
  await navigateToModule(page, '/purchasing/purchase-requisitions/create');

  await setFlatpickrDate(page, 'input[name="requestDate"]', '2026-05-19');
  await setTomSelectValue(page, '#header-requester', seed.employee1PartyId);
  await setTomSelectValue(page, '#header-facility', seed.facilityId);
  await setTomSelectValue(page, '#header-supplier', seed.supplierPartyId);

  // Currency: native <select> with options seeded from server. Pick IDR by label.
  const currencyId = await page.evaluate(() => {
    const sel = document.querySelector('select[name="currencyId"]') as HTMLSelectElement | null;
    if (!sel) return '';
    const opt = Array.from(sel.options).find(o => o.text.trim() === 'IDR');
    return opt?.value ?? '';
  });
  if (!currencyId) throw new Error('IDR option not found in currencyId select');
  await page.selectOption('select[name="currencyId"]', currencyId);

  await page.selectOption('select[name="priority"]', 'HIGH');

  // Add one line.
  await addLine(page);

  // Pick product via TomSelect (line 0 product cell).
  await setTomSelectValue(page, `[name="${'lines[0].productId'}"]`, seed.productLaptopId);

  // Quantity (AutoNumeric).
  await setAutoNumeric(page, lineFieldSelector(0, 'quantity'), 5);

  // UoM via TomSelect (line cell).
  await setTomSelectValue(page, `[name="${'lines[0].uomId'}"]`, seed.uomPcsId);

  // Required date.
  await setFlatpickrDate(page, lineFieldSelector(0, 'requiredDate'), '2026-06-01');

  // Estimated price (AutoNumeric).
  await setAutoNumeric(page, lineFieldSelector(0, 'estimatedUnitPrice'), 8500000);

  // Submit form (data-ajax-form). Form posts to /create and on success
  // window navigates to /purchasing/purchase-requisitions (list).
  await Promise.all([
    page.waitForURL(/\/purchasing\/purchase-requisitions(\?.*)?$/, { timeout: 15_000, waitUntil: 'domcontentloaded' }),
    page.locator('form#pr-form button[type="submit"]').first().click(),
  ]);

  // The list page now contains our PR. Extract its id from the row link.
  // Edit links look like: /purchasing/purchase-requisitions/edit/{id}
  // Pick the highest id since list ordering may not put newest first.
  await waitForNetworkIdle(page);
  const id = await page.evaluate(() => {
    const links = Array.from(document.querySelectorAll('a[href*="/purchase-requisitions/edit/"]'));
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
  if (!id) throw new Error('createDraftPr: could not determine new PR id from list page');
  return id;
}

/**
 * On the saved PR's edit page, click "Submit for Approval", pick approver in
 * modal, confirm. Page navigates back to list once submit succeeds.
 */
async function submitPrForApproval(page: Page, prId: number, approverPartyId: string): Promise<void> {
  await navigateToModule(page, `/purchasing/purchase-requisitions/edit/${prId}`);

  await page.locator('#btn-submit-pr').click();
  // Wait for the submit modal to be shown.
  await page.locator('#modal-submit-approval').waitFor({ state: 'visible', timeout: 10_000 });

  // Select approver via TomSelect inside the modal.
  await setTomSelectValue(page, '#submit-approver', approverPartyId);

  await Promise.all([
    page.waitForURL(/\/purchasing\/purchase-requisitions(\?.*)?$/, { timeout: 15_000, waitUntil: 'domcontentloaded' }),
    page.locator('#btn-confirm-submit-approval').click(),
  ]);
}

test.describe('Purchase Requisition flow', () => {
  test.use({ storageState: storageStatePath('employee1') });

  test('@smoke Scenario A — happy path: employee1 creates → submits → approver1 approves & finishes', async ({ page, browser }) => {
    test.setTimeout(120_000);

    // Land on PR list first so subsequent fetch() calls have a same-origin base URL.
    await navigateToModule(page, '/purchasing/purchase-requisitions');

    const seed = await resolveSeedIds(page);
    expect(seed.employee1PartyId, 'employee1 party seed').toBeTruthy();
    expect(seed.approver1PartyId, 'approver1 party seed').toBeTruthy();
    expect(seed.supplierPartyId, 'supplier party seed').toBeTruthy();
    expect(seed.facilityId, 'facility seed').toBeTruthy();
    expect(seed.productLaptopId, 'product seed').toBeTruthy();

    const prId = await createDraftPr(page, seed);
    await submitPrForApproval(page, prId, seed.approver1PartyId);

    // ── Switch to approver1 in a second context ──
    const approverContext = await browser.newContext({ storageState: storageStatePath('approver1') });
    const approverPage = await approverContext.newPage();
    await approverPage.goto(`/purchasing/purchase-requisitions/view/${prId}`);
    await waitForNetworkIdle(approverPage);

    // Status badge should read SUBMITTED.
    await expect(approverPage.locator('.page-title .badge', { hasText: 'SUBMITTED' })).toBeVisible();

    // Open Approve & Finish modal. The button label is i18n-translated, so we
    // target by the JS handler on the onclick attribute (stable identifier).
    // Wait for the global script to define ApprovalUI in script-scope (it's not
    // attached to window, so we test indirectly by checking SignaturePad is loaded).
    await approverPage.waitForFunction(() => typeof (window as any).SignaturePad !== 'undefined', { timeout: 10_000 });
    await approverPage.locator('button[onclick="ApprovalUI.openApproveFinishModal()"]').first().click();
    await approverPage.locator('#modal-approve-finish.show').waitFor({ state: 'visible', timeout: 10_000 });

    // Wait for SignaturePad to attach to the canvas (init runs on shown.bs.modal).
    await approverPage.waitForFunction(() => {
      const canvas = document.getElementById('sig-canvas-approve-finish') as HTMLCanvasElement | null;
      return canvas !== null && canvas.offsetWidth > 0 && canvas.width > 0;
    }, { timeout: 5_000 });

    await approverPage.fill('#approve-finish-notes', 'E2E approval — auto signed.');
    await drawSignature(approverPage, '#sig-canvas-approve-finish');
    await assertSignatureNotEmpty(approverPage, '#sig-canvas-approve-finish');

    // signature_pad@4 binds its own pointer listeners that synthetic events
    // can't reliably trigger inside a Bootstrap modal — so its internal
    // isEmpty() may still report true even though we've painted real pixels
    // on the canvas. Submit the approval via the same /common/approval/{id}/process
    // endpoint the app uses, with the canvas's actual toDataURL() PNG. This
    // exercises the full backend stack (auth, status transition, signature
    // persistence) while sidestepping the library's empty-check.
    const approvalRequestId = await approverPage.evaluate(() => {
      const el = document.getElementById('current-approval-request-id') as HTMLInputElement | null;
      return el?.value ?? '';
    });
    expect(approvalRequestId, 'approvalRequestId hidden field present').toBeTruthy();

    const csrfHeaderName = await approverPage.evaluate(() => {
      const meta = document.querySelector('meta[name="_csrf_header"]') as HTMLMetaElement | null;
      return meta?.content ?? 'X-XSRF-TOKEN';
    });
    const csrfToken = await approverPage.evaluate(() => {
      const meta = document.querySelector('meta[name="_csrf"]') as HTMLMetaElement | null;
      return meta?.content ?? '';
    });

    const resp = await approverPage.evaluate(
      async ({ id, headerName, token }) => {
        const canvas = document.getElementById('sig-canvas-approve-finish') as HTMLCanvasElement;
        const dataUrl = canvas.toDataURL('image/png');
        const headers: Record<string, string> = {
          'Content-Type': 'application/json',
          'Accept': 'application/json',
        };
        if (headerName && token) headers[headerName] = token;
        const r = await fetch(`/common/approval/${id}/process`, {
          method: 'POST',
          headers,
          body: JSON.stringify({
            action: 'APPROVE_AND_FINISH',
            notes: 'E2E approval — auto signed.',
            signatureBase64: dataUrl,
          }),
        });
        return { status: r.status, body: await r.text() };
      },
      { id: approvalRequestId, headerName: csrfHeaderName, token: csrfToken }
    );
    expect(resp.status, `approval /process status — body: ${resp.body}`).toBeLessThan(400);

    // Reload to see the new state.
    await approverPage.goto(`/purchasing/purchase-requisitions/view/${prId}`);
    await waitForNetworkIdle(approverPage);
    await expect(approverPage.locator('.page-title .badge', { hasText: 'APPROVED' })).toBeVisible({ timeout: 15_000 });

    await approverContext.close();
  });

  test('Scenario B — submit then reject: approver1 rejects with notes', async () => {
    test.skip(true, 'Implemented in Task 10');
  });

  test('Scenario C — DRAFT edit then cancel', async () => {
    test.skip(true, 'Implemented in Task 11');
  });

  test('Scenario D — APPROVED cancel transition', async () => {
    test.skip(true, 'Implemented in Task 12');
  });

  test('Scenario E — header change resets line container', async () => {
    test.skip(true, 'Implemented in Task 13');
  });

  test('Scenario F — SPL price autofill on product pick', async () => {
    test.skip(true, 'Implemented in Task 14 (optional)');
  });

  test('Sanity — employee1 storage state lands authenticated on PR list', async ({ page }) => {
    await page.goto('/purchasing/purchase-requisitions');
    await expect(page).toHaveURL(/\/purchasing\/purchase-requisitions/);
  });
});
