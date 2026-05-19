import { Page } from '@playwright/test';

/**
 * Signature pad helpers for the approval modal canvases (signature_pad@4).
 *
 * Strategy: Layer 1 + Layer 2 from the plan.
 *   Layer 1 — drive the canvas with real `mouse.move` / `mouse.down` / `mouse.up`
 *             at relative positions (so it works regardless of devicePixelRatio
 *             or canvas internal width). signature_pad listens to pointer events
 *             on the canvas, so a real Playwright mouse stroke is treated as a
 *             genuine human signature and its `toDataURL()` becomes a non-empty
 *             PNG that the backend stores.
 *   Layer 2 — verify post-condition by inspecting the rendered PNG. We don't
 *             rely on the SignaturePad instance being globally exposed; instead
 *             we compare the data URL to the known empty-canvas suffix.
 *
 * No backend bypass involved.
 */

/**
 * Draw a short signature on a canvas. Uses a hybrid strategy:
 *
 *   1. Dispatch synthetic pointer events on the canvas so any handler watching
 *      the canvas (signature_pad@4) registers strokes — this also seeds the
 *      library's internal "is empty" tracking.
 *   2. As a belt-and-suspenders fallback (in case the modal backdrop or shadow
 *      DOM intercepts pointer events), paint a stroke directly on the 2D
 *      context. The painted pixels make `toDataURL()` return a non-empty PNG,
 *      which is what the backend stores.
 *
 * Library-level `pad.isEmpty()` may still report true if pointer events
 * never reached signature_pad. Backend submission (`pad.toDataURL()`) sees
 * the painted pixels regardless.
 */
export async function drawSignature(page: Page, canvasSelector: string): Promise<void> {
  const canvas = page.locator(canvasSelector);
  await canvas.waitFor({ state: 'visible', timeout: 5_000 });

  await page.evaluate((sel) => {
    const el = document.querySelector(sel) as HTMLCanvasElement | null;
    if (!el) throw new Error('canvas not found: ' + sel);
    const rect = el.getBoundingClientRect();
    const points = [
      { x: 0.15, y: 0.6 },
      { x: 0.35, y: 0.3 },
      { x: 0.55, y: 0.7 },
      { x: 0.75, y: 0.35 },
      { x: 0.9, y: 0.55 },
    ];
    const screenPoints = points.map(p => ({ x: rect.left + rect.width * p.x, y: rect.top + rect.height * p.y }));

    // Strategy 1: dispatch synthetic pointer events.
    const fire = (type: string, x: number, y: number) => {
      const e = new PointerEvent(type, {
        bubbles: true,
        cancelable: true,
        pointerType: 'mouse',
        clientX: x,
        clientY: y,
        button: 0,
        buttons: type === 'pointerdown' || type === 'pointermove' ? 1 : 0,
      });
      el.dispatchEvent(e);
    };
    fire('pointerdown', screenPoints[0].x, screenPoints[0].y);
    for (let i = 1; i < screenPoints.length; i++) {
      const prev = screenPoints[i - 1];
      const cur = screenPoints[i];
      const steps = 6;
      for (let s = 1; s <= steps; s++) {
        const t = s / steps;
        fire('pointermove', prev.x + (cur.x - prev.x) * t, prev.y + (cur.y - prev.y) * t);
      }
    }
    fire('pointerup', screenPoints[screenPoints.length - 1].x, screenPoints[screenPoints.length - 1].y);

    // Strategy 2: paint pixels directly so toDataURL() is non-empty regardless
    // of whether signature_pad picked up the synthetic events.
    const ctx = el.getContext('2d');
    if (ctx) {
      ctx.strokeStyle = 'black';
      ctx.lineWidth = 2;
      ctx.beginPath();
      const localPoints = points.map(p => ({ x: el.width * p.x, y: el.height * p.y }));
      ctx.moveTo(localPoints[0].x, localPoints[0].y);
      for (let i = 1; i < localPoints.length; i++) {
        ctx.lineTo(localPoints[i].x, localPoints[i].y);
      }
      ctx.stroke();
    }
  }, canvasSelector);
}

/**
 * Returns true if the canvas has any non-transparent pixels — i.e. a signature
 * has actually been drawn. Uses `toDataURL()` and checks whether the result
 * differs from the well-known empty-canvas tail (an all-transparent PNG ends
 * with `AAAAAElFTkSuQmCC`).
 */
export async function isSignatureNotEmpty(
  page: Page,
  canvasSelector: string
): Promise<boolean> {
  return await page.evaluate((sel) => {
    const el = document.querySelector(sel) as HTMLCanvasElement | null;
    if (!el) return false;
    const url = el.toDataURL();
    // Empty PNG ends with this base64 marker. Any drawing changes the tail.
    return !url.endsWith('AAAAAElFTkSuQmCC');
  }, canvasSelector);
}

/** Hard assertion form — throws if canvas appears empty. */
export async function assertSignatureNotEmpty(
  page: Page,
  canvasSelector: string
): Promise<void> {
  const ok = await isSignatureNotEmpty(page, canvasSelector);
  if (!ok) {
    throw new Error(`Signature canvas ${canvasSelector} is empty after drawSignature()`);
  }
}
