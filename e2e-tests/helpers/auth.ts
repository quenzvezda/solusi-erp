import { Page } from '@playwright/test';

export const TEST_USERS = {
  admin: { username: 'admin', password: 'admin123' },
} as const;

export async function login(
  page: Page,
  username: string = TEST_USERS.admin.username,
  password: string = TEST_USERS.admin.password
): Promise<void> {
  await page.goto('/login');
  await page.fill('input[name="username"]', username);
  await page.fill('input[name="password"]', password);
  await page.click('button[type="submit"]');
  await page.waitForURL((url) => !url.pathname.includes('/login'), { timeout: 10_000 });
}
