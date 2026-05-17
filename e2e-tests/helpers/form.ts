import { Page, expect } from '@playwright/test';

export async function fillField(page: Page, name: string, value: string): Promise<void> {
  const input = page.locator(`input[name="${name}"], textarea[name="${name}"]`);
  await input.clear();
  await input.fill(value);
}

export async function selectDropdown(page: Page, name: string, value: string): Promise<void> {
  await page.selectOption(`select[name="${name}"]`, value);
}

export async function waitForAjaxFormReady(page: Page): Promise<void> {
  await page.waitForFunction(() => {
    const form = document.querySelector('form[data-ajax-form="true"]');
    return !!form && form.classList.contains('ajax-initialized');
  }, { timeout: 10_000 });
}

export async function submitAjaxForm(page: Page): Promise<void> {
  await waitForAjaxFormReady(page);
  const submitBtn = page.locator('[data-ajax-form] button[type="submit"]').first();
  await submitBtn.waitFor({ state: 'visible', timeout: 5_000 });
  await Promise.all([
    page.waitForResponse(
      (response) => response.request().method() === 'POST' && response.status() < 500,
      { timeout: 10_000 }
    ),
    submitBtn.click(),
  ]);
}

export async function submitAndExpectRedirect(page: Page, urlPattern: string | RegExp): Promise<void> {
  await waitForAjaxFormReady(page);
  const submitBtn = page.locator('[data-ajax-form] button[type="submit"]').first();
  await submitBtn.waitFor({ state: 'visible', timeout: 5_000 });
  await Promise.all([
    page.waitForURL(urlPattern, { timeout: 10_000 }),
    submitBtn.click(),
  ]);
}

export async function expectFormError(page: Page, fieldName?: string): Promise<void> {
  if (fieldName) {
    const errorLocator = page.locator(
      `.invalid-feedback:near(input[name="${fieldName}"]), ` +
      `.invalid-feedback:near(select[name="${fieldName}"]), ` +
      `[data-field="${fieldName}"] .invalid-feedback, ` +
      `input[name="${fieldName}"].is-invalid, ` +
      `select[name="${fieldName}"].is-invalid`
    );
    await expect(errorLocator.first()).toBeVisible({ timeout: 5_000 });
  } else {
    const anyError = page.locator(
      '.invalid-feedback, .alert-danger, .field-error, ' +
      '[role="alert"], input.is-invalid, select.is-invalid, textarea.is-invalid, .is-invalid-ts'
    );
    await expect(anyError.first()).toBeVisible({ timeout: 5_000 });
  }
}

export async function expectSuccessRedirect(page: Page, urlPattern: string | RegExp): Promise<void> {
  await page.waitForURL(urlPattern, { timeout: 10_000 });
}
