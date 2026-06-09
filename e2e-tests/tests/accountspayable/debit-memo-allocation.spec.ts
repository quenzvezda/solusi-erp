import { Browser, BrowserContext, Page } from '@playwright/test';
import { test, expect, storageStatePath } from '../../fixtures/base';
import { setAutoNumeric } from '../../helpers/autonumeric';
import { setFlatpickrDate } from '../../helpers/flatpickr';
import { navigateToModule } from '../../helpers/navigation';
import { setTomSelectValue } from '../../helpers/tomselect';
import { waitForNetworkIdle } from '../../helpers/waits';

const PO_ID = '9201';
const PO_LINE_LAPTOP_ID = '9201';
const CONTAINER_ID = '9101';
const FULL_AMOUNT = 8_500_000;

type ConfirmedVendorBill = {
  id: number;
  code: string;
  invoiceNumber: string;
};

type DebitMemo = {
  id: number;
  code: string;
  href: string;
  purchaseReturnId: number;
  purchaseReturnCode: string;
  purchaseReturnHref: string;
  goodsIssueHref: string;
};

type DebitMemoAllocation = {
  id: number;
  code: string;
};

async function waitForBootstrapModalClosed(page: Page, modalId: string): Promise<void> {
  await page.waitForFunction(
    (id) => {
      const el = document.getElementById(id);
      if (!el) return true;
      return !el.classList.contains('show') && !el.classList.contains('hiding') && !el.classList.contains('showing');
    },
    modalId,
    { timeout: 10_000 }
  );
}

async function waitForAjaxFormReady(page: Page, formSelector: string): Promise<void> {
  await page.waitForFunction(
    (selector) => document.querySelector(selector)?.classList.contains('ajax-initialized') === true,
    formSelector,
    { timeout: 10_000 }
  );
}

async function navigateToModuleCommit(page: Page, url: string): Promise<void> {
  await page.goto(url, { waitUntil: 'commit', timeout: 60_000 });
  await page.waitForLoadState('domcontentloaded', { timeout: 15_000 }).catch(() => {});
  await page.waitForLoadState('networkidle', { timeout: 5_000 }).catch(() => {});
}

async function submitAjaxFormForData<T>(
  page: Page,
  formSelector: string,
  urlPart: string | RegExp
): Promise<T> {
  await waitForAjaxFormReady(page, formSelector);
  const responsePromise = page.waitForResponse((response) => {
    const matchesUrl = typeof urlPart === 'string' ? response.url().includes(urlPart) : urlPart.test(response.url());
    return matchesUrl && response.request().method() !== 'GET';
  });

  await page.locator(`${formSelector} button[type="submit"]`).first().click();
  const response = await responsePromise;
  const body = await response.json();
  expect(response.ok(), JSON.stringify(body)).toBeTruthy();
  expect(body.success, JSON.stringify(body)).toBeTruthy();
  return body.data as T;
}

async function findRowIndexByReferenceLineId(page: Page, referenceLineId: string): Promise<number> {
  return page.evaluate((refId) => {
    const rows = Array.from(document.querySelectorAll('#line-container tr.line-row'));
    return rows.findIndex((row) => {
      const input = row.querySelector('input[name$=".referenceLineId"]') as HTMLInputElement | null;
      return input?.value === refId;
    });
  }, referenceLineId);
}

async function keepOnlyReferenceLine(page: Page, referenceLineId: string): Promise<number> {
  const removeIndexes = await page.evaluate((refId) => {
    const rows = Array.from(document.querySelectorAll('#line-container tr.line-row'));
    return rows
      .map((row, index) => {
        const input = row.querySelector('input[name$=".referenceLineId"]') as HTMLInputElement | null;
        return input?.value === refId ? -1 : index;
      })
      .filter((index) => index >= 0)
      .reverse();
  }, referenceLineId);

  for (const index of removeIndexes) {
    await page.locator('#line-container tr.line-row').nth(index).locator('.btn-remove-line').click();
  }

  const rowIndex = await findRowIndexByReferenceLineId(page, referenceLineId);
  if (rowIndex < 0) throw new Error(`PO line ${referenceLineId} was not present after cleanup`);
  await expect(page.locator('#line-container tr.line-row')).toHaveCount(1, { timeout: 5_000 });
  return rowIndex;
}

async function pickPoLineFromModalSelector(page: Page): Promise<number> {
  const existingIndex = await findRowIndexByReferenceLineId(page, PO_LINE_LAPTOP_ID);
  if (existingIndex >= 0) return keepOnlyReferenceLine(page, PO_LINE_LAPTOP_ID);

  const prevCount = await page.locator('#line-container tr.line-row').count();
  await page.locator('#btn-add-line').click();
  await expect(page.locator('#modal-gr-po-line-selector.show')).toBeVisible({ timeout: 10_000 });
  await expect(page.locator('#gr-po-line-selector-results')).toContainText('E2E-PRD-LAPTOP', { timeout: 10_000 });

  const row = page.locator('#gr-po-line-selector-results tbody tr', { hasText: 'E2E-PRD-LAPTOP' }).first();
  await row.locator('.js-gr-po-line-selector-item').check();
  await page.locator('#gr-po-line-selector-results .js-gr-po-line-selector-apply').click();
  await waitForBootstrapModalClosed(page, 'modal-gr-po-line-selector');
  await expect(page.locator('#line-container tr.line-row')).toHaveCount(prevCount + 1, { timeout: 5_000 });
  return keepOnlyReferenceLine(page, PO_LINE_LAPTOP_ID);
}

