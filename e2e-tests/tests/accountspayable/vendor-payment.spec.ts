import { Page } from '@playwright/test';
import { test, expect, storageStatePath } from '../../fixtures/base';
import { setAutoNumeric } from '../../helpers/autonumeric';
import { setFlatpickrDate } from '../../helpers/flatpickr';
import { waitForAjaxFormReady } from '../../helpers/form';
import { navigateToModule } from '../../helpers/navigation';
import { setTomSelectValue } from '../../helpers/tomselect';
import { waitForNetworkIdle } from '../../helpers/waits';

const PO_ID = '9201';
const PO_LINE_LAPTOP_ID = '9201';
const CONTAINER_ID = '9101';
const PAYMENT_AMOUNT = 8_500_000;

type ConfirmedVendorBill = {
  id: number;
  invoiceNumber: string;
  vendorId: string;
};

type DraftVendorPayment = {
  id: number;
  vendorBillId: number;
};

async function findRowIndexByReferenceLineId(page: Page, referenceLineId: string): Promise<number> {
  return page.evaluate((refId) => {
    const rows = Array.from(document.querySelectorAll('#line-container tr.line-row'));
    return rows.findIndex((row) => {
      const input = row.querySelector('input[name$=".referenceLineId"]') as HTMLInputElement | null;
      return input?.value === refId;
    });
  }, referenceLineId);
}

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
  if (existingIndex >= 0) {
    return keepOnlyReferenceLine(page, PO_LINE_LAPTOP_ID);
  }

  const prevCount = await page.locator('#line-container tr.line-row').count();
  await page.locator('#btn-add-line').click();

  const modal = page.locator('#modal-gr-po-line-selector.show');
  await expect(modal).toBeVisible({ timeout: 10_000 });
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
    const sel = document.querySelector('#drawer-non-serial .select-uom-target') as HTMLSelectElement | null;
    return sel != null && sel.options.length > 0;
  });
  await setAutoNumeric(page, '#drawer-non-serial .input-qty-target', qty);
  await drawer.locator('.btn-save-drawer').click();
  await page.waitForFunction(
    () => {
      const el = document.getElementById('drawer-non-serial');
      if (!el) return true;
      return !el.classList.contains('show') && !el.classList.contains('hiding') && !el.classList.contains('showing');
    },
    undefined,
    { timeout: 10_000 }
  );
}

async function createSampleDraftGr(page: Page): Promise<number> {
  await navigateToModule(page, `/inventory/goods-receipts/create?referenceType=PURCHASE_ORDER&referenceId=${PO_ID}`);
  await expect(page.locator('#gr-form')).toBeVisible();

  await setFlatpickrDate(page, 'input[name="receiptDate"]', '2026-05-20');
  const rowIndex = await pickPoLineFromModalSelector(page);
  await setTomSelectValue(
    page,
    `#line-container tr.line-row:nth-of-type(${rowIndex + 1}) select.select-container`,
    CONTAINER_ID
  );
  await setQuantityViaDrawer(page, rowIndex, 1);

  await waitForAjaxFormReady(page);
  await Promise.all([
    page.waitForURL(/\/inventory\/goods-receipts(\?.*)?$/, { timeout: 15_000, waitUntil: 'domcontentloaded' }),
    page.locator('#gr-form button[type="submit"]').first().click(),
  ]);

  await waitForNetworkIdle(page);
  await navigateToModule(page, `/inventory/goods-receipts?referenceType=PURCHASE_ORDER&referenceId=${PO_ID}&sort=id,desc`);
  const newId = await page.evaluate(() => {
    const row = Array.from(document.querySelectorAll('#table-gr-list tbody tr'))
      .find((candidate) => candidate.textContent?.includes('DRAFT'));
    const link = row?.querySelector('a[href*="/inventory/goods-receipts/edit/"]') as HTMLAnchorElement | null;
    const match = link?.href.match(/\/edit\/(\d+)/);
    return match ? Number(match[1]) : 0;
  });
  if (!newId) throw new Error('createSampleDraftGr: could not determine latest draft GR id');
  return newId;
}

