import { BrowserContext, Page } from '@playwright/test';
import * as path from 'path';
import * as fs from 'fs';

export interface TestUser {
  username: string;
  password: string;
  role: string;
}

export const TEST_USERS = {
  admin:     { username: 'admin',     password: 'admin123', role: 'admin' },
  approver1: { username: 'approver1', password: 'admin123', role: 'approver1' },
  employee1: { username: 'employee1', password: 'admin123', role: 'employee1' },
  warehouse1:{ username: 'warehouse1',password: 'admin123', role: 'warehouse1' },
} as const satisfies Record<string, TestUser>;

export type RoleKey = keyof typeof TEST_USERS;

const STORAGE_DIR = path.join(__dirname, '..', '.auth');

export function storageStatePath(role: RoleKey): string {
  return path.join(STORAGE_DIR, `${role}.json`);
}

/**
 * Log in via the real /login form. Handles the password-change redirect
 * gracefully (E2E user accounts have password_change_required=false but the
 * fallback stays as a safety net).
 */
export async function login(
  page: Page,
  username: string = TEST_USERS.admin.username,
  password: string = TEST_USERS.admin.password
): Promise<void> {
  await page.goto('/login', { waitUntil: 'domcontentloaded', timeout: 30_000 });
  await page.fill('input[name="username"]', username);
  await page.fill('input[name="password"]', password);
  await Promise.all([
    page.waitForURL((url) => !url.pathname.includes('/login'), {
      timeout: 30_000,
      waitUntil: 'domcontentloaded',
    }),
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
      page.waitForURL((url) => !url.pathname.includes('password'), {
        timeout: 30_000,
        waitUntil: 'domcontentloaded',
      }),
      page.click('button[type="submit"]'),
    ]);
  }
}

/**
 * Login as the given user and persist the session cookies / storage to disk
 * so subsequent tests can use `test.use({ storageState })` instead of paying
 * the login cost per test.
 */
export async function loginAndSaveState(
  page: Page,
  context: BrowserContext,
  user: TestUser
): Promise<string> {
  await login(page, user.username, user.password);
  if (!fs.existsSync(STORAGE_DIR)) {
    fs.mkdirSync(STORAGE_DIR, { recursive: true });
  }
  const file = storageStatePath(user.role as RoleKey);
  await context.storageState({ path: file });
  return file;
}