async function setQuantityViaDrawer(page: Page, rowIndex: number, qty: number): Promise<void> {
  await page.locator('#line-container tr.line-row').nth(rowIndex).locator('.btn-edit-detail').click();
  const drawer = page.locator('#drawer-non-serial');
  await expect(drawer).toBeVisible({ timeout: 5_000 });
  await page.waitForFunction(() => {
    const select = document.querySelector('#drawer-non-serial .select-uom-target') as HTMLSelectElement | null;
    return select != null && select.options.length > 0;
  });
  await setAutoNumeric(page, '#drawer-non-serial .input-qty-target', qty);
  await drawer.locator('.btn-save-drawer').click();
  await waitForBootstrapModalClosed(page, 'drawer-non-serial');
}

async function createCompletedGoodsReceipt(page: Page): Promise<number> {
  await navigateToModule(page, `/inventory/goods-receipts/create?referenceType=PURCHASE_ORDER&referenceId=${PO_ID}`);
  await expect(page.locator('#gr-form')).toBeVisible({ timeout: 10_000 });

  await setFlatpickrDate(page, 'input[name="receiptDate"]', '2026-06-05');
  const rowIndex = await pickPoLineFromModalSelector(page);
  await setTomSelectValue(
    page,
    `#line-container tr.line-row:nth-of-type(${rowIndex + 1}) select.select-container`,
    CONTAINER_ID
  );
  await setQuantityViaDrawer(page, rowIndex, 1);

  const dataPromise = submitAjaxFormForData<{ id: number }>(page, '#gr-form', '/inventory/goods-receipts');
  await page.waitForURL(/\/inventory\/goods-receipts(\?.*)?$/, { timeout: 20_000, waitUntil: 'domcontentloaded' });
  await waitForNetworkIdle(page);
  const data = await dataPromise;

  await navigateToModule(page, `/inventory/goods-receipts/edit/${data.id}`);
  await expect(page.locator('#btn-complete')).toBeVisible({ timeout: 10_000 });
  await page.locator('#btn-complete').click();
  const responsePromise = page.waitForResponse((response) =>
    response.url().includes(`/inventory/goods-receipts/${data.id}/complete`) &&
    response.request().method() === 'POST'
  );
  await page.locator('#confirm-modal-btn-yes').click();
  const response = await responsePromise;
  expect(response.ok()).toBeTruthy();
  await page.waitForURL(new RegExp(`/inventory/goods-receipts/${data.id}(\\?.*)?$`), {
    timeout: 20_000,
    waitUntil: 'domcontentloaded',
  });
  await expect(page.locator('.page-title .badge', { hasText: 'COMPLETED' })).toBeVisible({ timeout: 10_000 });
  return data.id;
}

async function createConfirmedVendorBillFromGoodsReceipt(page: Page, grId: number): Promise<ConfirmedVendorBill> {
  const invoiceNumber = `E2E-VB-DMA-${Date.now()}-${grId}`;

  await navigateToModule(page, '/accounts-payable/vendor-bills/select-references');
  const checkbox = page.locator(`.js-reference-checkbox[value="${grId}"]`);
  await expect(checkbox).toBeVisible({ timeout: 10_000 });
  await checkbox.check();

  await Promise.all([
    page.waitForURL(/\/accounts-payable\/vendor-bills\/create(\?.*)?$/, {
      timeout: 15_000,
      waitUntil: 'domcontentloaded',
    }),
    page.locator('#btn-continue-references').click(),
  ]);

  await expect(page.locator('#vendor-bill-form')).toBeVisible({ timeout: 10_000 });
  await expect(page.locator('#vendor-bill-lines-body tr.vendor-bill-line-row')).toHaveCount(1, { timeout: 10_000 });
  await page.locator('input[name="vendorInvoiceNumber"]').fill(invoiceNumber);
  await setFlatpickrDate(page, 'input[name="billDate"]', '2026-06-05');
  await setFlatpickrDate(page, 'input[name="dueDate"]', '2026-06-20');

  const dataPromise = submitAjaxFormForData<void>(page, '#vendor-bill-form', '/accounts-payable/vendor-bills');
  await page.waitForURL(/\/accounts-payable\/vendor-bills(\?.*)?$/, { timeout: 20_000, waitUntil: 'domcontentloaded' });
  await waitForNetworkIdle(page);
  await dataPromise;

  await navigateToModule(page, `/accounts-payable/vendor-bills?keyword=${encodeURIComponent(invoiceNumber)}`);
  await expect(page.locator('#vendor-bill-table-container')).toContainText(invoiceNumber, { timeout: 10_000 });

  const id = await page.evaluate((invoice) => {
    const rows = Array.from(document.querySelectorAll('#vendor-bill-table-container tbody tr'));
    const row = rows.find((candidate) => candidate.textContent?.includes(invoice));
    const link = row?.querySelector('a[href*="/accounts-payable/vendor-bills/"]') as HTMLAnchorElement | null;
    const match = link?.href.match(/\/accounts-payable\/vendor-bills\/(\d+)/);
    return match ? Number(match[1]) : 0;
  }, invoiceNumber);
  if (!id) throw new Error(`createConfirmedVendorBillFromGoodsReceipt: could not determine bill id for ${invoiceNumber}`);

  await navigateToModule(page, `/accounts-payable/vendor-bills/${id}`);
  await expect(page.locator('#btn-confirm-vendor-bill')).toBeVisible({ timeout: 10_000 });
  await page.locator('#btn-confirm-vendor-bill').click();
  const responsePromise = page.waitForResponse((response) =>
    response.url().includes(`/accounts-payable/vendor-bills/${id}/confirm`) &&
    response.request().method() === 'POST'
  );
  await page.locator('#confirm-modal-btn-yes').click();
  const response = await responsePromise;
  expect(response.ok()).toBeTruthy();
  await page.waitForURL(new RegExp(`/accounts-payable/vendor-bills/${id}(\\?.*)?$`), {
    timeout: 20_000,
    waitUntil: 'domcontentloaded',
  });
  await expect(page.locator('.page-title .badge', { hasText: 'CONFIRMED' })).toBeVisible({ timeout: 10_000 });
  const code = (await page.locator('.page-title span').first().innerText()).trim();
  return { id, code, invoiceNumber };
}

