import { test, expect } from '../../fixtures/base';
import { fillField, selectDropdown, submitAndExpectRedirect, expectFormError } from '../../helpers/form';
import { uniqueName } from '../../helpers/data-factory';
import { navigateToModule } from '../../helpers/navigation';

test.describe('Product Category CRUD', () => {

  test('should display category list page', async ({ page }) => {
    await navigateToModule(page, '/inventory/product-categories');
    await expect(page.locator('table')).toBeVisible();
    await expect(page.locator('table').locator('text=E2E Category Service').first()).toBeVisible();
  });

  test('should create new category', async ({ page }) => {
    await navigateToModule(page, '/inventory/product-categories/create');

    const name = uniqueName('Category');
    await fillField(page, 'name', name);
    await selectDropdown(page, 'type', 'STOCK');

    await submitAndExpectRedirect(page, /\/inventory\/product-categories(\?.*)?$/);

    // Verify in list
    await expect(page.locator('table').locator(`text=${name}`).first()).toBeVisible();
  });

  test('should edit existing category', async ({ page }) => {
    await navigateToModule(page, '/inventory/product-categories/edit/9001');

    const newName = uniqueName('Cat-Edit');
    await fillField(page, 'name', newName);

    await submitAndExpectRedirect(page, /\/inventory\/product-categories(\?.*)?$/);

    await expect(page.locator('table').locator(`text=${newName}`).first()).toBeVisible();
  });

  test('should show validation error for empty name', async ({ page }) => {
    await navigateToModule(page, '/inventory/product-categories/create');

    await fillField(page, 'name', '');
    await selectDropdown(page, 'type', 'STOCK');

    const submitBtn = page.locator('[data-ajax-form] button[type="submit"]').first();
    await submitBtn.click();

    await expectFormError(page);
  });

});
