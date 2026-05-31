import { Page } from '@playwright/test';
import { test, expect, storageStatePath } from '../../fixtures/base';
import { setAutoNumeric } from '../../helpers/autonumeric';
import { setFlatpickrDate } from '../../helpers/flatpickr';
import { navigateToModule } from '../../helpers/navigation';
import { setTomSelectValue } from '../../helpers/tomselect';
import { waitForNetworkIdle } from '../../helpers/waits';

const POSTING_DATE = '2026-05-20';
const CLOSED_PERIOD_DATE = '2026-06-01';
const DEBIT_ACCOUNT_ID = '9401';
const CREDIT_ACCOUNT_ID = '9405';
const AMOUNT = 125_000;

function lineSelector(rowNumber: number, suffix: string): string {
  return `#journal-lines-table tbody tr:nth-of-type(${rowNumber}) [name="lines[${rowNumber - 1}].${suffix}"]`;
}

async function dispatchInput(page: Page, selector: string): Promise<void> {
  await page.locator(selector).dispatchEvent('input');
  await page.locator(selector).dispatchEvent('change');
}

async function newestJournalIdFromList(page: Page): Promise<number> {
  await waitForNetworkIdle(page);
  return page.evaluate(() => {
    const links = Array.from(document.querySelectorAll('a[href*="/accounting/journal-entries/"]'));
    let max = 0;
    for (const link of links) {
      const match = (link as HTMLAnchorElement).href.match(/\/accounting\/journal-entries\/(\d+)/);
      if (match) max = Math.max(max, Number(match[1]));
    }
    return max;
  });
}

async function createDraftManualJournal(page: Page, referenceNo: string): Promise<number> {
  await navigateToModule(page, '/accounting/journal-entries/create');
  await expect(page.locator('#journal-entry-form')).toBeVisible({ timeout: 10_000 });

  await expect(page.locator('#currencyId')).toHaveValue(/\d+/, { timeout: 10_000 });
  await expect(page.locator('input[name="exchangeRate"]')).toHaveJSProperty('readOnly', true, {
    timeout: 10_000,
  });
  await expect(page.locator('input[name="exchangeRate"]')).toHaveValue(/1(\.00)?/);

  await setFlatpickrDate(page, 'input[name="postingDate"]', POSTING_DATE);
  await page.locator('input[name="referenceNo"]').fill(referenceNo);
  await page.locator('textarea[name="description"]').fill('E2E manual journal draft');

  await expect(page.locator('#journal-lines-table tbody tr')).toHaveCount(2, { timeout: 10_000 });
  await setTomSelectValue(page, lineSelector(1, 'accountId'), DEBIT_ACCOUNT_ID, 'E2E Inventory');
  await setTomSelectValue(page, lineSelector(2, 'accountId'), CREDIT_ACCOUNT_ID, 'E2E Bank');
  await setAutoNumeric(page, lineSelector(1, 'debitAmount'), AMOUNT);
  await setAutoNumeric(page, lineSelector(2, 'creditAmount'), AMOUNT);
  await dispatchInput(page, lineSelector(1, 'debitAmount'));
  await dispatchInput(page, lineSelector(2, 'creditAmount'));
  await page.locator(lineSelector(1, 'description')).fill('Debit memo');
  await page.locator(lineSelector(2, 'description')).fill('Credit memo');

  await expect(page.locator('#journal-balance-badge')).toContainText('Balanced');

  await Promise.all([
    page.waitForURL(/\/accounting\/journal-entries(\?.*)?$/, {
      timeout: 20_000,
      waitUntil: 'domcontentloaded',
    }),
    page.locator('#journal-entry-form button[type="submit"]').click(),
  ]);

  const id = await newestJournalIdFromList(page);
  expect(id, 'new manual journal should be present in the list').toBeGreaterThan(0);
  return id;
}

