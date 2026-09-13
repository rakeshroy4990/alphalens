import { describe, expect, it } from 'vitest';
import { router } from './index';

describe('router', () => {
  it('resolves / and /home to the home view', async () => {
    await router.push('/');
    expect(router.currentRoute.value.name).toBe('home');

    await router.push('/home');
    expect(router.currentRoute.value.name).toBe('home-path');
  });

  it('resolves a stock page by instrument id', async () => {
    await router.push('/stocks/12');
    expect(router.currentRoute.value.name).toBe('stock');
    expect(router.currentRoute.value.params.instrumentId).toBe('12');
  });
});