async function createCompletedGr(page: Page): Promise<number> {
  const id = await createSampleDraftGr(page);

  await navigateToModule(page, `/inventory/goods-receipts/edit/${id}`);
  await expect(page.locator('#btn-complete')).toBeVisible({ timeout: 10_000 });

  await page.locator('#btn-complete').click();
  await page.locator('#confirm-modal-btn-yes').click();
  await page.waitForURL(new RegExp(`/inventory/goods-receipts/${id}(\\?.*)?$`), {
    timeout: 15_000,
    waitUntil: 'domcontentloaded',
  });
  await expect(page.locator('.page-title .badge', { hasText: 'COMPLETED' })).toBeVisible({ timeout: 10_000 });

  return id;
}

async function resolveSupplierPartyId(page: Page): Promise<string> {
  const res = await page.request.get('/api/lookup/parties?q=Sumber');
  if (!res.ok()) return '';
  const data = await res.json();
  return String(data?.[0]?.id ?? '');
}

async function createConfirmedVendorBill(page: Page): Promise<ConfirmedVendorBill> {
  const grId = await createCompletedGr(page);
  const invoiceNumber = `E2E-VB-VP-${Date.now()}`;

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
  await setFlatpickrDate(page, 'input[name="billDate"]', '2026-05-20');
  await setFlatpickrDate(page, 'input[name="dueDate"]', '2026-05-30');

  await waitForAjaxFormReady(page);
  await Promise.all([
    page.waitForURL(/\/accounts-payable\/vendor-bills(\?.*)?$/, { timeout: 15_000, waitUntil: 'domcontentloaded' }),
    page.locator('#vendor-bill-form button[type="submit"]').first().click(),
  ]);

  await waitForNetworkIdle(page);
  await navigateToModule(page, `/accounts-payable/vendor-bills?keyword=${encodeURIComponent(invoiceNumber)}`);
  const id = await page.evaluate((invoice) => {
    const rows = Array.from(document.querySelectorAll('#vendor-bill-table-container tbody tr'));
    const row = rows.find((candidate) => candidate.textContent?.includes(invoice));
    const link = row?.querySelector('a[href*="/accounts-payable/vendor-bills/"]') as HTMLAnchorElement | null;
    const match = link?.href.match(/\/accounts-payable\/vendor-bills\/(\d+)/);
    return match ? Number(match[1]) : 0;
  }, invoiceNumber);
  if (!id) throw new Error(`createConfirmedVendorBill: could not determine bill id for ${invoiceNumber}`);

  await navigateToModule(page, `/accounts-payable/vendor-bills/${id}`);
  await expect(page.locator('#btn-confirm-vendor-bill')).toBeVisible({ timeout: 10_000 });
  await page.locator('#btn-confirm-vendor-bill').click();
  await page.locator('#confirm-modal-btn-yes').click();
  await page.waitForURL(new RegExp(`/accounts-payable/vendor-bills/${id}(\\?.*)?$`), {
    timeout: 20_000,
    waitUntil: 'domcontentloaded',
  });
  await expect(page.locator('.page-title .badge', { hasText: 'CONFIRMED' })).toBeVisible({ timeout: 10_000 });

  const vendorId = await resolveSupplierPartyId(page);
  if (!vendorId) throw new Error('Could not resolve E2E supplier party id');
  return { id, invoiceNumber, vendorId };
}

async function keepOnlyAllocationForBill(page: Page, vendorBillId: number): Promise<void> {
  await page.waitForFunction(
    (billId) => {
      const inputs = Array.from(document.querySelectorAll('#allocation-lines input[name$=".vendorBillId"]')) as HTMLInputElement[];
      return inputs.some((input) => input.value === String(billId));
    },
    vendorBillId,
    { timeout: 15_000 }
  );

  await page.evaluate((billId) => {
    const rows = Array.from(document.querySelectorAll('#allocation-lines tr'));
    rows.forEach((row) => {
      const input = row.querySelector('input[name$=".vendorBillId"]') as HTMLInputElement | null;
      if (input?.value !== String(billId)) row.remove();
    });

    Array.from(document.querySelectorAll('#allocation-lines tr')).forEach((row, index) => {
      row.setAttribute('data-index', String(index));
      row.querySelectorAll('[name]').forEach((input) => {
        const el = input as HTMLInputElement;
        el.name = el.name.replace(/lines\[\d+]/, `lines[${index}]`);
      });
    });
  }, vendorBillId);

  await expect(page.locator('#allocation-lines tr')).toHaveCount(1, { timeout: 5_000 });
}

