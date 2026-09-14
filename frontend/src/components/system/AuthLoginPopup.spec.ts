import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import { createRouter, createWebHistory } from 'vue-router';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import AuthLoginPopup from './AuthLoginPopup.vue';
import { usePopupStore } from '../../stores/popup.store';

vi.mock('../../services/auth.service', () => ({
  fetchSession: vi.fn().mockResolvedValue(null),
  registerAccount: vi.fn(),
  loginWithPassword: vi.fn(),
  loginWithGoogleAccessToken: vi.fn(),
  requestGoogleSignInAccessToken: vi.fn(),
  logoutSession: vi.fn()
}));

describe('AuthLoginPopup', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  it('renders email, password, and Google sign-in without storing tokens', async () => {
    const pinia = createPinia();
    setActivePinia(pinia);
    usePopupStore().openLogin();
    const router = createRouter({
      history: createWebHistory(),
      routes: [{ path: '/', component: { template: '<div />' } }]
    });
    await router.push('/');
    const wrapper = mount(AuthLoginPopup, {
      global: { plugins: [pinia, router] }
    });
    expect(wrapper.text()).toContain('Login');
    expect(wrapper.text()).toContain('Sign In With Google');
    expect(wrapper.find('input[type="email"]').exists()).toBe(true);
    expect(wrapper.find('input[type="password"]').exists()).toBe(true);
    expect(wrapper.text()).not.toContain('localStorage');
  });
});