async function createConfirmedVendorBill(page: Page): Promise<ConfirmedVendorBill> {
  const grId = await createCompletedGoodsReceipt(page);
  return createConfirmedVendorBillFromGoodsReceipt(page, grId);
}

async function processApproval(page: Page, approvalRequestId: string): Promise<void> {
  const response = await page.evaluate(async (id) => {
    const headerName = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content') ?? 'X-CSRF-TOKEN';
    const token = document.querySelector('meta[name="_csrf"]')?.getAttribute('content') ?? '';
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
      Accept: 'application/json',
    };
    if (token) headers[headerName] = token;
    const canvas = document.getElementById('sig-canvas-approve-finish') as HTMLCanvasElement | null;
    const res = await fetch(`/common/approval/${id}/process`, {
      method: 'POST',
      credentials: 'same-origin',
      headers,
      body: JSON.stringify({
        action: 'APPROVE_AND_FINISH',
        notes: 'E2E approve purchase return for DMA',
        signatureBase64: canvas?.toDataURL('image/png'),
      }),
    });
    return { status: res.status, body: await res.text() };
  }, approvalRequestId);
  expect(response.status, response.body).toBeLessThan(400);
}

async function createDebitMemoFromGoodsReceipt(browser: Browser, goodsReceiptId: number): Promise<DebitMemo> {
  const employeeContext = await browser.newContext({ storageState: storageStatePath('employee1') });
  const employeePage = await employeeContext.newPage();
  try {
    await navigateToModule(employeePage, `/purchasing/purchase-returns/create-from-reference?goodsReceiptId=${goodsReceiptId}`);
    await expect(employeePage.locator('#purchase-return-form')).toBeVisible({ timeout: 10_000 });
    await setFlatpickrDate(employeePage, 'input[name="returnDate"]', '2026-06-05');
    await employeePage.locator('#header-reason').selectOption('QUALITY_ISSUE');
    await expect(employeePage.locator('#line-container tr.line-row')).toHaveCount(1, { timeout: 10_000 });
    await setAutoNumeric(employeePage, '#line-container tr.line-row:first-child .input-qty', 1);
    await employeePage.locator('#line-container tr.line-row:first-child .input-line-reason').selectOption('QUALITY_ISSUE');
    await employeePage.locator('#purchase-return-form textarea[name="note"]').fill('E2E purchase return for debit memo allocation');
    await employeePage.locator('#line-container tr.line-row:first-child .input-line-note').fill('E2E DMA return line');

    const createDataPromise = submitAjaxFormForData<{ id: number }>(
      employeePage,
      '#purchase-return-form',
      '/purchasing/purchase-returns/create'
    );
    await employeePage.waitForURL(/\/purchasing\/purchase-returns(\?.*)?$/, {
      timeout: 20_000,
      waitUntil: 'domcontentloaded',
    });
    const created = await createDataPromise;

    await navigateToModule(employeePage, `/purchasing/purchase-returns/view/${created.id}`);
    const approverLookup = await employeePage.request.get('/api/lookup/parties/by-role-type?roleTypeCode=APPROVER&q=Budi');
    expect(approverLookup.ok()).toBeTruthy();
    const approverId = String((await approverLookup.json())?.[0]?.id ?? '');
    expect(approverId, 'approver lookup seed').toBeTruthy();

    await setTomSelectValue(employeePage, '#purchase-return-approver', approverId, 'Budi Santoso');
    await employeePage.locator(`[data-submit-url$="/${created.id}/submit"]`).click();
    const submitResponsePromise = employeePage.waitForResponse((response) =>
      response.url().includes(`/purchasing/purchase-returns/${created.id}/submit`) &&
      response.request().method() === 'POST'
    );
    await employeePage.locator('#confirm-modal-btn-yes').click();
    const submitResponse = await submitResponsePromise;
    expect(submitResponse.ok()).toBeTruthy();
    await employeePage.waitForURL(new RegExp(`/purchasing/purchase-returns/view/${created.id}`), {
      timeout: 20_000,
      waitUntil: 'domcontentloaded',
    });
    await expect(employeePage.locator('.page-header .badge')).toContainText(/submitted|diajukan/i);

    const approverContext = await browser.newContext({ storageState: storageStatePath('approver1') });
    const approverPage = await approverContext.newPage();
    try {
      await navigateToModule(approverPage, `/purchasing/purchase-returns/view/${created.id}`);
      const approvalRequestId = await approverPage.locator('#current-approval-request-id').getAttribute('value');
      expect(approvalRequestId, 'approval request id').toBeTruthy();
      await processApproval(approverPage, approvalRequestId!);
    } finally {
      await approverContext.close();
    }

    await navigateToModule(employeePage, `/purchasing/purchase-returns/view/${created.id}`);
    await expect(employeePage.locator('.page-header .badge')).toContainText(/approved|disetujui/i);
    await employeePage.locator(`[data-action-url$="/${created.id}/confirm"]`).click();
    const confirmResponsePromise = employeePage.waitForResponse((response) =>
      response.url().includes(`/purchasing/purchase-returns/${created.id}/confirm`) &&
      response.request().method() === 'POST'
    );
    await employeePage.locator('#confirm-modal-btn-yes').click();
    const confirmResponse = await confirmResponsePromise;
    expect(confirmResponse.ok()).toBeTruthy();
    await employeePage.waitForURL(new RegExp(`/purchasing/purchase-returns/view/${created.id}`), {
      timeout: 30_000,
      waitUntil: 'domcontentloaded',
    });
    await expect(employeePage.locator('.page-header .badge')).toContainText(/confirmed|dikonfirmasi/i);

    const purchaseReturnCode = (await employeePage.locator('.page-title span').first().innerText()).trim();
    const goodsIssueHref = await employeePage.locator('a[href^="/inventory/goods-issues/"]').first().getAttribute('href');
    expect(goodsIssueHref, 'generated goods issue href').toBeTruthy();
    const debitMemoLink = employeePage.locator('a[href^="/accounts-payable/debit-memos/"]').first();
    await expect(debitMemoLink).toBeVisible({ timeout: 15_000 });
    const href = await debitMemoLink.getAttribute('href');
    const code = (await debitMemoLink.innerText()).trim();
    const id = Number(href?.match(/\/debit-memos\/(\d+)/)?.[1] ?? 0);
    expect(id, 'generated debit memo id').toBeGreaterThan(0);
    expect(code, 'generated debit memo code').toMatch(/^DM-/);
    return {
      id,
      code,
      href: href!,
      purchaseReturnId: created.id,
      purchaseReturnCode,
      purchaseReturnHref: `/purchasing/purchase-returns/view/${created.id}`,
      goodsIssueHref: goodsIssueHref!,
    };
  } finally {
    await employeeContext.close();
  }
}

