/** Browser calls must hit Spring under /api, never Vue routes like /screener. */
export function resolveApiBaseUrl(raw: string | undefined): string {
  const value = (raw ?? '/api').trim().replace(/\/+$/, '');
  if (value === '' || value === '/api' || value.endsWith('/api')) {
    return value === '' ? '/api' : value;
  }
  return `${value}/api`;
}
