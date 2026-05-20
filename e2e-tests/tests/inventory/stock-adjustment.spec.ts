import { test, expect, storageStatePath } from '../../fixtures/base';
import { navigateToModule } from '../../helpers/navigation';

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

  test.skip('Scenario A — create DRAFT with 1 line', async () => {
    // warehouse1 navigates to /inventory/adjustments/create
    // -> fills date, facility (TomSelect), currency, exchangeRate
    // -> adds 1 line: product, grid (cascading from facility), container,
    //                 quantity (AutoNumeric), unitCost
    // -> save (AJAX) -> redirect to list
    // -> reopen edit page, assert status DRAFT
  });

  test.skip('Scenario B — edit DRAFT persists changes', async () => {
    // create DRAFT via helper -> open edit -> change quantity -> save
    // -> reopen, assert quantity persisted
  });

  test.skip('Scenario C — Process to Inventory transitions DRAFT to COMPLETED', async () => {
    // create DRAFT -> click Process to Inventory (POST redirect, not AJAX)
    // -> assert status badge COMPLETED
    // -> navigate /edit/{id} -> assert redirect to /view/{id}
  });

  test.skip('Scenario D — facility change clears all lines (confirm dialog)', async () => {
    // create DRAFT 1 line -> open edit -> install dialog accept handler
    // -> change facility (or trigger reset action) -> assert lines container empty
  });

  test.skip('Scenario E — delete DRAFT from list page', async () => {
    // create DRAFT -> navigate list -> click row delete (HTMX) -> dialog accept
    // -> waitForHtmx -> assert row absent
  });
});
