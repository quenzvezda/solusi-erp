import { test, expect, storageStatePath } from '../../fixtures/base';
import { navigateToModule } from '../../helpers/navigation';
import { setFlatpickrDate } from '../../helpers/flatpickr';
import { setTomSelectValue } from '../../helpers/tomselect';
import { setAutoNumeric } from '../../helpers/autonumeric';
import { addLine, waitForRowSettled, lineFieldSelector } from '../../helpers/line-editor';
import { waitForNetworkIdle } from '../../helpers/waits';
import { Page } from '@playwright/test';

// Seeded ids from V9000__e2e_seed_data.sql.
const FACILITY_ID = '9101';
const GRID_ID = '9101';
const CONTAINER_ID = '9101';

/**
 * Pick a product into a line's `select-product` TomSelect by issuing the lookup
 * search ourselves and injecting the full LookupDto (including `payload`) before
 * `setValue`. The page-JS change handler at stock-adjustment-form.js:204 reads
 * `tsProd.options[val].payload.uomId` to fill `.input-uom-id`. The generic
 * `setTomSelectValue` helper only injects `{id, name, text}` so the payload is
 * missing; the broken `selectTomSelect` helper never resolves because it asks
 * TomSelect's `load()` for a callback that the API does not deliver. Doing the
 * fetch in the test code routes around both.
 */
async function selectProductOnLine(
  page: Page,
  lineSelector: string,
  productCode: string = 'E2E-PRD-LAPTOP'
): Promise<void> {
  const res = await page.request.get(
    `/api/lookup/inventory/products?q=${encodeURIComponent(productCode)}`
  );
  if (!res.ok()) {
    throw new Error(`product lookup '${productCode}' failed: HTTP ${res.status()}`);
  }
  const list = (await res.json()) as Array<{
    id: string | number;
    name: string;
    subText?: string;
    payload?: Record<string, unknown>;
  }>;
  const opt = list?.[0];
  if (!opt?.id) throw new Error(`product lookup '${productCode}' returned no results`);

  await page.evaluate(
    ({ sel, option }) => {
      const el = document.querySelector(sel) as HTMLSelectElement & { tomselect?: any };
      if (!el?.tomselect) throw new Error('TomSelect not initialized: ' + sel);
      el.tomselect.addOption(option);
      el.tomselect.setValue(String(option.id));
    },
    { sel: lineSelector, option: opt }
  );
}

async function pickIdrCurrency(page: Page): Promise<void> {
  // The page boot script polls every 50ms for TomSelect/ErpLineManager/ErpDrawer
  // before initializing. On a cold-start box (JIT not warm + CDN scripts still
  // loading) this can exceed 10s. Wait for the <option> nodes AND TomSelect
  // instance with a generous timeout.
  await page.waitForFunction(
    () => {
      const sel = document.querySelector('#header-currency') as
        | (HTMLSelectElement & { tomselect?: unknown })
        | null;
      return !!sel && sel.options.length > 0 && !!sel.tomselect;
    },
    undefined,
    { timeout: 20_000 }
  );
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
  // Bootstrap 5 offcanvas dismiss memakai 3 state class: .show -> .hiding -> (none).
  // Polling `toBeHidden` di mesin sibuk kadang tetap report visible meskipun
  // semua state class sudah lepas (visibility transition belum settle). Trust
  // functional dismissal: tunggu semua state class lepas, itu cukup karena
  // setupUomDrawer.btn-save-drawer.onclick sudah commit data ke row sebelum
  // panggil ErpDrawer.close.
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

/**
 * Create a DRAFT SA with one line (laptop, qty=5, unitCost=100000) and return
 * the new SA id from the redirect target list page.
 *
 * Shared across Scenarios B-E so each scenario starts from a known DRAFT.
 */
async function createSampleDraftSa(page: Page): Promise<number> {
  await navigateToModule(page, '/inventory/adjustments/create');
  await expect(page.locator('#adjustment-form')).toBeVisible();

  await setFlatpickrDate(page, 'input[name="transactionDate"]', '2026-05-20');
  await pickIdrCurrency(page);
  await setTomSelectValue(page, '#header-facility', FACILITY_ID);

  const rowIndex = await addLine(page);
  await waitForRowSettled(page, rowIndex);
  // See note in Scenario A: setTomSelectValue cannot inject the product
  // payload (uomId, etc.) so the page change handler leaves .input-uom-id
  // empty. selectTomSelect from the shared helper has a broken signature
  // (load()'s callback never fires) so we use the local payload-aware helper.
  await selectProductOnLine(page, lineFieldSelector(rowIndex, 'productId'));
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
    // Use selectTomSelect (loads via the lookup AJAX endpoint) instead of
    // setTomSelectValue. The product change handler in stock-adjustment-form.js
    // reads `tsProd.options[val].payload.uomId` to fill `.input-uom-id`. The
    // bulk setTomSelectValue helper only injects {id, name, text} — no payload —
    // so the handler's `p.uomId` stays undefined and `.input-uom-id` never
    // populates, causing the next waitForFunction to time out.
    await selectProductOnLine(page, lineFieldSelector(rowIndex, 'productId'));
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

    // Open the view page and assert status badge DRAFT. The form/edit page
    // has no status badge — the badge lives in `.page-header` of view.html
    // as a sibling of `.page-title`, NOT inside it.
    await navigateToModule(page, `/inventory/adjustments/view/${newId}`);
    await expect(
      page.locator('.page-header .badge', { hasText: 'DRAFT' })
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

    // ErpAction.confirmAndSubmit opens a Bootstrap modal (#modal-global-confirm)
    // — NOT a native window.confirm. Click the modal's confirm button so the
    // hidden form submits and the page navigates to /view/{id}.
    await page.locator('#btn-process-inventory').click();
    await page.locator('#confirm-modal-btn-yes').click();
    await page.waitForURL(new RegExp(`/inventory/adjustments/view/${id}`), {
      timeout: 15_000,
      waitUntil: 'domcontentloaded',
    });

    // View page should show COMPLETED status (badge in .page-header, sibling
    // of .page-title, not inside it).
    await expect(
      page.locator('.page-header .badge', { hasText: 'COMPLETED' })
    ).toBeVisible({ timeout: 10_000 });

    // Edit URL must redirect to view for COMPLETED.
    await page.goto(`/inventory/adjustments/edit/${id}`, { waitUntil: 'domcontentloaded' });
    await expect(page).toHaveURL(new RegExp(`/inventory/adjustments/view/${id}`));
  });

  test('Scenario D — facility change clears all lines (confirm dialog)', async ({ page }) => {
    const id = await createSampleDraftSa(page);

    await navigateToModule(page, `/inventory/adjustments/edit/${id}`);
    await expect(page.locator('#line-container tr.line-row')).toHaveCount(1, { timeout: 10_000 });

    // Trigger facility change handler. ErpModal.confirm at
    // stock-adjustment-form.js:301 fires when the TomSelect change event
    // emits — `clear()` alone is enough; we don't need to set a new value.
    // Only one E2E facility is seeded (9101), so we cannot pick a *different*
    // facility to trigger the change. Clearing yields '' which differs from
    // the current '9101' and fires `change`.
    await page.evaluate(() => {
      const el = document.querySelector('#header-facility') as any;
      el?.tomselect?.clear();
    });

    // Accept the Bootstrap confirm modal (#modal-global-confirm) by clicking
    // its yes button — the modal callback wipes #line-container.
    await page.locator('#confirm-modal-btn-yes').click();

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