async function createDebitMemoWithOptionalBill(
  browser: Browser,
  adminPage: Page,
  createBill = true
): Promise<{ debitMemo: DebitMemo; vendorBill?: ConfirmedVendorBill }> {
  const grId = await createCompletedGoodsReceipt(adminPage);
  const vendorBill = createBill ? await createConfirmedVendorBillFromGoodsReceipt(adminPage, grId) : undefined;
  const debitMemo = await createDebitMemoFromGoodsReceipt(browser, grId);
  await navigateToModule(adminPage, debitMemo.href);
  await expect(adminPage.locator('.page-header .badge')).toContainText('OPEN', { timeout: 10_000 });
  return { debitMemo, vendorBill };
}

async function chooseVendorBill(page: Page, bill: ConfirmedVendorBill): Promise<void> {
  const responsePromise = page.waitForResponse((response) =>
    response.url().includes('/accounts-payable/debit-memo-allocations/selectors/vendor-bills') &&
    response.request().method() === 'GET'
  );
  await page.locator('#btn-select-vendor-bill').click();
  const response = await responsePromise;
  expect(response.ok()).toBeTruthy();
  await expect(page.locator('#vendor-bill-selector-modal.show')).toBeVisible({ timeout: 10_000 });
  const row = page.locator('#vendor-bill-selector-body tr.js-dma-vendor-bill-option', { hasText: bill.code }).first();
  await expect(row).toBeVisible({ timeout: 10_000 });
  await row.click();
  await waitForBootstrapModalClosed(page, 'vendor-bill-selector-modal');
  await expect(page.locator(`#dma-lines-body tr[data-vendor-bill-id="${bill.id}"]`)).toBeVisible({ timeout: 5_000 });
}

