import { Page } from '@playwright/test';

export async function navigateToModule(page: Page, url: string): Promise<void> {
  // Default waitUntil 'load' menunggu SEMUA resource (termasuk cdn.jsdelivr.net
  // dan rsms.me). Saat CDN slow, load event tidak fire meskipun halaman sudah
  // fully rendered & interaktif. domcontentloaded cukup karena setiap test
  // mengassert readiness elemennya sendiri (mis. expect(form).toBeVisible).
  await page.goto(url, { waitUntil: 'domcontentloaded', timeout: 60_000 });
  // networkidle adalah best-effort: kalau CDN tetap busy, jangan gagalkan
  // navigasi — test akan retry di assertion berikutnya.
  await page.waitForLoadState('networkidle', { timeout: 5_000 }).catch(() => {});
}

export async function waitForPageReady(page: Page): Promise<void> {
  await page.waitForFunction(() => {
    return document.querySelectorAll('.htmx-request').length === 0;
  }, { timeout: 10_000 });
  await page.waitForLoadState('networkidle', { timeout: 10_000 }).catch(() => {});
}
