import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import { createRouter, createWebHistory } from 'vue-router';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import SiteHeader from './SiteHeader.vue';
import { usePopupStore } from '../../stores/popup.store';

vi.mock('../../services/auth.service', () => ({
  fetchSession: vi.fn().mockResolvedValue(null),
  registerAccount: vi.fn(),
  loginWithPassword: vi.fn(),
  loginWithGoogleAccessToken: vi.fn(),
  requestGoogleSignInAccessToken: vi.fn(),
  logoutSession: vi.fn()
}));

describe('SiteHeader', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  it('renders brand and primary navigation', async () => {
    const router = createRouter({
      history: createWebHistory(),
      routes: [
        { path: '/', redirect: '/home' },
        { path: '/home', component: { template: '<div />' } },
        { path: '/screener', component: { template: '<div />' } }
      ]
    });
    await router.push('/');
    const wrapper = mount(SiteHeader, {
      global: { plugins: [createPinia(), router] }
    });

    expect(wrapper.text()).toContain('AlphaLens');
    expect(wrapper.text()).toContain('Home');
    expect(wrapper.text()).toContain('Screener');
    expect(wrapper.text()).toContain('Sign in');
    expect(wrapper.text()).not.toContain('System status');
    expect(wrapper.get('nav').classes()).toContain('flex-row');
    expect(wrapper.get('nav').classes()).not.toContain('flex-col');
    expect(wrapper.find('button[aria-label="Open menu"]').exists()).toBe(false);
  });

  it('opens the login popup from Sign in', async () => {
    const pinia = createPinia();
    setActivePinia(pinia);
    const router = createRouter({
      history: createWebHistory(),
      routes: [{ path: '/', component: { template: '<div />' } }]
    });
    await router.push('/');
    const wrapper = mount(SiteHeader, {
      global: { plugins: [pinia, router] }
    });
    await wrapper.get('button').trigger('click');
    expect(usePopupStore().isLogin).toBe(true);
  });
});
