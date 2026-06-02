import { Page } from '@playwright/test';
import { test, expect, storageStatePath } from '../../fixtures/base';
import { navigateToModule } from '../../helpers/navigation';
import { setAutoNumeric } from '../../helpers/autonumeric';
import { setTomSelectValue } from '../../helpers/tomselect';

const GOODS_RECEIPT_ID = '9601';
const GOODS_RECEIPT_CODE = 'E2E-GR-RETURN-9601';
const PURCHASE_ORDER_CODE = 'E2E-PO-RETURN-9202';
const CONTAINER_A1 = 'E2E-CTN-A1';
const CONTAINER_A2 = 'E2E-CTN-A2';
const LAPTOP = 'E2E-PRD-LAPTOP';
const SERIAL_PRODUCT = 'E2E-PRD-SERIAL';
const MOVED_SERIAL = 'E2E-SER-MOVED-001';

async function waitForModal(page: Page, modalId: string, visible: boolean): Promise<void> {
  await page.waitForFunction(
    ({ id, expectedVisible }) => {
      const modal = document.getElementById(id);
      if (!modal) return !expectedVisible;
      const transitioning = modal.classList.contains('hiding') || modal.classList.contains('showing');
      return !transitioning && modal.classList.contains('show') === expectedVisible;
    },
    { id: modalId, expectedVisible: visible },
    { timeout: 10_000 }
  );
}

function sliceRow(page: Page, productCode: string, containerCode: string) {
  return page.locator(
    `#purchase-return-gr-line-selector-results tr[data-product-subtext="${productCode}"]` +
      `[data-container-subtext="${containerCode}"]`
  );
}

function formRow(page: Page, productCode: string, containerId: string) {
  return page
    .locator('#line-container tr.line-row')
    .filter({ has: page.locator(`input[name$=".productCode"][value="${productCode}"]`) })
    .filter({ has: page.locator(`input[name$=".containerId"][value="${containerId}"]`) });
}

async function openSliceSelector(page: Page): Promise<void> {
  await page.locator('#btn-add-line').click();
  await waitForModal(page, 'modal-purchase-return-gr-lines', true);
  await expect(page.locator('#purchase-return-gr-line-selector-results')).toContainText(LAPTOP);
}

async function applySlices(page: Page, slices: Array<[string, string]>): Promise<void> {
  for (const [productCode, containerCode] of slices) {
    await sliceRow(page, productCode, containerCode).locator('.js-purchase-return-gr-line-item').check();
  }
  await page.locator('#purchase-return-gr-line-selector-results .js-purchase-return-gr-line-apply').click();
  await waitForModal(page, 'modal-purchase-return-gr-lines', false);
}

async function selectMovedSerial(page: Page): Promise<void> {
  const serializedRow = formRow(page, SERIAL_PRODUCT, '9102');
  await serializedRow.locator('.btn-select-serials').click();
  await waitForModal(page, 'modal-purchase-return-serials', true);
  const result = page.locator('#purchase-return-serial-selector-results');
  await expect(result).toContainText(MOVED_SERIAL);
  await expect(result).toContainText('E2E Container A2 Moved');
  await result.locator('tr', { hasText: MOVED_SERIAL }).locator('.js-purchase-return-serial-item').check();
  await result.locator('.js-purchase-return-serial-apply').click();
  await waitForModal(page, 'modal-purchase-return-serials', false);
  await expect(serializedRow.locator('.input-serial-numbers')).toHaveValue(MOVED_SERIAL);
}

async function readCsrf(page: Page): Promise<{ headerName: string; token: string }> {
  return await page.evaluate(() => ({
    headerName: document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content') ?? 'X-CSRF-TOKEN',
    token: document.querySelector('meta[name="_csrf"]')?.getAttribute('content') ?? '',
  }));
}

async function processApproval(page: Page, approvalRequestId: string): Promise<{ status: number; body: string }> {
  const { headerName, token } = await readCsrf(page);
  return await page.evaluate(
    async ({ id, csrfHeader, csrfToken }) => {
      const headers: Record<string, string> = {
        'Content-Type': 'application/json',
        Accept: 'application/json',
      };
      if (csrfToken) headers[csrfHeader] = csrfToken;
      const canvas = document.getElementById('sig-canvas-approve-finish') as HTMLCanvasElement | null;
      const response = await fetch(`/common/approval/${id}/process`, {
        method: 'POST',
        headers,
        credentials: 'same-origin',
        body: JSON.stringify({
          action: 'APPROVE_AND_FINISH',
          notes: 'E2E approve purchase return',
          signatureBase64: canvas?.toDataURL('image/png'),
        }),
      });
      return { status: response.status, body: await response.text() };
    },
    { id: approvalRequestId, csrfHeader: headerName, csrfToken: token }
  );
}

