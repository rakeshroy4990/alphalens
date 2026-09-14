import { createPinia, setActivePinia } from 'pinia';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { useAuthStore } from './auth.store';
import { usePopupStore } from './popup.store';

vi.mock('../services/auth.service', () => ({
  fetchSession: vi.fn().mockResolvedValue(null),
  registerAccount: vi.fn().mockResolvedValue({
    userId: 'u1',
    email: 'a@example.com',
    displayName: 'A',
    authProvider: 'EMAIL',
    status: 'ACTIVE'
  }),
  loginWithPassword: vi.fn().mockResolvedValue({
    userId: 'u1',
    email: 'a@example.com',
    displayName: 'A',
    authProvider: 'EMAIL',
    status: 'ACTIVE'
  }),
  loginWithGoogleAccessToken: vi.fn(),
  requestGoogleSignInAccessToken: vi.fn(),
  logoutSession: vi.fn().mockResolvedValue(undefined)
}));

describe('auth store', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  it('starts signed out until bootstrap finds a session', async () => {
    const auth = useAuthStore();
    expect(auth.isAuthenticated).toBe(false);
    await auth.bootstrap();
    expect(auth.bootstrapped).toBe(true);
    expect(auth.isAuthenticated).toBe(false);
  });

  it('stores the user after password login', async () => {
    const auth = useAuthStore();
    auth.setIdentity('a@example.com');
    auth.setPassword('CorrectHorse1!');
    await auth.login();
    expect(auth.isAuthenticated).toBe(true);
    expect(auth.user?.email).toBe('a@example.com');
  });

  it('opens the login popup without creating a session on register', async () => {
    const auth = useAuthStore();
    const popup = usePopupStore();
    auth.setRegisterEmail('a@example.com');
    auth.setRegisterPassword('CorrectHorse1!');
    await auth.register();
    expect(auth.isAuthenticated).toBe(false);
    auth.openLoginAfterRegister();
    expect(popup.isLogin).toBe(true);
    expect(auth.identity).toBe('a@example.com');
    expect(auth.loginInfoMessage).toContain('successfully registered');
  });
});
