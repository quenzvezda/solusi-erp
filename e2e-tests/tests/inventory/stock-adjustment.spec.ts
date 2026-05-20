import { test, expect, storageStatePath } from '../../fixtures/base';
import { navigateToModule } from '../../helpers/navigation';
import { setFlatpickrDate } from '../../helpers/flatpickr';
import { setTomSelectValue, selectTomSelect } from '../../helpers/tomselect';
import { setAutoNumeric } from '../../helpers/autonumeric';
import { addLine, waitForRowSettled, lineFieldSelector } from '../../helpers/line-editor';
import { waitForNetworkIdle } from '../../helpers/waits';
import { Page } from '@playwright/test';

// Seeded ids from V9000__e2e_seed_data.sql.
const FACILITY_ID = '9101';
const GRID_ID = '9101';
const CONTAINER_ID = '9101';

async function resolveProductLaptopId(page: Page): Promise<string> {
  const id = await page.evaluate(async () => {
    const res = await fetch('/api/lookup/inventory/products?q=E2E-PRD-LAPTOP', {
      credentials: 'same-origin',
    });
    if (!res.ok) return null;
    const json = await res.json();
    return json?.[0]?.id ?? null;
  });
  if (!id) throw new Error('E2E-PRD-LAPTOP not found via lookup');
  return String(id);
}

async function pickIdrCurrency(page: Page): Promise<void> {
  const currencyId = await page.evaluate(() => {
    const sel = document.querySelector('#header-currency') as HTMLSelectElement | null;
    if (!sel) return '';
    const opt = Array.from(sel.options).find((o) => o.text.trim() === 'IDR');
    return opt?.value ?? '';
  });
  if (!currencyId) throw new Error('IDR option not found in #header-currency');
  await setTomSelectValue(page, '#header-currency', currencyId);
}

/**
 * Set line quantity via the non-serial drawer. The line's .input-qty input is
 * readonly — quantity is committed only by clicking the row's pencil
 * (.btn-edit-detail), filling .input-qty-target in #drawer-non-serial, and
 * clicking .btn-save-drawer.
 */
async function setQuantityViaDrawer(page: Page, rowIndex: number, qty: number): Promise<void> {
  await page.locator(`#line-container tr.line-row >> nth=${rowIndex} >> .btn-edit-detail`).click();
  const drawer = page.locator('#drawer-non-serial');
  await expect(drawer).toBeVisible({ timeout: 5_000 });
  // UoM dropdown populates asynchronously from /api/lookup/inventory/uom-conversions.
  await page.waitForFunction(
    () => {
      const sel = document.querySelector('#drawer-non-serial .select-uom-target') as HTMLSelectElement | null;
      return sel != null && sel.options.length > 0;
    },
    { timeout: 5_000 }
  );
  await setAutoNumeric(page, '#drawer-non-serial .input-qty-target', qty);
  await drawer.locator('.btn-save-drawer').click();
  await expect(drawer).toBeHidden({ timeout: 5_000 });
}

/**
 * Create a DRAFT SA with one line (laptop, qty=5, unitCost=100000) and return
 * the new SA id from the redirect target list page.
 *
 * Shared across Scenarios B-E so each scenario starts from a known DRAFT.
 */
