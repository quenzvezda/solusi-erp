import { test as base } from '@playwright/test';
import { login } from '../helpers/auth';

export const test = base.extend({
  page: async ({ page }, use) => {
    await login(page);
    await use(page);
  },
});

export { expect } from '@playwright/test';
