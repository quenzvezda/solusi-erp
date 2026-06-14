import { Browser, Page } from '@playwright/test';
import { test, expect, storageStatePath } from '../../fixtures/base';
import { navigateToModule } from '../../helpers/navigation';
import { setAutoNumeric } from '../../helpers/autonumeric';
import { setFlatpickrDate } from '../../helpers/flatpickr';
import { submitAjaxForm, submitAndExpectRedirect, waitForAjaxFormReady } from '../../helpers/form';
import { setTomSelectValue } from '../../helpers/tomselect';

const GOODS_RECEIPT_ID = '9601';
const GOODS_RECEIPT_CODE = 'E2E-GR-RETURN-9601';
const PURCHASE_ORDER_CODE = 'E2E-PO-RETURN-9202';
const CONTAINER_A1 = 'E2E-CTN-A1';
const CONTAINER_A2 = 'E2E-CTN-A2';
const LAPTOP = 'E2E-PRD-LAPTOP';
const SERIAL_PRODUCT = 'E2E-PRD-SERIAL';
const MOVED_SERIAL = 'E2E-SER-MOVED-001';

type ConfirmedPurchaseReturn = {
  id: string;
  code: string;
  goodsIssueHref: string;
  debitMemoHref: string;
  debitMemoCode: string;
  journalHref: string;
  journalTotal: string;
};

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

async function submitPurchaseReturnCreate(page: Page): Promise<number> {
  await waitForAjaxFormReady(page);
  await Promise.all([
    page.waitForURL(/\/purchasing\/purchase-returns(\?.*)?$/, { timeout: 20_000, waitUntil: 'domcontentloaded' }),
    page.locator('#purchase-return-form button[type="submit"]').click(),
  ]);

  await navigateToModule(page, '/purchasing/purchase-returns?sort=id,desc');
  const id = await page.evaluate(() => {
    const link = document.querySelector('table tbody a[href^="/purchasing/purchase-returns/view/"]') as HTMLAnchorElement | null;
    const match = link?.getAttribute('href')?.match(/\/view\/(\d+)/);
    return match ? Number(match[1]) : 0;
  });
  expect(id, 'created purchase return id from latest list row').toBeGreaterThan(0);
  return id;
}

async function removeLineIfPresent(page: Page, productCode: string, containerId: string): Promise<void> {
  const row = formRow(page, productCode, containerId);
  if (await row.count()) {
    await row.locator('.btn-remove-line').click({ force: true });
  }
}