async function createSampleDraftSa(page: Page): Promise<number> {
  const productId = await resolveProductLaptopId(page);
  await navigateToModule(page, '/inventory/adjustments/create');
  await expect(page.locator('#adjustment-form')).toBeVisible();

  await setFlatpickrDate(page, 'input[name="transactionDate"]', '2026-05-20');
  await pickIdrCurrency(page);
  await setTomSelectValue(page, '#header-facility', FACILITY_ID);

  const rowIndex = await addLine(page);
  await waitForRowSettled(page, rowIndex);
  await setTomSelectValue(page, lineFieldSelector(rowIndex, 'productId'), productId);
  await page.waitForFunction(
    ({ idx }) => {
      const el = document.querySelector(
        `#line-container tr.line-row:nth-of-type(${idx + 1}) .input-uom-id`
      ) as HTMLInputElement | null;
      return !!el && el.value !== '';
    },
    { idx: rowIndex },
    { timeout: 5_000 }
  );
  const gridSel = `#line-container tr.line-row:nth-of-type(${rowIndex + 1}) select.select-grid`;
  const binSel = `#line-container tr.line-row:nth-of-type(${rowIndex + 1}) select.select-container`;
  await setTomSelectValue(page, gridSel, GRID_ID);
  await setTomSelectValue(page, binSel, CONTAINER_ID);
  await setQuantityViaDrawer(page, rowIndex, 5);
  await setAutoNumeric(page, lineFieldSelector(rowIndex, 'unitCost'), 100000);

  await Promise.all([
    page.waitForURL(/\/inventory\/adjustments(\?.*)?$/, { timeout: 15_000, waitUntil: 'domcontentloaded' }),
    page.locator('#adjustment-form button[type="submit"]').first().click(),
  ]);

  await waitForNetworkIdle(page);
  const newId = await page.evaluate(() => {
    const links = Array.from(document.querySelectorAll('a[href*="/inventory/adjustments/edit/"]'));
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
  if (!newId) throw new Error('createSampleDraftSa: could not determine new SA id from list');
  return newId;
}

/**
 * Stock Adjustment E2E spec.
 * Logged in as warehouse1 — has STOCK-ADJUSTMENT_{READ,CREATE,UPDATE,DELETE,PROCESS}
 * + LOOKUP_INVENTORY/FACILITY/GRID/CONTAINER/UOM-CONVERSION granted via D010 + V9000.
 *
 * Lifecycle: DRAFT -> COMPLETED (no approval, no signature).
 * "Process to Inventory" via POST /inventory/adjustments/{id}/process redirects
 * to view page (NOT AJAX form).
 */
test.describe('@inventory Stock Adjustment flow', () => {
  test.use({ storageState: storageStatePath('warehouse1') });

  test('sanity: warehouse1 can open adjustments list', async ({ page }) => {
    await navigateToModule(page, '/inventory/adjustments');
    await expect(page).toHaveURL(/\/inventory\/adjustments(\?.*)?$/);
    await expect(page.locator('table')).toBeVisible();
  });

  test('Scenario A — create DRAFT with 1 line', async ({ page }) => {
    const productId = await resolveProductLaptopId(page);

    await navigateToModule(page, '/inventory/adjustments/create');
    await expect(page.locator('#adjustment-form')).toBeVisible();

    // Header
    await setFlatpickrDate(page, 'input[name="transactionDate"]', '2026-05-20');
    await pickIdrCurrency(page);
    await setTomSelectValue(page, '#header-facility', FACILITY_ID);

    // Add a line — btn-add-line guards against missing facility, so set facility first.
    const rowIndex = await addLine(page);
    await waitForRowSettled(page, rowIndex);

    // Pick product (line 0). Page JS auto-fills uomId/uomAlias/serialized/lastCost.
    await setTomSelectValue(page, lineFieldSelector(rowIndex, 'productId'), productId);
    // Wait for uom hidden field to be populated by the change handler.
    await page.waitForFunction(
      ({ idx }) => {
        const el = document.querySelector(
          `#line-container tr.line-row:nth-of-type(${idx + 1}) .input-uom-id`
        ) as HTMLInputElement | null;
        return !!el && el.value !== '';
      },
      { idx: rowIndex },
      { timeout: 5_000 }
    );

    // Grid (cascading from facility) and Container (cascading from grid).
    // The grid TomSelect has no #id selector — use class on the line row.
    const gridSel = `#line-container tr.line-row:nth-of-type(${rowIndex + 1}) select.select-grid`;
    const binSel = `#line-container tr.line-row:nth-of-type(${rowIndex + 1}) select.select-container`;
    await setTomSelectValue(page, gridSel, GRID_ID);
    await setTomSelectValue(page, binSel, CONTAINER_ID);

    // Quantity must go through the drawer (input-qty is readonly).
    await setQuantityViaDrawer(page, rowIndex, 5);

    // Unit cost is a normal AutoNumeric input on the row.
    await setAutoNumeric(page, lineFieldSelector(rowIndex, 'unitCost'), 100000);

    // AJAX submit -> redirect to list.
    await Promise.all([
      page.waitForURL(/\/inventory\/adjustments(\?.*)?$/, { timeout: 15_000, waitUntil: 'domcontentloaded' }),
      page.locator('#adjustment-form button[type="submit"]').first().click(),
    ]);

    // Capture new SA id from the list page (highest edit-link id).
    await waitForNetworkIdle(page);
    const newId = await page.evaluate(() => {
      const links = Array.from(document.querySelectorAll('a[href*="/inventory/adjustments/edit/"]'));
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
    expect(newId, 'expected a new adjustment row in the list').toBeGreaterThan(0);

    // Reopen edit and assert status DRAFT (page-title badge mirrors PR pattern).
    await navigateToModule(page, `/inventory/adjustments/edit/${newId}`);
    await expect(
      page.locator('.page-title .badge, .page-title .status', { hasText: 'DRAFT' })
    ).toBeVisible({ timeout: 10_000 });
  });

  test('Scenario B — edit DRAFT persists changes', async ({ page }) => {
    const id = await createSampleDraftSa(page);

    await navigateToModule(page, `/inventory/adjustments/edit/${id}`);
    await expect(page.locator('#adjustment-form')).toBeVisible();
    await expect(page.locator('#line-container tr.line-row')).toHaveCount(1, { timeout: 10_000 });

    // Quantity 5 -> 7 via drawer (the only commit path).
    await setQuantityViaDrawer(page, 0, 7);

    // Save (AJAX) -> redirect list.
    await Promise.all([
      page.waitForURL(/\/inventory\/adjustments(\?.*)?$/, { timeout: 15_000, waitUntil: 'domcontentloaded' }),
      page.locator('#adjustment-form button[type="submit"]').first().click(),
    ]);

    // Reopen and assert persisted quantity.
    await navigateToModule(page, `/inventory/adjustments/edit/${id}`);
    await expect(page.locator('#line-container tr.line-row')).toHaveCount(1, { timeout: 10_000 });
    const persisted = await page.evaluate(() => {
      const el = document.querySelector('[name="lines[0].quantity"]') as HTMLInputElement | null;
      return el?.value ?? '';
    });
    // AutoNumeric formats with 2 decimals — accept "7" or "7.00".
    expect(persisted.replace(/[^\d.]/g, '')).toMatch(/^7(\.0+)?$/);
  });

  test('Scenario C — Process to Inventory transitions DRAFT to COMPLETED', async ({ page }) => {
    const id = await createSampleDraftSa(page);

    await navigateToModule(page, `/inventory/adjustments/edit/${id}`);
    await expect(page.locator('#btn-process-inventory')).toBeVisible({ timeout: 10_000 });

    // ErpAction.confirmAndSubmit posts a hidden form to data-process-url with
    // the page CSRF token. Page navigates to /view/{id} on success.
    page.on('dialog', (d) => d.accept());
    await Promise.all([
      page.waitForURL(new RegExp(`/inventory/adjustments/view/${id}`), { timeout: 15_000, waitUntil: 'domcontentloaded' }),
      page.locator('#btn-process-inventory').click(),
    ]);

    // View page should show COMPLETED status.
    await expect(
      page.locator('.page-title .badge, .page-title .status', { hasText: 'COMPLETED' })
    ).toBeVisible({ timeout: 10_000 });

    // Edit URL must redirect to view for COMPLETED.
    await page.goto(`/inventory/adjustments/edit/${id}`, { waitUntil: 'domcontentloaded' });
    await expect(page).toHaveURL(new RegExp(`/inventory/adjustments/view/${id}`));
  });

  test('Scenario D — facility change clears all lines (confirm dialog)', async ({ page }) => {
    const id = await createSampleDraftSa(page);

    await navigateToModule(page, `/inventory/adjustments/edit/${id}`);
    await expect(page.locator('#line-container tr.line-row')).toHaveCount(1, { timeout: 10_000 });

    // ErpModal.confirm renders a Bootstrap modal — accept by clicking the
    // primary button. Fall back to native dialog handler in case the build
    // uses window.confirm.
    page.on('dialog', (d) => d.accept());

    // Trigger facility change. Only one E2E facility is seeded (9101), so
    // re-selecting the same value won't fire the change handler. Clear the
    // TomSelect first so the next set is observed as a change.
    await page.evaluate(() => {
      const el = document.querySelector('#header-facility') as any;
      el?.tomselect?.clear();
    });
    // Set back to 9101; the page-JS facility change handler fires confirm
    // and (on accept) wipes #line-container.
    await setTomSelectValue(page, '#header-facility', FACILITY_ID);

    // Some builds open ErpModal — accept it if present.
    const modalConfirm = page.locator('.modal.show .btn-primary, .modal.show .btn-confirm').first();
    if (await modalConfirm.count()) {
      await modalConfirm.click().catch(() => undefined);
    }

    await expect(page.locator('#line-container tr.line-row')).toHaveCount(0, { timeout: 5_000 });
    await expect(page.locator('#empty-msg')).toBeVisible();
  });

  test('Scenario E — delete DRAFT via API endpoint', async ({ page }) => {
    // The list template does not expose a per-row delete control (verified
    // src/main/resources/templates/inventory/adjustments/list.html). The
    // DELETE /inventory/adjustments/{id} endpoint exists and is gated by
    // STOCK-ADJUSTMENT_DELETE — exercise it directly to validate the
    // server-side permission + use case wiring.
    const id = await createSampleDraftSa(page);

    // Make sure list page CSRF meta is available before issuing DELETE.
    await navigateToModule(page, '/inventory/adjustments');

    const status = await page.evaluate(async (saId) => {
      const headerName = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content') ?? 'X-XSRF-TOKEN';
      const token = document.querySelector('meta[name="_csrf"]')?.getAttribute('content') ?? '';
      const headers: Record<string, string> = { Accept: 'application/json' };
      if (token) headers[headerName] = token;
      const res = await fetch(`/inventory/adjustments/${saId}`, {
        method: 'DELETE',
        credentials: 'same-origin',
        headers,
      });
      return res.status;
    }, id);
    expect(status, 'DELETE endpoint should return 2xx').toBeLessThan(300);

    // Reload list and confirm the deleted SA's edit link is gone.
    await page.reload({ waitUntil: 'domcontentloaded' });
    const stillThere = await page.evaluate((saId) => {
      return Array.from(document.querySelectorAll(`a[href="/inventory/adjustments/edit/${saId}"]`)).length > 0;
    }, id);
    expect(stillThere).toBe(false);
  });
});
