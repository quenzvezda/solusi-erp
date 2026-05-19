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
 * Draw a short signature on a canvas by issuing real pointer events.
 * The shape is a simple zig-zag at relative positions inside the canvas
 * bounding box, which is enough for signature_pad to register strokes.
 */
export async function drawSignature(page: Page, canvasSelector: string): Promise<void> {
  const canvas = page.locator(canvasSelector);
  await canvas.waitFor({ state: 'visible', timeout: 5_000 });

  const box = await canvas.boundingBox();
  if (!box) {
    throw new Error(`drawSignature: cannot read bounding box for ${canvasSelector}`);
  }

  // Anchor strokes at relative offsets so any canvas size produces a visible mark.
  const points = [
    { x: 0.15, y: 0.6 },
    { x: 0.35, y: 0.3 },
    { x: 0.55, y: 0.7 },
    { x: 0.75, y: 0.35 },
    { x: 0.9, y: 0.55 },
  ].map(p => ({ x: box.x + box.width * p.x, y: box.y + box.height * p.y }));

  await page.mouse.move(points[0].x, points[0].y);
  await page.mouse.down();
  for (let i = 1; i < points.length; i++) {
    // Multiple intermediate moves so signature_pad records a curve, not a jump.
    await page.mouse.move(points[i].x, points[i].y, { steps: 8 });
  }
  await page.mouse.up();
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
