/** Capitalize the first character of a display label. Leaves the rest unchanged. */
export function capitalizeDisplay(value: string | null | undefined): string {
  if (value == null || value.length === 0) {
    return '';
  }
  return value.charAt(0).toUpperCase() + value.slice(1);
}