async function createDraftVendorPaymentForBill(page: Page, bill: ConfirmedVendorBill): Promise<DraftVendorPayment> {
  await navigateToModule(page, '/accounts-payable/vendor-payments/create');
  await expect(page.locator('#vendor-payment-form')).toBeVisible({ timeout: 10_000 });

  await setTomSelectValue(page, '#vp-vendor', bill.vendorId, 'PT. Sumber Makmur');
  await keepOnlyAllocationForBill(page, bill.id);

  await page.locator('#btn-select-bank-account').click();
  await expect(page.locator('#bank-account-modal.show')).toBeVisible({ timeout: 10_000 });
  await expect(page.locator('#bank-account-selector-body')).toContainText('E2E Operational', { timeout: 10_000 });
  await page.locator('#bank-account-selector-body .js-bank-account-select', { hasText: /Choose|Pilih/i }).first().click();
  await waitForBootstrapModalClosed(page, 'bank-account-modal');
  await expect(page.locator('#bankAccountId')).toHaveValue('9501', { timeout: 5_000 });

  await setFlatpickrDate(page, 'input[name="paymentDate"]', '2026-05-20');
  await setAutoNumeric(page, 'input[name="paymentAmount"]', PAYMENT_AMOUNT);
  await setAutoNumeric(page, '#allocation-lines tr:first-child .paid-amount-input', PAYMENT_AMOUNT);
  const paymentReference = `E2E-VP-${Date.now()}`;
  await page.locator('input[name="reference"]').fill(paymentReference);

  await waitForAjaxFormReady(page);
  await Promise.all([
    page.waitForURL(/\/accounts-payable\/vendor-payments(\?.*)?$/, { timeout: 15_000, waitUntil: 'domcontentloaded' }),
    page.locator('#vendor-payment-form button[type="submit"]').first().click(),
  ]);

  await waitForNetworkIdle(page);
  await navigateToModule(page, '/accounts-payable/vendor-payments?sort=id,desc');
  const id = await page.evaluate(() => {
    const links = Array.from(document.querySelectorAll('#vendor-payment-table-container tbody a[href^="/accounts-payable/vendor-payments/"]')) as HTMLAnchorElement[];
    const link = links.find((candidate) => !candidate.getAttribute('href')?.includes('/edit/'));
    const match = link?.getAttribute('href')?.match(/\/accounts-payable\/vendor-payments\/(\d+)/);
    return match ? Number(match[1]) : 0;
  });
  if (!id) throw new Error(`createDraftVendorPayment: could not determine latest payment id for ${paymentReference}`);
  return { id, vendorBillId: bill.id };
}

async function createDraftVendorPayment(page: Page): Promise<DraftVendorPayment> {
  const bill = await createConfirmedVendorBill(page);
  return createDraftVendorPaymentForBill(page, bill);
}

async function confirmVendorPayment(page: Page, paymentId: number): Promise<void> {
  await navigateToModule(page, `/accounts-payable/vendor-payments/${paymentId}`);
  await expect(page.locator('.btn-confirm-payment')).toBeVisible({ timeout: 10_000 });
  await page.locator('.btn-confirm-payment').click();
  await page.locator('#confirm-modal-btn-yes').click();
  await page.waitForURL(new RegExp(`/accounts-payable/vendor-payments/${paymentId}(\\?.*)?$`), {
    timeout: 20_000,
    waitUntil: 'domcontentloaded',
  });
}

async function expectVendorBillStatus(page: Page, vendorBillId: number, settlementStatus: string): Promise<void> {
  await navigateToModule(page, `/accounts-payable/vendor-bills/${vendorBillId}`);
  await expect(page.locator('.page-title .badge', { hasText: 'CONFIRMED' })).toBeVisible({ timeout: 10_000 });

  const settlementStatusField = page
    .locator('label.form-label', { hasText: /Settlement Status|Status Pelunasan/ })
    .locator('xpath=..');
  await expect(settlementStatusField).toContainText(settlementStatus, { timeout: 10_000 });
}

