import { test as base, expect } from '@playwright/test';
import { storageStatePath } from '../helpers/auth';

/**
 * Default `test` is pre-authenticated as `admin` via storage state loaded at
 * the fixture level (lazily, after the setup project has produced the file).
 *
 * Project-level `storageState` was avoided because Playwright resolves project
 * options at worker boot — before `setup` writes the storage file on a cold
 * `.auth/`. Loading at fixture level reads the file on demand, after setup
 * dependencies have completed.
 *
 * For role-specific scenarios, override at the describe level:
 *
 *   import { test, expect } from '../../fixtures/base';
 *   import { storageStatePath } from '../../helpers/auth';
 *
 *   test.describe('Approval — approver path', () => {
 *     test.use({ storageState: storageStatePath('approver1') });
 *     test('...', async ({ page }) => { ... });
 *   });
 */
export const test = base.extend<{ storageState: string }>({
  storageState: storageStatePath('admin'),
});

export { expect };
export { storageStatePath };
