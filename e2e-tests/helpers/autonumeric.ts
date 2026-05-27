import { Page } from '@playwright/test';

/**
 * Set a value on an AutoNumeric input field.
 * page.fill() WILL NOT WORK correctly on AutoNumeric inputs.
 *
 * @param page - Playwright page
 * @param selector - CSS selector for the input element
 * @param value - numeric value to set
 */
export async function setAutoNumeric(
  page: Page,
  selector: string,
  value: number
): Promise<void> {
  // Wait for AutoNumeric to be initialized
  await page.waitForFunction(
    (sel) => {
      const el = document.querySelector(sel) as any;
      try {
        return (window as any).AutoNumeric.getAutoNumericElement(el) !== null;
      } catch {
        return false;
      }
    },
    selector,
    { timeout: 10_000 }
  );

  // Use AutoNumeric API to set value
  await page.evaluate(
    ({ sel, val }) => {
      const el = document.querySelector(sel) as any;
      const an = (window as any).AutoNumeric.getAutoNumericElement(el);
      an.set(val);
    },
    { sel: selector, val: value }
  );
}

/**
 * Get the raw numeric value from an AutoNumeric input.
 */
export async function getAutoNumericValue(page: Page, selector: string): Promise<number> {
  return await page.evaluate((sel) => {
    const el = document.querySelector(sel) as any;
    try {
      const an = (window as any).AutoNumeric.getAutoNumericElement(el);
      return an.getNumber();
    } catch {
      return 0;
    }
  }, selector);
}

/**
 * Clear an AutoNumeric input (set to 0 or empty).
 */
export async function clearAutoNumeric(page: Page, selector: string): Promise<void> {
  await page.evaluate((sel) => {
    const el = document.querySelector(sel) as any;
    try {
      const an = (window as any).AutoNumeric.getAutoNumericElement(el);
      an.set(0);
    } catch {
      // fallback
    }
  }, selector);
}
