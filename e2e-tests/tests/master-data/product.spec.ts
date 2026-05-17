import { test, expect } from '../../fixtures/base';
import { fillField, selectDropdown, submitAndExpectRedirect, expectFormError } from '../../helpers/form';
import { uniqueName } from '../../helpers/data-factory';
import { navigateToModule } from '../../helpers/navigation';
import { selectTomSelect } from '../../helpers/tomselect';

test.describe('Product CRUD', () => {

  test('should display product list page', async ({ page }) => {
    await navigateToModule(page, '/inventory/products');
    await expect(page.locator('table')).toBeVisible();
  });

  test('should create new product with required fields', async ({ page }) => {
    await navigateToModule(page, '/inventory/products/create');

    const name = uniqueName('Product');
    await fillField(page, 'name', name);

    // Select category via TomSelect (required)
    await selectTomSelect(page, 'select[name="categoryId"]', '', 0);

    // Select UoM via standard dropdown (required)
    await page.waitForSelector('select[name="uomId"]', { state: 'visible', timeout: 5_000 });
    await selectDropdown(page, 'uomId', '9001');

    // Select brand via TomSelect (optional but testing it)
    await selectTomSelect(page, 'select[name="brandId"]', '', 0);

    await submitAndExpectRedirect(page, /\/inventory\/products(\?.*)?$/);

    // Verify redirect to list
    await expect(page.locator('table')).toBeVisible();
  });

  test('should show validation error for missing required fields', async ({ page }) => {
    await navigateToModule(page, '/inventory/products/create');

    // Submit without filling required fields
    const submitBtn = page.locator('[data-ajax-form] button[type="submit"]').first();
    await submitBtn.waitFor({ state: 'visible', timeout: 5_000 });
    await submitBtn.click();

    // Should show validation error
    await expectFormError(page);
  });

});
