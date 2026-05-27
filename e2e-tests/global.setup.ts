import { test as setup, request } from '@playwright/test';
import { TEST_USERS, loginAndSaveState, storageStatePath, RoleKey } from './helpers/auth';
import * as fs from 'fs';

/**
 * Validate that a saved storage state still authenticates against the live
 * server. Time-based freshness is NOT enough: H2 in-memory resets on every
 * server restart, invalidating all sessions immediately while the .auth/*.json
 * file on disk looks "fresh". Probe the server with the saved cookies and
 * accept only when an authenticated GET succeeds.
 *
 * Probe target: /dashboard (every authenticated user has DASHBOARD_READ).
 * Spring Security redirects unauthenticated requests to /login — so a probe
 * that lands on a /login URL means the saved session is dead.
 */
async function isStateValid(file: string, baseURL: string): Promise<boolean> {
  if (!fs.existsSync(file)) return false;
  let ctx;
  try {
    ctx = await request.newContext({ baseURL, storageState: file });
    const res = await ctx.get('/dashboard', { maxRedirects: 0 });
    const status = res.status();
    const location = res.headers()['location'] ?? '';
    // 200 OK = authenticated. 3xx redirect to /login (or /change-password) =
    // session dead.  Treat anything that is not a clean 2xx on /dashboard as
    // invalid.
    if (status >= 200 && status < 300) return true;
    if (status >= 300 && status < 400 && !/\/login(\?|$)/.test(location)) {
      // Some other redirect (e.g. force password change for admin) — not our
      // concern here; treat as invalid so we re-run the login flow which
      // already handles it.
      return false;
    }
    return false;
  } catch {
    return false;
  } finally {
    await ctx?.dispose();
  }
}

const ROLES: RoleKey[] = ['admin', 'approver1', 'employee1', 'warehouse1'];

for (const role of ROLES) {
  setup(`authenticate ${role}`, async ({ page, context, baseURL }) => {
    const file = storageStatePath(role);
    if (baseURL && (await isStateValid(file, baseURL))) {
      // eslint-disable-next-line no-console
      console.log(`[setup] reusing valid storage state for ${role}: ${file}`);
      return;
    }
    await loginAndSaveState(page, context, TEST_USERS[role]);
  });
}
