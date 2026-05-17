import { Page } from '@playwright/test';

export const TEST_USERS = {
  admin: { username: 'admin', password: 'admin123' },
} as const;

export async function login(
  page: Page,
  username: string = TEST_USERS.admin.username,
  password: string = TEST_USERS.admin.password
): Promise<void> {
  await page.goto('/login', { waitUntil: 'domcontentloaded', timeout: 30_000 });
  await page.fill('input[name="username"]', username);
  await page.fill('input[name="password"]', password);
  await Promise.all([
    page.waitForURL((url) => !url.pathname.includes('/login'), { timeout: 30_000 }),
    page.click('button[type="submit"]'),
  ]);

  const currentUrl = page.url();
  if (currentUrl.includes('change-password') || currentUrl.includes('password')) {
    const passwordInputs = page.locator('input[type="password"]');
    const count = await passwordInputs.count();
    for (let i = 0; i < count; i++) {
      await passwordInputs.nth(i).fill(password);
    }
    await Promise.all([
      page.waitForURL((url) => !url.pathname.includes('password'), { timeout: 30_000 }),
      page.click('button[type="submit"]'),
    ]);
  }
}
