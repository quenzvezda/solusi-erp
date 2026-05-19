import { test as setup } from '@playwright/test';
import { TEST_USERS, loginAndSaveState, storageStatePath, RoleKey } from './helpers/auth';
import * as fs from 'fs';

const FRESH_TTL_MS = 30 * 60 * 1000; // 30 min — skip re-login if storage state is recent.

function isStateFresh(file: string): boolean {
  try {
    const stat = fs.statSync(file);
    return Date.now() - stat.mtimeMs < FRESH_TTL_MS;
  } catch {
    return false;
  }
}

const ROLES: RoleKey[] = ['admin', 'approver1', 'employee1', 'warehouse1'];

for (const role of ROLES) {
  setup(`authenticate ${role}`, async ({ page, context }) => {
    const file = storageStatePath(role);
    if (isStateFresh(file)) {
      // eslint-disable-next-line no-console
      console.log(`[setup] reusing fresh storage state for ${role}: ${file}`);
      return;
    }
    await loginAndSaveState(page, context, TEST_USERS[role]);
  });
}
