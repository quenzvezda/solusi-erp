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
    { timeout: 30_000 }
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
    { timeout: 30_000 }
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

/**
 * Wait until a TomSelect's options match a predicate (e.g., loaded count > 0
 * or contains a specific id). Useful after a parent change triggers AJAX
 * reload of a child select.
 */
export async function waitForTomSelectOptions(
  page: Page,
  selector: string,
  predicateFn: (optionsJson: string) => boolean,
  timeout: number = 30_000
): Promise<void> {
  await page.waitForFunction(
    ({ sel, predFnSrc }) => {
      const el = document.querySelector(sel) as any;
      if (!el?.tomselect) return false;
      const opts = Object.values(el.tomselect.options);
      // eslint-disable-next-line no-new-func
      const pred = new Function('optionsJson', `return (${predFnSrc})(optionsJson);`);
      return pred(JSON.stringify(opts));
    },
    { sel: selector, predFnSrc: predicateFn.toString() },
    { timeout }
  );
}

/**
 * Set parent TomSelect, wait for child TomSelect to reload its options
 * (cascading lookup such as Facility -> Grid -> Container), then set child.
 *
 * @param page - Playwright page
 * @param parentSelector - CSS selector of parent <select>
 * @param parentValue - id to set on parent
 * @param childSelector - CSS selector of child <select>
 * @param childValue - id to set on child after reload
 * @param childOptionLoadHint - optional search query passed to child's load(); empty = load all
 */
export async function setCascadingTomSelect(
  page: Page,
  parentSelector: string,
  parentValue: string | number,
  childSelector: string,
  childValue: string | number,
  childOptionLoadHint: string = ''
): Promise<void> {
  await setTomSelectValue(page, parentSelector, parentValue);

  // Trigger child reload by calling its load() — the page-specific JS may
  // already do this on parent change, but calling explicitly is idempotent
  // and removes timing dependency.
  await page.evaluate(
    ({ sel, hint }) => {
      const el = document.querySelector(sel) as any;
      if (!el?.tomselect) return;
      el.tomselect.clearOptions();
      el.tomselect.load(hint);
    },
    { sel: childSelector, hint: childOptionLoadHint }
  );

  // Wait until the desired option id is present in child options
  const targetIdStr = String(childValue);
  await waitForTomSelectOptions(
    page,
    childSelector,
    new Function('optionsJson', `
      const opts = JSON.parse(optionsJson);
      return opts.some((o) => String(o.id) === '${targetIdStr}');
    `) as any,
    30_000
  );

  await setTomSelectValue(page, childSelector, childValue);
}
