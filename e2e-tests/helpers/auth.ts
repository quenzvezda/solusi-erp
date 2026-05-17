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

  // Handle forced password change page if it appears
  // SystemInitializer sets password_change_required=true for admin
  const currentUrl = page.url();
  if (currentUrl.includes('change-password') || currentUrl.includes('password')) {
    // Fill all password fields with the same password to bypass the change
    const passwordInputs = page.locator('input[type="password"]');
    const count = await passwordInputs.count();
    for (let i = 0; i < count; i++) {
      await passwordInputs.nth(i).fill(password);
    }
    await page.click('button[type="submit"]');
    await page.waitForURL((url) => !url.pathname.includes('password'), { timeout: 10_000 });
  }
}
