import { test, expect, storageStatePath } from '../../fixtures/base';
import { navigateToModule } from '../../helpers/navigation';
import { setFlatpickrDate } from '../../helpers/flatpickr';
import { setTomSelectValue } from '../../helpers/tomselect';
import { setAutoNumeric } from '../../helpers/autonumeric';
import { waitForNetworkIdle } from '../../helpers/waits';
import { Page } from '@playwright/test';

// Seeded ids from V9000__e2e_seed_data.sql.
const PO_ID = '9201';
const PO_LINE_LAPTOP_ID = '9201';
const PO_LINE_CHAIR_ID = '9202';
const FACILITY_ID = '9101';
const CONTAINER_ID = '9101';

const PRODUCT_TO_PO_LINE: Record<string, string> = {
  'E2E-PRD-LAPTOP': PO_LINE_LAPTOP_ID,
  'E2E-PRD-CHAIR': PO_LINE_CHAIR_ID,
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
      return (
        !el.classList.contains('show') &&
        !el.classList.contains('hiding') &&
        !el.classList.contains('showing')
      );
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

async function pickPoLineFromModalSelector(page: Page, productCode: string): Promise<number> {
  const referenceLineId = PRODUCT_TO_PO_LINE[productCode];
  if (!referenceLineId) throw new Error(`No seeded PO line id mapped for ${productCode}`);

  const existingIndex = await findRowIndexByReferenceLineId(page, referenceLineId);
  if (existingIndex >= 0) {
    return keepOnlyReferenceLine(page, referenceLineId);
  }

  const prevCount = await page.locator('#line-container tr.line-row').count();
  await page.locator('#btn-add-line').click();

  const modal = page.locator('#modal-gr-po-line-selector.show');
  await expect(modal).toBeVisible({ timeout: 10_000 });
  await expect(page.locator('#gr-po-line-selector-results')).toContainText(productCode, { timeout: 10_000 });

  const row = page.locator('#gr-po-line-selector-results tbody tr', { hasText: productCode }).first();
  await row.locator('.js-gr-po-line-selector-item').check();
  await page.locator('#gr-po-line-selector-results .js-gr-po-line-selector-apply').click();
  await waitForBootstrapModalClosed(page, 'modal-gr-po-line-selector');
  await expect(page.locator('#line-container tr.line-row')).toHaveCount(prevCount + 1, { timeout: 5_000 });

  return keepOnlyReferenceLine(page, referenceLineId);
}

async function setQuantityViaDrawer(page: Page, rowIndex: number, qty: number): Promise<void> {
  await page.locator('#line-container tr.line-row').nth(rowIndex).locator('.btn-edit-detail').click();
  const drawer = page.locator('#drawer-non-serial');
  await expect(drawer).toBeVisible({ timeout: 5_000 });
  await page.waitForFunction(
    () => {
      const sel = document.querySelector('#drawer-non-serial .select-uom-target') as HTMLSelectElement | null;
      return sel != null && sel.options.length > 0;
    },
    undefined,
    { timeout: 5_000 }
  );
  await setAutoNumeric(page, '#drawer-non-serial .input-qty-target', qty);
  await drawer.locator('.btn-save-drawer').click();
  await page.waitForFunction(
    () => {
      const el = document.getElementById('drawer-non-serial');
      if (!el) return true;
      return (
        !el.classList.contains('show') &&
        !el.classList.contains('hiding') &&
        !el.classList.contains('showing')
      );
    },
    undefined,
    { timeout: 10_000 }
  );
}

async function createSampleDraftGr(page: Page): Promise<number> {
  await navigateToModule(page, `/inventory/goods-receipts/create?referenceType=PURCHASE_ORDER&referenceId=${PO_ID}`);
  await expect(page.locator('#gr-form')).toBeVisible();

  await setFlatpickrDate(page, 'input[name="receiptDate"]', '2026-05-20');
  const rowIndex = await pickPoLineFromModalSelector(page, 'E2E-PRD-LAPTOP');
  await setTomSelectValue(
    page,
    `#line-container tr.line-row:nth-of-type(${rowIndex + 1}) select.select-container`,
    CONTAINER_ID
  );
  await setQuantityViaDrawer(page, rowIndex, 1);

  await Promise.all([
    page.waitForURL(/\/inventory\/goods-receipts(\?.*)?$/, { timeout: 15_000, waitUntil: 'domcontentloaded' }),
    page.locator('#gr-form button[type="submit"]').first().click(),
  ]);

  await waitForNetworkIdle(page);
  const newId = await page.evaluate(() => {
    const links = Array.from(document.querySelectorAll('a[href*="/inventory/goods-receipts/edit/"]'));
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
  if (!newId) throw new Error('createSampleDraftGr: could not determine new GR id from list');
  return newId;
}

test.describe('@inventory Goods Receipt flow', () => {
  test.describe.configure({ timeout: 90_000 });
  test.use({ storageState: storageStatePath('warehouse1') });

  test('sanity: warehouse1 can open goods-receipts list', async ({ page }) => {
    await navigateToModule(page, '/inventory/goods-receipts');
    await expect(page).toHaveURL(/\/inventory\/goods-receipts(\?.*)?$/);
    await expect(page.locator('table')).toBeVisible();
    expect(FACILITY_ID).toBe('9101');
  });

  test('@smoke Scenario A - create DRAFT from PO line', async ({ page }) => {
    const id = await createSampleDraftGr(page);

    await navigateToModule(page, `/inventory/goods-receipts/${id}`);
    await expect(page.locator('.page-title .badge', { hasText: 'DRAFT' })).toBeVisible({ timeout: 10_000 });
  });

  test('Scenario B - edit DRAFT persists changes', async ({ page }) => {
    const id = await createSampleDraftGr(page);

    await navigateToModule(page, `/inventory/goods-receipts/edit/${id}`);
    await expect(page.locator('#gr-form')).toBeVisible();
    await expect(page.locator('#line-container tr.line-row')).toHaveCount(1, { timeout: 10_000 });

    await setQuantityViaDrawer(page, 0, 2);

    await Promise.all([
      page.waitForURL(/\/inventory\/goods-receipts(\?.*)?$/, { timeout: 15_000, waitUntil: 'domcontentloaded' }),
      page.locator('#gr-form button[type="submit"]').first().click(),
    ]);

    await navigateToModule(page, `/inventory/goods-receipts/edit/${id}`);
    await expect(page.locator('#line-container tr.line-row')).toHaveCount(1, { timeout: 10_000 });
    const persisted = await page.evaluate(() => {
      const el = document.querySelector('[name="lines[0].quantityReceived"]') as HTMLInputElement | null;
      return el?.value ?? '';
    });
    expect(persisted.replace(/[^\d.]/g, '')).toMatch(/^2(\.0+)?$/);
  });

  test('Scenario C - Complete DRAFT transitions to COMPLETED', async ({ page }) => {
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
  });

  test('Scenario D - delete DRAFT via API endpoint', async ({ page }) => {
    const id = await createSampleDraftGr(page);

    await navigateToModule(page, '/inventory/goods-receipts');

    const status = await page.evaluate(async (grId) => {
      const headerName = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content') ?? 'X-CSRF-TOKEN';
      const token = document.querySelector('meta[name="_csrf"]')?.getAttribute('content') ?? '';
      const headers: Record<string, string> = { Accept: 'application/json' };
      if (token) headers[headerName] = token;
      const res = await fetch(`/inventory/goods-receipts/${grId}`, {
        method: 'DELETE',
        credentials: 'same-origin',
        headers,
      });
      return res.status;
    }, id);
    expect(status, 'DELETE endpoint should return 2xx').toBeLessThan(300);

    await page.reload({ waitUntil: 'domcontentloaded' });
    const stillThere = await page.evaluate((grId) => {
      return Array.from(document.querySelectorAll(`a[href="/inventory/goods-receipts/edit/${grId}"]`)).length > 0;
    }, id);
    expect(stillThere).toBe(false);
  });
});
