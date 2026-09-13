import { describe, expect, it } from 'vitest';
import { router } from './index';

describe('router', () => {
  it('redirects / to /home', async () => {
    await router.push('/');
    expect(router.currentRoute.value.path).toBe('/home');
    expect(router.currentRoute.value.name).toBe('home');
  });

  it('resolves a stock page by instrument id', async () => {
    await router.push('/stocks/12');
    expect(router.currentRoute.value.name).toBe('stock');
    expect(router.currentRoute.value.params.instrumentId).toBe('12');
  });
});
