import { test as base } from '@playwright/test';
import { login } from '../helpers/auth';

export const test = base.extend<{ authenticatedPage: void }>({
  authenticatedPage: [async ({ page }, use) => {
    await login(page);
    await use();
  }, { auto: true }],
});

export { expect } from '@playwright/test';
