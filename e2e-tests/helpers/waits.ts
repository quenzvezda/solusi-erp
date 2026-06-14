import { Page } from '@playwright/test';

export async function waitForHtmx(page: Page): Promise<void> {
  await page.waitForFunction(() => {
    return document.querySelectorAll('.htmx-request').length === 0;
  }, { timeout: 15_000 });
}

export async function waitForNetworkIdle(page: Page): Promise<void> {
  await page.waitForLoadState('networkidle', { timeout: 15_000 }).catch(() => {});
}

export async function waitForToast(page: Page, type: 'success' | 'error' = 'success'): Promise<void> {
  const selector = type === 'success'
    ? '.toast.bg-success, .toast-success, .alert-success'
    : '.toast.bg-danger, .toast-error, .alert-danger';
  await page.waitForSelector(selector, { timeout: 10_000, state: 'visible' });
}

export async function waitForPageLoad(page: Page): Promise<void> {
  await page.waitForLoadState('domcontentloaded');
  await waitForHtmx(page);
}
