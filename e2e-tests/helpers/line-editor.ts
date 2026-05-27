import { Page, expect } from '@playwright/test';

/**
 * Helpers for header-lines forms following the standard Solusi ERP pattern:
 *   - `<button id="btn-add-line">` — appends a new row from `#row-template-source`.
 *   - `<tbody id="line-container">` — holds `tr.line-row` rows.
 *   - `.btn-remove-line` — per-row remove button.
 *   - Inputs named `lines[N].fieldName` where N is the row index.
 *
 * Used by PR, SPL, PO, etc. Encapsulating the pattern keeps spec code
 * focused on the business assertion rather than DOM plumbing.
 */

const LINE_ROW_SELECTOR = '#line-container tr.line-row';

/**
 * Click the add-line button and wait for the row count to increment.
 * Returns the new row count (which is also the index of the new row).
 */
export async function addLine(
  page: Page,
  addBtnSelector: string = '#btn-add-line'
): Promise<number> {
  const before = await getLineCount(page);
  await page.locator(addBtnSelector).click();
  await page.waitForFunction(
    ({ sel, prev }) => document.querySelectorAll(sel).length > prev,
    { sel: LINE_ROW_SELECTOR, prev: before },
    { timeout: 5_000 }
  );
  return before;
}

/**
 * Click the remove button on the row at the given index.
 * Waits for the row count to decrement.
 */
export async function removeLineAt(page: Page, index: number): Promise<void> {
  const before = await getLineCount(page);
  await page.locator(`${LINE_ROW_SELECTOR} >> nth=${index} >> .btn-remove-line`).click();
  await page.waitForFunction(
    ({ sel, prev }) => document.querySelectorAll(sel).length < prev,
    { sel: LINE_ROW_SELECTOR, prev: before },
    { timeout: 5_000 }
  );
}

/** Current number of line rows in the form. */
export async function getLineCount(page: Page): Promise<number> {
  return await page.locator(LINE_ROW_SELECTOR).count();
}

/**
 * Build a Playwright selector for an input bound to `lines[index].field`.
 * Brackets are not special in CSS attribute selector syntax — the form is
 * `[name="lines[0].quantity"]` which Playwright accepts as-is.
 */
export function lineFieldSelector(index: number, field: string): string {
  return `[name="lines[${index}].${field}"]`;
}

/**
 * Wait for a newly added row to be fully initialized: TomSelect cells must
 * have their underlying `<select>` ready (page-specific JS may attach them
 * asynchronously after the row is cloned from `#row-template-source`).
 */
export async function waitForRowSettled(
  page: Page,
  index: number,
  selectFields: string[] = []
): Promise<void> {
  await expect(page.locator(`${LINE_ROW_SELECTOR} >> nth=${index}`)).toBeVisible();
  for (const field of selectFields) {
    await page.waitForFunction(
      ({ idx, f }) => {
        const sel = document.querySelectorAll('#line-container tr.line-row')[idx];
        if (!sel) return false;
        const el = sel.querySelector(`[name="lines[${idx}].${f}"]`) as any;
        return el !== null;
      },
      { idx: index, f: field },
      { timeout: 5_000 }
    );
  }
}
