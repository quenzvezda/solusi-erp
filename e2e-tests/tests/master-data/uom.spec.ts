import { test, expect } from '../../fixtures/base';
import { fillField, selectDropdown, submitAndExpectRedirect, expectFormError } from '../../helpers/form';
import { uniqueName } from '../../helpers/data-factory';
import { navigateToModule } from '../../helpers/navigation';

test.describe('Unit of Measure CRUD', () => {

  test('should display UoM list page', async ({ page }) => {
    await navigateToModule(page, '/inventory/unit-of-measures');
    await expect(page.locator('table')).toBeVisible();
    // Verify seeded data is present
    await expect(page.locator('table').locator('text=E2E Kilogram').first()).toBeVisible();
  });

  test('should create new UoM', async ({ page }) => {
    await navigateToModule(page, '/inventory/unit-of-measures/create');

    const name = uniqueName('UoM');
    await fillField(page, 'name', name);
    await selectDropdown(page, 'type', 'UNIT');

    await submitAndExpectRedirect(page, /\/inventory\/unit-of-measures(\?.*)?$/);

    // Verify in list
    await expect(page.locator('table').locator(`text=${name}`).first()).toBeVisible();
  });

  test('should edit existing UoM', async ({ page }) => {
    await navigateToModule(page, '/inventory/unit-of-measures/edit/9001');

    const newName = uniqueName('UoM-Edit');
    await fillField(page, 'name', newName);

    await submitAndExpectRedirect(page, /\/inventory\/unit-of-measures(\?.*)?$/);

    // Verify change in list
    await expect(page.locator('table').locator(`text=${newName}`).first()).toBeVisible();
  });

  test('should show validation error for empty name', async ({ page }) => {
    await navigateToModule(page, '/inventory/unit-of-measures/create');

    await fillField(page, 'name', '');
    await selectDropdown(page, 'type', 'UNIT');

    // Try to submit
    const submitBtn = page.locator('[data-ajax-form] button[type="submit"]').first();
    await submitBtn.click();

    // Should show error (stay on page, show validation message)
    await expectFormError(page);
  });

});
