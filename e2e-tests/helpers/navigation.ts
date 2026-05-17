import { Page } from '@playwright/test';

export async function navigateToModule(page: Page, url: string): Promise<void> {
  await page.goto(url);
  await page.waitForLoadState('networkidle', { timeout: 15_000 });
}

export async function waitForPageReady(page: Page): Promise<void> {
  await page.waitForFunction(() => {
    return document.querySelectorAll('.htmx-request').length === 0;
  }, { timeout: 10_000 });
  await page.waitForLoadState('networkidle', { timeout: 10_000 });
}
