import { test, expect } from '@playwright/test';

test.describe('@smoke Authentication', () => {

  test('should login successfully with valid credentials', async ({ page }) => {
    await page.goto('/login', { waitUntil: 'domcontentloaded' });
    await page.fill('input[name="username"]', 'admin');
    await page.fill('input[name="password"]', 'admin123');
    await page.click('button[type="submit"]');

    // Should redirect away from login page
    await page.waitForURL((url) => !url.pathname.includes('/login'), { timeout: 10_000 });

    // Verify we're on a protected page (dashboard or home)
    const url = page.url();
    expect(url).not.toContain('/login');
  });

  test('should show error with invalid credentials', async ({ page }) => {
    await page.goto('/login', { waitUntil: 'domcontentloaded' });
    await page.fill('input[name="username"]', 'wrong_user');
    await page.fill('input[name="password"]', 'wrong_pass');
    await page.click('button[type="submit"]');

    // Should stay on login page and render the server-side auth error.
    await expect(page).toHaveURL(/\/login(\?.*)?$/, { timeout: 10_000 });

    await expect(page.locator('.alert-danger, .alert-error, .invalid-feedback, [role="alert"]').first())
      .toBeVisible({ timeout: 10_000 });
  });

  test('should redirect unauthenticated user to login', async ({ page }) => {
    // Try to access a protected page directly
    await page.goto('/dashboard', { waitUntil: 'domcontentloaded' });

    // Should be redirected to login
    await expect(page).toHaveURL(/\/login(\?.*)?$/, { timeout: 10_000 });
  });

});

