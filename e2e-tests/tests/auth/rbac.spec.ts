import { test, expect, storageStatePath } from '../../fixtures/base';

/**
 * RBAC sample matrix.
 *
 * Coverage: 4 roles x 4 resources = 16 list-URL cases plus per-case
 * Create-button visibility check on allow rows.
 *
 * Roles (storage states from global.setup.ts):
 *   - admin (ROLE_ADMIN): all permissions.
 *   - approver1 (ROLE_APPROVER): PR_READ/UPDATE, PO_READ/UPDATE, SPL_READ,
 *     LOOKUP_*. No SA, no Brand list (only LOOKUP_BRAND).
 *   - employee1 (ROLE_EMPLOYEE): PR_*, LOOKUP_*. No SA, no Brand list.
 *   - warehouse1 (ROLE_WAREHOUSE): STOCK-ADJUSTMENT_*, BRAND_READ,
 *     PRODUCT_READ, LOOKUP_*. No PR, no admin.
 *
 * Resources:
 *   - PR             /purchasing/purchase-requisitions   (PR_READ + PR_CREATE)
 *   - SA             /inventory/adjustments              (STOCK-ADJUSTMENT_READ + _CREATE)
 *   - Brand          /inventory/brands                   (BRAND_READ + BRAND_CREATE)
 *   - PermissionGrp  /security/permission-groups         (admin only)
 *
 * Deny behavior: Spring Security's default for missing authority on a
 * @PreAuthorize controller is a 403 page. Our LayoutController + GlobalErrorAdvice
 * may instead serve `templates/error/403.html` (status 403) or redirect to
 * the dashboard. Treat any of those as "deny" — final URL not on the listed page
 * AND status either 403 or a non-resource redirect.
 */

type Role = 'admin' | 'approver1' | 'employee1' | 'warehouse1';
type ResourceKey = 'pr' | 'sa' | 'brand' | 'permGroup';

interface Resource {
  key: ResourceKey;
  label: string;
  listUrl: string;
  createUrl: string | null;
  // Selector for the create button on the list page (when permission allows it).
  createBtnSelector: string;
}

const RESOURCES: Record<ResourceKey, Resource> = {
  pr: {
    key: 'pr',
    label: 'PurchaseRequisition',
    listUrl: '/purchasing/purchase-requisitions',
    createUrl: '/purchasing/purchase-requisitions/create',
    createBtnSelector: 'a[href="/purchasing/purchase-requisitions/create"]',
  },
  sa: {
    key: 'sa',
    label: 'StockAdjustment',
    listUrl: '/inventory/adjustments',
    createUrl: '/inventory/adjustments/create',
    createBtnSelector: 'a[href="/inventory/adjustments/create"]',
  },
  brand: {
    key: 'brand',
    label: 'Brand',
    listUrl: '/inventory/brands',
    createUrl: '/inventory/brands/create',
    createBtnSelector: 'a[href="/inventory/brands/create"]',
  },
  permGroup: {
    key: 'permGroup',
    label: 'PermissionGroup',
    listUrl: '/security/permission-groups',
    createUrl: null,
    createBtnSelector: '',
  },
};

interface Expectation {
  role: Role;
  resource: ResourceKey;
  // Outcome for the list URL.
  list: 'allow' | 'deny';
  // Whether the Create button should be visible on the list page when allow.
  // null = not applicable (deny case or resource has no create flow tested).
  createVisible: boolean | null;
}

const MATRIX: Expectation[] = [
  // admin — full access
  { role: 'admin', resource: 'pr', list: 'allow', createVisible: true },
  { role: 'admin', resource: 'sa', list: 'allow', createVisible: true },
  { role: 'admin', resource: 'brand', list: 'allow', createVisible: true },
  { role: 'admin', resource: 'permGroup', list: 'allow', createVisible: null },

  // approver1 — PR read-only; no SA, no Brand list, no admin
  { role: 'approver1', resource: 'pr', list: 'allow', createVisible: false },
  { role: 'approver1', resource: 'sa', list: 'deny', createVisible: null },
  { role: 'approver1', resource: 'brand', list: 'deny', createVisible: null },
  { role: 'approver1', resource: 'permGroup', list: 'deny', createVisible: null },

  // employee1 — full PR requester; no SA, no Brand list, no admin
  { role: 'employee1', resource: 'pr', list: 'allow', createVisible: true },
  { role: 'employee1', resource: 'sa', list: 'deny', createVisible: null },
  { role: 'employee1', resource: 'brand', list: 'deny', createVisible: null },
  { role: 'employee1', resource: 'permGroup', list: 'deny', createVisible: null },

  // warehouse1 — full SA; Brand read-only; no PR, no admin
  { role: 'warehouse1', resource: 'pr', list: 'deny', createVisible: null },
  { role: 'warehouse1', resource: 'sa', list: 'allow', createVisible: true },
  { role: 'warehouse1', resource: 'brand', list: 'allow', createVisible: false },
  { role: 'warehouse1', resource: 'permGroup', list: 'deny', createVisible: null },
];

/**
 * Land on a URL and classify the outcome as allow or deny.
 *
 * Allow = final URL still under the requested resource path AND HTTP status < 400.
 * Deny  = final URL is NOT the requested resource (e.g. /error/403, /dashboard,
 *         or /login) OR HTTP status is 4xx/5xx.
 */
async function classify(
  page: import('@playwright/test').Page,
  listUrl: string
): Promise<'allow' | 'deny'> {
  const res = await page.goto(listUrl, { waitUntil: 'domcontentloaded' });
  const status = res?.status() ?? 0;
  const finalPath = new URL(page.url()).pathname;
  const isOnResource = finalPath === listUrl || finalPath.startsWith(listUrl + '/') || finalPath.startsWith(listUrl + '?');
  if (status >= 400) return 'deny';
  if (!isOnResource) return 'deny';
  return 'allow';
}

test.describe('@rbac RBAC matrix (4 roles x 4 resources)', () => {
  for (const exp of MATRIX) {
    const resource = RESOURCES[exp.resource];
    const title = `${exp.role} :: ${resource.label} list -> ${exp.list}`;

    test(title, async ({ browser }) => {
      const ctx = await browser.newContext({ storageState: storageStatePath(exp.role) });
      const page = await ctx.newPage();
      try {
        const outcome = await classify(page, resource.listUrl);
        expect(outcome, `expected list ${exp.list} for ${exp.role} on ${resource.label}`).toBe(exp.list);

        // UI element visibility — only meaningful when the role landed on the
        // list page AND the resource has a create flow we want to assert.
        if (exp.list === 'allow' && exp.createVisible !== null && resource.createBtnSelector) {
          const btn = page.locator(resource.createBtnSelector).first();
          if (exp.createVisible) {
            await expect(btn, `Create button should be visible for ${exp.role} on ${resource.label}`).toBeVisible();
          } else {
            await expect(btn, `Create button should be hidden for ${exp.role} on ${resource.label}`).toHaveCount(0);
          }
        }
      } finally {
        await ctx.close();
      }
    });
  }
});