test.describe('@accounting Manual Journal Entry flow', () => {
  test.describe.configure({ mode: 'serial', timeout: 120_000 });
  test.use({ storageState: storageStatePath('admin') });

  test('manual journal create edit post and reverse happy path', async ({ page }) => {
    const reference = `E2E-MJ-${Date.now()}`;
    const editedReference = `${reference}-EDIT`;
    const originalId = await createDraftManualJournal(page, reference);

    await navigateToModule(page, `/accounting/journal-entries/${originalId}`);
    await expect(page.locator('.badge', { hasText: 'DRAFT' })).toBeVisible({ timeout: 10_000 });
    await expect(page.locator('body')).toContainText(reference);

    await navigateToModule(page, `/accounting/journal-entries/edit/${originalId}`);
    await expect(page.locator('#journal-entry-form')).toBeVisible({ timeout: 10_000 });
    await page.locator('input[name="referenceNo"]').fill(editedReference);
    await page.locator(lineSelector(1, 'description')).fill('Edited debit memo');

    await Promise.all([
      page.waitForURL(/\/accounting\/journal-entries(\?.*)?$/, {
        timeout: 20_000,
        waitUntil: 'domcontentloaded',
      }),
      page.locator('#journal-entry-form button[type="submit"]').click(),
    ]);

    await navigateToModule(page, `/accounting/journal-entries/${originalId}`);
    await expect(page.locator('body')).toContainText(editedReference);
    await expect(page.locator('body')).toContainText('Edited debit memo');

    await page.locator('#btn-post-journal').click();
    await page.locator('#confirm-modal-btn-yes').click();
    await page.waitForLoadState('domcontentloaded', { timeout: 20_000 }).catch(() => {});
    await expect(page.locator('.badge', { hasText: 'POSTED' })).toBeVisible({ timeout: 10_000 });
    await expect(page.locator('#btn-reverse-journal')).toBeVisible({ timeout: 10_000 });

    await page.locator('#btn-reverse-journal').click();
    await expect(page.locator('#journal-reversal-modal.show')).toBeVisible({ timeout: 10_000 });
    await setFlatpickrDate(page, '#reversal-posting-date', CLOSED_PERIOD_DATE);
    await page.locator('#btn-confirm-reversal').click();
    await expect(page.locator('#journal-reversal-error')).toBeVisible({ timeout: 10_000 });
    await expect(page.locator('#journal-reversal-error')).toContainText(/No open accounting period|Tidak ada periode akuntansi terbuka/);
    await expect(page.locator('#journal-reversal-modal.show')).toBeVisible();

    await setFlatpickrDate(page, '#reversal-posting-date', POSTING_DATE);
    await Promise.all([
      page.waitForURL(
        (url) =>
          /\/accounting\/journal-entries\/\d+$/.test(url.pathname) &&
          !url.pathname.endsWith(`/${originalId}`),
        {
        timeout: 20_000,
        waitUntil: 'domcontentloaded',
        }
      ),
      page.locator('#btn-confirm-reversal').click(),
    ]);

    const reversalUrl = page.url();
    const reversalId = Number(reversalUrl.match(/\/accounting\/journal-entries\/(\d+)$/)?.[1] ?? 0);
    expect(reversalId, 'reversal should redirect to a new journal id').toBeGreaterThan(originalId);
    await expect(page.locator('.badge', { hasText: 'POSTED' })).toBeVisible({ timeout: 10_000 });
    await expect(page.locator('body')).toContainText(`JNL-${String(originalId).padStart(6, '0')}`);

    const reversalAmounts = await page.locator('tbody tr').evaluateAll((rows) =>
      rows.map((row) => Array.from(row.querySelectorAll('td')).map((td) => td.textContent?.trim() ?? ''))
    );
    expect(reversalAmounts[0][1]).toMatch(/^0\.00$/);
    expect(reversalAmounts[0][2].replace(/,/g, '')).toMatch(/^125000\.00$/);

    await navigateToModule(page, `/accounting/journal-entries/${originalId}`);
    await expect(page.locator('body')).toContainText(`JNL-${String(reversalId).padStart(6, '0')}`);
    await expect(page.locator('#btn-reverse-journal')).toHaveCount(0);
  });
});
