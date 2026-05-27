import { test, expect } from '../../fixtures/base';
import { fillField, selectDropdown, submitAndExpectRedirect, expectFormError } from '../../helpers/form';
import { uniqueName } from '../../helpers/data-factory';
import { navigateToModule } from '../../helpers/navigation';
import { setTomSelectValue } from '../../helpers/tomselect';

test.describe('Product CRUD', () => {

  test('should display product list page', async ({ page }) => {
    await navigateToModule(page, '/inventory/products');
    await expect(page.locator('table')).toBeVisible();
  });

  test('@smoke should create new product with required fields', async ({ page }) => {
    await navigateToModule(page, '/inventory/products/create');

    const name = uniqueName('Product');
    await fillField(page, 'name', name);

    await setTomSelectValue(page, '#category-select', '9001');

    await page.waitForSelector('select[name="uomId"]', { state: 'visible', timeout: 5_000 });
    await selectDropdown(page, 'uomId', '9001');

    await setTomSelectValue(page, '#brand-select', '9001');

    await submitAndExpectRedirect(page, /\/inventory\/products(\?.*)?$/);

    await expect(page.locator('table')).toBeVisible();
  });

  test('should show validation error for missing required fields', async ({ page }) => {
    await navigateToModule(page, '/inventory/products/create');

    const submitBtn = page.locator('[data-ajax-form] button[type="submit"]').first();
    await submitBtn.waitFor({ state: 'visible', timeout: 5_000 });
    await submitBtn.click();

    await expectFormError(page);
  });

});
