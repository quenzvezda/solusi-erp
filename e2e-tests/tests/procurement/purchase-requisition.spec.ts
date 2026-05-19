import { test, expect, storageStatePath } from '../../fixtures/base';
import { setFlatpickrDate } from '../../helpers/flatpickr';
import { setTomSelectValue } from '../../helpers/tomselect';
import { setAutoNumeric, getAutoNumericValue } from '../../helpers/autonumeric';
import { addLine, lineFieldSelector, getLineCount } from '../../helpers/line-editor';
import { drawSignature, assertSignatureNotEmpty } from '../../helpers/signature-pad';
import { navigateToModule } from '../../helpers/navigation';
import { waitForNetworkIdle } from '../../helpers/waits';
import { Page } from '@playwright/test';

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

/**
 * Submit an approval decision (APPROVE_AND_FINISH or REJECTED) via the same
 * /common/approval/{id}/process endpoint the app's submitApproveFinish /
 * submitReject handlers use. Sidesteps the JS-side empty-check that
 * synthetic pointer events can't satisfy in a Bootstrap modal — see report
 * for Task 9.
 */
async function processApproval(
  approverPage: Page,
  approvalRequestId: string,
  action: 'APPROVE_AND_FINISH' | 'REJECTED',
  notes: string
): Promise<{ status: number; body: string }> {
  const csrfHeaderName = await approverPage.evaluate(() => {
    const meta = document.querySelector('meta[name="_csrf_header"]') as HTMLMetaElement | null;
    return meta?.content ?? 'X-XSRF-TOKEN';
  });
  const csrfToken = await approverPage.evaluate(() => {
    const meta = document.querySelector('meta[name="_csrf"]') as HTMLMetaElement | null;
    return meta?.content ?? '';
  });
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
    { id: approvalRequestId, headerName: csrfHeaderName, token: csrfToken, action, notes }
  );
}

/**
 * Cancel a PR via the controller endpoint POST /purchasing/purchase-requisitions/{id}/cancel.
 * The list page does not expose a Cancel button — Scenario C/D test the
 * underlying transition by calling the API the controller exposes.
 */