async function chooseDebitMemo(page: Page, debitMemo: DebitMemo): Promise<void> {
  const responsePromise = page.waitForResponse((response) =>
    response.url().includes('/accounts-payable/debit-memo-allocations/selectors/debit-memos') &&
    response.request().method() === 'GET'
  );
  await page.locator('#btn-select-debit-memo').click();
  const response = await responsePromise;
  expect(response.ok()).toBeTruthy();
  await expect(page.locator('#debit-memo-selector-modal.show')).toBeVisible({ timeout: 10_000 });
  const row = page.locator('#debit-memo-selector-body tr.js-dma-debit-memo-option', { hasText: debitMemo.code }).first();
  await expect(row).toBeVisible({ timeout: 10_000 });
  await row.click();
  await waitForBootstrapModalClosed(page, 'debit-memo-selector-modal');
  await expect(page.locator('#debitMemoDisplay')).toHaveValue(debitMemo.code, { timeout: 5_000 });
}

async function createDraftDmaFromDebitMemo(
  page: Page,
  debitMemo: DebitMemo,
  bills: ConfirmedVendorBill[],
  amounts?: number[]
): Promise<DebitMemoAllocation> {
  await navigateToModule(page, debitMemo.href);
  await page.locator('a[href*="/accounts-payable/debit-memo-allocations/create"]').first().click();
  await expect(page.locator('#debit-memo-allocation-form')).toBeVisible({ timeout: 60_000 });
  await setFlatpickrDate(page, 'input[name="allocationDate"]', '2026-06-05');

  for (const bill of bills) {
    await chooseVendorBill(page, bill);
  }

  if (amounts) {
    for (let index = 0; index < amounts.length; index += 1) {
      await setAutoNumeric(page, `#dma-lines-body tr.dma-line-row:nth-of-type(${index + 1}) .dma-applied-input`, amounts[index]);
    }
  }
  await page.locator('input[name="notes"]').fill('E2E debit memo allocation');

  const dataPromise = submitAjaxFormForData<DebitMemoAllocation>(
    page,
    '#debit-memo-allocation-form',
    '/accounts-payable/debit-memo-allocations'
  );
  await page.waitForURL(/\/accounts-payable\/debit-memo-allocations(\?.*)?$/, {
    timeout: 20_000,
    waitUntil: 'domcontentloaded',
  });
  const data = await dataPromise;
  expect(data.id, 'DMA id').toBeGreaterThan(0);
  return data;
}

async function createDraftDmaFromVendorBillShortcut(
  page: Page,
  debitMemo: DebitMemo,
  bill: ConfirmedVendorBill,
  amount: number
): Promise<DebitMemoAllocation> {
  await navigateToModule(page, `/accounts-payable/vendor-bills/${bill.id}`);
  const shortcut = page.locator('a[href*="/accounts-payable/debit-memo-allocations/create"][href*="vendorBillId"]').first();
  await expect(shortcut).toBeVisible({ timeout: 10_000 });
  await shortcut.click();
  await expect(page.locator('#debit-memo-allocation-form')).toBeVisible({ timeout: 60_000 });
  await expect(page.locator(`#dma-lines-body tr[data-vendor-bill-id="${bill.id}"]`)).toBeVisible({ timeout: 10_000 });

  await chooseDebitMemo(page, debitMemo);
  await setFlatpickrDate(page, 'input[name="allocationDate"]', '2026-06-05');
  await setAutoNumeric(page, '#dma-lines-body tr:first-child .dma-applied-input', amount);
  await page.locator('input[name="notes"]').fill('E2E DMA from vendor bill shortcut');

  const dataPromise = submitAjaxFormForData<DebitMemoAllocation>(
    page,
    '#debit-memo-allocation-form',
    '/accounts-payable/debit-memo-allocations'
  );
  await page.waitForURL(/\/accounts-payable\/debit-memo-allocations(\?.*)?$/, {
    timeout: 20_000,
    waitUntil: 'domcontentloaded',
  });
  return dataPromise;
}

async function confirmDma(page: Page, allocation: DebitMemoAllocation): Promise<void> {
  await navigateToModule(page, `/accounts-payable/debit-memo-allocations/${allocation.id}`);
  await expect(page.locator('.page-title .badge', { hasText: 'DRAFT' })).toBeVisible({ timeout: 10_000 });
  await page.locator('.js-dma-action', { hasText: /Confirm|Konfirmasi/i }).click();
  const responsePromise = page.waitForResponse((response) =>
    response.url().includes(`/accounts-payable/debit-memo-allocations/${allocation.id}/confirm`) &&
    response.request().method() === 'POST'
  );
  await page.locator('#confirm-modal-btn-yes').click();
  const response = await responsePromise;
  const body = await response.json();
  expect(response.ok(), JSON.stringify(body)).toBeTruthy();
  expect(body.success, JSON.stringify(body)).toBeTruthy();
  await page.waitForURL(new RegExp(`/accounts-payable/debit-memo-allocations/${allocation.id}(\\?.*)?$`), {
    timeout: 20_000,
    waitUntil: 'domcontentloaded',
  });
  await expect(page.locator('.page-title .badge', { hasText: 'CONFIRMED' })).toBeVisible({ timeout: 10_000 });
}

