import { Page } from '@playwright/test';

/**
 * Set a date on a Flatpickr-managed input.
 *
 * `page.fill()` WILL NOT WORK reliably because Flatpickr usually marks the
 * underlying input readonly and manages display formatting itself. This helper
 * uses Flatpickr's JS API (`el._flatpickr.setDate(date, true)`) — the `true`
 * triggers onChange handlers so cascading behaviors (SPL price autofill, etc.)
 * fire as if a human picked the date.
 *
 * If the input is NOT Flatpickr-managed (no `_flatpickr` property after a short
 * wait), falls back to `page.fill()` so the helper also works for plain
 * `<input type="date">` fields.
 *
 * @param page - Playwright page
 * @param selector - CSS selector for the input element
 * @param dateStr - ISO date string (YYYY-MM-DD)
 */
export async function setFlatpickrDate(
  page: Page,
  selector: string,
  dateStr: string
): Promise<void> {
  // Brief wait — if Flatpickr ever attaches it should happen quickly.
  const isFlatpickr = await page
    .waitForFunction(
      (sel) => {
        const el = document.querySelector(sel) as any;
        return el?._flatpickr !== undefined;
      },
      selector,
      { timeout: 3_000 }
    )
    .then(() => true)
    .catch(() => false);

  if (isFlatpickr) {
    await page.evaluate(
      ({ sel, date }) => {
        const el = document.querySelector(sel) as any;
        el._flatpickr.setDate(date, true);
      },
      { sel: selector, date: dateStr }
    );
    return;
  }

  // Fallback: plain native date input.
  await page.fill(selector, dateStr);
}

/**
 * Read the current date from a Flatpickr input as ISO YYYY-MM-DD.
 * Returns empty string if no date set or input not initialized.
 */
export async function getFlatpickrDate(page: Page, selector: string): Promise<string> {
  return await page.evaluate((sel) => {
    const el = document.querySelector(sel) as any;
    if (!el) return '';
    if (el._flatpickr) {
      const d = el._flatpickr.selectedDates?.[0];
      if (!d) return '';
      const y = d.getFullYear();
      const m = String(d.getMonth() + 1).padStart(2, '0');
      const day = String(d.getDate()).padStart(2, '0');
      return `${y}-${m}-${day}`;
    }
    return el.value ?? '';
  }, selector);
}
