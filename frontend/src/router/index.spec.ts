import { createPinia, setActivePinia } from 'pinia';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { router } from './index';
import { usePopupStore } from '../stores/popup.store';

vi.mock('../services/auth.service', () => ({
  fetchSession: vi.fn().mockResolvedValue(null),
  registerAccount: vi.fn(),
  loginWithPassword: vi.fn(),
  loginWithGoogleAccessToken: vi.fn(),
  requestGoogleSignInAccessToken: vi.fn(),
  logoutSession: vi.fn()
}));

describe('router', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

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

  it('opens the login popup instead of a login page for watchlists', async () => {
    await router.push('/watchlists');
    const popup = usePopupStore();
    expect(router.currentRoute.value.path).toBe('/home');
    expect(popup.isLogin).toBe(true);
  });
});