async function expectDmaConfirmFailure(page: Page, allocation: DebitMemoAllocation, message: RegExp): Promise<void> {
  await navigateToModule(page, `/accounts-payable/debit-memo-allocations/${allocation.id}`);
  await expect(page.locator('.page-title .badge', { hasText: 'DRAFT' })).toBeVisible({ timeout: 10_000 });
  await page.locator('.js-dma-action', { hasText: /Confirm|Konfirmasi/i }).click();
  const responsePromise = page.waitForResponse((response) =>
    response.url().includes(`/accounts-payable/debit-memo-allocations/${allocation.id}/confirm`) &&
    response.request().method() === 'POST'
  );
  await page.locator('#confirm-modal-btn-yes').click();
  const response = await responsePromise;
  expect(response.ok()).toBeFalsy();
  await expect(page.locator('body')).toContainText(message, { timeout: 10_000 });
  await expect(page.locator('.page-title .badge', { hasText: 'DRAFT' })).toBeVisible({ timeout: 10_000 });
}

async function reverseDma(page: Page, allocation: DebitMemoAllocation): Promise<void> {
  await navigateToModule(page, `/accounts-payable/debit-memo-allocations/${allocation.id}`);
  await expect(page.locator('#btn-open-dma-reverse')).toBeVisible({ timeout: 10_000 });
  await page.locator('#btn-open-dma-reverse').click();
  await expect(page.locator('#dma-reverse-modal.show')).toBeVisible({ timeout: 10_000 });
  await setFlatpickrDate(page, '#dma-reverse-modal input[name="reversalDate"]', '2026-06-05');
  await page.locator('#dma-reverse-modal textarea[name="reversalReason"]').fill('E2E reverse DMA');

  const dataPromise = submitAjaxFormForData(page, '#dma-reverse-form', `/accounts-payable/debit-memo-allocations/${allocation.id}/reverse`);
  await page.waitForURL(new RegExp(`/accounts-payable/debit-memo-allocations/${allocation.id}(\\?.*)?$`), {
    timeout: 20_000,
    waitUntil: 'domcontentloaded',
  });
  await dataPromise;
  await expect(page.locator('.page-title .badge', { hasText: 'REVERSED' })).toBeVisible({ timeout: 10_000 });
}

async function readJournalTotals(page: Page): Promise<{ debit: string; credit: string }> {
  const totals = page.locator('table tfoot tr th.text-end.fw-bold');
  await expect(totals).toHaveCount(2);
  return {
    debit: (await totals.nth(0).innerText()).trim(),
    credit: (await totals.nth(1).innerText()).trim(),
  };
}

async function findPurchaseReturnJournalHref(page: Page, purchaseReturnCode: string): Promise<string> {
  await navigateToModule(
    page,
    `/accounting/journal-entries?sourceType=PURCHASE_RETURN&sourceCode=${encodeURIComponent(purchaseReturnCode)}`
  );
  const journalRow = page
    .locator('table tbody tr')
    .filter({ hasText: purchaseReturnCode })
    .filter({ hasText: /purchase return|retur pembelian/i })
    .first();
  await expect(journalRow).toBeVisible({ timeout: 10_000 });
  const href = await journalRow.locator('td').first().locator('a').getAttribute('href');
  expect(href, 'purchase return journal href').toBeTruthy();
  return href!;
}

async function tryReversePurchaseReturn(
  page: Page,
  debitMemo: DebitMemo,
  reason: string
): Promise<{ ok: boolean; body: string }> {
  await navigateToModule(page, `/purchasing/purchase-returns/${debitMemo.purchaseReturnId}/reverse`);
  await expect(page.locator('#purchase-return-reverse-form')).toBeVisible({ timeout: 10_000 });
  await setFlatpickrDate(page, '#reversal-date', '2026-06-06');
  await page.locator('#reversal-reason').fill(reason);
  await page.waitForFunction(() => {
    const selects = Array.from(document.querySelectorAll('[data-pr-reverse-target-container-select]')) as HTMLSelectElement[];
    return selects.length > 0 && selects.every((select: any) => {
      return select.tomselect ? Boolean(select.tomselect.getValue()) : Boolean(select.value);
    });
  });

  const responsePromise = page.waitForResponse((response) =>
    response.url().includes(`/purchasing/purchase-returns/${debitMemo.purchaseReturnId}/reverse`) &&
    response.request().method() === 'POST'
  );
  await page.locator('#purchase-return-reverse-form button[type="submit"]').click();
  const response = await responsePromise;
  return { ok: response.ok(), body: await response.text() };
}

