import { Page } from '@playwright/test';
import { test, expect, storageStatePath } from '../../fixtures/base';
import { setAutoNumeric } from '../../helpers/autonumeric';
import { setFlatpickrDate } from '../../helpers/flatpickr';
import { waitForAjaxFormReady } from '../../helpers/form';
import { navigateToModule } from '../../helpers/navigation';
import { setTomSelectValue } from '../../helpers/tomselect';
import { waitForNetworkIdle } from '../../helpers/waits';

// Seeded ids from V9000__e2e_seed_data.sql.
const PO_ID = '9201';
const PO_LINE_LAPTOP_ID = '9201';
const CONTAINER_ID = '9101';

type DraftVendorBill = {
  id: number;
  invoiceNumber: string;
  grId: number;
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

async function createDraftVendorBill(page: Page): Promise<DraftVendorBill> {
  const grId = await createCompletedGr(page);
  const invoiceNumber = `E2E-VB-${Date.now()}`;

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
  if (!id) throw new Error(`createDraftVendorBill: could not determine bill id for ${invoiceNumber}`);

  return { id, invoiceNumber, grId };
}

async function expectSettlementStatus(page: Page, settlementStatus: string): Promise<void> {
  const settlementStatusField = page
    .locator('label.form-label', { hasText: /Settlement Status|Status Pelunasan/ })
    .locator('xpath=..');

  await expect(settlementStatusField).toContainText(settlementStatus, { timeout: 10_000 });
}

test.describe('@accountspayable Vendor Bill flow', () => {
  test.describe.configure({ timeout: 120_000 });
  test.use({ storageState: storageStatePath('admin') });

  test('sanity: admin can open vendor-bills list', async ({ page }) => {
    await navigateToModule(page, '/accounts-payable/vendor-bills');
    await expect(page).toHaveURL(/\/accounts-payable\/vendor-bills(\?.*)?$/);
    await expect(page.locator('#vendor-bill-table-container table')).toBeVisible();
  });

  test('@smoke Scenario A - create DRAFT from completed GR', async ({ page }) => {
    const bill = await createDraftVendorBill(page);

    await navigateToModule(page, `/accounts-payable/vendor-bills/${bill.id}`);
    await expect(page.locator('.page-title .badge', { hasText: 'DRAFT' })).toBeVisible({ timeout: 10_000 });
    await expect(page.locator('body')).toContainText(bill.invoiceNumber);
  });

  test('Scenario B - confirm DRAFT transitions to CONFIRMED', async ({ page }) => {
    const bill = await createDraftVendorBill(page);

    await navigateToModule(page, `/accounts-payable/vendor-bills/${bill.id}`);
    await expect(page.locator('#btn-confirm-vendor-bill')).toBeVisible({ timeout: 10_000 });

    await page.locator('#btn-confirm-vendor-bill').click();
    await page.locator('#confirm-modal-btn-yes').click();
    await page.waitForURL(new RegExp(`/accounts-payable/vendor-bills/${bill.id}(\\?.*)?$`), {
      timeout: 20_000,
      waitUntil: 'domcontentloaded',
    });

    await expect(page.locator('.page-title .badge', { hasText: 'CONFIRMED' })).toBeVisible({ timeout: 10_000 });
    await expectSettlementStatus(page, 'OPEN');
  });

  test('Scenario C - cancel DRAFT transitions to CANCELLED and returns to list', async ({ page }) => {
    const bill = await createDraftVendorBill(page);

    await navigateToModule(page, `/accounts-payable/vendor-bills/${bill.id}`);
    await expect(page.locator('#btn-cancel-vendor-bill')).toBeVisible({ timeout: 10_000 });

    await page.locator('#btn-cancel-vendor-bill').click();
    await page.locator('#confirm-modal-btn-yes').click();
    await page.waitForURL(/\/accounts-payable\/vendor-bills(\?.*)?$/, {
      timeout: 20_000,
      waitUntil: 'domcontentloaded',
    });

    await navigateToModule(page, `/accounts-payable/vendor-bills?keyword=${encodeURIComponent(bill.invoiceNumber)}`);
    const row = page.locator('#vendor-bill-table-container tbody tr').filter({ hasText: bill.invoiceNumber }).first();
    await expect(row).toBeVisible({ timeout: 10_000 });
    await expect(row).toContainText('CANCELLED', { timeout: 10_000 });
  });

  test('Scenario D - delete DRAFT via API endpoint', async ({ page }) => {
    const bill = await createDraftVendorBill(page);

    await navigateToModule(page, '/accounts-payable/vendor-bills');

    const status = await page.evaluate(async (billId) => {
      const headerName = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content') ?? 'X-CSRF-TOKEN';
      const token = document.querySelector('meta[name="_csrf"]')?.getAttribute('content') ?? '';
      const headers: Record<string, string> = { Accept: 'application/json' };
      if (token) headers[headerName] = token;
      const res = await fetch(`/accounts-payable/vendor-bills/${billId}`, {
        method: 'DELETE',
        credentials: 'same-origin',
        headers,
      });
      return res.status;
    }, bill.id);
    expect(status, 'DELETE endpoint should return 2xx').toBeLessThan(300);

    await page.reload({ waitUntil: 'domcontentloaded' });
    await expect(page.locator(`#row-${bill.id}`)).toHaveCount(0);
  });
});
