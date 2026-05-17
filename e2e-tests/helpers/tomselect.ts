import { Page } from '@playwright/test';

/**
 * Select an option in a TomSelect field via its JavaScript API.
 * page.fill() WILL NOT WORK on TomSelect widgets.
 *
 * @param page - Playwright page
 * @param selector - CSS selector for the original <select> element (not the wrapper)
 * @param searchQuery - keyword to search (empty = load all)
 * @param optionIndex - index of option to select (0 = first)
 */
export async function selectTomSelect(
  page: Page,
  selector: string,
  searchQuery: string = '',
  optionIndex: number = 0
): Promise<{ id: string; name: string } | { error: string }> {
  // Wait for TomSelect to be initialized
  await page.waitForFunction(
    (sel) => {
      const el = document.querySelector(sel) as any;
      return el?.tomselect !== undefined;
    },
    selector,
    { timeout: 10_000 }
  );

  // Use TomSelect API to load and select
  const result = await page.evaluate(({ sel, query, idx }) => {
    return new Promise<{ id: string; name: string } | { error: string }>((resolve, reject) => {
      const el = document.querySelector(sel) as any;
      if (!el || !el.tomselect) {
        resolve({ error: `TomSelect not found on ${sel}` });
        return;
      }
      const ts = el.tomselect;
      ts.load(query, (options: any[]) => {
        if (options.length === 0) {
          resolve({ error: `No options found for query: ${query}` });
          return;
        }
        options.forEach((opt: any) => ts.addOption(opt));
        const target = options[Math.min(idx, options.length - 1)];
        ts.setValue(target.id);
        resolve({ id: String(target.id), name: target.name || target.text || '' });
      });
    });
  }, { sel: selector, query: searchQuery, idx: optionIndex });

  return result;
}

/**
 * Set TomSelect value by known ID (for seeded data).
 */
export async function setTomSelectValue(
  page: Page,
  selector: string,
  valueId: string | number,
  label?: string
): Promise<void> {
  await page.waitForFunction(
    (sel) => {
      const el = document.querySelector(sel) as any;
      return el?.tomselect !== undefined;
    },
    selector,
    { timeout: 10_000 }
  );

  await page.evaluate(({ sel, val, text }) => {
    const el = document.querySelector(sel) as any;
    const ts = el.tomselect;
    const value = String(val);
    if (!ts.options[value]) {
      const option = el.querySelector(`option[value="${CSS.escape(value)}"]`) as HTMLOptionElement | null;
      ts.addOption({ id: value, name: text || option?.textContent?.trim() || value, text: text || option?.textContent?.trim() || value });
    }
    ts.setValue(value);
    el.value = value;
    el.dispatchEvent(new Event('change', { bubbles: true }));
  }, { sel: selector, val: String(valueId), text: label });
}

/**
 * Get current TomSelect value.
 */
export async function getTomSelectValue(page: Page, selector: string): Promise<string> {
  return await page.evaluate((sel) => {
    const el = document.querySelector(sel) as any;
    return el?.tomselect?.getValue() || '';
  }, selector);
}

/**
 * Clear TomSelect selection.
 */
export async function clearTomSelect(page: Page, selector: string): Promise<void> {
  await page.evaluate((sel) => {
    const el = document.querySelector(sel) as any;
    if (el?.tomselect) {
      el.tomselect.clear();
    }
  }, selector);
}