function statusLabelPattern(status: string): RegExp {
  switch (status) {
    case 'OPEN':
      return /OPEN|Open/i;
    case 'COMPLETED':
      return /COMPLETED|Completed|Selesai/i;
    case 'CANCELLED':
      return /CANCELLED|Cancelled|Dibatalkan/i;
    default:
      return new RegExp(status.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'), 'i');
  }
}

async function readJournalTotals(page: Page): Promise<{ debit: string; credit: string }> {
  const totals = page.locator('table tfoot tr th.text-end.fw-bold');
  await expect(totals).toHaveCount(2);
  return {
    debit: (await totals.nth(0).innerText()).trim(),
    credit: (await totals.nth(1).innerText()).trim(),
  };
}

async function readJournalField(page: Page, labelPattern: string): Promise<string> {
  return await page.evaluate((pattern) => {
    const re = new RegExp(pattern, 'i');
    const labels = Array.from(document.querySelectorAll('label'));
    const label = labels.find((item) => re.test(item.textContent ?? ''));
    return label?.nextElementSibling?.textContent?.trim() ?? '';
  }, labelPattern);
}

async function createConfirmedPurchaseReturnForReversal(page: Page, browser: Browser): Promise<ConfirmedPurchaseReturn> {
  const approverLookup = await page.request.get('/api/lookup/parties/by-role-type?roleTypeCode=APPROVER&q=Budi');
  expect(approverLookup.ok()).toBeTruthy();
  const approverId = String((await approverLookup.json())?.[0]?.id ?? '');
  expect(approverId, 'approver lookup seed').toBeTruthy();

  await navigateToModule(
    page,
    `/purchasing/purchase-returns/create-from-reference?goodsReceiptId=${GOODS_RECEIPT_ID}`
  );
  await expect(page.locator('#purchase-return-form')).toBeVisible();
  await page.locator('#header-reason').selectOption('QUALITY_ISSUE');
  await removeLineIfPresent(page, LAPTOP, '9102');
  await removeLineIfPresent(page, SERIAL_PRODUCT, '9102');
  await expect(formRow(page, LAPTOP, '9101')).toHaveCount(1);
  await setAutoNumeric(
    page,
    `#line-container tr.line-row:has(input[name$=".productCode"][value="${LAPTOP}"])` +
      ':has(input[name$=".containerId"][value="9101"]) .input-qty',
    1
  );

  const purchaseReturnId = String(await submitPurchaseReturnCreate(page));
  await navigateToModule(page, `/purchasing/purchase-returns/view/${purchaseReturnId}`);
  await setTomSelectValue(page, '#purchase-return-approver', approverId, 'Budi Santoso');
  await page.locator(`[data-submit-url$="/${purchaseReturnId}/submit"]`).click();
  await page.locator('#confirm-modal-btn-yes').click();
  await page.waitForURL(new RegExp(`/purchasing/purchase-returns/view/${purchaseReturnId}`));
  await expect(page.locator('.page-header .badge')).toContainText(/submitted|diajukan/i);

  const approverContext = await browser.newContext({ storageState: storageStatePath('approver1') });
  const approverPage = await approverContext.newPage();
  await approverPage.goto(`/purchasing/purchase-returns/view/${purchaseReturnId}`, { waitUntil: 'domcontentloaded' });
  const approvalRequestId = await approverPage.locator('#current-approval-request-id').inputValue();
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

  const purchaseReturnCode = (await page.locator('.page-title span').first().innerText()).trim();
  const goodsIssueHref = await page.locator('a[href^="/inventory/goods-issues/"]').first().getAttribute('href');
  const debitMemoLink = page.locator('a[href^="/accounts-payable/debit-memos/"]').first();
  const debitMemoHref = await debitMemoLink.getAttribute('href');
  const debitMemoCode = (await debitMemoLink.innerText()).trim();
  expect(goodsIssueHref, 'generated goods issue href').toBeTruthy();
  expect(debitMemoHref, 'generated debit memo href').toBeTruthy();

  await navigateToModule(
    page,
    `/accounting/journal-entries?sourceType=PURCHASE_RETURN&sourceCode=${encodeURIComponent(purchaseReturnCode)}`
  );
  const journalRow = page
    .locator('table tbody tr')
    .filter({ hasText: purchaseReturnCode })
    .filter({ hasText: /purchase return|retur pembelian/i })
    .first();
  await expect(journalRow).toBeVisible();
  const journalHref = await journalRow.locator('td').first().locator('a').getAttribute('href');
  expect(journalHref, 'purchase return journal detail href').toBeTruthy();
  await navigateToModule(page, journalHref!);
  const journalTotals = await readJournalTotals(page);
  expect(journalTotals.debit).toBe(journalTotals.credit);
  expect(journalTotals.debit).not.toMatch(/^0([,.]0+)?$/);

  return {
    id: purchaseReturnId,
    code: purchaseReturnCode,
    goodsIssueHref: goodsIssueHref!,
    debitMemoHref: debitMemoHref!,
    debitMemoCode,
    journalHref: journalHref!,
    journalTotal: journalTotals.debit,
  };
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

    let purchaseReturnId = String(await submitPurchaseReturnCreate(page));
    const detailLink = page.locator(`a[href="/purchasing/purchase-returns/view/${purchaseReturnId}"]`).first();
    await expect(detailLink).toBeVisible();

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
    purchaseReturnId = String(await submitPurchaseReturnCreate(page));
    expect(purchaseReturnId, 'confirmation purchase return id').not.toBe(cancelledPurchaseReturnId);
    const confirmationDetailLink = page.locator(`a[href="/purchasing/purchase-returns/view/${purchaseReturnId}"]`).first();
    await expect(confirmationDetailLink).toBeVisible();

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
    const purchaseReturnCode = (await page.locator('.page-title span').first().innerText()).trim();
    expect(purchaseReturnCode, 'confirmed purchase return code').toBeTruthy();

    const giLink = page.locator('a[href^="/inventory/goods-issues/"]');
    await expect(giLink).toBeVisible();
    const giHref = await giLink.getAttribute('href');
    expect(giHref).toBeTruthy();

    const debitMemoLink = page.locator('a[href^="/accounts-payable/debit-memos/"]').first();
    await expect(debitMemoLink).toBeVisible({ timeout: 10_000 });
    const debitMemoHref = await debitMemoLink.getAttribute('href');
    const debitMemoCode = (await debitMemoLink.innerText()).trim();
    expect(debitMemoHref, 'debit memo detail href').toBeTruthy();
    expect(debitMemoCode, 'generated debit memo code').toMatch(/^DM-/);

    const adminContext = await browser.newContext({ storageState: storageStatePath('admin') });
    const adminPage = await adminContext.newPage();
    await navigateToModule(adminPage, debitMemoHref!);
    await expect(adminPage.locator('.page-header .badge')).toContainText(statusLabelPattern('OPEN'));
    await expect(adminPage.locator('.page-title')).toContainText(debitMemoCode);
    await expect(adminPage.locator(`a[href="/purchasing/purchase-returns/view/${purchaseReturnId}"]`))
      .toContainText(purchaseReturnCode);
    await expect(adminPage.locator(`a[href="${giHref}"]`)).toBeVisible();
    await expect(adminPage.locator('body')).toContainText(/Gross/);
    await expect(adminPage.locator('body')).toContainText(/Remaining|Sisa/);
    const debitMemoLineTable = adminPage.locator('table')
      .filter({ has: adminPage.locator('th', { hasText: /DPP/ }) })
      .first();
    await expect(debitMemoLineTable.locator('tbody tr').first()).toBeVisible();
    await expect(debitMemoLineTable.locator('tbody')).not.toContainText(/No debit memo lines|Tidak ada line debit memo/i);

    const supplierMemoNumber = `E2E-DM-SUP-${Date.now()}`;
    const taxDocumentNumber = `E2E-DM-TAX-${Date.now()}`;
    await adminPage.locator('input[name="supplierMemoNumber"]').fill(supplierMemoNumber);
    await setFlatpickrDate(adminPage, 'input[name="supplierMemoDate"]', '2026-06-04');
    await adminPage.locator('input[name="taxDocumentNumber"]').fill(taxDocumentNumber);
    await setFlatpickrDate(adminPage, 'input[name="taxDocumentDate"]', '2026-06-05');
    await adminPage.locator('textarea[name="notes"]').fill('E2E debit memo metadata update');
    await submitAjaxForm(adminPage);
    await adminPage.reload({ waitUntil: 'domcontentloaded' });
    await expect(adminPage.locator('input[name="supplierMemoNumber"]')).toHaveValue(supplierMemoNumber);
    await expect(adminPage.locator('input[name="taxDocumentNumber"]')).toHaveValue(taxDocumentNumber);
    await expect(adminPage.locator('textarea[name="notes"]')).toHaveValue('E2E debit memo metadata update');
    await adminContext.close();

    await navigateToModule(
      page,
      `/accounting/journal-entries?sourceType=PURCHASE_RETURN&sourceCode=${encodeURIComponent(purchaseReturnCode)}`
    );
    const journalRow = page
      .locator('table tbody tr')
      .filter({ hasText: purchaseReturnCode })
      .filter({ hasText: /purchase return|retur pembelian/i })
      .first();
    await expect(journalRow).toBeVisible();
    await expect(journalRow).toContainText(/POSTED/);
    const journalHref = await journalRow.locator('td').first().locator('a').getAttribute('href');
    expect(journalHref, 'purchase return journal detail href').toBeTruthy();

    await navigateToModule(page, journalHref!);
    await expect(page.locator('a[href^="/purchasing/purchase-returns/view/"]')).toContainText(purchaseReturnCode);
    const journalTotals = page.locator('table tfoot tr th.text-end.fw-bold');
    await expect(journalTotals).toHaveCount(2);
    const totalDebit = (await journalTotals.nth(0).innerText()).trim();
    const totalCredit = (await journalTotals.nth(1).innerText()).trim();
    expect(totalDebit).toBe(totalCredit);
    expect(totalDebit).not.toMatch(/^0([,.]0+)?$/);

    await navigateToModule(page, giHref!);
    await expect(page.locator('.page-title .badge')).toContainText(statusLabelPattern('COMPLETED'));
  });

  test('reverses confirmed purchase return and cancels generated documents', async ({ page, browser }) => {
    test.setTimeout(180_000);

    const confirmed = await createConfirmedPurchaseReturnForReversal(page, browser);

    const adminContext = await browser.newContext({ storageState: storageStatePath('admin') });
    const adminPage = await adminContext.newPage();

    await navigateToModule(adminPage, `/purchasing/purchase-returns/${confirmed.id}/reverse`);
    await expect(adminPage.locator('#purchase-return-reverse-form')).toBeVisible();
    await expect(adminPage.locator('#purchase-return-reverse-lines')).toContainText(LAPTOP);
    const reverseLineCount = await adminPage.locator('#purchase-return-reverse-lines tbody tr').count();
    expect(reverseLineCount, 'reverse line count').toBeGreaterThan(0);
    await adminPage.waitForFunction(() => {
      const selects = Array.from(document.querySelectorAll('[data-pr-reverse-target-container-select]')) as HTMLSelectElement[];
      return selects.length > 0 && selects.every((select: any) => {
        return select.tomselect ? Boolean(select.tomselect.getValue()) : Boolean(select.value);
      });
    });
    await setFlatpickrDate(adminPage, '#reversal-date', '2026-06-06');
    await adminPage.locator('#reversal-reason').fill('E2E reverse confirmed purchase return');
    await submitAndExpectRedirect(
      adminPage,
      new RegExp(`/purchasing/purchase-returns/view/${confirmed.id}(\\?.*)?$`)
    );
    await expect(adminPage.locator('.page-header .badge')).toContainText(/reversed|direversal/i);
    await expect(adminPage.locator('.purchase-return-view-content')).toContainText('E2E reverse confirmed purchase return');
    const reversalJournalHref = await adminPage.locator('a[href^="/accounting/journal-entries/"]').first().getAttribute('href');
    expect(reversalJournalHref, 'purchase return reversal journal href').toBeTruthy();

    await navigateToModule(adminPage, confirmed.goodsIssueHref);
    await expect(adminPage.locator('.page-title .badge')).toContainText(statusLabelPattern('CANCELLED'));
    await expect(adminPage.getByRole('link', { name: /Reversal Journal|Jurnal Reversal/i })).toBeVisible();
    await expect(adminPage.locator(`a[href="${reversalJournalHref}"]`)).toBeVisible();

    await navigateToModule(adminPage, confirmed.debitMemoHref);
    await expect(adminPage.locator('.page-header .badge')).toContainText(statusLabelPattern('CANCELLED'));
    await expect(adminPage.locator('.page-title')).toContainText(confirmed.debitMemoCode);
    await expect(adminPage.locator('a[href*="/accounts-payable/debit-memo-allocations/create"]')).toHaveCount(0);
    await expect(adminPage.locator('a[href^="/accounts-payable/debit-memo-allocations/"]')).toHaveCount(0);
    await expect(adminPage.locator('.debit-memo-detail-content')).toContainText(/No allocations yet|Belum ada alokasi/i);

    await navigateToModule(adminPage, confirmed.journalHref);
    const reversedBy = await readJournalField(adminPage, 'Reversed By|Dibalik Oleh');
    expect(reversedBy, 'original journal reversed-by code').toMatch(/^JNL-/);
    const originalTotals = await readJournalTotals(adminPage);
    expect(originalTotals.debit).toBe(originalTotals.credit);
    expect(originalTotals.debit).toBe(confirmed.journalTotal);

    await navigateToModule(adminPage, reversalJournalHref!);
    const reversalOf = await readJournalField(adminPage, 'Reversal Of|Reversal Dari');
    expect(reversalOf, 'reversal journal source link code').toMatch(/^JNL-/);
    const reversalTotals = await readJournalTotals(adminPage);
    expect(reversalTotals.debit).toBe(reversalTotals.credit);
    expect(reversalTotals.debit).toBe(confirmed.journalTotal);

    await adminContext.close();
  });
});