async function selectorHtml(page: Page): Promise<string> {
  const response = await page.request.get(
    `/purchasing/purchase-returns/selectors/goods-receipt-lines?goodsReceiptId=${GOODS_RECEIPT_ID}`
  );
  expect(response.ok(), `selector endpoint status ${response.status()}`).toBeTruthy();
  return await response.text();
}

test.describe('Purchase Return Phase 1 flow', () => {
  test.use({ storageState: storageStatePath('employee1') });

  test('create, reserve, release, approve, and confirm reserved issue', async ({ page, browser }) => {
    test.setTimeout(180_000);

    // Setup probe deliberately uses APIRequestContext before any navigation.
    const approverLookup = await page.request.get('/api/lookup/parties/by-role-type?roleTypeCode=APPROVER&q=Budi');
    expect(approverLookup.ok()).toBeTruthy();
    const approverId = String((await approverLookup.json())?.[0]?.id ?? '');
    expect(approverId, 'approver lookup seed').toBeTruthy();

    await navigateToModule(page, '/purchasing/purchase-returns/select-source');
    await expect(page.locator('input[name="keyword"]')).toBeVisible();
    await expect(page.locator('select[name="supplierId"]')).toBeAttached();
    await expect(page.locator('select[name="purchaseOrderId"]')).toBeAttached();
    await page.locator('input[name="keyword"]').fill(GOODS_RECEIPT_CODE);
    await page.locator('form').first().press('Enter');
    await expect(page.locator(`a[href="/inventory/goods-receipts/${GOODS_RECEIPT_ID}"]`)).toBeVisible();
    await expect(page.locator('a[href="/purchasing/purchase-orders/view/9202"]')).toContainText(PURCHASE_ORDER_CODE);
    await page.locator(`a[href*="/purchasing/purchase-returns/create-from-reference?goodsReceiptId=${GOODS_RECEIPT_ID}"]`).click();

    await expect(page.locator('#purchase-return-form')).toBeVisible();
    await page.locator('#header-reason').selectOption('OTHER');

    // Remove then restore the first source slice to prove selector exclusion is reversible.
    await expect(page.locator('#line-container tr.line-row')).toHaveCount(3);
    await expect(formRow(page, LAPTOP, '9102')).toHaveCount(1);
    await formRow(page, LAPTOP, '9102').locator('.btn-remove-line').click();
    await expect(formRow(page, LAPTOP, '9102')).toHaveCount(0);
    await openSliceSelector(page);
    await expect(sliceRow(page, LAPTOP, CONTAINER_A2)).toHaveCount(1);
    await applySlices(page, [[LAPTOP, CONTAINER_A2]]);

    await setAutoNumeric(
      page,
      `#line-container tr.line-row:has(input[name$=".productCode"][value="${LAPTOP}"])` +
        ':has(input[name$=".containerId"][value="9101"]) .input-qty',
      2
    );
    await setAutoNumeric(
      page,
      `#line-container tr.line-row:has(input[name$=".productCode"][value="${LAPTOP}"])` +
        ':has(input[name$=".containerId"][value="9102"]) .input-qty',
      1
    );
    await selectMovedSerial(page);

    // OTHER must carry notes. First submit proves frontend rejection, then fill notes and save.
    await page.locator('#purchase-return-form button[type="submit"]').click();
    await expect(page.locator('#modal-global-warning.show')).toBeVisible();
    await expect(page.locator('#warning-modal-message')).toContainText(/note|catatan/i);
    await page.locator('#modal-global-warning button[data-bs-dismiss="modal"]').last().click();
    await waitForModal(page, 'modal-global-warning', false);
    await page.locator('#purchase-return-form textarea[name="note"]').fill('E2E OTHER reason note');
    for (const note of await page.locator('#line-container .input-line-note').all()) {
      await note.fill('E2E OTHER reason note');
    }

    await Promise.all([
      page.waitForURL(/\/purchasing\/purchase-returns(\?.*)?$/, { waitUntil: 'domcontentloaded' }),
      page.locator('#purchase-return-form button[type="submit"]').click(),
    ]);
    const detailLink = page.locator('a[href*="/purchasing/purchase-returns/view/"]').first();
    await expect(detailLink).toBeVisible();
    const detailHref = await detailLink.getAttribute('href');
    let purchaseReturnId = detailHref?.match(/\/view\/(\d+)/)?.[1] ?? '';
    expect(purchaseReturnId, 'created purchase return id').toBeTruthy();

    // List exposes View only, including for DRAFT rows.
    const actionCell = detailLink.locator('xpath=ancestor::td[1]');
    await expect(actionCell).toContainText(/view|lihat/i);
    await expect(actionCell.locator(`a[href*="/edit/${purchaseReturnId}"]`)).toHaveCount(0);

    await navigateToModule(page, `/purchasing/purchase-returns/view/${purchaseReturnId}`);
    await setTomSelectValue(page, '#purchase-return-approver', approverId, 'Budi Santoso');
    await page.locator(`[data-submit-url$="/${purchaseReturnId}/submit"]`).click();
    await page.locator('#confirm-modal-btn-yes').click();
    await page.waitForURL(new RegExp(`/purchasing/purchase-returns/view/${purchaseReturnId}`));
    await expect(page.locator('.page-header .badge')).toContainText(/submitted|diajukan/i);

    const reservedSelector = await selectorHtml(page);
    expect(reservedSelector).toContain(CONTAINER_A1);
    expect(reservedSelector).toContain(CONTAINER_A2);
    expect(reservedSelector).not.toContain(SERIAL_PRODUCT);

    await page.locator(`[data-action-url*="/${purchaseReturnId}/cancel-submission"]`).click();
    await page.locator('#confirm-modal-btn-yes').click();
    await page.waitForURL(new RegExp(`/purchasing/purchase-returns/view/${purchaseReturnId}`));
    await expect(page.locator('.page-header .badge')).toContainText(/cancelled|dibatalkan/i);
    expect(await selectorHtml(page)).toContain(SERIAL_PRODUCT);

    // Cancellation is final. Create a fresh draft from the released stock for approval and confirm.
    const cancelledPurchaseReturnId = purchaseReturnId;
    await navigateToModule(
      page,
      `/purchasing/purchase-returns/create-from-reference?goodsReceiptId=${GOODS_RECEIPT_ID}`
    );
    await expect(page.locator('#purchase-return-form')).toBeVisible();
    await page.locator('#header-reason').selectOption('QUALITY_ISSUE');
    await setAutoNumeric(
      page,
      `#line-container tr.line-row:has(input[name$=".productCode"][value="${LAPTOP}"])` +
        ':has(input[name$=".containerId"][value="9101"]) .input-qty',
      2
    );
    await setAutoNumeric(
      page,
      `#line-container tr.line-row:has(input[name$=".productCode"][value="${LAPTOP}"])` +
        ':has(input[name$=".containerId"][value="9102"]) .input-qty',
      1
    );
    await selectMovedSerial(page);
    await Promise.all([
      page.waitForURL(/\/purchasing\/purchase-returns(\?.*)?$/, { waitUntil: 'domcontentloaded' }),
      page.locator('#purchase-return-form button[type="submit"]').click(),
    ]);
    const confirmationDetailLink = page
      .locator(`a[href*="/purchasing/purchase-returns/view/"]:not([href$="/${cancelledPurchaseReturnId}"])`)
      .first();
    await expect(confirmationDetailLink).toBeVisible();
    const confirmationDetailHref = await confirmationDetailLink.getAttribute('href');
    purchaseReturnId = confirmationDetailHref?.match(/\/view\/(\d+)/)?.[1] ?? '';
    expect(purchaseReturnId, 'confirmation purchase return id').toBeTruthy();

    await navigateToModule(page, `/purchasing/purchase-returns/view/${purchaseReturnId}`);
    await setTomSelectValue(page, '#purchase-return-approver', approverId, 'Budi Santoso');
    await page.locator(`[data-submit-url$="/${purchaseReturnId}/submit"]`).click();
    await page.locator('#confirm-modal-btn-yes').click();
    await page.waitForURL(new RegExp(`/purchasing/purchase-returns/view/${purchaseReturnId}`));

    const approverContext = await browser.newContext({ storageState: storageStatePath('approver1') });
    const approverPage = await approverContext.newPage();
    await approverPage.goto(`/purchasing/purchase-returns/view/${purchaseReturnId}`, { waitUntil: 'domcontentloaded' });
    const approvalRequestId = await approverPage
      .locator('#current-approval-request-id')
      .inputValue();
    expect(approvalRequestId, 'approval request id').toBeTruthy();
    const approval = await processApproval(approverPage, approvalRequestId);
    expect(approval.status, approval.body).toBeLessThan(400);
    await approverContext.close();

    await navigateToModule(page, `/purchasing/purchase-returns/view/${purchaseReturnId}`);
    await expect(page.locator('.page-header .badge')).toContainText(/approved|disetujui/i);
    await page.locator(`[data-action-url$="/${purchaseReturnId}/confirm"]`).click();
    await page.locator('#confirm-modal-btn-yes').click();
    await page.waitForURL(new RegExp(`/purchasing/purchase-returns/view/${purchaseReturnId}`));
    await expect(page.locator('.page-header .badge')).toContainText(/confirmed|dikonfirmasi/i);

    const giLink = page.locator('a[href^="/inventory/goods-issues/"]');
    await expect(giLink).toBeVisible();
    const giHref = await giLink.getAttribute('href');
    expect(giHref).toBeTruthy();
    await navigateToModule(page, giHref!);
    await expect(page.locator('.page-title .badge')).toContainText('COMPLETED');
  });
});
