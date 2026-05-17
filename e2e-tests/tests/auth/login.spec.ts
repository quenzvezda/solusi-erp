import { test, expect } from '@playwright/test';

test.describe('Authentication', () => {

  test('should login successfully with valid credentials', async ({ page }) => {
    await page.goto('/login');
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
    await page.goto('/login');
    await page.fill('input[name="username"]', 'wrong_user');
    await page.fill('input[name="password"]', 'wrong_pass');
    await page.click('button[type="submit"]');

    // Should stay on login page
    await page.waitForURL('**/login**', { timeout: 5_000 });

    // Should show error message
    const errorVisible = await page.locator('.alert-danger, .alert-error, .invalid-feedback, [role="alert"]').isVisible();
    expect(errorVisible).toBe(true);
  });

  test('should redirect unauthenticated user to login', async ({ page }) => {
    // Try to access a protected page directly
    await page.goto('/dashboard');

    // Should be redirected to login
    await page.waitForURL('**/login**', { timeout: 5_000 });
    expect(page.url()).toContain('/login');
  });

});

