import { Page, expect } from '@playwright/test';

export async function fillField(page: Page, name: string, value: string): Promise<void> {
  const input = page.locator(`input[name="${name}"], textarea[name="${name}"]`);
  await input.clear();
  await input.fill(value);
}

export async function selectDropdown(page: Page, name: string, value: string): Promise<void> {
  await page.selectOption(`select[name="${name}"]`, value);
}

export async function submitAjaxForm(page: Page): Promise<void> {
  // Click the submit/save button
  const submitBtn = page.locator('button[type="submit"], button.btn-primary:has-text("Save"), button.btn-primary:has-text("Simpan")');
  await submitBtn.click();

  // Wait for AJAX response to complete
  await page.waitForResponse(
    (response) => response.request().method() === 'POST' && response.status() < 400,
    { timeout: 10_000 }
  );
}

export async function submitAndExpectRedirect(page: Page, urlPattern: string | RegExp): Promise<void> {
  const submitBtn = page.locator('button[type="submit"], button.btn-primary:has-text("Save"), button.btn-primary:has-text("Simpan")');
  await submitBtn.click();

  // AJAX form submission triggers a JS redirect on success
  await page.waitForURL(urlPattern, { timeout: 10_000 });
}

export async function expectFormError(page: Page, fieldName?: string): Promise<void> {
  if (fieldName) {
    const errorLocator = page.locator(
      `.invalid-feedback:near(input[name="${fieldName}"]), ` +
      `.invalid-feedback:near(select[name="${fieldName}"]), ` +
      `[data-field="${fieldName}"] .invalid-feedback`
    );
    await expect(errorLocator.first()).toBeVisible({ timeout: 5_000 });
  } else {
    // Any validation error visible
    const anyError = page.locator('.invalid-feedback, .alert-danger, .field-error');
    await expect(anyError.first()).toBeVisible({ timeout: 5_000 });
  }
}

export async function expectSuccessRedirect(page: Page, urlPattern: string | RegExp): Promise<void> {
  await page.waitForURL(urlPattern, { timeout: 10_000 });
}
