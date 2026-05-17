let counter = 0;

export function uniqueId(prefix: string = 'e2e'): string {
  counter++;
  return `${prefix}-${Date.now()}-${counter}`;
}

export function uniqueName(entity: string): string {
  return `Test ${entity} ${uniqueId()}`;
}