async function expectVendorBillSettlement(
  page: Page,
  bill: ConfirmedVendorBill,
  settlementStatus: string,
  debitMemoApplied?: RegExp
): Promise<void> {
  await navigateToModule(page, `/accounts-payable/vendor-bills/${bill.id}`);
  await expect(page.locator('.page-title .badge', { hasText: 'CONFIRMED' })).toBeVisible({ timeout: 10_000 });
  const settlementStatusField = page
    .locator('label.form-label', { hasText: /Settlement Status|Status Pelunasan/ })
    .locator('xpath=..');
  await expect(settlementStatusField).toContainText(settlementStatus, { timeout: 10_000 });
  if (debitMemoApplied) {
    const debitMemoAppliedField = page
      .locator('label.form-label', { hasText: /Debit Memo Applied|Debit Memo Terpakai/ })
      .locator('xpath=..');
    await expect(debitMemoAppliedField).toContainText(debitMemoApplied, { timeout: 10_000 });
  }
  await expect(page.locator('body')).toContainText(/Debit Memo Allocations|Alokasi Debit Memo/i);
}

async function expectDebitMemoSettlement(page: Page, debitMemo: DebitMemo, settlementStatus: string): Promise<void> {
  await navigateToModule(page, debitMemo.href);
  await expect(page.locator('.page-header .badge', { hasText: settlementStatus })).toBeVisible({ timeout: 10_000 });
  await expect(page.locator('body')).toContainText(/Allocation History|Riwayat Alokasi/i);
}

async function expectApplyJournalVisible(page: Page, allocation: DebitMemoAllocation): Promise<void> {
  await navigateToModule(page, `/accounts-payable/debit-memo-allocations/${allocation.id}`);
  const journalLink = page.locator('a[href^="/accounting/journal-entries/"]').first();
  await expect(journalLink).toBeVisible({ timeout: 10_000 });
  const href = await journalLink.getAttribute('href');
  expect(href, 'apply journal href').toBeTruthy();
  await navigateToModule(page, href!);
  await expect(page.locator('body')).toContainText(/Debit Memo Application|Aplikasi Debit Memo|DEBIT_MEMO_APPLICATION/i, {
    timeout: 10_000,
  });
  await expect(page.locator('.badge', { hasText: 'POSTED' })).toBeVisible({ timeout: 10_000 });
}

