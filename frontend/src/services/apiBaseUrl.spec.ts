import { describe, expect, it } from 'vitest';
import { resolveApiBaseUrl } from './apiBaseUrl';

describe('resolveApiBaseUrl', () => {
  it('defaults to /api', () => {
    expect(resolveApiBaseUrl(undefined)).toBe('/api');
  });

  it('keeps an explicit /api suffix', () => {
    expect(resolveApiBaseUrl('https://example.run.app/api')).toBe('https://example.run.app/api');
  });

  it('appends /api when only the Cloud Run origin is set', () => {
    expect(resolveApiBaseUrl('https://alphalens-113523778150.asia-south1.run.app')).toBe(
      'https://alphalens-113523778150.asia-south1.run.app/api'
    );
  });
});
