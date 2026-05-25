import { test, expect, storageStatePath } from '../../fixtures/base';
import { navigateToModule } from '../../helpers/navigation';
import { Page } from '@playwright/test';

/**
 * Purchase Order E2E flow — STANDARD type only (DIRECT skipped per plan
 * docs/plans/2026-05-25-e2e-purchase-order.md, overlaps PR/SA pattern).
 *
 * Lifecycle covered: DRAFT -> SUBMITTED -> APPROVED -> SENT
 *                    DRAFT -> CANCELLED
 *                    DRAFT -> (DELETE)
 *                    SUBMITTED -> REJECTED
 *
 * Logged in as warehouse1 — granted PO_{READ,CREATE,UPDATE,DELETE,SUBMIT,SEND}
 * + LOOKUP_{PR,PO,TAX,PARTY} via V9000.
 */

// Seeded ids from V9000__e2e_seed_data.sql.
const PR_ID = '9301';
const PR_LINE_LAPTOP_ID = '9301';
const PR_LINE_CHAIR_ID = '9302';
const FACILITY_ID = '9101';
const TAX_ID = '9001';

/**
 * Resolve dynamic seed ids (party ids, product id) at runtime via lookup
 * endpoints. Adapted from PR spec's resolveSeedIds.
 */
async function resolveSeedIds(page: Page): Promise<{
  approver1PartyId: string;
  supplierPartyId: string;
  productLaptopId: string;
}> {
  const fetchJson = async (path: string) => {
    return await page.evaluate(async (p) => {
      const res = await fetch(p, { credentials: 'same-origin' });
      return res.ok ? await res.json() : null;
    }, path);
  };

  const apr = await fetchJson('/api/lookup/parties/by-role-type?roleTypeCode=APPROVER&q=Budi');
  const sup = await fetchJson('/api/lookup/parties?q=Sumber');
  const prd = await fetchJson('/api/lookup/inventory/products?q=E2E-PRD-LAPTOP');

  return {
    approver1PartyId: String(apr?.[0]?.id ?? ''),
    supplierPartyId: String(sup?.[0]?.id ?? ''),
    productLaptopId: String(prd?.[0]?.id ?? ''),
  };
}

test.describe('Purchase Order flow', () => {
  test.use({ storageState: storageStatePath('warehouse1') });

  test('sanity: warehouse1 can open purchase-orders list', async ({ page }) => {
    await navigateToModule(page, '/purchasing/purchase-orders');
    await expect(page).toHaveURL(/\/purchasing\/purchase-orders(\?.*)?$/);
    await expect(page.locator('table')).toBeVisible();
  });
});
