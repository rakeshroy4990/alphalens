import { mount } from '@vue/test-utils';
import { createRouter, createWebHistory } from 'vue-router';
import { describe, expect, it } from 'vitest';
import SiteHeader from './SiteHeader.vue';

describe('SiteHeader', () => {
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
      global: { plugins: [router] }
    });

    expect(wrapper.text()).toContain('AlphaLens');
    expect(wrapper.text()).toContain('Home');
    expect(wrapper.text()).toContain('Screener');
    expect(wrapper.text()).not.toContain('System status');
    expect(wrapper.get('nav').classes()).toContain('flex-row');
    expect(wrapper.get('nav').classes()).not.toContain('flex-col');
    expect(wrapper.find('button[aria-label="Open menu"]').exists()).toBe(false);
  });
});
