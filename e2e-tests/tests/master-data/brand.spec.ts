import { test, expect } from '../../fixtures/base';
import { fillField, submitAndExpectRedirect, expectFormError } from '../../helpers/form';
import { uniqueName } from '../../helpers/data-factory';
import { navigateToModule } from '../../helpers/navigation';

test.describe('Brand CRUD', () => {

  test('should display brand list page', async ({ page }) => {
    await navigateToModule(page, '/inventory/brands');
    await expect(page.locator('table')).toBeVisible();
    await expect(page.locator('table').locator('text=E2E Brand Beta').first()).toBeVisible();
  });

  test('should create new brand', async ({ page }) => {
    await navigateToModule(page, '/inventory/brands/create');

    const name = uniqueName('Brand');
    await fillField(page, 'name', name);

    await submitAndExpectRedirect(page, /\/inventory\/brands(\?.*)?$/);

    // Verify in list
    await expect(page.locator(`table`).locator(`text=${name}`).first()).toBeVisible();
  });

  test('should edit existing brand', async ({ page }) => {
    await navigateToModule(page, '/inventory/brands/edit/9001');

    const newName = uniqueName('Brand-Edit');
    await fillField(page, 'name', newName);

    await submitAndExpectRedirect(page, /\/inventory\/brands(\?.*)?$/);

    await expect(page.locator(`table`).locator(`text=${newName}`).first()).toBeVisible();
  });

  test('should show validation error for empty name', async ({ page }) => {
    await navigateToModule(page, '/inventory/brands/create');

    await fillField(page, 'name', '');

    const submitBtn = page.locator('[data-ajax-form] button[type="submit"]').first();
    await submitBtn.click();

    await expectFormError(page);
  });

});
