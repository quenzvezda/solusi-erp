import { test, expect } from '../../fixtures/base';
import { fillField, selectDropdown, submitAndExpectRedirect, expectFormError } from '../../helpers/form';
import { uniqueId, uniqueName } from '../../helpers/data-factory';
import { navigateToModule } from '../../helpers/navigation';

test.describe('Unit of Measure CRUD', () => {

  test('should display UoM list page', async ({ page }) => {
    await navigateToModule(page, '/inventory/unit-of-measures');
    await expect(page.locator('table')).toBeVisible();
    await expect(page.locator('table tbody tr').first()).toBeVisible();
  });

  test('should create new UoM', async ({ page }) => {
    await navigateToModule(page, '/inventory/unit-of-measures/create');

    const name = uniqueName('UoM');
    await fillField(page, 'code', uniqueId('UOM').toUpperCase().replace(/-/g, '').slice(0, 20));
    await fillField(page, 'name', name);
    await page.waitForSelector('select[name="type"]', { state: 'visible', timeout: 5_000 });
    await selectDropdown(page, 'type', 'UNIT');

    await submitAndExpectRedirect(page, /\/inventory\/unit-of-measures(\?.*)?$/);

    await expect(page.locator('table')).toBeVisible();
  });

  test('should edit existing UoM', async ({ page }) => {
    await navigateToModule(page, '/inventory/unit-of-measures/edit/9001');

    const newName = uniqueName('UoM-Edit');
    await fillField(page, 'name', newName);

    await submitAndExpectRedirect(page, /\/inventory\/unit-of-measures(\?.*)?$/);

    await expect(page.locator('table')).toBeVisible();
  });

  test('should show validation error for empty name', async ({ page }) => {
    await navigateToModule(page, '/inventory/unit-of-measures/create');

    await fillField(page, 'name', '');
    await selectDropdown(page, 'type', 'UNIT');

    const submitBtn = page.locator('[data-ajax-form] button[type="submit"]').first();
    await submitBtn.click();

    await expectFormError(page);
  });

});