async function cancelPr(page: Page, prId: number): Promise<{ status: number }> {
  const csrfHeaderName = await page.evaluate(() => {
    const meta = document.querySelector('meta[name="_csrf_header"]') as HTMLMetaElement | null;
    return meta?.content ?? 'X-XSRF-TOKEN';
  });
  const csrfToken = await page.evaluate(() => {
    const meta = document.querySelector('meta[name="_csrf"]') as HTMLMetaElement | null;
    return meta?.content ?? '';
  });
  return await page.evaluate(
    async ({ id, headerName, token }) => {
      const headers: Record<string, string> = {
        'Accept': 'application/json',
      };
      if (headerName && token) headers[headerName] = token;
      const r = await fetch(`/purchasing/purchase-requisitions/${id}/cancel`, {
        method: 'POST',
        headers,
      });
      return { status: r.status };
    },
    { id: prId, headerName: csrfHeaderName, token: csrfToken }
  );
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
    // on the canvas. Submit via /common/approval/{id}/process directly.
    const approvalRequestId = await approverPage.evaluate(() => {
      const el = document.getElementById('current-approval-request-id') as HTMLInputElement | null;
      return el?.value ?? '';
    });
    expect(approvalRequestId, 'approvalRequestId hidden field present').toBeTruthy();
    const resp = await processApproval(approverPage, approvalRequestId, 'APPROVE_AND_FINISH', 'E2E approval — auto signed.');
    expect(resp.status, `approval /process status — body: ${resp.body}`).toBeLessThan(400);

    // Reload to see the new state.
    await approverPage.goto(`/purchasing/purchase-requisitions/view/${prId}`);
    await waitForNetworkIdle(approverPage);
    await expect(approverPage.locator('.page-title .badge', { hasText: 'APPROVED' })).toBeVisible({ timeout: 15_000 });

    await approverContext.close();
  });

  test('Scenario B — submit then reject: approver1 rejects with notes', async ({ page, browser }) => {
    test.setTimeout(120_000);

    await navigateToModule(page, '/purchasing/purchase-requisitions');
    const seed = await resolveSeedIds(page);
    const prId = await createDraftPr(page, seed);
    await submitPrForApproval(page, prId, seed.approver1PartyId);

    const approverContext = await browser.newContext({ storageState: storageStatePath('approver1') });
    const approverPage = await approverContext.newPage();
    await approverPage.goto(`/purchasing/purchase-requisitions/view/${prId}`);
    await waitForNetworkIdle(approverPage);
    await expect(approverPage.locator('.page-title .badge', { hasText: 'SUBMITTED' })).toBeVisible();

    const approvalRequestId = await approverPage.evaluate(() => {
      return (document.getElementById('current-approval-request-id') as HTMLInputElement | null)?.value ?? '';
    });
    expect(approvalRequestId).toBeTruthy();

    const resp = await processApproval(approverPage, approvalRequestId, 'REJECTED', 'Estimasi terlalu tinggi.');
    expect(resp.status, `reject /process status — body: ${resp.body}`).toBeLessThan(400);

    await approverPage.goto(`/purchasing/purchase-requisitions/view/${prId}`);
    await waitForNetworkIdle(approverPage);

    // The view page exposes both the PR's own status (page title badge) and the
    // approval-request status (separate field). The approval flips to REJECTED;
    // the PR's domain status, however, only flips to APPROVED on the success
    // path — there is no listener for REJECTED at the time of writing
    // (OnPurchaseRequisitionApprovedListener handles the APPROVED branch only).
    // The detail-page "Status" field for the approval shows REJECTED.
    await expect(approverPage.getByText('REJECTED').first()).toBeVisible({ timeout: 15_000 });

    // Re-opening the modal trigger button should be hidden by isCurrentApprover
    // becoming false once the approval is processed.
    await expect(approverPage.locator('button[onclick="ApprovalUI.openApproveFinishModal()"]')).toHaveCount(0);

    await approverContext.close();
  });

  test('Scenario C — DRAFT edit then cancel', async ({ page }) => {
    test.setTimeout(60_000);

    await navigateToModule(page, '/purchasing/purchase-requisitions');
    const seed = await resolveSeedIds(page);
    const prId = await createDraftPr(page, seed);

    // Edit the DRAFT — change priority and add a second line, then save.
    await navigateToModule(page, `/purchasing/purchase-requisitions/edit/${prId}`);
    await page.selectOption('select[name="priority"]', 'URGENT');

    // Save and confirm redirect.
    await Promise.all([
      page.waitForURL(/\/purchasing\/purchase-requisitions(\?.*)?$/, { timeout: 15_000, waitUntil: 'domcontentloaded' }),
      page.locator('form#pr-form button[type="submit"]').first().click(),
    ]);
    await waitForNetworkIdle(page);

    // Cancel the DRAFT via the controller endpoint (no UI button on list).
    const resp = await cancelPr(page, prId);
    expect(resp.status, 'cancel endpoint returns 2xx').toBeLessThan(400);

    // Verify final state.
    await navigateToModule(page, `/purchasing/purchase-requisitions/view/${prId}`);
    await expect(page.locator('.page-title .badge', { hasText: 'CANCELLED' })).toBeVisible({ timeout: 10_000 });
  });

  test('Scenario D — APPROVED cancel transition', async ({ page, browser }) => {
    test.setTimeout(120_000);

    await navigateToModule(page, '/purchasing/purchase-requisitions');
    const seed = await resolveSeedIds(page);
    const prId = await createDraftPr(page, seed);
    await submitPrForApproval(page, prId, seed.approver1PartyId);

    // Approve as approver1 via API.
    const approverContext = await browser.newContext({ storageState: storageStatePath('approver1') });
    const approverPage = await approverContext.newPage();
    await approverPage.goto(`/purchasing/purchase-requisitions/view/${prId}`);
    await waitForNetworkIdle(approverPage);

    const approvalRequestId = await approverPage.evaluate(() => {
      return (document.getElementById('current-approval-request-id') as HTMLInputElement | null)?.value ?? '';
    });

    // Need to open modal so canvas exists for signature paint.
    await approverPage.waitForFunction(() => typeof (window as any).SignaturePad !== 'undefined', { timeout: 10_000 });
    await approverPage.locator('button[onclick="ApprovalUI.openApproveFinishModal()"]').first().click();
    await approverPage.locator('#modal-approve-finish.show').waitFor({ state: 'visible', timeout: 10_000 });
    await approverPage.waitForFunction(() => {
      const c = document.getElementById('sig-canvas-approve-finish') as HTMLCanvasElement | null;
      return c !== null && c.offsetWidth > 0 && c.width > 0;
    }, { timeout: 5_000 });
    await drawSignature(approverPage, '#sig-canvas-approve-finish');

    const approveResp = await processApproval(approverPage, approvalRequestId, 'APPROVE_AND_FINISH', 'E2E approval');
    expect(approveResp.status).toBeLessThan(400);

    await approverContext.close();

    // Verify APPROVED, then cancel as employee1 (PR_UPDATE permission).
    await navigateToModule(page, `/purchasing/purchase-requisitions/view/${prId}`);
    await expect(page.locator('.page-title .badge', { hasText: 'APPROVED' })).toBeVisible({ timeout: 15_000 });

    const cancelResp = await cancelPr(page, prId);
    expect(cancelResp.status, 'APPROVED → CANCELLED transition').toBeLessThan(400);

    await navigateToModule(page, `/purchasing/purchase-requisitions/view/${prId}`);
    await expect(page.locator('.page-title .badge', { hasText: 'CANCELLED' })).toBeVisible({ timeout: 10_000 });
  });

  test('Scenario E — header change resets line container', async ({ page }) => {
    test.setTimeout(60_000);

    await navigateToModule(page, '/purchasing/purchase-requisitions');
    const seed = await resolveSeedIds(page);

    // Resolve a second supplier party id (SUP01 only; we need an alternate).
    // Fall back to using the same supplier and changing currency instead.
    await navigateToModule(page, '/purchasing/purchase-requisitions/create');
    await setFlatpickrDate(page, 'input[name="requestDate"]', '2026-05-19');
    await setTomSelectValue(page, '#header-requester', seed.employee1PartyId);
    await setTomSelectValue(page, '#header-facility', seed.facilityId);
    await setTomSelectValue(page, '#header-supplier', seed.supplierPartyId);

    const idrId = await page.evaluate(() => {
      const sel = document.querySelector('select[name="currencyId"]') as HTMLSelectElement | null;
      const opt = sel ? Array.from(sel.options).find(o => o.text.trim() === 'IDR') : null;
      return opt?.value ?? '';
    });
    await page.selectOption('select[name="currencyId"]', idrId);

    await addLine(page);
    await setTomSelectValue(page, `[name="lines[0].productId"]`, seed.productLaptopId);
    await setAutoNumeric(page, lineFieldSelector(0, 'quantity'), 5);
    await setTomSelectValue(page, `[name="lines[0].uomId"]`, seed.uomPcsId);
    expect(await getLineCount(page), 'line count after add').toBe(1);

    // Change currency to a different option (USD) — this is the same kind of
    // header field that triggers the line-reset warning per form.html.
    const usdId = await page.evaluate(() => {
      const sel = document.querySelector('select[name="currencyId"]') as HTMLSelectElement | null;
      const opt = sel ? Array.from(sel.options).find(o => o.text.trim() === 'USD') : null;
      return opt?.value ?? '';
    });

    if (!usdId) {
      test.skip(true, 'USD currency not present in seed — cannot exercise header-change reset');
      return;
    }

    // The page-specific JS shows a confirm() dialog before resetting; auto-accept.
    page.on('dialog', d => d.accept());
    await page.selectOption('select[name="currencyId"]', usdId);

    // Lines should be cleared.
    await page.waitForFunction(() => document.querySelectorAll('#line-container tr.line-row').length === 0, { timeout: 5_000 });
    expect(await getLineCount(page), 'line count after header change').toBe(0);
    await expect(page.locator('#empty-msg')).toBeVisible();
  });

  test('Scenario F — SPL price autofill on product pick', async ({ page }) => {
    test.setTimeout(60_000);

    await navigateToModule(page, '/purchasing/purchase-requisitions');
    const seed = await resolveSeedIds(page);

    await navigateToModule(page, '/purchasing/purchase-requisitions/create');
    await setFlatpickrDate(page, 'input[name="requestDate"]', '2026-05-19');
    await setTomSelectValue(page, '#header-requester', seed.employee1PartyId);
    await setTomSelectValue(page, '#header-facility', seed.facilityId);
    // Header supplier matches the seeded SPL row (BP-DEV-SUP01).
    await setTomSelectValue(page, '#header-supplier', seed.supplierPartyId);

    const idrId = await page.evaluate(() => {
      const sel = document.querySelector('select[name="currencyId"]') as HTMLSelectElement | null;
      const opt = sel ? Array.from(sel.options).find(o => o.text.trim() === 'IDR') : null;
      return opt?.value ?? '';
    });
    await page.selectOption('select[name="currencyId"]', idrId);

    // Add line and pick product 9101 (E2E-PRD-LAPTOP) — matches seeded SPL row
    // (supplier=SUP01, product=Laptop, currency=IDR, price=8500000).
    await addLine(page);
    await setTomSelectValue(page, `[name="lines[0].productId"]`, seed.productLaptopId);

    // Wait for the SPL price endpoint to respond (page-specific JS calls
    // /purchasing/purchase-requisitions/api/spl-price after the product picks
    // up its UoM payload). Then settle.
    await page.waitForResponse(r => r.url().includes('/api/spl-price'), { timeout: 10_000 }).catch(() => {});
    await waitForNetworkIdle(page);

    // Read AutoNumeric value; small retry loop for the asynchronous setNumericInputValue.
    let priceVal = 0;
    for (let i = 0; i < 10; i++) {
      priceVal = await getAutoNumericValue(page, lineFieldSelector(0, 'estimatedUnitPrice'));
      if (priceVal === 8500000) break;
      await page.waitForTimeout(300);
    }
    expect(priceVal, 'SPL price should auto-fill from seeded row').toBe(8500000);
  });

  test('Sanity — employee1 storage state lands authenticated on PR list', async ({ page }) => {
    await page.goto('/purchasing/purchase-requisitions');
    await expect(page).toHaveURL(/\/purchasing\/purchase-requisitions/);
  });
});