test.describe('@accountspayable Vendor Payment flow', () => {
  test.describe.configure({ timeout: 180_000 });
  test.use({ storageState: storageStatePath('admin') });

  test('sanity: admin can open vendor-payments list', async ({ page }) => {
    await navigateToModule(page, '/accounts-payable/vendor-payments');
    await expect(page).toHaveURL(/\/accounts-payable\/vendor-payments(\?.*)?$/);
    await expect(page.locator('#vendor-payment-table-container table')).toBeVisible();
  });

  test('@smoke Scenario A - create DRAFT vendor payment', async ({ page }) => {
    const payment = await createDraftVendorPayment(page);

    await navigateToModule(page, `/accounts-payable/vendor-payments/${payment.id}`);
    await expect(page.locator('.page-title .badge', { hasText: 'DRAFT' })).toBeVisible({ timeout: 10_000 });
  });

  test('Scenario B - confirm DRAFT transitions to CONFIRMED', async ({ page }) => {
    const payment = await createDraftVendorPayment(page);

    await confirmVendorPayment(page, payment.id);

    await expect(page.locator('.page-title .badge', { hasText: 'CONFIRMED' })).toBeVisible({ timeout: 10_000 });
    await expectVendorBillStatus(page, payment.vendorBillId, 'SETTLED');
  });

  test('Scenario C - cancel DRAFT transitions to CANCELLED', async ({ page }) => {
    const payment = await createDraftVendorPayment(page);

    await navigateToModule(page, `/accounts-payable/vendor-payments/${payment.id}`);
    await expect(page.locator('.btn-cancel-payment')).toBeVisible({ timeout: 10_000 });
    await page.locator('.btn-cancel-payment').click();
    await page.locator('#confirm-modal-btn-yes').click();
    await page.waitForURL(new RegExp(`/accounts-payable/vendor-payments/${payment.id}(\\?.*)?$`), {
      timeout: 20_000,
      waitUntil: 'domcontentloaded',
    });

    await expect(page.locator('.page-title .badge', { hasText: 'CANCELLED' })).toBeVisible({ timeout: 10_000 });
  });

  test('Scenario D - delete DRAFT via API endpoint', async ({ page }) => {
    const payment = await createDraftVendorPayment(page);

    await navigateToModule(page, '/accounts-payable/vendor-payments');

    const status = await page.evaluate(async (paymentId) => {
      const headerName = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content') ?? 'X-CSRF-TOKEN';
      const token = document.querySelector('meta[name="_csrf"]')?.getAttribute('content') ?? '';
      const headers: Record<string, string> = { Accept: 'application/json' };
      if (token) headers[headerName] = token;
      const res = await fetch(`/accounts-payable/vendor-payments/${paymentId}`, {
        method: 'DELETE',
        credentials: 'same-origin',
        headers,
      });
      return res.status;
    }, payment.id);
    expect(status, 'DELETE endpoint should return 2xx').toBeLessThan(300);

    await page.reload({ waitUntil: 'domcontentloaded' });
    await expect(page.locator(`#row-${payment.id}`)).toHaveCount(0);
  });

  test('Scenario E - stale draft payment cannot confirm an already settled bill', async ({ page }) => {
    const bill = await createConfirmedVendorBill(page);
    const stalePayment = await createDraftVendorPaymentForBill(page, bill);
    const settlingPayment = await createDraftVendorPaymentForBill(page, bill);

    await confirmVendorPayment(page, settlingPayment.id);
    await expect(page.locator('.page-title .badge', { hasText: 'CONFIRMED' })).toBeVisible({ timeout: 10_000 });

    await navigateToModule(page, `/accounts-payable/vendor-payments/${stalePayment.id}`);
    await expect(page.locator('.btn-confirm-payment')).toBeVisible({ timeout: 10_000 });
    await page.locator('.btn-confirm-payment').click();
    await page.locator('#confirm-modal-btn-yes').click();

    await expect(page.locator('body')).toContainText(
      /Selected vendor bill no longer has outstanding amount|Tagihan vendor yang dipilih sudah tidak memiliki sisa tagihan|Outstanding tagihan vendor berubah/i,
      { timeout: 10_000 }
    );
    await expect(page.locator('.page-title .badge', { hasText: 'DRAFT' })).toBeVisible({ timeout: 10_000 });
    await expectVendorBillStatus(page, bill.id, 'SETTLED');
  });
});