test.describe('@accountspayable Debit Memo Allocation flow', () => {
  test.describe.configure({ timeout: 360_000 });
  test.use({ storageState: storageStatePath('admin') });

  test('sanity: admin can open DMA list and create page', async ({ page }) => {
    await navigateToModule(page, '/accounts-payable/debit-memo-allocations');
    await expect(page).toHaveURL(/\/accounts-payable\/debit-memo-allocations(\?.*)?$/);
    await expect(page.locator('.debit-memo-allocation-list-content table')).toBeVisible({ timeout: 10_000 });

    await navigateToModuleCommit(page, '/accounts-payable/debit-memo-allocations/create');
    await expect(page.locator('#debit-memo-allocation-form')).toBeVisible({ timeout: 60_000 });
  });

  test('@smoke Scenario A - confirmed DMA settles generated Debit Memo and Vendor Bill', async ({ page, browser }) => {
    const { debitMemo, vendorBill } = await createDebitMemoWithOptionalBill(browser, page);
    const allocation = await createDraftDmaFromDebitMemo(page, debitMemo, [vendorBill!]);

    await confirmDma(page, allocation);

    await expectDebitMemoSettlement(page, debitMemo, 'SETTLED');
    await expectVendorBillSettlement(page, vendorBill!, 'SETTLED', /8,500,000\.00/);
    await expectApplyJournalVisible(page, allocation);
  });

  test('Scenario B - partial allocation leaves both sides partially settled or open', async ({ page, browser }) => {
    const { debitMemo, vendorBill } = await createDebitMemoWithOptionalBill(browser, page);
    const allocation = await createDraftDmaFromDebitMemo(page, debitMemo, [vendorBill!], [4_000_000]);

    await confirmDma(page, allocation);

    await expectDebitMemoSettlement(page, debitMemo, 'PARTIALLY_SETTLED');
    await expectVendorBillSettlement(page, vendorBill!, 'PARTIALLY_SETTLED', /4,000,000\.00/);
  });

  test('Scenario C - one Debit Memo allocates across two Vendor Bills', async ({ page, browser }) => {
    const { debitMemo, vendorBill } = await createDebitMemoWithOptionalBill(browser, page);
    const secondBill = await createConfirmedVendorBill(page);
    const allocation = await createDraftDmaFromDebitMemo(
      page,
      debitMemo,
      [vendorBill!, secondBill],
      [4_000_000, 4_500_000]
    );

    await confirmDma(page, allocation);

    await expectDebitMemoSettlement(page, debitMemo, 'SETTLED');
    await expectVendorBillSettlement(page, vendorBill!, 'PARTIALLY_SETTLED', /4,000,000\.00/);
    await expectVendorBillSettlement(page, secondBill, 'PARTIALLY_SETTLED', /4,500,000\.00/);
  });

  test('Scenario D - Vendor Bill shortcut preselects bill and creates DMA draft', async ({ page, browser }) => {
    const { debitMemo, vendorBill } = await createDebitMemoWithOptionalBill(browser, page);

    const allocation = await createDraftDmaFromVendorBillShortcut(page, debitMemo, vendorBill!, 1_000_000);

    await navigateToModule(page, `/accounts-payable/debit-memo-allocations/${allocation.id}`);
    await expect(page.locator('.page-title .badge', { hasText: 'DRAFT' })).toBeVisible({ timeout: 10_000 });
    await expect(page.locator('body')).toContainText(vendorBill!.code);
    await expect(page.locator('body')).toContainText(debitMemo.code);
  });

  test('Scenario E - stale DMA draft cannot confirm after another DMA consumes the bill', async ({ page, browser }) => {
    const { debitMemo, vendorBill } = await createDebitMemoWithOptionalBill(browser, page);
    const staleAllocation = await createDraftDmaFromDebitMemo(page, debitMemo, [vendorBill!]);
    const settlingAllocation = await createDraftDmaFromDebitMemo(page, debitMemo, [vendorBill!]);

    await confirmDma(page, settlingAllocation);
    await expectDmaConfirmFailure(
      page,
      staleAllocation,
      /Debit memo remaining balance changed|Vendor bill outstanding changed|no longer has outstanding|sisa debit memo berubah|saldo debit memo berubah|sisa tagihan vendor berubah/i
    );
    await expectVendorBillSettlement(page, vendorBill!, 'SETTLED');
  });

  test('Scenario F - reverse confirmed DMA restores settlement and shows reversal journal', async ({ page, browser }) => {
    const { debitMemo, vendorBill } = await createDebitMemoWithOptionalBill(browser, page);
    const allocation = await createDraftDmaFromDebitMemo(page, debitMemo, [vendorBill!]);

    await confirmDma(page, allocation);
    await reverseDma(page, allocation);

    await expectDebitMemoSettlement(page, debitMemo, 'OPEN');
    await expectVendorBillSettlement(page, vendorBill!, 'OPEN', /0\.00/);
    await navigateToModule(page, `/accounts-payable/debit-memo-allocations/${allocation.id}`);
    await expect(page.locator('a[href^="/accounting/journal-entries/"]')).toHaveCount(2, { timeout: 10_000 });
  });

  test('Scenario G - blocks purchase return reversal while DMA is confirmed, then allows it after DMA reversal', async ({ page, browser }) => {
    const { debitMemo, vendorBill } = await createDebitMemoWithOptionalBill(browser, page);
    const allocation = await createDraftDmaFromDebitMemo(page, debitMemo, [vendorBill!]);
    await confirmDma(page, allocation);

    const originalJournalHref = await findPurchaseReturnJournalHref(page, debitMemo.purchaseReturnCode);
    await navigateToModule(page, originalJournalHref);
    const originalTotals = await readJournalTotals(page);
    expect(originalTotals.debit).toBe(originalTotals.credit);
    expect(originalTotals.debit).not.toMatch(/^0([,.]0+)?$/);

    const blocked = await tryReversePurchaseReturn(
      page,
      debitMemo,
      'E2E blocked while DMA is confirmed'
    );
    expect(blocked.ok, blocked.body).toBeFalsy();
    await expect(page.locator('body')).toContainText(
      /Reverse or cancel all confirmed debit memo allocations|Batalkan|reverse/i,
      { timeout: 10_000 }
    );

    await reverseDma(page, allocation);
    await expectDebitMemoSettlement(page, debitMemo, 'OPEN');
    await expectVendorBillSettlement(page, vendorBill!, 'OPEN', /0\.00/);

    const reversed = await tryReversePurchaseReturn(
      page,
      debitMemo,
      'E2E reverse purchase return after DMA reversal'
    );
    expect(reversed.ok, reversed.body).toBeTruthy();
    await page.waitForURL(new RegExp(`/purchasing/purchase-returns/view/${debitMemo.purchaseReturnId}(\\?.*)?$`), {
      timeout: 20_000,
      waitUntil: 'domcontentloaded',
    });
    await expect(page.locator('.page-header .badge')).toContainText(/reversed|direversal/i);

    const reversalJournalHref = await page.locator('a[href^="/accounting/journal-entries/"]').first().getAttribute('href');
    expect(reversalJournalHref, 'purchase return reversal journal href').toBeTruthy();

    await navigateToModule(page, debitMemo.goodsIssueHref);
    await expect(page.locator('.page-title .badge')).toContainText('CANCELLED');

    await navigateToModule(page, debitMemo.href);
    await expect(page.locator('.page-header .badge')).toContainText('CANCELLED');
    await expect(page.locator('a[href*="/accounts-payable/debit-memo-allocations/create"]')).toHaveCount(0);

    await navigateToModule(page, originalJournalHref);
    const originalAfterReversalTotals = await readJournalTotals(page);
    expect(originalAfterReversalTotals.debit).toBe(originalAfterReversalTotals.credit);
    expect(originalAfterReversalTotals.debit).toBe(originalTotals.debit);

    await navigateToModule(page, reversalJournalHref!);
    const reversalTotals = await readJournalTotals(page);
    expect(reversalTotals.debit).toBe(reversalTotals.credit);
    expect(reversalTotals.debit).toBe(originalTotals.debit);
  });
});
